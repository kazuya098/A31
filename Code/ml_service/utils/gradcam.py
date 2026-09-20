"""Swin feature activation visualization."""

from __future__ import annotations

import base64
import io

import matplotlib.cm as cm
import numpy as np
import torch
from PIL import Image


def get_swin_heatmap_overlay_color(
    model: torch.nn.Module,
    x: torch.Tensor,
    original_img: Image.Image | None = None,
    alpha: float = 0.5,
    colormap: str = "jet",
) -> str:
    """Return a base64-encoded PNG overlay built from Swin activations."""
    model.eval()
    with torch.no_grad():
        features = model.get_last_stage_feature_swin(x)
        if features is None or features.ndim != 4:
            return ""

        activation = torch.norm(features, dim=-1)
        activation = (activation - activation.min()) / (
            activation.max() - activation.min() + 1e-6
        )
        activation_np = (activation[0].cpu().numpy() * 255).astype(np.uint8)

    color_map = cm.get_cmap(colormap)
    heatmap = color_map(activation_np / 255.0)[:, :, :3]
    heatmap = (heatmap * 255).astype(np.uint8)
    heatmap_image = Image.fromarray(heatmap).resize((224, 224))

    if original_img is not None:
        original = original_img.resize((224, 224)).convert("RGB")
        heatmap_image = Image.blend(original, heatmap_image, alpha=alpha)

    buffer = io.BytesIO()
    heatmap_image.save(buffer, format="PNG")
    return base64.b64encode(buffer.getvalue()).decode("ascii")
