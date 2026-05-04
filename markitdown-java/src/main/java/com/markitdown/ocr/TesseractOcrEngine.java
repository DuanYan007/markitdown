package com.markitdown.ocr;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.lang.reflect.Method;

/**
 * Reflection-based tess4j OCR engine implementation so the core build does not
 * require tess4j on the compile classpath.
 */
public class TesseractOcrEngine implements OcrEngine {

    private static final Logger logger = LoggerFactory.getLogger(TesseractOcrEngine.class);
    private static final String TESSERACT_CLASS = "net.sourceforge.tess4j.Tesseract";

    private final String datapath;

    public TesseractOcrEngine() {
        this(null);
    }

    public TesseractOcrEngine(String datapath) {
        this.datapath = normalizeDatapath(datapath);
    }

    @Override
    public String extractText(File imageFile) throws OcrException {
        return extractText(imageFile, "eng+chi_sim");
    }

    @Override
    public String extractText(File imageFile, String language) throws OcrException {
        if (!imageFile.exists()) {
            throw new OcrException("Image file does not exist: " + imageFile.getAbsolutePath());
        }
        if (!imageFile.isFile()) {
            throw new OcrException("Path is not a file: " + imageFile.getAbsolutePath());
        }
        if (!isAvailable()) {
            throw new OcrException("tess4j is not available in the current build or runtime");
        }

        try {
            Object tesseract = newTesseractInstance();
            invokeIfPresent(tesseract, "setDatapath", new Class<?>[]{String.class}, new Object[]{datapath});
            invokeIfPresent(tesseract, "setLanguage", new Class<?>[]{String.class},
                    new Object[]{(language == null || language.isBlank()) ? "eng+chi_sim" : language});

            long startTime = System.currentTimeMillis();
            String text = (String) tesseract.getClass()
                    .getMethod("doOCR", File.class)
                    .invoke(tesseract, imageFile);
            long duration = System.currentTimeMillis() - startTime;

            logger.info("OCR complete: {} ({} ms, {} chars)", imageFile.getName(), duration, text.length());
            return text;
        } catch (Exception e) {
            String message = e.getCause() != null ? e.getCause().getMessage() : e.getMessage();
            throw new OcrException("tess4j OCR failed: " + message, e);
        }
    }

    @Override
    public boolean isAvailable() {
        try {
            Class.forName(TESSERACT_CLASS);
            return true;
        } catch (ClassNotFoundException e) {
            return false;
        }
    }

    @Override
    public String getEngineName() {
        return "Tess4J";
    }

    private Object newTesseractInstance() throws Exception {
        Class<?> clazz = Class.forName(TESSERACT_CLASS);
        return clazz.getDeclaredConstructor().newInstance();
    }

    private void invokeIfPresent(Object target, String methodName, Class<?>[] parameterTypes, Object[] args)
            throws Exception {
        if (target == null) {
            return;
        }
        if (args != null && args.length == 1 && args[0] == null) {
            return;
        }
        Method method = target.getClass().getMethod(methodName, parameterTypes);
        method.invoke(target, args);
    }

    private String normalizeDatapath(String configuredPath) {
        if (configuredPath != null && !configuredPath.isBlank()) {
            File configured = new File(configuredPath);
            File tessdataDir = new File(configured, "tessdata");
            return tessdataDir.exists() ? tessdataDir.getAbsolutePath() : configured.getAbsolutePath();
        }

        String[] possiblePaths = {
                "O:\\tesserOCR",
                "C:\\Program Files\\Tesseract-OCR",
                "C:\\Program Files (x86)\\Tesseract-OCR",
                "/usr/local/share",
                "/usr/share",
                "/opt/homebrew/share"
        };

        for (String path : possiblePaths) {
            File base = new File(path);
            File tessdataDir = new File(base, "tessdata");
            if (tessdataDir.exists()) {
                logger.debug("Detected tessdata path: {}", tessdataDir.getAbsolutePath());
                return tessdataDir.getAbsolutePath();
            }
        }
        return null;
    }
}
