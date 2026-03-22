package com.markitdown.ocr;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;

/**
 * 模拟OCR引擎 - 用于演示和测试
 */
public class MockOcrEngine implements OcrEngine {

    private static final Logger logger = LoggerFactory.getLogger(MockOcrEngine.class);

    @Override
    public String extractText(File imageFile) throws OcrException {
        logger.info("使用模拟OCR引擎处理: {}", imageFile.getName());

        // 根据文件名模拟不同的OCR结果
        String fileName = imageFile.getName().toLowerCase();

        if (fileName.contains("chinese")) {
            return "### 中文测试文档\n\n" +
                   "这是一个模拟的OCR识别结果。\n\n" +
                   "在实际部署中,这里将显示真实的OCR识别文本。\n\n" +
                   "当前使用模拟引擎是为了让您快速看到效果。\n\n" +
                   "OCR引擎状态: 模拟模式\n" +
                   "识别准确率: 模拟100%\n" +
                   "处理速度: 即时";

        } else if (fileName.contains("text")) {
            return "### Sample OCR Result\n\n" +
                   "This is a simulated OCR recognition result.\n\n" +
                   "In actual deployment, real OCR recognized text will appear here.\n\n" +
                   "Current mock engine is for quick demonstration.\n\n" +
                   "OCR Engine Status: Mock Mode\n" +
                   "Recognition Accuracy: Simulated 100%\n" +
                   "Processing Speed: Instant";

        } else {
            return "### OCR Result\n\n" +
                   "Image file: " + imageFile.getName() + "\n\n" +
                   "This is a simulated OCR result for demonstration purposes.\n\n" +
                   "Install Tesseract for real OCR functionality:\n" +
                   "- Windows: https://github.com/UB-Mannheim/tesseract/wiki\n" +
                   "- Linux: sudo apt-get install tesseract-ocr\n" +
                   "- Mac: brew install tesseract";
        }
    }

    @Override
    public String extractText(File imageFile, String language) throws OcrException {
        String result = extractText(imageFile);
        return result + "\n\nLanguage: " + language;
    }

    @Override
    public boolean isAvailable() {
        return true; // 模拟引擎总是可用
    }

    @Override
    public String getEngineName() {
        return "MockOCR";
    }
}
