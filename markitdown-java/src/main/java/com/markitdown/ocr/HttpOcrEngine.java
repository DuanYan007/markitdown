package com.markitdown.ocr;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.markitdown.config.ConversionOptions;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.time.Duration;
import java.util.Base64;
import java.util.List;
import java.util.Map;

/**
 * HTTP-based OCR engine for local or remote OCR services.
 */
public class HttpOcrEngine implements OcrEngine {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    private final String endpoint;
    private final String apiKey;
    private final int timeoutMs;
    private final HttpClient httpClient;

    public HttpOcrEngine(ConversionOptions options) {
        ConversionOptions.OcrOptions ocr = options.ocr();
        this.endpoint = ocr.endpoint();
        this.apiKey = ocr.apiKey();
        this.timeoutMs = ocr.timeout();
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofMillis(Math.max(timeoutMs, 1000)))
                .build();
    }

    @Override
    public String extractText(File imageFile) throws OcrException {
        return extractText(imageFile, "eng+chi_sim");
    }

    @Override
    public String extractText(File imageFile, String language) throws OcrException {
        if (!isAvailable()) {
            throw new OcrException("HTTP OCR endpoint is not configured");
        }

        try {
            String payload = OBJECT_MAPPER.writeValueAsString(Map.of(
                    "imageBase64", Base64.getEncoder().encodeToString(Files.readAllBytes(imageFile.toPath())),
                    "fileName", imageFile.getName(),
                    "language", language == null || language.isBlank() ? "eng+chi_sim" : language
            ));

            HttpRequest.Builder requestBuilder = HttpRequest.newBuilder()
                    .uri(URI.create(endpoint))
                    .timeout(Duration.ofMillis(Math.max(timeoutMs, 1000)))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(payload));

            if (apiKey != null && !apiKey.isBlank()) {
                requestBuilder.header("Authorization", "Bearer " + apiKey);
            }

            HttpResponse<String> response = httpClient.send(requestBuilder.build(), HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() < 200 || response.statusCode() >= 300) {
                throw new OcrException("HTTP OCR request failed with status " + response.statusCode());
            }

            return parseText(response.body());
        } catch (IOException | InterruptedException e) {
            if (e instanceof InterruptedException) {
                Thread.currentThread().interrupt();
            }
            throw new OcrException("HTTP OCR request failed: " + e.getMessage(), e);
        }
    }

    @Override
    public boolean isAvailable() {
        return endpoint != null && !endpoint.isBlank();
    }

    @Override
    public String getEngineName() {
        return "HttpOCR";
    }

    private String parseText(String responseBody) throws OcrException {
        if (responseBody == null || responseBody.isBlank()) {
            throw new OcrException("HTTP OCR response body is empty");
        }

        try {
            Map<String, Object> response = OBJECT_MAPPER.readValue(responseBody, new TypeReference<Map<String, Object>>() {});

            Object directText = response.get("text");
            if (directText instanceof String && !((String) directText).isBlank()) {
                return (String) directText;
            }

            Object result = response.get("result");
            String nestedText = extractTextFromNested(result);
            if (nestedText != null && !nestedText.isBlank()) {
                return nestedText;
            }

            Object data = response.get("data");
            nestedText = extractTextFromNested(data);
            if (nestedText != null && !nestedText.isBlank()) {
                return nestedText;
            }

            Object message = response.get("message");
            if (message instanceof String && !((String) message).isBlank()) {
                throw new OcrException("HTTP OCR service did not return text: " + message);
            }

            throw new OcrException("HTTP OCR response does not contain a supported text field");
        } catch (IOException e) {
            throw new OcrException("Failed to parse HTTP OCR response: " + e.getMessage(), e);
        }
    }

    @SuppressWarnings("unchecked")
    private String extractTextFromNested(Object nested) {
        if (nested instanceof String) {
            return (String) nested;
        }
        if (nested instanceof Map<?, ?>) {
            Map<?, ?> nestedMap = (Map<?, ?>) nested;
            Object text = nestedMap.get("text");
            if (text instanceof String) {
                return (String) text;
            }
        }
        if (nested instanceof List<?>) {
            List<?> list = (List<?>) nested;
            StringBuilder builder = new StringBuilder();
            for (Object item : list) {
                if (item instanceof String) {
                    if (builder.length() > 0) {
                        builder.append('\n');
                    }
                    builder.append(item);
                } else if (item instanceof Map<?, ?>) {
                    Map<?, ?> map = (Map<?, ?>) item;
                    Object text = map.get("text");
                    if (text instanceof String) {
                        if (builder.length() > 0) {
                            builder.append('\n');
                        }
                        builder.append(text);
                    }
                }
            }
            return builder.length() == 0 ? null : builder.toString();
        }
        return null;
    }
}
