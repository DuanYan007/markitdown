package com.markitdown.model;

import java.nio.file.Path;
import java.util.Objects;

/**
 * @class ExtractedImage
 * @brief 表示从文档中提取的图片信息
 */
public class ExtractedImage {

    private final Path imagePath;           // 图片文件路径
    private final String relativePath;      // 相对于Markdown文件的路径
    private final String originalFilename;  // 原始文件名
    private final String format;            // 图片格式 (PNG, JPEG等)
    private final long size;                // 文件大小
    private final int index;                // 图片索引

    public ExtractedImage(Path imagePath, String relativePath, String originalFilename,
                         String format, long size, int index) {
        this.imagePath = Objects.requireNonNull(imagePath, "图片路径不能为空");
        this.relativePath = Objects.requireNonNull(relativePath, "相对路径不能为空");
        this.originalFilename = originalFilename;
        this.format = Objects.requireNonNull(format, "图片格式不能为空");
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
     * 获取Markdown格式的图片引用
     * @param altText 替代文本
     * @return Markdown图片语法
     */
    public String toMarkdown(String altText) {
        String alt = (altText != null && !altText.isEmpty()) ? altText : "图片" + index;
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