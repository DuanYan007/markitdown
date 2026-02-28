"""
转换服务模块
提供文件转换的核心业务逻辑
"""

import os
import time
import tempfile
import shutil
import zipfile
from typing import Dict, Optional, Any
from flask import current_app


class ConversionService:
    """转换服务类"""

    @staticmethod
    def extract_archive_safe(archive_path: str, password: Optional[str] = None) -> Dict[str, Any]:
        """
        安全的 ZIP 文件提取，不依赖外部工具

        Args:
            archive_path: 压缩文件路径
            password: 解压密码

        Returns:
            Dict: 解压结果
        """
        try:
            temp_dir = tempfile.mkdtemp(prefix="archive_extract_")
            extracted_files = []

            with zipfile.ZipFile(archive_path, 'r') as zip_ref:
                file_list = zip_ref.infolist()
                total_files = len([f for f in file_list if not f.is_dir()])

                for file_info in file_list:
                    if file_info.is_dir():
                        continue

                    try:
                        # 处理文件名
                        filename = file_info.filename
                        filename_only = os.path.basename(filename)
                        extract_path = os.path.join(temp_dir, filename_only)

                        # 处理重名
                        base_name, ext = os.path.splitext(filename_only)
                        counter = 1
                        while os.path.exists(extract_path):
                            filename_only = f"{base_name}_{counter}{ext}"
                            extract_path = os.path.join(temp_dir, filename_only)
                            counter += 1

                        # 提取文件
                        with zip_ref.open(file_info, pwd=password.encode() if password else None) as source:
                            with open(extract_path, 'wb') as target:
                                shutil.copyfileobj(source, target)

                        # 检测格式
                        ext = filename_only.split('.')[-1].lower() if '.' in filename_only else ''
                        format_map = {
                            'pdf': 'pdf', 'doc': 'word', 'docx': 'word',
                            'xls': 'excel', 'xlsx': 'excel', 'ppt': 'ppt', 'pptx': 'ppt',
                            'jpg': 'image', 'jpeg': 'image', 'png': 'image', 'gif': 'image',
                            'html': 'html', 'htm': 'html', 'txt': 'text',
                            'csv': 'csv', 'json': 'json', 'xml': 'xml'
                        }
                        format_name = format_map.get(ext, 'unknown')

                        # 记录文件信息
                        file_info_dict = {
                            'filename': filename_only,
                            'original_path': filename,
                            'extracted_path': extract_path,
                            'size': file_info.file_size,
                            'format': format_name,
                            'error': None,
                            'extract_time': time.time()
                        }
                        extracted_files.append(file_info_dict)

                    except Exception as e:
                        # 记录失败文件
                        file_info_dict = {
                            'filename': file_info.filename,
                            'original_path': file_info.filename,
                            'extracted_path': None,
                            'size': file_info.file_size,
                            'format': 'unknown',
                            'error': str(e),
                            'extract_time': time.time()
                        }
                        extracted_files.append(file_info_dict)

            return {
                'success': True,
                'total_files': total_files,
                'extracted_files': len([f for f in extracted_files if not f.get('error')]),
                'failed_files': len([f for f in extracted_files if f.get('error')]),
                'files': extracted_files,
                'temp_dir': temp_dir
            }

        except Exception as e:
            return {
                'success': False,
                'error': str(e),
                'files': []
            }

    @staticmethod
    def convert_file_content(file_path: str, file_format: str) -> str:
        """
        转换单个文件内容

        Args:
            file_path: 文件路径
            file_format: 文件格式

        Returns:
            str: 转换后的 Markdown 内容
        """
        # 延迟导入转换器，避免循环依赖
        from converters import csv_converter, pdf_converter, img_converter
        from converters.word_converter import word_converter
        from converters.pdf_native_converter import pdf_native_converter
        from converters.ppt_native_converter import ppt_native_converter
        from converters.audio_converter import audio_converter
        from converters.video_converter import video_converter

        if file_format == 'csv':
            with open(file_path, "r", encoding="utf-8") as f:
                content = f.read()
            return csv_converter.csv_converter(content)

        elif file_format == "pdf":
            try:
                content = pdf_converter.pdf_converter(file_path)
            except Exception as e:
                print(f"[信息] PaddleOCR PDF转换失败，尝试原生转换器: {str(e)}")
                try:
                    content = pdf_native_converter(file_path)
                except Exception as e2:
                    print(f"[错误] 原生PDF转换也失败: {str(e2)}")
                    content = f"# 转换错误\n\nPDF转换失败\n\nPaddleOCR错误: {str(e)}\n原生转换错误: {str(e2)}"

        elif file_format == "image":
            content = img_converter.img_converter(file_path)

        elif file_format == "json":
            with open(file_path, "r", encoding="utf-8") as f:
                content = f.read()
            content = "```json\n" + content + "\n```"

        elif file_format == "xml":
            with open(file_path, "r", encoding="utf-8") as f:
                content = f.read()
            content = "```xml\n" + content + "\n```"

        elif file_format == "html":
            with open(file_path, "r", encoding="utf-8") as f:
                content = f.read()
            content = "```html\n" + content + "\n```"

        elif file_format == "word":
            content = word_converter(file_path)

        elif file_format == "ppt":
            try:
                content = ppt_native_converter(file_path)
            except Exception as e:
                print(f"[错误] PPT转换失败: {str(e)}")
                content = f"# 转换错误\n\nPPT转换失败\n\n错误详情: {str(e)}"

        elif file_format == "audio":
            try:
                content = audio_converter(file_path)
            except Exception as e:
                print(f"[错误] 音频转换失败: {str(e)}")
                content = f"# 转换错误\n\n音频转换失败\n\n错误详情: {str(e)}"

        elif file_format == "video":
            try:
                content = video_converter(file_path)
            except Exception as e:
                print(f"[错误] 视频转换失败: {str(e)}")
                content = f"# 转换错误\n\n视频转换失败\n\n错误详情: {str(e)}"

        else:
            content = f"# 不支持的格式\n\n文件格式 {file_format} 暂不支持转换"

        return content


class SimpleArchiveExtractor:
    """简化的解压器类"""

    def __init__(self):
        pass

    def is_supported_format(self, filename: str) -> bool:
        """检查是否是支持的压缩格式"""
        if not filename:
            return False
        ext = '.' + filename.split('.')[-1].lower() if '.' in filename else ''
        return ext in ['.zip']

    def extract_archive(self, archive_path: str, password: Optional[str] = None) -> Dict:
        """提取压缩文件"""
        return ConversionService.extract_archive_safe(archive_path, password)


# 全局实例（保持向后兼容）
archive_extractor = SimpleArchiveExtractor()


# 便捷函数（保持向后兼容）
def extract_archive_safe(archive_path: str, password: Optional[str] = None) -> Dict:
    """安全的ZIP文件提取（便捷函数）"""
    return ConversionService.extract_archive_safe(archive_path, password)


def convert_file_content(file_path: str, file_format: str) -> str:
    """转换单个文件内容（便捷函数）"""
    return ConversionService.convert_file_content(file_path, file_format)
