package com.markitdown.converters;

import com.markitdown.api.ConversionResult;
import com.markitdown.api.DocumentConverter;
import com.markitdown.config.ConversionOptions;
import com.markitdown.exceptions.ConversionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import static java.util.Objects.requireNonNull;

/**
 * Converts ZIP archives by delegating each supported entry back to the active conversion pipeline.
 */
public class ZipConverter implements DocumentConverter {

    private static final Logger logger = LoggerFactory.getLogger(ZipConverter.class);
    private static final int MAX_NESTING_DEPTH = 5;

    private DocumentConverterDelegate delegate;

    public void setDelegate(DocumentConverterDelegate delegate) {
        this.delegate = delegate;
    }

    @Override
    public ConversionResult convert(Path filePath, ConversionOptions options) throws ConversionException {
        requireNonNull(filePath, "File path cannot be null");
        requireNonNull(options, "Conversion options cannot be null");

        logger.info("Converting ZIP file: {}", filePath);

        try (FileInputStream inputStream = new FileInputStream(filePath.toFile())) {
            return convertZipStream(inputStream, filePath.getFileName().toString(), options, 0);
        } catch (IOException e) {
            String message = "Failed to process ZIP file: " + e.getMessage();
            logger.error(message, e);
            throw new ConversionException(message, e, filePath.getFileName().toString(), getName());
        }
    }

    private ConversionResult convertZipStream(InputStream inputStream, String zipName,
                                              ConversionOptions options, int depth) throws ConversionException {
        if (depth > MAX_NESTING_DEPTH) {
            throw new ConversionException("Maximum ZIP nesting depth exceeded: " + depth);
        }

        Map<String, Object> metadata = new HashMap<>();
        List<String> warnings = new ArrayList<>();
        StringBuilder markdown = new StringBuilder();
        markdown.append("# ZIP Archive: ").append(zipName).append("\n\n");

        int processedCount = 0;
        int errorCount = 0;
        long totalSize = 0;

        try (ZipInputStream zipInputStream = new ZipInputStream(inputStream, StandardCharsets.UTF_8)) {
            ZipEntry entry;
            while ((entry = zipInputStream.getNextEntry()) != null) {
                if (entry.isDirectory()) {
                    continue;
                }

                String entryName = entry.getName();
                byte[] content = readEntryContent(zipInputStream);
                totalSize += content.length;

                try {
                    String mimeType = detectMimeType(entryName, content);
                    if (mimeType != null && delegate != null && delegate.isSupported(mimeType)) {
                        markdown.append("## File: ").append(entryName).append("\n\n");

                        if ("application/zip".equals(mimeType)) {
                            ConversionResult nestedResult = convertZipStream(
                                    new ByteArrayInputStream(content),
                                    entryName,
                                    options,
                                    depth + 1
                            );
                            markdown.append(nestedResult.getMarkdown()).append("\n\n");
                        } else {
                            ConversionOptions entryOptions = new ConversionOptions(options)
                                    .setSourceFileName(entryName);
                            ConversionResult result = delegate.convert(
                                    new ByteArrayInputStream(content),
                                    mimeType,
                                    entryOptions
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
                    } else if (mimeType != null) {
                        warnings.add("Unsupported file type: " + entryName + " (" + mimeType + ")");
                    }
                } catch (Exception e) {
                    errorCount++;
                    warnings.add("Error processing " + entryName + ": " + e.getMessage());
                    logger.warn("Error processing ZIP entry: {}", entryName, e);
                }

                zipInputStream.closeEntry();
            }
        } catch (IOException e) {
            throw new ConversionException("Error reading ZIP: " + e.getMessage(), e, zipName, getName());
        }

        if (options.content().includeMetadata()) {
            metadata.put("ZIP File Name", zipName);
            metadata.put("Processed File Count", processedCount);
            metadata.put("Error Count", errorCount);
            metadata.put("Total Size", totalSize);
            metadata.put("Converted At", LocalDateTime.now());
        }

        markdown.insert(0, buildSummary(processedCount, errorCount));
        return new ConversionResult(markdown.toString(), metadata, warnings, totalSize, zipName);
    }

    private byte[] readEntryContent(ZipInputStream zipInputStream) throws IOException {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        byte[] buffer = new byte[8192];
        int len;
        while ((len = zipInputStream.read(buffer)) > 0) {
            outputStream.write(buffer, 0, len);
        }
        return outputStream.toByteArray();
    }

    private String detectMimeType(String fileName, byte[] content) {
        String extension = getFileExtension(fileName).toLowerCase();
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
            default:
                break;
        }

        if (content.length >= 4) {
            if (content[0] == 0x50 && content[1] == 0x4B) {
                return "application/zip";
            }
            if (content[0] == 0x25 && content[1] == 0x50 && content[2] == 0x44 && content[3] == 0x46) {
                return "application/pdf";
            }
        }

        return null;
    }

    private String getFileExtension(String fileName) {
        int lastDot = fileName.lastIndexOf('.');
        if (lastDot > 0 && lastDot < fileName.length() - 1) {
            return fileName.substring(lastDot + 1);
        }
        return "";
    }

    private String buildSummary(int processedCount, int errorCount) {
        StringBuilder summary = new StringBuilder();
        summary.append("> **ZIP Archive Summary**\n");
        summary.append("> - Processed files: ").append(processedCount).append("\n");
        summary.append("> - Errors: ").append(errorCount).append("\n\n");
        return summary.toString();
    }

    @Override
    public boolean supports(String mimeType) {
        return "application/zip".equals(mimeType) || "application/x-zip-compressed".equals(mimeType);
    }

    @Override
    public int getPriority() {
        return 50;
    }

    @Override
    public String getName() {
        return "ZipConverter";
    }

    @FunctionalInterface
    public interface DocumentConverterDelegate {
        ConversionResult convert(InputStream inputStream, String mimeType, ConversionOptions options)
                throws ConversionException;

        default boolean isSupported(String mimeType) {
            return true;
        }
    }
}
