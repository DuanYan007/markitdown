# Testing

This file lists the current test inventory only.

The repository currently ships the CLI as `target/markitdown4j-<version>.jar`.
`markitdown4j` in the functional test command column is documentation shorthand for `java -jar target/markitdown4j-<version>.jar`.
Use `java -jar ...` directly unless you create your own shell alias or wrapper script.

## Functional Test Items

| # | Test item | Test type | Test content | Test command |
| --- | --- | --- | --- | --- |
| 1 | `show-config-explicit-yaml` | `Functional / standard / config` | Load `verification/manual/markitdown.release.yml` and print the effective configuration. | `markitdown4j --show-config --config-path verification/manual/markitdown.release.yml` |
| 2 | `validate-config-success` | `Functional / standard / config` | Validate the release configuration file and confirm it passes. | `markitdown4j --validate-config --config-path verification/manual/markitdown.release.yml` |
| 3 | `validate-config-failure` | `Functional / standard / config` | Validate an intentionally invalid YAML file and confirm the command fails. | `markitdown4j --validate-config --config-path invalid.yml` |
| 4 | `show-config-cli-override` | `Functional / standard / config` | Apply CLI OCR overrides on top of YAML and confirm the effective configuration reports the CLI source. | `markitdown4j --show-config --config-path verification/manual/markitdown.release.yml --ocr --ocr-engine http` |
| 5 | `generate-config` | `Functional / standard / config` | Generate a default `markitdown.yml` and confirm expected sections such as `ocr` and `format`. | `markitdown4j --generate-config` |
| 6 | `convert-basic-txt` | `Functional / standard / file conversion` | Convert `test/basic.txt` and confirm non-empty Markdown output. | `markitdown4j test/basic.txt -o out/basic.txt.md` |
| 7 | `convert-basic-json` | `Functional / standard / file conversion` | Convert `test/basic.json` and confirm non-empty Markdown output. | `markitdown4j test/basic.json -o out/basic.json.md` |
| 8 | `convert-basic-html` | `Functional / standard / file conversion` | Convert `test/basic.html` and confirm non-empty Markdown output. | `markitdown4j test/basic.html -o out/basic.html.md` |
| 9 | `convert-basic-docx` | `Functional / standard / file conversion` | Convert `test/basic.docx` and confirm non-empty Markdown output. | `markitdown4j test/basic.docx -o out/basic.docx.md` |
| 10 | `convert-basic-pptx` | `Functional / standard / file conversion` | Convert `test/basic.pptx` and confirm non-empty Markdown output. | `markitdown4j test/basic.pptx -o out/basic.pptx.md` |
| 11 | `convert-basic-xlsx` | `Functional / standard / file conversion` | Convert `test/basic.xlsx` and confirm non-empty Markdown output. | `markitdown4j test/basic.xlsx -o out/basic.xlsx.md` |
| 12 | `convert-plain-text-pdf` | `Functional / standard / file conversion` | Convert `test/plain-text.pdf` and confirm non-empty Markdown output. | `markitdown4j test/plain-text.pdf -o out/plain-text.pdf.md` |
| 13 | `convert-nested-zip` | `Functional / standard / file conversion` | Convert `test/nested.zip` and confirm non-empty Markdown output. | `markitdown4j test/nested.zip -o out/nested.zip.md` |
| 14 | `convert-sample-png` | `Functional / standard / file conversion` | Convert `test/sample.png` and confirm non-empty Markdown output. | `markitdown4j test/sample.png -o out/sample.png.md` |
| 15 | `convert-sample-mp3` | `Functional / standard / file conversion` | Convert `test/sample.mp3` and confirm non-empty Markdown output. | `markitdown4j test/sample.mp3 -o out/sample.mp3.md` |
| 16 | `convert-with-images-docx` | `Functional / standard / file conversion` | Convert `test/with-images.docx` and confirm non-empty Markdown output. | `markitdown4j test/with-images.docx -o out/with-images.docx.md` |
| 17 | `convert-with-images-html` | `Functional / standard / file conversion` | Convert `test/with-images.html` and confirm non-empty Markdown output. | `markitdown4j test/with-images.html -o out/with-images.html.md` |
| 18 | `convert-with-images-xlsx` | `Functional / standard / file conversion` | Convert `test/with-images.xlsx` and confirm non-empty Markdown output. | `markitdown4j test/with-images.xlsx -o out/with-images.xlsx.md` |
| 19 | `convert-with-notes-pptx` | `Functional / standard / file conversion` | Convert `test/with-notes.pptx` and confirm non-empty Markdown output. | `markitdown4j test/with-notes.pptx -o out/with-notes.pptx.md` |
| 20 | `convert-multi-sheet-xlsx` | `Functional / standard / file conversion` | Convert `test/multi-sheet.xlsx` and confirm non-empty Markdown output. | `markitdown4j test/multi-sheet.xlsx -o out/multi-sheet.xlsx.md` |
| 21 | `convert-old-format-xls` | `Functional / standard / file conversion` | Convert `test/old-format.xls` and confirm non-empty Markdown output. | `markitdown4j test/old-format.xls -o out/old-format.xls.md` |
| 22 | `convert-complex-nested-zip` | `Functional / standard / file conversion` | Convert `test/complex-nested.zip` and confirm non-empty Markdown output. | `markitdown4j test/complex-nested.zip -o out/complex-nested.zip.md` |
| 23 | `convert-plain-text-chinese-pdf` | `Functional / standard / file conversion` | Convert `test/plain-text-chinese.pdf` and confirm non-empty Markdown output. | `markitdown4j test/plain-text-chinese.pdf -o out/plain-text-chinese.pdf.md` |
| 24 | `convert-100-pages-pdf` | `Functional / standard / file conversion` | Convert `test/100-pages.pdf` and confirm non-empty Markdown output. | `markitdown4j test/100-pages.pdf -o out/100-pages.pdf.md` |
| 25 | `convert-1000-pages-docx` | `Functional / standard / file conversion` | Convert `test/1000-pages.docx` and confirm non-empty Markdown output. | `markitdown4j test/1000-pages.docx -o out/1000-pages.docx.md` |
| 26 | `convert-10000-rows-xlsx` | `Functional / standard / file conversion` | Convert `test/10000-rows.xlsx` and confirm non-empty Markdown output. | `markitdown4j test/10000-rows.xlsx -o out/10000-rows.xlsx.md` |
| 27 | `convert-large-archive-zip` | `Functional / standard / file conversion` | Convert `test/large-archive.zip` and confirm non-empty Markdown output. | `markitdown4j test/large-archive.zip -o out/large-archive.zip.md` |
| 28 | `convert-with-media-pptx` | `Functional / standard / file conversion` | Convert `test/with-media.pptx` and confirm non-empty Markdown output. | `markitdown4j test/with-media.pptx -o out/with-media.pptx.md` |
| 29 | `convert-large-dataset-xlsx` | `Functional / standard / file conversion` | Convert `test/large-dataset.xlsx` and confirm non-empty Markdown output. | `markitdown4j test/large-dataset.xlsx -o out/large-dataset.xlsx.md` |
| 30 | `single-file-output-directory` | `Functional / standard / output path` | Write a single file into a directory output target and confirm `<source>.md` naming. | `markitdown4j test/basic.txt -o out/dir/` |
| 31 | `batch-directory-conversion` | `Functional / standard / output path` | Convert `verification/fixtures/batch` with `--batch` and confirm both output files are created. | `markitdown4j verification/fixtures/batch --batch -o out/batch/` |
| 32 | `stdin-text-conversion` | `Functional / standard / stdin` | Pipe plain text into the CLI and confirm Markdown text is emitted to stdout. | `Get-Content -Raw input.txt \| markitdown4j` |
| 33 | `stdin-json-explicit-mime` | `Functional / standard / stdin` | Pipe JSON into the CLI with explicit MIME type and confirm structured output. | `'{"hello":"world"}' \| markitdown4j --mime-type application/json` |
| 34 | `docx-no-tables-option` | `Functional / standard / option behavior` | Convert `test/with-tables.docx` with `--no-tables` and confirm output is reduced compared with the default conversion. | `markitdown4j test/with-tables.docx --no-tables -o out/with-tables.no-tables.md` |
| 35 | `image-html-format-option` | `Functional / standard / option behavior` | Convert `test/sample.png` with `--image-format html` and confirm HTML `<img>` output. | `markitdown4j test/sample.png --image-format html -o out/sample.image-html.md` |
| 36 | `pptx-no-metadata-option` | `Functional / standard / option behavior` | Convert `test/basic.pptx` with `--no-metadata` and confirm output is reduced compared with the default conversion. | `markitdown4j test/basic.pptx --no-metadata -o out/basic.pptx.no-metadata.md` |
| 37 | `xlsx-markdown-table-option` | `Functional / standard / option behavior` | Convert `test/basic.xlsx` with `--table-format markdown` and confirm markdown-style table output. | `markitdown4j test/basic.xlsx --table-format markdown -o out/basic.markdown-table.xlsx.md` |
| 38 | `ocr-tesseract-cli-png` | `Functional / standard / OCR` | Convert `test/with-text.png` with OCR enabled and confirm Tesseract CLI extracts the expected marker text. | `markitdown4j test/with-text.png --ocr --config-path ocr.yml -o out/ocr/` |
| 39 | `ocr-scanned-pdf` | `Functional / standard / OCR` | Convert `test/scanned.pdf` with OCR enabled and confirm non-empty Markdown output. | `markitdown4j test/scanned.pdf --ocr --config-path ocr.yml -o out/scanned.pdf.md` |
| 40 | `old-format-doc-failure` | `Functional / standard / failure path` | Convert `test/old-format.doc` and confirm the current OLE2 or OOXML mismatch path fails as expected. | `markitdown4j test/old-format.doc -o out/old-format.doc.md` |
| 41 | `encrypted-pdf-warning-path` | `Functional / standard / warning path` | Convert `test/encrypted.pdf` and confirm completion together with the current decrypt warning path. | `markitdown4j test/encrypted.pdf -o out/encrypted.pdf.md` |
| 42 | `password-protected-pdf-warning-path` | `Functional / standard / warning path` | Convert `test/password-protected.pdf` and confirm completion together with the current decrypt warning path. | `markitdown4j test/password-protected.pdf -o out/password-protected.pdf.md` |
| 43 | `huge-pdf-default-limit` | `Functional / stress / large file` | Convert `test/huge-pdf.pdf` without `--large-file` and confirm the default `50 MB` size limit rejects the file. | `markitdown4j test/huge-pdf.pdf -o out/huge-pdf.default.md` |
| 44 | `huge-pdf-with-large-file` | `Functional / stress / large file` | Convert `test/huge-pdf.pdf` with `--large-file` and confirm successful Markdown output. | `markitdown4j test/huge-pdf.pdf --large-file -o out/huge-pdf.large.md` |
| 45 | `mixed-formats-default-limit` | `Functional / stress / large file` | Convert `test/mixed-formats.zip` without `--large-file` and confirm the default `50 MB` size limit rejects the file. | `markitdown4j test/mixed-formats.zip -o out/mixed-formats.default.md` |
| 46 | `mixed-formats-with-large-file` | `Functional / stress / large file` | Convert `test/mixed-formats.zip` with `--large-file` and confirm overall success together with ZIP-entry warning-path messages. | `markitdown4j test/mixed-formats.zip --large-file -o out/mixed-formats.large.md` |

## Automated Unit And Integration Test Items

| # | Test suite | Test item | Test type | Test content | Test command |
| --- | --- | --- | --- | --- | --- |
| 1 | `com.markitdown.build.CodeStyleGuardTest` | `productionSourcesShouldNotUseWildcardImports` | `Automated / build guard / unit` | Verify that production Java source files do not use wildcard imports. | `-` |
| 2 | `com.markitdown.build.CodeStyleGuardTest` | `javaSourcesShouldNotContainTabsOrTrailingWhitespace` | `Automated / build guard / unit` | Verify that Java source files do not contain tab characters or trailing whitespace. | `-` |
| 3 | `com.markitdown.build.ProfileConfigurationTest` | `containsCurrentReleaseProfileOnly` | `Automated / build guard / unit` | Verify that `pom.xml` contains only the current `release` Maven profile. | `-` |
| 4 | `com.markitdown.build.ProfileConfigurationTest` | `usesShadePackagingAndDoesNotDependOnTess4j` | `Automated / build guard / unit` | Verify shaded release packaging and verify that `pom.xml` does not contain `tess4j`. | `-` |
| 5 | `com.markitdown.cli.BatchConversionRunnerTest` | `runSequentialShouldReportProgressAndCounts` | `Automated / CLI / unit` | Verify sequential batch execution progress, success counting, and failure counting. | `-` |
| 6 | `com.markitdown.cli.BatchConversionRunnerTest` | `runParallelShouldProcessAllFilesAndCollectCounts` | `Automated / CLI / unit` | Verify parallel batch execution covers all inputs and collects counts correctly. | `-` |
| 7 | `com.markitdown.cli.BatchConversionRunnerTest` | `noOpProgressListenerShouldBeSafe` | `Automated / CLI / unit` | Verify that the no-op progress listener does not break batch execution. | `-` |
| 8 | `com.markitdown.cli.CliConfigurationOverridesTest` | `shouldApplySelectedCliOverridesWithoutDroppingYamlValues` | `Automated / CLI / unit` | Verify selected CLI overrides replace targeted fields while preserving YAML-derived values and source labels. | `-` |
| 9 | `com.markitdown.cli.CliConfigurationOverridesTest` | `shouldRespectNegativeBooleanFlags` | `Automated / CLI / unit` | Verify negative CLI flags disable images, tables, metadata, and enable quiet mode. | `-` |
| 10 | `com.markitdown.cli.CliConfigurationOverridesTest` | `largeFileShouldForceUnlimitedMaxFileSize` | `Automated / CLI / unit` | Verify that `--large-file` forces unlimited max file size even when a size limit was also specified. | `-` |
| 11 | `com.markitdown.cli.ConfigCommandsTest` | `showConfigUsesExplicitYamlPath` | `Automated / CLI / integration` | Verify `--show-config` output for an explicit YAML file, including effective values, summaries, value sources, and next-step text. | `-` |
| 12 | `com.markitdown.cli.ConfigCommandsTest` | `showConfigShouldTrackCliOverridesWithoutOverwritingYamlDefaults` | `Automated / CLI / integration` | Verify `--show-config` keeps YAML defaults while reflecting CLI OCR overrides and correct source labels. | `-` |
| 13 | `com.markitdown.cli.ConfigCommandsTest` | `diagnosticCommandsShouldBeMutuallyExclusive` | `Automated / CLI / integration` | Verify incompatible diagnostic commands fail when combined. | `-` |
| 14 | `com.markitdown.cli.ConfigCommandsTest` | `validateConfigShouldPrintNextStepsOnSuccess` | `Automated / CLI / integration` | Verify success output and next-step guidance for valid configuration files. | `-` |
| 15 | `com.markitdown.cli.ConfigCommandsTest` | `validateConfigShouldPrintNextStepsOnFailure` | `Automated / CLI / integration` | Verify failure output and next-step guidance for invalid configuration files. | `-` |
| 16 | `com.markitdown.cli.ConfigCommandsTest` | `explicitConfigShouldBeCachedForCommandLifetime` | `Automated / CLI / integration` | Verify that the effective configuration manager is cached for a single command instance. | `-` |
| 17 | `com.markitdown.cli.ConfigCommandsTest` | `showConfigShouldFailWhenExplicitConfigPathDoesNotExist` | `Automated / CLI / integration` | Verify failure behavior when `--config-path` points to a missing file. | `-` |
| 18 | `com.markitdown.cli.InputDiscoveryHelperTest` | `shouldRecognizeHttpAndHttpsUrls` | `Automated / CLI / unit` | Verify that only HTTP and HTTPS inputs are treated as remote URLs. | `-` |
| 19 | `com.markitdown.cli.InputDiscoveryHelperTest` | `shouldExpandWildcardAndKeepOnlySupportedFiles` | `Automated / CLI / unit` | Verify wildcard expansion and filtering by supported file predicate. | `-` |
| 20 | `com.markitdown.cli.InputDiscoveryHelperTest` | `shouldCollectSupportedFilesFromDirectoryRecursively` | `Automated / CLI / unit` | Verify recursive directory scanning and supported-file counting. | `-` |
| 21 | `com.markitdown.cli.InputDiscoveryHelperTest` | `shouldWarnWhenDirectoryInputRequiresFlags` | `Automated / CLI / unit` | Verify warning behavior when directory input is used without `--recursive` or `--batch`. | `-` |
| 22 | `com.markitdown.cli.InputDiscoveryHelperTest` | `shouldCollectMixedInputsIncludingRemoteWildcardAndDirectory` | `Automated / CLI / unit` | Verify mixed input collection across remote URLs, wildcards, and directories. | `-` |
| 23 | `com.markitdown.cli.MarkItDownCommandFormatsTest` | `listFormatsShouldPrintUserFacingFormatTable` | `Automated / CLI / integration` | Verify user-facing format listing output, including format names, extensions, MIME types, and converter names. | `-` |
| 24 | `com.markitdown.cli.MarkItDownCommandFormatsTest` | `fileTypeDetectorShouldRecognizeWebp` | `Automated / CLI / integration` | Verify WebP MIME detection. | `-` |
| 25 | `com.markitdown.cli.MarkItDownCommandFormatsTest` | `commandShouldConvertHttpUrlToMarkdownFile` | `Automated / CLI / integration` | Verify end-to-end CLI conversion from an HTTP text URL into a Markdown output file. | `-` |
| 26 | `com.markitdown.cli.MarkItDownCommandFormatsTest` | `conversionShouldFailWhenExplicitConfigPathDoesNotExist` | `Automated / CLI / integration` | Verify conversion failure and error messaging when an explicit config path is missing. | `-` |
| 27 | `com.markitdown.cli.MarkItDownCommandFormatsTest` | `commandShouldPreserveLeadingBytesWhenMimeTypeIsDetectedFromPipeInput` | `Automated / CLI / integration` | Verify stdin MIME detection does not consume leading bytes before conversion. | `-` |
| 28 | `com.markitdown.cli.MarkItDownCommandFormatsTest` | `commandShouldPreferContentDispositionFileNameForRemoteInputs` | `Automated / CLI / integration` | Verify remote output naming prefers `Content-Disposition` file names. | `-` |
| 29 | `com.markitdown.cli.MarkItDownCommandFormatsTest` | `commandShouldWriteSingleLocalFileToExplicitOutputFile` | `Automated / CLI / integration` | Verify explicit single-file output path handling. | `-` |
| 30 | `com.markitdown.cli.MarkItDownCommandFormatsTest` | `commandShouldWriteBatchOutputsUsingSourceFileNamesPlusMarkdownExtension` | `Automated / CLI / integration` | Verify batch output naming uses `source.ext.md` and verify batch summary output. | `-` |
| 31 | `com.markitdown.cli.MarkItDownCommandFormatsTest` | `examplesCommandShouldPrintUsageExamplesAndExit` | `Automated / CLI / integration` | Verify `--examples` output and exit behavior. | `-` |
| 32 | `com.markitdown.cli.MarkItDownHelpTest` | `helpShouldShowTaskOrientedSectionsAndOptionOrder` | `Automated / CLI / integration` | Verify help output structure, task-oriented sections, and expected option ordering. | `-` |
| 33 | `com.markitdown.cli.OutputPathHelperTest` | `shouldResolveDirectoryOutputToMarkdownFileName` | `Automated / CLI / unit` | Verify directory output resolves to `<source>.md`. | `-` |
| 34 | `com.markitdown.cli.OutputPathHelperTest` | `shouldResolveDefaultRemoteOutputIntoWorkingDirectory` | `Automated / CLI / unit` | Verify default remote output is written to the current working directory. | `-` |
| 35 | `com.markitdown.cli.OutputPathHelperTest` | `shouldResolveDefaultLocalOutputBesideInput` | `Automated / CLI / unit` | Verify default local output is written beside the source file. | `-` |
| 36 | `com.markitdown.cli.OutputPathHelperTest` | `shouldTreatTrailingSeparatorAsDirectoryOutputEvenWhenDirectoryDoesNotExist` | `Automated / CLI / unit` | Verify trailing path separators force directory interpretation. | `-` |
| 37 | `com.markitdown.cli.OutputPathHelperTest` | `shouldKeepExplicitOutputFilePathUnchanged` | `Automated / CLI / unit` | Verify explicit file output paths are preserved as-is. | `-` |
| 38 | `com.markitdown.cli.OutputPathHelperTest` | `shouldCreateParentDirectoriesWhenWritingResult` | `Automated / CLI / unit` | Verify parent directories are created before writing conversion results. | `-` |
| 39 | `com.markitdown.cli.PipeInputHelperTest` | `shouldUseExplicitMimeTypeWithoutDetection` | `Automated / CLI / unit` | Verify explicit MIME type bypasses detection. | `-` |
| 40 | `com.markitdown.cli.PipeInputHelperTest` | `shouldDetectPlainTextWithoutConsumingStream` | `Automated / CLI / unit` | Verify plain-text pipe detection preserves stream content. | `-` |
| 41 | `com.markitdown.cli.PipeInputHelperTest` | `shouldDetectJsonFromHeader` | `Automated / CLI / unit` | Verify JSON header detection. | `-` |
| 42 | `com.markitdown.cli.PipeInputHelperTest` | `shouldReturnNullForUnknownBinaryHeader` | `Automated / CLI / unit` | Verify unknown binary headers return no inferred MIME type. | `-` |
| 43 | `com.markitdown.cli.RemoteInputHelperTest` | `shouldPreferEncodedContentDispositionFileName` | `Automated / CLI / unit` | Verify RFC 5987 encoded `Content-Disposition` file names are preferred. | `-` |
| 44 | `com.markitdown.cli.RemoteInputHelperTest` | `shouldFallBackToUriPathWhenContentDispositionIsMissing` | `Automated / CLI / unit` | Verify URI path fallback naming. | `-` |
| 45 | `com.markitdown.cli.RemoteInputHelperTest` | `shouldSanitizeUnsafeRemoteNames` | `Automated / CLI / unit` | Verify sanitization of unsafe remote file names. | `-` |
| 46 | `com.markitdown.cli.RemoteInputHelperTest` | `shouldUseFallbackNameWhenUriHasNoFileSegment` | `Automated / CLI / unit` | Verify fallback naming when the URL path does not contain a file segment. | `-` |
| 47 | `com.markitdown.cli.RemoteInputHelperTest` | `shouldKeepKnownExtensionWhenAlreadyPresent` | `Automated / CLI / unit` | Verify known file extensions are preserved. | `-` |
| 48 | `com.markitdown.cli.UserMessageHelperTest` | `shouldFormatConfigurationErrorsWithDedicatedHeading` | `Automated / CLI / unit` | Verify configuration-path errors are formatted with the expected heading and guidance. | `-` |
| 49 | `com.markitdown.cli.UserMessageHelperTest` | `shouldFormatRemoteInputErrorsWithDedicatedHeading` | `Automated / CLI / unit` | Verify remote-download failures are formatted with remote-input guidance. | `-` |
| 50 | `com.markitdown.cli.UserMessageHelperTest` | `shouldFormatUnavailableOcrErrorsWithDedicatedHeading` | `Automated / CLI / unit` | Verify unavailable OCR errors are formatted with OCR setup guidance. | `-` |
| 51 | `com.markitdown.cli.UserMessageHelperTest` | `shouldFormatOcrExecutionErrorsWithDedicatedHeading` | `Automated / CLI / unit` | Verify OCR execution failures are formatted with OCR troubleshooting guidance. | `-` |
| 52 | `com.markitdown.cli.UserMessageHelperTest` | `shouldFormatFileSizeErrorsWithDedicatedHeading` | `Automated / CLI / unit` | Verify file-size limit failures are formatted with `--large-file` guidance. | `-` |
| 53 | `com.markitdown.cli.UserMessageHelperTest` | `shouldKeepConversionContextForGenericConversionFailures` | `Automated / CLI / unit` | Verify generic conversion failures preserve file and converter context. | `-` |
| 54 | `com.markitdown.config.ConfigurationManagerTest` | `yamlConfigurationShouldOverrideDefaultsAndLocalYamlShouldWin` | `Automated / config / unit` | Verify YAML loading precedence and local YAML override behavior. | `-` |
| 55 | `com.markitdown.config.ConfigurationManagerTest` | `yamlOnlyConfigurationShouldIgnoreNonYamlFiles` | `Automated / config / unit` | Verify non-YAML configuration files are ignored. | `-` |
| 56 | `com.markitdown.config.ConfigurationManagerTest` | `explicitConfigPathShouldLoadWithoutProjectDiscovery` | `Automated / config / unit` | Verify explicit config loading works without project discovery. | `-` |
| 57 | `com.markitdown.config.ConfigurationManagerTest` | `createConversionOptionsFromConfigShouldExposeTypedConfigurationFields` | `Automated / config / unit` | Verify configuration is converted into typed `ConversionOptions`. | `-` |
| 58 | `com.markitdown.config.ConfigurationManagerTest` | `effectiveConfigurationShouldExposeTypedValuesAndSources` | `Automated / config / unit` | Verify effective configuration exposes typed values and source labels. | `-` |
| 59 | `com.markitdown.config.ConfigurationManagerTest` | `typedOverridesShouldTrackCliSourcesWithoutStringKeys` | `Automated / config / unit` | Verify typed CLI overrides are tracked as CLI-derived values. | `-` |
| 60 | `com.markitdown.config.ConfigurationManagerTest` | `effectiveConfigurationShouldBeRebuiltAfterTypedOverride` | `Automated / config / unit` | Verify effective configuration is rebuilt after typed overrides are applied. | `-` |
| 61 | `com.markitdown.config.ConfigurationManagerTest` | `propertySourceShouldTrackExplicitYamlOverridesAndDefaults` | `Automated / config / unit` | Verify property-source labeling for defaults and explicit YAML overrides. | `-` |
| 62 | `com.markitdown.config.ConfigurationManagerTest` | `nonYamlExplicitConfigShouldBeRejected` | `Automated / config / unit` | Verify non-YAML explicit config files are rejected. | `-` |
| 63 | `com.markitdown.config.ConfigurationManagerTest` | `validateConfigurationShouldReportSemanticErrors` | `Automated / config / unit` | Verify semantic validation errors are reported. | `-` |
| 64 | `com.markitdown.config.ConfigurationManagerTest` | `validateConfigurationShouldReportYamlParseFailures` | `Automated / config / unit` | Verify YAML parse failures are reported. | `-` |
| 65 | `com.markitdown.config.ConfigurationManagerTest` | `validateConfigurationShouldReportUnknownTopLevelSectionAndUnknownKey` | `Automated / config / unit` | Verify unknown sections and unknown keys are reported. | `-` |
| 66 | `com.markitdown.config.ConfigurationManagerTest` | `validateConfigurationShouldAllowProviderSpecificKeys` | `Automated / config / unit` | Verify provider-specific configuration keys are accepted. | `-` |
| 67 | `com.markitdown.converters.AudioConverterOptionsTest` | `omitsMetadataSectionWhenIncludeMetadataIsFalse` | `Automated / converter / unit` | Verify audio metadata is omitted when metadata output is disabled. | `-` |
| 68 | `com.markitdown.converters.AudioConverterOptionsTest` | `includesMetadataSectionWhenIncludeMetadataIsTrue` | `Automated / converter / unit` | Verify audio metadata is emitted when metadata output is enabled. | `-` |
| 69 | `com.markitdown.converters.AudioConverterOptionsTest` | `resolvesConfiguredEndpointAndModelWithDefaults` | `Automated / converter / unit` | Verify configured OCR endpoint and model resolution with defaults. | `-` |
| 70 | `com.markitdown.converters.DocxConverterTest` | `emitsEnglishMetadataAndFormattedParagraphs` | `Automated / converter / unit` | Verify DOCX conversion emits English metadata labels and formatted paragraph content. | `-` |
| 71 | `com.markitdown.converters.DocxConverterTest` | `omitsTableWhenIncludeTablesIsFalse` | `Automated / converter / unit` | Verify DOCX table output is suppressed when table output is disabled. | `-` |
| 72 | `com.markitdown.converters.ImageConverterOptionsTest` | `omitsEmbeddedImageWhenIncludeImagesIsFalse` | `Automated / converter / unit` | Verify embedded image output is omitted when image output is disabled. | `-` |
| 73 | `com.markitdown.converters.ImageConverterOptionsTest` | `rendersHtmlImageWhenImageFormatIsHtml` | `Automated / converter / unit` | Verify HTML image output mode. | `-` |
| 74 | `com.markitdown.converters.ImageConverterOptionsTest` | `emitsEnglishMetadataKeysWhenMetadataIsEnabled` | `Automated / converter / unit` | Verify English metadata keys are emitted when metadata output is enabled. | `-` |
| 75 | `com.markitdown.converters.ImageConverterOptionsTest` | `emitsUnifiedFileHeader` | `Automated / converter / unit` | Verify image conversion emits the expected unified file header. | `-` |
| 76 | `com.markitdown.converters.PdfConverterFormattingTest` | `appliesRulePageBreakModeToExtractedText` | `Automated / converter / unit` | Verify rule-based page-break formatting for extracted PDF text. | `-` |
| 77 | `com.markitdown.converters.PdfConverterFormattingTest` | `appliesNonePageBreakModeToExtractedText` | `Automated / converter / unit` | Verify no page-break markers are emitted when page-break mode is disabled. | `-` |
| 78 | `com.markitdown.converters.PdfConverterFormattingTest` | `normalizesCommonPdfBulletArtifacts` | `Automated / converter / unit` | Verify normalization of common PDF bullet artifacts. | `-` |
| 79 | `com.markitdown.converters.PdfConverterFormattingTest` | `rendersEnglishFallbackWhenPdfTextCannotBeExtracted` | `Automated / converter / unit` | Verify fallback text is emitted when PDF text extraction fails. | `-` |
| 80 | `com.markitdown.converters.PptConverterTest` | `emitsFriendlyPresentationMetadataAndSlideContent` | `Automated / converter / unit` | Verify PPT conversion emits presentation metadata and slide content. | `-` |
| 81 | `com.markitdown.converters.PptConverterTest` | `omitsPresentationMetadataSectionWhenDisabled` | `Automated / converter / unit` | Verify PPT metadata section is omitted when metadata output is disabled. | `-` |
| 82 | `com.markitdown.converters.PptxConverterTest` | `emitsFriendlyPresentationMetadataAndSlideContent` | `Automated / converter / unit` | Verify PPTX conversion emits presentation metadata and slide content. | `-` |
| 83 | `com.markitdown.converters.PptxConverterTest` | `omitsPresentationMetadataSectionWhenDisabled` | `Automated / converter / unit` | Verify PPTX metadata section is omitted when metadata output is disabled. | `-` |
| 84 | `com.markitdown.converters.TextConverterStreamingTest` | `convertsPlainTextStream` | `Automated / converter / unit` | Verify stream-based plain-text conversion. | `-` |
| 85 | `com.markitdown.converters.TextConverterStreamingTest` | `convertsJsonStream` | `Automated / converter / unit` | Verify stream-based JSON conversion. | `-` |
| 86 | `com.markitdown.converters.TextConverterStreamingTest` | `usesTypedSourceFileNameWhenProvided` | `Automated / converter / unit` | Verify typed source file name is preserved in conversion behavior. | `-` |
| 87 | `com.markitdown.converters.TextConverterStreamingTest` | `omitsCsvTableWhenTablesAreDisabled` | `Automated / converter / unit` | Verify CSV table output is omitted when table output is disabled. | `-` |
| 88 | `com.markitdown.converters.TextConverterStreamingTest` | `rendersMarkdownStyleCsvTableWithoutOuterPipes` | `Automated / converter / unit` | Verify Markdown-style CSV table rendering without outer pipes. | `-` |
| 89 | `com.markitdown.converters.XlsxConverterTest` | `usesFriendlyDefaultColumnNamesWhenHeaderIsNotDetected` | `Automated / converter / unit` | Verify default friendly column naming when spreadsheet headers are not detected. | `-` |
| 90 | `com.markitdown.converters.XlsxConverterTest` | `usesMarkdownTableFormatWithoutOuterPipesWhenConfigured` | `Automated / converter / unit` | Verify configured Markdown table output format for XLSX conversion. | `-` |
| 91 | `com.markitdown.converters.ZipConverterTest` | `convertsNestedTextAndJsonEntries` | `Automated / converter / unit` | Verify ZIP conversion processes nested text and JSON entries. | `-` |
| 92 | `com.markitdown.core.ConverterRegistryTest` | `reportsSupportedMimeTypesFromConverterCapabilities` | `Automated / core / unit` | Verify registry-supported MIME reporting is derived from converter capability declarations. | `-` |
| 93 | `com.markitdown.core.MarkItDownEngineTest` | `convertShouldRejectMissingFilesBeforeConversion` | `Automated / core / unit` | Verify missing files are rejected before conversion starts. | `-` |
| 94 | `com.markitdown.core.MarkItDownEngineTest` | `convertShouldReturnFailedResultWhenMimeTypeIsUnsupported` | `Automated / core / unit` | Verify unsupported MIME types produce a failed conversion result. | `-` |
| 95 | `com.markitdown.core.MarkItDownEngineTest` | `streamConversionShouldFailWhenMatchingConverterDoesNotSupportStreaming` | `Automated / core / unit` | Verify stream conversion fails when the matched converter does not support stream input. | `-` |
| 96 | `com.markitdown.core.MarkItDownEngineTest` | `supportChecksShouldUseRegistryForMimeTypesAndDetectedFileTypes` | `Automated / core / unit` | Verify support checks use registry lookups for explicit MIME types and detected file types. | `-` |
| 97 | `com.markitdown.ocr.OcrEngineFactoryTest` | `returnsUnavailableEngineWhenOcrIsDisabled` | `Automated / OCR / unit` | Verify OCR-disabled configuration returns an unavailable OCR engine. | `-` |
| 98 | `com.markitdown.ocr.OcrEngineFactoryTest` | `returnsUnavailableEngineForUnknownProvider` | `Automated / OCR / unit` | Verify unknown OCR provider names return an unavailable OCR engine. | `-` |
| 99 | `com.markitdown.ocr.OcrEngineFactoryTest` | `returnsMockEngineWhenMockProviderIsSelected` | `Automated / OCR / unit` | Verify the mock OCR provider returns a working mock engine. | `-` |
| 100 | `com.markitdown.ocr.OcrEngineFactoryTest` | `returnsUnavailableEngineWhenPaddleTokenIsMissing` | `Automated / OCR / unit` | Verify Paddle OCR is unavailable when required token configuration is missing. | `-` |
| 101 | `com.markitdown.ocr.OcrEngineFactoryTest` | `createsPaddleEngineWhenTokenIsConfigured` | `Automated / OCR / unit` | Verify Paddle OCR engine creation succeeds when required configuration is present. | `-` |
| 102 | `com.markitdown.ocr.PaddleOcrEngineTest` | `extractsMarkdownTextFromJsonlPayload` | `Automated / OCR / unit` | Verify Paddle OCR JSONL payload parsing extracts Markdown text correctly. | `-` |
| 103 | `com.markitdown.ocr.TesseractCliOcrProviderTest` | `providerShouldPassBothExecutableAndTessdataPaths` | `Automated / OCR / unit` | Verify the Tesseract CLI provider passes both executable path and `tessdata` path into the created OCR engine. | `-` |
| 104 | `com.markitdown.utils.FileTypeDetectorTest` | `rejectsNullAndBlankExtensions` | `Automated / utility / unit` | Verify null and blank file extensions are rejected. | `-` |
| 105 | `com.markitdown.utils.FileTypeDetectorTest` | `exposesKnownMimeTypesUsedByRegistryLookups` | `Automated / utility / unit` | Verify known MIME types are exposed for registry lookup use. | `-` |

