"""
API 蓝图模块
包含所有路由蓝图的定义和注册
"""

from flask import Blueprint

# 导入所有蓝图
from .main import main_bp
from .conversion import conversion_bp
from .batch import batch_bp
from .config import config_bp
from .history import history_bp
from .files import files_bp


def register_blueprints(app):
    """
    注册所有蓝图到 Flask 应用

    Args:
        app: Flask 应用实例
    """
    app.register_blueprint(main_bp)
    app.register_blueprint(conversion_bp)
    app.register_blueprint(batch_bp)
    app.register_blueprint(config_bp, url_prefix='/api')
    app.register_blueprint(history_bp, url_prefix='/api')
    app.register_blueprint(files_bp)


__all__ = [
    'register_blueprints',
    'main_bp',
    'conversion_bp',
    'batch_bp',
    'config_bp',
    'history_bp',
    'files_bp',
]
