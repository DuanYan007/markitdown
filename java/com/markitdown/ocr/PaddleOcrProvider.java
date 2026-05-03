package com.markitdown.ocr;

import com.markitdown.config.ConversionOptions;

/**
 * Provider for PaddleOCR cloud job API.
 */
public class PaddleOcrProvider implements OcrProvider {

    @Override
    public String getName() {
        return "paddleocr";
    }

    @Override
    public boolean isAvailable() {
        return true;
    }

    @Override
    public OcrEngine create(ConversionOptions options) {
        return new PaddleOcrEngine(options);
    }
}
