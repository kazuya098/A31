import pandas as pd
from datetime import datetime
import torch
from torch.utils.data import Dataset, DataLoader, random_split, Subset
from torchvision import transforms
from PIL import Image
import os
import matplotlib.pyplot as plt
# 创建数据集
class MFDataset(Dataset):
    def __init__(self, dataframe, img_dir, transform=None):
        
        self.dataframe = dataframe# 包含图片文件名(Photo_Name)和其他元数据的DataFrame
        self.img_dir = img_dir# 图片存储的根目录路径
        self.transform = transform# 可选的图片变换/增强函数

        self.img_path_map = {}
        print(f"正在扫描 {img_dir} 目录下的所有图片...")
        for root, dirs, files in os.walk(img_dir):
            for file in files:
                if file.lower().endswith(('.jpg')):
                    self.img_path_map[file] = os.path.join(root, file)

    def __len__(self):
        return len(self.dataframe)
    # 单一图像提取测试
    def __getitem__(self, idx):
        if torch.is_tensor(idx):
            idx = idx.tolist()

        # 获取文件名
        img_name = self.dataframe.iloc[idx]['Photo_Name'].strip()
        img_path = self.img_path_map.get(img_name)

        # 加载图片
        try:
            image = Image.open(img_path).convert('RGB') # 确保为RGB三通道
        except FileNotFoundError:
            print(f"警告：图片未找到 {img_path}")
            # 返回一个占位符或抛出异常，这里简单返回一个空白图片
            image = Image.new('RGB', (224, 224), color='black')

        # 应用变换
        if self.transform:
            image = self.transform(image)

        # 您可以在这里返回标签。例如，如果您想用Id作为分类标签：
        # label = self.dataframe.iloc[idx]['Id']
        # 但根据您的任务，标签可能不同。这里以Id为例。
        label = int(self.dataframe.iloc[idx]['Id'])
        # 返回图片张量和标签
        return image, label
    
if __name__ == "__main__":
    df = pd.read_csv('MFD_metadatas.csv') 
    # 查看列名和数据样例，确保正确加载
    print(df.columns)
    print(df.head())

    # 将Shootdate列转换为datetime类型，便于按年份筛选
    df['Shootdate'] = pd.to_datetime(df['Shootdate'], format='%Y-%m-%d')

    # 根据Shootdate的年份进行划分
    train_df = df[(df['Shootdate'].dt.year >= 2012) & (df['Shootdate'].dt.year <= 2020)]
    test_df = df[(df['Shootdate'].dt.year == 2021) ]

    print(f"训练集样本数: {len(train_df)}")
    print(f"测试集样本数: {len(test_df)}")

    data_transforms = transforms.Compose([
        transforms.Resize((224, 224)),
        transforms.ToTensor(), # 转为PyTorch张量，并缩放到[0,1]
        ])
    
    # 图像集路径：通过环境变量或命令行参数指定，避免硬编码本地路径
    img_directory = os.environ.get('MANDRILL_IMAGE_DIR', r'./Data/Mandrillus/examples')

    # 创建训练集和测试集Dataset实例
    train_dataset = MFDataset(dataframe=train_df, img_dir=img_directory, transform=data_transforms)
    test_dataset = MFDataset(dataframe=test_df, img_dir=img_directory, transform=data_transforms)
    print(f"训练集Dataset大小: {len(train_dataset)}")
    print(f"测试集Dataset大小: {len(test_dataset)}")

    # 检查是否有遗漏的样本
    other_df = df[~df.index.isin(train_df.index) & ~df.index.isin(test_df.index)]
    print(f"未包含在划分中的样本数: {len(other_df)}")
    
    # 创建DataLoader
    batch_size=32
    train_loader = DataLoader(train_dataset, batch_size=batch_size, shuffle=True, num_workers=0, pin_memory=True)
    test_loader = DataLoader(test_dataset, batch_size=batch_size, shuffle=False, num_workers=0, pin_memory=True)

#========下面是我的调试代码，记得删掉==========

    # 测试读取一个 batch
    print("=== 测试数据加载 ===")
    images, labels = next(iter(train_loader))
    print(f"图片张量形状：{images.shape}")  # 应为 [32, 3, H, W]
    print(f"标签形状：{labels.shape}")      # 应为 [32]
    print(f"图片像素范围：[{images.min():.3f}, {images.max():.3f}]")

    # 可视化一张图片（可选）

    img = images[0].permute(1, 2, 0).numpy()  # 转为 HWC 格式
    plt.imshow(img)
    plt.title(f"Label: {labels[0]}")
    plt.show()

    for i, (imgs, lbls) in enumerate(train_loader):
        print(f"Batch {i}: 图片形状 {imgs.shape}, 标签形状 {lbls.shape}")
        if i >= 2:  # 只测试前 3 个 batch
            break
    print("数据加载测试完成！")