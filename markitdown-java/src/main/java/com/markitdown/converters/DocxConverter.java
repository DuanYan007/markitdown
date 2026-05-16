package com.markitdown.converters;

import com.markdown.engine.MarkdownBuilder;
import com.markitdown.api.ConversionResult;
import com.markitdown.api.DocumentConverter;
import com.markitdown.config.ConversionOptions;
import com.markitdown.exceptions.ConversionException;
import com.markitdown.models.ExtractedImage;
import com.markitdown.utils.ImageExtractor;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.apache.poi.xwpf.usermodel.XWPFPicture;
import org.apache.poi.xwpf.usermodel.XWPFPictureData;
import org.apache.poi.xwpf.usermodel.XWPFRun;
import org.apache.poi.xwpf.usermodel.XWPFStyle;
import org.apache.poi.xwpf.usermodel.XWPFStyles;
import org.apache.poi.xwpf.usermodel.XWPFTable;
import org.apache.poi.xwpf.usermodel.XWPFTableCell;
import org.apache.poi.xwpf.usermodel.XWPFTableRow;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import static java.util.Objects.requireNonNull;

/**
 * Converts DOCX documents into Markdown.
 */
public class DocxConverter implements DocumentConverter {

    private static final Logger logger = LoggerFactory.getLogger(DocxConverter.class);

    private MarkdownBuilder markdown;
    private int currentImageIndex;

    @Override
    public ConversionResult convert(Path filePath, ConversionOptions options) throws ConversionException {
        requireNonNull(filePath, "File path cannot be null");
        requireNonNull(options, "Conversion options cannot be null");
        ConversionOptions.ContentOptions content = options.content();

        logger.info("Converting DOCX file: {}", filePath);
        markdown = new MarkdownBuilder(options.toMarkdownConfig());

        try (FileInputStream input = new FileInputStream(filePath.toFile());
             XWPFDocument document = new XWPFDocument(input)) {

            Map<String, Object> metadata = extractMetadata(document, options);
            if (content.includeMetadata()) {
                metadata.put("File Name", filePath.getFileName().toString());
                metadata.put("File Size", filePath.toFile().length());
            }

            String markdownContent = convertToMarkdown(document, metadata, options);
            return new ConversionResult(
                    markdownContent,
                    metadata,
                    new ArrayList<>(),
                    filePath.toFile().length(),
                    filePath.getFileName().toString()
            );
        } catch (IOException e) {
            String errorMessage = "Failed to process DOCX file: " + e.getMessage();
            logger.error(errorMessage, e);
            throw new ConversionException(errorMessage, e, filePath.getFileName().toString(), getName());
        }
    }

    @Override
    public boolean supports(String mimeType) {
        return "application/vnd.openxmlformats-officedocument.wordprocessingml.document".equals(mimeType)
                || "application/msword".equals(mimeType);
    }

    @Override
    public int getPriority() {
        return 100;
    }

    @Override
    public String getName() {
        return "DocxConverter";
    }

    private Map<String, Object> extractMetadata(XWPFDocument document, ConversionOptions options) {
        Map<String, Object> metadata = new HashMap<>();
        if (options.content().includeMetadata()) {
            metadata.put("Paragraph Count", document.getParagraphs().size());
            metadata.put("Table Count", document.getTables().size());
            metadata.put("Converted At", LocalDateTime.now());
        }
        return metadata;
    }

    private String convertToMarkdown(XWPFDocument document, Map<String, Object> metadata, ConversionOptions options) {
        if (options.content().includeMetadata() && !metadata.isEmpty()) {
            markdown.header(metadata);
        }

        markdown.append(markdown.heading("Content", 2));

        List<ExtractedImage> extractedImages = null;
        if (options.content().includeImages()) {
            extractedImages = extractImages(document, options);
            currentImageIndex = 0;
        }

        for (XWPFParagraph paragraph : document.getParagraphs()) {
            processParagraph(document, paragraph, extractedImages);
        }

        if (options.content().includeTables()) {
            for (XWPFTable table : document.getTables()) {
                processTable(table);
            }
        }

        return markdown.flush().toString();
    }

    private void processParagraph(XWPFDocument document, XWPFParagraph paragraph, List<ExtractedImage> extractedImages) {
        String text = paragraph.getText();
        if (text == null || text.trim().isEmpty()) {
            markdown.newline();
            return;
        }

        appendEmbeddedImages(paragraph, text, extractedImages);

        String style = getStyleName(document, paragraph);
        if (!appendStyledParagraph(style, text)) {
            markdown.append(processParagraphFormatting(paragraph));
            markdown.newline(2);
        }
    }

    private void appendEmbeddedImages(XWPFParagraph paragraph, String text, List<ExtractedImage> extractedImages) {
        if (extractedImages == null || extractedImages.isEmpty()) {
            return;
        }

        boolean foundPictureInRun = false;
        for (XWPFRun run : paragraph.getRuns()) {
            for (XWPFPicture picture : run.getEmbeddedPictures()) {
                foundPictureInRun = true;
                ExtractedImage extractedImage = findExtractedImage(picture, extractedImages);
                if (extractedImage != null) {
                    markdown.append(extractedImage.toMarkdown("Image")).newline();
                }
            }
        }

        if (foundPictureInRun || !text.toLowerCase(Locale.ROOT).contains("embedded image")) {
            return;
        }

        String imageFilename = extractImageFilename(text);
        if (imageFilename != null) {
            for (ExtractedImage image : extractedImages) {
                if (imageFilename.equals(image.getOriginalFilename())) {
                    markdown.append(image.toMarkdown("Image")).newline();
                    return;
                }
            }
        }

        if (currentImageIndex < extractedImages.size()) {
            markdown.append(extractedImages.get(currentImageIndex++).toMarkdown("Image")).newline();
        }
    }

    private String getStyleName(XWPFDocument document, XWPFParagraph paragraph) {
        XWPFStyles styles = document.getStyles();
        if (styles == null) {
            return "";
        }
        XWPFStyle style = styles.getStyle(paragraph.getStyleID());
        return style == null ? "" : style.getName();
    }

    private boolean appendStyledParagraph(String style, String text) {
        if (style == null) {
            return false;
        }

        switch (style.toLowerCase(Locale.ROOT)) {
            case "title":
                markdown.append(markdown.heading(markdown.escapeMarkdown(text), 1));
                markdown.horizontalRule();
                return true;
            case "heading 1":
                markdown.append(markdown.heading(markdown.escapeMarkdown(text), 2));
                return true;
            case "heading 2":
                markdown.append(markdown.heading(markdown.escapeMarkdown(text), 3));
                return true;
            case "heading 3":
                markdown.append(markdown.heading(markdown.escapeMarkdown(text), 4));
                return true;
            case "heading 4":
                markdown.append(markdown.heading(markdown.escapeMarkdown(text), 5));
                return true;
            case "heading 5":
                markdown.append(markdown.heading(markdown.escapeMarkdown(text), 6));
                return true;
            case "list bullet":
                markdown.append(markdown.unorder_item(markdown.escapeMarkdown(text)));
                markdown.newline(2);
                return true;
            case "list number":
                markdown.append("1. ").append(markdown.escapeMarkdown(text));
                markdown.newline(2);
                return true;
            default:
                return false;
        }
    }

    private StringBuilder processParagraphFormatting(XWPFParagraph paragraph) {
        StringBuilder formatted = new StringBuilder();

        for (XWPFRun run : paragraph.getRuns()) {
            String runText = run.getText(0);
            if (runText == null || runText.isEmpty()) {
                continue;
            }

            String escapedText = markdown.escapeMarkdown(runText);
            if (run.isBold() && run.isItalic()) {
                formatted.append("***").append(escapedText).append("***");
            } else if (run.isBold()) {
                formatted.append("**").append(escapedText).append("**");
            } else if (run.isItalic()) {
                formatted.append("*").append(escapedText).append("*");
            } else if (run.isStrikeThrough()) {
                formatted.append("~~").append(escapedText).append("~~");
            } else {
                formatted.append(escapedText);
            }
        }

        return formatted;
    }

    private void processTable(XWPFTable table) {
        List<XWPFTableRow> rows = table.getRows();
        if (rows.isEmpty()) {
            return;
        }

        markdown.newline();

        List<String> headers = new ArrayList<>();
        for (XWPFTableCell cell : rows.get(0).getTableCells()) {
            headers.add(cleanCellText(cell.getText()));
        }

        List<List<String>> bodyRows = new ArrayList<>();
        for (int rowIndex = 1; rowIndex < rows.size(); rowIndex++) {
            XWPFTableRow row = rows.get(rowIndex);
            if (row.getTableCells().isEmpty()) {
                continue;
            }

            List<String> values = new ArrayList<>();
            for (XWPFTableCell cell : row.getTableCells()) {
                values.add(cleanCellText(cell.getText()));
            }
            bodyRows.add(values);
        }

        String[][] tableData = bodyRows.stream()
                .map(values -> values.toArray(new String[0]))
                .toArray(String[][]::new);

        markdown.append(markdown.table(headers.toArray(new String[0]), tableData));
        markdown.newline();
    }

    private List<ExtractedImage> extractImages(XWPFDocument document, ConversionOptions options) {
        try {
            ConversionOptions.OutputOptions output = options.output();
            Path outputPath = output.outputPath();
            if (outputPath == null) {
                logger.warn("Output path is not set. Skipping image extraction.");
                return new ArrayList<>();
            }

            String outputFileName = outputPath.getFileName().toString();
            int extensionIndex = outputFileName.lastIndexOf('.');
            if (extensionIndex > 0) {
                outputFileName = outputFileName.substring(0, extensionIndex);
            }

            ImageExtractor extractor = new ImageExtractor(
                    outputPath.getParent(),
                    output.imageOutputDir(),
                    outputFileName
            );

            List<XWPFPictureData> pictures = document.getAllPictures();
            return extractor.extractPictures(pictures, outputPath.getParent());
        } catch (Exception e) {
            logger.warn("Image extraction failed: {}", e.getMessage());
            return new ArrayList<>();
        }
    }

    private ExtractedImage findExtractedImage(XWPFPicture picture, List<ExtractedImage> extractedImages) {
        try {
            XWPFPictureData pictureData = picture.getPictureData();
            String filename = pictureData.getFileName();
            for (ExtractedImage image : extractedImages) {
                if (filename.equals(image.getOriginalFilename())) {
                    return image;
                }
            }
        } catch (Exception e) {
            logger.debug("Failed to match extracted image: {}", e.getMessage());
        }
        return null;
    }

    private String extractImageFilename(String text) {
        if (text == null || text.isEmpty()) {
            return null;
        }

        String[] patterns = {
                "Embedded image:\\s*(\\S+)",
                "image:\\s*(\\S+)"
        };

        for (String pattern : patterns) {
            java.util.regex.Pattern compiled = java.util.regex.Pattern.compile(
                    pattern,
                    java.util.regex.Pattern.CASE_INSENSITIVE
            );
            java.util.regex.Matcher matcher = compiled.matcher(text);
            if (matcher.find()) {
                String filename = matcher.group(1).trim();
                logger.debug("Extracted image file name from paragraph text: {}", filename);
                return filename;
            }
        }

        return null;
    }

    private String cleanCellText(String text) {
        if (text == null) {
            return "";
        }
        return text.replace("\n", " ").replace("\r", "").trim();
    }
}
