"""
错误处理中间件模块
提供全局错误处理器
"""

from flask import jsonify, Blueprint


def register_error_handlers(app):
    """
    注册所有错误处理器

    Args:
        app: Flask 应用实例
    """

    @app.errorhandler(404)
    def not_found(error):
        """处理 404 错误"""
        return jsonify({'error': '文件未找到'}), 404

    @app.errorhandler(400)
    def bad_request(error):
        """处理 400 错误"""
        return jsonify({'error': '请求错误'}), 400

    @app.errorhandler(500)
    def internal_error(error):
        """处理 500 错误"""
        return jsonify({'error': '服务器内部错误'}), 500

    @app.errorhandler(Exception)
    def handle_exception(error):
        """处理未捕获的异常"""
        import logging
        logger = logging.getLogger(__name__)
        logger.exception(f"未捕获的异常: {error}")
        return jsonify({'error': f'服务器错误: {str(error)}'}), 500
