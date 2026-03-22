package com.markitdown.ocr;

/**
 * OCR处理异常
 */
public class OcrException extends Exception {
    public OcrException(String message) {
        super(message);
    }

    public OcrException(String message, Throwable cause) {
        super(message, cause);
    }
}
