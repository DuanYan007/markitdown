package com.markitdown.ocr;

import java.io.File;

/**
 * Contract for OCR engines used by the conversion pipeline.
 */
public interface OcrEngine {

    /**
     * Extracts text from an image file.
     *
     * @param imageFile image file to process
     * @return extracted text
     * @throws OcrException when OCR fails
     */
    String extractText(File imageFile) throws OcrException;

    /**
     * Extracts text from an image file using an explicit language hint.
     *
     * @param imageFile image file to process
     * @param language language hint or OCR language code
     * @return extracted text
     * @throws OcrException when OCR fails
     */
    String extractText(File imageFile, String language) throws OcrException;

    /**
     * Reports whether the engine is currently available.
     *
     * @return {@code true} when the engine can be used
     */
    boolean isAvailable();

    /**
     * Returns the display name of the engine.
     *
     * @return engine name
     */
    String getEngineName();
}
