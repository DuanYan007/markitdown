package com.markitdown.config;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
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
        assertTrue(manager.getBooleanProperty("ocr.enable", false));
        assertEquals("http", manager.getOcrEngine());
        assertEquals("local-secret", manager.getOcrApiKey());
        assertEquals(12000, manager.getOcrTimeout());
        assertEquals("./yaml-output", manager.getOutputDir());
    }

    @Test
    void legacyPropertiesShouldStillLoadWhenYamlIsAbsent() throws IOException {
        Files.writeString(tempDir.resolve(".markitdown.properties"),
                "ocr.enable=true\n" +
                "ocr.engine=tesseract-cli\n" +
                "output.dir=./legacy-output\n",
                StandardCharsets.UTF_8);

        ConfigurationManager manager = new ConfigurationManager(tempDir);
        assertTrue(manager.getBooleanProperty("ocr.enable", false));
        assertEquals("tesseract-cli", manager.getOcrEngine());
        assertEquals("./legacy-output", manager.getOutputDir());
    }
}
