package com.markitdown.converters;

import com.markdown.engine.MarkdownBuilder;
import com.markitdown.api.ConversionResult;
import com.markitdown.api.DocumentConverter;
import com.markitdown.config.ConversionOptions;
import com.markitdown.exceptions.ConversionException;
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
import java.util.Base64;
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
                throw new ConversionException(
                        "Cannot load image file: " + filePath,
                        filePath.getFileName().toString(),
                        getName()
                );
            }

            Map<String, Object> metadata = extractMetadata(filePath, image, options);
            String extractedText;

            if (options.ocr().enabled()) {
                try {
                    extractedText = performOcr(image, options);
                } catch (ConversionException e) {
                    logger.warn("OCR failed: {}", e.getMessage());
                    extractedText = "*OCR processing failed: " + e.getMessage() + "*";
                }
            } else {
                extractedText = "*OCR is disabled in conversion options.*";
            }

            String markdownContent = convertToMarkdown(extractedText, metadata, options, filePath);
            return new ConversionResult(
                    markdownContent,
                    metadata,
                    new ArrayList<>(),
                    filePath.toFile().length(),
                    filePath.getFileName().toString()
            );
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

        if (!options.content().includeMetadata()) {
            return metadata;
        }

        String fileName = filePath.getFileName().toString();
        String format = getFileExtension(fileName).toLowerCase();

        metadata.put("Width", image.getWidth());
        metadata.put("Height", image.getHeight());
        metadata.put("Format", format.toUpperCase());
        metadata.put("Color Type", getColorType(image));
        metadata.put("File Size", filePath.toFile().length());

        try {
            Map<String, Object> exifData = extractExifMetadata(filePath);
            if (!exifData.isEmpty()) {
                metadata.putAll(exifData);
            }
        } catch (Exception e) {
            logger.debug("Could not extract EXIF metadata: {}", e.getMessage());
        }

        metadata.put("Converted At", LocalDateTime.now());
        return metadata;
    }

    private Map<String, Object> extractExifMetadata(Path filePath) {
        Map<String, Object> exifData = new LinkedHashMap<>();

        try (InputStream stream = new FileInputStream(filePath.toFile())) {
            Metadata metadata = new Metadata();
            Parser parser = new AutoDetectParser();
            BodyContentHandler handler = new BodyContentHandler();
            ParseContext context = new ParseContext();
            context.set(Parser.class, parser);
            parser.parse(stream, handler, metadata, context);

            addIfNotEmpty(exifData, "Camera Make", metadata.get("Equipment Make"));
            addIfNotEmpty(exifData, "Camera Model", metadata.get("Equipment Model"));
            addIfNotEmpty(exifData, "Captured At", metadata.get("Date/Time Original"));
            addIfNotEmpty(exifData, "Exposure Time", metadata.get("Exposure Time"));
            addIfNotEmpty(exifData, "F Number", metadata.get("F-Number"));
            addIfNotEmpty(exifData, "ISO", metadata.get("ISO Speed Ratings"));
            addIfNotEmpty(exifData, "Focal Length", metadata.get("Focal Length"));
            addIfNotEmpty(exifData, "Flash", metadata.get("Flash"));
            addIfNotEmpty(exifData, "White Balance", metadata.get("White Balance"));
            addIfNotEmpty(exifData, "Orientation", metadata.get("Orientation"));
            addIfNotEmpty(exifData, "X Resolution", metadata.get("X Resolution"));
            addIfNotEmpty(exifData, "Y Resolution", metadata.get("Y Resolution"));
            addIfNotEmpty(exifData, "Resolution Units", metadata.get("Resolution Units"));
            addIfNotEmpty(exifData, "Software", metadata.get("Software"));
            addIfNotEmpty(exifData, "Artist", metadata.get("Artist"));
            addIfNotEmpty(exifData, "Copyright", metadata.get("Copyright Notice"));

            String gpsLatitude = metadata.get("GPS Latitude");
            String gpsLongitude = metadata.get("GPS Longitude");
            if (gpsLatitude != null || gpsLongitude != null) {
                StringBuilder gps = new StringBuilder();
                if (gpsLatitude != null) {
                    gps.append("Latitude: ").append(gpsLatitude);
                }
                if (gpsLongitude != null) {
                    if (gps.length() > 0) {
                        gps.append(", ");
                    }
                    gps.append("Longitude: ").append(gpsLongitude);
                }
                exifData.put("GPS Location", gps.toString());
            }

            Tika tika = new Tika();
            String detectedMimeType = tika.detect(filePath.toFile());
            addIfNotEmpty(exifData, "Detected MIME Type", detectedMimeType);
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

            String language = options.ocr().language();
            if ("auto".equals(language) || language == null || language.isEmpty()) {
                language = "eng+chi_sim";
            }

            OcrEngine effectiveOcrEngine = OcrEngineFactory.create(options);
            if (!effectiveOcrEngine.isAvailable()) {
                throw new ConversionException(
                        "OCR engine '" + effectiveOcrEngine.getEngineName() + "' is unavailable. " +
                                "Use the full build or configure another OCR engine.",
                        "image",
                        getName()
                );
            }

            logger.info("Using OCR engine: {}", effectiveOcrEngine.getEngineName());
            String result = effectiveOcrEngine.extractText(tempFile, language);
            return cleanupOcrResult(result);
        } catch (OcrException e) {
            throw new ConversionException("OCR processing failed: " + e.getMessage(), e, "image", getName());
        } catch (IOException e) {
            throw new ConversionException("Failed to create temporary OCR file: " + e.getMessage(), e, "image", getName());
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

        markdown.append("# ").append(getFileNameWithoutExtension(fileName)).append("\n\n");
        markdown.append("**File:** `").append(fileName).append("`\n\n");

        if (options.content().includeImages()) {
            markdown.append(renderImageReference(filePath, fileName, options)).append("\n\n");
        }

        if (options.content().includeMetadata() && !metadata.isEmpty()) {
            markdown.append("## Image Information\n\n");
            for (Map.Entry<String, Object> entry : metadata.entrySet()) {
                if (entry.getValue() != null) {
                    markdown.append("- **")
                            .append(MarkdownBuilder.prettifyMetadataKey(entry.getKey()))
                            .append(":** ")
                            .append(entry.getValue())
                            .append("\n");
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

    private String renderImageReference(Path filePath, String fileName, ConversionOptions options) {
        String imageFormat = options.format().image();
        String format = imageFormat != null ? imageFormat.toLowerCase() : "markdown";
        String title = "Source image";
        switch (format) {
            case "html":
                return "<img src=\"" + fileName + "\" alt=\"" + fileName + "\" title=\"" + title + "\" />";
            case "base64":
                try {
                    String mimeType = detectImageMimeType(fileName);
                    String encoded = Base64.getEncoder().encodeToString(java.nio.file.Files.readAllBytes(filePath));
                    return "![" + fileName + "](data:" + mimeType + ";base64," + encoded + ")";
                } catch (IOException e) {
                    logger.warn("Failed to inline image as base64, falling back to markdown reference: {}", e.getMessage());
                    return "![" + fileName + "](" + fileName + ")";
                }
            case "markdown":
            default:
                return "![" + fileName + "](" + fileName + ")";
        }
    }

    private String detectImageMimeType(String fileName) {
        String extension = getFileExtension(fileName).toLowerCase();
        switch (extension) {
            case "jpg":
            case "jpeg":
                return "image/jpeg";
            case "gif":
                return "image/gif";
            case "bmp":
                return "image/bmp";
            case "tif":
            case "tiff":
                return "image/tiff";
            case "webp":
                return "image/webp";
            case "png":
            default:
                return "image/png";
        }
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
