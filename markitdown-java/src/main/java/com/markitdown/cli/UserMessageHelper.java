package com.markitdown.cli;

import com.markitdown.exceptions.ConversionException;

import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Human-readable CLI messages for common failures and quick-start examples.
 */
public final class UserMessageHelper {

    private enum CliErrorKind {
        PDF_PASSWORD,
        FILE_SIZE,
        UNSUPPORTED_TYPE,
        CONFIGURATION,
        REMOTE_INPUT,
        OCR_UNAVAILABLE,
        OCR_EXECUTION,
        OUT_OF_MEMORY,
        CONVERSION_GENERIC,
        GENERAL
    }

    private UserMessageHelper() {
    }

    public static String getUserFriendlyError(Throwable error) {
        String message = error.getMessage();
        if (message == null || message.isBlank()) {
            message = error.getClass().getSimpleName();
        }

        CliErrorKind kind = classifyError(error, message);
        switch (kind) {
            case PDF_PASSWORD:
                return "PDF password error\n\n"
                        + "Cause: " + message + "\n\n"
                        + "Next steps:\n"
                        + "  - Provide --pdf-password when converting protected PDF files.\n"
                        + "  - Example: markitdown file.pdf --pdf-password yourpassword\n";
            case FILE_SIZE:
                return "File size limit reached\n\n"
                        + "Cause: " + message + "\n\n"
                        + "Next steps:\n"
                        + "  - Use --large-file to disable the configured size limit.\n"
                        + "  - Use --optimize-memory for large documents when memory pressure is the main issue.\n"
                        + "  - Example: markitdown large-file.pdf --large-file\n";
            case UNSUPPORTED_TYPE:
                return "Unsupported file type\n\n"
                        + "Cause: " + message + "\n\n"
                        + "Common supported inputs:\n"
                        + "  - PDF: .pdf\n"
                        + "  - Word: .docx, .doc\n"
                        + "  - Excel: .xlsx, .xls\n"
                        + "  - PowerPoint: .pptx, .ppt\n"
                        + "  - HTML: .html, .htm\n"
                        + "  - Images: .png, .jpg, .jpeg, .gif, .bmp\n"
                        + "  - Audio: .mp3, .wav, .m4a\n"
                        + "  - Archives: .zip\n"
                        + "  - Text: .txt, .md\n";
            case CONFIGURATION:
                return "Invalid configuration path\n\n"
                        + "Cause: " + message + "\n\n"
                        + "Next steps:\n"
                        + "  - Verify that --config-path points to an existing .yml or .yaml file.\n"
                        + "  - Use --generate-config to create a starter configuration file.\n"
                        + "  - Run --show-config after fixing the path to confirm the effective values.\n";
            case REMOTE_INPUT:
                return "Remote input error\n\n"
                        + "Cause: " + message + "\n\n"
                        + "Next steps:\n"
                        + "  - Verify the URL is correct and reachable from the current machine.\n"
                        + "  - Check HTTP status, redirects, authentication, and network policy.\n"
                        + "  - Retry with a local file if the remote source is unstable.\n";
            case OCR_UNAVAILABLE:
                return "OCR unavailable\n\n"
                        + "Cause: " + message + "\n\n"
                        + "Next steps:\n"
                        + "  - Confirm that the selected OCR engine is installed or configured.\n"
                        + "  - Verify provider-specific settings such as tesseract path, API endpoint, or API token.\n"
                        + "  - Use --show-config to confirm the active OCR engine and related settings.\n";
            case OCR_EXECUTION:
                return "OCR error\n\n"
                        + "Cause: " + message + "\n\n"
                        + "Next steps:\n"
                        + "  - Confirm that the selected OCR engine is installed or reachable.\n"
                        + "  - If you use Tesseract, verify the executable path and language data.\n"
                        + "  - Check language selection with --language.\n"
                        + "  - Tesseract: https://github.com/tesseract-ocr/tesseract\n"
                        + "  - Tessdata: https://github.com/tesseract-ocr/tessdata\n";
            case OUT_OF_MEMORY:
                return "Out of memory\n\n"
                        + "Cause: " + message + "\n\n"
                        + "Next steps:\n"
                        + "  - Use --optimize-memory for large conversions.\n"
                        + "  - Increase JVM heap size, for example: java -Xmx2g -jar markitdown4j.jar\n"
                        + "  - If the limit is intentional, use --large-file only when you want to remove it.\n";
            case CONVERSION_GENERIC:
                return formatConversionError((ConversionException) error);
            case GENERAL:
            default:
                return "Conversion failed\n\n"
                        + "Cause: " + message + "\n\n"
                        + "Next steps:\n"
                        + "  - Re-run with --verbose for more diagnostics.\n"
                        + "  - Check OCR, output path, and input file settings.\n"
                        + "  - Run --help to review the current CLI options.\n";
        }
    }

    private static CliErrorKind classifyError(Throwable error, String message) {
        String normalized = message == null ? "" : message.toLowerCase();

        if (normalized.contains("cannot decrypt pdf") || normalized.contains("password")) {
            return CliErrorKind.PDF_PASSWORD;
        }

        if (normalized.contains("exceeds maximum allowed size")) {
            return CliErrorKind.FILE_SIZE;
        }

        if (normalized.contains("unsupported file type")) {
            return CliErrorKind.UNSUPPORTED_TYPE;
        }

        if (normalized.contains("configuration file does not exist")
                || normalized.contains("configuration path is not a file")
                || normalized.contains("only yaml configuration files are supported")) {
            return CliErrorKind.CONFIGURATION;
        }

        if (normalized.contains("failed to download url")
                || normalized.contains("http ")
                && normalized.contains(" for http")) {
            return CliErrorKind.REMOTE_INPUT;
        }

        if (normalized.contains("is not available")
                || normalized.contains("not configured")
                || normalized.contains("unavailable")) {
            if (normalized.contains("ocr") || normalized.contains("tesseract") || normalized.contains("paddleocr")) {
                return CliErrorKind.OCR_UNAVAILABLE;
            }
        }

        if (normalized.contains("tesseract")
                || normalized.contains("ocr processing failed")
                || normalized.contains("http ocr request failed")
                || normalized.contains("http ocr response")
                || normalized.contains("paddleocr request failed")
                || normalized.contains("paddleocr job")
                || normalized.contains("failed to execute tesseract command")) {
            return CliErrorKind.OCR_EXECUTION;
        }

        if (normalized.contains("out of memory") || normalized.contains("java heap space")) {
            return CliErrorKind.OUT_OF_MEMORY;
        }

        if (error instanceof ConversionException) {
            return CliErrorKind.CONVERSION_GENERIC;
        }

        return CliErrorKind.GENERAL;
    }

    private static String formatConversionError(ConversionException error) {
        StringBuilder builder = new StringBuilder("Conversion failed");

        if (error.getFileName() != null && !error.getFileName().isBlank()) {
            builder.append("\nFile: ").append(error.getFileName());
        }
        if (error.getConverterName() != null && !error.getConverterName().isBlank()) {
            builder.append("\nConverter: ").append(error.getConverterName());
        }

        builder.append("\n\nCause: ").append(error.getMessage());

        String advice = getConverterSpecificAdvice(error.getConverterName());
        if (!advice.isBlank()) {
            builder.append("\n\n").append(advice);
        }
        return builder.toString();
    }

    private static String getConverterSpecificAdvice(String converterName) {
        if (converterName == null || converterName.isBlank()) {
            return "Run with --verbose to inspect the underlying converter failure.";
        }

        switch (converterName) {
            case "PdfConverter":
                return "PDF troubleshooting:\n"
                        + "  - Use --pdf-password for encrypted files.\n"
                        + "  - Use --ocr when the PDF is image-based.\n"
                        + "  - Use --large-file or --optimize-memory for large documents.";
            case "DocxConverter":
            case "DocConverter":
                return "Word troubleshooting:\n"
                        + "  - Verify the source file is not corrupted.\n"
                        + "  - Re-export the document if the original file is malformed.";
            case "XlsxConverter":
            case "XlsConverter":
                return "Spreadsheet troubleshooting:\n"
                        + "  - Use --large-file for very large workbooks.\n"
                        + "  - Check whether the workbook contains unsupported embedded content.";
            case "PptxConverter":
            case "PptConverter":
                return "Presentation troubleshooting:\n"
                        + "  - Verify the file opens correctly in the source application.\n"
                        + "  - Re-save the file if the original export is malformed.";
            case "ImageConverter":
                return "Image troubleshooting:\n"
                        + "  - Enable --ocr when converting image files to Markdown text.\n"
                        + "  - Verify the selected OCR engine is available.\n"
                        + "  - Set --language when OCR accuracy depends on language hints.";
            case "AudioConverter":
                return "Audio troubleshooting:\n"
                        + "  - Verify the input format is supported.\n"
                        + "  - Check transcription credentials or endpoint configuration when applicable.";
            case "HtmlConverter":
                return "HTML troubleshooting:\n"
                        + "  - Verify the document uses a supported HTML structure.\n"
                        + "  - Re-save or simplify the source if the markup is malformed.";
            case "TextConverter":
                return "Text troubleshooting:\n"
                        + "  - Verify the file encoding is readable.\n"
                        + "  - Re-save the file as UTF-8 when content looks corrupted.";
            case "ZipConverter":
                return "Archive troubleshooting:\n"
                        + "  - Verify the archive is readable.\n"
                        + "  - Check whether nested files use supported formats.";
            default:
                return "Run with --verbose to inspect the underlying converter failure.";
        }
    }

    public static String getUsageExamples() {
        return "Usage examples:\n\n"
                + "Basic conversion:\n"
                + "  markitdown document.pdf\n"
                + "  markitdown report.docx -o report.md\n\n"
                + "OCR:\n"
                + "  markitdown scanned.pdf --ocr\n"
                + "  markitdown image.png --ocr -l chi_sim\n"
                + "  markitdown file.pdf --ocr-engine paddleocr --ocr-endpoint https://example.com/ocr\n\n"
                + "Batch and directory processing:\n"
                + "  markitdown *.pdf --parallel\n"
                + "  markitdown docs/ --batch --recursive\n\n"
                + "Large inputs:\n"
                + "  markitdown large.pdf --large-file\n"
                + "  markitdown huge.xlsx --optimize-memory\n\n"
                + "Content controls:\n"
                + "  markitdown file.docx --no-images --no-tables\n"
                + "  markitdown file.pdf --no-metadata\n\n"
                + "Diagnostics:\n"
                + "  markitdown --show-config\n"
                + "  markitdown --validate-config\n"
                + "  markitdown --generate-config\n";
    }

    public static String getFileTypeDetectionInfo(String filePath) {
        if (filePath == null || filePath.isBlank()) {
            return "File type detection requires a non-empty path.";
        }

        Path path = Paths.get(filePath);
        String fileName = path.getFileName().toString();
        String extension = getFileExtension(fileName);
        String detectedType = detectFileType(extension);

        return "File type detection:\n"
                + "  File: " + fileName + "\n"
                + "  Extension: " + extension + "\n"
                + "  Detected type: " + detectedType + "\n";
    }

    private static String getFileExtension(String fileName) {
        int lastDot = fileName.lastIndexOf('.');
        return lastDot > 0 ? fileName.substring(lastDot + 1).toLowerCase() : "";
    }

    private static String detectFileType(String extension) {
        switch (extension) {
            case "pdf":
                return "PDF";
            case "docx":
            case "doc":
                return "Word document";
            case "xlsx":
            case "xls":
                return "Spreadsheet";
            case "pptx":
            case "ppt":
                return "Presentation";
            case "html":
            case "htm":
                return "HTML document";
            case "png":
            case "jpg":
            case "jpeg":
            case "gif":
            case "bmp":
                return "Image";
            case "mp3":
            case "wav":
            case "m4a":
                return "Audio";
            case "zip":
                return "ZIP archive";
            case "txt":
            case "md":
                return "Text document";
            case "csv":
                return "CSV document";
            case "json":
                return "JSON document";
            case "xml":
                return "XML document";
            default:
                return "Unknown or unsupported";
        }
    }
}
