package com.markitdown.cli;

import com.markitdown.exception.ConversionException;
import picocli.CommandLine;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

/**
 * 用户消息助手类，提供友好的错误消息和解决建议
 */
public class UserMessageHelper {

    /**
     * 获取用户友好的错误消息和解决方案
     */
    public static String getUserFriendlyError(Throwable e) {
        if (e instanceof ConversionException) {
            return formatConversionError((ConversionException) e);
        }

        String message = e.getMessage();
        if (message == null) {
            message = e.getClass().getSimpleName();
        }

        // 常见错误模式识别和建议
        if (message.contains("Cannot decrypt PDF") || message.contains("password")) {
            return "🔒 PDF文件需要密码\n\n" +
                   "错误: " + message + "\n\n" +
                   "💡 解决方案:\n" +
                   "  • 使用 --pdf-password 选项提供密码\n" +
                   "  • 示例: markitdown file.pdf --pdf-password yourpassword\n";
        }

        if (message.contains("exceeds maximum allowed size")) {
            return "📏 文件过大\n\n" +
                   "错误: " + message + "\n\n" +
                   "💡 解决方案:\n" +
                   "  • 使用 --large-file 选项允许处理大文件\n" +
                   "  • 示例: markitdown large-file.pdf --large-file\n" +
                   "  • 或者: markitdown large-file.pdf --optimize-memory\n";
        }

        if (message.contains("Unsupported file type")) {
            return "❌ 不支持的文件类型\n\n" +
                   "错误: " + message + "\n\n" +
                   "💡 支持的文件类型:\n" +
                   "  • PDF: .pdf\n" +
                   "  • Word: .docx, .doc\n" +
                   "  • Excel: .xlsx, .xls\n" +
                   "  • PowerPoint: .pptx, .ppt\n" +
                   "  • HTML: .html, .htm\n" +
                   "  • 图片: .png, .jpg, .gif, .bmp (需 --ocr)\n" +
                   "  • 音频: .mp3, .wav, .m4a\n" +
                   "  • 压缩包: .zip\n" +
                   "  • 文本: .txt, .md\n";
        }

        if (message.contains("Tesseract") || message.contains("OCR")) {
            return "🔍 OCR相关错误\n\n" +
                   "错误: " + message + "\n\n" +
                   "💡 解决方案:\n" +
                   "  • 确保Tesseract OCR已正确安装\n" +
                   "  • 检查语言包是否安装: --language选项\n" +
                   "  • 下载地址: https://github.com/tesseract-ocr/tesseract\n" +
                   "  • 语言包: https://github.com/tesseract-ocr/tessdata\n";
        }

        if (message.contains("Out of memory") || message.contains("Java heap space")) {
            return "💾 内存不足\n\n" +
                   "错误: " + message + "\n\n" +
                   "💡 解决方案:\n" +
                   "  • 使用 --optimize-memory 启用内存优化\n" +
                   "  • 增加JVM内存: java -Xmx2g -jar markitdown.jar\n" +
                   "  • 处理大文件时使用 --large-file\n";
        }

        // 默认友好消息
        return "⚠️ 转换失败\n\n" +
               "错误: " + message + "\n\n" +
               "💡 建议:\n" +
               "  • 使用 --verbose 查看详细错误信息\n" +
               "  • 检查文件是否损坏或格式是否正确\n" +
               "  • 尝试使用 --help 查看所有选项\n";
    }

    /**
     * 格式化转换错误
     */
    private static String formatConversionError(ConversionException e) {
        StringBuilder sb = new StringBuilder();

        String fileName = e.getFileName();
        String converterName = e.getConverterName();

        if (fileName != null) {
            sb.append("📄 文件: ").append(fileName).append("\n");
        }
        if (converterName != null) {
            sb.append("⚙️  转换器: ").append(converterName).append("\n");
        }

        sb.append("\n错误: ").append(e.getMessage()).append("\n");

        // 添加特定转换器的建议
        if (converterName != null) {
            sb.append("\n💡 ").append(getConverterSpecificAdvice(converterName));
        }

        return sb.toString();
    }

    /**
     * 获取特定转换器的建议
     */
    private static String getConverterSpecificAdvice(String converterName) {
        switch (converterName) {
            case "PdfConverter":
                return "PDF处理建议:\n" +
                       "  • 加密PDF: 使用 --pdf-password\n" +
                       "  • 扫描PDF: 使用 --ocr 启用OCR\n" +
                       "  • 大文件: 使用 --large-file\n" +
                       "  • 内存优化: 使用 --optimize-memory";

            case "DocxConverter":
                return "Word文档建议:\n" +
                       "  • 确保文档不是损坏的\n" +
                       "  • 检查文档是否受密码保护\n" +
                       "  • 尝试在Word中打开并重新保存";

            case "XlsxConverter":
                return "Excel表格建议:\n" +
                       "  • 大文件: 使用 --large-file\n" +
                       "  • 复杂公式可能无法完全转换\n" +
                       "  • 尝试另存为.xlsx格式";

            case "ImageConverter":
                return "图片处理建议:\n" +
                       "  • 确保 --ocr 选项已启用\n" +
                       "  • 检查Tesseract是否正确安装\n" +
                       "  • 尝试提高图片分辨率\n" +
                       "  • 使用 --language 指定正确语言";

            default:
                return "使用 --verbose 查看更多详细信息";
        }
    }

    /**
     * 获取使用示例
     */
    public static String getUsageExamples() {
        return "📚 常用使用示例:\n\n" +
               "基础转换:\n" +
               "  markitdown document.pdf\n" +
               "  markitdown report.docx -o report.md\n\n" +

               "OCR识别:\n" +
               "  markitdown scanned.pdf --ocr\n" +
               "  markitdown image.png --ocr -l chi_sim\n\n" +

               "批量处理:\n" +
               "  markitdown *.pdf --parallel\n" +
               "  markitdown docs/ --batch --recursive\n\n" +

               "大文件处理:\n" +
               "  markitdown large.pdf --large-file\n" +
               "  markitdown huge.xlsx --optimize-memory\n\n" +

               "内容控制:\n" +
               "  markitdown file.docx --no-images --no-tables\n" +
               "  markitdown file.pdf --no-metadata\n\n" +

               "性能选项:\n" +
               "  markitdown *.pdf --parallel --threads=8 --stats\n" +
               "  markitdown file.pdf --progress --verbose\n";
    }

    /**
     * 获取文件类型检测信息
     */
    public static String getFileTypeDetectionInfo(String filePath) {
        if (filePath == null || filePath.isEmpty()) {
            return "❓ 无法检测文件类型";
        }

        Path path = Paths.get(filePath);
        String fileName = path.getFileName().toString();
        String extension = getFileExtension(fileName);

        StringBuilder info = new StringBuilder();
        info.append("📋 文件信息:\n");
        info.append("  文件名: ").append(fileName).append("\n");
        info.append("  扩展名: ").append(extension).append("\n");

        String detectedType = detectFileType(extension);
        info.append("  检测类型: ").append(detectedType).append("\n");

        return info.toString();
    }

    /**
     * 获取文件扩展名
     */
    private static String getFileExtension(String fileName) {
        int lastDot = fileName.lastIndexOf('.');
        return lastDot > 0 ? fileName.substring(lastDot + 1).toLowerCase() : "";
    }

    /**
     * 根据扩展名检测文件类型
     */
    private static String detectFileType(String extension) {
        switch (extension) {
            case "pdf": return "PDF文档 (支持OCR)";
            case "docx": case "doc": return "Word文档";
            case "xlsx": case "xls": return "Excel表格";
            case "pptx": case "ppt": return "PowerPoint演示文稿";
            case "html": case "htm": return "HTML网页";
            case "png": case "jpg": case "jpeg": case "gif": case "bmp": return "图片 (需OCR)";
            case "mp3": case "wav": case "m4a": return "音频文件";
            case "zip": return "压缩包";
            case "txt": case "md": return "文本文件";
            default: return "未知类型";
        }
    }
}