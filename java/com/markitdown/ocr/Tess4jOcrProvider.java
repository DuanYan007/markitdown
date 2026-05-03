package com.markitdown.ocr;

import com.markitdown.config.ConversionOptions;

/**
 * Provider for the tess4j-backed OCR engine.
 */
public class Tess4jOcrProvider implements OcrProvider {

    @Override
    public String getName() {
        return "tess4j";
    }

    @Override
    public boolean isAvailable() {
        return new TesseractOcrEngine().isAvailable();
    }

    @Override
    public OcrEngine create(ConversionOptions options) {
        String tessdataPath = options.getCustomOption("tessdataPath");
        String tesseractPath = options.getCustomOption("tesseractPath");
        if (tessdataPath != null && !tessdataPath.isBlank()) {
            return new TesseractOcrEngine(tessdataPath);
        }
        return new TesseractOcrEngine(tesseractPath);
    }
}
