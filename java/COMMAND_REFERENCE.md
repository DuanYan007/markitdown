# markitdown4j Command Reference

## Basic Usage

```bash
java -jar target/markitdown4j-<version>-lite.jar <input> -o <output>
```

## Common Options

| Option | Description |
| --- | --- |
| `-o`, `--output` | Output Markdown file |
| `-r`, `--recursive` | Process directories recursively |
| `--batch` | Batch process multiple files |
| `--parallel` | Enable parallel conversion |
| `--show-config` | Print the active configuration |
| `--version` | Print CLI version |
| `--help` | Show help |

## OCR Options

| Option | Description |
| --- | --- |
| `--ocr` | Enable OCR |
| `--ocr-engine` | OCR backend: `tess4j`, `tesseract-cli`, `paddleocr`, `http` |
| `--ocr-endpoint` | Remote OCR endpoint |
| `--ocr-api-key` | Remote OCR API key or token |
| `--ocr-model` | Remote OCR model name |
| `--ocr-timeout` | OCR request timeout in milliseconds |
| `--ocr-poll-interval` | Poll interval for asynchronous OCR backends |
| `-l`, `--language` | OCR language |

## OCR Configuration Example

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

## Typical Commands

### PDF to Markdown

```bash
java -jar target/markitdown4j-<version>-lite.jar document.pdf -o output.md
```

### Windows OCR with embedded `tess4j`

```bash
java -jar target/markitdown4j-<version>-win64.jar image.png --ocr --ocr-engine tess4j -o result.md
```

### Linux or macOS OCR with `tesseract-cli`

```bash
java -jar target/markitdown4j-<version>-linux64.jar image.png --ocr --ocr-engine tesseract-cli -o result.md
```

### Remote PaddleOCR

```bash
java -jar target/markitdown4j-<version>-lite.jar image.png --ocr --ocr-engine paddleocr -o result.md
```

### Show Active Configuration

```bash
java -jar target/markitdown4j-<version>-lite.jar --show-config
```
