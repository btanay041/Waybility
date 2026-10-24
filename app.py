import io
import os
import time

import torch
import torch.nn as nn
import torch.nn.functional as F
from PIL import Image
from fastapi import FastAPI, File, UploadFile, HTTPException
from fastapi.responses import JSONResponse
from torchvision import models, transforms

MODEL_DIR = os.path.join(BASE_DIR, "models")
LOCALIZATION_MODEL_PATH = os.path.join(MODEL_DIR, "best_heatmap_localization_model.pth")
CLASSIFIER_MODEL_PATH = os.path.join(MODEL_DIR, "best_localized_model.pth")

CLASSES = ["Crosswalk", "CurbRamp", "Obstacle", "SurfaceProblem"]
IMAGE_SIZE = 224
HEATMAP_SIZE = 28
CROP_SIZE = 480
DEVICE = torch.device("cuda" if torch.cuda.is_available() else "cpu")

imagenet_normalize = transforms.Normalize(
    mean=[0.485, 0.456, 0.406],
    std=[0.229, 0.224, 0.225]
)

localization_transform = transforms.Compose([
    transforms.Resize((IMAGE_SIZE, IMAGE_SIZE)),
    transforms.ToTensor(),
    imagenet_normalize
])

classifier_transform = transforms.Compose([
    transforms.Resize((IMAGE_SIZE, IMAGE_SIZE)),
    transforms.ToTensor(),
    imagenet_normalize
])

app = FastAPI()


class HeatmapLocalizationModel(nn.Module):
    def __init__(self):
        super().__init__()
        backbone = models.efficientnet_b0(weights=None)
        self.features = backbone.features
        self.head = nn.Sequential(
            nn.Conv2d(1280, 512, kernel_size=3, padding=1),
            nn.BatchNorm2d(512),
            nn.ReLU(inplace=True),
            nn.Conv2d(512, 256, kernel_size=3, padding=1),
            nn.BatchNorm2d(256),
            nn.ReLU(inplace=True),
            nn.Conv2d(256, 128, kernel_size=3, padding=1),
            nn.BatchNorm2d(128),
            nn.ReLU(inplace=True),
            nn.Conv2d(128, 1, kernel_size=1)
        )

    def forward(self, x):
        x = self.features(x)
        x = self.head(x)
        return F.interpolate(
            x,
            size=(HEATMAP_SIZE, HEATMAP_SIZE),
            mode="bilinear",
            align_corners=False
        )


def load_checkpoint_state(path):
    checkpoint = torch.load(
        path,
        map_location=DEVICE,
        weights_only=False
    )

    if isinstance(checkpoint, dict) and "model_state_dict" in checkpoint:
        return checkpoint["model_state_dict"]

    if isinstance(checkpoint, dict) and "state_dict" in checkpoint:
        return checkpoint["state_dict"]

    return checkpoint


def load_models():
    for model_path in (
        LOCALIZATION_MODEL_PATH,
        CLASSIFIER_MODEL_PATH
    ):
        if not os.path.isfile(model_path):
            raise FileNotFoundError(
                f"Model file not found: {model_path}"
            )

    localization_model = HeatmapLocalizationModel()
    localization_model.load_state_dict(
        load_checkpoint_state(LOCALIZATION_MODEL_PATH)
    )
    localization_model.to(DEVICE).eval()

    classifier_model = models.efficientnet_b0(weights=None)
    classifier_model.classifier[1] = nn.Linear(
        classifier_model.classifier[1].in_features,
        len(CLASSES)
    )
    classifier_model.load_state_dict(
        load_checkpoint_state(CLASSIFIER_MODEL_PATH)
    )
    classifier_model.to(DEVICE).eval()

    return localization_model, classifier_model


localization_model, classifier_model = load_models()


def heatmap_to_xy(heatmaps):
    batch_size = heatmaps.shape[0]
    flat = heatmaps.reshape(batch_size, -1)
    indices = torch.argmax(flat, dim=1)
    ys = indices // HEATMAP_SIZE
    xs = indices % HEATMAP_SIZE

    x = xs.float() / (HEATMAP_SIZE - 1)
    y = ys.float() / (HEATMAP_SIZE - 1)

    return torch.stack([x, y], dim=1)


def crop_around_point(
    image,
    normalized_x,
    normalized_y,
    crop_size=CROP_SIZE
):
    width, height = image.size
    center_x = normalized_x * width
    center_y = normalized_y * height
    half = crop_size // 2

    left = int(center_x - half)
    top = int(center_y - half)
    right = left + crop_size
    bottom = top + crop_size

    if left < 0:
        right -= left
        left = 0

    if top < 0:
        bottom -= top
        top = 0

    if right > width:
        left -= right - width
        right = width

    if bottom > height:
        top -= bottom - height
        bottom = height

    left = max(0, left)
    top = max(0, top)
    right = min(width, right)
    bottom = min(height, bottom)

    crop = image.crop((left, top, right, bottom))

    if crop.size != (crop_size, crop_size):
        padded = Image.new(
            "RGB",
            (crop_size, crop_size),
            (0, 0, 0)
        )
        padded.paste(crop, (0, 0))
        crop = padded

    return crop, [left, top, right, bottom]


@app.get("/")
def root():
    return {
        "status": "running",
        "device": str(DEVICE),
        "pipeline": [
            "heatmap localization",
            "localized crop classification"
        ]
    }


@app.post("/predict")
async def predict(file: UploadFile = File(...)):
    started = time.time()

    if file.content_type and not file.content_type.startswith("image/"):
        raise HTTPException(
            status_code=400,
            detail="Upload an image file."
        )

    try:
        image_bytes = await file.read()
        image = Image.open(
            io.BytesIO(image_bytes)
        ).convert("RGB")
    except Exception:
        raise HTTPException(
            status_code=400,
            detail="The uploaded file could not be read as an image."
        )

    width, height = image.size

    print("[1/2] Running heatmap localization model...")

    localization_input = localization_transform(
        image
    ).unsqueeze(0).to(DEVICE)

    with torch.no_grad():
        heatmap = localization_model(localization_input)
        predicted_xy = heatmap_to_xy(heatmap)[0].cpu().tolist()

    normalized_x = float(predicted_xy[0])
    normalized_y = float(predicted_xy[1])

    center_x = normalized_x * width
    center_y = normalized_y * height

    print(
        f"Localization complete: "
        f"normalized=({normalized_x:.3f}, {normalized_y:.3f}), "
        f"pixels=({center_x:.1f}, {center_y:.1f})"
    )

    localized_crop, crop_box = crop_around_point(
        image,
        normalized_x,
        normalized_y
    )

    print("[2/2] Running localized classifier on the predicted crop...")

    classifier_input = classifier_transform(
        localized_crop
    ).unsqueeze(0).to(DEVICE)

    with torch.no_grad():
        logits = classifier_model(classifier_input)
        probabilities = torch.softmax(logits, dim=1)[0]
        confidence, predicted_index = torch.max(
            probabilities,
            dim=0
        )

    prediction = CLASSES[int(predicted_index.item())]

    probability_map = {
        CLASSES[i]: round(float(probabilities[i].item()), 6)
        for i in range(len(CLASSES))
    }

    elapsed = round(time.time() - started, 3)

    print(
        f"Classification complete: "
        f"{prediction} ({confidence.item() * 100:.1f}%)"
    )
    print(f"Pipeline completed in {elapsed}s")

    return JSONResponse({
        "prediction": prediction,
        "confidence": round(float(confidence.item()), 6),
        "probabilities": probability_map,
        "localization": {
            "x": round(center_x, 2),
            "y": round(center_y, 2),
            "normalized_x": round(normalized_x, 6),
            "normalized_y": round(normalized_y, 6),
            "image_width": width,
            "image_height": height,
            "crop_box": crop_box,
            "crop_size": CROP_SIZE
        },
        "stages": {
            "localization": "completed",
            "localized_classification": "completed"
        },
        "processing_seconds": elapsed
    })