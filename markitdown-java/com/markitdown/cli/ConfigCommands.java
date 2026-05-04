package com.markitdown.cli;

import com.markitdown.config.ConfigurationManager;
import picocli.CommandLine;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Properties;

/**
 * 配置文件管理命令
 */
public class ConfigCommands {

    /**
     * 生成默认配置文件
     */
    public static int generateConfig(String configPath) {
        try {
            ConfigurationManager manager = new ConfigurationManager();
            Path outputPath = configPath != null ?
                Paths.get(configPath) : Paths.get(".markitdown.properties");

            if (Files.exists(outputPath)) {
                System.err.println("配置文件已存在: " + outputPath);
                System.err.println("如需覆盖，请先删除现有文件");
                return 1;
            }

            manager.generateDefaultConfig(outputPath);
            System.out.println("✅ 默认配置文件已生成: " + outputPath);
            System.out.println("📝 请根据需要修改配置文件");
            System.out.println("💡 提示: 配置项支持 # 注释，每个配置项对应命令行参数");

            return 0;
        } catch (IOException e) {
            System.err.println("❌ 生成配置文件失败: " + e.getMessage());
            return 1;
        }
    }

    /**
     * 验证配置文件
     */
    public static int validateConfig(String configPath) {
        try {
            Path path = configPath != null ?
                Paths.get(configPath) : Paths.get(".markitdown.properties");

            if (!Files.exists(path)) {
                System.err.println("❌ 配置文件不存在: " + path);
                return 1;
            }

            // 验证配置文件
            ConfigurationManager manager = new ConfigurationManager();
            List<String> errors = manager.validateConfiguration(path);

            if (errors.isEmpty()) {
                System.out.println("✅ 配置文件验证通过: " + path);
                System.out.println("📋 配置概要:");
                printConfigSummary(manager);
                return 0;
            } else {
                System.err.println("❌ 配置文件验证失败: " + path);
                System.err.println("发现 " + errors.size() + " 个错误:");
                for (String error : errors) {
                    System.err.println("  - " + error);
                }
                return 1;
            }
        } catch (Exception e) {
            System.err.println("❌ 配置文件验证失败: " + e.getMessage());
            return 1;
        }
    }

    /**
     * 显示当前配置
     */
    public static int showConfig(String configPath) {
        try {
            ConfigurationManager manager = new ConfigurationManager();

            System.out.println("📋 当前配置:");
            System.out.println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
            printConfigSummary(manager);
            System.out.println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");

            return 0;
        } catch (Exception e) {
            System.err.println("❌ 读取配置失败: " + e.getMessage());
            return 1;
        }
    }

    /**
     * 打印配置摘要
     */
    private static void printConfigSummary(ConfigurationManager manager) {
        System.out.println("🔧 引擎路径:");
        System.out.println("   Tesseract: " + manager.getTesseractPath());
        System.out.println("   Tessdata: " + manager.getTessdataPath());

        System.out.println("📁 输出配置:");
        System.out.println("   输出目录: " + manager.getOutputDir());
        System.out.println("   图片目录: " + manager.getImageDir());
        System.out.println("   临时目录: " + manager.getTempDir());
        System.out.println("   按类型组织: " + manager.isOrganizeByType());
        System.out.println("   保持结构: " + manager.isPreserveStructure());

        System.out.println("⚙️  处理选项:");
        System.out.println("   包含元数据: " + manager.getProperty("content.include.metadata", "true"));
        System.out.println("   包含图片: " + manager.getProperty("content.include.images", "true"));
        System.out.println("   包含表格: " + manager.getProperty("content.include.tables", "true"));
        System.out.println("   使用OCR: " + manager.getProperty("ocr.enable", "false"));
        System.out.println("   OCR语言: " + manager.getProperty("ocr.language", "auto"));

        System.out.println("⚡ 性能配置:");
        System.out.println("   并行处理: " + manager.isParallelProcessing());
        System.out.println("   线程数量: " + (manager.getThreadCount() == 0 ? "自动" : manager.getThreadCount()));
        System.out.println("   内存优化: " + manager.isMemoryOptimization());
        System.out.println("   最大文件: " + formatFileSize(manager.getLongProperty("performance.max.file.size", 52428800)));

        System.out.println("🎛️  高级选项:");
        System.out.println("   详细输出: " + manager.isVerbose());
        System.out.println("   静默模式: " + manager.isQuiet());
        System.out.println("   进度显示: " + manager.isShowProgress());
        System.out.println("   交互模式: " + manager.isInteractiveMode());

        System.out.println("📁 文件处理:");
        System.out.println("   递归处理: " + manager.isRecursive());
        System.out.println("   批量处理: " + manager.isBatch());
        System.out.println("   允许大文件: " + manager.isLargeFile());
    }

    /**
     * 格式化文件大小
     */
    private static String formatFileSize(long bytes) {
        if (bytes < 1024) {
            return bytes + " B";
        } else if (bytes < 1024 * 1024) {
            return String.format("%.1f KB", bytes / 1024.0);
        } else if (bytes < 1024 * 1024 * 1024) {
            return String.format("%.1f MB", bytes / (1024.0 * 1024));
        } else {
            return String.format("%.1f GB", bytes / (1024.0 * 1024 * 1024));
        }
    }
}