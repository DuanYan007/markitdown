package com.markitdown.converters;

import com.markdown.engine.MarkdownBuilder;
import com.markdown.engine.config.MarkdownConfig;
import com.markitdown.api.ConversionResult;
import com.markitdown.api.DocumentConverter;
import com.markitdown.config.ConversionOptions;
import com.markitdown.exceptions.ConversionException;
import org.apache.poi.hwpf.HWPFDocument;
import org.apache.poi.hwpf.usermodel.CharacterRun;
import org.apache.poi.hwpf.usermodel.Paragraph;
import org.apache.poi.hwpf.usermodel.Range;
import org.apache.poi.hwpf.usermodel.Table;
import org.apache.poi.hwpf.usermodel.TableCell;
import org.apache.poi.hwpf.usermodel.TableRow;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.util.Objects.requireNonNull;

/**
 * Converts legacy DOC files into Markdown.
 */
public class DocConverter implements DocumentConverter {

    private static final Logger logger = LoggerFactory.getLogger(DocConverter.class);

    private MarkdownBuilder markdown;

    @Override
    public ConversionResult convert(Path filePath, ConversionOptions options) throws ConversionException {
        requireNonNull(filePath, "File path cannot be null");
        requireNonNull(options, "Conversion options cannot be null");

        logger.info("Converting DOC file: {}", filePath);
        markdown = new MarkdownBuilder(new MarkdownConfig());

        try (FileInputStream input = new FileInputStream(filePath.toFile());
             HWPFDocument document = new HWPFDocument(input)) {

            Map<String, Object> metadata = extractMetadata(document, options, filePath);
            String markdownContent = convertToMarkdown(document, metadata, options);

            return new ConversionResult(
                    markdownContent,
                    metadata,
                    new ArrayList<>(),
                    filePath.toFile().length(),
                    filePath.getFileName().toString()
            );
        } catch (IOException e) {
            String errorMessage = "Failed to process DOC file: " + e.getMessage();
            logger.error(errorMessage, e);
            throw new ConversionException(errorMessage, e, filePath.getFileName().toString(), getName());
        }
    }

    @Override
    public boolean supports(String mimeType) {
        return "application/msword".equals(mimeType);
    }

    @Override
    public int getPriority() {
        return 100;
    }

    @Override
    public String getName() {
        return "DocConverter";
    }

    private Map<String, Object> extractMetadata(HWPFDocument document, ConversionOptions options, Path filePath) {
        Map<String, Object> metadata = new HashMap<>();
        if (options.content().includeMetadata()) {
            metadata.put("Paragraph Count", document.getRange().numParagraphs());
            metadata.put("File Name", filePath.getFileName().toString());
            metadata.put("File Size", filePath.toFile().length());
            metadata.put("Converted At", LocalDateTime.now());
        }
        return metadata;
    }

    private String convertToMarkdown(HWPFDocument document, Map<String, Object> metadata, ConversionOptions options) {
        if (options.content().includeMetadata() && !metadata.isEmpty()) {
            markdown.header(metadata);
        }

        markdown.append(markdown.heading("Content", 2));

        Range range = document.getRange();
        for (int paragraphIndex = 0; paragraphIndex < range.numParagraphs(); paragraphIndex++) {
            Paragraph paragraph = range.getParagraph(paragraphIndex);
            processParagraph(paragraph);
        }

        if (options.content().includeTables()) {
            processTables(range);
        }

        return markdown.flush().toString();
    }

    private void processParagraph(Paragraph paragraph) {
        String text = paragraph.text();
        if (text == null || text.trim().isEmpty()) {
            markdown.newline();
            return;
        }

        markdown.append(processParagraphFormatting(paragraph));
        markdown.newline(2);
    }

    private StringBuilder processParagraphFormatting(Paragraph paragraph) {
        StringBuilder formatted = new StringBuilder();

        for (int runIndex = 0; runIndex < paragraph.numCharacterRuns(); runIndex++) {
            CharacterRun run = paragraph.getCharacterRun(runIndex);
            String runText = run.text();
            if (runText == null || runText.isEmpty()) {
                continue;
            }

            String cleanedText = runText.replace("\r", "").replace("\n", " ");
            if (run.isBold() && run.isItalic()) {
                formatted.append("***").append(cleanedText).append("***");
            } else if (run.isBold()) {
                formatted.append("**").append(cleanedText).append("**");
            } else if (run.isItalic()) {
                formatted.append("*").append(cleanedText).append("*");
            } else if (run.isStrikeThrough()) {
                formatted.append("~~").append(cleanedText).append("~~");
            } else {
                formatted.append(cleanedText);
            }
        }

        return formatted;
    }

    private void processTables(Range range) {
        for (int paragraphIndex = 0; paragraphIndex < range.numParagraphs(); paragraphIndex++) {
            Paragraph paragraph = range.getParagraph(paragraphIndex);
            if (!paragraph.isInTable()) {
                continue;
            }

            Table table = range.getTable(paragraph);
            if (table == null) {
                continue;
            }

            processTable(table);
            paragraphIndex += table.numRows() - 1;
        }
    }

    private void processTable(Table table) {
        int rowCount = table.numRows();
        if (rowCount == 0) {
            return;
        }

        markdown.newline();

        TableRow headerRow = table.getRow(0);
        List<String> headers = new ArrayList<>();
        for (int cellIndex = 0; cellIndex < headerRow.numCells(); cellIndex++) {
            TableCell cell = headerRow.getCell(cellIndex);
            headers.add(cleanCellText(cell.text()));
        }

        List<List<String>> rows = new ArrayList<>();
        for (int rowIndex = 1; rowIndex < rowCount; rowIndex++) {
            TableRow row = table.getRow(rowIndex);
            List<String> rowData = new ArrayList<>();
            for (int cellIndex = 0; cellIndex < row.numCells(); cellIndex++) {
                TableCell cell = row.getCell(cellIndex);
                rowData.add(cleanCellText(cell.text()));
            }
            rows.add(rowData);
        }

        String[][] tableData = rows.stream()
                .map(values -> values.toArray(new String[0]))
                .toArray(String[][]::new);

        markdown.append(markdown.table(headers.toArray(new String[0]), tableData));
        markdown.newline();
    }

    private String cleanCellText(String text) {
        if (text == null) {
            return "";
        }
        return text.trim().replace("\n", " ").replace("\r", "");
    }
}
