package com.markitdown.ocr;

import com.markitdown.config.ConversionOptions;

/**
 * Factory-style provider for OCR engines.
 */
public interface OcrProvider {

    /**
     * User-facing provider name, e.g. tess4j or mock.
     */
    String getName();

    /**
     * Whether the provider can be used in the current environment.
     */
    boolean isAvailable();

    /**
     * Creates an OCR engine instance for the current conversion.
     */
    OcrEngine create(ConversionOptions options);
}
