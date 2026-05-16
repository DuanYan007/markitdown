package com.markitdown.config;

import com.markdown.engine.config.MarkdownConfig;

import java.nio.file.Path;

/**
 * Mutable conversion options shared across CLI execution and library usage.
 *
 * <p>The class groups content toggles, rendering format choices, OCR settings,
 * output paths, and document-specific parameters. Converters can read the full
 * object directly or consume the narrower record-like views exposed by the
 * helper accessors.</p>
 */
public class ConversionOptions {

    private boolean includeImages = true;
    private boolean includeTables = true;
    private boolean includeMetadata = true;
    private String tableFormat = "github";
    private String imageFormat = "markdown";
    private String imageOutputDir = "assets";
    private Path outputPath;
    private String language = "auto";
    private long maxFileSize = 50 * 1024 * 1024;
    private Path tempDirectory;
    private String pageBreakMode = "heading";
    private String tesseractPath;
    private String tessdataPath;
    private String pdfPassword;
    private String sourceFileName;
    private boolean useOcr = true;
    private String ocrEngine = "tesseract-cli";
    private String ocrEndpoint;
    private String ocrApiKey;
    private String ocrModel;
    private int ocrTimeout = 30000;
    private int ocrPollInterval = 5000;

    /**
     * Creates a new options object with defaults.
     */
    public ConversionOptions() {
    }

    /**
     * Creates a copy of an existing options object.
     *
     * @param other source options
     */
    public ConversionOptions(ConversionOptions other) {
        this.includeImages = other.includeImages;
        this.includeTables = other.includeTables;
        this.includeMetadata = other.includeMetadata;
        this.tableFormat = other.tableFormat;
        this.imageFormat = other.imageFormat;
        this.imageOutputDir = other.imageOutputDir;
        this.outputPath = other.outputPath;
        this.language = other.language;
        this.maxFileSize = other.maxFileSize;
        this.tempDirectory = other.tempDirectory;
        this.pageBreakMode = other.pageBreakMode;
        this.tesseractPath = other.tesseractPath;
        this.tessdataPath = other.tessdataPath;
        this.pdfPassword = other.pdfPassword;
        this.sourceFileName = other.sourceFileName;
        this.useOcr = other.useOcr;
        this.ocrEngine = other.ocrEngine;
        this.ocrEndpoint = other.ocrEndpoint;
        this.ocrApiKey = other.ocrApiKey;
        this.ocrModel = other.ocrModel;
        this.ocrTimeout = other.ocrTimeout;
        this.ocrPollInterval = other.ocrPollInterval;
    }

    /**
     * Sets whether extracted images should be referenced in the output.
     *
     * @param includeImages true to include images
     * @return this instance
     */
    public ConversionOptions setIncludeImages(boolean includeImages) {
        this.includeImages = includeImages;
        return this;
    }

    /**
     * Sets whether tables should be included in the output.
     *
     * @param includeTables true to include tables
     * @return this instance
     */
    public ConversionOptions setIncludeTables(boolean includeTables) {
        this.includeTables = includeTables;
        return this;
    }

    /**
     * Sets whether metadata should be included in the output.
     *
     * @param includeMetadata true to include metadata
     * @return this instance
     */
    public ConversionOptions setIncludeMetadata(boolean includeMetadata) {
        this.includeMetadata = includeMetadata;
        return this;
    }

    /**
     * Sets the Markdown table style.
     *
     * @param tableFormat table format such as {@code github}, {@code markdown},
     *                    or {@code pipe}
     * @return this instance
     */
    public ConversionOptions setTableFormat(String tableFormat) {
        this.tableFormat = tableFormat;
        return this;
    }

    /**
     * Sets the image rendering style.
     *
     * @param imageFormat image format such as {@code markdown}, {@code html},
     *                    or {@code base64}
     * @return this instance
     */
    public ConversionOptions setImageFormat(String imageFormat) {
        this.imageFormat = imageFormat;
        return this;
    }

    /**
     * Sets the output directory for extracted images.
     *
     * @param imageOutputDir image directory relative to the output file
     * @return this instance
     */
    public ConversionOptions setImageOutputDir(String imageOutputDir) {
        this.imageOutputDir = imageOutputDir;
        return this;
    }

    /**
     * Sets the output path used to resolve related assets.
     *
     * @param outputPath output file path
     * @return this instance
     */
    public ConversionOptions setOutputPath(Path outputPath) {
        this.outputPath = outputPath;
        return this;
    }

    /**
     * Sets the OCR language.
     *
     * @param language OCR language code
     * @return this instance
     */
    public ConversionOptions setLanguage(String language) {
        this.language = language;
        return this;
    }

    /**
     * Sets the maximum allowed file size.
     *
     * @param maxFileSize maximum file size in bytes
     * @return this instance
     */
    public ConversionOptions setMaxFileSize(long maxFileSize) {
        this.maxFileSize = maxFileSize;
        return this;
    }

    /**
     * Sets the temporary directory used during conversion.
     *
     * @param tempDirectory temporary directory path
     * @return this instance
     */
    public ConversionOptions setTempDirectory(Path tempDirectory) {
        this.tempDirectory = tempDirectory;
        return this;
    }

    /**
     * Sets the page break rendering mode.
     *
     * @param pageBreakMode page break handling strategy
     * @return this instance
     */
    public ConversionOptions setPageBreakMode(String pageBreakMode) {
        this.pageBreakMode = pageBreakMode;
        return this;
    }

    /**
     * Sets the Tesseract executable path.
     *
     * @param tesseractPath tesseract executable path
     * @return this instance
     */
    public ConversionOptions setTesseractPath(String tesseractPath) {
        this.tesseractPath = tesseractPath;
        return this;
    }

    /**
     * Sets the Tesseract language data path.
     *
     * @param tessdataPath tessdata directory path
     * @return this instance
     */
    public ConversionOptions setTessdataPath(String tessdataPath) {
        this.tessdataPath = tessdataPath;
        return this;
    }

    /**
     * Sets the PDF password.
     *
     * @param pdfPassword PDF password
     * @return this instance
     */
    public ConversionOptions setPdfPassword(String pdfPassword) {
        this.pdfPassword = pdfPassword;
        return this;
    }

    /**
     * Sets the logical source file name for stream-based conversion.
     *
     * @param sourceFileName source file name
     * @return this instance
     */
    public ConversionOptions setSourceFileName(String sourceFileName) {
        this.sourceFileName = sourceFileName;
        return this;
    }

    /**
     * Enables or disables OCR.
     *
     * @param useOcr true to enable OCR
     * @return this instance
     */
    public ConversionOptions setUseOcr(boolean useOcr) {
        this.useOcr = useOcr;
        return this;
    }

    /**
     * Sets the OCR engine identifier.
     *
     * @param ocrEngine OCR engine name
     * @return this instance
     */
    public ConversionOptions setOcrEngine(String ocrEngine) {
        this.ocrEngine = ocrEngine;
        return this;
    }

    /**
     * Sets the HTTP OCR endpoint.
     *
     * @param ocrEndpoint OCR service endpoint
     * @return this instance
     */
    public ConversionOptions setOcrEndpoint(String ocrEndpoint) {
        this.ocrEndpoint = ocrEndpoint;
        return this;
    }

    /**
     * Sets the HTTP OCR API key.
     *
     * @param ocrApiKey OCR service API key
     * @return this instance
     */
    public ConversionOptions setOcrApiKey(String ocrApiKey) {
        this.ocrApiKey = ocrApiKey;
        return this;
    }

    /**
     * Sets the OCR model name.
     *
     * @param ocrModel OCR model identifier
     * @return this instance
     */
    public ConversionOptions setOcrModel(String ocrModel) {
        this.ocrModel = ocrModel;
        return this;
    }

    /**
     * Sets the OCR timeout in milliseconds.
     *
     * @param ocrTimeout timeout in milliseconds
     * @return this instance
     */
    public ConversionOptions setOcrTimeout(int ocrTimeout) {
        this.ocrTimeout = ocrTimeout;
        return this;
    }

    /**
     * Sets the OCR polling interval in milliseconds.
     *
     * @param ocrPollInterval polling interval in milliseconds
     * @return this instance
     */
    public ConversionOptions setOcrPollInterval(int ocrPollInterval) {
        this.ocrPollInterval = ocrPollInterval;
        return this;
    }

    /**
     * Builds a Markdown configuration view for the rendering layer.
     *
     * @return Markdown rendering configuration
     */
    public MarkdownConfig toMarkdownConfig() {
        ContentOptions content = content();
        FormatOptions format = format();
        return MarkdownConfig.builder()
                .includeTables(content.includeTables())
                .includeMetadata(content.includeMetadata())
                .tableFormat(format.table())
                .build();
    }

    /**
     * Returns the content-related options.
     *
     * @return content options view
     */
    public ContentOptions content() {
        return new ContentOptions(includeImages, includeTables, includeMetadata, pageBreakMode);
    }

    /**
     * Returns the formatting-related options.
     *
     * @return format options view
     */
    public FormatOptions format() {
        return new FormatOptions(tableFormat, imageFormat);
    }

    /**
     * Returns the output-related options.
     *
     * @return output options view
     */
    public OutputOptions output() {
        return new OutputOptions(imageOutputDir, outputPath, tempDirectory);
    }

    /**
     * Returns the OCR-related options.
     *
     * @return OCR options view
     */
    public OcrOptions ocr() {
        return new OcrOptions(
                useOcr,
                language,
                ocrEngine,
                ocrEndpoint,
                ocrApiKey,
                ocrModel,
                ocrTimeout,
                ocrPollInterval,
                tesseractPath,
                tessdataPath
        );
    }

    /**
     * Returns the conversion limits.
     *
     * @return limits options view
     */
    public LimitsOptions limits() {
        return new LimitsOptions(maxFileSize);
    }

    /**
     * Returns document-specific options.
     *
     * @return document options view
     */
    public DocumentOptions document() {
        return new DocumentOptions(pdfPassword, sourceFileName);
    }

    /**
     * Creates a builder.
     *
     * @return new builder
     */
    public static Builder builder() {
        return new Builder();
    }

    /**
     * Creates a builder seeded from the current options.
     *
     * @return new builder
     */
    public Builder toBuilder() {
        return new Builder(this);
    }

    /**
     * Builder for {@link ConversionOptions}.
     */
    public static class Builder {
        private final ConversionOptions options = new ConversionOptions();

        private Builder() {
        }

        private Builder(ConversionOptions existing) {
            copyFrom(existing);
        }

        private void copyFrom(ConversionOptions existing) {
            ContentOptions content = existing.content();
            FormatOptions format = existing.format();
            OutputOptions output = existing.output();
            OcrOptions ocr = existing.ocr();
            LimitsOptions limits = existing.limits();
            DocumentOptions document = existing.document();
            options.setIncludeImages(content.includeImages());
            options.setIncludeTables(content.includeTables());
            options.setIncludeMetadata(content.includeMetadata());
            options.setTableFormat(format.table());
            options.setImageFormat(format.image());
            options.setImageOutputDir(output.imageOutputDir());
            options.setOutputPath(output.outputPath());
            options.setLanguage(ocr.language());
            options.setMaxFileSize(limits.maxFileSize());
            options.setTempDirectory(output.tempDirectory());
            options.setPageBreakMode(content.pageBreakMode());
            options.setTesseractPath(ocr.tesseractPath());
            options.setTessdataPath(ocr.tessdataPath());
            options.setPdfPassword(document.pdfPassword());
            options.setSourceFileName(document.sourceFileName());
            options.setUseOcr(ocr.enabled());
            options.setOcrEngine(ocr.engine());
            options.setOcrEndpoint(ocr.endpoint());
            options.setOcrApiKey(ocr.apiKey());
            options.setOcrModel(ocr.model());
            options.setOcrTimeout(ocr.timeout());
            options.setOcrPollInterval(ocr.pollInterval());
        }

        public Builder includeImages(boolean includeImages) {
            options.setIncludeImages(includeImages);
            return this;
        }

        public Builder includeTables(boolean includeTables) {
            options.setIncludeTables(includeTables);
            return this;
        }

        public Builder includeMetadata(boolean includeMetadata) {
            options.setIncludeMetadata(includeMetadata);
            return this;
        }

        public Builder tableFormat(String tableFormat) {
            options.setTableFormat(tableFormat);
            return this;
        }

        public Builder imageFormat(String imageFormat) {
            options.setImageFormat(imageFormat);
            return this;
        }

        public Builder imageOutputDir(String imageOutputDir) {
            options.setImageOutputDir(imageOutputDir);
            return this;
        }

        public Builder outputPath(Path outputPath) {
            options.setOutputPath(outputPath);
            return this;
        }

        public Builder language(String language) {
            options.setLanguage(language);
            return this;
        }

        public Builder maxFileSize(long maxFileSize) {
            options.setMaxFileSize(maxFileSize);
            return this;
        }

        public Builder tempDirectory(Path tempDirectory) {
            options.setTempDirectory(tempDirectory);
            return this;
        }

        public Builder pageBreakMode(String pageBreakMode) {
            options.setPageBreakMode(pageBreakMode);
            return this;
        }

        public Builder tesseractPath(String tesseractPath) {
            options.setTesseractPath(tesseractPath);
            return this;
        }

        public Builder tessdataPath(String tessdataPath) {
            options.setTessdataPath(tessdataPath);
            return this;
        }

        public Builder pdfPassword(String pdfPassword) {
            options.setPdfPassword(pdfPassword);
            return this;
        }

        public Builder sourceFileName(String sourceFileName) {
            options.setSourceFileName(sourceFileName);
            return this;
        }

        public Builder useOcr(boolean useOcr) {
            options.setUseOcr(useOcr);
            return this;
        }

        public Builder ocrEngine(String ocrEngine) {
            options.setOcrEngine(ocrEngine);
            return this;
        }

        public Builder ocrEndpoint(String ocrEndpoint) {
            options.setOcrEndpoint(ocrEndpoint);
            return this;
        }

        public Builder ocrApiKey(String ocrApiKey) {
            options.setOcrApiKey(ocrApiKey);
            return this;
        }

        public Builder ocrModel(String ocrModel) {
            options.setOcrModel(ocrModel);
            return this;
        }

        public Builder ocrTimeout(int ocrTimeout) {
            options.setOcrTimeout(ocrTimeout);
            return this;
        }

        public Builder ocrPollInterval(int ocrPollInterval) {
            options.setOcrPollInterval(ocrPollInterval);
            return this;
        }

        public ConversionOptions build() {
            return new ConversionOptions(options);
        }
    }

    /**
     * Content-related conversion flags.
     */
    public static final class ContentOptions {
        private final boolean includeImages;
        private final boolean includeTables;
        private final boolean includeMetadata;
        private final String pageBreakMode;

        private ContentOptions(boolean includeImages, boolean includeTables, boolean includeMetadata, String pageBreakMode) {
            this.includeImages = includeImages;
            this.includeTables = includeTables;
            this.includeMetadata = includeMetadata;
            this.pageBreakMode = pageBreakMode;
        }

        public boolean includeImages() {
            return includeImages;
        }

        public boolean includeTables() {
            return includeTables;
        }

        public boolean includeMetadata() {
            return includeMetadata;
        }

        public String pageBreakMode() {
            return pageBreakMode;
        }
    }

    /**
     * Markdown rendering format options.
     */
    public static final class FormatOptions {
        private final String tableFormat;
        private final String imageFormat;

        private FormatOptions(String tableFormat, String imageFormat) {
            this.tableFormat = tableFormat;
            this.imageFormat = imageFormat;
        }

        public String table() {
            return tableFormat;
        }

        public String image() {
            return imageFormat;
        }
    }

    /**
     * Output path and asset placement options.
     */
    public static final class OutputOptions {
        private final String imageOutputDir;
        private final Path outputPath;
        private final Path tempDirectory;

        private OutputOptions(String imageOutputDir, Path outputPath, Path tempDirectory) {
            this.imageOutputDir = imageOutputDir;
            this.outputPath = outputPath;
            this.tempDirectory = tempDirectory;
        }

        public String imageOutputDir() {
            return imageOutputDir;
        }

        public Path outputPath() {
            return outputPath;
        }

        public Path tempDirectory() {
            return tempDirectory;
        }
    }

    /**
     * OCR provider and runtime options.
     */
    public static final class OcrOptions {
        private final boolean enabled;
        private final String language;
        private final String engine;
        private final String endpoint;
        private final String apiKey;
        private final String model;
        private final int timeout;
        private final int pollInterval;
        private final String tesseractPath;
        private final String tessdataPath;

        private OcrOptions(boolean enabled,
                           String language,
                           String engine,
                           String endpoint,
                           String apiKey,
                           String model,
                           int timeout,
                           int pollInterval,
                           String tesseractPath,
                           String tessdataPath) {
            this.enabled = enabled;
            this.language = language;
            this.engine = engine;
            this.endpoint = endpoint;
            this.apiKey = apiKey;
            this.model = model;
            this.timeout = timeout;
            this.pollInterval = pollInterval;
            this.tesseractPath = tesseractPath;
            this.tessdataPath = tessdataPath;
        }

        public boolean enabled() {
            return enabled;
        }

        public String language() {
            return language;
        }

        public String engine() {
            return engine;
        }

        public String endpoint() {
            return endpoint;
        }

        public String apiKey() {
            return apiKey;
        }

        public String model() {
            return model;
        }

        public int timeout() {
            return timeout;
        }

        public int pollInterval() {
            return pollInterval;
        }

        public String tesseractPath() {
            return tesseractPath;
        }

        public String tessdataPath() {
            return tessdataPath;
        }
    }

    /**
     * Resource limits used by converters.
     */
    public static final class LimitsOptions {
        private final long maxFileSize;

        private LimitsOptions(long maxFileSize) {
            this.maxFileSize = maxFileSize;
        }

        public long maxFileSize() {
            return maxFileSize;
        }
    }

    /**
     * Source-document options.
     */
    public static final class DocumentOptions {
        private final String pdfPassword;
        private final String sourceFileName;

        private DocumentOptions(String pdfPassword, String sourceFileName) {
            this.pdfPassword = pdfPassword;
            this.sourceFileName = sourceFileName;
        }

        public String pdfPassword() {
            return pdfPassword;
        }

        public String sourceFileName() {
            return sourceFileName;
        }
    }
}
