package com.markitdown.cli;

import com.markitdown.api.ConversionResult;
import com.markitdown.exceptions.ConversionException;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Helpers for resolving output destinations and writing Markdown results.
 */
final class OutputPathHelper {

    private OutputPathHelper() {
    }

    static Path determineDefaultOutputPath(Path inputPath, boolean remoteInput) {
        String fileName = inputPath.getFileName().toString() + ".md";
        Path parent = inputPath.getParent();
        if (remoteInput || parent == null) {
            return Paths.get("").toAbsolutePath().resolve(fileName);
        }
        return parent.resolve(fileName);
    }

    static Path determineOutputPath(Path inputPath, String outputPathStr) {
        Path outputPath = Paths.get(outputPathStr);

        if (Files.isDirectory(outputPath) || outputPathStr.endsWith("/") || outputPathStr.endsWith("\\")) {
            String fileName = inputPath.getFileName().toString();
            return outputPath.resolve(fileName + ".md");
        }

        return outputPath;
    }

    static void writeResult(ConversionResult result, Path outputPath) throws ConversionException {
        try {
            Path parentPath = outputPath.getParent();
            if (parentPath != null) {
                Files.createDirectories(parentPath);
            }

            try (BufferedWriter writer = Files.newBufferedWriter(outputPath)) {
                writer.write(result.getMarkdown());
            }
        } catch (IOException e) {
            throw new ConversionException("Failed to write output file: " + e.getMessage());
        }
    }
}
