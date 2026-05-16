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
        String tesseractPath = options.ocr().tesseractPath();
        String tessdataPath = options.ocr().tessdataPath();
        return new TesseractCliOcrEngine(tesseractPath, tessdataPath);
    }
}
