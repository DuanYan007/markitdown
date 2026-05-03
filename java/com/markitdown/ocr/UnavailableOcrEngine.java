package com.markitdown.ocr;

import java.io.File;

/**
 * Fallback OCR engine used when OCR is disabled or unavailable.
 */
public class UnavailableOcrEngine implements OcrEngine {

    private final String reason;

    public UnavailableOcrEngine(String reason) {
        this.reason = reason == null || reason.isBlank() ? "OCR engine is unavailable" : reason;
    }

    @Override
    public String extractText(File imageFile) throws OcrException {
        throw new OcrException(reason);
    }

    @Override
    public String extractText(File imageFile, String language) throws OcrException {
        throw new OcrException(reason);
    }

    @Override
    public boolean isAvailable() {
        return false;
    }

    @Override
    public String getEngineName() {
        return "UnavailableOCR";
    }
}
