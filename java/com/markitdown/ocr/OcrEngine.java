package com.markitdown.ocr;

import java.io.File;

/**
 * OCR引擎接口
 */
public interface OcrEngine {
    /**
     * 从图像文件中提取文本
     */
    String extractText(File imageFile) throws OcrException;

    /**
     * 从图像文件中提取文本(指定语言)
     */
    String extractText(File imageFile, String language) throws OcrException;

    /**
     * 检查引擎是否可用
     */
    boolean isAvailable();

    /**
     * 获取引擎名称
     */
    String getEngineName();
}
