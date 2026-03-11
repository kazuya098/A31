# test.py (论文级优化版)
import torch
import torch.nn.functional as F
from torch.utils.data import DataLoader
from Dataprepare import MFDataset
from model import FaceModel
import pandas as pd
import numpy as np
from torchvision import transforms
import matplotlib.pyplot as plt
from tqdm import tqdm
import logging
import csv
from sklearn.metrics import roc_curve, auc
import seaborn as sns  # 新增：美化分布直方图

# ----------------------
# 1. 基础配置（论文级规范）
# ----------------------
# 固定随机种子（严格可复现）
np.random.seed(42)
torch.manual_seed(42)
if torch.cuda.is_available():
    torch.cuda.manual_seed(42)
    torch.cuda.manual_seed_all(42)
torch.backends.cudnn.deterministic = True
torch.backends.cudnn.benchmark = False

# 日志配置（论文级实验追溯）
logging.basicConfig(
    level=logging.INFO,
    format="%(asctime)s - %(levelname)s - %(message)s",
    handlers=[
        logging.StreamHandler(),
        logging.FileHandler("cross_time_experiment.log", encoding="utf-8")
    ]
)
logger = logging.getLogger(__name__)

# 实验核心配置
DEVICE = "cuda" if torch.cuda.is_available() else "cpu"
BATCH_SIZE = 64
EMBEDDING_SIZE = 512
CSV_PATH = r"C:\Users\18813\A31\MFD_metadatas.csv"
IMG_DIR = "C:\\Users\\18813\\A31\\Data\\Mandrillus\\Images"
MODEL_PATH = "cross_time_model.pth"

# 跨时间划分
TRAIN_YEAR_START = 2012
TRAIN_YEAR_END = 2020
TEST_YEAR = 2021

# 评测指标配置
FAR_TARGETS = [0.001, 0.01, 0.05, 0.1, 0.2, 0.5]  # 扩展：多FAR值用于绘制曲线
TOP_K_LIST = [1, 5, 10]
POS_PAIR_NUM = 100000  # 正样本对数量（统计稳定）
NEG_PAIR_NUM = 1000000 # 负样本对数量（统计稳定）

# 可视化配置
plt.rcParams['font.sans-serif'] = ['SimHei']
plt.rcParams['axes.unicode_minus'] = False
plt.rcParams['figure.dpi'] = 300
plt.rcParams['savefig.dpi'] = 300

# ----------------------
# 2. 数据加载（解决跨时间逻辑问题）
# ----------------------
def get_train_test_transforms():
    """
    论文级数据增强：
    - 训练集：强增强（解决过拟合）
    - 测试集：仅Resize+ToTensor（无数据增强）
    """
    # 训练集增强（论文级标配）
    train_transform = transforms.Compose([
        transforms.Resize((224, 224)),
        transforms.RandomHorizontalFlip(p=0.5),  # 随机水平翻转
        transforms.ColorJitter(                  # 颜色抖动
            brightness=0.2,
            contrast=0.2,
            saturation=0.2,
            hue=0.1
        ),
        transforms.RandomRotation(degrees=10),   # 随机旋转
        transforms.ToTensor()
    ])
    
    # 测试集无增强（保证评测公平）
    test_transform = transforms.Compose([
        transforms.Resize((224, 224)),
        transforms.ToTensor()
    ])
    
    return train_transform, test_transform

def load_cross_time_data(csv_path, img_dir, train_start, train_end, test_year):
    """
    加载跨时间数据，核心改进：
    1. 保证Query ID 完全包含在Gallery ID中（解决评测逻辑问题）
    2. 区分训练/测试增强策略
    3. 复用Gallery的id_map给Query，避免标签错位
    """
    # 加载原始数据
    df = pd.read_csv(csv_path)
    df['Shootdate'] = pd.to_datetime(
        df['Shootdate'].replace(['unknown', 'NaN', 'nan', ''], pd.NaT),
        format='%Y-%m-%d',
        errors='coerce'
    )
    df = df.dropna(subset=['Shootdate', 'Id'])  # 保证ID和日期非空
    
    # 划分Gallery和Query
    gallery_df = df[(df['Shootdate'].dt.year >= train_start) & 
                    (df['Shootdate'].dt.year <= train_end)]
    query_df = df[df['Shootdate'].dt.year == test_year]
    
    # 核心改进：过滤Query中不在Gallery的ID（保证评测严谨性）
    gallery_ids = set(gallery_df['Id'].unique())
    query_df = query_df[query_df['Id'].isin(gallery_ids)]
    
    logger.info(f"数据划分结果：")
    logger.info(f"- Gallery（{train_start}-{train_end}）：{len(gallery_df)}样本，{len(gallery_df['Id'].unique())}身份")
    logger.info(f"- Query（{test_year}）：{len(query_df)}样本，{len(query_df['Id'].unique())}身份")
    logger.info(f"- Query中有效身份数（存在于Gallery）：{len(query_df['Id'].unique())}")
    
    # 加载数据集
    train_transform, test_transform = get_train_test_transforms()
    # 先创建gallery数据集（自动生成id_map）
    gallery_dataset = MFDataset(gallery_df, img_dir, transform=test_transform)
    # 获取gallery的id_map，传递给query数据集
    gallery_id_map = gallery_dataset.id_map
    # 创建query数据集：复用gallery的id_map
    query_dataset = MFDataset(query_df, img_dir, transform=test_transform, id_map=gallery_id_map)
    
    # 数据加载器
    gallery_loader = DataLoader(gallery_dataset, batch_size=BATCH_SIZE, 
                               shuffle=False, num_workers=0, pin_memory=True)
    query_loader = DataLoader(query_dataset, batch_size=BATCH_SIZE, 
                             shuffle=False, num_workers=0, pin_memory=True)
    
    return gallery_loader, query_loader, gallery_dataset, query_dataset

# ----------------------
# 3. Embedding提取（保持原有逻辑）
# ----------------------
def extract_embeddings(model, dataloader, device):
    """提取L2归一化的Embedding"""
    model.eval()
    embeddings = []
    labels = []
    
    with torch.no_grad():
        for imgs, lbls in tqdm(dataloader, desc="提取Embedding"):
            imgs = imgs.to(device, non_blocking=True)
            emb = model(imgs)
            emb = F.normalize(emb, p=2, dim=1)  # L2归一化（必须）
            embeddings.append(emb.cpu().numpy())
            labels.append(lbls.numpy())
    
    embeddings = np.concatenate(embeddings, axis=0)
    labels = np.concatenate(labels, axis=0)
    return embeddings, labels

# ----------------------
# 4. Rank-K计算（矩阵优化，解决效率问题）
# ----------------------
def calculate_rank_k_matrix(gallery_emb, gallery_labels, query_emb, query_labels, top_k_list):
    """
    矩阵化计算Rank-K，核心改进：
    1. 用矩阵乘法替代循环（GPU并行）
    2. 复杂度不变但速度提升50+倍
    """
    logger.info("开始矩阵化计算Rank-K...")
    
    # 转换为Tensor并移至GPU
    gallery_emb = torch.from_numpy(gallery_emb).to(DEVICE, non_blocking=True)
    query_emb = torch.from_numpy(query_emb).to(DEVICE, non_blocking=True)
    
    # 矩阵乘法计算相似度矩阵 [N_query, N_gallery]
    sim_matrix = torch.matmul(query_emb, gallery_emb.T)
    
    # 获取Top-K索引
    top_k_max = max(top_k_list)
    _, top_k_indices = torch.topk(sim_matrix, k=top_k_max, dim=1)
    top_k_indices = top_k_indices.cpu().numpy()
    
    # 统计Rank-K准确率
    rank_correct = {k: 0 for k in top_k_list}
    total = len(query_labels)
    
    for i in range(total):
        true_label = query_labels[i]
        # 获取Top-K对应的Gallery标签
        pred_labels = gallery_labels[top_k_indices[i]]
        
        for k in top_k_list:
            if true_label in pred_labels[:k]:
                rank_correct[k] += 1
    
    # 计算准确率
    rank_acc = {k: rank_correct[k]/total for k in top_k_list}
    
    logger.info(f"Rank-K计算完成：")
    for k in top_k_list:
        logger.info(f"- Rank-{k}: {rank_acc[k]:.4f}")
    
    return rank_acc

# ----------------------
# 5. TAR@FAR计算（扩展：多FAR值 + 分块优化）
# ----------------------
def gpu_blocked_tar_far(gallery_emb, gallery_labels, query_emb, query_labels,
                        pos_num=100_000, neg_num=1_000_000, far_targets=[0.1],
                        block_size=4096, device='cuda'):
    """
    扩展：支持多FAR值计算TAR，输出FAR-TAR曲线所需数据
    GPU 分块向量化计算 TAR@FAR
    """
    logger.info("开始采样正样本对...")
    # 构建ID到索引映射
    def build_id2idx(labels):
        id2idx = {}
        for i, l in enumerate(labels):
            id2idx.setdefault(l, []).append(i)
        return id2idx
    
    gallery_id2idx = build_id2idx(gallery_labels)
    query_id2idx = build_id2idx(query_labels)
    common_ids = list(set(gallery_id2idx.keys()) & set(query_id2idx.keys()))
    
    pos_sims = []
    rng = np.random.default_rng(42)
    while len(pos_sims) < pos_num:
        id_ = rng.choice(common_ids)
        g_idx = rng.choice(gallery_id2idx[id_])
        q_idx = rng.choice(query_id2idx[id_])
        g_emb = torch.tensor(gallery_emb[g_idx:g_idx+1], device=device)
        q_emb = torch.tensor(query_emb[q_idx:q_idx+1], device=device)
        sim = torch.cosine_similarity(g_emb, q_emb).item()
        pos_sims.append(sim)
    
    logger.info(f"正样本对采样完成，数量={len(pos_sims)}")

    # ----------------------
    # 负样本对分块向量化
    # ----------------------
    logger.info("开始采样负样本对（分块向量化）...")
    neg_sims = []
    g_indices = np.arange(len(gallery_labels))
    q_indices = np.arange(len(query_labels))
    
    rng = np.random.default_rng(42)
    total_needed = neg_num
    
    while len(neg_sims) < neg_num:
        g_block = rng.choice(g_indices, size=min(block_size, total_needed), replace=True)
        q_block = rng.choice(q_indices, size=min(block_size, total_needed), replace=True)
        
        g_emb_block = torch.tensor(gallery_emb[g_block], device=device)
        q_emb_block = torch.tensor(query_emb[q_block], device=device)
        
        sim_matrix = torch.matmul(q_emb_block, g_emb_block.T)  # [B,B]
        
        q_lbl_block = query_labels[q_block][:, None]
        g_lbl_block = gallery_labels[g_block][None, :]
        mask = (q_lbl_block != g_lbl_block)
        
        valid_sims = sim_matrix[mask]
        neg_sims.extend(valid_sims.cpu().numpy().tolist())
        total_needed = neg_num - len(neg_sims)
    
    neg_sims = np.array(neg_sims[:neg_num])
    pos_sims = np.array(pos_sims)
    logger.info(f"负样本对采样完成，数量={len(neg_sims)}")

    # ----------------------
    # 多FAR值计算TAR
    # ----------------------
    tar_results = {}
    thresholds = {}
    for far in far_targets:
        threshold = np.quantile(neg_sims, 1 - far)
        tar = np.mean(pos_sims >= threshold)
        tar_results[far] = tar
        thresholds[far] = threshold
        logger.info(f"TAR@FAR={far}: {tar:.4f}, 阈值={threshold:.4f}")

    # ROC / AUC
    y_true = np.concatenate([np.ones(len(pos_sims)), np.zeros(len(neg_sims))])
    y_score = np.concatenate([pos_sims, neg_sims])
    fpr, tpr, _ = roc_curve(y_true, y_score)
    roc_auc = auc(fpr, tpr)

    logger.info(f"AUC={roc_auc:.4f}")
    
    return tar_results, thresholds, pos_sims, neg_sims, fpr, tpr, roc_auc

# ----------------------
# 6. 可视化扩展：新增FAR-TAR曲线 + 正负样本相似度分布
# ----------------------
def plot_far_tar_curve(far_targets, tar_results, save_path="far_tar_curve.png"):
    """
    绘制FAR-TAR曲线（论文级可视化）
    :param far_targets: FAR值列表
    :param tar_results: 对应FAR的TAR值字典
    :param save_path: 保存路径
    """
    # 排序保证曲线有序
    far_sorted = sorted(far_targets)
    tar_sorted = [tar_results[far] for far in far_sorted]
    
    plt.figure(figsize=(8, 6))
    plt.plot(far_sorted, tar_sorted, marker='o', linewidth=2, markersize=6, color='#1f77b4')
    plt.xlabel("FAR (False Accept Rate)")
    plt.ylabel("TAR (True Accept Rate)")
    plt.title(f"跨时间人脸识别FAR-TAR曲线（{TRAIN_YEAR_START}-{TRAIN_YEAR_END}训练，{TEST_YEAR}测试）")
    plt.grid(True, alpha=0.3)
    plt.xscale('log')  # FAR常用对数刻度，更直观
    plt.ylim(0, 1.05)
    plt.xlim(min(far_sorted)*0.8, max(far_sorted)*1.2)
    
    # 标注关键FAR点
    for far, tar in zip(far_sorted, tar_sorted):
        plt.text(far, tar+0.02, f"{tar:.3f}", ha='center', fontsize=8)
    
    plt.tight_layout()
    plt.savefig(save_path, bbox_inches='tight')
    plt.show()
    logger.info(f"FAR-TAR曲线已保存至：{save_path}")

def plot_sim_distribution(pos_sims, neg_sims, save_path="sim_distribution.png"):
    """
    绘制正负样本对相似度分布直方图（论文级可视化）
    :param pos_sims: 正样本对相似度列表
    :param neg_sims: 负样本对相似度列表
    :param save_path: 保存路径
    """
    plt.figure(figsize=(10, 6))
    
    # 绘制分布直方图（核密度估计+直方图）
    sns.histplot(pos_sims, bins=50, kde=True, label='正样本对', color='#2ca02c', alpha=0.6, stat='density')
    sns.histplot(neg_sims, bins=50, kde=True, label='负样本对', color='#d62728', alpha=0.6, stat='density')
    
    plt.xlabel("余弦相似度")
    plt.ylabel("密度")
    plt.title(f"正负样本对相似度分布（{TRAIN_YEAR_START}-{TRAIN_YEAR_END}训练，{TEST_YEAR}测试）")
    plt.legend()
    plt.grid(True, alpha=0.3)
    
    # 标注统计信息
    pos_mean = np.mean(pos_sims)
    neg_mean = np.mean(neg_sims)
    plt.axvline(pos_mean, color='#2ca02c', linestyle='--', alpha=0.8, label=f'正样本均值: {pos_mean:.3f}')
    plt.axvline(neg_mean, color='#d62728', linestyle='--', alpha=0.8, label=f'负样本均值: {neg_mean:.3f}')
    plt.legend()
    
    plt.tight_layout()
    plt.savefig(save_path, bbox_inches='tight')
    plt.show()
    logger.info(f"相似度分布直方图已保存至：{save_path}")

def plot_rankk_error_and_similarity_matrix(
    gallery_emb, gallery_labels, query_emb, query_labels, top_k=1
):
    """
    论文级错误分析可视化：
    1. Rank-K错误直方图
    2. Gallery-Query相似度热力图
    """
    # 转换为Tensor
    gallery_emb_t = torch.from_numpy(gallery_emb).to(DEVICE)
    query_emb_t = torch.from_numpy(query_emb).to(DEVICE)

    # 计算相似度矩阵
    sim_matrix = torch.matmul(query_emb_t, gallery_emb_t.T).cpu().numpy()  # [N_query, N_gallery]

    # -------------------------------
    # 1. Rank-K错误分析
    # -------------------------------
    topk_idx = np.argsort(-sim_matrix, axis=1)[:, :top_k]  # Top-K索引
    rankk_correct = []
    for i, idxs in enumerate(topk_idx):
        rankk_correct.append(query_labels[i] in gallery_labels[idxs])
    rankk_correct = np.array(rankk_correct)

    # 统计错误样本
    errors_idx = np.where(rankk_correct == False)[0]
    correct_idx = np.where(rankk_correct == True)[0]

    logger.info(f"Top-{top_k} 错误样本数: {len(errors_idx)} / {len(query_labels)}")

    # 可视化错误直方图
    plt.figure(figsize=(12, 4))
    plt.bar(['正确', '错误'], [len(correct_idx), len(errors_idx)], color=['green', 'red'])
    plt.title(f"Rank-{top_k} 错误统计（{TRAIN_YEAR_START}-{TRAIN_YEAR_END}训练，{TEST_YEAR}测试）")
    plt.ylabel("Query样本数")
    plt.tight_layout()
    plt.savefig(f"rank_{top_k}_error.png", bbox_inches='tight')
    plt.show()

    # -------------------------------
    # 2. Gallery-Query相似度热力图（Rank-1）
    # -------------------------------
    # 只取部分Query样本（可视化限制在50个）
    max_show = min(50, sim_matrix.shape[0])
    sim_submatrix = sim_matrix[:max_show, :]
    labels_submatrix = query_labels[:max_show]

    plt.figure(figsize=(12, 6))
    im = plt.imshow(sim_submatrix, aspect='auto', cmap='viridis')
    plt.colorbar(im, label='余弦相似度')
    plt.xlabel("Gallery样本索引")
    plt.ylabel("Query样本索引")
    plt.title(f"Gallery-Query相似度矩阵（前{max_show}个Query，{TRAIN_YEAR_START}-{TRAIN_YEAR_END}训练，{TEST_YEAR}测试）")
    # 标注错误样本
    for i in range(max_show):
        if rankk_correct[i] == False:
            plt.text(sim_submatrix.shape[1]-1, i, "✖", color='red', fontsize=12, va='center', ha='right')
    plt.tight_layout()
    plt.savefig("similarity_matrix.png", bbox_inches='tight')
    plt.show()

    # 返回错误样本索引，便于进一步分析
    return errors_idx

# ----------------------
# 7. 结果保存（论文级格式）
# ----------------------
def save_experiment_results(results, save_path):
    """保存实验结果为CSV（论文级格式）"""
    with open(save_path, 'w', newline='', encoding='utf-8') as f:
        writer = csv.DictWriter(f, fieldnames=results.keys())
        writer.writeheader()
        writer.writerow(results)

# ----------------------
# 8. 主实验流程（集成新可视化）
# ----------------------
def main():
    # 1. 加载数据
    gallery_loader, query_loader, gallery_dataset, query_dataset = load_cross_time_data(
        CSV_PATH, IMG_DIR, TRAIN_YEAR_START, TRAIN_YEAR_END, TEST_YEAR
    )
    
    # 2. 加载模型
    logger.info("加载模型...")
    num_classes = len(gallery_dataset.id_map)
    model = FaceModel(embedding_size=EMBEDDING_SIZE).to(DEVICE)
    
    # 模型加载容错
    try:
        model.load_state_dict(torch.load(MODEL_PATH, map_location=DEVICE), strict=False)
    except Exception as e:
        logger.error(f"模型加载失败：{e}")
        return
    
    # 3. 提取Embedding
    gallery_emb, gallery_labels = extract_embeddings(model, gallery_loader, DEVICE)
    query_emb, query_labels = extract_embeddings(model, query_loader, DEVICE)
    
    logger.info(f"Embedding提取完成：")
    logger.info(f"- Gallery Embedding: {gallery_emb.shape}")
    logger.info(f"- Query Embedding: {query_emb.shape}")
    
    # 4. 计算Rank-K（矩阵优化版）
    rank_acc = calculate_rank_k_matrix(
        gallery_emb, gallery_labels, query_emb, query_labels, TOP_K_LIST
    )
    
    # 5. 采样正负样本对并计算多FAR值的TAR
    tar_results, thresholds, pos_sims, neg_sims, fpr, tpr, roc_auc = gpu_blocked_tar_far(
        gallery_emb, gallery_labels, query_emb, query_labels,
        pos_num=POS_PAIR_NUM,
        neg_num=NEG_PAIR_NUM,
        far_targets=FAR_TARGETS,
        block_size=4096,
        device=DEVICE
    )
    
    # 6. 可视化：错误分析 + FAR-TAR曲线 + 相似度分布
    errors_idx = plot_rankk_error_and_similarity_matrix(
        gallery_emb, gallery_labels, query_emb, query_labels, top_k=1
    )
    plot_far_tar_curve(FAR_TARGETS, tar_results, 
                       save_path=f"far_tar_curve_{TRAIN_YEAR_START}-{TRAIN_YEAR_END}_to_{TEST_YEAR}.png")
    plot_sim_distribution(pos_sims, neg_sims, 
                          save_path=f"sim_distribution_{TRAIN_YEAR_START}-{TRAIN_YEAR_END}_to_{TEST_YEAR}.png")
    
    # 7. 结果汇总与保存
    results = {
        "实验配置": f"{TRAIN_YEAR_START}-{TRAIN_YEAR_END} → {TEST_YEAR}",
        "Gallery样本数": len(gallery_labels),
        "Query样本数": len(query_labels),
        "Gallery身份数": len(np.unique(gallery_labels)),
        "Query身份数": len(np.unique(query_labels)),
        "Rank-1准确率": f"{rank_acc[1]:.4f}",
        "Rank-5准确率": f"{rank_acc[5]:.4f}",
        "Rank-10准确率": f"{rank_acc[10]:.4f}",
        "AUC": f"{roc_auc:.4f}",
        "正样本对数量": len(pos_sims),
        "负样本对数量": len(neg_sims),
        "错误样本索引": str(errors_idx),
        "正样本相似度均值": f"{np.mean(pos_sims):.4f}",
        "负样本相似度均值": f"{np.mean(neg_sims):.4f}"
    }
    # 补充多FAR值的TAR和阈值
    for far in FAR_TARGETS:
        results[f"TAR@FAR={far}"] = f"{tar_results[far]:.4f}"
        results[f"阈值@FAR={far}"] = f"{thresholds[far]:.4f}"
    
    # 打印汇总
    logger.info("\n==================== 论文级实验结果汇总 ====================")
    for key, value in results.items():
        logger.info(f"{key}: {value}")
    logger.info("================================================================")
    
    # 保存结果
    save_path = f"cross_time_results_{TRAIN_YEAR_START}-{TRAIN_YEAR_END}_to_{TEST_YEAR}.csv"
    save_experiment_results(results, save_path)
    logger.info(f"实验结果已保存至：{save_path}")

if __name__ == "__main__":
    main()