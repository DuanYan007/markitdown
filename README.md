# MarkItDown

[English](README.en.md) | Chinese

将 PDF、Office 文档、图片、HTML、压缩包和文本文件转换为 Markdown，适用于 AI 预处理、知识库整理、批量归档和自动化内容流水线。

## 仓库结构

当前仓库主要包含三个子项目：

1. `java/`：主线 Java CLI，也是当前推荐给最终用户的交付物
2. `markitdown-mcp/`：MCP 服务端集成项目
3. `markitdown-web/`：较早期的 Web 应用方向

如果你是第一次使用本项目，建议直接从 Java CLI 开始：

- [Java CLI 中文文档](java/README.md)
- [Java CLI English Guide](java/README.en.md)

## 项目能力

- 将 PDF、Word、Excel、PowerPoint、HTML、图片、文本、JSON、XML、CSV、ZIP 转为 Markdown
- 对扫描版 PDF 和图片启用 OCR 文本提取
- 支持多种 OCR 后端：
  - `tess4j`
  - `tesseract-cli`
  - `paddleocr`
  - `http`
- 通过 Maven Profile 构建不同平台制品
- 适用于本地自动化、批处理和 AI 文档预处理场景

## 快速开始

### 从源码构建

```bash
mvn package -DskipTests
```

默认会生成轻量版 Java CLI 制品。

### 构建指定制品

```bash
mvn package -DskipTests -Pfull
mvn package -DskipTests -Pwin32
mvn package -DskipTests -Pwin64
mvn package -DskipTests -Plinux64
mvn package -DskipTests -Pmac
```

### 制品说明

| Profile | 制品名 | 推荐场景 |
| --- | --- | --- |
| `lite` | `markitdown4j-<version>-lite.jar` | 最小体积，不内置 `tess4j` |
| `full` | `markitdown4j-<version>-full.jar` | 完整 OCR 资源 |
| `win32` | `markitdown4j-<version>-win32.jar` | 32 位 Windows |
| `win64` | `markitdown4j-<version>-win64.jar` | 64 位 Windows |
| `linux64` | `markitdown4j-<version>-linux64.jar` | Linux + 外部或远程 OCR |
| `mac` | `markitdown4j-<version>-mac.jar` | macOS + 外部或远程 OCR |

### 使用示例

```bash
java -jar target/markitdown4j-0.0.3-lite.jar test/basic.txt -o out/basic.md
java -jar target/markitdown4j-0.0.3-win64.jar test/with-text.png --ocr --ocr-engine tess4j -o out/ocr.md
java -jar target/markitdown4j-0.0.3-lite.jar test/with-text.png --ocr --ocr-engine paddleocr -o out/paddle.md
```

## OCR 配置

项目当前采用统一配置方式接入 OCR，用户只需要切换配置，不需要改转换流程。

示例：

```properties
ocr.enable=true
ocr.engine=paddleocr
ocr.endpoint=https://paddleocr.aistudio-app.com/api/v2/ocr/jobs
ocr.api.key=YOUR_TOKEN
ocr.model=PaddleOCR-VL-1.5
ocr.timeout=30000
ocr.poll.interval=5000
```

当前推荐：

- `tess4j`：适合 Windows 内嵌 OCR
- `tesseract-cli`：适合 Linux / macOS 本地 OCR
- `paddleocr`：适合远程结构化 OCR
- `http`：适合接自定义远程 OCR 服务

## 测试与验证

项目当前的测试分为三层：

### 1. 自动化测试

当前已纳入 `mvn test` 的测试包括：

- Profile 构建与命名验证
- OCR 工厂选择逻辑
- PaddleOCR 结果解析
- 文本流式转换
- ZIP 内部委托转换

可直接执行：

```bash
mvn test
```

### 2. 样例文件集验证

仓库中的 [`test/test.zip`](test/test.zip) 是完整测试文件包，当前包含约 104 个测试文件，是回归、兼容性验证和 release 前手工检查的核心测试资产。

解压后的 [`test/`](test/README.md) 目录用于浏览和按文件执行验证命令，覆盖：

- PDF
- Word
- Excel
- PowerPoint
- 图片 OCR
- 音频元数据
- HTML
- JSON / XML / CSV / TXT
- ZIP 归档和嵌套归档
- 大文件、空文件、加密文件、多语言文件

### 3. 发布前集成验证

在本次 `v0.0.3` 发布前，已经实际验证过这些关键路径：

- `lite` 基础文本转换
- `win64 + tess4j` OCR
- `linux64 + tesseract-cli` OCR
- `lite + paddleocr` 远程 OCR
- PDF / DOCX / XLSX / HTML / ZIP / 音频元数据转换

如果你想快速复现，建议优先查看：

- [`test/test.zip`](test/test.zip)
- [`test/README.md`](test/README.md)

## 文档

- [Java CLI 中文文档](java/README.md)
- [Java CLI English Guide](java/README.en.md)
- [命令参考](java/COMMAND_REFERENCE.md)
- [OCR 扩展路线图](OCR_PROVIDER_ROADMAP.md)

## 子项目

- [Java CLI](java/README.md)
- [MCP Server](markitdown-mcp/README.md)
- [Web App](markitdown-web/readme.md)

## License

[MIT](LICENSE)
