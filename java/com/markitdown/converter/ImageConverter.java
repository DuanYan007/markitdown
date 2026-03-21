package com.markitdown.converter;

import com.markitdown.api.ConversionResult;
import com.markitdown.api.DocumentConverter;
import com.markitdown.config.ConversionOptions;
import com.markitdown.exception.ConversionException;
import net.sourceforge.tess4j.ITesseract;
import net.sourceforge.tess4j.Tesseract;
import net.sourceforge.tess4j.TesseractException;
import org.apache.tika.Tika;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.parser.AutoDetectParser;
import org.apache.tika.parser.ParseContext;
import org.apache.tika.parser.Parser;
import org.apache.tika.sax.BodyContentHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.*;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.*;

import static java.util.Objects.requireNonNull;

/**
 * @class ImageConverter
 * @brief 图片转换器，用于从图片文件中提取文本信息和元数据
 * @details 使用Tesseract OCR引擎进行光学字符识别，支持多种图片格式
 *          使用 Apache Tika 提取 EXIF 元数据（相机信息、GPS等）
 *          提取图片尺寸信息、颜色类型和OCR识别文本
 *          支持多语言识别和文本清理优化
 *          支持流式处理
 *
 * @author duan yan
 * @version 2.1.0
 * @since 2.0.0
 */
public class ImageConverter implements DocumentConverter {

    private static final Logger logger = LoggerFactory.getLogger(ImageConverter.class);

    /**
     * @brief 支持的图片格式集合
     */
    private static final Set<String> SUPPORTED_FORMATS = Set.of("png", "jpg", "jpeg", "gif", "bmp", "tiff", "tif", "webp");

    /**
     * @brief Tesseract OCR引擎实例
     */
    private final ITesseract tesseract;

    /**
     * @brief 默认构造函数
     */
    public ImageConverter() {
        this.tesseract = new Tesseract();
    }

    /**
     * @brief 自定义 Tesseract 实例的构造函数
     */
    public ImageConverter(ITesseract tesseract) {
        this.tesseract = requireNonNull(tesseract, "Tesseract instance cannot be null");
    }

    @Override
    public ConversionResult convert(Path filePath, ConversionOptions options) throws ConversionException {
        requireNonNull(filePath, "File path cannot be null");
        requireNonNull(options, "Conversion options cannot be null");

        logger.info("Converting image file: {}", filePath);

        try {
            // 加载图片
            BufferedImage image = ImageIO.read(filePath.toFile());
            if (image == null) {
                throw new ConversionException("Cannot load image file: " + filePath,
                        filePath.getFileName().toString(), getName());
            }

            // 提取元数据（包括 EXIF）
            Map<String, Object> metadata = extractMetadata(filePath, image, options);

            // 如果启用则执行 OCR 识别
            String extractedText = "";
            if (options.isUseOcr()) {
                try {
                    extractedText = performOcr(image, options);
                } catch (ConversionException e) {
                    logger.warn("OCR failed: {}", e.getMessage());
                    extractedText = "*OCR processing failed: " + e.getMessage() + "*";
                }
            } else {
                extractedText = "*OCR is disabled in conversion options*";
            }

            // 转换为 Markdown 格式
            String markdownContent = convertToMarkdown(extractedText, metadata, options, filePath);

            List<String> warnings = new ArrayList<>();

            return new ConversionResult(markdownContent, metadata, warnings,
                    filePath.toFile().length(), filePath.getFileName().toString());

        } catch (IOException e) {
            String errorMessage = "Failed to read image file: " + e.getMessage();
            logger.error(errorMessage, e);
            throw new ConversionException(errorMessage, e, filePath.getFileName().toString(), getName());
        }
    }

    @Override
    public boolean supports(String mimeType) {
        return mimeType != null && mimeType.startsWith("image/");
    }

    @Override
    public boolean supportsStreaming() {
        return false; // OCR 需要完整图片数据
    }

    @Override
    public int getPriority() {
        return 80;
    }

    @Override
    public String getName() {
        return "ImageConverter";
    }

    /**
     * @brief 从图片文件中提取元数据（包括 EXIF）
     */
    private Map<String, Object> extractMetadata(Path filePath, BufferedImage image, ConversionOptions options) {
        Map<String, Object> metadata = new LinkedHashMap<>();

        if (options.isIncludeMetadata()) {
            // 基本图片信息
            metadata.put("宽度", image.getWidth());
            metadata.put("高度", image.getHeight());

            String fileName = filePath.getFileName().toString();
            String format = getFileExtension(fileName).toLowerCase();
            metadata.put("格式", format.toUpperCase());
            metadata.put("颜色类型", getColorType(image));
            metadata.put("文件大小", filePath.toFile().length());

            // 使用 Tika 提取 EXIF 元数据
            try {
                Map<String, Object> exifData = extractExifMetadata(filePath);
                if (!exifData.isEmpty()) {
                    metadata.putAll(exifData);
                }
            } catch (Exception e) {
                logger.debug("Could not extract EXIF metadata: {}", e.getMessage());
            }

            metadata.put("转换时刻", LocalDateTime.now());
        }

        return metadata;
    }

    /**
     * @brief 使用 Apache Tika 提取 EXIF 元数据
     */
    private Map<String, Object> extractExifMetadata(Path filePath) {
        Map<String, Object> exifData = new LinkedHashMap<>();

        try (InputStream stream = new FileInputStream(filePath.toFile())) {
            Tika tika = new Tika();
            Metadata metadata = new Metadata();

            // 使用 AutoDetectParser 解析
            Parser parser = new AutoDetectParser();
            BodyContentHandler handler = new BodyContentHandler();
            ParseContext context = new ParseContext();
            context.set(Parser.class, parser);

            parser.parse(stream, handler, metadata, context);

            // 提取常见的 EXIF 字段
            addIfNotEmpty(exifData, "相机品牌", metadata.get("Equipment Make"));
            addIfNotEmpty(exifData, "相机型号", metadata.get("Equipment Model"));
            addIfNotEmpty(exifData, "拍摄时间", metadata.get("Date/Time Original"));
            addIfNotEmpty(exifData, "曝光时间", metadata.get("Exposure Time"));
            addIfNotEmpty(exifData, "光圈值", metadata.get("F-Number"));
            addIfNotEmpty(exifData, "ISO感光度", metadata.get("ISO Speed Ratings"));
            addIfNotEmpty(exifData, "焦距", metadata.get("Focal Length"));
            addIfNotEmpty(exifData, "闪光灯", metadata.get("Flash"));
            addIfNotEmpty(exifData, "白平衡", metadata.get("White Balance"));
            addIfNotEmpty(exifData, "方位", metadata.get("Orientation"));
            addIfNotEmpty(exifData, "X分辨率", metadata.get("X Resolution"));
            addIfNotEmpty(exifData, "Y分辨率", metadata.get("Y Resolution"));
            addIfNotEmpty(exifData, "分辨率单位", metadata.get("Resolution Units"));
            addIfNotEmpty(exifData, "软件", metadata.get("Software"));
            addIfNotEmpty(exifData, "艺术家", metadata.get("Artist"));
            addIfNotEmpty(exifData, "版权", metadata.get("Copyright Notice"));

            // GPS 信息
            String gpsLatitude = metadata.get("GPS Latitude");
            String gpsLongitude = metadata.get("GPS Longitude");
            if (gpsLatitude != null || gpsLongitude != null) {
                StringBuilder gps = new StringBuilder();
                if (gpsLatitude != null) gps.append("纬度: ").append(gpsLatitude);
                if (gpsLongitude != null) {
                    if (gps.length() > 0) gps.append(", ");
                    gps.append("经度: ").append(gpsLongitude);
                }
                exifData.put("GPS位置", gps.toString());
            }

        } catch (Exception e) {
            logger.debug("EXIF extraction error: {}", e.getMessage());
        }

        return exifData;
    }

    /**
     * @brief 如果值不为空则添加到 map
     */
    private void addIfNotEmpty(Map<String, Object> map, String key, String value) {
        if (value != null && !value.trim().isEmpty()) {
            map.put(key, value);
        }
    }

    /**
     * @brief 使用 Tesseract 对图片进行 OCR 识别
     */
    private String performOcr(BufferedImage image, ConversionOptions options) throws ConversionException {
        try {
            // 设置 OCR 识别语言
            String language = options.getLanguage();
            if (!"auto".equals(language) && !language.isEmpty()) {
                tesseract.setLanguage(language);
            } else {
                // 默认支持中英文
                tesseract.setLanguage("chi_sim+eng");
            }

            // 执行 OCR 识别
            String result = tesseract.doOCR(image);

            // 清理识别结果
            return cleanupOcrResult(result);

        } catch (TesseractException e) {
            String errorMessage = "OCR processing failed: " + e.getMessage();
            logger.error(errorMessage, e);
            throw new ConversionException(errorMessage, e, "image", getName());
        }
    }

    /**
     * @brief 清理 OCR 识别结果
     */
    private String cleanupOcrResult(String ocrText) {
        if (ocrText == null || ocrText.trim().isEmpty()) {
            return "";
        }

        // 保留段落结构
        String cleaned = ocrText.replaceAll("[ \\t]+", " ");
        cleaned = cleaned.replaceAll("\\n{3,}", "\n\n");

        return cleaned.trim();
    }

    /**
     * @brief 将提取的文本转换为 Markdown 格式
     */
    private String convertToMarkdown(String extractedText, Map<String, Object> metadata,
                                   ConversionOptions options, Path filePath) {
        StringBuilder markdown = new StringBuilder();

        // 添加标题
        String fileName = filePath.getFileName().toString();
        markdown.append("# Image: ").append(getFileNameWithoutExtension(fileName)).append("\n\n");

        // 如果启用则添加图片引用
        if (options.isIncludeImages()) {
            markdown.append("![").append(fileName).append("](").append(fileName).append(")\n\n");
        }

        // 如果启用则添加元数据部分
        if (options.isIncludeMetadata() && !metadata.isEmpty()) {
            markdown.append("## Image Information\n\n");
            for (Map.Entry<String, Object> entry : metadata.entrySet()) {
                if (entry.getValue() != null) {
                    markdown.append("- **").append(entry.getKey())
                            .append(":** ").append(entry.getValue()).append("\n");
                }
            }
            markdown.append("\n");
        }

        // 添加提取的文本内容
        markdown.append("## Extracted Text\n\n");

        if (extractedText == null || extractedText.isEmpty() || extractedText.contains("OCR is disabled") || extractedText.contains("OCR processing failed")) {
            markdown.append(extractedText).append("\n\n");
        } else {
            // 格式化提取的文本
            String formattedText = formatExtractedText(extractedText);
            markdown.append(formattedText).append("\n\n");
        }

        return markdown.toString();
    }

    /**
     * @brief 格式化提取的文本
     */
    private String formatExtractedText(String text) {
        if (text == null || text.trim().isEmpty()) {
            return "";
        }

        // 分割为段落
        String[] paragraphs = text.split("\\n\\s*\\n");
        StringBuilder formatted = new StringBuilder();

        for (String paragraph : paragraphs) {
            String trimmed = paragraph.trim();
            if (!trimmed.isEmpty()) {
                formatted.append(trimmed).append("\n\n");
            }
        }

        return formatted.toString();
    }

    /**
     * @brief 获取图片的颜色类型
     */
    private String getColorType(BufferedImage image) {
        switch (image.getType()) {
            case BufferedImage.TYPE_INT_RGB:
                return "RGB (24-bit)";
            case BufferedImage.TYPE_INT_ARGB:
                return "ARGB (32-bit with alpha)";
            case BufferedImage.TYPE_INT_BGR:
                return "BGR (24-bit)";
            case BufferedImage.TYPE_3BYTE_BGR:
                return "BGR (3-byte)";
            case BufferedImage.TYPE_4BYTE_ABGR:
                return "ABGR (4-byte with alpha)";
            case BufferedImage.TYPE_BYTE_GRAY:
                return "Grayscale (8-bit)";
            case BufferedImage.TYPE_BYTE_BINARY:
                return "Binary (1-bit)";
            case BufferedImage.TYPE_USHORT_555_RGB:
                return "RGB 555 (15-bit)";
            case BufferedImage.TYPE_USHORT_565_RGB:
                return "RGB 565 (16-bit)";
            default:
                return "Unknown (" + image.getType() + ")";
        }
    }

    /**
     * @brief 获取文件扩展名
     */
    private String getFileExtension(String fileName) {
        requireNonNull(fileName, "File name cannot be null");

        int lastDotIndex = fileName.lastIndexOf('.');
        if (lastDotIndex > 0 && lastDotIndex < fileName.length() - 1) {
            return fileName.substring(lastDotIndex + 1);
        }

        return "";
    }

    /**
     * @brief 获取不带扩展名的文件名
     */
    private String getFileNameWithoutExtension(String fileName) {
        int lastDotIndex = fileName.lastIndexOf('.');
        if (lastDotIndex > 0) {
            return fileName.substring(0, lastDotIndex);
        }
        return fileName;
    }

    /**
     * @brief 检查文件格式是否被支持
     */
    public static boolean isSupportedFormat(String fileExtension) {
        return fileExtension != null && SUPPORTED_FORMATS.contains(fileExtension.toLowerCase());
    }

    /**
     * @brief 获取所有支持的图片格式
     */
    public static Set<String> getSupportedFormats() {
        return new HashSet<>(SUPPORTED_FORMATS);
    }
}
