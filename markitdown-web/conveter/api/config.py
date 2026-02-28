"""
配置管理路由蓝图
处理配置的获取、更新和重载
"""

from flask import Blueprint, request, jsonify, current_app
from config_manager import config_manager
from file_migrator import file_migrator
from core.extensions import get_logger

config_bp = Blueprint('config', __name__)

logger = get_logger()


@config_bp.route('/config', methods=['GET'])
def get_config_api():
    """获取当前配置"""
    try:
        config_data = config_manager.get_all()
        return jsonify({
            'success': True,
            'config': config_data
        })
    except Exception as e:
        return jsonify({
            'success': False,
            'message': f'获取配置失败: {str(e)}'
        }), 500


@config_bp.route('/config', methods=['PUT'])
def update_config_api():
    """更新配置（支持文件迁移）"""
    try:
        data = request.get_json()
        if not data:
            return jsonify({
                'success': False,
                'message': '没有提供配置数据'
            }), 400

        # 验证配置数据
        if not isinstance(data, dict):
            return jsonify({
                'success': False,
                'message': '配置数据格式错误'
            }), 400

        # 获取当前配置用于迁移
        current_config = config_manager.get_all()

        # 验证迁移路径的合法性
        is_valid, validation_message = file_migrator.validate_migration_paths(current_config, data)
        if not is_valid:
            return jsonify({
                'success': False,
                'message': f'路径验证失败: {validation_message}'
            }), 400

        # 检查是否需要文件迁移
        requires_migration = (
            current_config.get('storage', {}).get('upload_folder') != data.get('storage', {}).get('upload_folder') or
            current_config.get('storage', {}).get('download_folder') != data.get('storage', {}).get('download_folder') or
            current_config.get('storage', {}).get('history_file') != data.get('storage', {}).get('history_file')
        )

        if requires_migration:
            # 执行文件迁移
            logger.info("开始文件迁移...")
            migration_result = file_migrator.migrate_storage_directories(current_config, data)

            if not migration_result.success:
                return jsonify({
                    'success': False,
                    'message': f'文件迁移失败: {migration_result.message}',
                    'requires_migration': True
                }), 400

            # 迁移成功，记录迁移信息
            logger.info(f"文件迁移成功，迁移了 {len(migration_result.migrated_files)} 个文件")

        # 更新配置
        success = config_manager.update(data)
        if success:
            logger.info("配置已通过API更新")
            response_data = {
                'success': True,
                'message': '配置更新成功',
                'requires_migration': requires_migration
            }

            if requires_migration:
                response_data.update({
                    'migrated_files_count': len(migration_result.migrated_files),
                    'migration_details': {
                        'old_paths': current_config.get('storage', {}),
                        'new_paths': data.get('storage', {}),
                        'migrated_files': migration_result.migrated_files[:10]  # 只返回前10个文件示例
                    }
                })

            return jsonify(response_data)
        else:
            # 如果配置更新失败但迁移成功了，需要回滚迁移
            if requires_migration and migration_result.success:
                logger.error("配置更新失败，尝试回滚文件迁移...")

            return jsonify({
                'success': False,
                'message': '配置更新失败'
            }), 500

    except Exception as e:
        logger.error(f"API更新配置失败: {e}")
        return jsonify({
            'success': False,
            'message': f'配置更新失败: {str(e)}'
        }), 500


@config_bp.route('/config/reload', methods=['POST'])
def reload_config_api():
    """重新加载配置"""
    try:
        success = config_manager.reload()
        if success:
            logger.info("配置已通过API重新加载")
            return jsonify({
                'success': True,
                'message': '配置重新加载成功'
            })
        else:
            return jsonify({
                'success': False,
                'message': '配置重新加载失败'
            }), 500

    except Exception as e:
        logger.error(f"API重新加载配置失败: {e}")
        return jsonify({
            'success': False,
            'message': f'配置重新加载失败: {str(e)}'
        }), 500
