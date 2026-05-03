# MarkItDown Java CLI

[English](README.en.md) | 简体中文 | [返回仓库首页](../README.md)

`markitdown4j` 是当前仓库的主线项目。它是一个 Java 命令行工具，用来把常见文档格式转换成 Markdown，方便 AI 预处理、知识库整理、批量归档和自动化流程。

## 功能概览

- 支持 PDF、Word、Excel、PowerPoint、HTML、图片、文本、JSON、XML、CSV、ZIP
- 支持扫描版 PDF 和图片 OCR
- 支持可插拔 OCR 后端
- 支持平台化打包和轻量化制品
- 支持批量、递归、并行转换

## 环境要求

- Java 17+
- Maven 3.9+（从源码构建时）

## 快速开始

### 1. 构建

```bash
mvn package -DskipTests
```

### 2. 运行

```bash
java -jar target/markitdown4j-0.0.3-lite.jar test/basic.txt -o out/basic.md
```

### 3. 查看帮助

```bash
java -jar target/markitdown4j-0.0.3-lite.jar --help
```

## 制品与 Profile

| Profile | 产物 | OCR 策略 | 适用场景 |
| --- | --- | --- | --- |
| `lite` | `markitdown4j-<version>-lite.jar` | 不内置 `tess4j` | 默认下载、CI、远程 OCR |
| `full` | `markitdown4j-<version>-full.jar` | 内置完整 `tess4j` | Windows 一包即用 |
| `win32` | `markitdown4j-<version>-win32.jar` | 仅 32 位 Windows native | 32 位 Windows |
| `win64` | `markitdown4j-<version>-win64.jar` | 仅 64 位 Windows native | 64 位 Windows |
| `linux64` | `markitdown4j-<version>-linux64.jar` | 不内置 `tess4j` | Linux + 外部或远程 OCR |
| `mac` | `markitdown4j-<version>-mac.jar` | 不内置 `tess4j` | macOS + 外部或远程 OCR |

构建示例：

```bash
mvn package -DskipTests
mvn package -DskipTests -Pfull
mvn package -DskipTests -Pwin64
mvn package -DskipTests -Plinux64
mvn package -DskipTests -Pmac
```

## OCR 后端

当前可用的 OCR engine：

- `tess4j`
- `tesseract-cli`
- `paddleocr`
- `http`

推荐组合：

- Windows：`win64` 或 `full` + `--ocr-engine tess4j`
- Linux / macOS：`linux64` / `mac` + `--ocr-engine tesseract-cli`
- 远程结构化 OCR：`--ocr-engine paddleocr`
- 自定义远程 OCR：`--ocr-engine http`

### 统一 OCR 配置

```properties
ocr.enable=true
ocr.engine=paddleocr
ocr.endpoint=https://paddleocr.aistudio-app.com/api/v2/ocr/jobs
ocr.api.key=YOUR_TOKEN
ocr.model=PaddleOCR-VL-1.5
ocr.timeout=30000
ocr.poll.interval=5000
ocr.language=auto
```

### OCR 使用示例

```bash
# Windows embedded OCR
java -jar target/markitdown4j-0.0.3-win64.jar test/with-text.png --ocr --ocr-engine tess4j -o out/ocr.md

# Linux / macOS local OCR
java -jar target/markitdown4j-0.0.3-linux64.jar test/with-text.png --ocr --ocr-engine tesseract-cli -o out/ocr.md

# Remote PaddleOCR
java -jar target/markitdown4j-0.0.3-lite.jar test/with-text.png --ocr --ocr-engine paddleocr -o out/paddle.md
```

## 支持格式

| 类别 | 格式 |
| --- | --- |
| PDF | `.pdf` |
| Word | `.docx`, `.doc` |
| Excel | `.xlsx`, `.xls` |
| PowerPoint | `.pptx`, `.ppt` |
| HTML | `.html`, `.htm` |
| 图片 | `.png`, `.jpg`, `.jpeg`, `.gif`, `.bmp`, `.tiff` |
| 文本 | `.txt`, `.csv`, `.json`, `.xml` |
| 压缩包 | `.zip` |

## 常用命令

```bash
# PDF 转 Markdown
java -jar target/markitdown4j-0.0.3-lite.jar test/plain-text.pdf -o out/plain-text.md

# Word 转 Markdown
java -jar target/markitdown4j-0.0.3-lite.jar test/basic.docx -o out/basic.md

# 批量处理
java -jar target/markitdown4j-0.0.3-lite.jar test --batch -o out/
```

## 文档

- [命令参考](COMMAND_REFERENCE.md)
- [OCR Provider 路线图](../OCR_PROVIDER_ROADMAP.md)

## License

[MIT](../LICENSE)
