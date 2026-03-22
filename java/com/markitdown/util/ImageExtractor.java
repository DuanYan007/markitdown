package com.markitdown.util;

import com.markitdown.model.ExtractedImage;
import org.apache.poi.xwpf.usermodel.XWPFPictureData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

/**
 * @class ImageExtractor
 * @brief 图片提取工具类，负责从文档中提取图片并保存到指定位置
 */
public class ImageExtractor {

    private static final Logger logger = LoggerFactory.getLogger(ImageExtractor.class);

    private final Path outputDir;          // 图片输出目录
    private final String documentName;      // 文档名称（用于生成图片文件名）
    private int imageIndex = 0;             // 图片计数器

    /**
     * 创建图片提取器
     * @param baseOutputPath 基础输出路径（Markdown文件所在目录）
     * @param imageDirName 图片子目录名称
     * @param documentName 文档名称
     * @throws IOException 如果创建图片目录失败
     */
    public ImageExtractor(Path baseOutputPath, String imageDirName, String documentName) throws IOException {
        this.documentName = sanitizeFilename(documentName);

        // 创建图片输出目录
        this.outputDir = baseOutputPath.resolve(imageDirName);
        if (!Files.exists(outputDir)) {
            Files.createDirectories(outputDir);
            logger.info("创建图片输出目录: {}", outputDir);
        }
    }

    /**
     * 从Word文档中提取所有图片
     * @param pictures Word文档中的图片数据列表
     * @param baseOutputPath 基础输出路径（用于计算相对路径）
     * @return 提取的图片信息列表
     */
    public List<ExtractedImage> extractPictures(List<XWPFPictureData> pictures, Path baseOutputPath) {
        List<ExtractedImage> extractedImages = new ArrayList<>();

        if (pictures == null || pictures.isEmpty()) {
            logger.info("文档中没有找到图片");
            return extractedImages;
        }

        logger.info("开始提取{}张图片...", pictures.size());

        for (XWPFPictureData picture : pictures) {
            try {
                ExtractedImage image = extractSinglePicture(picture, baseOutputPath);
                if (image != null) {
                    extractedImages.add(image);
                    logger.debug("成功提取图片: {} ({})", image.getRelativePath(), formatSize(image.getSize()));
                }
            } catch (Exception e) {
                logger.warn("提取图片失败: {}", e.getMessage());
            }
        }

        logger.info("图片提取完成，成功{}张，失败{}张", extractedImages.size(), pictures.size() - extractedImages.size());
        return extractedImages;
    }

    /**
     * 提取单张图片
     */
    private ExtractedImage extractSinglePicture(XWPFPictureData picture, Path baseOutputPath) throws IOException {
        // 获取图片数据
        byte[] imageData = picture.getData();
        String format = getFormatName(picture.getPictureType());

        // 生成文件名
        String filename = String.format("%s_image_%d.%s", documentName, imageIndex++, format);

        // 保存图片
        Path imagePath = outputDir.resolve(filename);
        Files.write(imagePath, imageData);

        // 计算相对路径
        String relativePath = baseOutputPath.relativize(imagePath).toString().replace("\\", "/");

        return new ExtractedImage(
                imagePath,
                relativePath,
                picture.getFileName(),
                format,
                imageData.length,
                imageIndex - 1
        );
    }

    /**
     * 获取图片格式名称
     */
    private String getFormatName(int pictureType) {
        switch (pictureType) {
            case 6: return "png";    // PICTURE_TYPE_PNG
            case 5: return "jpg";    // PICTURE_TYPE_JPEG
            case 7: return "gif";    // PICTURE_TYPE_GIF
            case 2: return "bmp";    // PICTURE_TYPE_BMP
            case 4: return "emf";    // PICTURE_TYPE_EMF
            case 3: return "wmf";    // PICTURE_TYPE_WMF
            default: return "unknown";
        }
    }

    /**
     * 清理文件名，移除不安全字符
     */
    private String sanitizeFilename(String filename) {
        if (filename == null) {
            return "document";
        }
        // 移除文件扩展名
        String name = filename.contains(".") ?
                filename.substring(0, filename.lastIndexOf('.')) : filename;

        // 替换不安全字符
        return name.replaceAll("[^a-zA-Z0-9._-]", "_");
    }

    /**
     * 格式化文件大小
     */
    private String formatSize(long bytes) {
        if (bytes < 1024) return bytes + " B";
        if (bytes < 1024 * 1024) return String.format("%.1f KB", bytes / 1024.0);
        return String.format("%.1f MB", bytes / (1024.0 * 1024.0));
    }

    /**
     * 获取图片输出目录
     */
    public Path getOutputDir() {
        return outputDir;
    }
}