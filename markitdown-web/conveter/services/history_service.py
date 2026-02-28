"""
历史记录服务模块
提供转换历史记录的管理功能
"""

import os
import json
import uuid
import datetime
from typing import List, Dict, Optional
from flask import current_app


class HistoryService:
    """历史记录服务类"""

    @staticmethod
    def _get_history_file() -> str:
        """获取历史记录文件路径"""
        try:
            return current_app.config['HISTORY_FILE']
        except RuntimeError:
            # 在应用上下文外使用默认值
            return 'history.json'

    @staticmethod
    def load_history() -> List[Dict]:
        """
        加载历史记录

        Returns:
            List[Dict]: 历史记录列表
        """
        history_file = HistoryService._get_history_file()
        if os.path.exists(history_file):
            try:
                with open(history_file, 'r', encoding='utf-8') as f:
                    return json.load(f)
            except Exception:
                return []
        return []

    @staticmethod
    def save_history(history: List[Dict]) -> bool:
        """
        保存历史记录

        Args:
            history: 历史记录列表

        Returns:
            bool: 是否保存成功
        """
        history_file = HistoryService._get_history_file()
        try:
            with open(history_file, 'w', encoding='utf-8') as f:
                json.dump(history, f, ensure_ascii=False, indent=2)
            return True
        except Exception as e:
            print(f"保存历史记录失败: {e}")
            return False

    @staticmethod
    def add_to_history(
        original_name: str,
        file_format: str,
        file_size: int,
        md_file_path: str,
        download_url: str
    ) -> Dict:
        """
        添加转换记录到历史

        Args:
            original_name: 原始文件名
            file_format: 文件格式
            file_size: 文件大小
            md_file_path: Markdown 文件路径
            download_url: 下载 URL

        Returns:
            Dict: 新创建的历史记录
        """
        from config_manager import get_config

        history = HistoryService.load_history()
        record = {
            'id': uuid.uuid4().hex,
            'original_name': original_name,
            'format': file_format,
            'file_size': file_size,
            'md_file_path': md_file_path,
            'download_url': download_url,
            'converted_at': datetime.datetime.now().isoformat(),
            'status': 'completed'
        }
        history.insert(0, record)  # 添加到开头

        # 只保留配置中指定数量的记录
        max_records = get_config('limits.max_history_records', 100)
        if len(history) > max_records:
            history = history[:max_records]

        HistoryService.save_history(history)
        return record

    @staticmethod
    def clear_history() -> bool:
        """
        清空历史记录

        Returns:
            bool: 是否清空成功
        """
        return HistoryService.save_history([])

    @staticmethod
    def delete_history_item(history_id: str) -> bool:
        """
        删除单条历史记录

        Args:
            history_id: 历史记录 ID

        Returns:
            bool: 是否删除成功
        """
        history = HistoryService.load_history()
        original_length = len(history)
        new_history = [item for item in history if item['id'] != history_id]

        if len(history) == len(new_history):
            return False  # 记录不存在

        return HistoryService.save_history(new_history)


# 便捷函数（保持向后兼容）
def load_history() -> List[Dict]:
    """加载历史记录（便捷函数）"""
    return HistoryService.load_history()


def save_history(history: List[Dict]) -> bool:
    """保存历史记录（便捷函数）"""
    return HistoryService.save_history(history)


def add_to_history(
    original_name: str,
    file_format: str,
    file_size: int,
    md_file_path: str,
    download_url: str
) -> Dict:
    """添加转换记录到历史（便捷函数）"""
    return HistoryService.add_to_history(
        original_name, file_format, file_size, md_file_path, download_url
    )
