package com.markitdown.cli;

import java.io.IOException;
import java.io.InputStream;
import java.io.PushbackInputStream;
import java.nio.charset.StandardCharsets;

/**
 * Helpers for resolving stdin-backed conversion input and lightweight MIME detection.
 */
final class PipeInputHelper {

    private PipeInputHelper() {
    }

    static ResolvedPipeInput resolve(InputStream stdin, String explicitMimeType) throws IOException {
        if (explicitMimeType != null && !explicitMimeType.isBlank()) {
            return new ResolvedPipeInput(stdin, explicitMimeType, false);
        }

        PushbackInputStream pushbackInputStream = new PushbackInputStream(stdin, 1024);
        byte[] header = new byte[1024];
        int bytesRead = pushbackInputStream.read(header);
        if (bytesRead > 0) {
            pushbackInputStream.unread(header, 0, bytesRead);
        }

        String detectedMimeType = bytesRead > 0 ? detectMimeTypeFromHeader(header, bytesRead) : null;
        return new ResolvedPipeInput(pushbackInputStream, detectedMimeType, true);
    }

    static String detectMimeTypeFromHeader(byte[] header, int length) {
        String headerStr = new String(header, 0, Math.min(length, 100), StandardCharsets.UTF_8).toLowerCase();

        if (headerStr.startsWith("%pdf")) {
            return "application/pdf";
        }

        if (length >= 4 && header[0] == 0x50 && header[1] == 0x4B) {
            return "application/vnd.openxmlformats-officedocument.wordprocessingml.document";
        }

        if (headerStr.contains("<!doctype") || headerStr.contains("<html")) {
            return "text/html";
        }

        if (headerStr.trim().startsWith("<?xml")) {
            return "application/xml";
        }

        String trimmed = headerStr.trim();
        if (trimmed.startsWith("{") || trimmed.startsWith("[")) {
            return "application/json";
        }

        if (length >= 8 && header[0] == (byte) 0x89 && header[1] == 0x50
                && header[2] == 0x4E && header[3] == 0x47) {
            return "image/png";
        }
        if (length >= 2 && header[0] == (byte) 0xFF && header[1] == (byte) 0xD8) {
            return "image/jpeg";
        }
        if (length >= 6 && header[0] == 'G' && header[1] == 'I' && header[2] == 'F') {
            return "image/gif";
        }

        if (isTextContent(header, length)) {
            return "text/plain";
        }

        return null;
    }

    private static boolean isTextContent(byte[] bytes, int length) {
        for (int i = 0; i < length; i++) {
            byte b = bytes[i];
            if (b < 0x20 && b != '\t' && b != '\n' && b != '\r') {
                return false;
            }
        }
        return true;
    }

    static final class ResolvedPipeInput {
        private final InputStream stream;
        private final String mimeType;
        private final boolean detected;

        private ResolvedPipeInput(InputStream stream, String mimeType, boolean detected) {
            this.stream = stream;
            this.mimeType = mimeType;
            this.detected = detected;
        }

        InputStream stream() {
            return stream;
        }

        String mimeType() {
            return mimeType;
        }

        boolean detected() {
            return detected;
        }
    }
}
