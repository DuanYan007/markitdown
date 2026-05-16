package com.markitdown.config;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ConfigurationManagerTest {

    @TempDir
    Path tempDir;

    @Test
    void yamlConfigurationShouldOverrideDefaultsAndLocalYamlShouldWin() throws IOException {
        Files.writeString(tempDir.resolve("markitdown.yml"),
                "ocr:\n" +
                "  enabled: true\n" +
                "  engine: paddleocr\n" +
                "  timeout: 12000\n" +
                "output:\n" +
                "  dir: ./yaml-output\n",
                StandardCharsets.UTF_8);

        Files.writeString(tempDir.resolve("markitdown.local.yml"),
                "ocr:\n" +
                "  engine: http\n" +
                "  api_key: local-secret\n",
                StandardCharsets.UTF_8);

        ConfigurationManager manager = new ConfigurationManager(tempDir);
        ConfigurationManager.EffectiveConfiguration config = manager.getEffectiveConfiguration();

        assertTrue(config.ocr().enabled().value());
        assertEquals("http", config.ocr().engine().value());
        assertEquals("local-secret", config.ocr().apiKey().value());
        assertEquals(12000, config.ocr().timeout().value());
        assertEquals("./yaml-output", config.output().dir().value());
    }

    @Test
    void yamlOnlyConfigurationShouldIgnoreNonYamlFiles() throws IOException {
        Files.writeString(tempDir.resolve("markitdown.conf"),
                "ocr.enable=true\n" +
                "ocr.engine=tesseract-cli\n" +
                "output.dir=./ignored-output\n",
                StandardCharsets.UTF_8);

        ConfigurationManager manager = new ConfigurationManager(tempDir);
        ConfigurationManager.EffectiveConfiguration config = manager.getEffectiveConfiguration();

        assertFalse(config.ocr().enabled().value());
        assertEquals("tesseract-cli", config.ocr().engine().value());
        assertEquals("./output", config.output().dir().value());
    }

    @Test
    void explicitConfigPathShouldLoadWithoutProjectDiscovery() throws IOException {
        Path explicitConfig = tempDir.resolve("custom-config.yml");
        Files.writeString(explicitConfig,
                "content:\n" +
                "  include_metadata: false\n" +
                "output:\n" +
                "  dir: ./custom-output\n" +
                "ocr:\n" +
                "  engine: paddleocr\n",
                StandardCharsets.UTF_8);

        ConfigurationManager manager = new ConfigurationManager(tempDir, explicitConfig);
        ConfigurationManager.EffectiveConfiguration config = manager.getEffectiveConfiguration();

        assertEquals("./custom-output", config.output().dir().value());
        assertEquals("paddleocr", config.ocr().engine().value());
        assertFalse(config.content().includeMetadata().value());
    }

    @Test
    void createConversionOptionsFromConfigShouldExposeTypedConfigurationFields() throws IOException {
        Files.writeString(tempDir.resolve("markitdown.yml"),
                "content:\n" +
                "  page_break_mode: rule\n" +
                "ocr:\n" +
                "  enabled: true\n" +
                "  engine: tesseract-cli\n" +
                "tesseract:\n" +
                "  path: C:/tools/tesseract.exe\n" +
                "tessdata:\n" +
                "  path: C:/tools/tessdata\n" +
                "format:\n" +
                "  table: pipe\n" +
                "performance:\n" +
                "  max_file_size: 12345\n",
                StandardCharsets.UTF_8);

        ConfigurationManager manager = new ConfigurationManager(tempDir);
        ConversionOptions options = manager.createConversionOptionsFromConfig();

        assertEquals("rule", options.content().pageBreakMode());
        assertEquals("pipe", options.format().table());
        assertEquals("tesseract-cli", options.ocr().engine());
        assertEquals("C:/tools/tesseract.exe", options.ocr().tesseractPath());
        assertEquals("C:/tools/tessdata", options.ocr().tessdataPath());
        assertEquals(12345L, options.limits().maxFileSize());
        assertNull(options.document().pdfPassword());
    }

    @Test
    void effectiveConfigurationShouldExposeTypedValuesAndSources() throws IOException {
        Path explicitConfig = tempDir.resolve("custom-config.yml");
        Files.writeString(explicitConfig,
                "output:\n" +
                "  dir: ./typed-output\n" +
                "ocr:\n" +
                "  enabled: true\n" +
                "  engine: http\n" +
                "  timeout: 45000\n" +
                "format:\n" +
                "  image: html\n",
                StandardCharsets.UTF_8);

        ConfigurationManager manager = new ConfigurationManager(tempDir, explicitConfig);
        manager.overrideFilesLargeFile(true, ConfigurationManager.ConfigSource.CLI);

        ConfigurationManager.EffectiveConfiguration config = manager.getEffectiveConfiguration();

        assertEquals("./typed-output", config.output().dir().value());
        assertEquals("explicit-yaml", config.output().dir().sourceLabel());
        assertTrue(config.ocr().enabled().value());
        assertEquals("http", config.ocr().engine().value());
        assertEquals(45000, config.ocr().timeout().value());
        assertEquals("html", config.format().image().value());
        assertEquals(0L, config.performance().maxFileSize().value());
        assertEquals("cli", config.performance().maxFileSize().sourceLabel());
    }

    @Test
    void typedOverridesShouldTrackCliSourcesWithoutStringKeys() throws IOException {
        Path explicitConfig = tempDir.resolve("custom-config.yml");
        Files.writeString(explicitConfig,
                "ocr:\n" +
                "  engine: paddleocr\n" +
                "format:\n" +
                "  image: markdown\n",
                StandardCharsets.UTF_8);

        ConfigurationManager manager = new ConfigurationManager(tempDir, explicitConfig);
        manager.overrideOcrEngine("http", ConfigurationManager.ConfigSource.CLI);
        manager.overrideImageFormat("html", ConfigurationManager.ConfigSource.CLI);

        ConfigurationManager.EffectiveConfiguration config = manager.getEffectiveConfiguration();

        assertEquals("http", config.ocr().engine().value());
        assertEquals("cli", config.ocr().engine().sourceLabel());
        assertEquals("html", config.format().image().value());
        assertEquals("cli", config.format().image().sourceLabel());
    }

    @Test
    void effectiveConfigurationShouldBeRebuiltAfterTypedOverride() throws IOException {
        Path explicitConfig = tempDir.resolve("custom-config.yml");
        Files.writeString(explicitConfig,
                "ocr:\n" +
                "  engine: paddleocr\n",
                StandardCharsets.UTF_8);

        ConfigurationManager manager = new ConfigurationManager(tempDir, explicitConfig);

        ConfigurationManager.EffectiveConfiguration before = manager.getEffectiveConfiguration();
        manager.overrideOcrEngine("http", ConfigurationManager.ConfigSource.CLI);
        ConfigurationManager.EffectiveConfiguration after = manager.getEffectiveConfiguration();

        assertEquals("paddleocr", before.ocr().engine().value());
        assertEquals("http", after.ocr().engine().value());
        assertEquals("cli", after.ocr().engine().sourceLabel());
    }

    @Test
    void propertySourceShouldTrackExplicitYamlOverridesAndDefaults() throws IOException {
        Path explicitConfig = tempDir.resolve("custom-config.yml");
        Files.writeString(explicitConfig,
                "content:\n" +
                "  include_metadata: false\n" +
                "ocr:\n" +
                "  language: zh\n",
                StandardCharsets.UTF_8);

        ConfigurationManager manager = new ConfigurationManager(tempDir, explicitConfig);

        assertEquals("explicit-yaml", manager.getPropertySource(ConfigurationManager.ConfigKey.CONTENT_INCLUDE_METADATA));
        assertEquals("explicit-yaml", manager.getPropertySource(ConfigurationManager.ConfigKey.OCR_LANGUAGE));
        assertEquals("default", manager.getPropertySource(ConfigurationManager.ConfigKey.OCR_ENGINE));
        assertEquals("default", manager.getPropertySource(ConfigurationManager.ConfigKey.OUTPUT_DIR));
    }

    @Test
    void nonYamlExplicitConfigShouldBeRejected() {
        Path explicitConfig = tempDir.resolve("custom-config.txt");

        IllegalArgumentException error = org.junit.jupiter.api.Assertions.assertThrows(
                IllegalArgumentException.class,
                () -> new ConfigurationManager(tempDir, explicitConfig)
        );

        assertTrue(error.getMessage().contains("Only YAML configuration files are supported"));
    }

    @Test
    void validateConfigurationShouldReportSemanticErrors() throws IOException {
        Path configFile = tempDir.resolve("invalid-config.yml");
        Files.writeString(configFile,
                "ocr:\n" +
                "  enabled: true\n" +
                "  engine: http\n" +
                "ui:\n" +
                "  quiet: true\n" +
                "  progress: true\n",
                StandardCharsets.UTF_8);

        ConfigurationManager manager = new ConfigurationManager(tempDir);
        var errors = manager.validateConfiguration(configFile);

        assertTrue(errors.contains("ocr.endpoint is required when OCR is enabled and ocr.engine=http"));
        assertTrue(errors.contains("ui.progress cannot be enabled when ui.quiet is true"));
    }

    @Test
    void validateConfigurationShouldReportYamlParseFailures() throws IOException {
        Path configFile = tempDir.resolve("broken-config.yml");
        Files.writeString(configFile,
                "ocr:\n" +
                "  enabled: true\n" +
                "    engine: http\n",
                StandardCharsets.UTF_8);

        ConfigurationManager manager = new ConfigurationManager(tempDir);
        var errors = manager.validateConfiguration(configFile);

        assertFalse(errors.isEmpty());
        assertTrue(errors.get(0).startsWith("Failed to parse YAML configuration:"));
    }

    @Test
    void validateConfigurationShouldReportUnknownTopLevelSectionAndUnknownKey() throws IOException {
        Path configFile = tempDir.resolve("unknown-fields.yml");
        Files.writeString(configFile,
                "content:\n" +
                "  include_metadata: true\n" +
                "  mystery_option: surprise\n" +
                "unexpected:\n" +
                "  enabled: true\n",
                StandardCharsets.UTF_8);

        ConfigurationManager manager = new ConfigurationManager(tempDir);
        var errors = manager.validateConfiguration(configFile);

        assertTrue(errors.contains("Unknown configuration key: content.mystery_option"));
        assertTrue(errors.contains("Unknown configuration section: unexpected"));
    }

    @Test
    void validateConfigurationShouldAllowProviderSpecificKeys() throws IOException {
        Path configFile = tempDir.resolve("provider-fields.yml");
        Files.writeString(configFile,
                "providers:\n" +
                "  paddleocr:\n" +
                "    job_endpoint: https://example.test/jobs\n" +
                "    result_format: markdown\n",
                StandardCharsets.UTF_8);

        ConfigurationManager manager = new ConfigurationManager(tempDir);
        var errors = manager.validateConfiguration(configFile);

        assertTrue(errors.isEmpty());
    }
}
