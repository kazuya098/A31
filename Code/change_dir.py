import os
import shutil

# 待修正的文件信息（从检测报告中提取，已做好映射）
fix_files = [
    # 日期8位超出范围：02020121 → 20200121
    {"old_path": r"C:\Users\18813\A31\Data\Mandrillus\Images\242\02020121_id242.jpg", "new_date": "20200121"},
    {"old_path": r"C:\Users\18813\A31\Data\Mandrillus\Images\242\02020121_id242_2.jpg", "new_date": "20200121"},
    {"old_path": r"C:\Users\18813\A31\Data\Mandrillus\Images\242\02020121_id242_3.jpg", "new_date": "20200121"},
    {"old_path": r"C:\Users\18813\A31\Data\Mandrillus\Images\242\02020121_id242_4.jpg", "new_date": "20200121"},
    {"old_path": r"C:\Users\18813\A31\Data\Mandrillus\Images\242\02020121_id242_5.jpg", "new_date": "20200121"},
    # 日期8位格式错误：20182306 → 20181206
    {"old_path": r"C:\Users\18813\A31\Data\Mandrillus\Images\104\20182306_id104.jpg", "new_date": "20180606"},
    {"old_path": r"C:\Users\18813\A31\Data\Mandrillus\Images\104\20182306_id104_2.jpg", "new_date": "20180606"},
    {"old_path": r"C:\Users\18813\A31\Data\Mandrillus\Images\104\20182306_id104_3.jpg", "new_date": "20180606"},
    # 日期8位格式错误：20181303 → 20181203
    {"old_path": r"C:\Users\18813\A31\Data\Mandrillus\Images\16\20181303_id16.jpg", "new_date": "20180303"},
    {"old_path": r"C:\Users\18813\A31\Data\Mandrillus\Images\16\20181303_id16_2.jpg", "new_date": "20180303"},
    {"old_path": r"C:\Users\18813\A31\Data\Mandrillus\Images\16\20181303_id16_3.jpg", "new_date": "20180303"},
    {"old_path": r"C:\Users\18813\A31\Data\Mandrillus\Images\16\20181303_id16_4.jpg", "new_date": "20180303"},
   
]

def batch_rename_error_files():
    """批量重命名错误文件，保留原后缀和ID信息"""
    success_count = 0
    fail_count = 0
    fail_list = []

    print("开始批量修正文件...")
    print("-"*50)

    for file in fix_files:
        old_path = file["old_path"]
        new_date = file["new_date"]
        # 提取原文件名的后缀部分（_idXXX/_idXXX_数字.jpg）
        old_filename = os.path.basename(old_path)
        suffix_part = old_filename.split("_id")[1]  # 得到idXXX/idXXX_2.jpg等
        # 拼接新文件名
        new_filename = f"{new_date}_id{suffix_part}"
        new_path = os.path.join(os.path.dirname(old_path), new_filename)

        try:
            # 重命名文件（os.rename兼容同盘符，跨盘符用shutil.move）
            if os.path.exists(old_path):
                os.rename(old_path, new_path)
                print(f"✅ 修正成功：{old_filename} → {new_filename}")
                success_count += 1
            else:
                raise FileNotFoundError("文件不存在")
        except Exception as e:
            print(f"❌ 修正失败：{old_filename} | 错误：{str(e)}")
            fail_count += 1
            fail_list.append(old_filename)

    print("-"*50)
    print(f"修正完成！成功：{success_count}个 | 失败：{fail_count}个")
    if fail_list:
        print(f"失败文件列表：{fail_list}")
    else:
        print("🎉 所有13个错误文件均修正成功！")

if __name__ == "__main__":
    # 执行批量修正
    batch_rename_error_files()
    # 可选：修正后可重新运行之前的检测脚本，验证是否全部合法
    print("\n建议重新运行文件名检测脚本，验证修正结果！")