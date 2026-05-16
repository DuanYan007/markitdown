package com.markitdown.converters;

import com.markitdown.api.ConversionResult;
import com.markitdown.api.DocumentConverter;
import com.markitdown.config.ConversionOptions;
import com.markitdown.exceptions.ConversionException;
import com.markitdown.ocr.OcrEngine;
import com.markitdown.ocr.OcrEngineFactory;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.rendering.PDFRenderer;
import org.apache.pdfbox.rendering.ImageType;
import org.apache.pdfbox.text.PDFTextStripper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import javax.imageio.ImageIO;

import static java.util.Objects.requireNonNull;

/**
 * Converter for PDF documents using PDFBox with OCR fallback support.
 */
public class PdfConverter implements DocumentConverter {

    private static final Logger logger = LoggerFactory.getLogger(PdfConverter.class);

    @Override
    public ConversionResult convert(Path filePath, ConversionOptions options) throws ConversionException {
        requireNonNull(filePath, "File path cannot be null");
        requireNonNull(options, "Conversion options cannot be null");
        configurePdfBoxFontCache();

            logger.info("Converting PDF file: {}", filePath);

        try {
            // Resolve the source PDF file.
            File pdfFile = filePath.toFile();

            // Validate that the file starts with a PDF header.
            if (!isValidPDF(pdfFile)) {
                throw new ConversionException("The input file is not a valid PDF document.", filePath.getFileName().toString(), getName());
            }

            // Extract text with PDFBox or the configured fallback path.
            String textContent = extractTextWithPDFBox(pdfFile, options);

            // Extract document metadata when enabled.
            Map<String, Object> metadata = extractMetadata(pdfFile, options);

            // Render the final Markdown document.
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
     * Validates that the file looks like a PDF.
     */
    private boolean isValidPDF(File pdfFile) throws IOException {
        try (java.io.RandomAccessFile raf = new java.io.RandomAccessFile(pdfFile, "r")) {
            String header = raf.readLine();
            return header != null && header.contains("%PDF");
        }
    }

    /**
     * Extracts text with PDFBox and falls back when necessary.
     */
    private String extractTextWithPDFBox(File pdfFile, ConversionOptions options) throws IOException {
        try {
            // Probe PDFTextStripper early to surface GlyphList initialization issues.
            PDFTextStripper testStripper = new PDFTextStripper();
        } catch (ExceptionInInitializerError | NoClassDefFoundError | RuntimeException e) {
            // Fall back when the bundled GlyphList resources are unavailable.
            logger.warn("PDFBox GlyphList resources are unavailable. Falling back to raw text extraction: {}", e.getMessage());
            return extractTextFallback(pdfFile);
        }

        // Load the optional PDF password from conversion options.
        String password = options.document().pdfPassword();

        try (PDDocument document = password != null && !password.isEmpty()
                ? PDDocument.load(pdfFile, password)
                : PDDocument.load(pdfFile)) {
            PDFTextStripper textStripper = new PDFTextStripper();

            // Configure text extraction behavior for readable output.
            textStripper.setSortByPosition(true);
            textStripper.setLineSeparator("\n");

            // Use paged extraction for very large documents to reduce memory pressure.
            String text;
            int pageCount = document.getNumberOfPages();
            if (pageCount > 100 && options.limits().maxFileSize() == 0) {
                // Process large documents in page batches.
                text = extractTextInPages(document, textStripper, pageCount);
            } else {
                // Extract smaller documents in a single pass.
                text = textStripper.getText(document);
            }

            // Run OCR when text extraction returns empty content and OCR is enabled.
            if ((text == null || text.trim().isEmpty()) && options.ocr().enabled()) {
                logger.info("PDF text is empty. Running OCR on scanned pages.");
                return extractTextFromScannedPdf(document, options);
            }

            if (text == null || text.trim().isEmpty()) {
                return "*Unable to extract PDF text content. This may happen because:*\n\n" +
                       "1. The PDF contains scanned images instead of embedded text\n" +
                       "2. The PDF uses unsupported text encoding\n" +
                       "3. The PDF file is damaged\n\n" +
                       "*Suggestion: enable `--ocr` to process scanned PDF pages.*";
            }

            return text;
        } catch (Exception e) {
            logger.warn("PDFBox text extraction failed: {}", e.getMessage());
            return extractTextFallback(pdfFile);
        }
    }

    /**
     * Runs OCR against rendered PDF pages.
     */
    private String extractTextFromScannedPdf(PDDocument document, ConversionOptions options) throws IOException {
        try {
            logger.info("Starting OCR for scanned PDF ({} pages)", document.getNumberOfPages());

            StringBuilder ocrText = new StringBuilder();
            PDFRenderer renderer = new PDFRenderer(document);

            // Create the OCR engine using the current runtime configuration.
            OcrEngine ocrEngine = OcrEngineFactory.create(options);
            if (!ocrEngine.isAvailable()) {
                return "*OCR engine is unavailable in the current build or environment.*\n\n" +
                        "*Suggestion: use the full build or configure another OCR engine before enabling --ocr.*";
            }

            for (int pageNum = 0; pageNum < document.getNumberOfPages(); pageNum++) {
                logger.info("Running OCR on page {}", pageNum + 1);

                // Render the current PDF page into an image.
                BufferedImage image = renderer.renderImageWithDPI(pageNum, 300, ImageType.RGB);

                // Persist a temporary page image for OCR processing.
                File tempImage = File.createTempFile("pdf_page_", ".png");
                try {
                    javax.imageio.ImageIO.write(image, "png", tempImage);

                    // Execute OCR on the rendered page image.
                    String pageText = ocrEngine.extractText(tempImage);

                    // Immediately clean each page-level OCR result.
                    pageText = cleanupSinglePageText(pageText);

                    if (pageNum > 0) {
                        ocrText.append("\n\n");
                    }
                    ocrText.append("### Page ").append(pageNum + 1).append("\n\n");
                    ocrText.append(pageText);

                    logger.info("OCR finished for page {} ({} characters)", pageNum + 1, pageText.length());

                } finally {
                    // Delete the temporary OCR image file.
                    if (tempImage.exists()) {
                        tempImage.delete();
                    }
                }
            }

            String result = ocrText.toString();

            if (result.trim().isEmpty()) {
                return "*OCR did not extract any text. Possible reasons:*\n\n" +
                       "1. The PDF page quality is too low\n" +
                       "2. The rendered image resolution is insufficient\n" +
                       "3. The selected OCR language pack does not match the document\n\n" +
                       "*Suggestion: check PDF quality or increase the OCR DPI setting.*";
            }

            return result;

        } catch (Exception e) {
            logger.error("Scanned PDF OCR failed: {}", e.getMessage(), e);
            return "*OCR processing failed:* " + e.getMessage() + "\n\n" +
                   "*Suggestion: verify the OCR engine configuration and required language packs.*";
        }
    }

    /**
     * Extracts text in page batches to reduce memory pressure.
     */
    private String extractTextInPages(PDDocument document, PDFTextStripper textStripper, int pageCount) {
        StringBuilder result = new StringBuilder();

        try {
            // Process 20 pages per batch as a balance between speed and memory use.
            int batchSize = 20;
            for (int startPage = 0; startPage < pageCount; startPage += batchSize) {
                int endPage = Math.min(startPage + batchSize, pageCount);

                // PDFTextStripper page numbers are 1-based.
                textStripper.setStartPage(startPage + 1);
                textStripper.setEndPage(endPage);

                String pageText = textStripper.getText(document);
                if (pageText != null && !pageText.trim().isEmpty()) {
                    result.append(pageText);
                }

                // Periodically hint to the JVM that memory can be reclaimed.
                if (startPage % 50 == 0) {
                    System.gc();
                }
            }
        } catch (IOException e) {
            logger.warn("Paged PDF extraction failed. Falling back to single-pass extraction: {}", e.getMessage());
            try {
                return textStripper.getText(document);
            } catch (IOException ex) {
                return "";
            }
        }

        return result.toString();
    }

    /**
     * Fallback text extraction that scans raw PDF content for embedded strings.
     */
    private String extractTextFallback(File pdfFile) {
        try {
            // Read the raw PDF bytes as a Latin-1 string for token scanning.
            byte[] fileBytes = java.nio.file.Files.readAllBytes(pdfFile.toPath());
            String content = new String(fileBytes, java.nio.charset.StandardCharsets.ISO_8859_1);

            // Collect text tokens that appear between literal string parentheses.
            StringBuilder text = new StringBuilder();
            boolean inText = false;
            boolean inStream = false;
            StringBuilder currentText = new StringBuilder();

            for (int i = 0; i < content.length() - 1; i++) {
                char c = content.charAt(i);
                char next = content.charAt(i + 1);

                // Skip stream bodies because they are usually compressed or binary.
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

                // Detect the start of a literal PDF string.
                if (c == '(' && next != ')') {
                    inText = true;
                    currentText.setLength(0);
                    continue;
                }

                // Detect the end of a literal PDF string.
                if (c == ')' && inText) {
                    inText = false;
                    String decoded = decodePDFString(currentText.toString());
                    if (decoded.length() > 3 && isMostlyPrintable(decoded)) {
                        text.append(decoded).append(" ");
                    }
                    continue;
                }

                // Accumulate text characters inside the current string.
                if (inText) {
                    currentText.append(c);
                }
            }

            if (text.length() == 0) {
                return "*Unable to extract PDF text content. This may happen because:*\n\n" +
                       "1. The PDF contains scanned images instead of embedded text\n" +
                       "2. The PDF uses unsupported text encoding\n" +
                       "3. The PDF file is damaged\n\n" +
                       "*Suggestion: try enabling OCR to process scanned PDF pages.*";
            }

            return formatExtractedText(text.toString());

        } catch (Exception e) {
            logger.error("Fallback PDF text extraction failed: {}", e.getMessage());
            return "*PDF text extraction is currently unavailable. Please verify that the PDF is not only scanned image content.*";
        }
    }

    /**
     * Decodes PDF literal strings including common escape sequences.
     */
    private String decodePDFString(String str) {
        StringBuilder decoded = new StringBuilder();

        for (int i = 0; i < str.length(); i++) {
            char c = str.charAt(i);

            if (c == '\\' && i + 1 < str.length()) {
                char next = str.charAt(i + 1);

                // Handle PDF escape sequences.
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
                        // Handle octal escapes such as \ddd.
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
     * Returns whether a decoded string is mostly printable text.
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
     * Applies light formatting to fallback-extracted text.
     */
    private String formatExtractedText(String text) {
        // Collapse repeated whitespace first.
        text = text.replaceAll("\\s+", " ");

        // Insert paragraph breaks after sentence-ending punctuation.
        text = text.replaceAll("\\.\\s+", ".\n\n");
        text = text.replaceAll("!\\s+", "!\n\n");
        text = text.replaceAll("\\?\\s+", "?\n\n");

        return text.trim();
    }

    /**
     * Extracts PDF metadata when available.
     */
    private Map<String, Object> extractMetadata(File pdfFile, ConversionOptions options) {
        Map<String, Object> metadata = new HashMap<>();

        if (options.content().includeMetadata()) {
            metadata.put("File Name", pdfFile.getName());
            metadata.put("File Size", pdfFile.length());
            metadata.put("Converted At", LocalDateTime.now());

            // Load the optional PDF password from conversion options.
            String password = options.document().pdfPassword();

            try (PDDocument document = password != null && !password.isEmpty()
                    ? PDDocument.load(pdfFile, password)
                    : PDDocument.load(pdfFile)) {
                metadata.put("Pages", document.getNumberOfPages());

                // Read document information entries exposed by PDFBox.
                if (document.getDocumentInformation() != null) {
                    String title = document.getDocumentInformation().getTitle();
                    String author = document.getDocumentInformation().getAuthor();
                    String subject = document.getDocumentInformation().getSubject();
                    String keywords = document.getDocumentInformation().getKeywords();
                    String creator = document.getDocumentInformation().getCreator();
                    String producer = document.getDocumentInformation().getProducer();

                    if (title != null && !title.isEmpty()) metadata.put("Title", title);
                    if (author != null && !author.isEmpty()) metadata.put("Author", author);
                    if (subject != null && !subject.isEmpty()) metadata.put("Subject", subject);
                    if (keywords != null && !keywords.isEmpty()) metadata.put("Keywords", keywords);
                    if (creator != null && !creator.isEmpty()) metadata.put("Creator", creator);
                    if (producer != null && !producer.isEmpty()) metadata.put("PDF Producer", producer);
                }
            } catch (Exception e) {
                logger.warn("Unable to read PDF metadata: {}", e.getMessage());
            }
        }

        return metadata;
    }

    /**
     * Builds the final Markdown document.
     */
    private String convertToMarkdown(String textContent, Map<String, Object> metadata, ConversionOptions options) {
        StringBuilder markdown = new StringBuilder();

        // Add metadata when requested.
        if (options.content().includeMetadata() && !metadata.isEmpty()) {
            markdown.append("## Document Information\n\n");
            for (Map.Entry<String, Object> entry : metadata.entrySet()) {
                if (entry.getValue() != null) {
                    markdown.append("- **").append(formatMetadataKey(entry.getKey()))
                            .append(":** ").append(entry.getValue()).append("\n");
                }
            }
            markdown.append("\n");
        }

        // Add the main content section.
        markdown.append("## Content\n\n");

        if (textContent != null && !textContent.trim().isEmpty()) {
            markdown.append(formatTextContent(applyPageBreakMode(textContent, options)));
        } else {
            markdown.append("*Unable to extract PDF text content. This may happen because:*\n\n");
            markdown.append("1. The PDF contains scanned images instead of embedded text\n");
            markdown.append("2. The PDF uses unsupported text encoding\n");
            markdown.append("3. The PDF file is damaged\n\n");
            markdown.append("*Suggestion: try enabling OCR or using another PDF extraction strategy.*");
        }

        return markdown.toString();
    }

    private String resolvePageSeparator(ConversionOptions options) {
        String mode = options.content().pageBreakMode();
        if (mode == null || mode.trim().isEmpty()) {
            mode = "heading";
        }

        switch (mode.toLowerCase(Locale.ROOT)) {
            case "rule":
                return "\n\n---\n\n";
            case "blank":
                return "\n\n";
            case "none":
                return "\n";
            case "heading":
            default:
                return "\n\n--- Page Break ---\n\n";
        }
    }

    private String applyPageBreakMode(String textContent, ConversionOptions options) {
        if (textContent == null || textContent.isEmpty()) {
            return textContent;
        }
        return textContent.replace("\f", resolvePageSeparator(options));
    }

    /**
     * Formats extracted text into more readable Markdown paragraphs.
     */
    private String formatTextContent(String textContent) {
        if (textContent == null || textContent.trim().isEmpty()) {
            return "";
        }

        // Normalize the raw extracted text first.
        String cleaned = cleanupText(textContent);

        StringBuilder formatted = new StringBuilder();

        // Process the text line by line.
        String[] lines = cleaned.split("\n");

        for (int i = 0; i < lines.length; i++) {
            String line = normalizeExtractedLine(lines[i].trim());

            if (line.isEmpty()) {
                // Preserve paragraph separation.
                formatted.append("\n\n");
                continue;
            }

            // Detect likely headings.
            if (isHeadingLine(line)) {
                // Add extra spacing before headings when needed.
                if (formatted.length() > 0 && !formatted.toString().endsWith("\n\n\n")) {
                    formatted.append("\n");
                }
                formatted.append("### ").append(line).append("\n\n");
            }
            // Preserve list items as one line each.
            else if (isListItem(line)) {
                formatted.append(line).append("\n");
            }
            // Treat everything else as paragraph text.
            else {
                // Join lines that appear to continue the same paragraph.
                if (i + 1 < lines.length && !lines[i + 1].trim().isEmpty() &&
                    !isHeadingLine(lines[i + 1].trim()) && !isListItem(lines[i + 1].trim())) {
                    // Continue the current paragraph.
                    formatted.append(line).append(" ");
                } else {
                    // Close the current paragraph.
                    formatted.append(line).append("\n\n");
                }
            }
        }

        // Collapse excessive blank lines after formatting.
        String result = formatted.toString();
        result = result.replaceAll("\\n{3,}", "\n\n");

        return result.trim();
    }

    private String normalizeExtractedLine(String line) {
        if (line == null || line.isEmpty()) {
            return line;
        }

        String normalized = line;
        normalized = normalized.replaceFirst("^\\s*[^\\x00-\\x7F]{1,6}\\??\\s*", "- ");
        normalized = normalized.replaceFirst("^\\s*[^\\p{L}\\p{N}\\s]{1,3}\\s*", "- ");
        normalized = normalized.replaceFirst("^-\\s*\\?\\s*", "- ");
        return normalized;
    }

    /**
     * Normalizes raw extracted text before Markdown formatting.
     */
    private String cleanupText(String text) {
        if (text == null || text.trim().isEmpty()) {
            return "";
        }

        // Repair a few common PDF extraction artifacts.
        String cleaned = text.replaceAll("-\\s+", "-"); // Repair broken hyphenated words.
        cleaned = cleaned.replaceAll("\\s*\\f\\s*", "\n\n"); // Convert form feeds into paragraph breaks.
        cleaned = cleaned.replaceAll("\\r\\n", "\n"); // Normalize Windows line endings.
        cleaned = cleaned.replaceAll("\\r", "\n"); // Normalize legacy Mac line endings.

        // Collapse long runs of blank lines.
        cleaned = cleaned.replaceAll("\\n{3,}", "\n\n");

        return cleaned.trim();
    }

    /**
     * Heuristically detects heading-like lines.
     */
    private boolean isHeadingLine(String line) {
        if (line == null || line.trim().isEmpty()) {
            return false;
        }

        // Numbered headings such as "1. Introduction".
        if (line.matches("^\\d+\\.\\s+.*")) {
            return true;
        }

        // Short all-caps headings.
        if (line.length() < 80 && line.equals(line.toUpperCase()) &&
            line.matches(".*[A-Z].*") && !line.matches(".*[a-z].*")) {
            return true;
        }

        // Short title-cased lines without strong sentence punctuation.
        if (line.length() < 100 && Character.isUpperCase(line.charAt(0)) &&
            !line.matches(".*\\.$") && !line.contains(",")) {
            return true;
        }

        return false;
    }

    /**
     * Detects Markdown-style list items.
     */
    private boolean isListItem(String line) {
        if (line == null || line.trim().isEmpty()) {
            return false;
        }

        // Bullet lists.
        if (line.matches("^\\s*[-\u2022]\\s+.*")) {
            return true;
        }

        // Numbered lists.
        if (line.matches("^\\s*\\d+[.)]\\s+.*")) {
            return true;
        }

        return false;
    }

    /**
     * Formats metadata keys for display.
     */
    private String formatMetadataKey(String key) {
        return com.markdown.engine.MarkdownBuilder.prettifyMetadataKey(key);
    }

    private void configurePdfBoxFontCache() {
        if (System.getProperty("pdfbox.fontcache") != null) {
            return;
        }

        try {
            Path cacheDir = Path.of(System.getProperty("java.io.tmpdir"), "markitdown-pdfbox-cache");
            Files.createDirectories(cacheDir);
            System.setProperty("pdfbox.fontcache", cacheDir.toAbsolutePath().toString());
        } catch (IOException e) {
            logger.debug("Unable to configure PDFBox font cache: {}", e.getMessage());
        }
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
     * Cleans OCR text for a single rendered page.
     */
    private String cleanupSinglePageText(String pageText) {
        if (pageText == null || pageText.trim().isEmpty()) {
            return pageText;
        }

        // Split into lines and remove near-duplicates.
        String[] lines = pageText.split("\\n");
        List<String> uniqueLines = new ArrayList<>();
        String lastLine = "";

        for (String line : lines) {
            String trimmed = line.trim();
            if (trimmed.isEmpty()) {
                continue;
            }

            // Skip lines that are highly similar to the previous one.
            if (!lastLine.isEmpty() && trimmed.length() > 8) {
                double similarity = calculateSimilarity(trimmed, lastLine);
                if (similarity > 0.80) {
                    continue; // Skip highly similar duplicate lines.
                }
            }

            uniqueLines.add(line);
            lastLine = trimmed;
        }

        return String.join("\n", uniqueLines);
    }

    /**
     * Cleans OCR output across all pages by reducing duplication and noise.
     */
    private String cleanupOcrText(String ocrText) {
        if (ocrText == null || ocrText.trim().isEmpty()) {
            return ocrText;
        }

        // Split the OCR output into page-sized chunks first.
        String[] pages = ocrText.split("### Page \\d+");
        List<String> cleanedPages = new ArrayList<>();

        for (String page : pages) {
            if (page.trim().isEmpty()) continue;

            // Process the page line by line.
            String[] lines = page.split("\\n");
            List<String> cleanedLines = new ArrayList<>();
            String lastLine = "";

            for (String line : lines) {
                String trimmed = line.trim();
                if (trimmed.isEmpty()) {
                    continue; // Skip blank lines.
                }

                // Preserve page header lines.
                if (trimmed.startsWith("###")) {
                    cleanedLines.add(line);
                    continue;
                }

                // Skip lines that are too similar to the previous cleaned line.
                if (!lastLine.isEmpty() && trimmed.length() > 10) {
                    double similarity = calculateSimilarity(trimmed, lastLine);
                    if (similarity > 0.75) {
                        // Drop highly repetitive lines.
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

        // Reassemble the cleaned pages with page headers.
        StringBuilder result = new StringBuilder();
        for (int i = 0; i < cleanedPages.size(); i++) {
            if (i > 0) {
                result.append("\n\n");
            }
            result.append("### Page ").append(i + 1).append("\\n\\n");
            result.append(cleanedPages.get(i));
        }

        return result.toString();
    }

    /**
     * Removes duplicate lines within a paragraph-sized block.
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
     * Reduces repeated OCR sentences while keeping a small amount of context.
     */
    private String removeRepeatedSentences(String text) {
        if (text == null || text.isEmpty()) {
            return text;
        }

        // OCR output is typically line-oriented, so compare line by line.
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

            // Detect repeated or highly similar lines.
            if (trimmed.length() > 10 && calculateSimilarity(trimmed, lastLine) > 0.85) {
                repeatCount++;
                // Keep only the first two occurrences.
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
     * Computes a simple similarity score between two strings.
     */
    private double calculateSimilarity(String s1, String s2) {
        if (s1 == null || s2 == null) return 0.0;
        if (s1.equals(s2)) return 1.0;

        int maxLen = Math.max(s1.length(), s2.length());
        if (maxLen == 0) return 0.0;

        // Approximate similarity from normalized edit distance.
        return (maxLen - levenshteinDistance(s1, s2)) / (double) maxLen;
    }

    /**
     * Computes the Levenshtein edit distance between two strings.
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
