package com.markitdown.converters;

import com.markdown.engine.MarkdownBuilder;
import com.markdown.engine.config.MarkdownConfig;
import com.markitdown.api.ConversionResult;
import com.markitdown.api.DocumentConverter;
import com.markitdown.config.ConversionOptions;
import com.markitdown.exceptions.ConversionException;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.CellValue;
import org.apache.poi.ss.usermodel.DateUtil;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
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
 * Converts legacy XLS workbooks into Markdown tables.
 */
public class XlsConverter implements DocumentConverter {

    private static final Logger logger = LoggerFactory.getLogger(XlsConverter.class);

    private MarkdownBuilder markdown;

    @Override
    public ConversionResult convert(Path filePath, ConversionOptions options) throws ConversionException {
        requireNonNull(filePath, "File path cannot be null");
        requireNonNull(options, "Conversion options cannot be null");

        logger.info("Converting XLS file: {}", filePath);
        markdown = new MarkdownBuilder(new MarkdownConfig());

        try (FileInputStream input = new FileInputStream(filePath.toFile());
             HSSFWorkbook workbook = new HSSFWorkbook(input)) {

            Map<String, Object> metadata = extractMetadata(workbook, options);
            if (options.content().includeMetadata()) {
                metadata.put("File Name", filePath.getFileName().toString());
                metadata.put("File Size", filePath.toFile().length());
            }

            String markdownContent = convertToMarkdown(workbook, metadata, options);
            return new ConversionResult(
                    markdownContent,
                    metadata,
                    new ArrayList<>(),
                    filePath.toFile().length(),
                    filePath.getFileName().toString()
            );
        } catch (IOException e) {
            String errorMessage = "Failed to process XLS file: " + e.getMessage();
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

    private Map<String, Object> extractMetadata(HSSFWorkbook workbook, ConversionOptions options) {
        Map<String, Object> metadata = new HashMap<>();
        if (options.content().includeMetadata()) {
            metadata.put("Sheet Count", workbook.getNumberOfSheets());
            metadata.put("Active Sheet Index", workbook.getActiveSheetIndex());
            metadata.put("Converted At", LocalDateTime.now());

            int totalCells = 0;
            for (int sheetIndex = 0; sheetIndex < workbook.getNumberOfSheets(); sheetIndex++) {
                totalCells += estimateSheetSize(workbook.getSheetAt(sheetIndex));
            }
            metadata.put("Estimated Cell Count", totalCells);
        }
        return metadata;
    }

    private int estimateSheetSize(Sheet sheet) {
        int cellCount = 0;
        for (Row row : sheet) {
            cellCount += row.getPhysicalNumberOfCells();
        }
        return cellCount;
    }

    private String convertToMarkdown(HSSFWorkbook workbook, Map<String, Object> metadata, ConversionOptions options) {
        if (options.content().includeMetadata() && !metadata.isEmpty()) {
            markdown.header(metadata);
        }

        for (int sheetIndex = 0; sheetIndex < workbook.getNumberOfSheets(); sheetIndex++) {
            processSheet(workbook.getSheetAt(sheetIndex), sheetIndex + 1, options);
        }

        return markdown.flush();
    }

    private void processSheet(Sheet sheet, int sheetNumber, ConversionOptions options) {
        markdown.append(markdown.h2("Sheet " + sheetNumber + ": " + sheet.getSheetName()));

        if (!options.content().includeTables()) {
            markdown.append(markdown.italic("Table output is disabled in the current conversion options."));
            markdown.newline(2);
            return;
        }

        int firstRow = sheet.getFirstRowNum();
        int lastRow = sheet.getLastRowNum();
        if (firstRow < 0 || lastRow < 0 || lastRow < firstRow) {
            markdown.append(markdown.italic("Empty sheet"));
            markdown.newline(2);
            markdown.horizontalRule();
            return;
        }

        if (detectHeaderRow(sheet, firstRow)) {
            processTableWithHeader(sheet, firstRow, lastRow);
        } else {
            processTableWithoutHeader(sheet, firstRow, lastRow);
        }

        markdown.horizontalRule();
    }

    private boolean detectHeaderRow(Sheet sheet, int firstRow) {
        Row headerRow = sheet.getRow(firstRow);
        if (headerRow == null) {
            return false;
        }

        int nonEmptyCells = 0;
        int stringCells = 0;
        int totalCells = headerRow.getPhysicalNumberOfCells();
        for (Cell cell : headerRow) {
            if (cell != null && cell.getCellType() != CellType.BLANK) {
                nonEmptyCells++;
                if (cell.getCellType() == CellType.STRING && !cell.getStringCellValue().trim().isEmpty()) {
                    stringCells++;
                }
            }
        }

        return totalCells > 0
                && (double) nonEmptyCells / totalCells > 0.7
                && (double) stringCells / totalCells > 0.5;
    }

    private void processTableWithHeader(Sheet sheet, int firstRow, int lastRow) {
        Row headerRow = sheet.getRow(firstRow);
        if (headerRow == null) {
            markdown.append(markdown.italic("Empty sheet"));
            markdown.newline(2);
            return;
        }

        List<String> headers = new ArrayList<>();
        for (Cell cell : headerRow) {
            headers.add(getCellValueAsString(cell).trim());
        }

        List<List<String>> rows = new ArrayList<>();
        for (int rowIndex = firstRow + 1; rowIndex <= lastRow; rowIndex++) {
            Row row = sheet.getRow(rowIndex);
            if (row == null) {
                continue;
            }

            List<String> rowValues = new ArrayList<>();
            for (int cellIndex = 0; cellIndex < headers.size(); cellIndex++) {
                Cell cell = row.getCell(cellIndex, Row.MissingCellPolicy.RETURN_BLANK_AS_NULL);
                rowValues.add(getCellValueAsString(cell).trim());
            }

            if (!isEffectivelyEmpty(rowValues)) {
                rows.add(rowValues);
            }
        }

        String[][] table = rows.stream()
                .map(values -> values.toArray(new String[0]))
                .toArray(String[][]::new);
        markdown.append(markdown.table(headers.toArray(new String[0]), table));
    }

    private void processTableWithoutHeader(Sheet sheet, int firstRow, int lastRow) {
        Row firstDataRow = sheet.getRow(firstRow);
        if (firstDataRow == null) {
            markdown.append(markdown.italic("Empty sheet"));
            markdown.newline(2);
            return;
        }

        int columnCount = Math.max(1, firstDataRow.getPhysicalNumberOfCells());
        List<String> headers = new ArrayList<>();
        for (int columnIndex = 0; columnIndex < columnCount; columnIndex++) {
            headers.add("Column " + (columnIndex + 1));
        }

        List<List<String>> rows = new ArrayList<>();
        for (int rowIndex = firstRow; rowIndex <= lastRow; rowIndex++) {
            Row row = sheet.getRow(rowIndex);
            if (row == null) {
                continue;
            }

            List<String> rowValues = new ArrayList<>();
            for (int cellIndex = 0; cellIndex < columnCount; cellIndex++) {
                Cell cell = row.getCell(cellIndex, Row.MissingCellPolicy.RETURN_BLANK_AS_NULL);
                rowValues.add(getCellValueAsString(cell).trim());
            }

            if (!isEffectivelyEmpty(rowValues)) {
                rows.add(rowValues);
            }
        }

        String[][] table = rows.stream()
                .map(values -> values.toArray(new String[0]))
                .toArray(String[][]::new);
        markdown.append(markdown.table(headers.toArray(new String[0]), table));
    }

    private boolean isEffectivelyEmpty(List<String> rowValues) {
        for (String value : rowValues) {
            if (value != null && !value.isBlank()) {
                return false;
            }
        }
        return true;
    }

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
                }
                double numericValue = cell.getNumericCellValue();
                if (numericValue == (long) numericValue) {
                    return String.format("%d", (long) numericValue);
                }
                return String.format("%s", numericValue);
            case BOOLEAN:
                return Boolean.toString(cell.getBooleanCellValue());
            case FORMULA:
                try {
                    CellValue evaluatedValue = cell.getSheet().getWorkbook()
                            .getCreationHelper()
                            .createFormulaEvaluator()
                            .evaluate(cell);
                    if (evaluatedValue != null) {
                        switch (evaluatedValue.getCellType()) {
                            case STRING:
                                return evaluatedValue.getStringValue();
                            case NUMERIC:
                                if (DateUtil.isCellDateFormatted(cell)) {
                                    return cell.getDateCellValue().toString();
                                }
                                double evaluatedNumber = evaluatedValue.getNumberValue();
                                if (evaluatedNumber == (long) evaluatedNumber) {
                                    return String.format("%d", (long) evaluatedNumber);
                                }
                                return String.format("%s", evaluatedNumber);
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
