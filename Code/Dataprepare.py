import pandas as pd
from datetime import datetime
import torch
from torch.utils.data import Dataset, DataLoader, random_split, Subset
from torchvision import transforms
from PIL import Image
import os
import matplotlib.pyplot as plt
# 创建数据集
from torch.utils.data import Dataset
from PIL import Image
import os
import torch

class MFDataset(Dataset):

    def __init__(self, dataframe, img_dir, transform=None,id_map=None):

        self.dataframe = dataframe
        self.img_dir = img_dir
        self.transform = transform

        # ------- 新增：把 pandas 列转成 list（避免 iloc） -------
        self.photo_names = dataframe['Photo_Name'].astype(str).str.strip().tolist()
        self.ids_raw = dataframe['Id'].tolist()

        # ------- 原有代码保持 -------
        self.img_path_map = {}

        print(f"正在扫描 {img_dir} 目录下的所有图片...")

        for root, dirs, files in os.walk(img_dir):
            for file in files:
                if file.lower().endswith('.jpg'):
                    self.img_path_map[file] = os.path.join(root, file)
        # 创建 id_map：优先使用外部传入的，否则自动生成
        if id_map is not None:
            self.id_map = id_map  # 复用gallery的id_map
            self.ids = sorted(self.id_map.keys())  # 同步ids列表
        else:
            self.ids = sorted(dataframe['Id'].unique())
            self.id_map = {id_val: idx for idx, id_val in enumerate(self.ids)}
        self.num_classes = len(self.ids)

        

    def __len__(self):
        return len(self.photo_names)

    def __getitem__(self, idx):

        if torch.is_tensor(idx):
            idx = idx.tolist()

        # ------- 改动：不再用 dataframe.iloc -------
        img_name = self.photo_names[idx]
        id_val = self.ids_raw[idx]

        img_path = self.img_path_map.get(img_name)

        # 加载图片
        try:
            image = Image.open(img_path).convert('RGB')
        except:
            print(f"警告：图片未找到 {img_name}")
            image = Image.new('RGB', (224, 224), color='black')

        # transform
        if self.transform:
            image = self.transform(image)

        label = self.id_map[id_val]

        return image, label
    
if __name__ == "__main__":
    # 使用相对路径
    base_dir = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
    csv_path = "./MFD_metadatas.csv"
    img_directory = os.path.join(base_dir, "Data", "Mandrillus")
    
    # 检查文件是否存在
    if not os.path.exists(csv_path):
        print(f"错误：CSV文件不存在 {csv_path}")
        # 尝试创建一个示例CSV文件
        import pandas as pd
        import glob
        
        # 收集所有图片文件
        image_files = glob.glob(os.path.join(img_directory, "**", "*.jpg"), recursive=True)
        data = []
        for img_path in image_files:
            filename = os.path.basename(img_path)
            # 从文件名中提取Id
            if "_id" in filename:
                id_suffix_part = filename.split("_id")[1]  # 提前初始化并赋值
                id_suffix_part = id_suffix_part.split(".")[0] if len(id_suffix_part) > 0 else ""  # 判断长度
                pure_id = id_suffix_part.split("_")[0] if len(id_suffix_part) > 0 else ""  # 容错提取
                id_str = pure_id
                # 从文件名中提取日期
                date_str = filename.split("_id")[0]
                
                try:
                    date = datetime.strptime(date_str, "%Y%m%d").strftime("%Y-%m-%d")
                except:
                    date = "unknown"
                data.append({"Photo_Name": filename, "Id": id_str, "Shootdate": date})
        
        # 创建DataFrame并保存
        df = pd.DataFrame(data)
        df.to_csv(csv_path, index=False)
        print(f"已创建示例CSV文件 {csv_path}")
    
    df = pd.read_csv(csv_path) 
    # 查看列名和数据样例，确保正确加载
    print(df.columns)
    print(df.head())

    # 将Shootdate列转换为datetime类型（容错unknown）
    df['Shootdate'] = pd.to_datetime(df['Shootdate'], format='%Y-%m-%d', errors='coerce')
    # 剔除Shootdate为unknown/NaT的行
    df = df[~df['Shootdate'].isna()]
    df = df[df['Shootdate'].astype(str) != 'unknown']
    print(f"剔除unknown时间后剩余样本数: {len(df)}")

    # 根据Shootdate的年份进行划分
    train_df = df[(df['Shootdate'].dt.year >= 2012) & (df['Shootdate'].dt.year <= 2020)]
    test_df = df[(df['Shootdate'].dt.year == 2021) ]

    print(f"训练集样本数: {len(train_df)}")
    print(f"测试集样本数: {len(test_df)}")

    data_transforms = transforms.Compose([
        transforms.Resize((224, 224)),
        transforms.ToTensor(), # 转为PyTorch张量，并缩放到[0,1]
        ])
    
    # 创建训练集和测试集Dataset实例
    train_dataset = MFDataset(dataframe=train_df, img_dir=img_directory, transform=data_transforms)
    test_dataset = MFDataset(dataframe=test_df, img_dir=img_directory, transform=data_transforms)
    print(f"训练集Dataset大小: {len(train_dataset)}")
    print(f"测试集Dataset大小: {len(test_dataset)}")
    print(f"训练集类别数: {train_dataset.num_classes}")

    # 检查是否有遗漏的样本
    other_df = df[~df.index.isin(train_df.index) & ~df.index.isin(test_df.index)]
    print(f"未包含在划分中的样本数: {len(other_df)}")
    

