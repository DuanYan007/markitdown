# MarkItDown

[English](README.en.md) | 简体中文

Convert PDFs, Office documents, images, HTML, archives, and text files into Markdown for AI workflows, knowledge bases, and content pipelines.

## What This Repository Contains

This repository currently includes three subprojects:

1. `java/` - the main Java CLI and the primary end-user deliverable
2. `markitdown-mcp/` - an MCP server for tool integration
3. `markitdown-web/` - an older web application path

If you are new to the project, start with the Java CLI in [java/README.md](java/README.md).

## What MarkItDown Can Do

- Convert PDF, Word, Excel, PowerPoint, HTML, images, audio metadata, text, JSON, XML, CSV, and ZIP archives to Markdown
- Extract text from scanned PDFs and images through pluggable OCR providers
- Support multiple OCR backends including `tess4j`, `tesseract-cli`, and remote providers such as `paddleocr`
- Produce smaller platform-focused artifacts with Maven profiles
- Work well in local automation, batch conversion, and AI preprocessing pipelines

## Quick Start

### Option 1: Build from source

```bash
mvn package -DskipTests
```

The default output is a lightweight Java CLI artifact.

### Option 2: Use a prebuilt Java CLI artifact

Available profiles:

| Profile | Artifact | Recommended usage |
| --- | --- | --- |
| `lite` | `markitdown4j-<version>-lite.jar` | Smallest package, no embedded `tess4j` |
| `full` | `markitdown4j-<version>-full.jar` | Full embedded OCR resources |
| `win32` | `markitdown4j-<version>-win32.jar` | 32-bit Windows |
| `win64` | `markitdown4j-<version>-win64.jar` | 64-bit Windows |
| `linux64` | `markitdown4j-<version>-linux64.jar` | Linux with external OCR |
| `mac` | `markitdown4j-<version>-mac.jar` | macOS with external OCR |

Build examples:

```bash
mvn package -DskipTests
mvn package -DskipTests -Pfull
mvn package -DskipTests -Pwin64
mvn package -DskipTests -Plinux64
```

### Example usage

```bash
java -jar target/markitdown4j-0.0.3-lite.jar test/basic.txt -o out/basic.md
java -jar target/markitdown4j-0.0.3-win64.jar test/with-text.png --ocr --ocr-engine tess4j -o out/ocr.md
java -jar target/markitdown4j-0.0.3-lite.jar test/with-text.png --ocr --ocr-engine paddleocr -o out/paddle.md
```

## OCR Strategy

The project is moving toward a remote-first OCR model with a unified configuration shape.

Current practical options:

- `tess4j` for Windows embedded OCR
- `tesseract-cli` for local offline OCR
- `paddleocr` for remote structured OCR
- `http` for custom remote OCR integrations

Example configuration:

```properties
ocr.enable=true
ocr.engine=paddleocr
ocr.endpoint=https://paddleocr.aistudio-app.com/api/v2/ocr/jobs
ocr.api.key=YOUR_TOKEN
ocr.model=PaddleOCR-VL-1.5
ocr.timeout=30000
ocr.poll.interval=5000
```

## Documentation

Public documentation kept in this repository:

- [Java CLI Guide](java/README.md)
- [Java CLI Guide (English)](java/README.en.md)
- [Command Reference](java/COMMAND_REFERENCE.md)
- [OCR Provider Roadmap](OCR_PROVIDER_ROADMAP.md)

## Subprojects

### Java CLI

The main delivery path. See:

- [中文说明](java/README.md)
- [English Guide](java/README.en.md)

### MCP Server

- [markitdown-mcp/README.md](markitdown-mcp/README.md)

### Web App

- [markitdown-web/readme.md](markitdown-web/readme.md)

## License

[MIT](LICENSE)
