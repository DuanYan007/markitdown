package com.markitdown.converters;

import com.markdown.engine.MarkdownBuilder;
import com.markitdown.api.ConversionResult;
import com.markitdown.api.DocumentConverter;
import com.markitdown.config.ConversionOptions;
import com.markitdown.exceptions.ConversionException;
import org.apache.poi.xslf.usermodel.XMLSlideShow;
import org.apache.poi.xslf.usermodel.XSLFGroupShape;
import org.apache.poi.xslf.usermodel.XSLFShape;
import org.apache.poi.xslf.usermodel.XSLFSlide;
import org.apache.poi.xslf.usermodel.XSLFTable;
import org.apache.poi.xslf.usermodel.XSLFTableCell;
import org.apache.poi.xslf.usermodel.XSLFTableRow;
import org.apache.poi.xslf.usermodel.XSLFTextParagraph;
import org.apache.poi.xslf.usermodel.XSLFTextRun;
import org.apache.poi.xslf.usermodel.XSLFTextShape;
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
 * Converts PPTX presentations into Markdown.
 */
public class PptxConverter implements DocumentConverter {

    private static final Logger logger = LoggerFactory.getLogger(PptxConverter.class);

    @Override
    public ConversionResult convert(Path filePath, ConversionOptions options) throws ConversionException {
        requireNonNull(filePath, "File path cannot be null");
        requireNonNull(options, "Conversion options cannot be null");

        logger.info("Converting PPTX file: {}", filePath);

        try (FileInputStream input = new FileInputStream(filePath.toFile());
             XMLSlideShow presentation = new XMLSlideShow(input)) {

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
            String errorMessage = "Failed to process PPTX file: " + e.getMessage();
            logger.error(errorMessage, e);
            throw new ConversionException(errorMessage, e, filePath.getFileName().toString(), getName());
        }
    }

    @Override
    public boolean supports(String mimeType) {
        return "application/vnd.openxmlformats-officedocument.presentationml.presentation".equals(mimeType)
                || "application/vnd.ms-powerpoint".equals(mimeType);
    }

    @Override
    public int getPriority() {
        return 100;
    }

    @Override
    public String getName() {
        return "PptxConverter";
    }

    private Map<String, Object> extractMetadata(XMLSlideShow presentation, ConversionOptions options, Path filePath) {
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

    private String convertToMarkdown(XMLSlideShow presentation, Map<String, Object> metadata, ConversionOptions options) {
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

        List<XSLFSlide> slides = presentation.getSlides();
        for (int slideIndex = 0; slideIndex < slides.size(); slideIndex++) {
            processSlide(slides.get(slideIndex), slideIndex + 1, markdown, options);
        }

        return markdown.toString();
    }

    private void processSlide(XSLFSlide slide, int slideNumber, StringBuilder markdown, ConversionOptions options) {
        markdown.append("## Slide ").append(slideNumber).append("\n\n");
        processSlideShapes(slide, markdown, options);
        markdown.append("---\n\n");
    }

    private void processSlideShapes(XSLFSlide slide, StringBuilder markdown, ConversionOptions options) {
        for (XSLFShape shape : slide.getShapes()) {
            if (shape instanceof XSLFTextShape) {
                processTextShape((XSLFTextShape) shape, markdown);
            } else if (shape instanceof XSLFTable && options.content().includeTables()) {
                processTable((XSLFTable) shape, markdown);
            } else if (shape instanceof XSLFGroupShape) {
                processGroupShape((XSLFGroupShape) shape, markdown, options);
            }
        }
    }

    private void processTextShape(XSLFTextShape textShape, StringBuilder markdown) {
        for (XSLFTextParagraph paragraph : textShape.getTextParagraphs()) {
            String text = paragraph.getText();
            if (text == null || text.trim().isEmpty()) {
                continue;
            }

            if (isTitleParagraph(textShape, paragraph)) {
                markdown.append("### ").append(text.trim()).append("\n\n");
            } else {
                String formatted = processTextParagraph(paragraph);
                if (!formatted.trim().isEmpty()) {
                    markdown.append(formatted).append("\n\n");
                }
            }
        }
    }

    private String processTextParagraph(XSLFTextParagraph paragraph) {
        StringBuilder formatted = new StringBuilder();

        for (XSLFTextRun run : paragraph.getTextRuns()) {
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

        return formatted.toString();
    }

    private void processTable(XSLFTable table, StringBuilder markdown) {
        List<XSLFTableRow> rows = table.getRows();
        if (rows.isEmpty()) {
            return;
        }

        markdown.append("\n");

        for (int rowIndex = 0; rowIndex < rows.size(); rowIndex++) {
            XSLFTableRow row = rows.get(rowIndex);
            List<XSLFTableCell> cells = row.getCells();
            if (cells.isEmpty()) {
                continue;
            }

            markdown.append("| ");
            for (XSLFTableCell cell : cells) {
                markdown.append(cell.getText().replace("\n", " ").trim()).append(" | ");
            }
            markdown.append("\n");

            if (rowIndex == 0) {
                markdown.append("|");
                for (int columnIndex = 0; columnIndex < cells.size(); columnIndex++) {
                    markdown.append(" --- |");
                }
                markdown.append("\n");
            }
        }

        markdown.append("\n");
    }

    private void processGroupShape(XSLFGroupShape groupShape, StringBuilder markdown, ConversionOptions options) {
        for (XSLFShape shape : groupShape.getShapes()) {
            if (shape instanceof XSLFTextShape) {
                processTextShape((XSLFTextShape) shape, markdown);
            } else if (shape instanceof XSLFTable && options.content().includeTables()) {
                processTable((XSLFTable) shape, markdown);
            }
        }
    }

    private boolean isTitleParagraph(XSLFTextShape textShape, XSLFTextParagraph paragraph) {
        try {
            if (textShape.getPlaceholder() != null) {
                return true;
            }
        } catch (Exception ignored) {
            // Ignore placeholder resolution failures.
        }

        if (!paragraph.getTextRuns().isEmpty()) {
            XSLFTextRun firstRun = paragraph.getTextRuns().get(0);
            Double fontSize = firstRun.getFontSize();
            if (fontSize != null && fontSize > 30) {
                return true;
            }
        }

        String text = paragraph.getText();
        if (text == null) {
            return false;
        }

        String trimmed = text.trim();
        return !trimmed.isEmpty()
                && trimmed.length() < 100
                && Character.isUpperCase(trimmed.charAt(0))
                && (!trimmed.contains(".") || trimmed.split("\\.").length == 1);
    }
}
