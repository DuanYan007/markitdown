package com.markitdown.utils;

import com.markitdown.models.ExtractedImage;
import org.apache.poi.xwpf.usermodel.XWPFPictureData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * Extracts embedded images from documents and writes them to the configured
 * output directory.
 */
public class ImageExtractor {

    private static final Logger logger = LoggerFactory.getLogger(ImageExtractor.class);

    private final Path outputDir;
    private final String documentName;
    private int imageIndex = 0;

    /**
     * Creates an extractor rooted at the document output location.
     *
     * @param baseOutputPath base output directory for the converted document
     * @param imageDirName image subdirectory name
     * @param documentName source document name
     * @throws IOException when the image directory cannot be created
     */
    public ImageExtractor(Path baseOutputPath, String imageDirName, String documentName) throws IOException {
        this.documentName = sanitizeFilename(documentName);
        this.outputDir = baseOutputPath.resolve(imageDirName);
        if (!Files.exists(outputDir)) {
            Files.createDirectories(outputDir);
            logger.info("Created image output directory: {}", outputDir);
        }
    }

    /**
     * Extracts all pictures from a Word document payload.
     *
     * @param pictures picture list from the Word document
     * @param baseOutputPath base output path used to compute relative asset paths
     * @return extracted image descriptors
     */
    public List<ExtractedImage> extractPictures(List<XWPFPictureData> pictures, Path baseOutputPath) {
        List<ExtractedImage> extractedImages = new ArrayList<>();

        if (pictures == null || pictures.isEmpty()) {
            logger.info("No embedded images found in the document.");
            return extractedImages;
        }

        logger.info("Extracting {} embedded image(s).", pictures.size());

        for (XWPFPictureData picture : pictures) {
            try {
                ExtractedImage image = extractSinglePicture(picture, baseOutputPath);
                if (image != null) {
                    extractedImages.add(image);
                    logger.debug("Extracted image: {} ({})", image.getRelativePath(), formatSize(image.getSize()));
                }
            } catch (Exception e) {
                logger.warn("Failed to extract image: {}", e.getMessage());
            }
        }

        logger.info("Image extraction finished: {} succeeded, {} failed.",
                extractedImages.size(), pictures.size() - extractedImages.size());
        return extractedImages;
    }

    /**
     * Extracts and writes a single picture.
     */
    private ExtractedImage extractSinglePicture(XWPFPictureData picture, Path baseOutputPath) throws IOException {
        byte[] imageData = picture.getData();
        String format = getFormatName(picture.getPictureType());
        String filename = String.format("%s_image_%d.%s", documentName, imageIndex++, format);

        Path imagePath = outputDir.resolve(filename);
        Files.write(imagePath, imageData);

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
     * Maps Apache POI picture types to file extensions.
     */
    private String getFormatName(int pictureType) {
        switch (pictureType) {
            case 6:
                return "png";
            case 5:
                return "jpg";
            case 7:
                return "gif";
            case 2:
                return "bmp";
            case 4:
                return "emf";
            case 3:
                return "wmf";
            default:
                return "unknown";
        }
    }

    /**
     * Normalizes the base file name used for extracted image assets.
     */
    private String sanitizeFilename(String filename) {
        if (filename == null) {
            return "document";
        }

        String name = filename.contains(".")
                ? filename.substring(0, filename.lastIndexOf('.'))
                : filename;

        return name.replaceAll("[^a-zA-Z0-9._-]", "_");
    }

    /**
     * Formats a byte size for debug logging.
     */
    private String formatSize(long bytes) {
        if (bytes < 1024) {
            return bytes + " B";
        }
        if (bytes < 1024 * 1024) {
            return String.format("%.1f KB", bytes / 1024.0);
        }
        return String.format("%.1f MB", bytes / (1024.0 * 1024.0));
    }

    /**
     * Returns the directory where extracted images are written.
     *
     * @return output directory
     */
    public Path getOutputDir() {
        return outputDir;
    }
}
