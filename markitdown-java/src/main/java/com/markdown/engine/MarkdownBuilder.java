package com.markdown.engine;

import com.markdown.engine.config.MarkdownConfig;
import com.markdown.engine.context.RenderContext;

import java.util.Collection;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Stateful Markdown rendering helper used by converters and the core engine.
 */
public class MarkdownBuilder {

    private final StringBuilder content;
    private final RenderContext context;

    public MarkdownBuilder() {
        this(MarkdownConfig.builder().build());
    }

    public MarkdownBuilder(MarkdownConfig config) {
        this.context = new RenderContext(config);
        this.content = new StringBuilder();
    }

    public MarkdownBuilder(RenderContext context) {
        this.context = context;
        this.content = new StringBuilder();
    }

    public MarkdownBuilder(boolean includeTables, boolean escapeHtml, boolean wrapCodeBlocks) {
        MarkdownConfig config = MarkdownConfig.builder()
                .includeTables(includeTables)
                .escapeHtml(escapeHtml)
                .wrapCodeBlocks(wrapCodeBlocks)
                .build();
        this.context = new RenderContext(config);
        this.content = new StringBuilder();
    }
    public MarkdownBuilder append(String text) {
        content.append(text);
        return this;
    }

    public MarkdownBuilder append(StringBuilder text) {
        content.append(text);
        return this;
    }
    public StringBuilder heading(String text, int level) {
        StringBuilder ans = new StringBuilder();
        if (text == null || text.trim().isEmpty()) {
            return ans;
        }

        int safeLevel = Math.max(1, Math.min(6, level));
        String headingStyle = context.getHeadingStyle();

        if ("setext".equals(headingStyle) && safeLevel <= 2) {
            ans.append(text.trim()).append(System.lineSeparator());
            if (safeLevel == 1) {
                ans.append("=".repeat(text.trim().length()));
            } else {
                ans.append("-".repeat(text.trim().length()));
            }
        } else {
            ans.append("#".repeat(safeLevel))
                    .append(" ")
                    .append(text.trim());
        }

        ans.append(System.lineSeparator())
                .append(System.lineSeparator());
        return ans;
    }
    public StringBuilder h1(String text) {
        return heading(text, 1);
    }
    public StringBuilder h2(String text) {
        return heading(text, 2);
    }
    public StringBuilder h3(String text) {
        return heading(text, 3);
    }
    public StringBuilder h4(String text) {
        return heading(text, 4);
    }
    public StringBuilder h5(String text) {
        return heading(text, 5);
    }
    public StringBuilder h6(String text) {
        return heading(text, 6);
    }

    public StringBuilder paragraph(String text) {
        StringBuilder ans = new StringBuilder();
        if (text != null && !text.trim().isEmpty()) {
            ans.append(text.trim());
        }
        return ans;
    }

    public MarkdownBuilder text(String text) {
        content.append(paragraph(text));
        return this;
    }

    public StringBuilder bold(String text) {
        StringBuilder ans = new StringBuilder();
        if (text != null) {
            ans.append("**").append(text).append("**");
        }
        return ans;
    }

    public StringBuilder italic(String text) {
        StringBuilder ans = new StringBuilder();
        if (text != null) {
            ans.append("*").append(text).append("*");
        }
        return ans;
    }

    public StringBuilder inlineCode(String text) {
        StringBuilder ans = new StringBuilder();
        if (text != null) {
            ans.append("`").append(text).append("`");
        }
        return ans;
    }

    public StringBuilder codeBlock(String code, String language) {
        StringBuilder ans = new StringBuilder();
        if (code != null) {
            if (context.shouldWrapCodeBlocks()) {
                ans.append("```");
                if (language != null && !language.trim().isEmpty()) {
                    ans.append(language.trim());
                }
                ans.append(System.lineSeparator());
            }
            ans.append(code);
            if (context.shouldWrapCodeBlocks()) {
                ans.append(System.lineSeparator()).append("```");
            }
            ans.append(System.lineSeparator()).append(System.lineSeparator());
        }
        return ans;
    }

    public String unorder_item(String s){
        return "- " + s;
    }

    public StringBuilder unorderedList(int level, StringBuilder... items) {
        StringBuilder ans = new StringBuilder();
        if (items != null) {
            String marker = getListMarker("unordered");
            String indent = "  ".repeat(level);

            for (StringBuilder item : items) {
                if (item != null) {
                    ans.append(indent)
                            .append(marker)
                            .append(" ")
                            .append(item)
                            .append(System.lineSeparator());
                }
            }
            ans.append(System.lineSeparator());
        }
        return ans;
    }

    public StringBuilder orderedList(int level, int startNumber, String[] items) {
        StringBuilder ans = new StringBuilder();
        if (items != null) {
            String indent = "  ".repeat(level);

            for (int i = 0; i < items.length; i++) {
                String item = items[i];
                if (item != null && !item.trim().isEmpty()) {
                    ans.append(indent)
                            .append((startNumber + i) + ". ")
                            .append(item.trim())
                            .append(System.lineSeparator());
                }
            }
            ans.append(System.lineSeparator());
        }
        return ans;
    }
    public StringBuilder table(String[] headers, String[][] rows) {
        StringBuilder ans = new StringBuilder();
        if (!context.shouldIncludeTables() || headers == null || headers.length == 0) {
            return ans;
        }

        boolean fencedPipeTable = !"markdown".equalsIgnoreCase(context.getTableFormat());
        appendTableRow(ans, headers, headers.length, fencedPipeTable);

        String[] separatorRow = new String[headers.length];
        for (int i = 0; i < headers.length; i++) {
            separatorRow[i] = "-----";
        }
        appendTableRow(ans, separatorRow, separatorRow.length, fencedPipeTable);

        if (rows != null) {
            for (String[] row : rows) {
                appendTableRow(ans, row, headers.length, fencedPipeTable);
            }
        }

        ans.append(System.lineSeparator());
        return ans;
    }

    public StringBuilder blockquote(String text) {
        StringBuilder ans = new StringBuilder();
        if (text != null) {
            String[] lines = text.split("\\r?\\n");
            for (String line : lines) {
                ans.append("> ").append(line).append(System.lineSeparator());
            }
            ans.append(System.lineSeparator());
        }
        return ans;
    }

    public MarkdownBuilder horizontalRule() {
        content.append("---")
                .append(System.lineSeparator())
                .append(System.lineSeparator());
        return this;
    }
    public MarkdownBuilder link(String text, String url) {
        if (text != null && url != null) {
            content.append("[").append(escapeMarkdown(text)).append("](")
                    .append(url).append(")"); // URL should not be escaped
        }
        return this;
    }

    public MarkdownBuilder image(String altText, String url, String title) {
        if (url != null) {
            content.append("![")
                    .append(altText != null ? escapeMarkdown(altText) : "")
                    .append("](")
                    .append(url); // URL should not be escaped

            if (title != null && !title.trim().isEmpty()) {
                content.append(" \"").append(escapeMarkdown(title.trim())).append("\"");
            }
            content.append(")");
        }
        return this;
    }

    public MarkdownBuilder lineBreak() {
        content.append("  ").append(System.lineSeparator());
        return this;
    }
    public MarkdownBuilder newline() {
        content.append(System.lineSeparator());
        return this;
    }
    public MarkdownBuilder newline(int count) {
        for (int i = 0; i < count; i++) {
            content.append(System.lineSeparator());
        }
        return this;
    }
    public MarkdownBuilder raw(String text) {
        if (text != null) {
            content.append(text);
        }
        return this;
    }

    public String build() {
        return content.toString();
    }

    public MarkdownBuilder clear() {
        content.setLength(0);
        return this;
    }

    public String flush() {
        String flushedContent = content.toString();
        context.getOutput().append(flushedContent);
        content.setLength(0);
        return flushedContent;
    }


    public int length() {
        return content.length();
    }

    public RenderContext getContext() {
        return context;
    }


    public MarkdownBuilder document(String title, Map<String, Object> metadata, String content) {
        if (title != null && !title.trim().isEmpty()) {
            heading(title, 1);
        }

        if (metadata != null && !metadata.isEmpty()) {
            heading("Document Information", 2);
            for (Map.Entry<String, Object> entry : metadata.entrySet()) {
                if (entry.getValue() != null) {
                    String key = formatMetadataKey(entry.getKey());
                    String value = formatMetadataValue(entry.getValue());
                    text("- **").text(key).text(":** ").text(value).newline();
                }
            }
            newline();
        }

        if (content != null && !content.trim().isEmpty()) {
            heading("Content", 2);
            raw(content).newline();
        }

        return this;
    }
    public MarkdownBuilder header(Map<String, Object> metadata) {

        this.append(heading(escapeMarkdown(resolveDocumentTitle(metadata)), 1));
        this.append(heading("Document Information", 2));
        for (Map.Entry<String, Object> entry : metadata.entrySet()) {
            if (entry.getValue() != null) {
                String key = formatMetadataKey(entry.getKey());
                String value = entry.getValue().toString();
                raw("- **" + key + ":** " + value).newline();
            }
        }
        newline();

        return this;
    }

    public MarkdownBuilder escaped(String text) {
        if (text != null) {
            raw(escapeMarkdown(text));
        }
        return this;
    }


    public boolean isValidContent() {
        return isValidMarkdown(build());
    }

    public static boolean isValidMarkdown(String markdown) {
        if (markdown == null) {
            return false;
        }

        int openBrackets = markdown.length() - markdown.replace("[", "").length();
        int closeBrackets = markdown.length() - markdown.replace("]", "").length();
        if (openBrackets != closeBrackets) {
            return false;
        }

        int openParens = markdown.length() - markdown.replace("(", "").length();
        int closeParens = markdown.length() - markdown.replace(")", "").length();
        if (openParens != closeParens) {
            return false;
        }

        if (markdown.contains("[](")) {
            return false;
        }

        if (markdown.matches(".*\\[\\s*\\]\\([^)]*\\).*")) {
            return false;
        }

        return true;
    }


        public static String prettifyMetadataKey(String key) {
        if (key == null) {
            return "";
        }

        String trimmed = key.trim();
        Map<String, String> aliases = new LinkedHashMap<>();
        aliases.put("File Name", "File Name");
        aliases.put("File Size", "File Size");
        aliases.put("File Type", "File Type");
        aliases.put("Converted At", "Converted At");
        aliases.put("Width", "Width");
        aliases.put("Height", "Height");
        aliases.put("Format", "Format");
        aliases.put("Color Type", "Color Type");
        aliases.put("Pages", "Pages");
        aliases.put("Title", "Title");
        aliases.put("Author", "Author");
        aliases.put("Subject", "Subject");
        aliases.put("Creator", "Creator");
        aliases.put("PDF Producer", "PDF Producer");
        aliases.put("Producer", "Producer");
        aliases.put("Line Count", "Line Count");
        aliases.put("Character Count", "Character Count");
        aliases.put("Word Count", "Word Count");
        aliases.put("Sheet Count", "Sheet Count");
        aliases.put("Slide Count", "Slide Count");
        aliases.put("Slide Width", "Slide Width");
        aliases.put("Slide Height", "Slide Height");
        aliases.put("Archive Entry Count", "Archive Entry Count");
        aliases.put("Column Count", "Column Count");
        aliases.put("Data Row Count", "Data Row Count");
        aliases.put("Has Header", "Has Header");
        aliases.put("Valid JSON", "Valid JSON");
        aliases.put("Valid XML", "Valid XML");
        aliases.put("Root Element", "Root Element");
        aliases.put("Root Namespace", "Root Namespace");
        aliases.put("Element Count", "Element Count");
        aliases.put("Attribute Count", "Attribute Count");
        aliases.put("Text Node Count", "Text Node Count");
        aliases.put("Has CDATA", "Has CDATA");
        aliases.put("Has Comments", "Has Comments");
        aliases.put("Namespace Count", "Namespace Count");
        aliases.put("Validation Error", "Validation Error");
        aliases.put("Processing Error", "Processing Error");

        String alias = aliases.get(trimmed);
        if (alias != null) {
            return alias;
        }

        String spaced = trimmed.replaceAll("([a-z0-9])([A-Z])", "$1 $2")
                .replace('_', ' ')
                .replace('-', ' ')
                .trim();
        if (spaced.isEmpty()) {
            return "";
        }

        return Character.toUpperCase(spaced.charAt(0)) + spaced.substring(1);
    }
    private String formatMetadataKey(String key) {
        return prettifyMetadataKey(key);
    }

    private String formatMetadataValue(Object value) {
        if (value == null) {
            return "";
        }

        if (value instanceof Date) {
            java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            return sdf.format((Date) value);
        }

        if (value instanceof Collection) {
            Collection<?> collection = (Collection<?>) value;
            return "[" + String.join(", ", collection.stream().map(Object::toString).toArray(String[]::new)) + "]";
        }

        if (value instanceof Map) {
            Map<?, ?> map = (Map<?, ?>) value;
            return String.valueOf(map.size()) + " items";
        }

        return value.toString();
    }

    private String resolveDocumentTitle(Map<String, Object> metadata) {
        if (metadata == null || metadata.isEmpty()) {
            return "Document";
        }

        for (Map.Entry<String, Object> entry : metadata.entrySet()) {
            if (entry.getValue() == null) {
                continue;
            }
            if ("File Name".equals(prettifyMetadataKey(entry.getKey()))) {
                String value = String.valueOf(entry.getValue()).trim();
                if (!value.isEmpty()) {
                    return value;
                }
            }
        }

        Object title = metadata.get("title");
        if (title != null) {
            String value = String.valueOf(title).trim();
            if (!value.isEmpty()) {
                return value;
            }
        }

        return "Document";
    }

    private String getListMarker(String listType) {
        String style = context.getListStyle();
        if ("unordered".equals(listType)) {
            switch (style) {
                case "asterisk":
                    return "*";
                case "plus":
                    return "+";
                default:
                    return "-";
            }
        }
        return "-";
    }

    private void appendTableRow(StringBuilder ans, String[] row, int columnCount, boolean fencedPipeTable) {
        if (fencedPipeTable) {
            ans.append("| ");
        }

        for (int i = 0; i < columnCount; i++) {
            if (i > 0) {
                ans.append(" | ");
            }
            String cell = (row != null && i < row.length) ? row[i] : "";
            ans.append(cell != null ? cell.trim() : "");
        }

        if (fencedPipeTable) {
            ans.append(" |");
        }

        ans.append(System.lineSeparator());
    }

    public String escapeMarkdown(String text) {
        if (text == null) {
            return "";
        }

        if (context.shouldEscapeHtml()) {
            text = text.replace("<", "&lt;").replace(">", "&gt;");
        }

        return text.replace("\\", "\\\\")
                .replace("*", "\\*")
                .replace("_", "\\_")
                .replace("`", "\\`")
                .replace("[", "\\[")
                .replace("]", "\\]")
                .replace("(", "\\(")
                .replace(")", "\\)")
                .replace("#", "\\#")
                .replace("+", "\\+")
                .replace("-", "\\-")
                .replace(".", "\\.")
                .replace("!", "\\!");
    }

}

