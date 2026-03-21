package com.markitdown.cli;

import com.markitdown.api.ConversionResult;
import com.markitdown.api.DocumentConverter;
import com.markitdown.config.ConversionOptions;
import com.markitdown.converter.*;
import com.markitdown.core.ConverterRegistry;
import com.markitdown.core.MarkItDownEngine;
import com.markitdown.exception.ConversionException;
import com.markitdown.utils.FileTypeDetector;
import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @class MarkItDownCommand
 * @brief MarkItDown Java命令行接口类
 * @details 基于Picocli框架实现的命令行工具，提供文档转换功能
 *          支持多种输入格式、丰富的配置选项、批量处理和管道输入
 *          提供详细的帮助信息和错误处理机制
 *
 * @author duan yan
 * @version 2.1.0
 * @since 2.0.0
 */
@Command(
        name = "markitdown",
        mixinStandardHelpOptions = true,
        version = "MarkItDown-java 2.1.0",
        description = "将各种文档格式转换为Markdown格式",
        footerHeading = "示例:%n",
        footer = {
                "  markitdown document.pdf                       # 将PDF转换为Markdown",
                "  markitdown document.docx -o output.md         # 将Word文档转换为output.md",
                "  markitdown presentation.pptx --no-tables      # 转换PowerPoint不包含表格",
                "  markitdown spreadsheet.xlsx --ocr             # 转换Excel并对图片使用OCR",
                "  markitdown *.pdf                              # 转换目录下所有PDF文件",
                "  markitdown *.pdf --parallel                   # 并行转换多个PDF文件",
                "  cat document.pdf | markitdown                 # 管道输入转换",
                "  curl -s http://example.com/doc.pdf | markitdown  # 从URL转换"
        }
)
public class MarkItDownCommand implements Callable<Integer> {

    // ==================== 输出选项 ====================

    @Option(
            names = {"-o", "--output"},
            description = "Output file or directory (default: stdout for pipe, .md file for file input)"
    )
    private String output;

    // ==================== 内容包含选项 ====================

    @Option(
            names = {"--include-images"},
            description = "Include images in the output (default: true)"
    )
    private Boolean includeImages = true;

    @Option(
            names = {"--no-images"},
            description = "Exclude images from the output"
    )
    private boolean noImages;

    @Option(
            names = {"--include-tables"},
            description = "Include tables in the output (default: true)"
    )
    private Boolean includeTables = true;

    @Option(
            names = {"--no-tables"},
            description = "Exclude tables from the output"
    )
    private boolean noTables;

    @Option(
            names = {"--include-metadata"},
            description = "Include metadata in the output (default: true)"
    )
    private Boolean includeMetadata = true;

    @Option(
            names = {"--no-metadata"},
            description = "Exclude metadata from the output"
    )
    private boolean noMetadata;

    // ==================== OCR 选项 ====================

    @Option(
            names = {"--ocr"},
            description = "Use OCR for text extraction from images"
    )
    private boolean useOcr;

    @Option(
            names = {"--language", "-l"},
            description = "Language for OCR (default: auto)",
            defaultValue = "auto"
    )
    private String language;

    // ==================== 格式选项 ====================

    @Option(
            names = {"--table-format"},
            description = "Table format: github, markdown, pipe (default: github)",
            defaultValue = "github"
    )
    private String tableFormat;

    @Option(
            names = {"--image-format"},
            description = "Image format: markdown, html, base64 (default: markdown)",
            defaultValue = "markdown"
    )
    private String imageFormat;

    // ==================== 文件选项 ====================

    @Option(
            names = {"--max-file-size"},
            description = "Maximum file size in bytes (default: 50MB, use 0 for unlimited)",
            defaultValue = "52428800"
    )
    private long maxFileSize;

    // ==================== PDF 特定选项 ====================

    @Option(
            names = {"--pdf-password"},
            description = "Password for encrypted PDF files"
    )
    private String pdfPassword;

    @Option(
            names = {"--large-file"},
            description = "Allow processing of large files (>50MB)"
    )
    private boolean largeFile;

    @Option(
            names = {"--temp-dir"},
            description = "Temporary directory for file operations"
    )
    private String tempDir;

    // ==================== 输出控制选项 ====================

    @Option(
            names = {"--verbose", "-v"},
            description = "Enable verbose output"
    )
    private boolean verbose;

    @Option(
            names = {"--quiet", "-q"},
            description = "Suppress all output except errors"
    )
    private boolean quiet;

    // ==================== 性能选项 ====================

    @Option(
            names = {"--parallel", "-p"},
            description = "Enable parallel processing for multiple files"
    )
    private boolean parallel;

    @Option(
            names = {"--threads"},
            description = "Number of threads for parallel processing (default: CPU cores)",
            defaultValue = "0"
    )
    private int threads;

    @Option(
            names = {"--progress"},
            description = "Show progress bar during conversion"
    )
    private boolean showProgress;

    @Option(
            names = {"--stats"},
            description = "Show performance statistics after conversion"
    )
    private boolean showStats;

    // ==================== MIME 类型选项（用于管道输入）====================

    @Option(
            names = {"--mime-type", "-m"},
            description = "MIME type for pipe input (e.g., application/pdf)"
    )
    private String mimeType;

    // ==================== 输入文件参数 ====================

    @Parameters(
            arity = "0..*",
            description = "Input files to convert (optional if using pipe input)"
    )
    private String[] inputFiles;

    // ==================== 运行时状态 ====================

    private MarkItDownEngine engine;
    private PerformanceStats stats;

    @Override
    public Integer call() throws Exception {
        Instant startTime = Instant.now();
        stats = new PerformanceStats();

        try {
            // Initialize engine
            engine = createEngine();

            // Configure options
            ConversionOptions options = createConversionOptions();

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
            System.err.println("Fatal error: " + e.getMessage());
            if (verbose) {
                e.printStackTrace();
            }
            return 2;
        } finally {
            if (engine != null) {
                engine.shutdown();
            }
        }
    }

    /**
     * 检测是否是管道输入
     */
    private boolean isPipeInput() {
        try {
            return System.in.available() > 0;
        } catch (IOException e) {
            return false;
        }
    }

    /**
     * 处理管道输入
     */
    private int processPipeInput(ConversionOptions options) {
        try {
            // 如果没有指定 MIME 类型，尝试检测
            String detectedMimeType = mimeType;
            if (detectedMimeType == null) {
                // 尝试从输入流检测 MIME 类型（读取前几个字节）
                PushbackInputStream pbStream = new PushbackInputStream(System.in, 1024);
                byte[] header = new byte[1024];
                int bytesRead = pbStream.read(header);
                if (bytesRead > 0) {
                    pbStream.unread(header, 0, bytesRead);
                    detectedMimeType = detectMimeTypeFromHeader(header, bytesRead);
                }

                if (detectedMimeType == null) {
                    System.err.println("Error: Cannot detect MIME type from pipe input.");
                    System.err.println("Please specify --mime-type option.");
                    return 1;
                }

                if (!quiet) {
                    System.err.println("Detected MIME type: " + detectedMimeType);
                }
            }

            // 检查是否支持该 MIME 类型
            if (!engine.isSupported(detectedMimeType)) {
                System.err.println("Error: Unsupported MIME type: " + detectedMimeType);
                return 1;
            }

            // 执行转换
            ConversionResult result = engine.convert(System.in, detectedMimeType, options);

            if (result.isSuccessful()) {
                // 输出到 stdout
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

    /**
     * 从文件头检测 MIME 类型
     */
    private String detectMimeTypeFromHeader(byte[] header, int length) {
        String headerStr = new String(header, 0, Math.min(length, 100)).toLowerCase();

        // PDF signature
        if (headerStr.startsWith("%pdf")) {
            return "application/pdf";
        }

        // ZIP-based formats (DOCX, XLSX, PPTX, EPUB)
        if (length >= 4 && header[0] == 0x50 && header[1] == 0x4B) {
            // 需要进一步分析，默认返回 docx
            return "application/vnd.openxmlformats-officedocument.wordprocessingml.document";
        }

        // HTML signatures
        if (headerStr.contains("<!doctype") || headerStr.contains("<html")) {
            return "text/html";
        }

        // XML signatures
        if (headerStr.trim().startsWith("<?xml")) {
            return "application/xml";
        }

        // JSON signatures
        String trimmed = headerStr.trim();
        if (trimmed.startsWith("{") || trimmed.startsWith("[")) {
            return "application/json";
        }

        // Image signatures
        if (length >= 8 && header[0] == (byte)0x89 && header[1] == 0x50 &&
            header[2] == 0x4E && header[3] == 0x47) {
            return "image/png";
        }
        if (length >= 2 && header[0] == (byte)0xFF && header[1] == (byte)0xD8) {
            return "image/jpeg";
        }
        if (length >= 6 && header[0] == 'G' && header[1] == 'I' && header[2] == 'F') {
            return "image/gif";
        }

        // Default to plain text
        if (isTextContent(header, length)) {
            return "text/plain";
        }

        return null;
    }

    /**
     * 检测是否是文本内容
     */
    private boolean isTextContent(byte[] bytes, int length) {
        for (int i = 0; i < length; i++) {
            byte b = bytes[i];
            if (b < 0x20 && b != '\t' && b != '\n' && b != '\r') {
                return false;
            }
        }
        return true;
    }

    /**
     * 顺序处理文件
     */
    private int processFilesSequential(ConversionOptions options) {
        int successCount = 0;
        int errorCount = 0;

        for (int i = 0; i < inputFiles.length; i++) {
            String inputFile = inputFiles[i];

            if (showProgress) {
                showProgress(i + 1, inputFiles.length, inputFile);
            }

            try {
                if (inputFile.contains("*") || inputFile.contains("?")) {
                    int[] counts = processWildcard(inputFile, options);
                    successCount += counts[0];
                    errorCount += counts[1];
                } else {
                    processFile(inputFile, options);
                    successCount++;
                    stats.recordSuccess(inputFile);
                }
            } catch (Exception e) {
                errorCount++;
                stats.recordError(inputFile);
                if (!quiet) {
                    System.err.println("Error processing " + inputFile + ": " + e.getMessage());
                }
                if (verbose) {
                    e.printStackTrace();
                }
            }
        }

        if (showProgress) {
            System.err.println(); // 换行
        }

        if (!quiet && inputFiles.length > 1) {
            System.err.printf("Conversion completed: %d successful, %d failed%n", successCount, errorCount);
        }

        return errorCount > 0 ? 1 : 0;
    }

    /**
     * 并行处理文件
     */
    private int processFilesParallel(ConversionOptions options) {
        List<String> allFiles = new ArrayList<>();

        // 收集所有文件
        for (String inputFile : inputFiles) {
            if (inputFile.contains("*") || inputFile.contains("?")) {
                allFiles.addAll(expandWildcard(inputFile));
            } else {
                allFiles.add(inputFile);
            }
        }

        if (allFiles.isEmpty()) {
            System.err.println("No files to process.");
            return 1;
        }

        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger errorCount = new AtomicInteger(0);
        AtomicInteger processed = new AtomicInteger(0);

        // 并行处理
        List<CompletableFuture<Void>> futures = allFiles.stream()
                .map(inputFile -> CompletableFuture.runAsync(() -> {
                    try {
                        if (showProgress) {
                            int current = processed.incrementAndGet();
                            showProgress(current, allFiles.size(), inputFile);
                        }

                        processFile(inputFile, options);
                        successCount.incrementAndGet();
                        stats.recordSuccess(inputFile);
                    } catch (Exception e) {
                        errorCount.incrementAndGet();
                        stats.recordError(inputFile);
                        if (!quiet) {
                            System.err.println("Error processing " + inputFile + ": " + e.getMessage());
                        }
                    }
                }))
                .collect(java.util.stream.Collectors.toList());

        // 等待所有任务完成
        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();

        if (showProgress) {
            System.err.println();
        }

        if (!quiet) {
            System.err.printf("Parallel conversion completed: %d successful, %d failed%n",
                    successCount.get(), errorCount.get());
        }

        return errorCount.get() > 0 ? 1 : 0;
    }

    /**
     * 显示进度条
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

        // 截断文件名
        String displayName = fileName;
        if (displayName.length() > 30) {
            displayName = "..." + displayName.substring(displayName.length() - 27);
        }

        System.err.printf("\r%s %3d%% (%d/%d) %s",
                bar, percent, current, total, displayName);
    }

    /**
     * 展开通配符为文件列表
     */
    private List<String> expandWildcard(String pattern) {
        List<String> files = new ArrayList<>();
        try {
            Path parentPath = Paths.get(pattern).getParent();
            if (parentPath == null) {
                parentPath = Paths.get(".");
            }

            String fileName = Paths.get(pattern).getFileName().toString();
            String globPattern = fileName.replace("*", ".*").replace("?", ".");

            Files.list(parentPath)
                    .filter(path -> path.getFileName().toString().matches(globPattern))
                    .filter(path -> path.toFile().isFile())
                    .filter(engine::isSupported)
                    .forEach(path -> files.add(path.toString()));
        } catch (IOException e) {
            if (!quiet) {
                System.err.println("Error expanding wildcard: " + e.getMessage());
            }
        }
        return files;
    }

    /**
     * Creates and configures the MarkItDown engine.
     */
    private MarkItDownEngine createEngine() {
        ConverterRegistry registry = new ConverterRegistry();

        // Register all converters
        // PDF
        registry.registerConverter(new PdfConverter());

        // Word (DOCX and DOC)
        registry.registerConverter(new DocxConverter());
        registry.registerConverter(new DocConverter());

        // PowerPoint (PPTX and PPT)
        registry.registerConverter(new PptxConverter());
        registry.registerConverter(new PptConverter());

        // Excel (XLSX and XLS)
        registry.registerConverter(new XlsxConverter());
        registry.registerConverter(new XlsConverter());

        // Web and text formats
        registry.registerConverter(new HtmlConverter());
        registry.registerConverter(new TextConverter());

        // Media
        registry.registerConverter(new ImageConverter());
        registry.registerConverter(new AudioConverter());

        // Archives
        ZipConverter zipConverter = new ZipConverter();
        zipConverter.setDelegate(createZipDelegate(registry));
        registry.registerConverter(zipConverter);

        return new MarkItDownEngine(registry);
    }

    /**
     * Creates a delegate for ZIP converter to handle nested files.
     */
    private ZipConverter.DocumentConverterDelegate createZipDelegate(ConverterRegistry registry) {
        return (inputStream, mimeType, options) -> {
            Optional<DocumentConverter> converterOpt = registry.getConverter(mimeType);
            if (converterOpt.isPresent()) {
                DocumentConverter converter = converterOpt.get();
                if (converter.supportsStreaming()) {
                    return converter.convert(inputStream, mimeType, options);
                }
            }
            // Return empty result for unsupported types
            return new ConversionResult(
                    "Content not converted (unsupported format: " + mimeType + ")",
                    Collections.emptyMap(),
                    Collections.emptyList(),
                    0,
                    "unknown"
            );
        };
    }

    /**
     * Creates conversion options from command-line arguments.
     */
    private ConversionOptions createConversionOptions() {
        ConversionOptions.Builder builder = ConversionOptions.builder();

        // Process boolean options with precedence
        boolean incImages = this.includeImages != null ? this.includeImages : !noImages;
        boolean incTables = this.includeTables != null ? this.includeTables : !noTables;
        boolean incMetadata = this.includeMetadata != null ? this.includeMetadata : !noMetadata;

        // 处理大文件选项
        long effectiveMaxFileSize = maxFileSize;
        if (largeFile) {
            effectiveMaxFileSize = 0; // 0 表示无限制
        }

        builder.includeImages(incImages)
               .includeTables(incTables)
               .includeMetadata(incMetadata)
               .useOcr(useOcr)
               .language(language)
               .tableFormat(tableFormat)
               .imageFormat(imageFormat)
               .maxFileSize(effectiveMaxFileSize);

        if (tempDir != null) {
            builder.tempDirectory(Paths.get(tempDir));
        }

        // 添加PDF密码到自定义选项
        if (pdfPassword != null && !pdfPassword.isEmpty()) {
            builder.customOption("pdfPassword", pdfPassword);
        }

        return builder.build();
    }

    /**
     * Processes a single file.
     */
    private void processFile(String inputFile, ConversionOptions options) throws ConversionException {
        Instant startTime = Instant.now();
        Path inputPath = Paths.get(inputFile);
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

        // Convert the file
        ConversionResult result = engine.convert(inputPath, options);

        // Determine output destination
        if (output == null && inputFiles.length == 1) {
            // Single file, no output specified -> stdout
            System.out.println(result.getMarkdown());
        } else {
            // Multiple files or output specified -> write to file
            Path outputPath = determineOutputPath(inputPath);
            writeResult(result, outputPath);

            if (!quiet && !showProgress) {
                System.err.printf("Converted: %s -> %s%n", inputFile, outputPath);
            }
        }

        // Record stats
        Duration duration = Duration.between(startTime, Instant.now());
        stats.recordFileStats(inputFile, inputFileObj.length(), duration.toMillis());

        if (verbose && result.hasWarnings()) {
            System.err.println("Warnings for " + inputFile + ":");
            for (String warning : result.getWarnings()) {
                System.err.println("  - " + warning);
            }
        }
    }

    /**
     * Processes wildcard patterns.
     */
    private int[] processWildcard(String pattern, ConversionOptions options) {
        int successCount = 0;
        int errorCount = 0;

        List<String> files = expandWildcard(pattern);
        for (String file : files) {
            try {
                processFile(file, options);
                successCount++;
                stats.recordSuccess(file);
            } catch (ConversionException e) {
                errorCount++;
                stats.recordError(file);
                if (!quiet) {
                    System.err.println("Error processing " + file + ": " + e.getMessage());
                }
            }
        }

        return new int[]{successCount, errorCount};
    }

    /**
     * Determines the output path based on input path and options.
     */
    private Path determineOutputPath(Path inputPath) {
        if (output != null) {
            Path outputPath = Paths.get(output);

            // If output is a directory, use input filename with .md extension
            if (Files.isDirectory(outputPath) || output.endsWith("/") || output.endsWith("\\")) {
                String fileName = inputPath.getFileName().toString();
                return outputPath.resolve(fileName + ".md");
            }

            return outputPath;
        }

        // Default: same directory as input with .md extension appended to original filename
        String fileName = inputPath.getFileName().toString();
        Path parentPath = inputPath.getParent();

        if (parentPath != null) {
            return parentPath.resolve(fileName + ".md");
        } else {
            return Paths.get(fileName + ".md");
        }
    }

    /**
     * Writes the conversion result to the output file.
     */
    private void writeResult(ConversionResult result, Path outputPath) throws ConversionException {
        try {
            // Create parent directories if they don't exist
            Path parentPath = outputPath.getParent();
            if (parentPath != null) {
                Files.createDirectories(parentPath);
            }

            // Write the markdown content
            try (BufferedWriter writer = Files.newBufferedWriter(outputPath)) {
                writer.write(result.getMarkdown());
            }

        } catch (IOException e) {
            throw new ConversionException("Failed to write output file: " + e.getMessage());
        }
    }

    /**
     * Gets the file name without extension.
     */
    private String getFileNameWithoutExtension(String fileName) {
        if (fileName == null) {
            return "";
        }

        int lastDotIndex = fileName.lastIndexOf('.');
        if (lastDotIndex > 0) {
            return fileName.substring(0, lastDotIndex);
        }

        return fileName;
    }

    public static void main(String[] args) {
        int exitCode = new CommandLine(new MarkItDownCommand()).execute(args);
        System.exit(exitCode);
    }

    /**
     * 性能统计内部类
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
            System.err.println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
            System.err.println("  性能统计");
            System.err.println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
            System.err.printf("  %-25s %-12s %-10s %-12s%n", "文件", "大小", "时间", "速度");
            System.err.println("  ──────────────────────────────────────────────────────────────");

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

            System.err.println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
            System.err.printf("  总计: %d 文件, %s, %.2fs%n",
                    fileStats.size(), formatSize(totalSize), totalDuration.toMillis() / 1000.0);
            System.err.printf("  成功: %d, 失败: %d%n", successCount.get(), errorCount.get());
            if (totalTime > 0) {
                System.err.printf("  平均速度: %s/s%n", formatSize(totalSize * 1000 / totalTime));
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
