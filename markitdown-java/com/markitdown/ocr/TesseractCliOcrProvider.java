package com.markitdown.ocr;

import com.markitdown.config.ConversionOptions;

/**
 * Provider for system-installed tesseract command line OCR.
 */
public class TesseractCliOcrProvider implements OcrProvider {

    @Override
    public String getName() {
        return "tesseract-cli";
    }

    @Override
    public boolean isAvailable() {
        return true;
    }

    @Override
    public OcrEngine create(ConversionOptions options) {
        String tesseractPath = options.getCustomOption("tesseractPath");
        if (tesseractPath == null || tesseractPath.isBlank()) {
            tesseractPath = options.getCustomOption("tesseract.path");
        }
        return new TesseractCliOcrEngine(tesseractPath);
    }
}
