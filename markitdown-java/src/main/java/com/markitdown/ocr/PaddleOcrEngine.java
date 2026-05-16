package com.markitdown.ocr;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.markitdown.config.ConversionOptions;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URLConnection;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.time.Duration;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * PaddleOCR cloud job client.
 */
public class PaddleOcrEngine implements OcrEngine {

    static final String DEFAULT_JOB_URL = "https://paddleocr.aistudio-app.com/api/v2/ocr/jobs";
    static final String DEFAULT_MODEL = "PaddleOCR-VL-1.5";
    private static final String USER_AGENT = "markitdown4j-paddleocr/1.0";

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    private final String endpoint;
    private final String apiKey;
    private final String model;
    private final int timeoutMs;
    private final int pollIntervalMs;
    private final HttpClient httpClient;

    public PaddleOcrEngine(ConversionOptions options) {
        ConversionOptions.OcrOptions ocr = options.ocr();
        this.endpoint = isBlank(ocr.endpoint()) ? DEFAULT_JOB_URL : ocr.endpoint();
        this.apiKey = trimToNull(ocr.apiKey());
        this.model = isBlank(ocr.model()) ? DEFAULT_MODEL : ocr.model();
        this.timeoutMs = Math.max(ocr.timeout(), 1000);
        this.pollIntervalMs = Math.max(ocr.pollInterval(), 1000);
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofMillis(timeoutMs))
                .followRedirects(HttpClient.Redirect.NORMAL)
                .build();
    }

    @Override
    public String extractText(File imageFile) throws OcrException {
        return extractText(imageFile, "auto");
    }

    @Override
    public String extractText(File imageFile, String language) throws OcrException {
        if (!isAvailable()) {
            throw new OcrException("PaddleOCR token is not configured");
        }
        if (imageFile == null || !imageFile.exists()) {
            throw new OcrException("PaddleOCR input file does not exist");
        }

        try {
            String jobId = submitJob(imageFile, language);
            String jsonUrl = pollForResultUrl(jobId);
            String jsonl = downloadJsonl(jsonUrl);
            String markdown = extractMarkdownFromJsonl(jsonl);
            if (markdown.isBlank()) {
                throw new OcrException("PaddleOCR completed but returned no markdown text");
            }
            return markdown;
        } catch (IOException | InterruptedException e) {
            if (e instanceof InterruptedException) {
                Thread.currentThread().interrupt();
            }
            throw new OcrException("PaddleOCR request failed: " + e.getMessage(), e);
        }
    }

    @Override
    public boolean isAvailable() {
        return apiKey != null && !apiKey.isBlank();
    }

    @Override
    public String getEngineName() {
        return "PaddleOCR";
    }

    private String submitJob(File imageFile, String language) throws IOException, InterruptedException, OcrException {
        String boundary = "----MarkItDownBoundary" + UUID.randomUUID();
        Map<String, Object> optionalPayload = buildOptionalPayload(language);
        byte[] body = buildMultipartBody(boundary, imageFile, optionalPayload);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(endpoint))
                .timeout(Duration.ofMillis(timeoutMs))
                .header("Authorization", "bearer " + apiKey)
                .header("Content-Type", "multipart/form-data; boundary=" + boundary)
                .header("User-Agent", USER_AGENT)
                .header("Accept", "application/json")
                .POST(HttpRequest.BodyPublishers.ofByteArray(body))
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() < 200 || response.statusCode() >= 300) {
            throw new OcrException("PaddleOCR job submission failed with status " + response.statusCode());
        }

        Map<String, Object> responseBody = parseObject(response.body());
        String jobId = getNestedString(responseBody, "data", "jobId");
        if (isBlank(jobId)) {
            throw new OcrException("PaddleOCR response does not contain jobId");
        }
        return jobId;
    }

    private String pollForResultUrl(String jobId) throws IOException, InterruptedException, OcrException {
        String jobEndpoint = endpoint.endsWith("/") ? endpoint + jobId : endpoint + "/" + jobId;
        while (true) {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(jobEndpoint))
                    .timeout(Duration.ofMillis(timeoutMs))
                    .header("Authorization", "bearer " + apiKey)
                    .header("User-Agent", USER_AGENT)
                    .header("Accept", "application/json")
                    .GET()
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() < 200 || response.statusCode() >= 300) {
                throw new OcrException("PaddleOCR job polling failed with status " + response.statusCode());
            }

            Map<String, Object> responseBody = parseObject(response.body());
            String state = getNestedString(responseBody, "data", "state");
            if ("done".equalsIgnoreCase(state)) {
                String jsonUrl = getNestedString(responseBody, "data", "resultUrl", "jsonUrl");
                if (isBlank(jsonUrl)) {
                    throw new OcrException("PaddleOCR completed without jsonUrl");
                }
                return jsonUrl;
            }
            if ("failed".equalsIgnoreCase(state)) {
                String errorMsg = getNestedString(responseBody, "data", "errorMsg");
                throw new OcrException("PaddleOCR job failed: " + (isBlank(errorMsg) ? "unknown error" : errorMsg));
            }

            Thread.sleep(pollIntervalMs);
        }
    }

    private String downloadJsonl(String jsonUrl) throws IOException, InterruptedException, OcrException {
        URLConnection connection = URI.create(jsonUrl).toURL().openConnection();
        connection.setConnectTimeout(timeoutMs);
        connection.setReadTimeout(timeoutMs);
        connection.setRequestProperty("User-Agent", "python-requests/2.31.0");
        connection.setRequestProperty("Accept", "*/*");
        if (connection instanceof java.net.HttpURLConnection) {
            java.net.HttpURLConnection httpURLConnection = (java.net.HttpURLConnection) connection;
            httpURLConnection.setInstanceFollowRedirects(true);
            int statusCode = httpURLConnection.getResponseCode();
            if (statusCode < 200 || statusCode >= 300) {
                throw new OcrException("PaddleOCR result download failed with status " + statusCode);
            }
        }
        try (InputStream inputStream = connection.getInputStream()) {
            return new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);
        }
    }

    static String extractMarkdownFromJsonl(String jsonl) throws OcrException {
        if (jsonl == null || jsonl.isBlank()) {
            return "";
        }

        List<String> markdownBlocks = new ArrayList<>();
        String[] lines = jsonl.split("\\R");
        for (String line : lines) {
            if (line == null || line.isBlank()) {
                continue;
            }
            Map<String, Object> row = parseStaticObject(line);
            Object result = row.get("result");
            if (!(result instanceof Map<?, ?>)) {
                continue;
            }
            Map<?, ?> resultMap = (Map<?, ?>) result;
            Object layoutParsingResults = resultMap.get("layoutParsingResults");
            if (!(layoutParsingResults instanceof List<?>)) {
                continue;
            }
            List<?> items = (List<?>) layoutParsingResults;
            for (Object item : items) {
                if (!(item instanceof Map<?, ?>)) {
                    continue;
                }
                Map<?, ?> itemMap = (Map<?, ?>) item;
                Object markdown = itemMap.get("markdown");
                if (!(markdown instanceof Map<?, ?>)) {
                    continue;
                }
                Map<?, ?> markdownMap = (Map<?, ?>) markdown;
                Object text = markdownMap.get("text");
                if (text instanceof String && !((String) text).isBlank()) {
                    markdownBlocks.add(((String) text).trim());
                }
            }
        }

        return String.join("\n\n", markdownBlocks);
    }

    private byte[] buildMultipartBody(String boundary, File file, Map<String, Object> optionalPayload) throws IOException {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        writeFormField(output, boundary, "model", model);
        writeFormField(output, boundary, "optionalPayload", OBJECT_MAPPER.writeValueAsString(optionalPayload));
        writeFileField(output, boundary, "file", file);
        output.write(("--" + boundary + "--\r\n").getBytes(StandardCharsets.UTF_8));
        return output.toByteArray();
    }

    private void writeFormField(ByteArrayOutputStream output, String boundary, String name, String value) throws IOException {
        output.write(("--" + boundary + "\r\n").getBytes(StandardCharsets.UTF_8));
        output.write(("Content-Disposition: form-data; name=\"" + name + "\"\r\n\r\n").getBytes(StandardCharsets.UTF_8));
        output.write(value.getBytes(StandardCharsets.UTF_8));
        output.write("\r\n".getBytes(StandardCharsets.UTF_8));
    }

    private void writeFileField(ByteArrayOutputStream output, String boundary, String fieldName, File file) throws IOException {
        output.write(("--" + boundary + "\r\n").getBytes(StandardCharsets.UTF_8));
        output.write(("Content-Disposition: form-data; name=\"" + fieldName + "\"; filename=\"" + file.getName() + "\"\r\n")
                .getBytes(StandardCharsets.UTF_8));
        output.write(("Content-Type: " + Files.probeContentType(file.toPath()) + "\r\n\r\n").getBytes(StandardCharsets.UTF_8));
        output.write(Files.readAllBytes(file.toPath()));
        output.write("\r\n".getBytes(StandardCharsets.UTF_8));
    }

    private Map<String, Object> buildOptionalPayload(String language) {
        Map<String, Object> optionalPayload = new LinkedHashMap<>();
        optionalPayload.put("useDocOrientationClassify", false);
        optionalPayload.put("useDocUnwarping", false);
        optionalPayload.put("useChartRecognition", false);
        if (!isBlank(language) && !"auto".equalsIgnoreCase(language)) {
            optionalPayload.put("languageHint", language);
        }
        return optionalPayload;
    }

    private Map<String, Object> parseObject(String body) throws OcrException {
        return parseStaticObject(body);
    }

    private static Map<String, Object> parseStaticObject(String body) throws OcrException {
        try {
            return OBJECT_MAPPER.readValue(body, new TypeReference<Map<String, Object>>() {});
        } catch (IOException e) {
            throw new OcrException("Failed to parse PaddleOCR response: " + e.getMessage(), e);
        }
    }

    @SuppressWarnings("unchecked")
    private String getNestedString(Map<String, Object> payload, String... path) {
        Object current = payload;
        for (String part : path) {
            if (!(current instanceof Map<?, ?>)) {
                return null;
            }
            Map<String, Object> map = (Map<String, Object>) current;
            current = map.get(part);
        }
        return current instanceof String ? (String) current : null;
    }

    private static boolean isBlank(String value) {
        return value == null || value.isBlank();
    }

    private static String trimToNull(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}
