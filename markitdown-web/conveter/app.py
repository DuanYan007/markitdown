#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
MarkItDown Web Application - 主应用入口
文档转换服务，支持多种格式转换为 Markdown
"""

from flask import Flask
import logging

# 导入核心模块
from core.extensions import init_extensions, get_logger, config_manager
from api import register_blueprints
from middleware.error_handlers import register_error_handlers


def create_app():
    """
    应用工厂函数

    创建并配置 Flask 应用实例

    Returns:
        Flask: 配置好的 Flask 应用实例
    """
    app = Flask(__name__)

    # 初始化扩展和配置
    logger = init_extensions(app)

    # 注册蓝图
    register_blueprints(app)

    # 注册错误处理器
    register_error_handlers(app)

    # 配置变化回调
    def on_config_change(old_config, new_config):
        """配置变化回调函数"""
        logger.info("检测到配置变化，更新应用配置...")

        # 更新Flask配置
        from config_manager import config_manager
        flask_config = config_manager.get_flask_config()
        app.config.update(flask_config)

        # 获取存储路径配置并处理绝对路径
        import os
        upload_folder = config_manager.get('storage.upload_folder')
        download_folder = config_manager.get('storage.download_folder')

        if not os.path.isabs(upload_folder):
            upload_folder = os.path.abspath(upload_folder)
        if not os.path.isabs(download_folder):
            download_folder = os.path.abspath(download_folder)

        # 确保目录存在
        os.makedirs(upload_folder, exist_ok=True)
        os.makedirs(download_folder, exist_ok=True)

        # 更新应用配置
        app.config['UPLOAD_FOLDER'] = upload_folder
        app.config['DOWNLOAD_FOLDER'] = download_folder
        app.config['HISTORY_FILE'] = config_manager.get('storage.history_file', 'history.json')

        logger.info(f"应用配置已更新 - 上传目录: {upload_folder}, 下载目录: {download_folder}")

    # 注册配置变化回调
    config_manager.add_callback(on_config_change)

    return app


if __name__ == '__main__':
    # 创建应用实例
    app = create_app()

    # 从配置获取运行参数
    host = config_manager.get('app.host', '0.0.0.0')
    port = config_manager.get('app.port', 5000)
    debug = config_manager.get('app.debug', True)

    # 启动应用
    try:
        app.run(debug=debug, host=host, port=port)
    except KeyboardInterrupt:
        logging.info("应用已停止")
    finally:
        # 停止配置监控
        config_manager.stop_watching()
