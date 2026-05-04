# markitdown

[中文](README.md) | [English](README.en.md)

`markitdown` is a document-to-Markdown repository. The primary deliverable today is the `markitdown4j` Java CLI, which converts common office documents, web pages, images, archives, and some audio metadata into Markdown, with OCR support through a unified configuration model.

## What this project can do

- Convert PDF, Word, Excel, PowerPoint, HTML, images, text, and ZIP files into Markdown
- Use OCR for images and scanned PDFs
- Provide platform-focused artifacts: `lite`, `full`, `win32`, `win64`, `linux64`, `mac`
- Let users switch OCR backends without changing the conversion flow
- Support remote OCR providers such as `paddleocr`

## Repository layout

- [java/README.en.md](java/README.en.md): Java CLI guide
- [java/COMMAND_REFERENCE.md](java/COMMAND_REFERENCE.md): command and parameter reference
- [test/README.md](test/README.md): test dataset and validation notes
- [OCR_PROVIDER_ROADMAP.md](OCR_PROVIDER_ROADMAP.md): OCR / VLM roadmap
- [memory/PROJECT_MEMORY.md](memory/PROJECT_MEMORY.md): project memory

## Quick start

1. Install Java 11 or later
2. Download the artifact that matches your platform
3. Run a conversion command

Example:

```bash
java -jar target/markitdown4j-0.0.3-lite.jar document.pdf -o output.md
```

## Which artifact should I download?

- `win64`: 64-bit Windows with embedded Windows OCR natives
- `win32`: 32-bit Windows with embedded Windows OCR natives
- `linux64`: Linux, recommended with local or remote OCR
- `mac`: macOS, recommended with local or remote OCR
- `lite`: smallest package, no embedded `tess4j`
- `full`: full package with complete OCR resources

## OCR Configuration

The project uses a unified OCR configuration model. Users do not need to learn a different config shape for every OCR backend; they only switch values in the same set of fields.

Example:

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

- [`.markitdown.properties`](.markitdown.properties)

Shared OCR fields:

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
3. [`.markitdown.properties`](.markitdown.properties)
4. Built-in defaults

Common environment variables:

- `MARKITDOWN_OCR_ENGINE`
- `MARKITDOWN_OCR_ENDPOINT`
- `MARKITDOWN_OCR_API_KEY`
- `MARKITDOWN_OCR_MODEL`
- `MARKITDOWN_OCR_TIMEOUT`
- `MARKITDOWN_OCR_POLL_INTERVAL`

Public OCR backends:

- `tess4j`: embedded OCR for Windows
- `tesseract-cli`: local OCR for Linux / macOS
- `paddleocr`: remote structured OCR
- `http`: custom remote OCR integrations

## Testing and Validation

This project does not only describe features; it also ships reusable test assets and documented validation paths.

### Automated tests

Run:

```bash
mvn test
```

Current coverage includes:

- Profile build and naming checks
- OCR engine factory selection
- PaddleOCR response parsing
- Streaming text conversion
- ZIP delegation and nested conversion behavior

### Test dataset

[test/test.zip](test/test.zip) is the packaged test dataset for this repository. It currently contains about 104 test files and is used for:

- regression testing
- compatibility checks
- pre-release manual validation

The extracted [test/README.md](test/README.md) explains how to use the dataset.

### Verified paths

- `lite` basic conversion
- `win64 + tess4j`
- `linux64 + tesseract-cli`
- `lite + paddleocr`

## Documentation

- [Java CLI Guide (CN)](java/README.md)
- [Java CLI Guide (EN)](java/README.en.md)
- [Command Reference](java/COMMAND_REFERENCE.md)
- [Test Guide](test/README.md)
- [OCR Roadmap](OCR_PROVIDER_ROADMAP.md)

## Other subprojects

- `markitdown-mcp`: MCP-related content
- `markitdown-web`: historical web-direction experiment, not the primary delivery path

## License

See the repository license file or future release notes for the active license.
