# GitHub Release Template

## Summary

This release updates the Java CLI packaging and OCR architecture.

## Artifacts

| Artifact | Recommended usage |
| --- | --- |
| `markitdown4j-<version>-lite.jar` | Smallest package, no embedded `tess4j` native files |
| `markitdown4j-<version>-full.jar` | Full package with embedded `tess4j` resources |
| `markitdown4j-<version>-win32.jar` | Windows 32-bit OCR package |
| `markitdown4j-<version>-win64.jar` | Windows 64-bit OCR package |
| `markitdown4j-<version>-linux64.jar` | Linux package, recommended with `tesseract-cli` |
| `markitdown4j-<version>-mac.jar` | macOS package, recommended with `tesseract-cli` |

## OCR engines

- `tess4j`: best for `full`, `win32`, `win64`
- `tesseract-cli`: best for `linux64`, `mac`, `lite`
- `mock`: tests and debugging
- `http`: optional external OCR integration

## Build commands

```bash
mvn package -DskipTests
mvn package -DskipTests -Pfull
mvn package -DskipTests -Pwin32
mvn package -DskipTests -Pwin64
mvn package -DskipTests -Plinux64
mvn package -DskipTests -Pmac
```

## Recommended downloads

- Windows 64-bit users: `win64` or `full`
- Windows 32-bit users: `win32`
- Linux users: `linux64`
- macOS users: `mac`
- Minimal / CI / no-OCR usage: `lite`

## Notes

- `linux64` and `mac` do not embed `tess4j` native resources.
- `win32` and `win64` keep only their matching Windows OCR native files.
- OCR is now pluggable and can be selected with `--ocr-engine`.

## Example commands

```bash
java -jar markitdown4j-<version>-win64.jar image.png --ocr --ocr-engine tess4j -l chi_sim -o result.md
java -jar markitdown4j-<version>-linux64.jar image.png --ocr --ocr-engine tesseract-cli -l eng -o result.md
```
