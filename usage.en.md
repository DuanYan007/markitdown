# Usage

## CLI

The repository currently ships the CLI as `target/markitdown4j-<version>.jar`.
`markitdown4j` in the commands below is documentation shorthand for `java -jar target/markitdown4j-<version>.jar`.
Use `java -jar ...` directly unless you create your own shell alias or wrapper script.

Basic form:

```bash
markitdown4j [options] <input...>
```

Common examples:

```bash
markitdown4j input.pdf -o output.md
markitdown4j a.pdf b.docx c.xlsx -o out/
markitdown4j ./docs --batch -o out/
markitdown4j https://example.com/report.pdf -o out/
curl -s https://example.com/sample.pdf | markitdown4j --mime-type application/pdf
```

Common options:

- `-o`, `--output`
- `-f`, `--format`
- `-m`, `--mime-type`
- `--ocr`
- `--ocr-engine`
- `-l`, `--language`
- `--config-path`
- `--show-config`
- `--validate-config`
- `--generate-config`
- `--batch`
- `--parallel`
- `--recursive`

## Output Rules

- If `-o` points to a file path, single-file conversion writes to that exact file.
- If `-o` points to a directory, output files are created inside that directory.
- Batch output file names currently preserve the source extension and append `.md`, such as `note.txt.md`.
- Without `-o`, local file input defaults to `<source>.md` beside the input file.
- Without `-o`, remote input defaults to `<downloaded-name>.md` in the current working directory.

## Configuration Commands

```bash
markitdown4j --generate-config
markitdown4j --show-config
markitdown4j --validate-config
```

## OCR

Current OCR engines:

- `tesseract-cli`
- `paddleocr`
- `http`

Current behavior:

- `tesseract-cli` depends on a host-installed Tesseract executable.
- The jar does not bundle `tesseract` or `tessdata`.
- URL download and HTTP failures are surfaced as `Remote input error`.
- OCR initialization failures are surfaced as `OCR unavailable`.
- OCR execution failures are surfaced as `OCR error`.

Windows example:

```bash
markitdown4j image.png --ocr --ocr-engine tesseract-cli --language eng
```

## Java Library

The library surface is for direct in-process integration inside Java applications.

### Main API types

- `com.markitdown.core.MarkItDownEngine`
- `com.markitdown.config.ConversionOptions`
- `com.markitdown.api.ConversionResult`
- `com.markitdown.api.DocumentConverter`
- `com.markitdown.exceptions.ConversionException`
- `com.markitdown.MarkItDownApplication`

### Creating an engine

Default registry:

```java
MarkItDownEngine engine = new MarkItDownEngine(MarkItDownEngine.createDefaultRegistry());
```

Convenience helper:

```java
MarkItDownEngine engine = MarkItDownApplication.createEngine();
```

### File-based conversion

```java
MarkItDownEngine engine = MarkItDownApplication.createEngine();
try {
    ConversionOptions options = ConversionOptions.builder().build();
    ConversionResult result = engine.convert(Path.of("input.pdf"), options);

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

Current stream rules:

- you must provide a MIME type
- not every converter supports streaming
- unsupported stream conversion raises `ConversionException`
- path-based conversion is the safer default when you already have a real file

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

Typical option categories:

- content toggles
- OCR engine and OCR runtime fields
- image and table output format
- page-break handling
- file-size limits
- temporary directory and image output directory

### Reading results

Common result methods:

- `getMarkdown()`
- `getMetadata()`
- `getWarnings()`
- `isSuccessful()`
- `getOriginalFileName()`
- `getFileSize()`
- `getConversionTime()`

### Failure handling

Handle both:

- thrown `ConversionException`
- unsuccessful `ConversionResult`

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

Example:

```java
CompletableFuture<ConversionResult> future =
        engine.convertAsync(Path.of("input.pdf"), options);

ConversionResult result = future.join();
```

### Support checks

```java
boolean supportedPath = engine.isSupported(Path.of("input.pdf"));
boolean supportedMime = engine.isSupported("application/pdf");
```

You can also inspect the registered capability set:

```java
var mimeTypes = engine.getSupportedMimeTypes();
var converterInfo = engine.getConverterInfo();
```

### Lifecycle

Call `shutdown()` when the engine is no longer needed.

That is especially important when:

- the engine is long-lived
- async or parallel APIs are used
- a custom executor is supplied

