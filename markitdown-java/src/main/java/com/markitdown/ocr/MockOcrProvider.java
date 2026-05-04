package com.markitdown.ocr;

import com.markitdown.config.ConversionOptions;

/**
 * Provider for the mock OCR engine used in tests and demos.
 */
public class MockOcrProvider implements OcrProvider {

    @Override
    public String getName() {
        return "mock";
    }

    @Override
    public boolean isAvailable() {
        return true;
    }

    @Override
    public OcrEngine create(ConversionOptions options) {
        return new MockOcrEngine();
    }
}
