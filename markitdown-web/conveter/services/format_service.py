"""
格式检测服务模块
提供文件格式检测和验证功能
"""

from typing import Optional, Dict


class FormatService:
    """格式服务类，提供格式相关的静态方法"""

    @staticmethod
    def get_supported_formats() -> Dict[str, str]:
        """
        获取支持的文件格式

        Returns:
            Dict[str, str]: 格式类型到扩展名的映射
        """
        return {
            "pdf": ".pdf",
            "word": ".doc,.docx",
            "excel": ".xls,.xlsx",
            "ppt": ".ppt,.pptx",
            "image": ".jpg,.jpeg,.png,.gif,.bmp",
            "audio": ".mp3,.wav,.flac,.aac,.ogg,.m4a,.wma",
            "video": ".mp4,.avi,.mov,.mkv,.wmv,.flv,.webm,.m4v,.3gp,.mpg,.mpeg",
            "html": ".html,.htm",
            "csv": ".csv",
            "json": ".json",
            "xml": ".xml",
            "zip": ".zip",
            "rar": ".rar"
        }

    @staticmethod
    def detect_file_format(filename: str) -> Optional[str]:
        """
        根据文件扩展名检测格式

        Args:
            filename: 文件名

        Returns:
            Optional[str]: 格式类型，如果不支持则返回 None
        """
        if not filename:
            return None

        ext = '.' + filename.split('.')[-1].lower() if '.' in filename else ''

        supported_formats = FormatService.get_supported_formats()
        for format_type, extensions in supported_formats.items():
            if ext in extensions.split(','):
                return format_type

        return None

    @staticmethod
    def is_supported_file(filename: str, format_type: str) -> bool:
        """
        检查文件是否支持该格式

        Args:
            filename: 文件名
            format_type: 格式类型

        Returns:
            bool: 是否支持
        """
        supported_formats = FormatService.get_supported_formats()
        if format_type not in supported_formats:
            return False

        ext = '.' + filename.split('.')[-1].lower() if '.' in filename else ''
        return ext in supported_formats[format_type].split(',')


# 便捷函数
def get_supported_formats() -> Dict[str, str]:
    """获取支持的文件格式（便捷函数）"""
    return FormatService.get_supported_formats()


def detect_file_format(filename: str) -> Optional[str]:
    """检测文件格式（便捷函数）"""
    return FormatService.detect_file_format(filename)


def is_supported_file(filename: str, format_type: str) -> bool:
    """检查文件是否支持（便捷函数）"""
    return FormatService.is_supported_file(filename, format_type)
