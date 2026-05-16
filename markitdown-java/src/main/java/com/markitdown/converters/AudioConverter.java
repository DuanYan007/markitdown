package com.markitdown.converters;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.markdown.engine.MarkdownBuilder;
import com.markitdown.api.ConversionResult;
import com.markitdown.api.DocumentConverter;
import com.markitdown.config.ConversionOptions;
import com.markitdown.exceptions.ConversionException;
import org.apache.tika.Tika;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.parser.AutoDetectParser;
import org.apache.tika.parser.ParseContext;
import org.apache.tika.parser.Parser;
import org.apache.tika.sax.BodyContentHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import static java.util.Objects.requireNonNull;

/**
 * Converts audio files into Markdown with extracted metadata and optional transcription.
 */
public class AudioConverter implements DocumentConverter {

    private static final Logger logger = LoggerFactory.getLogger(AudioConverter.class);
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
    private static final Set<String> SUPPORTED_FORMATS = Set.of(
            "mp3", "wav", "ogg", "flac", "m4a", "aac", "opus", "wma", "aiff", "au"
    );
    private static final String DEFAULT_TRANSCRIPTION_ENDPOINT = "https://api.openai.com/v1/audio/transcriptions";
    private static final String DEFAULT_TRANSCRIPTION_MODEL = "whisper-1";
    private static final long MAX_TRANSCRIPTION_SIZE = 25 * 1024 * 1024;

    private String openaiApiKey;

    public AudioConverter() {
    }

    public AudioConverter(String openaiApiKey) {
        this.openaiApiKey = openaiApiKey;
    }

    public void setOpenaiApiKey(String apiKey) {
        this.openaiApiKey = apiKey;
    }

    @Override
    public ConversionResult convert(Path filePath, ConversionOptions options) throws ConversionException {
        requireNonNull(filePath, "File path cannot be null");
        requireNonNull(options, "Conversion options cannot be null");

        logger.info("Converting audio file: {}", filePath);

        try {
            resolveApiKey(options);
            Map<String, Object> metadata = extractAudioMetadata(filePath, options);
            String transcriptionContent = generateTranscription(filePath, options);
            String markdownContent = convertToMarkdown(filePath, metadata, transcriptionContent, options);

            List<String> warnings = new ArrayList<>();
            if (options.ocr().enabled() && (openaiApiKey == null || openaiApiKey.isBlank())) {
                warnings.add("OpenAI API key is not configured. Audio transcription is unavailable. "
                        + "Set `ocr.api_key` in `markitdown.yml` or `markitdown.local.yml`.");
            }

            return new ConversionResult(
                    markdownContent,
                    metadata,
                    warnings,
                    filePath.toFile().length(),
                    filePath.getFileName().toString()
            );
        } catch (Exception e) {
            String errorMessage = "Failed to process audio file: " + e.getMessage();
            logger.error(errorMessage, e);
            throw new ConversionException(errorMessage, e, filePath.getFileName().toString(), getName());
        }
    }

    @Override
    public boolean supports(String mimeType) {
        return mimeType != null && mimeType.startsWith("audio/");
    }

    @Override
    public boolean supportsStreaming() {
        return false;
    }

    @Override
    public int getPriority() {
        return 30;
    }

    @Override
    public String getName() {
        return "AudioConverter";
    }

    private Map<String, Object> extractAudioMetadata(Path filePath, ConversionOptions options) {
        Map<String, Object> metadata = new LinkedHashMap<>();
        String fileName = filePath.getFileName().toString();
        String fileExtension = getFileExtension(fileName).toLowerCase();

        metadata.put("File Name", fileName);
        metadata.put("File Size", formatFileSize(filePath.toFile().length()));
        metadata.put("Format", "audio/" + fileExtension);

        if (!options.content().includeMetadata()) {
            return metadata;
        }

        try (InputStream stream = Files.newInputStream(filePath)) {
            Parser parser = new AutoDetectParser();
            Metadata tikaMetadata = new Metadata();
            BodyContentHandler handler = new BodyContentHandler();
            ParseContext context = new ParseContext();
            context.set(Parser.class, parser);
            parser.parse(stream, handler, tikaMetadata, context);

            addIfNotEmpty(metadata, "Title", tikaMetadata.get("title"));
            addIfNotEmpty(metadata, "Artist", tikaMetadata.get("xmpDM:artist"));
            addIfNotEmpty(metadata, "Album", tikaMetadata.get("xmpDM:album"));
            addIfNotEmpty(metadata, "Release Year", tikaMetadata.get("xmpDM:releaseDate"));
            addIfNotEmpty(metadata, "Genre", tikaMetadata.get("xmpDM:genre"));
            addIfNotEmpty(metadata, "Track Number", tikaMetadata.get("xmpDM:trackNumber"));
            addIfNotEmpty(metadata, "Composer", tikaMetadata.get("xmpDM:composer"));
            addIfNotEmpty(metadata, "Duration", formatDuration(tikaMetadata.get("xmpDM:duration")));
            addIfNotEmpty(metadata, "Sample Rate", tikaMetadata.get("xmpDM:audioSampleRate"));
            addIfNotEmpty(metadata, "Channel Type", tikaMetadata.get("xmpDM:audioChannelType"));
            addIfNotEmpty(metadata, "Bitrate", tikaMetadata.get("xmpDM:audioCompressor"));

            Tika tika = new Tika();
            metadata.put("Detected MIME Type", tika.detect(filePath.toFile()));
        } catch (Exception e) {
            logger.warn("Failed to extract detailed audio metadata: {}", e.getMessage());
            metadata.put("Metadata Error", e.getMessage());
        }

        metadata.put("Converted At", LocalDateTime.now());
        return metadata;
    }

    private String generateTranscription(Path filePath, ConversionOptions options) {
        if (!options.ocr().enabled()) {
            return "*Audio transcription is disabled in conversion options.*";
        }

        resolveApiKey(options);
        if (openaiApiKey == null || openaiApiKey.isBlank()) {
            return generateTranscriptionPlaceholder(filePath);
        }

        long fileSize = filePath.toFile().length();
        if (fileSize > MAX_TRANSCRIPTION_SIZE) {
            logger.warn(
                    "Audio file too large for transcription: {} bytes (max: {} bytes)",
                    fileSize,
                    MAX_TRANSCRIPTION_SIZE
            );
            return "*Audio file is too large for transcription (max 25 MB).*\n\n"
                    + generateTranscriptionPlaceholder(filePath);
        }

        try {
            String transcription = transcribe(filePath, options);
            if (transcription != null && !transcription.isBlank()) {
                return transcription.trim();
            }
        } catch (Exception e) {
            logger.error("Audio transcription failed: {}", e.getMessage());
            return "*Transcription failed: " + e.getMessage() + "*\n\n"
                    + generateTranscriptionPlaceholder(filePath);
        }

        return generateTranscriptionPlaceholder(filePath);
    }

    private String transcribe(Path filePath, ConversionOptions options) throws IOException {
        logger.info("Transcribing audio file: {}", filePath);

        ConversionOptions.OcrOptions ocr = options.ocr();
        String endpoint = resolveTranscriptionEndpoint(ocr.endpoint());
        String model = resolveTranscriptionModel(ocr.model());
        int timeoutMillis = Math.max(ocr.timeout(), 1000);
        String boundary = "----MarkItDownBoundary" + UUID.randomUUID().toString().replace("-", "");

        HttpURLConnection connection = (HttpURLConnection) new URL(endpoint).openConnection();
        try {
            connection.setDoOutput(true);
            connection.setRequestMethod("POST");
            connection.setConnectTimeout(timeoutMillis);
            connection.setReadTimeout(timeoutMillis);
            connection.setRequestProperty("Authorization", "Bearer " + openaiApiKey);
            connection.setRequestProperty("Content-Type", "multipart/form-data; boundary=" + boundary);

            try (OutputStream outputStream = connection.getOutputStream();
                 PrintWriter writer = new PrintWriter(new OutputStreamWriter(outputStream, StandardCharsets.UTF_8), true)) {

                writeFilePart(writer, outputStream, boundary, filePath);
                writeFormField(writer, boundary, "model", model);

                String language = ocr.language();
                if (language != null && !language.isBlank() && !"auto".equals(language)) {
                    writeFormField(writer, boundary, "language", language);
                }

                writer.append("--").append(boundary).append("--\r\n");
                writer.flush();
            }

            int responseCode = connection.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK) {
                try (BufferedReader reader = new BufferedReader(
                        new InputStreamReader(connection.getInputStream(), StandardCharsets.UTF_8))) {
                    StringBuilder response = new StringBuilder();
                    String line;
                    while ((line = reader.readLine()) != null) {
                        response.append(line);
                    }
                    return parseTranscriptionResponse(response.toString());
                }
            }

            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(connection.getErrorStream(), StandardCharsets.UTF_8))) {
                StringBuilder errorResponse = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    errorResponse.append(line);
                }
                throw new IOException("Transcription API error (HTTP " + responseCode + "): " + errorResponse);
            }
        } finally {
            connection.disconnect();
        }
    }

    private void writeFilePart(PrintWriter writer, OutputStream outputStream, String boundary, Path filePath)
            throws IOException {
        writer.append("--").append(boundary).append("\r\n");
        writer.append("Content-Disposition: form-data; name=\"file\"; filename=\"")
                .append(filePath.getFileName().toString())
                .append("\"\r\n");

        String contentType = Files.probeContentType(filePath);
        if (contentType == null || contentType.isBlank()) {
            contentType = "application/octet-stream";
        }

        writer.append("Content-Type: ").append(contentType).append("\r\n\r\n");
        writer.flush();

        Files.copy(filePath, outputStream);
        outputStream.flush();
        writer.append("\r\n");
        writer.flush();
    }

    private void writeFormField(PrintWriter writer, String boundary, String name, String value) {
        writer.append("--").append(boundary).append("\r\n");
        writer.append("Content-Disposition: form-data; name=\"").append(name).append("\"\r\n\r\n");
        writer.append(value).append("\r\n");
        writer.flush();
    }

    private String parseTranscriptionResponse(String jsonResponse) {
        try {
            JsonNode root = OBJECT_MAPPER.readTree(jsonResponse);
            JsonNode textNode = root.get("text");
            if (textNode != null && textNode.isTextual()) {
                return textNode.asText();
            }
        } catch (Exception e) {
            logger.warn("Failed to parse transcription response: {}", e.getMessage());
        }
        return jsonResponse;
    }

    private String resolveTranscriptionEndpoint(String configuredEndpoint) {
        if (configuredEndpoint == null || configuredEndpoint.isBlank()) {
            return DEFAULT_TRANSCRIPTION_ENDPOINT;
        }
        return configuredEndpoint;
    }

    private String resolveTranscriptionModel(String configuredModel) {
        if (configuredModel == null || configuredModel.isBlank()) {
            return DEFAULT_TRANSCRIPTION_MODEL;
        }
        return configuredModel;
    }

    private String generateTranscriptionPlaceholder(Path filePath) {
        return "*Audio transcription is not available.*\n\n"
                + "To enable audio transcription:\n\n"
                + "1. Configure `ocr.api_key` in `markitdown.yml` or `markitdown.local.yml`\n"
                + "2. Optionally configure `ocr.endpoint` and `ocr.model`\n\n"
                + "**File:** `" + filePath.getFileName() + "`\n";
    }

    private void resolveApiKey(ConversionOptions options) {
        if (openaiApiKey == null || openaiApiKey.isBlank()) {
            openaiApiKey = options.ocr().apiKey();
        }
    }

    private String convertToMarkdown(
            Path filePath,
            Map<String, Object> metadata,
            String transcription,
            ConversionOptions options
    ) {
        StringBuilder markdown = new StringBuilder();
        MarkdownBuilder builder = new MarkdownBuilder();
        String fileName = filePath.getFileName().toString();
        String title = builder.escapeMarkdown(getFileNameWithoutExtension(fileName));
        String escapedFileName = builder.escapeMarkdown(fileName);

        markdown.append("# ").append(title).append("\n\n");
        markdown.append("**File:** `").append(escapedFileName).append("`\n\n");

        if (options.content().includeMetadata() && !metadata.isEmpty()) {
            markdown.append("## Metadata\n\n");
            for (Map.Entry<String, Object> entry : metadata.entrySet()) {
                if (entry.getValue() != null) {
                    markdown.append("- **")
                            .append(entry.getKey())
                            .append(":** ")
                            .append(entry.getValue())
                            .append("\n");
                }
            }
            markdown.append("\n");
        }

        markdown.append("## Transcription\n\n");
        markdown.append(transcription).append("\n");
        return markdown.toString();
    }

    private String formatDuration(String durationMs) {
        if (durationMs == null || durationMs.isEmpty()) {
            return null;
        }

        try {
            double milliseconds = Double.parseDouble(durationMs);
            long seconds = (long) (milliseconds / 1000);
            long minutes = seconds / 60;
            seconds = seconds % 60;
            return String.format("%d:%02d", minutes, seconds);
        } catch (NumberFormatException e) {
            return durationMs;
        }
    }

    private void addIfNotEmpty(Map<String, Object> map, String key, String value) {
        if (value != null && !value.trim().isEmpty()) {
            map.put(key, value);
        }
    }

    private String getFileExtension(String fileName) {
        requireNonNull(fileName, "File name cannot be null");
        int lastDotIndex = fileName.lastIndexOf('.');
        if (lastDotIndex > 0 && lastDotIndex < fileName.length() - 1) {
            return fileName.substring(lastDotIndex + 1);
        }
        return "";
    }

    private String getFileNameWithoutExtension(String fileName) {
        requireNonNull(fileName, "File name cannot be null");
        int lastDotIndex = fileName.lastIndexOf('.');
        if (lastDotIndex > 0) {
            return fileName.substring(0, lastDotIndex);
        }
        return fileName;
    }

    private String formatFileSize(long fileSize) {
        if (fileSize < 1024) {
            return fileSize + " B";
        }
        if (fileSize < 1024 * 1024) {
            return String.format("%.1f KB", fileSize / 1024.0);
        }
        if (fileSize < 1024L * 1024L * 1024L) {
            return String.format("%.1f MB", fileSize / (1024.0 * 1024.0));
        }
        return String.format("%.1f GB", fileSize / (1024.0 * 1024.0 * 1024.0));
    }

    public static boolean isSupportedFormat(String fileExtension) {
        return fileExtension != null && SUPPORTED_FORMATS.contains(fileExtension.toLowerCase());
    }

    public static Set<String> getSupportedFormats() {
        return Set.copyOf(SUPPORTED_FORMATS);
    }
}
