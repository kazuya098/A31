# train.py
import torch
from torch.utils.data import DataLoader
import pandas as pd
from torchvision import transforms
from Dataprepare import MFDataset   # 你的 Dataset 文件
from model import FaceModel, ArcFace, TripletLoss
import torch.nn.functional as F
from sklearn.metrics.pairwise import cosine_similarity
import numpy as np
from sklearn.metrics import roc_curve

torch.backends.cudnn.benchmark = True  # 加速卷积计算，适合输入尺寸固定的情况
# ----------------------
# 配置
# ----------------------
DEVICE = "cuda" if torch.cuda.is_available() else "cpu"
BATCH_SIZE = 96
EPOCHS = 50
EMBEDDING_SIZE = 512
LR = 3e-4
WEIGHT_DECAY = 1e-4
MARGIN = 0.3
ARCFACE_S = 64
ARCFACE_M = 0.5

CSV_PATH = "C:\\Users\\18813\\A31\\Data\\Mandrillus\\Images\\MFD_metadatas.csv"
IMG_DIR = "C:\\Users\\18813\\A31\\Data\\Mandrillus\\Images"

# ----------------------
# 数据增强（跨时间鲁棒）
# ----------------------
data_transforms = transforms.Compose([
    transforms.RandomResizedCrop(224, scale=(0.8,1.0)),
    transforms.RandomHorizontalFlip(),
    transforms.ColorJitter(brightness=0.2, contrast=0.2, saturation=0.2),
    transforms.RandomGrayscale(p=0.1),
    transforms.GaussianBlur(kernel_size=(3,3), sigma=(0.1,1.0)),
    transforms.ToTensor()
])

def extract_features(model, dataloader, device):
    model.eval()
    
    features = []
    labels = []
    
    with torch.no_grad():
        for imgs, lbls in dataloader:
            imgs = imgs.to(device)
            
            emb = model(imgs)           # 输出 embedding
            emb = F.normalize(emb)     # L2 normalize
            
            features.append(emb.cpu())
            labels.append(lbls)
    
    features = torch.cat(features)
    labels = torch.cat(labels)
    
    return features, labels

def compute_rank1_accuracy(features, labels):
    feats = features.numpy()
    labels = labels.numpy()
    
    sim_matrix = cosine_similarity(feats)
    correct = 0
    total = len(labels)
    
    for i in range(total):
        # ===== 关键修改：复制数组，不修改原矩阵 =====
        sim_vals = sim_matrix[i].copy()
        sim_vals[i] = -1  # 只改副本
        idx = np.argmax(sim_vals)
        
        if labels[i] == labels[idx]:
            correct += 1
    
    rank1 = correct / total
    return rank1

def compute_tar_far(features, labels, far_target=0.1):
    feats = features.numpy()
    labels = labels.numpy()
    
    # 向量化计算所有两两相似度（替代双重循环）
    sim_matrix = cosine_similarity(feats)
    # 提取上三角（排除i=j和重复对）
    mask = np.triu(np.ones_like(sim_matrix, dtype=bool), k=1)
    scores = sim_matrix[mask]
    # 生成ground truth
    label_matrix = np.equal.outer(labels, labels)
    gt = label_matrix[mask]
    
    # 计算ROC（仅保留核心逻辑）
    fpr, tpr, thresholds = roc_curve(gt, scores)
    idx = np.argmin(np.abs(fpr - far_target))
    tar = tpr[idx]
    return tar

def evaluate(model, dataloader, device):

    features, labels = extract_features(model, dataloader, device)

    rank1 = compute_rank1_accuracy(features, labels)

    tar = compute_tar_far(features, labels, far_target=0.1)

    accuracy = rank1   # 在识别任务中通常一致

    print("Evaluation Results:")
    print(f"Accuracy: {accuracy*100:.2f}%")
    print(f"Rank-1: {rank1*100:.2f}%")
    print(f"TAR@FAR=0.1: {tar*100:.2f}%")

    return accuracy, rank1, tar

def main():
    # ----------------------
    # 加载数据集
    # ----------------------
    df = pd.read_csv(CSV_PATH)
    df['Shootdate'] = pd.to_datetime(df['Shootdate'])

    train_df = df[(df['Shootdate'].dt.year >= 2012) & (df['Shootdate'].dt.year <= 2020)]
    test_df = df[df['Shootdate'].dt.year == 2021]

    train_dataset = MFDataset(train_df, IMG_DIR, transform=data_transforms)
    test_dataset = MFDataset(test_df, IMG_DIR, transform=data_transforms)

    train_loader = DataLoader(train_dataset, batch_size=BATCH_SIZE, shuffle=True, num_workers=4, pin_memory=True,persistent_workers=True,
    prefetch_factor=4)
    test_loader = DataLoader(test_dataset, batch_size=BATCH_SIZE, shuffle=False, num_workers=4, pin_memory=True,persistent_workers=True,
    prefetch_factor=4)

    print(f"训练集样本数: {len(train_dataset)}, 测试集样本数: {len(test_dataset)}")
     # ----------------------
    # 模型和损失
    # ----------------------
    num_classes = len(train_dataset.id_map)
    model = FaceModel(embedding_size=EMBEDDING_SIZE).to(DEVICE)
    arcface = ArcFace(EMBEDDING_SIZE, num_classes, s=ARCFACE_S, m=ARCFACE_M).to(DEVICE)
    #triplet_loss = TripletLoss(MARGIN)

    optimizer = torch.optim.AdamW(
            model.parameters(),
            lr=LR,
            weight_decay=WEIGHT_DECAY
        )

    # ----------------------
    # 训练循环
    # ----------------------


    scaler = torch.amp.GradScaler("cuda")

    for epoch in range(EPOCHS):

        model.train()
        total_loss = 0

        for imgs, labels in train_loader:

            imgs = imgs.to(DEVICE, non_blocking=True)
            labels = labels.to(DEVICE, non_blocking=True)

            with torch.amp.autocast("cuda"):

                embeddings = model(imgs)
                loss = arcface(embeddings, labels)

            optimizer.zero_grad()

            scaler.scale(loss).backward()
            scaler.step(optimizer)
            scaler.update()

            total_loss += loss.item()

        print(f"Epoch {epoch+1}/{EPOCHS}, Loss: {total_loss/len(train_loader):.4f}")
        #if epoch % 5 == 0:
            #evaluate(model, test_loader, DEVICE)
    torch.save(model.state_dict(), "cross_time_model.pth")
    print("训练完成，模型已保存为 cross_time_model.pth")
if __name__ == "__main__":
    main()
    # ----------------------
    # 保存模型
    # ----------------------
