package com.markitdown.cli;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.markitdown.config.ConfigurationManager;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

/**
 * CLI helpers for configuration file management.
 */
public class ConfigCommands {

    private static final ObjectMapper YAML_MAPPER = new ObjectMapper(new YAMLFactory());
    private static final String CLI_PREFIX = "[config]";

    public static int generateConfig(String configPath) {
        try {
            ConfigurationManager manager = new ConfigurationManager();
            Path outputPath = configPath != null
                    ? Paths.get(configPath)
                    : Paths.get(manager.getDefaultConfigFileName());

            if (Files.exists(outputPath)) {
                System.err.println(CLI_PREFIX + " Configuration file already exists: " + outputPath);
                System.err.println(CLI_PREFIX + " Remove the existing file before generating a new one.");
                return 1;
            }

            manager.generateDefaultConfig(outputPath);
            System.out.println(CLI_PREFIX + " Created configuration file: " + outputPath);
            System.out.println(CLI_PREFIX + " Update the file to match your OCR and output requirements.");
            return 0;
        } catch (IOException e) {
            System.err.println(CLI_PREFIX + " Failed to generate configuration file: " + e.getMessage());
            return 1;
        }
    }

    public static int validateConfig(String configPath) {
        try {
            ConfigurationManager manager = createConfigurationManager(configPath);
            Path path = configPath != null
                    ? Paths.get(configPath)
                    : Paths.get(resolveDefaultConfigPath(manager));

            if (!Files.exists(path)) {
                System.err.println(CLI_PREFIX + " Configuration file does not exist: " + path);
                System.err.println(CLI_PREFIX + " Next steps:");
                System.err.println("  - Check the path and try again.");
                System.err.println("  - Or generate a starter file with --generate-config.");
                return 1;
            }

            List<String> errors = manager.validateConfiguration(path);

            if (errors.isEmpty()) {
                System.out.println(CLI_PREFIX + " Configuration file is valid: " + path);
                System.out.println(CLI_PREFIX + " Summary:");
                printConfigSummary(manager);
                printValidationSuccessNextSteps();
                return 0;
            }

            System.err.println(CLI_PREFIX + " Configuration file is invalid: " + path);
            System.err.println(CLI_PREFIX + " Found " + errors.size() + " error(s):");
            for (String error : errors) {
                System.err.println("  - " + error);
            }
            printValidationFailureNextSteps();
            return 1;
        } catch (Exception e) {
            System.err.println(CLI_PREFIX + " Failed to validate configuration file: " + e.getMessage());
            return 1;
        }
    }

    public static int showConfig(String configPath) {
        return showConfig(createConfigurationManager(configPath));
    }

    public static int showConfig(ConfigurationManager manager) {
        try {
            ConfigurationManager.EffectiveConfiguration config = manager.getEffectiveConfiguration();
            System.out.println(CLI_PREFIX + " Active configuration:");
            System.out.println("--------------------------------------------------");
            System.out.println(YAML_MAPPER.writeValueAsString(config.toStructuredMap()).trim());
            System.out.println("--------------------------------------------------");
            System.out.println(CLI_PREFIX + " Summary:");
            printConfigSummary(manager);
            System.out.println();
            System.out.println(CLI_PREFIX + " Value sources:");
            printConfigSources(config);
            printShowConfigNextSteps();
            return 0;
        } catch (Exception e) {
            System.err.println(CLI_PREFIX + " Failed to read configuration: " + e.getMessage());
            return 1;
        }
    }

    private static void printConfigSummary(ConfigurationManager manager) {
        ConfigurationManager.EffectiveConfiguration config = manager.getEffectiveConfiguration();

        System.out.println("Engine paths:");
        printSetting("Tesseract", config.engine().tesseractPath());
        printSetting("Tessdata", config.engine().tessdataPath());

        System.out.println("Output settings:");
        printSetting("Output directory", config.output().dir());
        printSetting("Image directory", config.output().imageDir());
        printSetting("Temp directory", config.output().tempDir());
        printSetting("Organize by type", config.output().organizeByType());
        printSetting("Preserve structure", config.output().preserveStructure());

        System.out.println("Content settings:");
        printSetting("Include metadata", config.content().includeMetadata());
        printSetting("Include images", config.content().includeImages());
        printSetting("Include tables", config.content().includeTables());
        printSetting("Page break mode", config.content().pageBreakMode());
        printSetting("Use OCR", config.ocr().enabled());
        printSetting("OCR engine", config.ocr().engine());
        printSetting("OCR language", config.ocr().language());
        printSetting("OCR endpoint", config.ocr().endpoint());
        printSetting("OCR API key configured", booleanPresence(config.ocr().apiKey()));
        printSetting("OCR model", config.ocr().model());
        printSetting("OCR timeout", config.ocr().timeout());
        printSetting("OCR poll interval", config.ocr().pollInterval());
        printSetting("Table format", config.format().table());
        printSetting("Image format", config.format().image());

        System.out.println("Performance settings:");
        printSetting("Parallel processing", config.performance().parallel());
        printSetting("Thread count", config.performance().threads());
        printSetting("Optimize memory", config.performance().optimizeMemory());
        printSetting("Batch size", config.performance().batchSize());
        printTrackedValue("Max file size",
                formatFileSize(config.performance().maxFileSize().value()),
                config.performance().maxFileSize().sourceLabel());

        System.out.println("UI settings:");
        printSetting("Verbose", config.ui().verbose());
        printSetting("Quiet", config.ui().quiet());
        printSetting("Show progress", config.ui().progress());

        System.out.println("File handling:");
        printSetting("Recursive", config.files().recursive());
        printSetting("Batch", config.files().batch());
        printSetting("Allow large files", config.files().largeFile());
    }

    private static void printConfigSources(ConfigurationManager.EffectiveConfiguration config) {
        printSource("tesseract.path", config.engine().tesseractPath());
        printSource("tessdata.path", config.engine().tessdataPath());
        printSource("output.dir", config.output().dir());
        printSource("output.image_dir", config.output().imageDir());
        printSource("output.temp_dir", config.output().tempDir());
        printSource("content.include_metadata", config.content().includeMetadata());
        printSource("content.include_images", config.content().includeImages());
        printSource("content.include_tables", config.content().includeTables());
        printSource("content.page_break_mode", config.content().pageBreakMode());
        printSource("ocr.enabled", config.ocr().enabled());
        printSource("ocr.engine", config.ocr().engine());
        printSource("ocr.language", config.ocr().language());
        printSource("ocr.endpoint", config.ocr().endpoint());
        printSource("ocr.api_key", config.ocr().apiKey());
        printSource("ocr.model", config.ocr().model());
        printSource("ocr.timeout", config.ocr().timeout());
        printSource("ocr.poll_interval", config.ocr().pollInterval());
        printSource("format.image", config.format().image());
        printSource("format.table", config.format().table());
        printSource("performance.parallel", config.performance().parallel());
        printSource("performance.threads", config.performance().threads());
        printSource("performance.optimize_memory", config.performance().optimizeMemory());
        printSource("performance.max_file_size", config.performance().maxFileSize());
        printSource("performance.batch_size", config.performance().batchSize());
        printSource("ui.verbose", config.ui().verbose());
        printSource("ui.quiet", config.ui().quiet());
        printSource("ui.progress", config.ui().progress());
        printSource("ui.stats", config.ui().stats());
        printSource("files.recursive", config.files().recursive());
        printSource("files.batch", config.files().batch());
        printSource("files.large_file", config.files().largeFile());
    }

    private static void printSource(String key, ConfigurationManager.Setting<?> setting) {
        System.out.println("  " + key + ": [" + setting.sourceLabel() + "]");
    }

    private static void printSetting(String label, ConfigurationManager.Setting<?> setting) {
        printTrackedValue(label, String.valueOf(setting.value()), setting.sourceLabel());
    }

    private static ConfigurationManager.Setting<String> booleanPresence(ConfigurationManager.Setting<String> setting) {
        String value = setting.value();
        String displayValue = (value != null && !value.isBlank()) ? "true" : "false";
        return new ConfigurationManager.Setting<>(displayValue, setting.source());
    }

    private static void printTrackedValue(String label, String value, String sourceLabel) {
        System.out.println("  " + label + ": " + value + " [" + sourceLabel + "]");
    }

    private static void printValidationSuccessNextSteps() {
        System.out.println(CLI_PREFIX + " Next steps:");
        System.out.println("  - Run --show-config to inspect the effective configuration.");
        System.out.println("  - Run a conversion command, for example: markitdown document.pdf -o output.md");
    }

    private static void printValidationFailureNextSteps() {
        System.err.println(CLI_PREFIX + " Next steps:");
        System.err.println("  - Fix the reported configuration errors and run --validate-config again.");
        System.err.println("  - After validation passes, run --show-config to inspect effective values.");
    }

    private static void printShowConfigNextSteps() {
        System.out.println();
        System.out.println(CLI_PREFIX + " Next steps:");
        System.out.println("  - Run --validate-config to verify the configuration file before converting.");
        System.out.println("  - Run a conversion command, for example: markitdown document.pdf -o output.md");
    }

    private static String formatFileSize(long bytes) {
        if (bytes < 1024) {
            return bytes + " B";
        } else if (bytes < 1024 * 1024) {
            return String.format("%.1f KB", bytes / 1024.0);
        } else if (bytes < 1024L * 1024 * 1024) {
            return String.format("%.1f MB", bytes / (1024.0 * 1024));
        } else {
            return String.format("%.1f GB", bytes / (1024.0 * 1024 * 1024));
        }
    }

    private static String resolveDefaultConfigPath(ConfigurationManager manager) {
        Path yaml = Paths.get(manager.getDefaultConfigFileName());
        if (Files.exists(yaml)) {
            return yaml.toString();
        }

        return manager.getDefaultConfigFileName();
    }

    private static ConfigurationManager createConfigurationManager(String configPath) {
        if (configPath != null && !configPath.trim().isEmpty()) {
            return new ConfigurationManager(Paths.get(System.getProperty("user.dir")), Paths.get(configPath));
        }
        return new ConfigurationManager();
    }
}
