# MarkItDown

English | [Chinese](README.md)

Convert PDFs, Office documents, images, HTML, archives, and text files into Markdown for AI preprocessing, knowledge bases, batch conversion, and automation pipelines.

## Repository Layout

This repository currently contains three main subprojects:

1. `java/` - the main Java CLI and the primary end-user deliverable
2. `markitdown-mcp/` - an MCP server integration project
3. `markitdown-web/` - an older web application path

If you are new to the project, start with the Java CLI:

- [Java CLI Guide (Chinese)](java/README.md)
- [Java CLI Guide (English)](java/README.en.md)

## What It Can Do

- Convert PDF, Word, Excel, PowerPoint, HTML, images, text, JSON, XML, CSV, and ZIP archives to Markdown
- Extract text from scanned PDFs and images through OCR
- Support multiple OCR backends:
  - `tess4j`
  - `tesseract-cli`
  - `paddleocr`
  - `http`
- Produce platform-specific artifacts with Maven profiles
- Fit local automation, batch processing, and AI document preparation workflows

## Quick Start

### Build from source

```bash
mvn package -DskipTests
```

The default output is the lightweight Java CLI artifact.

### Build a specific artifact

```bash
mvn package -DskipTests -Pfull
mvn package -DskipTests -Pwin32
mvn package -DskipTests -Pwin64
mvn package -DskipTests -Plinux64
mvn package -DskipTests -Pmac
```

### Artifact profiles

| Profile | Artifact | Recommended usage |
| --- | --- | --- |
| `lite` | `markitdown4j-<version>-lite.jar` | Smallest package, no embedded `tess4j` |
| `full` | `markitdown4j-<version>-full.jar` | Full OCR resources |
| `win32` | `markitdown4j-<version>-win32.jar` | 32-bit Windows |
| `win64` | `markitdown4j-<version>-win64.jar` | 64-bit Windows |
| `linux64` | `markitdown4j-<version>-linux64.jar` | Linux with external or remote OCR |
| `mac` | `markitdown4j-<version>-mac.jar` | macOS with external or remote OCR |

### Example usage

```bash
java -jar target/markitdown4j-0.0.3-lite.jar test/basic.txt -o out/basic.md
java -jar target/markitdown4j-0.0.3-win64.jar test/with-text.png --ocr --ocr-engine tess4j -o out/ocr.md
java -jar target/markitdown4j-0.0.3-lite.jar test/with-text.png --ocr --ocr-engine paddleocr -o out/paddle.md
```

## OCR Configuration

The project uses a unified OCR configuration model so users can switch providers without changing the conversion flow.

Example:

```properties
ocr.enable=true
ocr.engine=paddleocr
ocr.endpoint=https://paddleocr.aistudio-app.com/api/v2/ocr/jobs
ocr.api.key=YOUR_TOKEN
ocr.model=PaddleOCR-VL-1.5
ocr.timeout=30000
ocr.poll.interval=5000
```

Current practical options:

- `tess4j` for embedded Windows OCR
- `tesseract-cli` for local Linux/macOS OCR
- `paddleocr` for remote structured OCR
- `http` for custom remote OCR integrations

## Documentation

- [Java CLI Guide (Chinese)](java/README.md)
- [Java CLI Guide (English)](java/README.en.md)
- [Command Reference](java/COMMAND_REFERENCE.md)
- [OCR Provider Roadmap](OCR_PROVIDER_ROADMAP.md)

## Subprojects

- [Java CLI](java/README.en.md)
- [MCP Server](markitdown-mcp/README.md)
- [Web App](markitdown-web/readme.md)

## License

[MIT](LICENSE)
