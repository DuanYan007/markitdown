# 测试

本文只列出当前版本的测试清单。

当前仓库交付的 CLI 产物为 `target/markitdown4j-<version>.jar`。本文件“测试命令”列中的 `markitdown4j` 是 `java -jar target/markitdown4j-<version>.jar` 的文档简写。如果你没有自定义别名或包装脚本，请直接使用 `java -jar ...`。

## 功能测试项

| 序号 | 测试项 | 测试类型 | 测试内容 | 测试命令 |
| --- | --- | --- | --- | --- |
| 1 | `show-config-explicit-yaml` | `功能 / 标准 / 配置` | 加载 `verification/manual/markitdown.release.yml` 并输出当前有效配置。 | `markitdown4j --show-config --config-path verification/manual/markitdown.release.yml` |
| 2 | `validate-config-success` | `功能 / 标准 / 配置` | 校验发布配置文件，并确认配置通过校验。 | `markitdown4j --validate-config --config-path verification/manual/markitdown.release.yml` |
| 3 | `validate-config-failure` | `功能 / 标准 / 配置` | 校验一份故意构造的非法 YAML，并确认命令失败。 | `markitdown4j --validate-config --config-path invalid.yml` |
| 4 | `show-config-cli-override` | `功能 / 标准 / 配置` | 在 YAML 基础上叠加 CLI OCR 覆盖项，并确认有效配置显示 CLI 来源。 | `markitdown4j --show-config --config-path verification/manual/markitdown.release.yml --ocr --ocr-engine http` |
| 5 | `generate-config` | `功能 / 标准 / 配置` | 生成默认 `markitdown.yml`，并确认包含 `ocr`、`format` 等预期 section。 | `markitdown4j --generate-config` |
| 6 | `convert-basic-txt` | `功能 / 标准 / 文件转换` | 转换 `test/basic.txt`，并确认生成非空 Markdown 输出。 | `markitdown4j test/basic.txt -o out/basic.txt.md` |
| 7 | `convert-basic-json` | `功能 / 标准 / 文件转换` | 转换 `test/basic.json`，并确认生成非空 Markdown 输出。 | `markitdown4j test/basic.json -o out/basic.json.md` |
| 8 | `convert-basic-html` | `功能 / 标准 / 文件转换` | 转换 `test/basic.html`，并确认生成非空 Markdown 输出。 | `markitdown4j test/basic.html -o out/basic.html.md` |
| 9 | `convert-basic-docx` | `功能 / 标准 / 文件转换` | 转换 `test/basic.docx`，并确认生成非空 Markdown 输出。 | `markitdown4j test/basic.docx -o out/basic.docx.md` |
| 10 | `convert-basic-pptx` | `功能 / 标准 / 文件转换` | 转换 `test/basic.pptx`，并确认生成非空 Markdown 输出。 | `markitdown4j test/basic.pptx -o out/basic.pptx.md` |
| 11 | `convert-basic-xlsx` | `功能 / 标准 / 文件转换` | 转换 `test/basic.xlsx`，并确认生成非空 Markdown 输出。 | `markitdown4j test/basic.xlsx -o out/basic.xlsx.md` |
| 12 | `convert-plain-text-pdf` | `功能 / 标准 / 文件转换` | 转换 `test/plain-text.pdf`，并确认生成非空 Markdown 输出。 | `markitdown4j test/plain-text.pdf -o out/plain-text.pdf.md` |
| 13 | `convert-nested-zip` | `功能 / 标准 / 文件转换` | 转换 `test/nested.zip`，并确认生成非空 Markdown 输出。 | `markitdown4j test/nested.zip -o out/nested.zip.md` |
| 14 | `convert-sample-png` | `功能 / 标准 / 文件转换` | 转换 `test/sample.png`，并确认生成非空 Markdown 输出。 | `markitdown4j test/sample.png -o out/sample.png.md` |
| 15 | `convert-sample-mp3` | `功能 / 标准 / 文件转换` | 转换 `test/sample.mp3`，并确认生成非空 Markdown 输出。 | `markitdown4j test/sample.mp3 -o out/sample.mp3.md` |
| 16 | `convert-with-images-docx` | `功能 / 标准 / 文件转换` | 转换 `test/with-images.docx`，并确认生成非空 Markdown 输出。 | `markitdown4j test/with-images.docx -o out/with-images.docx.md` |
| 17 | `convert-with-images-html` | `功能 / 标准 / 文件转换` | 转换 `test/with-images.html`，并确认生成非空 Markdown 输出。 | `markitdown4j test/with-images.html -o out/with-images.html.md` |
| 18 | `convert-with-images-xlsx` | `功能 / 标准 / 文件转换` | 转换 `test/with-images.xlsx`，并确认生成非空 Markdown 输出。 | `markitdown4j test/with-images.xlsx -o out/with-images.xlsx.md` |
| 19 | `convert-with-notes-pptx` | `功能 / 标准 / 文件转换` | 转换 `test/with-notes.pptx`，并确认生成非空 Markdown 输出。 | `markitdown4j test/with-notes.pptx -o out/with-notes.pptx.md` |
| 20 | `convert-multi-sheet-xlsx` | `功能 / 标准 / 文件转换` | 转换 `test/multi-sheet.xlsx`，并确认生成非空 Markdown 输出。 | `markitdown4j test/multi-sheet.xlsx -o out/multi-sheet.xlsx.md` |
| 21 | `convert-old-format-xls` | `功能 / 标准 / 文件转换` | 转换 `test/old-format.xls`，并确认生成非空 Markdown 输出。 | `markitdown4j test/old-format.xls -o out/old-format.xls.md` |
| 22 | `convert-complex-nested-zip` | `功能 / 标准 / 文件转换` | 转换 `test/complex-nested.zip`，并确认生成非空 Markdown 输出。 | `markitdown4j test/complex-nested.zip -o out/complex-nested.zip.md` |
| 23 | `convert-plain-text-chinese-pdf` | `功能 / 标准 / 文件转换` | 转换 `test/plain-text-chinese.pdf`，并确认生成非空 Markdown 输出。 | `markitdown4j test/plain-text-chinese.pdf -o out/plain-text-chinese.pdf.md` |
| 24 | `convert-100-pages-pdf` | `功能 / 标准 / 文件转换` | 转换 `test/100-pages.pdf`，并确认生成非空 Markdown 输出。 | `markitdown4j test/100-pages.pdf -o out/100-pages.pdf.md` |
| 25 | `convert-1000-pages-docx` | `功能 / 标准 / 文件转换` | 转换 `test/1000-pages.docx`，并确认生成非空 Markdown 输出。 | `markitdown4j test/1000-pages.docx -o out/1000-pages.docx.md` |
| 26 | `convert-10000-rows-xlsx` | `功能 / 标准 / 文件转换` | 转换 `test/10000-rows.xlsx`，并确认生成非空 Markdown 输出。 | `markitdown4j test/10000-rows.xlsx -o out/10000-rows.xlsx.md` |
| 27 | `convert-large-archive-zip` | `功能 / 标准 / 文件转换` | 转换 `test/large-archive.zip`，并确认生成非空 Markdown 输出。 | `markitdown4j test/large-archive.zip -o out/large-archive.zip.md` |
| 28 | `convert-with-media-pptx` | `功能 / 标准 / 文件转换` | 转换 `test/with-media.pptx`，并确认生成非空 Markdown 输出。 | `markitdown4j test/with-media.pptx -o out/with-media.pptx.md` |
| 29 | `convert-large-dataset-xlsx` | `功能 / 标准 / 文件转换` | 转换 `test/large-dataset.xlsx`，并确认生成非空 Markdown 输出。 | `markitdown4j test/large-dataset.xlsx -o out/large-dataset.xlsx.md` |
| 30 | `single-file-output-directory` | `功能 / 标准 / 输出路径` | 单文件输出到目录目标，确认结果文件命名为 `<source>.md`。 | `markitdown4j test/basic.txt -o out/dir/` |
| 31 | `batch-directory-conversion` | `功能 / 标准 / 输出路径` | 使用 `--batch` 转换 `verification/fixtures/batch`，确认两个输出文件都被创建。 | `markitdown4j verification/fixtures/batch --batch -o out/batch/` |
| 32 | `stdin-text-conversion` | `功能 / 标准 / 标准输入` | 通过管道输入纯文本，并确认 Markdown 文本输出到标准输出。 | `Get-Content -Raw input.txt \| markitdown4j` |
| 33 | `stdin-json-explicit-mime` | `功能 / 标准 / 标准输入` | 通过管道输入 JSON，并显式指定 MIME，确认输出符合预期。 | `'{"hello":"world"}' \| markitdown4j --mime-type application/json` |
| 34 | `docx-no-tables-option` | `功能 / 标准 / 选项行为` | 转换 `test/with-tables.docx` 时加 `--no-tables`，确认结果相较默认转换有所收敛。 | `markitdown4j test/with-tables.docx --no-tables -o out/with-tables.no-tables.md` |
| 35 | `image-html-format-option` | `功能 / 标准 / 选项行为` | 转换 `test/sample.png` 时使用 `--image-format html`，确认输出包含 HTML `<img>`。 | `markitdown4j test/sample.png --image-format html -o out/sample.image-html.md` |
| 36 | `pptx-no-metadata-option` | `功能 / 标准 / 选项行为` | 转换 `test/basic.pptx` 时加 `--no-metadata`，确认输出相较默认转换有所收敛。 | `markitdown4j test/basic.pptx --no-metadata -o out/basic.pptx.no-metadata.md` |
| 37 | `xlsx-markdown-table-option` | `功能 / 标准 / 选项行为` | 转换 `test/basic.xlsx` 时使用 `--table-format markdown`，确认输出为 Markdown 风格表格。 | `markitdown4j test/basic.xlsx --table-format markdown -o out/basic.markdown-table.xlsx.md` |
| 38 | `ocr-tesseract-cli-png` | `功能 / 标准 / OCR` | 转换 `test/with-text.png` 并启用 OCR，确认 Tesseract CLI 能提取预期标记文本。 | `markitdown4j test/with-text.png --ocr --config-path ocr.yml -o out/ocr/` |
| 39 | `ocr-scanned-pdf` | `功能 / 标准 / OCR` | 转换 `test/scanned.pdf` 并启用 OCR，确认生成非空 Markdown 输出。 | `markitdown4j test/scanned.pdf --ocr --config-path ocr.yml -o out/scanned.pdf.md` |
| 40 | `old-format-doc-failure` | `功能 / 标准 / 失败路径` | 转换 `test/old-format.doc`，确认当前 OLE2 或 OOXML 不匹配路径按预期失败。 | `markitdown4j test/old-format.doc -o out/old-format.doc.md` |
| 41 | `encrypted-pdf-warning-path` | `功能 / 标准 / 警告路径` | 转换 `test/encrypted.pdf`，确认命令完成并出现当前解密警告路径。 | `markitdown4j test/encrypted.pdf -o out/encrypted.pdf.md` |
| 42 | `password-protected-pdf-warning-path` | `功能 / 标准 / 警告路径` | 转换 `test/password-protected.pdf`，确认命令完成并出现当前解密警告路径。 | `markitdown4j test/password-protected.pdf -o out/password-protected.pdf.md` |
| 43 | `huge-pdf-default-limit` | `功能 / 压力 / 大文件` | 转换 `test/huge-pdf.pdf` 且不加 `--large-file`，确认默认 `50 MB` 限制会拒绝该文件。 | `markitdown4j test/huge-pdf.pdf -o out/huge-pdf.default.md` |
| 44 | `huge-pdf-with-large-file` | `功能 / 压力 / 大文件` | 转换 `test/huge-pdf.pdf` 并加 `--large-file`，确认能成功输出 Markdown。 | `markitdown4j test/huge-pdf.pdf --large-file -o out/huge-pdf.large.md` |
| 45 | `mixed-formats-default-limit` | `功能 / 压力 / 大文件` | 转换 `test/mixed-formats.zip` 且不加 `--large-file`，确认默认 `50 MB` 限制会拒绝该文件。 | `markitdown4j test/mixed-formats.zip -o out/mixed-formats.default.md` |
| 46 | `mixed-formats-with-large-file` | `功能 / 压力 / 大文件` | 转换 `test/mixed-formats.zip` 并加 `--large-file`，确认整体成功且保留 ZIP 条目警告路径信息。 | `markitdown4j test/mixed-formats.zip --large-file -o out/mixed-formats.large.md` |

## 自动化单元测试与集成测试项

| 序号 | 测试套件 | 测试项 | 测试类型 | 测试内容 | 测试命令 |
| --- | --- | --- | --- | --- | --- |
| 1 | `com.markitdown.build.CodeStyleGuardTest` | `productionSourcesShouldNotUseWildcardImports` | `自动化 / 构建守卫 / 单元` | 校验生产代码中不存在通配符 import。 | `-` |
| 2 | `com.markitdown.build.CodeStyleGuardTest` | `javaSourcesShouldNotContainTabsOrTrailingWhitespace` | `自动化 / 构建守卫 / 单元` | 校验 Java 源码不存在制表符和行尾空白。 | `-` |
| 3 | `com.markitdown.build.ProfileConfigurationTest` | `containsCurrentReleaseProfileOnly` | `自动化 / 构建守卫 / 单元` | 校验 `pom.xml` 只保留当前 `release` Maven profile。 | `-` |
| 4 | `com.markitdown.build.ProfileConfigurationTest` | `usesShadePackagingAndDoesNotDependOnTess4j` | `自动化 / 构建守卫 / 单元` | 校验使用 shaded 打包，且 `pom.xml` 不包含 `tess4j` 依赖。 | `-` |
| 5 | `com.markitdown.cli.BatchConversionRunnerTest` | `runSequentialShouldReportProgressAndCounts` | `自动化 / CLI / 单元` | 校验顺序批量执行时的进度、成功数和失败数统计。 | `-` |
| 6 | `com.markitdown.cli.BatchConversionRunnerTest` | `runParallelShouldProcessAllFilesAndCollectCounts` | `自动化 / CLI / 单元` | 校验并行批量执行能覆盖全部输入并正确收集统计值。 | `-` |
| 7 | `com.markitdown.cli.BatchConversionRunnerTest` | `noOpProgressListenerShouldBeSafe` | `自动化 / CLI / 单元` | 校验空操作进度监听器不会破坏批量执行。 | `-` |
| 8 | `com.markitdown.cli.CliConfigurationOverridesTest` | `shouldApplySelectedCliOverridesWithoutDroppingYamlValues` | `自动化 / CLI / 单元` | 校验选定 CLI 覆盖项会替换目标字段，同时保留 YAML 派生值和来源标记。 | `-` |
| 9 | `com.markitdown.cli.CliConfigurationOverridesTest` | `shouldRespectNegativeBooleanFlags` | `自动化 / CLI / 单元` | 校验负向布尔开关能关闭 images、tables、metadata，并启用 quiet。 | `-` |
| 10 | `com.markitdown.cli.CliConfigurationOverridesTest` | `largeFileShouldForceUnlimitedMaxFileSize` | `自动化 / CLI / 单元` | 校验 `--large-file` 会强制取消文件大小限制，即使同时指定了大小上限。 | `-` |
| 11 | `com.markitdown.cli.ConfigCommandsTest` | `showConfigUsesExplicitYamlPath` | `自动化 / CLI / 集成` | 校验 `--show-config` 在显式 YAML 路径下能输出有效值、摘要、来源和下一步提示。 | `-` |
| 12 | `com.markitdown.cli.ConfigCommandsTest` | `showConfigShouldTrackCliOverridesWithoutOverwritingYamlDefaults` | `自动化 / CLI / 集成` | 校验 `--show-config` 在显示 CLI OCR 覆盖时仍保留 YAML 默认值和正确来源。 | `-` |
| 13 | `com.markitdown.cli.ConfigCommandsTest` | `diagnosticCommandsShouldBeMutuallyExclusive` | `自动化 / CLI / 集成` | 校验互斥诊断命令组合时应失败。 | `-` |
| 14 | `com.markitdown.cli.ConfigCommandsTest` | `validateConfigShouldPrintNextStepsOnSuccess` | `自动化 / CLI / 集成` | 校验配置校验成功时输出下一步操作提示。 | `-` |
| 15 | `com.markitdown.cli.ConfigCommandsTest` | `validateConfigShouldUseExplicitYamlPath` | `自动化 / CLI / 集成` | 校验显式 YAML 路径下的配置校验行为。 | `-` |
| 16 | `com.markitdown.cli.ConfigCommandsTest` | `validateConfigShouldFailForMissingExplicitPath` | `自动化 / CLI / 集成` | 校验显式配置文件缺失时命令失败。 | `-` |
| 17 | `com.markitdown.cli.ConfigCommandsTest` | `generateConfigShouldWriteTemplateAndKeepExistingFileSafe` | `自动化 / CLI / 集成` | 校验配置模板生成成功且不会危险覆盖既有文件。 | `-` |
| 18 | `com.markitdown.cli.InputDiscoveryHelperTest` | `shouldRequireBatchOrRecursiveForDirectoryInput` | `自动化 / CLI / 单元` | 校验目录输入在缺少 `--recursive` 或 `--batch` 时的提示行为。 | `-` |
| 19 | `com.markitdown.cli.InputDiscoveryHelperTest` | `shouldExpandWildcardPatterns` | `自动化 / CLI / 单元` | 校验通配符输入可被正确展开。 | `-` |
| 20 | `com.markitdown.cli.InputDiscoveryHelperTest` | `shouldKeepRemoteUrlsAsInputs` | `自动化 / CLI / 单元` | 校验远程 URL 会作为输入保留。 | `-` |
| 21 | `com.markitdown.cli.InputDiscoveryHelperTest` | `directoryInputRequiresFlags` | `自动化 / CLI / 单元` | 校验目录输入缺失必要标志时的行为。 | `-` |
| 22 | `com.markitdown.cli.InputDiscoveryHelperTest` | `shouldCollectMixedInputsIncludingRemoteWildcardAndDirectory` | `自动化 / CLI / 单元` | 校验混合输入收集，覆盖远程 URL、通配符和目录。 | `-` |
| 23 | `com.markitdown.cli.MarkItDownCommandFormatsTest` | `listFormatsShouldPrintUserFacingFormatTable` | `自动化 / CLI / 集成` | 校验面向用户的格式列表输出，包括格式名、扩展名、MIME 和转换器名称。 | `-` |
| 24 | `com.markitdown.cli.MarkItDownCommandFormatsTest` | `fileTypeDetectorShouldRecognizeWebp` | `自动化 / CLI / 集成` | 校验 WebP MIME 识别。 | `-` |
| 25 | `com.markitdown.cli.MarkItDownCommandFormatsTest` | `commandShouldConvertHttpUrlToMarkdownFile` | `自动化 / CLI / 集成` | 校验从 HTTP 文本 URL 到 Markdown 文件的端到端 CLI 转换。 | `-` |
| 26 | `com.markitdown.cli.MarkItDownCommandFormatsTest` | `conversionShouldFailWhenExplicitConfigPathDoesNotExist` | `自动化 / CLI / 集成` | 校验显式配置路径不存在时转换失败且错误消息正确。 | `-` |
| 27 | `com.markitdown.cli.MarkItDownCommandFormatsTest` | `commandShouldPreserveLeadingBytesWhenMimeTypeIsDetectedFromPipeInput` | `自动化 / CLI / 集成` | 校验 stdin MIME 检测不会消耗前导字节。 | `-` |
| 28 | `com.markitdown.cli.MarkItDownCommandFormatsTest` | `commandShouldPreferContentDispositionFileNameForRemoteInputs` | `自动化 / CLI / 集成` | 校验远程输入输出命名优先使用 `Content-Disposition` 文件名。 | `-` |
| 29 | `com.markitdown.cli.MarkItDownCommandFormatsTest` | `commandShouldWriteSingleLocalFileToExplicitOutputFile` | `自动化 / CLI / 集成` | 校验单文件显式输出文件路径处理。 | `-` |
| 30 | `com.markitdown.cli.MarkItDownCommandFormatsTest` | `commandShouldWriteBatchOutputsUsingSourceFileNamesPlusMarkdownExtension` | `自动化 / CLI / 集成` | 校验批量输出命名采用 `source.ext.md`，并校验批量汇总输出。 | `-` |
| 31 | `com.markitdown.cli.MarkItDownCommandFormatsTest` | `examplesCommandShouldPrintUsageExamplesAndExit` | `自动化 / CLI / 集成` | 校验 `--examples` 输出和退出行为。 | `-` |
| 32 | `com.markitdown.cli.MarkItDownHelpTest` | `helpShouldShowTaskOrientedSectionsAndOptionOrder` | `自动化 / CLI / 集成` | 校验帮助输出结构、任务导向分区和选项顺序。 | `-` |
| 33 | `com.markitdown.cli.OutputPathHelperTest` | `shouldResolveDirectoryOutputToMarkdownFileName` | `自动化 / CLI / 单元` | 校验目录输出会被解析为 `<source>.md`。 | `-` |
| 34 | `com.markitdown.cli.OutputPathHelperTest` | `shouldResolveDefaultRemoteOutputIntoWorkingDirectory` | `自动化 / CLI / 单元` | 校验远程输入默认输出到当前工作目录。 | `-` |
| 35 | `com.markitdown.cli.OutputPathHelperTest` | `shouldResolveDefaultLocalOutputBesideInput` | `自动化 / CLI / 单元` | 校验本地输入默认输出到源文件旁边。 | `-` |
| 36 | `com.markitdown.cli.OutputPathHelperTest` | `shouldTreatTrailingSeparatorAsDirectoryOutputEvenWhenDirectoryDoesNotExist` | `自动化 / CLI / 单元` | 校验路径末尾分隔符会强制按目录输出处理，即使目录尚不存在。 | `-` |
| 37 | `com.markitdown.cli.OutputPathHelperTest` | `shouldKeepExplicitOutputFilePathUnchanged` | `自动化 / CLI / 单元` | 校验显式文件输出路径保持不变。 | `-` |
| 38 | `com.markitdown.cli.OutputPathHelperTest` | `shouldCreateParentDirectoriesWhenWritingResult` | `自动化 / CLI / 单元` | 校验写入结果前会自动创建父目录。 | `-` |
| 39 | `com.markitdown.cli.PipeInputHelperTest` | `shouldUseExplicitMimeTypeWithoutDetection` | `自动化 / CLI / 单元` | 校验显式 MIME 会跳过自动检测。 | `-` |
| 40 | `com.markitdown.cli.PipeInputHelperTest` | `shouldDetectPlainTextWithoutConsumingStream` | `自动化 / CLI / 单元` | 校验纯文本管道输入检测不会消耗流内容。 | `-` |
| 41 | `com.markitdown.cli.PipeInputHelperTest` | `shouldDetectJsonFromHeader` | `自动化 / CLI / 单元` | 校验 JSON 头部识别。 | `-` |
| 42 | `com.markitdown.cli.PipeInputHelperTest` | `shouldReturnNullForUnknownBinaryHeader` | `自动化 / CLI / 单元` | 校验未知二进制头部时不推断 MIME。 | `-` |
| 43 | `com.markitdown.cli.RemoteInputHelperTest` | `shouldPreferEncodedContentDispositionFileName` | `自动化 / CLI / 单元` | 校验优先使用 RFC 5987 编码的 `Content-Disposition` 文件名。 | `-` |
| 44 | `com.markitdown.cli.RemoteInputHelperTest` | `shouldFallBackToUriPathWhenContentDispositionIsMissing` | `自动化 / CLI / 单元` | 校验缺少 `Content-Disposition` 时回退到 URL 路径命名。 | `-` |
| 45 | `com.markitdown.cli.RemoteInputHelperTest` | `shouldSanitizeUnsafeRemoteNames` | `自动化 / CLI / 单元` | 校验远程文件名中的危险字符会被清理。 | `-` |
| 46 | `com.markitdown.cli.RemoteInputHelperTest` | `shouldUseFallbackNameWhenUriHasNoFileSegment` | `自动化 / CLI / 单元` | 校验 URL 路径没有文件段时使用兜底文件名。 | `-` |
| 47 | `com.markitdown.cli.RemoteInputHelperTest` | `shouldKeepKnownExtensionWhenAlreadyPresent` | `自动化 / CLI / 单元` | 校验已存在的已知扩展名会被保留。 | `-` |
| 48 | `com.markitdown.cli.UserMessageHelperTest` | `shouldFormatConfigurationErrorsWithDedicatedHeading` | `自动化 / CLI / 单元` | 校验配置类错误使用专门标题和引导文案。 | `-` |
| 49 | `com.markitdown.cli.UserMessageHelperTest` | `shouldFormatRemoteInputErrorsWithDedicatedHeading` | `自动化 / CLI / 单元` | 校验远程下载失败时使用远程输入指导文案。 | `-` |
| 50 | `com.markitdown.cli.UserMessageHelperTest` | `shouldFormatUnavailableOcrErrorsWithDedicatedHeading` | `自动化 / CLI / 单元` | 校验 OCR 不可用错误使用 OCR 安装指导文案。 | `-` |
| 51 | `com.markitdown.cli.UserMessageHelperTest` | `shouldFormatOcrExecutionErrorsWithDedicatedHeading` | `自动化 / CLI / 单元` | 校验 OCR 执行失败错误使用 OCR 排障文案。 | `-` |
| 52 | `com.markitdown.cli.UserMessageHelperTest` | `shouldFormatFileSizeErrorsWithDedicatedHeading` | `自动化 / CLI / 单元` | 校验文件大小超限错误使用 `--large-file` 指导文案。 | `-` |
| 53 | `com.markitdown.cli.UserMessageHelperTest` | `shouldKeepConversionContextForGenericConversionFailures` | `自动化 / CLI / 单元` | 校验通用转换失败时保留文件和转换器上下文。 | `-` |
| 54 | `com.markitdown.config.ConfigurationManagerTest` | `yamlConfigurationShouldOverrideDefaultsAndLocalYamlShouldWin` | `自动化 / 配置 / 单元` | 校验 YAML 加载优先级和本地 YAML 覆盖行为。 | `-` |
| 55 | `com.markitdown.config.ConfigurationManagerTest` | `yamlOnlyConfigurationShouldIgnoreNonYamlFiles` | `自动化 / 配置 / 单元` | 校验非 YAML 配置文件会被忽略。 | `-` |
| 56 | `com.markitdown.config.ConfigurationManagerTest` | `explicitConfigPathShouldLoadWithoutProjectDiscovery` | `自动化 / 配置 / 单元` | 校验显式配置路径在不依赖项目发现时也能加载。 | `-` |
| 57 | `com.markitdown.config.ConfigurationManagerTest` | `createConversionOptionsFromConfigShouldExposeTypedConfigurationFields` | `自动化 / 配置 / 单元` | 校验配置会被转换为类型化 `ConversionOptions`。 | `-` |
| 58 | `com.markitdown.config.ConfigurationManagerTest` | `effectiveConfigurationShouldExposeTypedValuesAndSources` | `自动化 / 配置 / 单元` | 校验有效配置能暴露类型化值和来源标记。 | `-` |
| 59 | `com.markitdown.config.ConfigurationManagerTest` | `typedOverridesShouldTrackCliSourcesWithoutStringKeys` | `自动化 / 配置 / 单元` | 校验类型化 CLI 覆盖项会被标记为 CLI 来源。 | `-` |
| 60 | `com.markitdown.config.ConfigurationManagerTest` | `effectiveConfigurationShouldBeRebuiltAfterTypedOverride` | `自动化 / 配置 / 单元` | 校验应用类型化覆盖后会重新构建有效配置。 | `-` |
| 61 | `com.markitdown.config.ConfigurationManagerTest` | `propertySourceShouldTrackExplicitYamlOverridesAndDefaults` | `自动化 / 配置 / 单元` | 校验默认值和显式 YAML 覆盖的来源标记。 | `-` |
| 62 | `com.markitdown.config.ConfigurationManagerTest` | `nonYamlExplicitConfigShouldBeRejected` | `自动化 / 配置 / 单元` | 校验显式非 YAML 配置文件会被拒绝。 | `-` |
| 63 | `com.markitdown.config.ConfigurationManagerTest` | `validateConfigurationShouldReportSemanticErrors` | `自动化 / 配置 / 单元` | 校验配置语义错误会被报告。 | `-` |
| 64 | `com.markitdown.config.ConfigurationManagerTest` | `validateConfigurationShouldReportYamlParseFailures` | `自动化 / 配置 / 单元` | 校验 YAML 解析失败会被报告。 | `-` |
| 65 | `com.markitdown.config.ConfigurationManagerTest` | `validateConfigurationShouldReportUnknownTopLevelSectionAndUnknownKey` | `自动化 / 配置 / 单元` | 校验未知顶层 section 和未知 key 会被报告。 | `-` |
| 66 | `com.markitdown.config.ConfigurationManagerTest` | `validateConfigurationShouldAllowProviderSpecificKeys` | `自动化 / 配置 / 单元` | 校验 provider 特定扩展键会被允许。 | `-` |
| 67 | `com.markitdown.converters.AudioConverterOptionsTest` | `omitsMetadataSectionWhenIncludeMetadataIsFalse` | `自动化 / 转换器 / 单元` | 校验关闭 metadata 输出时不会生成音频 metadata section。 | `-` |
| 68 | `com.markitdown.converters.AudioConverterOptionsTest` | `includesMetadataSectionWhenIncludeMetadataIsTrue` | `自动化 / 转换器 / 单元` | 校验启用 metadata 输出时会生成音频 metadata section。 | `-` |
| 69 | `com.markitdown.converters.AudioConverterOptionsTest` | `resolvesConfiguredEndpointAndModelWithDefaults` | `自动化 / 转换器 / 单元` | 校验带默认值的 endpoint 和 model 解析行为。 | `-` |
| 70 | `com.markitdown.converters.DocxConverterTest` | `emitsEnglishMetadataAndFormattedParagraphs` | `自动化 / 转换器 / 单元` | 校验 DOCX 转换会输出英文 metadata 标签和格式化段落内容。 | `-` |
| 71 | `com.markitdown.converters.DocxConverterTest` | `omitsTableWhenIncludeTablesIsFalse` | `自动化 / 转换器 / 单元` | 校验关闭表格输出时不会输出 DOCX 表格。 | `-` |
| 72 | `com.markitdown.converters.ImageConverterOptionsTest` | `omitsEmbeddedImageWhenIncludeImagesIsFalse` | `自动化 / 转换器 / 单元` | 校验关闭图片输出时不会输出嵌入图片内容。 | `-` |
| 73 | `com.markitdown.converters.ImageConverterOptionsTest` | `rendersHtmlImageWhenImageFormatIsHtml` | `自动化 / 转换器 / 单元` | 校验图片 HTML 输出模式。 | `-` |
| 74 | `com.markitdown.converters.ImageConverterOptionsTest` | `emitsEnglishMetadataKeysWhenMetadataIsEnabled` | `自动化 / 转换器 / 单元` | 校验启用 metadata 输出时会输出英文 metadata key。 | `-` |
| 75 | `com.markitdown.converters.ImageConverterOptionsTest` | `emitsUnifiedFileHeader` | `自动化 / 转换器 / 单元` | 校验图片转换会输出统一文件头。 | `-` |
| 76 | `com.markitdown.converters.PdfConverterFormattingTest` | `appliesRulePageBreakModeToExtractedText` | `自动化 / 转换器 / 单元` | 校验提取 PDF 文本时的 rule 分页线格式。 | `-` |
| 77 | `com.markitdown.converters.PdfConverterFormattingTest` | `appliesNonePageBreakModeToExtractedText` | `自动化 / 转换器 / 单元` | 校验关闭分页线模式时不输出分页标记。 | `-` |
| 78 | `com.markitdown.converters.PdfConverterFormattingTest` | `normalizesCommonPdfBulletArtifacts` | `自动化 / 转换器 / 单元` | 校验常见 PDF 项目符号伪影归一化。 | `-` |
| 79 | `com.markitdown.converters.PdfConverterFormattingTest` | `rendersEnglishFallbackWhenPdfTextCannotBeExtracted` | `自动化 / 转换器 / 单元` | 校验 PDF 无法提取文本时输出英文回退提示。 | `-` |
| 80 | `com.markitdown.converters.PptConverterTest` | `emitsFriendlyPresentationMetadataAndSlideContent` | `自动化 / 转换器 / 单元` | 校验 PPT 转换会输出展示友好的 metadata 和幻灯片内容。 | `-` |
| 81 | `com.markitdown.converters.PptConverterTest` | `omitsPresentationMetadataSectionWhenDisabled` | `自动化 / 转换器 / 单元` | 校验关闭 metadata 输出时不生成 PPT metadata section。 | `-` |
| 82 | `com.markitdown.converters.PptxConverterTest` | `emitsFriendlyPresentationMetadataAndSlideContent` | `自动化 / 转换器 / 单元` | 校验 PPTX 转换会输出展示友好的 metadata 和幻灯片内容。 | `-` |
| 83 | `com.markitdown.converters.PptxConverterTest` | `omitsPresentationMetadataSectionWhenDisabled` | `自动化 / 转换器 / 单元` | 校验关闭 metadata 输出时不生成 PPTX metadata section。 | `-` |
| 84 | `com.markitdown.converters.TextConverterStreamingTest` | `convertsPlainTextStream` | `自动化 / 转换器 / 单元` | 校验纯文本流式转换。 | `-` |
| 85 | `com.markitdown.converters.TextConverterStreamingTest` | `convertsJsonStream` | `自动化 / 转换器 / 单元` | 校验 JSON 流式转换。 | `-` |
| 86 | `com.markitdown.converters.TextConverterStreamingTest` | `usesTypedSourceFileNameWhenProvided` | `自动化 / 转换器 / 单元` | 校验传入类型化源文件名时会被正确保留。 | `-` |
| 87 | `com.markitdown.converters.TextConverterStreamingTest` | `omitsCsvTableWhenTablesAreDisabled` | `自动化 / 转换器 / 单元` | 校验关闭表格输出时不生成 CSV 表格。 | `-` |
| 88 | `com.markitdown.converters.TextConverterStreamingTest` | `rendersMarkdownStyleCsvTableWithoutOuterPipes` | `自动化 / 转换器 / 单元` | 校验 Markdown 风格 CSV 表格渲染不带外层竖线。 | `-` |
| 89 | `com.markitdown.converters.XlsxConverterTest` | `usesFriendlyDefaultColumnNamesWhenHeaderIsNotDetected` | `自动化 / 转换器 / 单元` | 校验未检测到表头时使用友好的默认列名。 | `-` |
| 90 | `com.markitdown.converters.XlsxConverterTest` | `usesMarkdownTableFormatWithoutOuterPipesWhenConfigured` | `自动化 / 转换器 / 单元` | 校验配置的 Markdown 表格格式输出。 | `-` |
| 91 | `com.markitdown.converters.ZipConverterTest` | `convertsNestedTextAndJsonEntries` | `自动化 / 转换器 / 单元` | 校验 ZIP 转换会处理嵌套文本和 JSON 条目。 | `-` |
| 92 | `com.markitdown.core.ConverterRegistryTest` | `reportsSupportedMimeTypesFromConverterCapabilities` | `自动化 / 核心 / 单元` | 校验注册中心的支持 MIME 报告来源于转换器能力声明。 | `-` |
| 93 | `com.markitdown.core.MarkItDownEngineTest` | `convertShouldRejectMissingFilesBeforeConversion` | `自动化 / 核心 / 单元` | 校验在真正转换前就拒绝缺失文件。 | `-` |
| 94 | `com.markitdown.core.MarkItDownEngineTest` | `convertShouldReturnFailedResultWhenMimeTypeIsUnsupported` | `自动化 / 核心 / 单元` | 校验不支持的 MIME 会返回失败结果。 | `-` |
| 95 | `com.markitdown.core.MarkItDownEngineTest` | `streamConversionShouldFailWhenMatchingConverterDoesNotSupportStreaming` | `自动化 / 核心 / 单元` | 校验匹配转换器不支持流输入时，流式转换会失败。 | `-` |
| 96 | `com.markitdown.core.MarkItDownEngineTest` | `supportChecksShouldUseRegistryForMimeTypesAndDetectedFileTypes` | `自动化 / 核心 / 单元` | 校验支持性检查使用注册中心处理显式 MIME 和检测出的文件类型。 | `-` |
| 97 | `com.markitdown.ocr.OcrEngineFactoryTest` | `returnsUnavailableEngineWhenOcrIsDisabled` | `自动化 / OCR / 单元` | 校验关闭 OCR 时返回不可用 OCR 引擎。 | `-` |
| 98 | `com.markitdown.ocr.OcrEngineFactoryTest` | `returnsUnavailableEngineForUnknownProvider` | `自动化 / OCR / 单元` | 校验未知 OCR provider 名称时返回不可用引擎。 | `-` |
| 99 | `com.markitdown.ocr.OcrEngineFactoryTest` | `returnsMockEngineWhenMockProviderIsSelected` | `自动化 / OCR / 单元` | 校验 mock OCR provider 会返回可用的 mock 引擎。 | `-` |
| 100 | `com.markitdown.ocr.OcrEngineFactoryTest` | `returnsUnavailableEngineWhenPaddleTokenIsMissing` | `自动化 / OCR / 单元` | 校验缺少 Paddle token 配置时 OCR 不可用。 | `-` |
| 101 | `com.markitdown.ocr.OcrEngineFactoryTest` | `createsPaddleEngineWhenTokenIsConfigured` | `自动化 / OCR / 单元` | 校验存在所需配置时可成功创建 Paddle OCR engine。 | `-` |
| 102 | `com.markitdown.ocr.PaddleOcrEngineTest` | `extractsMarkdownTextFromJsonlPayload` | `自动化 / OCR / 单元` | 校验 Paddle OCR JSONL 载荷解析能正确提取 Markdown 文本。 | `-` |
| 103 | `com.markitdown.ocr.TesseractCliOcrProviderTest` | `providerShouldPassBothExecutableAndTessdataPaths` | `自动化 / OCR / 单元` | 校验 Tesseract CLI provider 会同时把可执行路径和 `tessdata` 路径传入 OCR engine。 | `-` |
| 104 | `com.markitdown.utils.FileTypeDetectorTest` | `rejectsNullAndBlankExtensions` | `自动化 / 工具 / 单元` | 校验空扩展名和空白扩展名会被拒绝。 | `-` |
| 105 | `com.markitdown.utils.FileTypeDetectorTest` | `exposesKnownMimeTypesUsedByRegistryLookups` | `自动化 / 工具 / 单元` | 校验会暴露供注册中心查询使用的已知 MIME 类型。 | `-` |
