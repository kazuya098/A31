"""Inference-time model definition used by the ML service."""

from __future__ import annotations

import torch
import torch.nn as nn
import torch.nn.functional as F
import timm


class FaceModel(nn.Module):
    """Swin-Tiny feature extractor followed by a normalized embedding head."""

    def __init__(
        self,
        embedding_size: int = 512,
        backbone_name: str = "swin_tiny_patch4_window7_224",
    ) -> None:
        super().__init__()
        self.backbone = timm.create_model(
            backbone_name,
            pretrained=False,
            num_classes=0,
        )

        with torch.no_grad():
            feature_dim = self.backbone(torch.randn(1, 3, 224, 224)).shape[-1]
        self.embedding = nn.Linear(feature_dim, embedding_size)

    def forward(self, x: torch.Tensor) -> torch.Tensor:
        features = self.backbone(x)
        if features.ndim > 2:
            features = features.flatten(1)
        return F.normalize(self.embedding(features), dim=1)

    def get_last_stage_feature_swin(self, x: torch.Tensor) -> torch.Tensor:
        """Return the last Swin feature map for the attention visualization."""
        with torch.no_grad():
            features = self.backbone.forward_features(x)
            if features.ndim == 3:
                batch, tokens, channels = features.shape
                side = int(tokens**0.5)
                if side * side == tokens:
                    features = features.reshape(batch, side, side, channels)
            return features
