package com.markitdown.ocr;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

/**
 * OCR engine that invokes the system tesseract command-line executable.
 */
public class TesseractCliOcrEngine implements OcrEngine {

    private final String configuredPath;

    public TesseractCliOcrEngine(String configuredPath) {
        this.configuredPath = configuredPath;
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

        List<String> command = new ArrayList<>();
        command.add(resolveExecutable());
        command.add(imageFile.getAbsolutePath());
        command.add("stdout");

        String effectiveLanguage = (language == null || language.isBlank()) ? "eng+chi_sim" : language;
        command.add("-l");
        command.add(effectiveLanguage);

        ProcessBuilder processBuilder = new ProcessBuilder(command);
        try {
            Process process = processBuilder.start();
            String stdout = readAll(process.getInputStream());
            String stderr = readAll(process.getErrorStream());
            int exitCode = process.waitFor();
            if (exitCode != 0) {
                throw new OcrException("tesseract command failed with exit code " + exitCode +
                        (stderr.isBlank() ? "" : (": " + stderr.trim())));
            }
            return stdout == null ? "" : stdout.trim();
        } catch (IOException e) {
            throw new OcrException("Failed to execute tesseract command: " + e.getMessage(), e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new OcrException("Tesseract command was interrupted", e);
        }
    }

    @Override
    public boolean isAvailable() {
        String executable = resolveExecutable();
        try {
            Process process = new ProcessBuilder(executable, "--version").start();
            int exitCode = process.waitFor();
            return exitCode == 0;
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    public String getEngineName() {
        return "TesseractCLI";
    }

    private String resolveExecutable() {
        if (configuredPath == null || configuredPath.isBlank()) {
            return "tesseract";
        }

        File configured = new File(configuredPath);
        if (configured.isFile()) {
            return configured.getAbsolutePath();
        }

        String executableName = isWindows() ? "tesseract.exe" : "tesseract";
        return new File(configured, executableName).getAbsolutePath();
    }

    private boolean isWindows() {
        String os = System.getProperty("os.name");
        return os != null && os.toLowerCase().contains("win");
    }

    private String readAll(InputStream inputStream) throws IOException {
        try (InputStream in = inputStream; ByteArrayOutputStream buffer = new ByteArrayOutputStream()) {
            byte[] chunk = new byte[4096];
            int read;
            while ((read = in.read(chunk)) != -1) {
                buffer.write(chunk, 0, read);
            }
            return buffer.toString(StandardCharsets.UTF_8);
        }
    }
}
