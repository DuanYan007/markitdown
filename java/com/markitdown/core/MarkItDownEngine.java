package com.markitdown.core;

import com.markitdown.api.ConversionResult;
import com.markitdown.api.DocumentConverter;
import com.markitdown.config.ConversionOptions;
import com.markitdown.exception.ConversionException;
import com.markitdown.utils.FileTypeDetector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ForkJoinPool;
import java.util.stream.Collectors;

/**
 * @class MarkItDownEngine
 * @brief 文档转换核心引擎，提供统一的文档转换服务
 * @details 作为整个转换系统的主要入口点，集成转换器注册表、文件类型检测
 *          提供文件验证、转换器选择、异常处理等完整功能
 *          支持多种配置选项和转换器管理操作
 *          支持 Path 和 InputStream 两种输入方式，支持异步和并行处理
 *
 * @author duan yan
 * @version 2.1.0
 * @since 2.0.0
 */
public class MarkItDownEngine {

    private static final Logger logger = LoggerFactory.getLogger(MarkItDownEngine.class);

    /**
     * @brief 转换器注册表
     * @details 管理所有已注册的文档转换器，支持动态查找和选择
     */
    private final ConverterRegistry converterRegistry;

    /**
     * @brief 异步处理线程池
     * @details 用于异步转换和并行批量处理
     */
    private final ExecutorService executorService;

    /**
     * Creates a new MarkItDownEngine with a default converter registry.
     */
    public MarkItDownEngine() {
        this.converterRegistry = new ConverterRegistry();
        this.executorService = ForkJoinPool.commonPool();
    }

    /**
     * Creates a new MarkItDownEngine with the specified converter registry.
     *
     * @param converterRegistry the converter registry to use
     */
    public MarkItDownEngine(ConverterRegistry converterRegistry) {
        this.converterRegistry = Objects.requireNonNull(converterRegistry, "Converter registry cannot be null");
        this.executorService = ForkJoinPool.commonPool();
    }

    /**
     * Creates a new MarkItDownEngine with custom executor service.
     *
     * @param converterRegistry the converter registry to use
     * @param executorService   the executor service for async operations
     */
    public MarkItDownEngine(ConverterRegistry converterRegistry, ExecutorService executorService) {
        this.converterRegistry = Objects.requireNonNull(converterRegistry, "Converter registry cannot be null");
        this.executorService = Objects.requireNonNull(executorService, "Executor service cannot be null");
    }

    // ==================== 同步转换方法 ====================

    /**
     * Converts a document file to Markdown format using default options.
     *
     * @param filePath the path to the document file to convert
     * @return the conversion result
     * @throws ConversionException if the conversion fails
     */
    public ConversionResult convert(Path filePath) throws ConversionException {
        return convert(filePath, new ConversionOptions());
    }

    /**
     * Converts a document file to Markdown format.
     *
     * @param filePath the path to the document file to convert
     * @param options  the conversion options to apply
     * @return the conversion result
     * @throws ConversionException if the conversion fails
     */
    public ConversionResult convert(Path filePath, ConversionOptions options) throws ConversionException {
        Objects.requireNonNull(filePath, "File path cannot be null");
        Objects.requireNonNull(options, "Conversion options cannot be null");

        logger.info("Starting conversion for file: {}", filePath);

        // Validate file
        validateFile(filePath, options);

        try {
            // Detect MIME type
            String mimeType = FileTypeDetector.detectMimeType(filePath);
            logger.debug("Detected MIME type: {}", mimeType);

            return doConvert(filePath, null, mimeType, options);

        } catch (IOException e) {
            String errorMessage = "I/O error during conversion: " + e.getMessage();
            logger.error(errorMessage, e);
            throw new ConversionException(errorMessage, e, filePath.getFileName().toString(), "unknown");
        } catch (Exception e) {
            String errorMessage = "Unexpected error during conversion: " + e.getMessage();
            logger.error(errorMessage, e);
            throw new ConversionException(errorMessage, e, filePath.getFileName().toString(), "unknown");
        }
    }

    /**
     * Converts a document stream to Markdown format using default options.
     *
     * @param inputStream the input stream containing document content
     * @param mimeType    the MIME type of the document
     * @return the conversion result
     * @throws ConversionException if the conversion fails
     * @since 2.1.0
     */
    public ConversionResult convert(InputStream inputStream, String mimeType) throws ConversionException {
        return convert(inputStream, mimeType, new ConversionOptions());
    }

    /**
     * Converts a document stream to Markdown format.
     *
     * @param inputStream the input stream containing document content
     * @param mimeType    the MIME type of the document
     * @param options     the conversion options to apply
     * @return the conversion result
     * @throws ConversionException if the conversion fails
     * @since 2.1.0
     */
    public ConversionResult convert(InputStream inputStream, String mimeType, ConversionOptions options) throws ConversionException {
        Objects.requireNonNull(inputStream, "Input stream cannot be null");
        Objects.requireNonNull(mimeType, "MIME type cannot be null");
        Objects.requireNonNull(options, "Conversion options cannot be null");

        logger.info("Starting stream conversion for MIME type: {}", mimeType);

        return doConvert(null, inputStream, mimeType, options);
    }

    /**
     * Internal conversion method that handles both Path and InputStream inputs.
     *
     * @param filePath    the file path (can be null for stream conversion)
     * @param inputStream the input stream (can be null for file conversion)
     * @param mimeType    the MIME type
     * @param options     the conversion options
     * @return the conversion result
     * @throws ConversionException if the conversion fails
     */
    private ConversionResult doConvert(Path filePath, InputStream inputStream,
                                       String mimeType, ConversionOptions options) throws ConversionException {
        // Find appropriate converter
        Optional<DocumentConverter> converterOpt = converterRegistry.getConverter(mimeType);
        if (!converterOpt.isPresent()) {
            String supportedTypes = converterRegistry.getSupportedMimeTypes().toString();
            String errorMessage = String.format(
                    "No converter found for MIME type '%s'. Supported types: %s",
                    mimeType, supportedTypes);
            logger.error(errorMessage);

            // Safely get file info
            long fileSize = 0;
            String fileName = "stream";
            if (filePath != null) {
                try {
                    fileSize = Files.size(filePath);
                    fileName = filePath.getFileName().toString();
                } catch (Exception e) {
                    logger.warn("Cannot get file info: {}", e.getMessage());
                }
            }

            return new ConversionResult(List.of(errorMessage), fileSize, fileName);
        }

        DocumentConverter converter = converterOpt.get();
        logger.debug("Using converter: {}", converter.getName());

        // Perform conversion
        ConversionResult result;
        if (inputStream != null && converter.supportsStreaming()) {
            // Use stream-based conversion
            result = converter.convert(inputStream, mimeType, options);
        } else if (filePath != null) {
            // Use path-based conversion
            result = converter.convert(filePath, options);
        } else {
            throw new ConversionException(
                    "Converter " + converter.getName() + " does not support stream-based conversion",
                    "stream",
                    converter.getName()
            );
        }

        if (result.isSuccessful()) {
            logger.info("Successfully converted {} ({} bytes)",
                filePath != null ? filePath : "stream", result.getFileSize());
        } else {
            logger.warn("Conversion completed with warnings");
        }

        return result;
    }

    // ==================== 异步转换方法 ====================

    /**
     * Converts a document file to Markdown format asynchronously.
     *
     * @param filePath the path to the document file to convert
     * @return a CompletableFuture containing the conversion result
     * @since 2.1.0
     */
    public CompletableFuture<ConversionResult> convertAsync(Path filePath) {
        return convertAsync(filePath, new ConversionOptions());
    }

    /**
     * Converts a document file to Markdown format asynchronously.
     *
     * @param filePath the path to the document file to convert
     * @param options  the conversion options to apply
     * @return a CompletableFuture containing the conversion result
     * @since 2.1.0
     */
    public CompletableFuture<ConversionResult> convertAsync(Path filePath, ConversionOptions options) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                return convert(filePath, options);
            } catch (ConversionException e) {
                throw new RuntimeException(e);
            }
        }, executorService);
    }

    /**
     * Converts a document stream to Markdown format asynchronously.
     *
     * @param inputStream the input stream containing document content
     * @param mimeType    the MIME type of the document
     * @param options     the conversion options to apply
     * @return a CompletableFuture containing the conversion result
     * @since 2.1.0
     */
    public CompletableFuture<ConversionResult> convertAsync(InputStream inputStream, String mimeType, ConversionOptions options) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                return convert(inputStream, mimeType, options);
            } catch (ConversionException e) {
                throw new RuntimeException(e);
            }
        }, executorService);
    }

    // ==================== 批量并行转换方法 ====================

    /**
     * Converts multiple files in parallel.
     *
     * @param filePaths the list of file paths to convert
     * @param options   the conversion options to apply
     * @return a list of CompletableFuture containing conversion results
     * @since 2.1.0
     */
    public List<CompletableFuture<ConversionResult>> convertParallel(List<Path> filePaths, ConversionOptions options) {
        return filePaths.stream()
                .map(path -> convertAsync(path, options))
                .collect(Collectors.toList());
    }

    /**
     * Converts multiple files in parallel and waits for all to complete.
     *
     * @param filePaths the list of file paths to convert
     * @param options   the conversion options to apply
     * @return a list of conversion results
     * @since 2.1.0
     */
    public List<ConversionResult> convertAll(List<Path> filePaths, ConversionOptions options) {
        List<CompletableFuture<ConversionResult>> futures = convertParallel(filePaths, options);
        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();
        return futures.stream()
                .map(CompletableFuture::join)
                .collect(Collectors.toList());
    }

    // ==================== 验证方法 ====================

    /**
     * Validates the input file and options.
     *
     * @param filePath the file path to validate
     * @param options  the conversion options
     * @throws ConversionException if validation fails
     */
    private void validateFile(Path filePath, ConversionOptions options) throws ConversionException {
        // Check if file exists
        if (!Files.exists(filePath)) {
            throw new ConversionException("File does not exist: " + filePath,
                    filePath.getFileName().toString(), "validation");
        }

        // Check if it's a regular file
        if (!Files.isRegularFile(filePath)) {
            throw new ConversionException("Path is not a regular file: " + filePath,
                    filePath.getFileName().toString(), "validation");
        }

        // Check file size (only if maxFileSize > 0)
        try {
            long fileSize = Files.size(filePath);
            long maxFileSize = options.getMaxFileSize();
            if (maxFileSize > 0 && fileSize > maxFileSize) {
                String errorMessage = String.format(
                        "File size (%d bytes) exceeds maximum allowed size (%d bytes). Use --large-file option to process large files.",
                        fileSize, maxFileSize);
                throw new ConversionException(errorMessage,
                        filePath.getFileName().toString(), "validation");
            }
        } catch (IOException e) {
            throw new ConversionException("Cannot determine file size: " + e.getMessage(),
                    filePath.getFileName().toString(), "validation");
        }

        // Check if file is readable
        if (!Files.isReadable(filePath)) {
            throw new ConversionException("File is not readable: " + filePath,
                    filePath.getFileName().toString(), "validation");
        }
    }

    // ==================== 转换器管理方法 ====================

    /**
     * Registers a document converter with the engine.
     *
     * @param converter the converter to register
     */
    public void registerConverter(DocumentConverter converter) {
        converterRegistry.registerConverter(converter);
    }

    /**
     * Unregisters a document converter by name.
     *
     * @param converterName the name of the converter to unregister
     * @return true if the converter was found and removed, false otherwise
     */
    public boolean unregisterConverter(String converterName) {
        return converterRegistry.unregisterConverter(converterName);
    }

    /**
     * Gets the converter registry.
     *
     * @return the converter registry
     */
    public ConverterRegistry getConverterRegistry() {
        return converterRegistry;
    }

    // ==================== 查询方法 ====================

    /**
     * Checks if a file type is supported.
     *
     * @param filePath the file path to check
     * @return true if supported, false otherwise
     */
    public boolean isSupported(Path filePath) {
        try {
            String mimeType = FileTypeDetector.detectMimeType(filePath);
            return converterRegistry.isSupported(mimeType);
        } catch (IOException e) {
            logger.warn("Cannot determine MIME type for file: {}", filePath, e);
            return false;
        }
    }

    /**
     * Checks if a MIME type is supported.
     *
     * @param mimeType the MIME type to check
     * @return true if supported, false otherwise
     * @since 2.1.0
     */
    public boolean isSupported(String mimeType) {
        return converterRegistry.isSupported(mimeType);
    }

    /**
     * Gets all supported MIME types.
     *
     * @return a set of supported MIME types
     */
    public Set<String> getSupportedMimeTypes() {
        return converterRegistry.getSupportedMimeTypes();
    }

    /**
     * Gets information about all registered converters.
     *
     * @return a map of converter names to their information
     */
    public Map<String, String> getConverterInfo() {
        return converterRegistry.getConverterInfo();
    }

    /**
     * Shuts down the executor service.
     * Should be called when the engine is no longer needed.
     *
     * @since 2.1.0
     */
    public void shutdown() {
        if (executorService != ForkJoinPool.commonPool()) {
            executorService.shutdown();
        }
    }
}
