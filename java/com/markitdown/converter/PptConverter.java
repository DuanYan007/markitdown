package com.markitdown.converter;

import com.markitdown.api.ConversionResult;
import com.markitdown.api.DocumentConverter;
import com.markitdown.config.ConversionOptions;
import com.markitdown.exception.ConversionException;
import org.apache.poi.hslf.usermodel.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.Dimension;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.*;

import static java.util.Objects.requireNonNull;

/**
 * @class PptConverter
 * @brief PowerPoint 97-2003 演示文稿转换器
 */
public class PptConverter implements DocumentConverter {

    private static final Logger logger = LoggerFactory.getLogger(PptConverter.class);

    @Override
    public ConversionResult convert(Path filePath, ConversionOptions options) throws ConversionException {
        requireNonNull(filePath, "File path cannot be null");
        requireNonNull(options, "Conversion options cannot be null");

        logger.info("Converting PPT file: {}", filePath);

        try (FileInputStream fis = new FileInputStream(filePath.toFile());
             HSLFSlideShow ppt = new HSLFSlideShow(fis)) {

            Map<String, Object> metadata = extractMetadata(ppt, options);

            if (options.isIncludeMetadata()) {
                metadata.put("文件名", filePath.getFileName().toString());
                metadata.put("文件大小", filePath.toFile().length());
            }

            String markdownContent = convertToMarkdown(ppt, metadata, options);

            List<String> warnings = new ArrayList<>();

            return new ConversionResult(markdownContent, metadata, warnings,
                    filePath.toFile().length(), filePath.getFileName().toString());

        } catch (IOException e) {
            String errorMessage = "Failed to process PPT file: " + e.getMessage();
            logger.error(errorMessage, e);
            throw new ConversionException(errorMessage, e, filePath.getFileName().toString(), getName());
        }
    }

    @Override
    public boolean supports(String mimeType) {
        return "application/vnd.ms-powerpoint".equals(mimeType);
    }

    @Override
    public int getPriority() {
        return 100;
    }

    @Override
    public String getName() {
        return "PptConverter";
    }

    private Map<String, Object> extractMetadata(HSLFSlideShow ppt, ConversionOptions options) {
        Map<String, Object> metadata = new HashMap<>();

        if (options.isIncludeMetadata()) {
            metadata.put("幻灯片数量", ppt.getSlides().size());
            metadata.put("转换时刻", LocalDateTime.now());

            Dimension pageSize = ppt.getPageSize();
            if (pageSize != null) {
                metadata.put("幻灯片宽度", pageSize.width);
                metadata.put("幻灯片高度", pageSize.height);
            }
        }

        return metadata;
    }

    private String convertToMarkdown(HSLFSlideShow ppt, Map<String, Object> metadata, ConversionOptions options) {
        StringBuilder markdown = new StringBuilder();

        if (options.isIncludeMetadata() && !metadata.isEmpty()) {
            markdown.append("## 演示文稿信息\n\n");
            for (Map.Entry<String, Object> entry : metadata.entrySet()) {
                if (entry.getValue() != null) {
                    markdown.append("- **").append(entry.getKey())
                            .append(":** ").append(entry.getValue()).append("\n");
                }
            }
            markdown.append("\n");
        }

        List<HSLFSlide> slides = ppt.getSlides();
        for (int i = 0; i < slides.size(); i++) {
            processSlide(slides.get(i), i + 1, markdown, options);
        }

        return markdown.toString();
    }

    private void processSlide(HSLFSlide slide, int slideNum, StringBuilder markdown, ConversionOptions options) {
        markdown.append("## 幻灯片 ").append(slideNum).append("\n\n");

        // 处理所有形状
        for (HSLFShape shape : slide.getShapes()) {
            if (shape instanceof HSLFTextShape) {
                processTextShape((HSLFTextShape) shape, markdown, options);
            } else if (shape instanceof HSLFGroupShape) {
                processGroupShape((HSLFGroupShape) shape, markdown, options);
            }
            // 注意：HSLFTable 在 POI 5.x 中 API 不稳定，暂时跳过表格处理
        }

        markdown.append("---\n\n");
    }

    private void processTextShape(HSLFTextShape textShape, StringBuilder markdown, ConversionOptions options) {
        String text = textShape.getText();
        if (text == null || text.trim().isEmpty()) {
            return;
        }

        // 检测是否为标题（基于字体大小）
        boolean isTitle = false;
        for (HSLFTextParagraph paragraph : textShape.getTextParagraphs()) {
            for (HSLFTextRun run : paragraph.getTextRuns()) {
                Double fontSize = run.getFontSize();
                if (fontSize != null && fontSize > 30) {
                    isTitle = true;
                    break;
                }
            }
        }

        if (isTitle) {
            markdown.append("### ").append(text.trim()).append("\n\n");
        } else {
            String formattedText = processTextRuns(textShape);
            if (!formattedText.trim().isEmpty()) {
                markdown.append(formattedText).append("\n\n");
            }
        }
    }

    private String processTextRuns(HSLFTextShape textShape) {
        StringBuilder formatted = new StringBuilder();

        for (HSLFTextParagraph paragraph : textShape.getTextParagraphs()) {
            for (HSLFTextRun run : paragraph.getTextRuns()) {
                String runText = run.getRawText();
                if (runText == null || runText.isEmpty()) {
                    continue;
                }

                if (run.isBold() && run.isItalic()) {
                    formatted.append("***").append(runText).append("***");
                } else if (run.isBold()) {
                    formatted.append("**").append(runText).append("**");
                } else if (run.isItalic()) {
                    formatted.append("*").append(runText).append("*");
                } else if (run.isUnderlined()) {
                    formatted.append("<u>").append(runText).append("</u>");
                } else if (run.isStrikethrough()) {
                    formatted.append("~~").append(runText).append("~~");
                } else {
                    formatted.append(runText);
                }
            }
        }

        return formatted.toString();
    }

    private void processGroupShape(HSLFGroupShape groupShape, StringBuilder markdown, ConversionOptions options) {
        for (HSLFShape shape : groupShape.getShapes()) {
            if (shape instanceof HSLFTextShape) {
                processTextShape((HSLFTextShape) shape, markdown, options);
            } else if (shape instanceof HSLFGroupShape) {
                processGroupShape((HSLFGroupShape) shape, markdown, options);
            }
        }
    }
}
