package com.markitdown.api;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.Objects;

/**
 * Immutable-style result object returned by document converters.
 *
 * <p>The result bundles rendered Markdown, extracted metadata, warning
 * messages, basic source file information, and the success state of the
 * conversion.</p>
 */
public class ConversionResult {

    private final String markdownContent;
    private final Map<String, Object> metadata;
    private final List<String> warnings;
    private final LocalDateTime conversionTime;
    private final long fileSize;
    private final String originalFileName;
    private final boolean successful;

    /**
     * Creates a successful conversion result.
     *
     * @param markdownContent rendered Markdown content
     * @param metadata extracted metadata
     * @param warnings conversion warnings
     * @param fileSize source file size in bytes
     * @param originalFileName source file name
     */
    public ConversionResult(String markdownContent, Map<String, Object> metadata,
                            List<String> warnings, long fileSize, String originalFileName) {
        this.markdownContent = markdownContent != null ? markdownContent : "";
        this.metadata = new HashMap<>(metadata != null ? metadata : Collections.emptyMap());
        this.warnings = new ArrayList<>(warnings != null ? warnings : Collections.emptyList());
        this.conversionTime = LocalDateTime.now();
        this.fileSize = fileSize;
        this.originalFileName = originalFileName != null ? originalFileName : "";
        this.successful = true;
    }

    /**
     * Creates a failed conversion result.
     *
     * @param warnings failure warnings or error summaries
     * @param fileSize source file size in bytes
     * @param originalFileName source file name
     */
    public ConversionResult(List<String> warnings, long fileSize, String originalFileName) {
        this.markdownContent = "";
        this.metadata = Collections.emptyMap();
        this.warnings = new ArrayList<>(warnings != null ? warnings : Collections.emptyList());
        this.conversionTime = LocalDateTime.now();
        this.fileSize = fileSize;
        this.originalFileName = originalFileName != null ? originalFileName : "";
        this.successful = false;
    }

    /**
     * Returns the rendered text content.
     *
     * @return Markdown content
     */
    public String getTextContent() {
        return markdownContent;
    }

    /**
     * Returns an immutable metadata view.
     *
     * @return metadata map
     */
    public Map<String, Object> getMetadata() {
        return Collections.unmodifiableMap(metadata);
    }

    /**
     * Returns the warning list.
     *
     * @return immutable warnings list
     */
    public List<String> getWarnings() {
        return Collections.unmodifiableList(warnings);
    }

    /**
     * Returns the conversion timestamp.
     *
     * @return conversion timestamp
     */
    public LocalDateTime getConversionTime() {
        return conversionTime;
    }

    /**
     * Returns the source file size in bytes.
     *
     * @return file size
     */
    public long getFileSize() {
        return fileSize;
    }

    /**
     * Returns the original source file name.
     *
     * @return original file name
     */
    public String getOriginalFileName() {
        return originalFileName;
    }

    /**
     * Indicates whether the conversion succeeded.
     *
     * @return {@code true} when successful
     */
    public boolean isSuccessful() {
        return successful;
    }

    /**
     * Returns the rendered Markdown.
     *
     * @return Markdown content
     */
    public String getMarkdown() {
        return markdownContent;
    }

    /**
     * Looks up a metadata value by key.
     *
     * @param key metadata key
     * @return typed metadata value or {@code null}
     */
    @SuppressWarnings("unchecked")
    public <T> T getMetadata(String key) {
        return (T) metadata.get(key);
    }

    /**
     * Checks whether warnings were recorded.
     *
     * @return {@code true} when warnings exist
     */
    public boolean hasWarnings() {
        return !warnings.isEmpty();
    }

    @Override
    public String toString() {
        return "ConversionResult{" +
                "successful=" + successful +
                ", originalFileName='" + originalFileName + '\'' +
                ", fileSize=" + fileSize +
                ", markdownContentLength=" + markdownContent.length() +
                ", warningsCount=" + warnings.size() +
                ", conversionTime=" + conversionTime +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ConversionResult that = (ConversionResult) o;
        return fileSize == that.fileSize &&
                successful == that.successful &&
                Objects.equals(markdownContent, that.markdownContent) &&
                Objects.equals(metadata, that.metadata) &&
                Objects.equals(warnings, that.warnings) &&
                Objects.equals(conversionTime, that.conversionTime) &&
                Objects.equals(originalFileName, that.originalFileName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(markdownContent, metadata, warnings, conversionTime,
                fileSize, originalFileName, successful);
    }
}
