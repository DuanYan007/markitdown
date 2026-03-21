package com.markitdown.converter;

import com.markdown.engine.MarkdownBuilder;
import com.markdown.engine.config.MarkdownConfig;
import com.markitdown.api.ConversionResult;
import com.markitdown.api.DocumentConverter;
import com.markitdown.config.ConversionOptions;
import com.markitdown.exception.ConversionException;
import org.apache.poi.hssf.usermodel.*;
import org.apache.poi.ss.usermodel.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.*;

import static java.util.Objects.requireNonNull;

/**
 * @class XlsConverter
 * @brief Excel 97-2003 电子表格转换器，用于将 XLS 文件转换为 Markdown 格式
 * @details 使用 Apache POI HSSF 库解析旧版 Excel 工作簿，提取工作表数据和结构信息
 *          支持多工作表处理、自动表头检测、数据类型转换等功能
 *          将表格数据转换为标准 Markdown 表格格式
 *
 * @author duan yan
 * @version 2.1.0
 * @since 2.1.0
 */
public class XlsConverter implements DocumentConverter {

    private static final Logger logger = LoggerFactory.getLogger(XlsConverter.class);

    private MarkdownBuilder mb;

    @Override
    public ConversionResult convert(Path filePath, ConversionOptions options) throws ConversionException {
        requireNonNull(filePath, "文件路径不能为空");
        requireNonNull(options, "转换选项不能为空");

        logger.info("正在转换 XLS 文件: {}", filePath);
        mb = new MarkdownBuilder(new MarkdownConfig());

        try (FileInputStream fis = new FileInputStream(filePath.toFile());
             HSSFWorkbook workbook = new HSSFWorkbook(fis)) {

            Map<String, Object> metadata = extractMetadata(workbook, options);

            // 文件基本信息
            if (options.isIncludeMetadata()) {
                metadata.put("文件名", filePath.getFileName().toString());
                metadata.put("文件大小", filePath.toFile().length());
            }

            // 将工作簿转换为 Markdown 格式
            String markdownContent = convertToMarkdown(workbook, metadata, options);

            List<String> warnings = new ArrayList<>();

            return new ConversionResult(markdownContent, metadata, warnings,
                    filePath.toFile().length(), filePath.getFileName().toString());

        } catch (IOException e) {
            String errorMessage = "处理 XLS 文件失败: " + e.getMessage();
            logger.error(errorMessage, e);
            throw new ConversionException(errorMessage, e, filePath.getFileName().toString(), getName());
        }
    }

    @Override
    public boolean supports(String mimeType) {
        return "application/vnd.ms-excel".equals(mimeType);
    }

    @Override
    public int getPriority() {
        return 100;
    }

    @Override
    public String getName() {
        return "XlsConverter";
    }

    /**
     * 从 Excel 工作簿中提取元数据信息
     */
    private Map<String, Object> extractMetadata(HSSFWorkbook workbook, ConversionOptions options) {
        Map<String, Object> metadata = new HashMap<>();

        if (options.isIncludeMetadata()) {
            // 工作簿统计信息
            metadata.put("工作表数量", workbook.getNumberOfSheets());
            metadata.put("当前工作表索引(0为起始索引)", workbook.getActiveSheetIndex());
            metadata.put("转换时刻", LocalDateTime.now());

            // 计算总单元格数量
            int totalCells = 0;
            for (int i = 0; i < workbook.getNumberOfSheets(); i++) {
                Sheet sheet = workbook.getSheetAt(i);
                totalCells += estimateSheetSize(sheet);
            }
            metadata.put("统计单元格数量", totalCells);
        }

        return metadata;
    }

    /**
     * 估算工作表中的单元格数量
     */
    private int estimateSheetSize(Sheet sheet) {
        int cellCount = 0;
        for (Row row : sheet) {
            cellCount += row.getPhysicalNumberOfCells();
        }
        return cellCount;
    }

    /**
     * 将 Excel 工作簿转换为 Markdown 格式内容
     */
    private String convertToMarkdown(HSSFWorkbook workbook, Map<String, Object> metadata, ConversionOptions options) {
        if (options.isIncludeMetadata() && !metadata.isEmpty()) {
            mb.header(metadata);
        }

        // 处理所有工作表
        for (int i = 0; i < workbook.getNumberOfSheets(); i++) {
            Sheet sheet = workbook.getSheetAt(i);
            processSheet(sheet, i + 1, options);
        }

        return mb.flush();
    }

    /**
     * 处理单个工作表并将其转换为 Markdown 格式
     */
    private void processSheet(Sheet sheet, int sheetNum, ConversionOptions options) {
        String sheetName = sheet.getSheetName();
        mb.append(mb.h2("工作表 " + sheetNum + ": " + sheetName));

        if (!options.isIncludeTables()) {
            mb.append(mb.italic("表格功能在转换选项中被禁用"));
            mb.newline(2);
            return;
        }

        // 查找数据范围
        int firstRow = sheet.getFirstRowNum();
        int lastRow = sheet.getLastRowNum();

        if (firstRow < 0 || lastRow < 0 || lastRow < firstRow) {
            mb.append(mb.italic("空工作表"));
            mb.newline(2);
            mb.horizontalRule();
            return;
        }

        // 判断第一行是否可能是表头
        boolean hasHeader = detectHeaderRow(sheet, firstRow);

        // 处理数据
        if (hasHeader) {
            processTableWithHeader(sheet, firstRow, lastRow);
        } else {
            processTableWithoutHeader(sheet, firstRow, lastRow);
        }

        mb.horizontalRule();
    }

    /**
     * 检测第一行是否可能是表头行
     */
    private boolean detectHeaderRow(Sheet sheet, int firstRow) {
        Row firstRowData = sheet.getRow(firstRow);
        if (firstRowData == null) {
            return false;
        }

        int nonEmptyCells = 0;
        int stringCells = 0;
        int totalCells = firstRowData.getPhysicalNumberOfCells();

        for (Cell cell : firstRowData) {
            if (cell != null && cell.getCellType() != CellType.BLANK) {
                nonEmptyCells++;
                if (cell.getCellType() == CellType.STRING) {
                    String value = cell.getStringCellValue().trim();
                    if (!value.isEmpty()) {
                        stringCells++;
                    }
                }
            }
        }

        return totalCells > 0 && (double) nonEmptyCells / totalCells > 0.7 && (double) stringCells / totalCells > 0.5;
    }

    /**
     * 处理带表头行的表格
     */
    private void processTableWithHeader(Sheet sheet, int firstRow, int lastRow) {
        List<String> headers = new ArrayList<>();
        Row headRow = sheet.getRow(firstRow);
        if (headRow != null) {
            for (Cell cell : headRow) {
                headers.add(getCellValueAsString(cell).trim());
            }
        }

        List<List<String>> data = new ArrayList<>();
        for (int i = firstRow + 1; i <= lastRow; i++) {
            Row row = sheet.getRow(i);
            if (row != null) {
                List<String> rowData = new ArrayList<>();
                for (Cell cell : row) {
                    rowData.add(getCellValueAsString(cell).trim());
                }
                data.add(rowData);
            }
        }

        String[][] table = data.stream()
                .map(list -> list.toArray(new String[0]))
                .toArray(String[][]::new);
        mb.append(mb.table(headers.toArray(new String[0]), table));
    }

    /**
     * 处理不带表头行的表格
     */
    private void processTableWithoutHeader(Sheet sheet, int firstRow, int lastRow) {
        Row firstRowData = sheet.getRow(firstRow);
        int numCols = firstRowData != null ? firstRowData.getPhysicalNumberOfCells() : 1;

        List<String> headers = new ArrayList<>();
        for (int i = 0; i < numCols; i++) {
            headers.add("Column " + (i + 1));
        }

        List<List<String>> data = new ArrayList<>();
        for (int i = firstRow; i <= lastRow; i++) {
            Row row = sheet.getRow(i);
            if (row != null) {
                List<String> rowData = new ArrayList<>();
                for (Cell cell : row) {
                    rowData.add(getCellValueAsString(cell).trim());
                }
                data.add(rowData);
            }
        }

        String[][] table = data.stream()
                .map(list -> list.toArray(new String[0]))
                .toArray(String[][]::new);
        mb.append(mb.table(headers.toArray(new String[0]), table));
    }

    /**
     * 将单元格值转换为字符串表示
     */
    private String getCellValueAsString(Cell cell) {
        if (cell == null || cell.getCellType() == CellType.BLANK) {
            return "";
        }

        switch (cell.getCellType()) {
            case STRING:
                return cell.getStringCellValue();
            case NUMERIC:
                if (DateUtil.isCellDateFormatted(cell)) {
                    return cell.getDateCellValue().toString();
                } else {
                    double numValue = cell.getNumericCellValue();
                    if (numValue == (long) numValue) {
                        return String.format("%d", (long) numValue);
                    } else {
                        return String.format("%s", numValue);
                    }
                }
            case BOOLEAN:
                return Boolean.toString(cell.getBooleanCellValue());
            case FORMULA:
                try {
                    CellValue evaluatedValue = cell.getSheet().getWorkbook().getCreationHelper()
                            .createFormulaEvaluator().evaluate(cell);
                    if (evaluatedValue != null) {
                        switch (evaluatedValue.getCellType()) {
                            case STRING:
                                return evaluatedValue.getStringValue();
                            case NUMERIC:
                                double numValue = evaluatedValue.getNumberValue();
                                if (numValue == (long) numValue) {
                                    return String.format("%d", (long) numValue);
                                } else {
                                    return String.format("%s", numValue);
                                }
                            case BOOLEAN:
                                return Boolean.toString(evaluatedValue.getBooleanValue());
                            default:
                                return "";
                        }
                    }
                } catch (Exception e) {
                    return cell.getCellFormula();
                }
                return cell.getCellFormula();
            default:
                return "";
        }
    }
}
