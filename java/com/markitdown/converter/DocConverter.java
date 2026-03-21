package com.markitdown.converter;

import com.markdown.engine.MarkdownBuilder;
import com.markdown.engine.config.MarkdownConfig;
import com.markitdown.api.ConversionResult;
import com.markitdown.api.DocumentConverter;
import com.markitdown.config.ConversionOptions;
import com.markitdown.exception.ConversionException;
import org.apache.poi.hwpf.HWPFDocument;
import org.apache.poi.hwpf.usermodel.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.*;

import static java.util.Objects.requireNonNull;

/**
 * @class DocConverter
 * @brief Word 97-2003 文档转换器
 */
public class DocConverter implements DocumentConverter {

    private static final Logger logger = LoggerFactory.getLogger(DocConverter.class);

    private MarkdownBuilder mb;

    @Override
    public ConversionResult convert(Path filePath, ConversionOptions options) throws ConversionException {
        requireNonNull(filePath, "文件路径不能为空");
        requireNonNull(options, "转换选项不能为空");

        logger.info("开始转换 DOC 文件: {}", filePath);
        mb = new MarkdownBuilder(new MarkdownConfig());

        try (FileInputStream fis = new FileInputStream(filePath.toFile());
             HWPFDocument document = new HWPFDocument(fis)) {

            Map<String, Object> metadata = extractMetadata(document, options, filePath);
            String markdownContent = convertToMarkdown(document, metadata, options);

            List<String> warnings = new ArrayList<>();

            return new ConversionResult(markdownContent, metadata, warnings,
                    filePath.toFile().length(), filePath.getFileName().toString());

        } catch (IOException e) {
            String errorMessage = "处理 DOC 文件失败: " + e.getMessage();
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

        if (options.isIncludeMetadata()) {
            metadata.put("段落数量", document.getRange().numParagraphs());
            metadata.put("文件名", filePath.getFileName().toString());
            metadata.put("文件大小", filePath.toFile().length());
            metadata.put("转换时刻", LocalDateTime.now());
        }

        return metadata;
    }

    private String convertToMarkdown(HWPFDocument document, Map<String, Object> metadata, ConversionOptions options) {
        if (options.isIncludeMetadata() && !metadata.isEmpty()) {
            mb.header(metadata);
        }

        mb.append(mb.heading("内容", 2));

        Range range = document.getRange();
        for (int i = 0; i < range.numParagraphs(); i++) {
            Paragraph paragraph = range.getParagraph(i);
            processParagraph(paragraph, options);
        }

        // 处理表格 - 使用简化方法
        if (options.isIncludeTables()) {
            processTables(range, options);
        }

        return mb.flush().toString();
    }

    private void processParagraph(Paragraph paragraph, ConversionOptions options) {
        String text = paragraph.text();
        if (text == null || text.trim().isEmpty()) {
            mb.newline();
            return;
        }

        // 处理带格式化的普通段落
        StringBuilder formatted = processParagraphFormatting(paragraph);
        mb.append(formatted);
        mb.newline(2);
    }

    private StringBuilder processParagraphFormatting(Paragraph paragraph) {
        StringBuilder formatted = new StringBuilder();

        for (int i = 0; i < paragraph.numCharacterRuns(); i++) {
            CharacterRun run = paragraph.getCharacterRun(i);
            String runText = run.text();

            if (runText == null || runText.isEmpty()) {
                continue;
            }

            runText = runText.replace("\r", "").replace("\n", " ");

            if (run.isBold() && run.isItalic()) {
                formatted.append("***").append(runText).append("***");
            } else if (run.isBold()) {
                formatted.append("**").append(runText).append("**");
            } else if (run.isItalic()) {
                formatted.append("*").append(runText).append("*");
            } else if (run.isStrikeThrough()) {
                formatted.append("~~").append(runText).append("~~");
            } else {
                formatted.append(runText);
            }
        }

        return formatted;
    }

    private void processTables(Range range, ConversionOptions options) {
        // 简化表格处理 - 遍历所有段落，检测表格段落
        for (int i = 0; i < range.numParagraphs(); i++) {
            Paragraph para = range.getParagraph(i);
            if (para.isInTable()) {
                // 找到表格起始，收集整个表格
                Table table = range.getTable(para);
                if (table != null) {
                    processTable(table, options);
                    // 跳过已处理的表格行
                    i += table.numRows() - 1;
                }
            }
        }
    }

    private void processTable(Table table, ConversionOptions options) {
        if (!options.isIncludeTables()) {
            return;
        }

        int numRows = table.numRows();
        if (numRows == 0) {
            return;
        }

        mb.newline();

        // 处理表头行
        TableRow headerRow = table.getRow(0);
        List<String> headers = new ArrayList<>();
        for (int i = 0; i < headerRow.numCells(); i++) {
            TableCell cell = headerRow.getCell(i);
            headers.add(cell.text().trim().replace("\n", " "));
        }

        // 处理数据行
        List<List<String>> dataRows = new ArrayList<>();
        for (int i = 1; i < numRows; i++) {
            TableRow row = table.getRow(i);
            List<String> rowData = new ArrayList<>();
            for (int j = 0; j < row.numCells(); j++) {
                TableCell cell = row.getCell(j);
                rowData.add(cell.text().trim().replace("\n", " "));
            }
            dataRows.add(rowData);
        }

        String[][] dataArray = dataRows.stream()
                .map(list -> list.toArray(new String[0]))
                .toArray(String[][]::new);

        mb.append(mb.table(headers.toArray(new String[0]), dataArray));
        mb.newline();
    }
}
