package com.markitdown.ocr;

import net.sourceforge.tess4j.Tesseract;
import net.sourceforge.tess4j.TesseractException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.Arrays;

/**
 * Tesseract OCR引擎实现
 */
public class TesseractOcrEngine implements OcrEngine {

    private static final Logger logger = LoggerFactory.getLogger(TesseractOcrEngine.class);

    private final Tesseract tesseract;
    private final String datapath;

    public TesseractOcrEngine() {
        this(null);
    }

    public TesseractOcrEngine(String datapath) {
        // 如果没有指定datapath,尝试自动检测
        if (datapath == null || datapath.isEmpty()) {
            // 尝试常见安装位置
            String[] possiblePaths = {
                "O:\\tesserOCR",  // 你的安装位置
                "C:\\Program Files\\Tesseract-OCR",
                "C:\\Program Files (x86)\\Tesseract-OCR",
                "/usr/local/bin",
                "/usr/bin"
            };

            for (String path : possiblePaths) {
                File testFile = new File(path);
                if (testFile.exists()) {
                    File tessdataDir = new File(path, "tessdata");
                    if (tessdataDir.exists()) {
                        datapath = path;
                        logger.info("自动检测到Tesseract路径: {}", datapath);
                        break;
                    }
                }
            }

            if (datapath == null) {
                logger.debug("未找到Tesseract安装目录,使用默认配置");
            }
        }

        this.datapath = datapath;
        this.tesseract = new Tesseract();

        // 设置数据路径和tessdata子目录
        if (datapath != null && !datapath.isEmpty()) {
            // 检查是否需要添加tessdata子目录
            File tessdataDir = new File(datapath, "tessdata");
            if (tessdataDir.exists()) {
                tesseract.setDatapath(tessdataDir.getAbsolutePath());
                logger.info("设置tessdata路径: {}", tessdataDir.getAbsolutePath());
            } else {
                tesseract.setDatapath(datapath);
            }
        }

        // 设置默认语言为英文+中文
        tesseract.setLanguage("eng+chi_sim");

        logger.info("Tesseract OCR引擎初始化完成 (数据路径: {})",
                    datapath != null ? datapath : "默认");
    }

    @Override
    public String extractText(File imageFile) throws OcrException {
        return extractText(imageFile, "eng+chi_sim");
    }

    @Override
    public String extractText(File imageFile, String language) throws OcrException {
        if (!imageFile.exists()) {
            throw new OcrException("图像文件不存在: " + imageFile.getAbsolutePath());
        }

        if (!imageFile.isFile()) {
            throw new OcrException("路径不是文件: " + imageFile.getAbsolutePath());
        }

        try {
            // 设置语言
            tesseract.setLanguage(language);

            logger.info("正在使用Tesseract提取文本: {} (语言: {})", imageFile.getName(), language);

            // 执行OCR
            long startTime = System.currentTimeMillis();
            String text = tesseract.doOCR(imageFile);
            long duration = System.currentTimeMillis() - startTime;

            logger.info("OCR完成: {} (耗时: {}ms, 提取字符数: {})",
                       imageFile.getName(), duration, text.length());

            return text;

        } catch (TesseractException e) {
            String errorMsg = "OCR处理失败: " + e.getMessage();
            logger.error(errorMsg, e);
            throw new OcrException(errorMsg, e);
        }
    }

    @Override
    public boolean isAvailable() {
        // 检查Tesseract可执行文件是否存在
        try {
            String os = System.getProperty("os.name").toLowerCase();
            String[] commands;

            if (os.contains("win")) {
                // Windows: 检查我们已知的安装路径
                String[] possiblePaths = {
                    "O:\\tesserOCR\\tesseract.exe",
                    "C:\\Program Files\\Tesseract-OCR\\tesseract.exe",
                    "C:\\Program Files (x86)\\Tesseract-OCR\\tesseract.exe"
                };

                for (String path : possiblePaths) {
                    File exeFile = new File(path);
                    if (exeFile.exists()) {
                        logger.debug("找到Tesseract可执行文件: {}", path);
                        return true;
                    }
                }

                // 也尝试通过PATH查找
                try {
                    commands = new String[]{"where", "tesseract"};
                    Process process = Runtime.getRuntime().exec(commands);
                    int exitCode = process.waitFor();
                    if (exitCode == 0) {
                        logger.debug("通过PATH找到Tesseract");
                        return true;
                    }
                } catch (Exception e) {
                    // PATH查找失败,继续尝试其他方法
                }

                return false;

            } else if (os.contains("mac") || os.contains("nix") || os.contains("nux")) {
                commands = new String[]{"which", "tesseract"};
                Process process = Runtime.getRuntime().exec(commands);
                int exitCode = process.waitFor();
                return exitCode == 0;
            } else {
                return false;
            }

        } catch (Exception e) {
            logger.debug("Tesseract可用性检测失败: {}", e.getMessage());
            return false;
        }
    }

    @Override
    public String getEngineName() {
        return "Tesseract";
    }

    /**
     * 设置OCR语言
     */
    public void setLanguage(String language) {
        tesseract.setLanguage(language);
        logger.debug("OCR语言设置为: {}", language);
    }

    /**
     * 设置OCR页面分割模式
     */
    public void setPageSegMode(int mode) {
        tesseract.setPageSegMode(mode);
        logger.debug("页面分割模式设置为: {}", mode);
    }

    /**
     * 获取支持的语言列表
     */
    public String[] getSupportedLanguages() {
        // 返回常用语言列表
        return new String[]{"eng", "chi_sim", "chi_tra", "jpn", "kor"};
    }
}
