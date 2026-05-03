package com.markitdown.ocr;

import com.markitdown.config.ConversionOptions;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class OcrEngineFactoryTest {

    @Test
    void returnsUnavailableEngineWhenOcrIsDisabled() {
        ConversionOptions options = ConversionOptions.builder()
                .useOcr(false)
                .build();

        OcrEngine engine = OcrEngineFactory.create(options);

        assertFalse(engine.isAvailable());
        assertEquals("UnavailableOCR", engine.getEngineName());
    }

    @Test
    void returnsUnavailableEngineForUnknownProvider() {
        ConversionOptions options = ConversionOptions.builder()
                .useOcr(true)
                .ocrEngine("does-not-exist")
                .build();

        OcrEngine engine = OcrEngineFactory.create(options);

        assertFalse(engine.isAvailable());
        OcrException error = assertThrows(OcrException.class, () -> engine.extractText(null));
        assertTrue(error.getMessage().contains("Unsupported OCR engine"));
    }

    @Test
    void returnsMockEngineWhenMockProviderIsSelected() throws Exception {
        ConversionOptions options = ConversionOptions.builder()
                .useOcr(true)
                .ocrEngine("mock")
                .build();

        OcrEngine engine = OcrEngineFactory.create(options);

        assertTrue(engine.isAvailable());
        assertEquals("MockOCR", engine.getEngineName());
        java.io.File tempFile = java.io.File.createTempFile("mock-ocr-text", ".png");
        tempFile.deleteOnExit();
        assertTrue(engine.extractText(tempFile).contains("Sample OCR Result"));
    }
}
