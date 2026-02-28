"""
历史记录路由蓝图
处理转换历史记录的查询、删除和清空
"""

from flask import Blueprint, jsonify
from services.history_service import HistoryService

history_bp = Blueprint('history', __name__)


@history_bp.route('/history', methods=['GET'])
def get_history():
    """获取转换历史记录"""
    try:
        history = HistoryService.load_history()
        return jsonify({
            'success': True,
            'history': history,
            'total': len(history)
        })
    except Exception as e:
        return jsonify({
            'success': False,
            'message': f'获取历史记录失败: {str(e)}'
        }), 500


@history_bp.route('/history/clear', methods=['POST'])
def clear_history():
    """清空历史记录"""
    try:
        HistoryService.clear_history()
        return jsonify({
            'success': True,
            'message': '历史记录已清空'
        })
    except Exception as e:
        return jsonify({
            'success': False,
            'message': f'清空历史记录失败: {str(e)}'
        }), 500


@history_bp.route('/history/<history_id>', methods=['DELETE'])
def delete_history_item(history_id):
    """删除单条历史记录"""
    try:
        success = HistoryService.delete_history_item(history_id)
        if not success:
            return jsonify({
                'success': False,
                'message': '记录不存在'
            }), 404

        return jsonify({
            'success': True,
            'message': '记录已删除'
        })
    except Exception as e:
        return jsonify({
            'success': False,
            'message': f'删除记录失败: {str(e)}'
        }), 500
