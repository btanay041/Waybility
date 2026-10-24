import pandas as pd
from pathlib import Path


categories = {
    "Crosswalk": "Crosswalk",
    "CurbRamp": "CurbRamp",
    "Obstacle": "Obstacle",
    "SurfaceProblem": "SurfaceProblem"
}

for category, folder in categories.items():
    csv_path = root / folder / folder / "csv" / "train.csv"
    image_dir = root / folder / folder / "crops"

    df = pd.read_csv(csv_path)

    missing = [
        f for f in df["filename"]
        if not (image_dir / f).exists()
    ]

    print(f"{category}: {len(df)} rows, {len(missing)} missing images")