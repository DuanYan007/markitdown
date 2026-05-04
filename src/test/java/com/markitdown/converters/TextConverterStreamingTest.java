package com.markitdown.converters;

import com.markitdown.api.ConversionResult;
import com.markitdown.config.ConversionOptions;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.*;

class TextConverterStreamingTest {

    private final TextConverter converter = new TextConverter();

    @Test
    void convertsPlainTextStream() throws Exception {
        ConversionOptions options = ConversionOptions.builder()
                .includeMetadata(true)
                .build();

        ConversionResult result = converter.convert(
                new ByteArrayInputStream("hello stream".getBytes(StandardCharsets.UTF_8)),
                "text/plain",
                options
        );

        assertTrue(result.isSuccessful());
        assertEquals("stream.txt", result.getOriginalFileName());
        assertTrue(result.getMarkdown().contains("hello stream"));
        assertEquals("plain", result.getMetadata("File Type"));
    }

    @Test
    void convertsJsonStream() throws Exception {
        ConversionOptions options = ConversionOptions.builder()
                .includeMetadata(true)
                .build();

        ConversionResult result = converter.convert(
                new ByteArrayInputStream("{\"name\":\"markitdown\"}".getBytes(StandardCharsets.UTF_8)),
                "application/json",
                options
        );

        assertTrue(result.isSuccessful());
        assertEquals("stream.json", result.getOriginalFileName());
        assertTrue(result.getMarkdown().contains("```json"));
        assertEquals("json", result.getMetadata("File Type"));
    }
}
