"""
日志配置模块
提供统一的日志配置功能
"""

import os
import logging


def setup_logging():
    """
    设置日志配置

    Returns:
        logging.Logger: 配置好的日志记录器
    """
    # 简化日志配置 - 使用默认值
    log_level = 'INFO'
    log_file = 'markitdown.log'

    # 确保日志目录存在
    log_dir = os.path.dirname(log_file) if os.path.dirname(log_file) else '.'
    os.makedirs(log_dir, exist_ok=True)

    logging.basicConfig(
        level=getattr(logging, log_level.upper()),
        format='%(asctime)s - %(name)s - %(levelname)s - %(message)s',
        handlers=[
            logging.FileHandler(log_file, encoding='utf-8'),
            logging.StreamHandler()  # 同时输出到控制台
        ]
    )

    logger = logging.getLogger(__name__)
    return logger
