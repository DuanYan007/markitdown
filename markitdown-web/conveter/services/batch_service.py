"""
批量转换服务模块
提供批量文件转换的状态管理和执行功能
"""

import os
import json
import uuid
import datetime
import threading
from typing import Dict, List, Optional, Any
from flask import current_app


class BatchService:
    """批量转换服务类"""

    # 类变量，用于存储批量转换状态
    _batch_conversion_status: Dict[str, Dict] = {}
    _batch_lock = threading.Lock()

    @classmethod
    def _get_batch_status_file(cls) -> str:
        """获取批量状态文件路径"""
        try:
            return current_app.config.get('BATCH_STATUS_FILE', 'batch_status.json')
        except RuntimeError:
            return 'batch_status.json'

    @classmethod
    def save_batch_status(cls, batch_id: str, status_data: Dict) -> None:
        """
        保存批量转换状态

        Args:
            batch_id: 批次 ID
            status_data: 状态数据
        """
        with cls._batch_lock:
            cls._batch_conversion_status[batch_id] = status_data

            # 可选：持久化到文件
            try:
                batch_status_file = cls._get_batch_status_file()
                with open(batch_status_file, 'w', encoding='utf-8') as f:
                    json.dump(cls._batch_conversion_status, f, ensure_ascii=False, indent=2)
            except Exception as e:
                print(f"保存批量状态失败: {e}")

    @classmethod
    def get_batch_status(cls, batch_id: str) -> Optional[Dict]:
        """
        获取批量转换状态

        Args:
            batch_id: 批次 ID

        Returns:
            Optional[Dict]: 批次状态数据
        """
        with cls._batch_lock:
            return cls._batch_conversion_status.get(batch_id, {})

    @classmethod
    def convert_batch_files(
        cls,
        batch_id: str,
        files: List[Dict],
        app_context: Any = None
    ) -> None:
        """
        异步执行批量文件转换

        Args:
            batch_id: 批次 ID
            files: 文件信息列表
            app_context: Flask 应用上下文
        """
        batch_status = cls.get_batch_status(batch_id)

        for i, file_info in enumerate(files):
            try:
                print(f"[批量转换] 开始处理文件 {i+1}/{len(files)}: {file_info['filename']}")

                if not file_info.get('extracted_path') or file_info.get('error'):
                    print(f"[批量转换] 跳过文件 {file_info['filename']} - 无路径或存在错误: {file_info.get('error')}")
                    continue

                # 找到全局状态中对应的文件对象并更新
                global_file = None
                for f in batch_status['files']:
                    if f['filename'] == file_info['filename']:
                        global_file = f
                        break

                if not global_file:
                    print(f"[批量转换] ❌ 找不到全局文件对象: {file_info['filename']}")
                    continue

                # 更新文件状态为处理中
                global_file['conversion_status'] = 'processing'
                global_file['conversion_progress'] = 0
                print(f"[批量转换] 更新文件状态为处理中: {global_file['filename']}")
                cls.save_batch_status(batch_id, batch_status)

                # 更新整体进度
                batch_status['conversion_progress']['processing'] += 1
                cls.save_batch_status(batch_id, batch_status)

                # 执行转换
                file_path = global_file['extracted_path']
                file_format = global_file['format']
                filename = global_file['filename']

                print(f"[批量转换] 开始转换文件: {filename} (格式: {file_format}, 路径: {file_path})")

                from services.format_service import get_supported_formats
                supported_formats = get_supported_formats()
                if file_format not in supported_formats:
                    print(f"[批量转换] ❌ 不支持的文件格式: {file_format}")
                    global_file['conversion_status'] = 'failed'
                    global_file['conversion_error'] = f'不支持的文件格式: {file_format}'
                    batch_status['conversion_progress']['failed'] += 1
                    batch_status['conversion_progress']['processing'] -= 1
                    cls.save_batch_status(batch_id, batch_status)
                    continue

                print(f"[批量转换] 调用转换函数...")
                from services.conversion_service import convert_file_content
                content = convert_file_content(file_path, file_format)
                print(f"[批量转换] 转换完成，内容长度: {len(content)} 字符")

                # 保存转换后的文件
                output_filename = f"{os.path.splitext(filename)[0]}_{uuid.uuid4().hex[:8]}.md"

                # 获取下载目录
                try:
                    download_folder = current_app.config['DOWNLOAD_FOLDER']
                except RuntimeError:
                    download_folder = 'downloads'

                output_path = os.path.join(download_folder, output_filename)

                print(f"[批量转换] 保存文件到: {output_path}")
                with open(output_path, 'w', encoding='utf-8') as f:
                    f.write(content)

                # 创建下载链接
                download_url = f"/download-md?file_path={output_path}&filename={output_filename}"
                print(f"[批量转换] 创建下载链接: {download_url}")

                # 更新文件状态为完成
                global_file['conversion_status'] = 'completed'
                global_file['conversion_progress'] = 100
                global_file['md_file_path'] = output_path
                global_file['download_url'] = download_url
                global_file['converted_at'] = datetime.datetime.now().isoformat()
                print(f"[批量转换] ✅ 文件转换完成: {filename}")

                # 添加到历史记录
                from services.history_service import add_to_history
                print(f"[批量转换] 添加到历史记录...")
                add_to_history(
                    original_name=filename,
                    file_format=file_format,
                    file_size=os.path.getsize(output_path),
                    md_file_path=output_path,
                    download_url=download_url
                )

                # 更新整体进度
                batch_status['conversion_progress']['completed'] += 1
                batch_status['conversion_progress']['processing'] -= 1
                batch_status['conversion_progress']['current_index'] = i + 1

                cls.save_batch_status(batch_id, batch_status)

            except Exception as e:
                print(f"[批量转换] ❌ 处理文件出错: {file_info.get('filename', 'unknown')}, 错误: {str(e)}")
                # 更新文件状态为失败
                if 'global_file' in locals() and global_file:
                    global_file['conversion_status'] = 'failed'
                    global_file['conversion_error'] = str(e)
                    batch_status['conversion_progress']['failed'] += 1
                    batch_status['conversion_progress']['processing'] -= 1
                    cls.save_batch_status(batch_id, batch_status)

        # 更新批次状态
        batch_status['status'] = 'completed'
        batch_status['completed_at'] = datetime.datetime.now().isoformat()
        cls.save_batch_status(batch_id, batch_status)
        print(f"[批量转换] ✅ 批次 {batch_id} 处理完成")


# 便捷函数（保持向后兼容）
def save_batch_status(batch_id: str, status_data: Dict) -> None:
    """保存批量转换状态（便捷函数）"""
    BatchService.save_batch_status(batch_id, status_data)


def get_batch_status(batch_id: str) -> Optional[Dict]:
    """获取批量转换状态（便捷函数）"""
    return BatchService.get_batch_status(batch_id)


def convert_batch_files(batch_id: str, files: List[Dict]) -> None:
    """异步执行批量文件转换（便捷函数）"""
    BatchService.convert_batch_files(batch_id, files)
