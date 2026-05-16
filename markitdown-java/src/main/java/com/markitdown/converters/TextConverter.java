package com.markitdown.converters;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.markdown.engine.MarkdownBuilder;
import com.markdown.engine.config.MarkdownConfig;
import com.markitdown.api.ConversionResult;
import com.markitdown.api.DocumentConverter;
import com.markitdown.config.ConversionOptions;
import com.markitdown.exceptions.ConversionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static java.util.Objects.requireNonNull;

/**
 * Converts plain-text-like formats such as TXT, Markdown, CSV, logs, JSON, and XML
 * into Markdown output suitable for the CLI and library APIs.
 */
public class TextConverter implements DocumentConverter {

    private static final Logger logger = LoggerFactory.getLogger(TextConverter.class);
    private static final Set<String> SUPPORTED_FORMATS = Set.of("txt", "md", "csv", "log", "json", "xml");

    private MarkdownBuilder mb;

    @Override
    public ConversionResult convert(Path filePath, ConversionOptions options) throws ConversionException {
        requireNonNull(filePath, "File path cannot be null");
        requireNonNull(options, "Conversion options cannot be null");

        logger.info("Converting text file: {}", filePath);
        mb = new MarkdownBuilder(new MarkdownConfig());

        try {
            mb = new MarkdownBuilder(options.toMarkdownConfig());
            String content = Files.readString(filePath, StandardCharsets.UTF_8);
            String format = detectFileFormat(filePath);
            Map<String, Object> metadata = extractMetadata(filePath, content, format, options);
            String markdownContent = convertToMarkdown(content, format, metadata, options);

            return new ConversionResult(
                    markdownContent,
                    metadata,
                    new ArrayList<>(),
                    filePath.toFile().length(),
                    filePath.getFileName().toString()
            );
        } catch (IOException e) {
            String errorMessage = "Failed to process text file: " + e.getMessage();
            logger.error(errorMessage, e);
            throw new ConversionException(errorMessage, e, filePath.getFileName().toString(), getName());
        }
    }

    @Override
    public ConversionResult convert(InputStream inputStream, String mimeType, ConversionOptions options)
            throws ConversionException {
        requireNonNull(inputStream, "Input stream cannot be null");
        requireNonNull(mimeType, "MIME type cannot be null");
        requireNonNull(options, "Conversion options cannot be null");

        logger.info("Converting text stream: {}", mimeType);
        mb = new MarkdownBuilder(new MarkdownConfig());

        try {
            mb = new MarkdownBuilder(options.toMarkdownConfig());
            String content = new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);
            String format = detectFormatFromMimeType(mimeType);
            String sourceFileName = resolveStreamFileName(options, format);
            Map<String, Object> metadata = extractStreamMetadata(sourceFileName, content, format, options);
            String markdownContent = convertToMarkdown(content, format, metadata, options);

            return new ConversionResult(
                    markdownContent,
                    metadata,
                    new ArrayList<>(),
                    content.getBytes(StandardCharsets.UTF_8).length,
                    sourceFileName
            );
        } catch (IOException e) {
            String errorMessage = "Failed to process text stream: " + e.getMessage();
            logger.error(errorMessage, e);
            throw new ConversionException(errorMessage, e, "stream", getName());
        }
    }

    @Override
    public boolean supportsStreaming() {
        return true;
    }

    @Override
    public boolean supports(String mimeType) {
        return "text/plain".equals(mimeType)
                || "text/markdown".equals(mimeType)
                || "text/csv".equals(mimeType)
                || "application/json".equals(mimeType)
                || "application/xml".equals(mimeType)
                || "text/xml".equals(mimeType);
    }

    @Override
    public int getPriority() {
        return 50;
    }

    @Override
    public String getName() {
        return "TextConverter";
    }

    private String detectFileFormat(Path filePath) {
        String extension = getFileExtension(filePath.getFileName().toString()).toLowerCase();
        switch (extension) {
            case "md":
                return "markdown";
            case "csv":
                return "csv";
            case "json":
                return "json";
            case "xml":
                return "xml";
            case "log":
                return "log";
            default:
                return "plain";
        }
    }

    private String detectFormatFromMimeType(String mimeType) {
        switch (mimeType) {
            case "text/markdown":
                return "markdown";
            case "text/csv":
                return "csv";
            case "application/json":
                return "json";
            case "application/xml":
            case "text/xml":
                return "xml";
            default:
                return "plain";
        }
    }

    private String buildSyntheticFileName(String format) {
        switch (format) {
            case "markdown":
                return "stream.md";
            case "csv":
                return "stream.csv";
            case "json":
                return "stream.json";
            case "xml":
                return "stream.xml";
            case "log":
                return "stream.log";
            default:
                return "stream.txt";
        }
    }

    private String resolveStreamFileName(ConversionOptions options, String format) {
        String sourceFileName = options.document().sourceFileName();
        if (sourceFileName != null && !sourceFileName.isBlank()) {
            return sourceFileName;
        }
        return buildSyntheticFileName(format);
    }

    private Map<String, Object> extractMetadata(
            Path filePath,
            String content,
            String format,
            ConversionOptions options
    ) {
        Map<String, Object> metadata = new LinkedHashMap<>();
        if (!options.content().includeMetadata()) {
            return metadata;
        }

        metadata.put("File Name", filePath.getFileName().toString());
        metadata.put("File Size", filePath.toFile().length());
        metadata.put("File Type", format);

        addCommonTextMetadata(metadata, content);
        addFormatSpecificMetadata(metadata, content, format);
        metadata.put("Converted At", LocalDateTime.now());
        return metadata;
    }

    private Map<String, Object> extractStreamMetadata(
            String fileName,
            String content,
            String format,
            ConversionOptions options
    ) {
        Map<String, Object> metadata = new LinkedHashMap<>();
        if (!options.content().includeMetadata()) {
            return metadata;
        }

        metadata.put("File Name", fileName);
        metadata.put("File Size", content.getBytes(StandardCharsets.UTF_8).length);
        metadata.put("File Type", format);

        addCommonTextMetadata(metadata, content);
        addFormatSpecificMetadata(metadata, content, format);
        metadata.put("Converted At", LocalDateTime.now());
        return metadata;
    }

    private void addCommonTextMetadata(Map<String, Object> metadata, String content) {
        String[] lines = content.split("\\r?\\n", -1);
        metadata.put("Line Count", lines.length);
        metadata.put("Character Count", content.length());
    }

    private void addFormatSpecificMetadata(Map<String, Object> metadata, String content, String format) {
        switch (format) {
            case "csv":
                extractCsvMetadata(content, metadata);
                break;
            case "json":
                extractJsonMetadata(content, metadata);
                break;
            case "xml":
                extractXmlMetadata(content, metadata);
                break;
            default:
                break;
        }
    }

    private void extractCsvMetadata(String content, Map<String, Object> metadata) {
        String[] lines = content.split("\\r?\\n");
        if (lines.length == 0 || lines[0].isEmpty()) {
            metadata.put("Column Count", 0);
            metadata.put("Data Row Count", 0);
            metadata.put("Has Header", false);
            return;
        }

        String[] columns = lines[0].split(",");
        metadata.put("Column Count", columns.length);
        metadata.put("Data Row Count", Math.max(lines.length - 1, 0));
        metadata.put("Has Header", true);
    }

    private void extractJsonMetadata(String content, Map<String, Object> metadata) {
        metadata.put("Valid JSON", isValidJson(content));
    }

    private boolean isValidJson(String content) {
        if (content == null || content.trim().isEmpty()) {
            logger.error("Content is empty and cannot be parsed as JSON");
            return false;
        }

        String trimmed = content.trim();
        if (!(trimmed.startsWith("{") && trimmed.endsWith("}"))
                && !(trimmed.startsWith("[") && trimmed.endsWith("]"))) {
            logger.error("Content is neither a JSON object nor a JSON array");
            return false;
        }

        try {
            new ObjectMapper().readTree(trimmed);
            return true;
        } catch (IOException e) {
            logger.error("Invalid JSON content: {}", e.getMessage());
            return false;
        }
    }

    private void extractXmlMetadata(String content, Map<String, Object> metadata) {
        String trimmed = content.trim();
        if (!trimmed.startsWith("<") || !trimmed.endsWith(">")) {
            metadata.put("Valid XML", false);
            return;
        }

        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            factory.setNamespaceAware(true);
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document document = builder.parse(new ByteArrayInputStream(trimmed.getBytes(StandardCharsets.UTF_8)));
            Element rootElement = document.getDocumentElement();

            metadata.put("Valid XML", true);
            metadata.put("Root Element", rootElement.getNodeName());
            metadata.put("Root Namespace", rootElement.getNamespaceURI());
            metadata.put("Element Count", countElements(rootElement));
            metadata.put("Attribute Count", countAttributes(rootElement));
            metadata.put("Text Node Count", countTextNodes(rootElement));
            metadata.put("Has CDATA", hasCDataSections(rootElement));
            metadata.put("Has Comments", hasComments(rootElement));
            metadata.put("Namespace Count", countNamespaces(rootElement));
        } catch (Exception e) {
            logger.warn("Failed to extract XML metadata: {}", e.getMessage());
            metadata.put("Valid XML", false);
            metadata.put("XML Error", e.getMessage());
        }
    }

    private int countElements(Element element) {
        int count = 1;
        NodeList children = element.getChildNodes();
        for (int i = 0; i < children.getLength(); i++) {
            Node child = children.item(i);
            if (child.getNodeType() == Node.ELEMENT_NODE) {
                count += countElements((Element) child);
            }
        }
        return count;
    }

    private int countAttributes(Element element) {
        int count = element.getAttributes() != null ? element.getAttributes().getLength() : 0;
        NodeList children = element.getChildNodes();
        for (int i = 0; i < children.getLength(); i++) {
            Node child = children.item(i);
            if (child.getNodeType() == Node.ELEMENT_NODE) {
                count += countAttributes((Element) child);
            }
        }
        return count;
    }

    private int countTextNodes(Element element) {
        int count = 0;
        NodeList children = element.getChildNodes();
        for (int i = 0; i < children.getLength(); i++) {
            Node child = children.item(i);
            if (child.getNodeType() == Node.TEXT_NODE) {
                if (!child.getTextContent().trim().isEmpty()) {
                    count++;
                }
            } else if (child.getNodeType() == Node.ELEMENT_NODE) {
                count += countTextNodes((Element) child);
            }
        }
        return count;
    }

    private boolean hasCDataSections(Element element) {
        NodeList children = element.getChildNodes();
        for (int i = 0; i < children.getLength(); i++) {
            Node child = children.item(i);
            if (child.getNodeType() == Node.CDATA_SECTION_NODE) {
                return true;
            }
            if (child.getNodeType() == Node.ELEMENT_NODE && hasCDataSections((Element) child)) {
                return true;
            }
        }
        return false;
    }

    private boolean hasComments(Element element) {
        NodeList children = element.getChildNodes();
        for (int i = 0; i < children.getLength(); i++) {
            Node child = children.item(i);
            if (child.getNodeType() == Node.COMMENT_NODE) {
                return true;
            }
            if (child.getNodeType() == Node.ELEMENT_NODE && hasComments((Element) child)) {
                return true;
            }
        }
        return false;
    }

    private int countNamespaces(Element element) {
        Set<String> namespaces = new HashSet<>();
        collectNamespaces(element, namespaces);
        return namespaces.size();
    }

    private void collectNamespaces(Element element, Set<String> namespaces) {
        String namespaceUri = element.getNamespaceURI();
        if (namespaceUri != null && !namespaceUri.isEmpty()) {
            namespaces.add(namespaceUri);
        }

        NodeList children = element.getChildNodes();
        for (int i = 0; i < children.getLength(); i++) {
            Node child = children.item(i);
            if (child.getNodeType() == Node.ELEMENT_NODE) {
                collectNamespaces((Element) child, namespaces);
            }
        }
    }

    private String convertToMarkdown(
            String content,
            String format,
            Map<String, Object> metadata,
            ConversionOptions options
    ) {
        if (options.content().includeMetadata() && !metadata.isEmpty()) {
            mb.header(metadata);
        }
        mb.append(mb.h2("Content"));

        switch (format) {
            case "markdown":
                mb.append(content);
                break;
            case "csv":
                mb.append(convertCsvToMarkdown(content));
                break;
            case "json":
                mb.append(convertJsonToMarkdown(content));
                break;
            case "xml":
                mb.append(convertXmlToMarkdown(content));
                break;
            case "log":
                mb.append(convertLogToMarkdown(content));
                break;
            default:
                mb.append(convertPlainTextToMarkdown(content));
                break;
        }

        return mb.flush().toString();
    }

    private StringBuilder convertCsvToMarkdown(String csvContent) {
        String[] lines = csvContent.split("\\r?\\n");
        if (lines.length == 0 || lines[0].isEmpty()) {
            return mb.bold("Empty file").append("\n\n");
        }

        String[] headers = lines[0].split(",");
        int columns = headers.length;
        String[][] data = new String[Math.max(lines.length - 1, 0)][columns];

        for (int i = 1; i < lines.length; i++) {
            String[] row = lines[i].split(",");
            for (int j = 0; j < columns; j++) {
                data[i - 1][j] = j < row.length ? row[j] : "";
            }
        }

        return mb.table(headers, data);
    }

    private StringBuilder convertJsonToMarkdown(String jsonContent) {
        return mb.codeBlock(jsonContent, "json");
    }

    private StringBuilder convertXmlToMarkdown(String xmlContent) {
        return mb.codeBlock(xmlContent, "xml");
    }

    private StringBuilder convertLogToMarkdown(String logContent) {
        String[] lines = logContent.split("\\r?\\n");
        StringBuilder markdown = new StringBuilder();

        for (String line : lines) {
            String trimmed = line.trim();
            if (trimmed.isEmpty()) {
                continue;
            }

            String upper = trimmed.toUpperCase();
            if (upper.contains("ERROR")) {
                markdown.append(mb.bold("ERROR: ")).append(trimmed).append("\n");
            } else if (upper.contains("WARN")) {
                markdown.append(mb.bold("WARN: ")).append(trimmed).append("\n");
            } else if (upper.contains("INFO")) {
                markdown.append(mb.bold("INFO: ")).append(trimmed).append("\n");
            } else {
                markdown.append(trimmed).append("\n");
            }
        }

        markdown.append("\n");
        return markdown;
    }

    private String convertPlainTextToMarkdown(String textContent) {
        String[] lines = textContent.split("\\r?\\n");
        StringBuilder markdown = new StringBuilder();

        for (String line : lines) {
            String trimmed = line.trim();

            if (trimmed.isEmpty()) {
                markdown.append("\n");
            } else if (trimmed.startsWith("#")) {
                markdown.append(trimmed).append("\n\n");
            } else if (line.startsWith(" ") || line.startsWith("\t")) {
                markdown.append("    ").append(trimmed).append("\n");
            } else if (trimmed.startsWith("- ") || trimmed.startsWith("* ")) {
                markdown.append(trimmed).append("\n");
            } else {
                markdown.append(trimmed).append("\n\n");
            }
        }

        return markdown.toString();
    }

    private String getFileExtension(String fileName) {
        requireNonNull(fileName, "File name cannot be null");

        int lastDotIndex = fileName.lastIndexOf('.');
        if (lastDotIndex > 0 && lastDotIndex < fileName.length() - 1) {
            return fileName.substring(lastDotIndex + 1);
        }
        return "";
    }

    private String getFileNameWithoutExtension(String fileName) {
        requireNonNull(fileName, "File name cannot be null");

        int lastDotIndex = fileName.lastIndexOf('.');
        if (lastDotIndex > 0) {
            return fileName.substring(0, lastDotIndex);
        }
        return fileName;
    }

    private String formatMetadataKey(String key) {
        requireNonNull(key, "Metadata key cannot be null");
        String withSpaces = key.replaceAll("([a-z])([A-Z])", "$1 $2");
        return Character.toUpperCase(withSpaces.charAt(0)) + withSpaces.substring(1);
    }

    public static boolean isSupportedFormat(String fileExtension) {
        return fileExtension != null && SUPPORTED_FORMATS.contains(fileExtension.toLowerCase());
    }

    public static Set<String> getSupportedFormats() {
        return new HashSet<>(SUPPORTED_FORMATS);
    }
}
