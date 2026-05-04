# Configuration Design

## Goal

`markitdown` should move from scattered `properties` configuration toward a
YAML-first configuration model that is:

- easy for end users to understand
- suitable for remote OCR / VLM providers
- friendly to open-source distribution
- minimally dependent on system-level configuration

The core principle is:

**Prefer project-local configuration files over environment setup.**

That means users should normally be able to:

1. copy a template file
2. fill in endpoints / keys / model names
3. run the tool

without editing system PATH, global config files, or large sets of environment
variables.

## Design Principles

### 1. YAML-first

Use YAML as the primary configuration format because it supports:

- nested configuration blocks
- provider-specific sections
- clearer grouping than flat `properties`

Recommended file names:

- `markitdown.yml`
- `markitdown.local.yml`
- `markitdown.example.yml`

### 2. Configuration-file-first

Recommended runtime priority:

1. CLI arguments
2. `markitdown.local.yml`
3. `markitdown.yml`
4. built-in defaults
5. environment variables only for sensitive fallback values

Recommended user guidance:

- primary configuration method: YAML files
- CLI: temporary overrides
- environment variables: API keys / CI secrets / deployment secrets

### 3. Unified provider contract

Users should not learn a different configuration model for each OCR / VLM
provider.

Instead, all providers should map to a shared contract such as:

- `ocr.engine`
- `ocr.endpoint`
- `ocr.api_key`
- `ocr.model`
- `ocr.timeout`
- `ocr.poll_interval`

Provider-specific differences should stay internal.

### 4. Remote-first, not remote-only

The long-term direction is remote OCR / document parsing providers, but local
fallbacks still matter.

Recommended public provider categories:

- remote OCR providers
- remote document parsing / VLM providers
- local fallback providers

## Proposed File Layout

### `markitdown.example.yml`

Tracked in the repository.

Purpose:

- onboarding template
- documentation reference
- starting point for users

### `markitdown.yml`

Project-level runtime configuration.

Purpose:

- shared defaults
- non-sensitive project options
- common OCR / output behavior

### `markitdown.local.yml`

Local private override file.

Purpose:

- API keys
- local endpoints
- machine-specific settings

This file should be gitignored.

## Proposed YAML Structure

```yaml
app:
  profile: default

ocr:
  enabled: true
  engine: paddleocr
  endpoint: ""
  api_key: ""
  model: ""
  timeout: 30000
  poll_interval: 5000
  language: auto

content:
  include_metadata: true
  include_images: true
  include_tables: true
  page_break_mode: heading

output:
  dir: ./output
  image_dir: assets
  preserve_structure: false
  organize_by_type: false

format:
  image: markdown
  table: github

performance:
  parallel: false
  threads: 0
  optimize_memory: false
  max_file_size: 52428800
  batch_size: 20

providers:
  paddleocr:
    job_endpoint: ""
    result_format: markdown

  mistral_ocr:
    endpoint: ""
    model: ""

  azure_docintel:
    endpoint: ""
    api_version: ""
```

## Configuration Sections

### `app`

Global runtime profile and feature grouping.

Examples:

- `default`
- `ci`
- `ocr-heavy`
- `remote-first`

### `ocr`

Shared OCR configuration contract.

This block should be stable across providers.

Fields:

- `enabled`
- `engine`
- `endpoint`
- `api_key`
- `model`
- `timeout`
- `poll_interval`
- `language`

Supported public engines today:

- `tess4j`
- `tesseract-cli`
- `paddleocr`
- `http`

Planned remote engines:

- `mistral-ocr`
- `azure-docintel`
- `google-document-ai`
- `openai-vision`

### `content`

Controls Markdown output behavior.

Candidate fields:

- `include_metadata`
- `include_images`
- `include_tables`
- `page_break_mode`

Potential future additions:

- `normalize_titles`
- `merge_short_paragraphs`
- `preserve_line_breaks`
- `zip_recursion_depth`

### `output`

Controls output directory behavior and file organization.

Fields:

- `dir`
- `image_dir`
- `preserve_structure`
- `organize_by_type`

### `format`

Controls rendering style.

Fields:

- `image`
- `table`

Potential future additions:

- `heading_style`
- `list_style`
- `code_block_style`

### `performance`

Controls batching and processing limits.

Fields:

- `parallel`
- `threads`
- `optimize_memory`
- `max_file_size`
- `batch_size`

### `providers`

Optional provider-specific extensions.

This block should only be used when a provider needs extra fields beyond the
shared OCR contract.

Examples:

- `providers.paddleocr.job_endpoint`
- `providers.azure_docintel.api_version`
- `providers.openai_vision.response_schema`

## Provider Mapping Strategy

### Shared contract first

The runtime should first read the shared `ocr` block.

Then the selected provider may merge additional values from its own provider
block.

Example:

```yaml
ocr:
  engine: paddleocr
  api_key: ${PADDLE_OCR_TOKEN}
  model: PaddleOCR-VL-1.5

providers:
  paddleocr:
    job_endpoint: https://example.com/api/v2/ocr/jobs
```

### Internal mapping example

User-facing config:

```yaml
ocr:
  engine: azure-docintel
  endpoint: https://example.cognitiveservices.azure.com/
  api_key: xxx
  model: prebuilt-layout
  timeout: 30000
  poll_interval: 3000
```

Internal provider behavior:

- build Azure request URL
- send layout request
- poll operation status
- normalize the result into tool-owned Markdown / OCR result

## Sensitive Data Strategy

Recommended rule:

- non-sensitive runtime defaults go in `markitdown.yml`
- sensitive secrets go in `markitdown.local.yml`
- environment variables are optional fallback for secrets

Examples of secret fields:

- `ocr.api_key`
- provider tokens
- signed endpoint secrets

## Backward Compatibility Plan

The current project already uses `.markitdown.properties`.

Recommended migration path:

### Phase 1

- add YAML support
- prefer YAML in docs
- keep `.markitdown.properties` compatible

### Phase 2

- print a deprecation warning when `.markitdown.properties` is used
- keep CLI behavior unchanged

### Phase 3

- retire properties as the primary documented format
- keep compatibility only if maintenance cost remains low

## Recommended Implementation Plan

### Step 1

Add YAML parser support using Jackson YAML:

- `jackson-dataformat-yaml`

Reason:

- Jackson is already in use
- the mental model stays consistent

### Step 2

Introduce a dedicated YAML configuration loader, for example:

- `YamlConfigurationManager`

Responsibilities:

- load `markitdown.yml`
- merge `markitdown.local.yml`
- apply CLI overrides
- expose a unified normalized config object

### Step 3

Normalize configuration into `ConversionOptions`

Keep `ConversionOptions` as the runtime boundary object so that converters and
providers do not directly parse YAML files.

### Step 4

Provider-specific config handling

Each OCR / VLM provider should:

- consume shared fields first
- consume provider extension fields second
- hide protocol differences internally

## Example User Flows

### Local fallback OCR

```yaml
ocr:
  enabled: true
  engine: tesseract-cli
  language: eng
```

### Remote PaddleOCR

```yaml
ocr:
  enabled: true
  engine: paddleocr
  api_key: ""
  model: PaddleOCR-VL-1.5
  timeout: 30000
  poll_interval: 5000

providers:
  paddleocr:
    job_endpoint: https://paddleocr.aistudio-app.com/api/v2/ocr/jobs
```

### Future document parsing provider

```yaml
ocr:
  enabled: true
  engine: mistral-ocr
  endpoint: https://api.mistral.ai
  api_key: ""
  model: mistral-ocr-latest
```

## Project-Level Recommendation

The project should publicly move toward this message:

**markitdown is a configuration-driven document-to-Markdown platform with
pluggable remote OCR / VLM providers.**

That is stronger and more future-proof than describing it only as a fixed CLI
converter.
