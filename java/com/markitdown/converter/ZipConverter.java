package com.markitdown.converter;

import com.markitdown.api.ConversionResult;
import com.markitdown.api.DocumentConverter;
import com.markitdown.config.ConversionOptions;
import com.markitdown.exception.ConversionException;
import com.markitdown.utils.FileTypeDetector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import static java.util.Objects.requireNonNull;

/**
 * @class ZipConverter
 * @brief ZIP 压缩文件转换器，用于递归处理 ZIP 内的文件
 * @details 解压 ZIP 文件，遍历其中的所有支持格式的文件，逐个转换为 Markdown
 *          支持嵌套 ZIP 文件的递归处理
 *          保持文件结构信息，便于追踪来源
 *
 * @author duan yan
 * @version 2.1.0
 * @since 2.1.0
 */
public class ZipConverter implements DocumentConverter {

    private static final Logger logger = LoggerFactory.getLogger(ZipConverter.class);

    // 嵌套深度限制
    private static final int MAX_NESTING_DEPTH = 5;

    // 委托转换器注册表（需要外部设置）
    private DocumentConverterDelegate delegate;

    /**
     * 设置委托转换器
     */
    public void setDelegate(DocumentConverterDelegate delegate) {
        this.delegate = delegate;
    }

    @Override
    public ConversionResult convert(Path filePath, ConversionOptions options) throws ConversionException {
        requireNonNull(filePath, "File path cannot be null");
        requireNonNull(options, "Conversion options cannot be null");

        logger.info("Converting ZIP file: {}", filePath);

        try (FileInputStream fis = new FileInputStream(filePath.toFile())) {
            return convertZipStream(fis, filePath.getFileName().toString(), options, 0);
        } catch (IOException e) {
            String errorMessage = "Failed to process ZIP file: " + e.getMessage();
            logger.error(errorMessage, e);
            throw new ConversionException(errorMessage, e, filePath.getFileName().toString(), getName());
        }
    }

    /**
     * 转换 ZIP 输入流
     */
    private ConversionResult convertZipStream(InputStream inputStream, String zipName,
                                              ConversionOptions options, int depth) throws ConversionException {
        if (depth > MAX_NESTING_DEPTH) {
            throw new ConversionException("Maximum ZIP nesting depth exceeded: " + depth);
        }

        Map<String, Object> metadata = new HashMap<>();
        List<String> warnings = new ArrayList<>();
        StringBuilder markdown = new StringBuilder();

        // 添加 ZIP 文件标题
        markdown.append("# ZIP Archive: ").append(zipName).append("\n\n");

        int processedCount = 0;
        int errorCount = 0;
        long totalSize = 0;

        try (ZipInputStream zis = new ZipInputStream(inputStream, StandardCharsets.UTF_8)) {
            ZipEntry entry;
            while ((entry = zis.getNextEntry()) != null) {
                String entryName = entry.getName();

                // 跳过目录
                if (entry.isDirectory()) {
                    continue;
                }

                // 读取条目内容到内存
                byte[] content = readEntryContent(zis);
                totalSize += content.length;

                try {
                    // 检测 MIME 类型
                    String mimeType = detectMimeType(entryName, content);

                    if (mimeType != null && delegate != null && delegate.isSupported(mimeType)) {
                        // 处理支持的文件
                        markdown.append("## File: ").append(entryName).append("\n\n");

                        // 如果是嵌套 ZIP，递归处理
                        if ("application/zip".equals(mimeType)) {
                            ConversionResult nestedResult = convertZipStream(
                                    new ByteArrayInputStream(content),
                                    entryName,
                                    options,
                                    depth + 1
                            );
                            markdown.append(nestedResult.getMarkdown()).append("\n\n");
                        } else {
                            // 委托给对应的转换器
                            ConversionResult result = delegate.convert(
                                    new ByteArrayInputStream(content),
                                    mimeType,
                                    options
                            );
                            markdown.append(result.getMarkdown()).append("\n\n");

                            if (result.hasWarnings()) {
                                for (String warning : result.getWarnings()) {
                                    warnings.add(entryName + ": " + warning);
                                }
                            }
                        }

                        markdown.append("---\n\n");
                        processedCount++;
                    } else {
                        // 不支持的文件类型，记录信息
                        if (mimeType != null) {
                            warnings.add("Unsupported file type: " + entryName + " (" + mimeType + ")");
                        }
                    }
                } catch (Exception e) {
                    errorCount++;
                    warnings.add("Error processing " + entryName + ": " + e.getMessage());
                    logger.warn("Error processing ZIP entry: {}", entryName, e);
                }

                zis.closeEntry();
            }
        } catch (IOException e) {
            throw new ConversionException("Error reading ZIP: " + e.getMessage(), e, zipName, getName());
        }

        // 构建元数据
        if (options.isIncludeMetadata()) {
            metadata.put("ZIP文件名", zipName);
            metadata.put("处理文件数", processedCount);
            metadata.put("错误数", errorCount);
            metadata.put("总大小", totalSize);
            metadata.put("转换时刻", LocalDateTime.now());
        }

        // 添加摘要
        markdown.insert(0, buildSummary(metadata, processedCount, errorCount));

        return new ConversionResult(markdown.toString(), metadata, warnings, totalSize, zipName);
    }

    /**
     * 读取 ZIP 条目内容
     */
    private byte[] readEntryContent(ZipInputStream zis) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        byte[] buffer = new byte[8192];
        int len;
        while ((len = zis.read(buffer)) > 0) {
            baos.write(buffer, 0, len);
        }
        return baos.toByteArray();
    }

    /**
     * 检测 MIME 类型
     */
    private String detectMimeType(String fileName, byte[] content) {
        // 先尝试通过扩展名
        String extension = getFileExtension(fileName).toLowerCase();

        // 常见扩展名映射
        switch (extension) {
            case "pdf":
                return "application/pdf";
            case "docx":
                return "application/vnd.openxmlformats-officedocument.wordprocessingml.document";
            case "doc":
                return "application/msword";
            case "xlsx":
                return "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";
            case "xls":
                return "application/vnd.ms-excel";
            case "pptx":
                return "application/vnd.openxmlformats-officedocument.presentationml.presentation";
            case "ppt":
                return "application/vnd.ms-powerpoint";
            case "html":
            case "htm":
                return "text/html";
            case "txt":
                return "text/plain";
            case "csv":
                return "text/csv";
            case "json":
                return "application/json";
            case "xml":
                return "application/xml";
            case "md":
            case "markdown":
                return "text/markdown";
            case "png":
                return "image/png";
            case "jpg":
            case "jpeg":
                return "image/jpeg";
            case "gif":
                return "image/gif";
            case "zip":
                return "application/zip";
            case "epub":
                return "application/epub+zip";
        }

        // 尝试通过内容检测
        if (content.length >= 4) {
            // ZIP 签名
            if (content[0] == 0x50 && content[1] == 0x4B) {
                return "application/zip";
            }
            // PDF 签名
            if (content[0] == 0x25 && content[1] == 0x50 && content[2] == 0x44 && content[3] == 0x46) {
                return "application/pdf";
            }
        }

        return null;
    }

    /**
     * 获取文件扩展名
     */
    private String getFileExtension(String fileName) {
        int lastDot = fileName.lastIndexOf('.');
        if (lastDot > 0 && lastDot < fileName.length() - 1) {
            return fileName.substring(lastDot + 1);
        }
        return "";
    }

    /**
     * 构建摘要信息
     */
    private String buildSummary(Map<String, Object> metadata, int processedCount, int errorCount) {
        StringBuilder summary = new StringBuilder();
        summary.append("> **ZIP Archive Summary**\n");
        summary.append("> - Processed files: ").append(processedCount).append("\n");
        summary.append("> - Errors: ").append(errorCount).append("\n\n");
        return summary.toString();
    }

    @Override
    public boolean supports(String mimeType) {
        return "application/zip".equals(mimeType) ||
               "application/x-zip-compressed".equals(mimeType);
    }

    @Override
    public int getPriority() {
        return 50; // 较低优先级，让其他转换器优先
    }

    @Override
    public String getName() {
        return "ZipConverter";
    }

    /**
     * 委托转换器接口
     */
    @FunctionalInterface
    public interface DocumentConverterDelegate {
        ConversionResult convert(InputStream inputStream, String mimeType, ConversionOptions options)
                throws ConversionException;

        default boolean isSupported(String mimeType) {
            return true; // 默认支持所有类型
        }
    }
}
