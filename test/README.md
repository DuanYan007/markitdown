# MarkItDown Test Files

This directory contains the sample files used for regression, format verification, OCR validation, and release smoke testing.

## Coverage Summary

The current sample set covers:

- PDF
- Word
- Excel
- PowerPoint
- Images
- Audio metadata
- HTML
- Text / JSON / XML / CSV
- ZIP archives and nested ZIP archives
- Large files, empty files, encrypted files, multilingual files, and layout-heavy files

Representative examples include:

- `plain-text.pdf`, `scanned.pdf`, `with-tables.pdf`, `complex-layout.pdf`
- `basic.docx`, `with-images.docx`, `with-tables.docx`, `multi-level-lists.docx`
- `basic.xlsx`, `with-formulas.xlsx`, `multi-sheet.xlsx`, `merged-cells.xlsx`
- `basic.pptx`, `with-images.pptx`, `with-charts.pptx`, `animations.pptx`
- `with-text.png`, `with-text-chinese.png`, `sample.jpg`, `high-resolution.tiff`
- `sample.mp3`, `speech-english.mp3`, `speech-chinese.mp3`
- `basic.html`, `with-images.html`, `with-tables.html`, `html5-semantic.html`
- `basic.txt`, `basic.json`, `basic.xml`, `basic.csv`
- `nested.zip`, `mixed-documents.zip`, `complex-nested.zip`

## Automated Tests

The code-level automated tests live in:

- `src/test/java/com/markitdown/build/ProfileConfigurationTest.java`
- `src/test/java/com/markitdown/ocr/OcrEngineFactoryTest.java`
- `src/test/java/com/markitdown/ocr/PaddleOcrEngineTest.java`
- `src/test/java/com/markitdown/converter/TextConverterStreamingTest.java`
- `src/test/java/com/markitdown/converter/ZipConverterTest.java`

Run them with:

```bash
mvn test
```

## Manual Verification Examples

### Basic text conversion

```bash
java -jar target/markitdown4j-0.0.3-lite.jar test/basic.txt -o out/basic.md
```

### PDF conversion

```bash
java -jar target/markitdown4j-0.0.3-lite.jar test/plain-text.pdf -o out/plain-text.md
```

### Word conversion

```bash
java -jar target/markitdown4j-0.0.3-lite.jar test/basic.docx -o out/basic-docx.md
```

### Excel conversion

```bash
java -jar target/markitdown4j-0.0.3-lite.jar test/basic.xlsx -o out/basic-xlsx.md
```

### HTML conversion

```bash
java -jar target/markitdown4j-0.0.3-lite.jar test/basic.html -o out/basic-html.md
```

### ZIP conversion

```bash
java -jar target/markitdown4j-0.0.3-lite.jar test/nested.zip -o out/nested-zip.md
```

## OCR Verification Examples

### Windows embedded OCR with `tess4j`

```bash
java -jar target/markitdown4j-0.0.3-win64.jar test/with-text.png --ocr --ocr-engine tess4j -l eng -o out/win64-ocr.md
```

### Linux or macOS OCR with `tesseract-cli`

```bash
java -jar target/markitdown4j-0.0.3-linux64.jar test/with-text.png --ocr --ocr-engine tesseract-cli -l eng -o out/linux-ocr.md
```

### Remote PaddleOCR

```bash
java -jar target/markitdown4j-0.0.3-lite.jar test/with-text.png --ocr --ocr-engine paddleocr -o out/paddle-ocr.md
```

## Release Smoke Paths

Before release, the most important paths to verify are:

1. `lite` basic conversion
2. `win64 + tess4j`
3. `linux64/mac + tesseract-cli`
4. `lite + paddleocr`

These paths were exercised during the current release preparation in addition to the automated tests.
