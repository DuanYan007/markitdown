# markitdown4j

[![version](https://img.shields.io/badge/version-0.0.4-0F766E)](pom.xml)
[![java](https://img.shields.io/badge/java-11-1D4ED8)](pom.xml)
[![packaging](https://img.shields.io/badge/packaging-cli%20%2B%20library-7C3AED)](readme.en.md)
[![license](https://img.shields.io/badge/license-MIT-15803D)](LICENSE)
[![ocr](https://img.shields.io/badge/ocr-tesseract--cli%20%7C%20http%20%7C%20paddleocr-B45309)](readme.en.md)

> A Java reimplementation of Microsoft MarkItDown for document-to-Markdown conversion, built as an open-source rewrite project and delivered as a CLI and Java library.

| Item | Value |
| --- | --- |
| Current version | `0.0.4` |
| Language | Java 11 |
| Packaging | Executable shaded JAR + reusable library |
| OCR engines | `tesseract-cli`, `http`, `paddleocr` |
| Main artifact | `target/markitdown4j-0.0.4.jar` |
| License | MIT |

## 馃幆 Project Positioning

This repository is positioned as:

- a Java rewrite of Microsoft MarkItDown
- an open-source rewrite competition project
- a practical document-to-Markdown tool for local CLI use
- a reusable Java library for embedding conversion into Java systems

The current repository documents and implements the current Java version only.

## 馃Л What It Does

`markitdown4j` converts heterogeneous input files into Markdown and exposes the same core engine through two product surfaces:

- CLI: for local conversion, batch processing, OCR control, config inspection, and automation-oriented workflows
- Java library: for direct embedding in Java applications and services

## 鈿欙笍 Current Capabilities

### Conversion Features

- Convert local files, directories, HTTP(S) URLs, and stdin streams
- Output Markdown with configurable table and image rendering
- Extract images into output-relative asset directories
- Apply OCR when image-based text extraction is required
- Load YAML configuration and inspect the effective resolved config
- Run sequential or multi-file batch workflows from the CLI

### OCR Options

| Engine | Typical use |
| --- | --- |
| `tesseract-cli` | Local cross-platform OCR with a system Tesseract installation |
| `http` | OCR through a custom HTTP endpoint |
| `paddleocr` | OCR through PaddleOCR job API |

## 馃梻锔?Supported Inputs

### Main formats

- PDF
- DOCX
- XLSX
- HTML / HTM
- TXT / Markdown
- CSV / JSON / XML
- images
- ZIP archives

### Additional formats with format-specific handling

- DOC
- PPTX
- PPT
- XLS
- audio

## 馃殌 Quick Start

The repository currently ships the CLI as a JAR:

```bash
target/markitdown4j-0.0.4.jar
```

In this document, `markitdown4j` is shorthand for:

```bash
java -jar target/markitdown4j-0.0.4.jar
```

Use the full `java -jar ...` form directly unless you created your own alias or wrapper script.

### Build

```bash
mvn clean package
```

### Basic CLI form

```bash
markitdown4j [options] <input...>
```

### Common examples

Convert one PDF:

```bash
markitdown4j report.pdf -o output.md
```

Batch-convert supported files in a directory:

```bash
markitdown4j ./docs --batch -o out/
```

Convert a remote file:

```bash
markitdown4j https://example.com/report.pdf -o out/
```

Convert with OCR:

```bash
markitdown4j scan.pdf --ocr --ocr-engine tesseract-cli -o output.md
```

Generate and inspect config:

```bash
markitdown4j --generate-config
markitdown4j --validate-config
markitdown4j --show-config
```

## 馃З Configuration Model

The current configuration system is YAML-first.

Recognized files:

- `markitdown.yml`
- `markitdown.local.yml`
- `markitdown.example.yml`

Resolution order:

1. built-in defaults
2. `markitdown.yml`
3. `markitdown.local.yml`
4. CLI overrides

Use [configuration.en.md](configuration.en.md) for the full setting reference, scenario-based examples, validation rules, and CLI override behavior.

## 鈽?Library Usage

The library surface is intended for direct Java integration, not just as a secondary note under the CLI.

Typical use cases:

- process uploaded documents inside a Java service
- run conversion jobs in Java batch systems
- consume Markdown, metadata, and warnings as structured return values
- customize converter registration and invocation inside Java code

### Install

Install locally:

```bash
mvn clean install
```

Maven coordinates:

```xml
<dependency>
  <groupId>com.markitdown</groupId>
  <artifactId>markitdown4j</artifactId>
  <version>0.0.4</version>
</dependency>
```

### Main API types

| Type | Purpose |
| --- | --- |
| `com.markitdown.core.MarkItDownEngine` | Main conversion runtime entry point |
| `com.markitdown.config.ConversionOptions` | Runtime conversion options |
| `com.markitdown.api.ConversionResult` | Result object with Markdown, metadata, warnings, and status |
| `com.markitdown.api.DocumentConverter` | Converter extension contract |
| `com.markitdown.exceptions.ConversionException` | Checked exception for conversion failures |
| `com.markitdown.MarkItDownApplication` | Convenience helper exposing `createEngine()` |

### Minimal file conversion

```java
MarkItDownEngine engine = MarkItDownApplication.createEngine();
try {
    ConversionOptions options = ConversionOptions.builder().build();
    ConversionResult result = engine.convert(Path.of("report.pdf"), options);

    if (result.isSuccessful()) {
        System.out.println(result.getMarkdown());
    } else {
        System.err.println(result.getWarnings());
    }
} finally {
    engine.shutdown();
}
```

### Stream-based conversion

```java
try (InputStream input = /* your stream */) {
    ConversionResult result = engine.convert(input, "text/plain", options);
}
```

Important notes:

- stream conversion requires an explicit MIME type
- not every converter supports streaming
- unsupported stream conversion throws `ConversionException`
- if you already have a real file, path-based conversion is the safer default

### Building options

```java
ConversionOptions options = ConversionOptions.builder()
        .includeMetadata(true)
        .includeImages(true)
        .includeTables(true)
        .tableFormat("github")
        .imageFormat("markdown")
        .useOcr(true)
        .ocrEngine("tesseract-cli")
        .language("eng")
        .tesseractPath("O:/tesserOCR/tesseract.exe")
        .tessdataPath("O:/tesserOCR/tessdata")
        .maxFileSize(0)
        .build();
```

### Result handling

Use:

- `getMarkdown()`
- `getMetadata()`
- `getWarnings()`
- `isSuccessful()`
- `getOriginalFileName()`
- `getFileSize()`
- `getConversionTime()`

### Failure handling

Current library code can fail in two ways:

- by throwing `ConversionException`
- by returning an unsuccessful `ConversionResult`

```java
try {
    ConversionResult result = engine.convert(Path.of("input.bin"), options);
    if (!result.isSuccessful()) {
        System.err.println(result.getWarnings());
    }
} catch (ConversionException ex) {
    System.err.println(ex.getMessage());
}
```

### Async and batch APIs

Current async and batch methods:

- `convertAsync(Path, ConversionOptions)`
- `convertAsync(InputStream, String, ConversionOptions)`
- `convertParallel(List<Path>, ConversionOptions)`
- `convertAll(List<Path>, ConversionOptions)`

### Support checks

```java
boolean supportedPath = engine.isSupported(Path.of("input.pdf"));
boolean supportedMime = engine.isSupported("application/pdf");
```

### Lifecycle

Call `shutdown()` when the engine is no longer needed, especially for long-lived engines and async workloads.

## 馃摎 Documentation

English documents:

- 馃摌 [usage.en.md](usage.en.md): CLI and Java library usage, command patterns, output rules, OCR usage, and integration examples.
- 鈿欙笍 [configuration.en.md](configuration.en.md): configuration model, YAML keys, scenario-based configuration examples, validation rules, and CLI override behavior.
- 馃И [testing.en.md](testing.en.md): current test inventory, test scope, and concrete test command references.
- 馃洜锔?[development.en.md](development.en.md): implementation-oriented design and development documentation for contributors.

涓枃鏂囨。锛?
- 馃摌 [usage.md](usage.md)锛氳鏄?CLI 涓?Java Library 鐨勪娇鐢ㄦ柟寮忋€佸懡浠ゆā寮忋€佽緭鍑鸿鍒欍€丱CR 鐢ㄦ硶鍜岄泦鎴愮ず渚嬨€?- 鈿欙笍 [configuration.md](configuration.md)锛氳鏄庨厤缃ā鍨嬨€乊AML 閰嶇疆椤广€佸満鏅寲閰嶇疆绀轰緥銆佹牎楠岃鍒欏拰 CLI 瑕嗙洊鍏崇郴銆?- 馃И [testing.md](testing.md)锛氬垪鍑哄綋鍓嶆祴璇曢」銆佹祴璇曡寖鍥村拰瀵瑰簲娴嬭瘯鍛戒护銆?- 馃洜锔?[development.md](development.md)锛氶潰鍚戣础鐚€呯殑瀹炵幇绾ц璁′笌寮€鍙戞枃妗ｃ€?
## 馃彈锔?Build And Runtime Requirements

- JDK 11
- Maven 3.8+
- Tesseract installation only if you use `tesseract-cli` OCR

## 馃П Current Product Surfaces

| Surface | Status | Purpose |
| --- | --- | --- |
| CLI | primary | End-user conversion workflow |
| Java library | supported | Embedded Java integration |

## 馃搫 License

- [LICENSE](LICENSE)

