package com.markitdown.models;

import java.nio.file.Path;
import java.util.Objects;

/**
 * Metadata for an image extracted from a source document.
 */
public class ExtractedImage {

    private final Path imagePath;
    private final String relativePath;
    private final String originalFilename;
    private final String format;
    private final long size;
    private final int index;

    public ExtractedImage(Path imagePath, String relativePath, String originalFilename,
                          String format, long size, int index) {
        this.imagePath = Objects.requireNonNull(imagePath, "Image path cannot be null");
        this.relativePath = Objects.requireNonNull(relativePath, "Relative path cannot be null");
        this.originalFilename = originalFilename;
        this.format = Objects.requireNonNull(format, "Image format cannot be null");
        this.size = size;
        this.index = index;
    }

    public Path getImagePath() {
        return imagePath;
    }

    public String getRelativePath() {
        return relativePath;
    }

    public String getOriginalFilename() {
        return originalFilename;
    }

    public String getFormat() {
        return format;
    }

    public long getSize() {
        return size;
    }

    public int getIndex() {
        return index;
    }

    /**
     * Builds a Markdown image reference for the extracted asset.
     *
     * @param altText alt text to use when available
     * @return Markdown image syntax
     */
    public String toMarkdown(String altText) {
        String alt = (altText != null && !altText.isEmpty()) ? altText : "Image " + index;
        return "![" + alt + "](" + relativePath + ")";
    }

    @Override
    public String toString() {
        return "ExtractedImage{" +
                "path='" + imagePath + '\'' +
                ", relativePath='" + relativePath + '\'' +
                ", format='" + format + '\'' +
                ", size=" + size +
                ", index=" + index +
                '}';
    }
}
