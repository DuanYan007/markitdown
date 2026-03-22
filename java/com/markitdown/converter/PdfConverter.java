package com.markitdown.converter;

import com.markitdown.api.ConversionResult;
import com.markitdown.api.DocumentConverter;
import com.markitdown.config.ConversionOptions;
import com.markitdown.exception.ConversionException;
import com.markitdown.ocr.OcrEngine;
import com.markitdown.ocr.TesseractOcrEngine;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.rendering.PDFRenderer;
import org.apache.pdfbox.rendering.ImageType;
import org.apache.pdfbox.text.PDFTextStripper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.*;

import javax.imageio.ImageIO;

import static java.util.Objects.requireNonNull;

/**
 * @class PdfConverter
 * @brief PDF文档转换器，使用PDFBox提取文本内容
 */
public class PdfConverter implements DocumentConverter {

    private static final Logger logger = LoggerFactory.getLogger(PdfConverter.class);

    @Override
    public ConversionResult convert(Path filePath, ConversionOptions options) throws ConversionException {
        requireNonNull(filePath, "文件路径不能为空");
        requireNonNull(options, "转换选项不能为空");

        logger.info("正在转换PDF文件: {}", filePath);

        try {
            // 加载PDF文档
            File pdfFile = filePath.toFile();

            // 检查文件是否是有效的PDF
            if (!isValidPDF(pdfFile)) {
                throw new ConversionException("不是有效的PDF文件", filePath.getFileName().toString(), getName());
            }

            // 使用PDFBox提取文本
            String textContent = extractTextWithPDFBox(pdfFile, options);

            // 处理元数据
            Map<String, Object> metadata = extractMetadata(pdfFile, options);

            // 转换为Markdown
            String markdownContent = convertToMarkdown(textContent, metadata, options);

            List<String> warnings = new ArrayList<>();

            return new ConversionResult(markdownContent, metadata, warnings,
                    filePath.toFile().length(), filePath.getFileName().toString());

        } catch (IOException e) {
            String errorMessage = "Failed to process PDF file: " + e.getMessage();
            logger.error(errorMessage, e);
            throw new ConversionException(errorMessage, e, filePath.getFileName().toString(), getName());
        }
    }

    /**
     * 验证PDF文件有效性
     */
    private boolean isValidPDF(File pdfFile) throws IOException {
        try (java.io.RandomAccessFile raf = new java.io.RandomAccessFile(pdfFile, "r")) {
            String header = raf.readLine();
            return header != null && header.contains("%PDF");
        }
    }

    /**
     * 使用PDFBox提取文本
     */
    private String extractTextWithPDFBox(File pdfFile, ConversionOptions options) throws IOException {
        try {
            // 尝试初始化PDFTextStripper以检测GlyphList问题
            PDFTextStripper testStripper = new PDFTextStripper();
        } catch (ExceptionInInitializerError | NoClassDefFoundError | RuntimeException e) {
            // PDFBox GlyphList资源缺失，使用备用方法
            logger.warn("PDFBox GlyphList资源不可用，使用备用文本提取方法: {}", e.getMessage());
            return extractTextFallback(pdfFile);
        }

        // 获取密码
        String password = (String) options.getCustomOption("pdfPassword");

        try (PDDocument document = password != null && !password.isEmpty()
                ? PDDocument.load(pdfFile, password)
                : PDDocument.load(pdfFile)) {
            PDFTextStripper textStripper = new PDFTextStripper();

            // 设置文本提取选项
            textStripper.setSortByPosition(true);
            textStripper.setLineSeparator("\n");

            // 内存优化：分页处理大文件
            String text;
            int pageCount = document.getNumberOfPages();
            if (pageCount > 100 && options.getMaxFileSize() == 0) {
                // 大文件分页处理以减少内存占用
                text = extractTextInPages(document, textStripper, pageCount);
            } else {
                // 普通文件一次性提取
                text = textStripper.getText(document);
            }

            // 如果文本为空且启用了OCR,则对扫描PDF进行OCR识别
            if ((text == null || text.trim().isEmpty()) && options.isUseOcr()) {
                logger.info("PDF文本为空,启用OCR进行扫描页面识别");
                return extractTextFromScannedPdf(document, options);
            }

            if (text == null || text.trim().isEmpty()) {
                return "*无法提取PDF文本内容。这可能是因为：*\n\n" +
                       "1. PDF是扫描版图片格式\n" +
                       "2. PDF使用了特殊编码\n" +
                       "3. PDF文件损坏\n\n" +
                       "*建议：使用 --ocr 选项对扫描PDF进行OCR识别*";
            }

            return text;
        } catch (Exception e) {
            logger.warn("使用PDFBox提取文本失败: {}", e.getMessage());
            return extractTextFallback(pdfFile);
        }
    }

    /**
     * 对扫描PDF进行OCR识别
     */
    private String extractTextFromScannedPdf(PDDocument document, ConversionOptions options) throws IOException {
        try {
            logger.info("开始对扫描PDF进行OCR识别,共{}页", document.getNumberOfPages());

            StringBuilder ocrText = new StringBuilder();
            PDFRenderer renderer = new PDFRenderer(document);

            // 创建OCR引擎，使用配置文件中的路径
            String tessdataPath = (String) options.getCustomOption("tessdataPath");
            OcrEngine ocrEngine = tessdataPath != null ? new TesseractOcrEngine(tessdataPath) : new TesseractOcrEngine();

            for (int pageNum = 0; pageNum < document.getNumberOfPages(); pageNum++) {
                logger.info("正在OCR识别第{}页...", pageNum + 1);

                // 将PDF页面渲染为图像
                BufferedImage image = renderer.renderImageWithDPI(pageNum, 300, ImageType.RGB);

                // 保存临时图像文件用于OCR
                File tempImage = File.createTempFile("pdf_page_", ".png");
                try {
                    javax.imageio.ImageIO.write(image, "png", tempImage);

                    // 执行OCR识别
                    String pageText = ocrEngine.extractText(tempImage);

                    // 立即清理每个页面的OCR结果
                    pageText = cleanupSinglePageText(pageText);

                    if (pageNum > 0) {
                        ocrText.append("\n\n");
                    }
                    ocrText.append("### 第").append(pageNum + 1).append("页\n\n");
                    ocrText.append(pageText);

                    logger.info("第{}页OCR完成,提取{}字符", pageNum + 1, pageText.length());

                } finally {
                    // 清理临时文件
                    if (tempImage.exists()) {
                        tempImage.delete();
                    }
                }
            }

            String result = ocrText.toString();

            if (result.trim().isEmpty()) {
                return "*OCR识别未提取到文本内容。可能原因:\n\n" +
                       "1. PDF页面质量过低\n" +
                       "2. 图像分辨率不足\n" +
                       "3. 语言包不匹配\n\n" +
                       "*建议: 检查PDF质量或使用更高DPI设置*";
            }

            return result;

        } catch (Exception e) {
            logger.error("扫描PDF OCR处理失败: {}", e.getMessage(), e);
            return "*OCR处理失败: " + e.getMessage() + "\n\n" +
                   "*建议: 确保Tesseract正确安装并配置了中文语言包*";
        }
    }

    /**
     * 分页提取PDF文本以减少内存占用
     */
    private String extractTextInPages(PDDocument document, PDFTextStripper textStripper, int pageCount) {
        StringBuilder result = new StringBuilder();

        try {
            // 每次处理20页，平衡性能和内存
            int batchSize = 20;
            for (int startPage = 0; startPage < pageCount; startPage += batchSize) {
                int endPage = Math.min(startPage + batchSize, pageCount);

                // 设置提取范围（页码从1开始）
                textStripper.setStartPage(startPage + 1);
                textStripper.setEndPage(endPage);

                String pageText = textStripper.getText(document);
                if (pageText != null && !pageText.trim().isEmpty()) {
                    result.append(pageText);
                }

                // 每50页清理一次内存
                if (startPage % 50 == 0) {
                    System.gc();
                }
            }
        } catch (IOException e) {
            logger.warn("分页提取失败，回退到一次性提取: {}", e.getMessage());
            try {
                return textStripper.getText(document);
            } catch (IOException ex) {
                return "";
            }
        }

        return result.toString();
    }

    /**
     * 备用文本提取方法 - 从PDF中提取原始文本字符串
     */
    private String extractTextFallback(File pdfFile) {
        try {
            // 读取PDF文件内容
            byte[] fileBytes = java.nio.file.Files.readAllBytes(pdfFile.toPath());
            String content = new String(fileBytes, java.nio.charset.StandardCharsets.ISO_8859_1);

            // 提取文本字符串（在PDF中，文本在括号()之间）
            StringBuilder text = new StringBuilder();
            boolean inText = false;
            boolean inStream = false;
            StringBuilder currentText = new StringBuilder();

            for (int i = 0; i < content.length() - 1; i++) {
                char c = content.charAt(i);
                char next = content.charAt(i + 1);

                // 跳过流对象
                if (c == 's' && next == 't' && i + 5 < content.length() &&
                    content.substring(i, i + 6).equals("stream")) {
                    inStream = true;
                    i += 6;
                    continue;
                }
                if (inStream && c == 'e' && next == 'n' && i + 4 < content.length() &&
                    content.substring(i, i + 4).equals("endstream")) {
                    inStream = false;
                    i += 9;
                    continue;
                }

                if (inStream) continue;

                // 查找文本字符串开始
                if (c == '(' && next != ')') {
                    inText = true;
                    currentText.setLength(0);
                    continue;
                }

                // 查找文本字符串结束
                if (c == ')' && inText) {
                    inText = false;
                    String decoded = decodePDFString(currentText.toString());
                    if (decoded.length() > 3 && isMostlyPrintable(decoded)) {
                        text.append(decoded).append(" ");
                    }
                    continue;
                }

                // 收集文本字符
                if (inText) {
                    currentText.append(c);
                }
            }

            if (text.length() == 0) {
                return "*无法提取PDF文本内容。这可能是因为：*\n\n" +
                       "1. PDF是扫描版图片格式\n" +
                       "2. PDF使用了特殊编码\n" +
                       "3. PDF文件损坏\n\n" +
                       "*建议：尝试使用OCR工具处理扫描版PDF*";
            }

            return formatExtractedText(text.toString());

        } catch (Exception e) {
            logger.error("备用文本提取失败: {}", e.getMessage());
            return "*PDF文本提取功能当前不可用。请确保PDF文件不是扫描版图片格式。*";
        }
    }

    /**
     * 解码PDF字符串（处理转义序列）
     */
    private String decodePDFString(String str) {
        StringBuilder decoded = new StringBuilder();

        for (int i = 0; i < str.length(); i++) {
            char c = str.charAt(i);

            if (c == '\\' && i + 1 < str.length()) {
                char next = str.charAt(i + 1);

                // 处理转义字符
                switch (next) {
                    case 'n':
                        decoded.append('\n');
                        i++;
                        break;
                    case 'r':
                        decoded.append('\r');
                        i++;
                        break;
                    case 't':
                        decoded.append('\t');
                        i++;
                        break;
                    case 'b':
                        decoded.append('\b');
                        i++;
                        break;
                    case 'f':
                        decoded.append('\f');
                        i++;
                        break;
                    case '(':
                        decoded.append('(');
                        i++;
                        break;
                    case ')':
                        decoded.append(')');
                        i++;
                        break;
                    case '\\':
                        decoded.append('\\');
                        i++;
                        break;
                    default:
                        // 处理八进制转义 \ddd
                        if (i + 3 < str.length() && Character.isDigit(next)) {
                            String octal = str.substring(i + 1, Math.min(i + 4, str.length()));
                            try {
                                int code = Integer.parseInt(octal, 8);
                                if (code > 0 && code < 256) {
                                    decoded.append((char) code);
                                    i += octal.length();
                                } else {
                                    decoded.append(c);
                                }
                            } catch (NumberFormatException e) {
                                decoded.append(c);
                            }
                        } else {
                            decoded.append(c);
                        }
                        break;
                }
            } else {
                decoded.append(c);
            }
        }

        return decoded.toString();
    }

    /**
     * 检查字符串是否主要是可打印字符
     */
    private boolean isMostlyPrintable(String str) {
        if (str.length() == 0) return false;

        int printableCount = 0;
        for (char c : str.toCharArray()) {
            if ((c >= 32 && c <= 126) || (c >= 160 && c <= 255) || c == '\n' || c == '\t') {
                printableCount++;
            }
        }

        return printableCount > str.length() * 0.6;
    }

    /**
     * 格式化提取的文本
     */
    private String formatExtractedText(String text) {
        // 清理多余空格
        text = text.replaceAll("\\s+", " ");

        // 添加换行符以提高可读性
        text = text.replaceAll("\\.\\s+", ".\n\n");
        text = text.replaceAll("!\\s+", "!\n\n");
        text = text.replaceAll("\\?\\s+", "?\n\n");

        return text.trim();
    }

    /**
     * 提取元数据
     */
    private Map<String, Object> extractMetadata(File pdfFile, ConversionOptions options) {
        Map<String, Object> metadata = new HashMap<>();

        if (options.isIncludeMetadata()) {
            metadata.put("文件名", pdfFile.getName());
            metadata.put("文件大小", pdfFile.length());
            metadata.put("转换时刻", LocalDateTime.now());

            // 获取密码
            String password = (String) options.getCustomOption("pdfPassword");

            try (PDDocument document = password != null && !password.isEmpty()
                    ? PDDocument.load(pdfFile, password)
                    : PDDocument.load(pdfFile)) {
                metadata.put("页数", document.getNumberOfPages());

                // 提取文档信息
                if (document.getDocumentInformation() != null) {
                    String title = document.getDocumentInformation().getTitle();
                    String author = document.getDocumentInformation().getAuthor();
                    String subject = document.getDocumentInformation().getSubject();
                    String keywords = document.getDocumentInformation().getKeywords();
                    String creator = document.getDocumentInformation().getCreator();
                    String producer = document.getDocumentInformation().getProducer();

                    if (title != null && !title.isEmpty()) metadata.put("标题", title);
                    if (author != null && !author.isEmpty()) metadata.put("作者", author);
                    if (subject != null && !subject.isEmpty()) metadata.put("主题", subject);
                    if (keywords != null && !keywords.isEmpty()) metadata.put("关键词", keywords);
                    if (creator != null && !creator.isEmpty()) metadata.put("创建工具", creator);
                    if (producer != null && !producer.isEmpty()) metadata.put("PDF生成器", producer);
                }
            } catch (Exception e) {
                logger.warn("无法读取PDF元数据: {}", e.getMessage());
            }
        }

        return metadata;
    }

    /**
     * 转换为Markdown格式
     */
    private String convertToMarkdown(String textContent, Map<String, Object> metadata, ConversionOptions options) {
        StringBuilder markdown = new StringBuilder();

        // 添加元数据部分（如果启用）
        if (options.isIncludeMetadata() && !metadata.isEmpty()) {
            markdown.append("## Document Information\n\n");
            for (Map.Entry<String, Object> entry : metadata.entrySet()) {
                if (entry.getValue() != null) {
                    markdown.append("- **").append(formatMetadataKey(entry.getKey()))
                            .append(":** ").append(entry.getValue()).append("\n");
                }
            }
            markdown.append("\n");
        }

        // 添加主要内容
        markdown.append("## Content\n\n");

        if (textContent != null && !textContent.trim().isEmpty()) {
            markdown.append(formatTextContent(textContent));
        } else {
            markdown.append("*无法提取PDF文本内容。这可能是因为：*\n\n");
            markdown.append("1. PDF是扫描版图片格式\n");
            markdown.append("2. PDF使用了特殊编码\n");
            markdown.append("3. PDF文件损坏\n\n");
            markdown.append("*建议：尝试使用OCR工具处理扫描版PDF*");
        }

        return markdown.toString();
    }

    /**
     * 格式化文本内容
     */
    private String formatTextContent(String textContent) {
        if (textContent == null || textContent.trim().isEmpty()) {
            return "";
        }

        // 清理文本
        String cleaned = cleanupText(textContent);

        StringBuilder formatted = new StringBuilder();

        // 按行处理
        String[] lines = cleaned.split("\n");

        for (int i = 0; i < lines.length; i++) {
            String line = lines[i].trim();

            if (line.isEmpty()) {
                // 添加段落分隔
                formatted.append("\n\n");
                continue;
            }

            // 检查是否可能是标题
            if (isHeadingLine(line)) {
                // 在标题前添加额外间距
                if (formatted.length() > 0 && !formatted.toString().endsWith("\n\n\n")) {
                    formatted.append("\n");
                }
                formatted.append("### ").append(line).append("\n\n");
            }
            // 检查是否是列表项
            else if (isListItem(line)) {
                formatted.append(line).append("\n");
            }
            // 普通段落文本
            else {
                // 如果下一行存在且不为空，这可能是续行
                if (i + 1 < lines.length && !lines[i + 1].trim().isEmpty() &&
                    !isHeadingLine(lines[i + 1].trim()) && !isListItem(lines[i + 1].trim())) {
                    // 继续当前段落
                    formatted.append(line).append(" ");
                } else {
                    // 段落结束
                    formatted.append(line).append("\n\n");
                }
            }
        }

        // 清理多余的换行符
        String result = formatted.toString();
        result = result.replaceAll("\\n{3,}", "\n\n");

        return result.trim();
    }

    /**
     * 清理文本
     */
    private String cleanupText(String text) {
        if (text == null || text.trim().isEmpty()) {
            return "";
        }

        // 修复常见的PDF提取问题
        String cleaned = text.replaceAll("-\\s+", "-"); // 修复连字符
        cleaned = cleaned.replaceAll("\\s*\\f\\s*", "\n\n"); // 换页符转换为段落分隔
        cleaned = cleaned.replaceAll("\\r\\n", "\n"); // 标准化Windows换行符
        cleaned = cleaned.replaceAll("\\r", "\n"); // 标准化Mac换行符

        // 处理连续的空行（超过2个换行符）
        cleaned = cleaned.replaceAll("\\n{3,}", "\n\n");

        return cleaned.trim();
    }

    /**
     * 判断是否是标题行
     */
    private boolean isHeadingLine(String line) {
        if (line == null || line.trim().isEmpty()) {
            return false;
        }

        // 编号标题，如"1. Introduction"
        if (line.matches("^\\d+\\.\\s+.*")) {
            return true;
        }

        // 全大写标题（短于80字符）
        if (line.length() < 80 && line.equals(line.toUpperCase()) &&
            line.matches(".*[A-Z].*") && !line.matches(".*[a-z].*")) {
            return true;
        }

        // 标题大小写（首字母大写，相对较短）
        if (line.length() < 100 && Character.isUpperCase(line.charAt(0)) &&
            !line.matches(".*\\.$") && !line.contains(",")) {
            return true;
        }

        return false;
    }

    /**
     * 判断是否是列表项
     */
    private boolean isListItem(String line) {
        if (line == null || line.trim().isEmpty()) {
            return false;
        }

        // 项目符号列表
        if (line.matches("^\\s*[-•*]\\s+.*")) {
            return true;
        }

        // 编号列表
        if (line.matches("^\\s*\\d+[.)]\\s+.*")) {
            return true;
        }

        return false;
    }

    /**
     * 格式化元数据键
     */
    private String formatMetadataKey(String key) {
        // 将camelCase转换为Title Case
        return key.replaceAll("([a-z])([A-Z])", "$1 $2")
                .replaceAll("^([a-z])", String.valueOf(Character.toUpperCase(key.charAt(0))))
                .toLowerCase();
    }

    @Override
    public boolean supports(String mimeType) {
        return "application/pdf".equals(mimeType);
    }

    @Override
    public int getPriority() {
        return 100;
    }

    @Override
    public String getName() {
        return "PdfConverter";
    }

    /**
     * 清理单个页面的OCR文本
     */
    private String cleanupSinglePageText(String pageText) {
        if (pageText == null || pageText.trim().isEmpty()) {
            return pageText;
        }

        // 按行分割并去重
        String[] lines = pageText.split("\\n");
        List<String> uniqueLines = new ArrayList<>();
        String lastLine = "";

        for (String line : lines) {
            String trimmed = line.trim();
            if (trimmed.isEmpty()) {
                continue;
            }

            // 检查是否与上一行高度相似
            if (!lastLine.isEmpty() && trimmed.length() > 8) {
                double similarity = calculateSimilarity(trimmed, lastLine);
                if (similarity > 0.80) {
                    continue; // 跳过重复行
                }
            }

            uniqueLines.add(line);
            lastLine = trimmed;
        }

        return String.join("\n", uniqueLines);
    }

    /**
     * 清理OCR识别的文本，去除重复和格式问题
     */
    private String cleanupOcrText(String ocrText) {
        if (ocrText == null || ocrText.trim().isEmpty()) {
            return ocrText;
        }

        // 先按页分割
        String[] pages = ocrText.split("### 第\\d+页");
        List<String> cleanedPages = new ArrayList<>();

        for (String page : pages) {
            if (page.trim().isEmpty()) continue;

            // 按行处理
            String[] lines = page.split("\\n");
            List<String> cleanedLines = new ArrayList<>();
            String lastLine = "";

            for (String line : lines) {
                String trimmed = line.trim();
                if (trimmed.isEmpty()) {
                    continue; // 跳过空行
                }

                // 跳过页面标题行
                if (trimmed.startsWith("###")) {
                    cleanedLines.add(line);
                    continue;
                }

                // 检查是否与上一行高度相似（超过75%相似度就认为是重复）
                if (!lastLine.isEmpty() && trimmed.length() > 10) {
                    double similarity = calculateSimilarity(trimmed, lastLine);
                    if (similarity > 0.75) {
                        // 跳过高相似度的行
                        continue;
                    }
                }

                cleanedLines.add(line);
                lastLine = trimmed;
            }

            if (!cleanedLines.isEmpty()) {
                cleanedPages.add(String.join("\n", cleanedLines));
            }
        }

        // 重新组合页面，添加页面标题
        StringBuilder result = new StringBuilder();
        for (int i = 0; i < cleanedPages.size(); i++) {
            if (i > 0) {
                result.append("\n\n");
            }
            result.append("### 第").append(i + 1).append("页\n\n");
            result.append(cleanedPages.get(i));
        }

        return result.toString();
    }

    /**
     * 去除段落内的重复行
     */
    private String removeDuplicateLines(String paragraph) {
        String[] lines = paragraph.split("\\n");
        List<String> uniqueLines = new ArrayList<>();
        Set<String> seenLines = new HashSet<>();

        for (String line : lines) {
            String trimmed = line.trim();
            if (!trimmed.isEmpty() && !seenLines.contains(trimmed)) {
                uniqueLines.add(line);
                seenLines.add(trimmed);
            }
        }

        return String.join("\n", uniqueLines);
    }

    /**
     * 去除过度重复的句子
     */
    private String removeRepeatedSentences(String text) {
        if (text == null || text.isEmpty()) {
            return text;
        }

        // 按行分割，因为OCR通常按行识别
        String[] lines = text.split("\\n");
        List<String> result = new ArrayList<>();
        String lastLine = "";
        int repeatCount = 1;

        for (String line : lines) {
            String trimmed = line.trim();
            if (trimmed.isEmpty()) {
                result.add(line);
                continue;
            }

            // 检查是否与上一行相同或高度相似
            if (trimmed.length() > 10 && calculateSimilarity(trimmed, lastLine) > 0.85) {
                repeatCount++;
                // 只保留前2次重复
                if (repeatCount <= 2) {
                    result.add(line);
                }
            } else {
                result.add(line);
                lastLine = trimmed;
                repeatCount = 1;
            }
        }

        return String.join("\n", result);
    }

    /**
     * 计算两个字符串的相似度（简单版本）
     */
    private double calculateSimilarity(String s1, String s2) {
        if (s1 == null || s2 == null) return 0.0;
        if (s1.equals(s2)) return 1.0;

        int maxLen = Math.max(s1.length(), s2.length());
        if (maxLen == 0) return 0.0;

        // 简单的编辑距离近似
        return (maxLen - levenshteinDistance(s1, s2)) / (double) maxLen;
    }

    /**
     * 简单的编辑距离计算
     */
    private int levenshteinDistance(String s1, String s2) {
        int[][] dp = new int[s1.length() + 1][s2.length() + 1];

        for (int i = 0; i <= s1.length(); i++) {
            dp[i][0] = i;
        }
        for (int j = 0; j <= s2.length(); j++) {
            dp[0][j] = j;
        }

        for (int i = 1; i <= s1.length(); i++) {
            for (int j = 1; j <= s2.length(); j++) {
                int cost = s1.charAt(i - 1) == s2.charAt(j - 1) ? 0 : 1;
                dp[i][j] = Math.min(
                    Math.min(dp[i - 1][j] + 1, dp[i][j - 1] + 1),
                    dp[i - 1][j - 1] + cost
                );
            }
        }

        return dp[s1.length()][s2.length()];
    }
}
