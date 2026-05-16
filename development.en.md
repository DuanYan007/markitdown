# Development

## Purpose

This document describes the current software design and development structure of `markitdown4j`.

It covers only the current implementation:

- system purpose
- build and runtime baseline
- code layout
- core module responsibilities
- CLI and library execution flow
- configuration system
- converter system
- OCR system
- testing, CI, and release validation

## System Overview

`markitdown4j` currently provides two runtime entry points:

- the CLI
- the Java library

The system converts multiple document inputs into Markdown and can use OCR when additional text extraction is required.

Current primary input types include:

- PDF
- DOCX
- XLSX
- HTML
- TXT
- Markdown
- CSV
- JSON
- XML
- images
- ZIP archives

Current OCR engines include:

- `tesseract-cli`
- `http`
- `paddleocr`

## Build And Runtime Baseline

Baseline requirements:

- Java 11 compilation target
- Maven build
- an executable shaded jar as the main artifact

Current Maven coordinates:

```xml
<groupId>com.markitdown</groupId>
<artifactId>markitdown4j</artifactId>
<version>0.0.4</version>
```

Key build configuration lives in [pom.xml](pom.xml):

- `maven.compiler.release=11`
- `maven-shade-plugin` packages the CLI jar
- `maven-surefire-plugin` runs tests
- `jacoco-maven-plugin` generates coverage reports

Current compiler gate:

- `-Xlint:deprecation`
- `-Xlint:unchecked`
- `-Werror`

## Repository Layout

Main directories:

- `markitdown-java/src/main/java`
- `src/test/java`
- `src/main/resources`
- `.github/workflows`
- `scripts`
- `verification`

Main files:

- [pom.xml](pom.xml)
- [markitdown.example.yml](markitdown.example.yml)
- [scripts/release_smoke.ps1](scripts/release_smoke.ps1)
- [verification/manual/markitdown.release.yml](verification/manual/markitdown.release.yml)

## Package Structure

### `com.markitdown`

Primary application entry point:

- [MarkItDownApplication.java](markitdown-java/src/main/java/com/markitdown/MarkItDownApplication.java)

Responsibilities:

- expose the top-level application entry point
- delegate CLI startup to `MarkItDownCommand`
- provide `createEngine()` for default engine creation

### `com.markitdown.cli`

Core CLI files:

- [MarkItDownCommand.java](markitdown-java/src/main/java/com/markitdown/cli/MarkItDownCommand.java)
- [ConfigCommands.java](markitdown-java/src/main/java/com/markitdown/cli/ConfigCommands.java)
- [UserMessageHelper.java](markitdown-java/src/main/java/com/markitdown/cli/UserMessageHelper.java)
- [CliConfigurationOverrides.java](markitdown-java/src/main/java/com/markitdown/cli/CliConfigurationOverrides.java)
- [InputDiscoveryHelper.java](markitdown-java/src/main/java/com/markitdown/cli/InputDiscoveryHelper.java)
- [RemoteInputHelper.java](markitdown-java/src/main/java/com/markitdown/cli/RemoteInputHelper.java)
- [PipeInputHelper.java](markitdown-java/src/main/java/com/markitdown/cli/PipeInputHelper.java)
- [OutputPathHelper.java](markitdown-java/src/main/java/com/markitdown/cli/OutputPathHelper.java)
- [BatchConversionRunner.java](markitdown-java/src/main/java/com/markitdown/cli/BatchConversionRunner.java)

Responsibilities:

- define CLI options
- handle configuration and diagnostic commands
- discover inputs
- orchestrate single-file and batch conversion
- manage output paths, progress, statistics, and user-facing errors

### `com.markitdown.config`

Configuration system files:

- [ConfigurationManager.java](markitdown-java/src/main/java/com/markitdown/config/ConfigurationManager.java)
- [ConversionOptions.java](markitdown-java/src/main/java/com/markitdown/config/ConversionOptions.java)

Responsibilities:

- `ConfigurationManager` loads YAML, flattens configuration data, tracks value sources, validates configuration, and builds the effective configuration view
- `ConversionOptions` carries runtime options shared by the CLI, engine, converters, and OCR engines

### `com.markitdown.core`

Core runtime files:

- [MarkItDownEngine.java](markitdown-java/src/main/java/com/markitdown/core/MarkItDownEngine.java)
- [ConverterRegistry.java](markitdown-java/src/main/java/com/markitdown/core/ConverterRegistry.java)

Responsibilities:

- `MarkItDownEngine` handles MIME detection, converter selection, and synchronous/asynchronous conversion
- `ConverterRegistry` handles converter registration, MIME lookup, and priority ordering

### `com.markitdown.converters`

Current built-in converters:

- `PdfConverter`
- `DocConverter`
- `DocxConverter`
- `PptConverter`
- `PptxConverter`
- `XlsConverter`
- `XlsxConverter`
- `HtmlConverter`
- `TextConverter`
- `ImageConverter`
- `AudioConverter`
- `ZipConverter`

These converters are integrated through the shared `DocumentConverter` interface.

### `com.markitdown.ocr`

OCR layer files:

- [OcrEngine.java](markitdown-java/src/main/java/com/markitdown/ocr/OcrEngine.java)
- [OcrProvider.java](markitdown-java/src/main/java/com/markitdown/ocr/OcrProvider.java)
- [OcrEngineFactory.java](markitdown-java/src/main/java/com/markitdown/ocr/OcrEngineFactory.java)
- [TesseractCliOcrEngine.java](markitdown-java/src/main/java/com/markitdown/ocr/TesseractCliOcrEngine.java)
- [HttpOcrEngine.java](markitdown-java/src/main/java/com/markitdown/ocr/HttpOcrEngine.java)
- [PaddleOcrEngine.java](markitdown-java/src/main/java/com/markitdown/ocr/PaddleOcrEngine.java)
- `UnavailableOcrEngine`

Responsibilities:

- `OcrProvider` handles engine availability checks and creation
- `OcrEngineFactory` selects the OCR engine from configuration
- `OcrEngine` performs OCR execution

### Other Packages

Other main packages:

- `com.markitdown.api`
- `com.markitdown.exceptions`
- `com.markitdown.models`
- `com.markitdown.utils`
- `com.markdown.engine`

In practice:

- `api` provides public contracts and result objects
- `utils` provides MIME detection and image extraction utilities
- `com.markdown.engine` provides Markdown rendering support

## Runtime Flow

### CLI Flow

The main CLI execution path is:

1. `MarkItDownApplication.main`
2. `MarkItDownCommand.main`
3. Picocli parses arguments
4. `MarkItDownCommand.call()`
5. the command branches into:
   - help/examples/version output
   - configuration diagnostics
   - normal conversion flow
6. effective configuration is built
7. `ConversionOptions` are assembled
8. inputs are discovered:
   - local files
   - directories
   - wildcards
   - URLs
   - stdin
9. the command selects single-file, batch, sequential, or parallel execution
10. `MarkItDownEngine.convert(...)` is invoked
11. the engine performs MIME detection, converter selection, and conversion
12. results are written to stdout or target files

### Library Flow

The main library execution path is:

1. create `MarkItDownEngine`
2. build `ConversionOptions`
3. call:
   - `convert(Path, options)`
   - `convert(InputStream, mimeType, options)`
4. read `ConversionResult`

## CLI Design

### `MarkItDownCommand`

The main command currently handles:

- option definition
- command-mode branching
- normal conversion orchestration
- output and statistics control

Its current role is orchestration rather than direct implementation of all input and output details.

### Helper Responsibilities

Current helper responsibilities:

- `CliConfigurationOverrides`
  - maps CLI arguments into the configuration system
- `InputDiscoveryHelper`
  - resolves file, directory, wildcard, and URL inputs
- `RemoteInputHelper`
  - handles remote downloads and file-name inference
- `PipeInputHelper`
  - handles stdin input and MIME detection
- `OutputPathHelper`
  - resolves output paths and writes files
- `BatchConversionRunner`
  - provides the batch sequential/parallel execution skeleton

## Configuration Design

### Supported Files

Current supported configuration file names:

- `markitdown.yml`
- `markitdown.local.yml`
- `markitdown.example.yml`

### Resolution Order

Without `--config-path`:

1. built-in defaults
2. `markitdown.yml`
3. `markitdown.local.yml`
4. CLI arguments

With `--config-path`:

- the explicit YAML file replaces project and local configuration files
- CLI arguments remain the highest-precedence source

### ConfigurationManager Behavior

`ConfigurationManager` currently:

- supports YAML only
- flattens nested structures
- normalizes YAML keys into internal keys
- tracks value sources
- validates structure and semantics

Key rules:

- `ocr.enabled` is internally mapped to `ocr.enable`
- underscore-separated keys are normalized into dotted internal keys

Current source labels:

- `default`
- `project-yaml`
- `local-yaml`
- `explicit-yaml`
- `cli`

### ConversionOptions Structure

`ConversionOptions` currently carries:

- content toggles
- table and image output formats
- output file path
- OCR parameters
- temporary directory
- maximum file size
- PDF password
- source file name

## Converter Design

### `DocumentConverter` Contract

Converters integrate through the `DocumentConverter` contract. Core concerns include:

- converter name
- supported MIME types
- priority
- stream-conversion capability
- file-mode and stream-mode conversion logic

### `ConverterRegistry`

Current `ConverterRegistry` behavior:

- converters are registered by name
- duplicate registration fails
- MIME lookup results are priority-ordered
- lookup results are cached by MIME type

The current supported MIME set is inferred from the known MIME list and `converter.supports(mimeType)`.

### `MarkItDownEngine`

`MarkItDownEngine` currently handles:

- file validation
- MIME detection
- converter lookup
- synchronous conversion
- asynchronous conversion

Main public methods:

- `convert(Path)`
- `convert(Path, ConversionOptions)`
- `convert(InputStream, String)`
- `convert(InputStream, String, ConversionOptions)`
- `convertAsync(...)`

The default asynchronous executor is `ForkJoinPool.commonPool()`.

## OCR Design

### Factory Model

Current providers registered in `OcrEngineFactory`:

- `tesseract-cli`
- `http`
- `paddleocr`
- `mock`

Current alias:

- `paddle-ocr` -> `paddleocr`

Engine creation flow:

1. return `UnavailableOcrEngine` when OCR is disabled
2. default to `tesseract-cli` when no engine is specified
3. resolve the provider by name
4. ask the provider whether it is available
5. create the concrete `OcrEngine`

### Current OCR Engines

#### `tesseract-cli`

Characteristics:

- uses the host-installed Tesseract command-line executable
- does not bundle `tesseract` or `tessdata`

#### `http`

Characteristics:

- uses a generic HTTP OCR integration
- depends on a configured endpoint

#### `paddleocr`

Characteristics:

- uses the current remote job-style OCR implementation in this repository
- uses endpoint, API key, model, timeout, and poll interval parameters

## Output Design

Current output contract:

- single file + explicit file path: write to that file
- single file + explicit directory path: write into that directory as `<source>.md`
- batch + directory path: create one output file per source file
- batch output names preserve the original extension and append `.md`

Examples:

- `note.txt` -> `note.txt.md`
- `data.json` -> `data.json.md`

Default path rules:

- local inputs default to output beside the source file
- remote inputs default to output in the current working directory

These rules are implemented in [OutputPathHelper.java](markitdown-java/src/main/java/com/markitdown/cli/OutputPathHelper.java).

## Remote And Pipe Input

### Remote Input

Remote input flow:

- accept HTTP(S) URLs
- download into a temporary area
- infer a file name from the URL or `Content-Disposition` where possible
- continue through the shared conversion flow

### Pipe Input

stdin input flow:

- read the input stream
- infer MIME when required
- pass the stream and MIME data into the engine

The current implementation requires MIME detection not to destroy leading-byte content.

## Testing Design

### Automated Tests

Default automated test command:

```bash
mvn test
```

Test directories:

- `src/test/java/com/markitdown/cli`
- `src/test/java/com/markitdown/config`
- `src/test/java/com/markitdown/converters`
- `src/test/java/com/markitdown/core`
- `src/test/java/com/markitdown/ocr`
- `src/test/java/com/markitdown/utils`

Current automated coverage mainly includes:

- CLI help and format listing
- configuration loading, validation, and overrides
- remote-input file-name resolution
- pipe-input MIME detection
- output-path contracts
- batch execution behavior
- core converters
- OCR factory/provider behavior

### Release Validation

Release validation script:

- [scripts/release_smoke.ps1](scripts/release_smoke.ps1)

Script responsibilities:

- auto-discover the CLI jar under `target/`
- validate `--help`
- validate `--show-config`
- validate `--validate-config`
- validate text and structured-text conversion
- validate batch conversion
- optionally validate a remote URL path
- optionally validate real Tesseract OCR

Tracked validation assets:

- [verification/manual/markitdown.release.yml](verification/manual/markitdown.release.yml)
- `verification/fixtures/*`

## CI

Current CI workflow file:

- [ci.yml](.github/workflows/ci.yml)

Current CI behavior:

- Ubuntu
- JDK 17
- `mvn -B verify`

## Development Tasks

### Run The Main Test Suite

```bash
mvn test
```

### Build The Packaged Jar

```bash
mvn clean package
```

### Run Focused CLI Tests

```bash
mvn "-Dtest=CliConfigurationOverridesTest,InputDiscoveryHelperTest,RemoteInputHelperTest,PipeInputHelperTest,OutputPathHelperTest,BatchConversionRunnerTest" test
```

### Run Release Smoke

```powershell
powershell -ExecutionPolicy Bypass -File scripts\release_smoke.ps1
```

### Run OCR Smoke With Real Tesseract

```powershell
powershell -ExecutionPolicy Bypass -File scripts\release_smoke.ps1 `
  -TesseractPath O:\tesserOCR\tesseract.exe `
  -TessdataPath O:\tesserOCR\tessdata `
  -OcrInputPath test\with-text.png
```


