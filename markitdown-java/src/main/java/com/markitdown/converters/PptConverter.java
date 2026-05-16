package com.markitdown.converters;

import com.markitdown.api.ConversionResult;
import com.markitdown.api.DocumentConverter;
import com.markitdown.config.ConversionOptions;
import com.markitdown.exceptions.ConversionException;
import org.apache.poi.hslf.usermodel.HSLFGroupShape;
import org.apache.poi.hslf.usermodel.HSLFShape;
import org.apache.poi.hslf.usermodel.HSLFSlide;
import org.apache.poi.hslf.usermodel.HSLFSlideShow;
import org.apache.poi.hslf.usermodel.HSLFTextParagraph;
import org.apache.poi.hslf.usermodel.HSLFTextRun;
import org.apache.poi.hslf.usermodel.HSLFTextShape;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.Dimension;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static java.util.Objects.requireNonNull;

/**
 * Converts legacy PPT presentations into Markdown.
 */
public class PptConverter implements DocumentConverter {

    private static final Logger logger = LoggerFactory.getLogger(PptConverter.class);

    @Override
    public ConversionResult convert(Path filePath, ConversionOptions options) throws ConversionException {
        requireNonNull(filePath, "File path cannot be null");
        requireNonNull(options, "Conversion options cannot be null");

        logger.info("Converting PPT file: {}", filePath);

        try (FileInputStream input = new FileInputStream(filePath.toFile());
             HSLFSlideShow presentation = new HSLFSlideShow(input)) {

            Map<String, Object> metadata = extractMetadata(presentation, options, filePath);
            String markdownContent = convertToMarkdown(presentation, metadata, options);

            return new ConversionResult(
                    markdownContent,
                    metadata,
                    new ArrayList<>(),
                    filePath.toFile().length(),
                    filePath.getFileName().toString()
            );
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

    private Map<String, Object> extractMetadata(HSLFSlideShow presentation, ConversionOptions options, Path filePath) {
        Map<String, Object> metadata = new LinkedHashMap<>();
        if (!options.content().includeMetadata()) {
            return metadata;
        }

        metadata.put("Slide Count", presentation.getSlides().size());
        Dimension pageSize = presentation.getPageSize();
        if (pageSize != null) {
            metadata.put("Slide Width", pageSize.width);
            metadata.put("Slide Height", pageSize.height);
        }
        metadata.put("File Name", filePath.getFileName().toString());
        metadata.put("File Size", filePath.toFile().length());
        metadata.put("Converted At", LocalDateTime.now());
        return metadata;
    }

    private String convertToMarkdown(HSLFSlideShow presentation, Map<String, Object> metadata, ConversionOptions options) {
        StringBuilder markdown = new StringBuilder();

        if (options.content().includeMetadata() && !metadata.isEmpty()) {
            markdown.append("## Presentation Information\n\n");
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

        List<HSLFSlide> slides = presentation.getSlides();
        for (int slideIndex = 0; slideIndex < slides.size(); slideIndex++) {
            processSlide(slides.get(slideIndex), slideIndex + 1, markdown);
        }

        return markdown.toString();
    }

    private void processSlide(HSLFSlide slide, int slideNumber, StringBuilder markdown) {
        markdown.append("## Slide ").append(slideNumber).append("\n\n");

        for (HSLFShape shape : slide.getShapes()) {
            if (shape instanceof HSLFTextShape) {
                processTextShape((HSLFTextShape) shape, markdown);
            } else if (shape instanceof HSLFGroupShape) {
                processGroupShape((HSLFGroupShape) shape, markdown);
            }
        }

        markdown.append("---\n\n");
    }

    private void processTextShape(HSLFTextShape textShape, StringBuilder markdown) {
        String text = textShape.getText();
        if (text == null || text.trim().isEmpty()) {
            return;
        }

        boolean titleLike = false;
        for (HSLFTextParagraph paragraph : textShape.getTextParagraphs()) {
            for (HSLFTextRun run : paragraph.getTextRuns()) {
                Double fontSize = run.getFontSize();
                if (fontSize != null && fontSize > 30) {
                    titleLike = true;
                    break;
                }
            }
        }

        if (titleLike) {
            markdown.append("### ").append(text.trim()).append("\n\n");
            return;
        }

        String formatted = processTextRuns(textShape);
        if (!formatted.trim().isEmpty()) {
            markdown.append(formatted).append("\n\n");
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

    private void processGroupShape(HSLFGroupShape groupShape, StringBuilder markdown) {
        for (HSLFShape shape : groupShape.getShapes()) {
            if (shape instanceof HSLFTextShape) {
                processTextShape((HSLFTextShape) shape, markdown);
            } else if (shape instanceof HSLFGroupShape) {
                processGroupShape((HSLFGroupShape) shape, markdown);
            }
        }
    }
}
