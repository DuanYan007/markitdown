# MarkItDown Java CLI Command Reference

[Chinese Guide](README.md) | [English Guide](README.en.md)

## Basic Usage

```bash
java -jar target/markitdown4j-<version>-lite.jar <input> [options]
```

Examples:

```bash
java -jar target/markitdown4j-<version>-lite.jar document.pdf -o output.md
java -jar target/markitdown4j-<version>-lite.jar image.png --ocr --ocr-engine paddleocr -o result.md
java -jar target/markitdown4j-<version>-lite.jar docs --batch -o out/
```

## Core Options

| Option | Description |
| --- | --- |
| `-h`, `--help` | Show help |
| `-V`, `--version` | Show version |
| `-o`, `--output <path>` | Output file or directory |
| `-f`, `--format <markdown\|plain\|json>` | Output format |
| `-v`, `--verbose` | Verbose logs |
| `-q`, `--quiet` | Quiet mode |
| `-i`, `--interactive` | Interactive mode |

## Content Options

| Option | Description |
| --- | --- |
| `--include-metadata` | Include metadata |
| `--no-metadata` | Exclude metadata |
| `--include-images` | Include images |
| `--no-images` | Exclude images |
| `--include-tables` | Include tables |
| `--no-tables` | Exclude tables |
| `--image-format <markdown\|html\|base64>` | Image rendering format |
| `--table-format <github\|markdown\|pipe>` | Table rendering format |
| `--image-output-dir <dir>` | Output directory for extracted images |

## OCR Options

| Option | Description |
| --- | --- |
| `--ocr` | Enable OCR |
| `-l`, `--language <lang>` | OCR language, for example `auto`, `eng`, `chi_sim` |
| `--ocr-engine <engine>` | OCR backend: `tess4j`, `tesseract-cli`, `paddleocr`, `http` |
| `--ocr-endpoint <url>` | Remote OCR endpoint |
| `--ocr-api-key <key>` | Remote OCR API key or token |
| `--ocr-model <model>` | Remote OCR model name |
| `--ocr-timeout <ms>` | OCR timeout in milliseconds |
| `--ocr-poll-interval <ms>` | Polling interval for async OCR providers |

## PDF and File Options

| Option | Description |
| --- | --- |
| `--pdf-password <password>` | Password for encrypted PDF files |
| `--large-file` | Allow large file processing |
| `--max-file-size <bytes>` | Maximum allowed file size |
| `--temp-dir <dir>` | Temporary directory |
| `-m`, `--mime-type <type>` | MIME type for stdin / pipe input |

## Performance Options

| Option | Description |
| --- | --- |
| `-p`, `--parallel` | Enable parallel processing |
| `--threads <n>` | Number of worker threads |
| `--progress` | Show progress output |
| `--stats` | Show processing statistics |
| `--memory-limit <mb>` | Memory limit in MB |
| `--optimize-memory` | Enable memory optimization |

## Directory and Config Options

| Option | Description |
| --- | --- |
| `-r`, `--recursive` | Process subdirectories recursively |
| `--batch` | Batch process supported files in a directory |
| `--examples` | Show usage examples |
| `--generate-config` | Generate a default config file |
| `--config-path <path>` | Path to a config file |
| `--validate-config` | Validate a config file |
| `--show-config` | Print the active configuration |

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
