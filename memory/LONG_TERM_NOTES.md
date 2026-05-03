# MarkItDown Long-Term Notes

Last updated: 2026-05-02

## Product direction

- Primary recommendation is the Java CLI.
- Keep packaging and OCR choices aligned with a CLI-first distribution model.
- Do not accidentally let older web/MCP docs redefine the current Java packaging strategy.

## Decisions already made

- OCR is provider-based, not hard-wired to `tess4j`.
- `lite` remains the default artifact.
- `linux64` and `mac` intentionally prefer `tesseract-cli` over embedded tess4j native payloads.
- `win32` and `win64` are trimmed Windows OCR builds rather than clones of `full`.
- Output metadata labels were intentionally normalized to English in the current core Markdown paths to avoid mojibake and improve consistency.

## Places future edits can easily regress

- [pom.xml](/O:/markitdown/pom.xml)
  - profile wiring
  - shade filtering
  - logging bridge dependencies
- [MarkdownBuilder.java](/O:/markitdown/java/com/markdown/engine/MarkdownBuilder.java)
  - metadata label normalization
  - document title resolution
- [OcrEngineFactory.java](/O:/markitdown/java/com/markitdown/ocr/OcrEngineFactory.java)
  - provider registration and fallback behavior
- [ImageConverter.java](/O:/markitdown/java/com/markitdown/converter/ImageConverter.java)
  - easy place to accidentally reintroduce direct tess4j coupling
- [PdfConverter.java](/O:/markitdown/java/com/markitdown/converter/PdfConverter.java)
  - OCR fallback path and messaging

## Verification habits that worked well

- Use small smoke conversions under [smoke-out](/O:/markitdown/smoke-out) after touching:
  - OCR
  - packaging
  - metadata formatting
  - logging
- Good smoke checks:
  - basic txt conversion
  - image OCR with `win64 + tess4j`
  - image OCR with `linux64/mac + tesseract-cli`
  - `lite + tess4j` degradation message

## Practical warnings

- Be careful with stale jars in `target`; if a jar is corrupted once, future shade runs may keep failing until it is removed.
- On Windows, a previous `java -jar ...` invocation can hold file locks longer than expected.
- Some repository documentation is still garbled because of historical encoding issues; prefer current code and the recently updated docs over old prose.

## Good next investments

- Add explicit tests for packaging assumptions instead of only smoke checks.
- Add OCR provider tests that do not require native OCR availability.
- Clean up the remaining encoding-damaged documents in a controlled pass.
