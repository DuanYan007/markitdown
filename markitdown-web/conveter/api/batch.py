"""
批量处理路由蓝图
处理批量文件上传、解压和转换
"""

import os
import uuid
import datetime
import threading
from flask import Blueprint, request, jsonify, current_app
from services.conversion_service import archive_extractor
from services.batch_service import BatchService

batch_bp = Blueprint('batch', __name__)


@batch_bp.route('/upload/batch', methods=['POST'])
def upload_batch():
    """批量上传压缩包文件"""
    try:
        print(f"[调试] 批量上传请求被触发")

        if 'file' not in request.files:
            print(f"[调试] 错误: 没有文件字段")
            return jsonify({
                'success': False,
                'message': '没有选择文件'
            }), 400

        file = request.files['file']
        print(f"[调试] 文件名: {file.filename}")

        if file.filename == '':
            print(f"[调试] 错误: 文件名为空")
            return jsonify({
                'success': False,
                'message': '没有选择文件'
            }), 400

        # 获取密码
        password = request.form.get('password', '')
        print(f"[调试] 密码: {password}")

        # 验证文件格式
        is_supported = archive_extractor.is_supported_format(file.filename)
        print(f"[调试] 文件格式支持检查: {file.filename} -> {is_supported}")

        if not is_supported:
            return jsonify({
                'success': False,
                'message': f'文件格式不支持，请上传ZIP文件。当前文件: {file.filename}'
            }), 400

        # 保存上传的压缩包
        batch_id = uuid.uuid4().hex
        archive_filename = f"batch_{batch_id}_{file.filename}"
        archive_path = os.path.join(current_app.config['UPLOAD_FOLDER'], archive_filename)
        file.save(archive_path)

        # 创建批次状态
        batch_status = {
            'batch_id': batch_id,
            'archive_name': file.filename,
            'archive_path': archive_path,
            'password': password,
            'status': 'uploaded',
            'total_files': 0,
            'extracted_files': 0,
            'failed_files': 0,
            'files': [],
            'extracted_at': None,
            'converted_at': None,
            'created_at': datetime.datetime.now().isoformat()
        }

        BatchService.save_batch_status(batch_id, batch_status)

        return jsonify({
            'success': True,
            'batch_id': batch_id,
            'archive_name': file.filename,
            'file_size': os.path.getsize(archive_path),
            'message': f'压缩包 {file.filename} 上传成功'
        })

    except Exception as e:
        return jsonify({
            'success': False,
            'message': f'上传失败: {str(e)}'
        }), 500


@batch_bp.route('/extract/batch/<batch_id>', methods=['POST'])
def extract_batch(batch_id):
    """解压批量上传的压缩包"""
    try:
        batch_status = BatchService.get_batch_status(batch_id)
        if not batch_status:
            return jsonify({
                'success': False,
                'message': '批次不存在'
            }), 404

        if batch_status['status'] != 'uploaded':
            return jsonify({
                'success': False,
                'message': f'批次状态错误: {batch_status["status"]}'
            }), 400

        # 更新状态为解压中
        batch_status['status'] = 'extracting'
        BatchService.save_batch_status(batch_id, batch_status)

        try:
            # 解压文件
            extract_result = archive_extractor.extract_archive(
                batch_status['archive_path'],
                batch_status['password'] or None
            )

            if extract_result['success']:
                # 更新批次状态
                batch_status.update({
                    'status': 'extracted',
                    'total_files': extract_result['total_files'],
                    'extracted_files': extract_result['extracted_files'],
                    'failed_files': extract_result['failed_files'],
                    'files': extract_result['files'],
                    'temp_dir': extract_result['temp_dir'],
                    'extracted_at': datetime.datetime.now().isoformat()
                })

                BatchService.save_batch_status(batch_id, batch_status)

                return jsonify({
                    'success': True,
                    'message': f'解压完成，共 {extract_result["total_files"]} 个文件',
                    'total_files': extract_result['total_files'],
                    'extracted_files': extract_result['extracted_files'],
                    'failed_files': extract_result['failed_files'],
                    'files': extract_result['files']
                })
            else:
                raise Exception('解压失败')

        except Exception as e:
            # 更新状态为解压失败
            batch_status['status'] = 'extract_failed'
            batch_status['error'] = str(e)
            BatchService.save_batch_status(batch_id, batch_status)
            raise e

    except Exception as e:
        return jsonify({
            'success': False,
            'message': f'解压失败: {str(e)}'
        }), 500


@batch_bp.route('/convert/batch/<batch_id>', methods=['POST'])
def convert_batch(batch_id):
    """异步批量转换文件"""
    try:
        batch_status = BatchService.get_batch_status(batch_id)
        if not batch_status:
            return jsonify({
                'success': False,
                'message': '批次不存在'
            }), 404

        if batch_status['status'] != 'extracted':
            return jsonify({
                'success': False,
                'message': f'批次状态错误: {batch_status["status"]}'
            }), 400

        # 获取要转换的文件列表
        data = request.get_json() or {}
        selected_files = data.get('files', [])

        # 如果没有指定文件，则转换所有成功的文件
        if not selected_files:
            selected_files = [f for f in batch_status['files'] if f.get('extracted_path') and not f.get('error')]

        # 更新状态为转换中
        batch_status['status'] = 'converting'
        batch_status['converted_at'] = datetime.datetime.now().isoformat()
        batch_status['conversion_progress'] = {
            'total': len(selected_files),
            'completed': 0,
            'failed': 0,
            'processing': 0,
            'current_index': 0
        }
        BatchService.save_batch_status(batch_id, batch_status)

        # 启动异步转换线程
        def async_convert():
            try:
                BatchService.convert_batch_files(batch_id, selected_files)
            except Exception as e:
                # 更新错误状态
                batch_status = BatchService.get_batch_status(batch_id)
                batch_status['status'] = 'conversion_failed'
                batch_status['error'] = str(e)
                BatchService.save_batch_status(batch_id, batch_status)

        thread = threading.Thread(target=async_convert)
        thread.daemon = True
        thread.start()

        return jsonify({
            'success': True,
            'message': '批量转换已开始',
            'total_files': len(selected_files)
        })

    except Exception as e:
        return jsonify({
            'success': False,
            'message': f'启动批量转换失败: {str(e)}'
        }), 500


@batch_bp.route('/status/batch/<batch_id>', methods=['GET'])
def get_batch_conversion_status(batch_id):
    """获取批量转换状态"""
    try:
        batch_status = BatchService.get_batch_status(batch_id)
        if not batch_status:
            return jsonify({
                'success': False,
                'message': '批次不存在'
            }), 404

        return jsonify({
            'success': True,
            'status': batch_status
        })

    except Exception as e:
        return jsonify({
            'success': False,
            'message': f'获取状态失败: {str(e)}'
        }), 500
