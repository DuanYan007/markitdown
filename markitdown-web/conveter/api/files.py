"""
文件服务路由蓝图
处理文件下载和静态资源服务
"""

import os
from flask import Blueprint, send_file, abort, current_app

files_bp = Blueprint('files', __name__)


@files_bp.route("/images/<img_file>", methods=['GET'])
def serve_imgs_image(img_file):
    """服务imgs目录中的图片文件"""
    try:
        # 检查多个可能的imgs目录位置
        possible_paths = [
            os.path.join('./imgs', img_file),  # 相对于当前工作目录
            os.path.join(current_app.config['DOWNLOAD_FOLDER'], 'images', img_file),  # 下载目录下的imgs
            os.path.join(current_app.config['UPLOAD_FOLDER'], 'images', img_file),  # 上传目录下的imgs
        ]

        # 如果配置了绝对路径的imgs目录，也检查那里
        if 'IMGS_FOLDER' in current_app.config:
            possible_paths.append(os.path.join(current_app.config['IMGS_FOLDER'], img_file))

        for path in possible_paths:
            if os.path.exists(path):
                return send_file(path)

        abort(404)
    except Exception as e:
        print(f"[错误] 服务imgs图片失败: {str(e)}")
        abort(404)


@files_bp.route("/downloads/images/<img_file>", methods=['GET'])
def serve_download_image(img_file):
    """服务downloads目录中的图片文件"""
    try:
        image_path = os.path.join(current_app.config['DOWNLOAD_FOLDER'], 'images', img_file)
        if os.path.exists(image_path):
            return send_file(image_path)
        else:
            abort(404)
    except Exception as e:
        print(f"[错误] 服务图片失败: {str(e)}")
        abort(404)


@files_bp.route("/downloads/<folder>/<img_file>", methods=['GET'])
def serve_folder_image(folder, img_file):
    """服务downloads子目录中的图片文件"""
    try:
        image_path = os.path.join(current_app.config['DOWNLOAD_FOLDER'], folder, img_file)
        if os.path.exists(image_path):
            return send_file(image_path)
        else:
            abort(404)
    except Exception as e:
        print(f"[错误] 服务图片失败: {str(e)}")
        abort(404)


@files_bp.route("/downloads/<path:subpath>", methods=['GET'])
def serve_downloads_file(subpath):
    """服务downloads目录下的任何文件（包括图片）"""
    try:
        file_path = os.path.join(current_app.config['DOWNLOAD_FOLDER'], subpath)
        if os.path.exists(file_path) and os.path.isfile(file_path):
            return send_file(file_path)
        else:
            abort(404)
    except Exception as e:
        print(f"[错误] 服务downloads文件失败: {str(e)}")
        abort(404)


@files_bp.route("/<path:subpath>", methods=['GET'])
def serve_any_file(subpath):
    """服务任何位置的文件（处理根目录下的文件路径）"""
    try:
        # 安全检查：只允许图片文件和特定目录
        if not any(subpath.endswith(ext) for ext in ['.jpg', '.jpeg', '.png', '.gif', '.bmp', '.webp']):
            abort(404)

        # 构建可能的文件路径
        possible_paths = [
            subpath,  # 直接路径
            os.path.join(current_app.config['DOWNLOAD_FOLDER'], subpath),  # 下载目录下
            os.path.join(current_app.config['UPLOAD_FOLDER'], subpath),   # 上传目录下
        ]

        # 如果是imgs开头的路径，也检查当前工作目录
        if subpath.startswith('images/'):
            possible_paths.append(subpath)

        for file_path in possible_paths:
            if os.path.exists(file_path) and os.path.isfile(file_path):
                return send_file(file_path)

        abort(404)
    except Exception as e:
        print(f"[错误] 服务文件失败: {str(e)}")
        abort(404)


@files_bp.route('/download/<filename>')
def download_file(filename):
    """下载单个文件"""
    try:
        file_path = os.path.join(current_app.config['DOWNLOAD_FOLDER'], filename)
        if not os.path.exists(file_path):
            # 如果文件不存在，创建一个示例文件
            sample_content = f"# 示例文件\n\n这是一个示例下载文件: {filename}"
            with open(file_path, 'w', encoding='utf-8') as f:
                f.write(sample_content)

        return send_file(file_path, as_attachment=True)
    except Exception as e:
        abort(500)


@files_bp.route('/download-md')
def download_md():
    """下载MD文件（通过文件路径）"""
    try:
        from flask import request
        file_path = request.args.get('file_path')
        filename = request.args.get('filename', 'converted.md')

        if not file_path or not os.path.exists(file_path):
            abort(404)

        # 直接返回原始文件（已经包含绝对路径）
        return send_file(file_path, as_attachment=True, download_name=filename)
    except Exception as e:
        abort(500)
