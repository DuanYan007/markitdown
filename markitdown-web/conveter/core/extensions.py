"""
Flask 扩展初始化模块
负责初始化 Flask 应用和相关扩展
"""

import os
import sys
import logging
from flask import Flask

# 确保当前目录在 Python 路径中，以便导入同目录下的模块
_current_dir = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
if _current_dir not in sys.path:
    sys.path.insert(0, _current_dir)

from config_manager import config_manager, get_config


# 全局 logger 实例
_logger = None


def init_extensions(app: Flask):
    """
    初始化 Flask 应用和相关扩展

    Args:
        app: Flask 应用实例

    Returns:
        logging.Logger: 应用日志记录器
    """
    global _logger

    # 获取 Flask 配置
    flask_config = config_manager.get_flask_config()
    app.config.update(flask_config)

    # 获取存储路径配置
    upload_folder = get_config('storage.upload_folder')
    download_folder = get_config('storage.download_folder')

    # 处理绝对路径：如果路径不是绝对路径，则相对于当前工作目录
    if not os.path.isabs(upload_folder):
        upload_folder = os.path.abspath(upload_folder)
    if not os.path.isabs(download_folder):
        download_folder = os.path.abspath(download_folder)

    # 确保必要的目录存在
    os.makedirs(upload_folder, exist_ok=True)
    os.makedirs(download_folder, exist_ok=True)

    # 更新应用配置中的绝对路径
    app.config['UPLOAD_FOLDER'] = upload_folder
    app.config['DOWNLOAD_FOLDER'] = download_folder

    # 配置文件路径 - 使用函数动态获取
    app.config['HISTORY_FILE'] = get_config('storage.history_file', 'history.json')
    app.config['BATCH_STATUS_FILE'] = 'batch_status.json'

    # 设置日志
    from utils.logging_config import setup_logging
    _logger = setup_logging()
    _logger.info("MarkItDown Web 应用启动完成")
    _logger.info(f"上传目录: {upload_folder}")
    _logger.info(f"下载目录: {download_folder}")
    max_file_size = get_config('limits.max_file_size', 104857600)
    max_file_size_mb = max_file_size / (1024 * 1024)
    _logger.info(f"最大文件大小: {max_file_size_mb:.1f}MB")

    return _logger


def get_logger():
    """
    获取应用日志记录器

    Returns:
        logging.Logger: 应用日志记录器
    """
    global _logger
    if _logger is None:
        _logger = logging.getLogger(__name__)
    return _logger
