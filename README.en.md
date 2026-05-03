# MarkItDown

English | [简体中文](README.md)

Convert PDFs, Office documents, images, HTML, archives, and text files into Markdown for AI workflows, knowledge bases, and content pipelines.

## Repository Layout

This repository currently includes three subprojects:

1. `java/` - the main Java CLI and primary end-user deliverable
2. `markitdown-mcp/` - an MCP server
3. `markitdown-web/` - an older web application path

If you are new to the project, start with the Java CLI in [java/README.en.md](java/README.en.md).

## Features

- Convert PDF, Word, Excel, PowerPoint, HTML, images, audio metadata, text, JSON, XML, CSV, and ZIP archives to Markdown
- Extract text from scanned PDFs and images through pluggable OCR providers
- Support multiple OCR backends including `tess4j`, `tesseract-cli`, and remote providers such as `paddleocr`
- Produce smaller platform-focused artifacts with Maven profiles
- Fit local automation, batch conversion, and AI preprocessing pipelines

## Quick Start

### Build from source

```bash
mvn package -DskipTests
```

### Build a specific artifact

```bash
mvn package -DskipTests -Pfull
mvn package -DskipTests -Pwin64
mvn package -DskipTests -Plinux64
```

### Artifact profiles

| Profile | Artifact | Recommended usage |
| --- | --- | --- |
| `lite` | `markitdown4j-<version>-lite.jar` | Smallest package, no embedded `tess4j` |
| `full` | `markitdown4j-<version>-full.jar` | Full embedded OCR resources |
| `win32` | `markitdown4j-<version>-win32.jar` | 32-bit Windows |
| `win64` | `markitdown4j-<version>-win64.jar` | 64-bit Windows |
| `linux64` | `markitdown4j-<version>-linux64.jar` | Linux with external OCR |
| `mac` | `markitdown4j-<version>-mac.jar` | macOS with external OCR |

### Example usage

```bash
java -jar target/markitdown4j-1.0.0-SNAPSHOT-lite.jar test/basic.txt -o out/basic.md
java -jar target/markitdown4j-1.0.0-SNAPSHOT-win64.jar test/with-text.png --ocr --ocr-engine tess4j -o out/ocr.md
java -jar target/markitdown4j-1.0.0-SNAPSHOT-lite.jar test/with-text.png --ocr --ocr-engine paddleocr -o out/paddle.md
```

## OCR

The project is moving toward a remote-first OCR model with unified user-facing configuration.

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

Current practical OCR options:

- `tess4j`
- `tesseract-cli`
- `paddleocr`
- `http`

## Documentation

- [Java CLI Guide](java/README.en.md)
- [Java CLI Guide (Chinese)](java/README.md)
- [Command Reference](java/COMMAND_REFERENCE.md)
- [OCR Provider Roadmap](OCR_PROVIDER_ROADMAP.md)

## Subprojects

- [Java CLI](java/README.en.md)
- [MCP Server](markitdown-mcp/README.md)
- [Web App](markitdown-web/readme.md)

## License

[MIT](LICENSE)
