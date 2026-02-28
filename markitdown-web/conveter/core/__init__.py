"""
核心配置模块
包含 Flask 应用初始化和扩展配置
"""

from .extensions import init_extensions, get_logger

__all__ = ['init_extensions', 'get_logger']
