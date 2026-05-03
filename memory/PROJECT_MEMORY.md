# MarkItDown Project Memory

Last updated: 2026-05-02

## Project shape

- Monorepo with three visible subprojects:
  - `java/`: main Java CLI implementation, currently the primary delivery path.
  - `markitdown-mcp/`: Python MCP server.
  - `markitdown-web/`: older web app path, still present but not the main line for recent work.
- Top-level build for the Java CLI is driven from [pom.xml](/O:/markitdown/pom.xml), with Java sources under [java](/O:/markitdown/java).

## Current Java CLI architecture

- Entry points:
  - [MarkItDownApplication.java](/O:/markitdown/java/com/markitdown/MarkItDownApplication.java)
  - [MarkItDownCommand.java](/O:/markitdown/java/com/markitdown/cli/MarkItDownCommand.java)
- Core conversion flow:
  - [MarkItDownEngine.java](/O:/markitdown/java/com/markitdown/core/MarkItDownEngine.java)
  - [ConverterRegistry.java](/O:/markitdown/java/com/markitdown/core/ConverterRegistry.java)
  - [DocumentConverter.java](/O:/markitdown/java/com/markitdown/api/DocumentConverter.java)
- Markdown rendering:
  - [MarkdownBuilder.java](/O:/markitdown/java/com/markdown/engine/MarkdownBuilder.java)
- Config:
  - [ConversionOptions.java](/O:/markitdown/java/com/markitdown/config/ConversionOptions.java)
  - [ConfigurationManager.java](/O:/markitdown/java/com/markitdown/config/ConfigurationManager.java)

## OCR design after 2026-05 refactor

- OCR is no longer hard-wired to `tess4j` in the main flow.
- Main abstractions now live under [java/com/markitdown/ocr](/O:/markitdown/java/com/markitdown/ocr):
  - [OcrEngine.java](/O:/markitdown/java/com/markitdown/ocr/OcrEngine.java)
  - [OcrProvider.java](/O:/markitdown/java/com/markitdown/ocr/OcrProvider.java)
  - [OcrEngineFactory.java](/O:/markitdown/java/com/markitdown/ocr/OcrEngineFactory.java)
  - [UnavailableOcrEngine.java](/O:/markitdown/java/com/markitdown/ocr/UnavailableOcrEngine.java)
- Implemented providers:
  - [Tess4jOcrProvider.java](/O:/markitdown/java/com/markitdown/ocr/Tess4jOcrProvider.java)
  - [TesseractCliOcrProvider.java](/O:/markitdown/java/com/markitdown/ocr/TesseractCliOcrProvider.java)
  - [MockOcrProvider.java](/O:/markitdown/java/com/markitdown/ocr/MockOcrProvider.java)
  - [HttpOcrProvider.java](/O:/markitdown/java/com/markitdown/ocr/HttpOcrProvider.java)
- Implemented engines:
  - [TesseractOcrEngine.java](/O:/markitdown/java/com/markitdown/ocr/TesseractOcrEngine.java)
  - [TesseractCliOcrEngine.java](/O:/markitdown/java/com/markitdown/ocr/TesseractCliOcrEngine.java)
  - [HttpOcrEngine.java](/O:/markitdown/java/com/markitdown/ocr/HttpOcrEngine.java)
  - [MockOcrEngine.java](/O:/markitdown/java/com/markitdown/ocr/MockOcrEngine.java)
- Main flow callers that were decoupled from direct tess4j usage:
  - [ImageConverter.java](/O:/markitdown/java/com/markitdown/converter/ImageConverter.java)
  - [PdfConverter.java](/O:/markitdown/java/com/markitdown/converter/PdfConverter.java)

## Packaging strategy after 2026-05 refactor

- The Java CLI now ships as multiple Maven profile-driven artifacts:
  - `lite`
  - `full`
  - `win32`
  - `win64`
  - `linux64`
  - `mac`
- Artifact naming comes from `finalName` in [pom.xml](/O:/markitdown/pom.xml):
  - `markitdown4j-${project.version}-${build.profile}.jar`
- High-level intent:
  - `lite`: smallest package, no embedded tess4j native files.
  - `full`: all OCR resources included.
  - `win32` / `win64`: embedded Windows-only OCR native subsets.
  - `linux64` / `mac`: no embedded tess4j, expect `tesseract-cli`.
- Native filtering is implemented in the `maven-shade-plugin` filters via `shade.native.exclude.*` properties in [pom.xml](/O:/markitdown/pom.xml).

## Recommended artifact / OCR pairings

- Windows 64-bit:
  - `win64` or `full`
  - `--ocr-engine tess4j`
- Windows 32-bit:
  - `win32`
  - `--ocr-engine tess4j`
- Linux:
  - `linux64`
  - `--ocr-engine tesseract-cli`
- macOS:
  - `mac`
  - `--ocr-engine tesseract-cli`
- No OCR / CI / smallest size:
  - `lite`

## CLI OCR options now supported

- `--ocr-engine`
- `--ocr-endpoint`
- `--ocr-api-key`
- `--ocr-timeout`

Related config keys:

- `ocr.engine`
- `ocr.endpoint`
- `ocr.api.key`
- `ocr.timeout`

## Important codebase changes already made

- [MarkdownBuilder.java](/O:/markitdown/java/com/markdown/engine/MarkdownBuilder.java)
  - `static` shared render context was removed in favor of instance-level context.
  - metadata headings and labels were normalized to English-friendly output.
- [ConverterRegistry.java](/O:/markitdown/java/com/markitdown/core/ConverterRegistry.java)
  - priority semantics were clarified to match actual behavior.
- Added [src/main/resources/logback.xml](/O:/markitdown/src/main/resources/logback.xml)
  - this is the default logging configuration for quieter CLI output.
- Added `log4j-to-slf4j` bridge in [pom.xml](/O:/markitdown/pom.xml)
  - used to suppress log4j fallback noise from dependencies.

## Verified smoke outputs

- Smoke outputs were written under [smoke-out](/O:/markitdown/smoke-out).
- Useful reference files:
  - [basic-lite-final.md](/O:/markitdown/smoke-out/basic-lite-final.md)
  - [linux-ocr.md](/O:/markitdown/smoke-out/linux-ocr.md)
  - [win64-ocr.md](/O:/markitdown/smoke-out/win64-ocr.md)
  - [lite-ocr.md](/O:/markitdown/smoke-out/lite-ocr.md)
- These are handy for quick regression comparison after future converter or OCR changes.

## Documentation status

- Updated docs relevant to the current Java CLI direction:
  - [README.md](/O:/markitdown/README.md)
  - [RELEASE_TEMPLATE.md](/O:/markitdown/RELEASE_TEMPLATE.md)
  - [java/README.md](/O:/markitdown/java/README.md)
  - [java/INSTALLATION.md](/O:/markitdown/java/INSTALLATION.md)
  - [java/COMMAND_REFERENCE.md](/O:/markitdown/java/COMMAND_REFERENCE.md)
- Note: some older project docs and historical files still contain mojibake / encoding corruption and outdated wording. Do not assume every old document reflects the current Java CLI direction.

## Open technical debt

- Logging:
  - `logback.xml` was added and validated, but local Windows file locking around `target/classes/logback.xml` can block rebuilds.
  - If logs seem too noisy after a rebuild, verify whether the new `logback.xml` actually made it into the built jar.
- Encoding:
  - Current generated Markdown labels were normalized in core paths, but older docs still show garbled Chinese text.
- Tests:
  - Recent work was validated mainly with smoke tests and `-DskipTests` builds, not a full automated regression suite.
- OCR HTTP provider:
  - implemented as an optional extension path, but not the main CLI recommendation.

## Suggested next work items

- Make the quieter logging configuration reliably packaged on Windows without file-lock friction.
- Add focused automated tests for:
  - `OcrEngineFactory`
  - `tesseract-cli`
  - `lite/full/win64` packaging assumptions
  - OCR degradation behavior
- Consider cleaning or replacing old mojibake-heavy root docs if they are still user-facing.
