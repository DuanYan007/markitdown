package com.markitdown.converter;

import com.markitdown.api.ConversionResult;
import com.markitdown.api.DocumentConverter;
import com.markitdown.config.ConversionOptions;
import com.markitdown.exception.ConversionException;
import com.markdown.engine.MarkdownBuilder;
import com.markitdown.ocr.OcrEngine;
import com.markitdown.ocr.OcrEngineFactory;
import com.markitdown.ocr.OcrException;
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
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static java.util.Objects.requireNonNull;

/**
 * Image converter with optional OCR support.
 */
public class ImageConverter implements DocumentConverter {

    private static final Logger logger = LoggerFactory.getLogger(ImageConverter.class);
    private static final Set<String> SUPPORTED_FORMATS =
            Set.of("png", "jpg", "jpeg", "gif", "bmp", "tiff", "tif", "webp");

    @Override
    public ConversionResult convert(Path filePath, ConversionOptions options) throws ConversionException {
        requireNonNull(filePath, "File path cannot be null");
        requireNonNull(options, "Conversion options cannot be null");

        logger.info("Converting image file: {}", filePath);

        try {
            BufferedImage image = ImageIO.read(filePath.toFile());
            if (image == null) {
                throw new ConversionException("Cannot load image file: " + filePath,
                        filePath.getFileName().toString(), getName());
            }

            Map<String, Object> metadata = extractMetadata(filePath, image, options);

            String extractedText;
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
        return false;
    }

    @Override
    public int getPriority() {
        return 80;
    }

    @Override
    public String getName() {
        return "ImageConverter";
    }

    private Map<String, Object> extractMetadata(Path filePath, BufferedImage image, ConversionOptions options) {
        Map<String, Object> metadata = new LinkedHashMap<>();

        if (options.isIncludeMetadata()) {
            metadata.put("宽度", image.getWidth());
            metadata.put("高度", image.getHeight());

            String fileName = filePath.getFileName().toString();
            String format = getFileExtension(fileName).toLowerCase();
            metadata.put("格式", format.toUpperCase());
            metadata.put("颜色类型", getColorType(image));
            metadata.put("文件大小", filePath.toFile().length());

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

    private Map<String, Object> extractExifMetadata(Path filePath) {
        Map<String, Object> exifData = new LinkedHashMap<>();

        try (InputStream stream = new FileInputStream(filePath.toFile())) {
            Tika tika = new Tika();
            Metadata metadata = new Metadata();
            Parser parser = new AutoDetectParser();
            BodyContentHandler handler = new BodyContentHandler();
            ParseContext context = new ParseContext();
            context.set(Parser.class, parser);
            parser.parse(stream, handler, metadata, context);

            addIfNotEmpty(exifData, "相机品牌", metadata.get("Equipment Make"));
            addIfNotEmpty(exifData, "相机型号", metadata.get("Equipment Model"));
            addIfNotEmpty(exifData, "拍摄时间", metadata.get("Date/Time Original"));
            addIfNotEmpty(exifData, "曝光时间", metadata.get("Exposure Time"));
            addIfNotEmpty(exifData, "光圈值", metadata.get("F-Number"));
            addIfNotEmpty(exifData, "ISO感光度", metadata.get("ISO Speed Ratings"));
            addIfNotEmpty(exifData, "焦距", metadata.get("Focal Length"));
            addIfNotEmpty(exifData, "闪光灯", metadata.get("Flash"));
            addIfNotEmpty(exifData, "白平衡", metadata.get("White Balance"));
            addIfNotEmpty(exifData, "方向", metadata.get("Orientation"));
            addIfNotEmpty(exifData, "X分辨率", metadata.get("X Resolution"));
            addIfNotEmpty(exifData, "Y分辨率", metadata.get("Y Resolution"));
            addIfNotEmpty(exifData, "分辨率单位", metadata.get("Resolution Units"));
            addIfNotEmpty(exifData, "软件", metadata.get("Software"));
            addIfNotEmpty(exifData, "艺术家", metadata.get("Artist"));
            addIfNotEmpty(exifData, "版权", metadata.get("Copyright Notice"));

            String gpsLatitude = metadata.get("GPS Latitude");
            String gpsLongitude = metadata.get("GPS Longitude");
            if (gpsLatitude != null || gpsLongitude != null) {
                StringBuilder gps = new StringBuilder();
                if (gpsLatitude != null) {
                    gps.append("纬度: ").append(gpsLatitude);
                }
                if (gpsLongitude != null) {
                    if (gps.length() > 0) {
                        gps.append(", ");
                    }
                    gps.append("经度: ").append(gpsLongitude);
                }
                exifData.put("GPS位置", gps.toString());
            }
        } catch (Exception e) {
            logger.debug("EXIF extraction error: {}", e.getMessage());
        }

        return exifData;
    }

    private void addIfNotEmpty(Map<String, Object> map, String key, String value) {
        if (value != null && !value.trim().isEmpty()) {
            map.put(key, value);
        }
    }

    private String performOcr(BufferedImage image, ConversionOptions options) throws ConversionException {
        File tempFile = null;
        try {
            tempFile = File.createTempFile("ocr_", ".png");
            ImageIO.write(image, "png", tempFile);

            String language = options.getLanguage();
            if ("auto".equals(language) || language == null || language.isEmpty()) {
                language = "eng+chi_sim";
            }

            OcrEngine effectiveOcrEngine = OcrEngineFactory.create(options);
            if (!effectiveOcrEngine.isAvailable()) {
                throw new ConversionException(
                        "OCR engine '" + effectiveOcrEngine.getEngineName() + "' is unavailable. " +
                                "Use the full build or configure another OCR engine.",
                        "image",
                        getName());
            }

            logger.info("Using OCR engine: {}", effectiveOcrEngine.getEngineName());
            String result = effectiveOcrEngine.extractText(tempFile, language);
            return cleanupOcrResult(result);
        } catch (OcrException e) {
            throw new ConversionException("OCR processing failed: " + e.getMessage(), e, "image", getName());
        } catch (IOException e) {
            throw new ConversionException("Failed to create temporary OCR file: " + e.getMessage(),
                    e, "image", getName());
        } finally {
            if (tempFile != null && tempFile.exists()) {
                tempFile.delete();
            }
        }
    }

    private String cleanupOcrResult(String ocrText) {
        if (ocrText == null || ocrText.trim().isEmpty()) {
            return "";
        }
        String cleaned = ocrText.replaceAll("[ \\t]+", " ");
        cleaned = cleaned.replaceAll("\\n{3,}", "\n\n");
        return cleaned.trim();
    }

    private String convertToMarkdown(String extractedText, Map<String, Object> metadata,
                                     ConversionOptions options, Path filePath) {
        StringBuilder markdown = new StringBuilder();
        String fileName = filePath.getFileName().toString();

        markdown.append("# Image: ").append(getFileNameWithoutExtension(fileName)).append("\n\n");

        if (options.isIncludeImages()) {
            markdown.append("![").append(fileName).append("](").append(fileName).append(")\n\n");
        }

        if (options.isIncludeMetadata() && !metadata.isEmpty()) {
            markdown.append("## Image Information\n\n");
            for (Map.Entry<String, Object> entry : metadata.entrySet()) {
                if (entry.getValue() != null) {
                    markdown.append("- **").append(MarkdownBuilder.prettifyMetadataKey(entry.getKey()))
                            .append(":** ").append(entry.getValue()).append("\n");
                }
            }
            markdown.append("\n");
        }

        markdown.append("## Extracted Text\n\n");
        if (extractedText == null || extractedText.isEmpty()
                || extractedText.contains("OCR is disabled")
                || extractedText.contains("OCR processing failed")) {
            markdown.append(extractedText).append("\n\n");
        } else {
            markdown.append(formatExtractedText(extractedText)).append("\n\n");
        }

        return markdown.toString();
    }

    private String formatExtractedText(String text) {
        if (text == null || text.trim().isEmpty()) {
            return "";
        }
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

    private String getFileExtension(String fileName) {
        requireNonNull(fileName, "File name cannot be null");
        int lastDotIndex = fileName.lastIndexOf('.');
        if (lastDotIndex > 0 && lastDotIndex < fileName.length() - 1) {
            return fileName.substring(lastDotIndex + 1);
        }
        return "";
    }

    private String getFileNameWithoutExtension(String fileName) {
        int lastDotIndex = fileName.lastIndexOf('.');
        if (lastDotIndex > 0) {
            return fileName.substring(0, lastDotIndex);
        }
        return fileName;
    }

    public static boolean isSupportedFormat(String fileExtension) {
        return fileExtension != null && SUPPORTED_FORMATS.contains(fileExtension.toLowerCase());
    }

    public static Set<String> getSupportedFormats() {
        return Set.copyOf(SUPPORTED_FORMATS);
    }
}
