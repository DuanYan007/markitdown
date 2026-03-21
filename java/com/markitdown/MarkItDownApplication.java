package com.markitdown;

import com.markitdown.api.ConversionResult;
import com.markitdown.api.DocumentConverter;
import com.markitdown.cli.MarkItDownCommand;
import com.markitdown.config.ConversionOptions;
import com.markitdown.converter.*;
import com.markitdown.core.ConverterRegistry;
import com.markitdown.core.MarkItDownEngine;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;
import java.util.Collections;
import java.util.Optional;

/**
 * @class MarkItDownApplication
 * @brief MarkItDown Java应用程序主入口类
 * @details 作为整个应用程序的启动入口，负责初始化和配置转换引擎
 *          注册所有可用的文档转换器，提供完整的文档转换功能
 *          委托给CLI命令行工具处理用户交互
 *
 * @author duan yan
 * @version 2.1.0
 * @since 2.0.0
 */
public class MarkItDownApplication {

    private static final Logger logger = LoggerFactory.getLogger(MarkItDownApplication.class);

    /**
     * @brief 应用程序主入口点
     * @details 程序启动的入口方法，直接委托给CLI命令处理器
     * @param args 命令行参数数组
     */
    public static void main(String[] args) {
        // 委托给CLI命令处理器
        MarkItDownCommand.main(args);
    }

    /**
     * @brief 创建并配置带有默认转换器的引擎实例
     * @details 创建转换器注册表并注册所有可用的文档转换器
     *          包括PDF、DOCX、PPTX、XLSX、HTML、图片、音频和文本转换器
     *          同时支持旧版 Office 格式（DOC、XLS、PPT）和 ZIP 压缩包
     * @return MarkItDownEngine 配置完成的引擎实例
     */
    public static MarkItDownEngine createEngine() {
        ConverterRegistry registry = new ConverterRegistry();

        // PDF 转换器
        registry.registerConverter(new PdfConverter());

        // Word 转换器（DOCX 和 DOC）
        registry.registerConverter(new DocxConverter());
        registry.registerConverter(new DocConverter());

        // PowerPoint 转换器（PPTX 和 PPT）
        registry.registerConverter(new PptxConverter());
        registry.registerConverter(new PptConverter());

        // Excel 转换器（XLSX 和 XLS）
        registry.registerConverter(new XlsxConverter());
        registry.registerConverter(new XlsConverter());

        // Web 和文本格式转换器
        registry.registerConverter(new HtmlConverter());
        registry.registerConverter(new TextConverter());

        // 媒体转换器
        registry.registerConverter(new ImageConverter());
        registry.registerConverter(new AudioConverter());

        // ZIP 压缩包转换器
        ZipConverter zipConverter = new ZipConverter();
        zipConverter.setDelegate(createZipDelegate(registry));
        registry.registerConverter(zipConverter);

        return new MarkItDownEngine(registry);
    }

    /**
     * 创建 ZIP 转换器的委托处理器
     */
    private static ZipConverter.DocumentConverterDelegate createZipDelegate(ConverterRegistry registry) {
        return new ZipConverter.DocumentConverterDelegate() {
            @Override
            public ConversionResult convert(InputStream inputStream, String mimeType, ConversionOptions options) {
                Optional<DocumentConverter> converterOpt = registry.getConverter(mimeType);
                if (converterOpt.isPresent()) {
                    DocumentConverter converter = converterOpt.get();
                    if (converter.supportsStreaming()) {
                        try {
                            return converter.convert(inputStream, mimeType, options);
                        } catch (Exception e) {
                            logger.warn("Error converting nested file: {}", e.getMessage());
                        }
                    }
                }
                // 返回空结果表示不支持
                return new ConversionResult(
                        "Content not converted (unsupported format: " + mimeType + ")",
                        Collections.emptyMap(),
                        Collections.emptyList(),
                        0,
                        "unknown"
                );
            }

            @Override
            public boolean isSupported(String mimeType) {
                return registry.isSupported(mimeType);
            }
        };
    }
}
