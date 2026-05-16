package com.markitdown.ocr;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;

/**
 * Mock OCR engine used for demos and lightweight testing.
 */
public class MockOcrEngine implements OcrEngine {

    private static final Logger logger = LoggerFactory.getLogger(MockOcrEngine.class);

    @Override
    public String extractText(File imageFile) throws OcrException {
        logger.info("Processing with mock OCR engine: {}", imageFile.getName());

        String fileName = imageFile.getName().toLowerCase();

        if (fileName.contains("chinese")) {
            return "### Chinese OCR Sample\n\n" +
                    "This is a simulated OCR result for a Chinese-language input.\n\n" +
                    "In a real deployment, recognized text from the OCR engine would appear here.\n\n" +
                    "Engine Status: Mock Mode\n" +
                    "Recognition Accuracy: Simulated 100%\n" +
                    "Processing Speed: Instant";
        } else if (fileName.contains("text")) {
            return "### Sample OCR Result\n\n" +
                    "This is a simulated OCR recognition result.\n\n" +
                    "In a real deployment, recognized text from the OCR engine would appear here.\n\n" +
                    "Engine Status: Mock Mode\n" +
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
        return true;
    }

    @Override
    public String getEngineName() {
        return "MockOCR";
    }
}
