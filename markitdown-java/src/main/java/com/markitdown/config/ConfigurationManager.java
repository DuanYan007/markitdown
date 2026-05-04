package com.markitdown.config;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Properties;

/**
 * Configuration manager with YAML-first loading and legacy properties compatibility.
 */
public class ConfigurationManager {

    private static final Logger logger = LoggerFactory.getLogger(ConfigurationManager.class);

    private static final String PRIMARY_YAML_CONFIG_FILE = "markitdown.yml";
    private static final String LOCAL_YAML_CONFIG_FILE = "markitdown.local.yml";
    private static final String EXAMPLE_YAML_CONFIG_FILE = "markitdown.example.yml";
    private static final String LEGACY_PROPERTIES_CONFIG_FILE = ".markitdown.properties";

    private static final ObjectMapper YAML_MAPPER = new ObjectMapper(new YAMLFactory());

    private final Properties properties;
    private final Path baseDirectory;

    public ConfigurationManager() {
        this(Paths.get(System.getProperty("user.dir")));
    }

    public ConfigurationManager(Path baseDirectory) {
        this.baseDirectory = baseDirectory.toAbsolutePath().normalize();
        this.properties = loadConfiguration();
    }

    private Properties loadConfiguration() {
        Properties defaults = createDefaultProperties();
        Properties props = new Properties();
        props.putAll(defaults);

        loadYamlFile(findConfigFile(PRIMARY_YAML_CONFIG_FILE), props);
        loadYamlFile(findProjectLocalConfigFile(LOCAL_YAML_CONFIG_FILE), props);
        loadLegacyProperties(findConfigFile(LEGACY_PROPERTIES_CONFIG_FILE), props);
        applyEnvironmentVariables(props, defaults);

        return props;
    }

    private Properties createDefaultProperties() {
        Properties props = new Properties();
        setDefaultValues(props);
        return props;
    }

    private void setDefaultValues(Properties props) {
        props.setProperty("app.profile", "default");

        props.setProperty("tesseract.path", "");
        props.setProperty("tessdata.path", "");

        props.setProperty("output.dir", "./output");
        props.setProperty("output.image.dir", "assets");
        props.setProperty("output.temp.dir", System.getProperty("java.io.tmpdir"));
        props.setProperty("output.organize.by.type", "false");
        props.setProperty("output.preserve.structure", "false");

        props.setProperty("content.include.metadata", "true");
        props.setProperty("content.include.images", "true");
        props.setProperty("content.include.tables", "true");
        props.setProperty("content.page.break.mode", "heading");

        props.setProperty("ocr.enable", "false");
        props.setProperty("ocr.engine", "tess4j");
        props.setProperty("ocr.language", "auto");
        props.setProperty("ocr.endpoint", "");
        props.setProperty("ocr.api.key", "");
        props.setProperty("ocr.model", "");
        props.setProperty("ocr.timeout", "30000");
        props.setProperty("ocr.poll.interval", "5000");

        props.setProperty("format.image", "markdown");
        props.setProperty("format.table", "github");

        props.setProperty("performance.parallel", "false");
        props.setProperty("performance.threads", "0");
        props.setProperty("performance.optimize.memory", "false");
        props.setProperty("performance.max.file.size", "52428800");
        props.setProperty("performance.batch.size", "20");

        props.setProperty("ui.verbose", "false");
        props.setProperty("ui.quiet", "false");
        props.setProperty("ui.progress", "false");
        props.setProperty("ui.interactive", "false");
        props.setProperty("ui.stats", "false");

        props.setProperty("files.recursive", "false");
        props.setProperty("files.batch", "false");
        props.setProperty("files.large.file", "false");

        props.setProperty("logging.level", "1");
    }

    private void loadYamlFile(Path configPath, Properties props) {
        if (configPath == null) {
            return;
        }

        try {
            Map<String, Object> yamlData = YAML_MAPPER.readValue(
                    Files.newBufferedReader(configPath, StandardCharsets.UTF_8),
                    new TypeReference<Map<String, Object>>() {}
            );
            if (yamlData != null) {
                Map<String, String> flattened = new LinkedHashMap<>();
                flattenYaml("", yamlData, flattened);
                for (Map.Entry<String, String> entry : flattened.entrySet()) {
                    props.setProperty(normalizeYamlKey(entry.getKey()), entry.getValue());
                }
            }
            logger.info("Loaded YAML configuration: {}", configPath);
        } catch (Exception e) {
            logger.warn("Failed to load YAML configuration {}: {}", configPath, e.getMessage());
        }
    }

    private void loadLegacyProperties(Path configPath, Properties props) {
        if (configPath == null) {
            return;
        }

        try (FileInputStream fis = new FileInputStream(configPath.toFile())) {
            Properties legacy = new Properties();
            legacy.load(new InputStreamReader(fis, StandardCharsets.UTF_8));
            props.putAll(legacy);
            logger.info("Loaded legacy properties configuration: {}", configPath);
        } catch (Exception e) {
            logger.warn("Failed to load legacy properties configuration {}: {}", configPath, e.getMessage());
        }
    }

    @SuppressWarnings("unchecked")
    private void flattenYaml(String prefix, Object value, Map<String, String> flattened) {
        if (value instanceof Map<?, ?>) {
            Map<?, ?> map = (Map<?, ?>) value;
            for (Map.Entry<?, ?> entry : map.entrySet()) {
                String key = String.valueOf(entry.getKey());
                String nextPrefix = prefix.isEmpty() ? key : prefix + "." + key;
                flattenYaml(nextPrefix, entry.getValue(), flattened);
            }
            return;
        }

        if (value instanceof List<?>) {
            List<?> list = (List<?>) value;
            List<String> values = new ArrayList<>();
            for (Object item : list) {
                values.add(String.valueOf(item));
            }
            flattened.put(prefix, String.join(",", values));
            return;
        }

        if (value != null) {
            flattened.put(prefix, String.valueOf(value));
        }
    }

    private String normalizeYamlKey(String key) {
        String normalized = key.replace('_', '.');
        if ("ocr.enabled".equals(normalized)) {
            return "ocr.enable";
        }
        return normalized;
    }

    private Path findProjectLocalConfigFile(String fileName) {
        Path path = baseDirectory.resolve(fileName);
        return Files.exists(path) ? path : null;
    }

    private Path findConfigFile(String fileName) {
        for (String path : getConfigSearchPaths()) {
            Path configPath = ".".equals(path) ? Paths.get(fileName) : Paths.get(path, fileName);
            if (Files.exists(configPath)) {
                return configPath;
            }
        }
        return null;
    }

    private List<String> getConfigSearchPaths() {
        return Arrays.asList(
                baseDirectory.toString(),
                baseDirectory.resolve("config").toString(),
                System.getProperty("user.home"),
                "/etc/markitdown"
        );
    }

    private void applyEnvironmentVariables(Properties props, Properties defaults) {
        applyEnvironmentVariableIfSupplemental(props, defaults, "TESSERACT_PATH", "tesseract.path");
        applyEnvironmentVariableIfSupplemental(props, defaults, "TESSDATA_PATH", "tessdata.path");
        applyEnvironmentVariableIfSupplemental(props, defaults, "MARKITDOWN_OUTPUT_DIR", "output.dir");
        applyEnvironmentVariableIfSupplemental(props, defaults, "MARKITDOWN_IMAGE_DIR", "output.image.dir");
        applyEnvironmentVariableIfSupplemental(props, defaults, "MARKITDOWN_TEMP_DIR", "output.temp.dir");

        applyEnvironmentVariableIfSupplemental(props, defaults, "MARKITDOWN_OCR_ENGINE", "ocr.engine");
        applyEnvironmentVariableIfSupplemental(props, defaults, "MARKITDOWN_OCR_ENDPOINT", "ocr.endpoint");
        applyEnvironmentVariableIfSupplemental(props, defaults, "MARKITDOWN_OCR_API_KEY", "ocr.api.key");
        applyEnvironmentVariableIfSupplemental(props, defaults, "MARKITDOWN_OCR_MODEL", "ocr.model");
        applyEnvironmentVariableIfSupplemental(props, defaults, "MARKITDOWN_OCR_TIMEOUT", "ocr.timeout");
        applyEnvironmentVariableIfSupplemental(props, defaults, "MARKITDOWN_OCR_POLL_INTERVAL", "ocr.poll.interval");

        applyEnvironmentVariableIfSupplemental(props, defaults, "PADDLE_OCR_TOKEN", "ocr.api.key");
        applyEnvironmentVariableIfSupplemental(props, defaults, "PADDLE_OCR_JOB_URL", "ocr.endpoint");
        applyEnvironmentVariableIfSupplemental(props, defaults, "PADDLE_OCR_MODEL", "ocr.model");
        applyEnvironmentVariableIfSupplemental(props, defaults, "PADDLE_OCR_POLL_INTERVAL_MS", "ocr.poll.interval");
    }

    private void applyEnvironmentVariableIfSupplemental(Properties props, Properties defaults,
                                                        String envName, String propertyName) {
        String envValue = System.getenv(envName);
        if (envValue == null || envValue.isBlank()) {
            return;
        }

        String currentValue = props.getProperty(propertyName);
        String defaultValue = defaults.getProperty(propertyName);
        if (currentValue == null || currentValue.isBlank() || Objects.equals(currentValue, defaultValue)) {
            props.setProperty(propertyName, envValue);
        }
    }

    public String getProperty(String key) {
        return properties.getProperty(key);
    }

    public String getProperty(String key, String defaultValue) {
        return properties.getProperty(key, defaultValue);
    }

    public boolean getBooleanProperty(String key, boolean defaultValue) {
        String value = properties.getProperty(key);
        return value == null ? defaultValue : Boolean.parseBoolean(value);
    }

    public int getIntProperty(String key, int defaultValue) {
        String value = properties.getProperty(key);
        if (value == null) {
            return defaultValue;
        }
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            logger.warn("Invalid integer for {}='{}', using default {}", key, value, defaultValue);
            return defaultValue;
        }
    }

    public long getLongProperty(String key, long defaultValue) {
        String value = properties.getProperty(key);
        if (value == null) {
            return defaultValue;
        }
        try {
            return Long.parseLong(value);
        } catch (NumberFormatException e) {
            logger.warn("Invalid long for {}='{}', using default {}", key, value, defaultValue);
            return defaultValue;
        }
    }

    public Properties getAllProperties() {
        Properties copy = new Properties();
        copy.putAll(properties);
        return copy;
    }

    public void saveConfiguration(Path outputPath) throws IOException {
        if (isYamlFile(outputPath)) {
            writeYamlConfiguration(outputPath, properties);
            return;
        }

        try (FileOutputStream fos = new FileOutputStream(outputPath.toFile())) {
            properties.store(fos, "MarkItDown Java Configuration File");
            logger.info("Saved configuration to {}", outputPath);
        }
    }

    public void generateDefaultConfig(Path outputPath) throws IOException {
        Properties defaultProps = createDefaultProperties();
        if (isYamlFile(outputPath)) {
            writeYamlConfiguration(outputPath, defaultProps);
            logger.info("Generated default YAML configuration at {}", outputPath);
            return;
        }

        try (FileOutputStream fos = new FileOutputStream(outputPath.toFile())) {
            defaultProps.store(fos,
                    "# MarkItDown Java Configuration File\n" +
                            "# Generated at: " + new Date()
            );
            logger.info("Generated default properties configuration at {}", outputPath);
        }
    }

    private boolean isYamlFile(Path path) {
        String fileName = path.getFileName().toString().toLowerCase();
        return fileName.endsWith(".yml") || fileName.endsWith(".yaml");
    }

    private void writeYamlConfiguration(Path outputPath, Properties source) throws IOException {
        Map<String, Object> yaml = buildYamlMap(source);
        try (Writer writer = Files.newBufferedWriter(outputPath, StandardCharsets.UTF_8)) {
            YAML_MAPPER.writeValue(writer, yaml);
        }
    }

    private Map<String, Object> buildYamlMap(Properties source) {
        Map<String, Object> root = new LinkedHashMap<>();

        putYamlValue(root, "app.profile", source.getProperty("app.profile", "default"));

        putYamlValue(root, "ocr.enabled", Boolean.parseBoolean(source.getProperty("ocr.enable", "false")));
        putYamlValue(root, "ocr.engine", source.getProperty("ocr.engine", "tess4j"));
        putYamlValue(root, "ocr.endpoint", source.getProperty("ocr.endpoint", ""));
        putYamlValue(root, "ocr.api_key", source.getProperty("ocr.api.key", ""));
        putYamlValue(root, "ocr.model", source.getProperty("ocr.model", ""));
        putYamlValue(root, "ocr.timeout", Integer.parseInt(source.getProperty("ocr.timeout", "30000")));
        putYamlValue(root, "ocr.poll_interval", Integer.parseInt(source.getProperty("ocr.poll.interval", "5000")));
        putYamlValue(root, "ocr.language", source.getProperty("ocr.language", "auto"));

        putYamlValue(root, "content.include_metadata", Boolean.parseBoolean(source.getProperty("content.include.metadata", "true")));
        putYamlValue(root, "content.include_images", Boolean.parseBoolean(source.getProperty("content.include.images", "true")));
        putYamlValue(root, "content.include_tables", Boolean.parseBoolean(source.getProperty("content.include.tables", "true")));
        putYamlValue(root, "content.page_break_mode", source.getProperty("content.page.break.mode", "heading"));

        putYamlValue(root, "output.dir", source.getProperty("output.dir", "./output"));
        putYamlValue(root, "output.image_dir", source.getProperty("output.image.dir", "assets"));
        putYamlValue(root, "output.preserve_structure", Boolean.parseBoolean(source.getProperty("output.preserve.structure", "false")));
        putYamlValue(root, "output.organize_by_type", Boolean.parseBoolean(source.getProperty("output.organize.by.type", "false")));

        putYamlValue(root, "format.image", source.getProperty("format.image", "markdown"));
        putYamlValue(root, "format.table", source.getProperty("format.table", "github"));

        putYamlValue(root, "performance.parallel", Boolean.parseBoolean(source.getProperty("performance.parallel", "false")));
        putYamlValue(root, "performance.threads", Integer.parseInt(source.getProperty("performance.threads", "0")));
        putYamlValue(root, "performance.optimize_memory", Boolean.parseBoolean(source.getProperty("performance.optimize.memory", "false")));
        putYamlValue(root, "performance.max_file_size", Long.parseLong(source.getProperty("performance.max.file.size", "52428800")));
        putYamlValue(root, "performance.batch_size", Integer.parseInt(source.getProperty("performance.batch.size", "20")));

        return root;
    }

    @SuppressWarnings("unchecked")
    private void putYamlValue(Map<String, Object> root, String path, Object value) {
        String[] parts = path.split("\\.");
        Map<String, Object> current = root;
        for (int i = 0; i < parts.length - 1; i++) {
            current = (Map<String, Object>) current.computeIfAbsent(parts[i], ignored -> new LinkedHashMap<>());
        }
        current.put(parts[parts.length - 1], value);
    }

    public List<String> validateConfiguration(Path configPath) {
        List<String> errors = new ArrayList<>();

        if (!Files.exists(configPath)) {
            errors.add("Configuration file does not exist: " + configPath);
            return errors;
        }

        Properties testProps = createDefaultProperties();
        if (isYamlFile(configPath)) {
            loadYamlFile(configPath, testProps);
        } else {
            loadLegacyProperties(configPath, testProps);
        }

        validatePathConfig(testProps, errors);
        validateBooleanConfig(testProps, errors);
        validateNumericConfig(testProps, errors);
        validateEnumConfig(testProps, errors);

        return errors;
    }

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
                    errors.add("Path does not exist: " + config + " = " + value);
                }
            }
        }
    }

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
            if (value != null && !value.equalsIgnoreCase("true") && !value.equalsIgnoreCase("false")) {
                errors.add("Invalid boolean: " + config + " = " + value);
            }
        }
    }

    private void validateNumericConfig(Properties props, List<String> errors) {
        for (String config : Arrays.asList("performance.threads", "performance.batch.size", "logging.level", "ocr.timeout", "ocr.poll.interval")) {
            String value = props.getProperty(config);
            if (value == null) {
                continue;
            }
            try {
                if (Integer.parseInt(value) < 0) {
                    errors.add("Negative integer is not allowed: " + config + " = " + value);
                }
            } catch (NumberFormatException e) {
                errors.add("Invalid integer: " + config + " = " + value);
            }
        }

        String maxFileSize = props.getProperty("performance.max.file.size");
        if (maxFileSize != null) {
            try {
                if (Long.parseLong(maxFileSize) < 0) {
                    errors.add("Negative long is not allowed: performance.max.file.size = " + maxFileSize);
                }
            } catch (NumberFormatException e) {
                errors.add("Invalid long: performance.max.file.size = " + maxFileSize);
            }
        }
    }

    private void validateEnumConfig(Properties props, List<String> errors) {
        validateEnum(props, errors, "ocr.language", new String[]{"auto", "eng", "chi_sim", "chi_tra", "jpn", "kor", "fra", "deu"});
        validateEnum(props, errors, "format.image", new String[]{"markdown", "html", "base64"});
        validateEnum(props, errors, "format.table", new String[]{"github", "markdown", "pipe"});
        validateEnum(props, errors, "ocr.engine", new String[]{"tess4j", "tesseract-cli", "paddleocr", "http"});
    }

    private void validateEnum(Properties props, List<String> errors, String key, String[] validValues) {
        String value = props.getProperty(key);
        if (value == null || value.isBlank()) {
            return;
        }
        for (String valid : validValues) {
            if (valid.equals(value)) {
                return;
            }
        }
        errors.add("Invalid value for " + key + ": " + value);
    }

    public ConversionOptions createConversionOptionsFromConfig() {
        return ConversionOptions.builder()
                .includeMetadata(getBooleanProperty("content.include.metadata", true))
                .includeImages(getBooleanProperty("content.include.images", true))
                .includeTables(getBooleanProperty("content.include.tables", true))
                .useOcr(getBooleanProperty("ocr.enable", false))
                .language(getProperty("ocr.language", "auto"))
                .ocrEngine(getProperty("ocr.engine", "tess4j"))
                .ocrEndpoint(getProperty("ocr.endpoint", ""))
                .ocrApiKey(getProperty("ocr.api.key", ""))
                .ocrModel(getProperty("ocr.model", ""))
                .ocrTimeout(getIntProperty("ocr.timeout", 30000))
                .ocrPollInterval(getIntProperty("ocr.poll.interval", 5000))
                .imageFormat(getProperty("format.image", "markdown"))
                .tableFormat(getProperty("format.table", "github"))
                .maxFileSize(getLongProperty("performance.max.file.size", 50L * 1024 * 1024))
                .build();
    }

    public String getTesseractPath() {
        return getProperty("tesseract.path", "");
    }

    public String getTessdataPath() {
        return getProperty("tessdata.path", "");
    }

    public String getOcrEngine() {
        return getProperty("ocr.engine", "tess4j");
    }

    public String getOcrEndpoint() {
        return getProperty("ocr.endpoint", "");
    }

    public String getOcrApiKey() {
        return getProperty("ocr.api.key", "");
    }

    public int getOcrTimeout() {
        return getIntProperty("ocr.timeout", 30000);
    }

    public String getOcrModel() {
        return getProperty("ocr.model", "");
    }

    public int getOcrPollInterval() {
        return getIntProperty("ocr.poll.interval", 5000);
    }

    public String getOutputDir() {
        return getProperty("output.dir", "./output");
    }

    public String getImageDir() {
        return getProperty("output.image.dir", "assets");
    }

    public String getTempDir() {
        return getProperty("output.temp.dir", System.getProperty("java.io.tmpdir"));
    }

    public boolean isOrganizeByType() {
        return getBooleanProperty("output.organize.by.type", false);
    }

    public boolean isPreserveStructure() {
        return getBooleanProperty("output.preserve.structure", false);
    }

    public boolean isParallelProcessing() {
        return getBooleanProperty("performance.parallel", false);
    }

    public int getThreadCount() {
        int threads = getIntProperty("performance.threads", 0);
        return threads == 0 ? Runtime.getRuntime().availableProcessors() : threads;
    }

    public boolean isMemoryOptimization() {
        return getBooleanProperty("performance.optimize.memory", false);
    }

    public boolean isInteractiveMode() {
        return getBooleanProperty("ui.interactive", false);
    }

    public boolean isShowProgress() {
        return getBooleanProperty("ui.progress", false);
    }

    public boolean isShowStats() {
        return getBooleanProperty("ui.stats", false);
    }

    public boolean isVerbose() {
        return getBooleanProperty("ui.verbose", false);
    }

    public boolean isQuiet() {
        return getBooleanProperty("ui.quiet", false);
    }

    public boolean isRecursive() {
        return getBooleanProperty("files.recursive", false);
    }

    public boolean isBatch() {
        return getBooleanProperty("files.batch", false);
    }

    public boolean isLargeFile() {
        return getBooleanProperty("files.large.file", false);
    }

    public String getDefaultConfigFileName() {
        return PRIMARY_YAML_CONFIG_FILE;
    }

    public String getLocalConfigFileName() {
        return LOCAL_YAML_CONFIG_FILE;
    }

    public String getLegacyConfigFileName() {
        return LEGACY_PROPERTIES_CONFIG_FILE;
    }

    public String getExampleConfigFileName() {
        return EXAMPLE_YAML_CONFIG_FILE;
    }
}
