import os
from datetime import datetime

def check_image_filename_format(folder_path):
    """
    检测规则：
    - 必须关注（不能忽略）：无_id分隔符、日期8位但超出1900-2200、日期8位但格式错误（如13月）、日期非8位数字
    - 可忽略：日期标注为unknown（文件名前缀非8位数字/无法解析，如abc_id89.jpg）
    """
    # 初始化统计
    total_files = 0
    must_fix_count = 0  # 必须修正的错误数
    ignore_count = 0    # 可忽略的unknown日期数
    valid_count = 0     # 完全合法的文件数

    # 错误分类存储
    must_fix_files = {
        "no_id_separator": [],        # 无_id分隔符
        "date_8bit_out_of_range": [], # 日期8位但超出1900-2200
        "date_8bit_invalid_format": [],# 日期8位但格式错误（如13月）
        "date_not_8bit_digit": []     # 日期非8位纯数字（非unknown，如201812a2）
    }
    ignore_files = []                 # 可忽略的unknown日期文件

    # 支持的图片后缀
    image_suffixes = ('.jpg', '.jpeg', '.png', '.bmp', '.gif', '.tiff',
                      '.JPG', '.JPEG', '.PNG', '.BMP', '.GIF', '.TIFF',
                      '.webp', '.WEBP')
    
    print(f"开始扫描文件夹（含子文件夹）：{folder_path}")
    print("-"*40)

    # 递归扫描所有文件
    for root, dirs, files in os.walk(folder_path):
        for filename in files:
            if not filename.endswith(image_suffixes):
                continue
            total_files += 1
            file_path = os.path.join(root, filename)
            is_valid = True
            error_type = None

            # 1. 检测是否有_id分隔符（必须关注）
            if "_id" not in filename:
                must_fix_files["no_id_separator"].append({
                    "path": file_path,
                    "name": filename,
                    "reason": "无_id分隔符（核心错误）"
                })
                must_fix_count += 1
                is_valid = False
                continue

            # 2. 拆分日期和ID部分
            date_part, id_part = filename.split("_id", 1)
            id_part = id_part.split(".")[0]

            # 3. 检测日期部分
            if len(date_part) == 8 and date_part.isdigit():
                # 日期是8位纯数字 → 必须检测合法性（不能忽略）
                try:
                    date = datetime.strptime(date_part, "%Y%m%d")
                    # 检测是否超出1900-2200
                    if not (1900 <= date.year <= 2200):
                        must_fix_files["date_8bit_out_of_range"].append({
                            "path": file_path,
                            "name": filename,
                            "reason": f"日期8位但超出范围（1900-2200）：{date.strftime('%Y-%m-%d')}"
                        })
                        must_fix_count += 1
                        is_valid = False
                except ValueError:
                    # 8位数字但格式错误（如13月、32日）
                    must_fix_files["date_8bit_invalid_format"].append({
                        "path": file_path,
                        "name": filename,
                        "reason": f"日期8位但格式错误（非法日期）：{date_part}"
                    })
                    must_fix_count += 1
                    is_valid = False
            else:
                # 日期非8位纯数字 → 区分「可忽略的unknown」和「必须关注的非8位数字」
                if date_part.isdigit() and len(date_part) != 8:
                    # 是数字但非8位（如6位、9位）→ 必须关注
                    must_fix_files["date_not_8bit_digit"].append({
                        "path": file_path,
                        "name": filename,
                        "reason": f"日期为数字但非8位：{date_part}（长度{len(date_part)}）"
                    })
                    must_fix_count += 1
                    is_valid = False
                else:
                    # 非数字（如abc、2018a12）→ 可忽略的unknown日期
                    ignore_files.append({
                        "path": file_path,
                        "name": filename,
                        "reason": f"日期标注unknown（非纯数字）：{date_part}"
                    })
                    ignore_count += 1
                    # unknown不影响合法性判定（仅标记为可忽略）

            if is_valid:
                valid_count += 1

    # 生成报告
    print("="*60)
    print("图片文件名格式检测报告（严格版）")
    print("="*60)
    print(f"📊 整体统计：")
    print(f"   总图片数：{total_files}")
    print(f"   完全合法数：{valid_count}")
    print(f"   必须修正数：{must_fix_count}")
    print(f"   可忽略（unknown日期）：{ignore_count}")

    # 输出必须修正的错误
    if must_fix_count > 0:
        print("\n🔴 必须修正的错误文件（不能忽略）：")
        for err_type, files in must_fix_files.items():
            if files:
                type_name = {
                    "no_id_separator": "无_id分隔符",
                    "date_8bit_out_of_range": "日期8位但超出1900-2200范围",
                    "date_8bit_invalid_format": "日期8位但格式错误（非法日期）",
                    "date_not_8bit_digit": "日期为数字但非8位"
                }[err_type]
                print(f"\n   📌 {type_name}（共{len(files)}个）：")
                for idx, f in enumerate(files, 1):
                    print(f"      {idx}. 文件：{f['name']}")
                    print(f"         路径：{f['path']}")
                    print(f"         原因：{f['reason']}")
    else:
        print("\n✅ 无必须修正的错误文件！")

    # 输出可忽略的unknown日期
    if ignore_count > 0:
        print(f"\n🟡 可忽略的文件（unknown日期，共{ignore_count}个，仅显示前10条）：")
        for idx, f in enumerate(ignore_files[:10], 1):
            print(f"   {idx}. 文件：{f['name']} | 原因：{f['reason']}")
        if len(ignore_files) > 10:
            print(f"   ... 剩余{len(ignore_files)-10}个unknown日期文件未显示")

if __name__ == "__main__":
    # 替换为你的图片文件夹路径
    IMAGE_FOLDER = r"C:\Users\18813\A31\Data\Mandrillus"
    
    if not os.path.exists(IMAGE_FOLDER):
        print(f"❌ 错误：文件夹 {IMAGE_FOLDER} 不存在！")
    else:
        check_image_filename_format(IMAGE_FOLDER)