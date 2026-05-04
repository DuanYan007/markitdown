# markitdown4j

[中文](README.md) | [English](README.en.md)

`markitdown4j` is the primary deliverable in this repository. It is a Java CLI that converts common documents and file content into Markdown, and calls different OCR backends through a unified configuration model when OCR is needed.

## Features

- Multi-format Markdown conversion
- Pluggable OCR backends
- Platform-focused build artifacts
- Batch, recursive, and parallel processing
- Unified configuration and CLI entry points

## Requirements

- Java 11+
- Maven 3.8+

## Quick start

Build:

```bash
mvn package -DskipTests
```

Minimal example:

```bash
java -jar target/markitdown4j-0.0.3-lite.jar document.pdf -o output.md
```

## Release artifacts

- `lite`
- `full`
- `win32`
- `win64`
- `linux64`
- `mac`

Recommended choices:

- Windows 64-bit: `win64`
- Windows 32-bit: `win32`
- Linux: `linux64`
- macOS: `mac`
- Smallest package: `lite`
- Full OCR payload: `full`

## OCR Backends

Public OCR engines:

- `tess4j`
- `tesseract-cli`
- `paddleocr`
- `http`

Recommended use:

- `tess4j`: embedded OCR on Windows
- `tesseract-cli`: local OCR on Linux / macOS
- `paddleocr`: remote structured OCR
- `http`: custom remote OCR services

## Unified OCR Configuration

All OCR backends use the same fields, so users do not need a different configuration model for each provider.

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

Configuration file:

- [`../.markitdown.properties`](../.markitdown.properties)

Shared fields:

- `ocr.enable`
- `ocr.engine`
- `ocr.endpoint`
- `ocr.api.key`
- `ocr.model`
- `ocr.timeout`
- `ocr.poll.interval`
- `ocr.language`

Configuration precedence:

1. CLI arguments such as `--ocr-engine`
2. Environment variables such as `MARKITDOWN_OCR_ENGINE`
3. [`../.markitdown.properties`](../.markitdown.properties)
4. Built-in defaults

Common environment variables:

- `MARKITDOWN_OCR_ENGINE`
- `MARKITDOWN_OCR_ENDPOINT`
- `MARKITDOWN_OCR_API_KEY`
- `MARKITDOWN_OCR_MODEL`
- `MARKITDOWN_OCR_TIMEOUT`
- `MARKITDOWN_OCR_POLL_INTERVAL`

## OCR Examples

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

## Testing

Automated tests:

```bash
mvn test
```

Current automated coverage:

- Profile build and naming checks
- OCR engine factory selection
- PaddleOCR response parsing
- Streaming text conversion
- ZIP delegation and nested conversion behavior

Test assets:

- [`../test/test.zip`](../test/test.zip) is the packaged test dataset
- It currently contains about 104 test files
- It is used for regression, compatibility, and pre-release manual validation

Verified paths:

- `lite` basic conversion
- `win64 + tess4j`
- `linux64 + tesseract-cli`
- `lite + paddleocr`

For more details, see [../test/README.md](../test/README.md).

## Related Documents

- [Command Reference](COMMAND_REFERENCE.md)
- [Test Guide](../test/README.md)
- [OCR Roadmap](../OCR_PROVIDER_ROADMAP.md)
