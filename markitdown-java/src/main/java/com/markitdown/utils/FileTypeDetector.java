package com.markitdown.utils;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

/**
 * Utility methods for file extension and MIME type detection.
 *
 * <p>Detection primarily relies on known extensions and falls back to light
 * content sniffing when an extension is unavailable.</p>
 *
 * @author duan yan
 * @version 2.0.0
 * @since 2.0.0
 */
public class FileTypeDetector {

    private static final Map<String, String> EXTENSION_TO_MIME_TYPE;
    private static final Set<String> KNOWN_MIME_TYPES;
    private static final Set<String> TEXT_FILE_EXTENSIONS;

    static {
        EXTENSION_TO_MIME_TYPE = new HashMap<>();

        // Text files
        EXTENSION_TO_MIME_TYPE.put("txt", "text/plain");
        EXTENSION_TO_MIME_TYPE.put("md", "text/markdown");
        EXTENSION_TO_MIME_TYPE.put("markdown", "text/markdown");
        EXTENSION_TO_MIME_TYPE.put("csv", "text/csv");

        // Microsoft Office
        EXTENSION_TO_MIME_TYPE.put("docx", "application/vnd.openxmlformats-officedocument.wordprocessingml.document");
        EXTENSION_TO_MIME_TYPE.put("pptx", "application/vnd.openxmlformats-officedocument.presentationml.presentation");
        EXTENSION_TO_MIME_TYPE.put("xlsx", "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        EXTENSION_TO_MIME_TYPE.put("doc", "application/msword");
        EXTENSION_TO_MIME_TYPE.put("ppt", "application/vnd.ms-powerpoint");
        EXTENSION_TO_MIME_TYPE.put("xls", "application/vnd.ms-excel");

        // PDF
        EXTENSION_TO_MIME_TYPE.put("pdf", "application/pdf");

        // Web formats
        EXTENSION_TO_MIME_TYPE.put("html", "text/html");
        EXTENSION_TO_MIME_TYPE.put("htm", "text/html");
        EXTENSION_TO_MIME_TYPE.put("xml", "application/xml");
        EXTENSION_TO_MIME_TYPE.put("json", "application/json");

        // Images
        EXTENSION_TO_MIME_TYPE.put("png", "image/png");
        EXTENSION_TO_MIME_TYPE.put("jpg", "image/jpeg");
        EXTENSION_TO_MIME_TYPE.put("jpeg", "image/jpeg");
        EXTENSION_TO_MIME_TYPE.put("gif", "image/gif");
        EXTENSION_TO_MIME_TYPE.put("bmp", "image/bmp");
        EXTENSION_TO_MIME_TYPE.put("tiff", "image/tiff");
        EXTENSION_TO_MIME_TYPE.put("tif", "image/tiff");
        EXTENSION_TO_MIME_TYPE.put("webp", "image/webp");

        // Archives
        EXTENSION_TO_MIME_TYPE.put("zip", "application/zip");
        EXTENSION_TO_MIME_TYPE.put("rar", "application/x-rar-compressed");
        EXTENSION_TO_MIME_TYPE.put("7z", "application/x-7z-compressed");

        // E-books
        EXTENSION_TO_MIME_TYPE.put("epub", "application/epub+zip");
        EXTENSION_TO_MIME_TYPE.put("mobi", "application/x-mobipocket-ebook");

        // Audio files
        EXTENSION_TO_MIME_TYPE.put("mp3", "audio/mpeg");
        EXTENSION_TO_MIME_TYPE.put("mp2", "audio/mpeg");
        EXTENSION_TO_MIME_TYPE.put("wav", "audio/wav");
        EXTENSION_TO_MIME_TYPE.put("ogg", "audio/ogg");
        EXTENSION_TO_MIME_TYPE.put("flac", "audio/flac");
        EXTENSION_TO_MIME_TYPE.put("m4a", "audio/mp4");
        EXTENSION_TO_MIME_TYPE.put("aac", "audio/aac");
        EXTENSION_TO_MIME_TYPE.put("wma", "audio/x-ms-wma");
        EXTENSION_TO_MIME_TYPE.put("opus", "audio/opus");
        EXTENSION_TO_MIME_TYPE.put("aiff", "audio/aiff");
        EXTENSION_TO_MIME_TYPE.put("au", "audio/basic");

        KNOWN_MIME_TYPES = new HashSet<>(EXTENSION_TO_MIME_TYPE.values());
        KNOWN_MIME_TYPES.add("application/xhtml+xml");
        KNOWN_MIME_TYPES.add("text/xml");
        KNOWN_MIME_TYPES.add("application/x-zip-compressed");

        TEXT_FILE_EXTENSIONS = new HashSet<>(Arrays.asList(
                "txt", "md", "markdown", "csv", "json", "xml", "html", "htm", "log"
        ));
    }

    /**
     * Detects the MIME type of a file from its extension or content.
     *
     * @param filePath file path to inspect
     * @return detected MIME type, or {@code application/octet-stream}
     * @throws IOException when file access fails
     */
    public static String detectMimeType(Path filePath) throws IOException {
        Objects.requireNonNull(filePath, "File path cannot be null");

        String fileName = filePath.getFileName().toString();
        String extension = getFileExtension(fileName);

        if (extension.isEmpty()) {
            return detectByContent(filePath);
        }

        String mimeType = EXTENSION_TO_MIME_TYPE.get(extension.toLowerCase());
        return mimeType != null ? mimeType : "application/octet-stream";
    }

    /**
     * Returns whether the file looks like a text file based on its extension.
     *
     * @param filePath file path to inspect
     * @return {@code true} when the extension is treated as text
     */
    public static boolean isTextFile(Path filePath) {
        Objects.requireNonNull(filePath, "File path cannot be null");

        String fileName = filePath.getFileName().toString();
        String extension = getFileExtension(fileName);

        return TEXT_FILE_EXTENSIONS.contains(extension.toLowerCase());
    }

    /**
     * Returns the file extension without the leading dot.
     *
     * @param fileName file name
     * @return extension or an empty string
     */
    public static String getFileExtension(String fileName) {
        Objects.requireNonNull(fileName, "File name cannot be null");

        int lastDotIndex = fileName.lastIndexOf('.');
        if (lastDotIndex > 0 && lastDotIndex < fileName.length() - 1) {
            return fileName.substring(lastDotIndex + 1);
        }

        return "";
    }

    /**
     * Returns the file name without the extension.
     *
     * @param fileName file name
     * @return file name without extension
     */
    public static String getFileNameWithoutExtension(String fileName) {
        Objects.requireNonNull(fileName, "File name cannot be null");

        int lastDotIndex = fileName.lastIndexOf('.');
        if (lastDotIndex > 0) {
            return fileName.substring(0, lastDotIndex);
        }

        return fileName;
    }

    /**
     * Performs lightweight content-based MIME detection when no extension exists.
     *
     * @param filePath file path to inspect
     * @return detected MIME type
     * @throws IOException when file access fails
     */
    private static String detectByContent(Path filePath) throws IOException {
        if (!Files.exists(filePath) || !Files.isRegularFile(filePath)) {
            return "application/octet-stream";
        }

        byte[] header = new byte[1024];
        try (InputStream inputStream = Files.newInputStream(filePath)) {
            int bytesRead = inputStream.read(header);
            if (bytesRead <= 0) {
                return "application/octet-stream";
            }

            String headerStr = new String(header, 0, Math.min(bytesRead, 100)).toLowerCase();

            if (headerStr.startsWith("%pdf")) {
                return "application/pdf";
            }

            if (headerStr.contains("<!doctype") || headerStr.contains("<html")) {
                return "text/html";
            }

            if (headerStr.trim().startsWith("<?xml")) {
                return "application/xml";
            }

            // JSON detection is intentionally simple and can misclassify plain text.
            String trimmed = headerStr.trim();
            if (trimmed.startsWith("{") || trimmed.startsWith("[")) {
                return "application/json";
            }

            if (isTextContent(header, bytesRead)) {
                return "text/plain";
            }

        } catch (IOException e) {
            // Fall through to the generic binary type.
        }

        return "application/octet-stream";
    }

    /**
     * Heuristically checks whether the given bytes look like text.
     *
     * @param bytes input buffer
     * @param byteCount number of valid bytes
     * @return {@code true} when the content appears textual
     */
    private static boolean isTextContent(byte[] bytes, int byteCount) {
        for (int i = 0; i < byteCount; i++) {
            byte b = bytes[i];
            if (b < 0x20 && b != '\t' && b != '\n' && b != '\r') {
                return false;
            }

            if (b > 0x7F && !isValidUtf8Continuation(bytes, i, byteCount)) {
                return false;
            }
        }
        return true;
    }

    /**
     * Validates a byte sequence against common UTF-8 continuation patterns.
     *
     * @param bytes input buffer
     * @param index current byte index
     * @param maxLength number of valid bytes
     * @return {@code true} when the sequence looks like valid UTF-8
     */
    private static boolean isValidUtf8Continuation(byte[] bytes, int index, int maxLength) {
        byte b = bytes[index];
        if ((b & 0xC0) == 0x80) {
            return true;
        } else if ((b & 0xE0) == 0xC0 && index + 1 < maxLength) {
            return (bytes[index + 1] & 0xC0) == 0x80;
        } else if ((b & 0xF0) == 0xE0 && index + 2 < maxLength) {
            return (bytes[index + 1] & 0xC0) == 0x80 && (bytes[index + 2] & 0xC0) == 0x80;
        } else if ((b & 0xF8) == 0xF0 && index + 3 < maxLength) {
            return (bytes[index + 1] & 0xC0) == 0x80
                    && (bytes[index + 2] & 0xC0) == 0x80
                    && (bytes[index + 3] & 0xC0) == 0x80;
        }
        return false;
    }

    /**
     * Returns all supported file extensions.
     *
     * @return supported extensions
     */
    public static Set<String> getSupportedExtensions() {
        return new HashSet<>(EXTENSION_TO_MIME_TYPE.keySet());
    }

    /**
     * Returns the known MIME types used by extension and registry lookups.
     *
     * @return immutable MIME type set
     */
    public static Set<String> getKnownMimeTypes() {
        return Collections.unmodifiableSet(KNOWN_MIME_TYPES);
    }

    /**
     * Returns whether an extension is explicitly supported.
     *
     * @param extension file extension
     * @return {@code true} when supported
     */
    public static boolean isSupportedExtension(String extension) {
        if (extension == null || extension.isBlank()) {
            return false;
        }
        return EXTENSION_TO_MIME_TYPE.containsKey(extension.toLowerCase());
    }
}
