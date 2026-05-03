# Release Checklist

## Release Notes Draft

Title suggestion:

`v0.0.x - Java CLI Packaging and OCR Refactor`

```md
## Highlights

This release upgrades the Java CLI packaging model and refactors OCR into a selectable provider architecture.

### What changed

- Added multiple Java CLI artifacts:
  - `lite`
  - `full`
  - `win32`
  - `win64`
  - `linux64`
  - `mac`
- Decoupled OCR from the core conversion flow
- Added selectable OCR engines:
  - `tess4j`
  - `tesseract-cli`
  - `mock`
  - `http`
- Reduced package size for non-Windows and no-OCR scenarios
- Improved OCR degradation behavior when an engine is unavailable
- Normalized Markdown metadata labels to avoid garbled output
- Improved logging behavior for CLI usage

## Which package should I download?

- Windows 64-bit: `win64`
- Windows 32-bit: `win32`
- Linux: `linux64`
- macOS: `mac`
- Smallest package / no OCR: `lite`
- Want everything bundled: `full`

## OCR guidance

- `tess4j`
  - Recommended for `full`, `win32`, `win64`
  - Best when you want embedded OCR support
- `tesseract-cli`
  - Recommended for `linux64`, `mac`, `lite`
  - Best when Tesseract is installed on the host system
- `mock`
  - Useful for testing and debugging
- `http`
  - Optional external integration path

## Build commands

```bash
mvn package -DskipTests
mvn package -DskipTests -Pfull
mvn package -DskipTests -Pwin32
mvn package -DskipTests -Pwin64
mvn package -DskipTests -Plinux64
mvn package -DskipTests -Pmac
```

## Notes

- Linux and macOS packages do not embed `tess4j` native OCR payloads by default.
- For Linux and macOS, `tesseract-cli` is the recommended OCR path.
- If OCR is unavailable, the CLI should now degrade more gracefully instead of crashing.

## Documentation

- Root guide: [README.md](https://github.com/DuanYan007/markitdown/blob/main/README.md)
- Java CLI guide: [java/README.md](https://github.com/DuanYan007/markitdown/blob/main/java/README.md)
- Installation: [java/INSTALLATION.md](https://github.com/DuanYan007/markitdown/blob/main/java/INSTALLATION.md)
- Command reference: [java/COMMAND_REFERENCE.md](https://github.com/DuanYan007/markitdown/blob/main/java/COMMAND_REFERENCE.md)
```

## Acceptance Record Template

```md
# Release Acceptance Record

## Basic info

- Version:
- Date:
- Operator:
- Environment:
- JDK version:
- OS:

## Artifact build check

- [ ] `mvn -DskipTests package`
- [ ] `mvn -DskipTests -Pfull package`
- [ ] `mvn -DskipTests -Pwin32 package`
- [ ] `mvn -DskipTests -Pwin64 package`
- [ ] `mvn -DskipTests -Plinux64 package`
- [ ] `mvn -DskipTests -Pmac package`

## Artifact presence check

- [ ] `markitdown4j-<version>-lite.jar`
- [ ] `markitdown4j-<version>-full.jar`
- [ ] `markitdown4j-<version>-win32.jar`
- [ ] `markitdown4j-<version>-win64.jar`
- [ ] `markitdown4j-<version>-linux64.jar`
- [ ] `markitdown4j-<version>-mac.jar`

## Artifact size check

- [ ] `lite` is smaller than `full`
- [ ] `linux64` is smaller than `full`
- [ ] `mac` is smaller than `full`
- [ ] `win32/win64` are between `lite` and `full`

Recorded sizes:
- lite:
- full:
- win32:
- win64:
- linux64:
- mac:

## Runtime smoke checks

### 1. lite basic conversion
- [ ] pass
- Command:
- Result file:

### 2. win64 + tess4j OCR
- [ ] pass
- Command:
- Result file:

### 3. linux64/mac + tesseract-cli OCR
- [ ] pass
- Command:
- Result file:

### 4. lite degradation behavior
- [ ] pass
- Command:
- Expected:
- Actual:

## Output quality check

- [ ] Markdown metadata labels are not garbled
- [ ] Default logs are acceptable
- [ ] No unexpected dependency/logging warnings
- [ ] OCR unavailable messages are understandable

## Documentation check

- [ ] root README matches release
- [ ] java README matches release
- [ ] installation guide matches release
- [ ] command reference matches release
- [ ] release notes match actual artifacts

## Known issues

- Issue 1:
- Issue 2:

## Final decision

- [ ] Ready to publish
- [ ] Needs fixes before publish

Notes:
```

## Minimum publish gate

- All six artifacts build successfully in a clean environment.
- `lite`, `win64 + tess4j`, and `linux64/mac + tesseract-cli` all pass smoke checks.
- Output is readable, non-garbled, and not overly noisy.
- README and release notes match actual artifacts and OCR guidance.
