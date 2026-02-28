"""
主页面路由蓝图
处理基础页面路由
"""

from flask import Blueprint, render_template, send_file

main_bp = Blueprint('main', __name__)


@main_bp.route('/')
def index():
    """主页"""
    return render_template('index.html')


@main_bp.route('/config')
def config_page():
    """配置管理页面"""
    return render_template('config.html')


@main_bp.route('/test_batch')
def test_batch():
    """批量转换测试页面"""
    return send_file('test_batch.html')
