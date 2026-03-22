package com.markitdown.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

/**
 * 配置管理器 - 支持properties格式配置文件
 */
public class ConfigurationManager {

    private static final Logger logger = LoggerFactory.getLogger(ConfigurationManager.class);
    private static final String DEFAULT_CONFIG_FILE = ".markitdown.properties";
    private static final String[] CONFIG_SEARCH_PATHS = {
        ".", // 当前目录
        System.getProperty("user.home"), // 用户主目录
        System.getProperty("user.dir") + "/config", // 项目配置目录
        "/etc/markitdown" // 系统配置目录
    };

    private final Properties properties;

    public ConfigurationManager() {
        this.properties = loadConfiguration();
    }

    /**
     * 加载配置（按优先级：环境变量 > 配置文件 > 默认值）
     */
    private Properties loadConfiguration() {
        Properties props = new Properties();

        // 1. 加载默认配置
        setDefaultValues(props);

        // 2. 从配置文件加载
        Path configPath = findConfigFile();
        if (configPath != null) {
            try (FileInputStream fis = new FileInputStream(configPath.toFile())) {
                props.load(new java.io.InputStreamReader(fis, "UTF-8"));
                logger.info("已加载配置文件: {}", configPath);
            } catch (Exception e) {
                logger.warn("配置文件加载失败，使用默认配置: {}", e.getMessage());
            }
        }

        // 3. 环境变量覆盖
        applyEnvironmentVariables(props);

        return props;
    }

    /**
     * 设置默认配置值
     */
    private void setDefaultValues(Properties props) {
        // 引擎路径默认值
        props.setProperty("tesseract.path", "O:\\tesserOCR");
        props.setProperty("tessdata.path", "O:\\tesserOCR\\tessdata");

        // 输出配置默认值
        props.setProperty("output.dir", "./output");
        props.setProperty("output.image.dir", "assets");
        props.setProperty("output.temp.dir", System.getProperty("java.io.tmpdir"));
        props.setProperty("output.organize.by.type", "false");
        props.setProperty("output.preserve.structure", "false");

        // 内容包含默认值
        props.setProperty("content.include.metadata", "true");
        props.setProperty("content.include.images", "true");
        props.setProperty("content.include.tables", "true");

        // OCR默认值
        props.setProperty("ocr.enable", "false");
        props.setProperty("ocr.language", "auto");

        // 格式化默认值
        props.setProperty("format.image", "markdown");
        props.setProperty("format.table", "github");

        // 性能默认值
        props.setProperty("performance.parallel", "false");
        props.setProperty("performance.threads", "0");
        props.setProperty("performance.optimize.memory", "false");
        props.setProperty("performance.max.file.size", "52428800");
        props.setProperty("performance.batch.size", "20");

        // 用户界面默认值
        props.setProperty("ui.verbose", "false");
        props.setProperty("ui.quiet", "false");
        props.setProperty("ui.progress", "false");
        props.setProperty("ui.interactive", "false");
        props.setProperty("ui.stats", "false");

        // 文件处理默认值
        props.setProperty("files.recursive", "false");
        props.setProperty("files.batch", "false");
        props.setProperty("files.large.file", "false");

        // 日志默认值
        props.setProperty("logging.level", "1");
    }

    /**
     * 查找配置文件
     */
    private Path findConfigFile() {
        // 首先检查当前目录
        Path localConfig = Paths.get(DEFAULT_CONFIG_FILE);
        if (Files.exists(localConfig)) {
            return localConfig;
        }

        // 搜索其他路径
        for (String path : CONFIG_SEARCH_PATHS) {
            Path configPath = Paths.get(path, DEFAULT_CONFIG_FILE);
            if (Files.exists(configPath)) {
                return configPath;
            }
        }

        return null;
    }

    /**
     * 应用环境变量覆盖
     */
    private void applyEnvironmentVariables(Properties props) {
        // Tesseract 路径环境变量
        String tesseractPath = System.getenv("TESSERACT_PATH");
        if (tesseractPath != null) {
            props.setProperty("tesseract.path", tesseractPath);
        }

        String tessdataPath = System.getenv("TESSDATA_PATH");
        if (tessdataPath != null) {
            props.setProperty("tessdata.path", tessdataPath);
        }

        // 输出目录环境变量
        String outputDir = System.getenv("MARKITDOWN_OUTPUT_DIR");
        if (outputDir != null) {
            props.setProperty("output.dir", outputDir);
        }

        String imageDir = System.getenv("MARKITDOWN_IMAGE_DIR");
        if (imageDir != null) {
            props.setProperty("output.image.dir", imageDir);
        }

        String tempDir = System.getenv("MARKITDOWN_TEMP_DIR");
        if (tempDir != null) {
            props.setProperty("output.temp.dir", tempDir);
        }
    }

    /**
     * 获取配置值
     */
    public String getProperty(String key) {
        return properties.getProperty(key);
    }

    /**
     * 获取配置值，带默认值
     */
    public String getProperty(String key, String defaultValue) {
        return properties.getProperty(key, defaultValue);
    }

    /**
     * 获取布尔配置值
     */
    public boolean getBooleanProperty(String key, boolean defaultValue) {
        String value = properties.getProperty(key);
        if (value == null) {
            return defaultValue;
        }
        return Boolean.parseBoolean(value);
    }

    /**
     * 获取整数配置值
     */
    public int getIntProperty(String key, int defaultValue) {
        String value = properties.getProperty(key);
        if (value == null) {
            return defaultValue;
        }
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            logger.warn("配置项 {} 的值 '{}' 不是有效的整数，使用默认值: {}", key, value, defaultValue);
            return defaultValue;
        }
    }

    /**
     * 获取长整数配置值
     */
    public long getLongProperty(String key, long defaultValue) {
        String value = properties.getProperty(key);
        if (value == null) {
            return defaultValue;
        }
        try {
            return Long.parseLong(value);
        } catch (NumberFormatException e) {
            logger.warn("配置项 {} 的值 '{}' 不是有效的长整数，使用默认值: {}", key, value, defaultValue);
            return defaultValue;
        }
    }

    /**
     * 获取所有配置
     */
    public Properties getAllProperties() {
        return new Properties(properties);
    }

    /**
     * 保存配置到文件
     */
    public void saveConfiguration(Path outputPath) throws IOException {
        try (FileOutputStream fos = new FileOutputStream(outputPath.toFile())) {
            properties.store(fos, "MarkItDown Java Configuration File");
            logger.info("配置已保存到: {}", outputPath);
        }
    }

    /**
     * 生成默认配置文件
     */
    public void generateDefaultConfig(Path outputPath) throws IOException {
        Properties defaultProps = new Properties();
        setDefaultValues(defaultProps);

        try (FileOutputStream fos = new FileOutputStream(outputPath.toFile())) {
            defaultProps.store(fos,
                "# MarkItDown Java 配置文件\n" +
                "# 每个配置项对应命令行参数，支持 # 注释\n" +
                "# 详细配置说明请参考: CONFIG_GUIDE.md\n\n" +
                "# 生成时间: " + new Date()
            );
            logger.info("已生成默认配置文件: {}", outputPath);
        }
    }

    /**
     * 验证配置文件
     */
    public List<String> validateConfiguration(Path configPath) {
        List<String> errors = new ArrayList<>();

        if (!Files.exists(configPath)) {
            errors.add("配置文件不存在: " + configPath);
            return errors;
        }

        Properties testProps = new Properties();
        try (FileInputStream fis = new FileInputStream(configPath.toFile())) {
            testProps.load(new java.io.InputStreamReader(fis, "UTF-8"));
        } catch (IOException e) {
            errors.add("配置文件读取失败: " + e.getMessage());
            return errors;
        }

        // 验证关键配置项
        validatePathConfig(testProps, errors);
        validateBooleanConfig(testProps, errors);
        validateNumericConfig(testProps, errors);
        validateEnumConfig(testProps, errors);

        return errors;
    }

    /**
     * 验证路径配置
     */
    private void validatePathConfig(Properties props, List<String> errors) {
        String[] pathConfigs = {
            "tesseract.path", "tessdata.path", "output.dir",
            "output.image.dir", "output.temp.dir"
        };

        for (String config : pathConfigs) {
            String value = props.getProperty(config);
            if (value != null && !value.trim().isEmpty()) {
                Path path = Paths.get(value);
                if (!Files.exists(path)) {
                    errors.add("路径不存在: " + config + " = " + value);
                }
            }
        }
    }

    /**
     * 验证布尔配置
     */
    private void validateBooleanConfig(Properties props, List<String> errors) {
        String[] boolConfigs = {
            "content.include.metadata", "content.include.images", "content.include.tables",
            "ocr.enable", "output.organize.by.type", "output.preserve.structure",
            "performance.parallel", "performance.optimize.memory",
            "ui.verbose", "ui.quiet", "ui.progress", "ui.interactive",
            "files.recursive", "files.batch", "files.large.file"
        };

        for (String config : boolConfigs) {
            String value = props.getProperty(config);
            if (value != null && !value.equalsIgnoreCase("true") &&
                !value.equalsIgnoreCase("false")) {
                errors.add("布尔值无效: " + config + " = " + value + " (应为 true 或 false)");
            }
        }
    }

    /**
     * 验证数值配置
     */
    private void validateNumericConfig(Properties props, List<String> errors) {
        String[] intConfigs = {
            "performance.threads", "performance.batch.size", "logging.level"
        };

        for (String config : intConfigs) {
            String value = props.getProperty(config);
            if (value != null) {
                try {
                    int intValue = Integer.parseInt(value);
                    if (intValue < 0) {
                        errors.add("数值不能为负数: " + config + " = " + value);
                    }
                } catch (NumberFormatException e) {
                    errors.add("整数值无效: " + config + " = " + value);
                }
            }
        }

        String[] longConfigs = {
            "performance.max.file.size"
        };

        for (String config : longConfigs) {
            String value = props.getProperty(config);
            if (value != null) {
                try {
                    long longValue = Long.parseLong(value);
                    if (longValue < 0) {
                        errors.add("数值不能为负数: " + config + " = " + value);
                    }
                } catch (NumberFormatException e) {
                    errors.add("长整数值无效: " + config + " = " + value);
                }
            }
        }
    }

    /**
     * 验证枚举配置
     */
    private void validateEnumConfig(Properties props, List<String> errors) {
        // OCR语言
        String language = props.getProperty("ocr.language");
        if (language != null && !language.equals("auto")) {
            String[] validLanguages = {"eng", "chi_sim", "chi_tra", "jpn", "kor", "fra", "deu"};
            boolean isValid = false;
            for (String valid : validLanguages) {
                if (valid.equals(language)) {
                    isValid = true;
                    break;
                }
            }
            if (!isValid) {
                errors.add("OCR语言代码无效: " + language + " (支持的值: auto, eng, chi_sim, chi_tra, jpn, kor, fra, deu)");
            }
        }

        // 图片格式
        String imageFormat = props.getProperty("format.image");
        if (imageFormat != null) {
            String[] validFormats = {"markdown", "html", "base64"};
            boolean isValid = false;
            for (String valid : validFormats) {
                if (valid.equals(imageFormat)) {
                    isValid = true;
                    break;
                }
            }
            if (!isValid) {
                errors.add("图片格式无效: " + imageFormat + " (支持的值: markdown, html, base64)");
            }
        }

        // 表格格式
        String tableFormat = props.getProperty("format.table");
        if (tableFormat != null) {
            String[] validFormats = {"github", "markdown", "pipe"};
            boolean isValid = false;
            for (String valid : validFormats) {
                if (valid.equals(tableFormat)) {
                    isValid = true;
                    break;
                }
            }
            if (!isValid) {
                errors.add("表格格式无效: " + tableFormat + " (支持的值: github, markdown, pipe)");
            }
        }
    }

    /**
     * 创建ConversionOptions（从配置文件）
     */
    public ConversionOptions createConversionOptionsFromConfig() {
        ConversionOptions.Builder builder = ConversionOptions.builder();

        // 内容包含选项
        builder.includeMetadata(getBooleanProperty("content.include.metadata", true))
               .includeImages(getBooleanProperty("content.include.images", true))
               .includeTables(getBooleanProperty("content.include.tables", true));

        // OCR选项
        builder.useOcr(getBooleanProperty("ocr.enable", false))
               .language(getProperty("ocr.language", "auto"));

        // 格式选项
        builder.imageFormat(getProperty("format.image", "markdown"))
               .tableFormat(getProperty("format.table", "github"));

        // 性能选项
        builder.maxFileSize(getLongProperty("performance.max.file.size", 50 * 1024 * 1024));

        return builder.build();
    }

    /**
     * 获取Tesseract路径配置
     */
    public String getTesseractPath() {
        return getProperty("tesseract.path", "O:\\tesserOCR");
    }

    /**
     * 获取Tessdata路径配置
     */
    public String getTessdataPath() {
        return getProperty("tessdata.path", "O:\\tesserOCR\\tessdata");
    }

    /**
     * 获取输出目录配置
     */
    public String getOutputDir() {
        return getProperty("output.dir", "./output");
    }

    /**
     * 获取图片目录配置
     */
    public String getImageDir() {
        return getProperty("output.image.dir", "assets");
    }

    /**
     * 获取临时目录配置
     */
    public String getTempDir() {
        return getProperty("output.temp.dir", System.getProperty("java.io.tmpdir"));
    }

    /**
     * 是否按类型组织输出
     */
    public boolean isOrganizeByType() {
        return getBooleanProperty("output.organize.by.type", false);
    }

    /**
     * 是否保持目录结构
     */
    public boolean isPreserveStructure() {
        return getBooleanProperty("output.preserve.structure", false);
    }

    /**
     * 是否启用并行处理
     */
    public boolean isParallelProcessing() {
        return getBooleanProperty("performance.parallel", false);
    }

    /**
     * 获取线程数量
     */
    public int getThreadCount() {
        int threads = getIntProperty("performance.threads", 0);
        return threads == 0 ? Runtime.getRuntime().availableProcessors() : threads;
    }

    /**
     * 是否启用内存优化
     */
    public boolean isMemoryOptimization() {
        return getBooleanProperty("performance.optimize.memory", false);
    }

    /**
     * 是否启用交互模式
     */
    public boolean isInteractiveMode() {
        return getBooleanProperty("ui.interactive", false);
    }

    /**
     * 是否显示进度
     */
    public boolean isShowProgress() {
        return getBooleanProperty("ui.progress", false);
    }

    /**
     * 是否显示统计信息
     */
    public boolean isShowStats() {
        return getBooleanProperty("ui.stats", false);
    }

    /**
     * 是否详细模式
     */
    public boolean isVerbose() {
        return getBooleanProperty("ui.verbose", false);
    }

    /**
     * 是否静默模式
     */
    public boolean isQuiet() {
        return getBooleanProperty("ui.quiet", false);
    }

    /**
     * 是否递归处理
     */
    public boolean isRecursive() {
        return getBooleanProperty("files.recursive", false);
    }

    /**
     * 是否批量处理
     */
    public boolean isBatch() {
        return getBooleanProperty("files.batch", false);
    }

    /**
     * 是否允许大文件
     */
    public boolean isLargeFile() {
        return getBooleanProperty("files.large.file", false);
    }
}