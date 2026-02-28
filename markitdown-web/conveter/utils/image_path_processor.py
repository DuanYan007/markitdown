"""
图片路径处理模块
提供图片路径转换功能
"""

import os
import re


def process_images_to_relative_paths_for_web(content: str) -> str:
    """
    为网页渲染将绝对路径转换为相对路径

    Args:
        content: Markdown 内容

    Returns:
        str: 处理后的内容
    """
    try:
        # 获取当前工作目录
        current_dir = os.getcwd()

        # 处理HTML img标签中的绝对路径
        # 匹配 <img src="O:/Project/.../images/xxx.jpg"> 格式
        img_pattern = r'<img\s+src="([^"]*images/[^"]+)"'

        def replace_img_src(match):
            abs_path = match.group(1)
            # 提取images/xxx.jpg部分
            if 'images/' in abs_path:
                rel_path = abs_path.split('images/')[-1]
                return f'<img src="images/{rel_path}"'
            return match.group(0)

        content = re.sub(img_pattern, replace_img_src, content)

        # 处理markdown格式的图片路径 ![alt](O:/Project/.../images/xxx.jpg)
        md_img_pattern = r'!\[(.*?)\]\(([^)]*images/[^)]+)\)'

        def replace_md_img(match):
            alt_text = match.group(1)
            abs_path = match.group(2)
            # 提取images/xxx.jpg部分
            if 'images/' in abs_path:
                rel_path = abs_path.split('images/')[-1]
                return f'![{alt_text}](images/{rel_path})'
            return match.group(0)

        content = re.sub(md_img_pattern, replace_md_img, content)

        print(f"[信息] 已将绝对路径转换为相对路径用于网页渲染")
        return content

    except Exception as e:
        print(f"[警告] 处理网页渲染路径转换时出错: {str(e)}")
        return content


__all__ = ['process_images_to_relative_paths_for_web']
