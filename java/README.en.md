# MarkItDown Java CLI

[Chinese](README.md) | English | [Back to repository root](../README.md)

`markitdown4j` is the main project in this repository. It is a Java command-line tool that converts common document formats into Markdown for AI preprocessing, knowledge-base workflows, batch archiving, and automation.

## Features

- Convert PDF, Word, Excel, PowerPoint, HTML, images, text, JSON, XML, CSV, and ZIP archives
- OCR support for scanned PDFs and images
- Pluggable OCR backends
- Platform-focused and lightweight build artifacts
- Batch, recursive, and parallel conversion

## Requirements

- Java 11+
- Maven 3.9+ when building from source

## Quick Start

### 1. Build

```bash
mvn package -DskipTests
```

### 2. Run

```bash
java -jar target/markitdown4j-0.0.3-lite.jar test/basic.txt -o out/basic.md
```

### 3. Help

```bash
java -jar target/markitdown4j-0.0.3-lite.jar --help
```

## Artifact Profiles

| Profile | Artifact | OCR strategy | Recommended usage |
| --- | --- | --- | --- |
| `lite` | `markitdown4j-<version>-lite.jar` | No embedded `tess4j` | Default download, CI, remote OCR |
| `full` | `markitdown4j-<version>-full.jar` | Full embedded `tess4j` | Windows all-in-one OCR |
| `win32` | `markitdown4j-<version>-win32.jar` | 32-bit Windows native only | 32-bit Windows |
| `win64` | `markitdown4j-<version>-win64.jar` | 64-bit Windows native only | 64-bit Windows |
| `linux64` | `markitdown4j-<version>-linux64.jar` | No embedded `tess4j` | Linux with external or remote OCR |
| `mac` | `markitdown4j-<version>-mac.jar` | No embedded `tess4j` | macOS with external or remote OCR |

Build examples:

```bash
mvn package -DskipTests
mvn package -DskipTests -Pfull
mvn package -DskipTests -Pwin32
mvn package -DskipTests -Pwin64
mvn package -DskipTests -Plinux64
mvn package -DskipTests -Pmac
```

## OCR Backends

Available OCR engines:

- `tess4j`
- `tesseract-cli`
- `paddleocr`
- `http`

Recommended pairings:

- Windows: `win64` or `full` + `--ocr-engine tess4j`
- Linux / macOS: `linux64` / `mac` + `--ocr-engine tesseract-cli`
- Remote structured OCR: `--ocr-engine paddleocr`
- Custom remote OCR: `--ocr-engine http`

### Unified OCR Configuration

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

### OCR Examples

```bash
# Windows embedded OCR
java -jar target/markitdown4j-0.0.3-win64.jar test/with-text.png --ocr --ocr-engine tess4j -o out/ocr.md

# Linux / macOS local OCR
java -jar target/markitdown4j-0.0.3-linux64.jar test/with-text.png --ocr --ocr-engine tesseract-cli -o out/ocr.md

# Remote PaddleOCR
java -jar target/markitdown4j-0.0.3-lite.jar test/with-text.png --ocr --ocr-engine paddleocr -o out/paddle.md
```

## Supported Formats

| Category | Formats |
| --- | --- |
| PDF | `.pdf` |
| Word | `.docx`, `.doc` |
| Excel | `.xlsx`, `.xls` |
| PowerPoint | `.pptx`, `.ppt` |
| HTML | `.html`, `.htm` |
| Images | `.png`, `.jpg`, `.jpeg`, `.gif`, `.bmp`, `.tiff` |
| Text | `.txt`, `.csv`, `.json`, `.xml` |
| Archives | `.zip` |
| Audio metadata | `.mp3`, `.wav`, `.flac` |

## Common Commands

```bash
# PDF to Markdown
java -jar target/markitdown4j-0.0.3-lite.jar test/plain-text.pdf -o out/plain-text.md

# Word to Markdown
java -jar target/markitdown4j-0.0.3-lite.jar test/basic.docx -o out/basic.md

# Batch processing
java -jar target/markitdown4j-0.0.3-lite.jar test --batch -o out/
```

## Documentation

- [Command Reference](COMMAND_REFERENCE.md)
- [OCR Provider Roadmap](../OCR_PROVIDER_ROADMAP.md)

## License

[MIT](../LICENSE)
