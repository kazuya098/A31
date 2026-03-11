# model.py
import torch
import torch.nn as nn
import torch.nn.functional as F
import timm

class FaceModel(nn.Module):
    """
    Backbone: Swin-Tiny / ViT-B/16
    Output: 512-d embedding (L2 normalized)
    """
    def __init__(self, embedding_size=512, backbone_name="swin_tiny_patch4_window7_224"):
        super().__init__()
        self.backbone = timm.create_model(
            backbone_name,
            pretrained=True,
            num_classes=0  # remove classification head
        )
        self.embedding = nn.Linear(self.backbone.num_features, embedding_size)
        self.bn = nn.BatchNorm1d(embedding_size)
        
    def forward(self, x):
        x = self.backbone(x)
        x = self.embedding(x)
        x = self.bn(x)
        x = F.normalize(x)  # L2 正则化 embedding
        return x

# ----------------------
# ArcFace Loss
# ----------------------
class ArcFace(nn.Module):
    def __init__(self, in_features, out_features, s=64.0, m=0.5):
        super().__init__()
        self.weight = nn.Parameter(torch.FloatTensor(out_features, in_features))
        nn.init.xavier_uniform_(self.weight)
        self.s = s
        self.m = m
    
    def forward(self, embeddings, labels):
        cosine = F.linear(F.normalize(embeddings), F.normalize(self.weight))
        theta = torch.acos(torch.clamp(cosine, -1+1e-7, 1-1e-7))
        target_logits = torch.cos(theta + self.m)
        one_hot = F.one_hot(labels, num_classes=cosine.size(1)).float()
        output = cosine * (1 - one_hot) + target_logits * one_hot
        output = output * self.s
        loss = F.cross_entropy(output, labels)
        return loss

# ----------------------
# Optional: Triplet Loss
# ----------------------
class TripletLoss(nn.Module):
    def __init__(self, margin=0.3):
        super().__init__()
        self.margin = margin

    def forward(self, anchor, positive, negative):
        pos_dist = torch.sum((anchor - positive)**2, dim=1)
        neg_dist = torch.sum((anchor - negative)**2, dim=1)
        loss = torch.relu(pos_dist - neg_dist + self.margin)
        return loss.mean()