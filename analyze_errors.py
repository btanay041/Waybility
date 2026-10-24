
from pathlib import Path
import shutil
import pandas as pd
import torch
from torch.utils.data import Dataset, DataLoader
from torchvision import transforms, models
from PIL import Image
from sklearn.metrics import confusion_matrix, classification_report



CLASSES = {
    "Crosswalk": 0,
    "CurbRamp": 1,
    "Obstacle": 2,
    "SurfaceProblem": 3
}

IDX_TO_CLASS = {v: k for k, v in CLASSES.items()}



device = torch.device("cuda" if torch.cuda.is_available() else "cpu")

print("=" * 60)
print("ERROR ANALYSIS")
print("=" * 60)
print(f"Using device: {device}")



records = []

for class_name, class_idx in CLASSES.items():

    category_dir = ROOT / class_name

    csv_path = category_dir / class_name / "csv" / "test.csv"
    image_dir = category_dir / class_name / "crops"

    df = pd.read_csv(csv_path)

    for _, row in df.iterrows():

        filename = row["filename"]
        image_path = image_dir / filename

        if not image_path.exists():
            print(f"WARNING: Missing image: {image_path}")
            continue

        records.append({
            "image_path": image_path,
            "filename": filename,
            "actual": class_idx,
            "actual_name": class_name
        })


print(f"\nTest images found: {len(records)}")



transform = transforms.Compose([
    transforms.Resize((224, 224)),
    transforms.ToTensor(),
    transforms.Normalize(
        mean=[0.485, 0.456, 0.406],
        std=[0.229, 0.224, 0.225]
    )
])


class TestDataset(Dataset):

    def __init__(self, records):
        self.records = records

    def __len__(self):
        return len(self.records)

    def __getitem__(self, idx):

        record = self.records[idx]

        image = Image.open(record["image_path"]).convert("RGB")
        image = transform(image)

        return (
            image,
            record["actual"],
            record["filename"],
            str(record["image_path"])
        )


dataset = TestDataset(records)

loader = DataLoader(
    dataset,
    batch_size=32,
    shuffle=False,
    num_workers=0
)



print("\nLoading best model...")

model = models.efficientnet_b0(
    weights=None
)

model.classifier[1] = torch.nn.Linear(
    model.classifier[1].in_features,
    4
)

checkpoint = torch.load(
    MODEL_PATH,
    map_location=device
)

if isinstance(checkpoint, dict) and "model_state_dict" in checkpoint:
    model.load_state_dict(checkpoint["model_state_dict"])
else:
    model.load_state_dict(checkpoint)

model = model.to(device)
model.eval()

print("Model loaded successfully.")



if OUTPUT_DIR.exists():
    print(f"\nOutput directory already exists:")
    print(OUTPUT_DIR)

    response = input("Delete old error analysis and create a new one? (y/n): ")

    if response.lower() == "y":
        shutil.rmtree(OUTPUT_DIR)
    else:
        print("Keeping existing directory.")

OUTPUT_DIR.mkdir(parents=True, exist_ok=True)



print("\nRunning model on test set...")

all_actual = []
all_predicted = []
all_confidence = []
all_filenames = []
all_paths = []

with torch.no_grad():

    for batch_idx, (images, labels, filenames, paths) in enumerate(loader):

        images = images.to(device)

        outputs = model(images)

        probabilities = torch.softmax(outputs, dim=1)

        confidence, predictions = torch.max(
            probabilities,
            dim=1
        )

        all_actual.extend(labels.numpy())
        all_predicted.extend(predictions.cpu().numpy())
        all_confidence.extend(confidence.cpu().numpy())
        all_filenames.extend(filenames)
        all_paths.extend(paths)

        if (batch_idx + 1) % 20 == 0:
            print(
                f"Processed {(batch_idx + 1) * loader.batch_size}"
                f"/{len(dataset)} images..."
            )




results = pd.DataFrame({
    "filename": all_filenames,
    "image_path": all_paths,
    "actual": all_actual,
    "predicted": all_predicted,
    "confidence": all_confidence
})

results["actual_class"] = results["actual"].map(IDX_TO_CLASS)
results["predicted_class"] = results["predicted"].map(IDX_TO_CLASS)

results["correct"] = (
    results["actual"] == results["predicted"]
)



results.to_csv(
    OUTPUT_DIR / "all_predictions.csv",
    index=False
)



errors = results[~results["correct"]].copy()

errors.to_csv(
    OUTPUT_DIR / "misclassified_images.csv",
    index=False
)



print("\nCopying misclassified images...")

for _, row in errors.iterrows():

    actual = row["actual_class"]
    predicted = row["predicted_class"]

    folder_name = f"{actual}_as_{predicted}"

    error_folder = OUTPUT_DIR / folder_name
    error_folder.mkdir(parents=True, exist_ok=True)

    source = Path(row["image_path"])

    confidence = row["confidence"]

    destination_name = (
        f"conf_{confidence:.3f}__{row['filename']}"
    )

    destination = error_folder / destination_name

    shutil.copy2(
        source,
        destination
    )



print("\n" + "=" * 60)
print("ERROR ANALYSIS RESULTS")
print("=" * 60)

total = len(results)
incorrect = len(errors)
correct = total - incorrect

print(f"Total test images: {total}")
print(f"Correct predictions: {correct}")
print(f"Incorrect predictions: {incorrect}")
print(f"Accuracy: {correct / total:.4f}")



print("\n" + "=" * 60)
print("MOST COMMON ERRORS")
print("=" * 60)

error_pairs = (
    errors
    .groupby(["actual_class", "predicted_class"])
    .size()
    .sort_values(ascending=False)
)

for (actual, predicted), count in error_pairs.items():

    print(
        f"{actual:16s} -> {predicted:16s}: {count}"
    )



print("\n" + "=" * 60)
print("CLASSIFICATION REPORT")
print("=" * 60)

print(
    classification_report(
        all_actual,
        all_predicted,
        target_names=[
            "Crosswalk",
            "CurbRamp",
            "Obstacle",
            "SurfaceProblem"
        ],
        digits=4
    )
)


print("\n" + "=" * 60)
print("CONFUSION MATRIX")
print("=" * 60)

cm = confusion_matrix(
    all_actual,
    all_predicted
)

cm_df = pd.DataFrame(
    cm,
    index=["Crosswalk", "CurbRamp", "Obstacle", "SurfaceProblem"],
    columns=["Crosswalk", "CurbRamp", "Obstacle", "SurfaceProblem"]
)

print(cm_df)

cm_df.to_csv(
    OUTPUT_DIR / "confusion_matrix.csv"
)



print("\n" + "=" * 60)
print("LOW-CONFIDENCE ERRORS")
print("=" * 60)

low_confidence = (
    errors
    .sort_values("confidence")
    .head(20)
)

print(
    low_confidence[
        [
            "filename",
            "actual_class",
            "predicted_class",
            "confidence"
        ]
    ].to_string(index=False)
)




print("\n" + "=" * 60)
print("HIGH-CONFIDENCE ERRORS")
print("=" * 60)

high_confidence = (
    errors
    .sort_values("confidence", ascending=False)
    .head(20)
)

print(
    high_confidence[
        [
            "filename",
            "actual_class",
            "predicted_class",
            "confidence"
        ]
    ].to_string(index=False)
)



print("\n" + "=" * 60)
print("DONE")
print("=" * 60)

print("\nError analysis saved to:")

print(OUTPUT_DIR)

print("\nInside it you will find:")

print("  all_predictions.csv")
print("  misclassified_images.csv")
print("  confusion_matrix.csv")
print("  Crosswalk_as_CurbRamp/")
print("  CurbRamp_as_Crosswalk/")
print("  Obstacle_as_SurfaceProblem/")
print("  SurfaceProblem_as_Obstacle/")
print("  etc.")

print("\nOpen the folders and inspect the images.")