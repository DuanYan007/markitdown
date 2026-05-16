package com.markitdown.converters;

import com.markdown.engine.MarkdownBuilder;
import com.markdown.engine.config.MarkdownConfig;
import com.markitdown.api.ConversionResult;
import com.markitdown.api.DocumentConverter;
import com.markitdown.config.ConversionOptions;
import com.markitdown.exceptions.ConversionException;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;
import org.jsoup.nodes.TextNode;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.util.Objects.requireNonNull;

/**
 * Converts HTML documents to Markdown.
 *
 * <p>The converter uses Jsoup to parse HTML, extracts basic metadata, and then
 * walks the document tree to emit Markdown through {@link MarkdownBuilder}.</p>
 */
public class HtmlConverter implements DocumentConverter {

    private static final Logger logger = LoggerFactory.getLogger(HtmlConverter.class);

    private MarkdownBuilder mb;

    /**
     * Converts an HTML file to Markdown.
     *
     * @param filePath source HTML file path
     * @param options conversion options
     * @return conversion result
     * @throws ConversionException when the HTML file cannot be processed
     */
    @Override
    public ConversionResult convert(Path filePath, ConversionOptions options) throws ConversionException {
        requireNonNull(filePath, "File path cannot be null");
        requireNonNull(options, "Conversion options cannot be null");

        logger.info("Converting HTML file: {}", filePath);
        mb = new MarkdownBuilder(new MarkdownConfig());

        try {
            Document document = Jsoup.parse(filePath.toFile(), "UTF-8");

            Map<String, Object> metadata = extractMetadata(document, options);
            if (options.content().includeMetadata()) {
                metadata.put("File Name", filePath.getFileName().toString());
                metadata.put("File Size", filePath.toFile().length());
            }

            String markdownContent = convertToMarkdown(document, metadata, options);
            List<String> warnings = new ArrayList<>();

            return new ConversionResult(markdownContent, metadata, warnings,
                    filePath.toFile().length(), filePath.getFileName().toString());
        } catch (IOException e) {
            String errorMessage = "Failed to process HTML file: " + e.getMessage();
            logger.error(errorMessage, e);
            throw new ConversionException(errorMessage, e, filePath.getFileName().toString(), getName());
        }
    }

    /**
     * Checks whether the converter supports the MIME type.
     *
     * @param mimeType MIME type to evaluate
     * @return {@code true} for HTML MIME types
     */
    @Override
    public boolean supports(String mimeType) {
        return "text/html".equals(mimeType) || "application/xhtml+xml".equals(mimeType);
    }

    /**
     * Returns the converter priority.
     *
     * @return priority value
     */
    @Override
    public int getPriority() {
        return 100;
    }

    /**
     * Returns the converter name.
     *
     * @return converter name
     */
    @Override
    public String getName() {
        return "HtmlConverter";
    }

    /**
     * Extracts metadata from the parsed HTML document.
     *
     * @param document parsed HTML document
     * @param options conversion options
     * @return metadata map
     */
    private Map<String, Object> extractMetadata(Document document, ConversionOptions options) {
        Map<String, Object> metadata = new HashMap<>();

        if (options.content().includeMetadata()) {
            String title = document.title();
            if (title != null && !title.trim().isEmpty()) {
                metadata.put("Title", title.trim());
            }

            Elements metaTags = document.select("meta");
            for (Element meta : metaTags) {
                String name = meta.attr("name");
                String property = meta.attr("property");
                String content = meta.attr("content");

                if (content != null && !content.trim().isEmpty()) {
                    if (name != null && !name.trim().isEmpty()) {
                        metadata.put(name.toLowerCase().replace(":", "_"), content.trim());
                    } else if (property != null && !property.trim().isEmpty()) {
                        metadata.put(property.toLowerCase().replace(":", "_"), content.trim());
                    }
                }
            }

            String language = document.select("html").attr("lang");
            if (!language.isEmpty()) {
                metadata.put("Language", language);
            }

            metadata.put("Heading Count", document.select("h1, h2, h3, h4, h5, h6").size());
            metadata.put("Link Count", document.select("a[href]").size());
            metadata.put("Image Count", document.select("img[src]").size());
            metadata.put("Table Count", document.select("table").size());
            metadata.put("Converted At", LocalDateTime.now());
        }

        return metadata;
    }

    /**
     * Renders the parsed HTML document to Markdown.
     *
     * @param document parsed HTML document
     * @param metadata extracted metadata
     * @param options conversion options
     * @return Markdown output
     */
    private String convertToMarkdown(Document document, Map<String, Object> metadata, ConversionOptions options) {
        if (options.content().includeMetadata() && !metadata.isEmpty()) {
            mb.header(metadata);
        }

        Element body = document.body();
        if (body != null) {
            processNode(body, options, 0);
        }

        return mb.flush();
    }

    /**
     * Processes a single HTML node.
     *
     * @param node current node
     * @param options conversion options
     * @param depth nesting depth used for lists
     */
    private void processNode(Node node, ConversionOptions options, int depth) {
        if (node instanceof TextNode) {
            String text = ((TextNode) node).text().trim();
            if (!text.isEmpty()) {
                mb.text(mb.escapeMarkdown(text)).text(" ");
            }
            return;
        }

        if (!(node instanceof Element)) {
            return;
        }

        Element element = (Element) node;
        String tagName = element.tagName().toLowerCase();

        switch (tagName) {
            case "h1":
                mb.text("\n").append(mb.h1(mb.escapeMarkdown(element.text())));
                break;
            case "h2":
                mb.text("\n").append(mb.h2(mb.escapeMarkdown(element.text())));
                break;
            case "h3":
                mb.text("\n").append(mb.h3(mb.escapeMarkdown(element.text())));
                break;
            case "h4":
                mb.text("\n").append(mb.h4(mb.escapeMarkdown(element.text())));
                break;
            case "h5":
                mb.text("\n").append(mb.h5(mb.escapeMarkdown(element.text())));
                break;
            case "h6":
                mb.text("\n").append(mb.h6(mb.escapeMarkdown(element.text())));
                break;
            case "p":
                mb.newline();
                processChildren(element, options, depth + 1);
                mb.newline(2);
                break;
            case "br":
                mb.lineBreak();
                break;
            case "strong":
            case "b":
                mb.append("**");
                processChildren(element, options, depth + 1);
                mb.append("**");
                break;
            case "em":
            case "i":
                mb.append("*");
                processChildren(element, options, depth + 1);
                mb.append("*");
                break;
            case "u":
                mb.append("<u>");
                processChildren(element, options, depth + 1);
                mb.append("</u>");
                break;
            case "del":
            case "s":
            case "strike":
                mb.append("~~");
                processChildren(element, options, depth + 1);
                mb.append("~~");
                break;
            case "code":
                mb.append("`");
                processChildren(element, options, depth + 1);
                mb.append("`");
                break;
            case "pre":
                String codeText = element.text();
                mb.append("\n```\n").append(codeText).append("\n```\n\n");
                break;
            case "blockquote":
                String quoteText = element.text();
                mb.append("\n> ").append(quoteText.replace("\n", "\n> ")).append("\n\n");
                break;
            case "ul":
            case "ol":
                processList(element, options, depth + 1);
                break;
            case "li":
                processListItem(element, options, depth + 1);
                break;
            case "a":
                processLink(element, options);
                break;
            case "img":
                processImage(element, options);
                break;
            case "table":
                if (options.content().includeTables()) {
                    processTable(element, options);
                }
                break;
            case "hr":
                mb.text("\n").horizontalRule();
                break;
            case "div":
            case "section":
            case "article":
            case "main":
            case "header":
            case "footer":
            case "nav":
            case "aside":
                // Treat container elements as transparent blocks.
                mb.newline();
                processChildren(element, options, depth + 1);
                mb.newline();
                break;
            case "span":
            case "small":
            case "big":
            case "sub":
            case "sup":
            case "mark":
            case "q":
            case "cite":
            case "dfn":
            case "abbr":
            case "time":
            case "var":
            case "samp":
            case "kbd":
                // Inline formatting falls through to child content.
                processChildren(element, options, depth + 1);
                break;
            default:
                // Unknown tags are flattened to preserve readable text.
                processChildren(element, options, depth + 1);
                break;
        }
    }

    /**
     * Processes all child nodes of an element.
     *
     * @param element parent element
     * @param options conversion options
     * @param depth nesting depth
     */
    private void processChildren(Element element, ConversionOptions options, int depth) {
        for (Node child : element.childNodes()) {
            processNode(child, options, depth);
        }
    }

    /**
     * Processes an ordered or unordered list.
     *
     * @param list list element
     * @param options conversion options
     * @param depth nesting depth
     */
    private void processList(Element list, ConversionOptions options, int depth) {
        mb.append("\n");
        for (Element li : list.select("li")) {
            String indent = "  ".repeat(Math.max(0, depth - 1));
            if (list.tagName().equals("ol")) {
                mb.append(indent).append("1. ");
            } else {
                mb.append(indent).append("- ");
            }
            processListItemContent(li, options, depth);
            mb.append("\n");
        }
        mb.append("\n");
    }

    /**
     * Processes a list item as a standalone node.
     *
     * @param li list item
     * @param options conversion options
     * @param depth nesting depth
     */
    private void processListItem(Element li, ConversionOptions options, int depth) {
        String text = li.text().trim();
        mb.append(text);
    }

    /**
     * Processes the contents of a list item, including nested lists.
     *
     * @param li list item
     * @param options conversion options
     * @param depth nesting depth
     */
    private void processListItemContent(Element li, ConversionOptions options, int depth) {
        for (Node child : li.childNodes()) {
            if (child instanceof Element) {
                Element childElement = (Element) child;
                if (childElement.tagName().equals("ul") || childElement.tagName().equals("ol")) {
                    processList(childElement, options, depth + 1);
                } else {
                    processNode(child, options, depth);
                }
            } else {
                processNode(child, options, depth);
            }
        }
    }

    /**
     * Processes an anchor element.
     *
     * @param link link element
     * @param options conversion options
     */
    private void processLink(Element link, ConversionOptions options) {
        String href = link.attr("href");
        String text = link.text();

        if (!text.isEmpty()) {
            if (!href.isEmpty()) {
                mb.append("[").append(text).append("](").append(href).append(")");
            } else {
                mb.append(text);
            }
        }
    }

    /**
     * Processes an image element.
     *
     * @param img image element
     * @param options conversion options
     */
    private void processImage(Element img, ConversionOptions options) {
        if (!options.content().includeImages()) {
            return;
        }

        String src = img.attr("src");
        String alt = img.attr("alt");

        if (!src.isEmpty()) {
            mb.append("![");
            if (!alt.isEmpty()) {
                mb.append(alt);
            }
            mb.append("](").append(src).append(")");
        }
    }

    /**
     * Processes an HTML table as a Markdown table.
     *
     * @param table table element
     * @param options conversion options
     */
    private void processTable(Element table, ConversionOptions options) {
        if (!options.content().includeTables()) {
            return;
        }

        Elements rows = table.select("tr");
        if (rows.isEmpty()) {
            return;
        }

        mb.append("\n");

        boolean isFirstRow = true;
        for (Element row : rows) {
            Elements cells = row.select("td, th");
            if (cells.isEmpty()) {
                continue;
            }

            mb.append("| ");
            for (Element cell : cells) {
                String cellText = cell.text().trim().replace("|", "\\|");
                mb.append(cellText).append(" | ");
            }
            mb.append("\n");

            // Use the first row as the header separator row.
            if (isFirstRow) {
                mb.append("|");
                for (int i = 0; i < cells.size(); i++) {
                    mb.append(" --- |");
                }
                mb.append("\n");
                isFirstRow = false;
            }
        }

        mb.append("\n");
    }
}
