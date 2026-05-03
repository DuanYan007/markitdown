package com.markitdown.converter;

import com.markitdown.api.ConversionResult;
import com.markitdown.config.ConversionOptions;
import com.markitdown.core.MarkItDownEngine;
import com.markitdown.exception.ConversionException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import static org.junit.jupiter.api.Assertions.*;

class ZipConverterTest {

    @TempDir
    Path tempDir;

    @Test
    void convertsNestedTextAndJsonEntries() throws Exception {
        Path zipFile = tempDir.resolve("nested.zip");
        writeZip(zipFile);

        MarkItDownEngine engine = new MarkItDownEngine();
        ZipConverter zipConverter = new ZipConverter();
        zipConverter.setDelegate(new ZipConverter.DocumentConverterDelegate() {
            @Override
            public ConversionResult convert(java.io.InputStream inputStream, String mimeType, ConversionOptions options)
                    throws ConversionException {
                return engine.convert(inputStream, mimeType, options);
            }

            @Override
            public boolean isSupported(String mimeType) {
                return engine.isSupported(mimeType);
            }
        });

        ConversionResult result = zipConverter.convert(zipFile, ConversionOptions.builder().includeMetadata(true).build());

        assertTrue(result.isSuccessful());
        assertTrue(result.getMarkdown().contains("root file"));
        assertTrue(result.getMarkdown().contains("\"env\":\"test\""));
        assertFalse(result.getMarkdown().contains("unsupported format"));
    }

    private void writeZip(Path zipFile) throws IOException {
        try (OutputStream out = Files.newOutputStream(zipFile);
             ZipOutputStream zip = new ZipOutputStream(out, StandardCharsets.UTF_8)) {
            addEntry(zip, "root.txt", "root file");
            addEntry(zip, "level1/note.txt", "nested note");
            addEntry(zip, "level1/level2/data.json", "{\"env\":\"test\"}");
        }
    }

    private void addEntry(ZipOutputStream zip, String name, String content) throws IOException {
        zip.putNextEntry(new ZipEntry(name));
        zip.write(content.getBytes(StandardCharsets.UTF_8));
        zip.closeEntry();
    }
}
