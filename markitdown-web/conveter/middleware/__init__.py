"""
中间件模块
包含全局中间件和错误处理器
"""

from .error_handlers import register_error_handlers

__all__ = ['register_error_handlers']
