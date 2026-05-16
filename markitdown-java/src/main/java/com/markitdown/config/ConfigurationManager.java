package com.markitdown.config;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Configuration manager with YAML-only loading.
 */
public class ConfigurationManager {

    private static final Logger logger = LoggerFactory.getLogger(ConfigurationManager.class);

    private static final String PRIMARY_YAML_CONFIG_FILE = "markitdown.yml";
    private static final String LOCAL_YAML_CONFIG_FILE = "markitdown.local.yml";
    private static final String EXAMPLE_YAML_CONFIG_FILE = "markitdown.example.yml";

    private static final ObjectMapper YAML_MAPPER = new ObjectMapper(new YAMLFactory());

    private final Map<String, String> values;
    private final Map<String, TrackedValue> trackedProperties;
    private final Path baseDirectory;
    private final Path explicitConfigPath;
    private EffectiveConfiguration effectiveConfiguration;

    public ConfigurationManager() {
        this(Paths.get(System.getProperty("user.dir")));
    }

    public ConfigurationManager(Path baseDirectory) {
        this(baseDirectory, null);
    }

    public ConfigurationManager(Path baseDirectory, Path explicitConfigPath) {
        this.baseDirectory = baseDirectory.toAbsolutePath().normalize();
        this.explicitConfigPath = explicitConfigPath == null ? null : explicitConfigPath.toAbsolutePath().normalize();
        if (this.explicitConfigPath != null && !isYamlFile(this.explicitConfigPath)) {
            throw new IllegalArgumentException("Only YAML configuration files are supported: " + this.explicitConfigPath);
        }
        LoadedConfiguration configuration = loadConfiguration();
        this.values = configuration.values;
        this.trackedProperties = configuration.trackedProperties;
    }

    private LoadedConfiguration loadConfiguration() {
        Map<String, String> defaults = createDefaultValues();
        Map<String, String> resolvedValues = new LinkedHashMap<>(defaults);
        Map<String, TrackedValue> tracked = initializeTrackedDefaults(defaults);

        if (explicitConfigPath != null) {
            loadYamlFile(explicitConfigPath, resolvedValues, tracked, ConfigSource.EXPLICIT_YAML);
        } else {
            loadYamlFile(findProjectLocalConfigFile(PRIMARY_YAML_CONFIG_FILE), resolvedValues, tracked, ConfigSource.PROJECT_YAML);
            loadYamlFile(findProjectLocalConfigFile(LOCAL_YAML_CONFIG_FILE), resolvedValues, tracked, ConfigSource.LOCAL_YAML);
        }

        return new LoadedConfiguration(resolvedValues, tracked);
    }

    private Map<String, String> createDefaultValues() {
        Map<String, String> defaults = new LinkedHashMap<>();
        for (ConfigKey key : ConfigKey.values()) {
            defaults.put(key.key, key.defaultValue);
        }
        return defaults;
    }

    private void loadYamlFile(Path configPath, Map<String, String> resolvedValues, Map<String, TrackedValue> tracked,
                              ConfigSource source) {
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
                    setTrackedProperty(resolvedValues, tracked, normalizeYamlKey(entry.getKey()), entry.getValue(), source);
                }
            }
            logger.info("Loaded YAML configuration: {}", configPath);
        } catch (Exception e) {
            logger.warn("Failed to load YAML configuration {}: {}", configPath, e.getMessage());
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

    private Map<String, TrackedValue> initializeTrackedDefaults(Map<String, String> defaults) {
        Map<String, TrackedValue> tracked = new LinkedHashMap<>();
        for (Map.Entry<String, String> entry : defaults.entrySet()) {
            tracked.put(entry.getKey(), new TrackedValue(entry.getValue(), ConfigSource.DEFAULT));
        }
        return tracked;
    }

    private void setTrackedProperty(Map<String, String> resolvedValues, Map<String, TrackedValue> tracked,
                                    String key, String value, ConfigSource source) {
        resolvedValues.put(key, value);
        tracked.put(key, new TrackedValue(value, source));
    }

    public String getPropertySource(ConfigKey key) {
        if (key == null) {
            return "unknown";
        }
        TrackedValue trackedValue = trackedProperties.get(key.key);
        return trackedValue == null ? "unknown" : trackedValue.source.label;
    }

    public Map<String, TrackedValue> getTrackedProperties() {
        return new LinkedHashMap<>(trackedProperties);
    }

    private void applyOverride(ConfigKey key, String value, ConfigSource source) {
        if (key == null || value == null || source == null) {
            return;
        }
        setTrackedProperty(values, trackedProperties, key.key, value, source);
        effectiveConfiguration = null;
    }

    private void applyOverride(ConfigKey key, boolean value, ConfigSource source) {
        applyOverride(key, String.valueOf(value), source);
    }

    private void applyOverride(ConfigKey key, int value, ConfigSource source) {
        applyOverride(key, String.valueOf(value), source);
    }

    private void applyOverride(ConfigKey key, long value, ConfigSource source) {
        applyOverride(key, String.valueOf(value), source);
    }

    public void overrideContentIncludeImages(boolean value, ConfigSource source) {
        applyOverride(ConfigKey.CONTENT_INCLUDE_IMAGES, value, source);
    }

    public void overrideContentIncludeTables(boolean value, ConfigSource source) {
        applyOverride(ConfigKey.CONTENT_INCLUDE_TABLES, value, source);
    }

    public void overrideContentIncludeMetadata(boolean value, ConfigSource source) {
        applyOverride(ConfigKey.CONTENT_INCLUDE_METADATA, value, source);
    }

    public void overrideOcrEnabled(boolean value, ConfigSource source) {
        applyOverride(ConfigKey.OCR_ENABLE, value, source);
    }

    public void overrideOcrLanguage(String value, ConfigSource source) {
        applyOverride(ConfigKey.OCR_LANGUAGE, value, source);
    }

    public void overrideOcrEngine(String value, ConfigSource source) {
        applyOverride(ConfigKey.OCR_ENGINE, value, source);
    }

    public void overrideOcrEndpoint(String value, ConfigSource source) {
        applyOverride(ConfigKey.OCR_ENDPOINT, value, source);
    }

    public void overrideOcrApiKey(String value, ConfigSource source) {
        applyOverride(ConfigKey.OCR_API_KEY, value, source);
    }

    public void overrideOcrModel(String value, ConfigSource source) {
        applyOverride(ConfigKey.OCR_MODEL, value, source);
    }

    public void overrideOcrTimeout(int value, ConfigSource source) {
        applyOverride(ConfigKey.OCR_TIMEOUT, value, source);
    }

    public void overrideOcrPollInterval(int value, ConfigSource source) {
        applyOverride(ConfigKey.OCR_POLL_INTERVAL, value, source);
    }

    public void overrideTableFormat(String value, ConfigSource source) {
        applyOverride(ConfigKey.FORMAT_TABLE, value, source);
    }

    public void overrideImageFormat(String value, ConfigSource source) {
        applyOverride(ConfigKey.FORMAT_IMAGE, value, source);
    }

    public void overrideOutputImageDir(String value, ConfigSource source) {
        applyOverride(ConfigKey.OUTPUT_IMAGE_DIR, value, source);
    }

    public void overrideOutputTempDir(String value, ConfigSource source) {
        applyOverride(ConfigKey.OUTPUT_TEMP_DIR, value, source);
    }

    public void overridePerformanceMaxFileSize(long value, ConfigSource source) {
        applyOverride(ConfigKey.PERFORMANCE_MAX_FILE_SIZE, value, source);
    }

    public void overridePerformanceParallel(boolean value, ConfigSource source) {
        applyOverride(ConfigKey.PERFORMANCE_PARALLEL, value, source);
    }

    public void overridePerformanceThreads(int value, ConfigSource source) {
        applyOverride(ConfigKey.PERFORMANCE_THREADS, value, source);
    }

    public void overridePerformanceOptimizeMemory(boolean value, ConfigSource source) {
        applyOverride(ConfigKey.PERFORMANCE_OPTIMIZE_MEMORY, value, source);
    }

    public void overrideUiProgress(boolean value, ConfigSource source) {
        applyOverride(ConfigKey.UI_PROGRESS, value, source);
    }

    public void overrideUiStats(boolean value, ConfigSource source) {
        applyOverride(ConfigKey.UI_STATS, value, source);
    }

    public void overrideUiVerbose(boolean value, ConfigSource source) {
        applyOverride(ConfigKey.UI_VERBOSE, value, source);
    }

    public void overrideUiQuiet(boolean value, ConfigSource source) {
        applyOverride(ConfigKey.UI_QUIET, value, source);
    }

    public void overrideFilesRecursive(boolean value, ConfigSource source) {
        applyOverride(ConfigKey.FILES_RECURSIVE, value, source);
    }

    public void overrideFilesBatch(boolean value, ConfigSource source) {
        applyOverride(ConfigKey.FILES_BATCH, value, source);
    }

    public void overrideFilesLargeFile(boolean value, ConfigSource source) {
        applyOverride(ConfigKey.FILES_LARGE_FILE, value, source);
    }

    public void saveConfiguration(Path outputPath) throws IOException {
        if (!isYamlFile(outputPath)) {
            throw new IOException("Only YAML configuration files are supported: " + outputPath);
        }
        writeYamlConfiguration(outputPath, values);
    }

    public void generateDefaultConfig(Path outputPath) throws IOException {
        if (!isYamlFile(outputPath)) {
            throw new IOException("Only YAML configuration files are supported: " + outputPath);
        }
        Map<String, String> defaultValues = createDefaultValues();
        writeYamlConfiguration(outputPath, defaultValues);
        logger.info("Generated default YAML configuration at {}", outputPath);
    }

    private boolean isYamlFile(Path path) {
        String fileName = path.getFileName().toString().toLowerCase();
        return fileName.endsWith(".yml") || fileName.endsWith(".yaml");
    }

    private void writeYamlConfiguration(Path outputPath, Map<String, String> source) throws IOException {
        Map<String, Object> yaml = buildYamlMap(source);
        try (Writer writer = Files.newBufferedWriter(outputPath, StandardCharsets.UTF_8)) {
            YAML_MAPPER.writeValue(writer, yaml);
        }
    }

    private Map<String, Object> buildYamlMap(Map<String, String> source) {
        Map<String, Object> root = new LinkedHashMap<>();

        putYamlValue(root, ConfigKey.TESSERACT_PATH.yamlPath, stringValue(source, ConfigKey.TESSERACT_PATH));
        putYamlValue(root, ConfigKey.TESSDATA_PATH.yamlPath, stringValue(source, ConfigKey.TESSDATA_PATH));

        putYamlValue(root, ConfigKey.OUTPUT_DIR.yamlPath, stringValue(source, ConfigKey.OUTPUT_DIR));
        putYamlValue(root, ConfigKey.OUTPUT_IMAGE_DIR.yamlPath, stringValue(source, ConfigKey.OUTPUT_IMAGE_DIR));
        putYamlValue(root, ConfigKey.OUTPUT_TEMP_DIR.yamlPath, stringValue(source, ConfigKey.OUTPUT_TEMP_DIR));
        putYamlValue(root, ConfigKey.OUTPUT_ORGANIZE_BY_TYPE.yamlPath, booleanValue(source, ConfigKey.OUTPUT_ORGANIZE_BY_TYPE));
        putYamlValue(root, ConfigKey.OUTPUT_PRESERVE_STRUCTURE.yamlPath, booleanValue(source, ConfigKey.OUTPUT_PRESERVE_STRUCTURE));

        putYamlValue(root, ConfigKey.CONTENT_INCLUDE_METADATA.yamlPath, booleanValue(source, ConfigKey.CONTENT_INCLUDE_METADATA));
        putYamlValue(root, ConfigKey.CONTENT_INCLUDE_IMAGES.yamlPath, booleanValue(source, ConfigKey.CONTENT_INCLUDE_IMAGES));
        putYamlValue(root, ConfigKey.CONTENT_INCLUDE_TABLES.yamlPath, booleanValue(source, ConfigKey.CONTENT_INCLUDE_TABLES));
        putYamlValue(root, ConfigKey.CONTENT_PAGE_BREAK_MODE.yamlPath, stringValue(source, ConfigKey.CONTENT_PAGE_BREAK_MODE));

        putYamlValue(root, ConfigKey.OCR_ENABLE.yamlPath, booleanValue(source, ConfigKey.OCR_ENABLE));
        putYamlValue(root, ConfigKey.OCR_ENGINE.yamlPath, stringValue(source, ConfigKey.OCR_ENGINE));
        putYamlValue(root, ConfigKey.OCR_LANGUAGE.yamlPath, stringValue(source, ConfigKey.OCR_LANGUAGE));
        putYamlValue(root, ConfigKey.OCR_ENDPOINT.yamlPath, stringValue(source, ConfigKey.OCR_ENDPOINT));
        putYamlValue(root, ConfigKey.OCR_API_KEY.yamlPath, stringValue(source, ConfigKey.OCR_API_KEY));
        putYamlValue(root, ConfigKey.OCR_MODEL.yamlPath, stringValue(source, ConfigKey.OCR_MODEL));
        putYamlValue(root, ConfigKey.OCR_TIMEOUT.yamlPath, intValue(source, ConfigKey.OCR_TIMEOUT));
        putYamlValue(root, ConfigKey.OCR_POLL_INTERVAL.yamlPath, intValue(source, ConfigKey.OCR_POLL_INTERVAL));

        putYamlValue(root, ConfigKey.FORMAT_IMAGE.yamlPath, stringValue(source, ConfigKey.FORMAT_IMAGE));
        putYamlValue(root, ConfigKey.FORMAT_TABLE.yamlPath, stringValue(source, ConfigKey.FORMAT_TABLE));

        putYamlValue(root, ConfigKey.PERFORMANCE_PARALLEL.yamlPath, booleanValue(source, ConfigKey.PERFORMANCE_PARALLEL));
        putYamlValue(root, ConfigKey.PERFORMANCE_THREADS.yamlPath, intValue(source, ConfigKey.PERFORMANCE_THREADS));
        putYamlValue(root, ConfigKey.PERFORMANCE_OPTIMIZE_MEMORY.yamlPath, booleanValue(source, ConfigKey.PERFORMANCE_OPTIMIZE_MEMORY));
        putYamlValue(root, ConfigKey.PERFORMANCE_MAX_FILE_SIZE.yamlPath, longValue(source, ConfigKey.PERFORMANCE_MAX_FILE_SIZE));
        putYamlValue(root, ConfigKey.PERFORMANCE_BATCH_SIZE.yamlPath, intValue(source, ConfigKey.PERFORMANCE_BATCH_SIZE));

        putYamlValue(root, ConfigKey.UI_VERBOSE.yamlPath, booleanValue(source, ConfigKey.UI_VERBOSE));
        putYamlValue(root, ConfigKey.UI_QUIET.yamlPath, booleanValue(source, ConfigKey.UI_QUIET));
        putYamlValue(root, ConfigKey.UI_PROGRESS.yamlPath, booleanValue(source, ConfigKey.UI_PROGRESS));
        putYamlValue(root, ConfigKey.UI_STATS.yamlPath, booleanValue(source, ConfigKey.UI_STATS));

        putYamlValue(root, ConfigKey.FILES_RECURSIVE.yamlPath, booleanValue(source, ConfigKey.FILES_RECURSIVE));
        putYamlValue(root, ConfigKey.FILES_BATCH.yamlPath, booleanValue(source, ConfigKey.FILES_BATCH));
        putYamlValue(root, ConfigKey.FILES_LARGE_FILE.yamlPath, booleanValue(source, ConfigKey.FILES_LARGE_FILE));

        return root;
    }

    private String stringValue(Map<String, String> source, ConfigKey key) {
        return source.getOrDefault(key.key, key.defaultValue);
    }

    private boolean booleanValue(Map<String, String> source, ConfigKey key) {
        return Boolean.parseBoolean(stringValue(source, key));
    }

    private int intValue(Map<String, String> source, ConfigKey key) {
        return Integer.parseInt(stringValue(source, key));
    }

    private long longValue(Map<String, String> source, ConfigKey key) {
        return Long.parseLong(stringValue(source, key));
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

        Map<String, String> testValues = createDefaultValues();
        Map<String, TrackedValue> tracked = initializeTrackedDefaults(testValues);
        if (!isYamlFile(configPath)) {
            errors.add("Only YAML configuration files are supported: " + configPath);
            return errors;
        }
        if (!loadYamlFileForValidation(configPath, testValues, tracked, errors)) {
            return errors;
        }

        validatePathConfig(testValues, errors);
        validateBooleanConfig(testValues, errors);
        validateNumericConfig(testValues, errors);
        validateEnumConfig(testValues, errors);
        validateSemanticConfig(testValues, errors);

        return errors;
    }

    private boolean loadYamlFileForValidation(Path configPath, Map<String, String> values,
                                              Map<String, TrackedValue> tracked,
                                              List<String> errors) {
        try {
            Map<String, Object> yamlData = YAML_MAPPER.readValue(
                    Files.newBufferedReader(configPath, StandardCharsets.UTF_8),
                    new TypeReference<Map<String, Object>>() {}
            );
            if (yamlData != null) {
                Map<String, String> flattened = new LinkedHashMap<>();
                flattenYaml("", yamlData, flattened);
                validateKnownKeys(flattened, errors);
                for (Map.Entry<String, String> entry : flattened.entrySet()) {
                    setTrackedProperty(values, tracked, normalizeYamlKey(entry.getKey()), entry.getValue(), ConfigSource.EXPLICIT_YAML);
                }
            }
            return true;
        } catch (Exception e) {
            errors.add("Failed to parse YAML configuration: " + e.getMessage());
            return false;
        }
    }

    private void validatePathConfig(Map<String, String> values, List<String> errors) {
        String tesseractPath = values.get("tesseract.path");
        if (tesseractPath != null && !tesseractPath.trim().isEmpty()) {
            Path path = Paths.get(tesseractPath);
            if (!Files.exists(path)) {
                errors.add("Tesseract path does not exist: " + tesseractPath);
            } else if (Files.isDirectory(path)) {
                errors.add("Tesseract path must point to an executable file: " + tesseractPath);
            }
        }

        String tessdataPath = values.get("tessdata.path");
        if (tessdataPath != null && !tessdataPath.trim().isEmpty()) {
            Path path = Paths.get(tessdataPath);
            if (!Files.exists(path)) {
                errors.add("Tessdata path does not exist: " + tessdataPath);
            } else if (!Files.isDirectory(path)) {
                errors.add("Tessdata path must be a directory: " + tessdataPath);
            }
        }

        for (String config : Arrays.asList("output.dir", "output.temp.dir")) {
            String value = values.get(config);
            if (value == null || value.trim().isEmpty()) {
                continue;
            }
            Path path = Paths.get(value);
            if (Files.exists(path) && !Files.isDirectory(path)) {
                errors.add(config + " must be a directory path: " + value);
            }
        }
    }

    private void validateBooleanConfig(Map<String, String> values, List<String> errors) {
        String[] boolConfigs = {
                "content.include.metadata", "content.include.images", "content.include.tables",
                "ocr.enable", "output.organize.by.type", "output.preserve.structure",
                "performance.parallel", "performance.optimize.memory",
                "ui.verbose", "ui.quiet", "ui.progress",
                "files.recursive", "files.batch", "files.large.file"
        };

        for (String config : boolConfigs) {
            String value = values.get(config);
            if (value != null && !value.equalsIgnoreCase("true") && !value.equalsIgnoreCase("false")) {
                errors.add("Invalid boolean: " + config + " = " + value);
            }
        }
    }

    private void validateNumericConfig(Map<String, String> values, List<String> errors) {
        for (String config : Arrays.asList("performance.threads", "performance.batch.size", "ocr.timeout", "ocr.poll.interval")) {
            String value = values.get(config);
            if (value == null) {
                continue;
            }
            try {
                int parsed = Integer.parseInt(value);
                if (parsed < 0) {
                    errors.add("Negative integer is not allowed: " + config + " = " + value);
                }
                if (("performance.batch.size".equals(config) || "ocr.timeout".equals(config) || "ocr.poll.interval".equals(config))
                        && parsed == 0) {
                    errors.add("Zero is not allowed for " + config + ": " + value);
                }
            } catch (NumberFormatException e) {
                errors.add("Invalid integer: " + config + " = " + value);
            }
        }

        String maxFileSize = values.get("performance.max.file.size");
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

    private void validateEnumConfig(Map<String, String> values, List<String> errors) {
        validateEnum(values, errors, "ocr.language", new String[]{"auto", "eng", "chi_sim", "chi_tra", "jpn", "kor", "fra", "deu"});
        validateEnum(values, errors, "format.image", new String[]{"markdown", "html", "base64"});
        validateEnum(values, errors, "format.table", new String[]{"github", "markdown", "pipe"});
        validateEnum(values, errors, "ocr.engine", new String[]{"tesseract-cli", "paddleocr", "http"});
        validateEnum(values, errors, "content.page.break.mode", new String[]{"heading", "rule", "none"});
    }

    private void validateEnum(Map<String, String> values, List<String> errors, String key, String[] validValues) {
        String value = values.get(key);
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

    private void validateSemanticConfig(Map<String, String> values, List<String> errors) {
        boolean ocrEnabled = Boolean.parseBoolean(values.getOrDefault("ocr.enable", "false"));
        String ocrEngine = values.getOrDefault("ocr.engine", "tesseract-cli");
        String ocrEndpoint = values.getOrDefault("ocr.endpoint", "");

        if (ocrEnabled && "http".equals(ocrEngine) && (ocrEndpoint == null || ocrEndpoint.isBlank())) {
            errors.add("ocr.endpoint is required when OCR is enabled and ocr.engine=http");
        }

        boolean verbose = Boolean.parseBoolean(values.getOrDefault("ui.verbose", "false"));
        boolean quiet = Boolean.parseBoolean(values.getOrDefault("ui.quiet", "false"));
        boolean progress = Boolean.parseBoolean(values.getOrDefault("ui.progress", "false"));

        if (verbose && quiet) {
            errors.add("ui.verbose and ui.quiet cannot both be true");
        }
        if (quiet && progress) {
            errors.add("ui.progress cannot be enabled when ui.quiet is true");
        }
    }

    private void validateKnownKeys(Map<String, String> flattened, List<String> errors) {
        Set<String> knownKeys = new HashSet<>();
        Set<String> knownTopLevelSections = new HashSet<>();

        for (ConfigKey key : ConfigKey.values()) {
            knownKeys.add(key.key);
            knownTopLevelSections.add(key.yamlPath.split("\\.")[0]);
        }
        knownTopLevelSections.add("providers");

        for (String rawKey : flattened.keySet()) {
            String normalizedKey = normalizeYamlKey(rawKey);
            String topLevelSection = rawKey.contains(".")
                    ? rawKey.substring(0, rawKey.indexOf('.'))
                    : rawKey;

            if (!knownTopLevelSections.contains(topLevelSection)) {
                errors.add("Unknown configuration section: " + topLevelSection);
                continue;
            }

            if (normalizedKey.startsWith("providers.")) {
                continue;
            }

            if (!knownKeys.contains(normalizedKey)) {
                errors.add("Unknown configuration key: " + rawKey);
            }
        }
    }

    public ConversionOptions createConversionOptionsFromConfig() {
        return getEffectiveConfiguration().toConversionOptions();
    }

    public EffectiveConfiguration getEffectiveConfiguration() {
        if (effectiveConfiguration != null) {
            return effectiveConfiguration;
        }

        Setting<String> tesseractPath = trackedString(ConfigKey.TESSERACT_PATH);
        Setting<String> tessdataPath = trackedString(ConfigKey.TESSDATA_PATH);

        Setting<String> outputDir = trackedString(ConfigKey.OUTPUT_DIR);
        Setting<String> imageDir = trackedString(ConfigKey.OUTPUT_IMAGE_DIR);
        Setting<String> tempDir = trackedString(ConfigKey.OUTPUT_TEMP_DIR);
        Setting<Boolean> organizeByType = trackedBoolean(ConfigKey.OUTPUT_ORGANIZE_BY_TYPE);
        Setting<Boolean> preserveStructure = trackedBoolean(ConfigKey.OUTPUT_PRESERVE_STRUCTURE);

        Setting<Boolean> includeMetadata = trackedBoolean(ConfigKey.CONTENT_INCLUDE_METADATA);
        Setting<Boolean> includeImages = trackedBoolean(ConfigKey.CONTENT_INCLUDE_IMAGES);
        Setting<Boolean> includeTables = trackedBoolean(ConfigKey.CONTENT_INCLUDE_TABLES);
        Setting<String> pageBreakMode = trackedString(ConfigKey.CONTENT_PAGE_BREAK_MODE);

        Setting<Boolean> useOcr = trackedBoolean(ConfigKey.OCR_ENABLE);
        Setting<String> ocrEngine = trackedString(ConfigKey.OCR_ENGINE);
        Setting<String> ocrLanguage = trackedString(ConfigKey.OCR_LANGUAGE);
        Setting<String> ocrEndpoint = trackedString(ConfigKey.OCR_ENDPOINT);
        Setting<String> ocrApiKey = trackedString(ConfigKey.OCR_API_KEY);
        Setting<String> ocrModel = trackedString(ConfigKey.OCR_MODEL);
        Setting<Integer> ocrTimeout = trackedInt(ConfigKey.OCR_TIMEOUT);
        Setting<Integer> ocrPollInterval = trackedInt(ConfigKey.OCR_POLL_INTERVAL);

        Setting<String> imageFormat = trackedString(ConfigKey.FORMAT_IMAGE);
        Setting<String> tableFormat = trackedString(ConfigKey.FORMAT_TABLE);

        Setting<Boolean> parallel = trackedBoolean(ConfigKey.PERFORMANCE_PARALLEL);
        Setting<Integer> threads = trackedInt(ConfigKey.PERFORMANCE_THREADS);
        Setting<Boolean> optimizeMemory = trackedBoolean(ConfigKey.PERFORMANCE_OPTIMIZE_MEMORY);
        Setting<Integer> batchSize = trackedInt(ConfigKey.PERFORMANCE_BATCH_SIZE);
        Setting<Boolean> largeFile = trackedBoolean(ConfigKey.FILES_LARGE_FILE);
        Setting<Long> maxFileSize = largeFile.value()
                ? new Setting<>(0L, largeFile.source())
                : trackedLong(ConfigKey.PERFORMANCE_MAX_FILE_SIZE);

        Setting<Boolean> verbose = trackedBoolean(ConfigKey.UI_VERBOSE);
        Setting<Boolean> quiet = trackedBoolean(ConfigKey.UI_QUIET);
        Setting<Boolean> progress = trackedBoolean(ConfigKey.UI_PROGRESS);
        Setting<Boolean> stats = trackedBoolean(ConfigKey.UI_STATS);

        Setting<Boolean> recursive = trackedBoolean(ConfigKey.FILES_RECURSIVE);
        Setting<Boolean> batch = trackedBoolean(ConfigKey.FILES_BATCH);

        effectiveConfiguration = new EffectiveConfiguration(
                new EngineSettings(tesseractPath, tessdataPath),
                new OutputSettings(outputDir, imageDir, tempDir, organizeByType, preserveStructure),
                new ContentSettings(includeMetadata, includeImages, includeTables, pageBreakMode),
                new OcrSettings(useOcr, ocrEngine, ocrLanguage, ocrEndpoint, ocrApiKey, ocrModel, ocrTimeout, ocrPollInterval),
                new FormatSettings(imageFormat, tableFormat),
                new PerformanceSettings(parallel, threads, maxFileSize, optimizeMemory, batchSize),
                new UiSettings(verbose, quiet, progress, stats),
                new FileSettings(recursive, batch, largeFile),
                getTrackedProperties()
        );
        return effectiveConfiguration;
    }

    private Setting<String> trackedString(ConfigKey key) {
        return new Setting<>(values.getOrDefault(key.key, key.defaultValue), trackedSource(key));
    }

    private Setting<Boolean> trackedBoolean(ConfigKey key) {
        String value = values.get(key.key);
        return new Setting<>(value == null ? Boolean.parseBoolean(key.defaultValue) : Boolean.parseBoolean(value), trackedSource(key));
    }

    private Setting<Integer> trackedInt(ConfigKey key) {
        return new Setting<>(readIntValue(key), trackedSource(key));
    }

    private Setting<Long> trackedLong(ConfigKey key) {
        return new Setting<>(readLongValue(key), trackedSource(key));
    }

    private ConfigSource trackedSource(ConfigKey key) {
        TrackedValue trackedValue = trackedProperties.get(key.key);
        return trackedValue == null ? ConfigSource.DEFAULT : trackedValue.source;
    }

    private int readIntValue(ConfigKey key) {
        String value = values.get(key.key);
        if (value == null) {
            return Integer.parseInt(key.defaultValue);
        }
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            logger.warn("Invalid integer for {}='{}', using default {}", key.key, value, key.defaultValue);
            return Integer.parseInt(key.defaultValue);
        }
    }

    private long readLongValue(ConfigKey key) {
        String value = values.get(key.key);
        if (value == null) {
            return Long.parseLong(key.defaultValue);
        }
        try {
            return Long.parseLong(value);
        } catch (NumberFormatException e) {
            logger.warn("Invalid long for {}='{}', using default {}", key.key, value, key.defaultValue);
            return Long.parseLong(key.defaultValue);
        }
    }

    public String getDefaultConfigFileName() {
        return PRIMARY_YAML_CONFIG_FILE;
    }

    public String getLocalConfigFileName() {
        return LOCAL_YAML_CONFIG_FILE;
    }

    public String getExampleConfigFileName() {
        return EXAMPLE_YAML_CONFIG_FILE;
    }

    public enum ConfigSource {
        DEFAULT("default"),
        PROJECT_YAML("project-yaml"),
        LOCAL_YAML("local-yaml"),
        EXPLICIT_YAML("explicit-yaml"),
        CLI("cli");

        private final String label;

        ConfigSource(String label) {
            this.label = label;
        }
    }

    public enum ConfigKey {
        TESSERACT_PATH("tesseract.path", "tesseract.path", ""),
        TESSDATA_PATH("tessdata.path", "tessdata.path", ""),
        OUTPUT_DIR("output.dir", "output.dir", "./output"),
        OUTPUT_IMAGE_DIR("output.image.dir", "output.image_dir", "assets"),
        OUTPUT_TEMP_DIR("output.temp.dir", "output.temp_dir", System.getProperty("java.io.tmpdir")),
        OUTPUT_ORGANIZE_BY_TYPE("output.organize.by.type", "output.organize_by_type", "false"),
        OUTPUT_PRESERVE_STRUCTURE("output.preserve.structure", "output.preserve_structure", "false"),
        CONTENT_INCLUDE_METADATA("content.include.metadata", "content.include_metadata", "true"),
        CONTENT_INCLUDE_IMAGES("content.include.images", "content.include_images", "true"),
        CONTENT_INCLUDE_TABLES("content.include.tables", "content.include_tables", "true"),
        CONTENT_PAGE_BREAK_MODE("content.page.break.mode", "content.page_break_mode", "heading"),
        OCR_ENABLE("ocr.enable", "ocr.enabled", "false"),
        OCR_ENGINE("ocr.engine", "ocr.engine", "tesseract-cli"),
        OCR_LANGUAGE("ocr.language", "ocr.language", "auto"),
        OCR_ENDPOINT("ocr.endpoint", "ocr.endpoint", ""),
        OCR_API_KEY("ocr.api.key", "ocr.api_key", ""),
        OCR_MODEL("ocr.model", "ocr.model", ""),
        OCR_TIMEOUT("ocr.timeout", "ocr.timeout", "30000"),
        OCR_POLL_INTERVAL("ocr.poll.interval", "ocr.poll_interval", "5000"),
        FORMAT_IMAGE("format.image", "format.image", "markdown"),
        FORMAT_TABLE("format.table", "format.table", "github"),
        PERFORMANCE_PARALLEL("performance.parallel", "performance.parallel", "false"),
        PERFORMANCE_THREADS("performance.threads", "performance.threads", "0"),
        PERFORMANCE_OPTIMIZE_MEMORY("performance.optimize.memory", "performance.optimize_memory", "false"),
        PERFORMANCE_MAX_FILE_SIZE("performance.max.file.size", "performance.max_file_size", "52428800"),
        PERFORMANCE_BATCH_SIZE("performance.batch.size", "performance.batch_size", "20"),
        UI_VERBOSE("ui.verbose", "ui.verbose", "false"),
        UI_QUIET("ui.quiet", "ui.quiet", "false"),
        UI_PROGRESS("ui.progress", "ui.progress", "false"),
        UI_STATS("ui.stats", "ui.stats", "false"),
        FILES_RECURSIVE("files.recursive", "files.recursive", "false"),
        FILES_BATCH("files.batch", "files.batch", "false"),
        FILES_LARGE_FILE("files.large.file", "files.large_file", "false");

        private final String key;
        private final String yamlPath;
        private final String defaultValue;

        ConfigKey(String key, String yamlPath, String defaultValue) {
            this.key = key;
            this.yamlPath = yamlPath;
            this.defaultValue = defaultValue;
        }
    }

    public static final class TrackedValue {
        private final String value;
        private final ConfigSource source;

        public TrackedValue(String value, ConfigSource source) {
            this.value = value;
            this.source = source;
        }

        public String getValue() {
            return value;
        }

        public ConfigSource getSource() {
            return source;
        }
    }

    public static final class Setting<T> {
        private final T value;
        private final ConfigSource source;

        public Setting(T value, ConfigSource source) {
            this.value = value;
            this.source = source;
        }

        public T value() {
            return value;
        }

        public ConfigSource source() {
            return source;
        }

        public String sourceLabel() {
            return source.label;
        }
    }

    public static final class EffectiveConfiguration {
        private final EngineSettings engine;
        private final OutputSettings output;
        private final ContentSettings content;
        private final OcrSettings ocr;
        private final FormatSettings format;
        private final PerformanceSettings performance;
        private final UiSettings ui;
        private final FileSettings files;
        private final Map<String, TrackedValue> trackedProperties;

        private EffectiveConfiguration(EngineSettings engine,
                                       OutputSettings output,
                                       ContentSettings content,
                                       OcrSettings ocr,
                                       FormatSettings format,
                                       PerformanceSettings performance,
                                       UiSettings ui,
                                       FileSettings files,
                                       Map<String, TrackedValue> trackedProperties) {
            this.engine = engine;
            this.output = output;
            this.content = content;
            this.ocr = ocr;
            this.format = format;
            this.performance = performance;
            this.ui = ui;
            this.files = files;
            this.trackedProperties = Collections.unmodifiableMap(new LinkedHashMap<>(trackedProperties));
        }

        public EngineSettings engine() {
            return engine;
        }

        public OutputSettings output() {
            return output;
        }

        public ContentSettings content() {
            return content;
        }

        public OcrSettings ocr() {
            return ocr;
        }

        public FormatSettings format() {
            return format;
        }

        public PerformanceSettings performance() {
            return performance;
        }

        public UiSettings ui() {
            return ui;
        }

        public FileSettings files() {
            return files;
        }

        public Map<String, TrackedValue> trackedProperties() {
            return trackedProperties;
        }

        public Map<String, Object> toStructuredMap() {
            Map<String, Object> root = new LinkedHashMap<>();

            putStructuredValue(root, "tesseract.path", engine.tesseractPath.value());
            putStructuredValue(root, "tessdata.path", engine.tessdataPath.value());

            putStructuredValue(root, "output.dir", output.dir.value());
            putStructuredValue(root, "output.image_dir", output.imageDir.value());
            putStructuredValue(root, "output.temp_dir", output.tempDir.value());
            putStructuredValue(root, "output.organize_by_type", output.organizeByType.value());
            putStructuredValue(root, "output.preserve_structure", output.preserveStructure.value());

            putStructuredValue(root, "content.include_metadata", content.includeMetadata.value());
            putStructuredValue(root, "content.include_images", content.includeImages.value());
            putStructuredValue(root, "content.include_tables", content.includeTables.value());
            putStructuredValue(root, "content.page_break_mode", content.pageBreakMode.value());

            putStructuredValue(root, "ocr.enabled", ocr.enabled.value());
            putStructuredValue(root, "ocr.engine", ocr.engine.value());
            putStructuredValue(root, "ocr.language", ocr.language.value());
            putStructuredValue(root, "ocr.endpoint", ocr.endpoint.value());
            putStructuredValue(root, "ocr.api_key", ocr.apiKey.value());
            putStructuredValue(root, "ocr.model", ocr.model.value());
            putStructuredValue(root, "ocr.timeout", ocr.timeout.value());
            putStructuredValue(root, "ocr.poll_interval", ocr.pollInterval.value());

            putStructuredValue(root, "format.image", format.image.value());
            putStructuredValue(root, "format.table", format.table.value());

            putStructuredValue(root, "performance.parallel", performance.parallel.value());
            putStructuredValue(root, "performance.threads", performance.threads.value());
            putStructuredValue(root, "performance.optimize_memory", performance.optimizeMemory.value());
            putStructuredValue(root, "performance.max_file_size", performance.maxFileSize.value());
            putStructuredValue(root, "performance.batch_size", performance.batchSize.value());

            putStructuredValue(root, "ui.verbose", ui.verbose.value());
            putStructuredValue(root, "ui.quiet", ui.quiet.value());
            putStructuredValue(root, "ui.progress", ui.progress.value());
            putStructuredValue(root, "ui.stats", ui.stats.value());

            putStructuredValue(root, "files.recursive", files.recursive.value());
            putStructuredValue(root, "files.batch", files.batch.value());
            putStructuredValue(root, "files.large_file", files.largeFile.value());

            return root;
        }

        @SuppressWarnings("unchecked")
        private void putStructuredValue(Map<String, Object> root, String path, Object value) {
            String[] parts = path.split("\\.");
            Map<String, Object> current = root;
            for (int i = 0; i < parts.length - 1; i++) {
                current = (Map<String, Object>) current.computeIfAbsent(parts[i], ignored -> new LinkedHashMap<>());
            }
            current.put(parts[parts.length - 1], value);
        }

        public ConversionOptions toConversionOptions() {
            ConversionOptions.Builder builder = ConversionOptions.builder()
                    .includeMetadata(content.includeMetadata.value())
                    .includeImages(content.includeImages.value())
                    .includeTables(content.includeTables.value())
                    .pageBreakMode(content.pageBreakMode.value())
                    .useOcr(ocr.enabled.value())
                    .language(ocr.language.value())
                    .ocrEngine(ocr.engine.value())
                    .ocrEndpoint(ocr.endpoint.value())
                    .ocrApiKey(ocr.apiKey.value())
                    .ocrModel(ocr.model.value())
                    .ocrTimeout(ocr.timeout.value())
                    .ocrPollInterval(ocr.pollInterval.value())
                    .imageFormat(format.image.value())
                    .tableFormat(format.table.value())
                    .imageOutputDir(output.imageDir.value())
                    .maxFileSize(performance.maxFileSize.value())
                    .tesseractPath(engine.tesseractPath.value())
                    .tessdataPath(engine.tessdataPath.value());

            String tempDirectory = output.tempDir.value();
            if (tempDirectory != null && !tempDirectory.isBlank()) {
                builder.tempDirectory(Paths.get(tempDirectory));
            }
            return builder.build();
        }
    }

    public static final class EngineSettings {
        private final Setting<String> tesseractPath;
        private final Setting<String> tessdataPath;

        private EngineSettings(Setting<String> tesseractPath, Setting<String> tessdataPath) {
            this.tesseractPath = tesseractPath;
            this.tessdataPath = tessdataPath;
        }

        public Setting<String> tesseractPath() {
            return tesseractPath;
        }

        public Setting<String> tessdataPath() {
            return tessdataPath;
        }
    }

    public static final class OutputSettings {
        private final Setting<String> dir;
        private final Setting<String> imageDir;
        private final Setting<String> tempDir;
        private final Setting<Boolean> organizeByType;
        private final Setting<Boolean> preserveStructure;

        private OutputSettings(Setting<String> dir,
                               Setting<String> imageDir,
                               Setting<String> tempDir,
                               Setting<Boolean> organizeByType,
                               Setting<Boolean> preserveStructure) {
            this.dir = dir;
            this.imageDir = imageDir;
            this.tempDir = tempDir;
            this.organizeByType = organizeByType;
            this.preserveStructure = preserveStructure;
        }

        public Setting<String> dir() {
            return dir;
        }

        public Setting<String> imageDir() {
            return imageDir;
        }

        public Setting<String> tempDir() {
            return tempDir;
        }

        public Setting<Boolean> organizeByType() {
            return organizeByType;
        }

        public Setting<Boolean> preserveStructure() {
            return preserveStructure;
        }
    }

    public static final class ContentSettings {
        private final Setting<Boolean> includeMetadata;
        private final Setting<Boolean> includeImages;
        private final Setting<Boolean> includeTables;
        private final Setting<String> pageBreakMode;

        private ContentSettings(Setting<Boolean> includeMetadata,
                                Setting<Boolean> includeImages,
                                Setting<Boolean> includeTables,
                                Setting<String> pageBreakMode) {
            this.includeMetadata = includeMetadata;
            this.includeImages = includeImages;
            this.includeTables = includeTables;
            this.pageBreakMode = pageBreakMode;
        }

        public Setting<Boolean> includeMetadata() {
            return includeMetadata;
        }

        public Setting<Boolean> includeImages() {
            return includeImages;
        }

        public Setting<Boolean> includeTables() {
            return includeTables;
        }

        public Setting<String> pageBreakMode() {
            return pageBreakMode;
        }
    }

    public static final class OcrSettings {
        private final Setting<Boolean> enabled;
        private final Setting<String> engine;
        private final Setting<String> language;
        private final Setting<String> endpoint;
        private final Setting<String> apiKey;
        private final Setting<String> model;
        private final Setting<Integer> timeout;
        private final Setting<Integer> pollInterval;

        private OcrSettings(Setting<Boolean> enabled,
                            Setting<String> engine,
                            Setting<String> language,
                            Setting<String> endpoint,
                            Setting<String> apiKey,
                            Setting<String> model,
                            Setting<Integer> timeout,
                            Setting<Integer> pollInterval) {
            this.enabled = enabled;
            this.engine = engine;
            this.language = language;
            this.endpoint = endpoint;
            this.apiKey = apiKey;
            this.model = model;
            this.timeout = timeout;
            this.pollInterval = pollInterval;
        }

        public Setting<Boolean> enabled() {
            return enabled;
        }

        public Setting<String> engine() {
            return engine;
        }

        public Setting<String> language() {
            return language;
        }

        public Setting<String> endpoint() {
            return endpoint;
        }

        public Setting<String> apiKey() {
            return apiKey;
        }

        public Setting<String> model() {
            return model;
        }

        public Setting<Integer> timeout() {
            return timeout;
        }

        public Setting<Integer> pollInterval() {
            return pollInterval;
        }
    }

    public static final class FormatSettings {
        private final Setting<String> image;
        private final Setting<String> table;

        private FormatSettings(Setting<String> image, Setting<String> table) {
            this.image = image;
            this.table = table;
        }

        public Setting<String> image() {
            return image;
        }

        public Setting<String> table() {
            return table;
        }
    }

    public static final class PerformanceSettings {
        private final Setting<Boolean> parallel;
        private final Setting<Integer> threads;
        private final Setting<Long> maxFileSize;
        private final Setting<Boolean> optimizeMemory;
        private final Setting<Integer> batchSize;

        private PerformanceSettings(Setting<Boolean> parallel,
                                    Setting<Integer> threads,
                                    Setting<Long> maxFileSize,
                                    Setting<Boolean> optimizeMemory,
                                    Setting<Integer> batchSize) {
            this.parallel = parallel;
            this.threads = threads;
            this.maxFileSize = maxFileSize;
            this.optimizeMemory = optimizeMemory;
            this.batchSize = batchSize;
        }

        public Setting<Boolean> parallel() {
            return parallel;
        }

        public Setting<Integer> threads() {
            return threads;
        }

        public Setting<Long> maxFileSize() {
            return maxFileSize;
        }

        public Setting<Boolean> optimizeMemory() {
            return optimizeMemory;
        }

        public Setting<Integer> batchSize() {
            return batchSize;
        }
    }

    public static final class UiSettings {
        private final Setting<Boolean> verbose;
        private final Setting<Boolean> quiet;
        private final Setting<Boolean> progress;
        private final Setting<Boolean> stats;

        private UiSettings(Setting<Boolean> verbose,
                           Setting<Boolean> quiet,
                           Setting<Boolean> progress,
                           Setting<Boolean> stats) {
            this.verbose = verbose;
            this.quiet = quiet;
            this.progress = progress;
            this.stats = stats;
        }

        public Setting<Boolean> verbose() {
            return verbose;
        }

        public Setting<Boolean> quiet() {
            return quiet;
        }

        public Setting<Boolean> progress() {
            return progress;
        }

        public Setting<Boolean> stats() {
            return stats;
        }
    }

    public static final class FileSettings {
        private final Setting<Boolean> recursive;
        private final Setting<Boolean> batch;
        private final Setting<Boolean> largeFile;

        private FileSettings(Setting<Boolean> recursive,
                             Setting<Boolean> batch,
                             Setting<Boolean> largeFile) {
            this.recursive = recursive;
            this.batch = batch;
            this.largeFile = largeFile;
        }

        public Setting<Boolean> recursive() {
            return recursive;
        }

        public Setting<Boolean> batch() {
            return batch;
        }

        public Setting<Boolean> largeFile() {
            return largeFile;
        }
    }

    private static final class LoadedConfiguration {
        private final Map<String, String> values;
        private final Map<String, TrackedValue> trackedProperties;

        private LoadedConfiguration(Map<String, String> values, Map<String, TrackedValue> trackedProperties) {
            this.values = values;
            this.trackedProperties = trackedProperties;
        }
    }
}
