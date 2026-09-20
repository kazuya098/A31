"""Inference service for the Mandrillus face re-identification model.

The model checkpoint and gallery are deployment artifacts. They are deliberately
loaded from paths supplied through environment variables and are not part of
this repository.
"""

from __future__ import annotations

import base64
import os
import uuid
from collections import defaultdict
from datetime import datetime
from io import BytesIO
from pathlib import Path
from typing import List

import numpy as np
import torch
import torch.nn.functional as F
from fastapi import FastAPI, File, HTTPException, UploadFile
from PIL import Image
from starlette.staticfiles import StaticFiles
from torchvision import transforms

from model import FaceModel
from utils.gradcam import get_swin_heatmap_overlay_color


SERVICE_DIR = Path(__file__).resolve().parent
DEVICE = "cuda" if torch.cuda.is_available() else "cpu"
EMBEDDING_SIZE = 512


def env_path(name: str, default: Path) -> Path:
    return Path(os.getenv(name, str(default))).expanduser()


MODEL_PATH = env_path("MODEL_PATH", SERVICE_DIR / "weights" / "clean_model.pth")
GALLERY_DIR = env_path("GALLERY_DIR", SERVICE_DIR / "gallery")
GALLERY_EMB_PATH = env_path(
    "GALLERY_EMB_PATH", GALLERY_DIR / "gallery_emb.npy"
)
GALLERY_LABEL_PATH = env_path(
    "GALLERY_LABEL_PATH", GALLERY_DIR / "gallery_labels (1).npy"
)
STATIC_DIR = env_path("STATIC_DIR", SERVICE_DIR / "static")
CONFIDENCE_THRESHOLD = float(os.getenv("CONFIDENCE_THRESHOLD", "0.0"))
STATIC_DIR.mkdir(parents=True, exist_ok=True)


app = FastAPI(title="Mandrillus Face Recognition API")
app.mount("/static", StaticFiles(directory=str(STATIC_DIR)), name="static")


transform = transforms.Compose(
    [
        transforms.Resize((224, 224)),
        transforms.ToTensor(),
        transforms.Normalize([0.485, 0.456, 0.406], [0.229, 0.224, 0.225]),
    ]
)


def load_model() -> FaceModel:
    if not MODEL_PATH.is_file():
        raise FileNotFoundError(
            f"Model checkpoint not found: {MODEL_PATH}. "
            "Set MODEL_PATH to an external checkpoint."
        )

    loaded_model = FaceModel(embedding_size=EMBEDDING_SIZE).to(DEVICE)
    checkpoint = torch.load(MODEL_PATH, map_location=DEVICE, weights_only=True)
    if isinstance(checkpoint, dict) and "state_dict" in checkpoint:
        checkpoint = checkpoint["state_dict"]
    if isinstance(checkpoint, dict):
        checkpoint = {
            key.removeprefix("module."): value
            for key, value in checkpoint.items()
        }
    loaded_model.load_state_dict(checkpoint, strict=False)
    loaded_model.eval()
    return loaded_model


def load_gallery() -> tuple[torch.Tensor, torch.Tensor]:
    if not GALLERY_EMB_PATH.is_file() or not GALLERY_LABEL_PATH.is_file():
        raise FileNotFoundError(
            "Gallery files not found. Set GALLERY_EMB_PATH and "
            "GALLERY_LABEL_PATH to external .npy files."
        )

    embeddings = np.load(GALLERY_EMB_PATH)
    labels = np.load(GALLERY_LABEL_PATH)
    if embeddings.ndim != 2 or len(embeddings) != len(labels):
        raise ValueError("Gallery embeddings and labels have incompatible shapes")

    embeddings = embeddings / (
        np.linalg.norm(embeddings, axis=1, keepdims=True) + 1e-8
    )
    return (
        torch.from_numpy(embeddings).float().to(DEVICE),
        torch.from_numpy(labels),
    )


model: FaceModel | None = None
gallery_emb: torch.Tensor | None = None
gallery_labels: torch.Tensor | None = None
startup_error: str | None = None


def initialize_runtime() -> None:
    global model, gallery_emb, gallery_labels, startup_error
    try:
        model = load_model()
        gallery_emb, gallery_labels = load_gallery()
        startup_error = None
    except Exception as exc:  # keep /health usable for configuration diagnosis
        model = None
        gallery_emb = None
        gallery_labels = None
        startup_error = f"{type(exc).__name__}: {exc}"


initialize_runtime()


@app.get("/health")
def health() -> dict:
    return {
        "status": "ok" if model is not None else "degraded",
        "device": DEVICE,
        "model_loaded": model is not None,
        "gallery_loaded": gallery_emb is not None,
        "error": startup_error,
    }


def require_runtime() -> tuple[FaceModel, torch.Tensor, torch.Tensor]:
    if model is None or gallery_emb is None or gallery_labels is None:
        raise HTTPException(
            status_code=503,
            detail=(
                "ML runtime is not ready. Check /health and configure the "
                "external model and gallery artifacts."
            ),
        )
    return model, gallery_emb, gallery_labels


def match_batch(query_embeddings: torch.Tensor) -> list[tuple[object, float]]:
    _, embeddings, labels = require_runtime()
    similarities = torch.matmul(query_embeddings, embeddings.T)
    scores, indices = torch.max(similarities, dim=1)
    results: list[tuple[object, float]] = []
    for score, index in zip(scores, indices):
        score_value = float(score)
        if score_value < CONFIDENCE_THRESHOLD:
            results.append(("unknown", 0.0))
        else:
            results.append((int(labels[index]), score_value))
    return results


@torch.no_grad()
def extract_embedding_tensor(batch: torch.Tensor) -> torch.Tensor:
    loaded_model, _, _ = require_runtime()
    return F.normalize(loaded_model(batch), p=2, dim=1)


def save_image_url(image: Image.Image, prefix: str) -> str:
    filename = f"{prefix}_{uuid.uuid4().hex}.jpg"
    image.convert("RGB").save(STATIC_DIR / filename, quality=80)
    return f"/static/{filename}"


def base64_to_url(value: str) -> str | None:
    if not value:
        return None
    if "," in value:
        value = value.split(",", 1)[1]
    image = Image.open(BytesIO(base64.b64decode(value)))
    return save_image_url(image, "heat")


def get_representative_images(identity_id: object) -> list[dict[str, str]]:
    person_dir = GALLERY_DIR / str(identity_id)
    if not person_dir.is_dir():
        return []

    by_year: defaultdict[str, list[tuple[str, str]]] = defaultdict(list)
    for file_path in person_dir.iterdir():
        if (
            not file_path.is_file()
            or file_path.suffix.lower() not in {".jpg", ".jpeg", ".png"}
        ):
            continue
        date_value = file_path.name.split("_", 1)[0]
        by_year[date_value[:4]].append((file_path.name, date_value))

    result: list[dict[str, str]] = []
    for year in sorted(by_year):
        filename, date_value = sorted(by_year[year])[0]
        try:
            formatted_date = datetime.strptime(date_value, "%Y%m%d").strftime(
                "%Y-%m-%d"
            )
        except ValueError:
            formatted_date = date_value
        image = Image.open(person_dir / filename)
        result.append(
            {
                "year": year,
                "date": formatted_date,
                "image_url": save_image_url(image, f"gallery_{identity_id}"),
            }
        )
    return result


def predict_one(image: Image.Image) -> dict:
    loaded_model, _, _ = require_runtime()
    tensor = transform(image).unsqueeze(0).to(DEVICE)
    identity_id, confidence = match_batch(extract_embedding_tensor(tensor))[0]
    heatmap = get_swin_heatmap_overlay_color(
        loaded_model, tensor, original_img=image, alpha=0.5
    )
    result = {
        "identity_id": identity_id,
        "confidence": confidence,
        "heatmap_url": base64_to_url(heatmap),
        "gallery_images": [],
    }
    if identity_id != "unknown":
        result["gallery_images"] = get_representative_images(identity_id)
    return result


@app.post("/predict")
async def predict(file: UploadFile = File(...)) -> dict:
    try:
        image = Image.open(file.file).convert("RGB")
    except Exception as exc:
        raise HTTPException(status_code=400, detail="Invalid image file") from exc
    return predict_one(image)


@app.post("/predict_batch")
async def predict_batch(files: List[UploadFile] = File(...)) -> dict:
    images: list[Image.Image] = []
    for file in files:
        try:
            images.append(Image.open(file.file).convert("RGB"))
        except Exception as exc:
            raise HTTPException(status_code=400, detail="Invalid image file") from exc
    return {"results": [predict_one(image) for image in images]}
