
import os
import random
from pathlib import Path
print("TRAIN.PY STARTED")

print("Hello from training script")
import numpy as np
import pandas as pd
from PIL import Image

import torch
import torch.nn as nn
from torch.utils.data import Dataset, DataLoader, WeightedRandomSampler
from torchvision import models, transforms

from sklearn.model_selection import train_test_split
from sklearn.metrics import (
    classification_report,
    confusion_matrix,
    accuracy_score,
    f1_score,
)




CLASSES = {
    "Crosswalk": 0,
    "CurbRamp": 1,
    "Obstacle": 2,
    "SurfaceProblem": 3,
}

CLASS_NAMES = list(CLASSES.keys())

BATCH_SIZE = 32
NUM_EPOCHS = 10
LEARNING_RATE = 1e-4
IMAGE_SIZE = 224
RANDOM_SEED = 42

DEVICE = torch.device(
    "cuda" if torch.cuda.is_available() else "cpu"
)

print(f"Using device: {DEVICE}")



random.seed(RANDOM_SEED)
np.random.seed(RANDOM_SEED)
torch.manual_seed(RANDOM_SEED)

if torch.cuda.is_available():
    torch.cuda.manual_seed_all(RANDOM_SEED)


def load_dataset(split="train"):
    records = []

    for class_name, class_id in CLASSES.items():
        category_dir = ROOT / class_name / class_name
        csv_path = category_dir / "csv" / f"{split}.csv"
        image_dir = category_dir / "crops"

        df = pd.read_csv(csv_path)

        for filename in df["filename"]:
            image_path = image_dir / filename

            if not image_path.exists():
                raise FileNotFoundError(
                    f"Image not found: {image_path}"
                )

            records.append({
                "image": str(image_path),
                "label": class_id,
                "class_name": class_name,
            })

    return pd.DataFrame(records)


train_df = load_dataset("train")
test_df = load_dataset("test")

print("\nTraining examples:")
print(train_df["class_name"].value_counts())

print("\nTest examples:")
print(test_df["class_name"].value_counts())

print(f"\nTotal training images: {len(train_df)}")
print(f"Total test images: {len(test_df)}")




train_df, val_df = train_test_split(
    train_df,
    test_size=0.15,
    stratify=train_df["label"],
    random_state=RANDOM_SEED,
)

print(f"\nTraining after split: {len(train_df)}")
print(f"Validation: {len(val_df)}")



train_transform = transforms.Compose([
    transforms.Resize((IMAGE_SIZE, IMAGE_SIZE)),
    transforms.RandomHorizontalFlip(),
    transforms.RandomRotation(10),
    transforms.ColorJitter(
        brightness=0.2,
        contrast=0.2,
        saturation=0.2,
    ),
    transforms.ToTensor(),
    transforms.Normalize(
        mean=[0.485, 0.456, 0.406],
        std=[0.229, 0.224, 0.225],
    ),
])

eval_transform = transforms.Compose([
    transforms.Resize((IMAGE_SIZE, IMAGE_SIZE)),
    transforms.ToTensor(),
    transforms.Normalize(
        mean=[0.485, 0.456, 0.406],
        std=[0.229, 0.224, 0.225],
    ),
])




class SidewalkDataset(Dataset):
    def __init__(self, dataframe, transform=None):
        self.dataframe = dataframe.reset_index(drop=True)
        self.transform = transform

    def __len__(self):
        return len(self.dataframe)

    def __getitem__(self, index):
        row = self.dataframe.iloc[index]

        image = Image.open(
            row["image"]
        ).convert("RGB")

        if self.transform:
            image = self.transform(image)

        label = int(row["label"])

        return image, label


train_dataset = SidewalkDataset(
    train_df,
    transform=train_transform
)

val_dataset = SidewalkDataset(
    val_df,
    transform=eval_transform
)

test_dataset = SidewalkDataset(
    test_df,
    transform=eval_transform
)



class_counts = (
    train_df["label"]
    .value_counts()
    .sort_index()
)

print("\nClass counts used for training:")

for class_id, count in class_counts.items():
    print(f"{CLASS_NAMES[class_id]}: {count}")


# Give rarer classes larger sampling weights.
sample_weights = train_df["label"].map(
    lambda label: 1.0 / class_counts[label]
).values

sample_weights = torch.DoubleTensor(sample_weights)

sampler = WeightedRandomSampler(
    weights=sample_weights,
    num_samples=len(sample_weights),
    replacement=True,
)



train_loader = DataLoader(
    train_dataset,
    batch_size=BATCH_SIZE,
    sampler=sampler,
    num_workers=0,
)

val_loader = DataLoader(
    val_dataset,
    batch_size=BATCH_SIZE,
    shuffle=False,
    num_workers=0,
)

test_loader = DataLoader(
    test_dataset,
    batch_size=BATCH_SIZE,
    shuffle=False,
    num_workers=0,
)



print("\nLoading pretrained EfficientNet-B0...")

weights = models.EfficientNet_B0_Weights.DEFAULT

model = models.efficientnet_b0(
    weights=weights
)

# Replace ImageNet classifier with our four classes.
num_features = model.classifier[1].in_features

model.classifier[1] = nn.Linear(
    num_features,
    len(CLASSES)
)

model = model.to(DEVICE)



criterion = nn.CrossEntropyLoss()

optimizer = torch.optim.AdamW(
    model.parameters(),
    lr=LEARNING_RATE,
    weight_decay=1e-4,
)


def train_one_epoch():
    model.train()

    total_loss = 0
    all_predictions = []
    all_labels = []

    for images, labels in train_loader:
        images = images.to(DEVICE)
        labels = labels.to(DEVICE)

        optimizer.zero_grad()

        outputs = model(images)

        loss = criterion(
            outputs,
            labels
        )

        loss.backward()
        optimizer.step()

        total_loss += (
            loss.item() * images.size(0)
        )

        predictions = outputs.argmax(dim=1)

        all_predictions.extend(
            predictions.cpu().numpy()
        )

        all_labels.extend(
            labels.cpu().numpy()
        )

    average_loss = (
        total_loss / len(train_loader.dataset)
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

    return average_loss, accuracy, macro_f1


@torch.no_grad()
def evaluate(loader):
    model.eval()

    total_loss = 0
    all_predictions = []
    all_labels = []

    for images, labels in loader:
        images = images.to(DEVICE)
        labels = labels.to(DEVICE)

        outputs = model(images)

        loss = criterion(
            outputs,
            labels
        )

        total_loss += (
            loss.item() * images.size(0)
        )

        predictions = outputs.argmax(dim=1)

        all_predictions.extend(
            predictions.cpu().numpy()
        )

        all_labels.extend(
            labels.cpu().numpy()
        )

    average_loss = (
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

    return (
        average_loss,
        accuracy,
        macro_f1,
        all_labels,
        all_predictions,
    )



best_val_f1 = 0

os.makedirs("models", exist_ok=True)

print("\nStarting training...\n")

for epoch in range(NUM_EPOCHS):

    train_loss, train_acc, train_f1 = (
        train_one_epoch()
    )

    (
        val_loss,
        val_acc,
        val_f1,
        _,
        _,
    ) = evaluate(val_loader)

    print(
        f"Epoch {epoch + 1}/{NUM_EPOCHS} | "
        f"Train Loss: {train_loss:.4f} | "
        f"Train Acc: {train_acc:.4f} | "
        f"Train F1: {train_f1:.4f} | "
        f"Val Loss: {val_loss:.4f} | "
        f"Val Acc: {val_acc:.4f} | "
        f"Val F1: {val_f1:.4f}"
    )

    # Save the model with the best validation macro F1.
    if val_f1 > best_val_f1:
        best_val_f1 = val_f1

        torch.save(
            {
                "model_state_dict": model.state_dict(),
                "class_names": CLASS_NAMES,
            },
            "models/best_sidewalk_model.pth",
        )

        print(" Saved new best model.")



print("\nLoading best model...")

checkpoint = torch.load(
    "models/best_sidewalk_model.pth",
    map_location=DEVICE,
)

model.load_state_dict(
    checkpoint["model_state_dict"]
)



(
    test_loss,
    test_accuracy,
    test_f1,
    test_labels,
    test_predictions,
) = evaluate(test_loader)

print("\n" + "=" * 60)
print("FINAL TEST RESULTS")
print("=" * 60)

print(f"Test Loss: {test_loss:.4f}")
print(f"Test Accuracy: {test_accuracy:.4f}")
print(f"Test Macro F1: {test_f1:.4f}")

print("\nClassification Report:")

print(
    classification_report(
        test_labels,
        test_predictions,
        target_names=CLASS_NAMES,
        digits=4,
    )
)




cm = confusion_matrix(
    test_labels,
    test_predictions,
)

print("\nConfusion Matrix:")
print("Rows = Actual")
print("Columns = Predicted\n")

print(
    pd.DataFrame(
        cm,
        index=CLASS_NAMES,
        columns=CLASS_NAMES,
    )
)

print("\nModel saved to:")
print(
    Path(
        "models/best_sidewalk_model.pth"
    ).resolve()
)
