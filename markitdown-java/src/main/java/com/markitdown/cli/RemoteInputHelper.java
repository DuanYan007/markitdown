package com.markitdown.cli;

import com.markitdown.utils.FileTypeDetector;

import java.net.URI;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.Locale;

/**
 * Shared helpers for remote input download naming and MIME-based file extension handling.
 */
final class RemoteInputHelper {

    private RemoteInputHelper() {
    }

    static String determineRemoteFileName(URI uri, String contentDisposition, String contentType) {
        String fileName = extractFileNameFromContentDisposition(contentDisposition);
        if (fileName == null || fileName.isBlank()) {
            fileName = extractFileNameFromUri(uri);
        }
        if (fileName == null || fileName.isBlank()) {
            fileName = "remote-file";
        }

        fileName = sanitizeFileName(fileName);
        String normalizedMimeType = normalizeMimeType(contentType);
        String extension = extensionForMimeType(normalizedMimeType);
        String currentExtension = FileTypeDetector.getFileExtension(fileName);

        if (extension != null && (currentExtension.isEmpty() || !FileTypeDetector.isSupportedExtension(currentExtension))) {
            fileName = fileName + "." + extension;
        }

        return fileName;
    }

    private static String extractFileNameFromContentDisposition(String contentDisposition) {
        if (contentDisposition == null || contentDisposition.isBlank()) {
            return null;
        }

        for (String part : contentDisposition.split(";")) {
            String trimmed = part.trim();
            if (trimmed.regionMatches(true, 0, "filename*=", 0, "filename*=".length())) {
                String encodedValue = stripQuotes(trimmed.substring("filename*=".length()).trim());
                int charsetSeparator = encodedValue.indexOf("''");
                if (charsetSeparator >= 0) {
                    encodedValue = encodedValue.substring(charsetSeparator + 2);
                }
                return URLDecoder.decode(encodedValue, StandardCharsets.UTF_8);
            }
        }

        for (String part : contentDisposition.split(";")) {
            String trimmed = part.trim();
            if (trimmed.regionMatches(true, 0, "filename=", 0, "filename=".length())) {
                return stripQuotes(trimmed.substring("filename=".length()).trim());
            }
        }

        return null;
    }

    private static String extractFileNameFromUri(URI uri) {
        if (uri == null) {
            return null;
        }

        String path = uri.getPath();
        if (path == null || path.isBlank() || path.endsWith("/")) {
            return null;
        }

        int lastSlash = path.lastIndexOf('/');
        String fileName = lastSlash >= 0 ? path.substring(lastSlash + 1) : path;
        return fileName.isBlank() ? null : fileName;
    }

    private static String stripQuotes(String value) {
        if (value == null || value.length() < 2) {
            return value;
        }
        if ((value.startsWith("\"") && value.endsWith("\""))
                || (value.startsWith("'") && value.endsWith("'"))) {
            return value.substring(1, value.length() - 1);
        }
        return value;
    }

    private static String sanitizeFileName(String fileName) {
        if (fileName == null) {
            return "remote-file";
        }

        String sanitized = fileName.trim().replace('\\', '/');
        int lastSlash = sanitized.lastIndexOf('/');
        if (lastSlash >= 0) {
            sanitized = sanitized.substring(lastSlash + 1);
        }

        sanitized = sanitized.replaceAll("[<>:\"/\\\\|?*\\p{Cntrl}]", "_");
        sanitized = sanitized.replaceAll("[. ]+$", "");

        if (sanitized.isBlank() || ".".equals(sanitized) || "..".equals(sanitized)) {
            return "remote-file";
        }

        return sanitized;
    }

    private static String normalizeMimeType(String contentType) {
        if (contentType == null || contentType.isBlank()) {
            return null;
        }

        int separator = contentType.indexOf(';');
        String mimeType = separator >= 0 ? contentType.substring(0, separator) : contentType;
        mimeType = mimeType.trim().toLowerCase(Locale.ROOT);
        return mimeType.isEmpty() ? null : mimeType;
    }

    private static String extensionForMimeType(String mimeType) {
        if (mimeType == null) {
            return null;
        }

        switch (mimeType) {
            case "application/pdf":
                return "pdf";
            case "application/vnd.openxmlformats-officedocument.wordprocessingml.document":
                return "docx";
            case "application/vnd.openxmlformats-officedocument.presentationml.presentation":
                return "pptx";
            case "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet":
                return "xlsx";
            case "application/msword":
                return "doc";
            case "application/vnd.ms-powerpoint":
                return "ppt";
            case "application/vnd.ms-excel":
                return "xls";
            case "text/plain":
                return "txt";
            case "text/markdown":
                return "md";
            case "text/csv":
                return "csv";
            case "text/html":
                return "html";
            case "application/xml":
            case "text/xml":
                return "xml";
            case "application/json":
                return "json";
            case "image/png":
                return "png";
            case "image/jpeg":
                return "jpg";
            case "image/gif":
                return "gif";
            case "image/bmp":
                return "bmp";
            case "image/tiff":
                return "tiff";
            case "image/webp":
                return "webp";
            case "application/zip":
            case "application/x-zip-compressed":
                return "zip";
            case "audio/mpeg":
                return "mp3";
            case "audio/wav":
                return "wav";
            case "audio/ogg":
                return "ogg";
            case "audio/flac":
                return "flac";
            case "audio/mp4":
                return "m4a";
            case "audio/aac":
                return "aac";
            default:
                return null;
        }
    }
}
