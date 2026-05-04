package com.markitdown.ocr;

import com.markitdown.config.ConversionOptions;

/**
 * Provider for HTTP-based OCR services.
 */
public class HttpOcrProvider implements OcrProvider {

    @Override
    public String getName() {
        return "http";
    }

    @Override
    public boolean isAvailable() {
        return true;
    }

    @Override
    public OcrEngine create(ConversionOptions options) {
        return new HttpOcrEngine(options);
    }
}
