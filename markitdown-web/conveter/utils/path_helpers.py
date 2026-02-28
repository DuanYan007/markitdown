"""
路径处理工具模块
提供文件路径相关的处理函数
"""

import os


def format_file_size(size_bytes: int) -> str:
    """
    格式化文件大小为人类可读的格式

    Args:
        size_bytes: 文件大小（字节）

    Returns:
        str: 格式化后的文件大小字符串
    """
    if size_bytes < 1024:
        return f"{size_bytes} B"
    elif size_bytes < 1024 * 1024:
        return f"{size_bytes / 1024:.1f} KB"
    elif size_bytes < 1024 * 1024 * 1024:
        return f"{size_bytes / (1024 * 1024):.1f} MB"
    else:
        return f"{size_bytes / (1024 * 1024 * 1024):.1f} GB"


# 从 services/format_service 导入，保持向后兼容
from services.format_service import detect_file_format, is_supported_file

__all__ = [
    'format_file_size',
    'detect_file_format',
    'is_supported_file',
]
