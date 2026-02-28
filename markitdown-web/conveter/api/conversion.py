"""
转换相关路由蓝图
处理文件上传、转换和格式查询
"""

import os
import uuid
from flask import Blueprint, request, jsonify, current_app
from services.format_service import get_supported_formats, is_supported_file
from services.conversion_service import convert_file_content
from services.history_service import add_to_history

conversion_bp = Blueprint('conversion', __name__)


@conversion_bp.route('/upload/<format_type>', methods=['POST'])
def upload_file(format_type):
    """上传文件接口"""
    try:
        supported_formats = get_supported_formats()
        if format_type not in supported_formats:
            return jsonify({
                'success': False,
                'message': f'不支持的格式: {format_type}'
            }), 400

        if 'file' not in request.files:
            return jsonify({
                'success': False,
                'message': '没有选择文件'
            }), 400

        file = request.files['file']
        if file.filename == '':
            return jsonify({
                'success': False,
                'message': '没有选择文件'
            }), 400

        # 验证文件格式
        if not is_supported_file(file.filename, format_type):
            return jsonify({
                'success': False,
                'message': f'文件格式不匹配，期望 {format_type} 格式'
            }), 400

        # 保存上传的文件
        filename = file.filename
        upload_path = os.path.join(current_app.config['UPLOAD_FOLDER'], f"{uuid.uuid4().hex}_{filename}")
        file.save(upload_path)

        # 获取文件大小
        file_size = os.path.getsize(upload_path)
        return jsonify({
            'success': True,
            'file_id': os.path.basename(upload_path),  # 返回保存后的文件名
            'original_name': filename,
            'file_size': file_size,
            'upload_path': upload_path,
            'message': f'文件 {filename} 上传成功'
        })

    except Exception as e:
        return jsonify({
            'success': False,
            'message': f'上传失败: {str(e)}'
        }), 500


@conversion_bp.route('/convert/<format_type>', methods=['POST'])
def convert_format(format_type):
    """格式特定的转换接口"""
    try:
        supported_formats = get_supported_formats()
        if format_type not in supported_formats:
            return jsonify({
                'success': False,
                'message': f'不支持的格式: {format_type}'
            }), 400

        # 获取文件ID
        data = request.get_json()
        if not data or 'file_id' not in data:
            return jsonify({
                'success': False,
                'message': '没有提供文件ID'
            }), 400

        file_id = data['file_id']
        upload_path = os.path.join(current_app.config['UPLOAD_FOLDER'], file_id)

        # 验证文件是否存在
        if not os.path.exists(upload_path):
            return jsonify({
                'success': False,
                'message': f'文件不存在，请重新上传'
            }), 400

        # 执行转换
        content = convert_file_content(upload_path, format_type)

        filename = os.path.basename(upload_path)
        output_filename = f"{os.path.splitext(filename)[0]}_{uuid.uuid4().hex[:8]}.md"
        output_path = os.path.join(current_app.config['DOWNLOAD_FOLDER'], output_filename)

        # 保存转换结果
        with open(output_path, 'w', encoding='utf-8') as f:
            f.write(content)

        # 删除临时上传文件
        os.remove(upload_path)

        # 添加到历史记录
        basename = os.path.basename(upload_path)
        underscore_pos = basename.find('_')
        if underscore_pos != -1:
            original_filename = basename[underscore_pos + 1:]
        else:
            original_filename = basename

        download_url = f"/download-md?file_path={output_path}&filename={os.path.basename(output_filename)}"
        history_record = add_to_history(
            original_name=original_filename,
            file_format=format_type,
            file_size=os.path.getsize(output_path) if os.path.exists(output_path) else 0,
            md_file_path=output_path,
            download_url=download_url
        )

        return jsonify({
            'success': True,
            'md_file_path': output_path,
            'message': f'成功转换 {filename}',
            'history_id': history_record['id']
        })

    except Exception as e:
        print(e)
        return jsonify({
            'success': False,
            'message': f'服务器错误: {str(e)}'
        }), 500


@conversion_bp.route('/api/formats')
def get_file_formats():
    """获取支持的文件格式"""
    from config_manager import get_config

    supported_formats = get_supported_formats()
    max_file_size = get_config('limits.max_file_size', 104857600)
    max_file_size_mb = max_file_size / (1024 * 1024)
    return jsonify({
        'supported_formats': supported_formats,
        'max_file_size': f"{max_file_size_mb:.1f}MB",
        'supported_types': list(supported_formats.keys())
    })


@conversion_bp.route('/read-md-file', methods=['POST'])
def read_md_file():
    """读取MD文件内容"""
    try:
        data = request.get_json()
        if not data or 'file_path' not in data:
            return jsonify({
                'success': False,
                'message': '没有提供文件路径'
            }), 400

        file_path = data['file_path']
        use_absolute = data.get('use_absolute_paths', False)  # 默认为False（网页预览）

        # 验证文件是否存在
        if not os.path.exists(file_path):
            return jsonify({
                'success': False,
                'message': f'文件不存在: {file_path}'
            }), 400

        # 读取文件内容（原始文件包含绝对路径）
        with open(file_path, 'r', encoding='utf-8') as f:
            content = f.read()

        # 根据参数决定是否转换路径
        if use_absolute:
            # 原始Markdown展示 - 返回绝对路径
            final_content = content
            print(f"[信息] 返回原始Markdown内容（绝对路径）")
        else:
            # 网页预览 - 转换为相对路径
            from utils.image_path_processor import process_images_to_relative_paths_for_web
            final_content = process_images_to_relative_paths_for_web(content)
            print(f"[信息] 返回网页预览内容（相对路径）")

        return jsonify({
            'success': True,
            'content': final_content
        })

    except Exception as e:
        return jsonify({
            'success': False,
            'message': f'读取文件失败: {str(e)}'
        }), 500
