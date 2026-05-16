# Configuration

`markitdown4j` in this document is a shorthand for `java -jar target/markitdown4j-<version>.jar`. The repository currently ships the CLI as a JAR, so run the full `java -jar ...` form directly unless you created your own wrapper or alias.

## What Configuration Is For

The configuration file is the stable place to define how this application should convert files by default:

- where output files should go
- whether OCR is enabled
- which OCR engine should be used
- how images and tables should be rendered
- file-size and temporary-directory limits

If you only need one-off changes, use CLI flags. CLI flags override configuration values for that command.

## Choose A Configuration Approach

Use this table first:

| Scenario | Recommended OCR setup | What you usually need to set |
| --- | --- | --- |
| Convert office, PDF, HTML, text, and archive files without OCR | `ocr.enabled: false` | Usually only `output.dir` |
| OCR with local Tesseract on Windows, macOS, or Linux | `ocr.enabled: true`, `ocr.engine: tesseract-cli` | `tesseract.path` only when `tesseract` is not already on `PATH`; `tessdata.path` only when language data is not auto-discovered |
| OCR through your own HTTP service | `ocr.enabled: true`, `ocr.engine: http` | `ocr.endpoint`, optionally `ocr.api_key`, `ocr.timeout` |
| OCR through PaddleOCR cloud job API | `ocr.enabled: true`, `ocr.engine: paddleocr` | `ocr.api_key`, optionally `ocr.endpoint`, `ocr.model`, `ocr.timeout`, `ocr.poll_interval` |
| Large local files | any OCR mode | `performance.max_file_size` or `files.large_file: true`, optionally `output.temp_dir` |

If you do not need OCR, keep it disabled. That is the simplest and most predictable setup.

## Configuration Files

The current application recognizes these YAML files in the working directory:

- `markitdown.yml`
- `markitdown.local.yml`
- `markitdown.example.yml`

Recommended usage:

- `markitdown.yml`: shared project defaults
- `markitdown.local.yml`: machine-specific overrides such as local OCR paths or secrets
- `markitdown.example.yml`: reference template only

## Resolution Order

Without `--config-path`, values are resolved in this order:

1. built-in defaults
2. `markitdown.yml`
3. `markitdown.local.yml`
4. CLI arguments

With `--config-path`, the explicit YAML file replaces `markitdown.yml` and `markitdown.local.yml`, and CLI arguments still win.

That means:

- a value in `markitdown.local.yml` overrides the same key in `markitdown.yml`
- a CLI flag overrides both YAML files for the current command only
- `--show-config` shows the final effective result after resolution

## Start Here

Generate a starter file:

```bash
markitdown4j --generate-config
```

Validate a file:

```bash
markitdown4j --validate-config
```

Inspect the effective configuration and value sources:

```bash
markitdown4j --show-config
```

## Starter Configurations

### 1. Minimal Local Conversion Without OCR

Use this if you mainly convert PDFs, Office files, HTML, text, CSV, JSON, XML, ZIP, or already-textual content.

```yaml
output:
  dir: ./output

ocr:
  enabled: false
```

### 2. Local Tesseract OCR

Use this when OCR should run through a local Tesseract executable.

```yaml
output:
  dir: ./output

ocr:
  enabled: true
  engine: tesseract-cli
  language: chi_sim

tesseract:
  path: O:/tesserOCR/tesseract.exe

tessdata:
  path: O:/tesserOCR/tessdata
```

Practical notes:

- If `tesseract` is already available on `PATH`, leave `tesseract.path` empty.
- If Tesseract already finds its language data correctly, leave `tessdata.path` empty.
- If `tesseract.path` points to a directory instead of a file, the runtime resolves `tesseract.exe` on Windows or `tesseract` on non-Windows inside that directory.

### 3. HTTP OCR Service

Use this when OCR should be delegated to your own HTTP endpoint.

```yaml
ocr:
  enabled: true
  engine: http
  endpoint: https://example.com/ocr
  api_key: your-token-if-needed
  language: eng
  timeout: 30000
```

Practical notes:

- `ocr.endpoint` is required for `http`.
- `ocr.api_key` is optional and is sent as `Authorization: Bearer <token>`.
- The current HTTP OCR client sends JSON with `imageBase64`, `fileName`, and `language`.

### 4. PaddleOCR Cloud

Use this when OCR should run through the PaddleOCR job API.

```yaml
ocr:
  enabled: true
  engine: paddleocr
  api_key: your-paddleocr-token
  language: auto
  timeout: 30000
  poll_interval: 5000
```

Practical notes:

- `ocr.api_key` is required for `paddleocr`.
- If `ocr.endpoint` is empty, the current default is `https://paddleocr.aistudio-app.com/api/v2/ocr/jobs`.
- If `ocr.model` is empty, the current default is `PaddleOCR-VL-1.5`.
- The runtime clamps PaddleOCR timeout and poll interval to at least `1000` ms.

### 5. Large Files

Use this when you regularly process files above 50 MB.

```yaml
performance:
  max_file_size: 209715200

files:
  large_file: false
```

Or remove the size limit entirely:

```yaml
files:
  large_file: true
```

Practical notes:

- `performance.max_file_size` is in bytes.
- `files.large_file: true` effectively makes the max file size unlimited in the current runtime by forcing the effective limit to `0`.
- For large remote downloads or temporary extraction work, also set `output.temp_dir` if the default system temp location is not suitable.

## Effective Vs CLI-Only Settings In The Current Version

Most users need this distinction.

These configuration keys directly affect conversion behavior in the current version:

- `tesseract.path`
- `tessdata.path`
- `output.dir`
- `output.image_dir`
- `output.temp_dir`
- `content.include_metadata`
- `content.include_images`
- `content.include_tables`
- `content.page_break_mode`
- `ocr.*`
- `format.image`
- `format.table`
- `performance.max_file_size`
- `files.large_file`

These keys are valid and appear in `--show-config`, but current conversion flow is still primarily triggered by CLI flags rather than YAML alone:

- `output.organize_by_type`
- `output.preserve_structure`
- `performance.parallel`
- `performance.threads`
- `performance.optimize_memory`
- `performance.batch_size`
- `ui.verbose`
- `ui.quiet`
- `ui.progress`
- `ui.stats`
- `files.recursive`
- `files.batch`

For those behaviors, use CLI flags such as `--parallel`, `--threads`, `--progress`, `--stats`, `--recursive`, and `--batch` when running a command.

## Complete Setting Reference

### Engine Paths

| Key | Default | Allowed values | What it controls | When to change it |
| --- | --- | --- | --- | --- |
| `tesseract.path` | `""` | file path or directory path | Tesseract executable resolution | Set it when `tesseract` is not available on `PATH` or when multiple installations exist |
| `tessdata.path` | `""` | directory path | Tesseract language data directory | Set it when OCR language packs are not discovered automatically |

### Output

| Key | Default | Allowed values | What it controls | When to change it |
| --- | --- | --- | --- | --- |
| `output.dir` | `./output` | path | Default output file or directory target when `-o/--output` is not provided | Change it if you want a stable output location |
| `output.image_dir` | `assets` | directory name or relative path | Where extracted images are written relative to the output file | Change it if you want image assets stored elsewhere |
| `output.temp_dir` | system temp directory | path | Temporary directory for remote downloads and temp processing | Change it for disk-space control, permission control, or predictable cleanup |
| `output.organize_by_type` | `false` | `true` or `false` | Recorded output organization preference | Keep default unless you are aligning config state; current conversion flow does not use it directly |
| `output.preserve_structure` | `false` | `true` or `false` | Recorded source-structure preservation preference | Keep default unless you are aligning config state; current conversion flow does not use it directly |

### Content

| Key | Default | Allowed values | What it controls | When to change it |
| --- | --- | --- | --- | --- |
| `content.include_metadata` | `true` | `true` or `false` | Whether metadata is included in Markdown output | Disable it if you want cleaner content-only output |
| `content.include_images` | `true` | `true` or `false` | Whether images are referenced or embedded according to the image format | Disable it when image extraction is not useful |
| `content.include_tables` | `true` | `true` or `false` | Whether tables are kept in the output | Disable it when you prefer flat text |
| `content.page_break_mode` | `heading` | `heading`, `rule`, `none` | How page boundaries are represented in Markdown | Change it when page separation matters for downstream reading or parsing |

Page break modes:

- `heading`: emit page headings
- `rule`: emit horizontal rules
- `none`: emit no explicit page-break marker

### OCR

| Key | Default | Allowed values | What it controls | When to change it |
| --- | --- | --- | --- | --- |
| `ocr.enabled` | `false` | `true` or `false` | Turns OCR on or off | Enable it only when image-based text extraction is needed |
| `ocr.engine` | `tesseract-cli` | `tesseract-cli`, `paddleocr`, `http` | OCR backend | Choose the engine that matches your deployment model |
| `ocr.language` | `auto` | `auto`, `eng`, `chi_sim`, `chi_tra`, `jpn`, `kor`, `fra`, `deu` | OCR language hint | Set an explicit value when you need deterministic OCR behavior |
| `ocr.endpoint` | `""` | URL string | Remote OCR endpoint | Required for `http`; optional override for `paddleocr` |
| `ocr.api_key` | `""` | string | Bearer token or provider token | Set it when the OCR provider requires authentication |
| `ocr.model` | `""` | string | OCR model identifier | Set it only when the provider supports model selection |
| `ocr.timeout` | `30000` | integer milliseconds | OCR request timeout | Increase it for slow remote OCR or large pages |
| `ocr.poll_interval` | `5000` | integer milliseconds | Polling interval for async OCR providers | Adjust it mainly for `paddleocr` |

OCR selection guidance:

- `tesseract-cli`: best for local, offline, cross-platform deployments where Tesseract is installed.
- `http`: best when your team already owns an OCR service.
- `paddleocr`: best when you want a hosted OCR job workflow and have the provider token.

### Format

| Key | Default | Allowed values | What it controls | When to change it |
| --- | --- | --- | --- | --- |
| `format.image` | `markdown` | `markdown`, `html`, `base64` | How images are represented in output | Use `html` or `base64` only if your downstream consumer requires it |
| `format.table` | `github` | `github`, `markdown`, `pipe` | Markdown table style | Change it to match your rendering target |

Image formats:

- `markdown`: standard Markdown image references
- `html`: HTML `<img>` output
- `base64`: inline embedded image payloads

### Performance

| Key | Default | Allowed values | What it controls | When to change it |
| --- | --- | --- | --- | --- |
| `performance.parallel` | `false` | `true` or `false` | Recorded parallel-processing preference | Use CLI `--parallel` for actual multi-file parallel execution |
| `performance.threads` | `0` | integer, `0` means automatic | Desired thread count | Use CLI `--threads` for actual thread selection |
| `performance.optimize_memory` | `false` | `true` or `false` | Recorded memory-optimization preference | Use CLI `--optimize-memory` for current runtime behavior |
| `performance.max_file_size` | `52428800` | long integer bytes | Maximum allowed input size | Raise it when processing large files |
| `performance.batch_size` | `20` | integer | Recorded batch-size preference | Keep default; current conversion flow does not directly consume this YAML key |

### UI

| Key | Default | Allowed values | What it controls | When to change it |
| --- | --- | --- | --- | --- |
| `ui.verbose` | `false` | `true` or `false` | Recorded verbose preference | Use CLI `--verbose` for current command output |
| `ui.quiet` | `false` | `true` or `false` | Recorded quiet preference | Use CLI `--quiet` for current command output |
| `ui.progress` | `false` | `true` or `false` | Recorded progress-display preference | Use CLI `--progress` for current command output |
| `ui.stats` | `false` | `true` or `false` | Recorded statistics-display preference | Use CLI `--stats` for current command output |

### File Discovery

| Key | Default | Allowed values | What it controls | When to change it |
| --- | --- | --- | --- | --- |
| `files.recursive` | `false` | `true` or `false` | Recorded recursive-discovery preference | Use CLI `--recursive` for current directory traversal |
| `files.batch` | `false` | `true` or `false` | Recorded batch-discovery preference | Use CLI `--batch` for current directory processing |
| `files.large_file` | `false` | `true` or `false` | Removes file-size limit in effective config | Enable it when max file size should become unlimited |

### Provider Extensions

`providers.*` is reserved extension space. Keys under that section are accepted by configuration validation and ignored by the built-in key validator.

Use it only when a specific provider integration in your own environment expects extra settings. The built-in OCR engines in the current application do not require `providers.*` keys.

## Validation Rules

The current validator enforces all of the following:

- the file must exist when referenced explicitly
- only `.yml` and `.yaml` files are accepted
- unknown top-level sections are rejected
- unknown keys are rejected, except under `providers.*`
- boolean fields must be valid booleans
- integer fields must be valid integers
- `performance.max_file_size` must be a valid long integer
- enum fields must use one of the allowed values shown above

Path validation:

- `tesseract.path` must exist and must point to an executable file when it is set
- `tessdata.path` must exist and must be a directory when it is set

Semantic validation:

- `ocr.endpoint` is required when `ocr.enabled=true` and `ocr.engine=http`
- `ui.verbose` and `ui.quiet` cannot both be `true`
- `ui.quiet` and `ui.progress` cannot both be `true`

## Configuration And CLI Overrides

Use configuration for stable defaults. Use CLI flags for per-command changes.

Examples:

```bash
markitdown4j report.pdf --ocr
markitdown4j report.pdf --ocr-engine http --ocr-endpoint https://example.com/ocr
markitdown4j report.pdf --image-format html --table-format pipe
markitdown4j large.pdf --large-file --temp-dir D:/markitdown-temp
```

Override behavior in the current CLI:

- `--ocr` enables OCR for that command even if YAML disables it
- `--language`, `--ocr-engine`, `--ocr-endpoint`, `--ocr-api-key`, `--ocr-model`, `--ocr-timeout`, and `--ocr-poll-interval` override the corresponding `ocr.*` keys
- `--include-images`, `--no-images`, `--include-tables`, `--no-tables`, `--include-metadata`, and `--no-metadata` override `content.*`
- `--image-format`, `--table-format`, `--image-output-dir`, `--temp-dir`, `--max-file-size`, and `--large-file` override the corresponding runtime-effective configuration keys
- `--large-file` also forces the effective maximum file size to `0`

## Recommended Workflow For New Users

1. Start with `markitdown4j --generate-config`.
2. Keep OCR disabled unless you know you need it.
3. Set `output.dir` first so output is predictable.
4. If OCR is needed, choose exactly one engine and configure only that engine's required fields.
5. Run `markitdown4j --validate-config`.
6. Run `markitdown4j --show-config` to confirm the final values and their sources.
7. Run real conversions and use CLI flags only for one-off overrides.

## Reference Template

- [markitdown.example.yml](markitdown.example.yml)

