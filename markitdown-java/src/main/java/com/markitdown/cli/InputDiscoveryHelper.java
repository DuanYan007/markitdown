package com.markitdown.cli;

import java.io.IOException;
import java.net.URI;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.PathMatcher;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.stream.Stream;

/**
 * Helpers for expanding CLI input arguments into concrete local files or remote URLs.
 */
final class InputDiscoveryHelper {

    private InputDiscoveryHelper() {
    }

    static boolean isRemoteUrl(String inputFile) {
        if (inputFile == null || inputFile.isBlank()) {
            return false;
        }

        try {
            URI uri = URI.create(inputFile.trim());
            String scheme = uri.getScheme();
            return scheme != null
                    && ("http".equalsIgnoreCase(scheme) || "https".equalsIgnoreCase(scheme));
        } catch (IllegalArgumentException e) {
            return false;
        }
    }

    static List<String> collectInputFiles(
            String[] inputFiles,
            boolean recursive,
            boolean batch,
            Predicate<Path> isSupported,
            Consumer<String> warningSink,
            Consumer<String> infoSink,
            Consumer<String> errorSink
    ) {
        List<String> allFiles = new ArrayList<>();

        for (String inputFile : inputFiles) {
            if (isRemoteUrl(inputFile)) {
                allFiles.add(inputFile);
                continue;
            }

            if (inputFile.contains("*") || inputFile.contains("?")) {
                allFiles.addAll(expandWildcard(inputFile, isSupported, errorSink));
                continue;
            }

            Path path = Paths.get(inputFile);
            if (Files.isDirectory(path)) {
                if (recursive || batch) {
                    allFiles.addAll(collectFilesFromDirectory(path, recursive, isSupported, infoSink, errorSink));
                } else {
                    warningSink.accept("Warning: " + inputFile
                            + " is a directory. Use --recursive or --batch to process directories.");
                }
            } else {
                allFiles.add(inputFile);
            }
        }

        return allFiles;
    }

    static List<String> expandWildcard(String pattern, Predicate<Path> isSupported, Consumer<String> errorSink) {
        List<String> files = new ArrayList<>();
        try {
            int separatorIndex = Math.max(pattern.lastIndexOf('/'), pattern.lastIndexOf('\\'));
            Path parentPath = separatorIndex >= 0
                    ? Paths.get(pattern.substring(0, separatorIndex))
                    : Paths.get(".");
            String fileNamePattern = separatorIndex >= 0
                    ? pattern.substring(separatorIndex + 1)
                    : pattern;
            PathMatcher matcher = FileSystems.getDefault().getPathMatcher("glob:" + fileNamePattern);

            try (Stream<Path> paths = Files.list(parentPath)) {
                paths.filter(path -> matcher.matches(path.getFileName()))
                        .filter(Files::isRegularFile)
                        .filter(isSupported)
                        .forEach(path -> files.add(path.toString()));
            }
        } catch (IOException e) {
            errorSink.accept("Error expanding wildcard: " + e.getMessage());
        }
        return files;
    }

    static List<String> collectFilesFromDirectory(
            Path directory,
            boolean recursive,
            Predicate<Path> isSupported,
            Consumer<String> infoSink,
            Consumer<String> errorSink
    ) {
        List<String> files = new ArrayList<>();
        try (Stream<Path> paths = recursive ? Files.walk(directory) : Files.list(directory)) {
            paths.filter(Files::isRegularFile)
                    .filter(isSupported)
                    .forEach(path -> files.add(path.toString()));

            if (!files.isEmpty()) {
                infoSink.accept(String.format("Found %d supported file(s) in %s", files.size(), directory));
            }
        } catch (IOException e) {
            errorSink.accept("Error scanning directory " + directory + ": " + e.getMessage());
        }
        return files;
    }
}
