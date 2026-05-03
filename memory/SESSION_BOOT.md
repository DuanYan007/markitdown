# MarkItDown Session Boot

Read this first before editing the repo.

## What this repo is today

- Treat the Java CLI as the current primary workstream.
- Recent work was centered on:
  - OCR decoupling
  - Maven profile-based packaging
  - platform-specific release artifacts
  - reducing noisy logs
  - normalizing Markdown metadata labels

## Where to start in code

- Build and packaging:
  - [pom.xml](/O:/markitdown/pom.xml)
- CLI:
  - [MarkItDownCommand.java](/O:/markitdown/java/com/markitdown/cli/MarkItDownCommand.java)
  - [MarkItDownApplication.java](/O:/markitdown/java/com/markitdown/MarkItDownApplication.java)
- Engine and registry:
  - [MarkItDownEngine.java](/O:/markitdown/java/com/markitdown/core/MarkItDownEngine.java)
  - [ConverterRegistry.java](/O:/markitdown/java/com/markitdown/core/ConverterRegistry.java)
- OCR:
  - [OcrEngineFactory.java](/O:/markitdown/java/com/markitdown/ocr/OcrEngineFactory.java)
  - [TesseractOcrEngine.java](/O:/markitdown/java/com/markitdown/ocr/TesseractOcrEngine.java)
  - [TesseractCliOcrEngine.java](/O:/markitdown/java/com/markitdown/ocr/TesseractCliOcrEngine.java)
- Converters most affected by recent work:
  - [ImageConverter.java](/O:/markitdown/java/com/markitdown/converter/ImageConverter.java)
  - [PdfConverter.java](/O:/markitdown/java/com/markitdown/converter/PdfConverter.java)
  - [TextConverter.java](/O:/markitdown/java/com/markitdown/converter/TextConverter.java)
- Markdown formatting / metadata labels:
  - [MarkdownBuilder.java](/O:/markitdown/java/com/markdown/engine/MarkdownBuilder.java)
- Logging:
  - [logback.xml](/O:/markitdown/src/main/resources/logback.xml)

## Current packaging model

- Default build is `lite`.
- Key commands:
  - `mvn -DskipTests package`
  - `mvn -DskipTests -Pfull package`
  - `mvn -DskipTests -Pwin32 package`
  - `mvn -DskipTests -Pwin64 package`
  - `mvn -DskipTests -Plinux64 package`
  - `mvn -DskipTests -Pmac package`

## Current OCR model

- Supported engines:
  - `tess4j`
  - `tesseract-cli`
  - `mock`
  - `http`
- Recommended operational path:
  - Windows: `tess4j`
  - Linux/macOS: `tesseract-cli`
  - CI/no OCR: `lite` with OCR disabled

## Known environment gotchas

- Windows file locking around `target` can break rebuilds.
- If Maven says it cannot copy `src/main/resources/logback.xml` into `target/classes/logback.xml`, there is usually a lingering `java.exe` still holding the old file.
- If a built jar suddenly looks corrupt, check for:
  - a stale locked jar in `target`
  - a lingering Java process from a previous run

## Quick regression references

- [smoke-out/basic-lite-final.md](/O:/markitdown/smoke-out/basic-lite-final.md)
- [smoke-out/linux-ocr.md](/O:/markitdown/smoke-out/linux-ocr.md)
- [smoke-out/win64-ocr.md](/O:/markitdown/smoke-out/win64-ocr.md)
- [smoke-out/lite-ocr.md](/O:/markitdown/smoke-out/lite-ocr.md)

## If continuing the OCR/package line

- Prefer preserving the current "CLI-first" direction.
- Avoid re-coupling OCR directly into converters.
- Keep `lite` as the smallest/default package.
- Treat `http` OCR as optional, not the mainline recommendation.
