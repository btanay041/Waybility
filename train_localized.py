from pathlib import Path
import random
import numpy as np
import pandas as pd
from PIL import Image
print("starting")
import torch
import torch.nn as nn
from torch.utils.data import Dataset, DataLoader, WeightedRandomSampler
from torchvision import transforms, models

from sklearn.model_selection import train_test_split
from sklearn.metrics import accuracy_score, f1_score, classification_report, confusion_matrix




MODEL_DIR.mkdir(parents=True, exist_ok=True)

MODEL_PATH = MODEL_DIR / "best_localized_model.pth"

CLASSES = {
    "Crosswalk": 0,
    "CurbRamp": 1,
    "Obstacle": 2,
    "SurfaceProblem": 3
}

IDX_TO_CLASS = {v: k for k, v in CLASSES.items()}

CROP_SIZE = 480

IMAGE_SIZE = 224

BATCH_SIZE = 32
EPOCHS = 10

LEARNING_RATE = 1e-4
WEIGHT_DECAY = 1e-4

RANDOM_STATE = 42




random.seed(RANDOM_STATE)
np.random.seed(RANDOM_STATE)
torch.manual_seed(RANDOM_STATE)

if torch.cuda.is_available():
    torch.cuda.manual_seed_all(RANDOM_STATE)



device = torch.device(
    "cuda" if torch.cuda.is_available() else "cpu"
)

print("=" * 60)
print("LOCALIZED TRAINING SCRIPT")
print("=" * 60)
print("Hello from localized training script")
print(f"Using device: {device}")
print(f"Crop size: {CROP_SIZE} x {CROP_SIZE}")
print(f"Model input size: {IMAGE_SIZE} x {IMAGE_SIZE}")




def load_split(split):
    records = []

    for class_name, class_idx in CLASSES.items():

        category_dir = ROOT / class_name / class_name

        csv_path = category_dir / "csv" / f"{split}.csv"
        image_dir = category_dir / "crops"

        df = pd.read_csv(csv_path)

        for _, row in df.iterrows():

            filename = row["filename"]

            image_path = image_dir / filename

            if not image_path.exists():
                print(f"WARNING: Missing image: {image_path}")
                continue

            records.append({
                "image_path": str(image_path),
                "filename": filename,
                "class_idx": class_idx,
                "class_name": class_name,
                "normalized_x": float(row["normalized_x"]),
                "normalized_y": float(row["normalized_y"])
            })

    return pd.DataFrame(records)


train_df = load_split("train")
test_df = load_split("test")


print("\nTraining examples:")
print(train_df["class_name"].value_counts())

print("\nTest examples:")
print(test_df["class_name"].value_counts())

print(f"\nTotal training images: {len(train_df)}")
print(f"Total test images: {len(test_df)}")



train_df, val_df = train_test_split(
    train_df,
    test_size=0.15,
    random_state=RANDOM_STATE,
    stratify=train_df["class_idx"]
)

train_df = train_df.reset_index(drop=True)
val_df = val_df.reset_index(drop=True)

print(f"\nTraining after split: {len(train_df)}")
print(f"Validation: {len(val_df)}")

print("\nClass counts used for training:")

for class_name, class_idx in CLASSES.items():

    count = (train_df["class_idx"] == class_idx).sum()

    print(f"{class_name}: {count}")



def crop_around_point(image, normalized_x, normalized_y, crop_size):
    """
    Crop a square centered on the annotated problem.

    normalized_x and normalized_y are assumed to be
    normalized to the original image dimensions.
    """

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

    return image.crop((left, top, right, bottom))



class LocalizedDataset(Dataset):

    def __init__(self, dataframe, transform=None):
        self.df = dataframe.reset_index(drop=True)
        self.transform = transform

    def __len__(self):
        return len(self.df)

    def __getitem__(self, index):

        row = self.df.iloc[index]

        image = Image.open(
            row["image_path"]
        ).convert("RGB")

        image = crop_around_point(
            image,
            row["normalized_x"],
            row["normalized_y"],
            CROP_SIZE
        )

        if self.transform:
            image = self.transform(image)

        label = int(row["class_idx"])

        return image, label



imagenet_mean = [
    0.485,
    0.456,
    0.406
]

imagenet_std = [
    0.229,
    0.224,
    0.225
]


train_transform = transforms.Compose([

    transforms.Resize(
        (IMAGE_SIZE, IMAGE_SIZE)
    ),

    transforms.RandomHorizontalFlip(
        p=0.5
    ),

    transforms.RandomRotation(
        10
    ),

    transforms.ColorJitter(
        brightness=0.2,
        contrast=0.2,
        saturation=0.2,
        hue=0.05
    ),

    transforms.ToTensor(),

    transforms.Normalize(
        mean=imagenet_mean,
        std=imagenet_std
    )
])


eval_transform = transforms.Compose([

    transforms.Resize(
        (IMAGE_SIZE, IMAGE_SIZE)
    ),

    transforms.ToTensor(),

    transforms.Normalize(
        mean=imagenet_mean,
        std=imagenet_std
    )
])


train_dataset = LocalizedDataset(
    train_df,
    transform=train_transform
)

val_dataset = LocalizedDataset(
    val_df,
    transform=eval_transform
)

test_dataset = LocalizedDataset(
    test_df,
    transform=eval_transform
)




class_counts = np.bincount(
    train_df["class_idx"].values,
    minlength=len(CLASSES)
)

class_weights = 1.0 / class_counts

sample_weights = [
    class_weights[label]
    for label in train_df["class_idx"]
]

sample_weights = torch.DoubleTensor(
    sample_weights
)

sampler = WeightedRandomSampler(
    weights=sample_weights,
    num_samples=len(sample_weights),
    replacement=True
)




train_loader = DataLoader(
    train_dataset,
    batch_size=BATCH_SIZE,
    sampler=sampler,
    num_workers=0
)

val_loader = DataLoader(
    val_dataset,
    batch_size=BATCH_SIZE,
    shuffle=False,
    num_workers=0
)

test_loader = DataLoader(
    test_dataset,
    batch_size=BATCH_SIZE,
    shuffle=False,
    num_workers=0
)




print("\nLoading pretrained EfficientNet-B0...")

model = models.efficientnet_b0(
    weights=models.EfficientNet_B0_Weights.DEFAULT
)

model.classifier[1] = nn.Linear(
    model.classifier[1].in_features,
    len(CLASSES)
)

model = model.to(device)


criterion = nn.CrossEntropyLoss()

optimizer = torch.optim.AdamW(
    model.parameters(),
    lr=LEARNING_RATE,
    weight_decay=WEIGHT_DECAY
)



def run_epoch(model, loader, optimizer=None):

    training = optimizer is not None

    if training:
        model.train()
    else:
        model.eval()

    total_loss = 0.0

    all_labels = []
    all_predictions = []

    for images, labels in loader:

        images = images.to(device)
        labels = labels.to(device)

        if training:
            optimizer.zero_grad()

        with torch.set_grad_enabled(training):

            outputs = model(images)

            loss = criterion(
                outputs,
                labels
            )

            predictions = torch.argmax(
                outputs,
                dim=1
            )

            if training:
                loss.backward()
                optimizer.step()

        total_loss += (
            loss.item() * images.size(0)
        )

        all_labels.extend(
            labels.cpu().numpy()
        )

        all_predictions.extend(
            predictions.cpu().numpy()
        )

    avg_loss = (
        total_loss / len(loader.dataset)
    )

    accuracy = accuracy_score(
        all_labels,
        all_predictions
    )

    macro_f1 = f1_score(
        all_labels,
        all_predictions,
        average="macro"
    )

    return avg_loss, accuracy, macro_f1



print("\nStarting localized training...")

best_val_f1 = -1.0

for epoch in range(EPOCHS):

    train_loss, train_acc, train_f1 = run_epoch(
        model,
        train_loader,
        optimizer
    )

    val_loss, val_acc, val_f1 = run_epoch(
        model,
        val_loader
    )

    print(
        f"\nEpoch {epoch + 1}/{EPOCHS}"
        f" | Train Loss: {train_loss:.4f}"
        f" | Train Acc: {train_acc:.4f}"
        f" | Train F1: {train_f1:.4f}"
        f" | Val Loss: {val_loss:.4f}"
        f" | Val Acc: {val_acc:.4f}"
        f" | Val F1: {val_f1:.4f}"
    )

    if val_f1 > best_val_f1:

        best_val_f1 = val_f1

        torch.save(
            model.state_dict(),
            MODEL_PATH
        )

        print(" Saved new best localized model.")



print("\nLoading best localized model...")

model.load_state_dict(
    torch.load(
        MODEL_PATH,
        map_location=device
    )
)

model.eval()




print("\n" + "=" * 60)
print("FINAL LOCALIZED TEST RESULTS")
print("=" * 60)

test_loss, test_acc, test_f1 = run_epoch(
    model,
    test_loader
)

print(f"Test Loss: {test_loss:.4f}")
print(f"Test Accuracy: {test_acc:.4f}")
print(f"Test Macro F1: {test_f1:.4f}")




all_labels = []
all_predictions = []

with torch.no_grad():

    for images, labels in test_loader:

        images = images.to(device)

        outputs = model(images)

        predictions = torch.argmax(
            outputs,
            dim=1
        )

        all_labels.extend(
            labels.numpy()
        )

        all_predictions.extend(
            predictions.cpu().numpy()
        )



print("\nClassification Report:")

print(
    classification_report(
        all_labels,
        all_predictions,
        target_names=[
            "Crosswalk",
            "CurbRamp",
            "Obstacle",
            "SurfaceProblem"
        ],
        digits=4
    )
)



print("\nConfusion Matrix:")
print("Rows = Actual")
print("Columns = Predicted\n")

cm = confusion_matrix(
    all_labels,
    all_predictions
)

cm_df = pd.DataFrame(
    cm,
    index=[
        "Crosswalk",
        "CurbRamp",
        "Obstacle",
        "SurfaceProblem"
    ],
    columns=[
        "Crosswalk",
        "CurbRamp",
        "Obstacle",
        "SurfaceProblem"
    ]
)

print(cm_df)



cm_path = MODEL_DIR / "localized_confusion_matrix.csv"

cm_df.to_csv(cm_path)

print("\nConfusion matrix saved to:")
print(cm_path)



print("\n" + "=" * 60)
print("DONE")
print("=" * 60)

print("\nBest validation Macro F1:")
print(f"{best_val_f1:.4f}")

print("\nFinal test Accuracy:")
print(f"{test_acc:.4f}")

print("\nFinal test Macro F1:")
print(f"{test_f1:.4f}")

print("\nModel saved to:")
print(MODEL_PATH)