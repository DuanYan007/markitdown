# OCR Provider Roadmap

## Goal

`markitdown4j` should expose a unified remote OCR configuration model to users, while handling provider-specific request/response differences internally.

User-facing configuration should stay stable:

```properties
ocr.enable=true
ocr.engine=paddleocr
ocr.endpoint=
ocr.api.key=
ocr.model=
ocr.timeout=30000
ocr.poll.interval=5000
ocr.language=auto
```

The same shape should work for other providers such as:

```properties
ocr.engine=mistral-ocr
ocr.engine=azure-docintel
ocr.engine=google-document-ai
ocr.engine=openai-vision
ocr.engine=claude-pdf
ocr.engine=gemini-doc
ocr.engine=qwen-vl
```

## Provider Categories

### 1. Native Markdown Providers

These providers already return Markdown or something very close to Markdown.

- `paddleocr`
- `mistral-ocr`
- `azure-docintel`

Best fit:
- PDF to Markdown
- Image to Markdown
- Layout-aware parsing
- Tables, charts, formulas, mixed layouts

### 2. Structured JSON Providers

These providers return rich document structure that can be normalized into Markdown.

- `google-document-ai`
- `gemini-doc`

Best fit:
- Enterprise document pipelines
- Structured extraction
- Layout, entity, and table aware parsing

### 3. Prompted VLM Providers

These providers are not pure OCR systems, but can produce structured output via prompt + schema.

- `openai-vision`
- `claude-pdf`
- `gemini-vision`
- `qwen-vl`

Best fit:
- Complex screenshots
- Multimodal understanding
- OCR plus semantic interpretation
- Fallback for difficult documents

### 4. Self-hosted Remote Providers

These are useful for users who want remote invocation without depending on a public cloud service.

- `qwen-vl`
- `olmocr`
- local `paddleocr-http`
- local `easyocr-http`

Best fit:
- Private deployment
- Intranet usage
- Large batch processing
- Compliance-sensitive data

## Candidate Providers

### Short-term

#### `paddleocr`

Status:
- Implemented and real-service integration verified

Why it matters:
- Strong for Chinese and mixed-layout documents
- Supports Markdown-like structured output
- Supports async job workflow suitable for large files

Expected mapping:
- `ocr.endpoint` -> Paddle job endpoint
- `ocr.api.key` -> Bearer token
- `ocr.model` -> `PaddleOCR-VL-1.5` or similar
- `ocr.poll.interval` -> job polling interval

Recommended use:
- Scanned PDF
- Image-heavy document parsing
- Layout-aware OCR

#### `mistral-ocr`

Status:
- Recommended next provider

Why it matters:
- Official OCR/document processing capability
- Markdown-oriented output
- Strong candidate for general cloud OCR

Expected mapping:
- `ocr.endpoint` -> Mistral OCR endpoint
- `ocr.api.key` -> API key
- `ocr.model` -> OCR/document model name

Recommended use:
- PDF to Markdown
- Image to Markdown
- Table and layout extraction

#### `azure-docintel`

Status:
- Recommended next provider

Why it matters:
- Enterprise-friendly
- Official Markdown output support in layout mode
- Good for structured documents and tables

Expected mapping:
- `ocr.endpoint` -> Azure Document Intelligence endpoint
- `ocr.api.key` -> Azure key
- `ocr.model` -> layout/prebuilt model selection
- `ocr.poll.interval` -> operation polling interval

Recommended use:
- Enterprise document parsing
- Forms, tables, reports, contracts

### Mid-term

#### `google-document-ai`

Why it matters:
- Strong structured output
- Good for enterprise pipelines
- Rich page/block/table/entity hierarchy

Tradeoff:
- Usually needs a Markdown normalization layer after JSON output

#### `openai-vision`

Why it matters:
- Great fallback for hard documents
- Strong OCR plus semantic reasoning
- Useful for screenshots and mixed visual content

Tradeoff:
- Markdown output should be standardized by our own schema/prompt layer

#### `claude-pdf`

Why it matters:
- Strong PDF comprehension
- Good for charts, tables, and long documents

Tradeoff:
- Best treated as a structured remote VLM provider, not a traditional OCR engine

### Long-term

#### `gemini-doc`

Why it matters:
- Good PDF understanding
- Strong structured output potential
- Can be standardized via response schema

#### `qwen-vl`

Why it matters:
- Strong self-hosted option
- Good document parsing potential
- Attractive for Chinese users and private deployment

#### `olmocr`

Why it matters:
- Specifically aligned with document-to-Markdown workflows
- Good candidate for self-hosted remote deployment

## Unified Configuration Contract

Every remote provider should map to the same user-facing keys.

### Required base fields

- `ocr.enable`
- `ocr.engine`
- `ocr.endpoint`
- `ocr.api.key`
- `ocr.timeout`

### Optional standard fields

- `ocr.model`
- `ocr.poll.interval`
- `ocr.language`

### Provider-specific extension fields

Use `customOptions` only when a provider truly needs extra fields beyond the standard contract.

Examples:
- `ocr.region`
- `ocr.project`
- `ocr.version`
- `ocr.output.format`

## Unified Response Contract

Current `OcrEngine` returns a `String`, which works for plain OCR but is not sufficient long-term.

Recommended future response model:

```java
public class OcrResult {
    private String text;
    private String markdown;
    private boolean structured;
    private Map<String, Object> metadata;
}
```

Suggested semantics:
- `text`: plain OCR text fallback
- `markdown`: provider-generated or normalized Markdown
- `structured`: whether the provider returned layout-aware structured output
- `metadata`: provider-specific structured data such as tables, page count, layout blocks, confidence

## Recommended Implementation Order

### Phase 1

- Keep `paddleocr` as the reference remote provider
- Finish docs and examples for `paddleocr`
- Standardize remote OCR configuration fields

### Phase 2

- Add `mistral-ocr`
- Add `azure-docintel`
- Keep `http` as a generic simple text provider

### Phase 3

- Add `google-document-ai`
- Add `openai-vision`
- Add `claude-pdf`

### Phase 4

- Add self-hosted remote VLM providers
- Add `qwen-vl`
- Add `olmocr`

## Decision Principles

- Prefer remote invocation over local native dependency
- Keep user configuration uniform
- Keep provider behavior internal
- Normalize all provider outputs into a stable project-owned result model
- Treat OCR as part of a broader document parsing capability, not just text extraction

## Notes

- `tesseract-cli` should remain as a fallback provider
- Remote-first does not mean remote-only; offline fallback still matters
- Providers that already return Markdown should be prioritized because they align well with the project goal
