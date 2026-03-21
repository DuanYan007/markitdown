package com.markitdown.converter;

import com.markitdown.api.ConversionResult;
import com.markitdown.api.DocumentConverter;
import com.markitdown.config.ConversionOptions;
import com.markitdown.exception.ConversionException;
import org.apache.tika.Tika;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.parser.AutoDetectParser;
import org.apache.tika.parser.ParseContext;
import org.apache.tika.parser.Parser;
import org.apache.tika.sax.BodyContentHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.*;

import static java.util.Objects.requireNonNull;

/**
 * @class AudioConverter
 * @brief 音频文件转换器，用于将音频文件转换为 Markdown 格式
 * @details 使用 Apache Tika 库提取音频文件元数据，支持多种音频格式
 *          提供可选的 OpenAI Whisper API 语音转写功能
 *          支持流式处理（仅元数据提取）
 *          包含文件信息、音频元数据和转录文本的完整文档结构
 *
 * @author duan yan
 * @version 2.1.0
 * @since 2.0.0
 */
public class AudioConverter implements DocumentConverter {

    private static final Logger logger = LoggerFactory.getLogger(AudioConverter.class);

    /**
     * @brief 支持的音频格式集合
     */
    private static final Set<String> SUPPORTED_FORMATS = Set.of(
            "mp3", "wav", "ogg", "flac", "m4a", "aac", "opus", "wma", "aiff", "au"
    );

    /**
     * @brief OpenAI API endpoint for Whisper
     */
    private static final String OPENAI_WHISPER_ENDPOINT = "https://api.openai.com/v1/audio/transcriptions";

    /**
     * @brief 最大音频文件大小（用于转写）
     */
    private static final long MAX_TRANSCRIPTION_SIZE = 25 * 1024 * 1024; // 25MB

    /**
     * @brief OpenAI API Key（可选）
     */
    private String openaiApiKey;

    /**
     * @brief 默认构造函数
     */
    public AudioConverter() {
        // 从环境变量获取 API Key
        this.openaiApiKey = System.getenv("OPENAI_API_KEY");
    }

    /**
     * @brief 带 API Key 的构造函数
     * @param openaiApiKey OpenAI API Key，用于语音转写
     */
    public AudioConverter(String openaiApiKey) {
        this.openaiApiKey = openaiApiKey;
    }

    /**
     * @brief 设置 OpenAI API Key
     */
    public void setOpenaiApiKey(String apiKey) {
        this.openaiApiKey = apiKey;
    }

    @Override
    public ConversionResult convert(Path filePath, ConversionOptions options) throws ConversionException {
        requireNonNull(filePath, "File path cannot be null");
        requireNonNull(options, "Conversion options cannot be null");

        logger.info("Converting audio file: {}", filePath);

        try {
            // 提取音频元数据
            Map<String, Object> metadata = extractAudioMetadata(filePath, options);

            // 生成转写内容
            String transcriptionContent = generateTranscription(filePath, options);

            // 转换为 Markdown 格式
            String markdownContent = convertToMarkdown(filePath, metadata, transcriptionContent, options);

            List<String> warnings = new ArrayList<>();

            // 如果没有可用的 API Key，添加警告
            if (options.isUseOcr() && (openaiApiKey == null || openaiApiKey.isEmpty())) {
                warnings.add("OpenAI API Key not configured. Audio transcription is not available. " +
                        "Set OPENAI_API_KEY environment variable or use setApiKey() method.");
            }

            return new ConversionResult(markdownContent, metadata, warnings,
                    filePath.toFile().length(), filePath.getFileName().toString());

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
        return false; // 音频处理需要完整文件
    }

    @Override
    public int getPriority() {
        return 30;
    }

    @Override
    public String getName() {
        return "AudioConverter";
    }

    /**
     * @brief 使用 Apache Tika 提取音频元数据
     */
    private Map<String, Object> extractAudioMetadata(Path filePath, ConversionOptions options) {
        Map<String, Object> metadata = new LinkedHashMap<>();

        String fileName = filePath.getFileName().toString();
        String fileExtension = getFileExtension(fileName).toLowerCase();

        // 基本文件信息
        metadata.put("文件名", fileName);
        metadata.put("文件大小", formatFileSize(filePath.toFile().length()));
        metadata.put("格式", "audio/" + fileExtension);

        if (!options.isIncludeMetadata()) {
            return metadata;
        }

        try (InputStream stream = Files.newInputStream(filePath)) {
            // 使用适当的解析器
            Parser parser = new AutoDetectParser();

            Metadata tikaMetadata = new Metadata();
            BodyContentHandler handler = new BodyContentHandler();
            ParseContext context = new ParseContext();
            context.set(Parser.class, parser);

            parser.parse(stream, handler, tikaMetadata, context);

            // 提取音频特定元数据
            addIfNotEmpty(metadata, "标题", tikaMetadata.get("title"));
            addIfNotEmpty(metadata, "艺术家", tikaMetadata.get("xmpDM:artist"));
            addIfNotEmpty(metadata, "专辑", tikaMetadata.get("xmpDM:album"));
            addIfNotEmpty(metadata, "年份", tikaMetadata.get("xmpDM:releaseDate"));
            addIfNotEmpty(metadata, "流派", tikaMetadata.get("xmpDM:genre"));
            addIfNotEmpty(metadata, "曲目号", tikaMetadata.get("xmpDM:trackNumber"));
            addIfNotEmpty(metadata, "作曲家", tikaMetadata.get("xmpDM:composer"));
            addIfNotEmpty(metadata, "时长", formatDuration(tikaMetadata.get("xmpDM:duration")));
            addIfNotEmpty(metadata, "采样率", tikaMetadata.get("xmpDM:audioSampleRate"));
            addIfNotEmpty(metadata, "声道数", tikaMetadata.get("xmpDM:audioChannelType"));
            addIfNotEmpty(metadata, "比特率", tikaMetadata.get("xmpDM:audioCompressor"));

            // 检测 MIME 类型
            Tika tika = new Tika();
            String detectedMime = tika.detect(filePath.toFile());
            metadata.put("检测到的MIME类型", detectedMime);

            logger.debug("Successfully extracted metadata from audio file: {}", fileName);

        } catch (Exception e) {
            logger.warn("Failed to extract detailed metadata: {}", e.getMessage());
            metadata.put("元数据提取错误", e.getMessage());
        }

        metadata.put("转换时刻", LocalDateTime.now());
        return metadata;
    }

    /**
     * @brief 格式化时长（毫秒转换为 mm:ss 格式）
     */
    private String formatDuration(String durationMs) {
        if (durationMs == null || durationMs.isEmpty()) {
            return null;
        }
        try {
            double ms = Double.parseDouble(durationMs);
            long seconds = (long) (ms / 1000);
            long minutes = seconds / 60;
            seconds = seconds % 60;
            return String.format("%d:%02d", minutes, seconds);
        } catch (NumberFormatException e) {
            return durationMs;
        }
    }

    /**
     * @brief 如果值不为空则添加到 map
     */
    private void addIfNotEmpty(Map<String, Object> map, String key, String value) {
        if (value != null && !value.trim().isEmpty()) {
            map.put(key, value);
        }
    }

    /**
     * @brief 生成音频转写内容
     */
    private String generateTranscription(Path filePath, ConversionOptions options) {
        // 如果不需要转写，返回占位符
        if (!options.isUseOcr()) {
            return "*Audio transcription is disabled in conversion options.*";
        }

        // 检查 API Key 是否配置
        if (openaiApiKey == null || openaiApiKey.isEmpty()) {
            return generateTranscriptionPlaceholder(filePath);
        }

        // 检查文件大小限制
        long fileSize = filePath.toFile().length();
        if (fileSize > MAX_TRANSCRIPTION_SIZE) {
            logger.warn("Audio file too large for transcription: {} bytes (max: {} bytes)", fileSize, MAX_TRANSCRIPTION_SIZE);
            return "*Audio file is too large for transcription (max 25MB).*\n\n" +
                   generateTranscriptionPlaceholder(filePath);
        }

        // 尝试使用 Whisper API 转写
        try {
            String transcription = transcribeWithWhisper(filePath, options);
            if (transcription != null && !transcription.isEmpty()) {
                return transcription;
            }
        } catch (Exception e) {
            logger.error("Whisper transcription failed: {}", e.getMessage());
            return "*Transcription failed: " + e.getMessage() + "*\n\n" +
                   generateTranscriptionPlaceholder(filePath);
        }

        return generateTranscriptionPlaceholder(filePath);
    }

    /**
     * @brief 使用 OpenAI Whisper API 进行语音转写
     */
    private String transcribeWithWhisper(Path filePath, ConversionOptions options) throws IOException {
        logger.info("Transcribing audio file with OpenAI Whisper: {}", filePath);

        String boundary = "----WebKitFormBoundary" + UUID.randomUUID().toString().replace("-", "");
        URL url = new URL(OPENAI_WHISPER_ENDPOINT);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();

        try {
            connection.setDoOutput(true);
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Authorization", "Bearer " + openaiApiKey);
            connection.setRequestProperty("Content-Type", "multipart/form-data; boundary=" + boundary);

            // 构建多部分表单数据
            try (OutputStream os = connection.getOutputStream();
                 PrintWriter writer = new PrintWriter(new OutputStreamWriter(os, StandardCharsets.UTF_8), true)) {

                // 文件部分
                writer.append("--").append(boundary).append("\r\n");
                writer.append("Content-Disposition: form-data; name=\"file\"; filename=\"")
                      .append(filePath.getFileName().toString()).append("\"\r\n");
                writer.append("Content-Type: ").append(Files.probeContentType(filePath)).append("\r\n\r\n");
                writer.flush();

                // 写入文件内容
                Files.copy(filePath, os);
                os.flush();

                writer.append("\r\n");

                // 模型部分
                writer.append("--").append(boundary).append("\r\n");
                writer.append("Content-Disposition: form-data; name=\"model\"\r\n\r\n");
                writer.append("whisper-1\r\n");

                // 语言部分（如果指定）
                String language = options.getLanguage();
                if (language != null && !"auto".equals(language)) {
                    writer.append("--").append(boundary).append("\r\n");
                    writer.append("Content-Disposition: form-data; name=\"language\"\r\n\r\n");
                    writer.append(language).append("\r\n");
                }

                writer.append("--").append(boundary).append("--\r\n");
            }

            // 读取响应
            int responseCode = connection.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK) {
                try (BufferedReader reader = new BufferedReader(
                        new InputStreamReader(connection.getInputStream(), StandardCharsets.UTF_8))) {
                    StringBuilder response = new StringBuilder();
                    String line;
                    while ((line = reader.readLine()) != null) {
                        response.append(line);
                    }

                    // 解析 JSON 响应提取 text 字段
                    return parseTranscriptionResponse(response.toString());
                }
            } else {
                try (BufferedReader reader = new BufferedReader(
                        new InputStreamReader(connection.getErrorStream(), StandardCharsets.UTF_8))) {
                    StringBuilder errorResponse = new StringBuilder();
                    String line;
                    while ((line = reader.readLine()) != null) {
                        errorResponse.append(line);
                    }
                    throw new IOException("Whisper API error (HTTP " + responseCode + "): " + errorResponse);
                }
            }
        } finally {
            connection.disconnect();
        }
    }

    /**
     * @brief 解析 Whisper API 响应
     */
    private String parseTranscriptionResponse(String jsonResponse) {
        // 简单的 JSON 解析（提取 "text" 字段）
        // 在生产环境中应使用 Jackson 或 Gson
        try {
            int textIndex = jsonResponse.indexOf("\"text\":\"");
            if (textIndex >= 0) {
                int startIndex = textIndex + 8;
                int endIndex = jsonResponse.indexOf("\"", startIndex);
                if (endIndex > startIndex) {
                    String text = jsonResponse.substring(startIndex, endIndex);
                    // 处理转义字符
                    return text.replace("\\n", "\n")
                              .replace("\\\"", "\"")
                              .replace("\\\\", "\\");
                }
            }
        } catch (Exception e) {
            logger.warn("Failed to parse Whisper response: {}", e.getMessage());
        }
        return jsonResponse;
    }

    /**
     * @brief 生成转写占位符内容
     */
    private String generateTranscriptionPlaceholder(Path filePath) {
        StringBuilder placeholder = new StringBuilder();
        placeholder.append("*Audio transcription is not available.*\n\n");
        placeholder.append("To enable audio transcription:\n\n");
        placeholder.append("1. **Configure OpenAI API Key:**\n");
        placeholder.append("   - Set environment variable: `OPENAI_API_KEY=your-api-key`\n");
        placeholder.append("   - Or use: `new AudioConverter(apiKey)`\n\n");
        placeholder.append("2. **Alternative transcription services:**\n");
        placeholder.append("   - Google Speech-to-Text API\n");
        placeholder.append("   - AWS Transcribe\n");
        placeholder.append("   - Azure Speech Services\n");
        placeholder.append("   - Local Whisper model\n\n");
        placeholder.append("**File:** `").append(filePath.getFileName()).append("`\n");

        return placeholder.toString();
    }

    /**
     * @brief 将音频信息转换为 Markdown 格式
     */
    private String convertToMarkdown(Path filePath, Map<String, Object> metadata,
                                   String transcription, ConversionOptions options) {
        StringBuilder markdown = new StringBuilder();

        String fileName = filePath.getFileName().toString();
        String title = getFileNameWithoutExtension(fileName);

        // 添加标题
        markdown.append("# ").append(title).append("\n\n");

        // 添加音频文件信息
        markdown.append("## 音频文件信息\n\n");
        markdown.append("**文件:** `").append(fileName).append("`\n\n");

        // 添加元数据部分
        if (!metadata.isEmpty()) {
            markdown.append("## 元数据\n\n");
            for (Map.Entry<String, Object> entry : metadata.entrySet()) {
                if (entry.getValue() != null) {
                    markdown.append("- **").append(entry.getKey())
                            .append(":** ").append(entry.getValue()).append("\n");
                }
            }
            markdown.append("\n");
        }

        // 添加转写部分
        markdown.append("## 转写内容\n\n");
        markdown.append(transcription);
        markdown.append("\n");

        return markdown.toString();
    }

    /**
     * @brief 获取文件扩展名
     */
    private String getFileExtension(String fileName) {
        requireNonNull(fileName, "File name cannot be null");

        int lastDotIndex = fileName.lastIndexOf('.');
        if (lastDotIndex > 0 && lastDotIndex < fileName.length() - 1) {
            return fileName.substring(lastDotIndex + 1);
        }

        return "";
    }

    /**
     * @brief 获取不带扩展名的文件名
     */
    private String getFileNameWithoutExtension(String fileName) {
        int lastDotIndex = fileName.lastIndexOf('.');
        if (lastDotIndex > 0) {
            return fileName.substring(0, lastDotIndex);
        }
        return fileName;
    }

    /**
     * @brief 格式化文件大小
     */
    private String formatFileSize(long fileSize) {
        if (fileSize < 1024) {
            return fileSize + " B";
        } else if (fileSize < 1024 * 1024) {
            return String.format("%.1f KB", fileSize / 1024.0);
        } else if (fileSize < 1024 * 1024 * 1024) {
            return String.format("%.1f MB", fileSize / (1024.0 * 1024.0));
        } else {
            return String.format("%.1f GB", fileSize / (1024.0 * 1024.0 * 1024.0));
        }
    }

    /**
     * @brief 检查文件格式是否被支持
     */
    public static boolean isSupportedFormat(String fileExtension) {
        return fileExtension != null && SUPPORTED_FORMATS.contains(fileExtension.toLowerCase());
    }

    /**
     * @brief 获取所有支持的音频格式
     */
    public static Set<String> getSupportedFormats() {
        return new HashSet<>(SUPPORTED_FORMATS);
    }
}
