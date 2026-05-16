package com.markdown.engine.config;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Configuration options for Markdown rendering helpers.
 *
 * <p>This model controls table rendering, list and heading styles, HTML
 * escaping, code block wrapping, and a small set of general formatting
 * switches. It also exposes a builder for fluent setup.</p>
 *
 * @author duan yan
 * @version 2.0.0
 * @since 2.0.0
 */
public class MarkdownConfig {

    private boolean includeTables = true;
    private boolean includeMetadata = false;
    private String tableFormat = "github"; // github, markdown, pipe
    private String listStyle = "dash"; // dash, asterisk, plus
    private String headingStyle = "atx"; // atx, setext
    private boolean escapeHtml = true;
    private boolean wrapCodeBlocks = true;
    private int maxListDepth = 10;
    private boolean sortMapKeys = false;
    private String dateFormat = "yyyy-MM-dd HH:mm:ss";
    private Map<String, Object> customOptions = new HashMap<>();

    /**
     * Creates a config instance with default values.
     */
    public MarkdownConfig() {
    }

    /**
     * Creates a copy of an existing configuration instance.
     *
     * @param other source configuration
     */
    public MarkdownConfig(MarkdownConfig other) {
        this.includeTables = other.includeTables;
        this.includeMetadata = other.includeMetadata;
        this.tableFormat = other.tableFormat;
        this.listStyle = other.listStyle;
        this.headingStyle = other.headingStyle;
        this.escapeHtml = other.escapeHtml;
        this.wrapCodeBlocks = other.wrapCodeBlocks;
        this.maxListDepth = other.maxListDepth;
        this.sortMapKeys = other.sortMapKeys;
        this.dateFormat = other.dateFormat;
        this.customOptions = new HashMap<>(other.customOptions);
    }

    // ==================== Table options ====================

    /**
     * Returns whether tables should be included in rendered output.
     *
     * @return {@code true} when table rendering is enabled
     */
    public boolean isIncludeTables() {
        return includeTables;
    }

    /**
     * Enables or disables table rendering.
     *
     * @param includeTables whether to include tables
     * @return current config instance
     */
    public MarkdownConfig setIncludeTables(boolean includeTables) {
        this.includeTables = includeTables;
        return this;
    }

    /**
     * Returns the configured table format.
     *
     * @return table format identifier
     */
    public String getTableFormat() {
        return tableFormat;
    }

    /**
     * Sets the table format.
     *
     * @param tableFormat requested table format
     * @return current config instance
     */
    public MarkdownConfig setTableFormat(String tableFormat) {
        this.tableFormat = validateTableFormat(tableFormat);
        return this;
    }

    // ==================== Metadata options ====================

    /**
     * Returns whether metadata should be included in output.
     *
     * @return {@code true} when metadata output is enabled
     */
    public boolean isIncludeMetadata() {
        return includeMetadata;
    }

    /**
     * Enables or disables metadata rendering.
     *
     * @param includeMetadata whether to include metadata
     * @return current config instance
     */
    public MarkdownConfig setIncludeMetadata(boolean includeMetadata) {
        this.includeMetadata = includeMetadata;
        return this;
    }

    // ==================== List options ====================

    /**
     * Returns the configured unordered list style.
     *
     * @return list style identifier
     */
    public String getListStyle() {
        return listStyle;
    }

    /**
     * Sets the unordered list style.
     *
     * @param listStyle requested list style
     * @return current config instance
     */
    public MarkdownConfig setListStyle(String listStyle) {
        this.listStyle = validateListStyle(listStyle);
        return this;
    }

    // ==================== Heading options ====================

    /**
     * Returns the configured heading style.
     *
     * @return heading style identifier
     */
    public String getHeadingStyle() {
        return headingStyle;
    }

    /**
     * Sets the heading style.
     *
     * @param headingStyle requested heading style
     * @return current config instance
     */
    public MarkdownConfig setHeadingStyle(String headingStyle) {
        this.headingStyle = validateHeadingStyle(headingStyle);
        return this;
    }

    // ==================== HTML escaping ====================

    /**
     * Returns whether HTML-sensitive characters should be escaped.
     *
     * @return {@code true} when HTML escaping is enabled
     */
    public boolean isEscapeHtml() {
        return escapeHtml;
    }

    /**
     * Enables or disables HTML escaping.
     *
     * @param escapeHtml whether to escape HTML-sensitive characters
     * @return current config instance
     */
    public MarkdownConfig setEscapeHtml(boolean escapeHtml) {
        this.escapeHtml = escapeHtml;
        return this;
    }

    // ==================== Code block options ====================

    /**
     * Returns whether fenced code blocks should be wrapped with triple backticks.
     *
     * @return {@code true} when fenced blocks are enabled
     */
    public boolean isWrapCodeBlocks() {
        return wrapCodeBlocks;
    }

    /**
     * Enables or disables fenced code blocks.
     *
     * @param wrapCodeBlocks whether to wrap code blocks
     * @return current config instance
     */
    public MarkdownConfig setWrapCodeBlocks(boolean wrapCodeBlocks) {
        this.wrapCodeBlocks = wrapCodeBlocks;
        return this;
    }

    // ==================== Nested list depth ====================

    /**
     * Returns the maximum supported nested list depth.
     *
     * @return maximum list depth
     */
    public int getMaxListDepth() {
        return maxListDepth;
    }

    /**
     * Sets the maximum nested list depth.
     *
     * @param maxListDepth requested depth
     * @return current config instance
     */
    public MarkdownConfig setMaxListDepth(int maxListDepth) {
        this.maxListDepth = Math.max(1, maxListDepth);
        return this;
    }

    // ==================== Map ordering ====================

    /**
     * Returns whether map keys should be sorted before rendering.
     *
     * @return {@code true} when map keys should be sorted
     */
    public boolean isSortMapKeys() {
        return sortMapKeys;
    }

    /**
     * Enables or disables map key sorting.
     *
     * @param sortMapKeys whether to sort map keys
     * @return current config instance
     */
    public MarkdownConfig setSortMapKeys(boolean sortMapKeys) {
        this.sortMapKeys = sortMapKeys;
        return this;
    }

    // ==================== Date formatting ====================

    /**
     * Returns the configured date format pattern.
     *
     * @return date format pattern
     */
    public String getDateFormat() {
        return dateFormat;
    }

    /**
     * Sets the date format pattern.
     *
     * @param dateFormat requested date format
     * @return current config instance
     */
    public MarkdownConfig setDateFormat(String dateFormat) {
        this.dateFormat = dateFormat != null ? dateFormat : "yyyy-MM-dd HH:mm:ss";
        return this;
    }

    // ==================== Custom options ====================

    /**
     * Returns a defensive copy of all custom options.
     *
     * @return custom option map copy
     */
    public Map<String, Object> getCustomOptions() {
        return new HashMap<>(customOptions);
    }

    /**
     * Stores a custom option value.
     *
     * @param key option key
     * @param value option value
     * @return current config instance
     */
    public MarkdownConfig setCustomOption(String key, Object value) {
        this.customOptions.put(key, value);
        return this;
    }

    /**
     * Returns a custom option value cast to the requested type.
     *
     * @param key option key
     * @param <T> expected type
     * @return stored option value
     */
    @SuppressWarnings("unchecked")
    public <T> T getCustomOption(String key) {
        return (T) customOptions.get(key);
    }

    // ==================== Validators ====================

    /**
     * Normalizes table format input.
     *
     * @param format requested table format
     * @return validated table format
     */
    private String validateTableFormat(String format) {
        Set<String> validFormats = Set.of("github", "markdown", "pipe");
        return validFormats.contains(format) ? format : "github";
    }

    /**
     * Normalizes list style input.
     *
     * @param style requested list style
     * @return validated list style
     */
    private String validateListStyle(String style) {
        Set<String> validStyles = Set.of("dash", "asterisk", "plus");
        return validStyles.contains(style) ? style : "dash";
    }

    /**
     * Normalizes heading style input.
     *
     * @param style requested heading style
     * @return validated heading style
     */
    private String validateHeadingStyle(String style) {
        Set<String> validStyles = Set.of("atx", "setext");
        return validStyles.contains(style) ? style : "atx";
    }

    // ==================== Builder ====================

    /**
     * Creates a fluent builder for configuration setup.
     *
     * @return builder instance
     */
    public static Builder builder() {
        return new Builder();
    }

    /**
     * Fluent builder for {@link MarkdownConfig}.
     */
    public static class Builder {
        private final MarkdownConfig config = new MarkdownConfig();

        private Builder() {
        }

        public Builder includeTables(boolean includeTables) {
            config.setIncludeTables(includeTables);
            return this;
        }

        public Builder includeMetadata(boolean includeMetadata) {
            config.setIncludeMetadata(includeMetadata);
            return this;
        }

        public Builder tableFormat(String tableFormat) {
            config.setTableFormat(tableFormat);
            return this;
        }

        public Builder listStyle(String listStyle) {
            config.setListStyle(listStyle);
            return this;
        }

        public Builder headingStyle(String headingStyle) {
            config.setHeadingStyle(headingStyle);
            return this;
        }

        public Builder escapeHtml(boolean escapeHtml) {
            config.setEscapeHtml(escapeHtml);
            return this;
        }

        public Builder wrapCodeBlocks(boolean wrapCodeBlocks) {
            config.setWrapCodeBlocks(wrapCodeBlocks);
            return this;
        }

        public Builder maxListDepth(int maxListDepth) {
            config.setMaxListDepth(maxListDepth);
            return this;
        }

        public Builder sortMapKeys(boolean sortMapKeys) {
            config.setSortMapKeys(sortMapKeys);
            return this;
        }

        public Builder dateFormat(String dateFormat) {
            config.setDateFormat(dateFormat);
            return this;
        }

        public Builder customOption(String key, Object value) {
            config.setCustomOption(key, value);
            return this;
        }

        public MarkdownConfig build() {
            return new MarkdownConfig(config);
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof MarkdownConfig)) return false;

        MarkdownConfig that = (MarkdownConfig) o;
        return includeTables == that.includeTables &&
               includeMetadata == that.includeMetadata &&
               escapeHtml == that.escapeHtml &&
               wrapCodeBlocks == that.wrapCodeBlocks &&
               maxListDepth == that.maxListDepth &&
               sortMapKeys == that.sortMapKeys &&
               tableFormat.equals(that.tableFormat) &&
               listStyle.equals(that.listStyle) &&
               headingStyle.equals(that.headingStyle) &&
               dateFormat.equals(that.dateFormat) &&
               customOptions.equals(that.customOptions);
    }

    @Override
    public int hashCode() {
        int result = Boolean.hashCode(includeTables);
        result = 31 * result + Boolean.hashCode(includeMetadata);
        result = 31 * result + tableFormat.hashCode();
        result = 31 * result + listStyle.hashCode();
        result = 31 * result + headingStyle.hashCode();
        result = 31 * result + Boolean.hashCode(escapeHtml);
        result = 31 * result + Boolean.hashCode(wrapCodeBlocks);
        result = 31 * result + maxListDepth;
        result = 31 * result + Boolean.hashCode(sortMapKeys);
        result = 31 * result + dateFormat.hashCode();
        result = 31 * result + customOptions.hashCode();
        return result;
    }

    @Override
    public String toString() {
        return "MarkdownConfig{" +
               "includeTables=" + includeTables +
               ", includeMetadata=" + includeMetadata +
               ", tableFormat='" + tableFormat + '\'' +
               ", listStyle='" + listStyle + '\'' +
               ", headingStyle='" + headingStyle + '\'' +
               ", escapeHtml=" + escapeHtml +
               ", wrapCodeBlocks=" + wrapCodeBlocks +
               ", maxListDepth=" + maxListDepth +
               ", sortMapKeys=" + sortMapKeys +
               ", dateFormat='" + dateFormat + '\'' +
               ", customOptions=" + customOptions +
               '}';
    }
}
