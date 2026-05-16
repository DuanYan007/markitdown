package com.markitdown.cli;

import com.markitdown.api.ConversionResult;
import com.markitdown.config.ConversionOptions;
import com.markitdown.config.ConfigurationManager;
import com.markitdown.core.MarkItDownEngine;
import com.markitdown.exceptions.ConversionException;
import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Model.CommandSpec;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;
import picocli.CommandLine.Spec;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.Callable;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

/**
 * Command-line entry point for the MarkItDown Java application.
 *
 * <p>This command wraps the conversion engine behind a Picocli-based CLI. It
 * supports file conversion, pipe input, configuration and diagnostic commands,
 * OCR-related options, and sequential or parallel batch processing.</p>
 *
 * @author duan yan
 * @version 2.1.0
 * @since 2.0.0
 */
@Command(
        name = "markitdown",
        mixinStandardHelpOptions = true,
        version = "markitdown4j 0.0.4",
        sortOptions = false,
        synopsisHeading = "%nUsage:%n",
        descriptionHeading = "%nWhat It Does:%n",
        parameterListHeading = "%nInputs:%n",
        optionListHeading = "%nOptions (ordered by common tasks):%n",
        description = {
                "Convert documents to Markdown.",
                "",
                "Typical flow:",
                "  1. Choose your input files or pipe content with --mime-type",
                "  2. Set output, content, and OCR options as needed",
                "  3. Use config and diagnostic commands to inspect behavior"
        },
        footerHeading = "Examples:%n",
        footer = {
                "  markitdown document.pdf                       # Convert a PDF to Markdown",
                "  markitdown document.docx -o output.md         # Convert a Word document",
                "  markitdown presentation.pptx --no-tables      # Convert a PowerPoint without tables",
                "  markitdown spreadsheet.xlsx --ocr             # Convert an Excel file with OCR",
                "  markitdown *.pdf                              # Convert all PDFs in the directory",
                "  markitdown *.pdf --parallel                   # Convert multiple PDFs in parallel",
                "  markitdown https://example.com/report.pdf -o out/ # Download a URL and convert it",
                "  cat document.pdf | markitdown                 # Convert from stdin",
                "  curl -s http://example.com/doc.pdf | markitdown  # Convert a remote document stream"
        }
)
public class MarkItDownCommand implements Callable<Integer> {

    @Spec
    CommandSpec spec;

    // ==================== Output options ====================

    @Option(
            names = {"-o", "--output"},
            order = 10,
            description = "Output file or directory (default: stdout for pipe, .md file for file input)"
    )
    private String output;

    @Option(
            names = {"--format", "-f"},
            order = 11,
            description = "Output format: markdown, plain, json (default: markdown)"
    )
    private String outputFormat = "markdown";

    // ==================== Content options ====================

    @Option(
            names = {"--include-images"},
            order = 20,
            description = "Include images in the output (default: true)"
    )
    private Boolean includeImages = null;

    @Option(
            names = {"--no-images"},
            order = 21,
            description = "Exclude images from the output"
    )
    private boolean noImages;

    @Option(
            names = {"--include-tables"},
            order = 22,
            description = "Include tables in the output (default: true)"
    )
    private Boolean includeTables = null;

    @Option(
            names = {"--no-tables"},
            order = 23,
            description = "Exclude tables from the output"
    )
    private boolean noTables;

    @Option(
            names = {"--include-metadata"},
            order = 24,
            description = "Include metadata in the output (default: true)"
    )
    private Boolean includeMetadata = null;

    @Option(
            names = {"--no-metadata"},
            order = 25,
            description = "Exclude metadata from the output"
    )
    private boolean noMetadata;

    // ==================== OCR options ====================

    @Option(
            names = {"--ocr"},
            order = 30,
            description = "Use OCR for text extraction from images"
    )
    private boolean useOcr;

    @Option(
            names = {"--language", "-l"},
            order = 31,
            description = "Language for OCR (default: auto)",
            defaultValue = "auto"
    )
    private String language;

    @Option(
            names = {"--ocr-engine"},
            order = 32,
            description = "OCR engine: tesseract-cli, http, paddleocr (default: tesseract-cli)"
    )
    private String ocrEngine;

    @Option(
            names = {"--ocr-endpoint"},
            order = 33,
            description = "Remote OCR endpoint"
    )
    private String ocrEndpoint;

    @Option(
            names = {"--ocr-api-key"},
            order = 34,
            description = "Remote OCR API key or token"
    )
    private String ocrApiKey;

    @Option(
            names = {"--ocr-model"},
            order = 35,
            description = "OCR model name for remote providers"
    )
    private String ocrModel;

    @Option(
            names = {"--ocr-timeout"},
            order = 36,
            description = "OCR timeout in milliseconds"
    )
    private int ocrTimeout;

    @Option(
            names = {"--ocr-poll-interval"},
            order = 37,
            description = "OCR polling interval in milliseconds for async providers"
    )
    private int ocrPollInterval;

    // ==================== Format options ====================

    @Option(
            names = {"--table-format"},
            order = 40,
            description = "Table format: github, markdown, pipe (default: github)",
            defaultValue = "github"
    )
    private String tableFormat;

    @Option(
            names = {"--image-format"},
            order = 41,
            description = "Image format: markdown, html, base64 (default: markdown)",
            defaultValue = "markdown"
    )
    private String imageFormat;

    @Option(
            names = {"--image-output-dir"},
            order = 42,
            description = "Directory for extracted images relative to output file (default: assets/)",
            defaultValue = "assets"
    )
    private String imageOutputDir;

    // ==================== File options ====================

    @Option(
            names = {"--max-file-size"},
            order = 50,
            description = "Maximum file size in bytes (default: 50MB, use 0 for unlimited)",
            defaultValue = "52428800"
    )
    private long maxFileSize;

    // ==================== PDF options ====================

    @Option(
            names = {"--pdf-password"},
            order = 51,
            description = "Password for encrypted PDF files"
    )
    private String pdfPassword;

    @Option(
            names = {"--large-file"},
            order = 52,
            description = "Allow processing of large files (>50MB)"
    )
    private boolean largeFile;

    @Option(
            names = {"--temp-dir"},
            order = 53,
            description = "Temporary directory for file operations"
    )
    private String tempDir;

    // ==================== Output control options ====================

    @Option(
            names = {"--verbose", "-v"},
            order = 60,
            description = "Enable verbose output"
    )
    private boolean verbose;

    @Option(
            names = {"--quiet", "-q"},
            order = 61,
            description = "Suppress all output except errors"
    )
    private boolean quiet;

    // ==================== Performance options ====================

    @Option(
            names = {"--parallel", "-p"},
            order = 70,
            description = "Enable parallel processing for multiple files"
    )
    private boolean parallel;

    @Option(
            names = {"--threads"},
            order = 71,
            description = "Number of threads for parallel processing (default: CPU cores)",
            defaultValue = "0"
    )
    private int threads;

    @Option(
            names = {"--progress"},
            order = 72,
            description = "Show progress bar during conversion"
    )
    private boolean showProgress;

    @Option(
            names = {"--stats"},
            order = 73,
            description = "Show performance statistics after conversion"
    )
    private boolean showStats;

    @Option(
            names = {"--memory-limit"},
            order = 74,
            description = "Memory limit in MB for batch processing (default: auto-detect)"
    )
    private int memoryLimit = 0;

    @Option(
            names = {"--optimize-memory"},
            order = 75,
            description = "Enable memory optimization for large file processing"
    )
    private boolean optimizeMemory;

    @Option(
            names = {"--examples"},
            order = 80,
            description = "Show usage examples and exit"
    )
    private boolean showExamples;

    @Option(
            names = {"--generate-config"},
            order = 81,
            description = "Generate default configuration file"
    )
    private boolean generateConfig;

    @Option(
            names = {"--config-path"},
            order = 82,
            description = "Path to configuration file"
    )
    private String configPath;

    @Option(
            names = {"--validate-config"},
            order = 83,
            description = "Validate configuration file"
    )
    private boolean validateConfig;

    @Option(
            names = {"--show-config"},
            order = 84,
            description = "Show current configuration"
    )
    private boolean showConfig;

    @Option(
            names = {"--list-formats"},
            order = 85,
            description = "List supported MIME types and converters"
    )
    private boolean listFormats;

    @Option(
            names = {"--recursive", "-r"},
            order = 86,
            description = "Recursively process files in directories"
    )
    private boolean recursive;

    @Option(
            names = {"--batch"},
            order = 87,
            description = "Batch process all supported files in directory"
    )
    private boolean batch;

    // ==================== MIME type option for pipe input ====================

    @Option(
            names = {"--mime-type", "-m"},
            order = 12,
            description = "MIME type for pipe input (e.g., application/pdf)"
    )
    private String mimeType;

    // ==================== Input arguments ====================

    @Parameters(
            arity = "0..*",
            description = "Input files or HTTP(S) URLs to convert (optional if using pipe input)"
    )
    private String[] inputFiles;

    // ==================== Runtime state ====================

    private MarkItDownEngine engine;
    private PerformanceStats stats;
    private ConfigurationManager effectiveConfigurationManager;

    @Override
    public Integer call() throws Exception {
        Instant startTime = Instant.now();
        stats = new PerformanceStats();

        List<String> adminCommands = new ArrayList<>();
        if (generateConfig) {
            adminCommands.add("--generate-config");
        }
        if (validateConfig) {
            adminCommands.add("--validate-config");
        }
        if (showConfig) {
            adminCommands.add("--show-config");
        }
        if (listFormats) {
            adminCommands.add("--list-formats");
        }
        if (showExamples) {
            adminCommands.add("--examples");
        }

        if (adminCommands.size() > 1) {
            System.err.println("Error: diagnostic/config commands cannot be combined: " + String.join(", ", adminCommands));
            System.err.println("Choose exactly one of: --generate-config, --validate-config, --show-config, --list-formats, --examples");
            return 1;
        }

        try {
            // Handle config and diagnostic commands before conversion work starts.
            if (generateConfig) {
                return ConfigCommands.generateConfig(configPath);
            }

            if (validateConfig) {
                return ConfigCommands.validateConfig(configPath);
            }

            if (showConfig) {
                return ConfigCommands.showConfig(buildEffectiveConfigurationManager());
            }

            if (listFormats) {
                return printSupportedFormats();
            }

            // Show usage examples and exit.
            if (showExamples) {
                System.out.println(UserMessageHelper.getUsageExamples());
                return 0;
            }

            // Trigger a best-effort garbage collection before heavy processing.
            if (optimizeMemory) {
                System.gc(); // Hint the JVM to reclaim memory before conversion starts.
            }

            // Initialize engine
            engine = createEngine();

            // Configure options
            ConversionOptions options = createConversionOptions();

            // Sample current memory pressure for warnings and diagnostics.
            Runtime runtime = Runtime.getRuntime();
            long maxMemory = runtime.maxMemory();
            long usedMemory = runtime.totalMemory() - runtime.freeMemory();
            double memoryUsage = (double) usedMemory / maxMemory;

            if (verbose && memoryUsage > 0.7) {
                System.err.printf("Warning: High memory usage detected: %.1f%%%n", memoryUsage * 100);
            }

            // Check for pipe input
            if (isPipeInput()) {
                return processPipeInput(options);
            }

            // Check if input files are provided
            if (inputFiles == null || inputFiles.length == 0) {
                System.err.println("Error: No input files specified and no pipe input detected.");
                System.err.println("Use --help for usage information.");
                return 1;
            }

            // Process files
            int result;
            if (parallel && inputFiles.length > 1) {
                result = processFilesParallel(options);
            } else {
                result = processFilesSequential(options);
            }

            // Show statistics
            if (showStats) {
                stats.printSummary(Duration.between(startTime, Instant.now()));
            }

            return result;

        } catch (Exception e) {
            // Print a user-facing error first, then expose details in verbose mode.
            if (!quiet) {
                String errorMessage = UserMessageHelper.getUserFriendlyError(e);
                System.err.println(errorMessage);
            }

            boolean userInputError = e instanceof ConversionException || e instanceof IllegalArgumentException;
            if (verbose && !userInputError) {
                System.err.println("\nDetailed error information:");
                e.printStackTrace();
            }

            // Use distinct exit codes for conversion failures versus unexpected errors.
            if (userInputError) {
                return 1; // Conversion failed.
            } else {
                return 2; // Unexpected runtime failure.
            }
        } finally {
            if (engine != null) {
                engine.shutdown();
            }
        }
    }

    /**
     * Detects whether content is being piped into stdin.
     */
    private boolean isPipeInput() {
        try {
            return System.in.available() > 0;
        } catch (IOException e) {
            return false;
        }
    }

    /**
     * Processes pipe input using an explicit or detected MIME type.
     */
    private int processPipeInput(ConversionOptions options) {
        try {
            PipeInputHelper.ResolvedPipeInput resolvedPipeInput = PipeInputHelper.resolve(System.in, mimeType);
            String detectedMimeType = resolvedPipeInput.mimeType();
            if (detectedMimeType == null) {
                System.err.println("Error: Cannot detect MIME type from pipe input.");
                System.err.println("Please specify --mime-type option.");
                return 1;
            }

            if (resolvedPipeInput.detected() && !quiet) {
                System.err.println("Detected MIME type: " + detectedMimeType);
            }

            // Reject stream input when no converter supports the resolved MIME type.
            if (!engine.isSupported(detectedMimeType)) {
                System.err.println("Error: Unsupported MIME type: " + detectedMimeType);
                return 1;
            }

            // Convert stdin using the resolved MIME type.
            ConversionResult result = engine.convert(resolvedPipeInput.stream(), detectedMimeType, options);

            if (result.isSuccessful()) {
                // Pipe-mode output is always written to stdout.
                System.out.println(result.getMarkdown());

                if (verbose) {
                    System.err.println("Conversion successful. Output size: " + result.getMarkdown().length() + " chars");
                }

                if (result.hasWarnings()) {
                    System.err.println("Warnings:");
                    for (String warning : result.getWarnings()) {
                        System.err.println("  - " + warning);
                    }
                }

                return 0;
            } else {
                System.err.println("Conversion failed:");
                for (String warning : result.getWarnings()) {
                    System.err.println("  - " + warning);
                }
                return 1;
            }

        } catch (Exception e) {
            System.err.println("Pipe conversion error: " + e.getMessage());
            if (verbose) {
                e.printStackTrace();
            }
            return 1;
        }
    }

    private List<String> collectInputFiles() {
        Consumer<String> warningSink = message -> {
            if (!quiet) {
                System.err.println(message);
            }
        };
        Consumer<String> infoSink = message -> {
            if (!quiet) {
                System.err.println(message);
            }
        };
        Consumer<String> errorSink = message -> {
            if (!quiet) {
                System.err.println(message);
            }
        };

        return InputDiscoveryHelper.collectInputFiles(
                inputFiles,
                recursive,
                batch,
                engine::isSupported,
                warningSink,
                infoSink,
                errorSink
        );
    }

    /**
     * Processes files sequentially.
     */
    private int processFilesSequential(ConversionOptions options) {
        List<String> allFiles = collectInputFiles();

        if (allFiles.isEmpty()) {
            System.err.println("No files to process.");
            return 1;
        }

        BatchConversionRunner.BatchResult result = BatchConversionRunner.runSequential(
                allFiles,
                showProgress ? this::showProgress : BatchConversionRunner.ProgressListener.NO_OP,
                inputFile -> processFile(inputFile, options),
                stats::recordSuccess,
                this::handleProcessingError
        );

        if (showProgress) {
            System.err.println(); // Finish the progress line cleanly.
        }

        if (!quiet && allFiles.size() > 1) {
            System.err.printf("Conversion completed: %d successful, %d failed%n",
                    result.successCount(), result.errorCount());
        }

        return result.errorCount() > 0 ? 1 : 0;
    }

    /**
     * Processes files in parallel.
     */
    private int processFilesParallel(ConversionOptions options) {
        List<String> allFiles = collectInputFiles();

        // Bail out early when wildcard or directory expansion produced no files.
        if (allFiles.isEmpty()) {
            System.err.println("No files to process.");
            return 1;
        }

        int poolSize = threads > 0 ? threads : Runtime.getRuntime().availableProcessors();
        BatchConversionRunner.BatchResult result = BatchConversionRunner.runParallel(
                allFiles,
                poolSize,
                showProgress ? this::showProgress : BatchConversionRunner.ProgressListener.NO_OP,
                inputFile -> processFile(inputFile, options),
                stats::recordSuccess,
                this::handleProcessingError
        );

        if (showProgress) {
            System.err.println();
        }

        if (!quiet) {
            System.err.printf("Parallel conversion completed: %d successful, %d failed%n",
                    result.successCount(), result.errorCount());
        }

        return result.errorCount() > 0 ? 1 : 0;
    }

    private void handleProcessingError(String inputFile, Exception e) {
        stats.recordError(inputFile);
        if (!quiet) {
            System.err.println("Error processing " + inputFile + ": " + e.getMessage());
        }
        if (verbose) {
            e.printStackTrace();
        }
    }

    /**
     * Renders a simple progress bar to stderr.
     */
    private void showProgress(int current, int total, String fileName) {
        int percent = (int) ((current * 100) / total);
        int barLength = 30;
        int filled = (percent * barLength) / 100;

        StringBuilder bar = new StringBuilder();
        bar.append("[");
        for (int i = 0; i < barLength; i++) {
            if (i < filled) {
                bar.append("=");
            } else if (i == filled) {
                bar.append(">");
            } else {
                bar.append(" ");
            }
        }
        bar.append("]");

        // Truncate long paths so the progress display stays readable.
        String displayName = fileName;
        if (displayName.length() > 30) {
            displayName = "..." + displayName.substring(displayName.length() - 27);
        }

        System.err.printf("\r%s %3d%% (%d/%d) %s",
                bar, percent, current, total, displayName);
    }

    /**
     * Creates and configures the MarkItDown engine.
     */
    private MarkItDownEngine createEngine() {
        return new MarkItDownEngine(MarkItDownEngine.createDefaultRegistry());
    }

    private int printSupportedFormats() {
        MarkItDownEngine formatsEngine = createEngine();
        try {
            Map<String, String> converterInfo = new TreeMap<>(formatsEngine.getConverterInfo());
            List<FormatRow> rows = Arrays.asList(
                    new FormatRow("PDF", ".pdf", "application/pdf", "PdfConverter"),
                    new FormatRow("Word (DOCX)", ".docx", "application/vnd.openxmlformats-officedocument.wordprocessingml.document", "DocxConverter"),
                    new FormatRow("Word (DOC)", ".doc", "application/msword", "DocConverter"),
                    new FormatRow("PowerPoint (PPTX)", ".pptx", "application/vnd.openxmlformats-officedocument.presentationml.presentation", "PptxConverter"),
                    new FormatRow("PowerPoint (PPT)", ".ppt", "application/vnd.ms-powerpoint", "PptConverter"),
                    new FormatRow("Excel (XLSX)", ".xlsx", "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet", "XlsxConverter"),
                    new FormatRow("Excel (XLS)", ".xls", "application/vnd.ms-excel", "XlsConverter"),
                    new FormatRow("HTML", ".html, .htm", "text/html", "HtmlConverter"),
                    new FormatRow("Text / Markdown", ".txt, .md, .markdown", "text/plain, text/markdown", "TextConverter"),
                    new FormatRow("Structured Text", ".csv, .json, .xml", "text/csv, application/json, application/xml", "TextConverter"),
                    new FormatRow("Images", ".png, .jpg, .jpeg, .gif, .bmp, .tif, .tiff, .webp", "image/*", "ImageConverter"),
                    new FormatRow("Audio", ".mp3, .wav, .ogg, .flac, .m4a, .aac, .wma, .opus, .aiff, .au", "audio/*", "AudioConverter"),
                    new FormatRow("ZIP Archive", ".zip", "application/zip", "ZipConverter")
            );

            System.out.println("Supported formats:");
            for (FormatRow row : rows) {
                String converterInfoLine = converterInfo.containsKey(row.converterName())
                        ? row.converterName()
                        : row.converterName() + " (not currently registered)";
                System.out.printf("- %s%n", row.label());
                System.out.printf("  Extensions: %s%n", row.extensions());
                System.out.printf("  MIME: %s%n", row.mimeTypes());
                System.out.printf("  Converter: %s%n", converterInfoLine);
            }

            return 0;
        } finally {
            formatsEngine.shutdown();
        }
    }

    private static final class FormatRow {
        private final String label;
        private final String extensions;
        private final String mimeTypes;
        private final String converterName;

        private FormatRow(String label, String extensions, String mimeTypes, String converterName) {
            this.label = label;
            this.extensions = extensions;
            this.mimeTypes = mimeTypes;
            this.converterName = converterName;
        }

        private String label() {
            return label;
        }

        private String extensions() {
            return extensions;
        }

        private String mimeTypes() {
            return mimeTypes;
        }

        private String converterName() {
            return converterName;
        }
    }
    private ConversionOptions createConversionOptions() {
        ConfigurationManager configManager = buildEffectiveConfigurationManager();
        ConfigurationManager.EffectiveConfiguration effectiveConfig = configManager.getEffectiveConfiguration();
        ConversionOptions.Builder builder = effectiveConfig.toConversionOptions().toBuilder();

        if (pdfPassword != null && !pdfPassword.isEmpty()) {
            builder.pdfPassword(pdfPassword);
        }

        return builder.build();
    }

    private ConfigurationManager createConfigurationManager() {
        if (configPath != null && !configPath.trim().isEmpty()) {
            Path explicitConfigPath = Paths.get(configPath);
            validateExplicitConfigPath(explicitConfigPath);
            return new ConfigurationManager(Paths.get(System.getProperty("user.dir")), explicitConfigPath);
        }
        return new ConfigurationManager();
    }

    private ConfigurationManager buildEffectiveConfigurationManager() {
        if (effectiveConfigurationManager == null) {
            ConfigurationManager configManager = createConfigurationManager();
            CliConfigurationOverrides.apply(configManager, buildCliOverrideInputs());
            effectiveConfigurationManager = configManager;
        }
        return effectiveConfigurationManager;
    }

    private CliConfigurationOverrides.Inputs buildCliOverrideInputs() {
        return CliConfigurationOverrides.Inputs.builder()
                .includeImages(includeImages)
                .noImages(noImages)
                .includeTables(includeTables)
                .noTables(noTables)
                .includeMetadata(includeMetadata)
                .noMetadata(noMetadata)
                .useOcr(useOcr)
                .languageSpecified(wasOptionSpecified("--language", "-l"))
                .language(language)
                .ocrEngineSpecified(wasOptionSpecified("--ocr-engine"))
                .ocrEngine(ocrEngine)
                .ocrEndpointSpecified(wasOptionSpecified("--ocr-endpoint"))
                .ocrEndpoint(ocrEndpoint)
                .ocrApiKeySpecified(wasOptionSpecified("--ocr-api-key"))
                .ocrApiKey(ocrApiKey)
                .ocrModelSpecified(wasOptionSpecified("--ocr-model"))
                .ocrModel(ocrModel)
                .ocrTimeoutSpecified(wasOptionSpecified("--ocr-timeout"))
                .ocrTimeout(ocrTimeout)
                .ocrPollIntervalSpecified(wasOptionSpecified("--ocr-poll-interval"))
                .ocrPollInterval(ocrPollInterval)
                .tableFormatSpecified(wasOptionSpecified("--table-format"))
                .tableFormat(tableFormat)
                .imageFormatSpecified(wasOptionSpecified("--image-format"))
                .imageFormat(imageFormat)
                .imageOutputDirSpecified(wasOptionSpecified("--image-output-dir"))
                .imageOutputDir(imageOutputDir)
                .tempDirSpecified(wasOptionSpecified("--temp-dir"))
                .tempDir(tempDir)
                .maxFileSizeSpecified(wasOptionSpecified("--max-file-size"))
                .maxFileSize(maxFileSize)
                .largeFile(largeFile)
                .parallel(parallel)
                .threadsSpecified(wasOptionSpecified("--threads"))
                .threads(threads)
                .showProgress(showProgress)
                .showStats(showStats)
                .optimizeMemory(optimizeMemory)
                .verbose(verbose)
                .quiet(quiet)
                .recursive(recursive)
                .batch(batch)
                .build();
    }

    private void validateExplicitConfigPath(Path explicitConfigPath) {
        if (!Files.exists(explicitConfigPath)) {
            throw new IllegalArgumentException("Configuration file does not exist: " + explicitConfigPath);
        }
        if (!Files.isRegularFile(explicitConfigPath)) {
            throw new IllegalArgumentException("Configuration path is not a file: " + explicitConfigPath);
        }
    }

    private boolean wasOptionSpecified(String... names) {
        if (spec == null || spec.commandLine() == null || spec.commandLine().getParseResult() == null) {
            return false;
        }
        for (String name : names) {
            if (spec.commandLine().getParseResult().hasMatchedOption(name)) {
                return true;
            }
        }
        return false;
    }

    private PreparedInput prepareInput(String inputFile, ConversionOptions options) throws ConversionException {
        if (!InputDiscoveryHelper.isRemoteUrl(inputFile)) {
            return PreparedInput.local(inputFile, Paths.get(inputFile));
        }
        return downloadRemoteInput(inputFile, options);
    }

    private PreparedInput downloadRemoteInput(String inputFile, ConversionOptions options) throws ConversionException {
        HttpURLConnection connection = null;
        try {
            URI requestUri = URI.create(inputFile);
            connection = (HttpURLConnection) requestUri.toURL().openConnection();
            connection.setRequestMethod("GET");
            connection.setInstanceFollowRedirects(true);
            connection.setConnectTimeout(30000);
            connection.setReadTimeout(60000);
            connection.setRequestProperty("User-Agent", "markitdown4j/0.0.4");

            int status = connection.getResponseCode();
            if (status >= 400) {
                throw new ConversionException("Failed to download URL: HTTP " + status + " for " + inputFile);
            }

            URI resolvedUri = URI.create(connection.getURL().toString());
            String remoteFileName = RemoteInputHelper.determineRemoteFileName(
                    resolvedUri,
                    connection.getHeaderField("Content-Disposition"),
                    connection.getContentType()
            );

            Path tempDirectory = createUrlTempDirectory(options);
            Path downloadedFile = tempDirectory.resolve(remoteFileName);

            try (InputStream remoteStream = connection.getInputStream()) {
                Files.copy(remoteStream, downloadedFile, StandardCopyOption.REPLACE_EXISTING);
            }

            return PreparedInput.remote(inputFile, downloadedFile, tempDirectory);
        } catch (ConversionException e) {
            throw e;
        } catch (IOException | IllegalArgumentException e) {
            throw new ConversionException("Failed to download URL: " + inputFile + " (" + e.getMessage() + ")", e);
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }
    }

    private Path createUrlTempDirectory(ConversionOptions options) throws IOException {
        Path configuredTempDirectory = options.output().tempDirectory();
        Path baseDirectory = configuredTempDirectory != null
                ? configuredTempDirectory
                : Paths.get(System.getProperty("java.io.tmpdir"));
        Files.createDirectories(baseDirectory);
        return Files.createTempDirectory(baseDirectory, "markitdown-url-");
    }

    private void cleanupPreparedInput(PreparedInput preparedInput) {
        if (preparedInput == null || preparedInput.cleanupRoot == null) {
            return;
        }

        try (java.util.stream.Stream<Path> paths = Files.walk(preparedInput.cleanupRoot)) {
            paths.sorted(Comparator.reverseOrder()).forEach(path -> {
                try {
                    Files.deleteIfExists(path);
                } catch (IOException e) {
                    if (verbose) {
                        System.err.println("Warning: failed to delete temporary file " + path + ": " + e.getMessage());
                    }
                }
            });
        } catch (IOException e) {
            if (verbose) {
                System.err.println("Warning: failed to clean temporary directory " + preparedInput.cleanupRoot + ": " + e.getMessage());
            }
        }
    }

    /**
     * Processes a single file.
     */
    private void processFile(String inputFile, ConversionOptions options) throws ConversionException {
        Instant startTime = Instant.now();
        PreparedInput preparedInput = prepareInput(inputFile, options);

        try {
            Path inputPath = preparedInput.inputPath;
            File inputFileObj = inputPath.toFile();

            if (!inputFileObj.exists()) {
                throw new ConversionException("Input file does not exist: " + inputFile);
            }

            if (!inputFileObj.isFile()) {
                throw new ConversionException("Input path is not a file: " + inputFile);
            }

            // Check if file type is supported
            if (!engine.isSupported(inputPath)) {
                throw new ConversionException("Unsupported file type: " + inputFile);
            }

            // Determine output path for image extraction and file writing
            Path outputPath;
            String effectiveOutput = output;
            if (effectiveOutput == null) {
                ConfigurationManager.EffectiveConfiguration effectiveConfig =
                        buildEffectiveConfigurationManager().getEffectiveConfiguration();
                effectiveOutput = effectiveConfig.output().dir().value();
            }

            if (effectiveOutput != null) {
                outputPath = OutputPathHelper.determineOutputPath(inputPath, effectiveOutput);
            } else {
                outputPath = OutputPathHelper.determineDefaultOutputPath(inputPath, preparedInput.remote);
            }

            ConversionOptions optionsWithPath = new ConversionOptions(options)
                    .setOutputPath(outputPath);

            ConversionResult result = engine.convert(inputPath, optionsWithPath);

            if (output == null && inputFiles.length == 1) {
                System.out.println(result.getMarkdown());
            } else {
                OutputPathHelper.writeResult(result, outputPath);

                if (!quiet && !showProgress) {
                    System.err.printf("Converted: %s -> %s%n", inputFile, outputPath);
                }
            }

            Duration duration = Duration.between(startTime, Instant.now());
            stats.recordFileStats(inputFile, inputFileObj.length(), duration.toMillis());

            if (verbose && result.hasWarnings()) {
                System.err.println("Warnings for " + inputFile + ":");
                for (String warning : result.getWarnings()) {
                    System.err.println("  - " + warning);
                }
            }
        } finally {
            cleanupPreparedInput(preparedInput);
        }
    }

    /**
     * Formats a file size for user-facing output.
     */
    private String formatFileSize(long bytes) {
        if (bytes < 1024) {
            return bytes + " B";
        } else if (bytes < 1024 * 1024) {
            return String.format("%.1f KB", bytes / 1024.0);
        } else if (bytes < 1024 * 1024 * 1024) {
            return String.format("%.1f MB", bytes / (1024.0 * 1024));
        } else {
            return String.format("%.1f GB", bytes / (1024.0 * 1024 * 1024));
        }
    }

    public static void main(String[] args) {
        int exitCode = new CommandLine(new MarkItDownCommand()).execute(args);
        System.exit(exitCode);
    }

    private static final class PreparedInput {
        private final Path inputPath;
        private final Path cleanupRoot;
        private final boolean remote;

        private PreparedInput(Path inputPath, Path cleanupRoot, boolean remote) {
            this.inputPath = inputPath;
            this.cleanupRoot = cleanupRoot;
            this.remote = remote;
        }

        private static PreparedInput local(String originalInput, Path inputPath) {
            return new PreparedInput(inputPath, null, false);
        }

        private static PreparedInput remote(String originalInput, Path inputPath, Path cleanupRoot) {
            return new PreparedInput(inputPath, cleanupRoot, true);
        }
    }

    /**
     * Tracks per-file and aggregate conversion statistics.
     */
    private static class PerformanceStats {
        private final List<FileStats> fileStats = new java.util.concurrent.CopyOnWriteArrayList<>();
        private final AtomicInteger successCount = new AtomicInteger(0);
        private final AtomicInteger errorCount = new AtomicInteger(0);

        void recordSuccess(String file) {
            successCount.incrementAndGet();
        }

        void recordError(String file) {
            errorCount.incrementAndGet();
        }

        void recordFileStats(String file, long size, long durationMs) {
            fileStats.add(new FileStats(file, size, durationMs));
        }

        void printSummary(Duration totalDuration) {
            System.err.println();
            System.err.println("==============================================================");
            System.err.println("  Performance Summary");
            System.err.println("==============================================================");
            System.err.printf("  %-25s %-12s %-10s %-12s%n", "File", "Size", "Time", "Speed");
            System.err.println("  ------------------------------------------------------------");

            long totalSize = 0;
            long totalTime = 0;

            for (FileStats fs : fileStats) {
                String displayName = fs.file.length() > 25 ? "..." + fs.file.substring(fs.file.length() - 22) : fs.file;
                String sizeStr = formatSize(fs.size);
                String timeStr = String.format("%.2fs", fs.durationMs / 1000.0);
                String speedStr = fs.durationMs > 0 ? formatSize(fs.size * 1000 / fs.durationMs) + "/s" : "N/A";

                System.err.printf("  %-25s %-12s %-10s %-12s%n", displayName, sizeStr, timeStr, speedStr);

                totalSize += fs.size;
                totalTime += fs.durationMs;
            }

            System.err.println("==============================================================");
            System.err.printf("  Total: %d file(s), %s, %.2fs%n",
                    fileStats.size(), formatSize(totalSize), totalDuration.toMillis() / 1000.0);
            System.err.printf("  Successful: %d, Failed: %d%n", successCount.get(), errorCount.get());
            if (totalTime > 0) {
                System.err.printf("  Average speed: %s/s%n", formatSize(totalSize * 1000 / totalTime));
            }
            System.err.println();
        }

        private String formatSize(long bytes) {
            if (bytes < 1024) return bytes + "B";
            if (bytes < 1024 * 1024) return String.format("%.1fKB", bytes / 1024.0);
            if (bytes < 1024 * 1024 * 1024) return String.format("%.1fMB", bytes / (1024.0 * 1024));
            return String.format("%.1fGB", bytes / (1024.0 * 1024 * 1024));
        }

        private static class FileStats {
            final String file;
            final long size;
            final long durationMs;

            FileStats(String file, long size, long durationMs) {
                this.file = file;
                this.size = size;
                this.durationMs = durationMs;
            }
        }
    }
}
