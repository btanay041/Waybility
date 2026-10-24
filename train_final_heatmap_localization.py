import os
import math
import copy
import random
import numpy as np
import pandas as pd
from PIL import Image

import torch
import torch.nn as nn
import torch.nn.functional as F
from torch.utils.data import Dataset, DataLoader
from torchvision import models, transforms

from sklearn.model_selection import train_test_split




DATASET_DIR = os.path.join(
    BASE_DIR,
    "sidewalk-tagger-ai-validated",
    "Validated"
)

MODEL_DIR = os.path.join(BASE_DIR, "models")
os.makedirs(MODEL_DIR, exist_ok=True)

BEST_MODEL_PATH = os.path.join(
    MODEL_DIR,
    "best_final_heatmap_localization_model.pth"
)

CLASSES = [
    "Crosswalk",
    "CurbRamp",
    "Obstacle",
    "SurfaceProblem"
]

ORIGINAL_WIDTH = 1440
ORIGINAL_HEIGHT = 960

IMAGE_SIZE = 224

HEATMAP_SIZE = 56

SIGMA = 2.5


BATCH_SIZE = 16
EPOCHS = 25
LEARNING_RATE = 1e-4
WEIGHT_DECAY = 2e-4

PATIENCE = 6

NUM_WORKERS = 0

SEED = 42

DEVICE = torch.device(
    "cuda" if torch.cuda.is_available() else "cpu"
)




def set_seed(seed):
    random.seed(seed)
    np.random.seed(seed)

    torch.manual_seed(seed)

    if torch.cuda.is_available():
        torch.cuda.manual_seed_all(seed)

        torch.backends.cudnn.deterministic = True
        torch.backends.cudnn.benchmark = False


set_seed(SEED)



def read_all_data():
    rows = []

    for class_name in CLASSES:

        csv_path = os.path.join(
            DATASET_DIR,
            class_name,
            class_name,
            "csv",
            "train.csv"
        )

        print(f"Reading: {csv_path}")

        df = pd.read_csv(csv_path)

        filename_col = "filename"

        for _, row in df.iterrows():

            filename = str(row[filename_col])

            image_path = os.path.join(
                DATASET_DIR,
                class_name,
                class_name,
                "crops",
                filename
            )

            if not os.path.exists(image_path):

                image_path = os.path.join(
                    DATASET_DIR,
                    class_name,
                    class_name,
                    "crops",
                    os.path.basename(filename)
                )

            if not os.path.exists(image_path):
                continue

            rows.append({
                "image_path": image_path,
                "x": float(row["normalized_x"]),
                "y": float(row["normalized_y"]),
                "class": class_name
            })

    return pd.DataFrame(rows)



def resize_and_pad(image):
    """
    Resize 1440x960 while preserving aspect ratio.

    The resized image is centered on a 224x224 canvas.

    Returns:
        image
        scale
        pad_x
        pad_y
    """

    width, height = image.size

    scale = min(
        IMAGE_SIZE / width,
        IMAGE_SIZE / height
    )

    new_width = int(round(width * scale))
    new_height = int(round(height * scale))

    image = image.resize(
        (new_width, new_height),
        Image.Resampling.BILINEAR
    )

    canvas = Image.new(
        "RGB",
        (IMAGE_SIZE, IMAGE_SIZE),
        (0, 0, 0)
    )

    pad_x = (IMAGE_SIZE - new_width) // 2
    pad_y = (IMAGE_SIZE - new_height) // 2

    canvas.paste(
        image,
        (pad_x, pad_y)
    )

    return canvas, scale, pad_x, pad_y




def create_gaussian_heatmap(x, y):
    """
    x/y are coordinates in the 224x224 network input.
    """

    yy, xx = torch.meshgrid(
        torch.arange(HEATMAP_SIZE),
        torch.arange(HEATMAP_SIZE),
        indexing="ij"
    )

    cx = x / IMAGE_SIZE * (HEATMAP_SIZE - 1)
    cy = y / IMAGE_SIZE * (HEATMAP_SIZE - 1)

    heatmap = torch.exp(
        -(
            (xx.float() - cx) ** 2
            +
            (yy.float() - cy) ** 2
        )
        /
        (2 * SIGMA ** 2)
    )

    return heatmap



class HeatmapLocalizationDataset(Dataset):

    def __init__(self, dataframe, train=False):

        self.df = dataframe.reset_index(drop=True)
        self.train = train

    def __len__(self):
        return len(self.df)

    def __getitem__(self, idx):

        row = self.df.iloc[idx]

        image = Image.open(
            row["image_path"]
        ).convert("RGB")



        original_x = row["x"] * ORIGINAL_WIDTH
        original_y = row["y"] * ORIGINAL_HEIGHT


        image, scale, pad_x, pad_y = resize_and_pad(image)

        x = original_x * scale + pad_x
        y = original_y * scale + pad_y



        if self.train and random.random() < 0.5:

            image = image.transpose(
                Image.Transpose.FLIP_LEFT_RIGHT
            )

            x = IMAGE_SIZE - x

 

        if self.train:

            image = transforms.ColorJitter(
                brightness=0.20,
                contrast=0.20,
                saturation=0.15,
                hue=0.03


        image = transforms.ToTensor()(image)

        image = transforms.Normalize(
            mean=[0.485, 0.456, 0.406],
            std=[0.229, 0.224, 0.225]
        )(image)


        heatmap = create_gaussian_heatmap(
            x,
            y
        )

        return image, heatmap, torch.tensor(
            [x / IMAGE_SIZE, y / IMAGE_SIZE],
            dtype=torch.float32
        )


class FinalHeatmapLocalizationModel(nn.Module):

    def __init__(self):

        super().__init__()

        backbone = models.efficientnet_b0(
            weights=models.EfficientNet_B0_Weights.DEFAULT
        )

        self.features = backbone.features

        self.head = nn.Sequential(

            nn.Conv2d(
                1280,
                512,
                kernel_size=3,
                padding=1
            ),

            nn.BatchNorm2d(512),

            nn.SiLU(),

            nn.Dropout2d(0.15),

            nn.Conv2d(
                512,
                256,
                kernel_size=3,
                padding=1
            ),

            nn.BatchNorm2d(256),

            nn.SiLU(),

            nn.Dropout2d(0.10),

            nn.Conv2d(
                256,
                64,
                kernel_size=3,
                padding=1
            ),

            nn.SiLU(),

            nn.Conv2d(
                64,
                1,
                kernel_size=1
            )
        )

    def forward(self, x):

        x = self.features(x)

        x = F.interpolate(
            x,
            size=(56, 56),
            mode="bilinear",
            align_corners=False
        )

        x = self.head(x)

        return x


def soft_argmax(heatmaps):

    """
    Convert heatmap into continuous x/y coordinates.

    Instead of selecting one pixel with argmax,
    this computes the probability-weighted center.
    """

    batch_size = heatmaps.shape[0]

    heatmaps = heatmaps.view(
        batch_size,
        -1
    )

    probabilities = F.softmax(
        heatmaps * 10.0,
        dim=1
    )

    ys, xs = torch.meshgrid(
        torch.arange(
            HEATMAP_SIZE,
            device=heatmaps.device,
            dtype=torch.float32
        ),
        torch.arange(
            HEATMAP_SIZE,
            device=heatmaps.device,
            dtype=torch.float32
        ),
        indexing="ij"
    )

    xs = xs.reshape(-1)
    ys = ys.reshape(-1)

    pred_x = (
        probabilities * xs.unsqueeze(0)
    ).sum(dim=1)

    pred_y = (
        probabilities * ys.unsqueeze(0)
    ).sum(dim=1)

    pred_x = (
        pred_x /
        (HEATMAP_SIZE - 1)
        *
        IMAGE_SIZE
    )

    pred_y = (
        pred_y /
        (HEATMAP_SIZE - 1)
        *
        IMAGE_SIZE
    )

    return torch.stack(
        [pred_x, pred_y],
        dim=1
    )


def calculate_metrics(predictions, targets):

    pred_x = predictions[:, 0] * ORIGINAL_WIDTH
    pred_y = predictions[:, 1] * ORIGINAL_HEIGHT

    true_x = targets[:, 0] * ORIGINAL_WIDTH
    true_y = targets[:, 1] * ORIGINAL_HEIGHT

    errors = torch.sqrt(
        (pred_x - true_x) ** 2
        +
        (pred_y - true_y) ** 2
    )

    mean_error = errors.mean().item()

    median_error = errors.median().item()

    within_25 = (
        (errors <= 25).float().mean().item()
    )

    within_50 = (
        (errors <= 50).float().mean().item()
    )

    within_100 = (
        (errors <= 100).float().mean().item()
    )

    within_150 = (
        (errors <= 150).float().mean().item()
    )

    within_200 = (
        (errors <= 200).float().mean().item()
    )

    return {
        "mean": mean_error,
        "median": median_error,
        "within25": within_25,
        "within50": within_50,
        "within100": within_100,
        "within150": within_150,
        "within200": within_200
    }



def train():

    print("=" * 60)
    print("FINAL HEATMAP LOCALIZATION MODEL")
    print("=" * 60)

    print(f"Device: {DEVICE}")
    print(f"Heatmap: {HEATMAP_SIZE}x{HEATMAP_SIZE}")
    print(f"Input: {IMAGE_SIZE}x{IMAGE_SIZE}")
    print()

  

    df = read_all_data()

    print()
    print(f"Total samples: {len(df)}")

    # Stratify by class
    train_df, val_df = train_test_split(
        df,
        test_size=0.15,
        random_state=SEED,
        stratify=df["class"]
    )

    print(f"Training samples:   {len(train_df)}")
    print(f"Validation samples: {len(val_df)}")


    train_dataset = HeatmapLocalizationDataset(
        train_df,
        train=True
    )

    val_dataset = HeatmapLocalizationDataset(
        val_df,
        train=False
    )


    train_loader = DataLoader(
        train_dataset,
        batch_size=BATCH_SIZE,
        shuffle=True,
        num_workers=NUM_WORKERS,
        pin_memory=False
    )

    val_loader = DataLoader(
        val_dataset,
        batch_size=BATCH_SIZE,
        shuffle=False,
        num_workers=NUM_WORKERS,
        pin_memory=False
    )


    model = FinalHeatmapLocalizationModel().to(
        DEVICE
    )



    optimizer = torch.optim.AdamW(
        model.parameters(),
        lr=LEARNING_RATE,
        weight_decay=WEIGHT_DECAY
    )

    scheduler = torch.optim.lr_scheduler.ReduceLROnPlateau(
        optimizer,
        mode="min",
        factor=0.5,
        patience=2,
        min_lr=1e-6
    )

 

    best_median_error = float("inf")

    best_state = None

    epochs_without_improvement = 0


    for epoch in range(EPOCHS):

        print()
        print("=" * 60)
        print(
            f"Epoch {epoch + 1}/{EPOCHS}"
        )
        print("=" * 60)


        model.train()

        train_loss = 0.0

        for batch_idx, (
            images,
            heatmaps,
            targets
        ) in enumerate(train_loader):

            images = images.to(DEVICE)
            heatmaps = heatmaps.to(DEVICE)

            optimizer.zero_grad()

            predictions = model(images)

            loss = F.mse_loss(
                predictions.squeeze(1),
                heatmaps
            )

            loss.backward()

            torch.nn.utils.clip_grad_norm_(
                model.parameters(),
                max_norm=1.0
            )

            optimizer.step()

            train_loss += (
                loss.item()
                *
                images.size(0)
            )

            if (
                batch_idx + 1
            ) % 200 == 0:

                print(
                    f"  Batch "
                    f"{batch_idx + 1}/"
                    f"{len(train_loader)}"
                )

        train_loss /= len(train_dataset)


        model.eval()

        val_loss = 0.0

        all_predictions = []
        all_targets = []

        with torch.no_grad():

            for (
                images,
                heatmaps,
                targets
            ) in val_loader:

                images = images.to(DEVICE)
                heatmaps = heatmaps.to(DEVICE)

                predictions = model(images)

                loss = F.mse_loss(
                    predictions.squeeze(1),
                    heatmaps
                )

                val_loss += (
                    loss.item()
                    *
                    images.size(0)
                )

                coords = soft_argmax(
                    predictions
                )

      
                coords_normalized = (
                    coords / IMAGE_SIZE
                )

                all_predictions.append(
                    coords_normalized.cpu()
                )

                all_targets.append(
                    targets
                )

        val_loss /= len(val_dataset)

        all_predictions = torch.cat(
            all_predictions
        )

        all_targets = torch.cat(
            all_targets
        )

        metrics = calculate_metrics(
            all_predictions,
            all_targets
        )

        scheduler.step(
            metrics["median"]
        )

        current_lr = optimizer.param_groups[0]["lr"]


        print()
        print(
            f"Train Loss:       {train_loss:.6f}"
        )

        print(
            f"Val Loss:         {val_loss:.6f}"
        )

        print(
            f"Mean Error:       "
            f"{metrics['mean']:.2f} px"
        )

        print(
            f"Median Error:     "
            f"{metrics['median']:.2f} px"
        )

        print(
            f"Within 25px:      "
            f"{metrics['within25']:.4f}"
        )

        print(
            f"Within 50px:      "
            f"{metrics['within50']:.4f}"
        )

        print(
            f"Within 100px:     "
            f"{metrics['within100']:.4f}"
        )

        print(
            f"Within 150px:     "
            f"{metrics['within150']:.4f}"
        )

        print(
            f"Within 200px:     "
            f"{metrics['within200']:.4f}"
        )

        print(
            f"Learning Rate:    "
            f"{current_lr:.7f}"
        )


        if (
            metrics["median"]
            <
            best_median_error
        ):

            best_median_error = (
                metrics["median"]
            )

            best_state = copy.deepcopy(
                model.state_dict()
            )

            torch.save(
                {
                    "model_state_dict":
                        best_state,

                    "epoch":
                        epoch + 1,

                    "mean_error":
                        metrics["mean"],

                    "median_error":
                        metrics["median"],

                    "within25":
                        metrics["within25"],

                    "within50":
                        metrics["within50"],

                    "within100":
                        metrics["within100"],

                    "within150":
                        metrics["within150"],

                    "within200":
                        metrics["within200"],

                    "heatmap_size":
                        HEATMAP_SIZE,

                    "image_size":
                        IMAGE_SIZE,

                    "sigma":
                        SIGMA
                },
                BEST_MODEL_PATH
            )

            print()
            print(
                "*** NEW BEST MODEL SAVED ***"
            )

            epochs_without_improvement = 0

        else:

            epochs_without_improvement += 1

            print()
            print(
                f"No improvement "
                f"({epochs_without_improvement}/"
                f"{PATIENCE})"
            )



        if (
            epochs_without_improvement
            >= PATIENCE
        ):

            print()
            print(
                "Early stopping."
            )

            break



    if best_state is not None:

        model.load_state_dict(
            best_state
        )

    print()
    print("=" * 60)
    print("TRAINING COMPLETE")
    print("=" * 60)

    print(
        f"Best median error: "
        f"{best_median_error:.2f} px"
    )

    print(
        f"Saved to:"
    )

    print(
        BEST_MODEL_PATH
    )


if __name__ == "__main__":
    train()