[CmdletBinding()]
param(
    [string]$JarPath,
    [string]$OutputRoot,
    [ValidateSet("all", "standard", "stress")]
    [string]$Suite = "all"
)

$ErrorActionPreference = "Stop"

function Assert-True {
    param(
        [bool]$Condition,
        [string]$Message
    )

    if (-not $Condition) {
        throw $Message
    }
}

function Resolve-JarPath {
    param(
        [string]$RepoRoot,
        [string]$RequestedJarPath
    )

    if ($RequestedJarPath) {
        return (Resolve-Path -LiteralPath $RequestedJarPath).Path
    }

    $targetDir = Join-Path $RepoRoot "target"
    $jar = Get-ChildItem -LiteralPath $targetDir -Filter "markitdown4j-*.jar" -File |
        Where-Object { $_.Name -notlike "original-*" } |
        Sort-Object LastWriteTime -Descending |
        Select-Object -First 1

    Assert-True ($null -ne $jar) "Packaged jar was not found under target."
    return $jar.FullName
}

function Invoke-Java {
    param(
        [string]$Name,
        [string[]]$Arguments,
        [string]$WorkingDirectory,
        [string]$StdInText
    )

    $stdoutPath = Join-Path $WorkingDirectory ($Name + ".stdout.txt")
    $stderrPath = Join-Path $WorkingDirectory ($Name + ".stderr.txt")
    Remove-Item -LiteralPath $stdoutPath, $stderrPath -Force -ErrorAction SilentlyContinue

    $escapedArguments = $Arguments | ForEach-Object {
        if ($_ -match '[\s"]') {
            '"' + ($_ -replace '"', '\"') + '"'
        } else {
            $_
        }
    }

    $psi = New-Object System.Diagnostics.ProcessStartInfo
    $psi.FileName = "java"
    $psi.Arguments = [string]::Join(" ", $escapedArguments)
    $psi.WorkingDirectory = $WorkingDirectory
    $psi.UseShellExecute = $false
    $psi.RedirectStandardOutput = $true
    $psi.RedirectStandardError = $true
    $psi.RedirectStandardInput = $null -ne $StdInText

    $process = New-Object System.Diagnostics.Process
    $process.StartInfo = $psi
    [void]$process.Start()

    if ($null -ne $StdInText) {
        $process.StandardInput.Write($StdInText)
        $process.StandardInput.Close()
    }

    $stdout = $process.StandardOutput.ReadToEnd()
    $stderr = $process.StandardError.ReadToEnd()
    $process.WaitForExit()

    Set-Content -LiteralPath $stdoutPath -Value $stdout -Encoding UTF8
    Set-Content -LiteralPath $stderrPath -Value $stderr -Encoding UTF8

    return [pscustomobject]@{
        ExitCode = $process.ExitCode
        Stdout = $stdout
        Stderr = $stderr
        Combined = ($stdout + "`n" + $stderr).Trim()
        StdoutPath = $stdoutPath
        StderrPath = $stderrPath
    }
}

function Add-Result {
    param(
        [System.Collections.Generic.List[object]]$Results,
        [string]$Name,
        [string]$Status,
        [string]$Category,
        [string]$Detail
    )

    $Results.Add([pscustomobject]@{
        Name = $Name
        Status = $Status
        Category = $Category
        Detail = $Detail
    }) | Out-Null

    Write-Output ("[{0}] {1} :: {2}" -f $Status, $Name, $Detail)
}

function Should-RunSuite {
    param(
        [string]$RequestedSuite,
        [string]$CaseSuite
    )

    return $RequestedSuite -eq "all" -or $RequestedSuite -eq $CaseSuite
}

$repoRoot = (Get-Location).Path
$resolvedJarPath = Resolve-JarPath -RepoRoot $repoRoot -RequestedJarPath $JarPath

if (-not $OutputRoot) {
    $OutputRoot = Join-Path $repoRoot ("functional-out\" + (Get-Date -Format "yyyyMMdd-HHmmss"))
}

New-Item -ItemType Directory -Force -Path $OutputRoot | Out-Null

$results = New-Object System.Collections.Generic.List[object]
$configPath = Join-Path $repoRoot "verification\manual\markitdown.release.yml"
$invalidConfigPath = Join-Path $OutputRoot "invalid.yml"
$ocrConfigPath = Join-Path $OutputRoot "ocr.yml"

@"
ocr:
  enabled: true
  engine: http
"@ | Set-Content -LiteralPath $invalidConfigPath -Encoding UTF8

@"
ocr:
  enabled: true
  engine: tesseract-cli
  language: eng
tesseract:
  path: O:\tesserOCR\tesseract.exe
tessdata:
  path: O:\tesserOCR\tessdata
"@ | Set-Content -LiteralPath $ocrConfigPath -Encoding UTF8

try {
    if (Should-RunSuite -RequestedSuite $Suite -CaseSuite "standard") {
        $r = Invoke-Java -Name "show-config-explicit-yaml" -WorkingDirectory $OutputRoot -Arguments @(
            "-jar", $resolvedJarPath, "--show-config", "--config-path", $configPath
        )
        Assert-True ($r.ExitCode -eq 0) "show-config exit code != 0"
        Assert-True ($r.Combined -match "Active configuration") "show-config output missing expected text"
        Add-Result -Results $results -Name "show-config-explicit-yaml" -Status "PASS" -Category "config" -Detail "Loaded explicit YAML and printed effective configuration."

        $r = Invoke-Java -Name "validate-config-success" -WorkingDirectory $OutputRoot -Arguments @(
            "-jar", $resolvedJarPath, "--validate-config", "--config-path", $configPath
        )
        Assert-True ($r.ExitCode -eq 0) "validate-config success exit code != 0"
        Assert-True ($r.Combined -match "Configuration file is valid") "validate-config success output missing expected text"
        Add-Result -Results $results -Name "validate-config-success" -Status "PASS" -Category "config" -Detail "Valid configuration file passed CLI validation."

        $r = Invoke-Java -Name "validate-config-failure" -WorkingDirectory $OutputRoot -Arguments @(
            "-jar", $resolvedJarPath, "--validate-config", "--config-path", $invalidConfigPath
        )
        Assert-True ($r.ExitCode -eq 1) "validate-config failure exit code != 1"
        Assert-True ($r.Combined -match "Configuration file is invalid") "validate-config failure output missing expected text"
        Add-Result -Results $results -Name "validate-config-failure" -Status "PASS" -Category "config" -Detail "Invalid configuration file failed validation as expected."

        $r = Invoke-Java -Name "show-config-cli-override" -WorkingDirectory $OutputRoot -Arguments @(
            "-jar", $resolvedJarPath, "--show-config", "--config-path", $configPath, "--ocr", "--ocr-engine", "http"
        )
        Assert-True ($r.ExitCode -eq 0) "show-config override exit code != 0"
        Assert-True ($r.Combined -match "OCR engine: http \[cli\]") "show-config override missing cli source marker"
        Add-Result -Results $results -Name "show-config-cli-override" -Status "PASS" -Category "config" -Detail "CLI override replaced OCR engine and preserved config inspection output."

        $generateDir = Join-Path $OutputRoot "generate-config"
        New-Item -ItemType Directory -Force -Path $generateDir | Out-Null
        $r = Invoke-Java -Name "generate-config" -WorkingDirectory $generateDir -Arguments @(
            "-jar", $resolvedJarPath, "--generate-config"
        )
        $generatedConfigPath = Join-Path $generateDir "markitdown.yml"
        Assert-True ($r.ExitCode -eq 0) "generate-config exit code != 0"
        Assert-True (Test-Path -LiteralPath $generatedConfigPath) "generated config file missing"
        $generatedConfig = Get-Content -LiteralPath $generatedConfigPath -Raw
        Assert-True ($generatedConfig -match "ocr:" -and $generatedConfig -match "format:") "generated config missing expected sections"
        Add-Result -Results $results -Name "generate-config" -Status "PASS" -Category "config" -Detail "Generated default markitdown.yml successfully."
    }

    $conversionCases = @(
        @{ Name = "convert-basic-txt"; Input = "test\basic.txt"; Output = "basic.txt.md"; Detail = "Converted basic.txt to Markdown file." },
        @{ Name = "convert-basic-json"; Input = "test\basic.json"; Output = "basic.json.md"; Detail = "Converted basic.json to Markdown file." },
        @{ Name = "convert-basic-html"; Input = "test\basic.html"; Output = "basic.html.md"; Detail = "Converted basic.html to Markdown file." },
        @{ Name = "convert-basic-docx"; Input = "test\basic.docx"; Output = "basic.docx.md"; Detail = "Converted basic.docx to Markdown file." },
        @{ Name = "convert-basic-pptx"; Input = "test\basic.pptx"; Output = "basic.pptx.md"; Detail = "Converted basic.pptx to Markdown file." },
        @{ Name = "convert-basic-xlsx"; Input = "test\basic.xlsx"; Output = "basic.xlsx.md"; Detail = "Converted basic.xlsx to Markdown file." },
        @{ Name = "convert-plain-text-pdf"; Input = "test\plain-text.pdf"; Output = "plain-text.pdf.md"; Detail = "Converted plain-text.pdf to Markdown file." },
        @{ Name = "convert-nested-zip"; Input = "test\nested.zip"; Output = "nested.zip.md"; Detail = "Converted nested.zip to Markdown file." },
        @{ Name = "convert-sample-png"; Input = "test\sample.png"; Output = "sample.png.md"; Detail = "Converted sample.png to Markdown file." },
        @{ Name = "convert-sample-mp3"; Input = "test\sample.mp3"; Output = "sample.mp3.md"; Detail = "Converted sample.mp3 to Markdown output." },
        @{ Name = "convert-with-images-docx"; Input = "test\with-images.docx"; Output = "with-images.docx.md"; Detail = "Converted with-images.docx to Markdown file." },
        @{ Name = "convert-with-images-html"; Input = "test\with-images.html"; Output = "with-images.html.md"; Detail = "Converted with-images.html to Markdown file." },
        @{ Name = "convert-with-images-xlsx"; Input = "test\with-images.xlsx"; Output = "with-images.xlsx.md"; Detail = "Converted with-images.xlsx to Markdown file." },
        @{ Name = "convert-with-notes-pptx"; Input = "test\with-notes.pptx"; Output = "with-notes.pptx.md"; Detail = "Converted with-notes.pptx to Markdown file." },
        @{ Name = "convert-multi-sheet-xlsx"; Input = "test\multi-sheet.xlsx"; Output = "multi-sheet.xlsx.md"; Detail = "Converted multi-sheet.xlsx to Markdown file." },
        @{ Name = "convert-old-format-xls"; Input = "test\old-format.xls"; Output = "old-format.xls.md"; Detail = "Converted old-format.xls to Markdown file." },
        @{ Name = "convert-complex-nested-zip"; Input = "test\complex-nested.zip"; Output = "complex-nested.zip.md"; Detail = "Converted complex-nested.zip to Markdown file." },
        @{ Name = "convert-plain-text-chinese-pdf"; Input = "test\plain-text-chinese.pdf"; Output = "plain-text-chinese.pdf.md"; Detail = "Converted plain-text-chinese.pdf to Markdown file." },
        @{ Name = "convert-100-pages-pdf"; Input = "test\100-pages.pdf"; Output = "100-pages.pdf.md"; Detail = "Converted 100-pages.pdf to Markdown file." },
        @{ Name = "convert-1000-pages-docx"; Input = "test\1000-pages.docx"; Output = "1000-pages.docx.md"; Detail = "Converted 1000-pages.docx to Markdown file." },
        @{ Name = "convert-10000-rows-xlsx"; Input = "test\10000-rows.xlsx"; Output = "10000-rows.xlsx.md"; Detail = "Converted 10000-rows.xlsx to Markdown file." },
        @{ Name = "convert-large-archive-zip"; Input = "test\large-archive.zip"; Output = "large-archive.zip.md"; Detail = "Converted large-archive.zip to Markdown file." },
        @{ Name = "convert-with-media-pptx"; Input = "test\with-media.pptx"; Output = "with-media.pptx.md"; Detail = "Converted with-media.pptx to Markdown file." },
        @{ Name = "convert-large-dataset-xlsx"; Input = "test\large-dataset.xlsx"; Output = "large-dataset.xlsx.md"; Detail = "Converted large-dataset.xlsx to Markdown file." }
    )

    if (Should-RunSuite -RequestedSuite $Suite -CaseSuite "standard") {
        foreach ($case in $conversionCases) {
            $inputPath = Join-Path $repoRoot $case.Input
            $outputPath = Join-Path $OutputRoot $case.Output
            $r = Invoke-Java -Name $case.Name -WorkingDirectory $OutputRoot -Arguments @(
                "-jar", $resolvedJarPath, $inputPath, "-o", $outputPath
            )
            Assert-True ($r.ExitCode -eq 0) ($case.Name + " exit code != 0")
            Assert-True (Test-Path -LiteralPath $outputPath) ($case.Name + " output missing")
            Assert-True ((Get-Content -LiteralPath $outputPath -Raw).Length -gt 0) ($case.Name + " output empty")
            Add-Result -Results $results -Name $case.Name -Status "PASS" -Category "file" -Detail $case.Detail
        }

        $singleDir = Join-Path $OutputRoot "single-dir"
        New-Item -ItemType Directory -Force -Path $singleDir | Out-Null
        $r = Invoke-Java -Name "single-file-output-directory" -WorkingDirectory $OutputRoot -Arguments @(
            "-jar", $resolvedJarPath, (Join-Path $repoRoot "test\basic.txt"), "-o", $singleDir
        )
        Assert-True ($r.ExitCode -eq 0) "single-file-output-directory exit code != 0"
        Assert-True (Test-Path -LiteralPath (Join-Path $singleDir "basic.txt.md")) "single-file-output-directory output missing"
        Add-Result -Results $results -Name "single-file-output-directory" -Status "PASS" -Category "output" -Detail "Single file written into output directory using source file name plus .md."

        $batchDir = Join-Path $OutputRoot "batch-dir"
        New-Item -ItemType Directory -Force -Path $batchDir | Out-Null
        $r = Invoke-Java -Name "batch-directory-conversion" -WorkingDirectory $OutputRoot -Arguments @(
            "-jar", $resolvedJarPath, (Join-Path $repoRoot "verification\fixtures\batch"), "--batch", "-o", $batchDir
        )
        Assert-True ($r.ExitCode -eq 0) "batch-directory-conversion exit code != 0"
        Assert-True (Test-Path -LiteralPath (Join-Path $batchDir "note.txt.md")) "batch note output missing"
        Assert-True (Test-Path -LiteralPath (Join-Path $batchDir "data.json.md")) "batch json output missing"
        Add-Result -Results $results -Name "batch-directory-conversion" -Status "PASS" -Category "output" -Detail "Batch conversion created note.txt.md and data.json.md."

        $stdinDir = Join-Path $OutputRoot "stdin"
        New-Item -ItemType Directory -Force -Path $stdinDir | Out-Null
        $r = Invoke-Java -Name "stdin-text-conversion" -WorkingDirectory $stdinDir -Arguments @("-jar", $resolvedJarPath) -StdInText "functional stdin line"
        Assert-True ($r.ExitCode -eq 0) "stdin-text-conversion exit code != 0"
        Assert-True ($r.Combined -match "functional stdin line") "stdin text output missing"
        Add-Result -Results $results -Name "stdin-text-conversion" -Status "PASS" -Category "stdin" -Detail "Read text from stdin and emitted Markdown to stdout."

        $r = Invoke-Java -Name "stdin-json-explicit-mime" -WorkingDirectory $stdinDir -Arguments @(
            "-jar", $resolvedJarPath, "--mime-type", "application/json"
        ) -StdInText '{"hello":"world"}'
        Assert-True ($r.ExitCode -eq 0) "stdin-json-explicit-mime exit code != 0"
        Assert-True ($r.Combined -match "hello") "stdin json output missing expected key"
        Add-Result -Results $results -Name "stdin-json-explicit-mime" -Status "PASS" -Category "stdin" -Detail "Read JSON from stdin with explicit MIME type."

        $defaultDocxOut = Join-Path $OutputRoot "with-tables.default.md"
        $optionDocxOut = Join-Path $OutputRoot "with-tables.no-tables.md"
        $r1 = Invoke-Java -Name "docx-default" -WorkingDirectory $OutputRoot -Arguments @(
            "-jar", $resolvedJarPath, (Join-Path $repoRoot "test\with-tables.docx"), "-o", $defaultDocxOut
        )
        $r2 = Invoke-Java -Name "docx-no-tables-option" -WorkingDirectory $OutputRoot -Arguments @(
            "-jar", $resolvedJarPath, (Join-Path $repoRoot "test\with-tables.docx"), "--no-tables", "-o", $optionDocxOut
        )
        Assert-True ($r1.ExitCode -eq 0 -and $r2.ExitCode -eq 0) "docx-no-tables-option exit code != 0"
        $defaultDocxContent = Get-Content -LiteralPath $defaultDocxOut -Raw
        $optionDocxContent = Get-Content -LiteralPath $optionDocxOut -Raw
        Assert-True ($defaultDocxContent.Length -gt $optionDocxContent.Length) "docx no-tables output was not reduced"
        Add-Result -Results $results -Name "docx-no-tables-option" -Status "PASS" -Category "option" -Detail "DOCX conversion with --no-tables produced reduced output relative to default conversion."

        $imageHtmlOut = Join-Path $OutputRoot "sample.image-html.md"
        $r = Invoke-Java -Name "image-html-format-option" -WorkingDirectory $OutputRoot -Arguments @(
            "-jar", $resolvedJarPath, (Join-Path $repoRoot "test\sample.png"), "--image-format", "html", "-o", $imageHtmlOut
        )
        Assert-True ($r.ExitCode -eq 0) "image-html-format-option exit code != 0"
        $imageHtmlContent = Get-Content -LiteralPath $imageHtmlOut -Raw
        Assert-True ($imageHtmlContent -match "<img") "image html output missing img tag"
        Add-Result -Results $results -Name "image-html-format-option" -Status "PASS" -Category "option" -Detail "Image conversion with --image-format html emitted HTML image markup."

        $defaultPptxOut = Join-Path $OutputRoot "basic.pptx.default.md"
        $optionPptxOut = Join-Path $OutputRoot "basic.pptx.no-metadata.md"
        $r1 = Invoke-Java -Name "pptx-default" -WorkingDirectory $OutputRoot -Arguments @(
            "-jar", $resolvedJarPath, (Join-Path $repoRoot "test\basic.pptx"), "-o", $defaultPptxOut
        )
        $r2 = Invoke-Java -Name "pptx-no-metadata-option" -WorkingDirectory $OutputRoot -Arguments @(
            "-jar", $resolvedJarPath, (Join-Path $repoRoot "test\basic.pptx"), "--no-metadata", "-o", $optionPptxOut
        )
        Assert-True ($r1.ExitCode -eq 0 -and $r2.ExitCode -eq 0) "pptx-no-metadata-option exit code != 0"
        $defaultPptxContent = Get-Content -LiteralPath $defaultPptxOut -Raw
        $optionPptxContent = Get-Content -LiteralPath $optionPptxOut -Raw
        Assert-True ($defaultPptxContent.Length -gt $optionPptxContent.Length) "pptx no-metadata output was not reduced"
        Add-Result -Results $results -Name "pptx-no-metadata-option" -Status "PASS" -Category "option" -Detail "PPTX conversion with --no-metadata produced reduced output relative to default conversion."

        $xlsxMarkdownOut = Join-Path $OutputRoot "basic.markdown-table.xlsx.md"
        $r = Invoke-Java -Name "xlsx-markdown-table-option" -WorkingDirectory $OutputRoot -Arguments @(
            "-jar", $resolvedJarPath, (Join-Path $repoRoot "test\basic.xlsx"), "--table-format", "markdown", "-o", $xlsxMarkdownOut
        )
        Assert-True ($r.ExitCode -eq 0) "xlsx-markdown-table-option exit code != 0"
        $xlsxMarkdownContent = Get-Content -LiteralPath $xlsxMarkdownOut -Raw
        Assert-True ($xlsxMarkdownContent -match "---") "xlsx markdown output missing table separator"
        Add-Result -Results $results -Name "xlsx-markdown-table-option" -Status "PASS" -Category "option" -Detail "XLSX conversion with --table-format markdown emitted markdown-style table output."

        $ocrDir = Join-Path $OutputRoot "ocr"
        New-Item -ItemType Directory -Force -Path $ocrDir | Out-Null
        $r = Invoke-Java -Name "ocr-tesseract-cli-png" -WorkingDirectory $OutputRoot -Arguments @(
            "-jar", $resolvedJarPath, (Join-Path $repoRoot "test\with-text.png"), "--ocr", "--config-path", $ocrConfigPath, "-o", $ocrDir
        )
        Assert-True ($r.ExitCode -eq 0) "ocr-tesseract-cli-png exit code != 0"
        $ocrFile = Get-ChildItem -LiteralPath $ocrDir -File | Select-Object -First 1
        Assert-True ($null -ne $ocrFile) "ocr output file missing"
        $ocrContent = Get-Content -LiteralPath $ocrFile.FullName -Raw
        Assert-True ($ocrContent -match "MARKER_12345") "ocr output missing expected marker"
        Add-Result -Results $results -Name "ocr-tesseract-cli-png" -Status "PASS" -Category "ocr" -Detail "Tesseract CLI OCR extracted expected marker text from with-text.png."

        $scannedPdfOcrOut = Join-Path $OutputRoot "scanned.pdf.md"
        $r = Invoke-Java -Name "ocr-scanned-pdf" -WorkingDirectory $OutputRoot -Arguments @(
            "-jar", $resolvedJarPath, (Join-Path $repoRoot "test\scanned.pdf"), "--ocr", "--config-path", $ocrConfigPath, "-o", $scannedPdfOcrOut
        )
        Assert-True ($r.ExitCode -eq 0) "ocr-scanned-pdf exit code != 0"
        Assert-True (Test-Path -LiteralPath $scannedPdfOcrOut) "ocr-scanned-pdf output missing"
        Assert-True ((Get-Content -LiteralPath $scannedPdfOcrOut -Raw).Length -gt 0) "ocr-scanned-pdf output empty"
        Add-Result -Results $results -Name "ocr-scanned-pdf" -Status "PASS" -Category "ocr" -Detail "Converted scanned.pdf with OCR and produced non-empty Markdown output."

        $oldFormatDocOut = Join-Path $OutputRoot "old-format.doc.md"
        $r = Invoke-Java -Name "old-format-doc-failure" -WorkingDirectory $OutputRoot -Arguments @(
            "-jar", $resolvedJarPath, (Join-Path $repoRoot "test\old-format.doc"), "-o", $oldFormatDocOut
        )
        Assert-True ($r.ExitCode -eq 1) "old-format-doc-failure exit code != 1"
        Assert-True ($r.Combined -match "OLE2 Format" -or $r.Combined -match "OOXML") "old-format-doc-failure output missing expected failure text"
        Add-Result -Results $results -Name "old-format-doc-failure" -Status "PASS" -Category "failure-path" -Detail "Converting old-format.doc failed with the current OLE2/OOXML mismatch error path, and the failure was surfaced by the CLI."

        $encryptedPdfOut = Join-Path $OutputRoot "encrypted.pdf.md"
        $r = Invoke-Java -Name "encrypted-pdf-warning-path" -WorkingDirectory $OutputRoot -Arguments @(
            "-jar", $resolvedJarPath, (Join-Path $repoRoot "test\encrypted.pdf"), "-o", $encryptedPdfOut
        )
        Assert-True ($r.ExitCode -eq 0) "encrypted-pdf-warning-path exit code != 0"
        Assert-True (Test-Path -LiteralPath $encryptedPdfOut) "encrypted-pdf-warning-path output missing"
        Assert-True ($r.Combined -match "Cannot decrypt PDF" -or $r.Combined -match "incorrect") "encrypted-pdf-warning-path missing decrypt warning"
        Add-Result -Results $results -Name "encrypted-pdf-warning-path" -Status "PASS" -Category "warning-path" -Detail "encrypted.pdf completed with warning-path behavior when PDF decryption failed."

        $passwordProtectedPdfOut = Join-Path $OutputRoot "password-protected.pdf.md"
        $r = Invoke-Java -Name "password-protected-pdf-warning-path" -WorkingDirectory $OutputRoot -Arguments @(
            "-jar", $resolvedJarPath, (Join-Path $repoRoot "test\password-protected.pdf"), "-o", $passwordProtectedPdfOut
        )
        Assert-True ($r.ExitCode -eq 0) "password-protected-pdf-warning-path exit code != 0"
        Assert-True (Test-Path -LiteralPath $passwordProtectedPdfOut) "password-protected-pdf-warning-path output missing"
        Assert-True ($r.Combined -match "Cannot decrypt PDF" -or $r.Combined -match "incorrect") "password-protected-pdf-warning-path missing decrypt warning"
        Add-Result -Results $results -Name "password-protected-pdf-warning-path" -Status "PASS" -Category "warning-path" -Detail "password-protected.pdf completed with warning-path behavior when PDF decryption failed."
    }

    if (Should-RunSuite -RequestedSuite $Suite -CaseSuite "stress") {
        $hugePdfDefaultOut = Join-Path $OutputRoot "huge-pdf.default.md"
        $r = Invoke-Java -Name "huge-pdf-default-limit" -WorkingDirectory $OutputRoot -Arguments @(
            "-jar", $resolvedJarPath, (Join-Path $repoRoot "test\huge-pdf.pdf"), "-o", $hugePdfDefaultOut
        )
        Assert-True ($r.ExitCode -eq 1) "huge-pdf-default-limit exit code != 1"
        Assert-True ($r.Combined -match "exceeds maximum allowed size" -and $r.Combined -match "--large-file") "huge-pdf-default-limit missing size-limit message"
        Add-Result -Results $results -Name "huge-pdf-default-limit" -Status "PASS" -Category "large-file" -Detail "huge-pdf.pdf was rejected by the default 50 MB size limit and reported the --large-file guidance."

        $hugePdfLargeOut = Join-Path $OutputRoot "huge-pdf.large.md"
        $r = Invoke-Java -Name "huge-pdf-with-large-file" -WorkingDirectory $OutputRoot -Arguments @(
            "-jar", $resolvedJarPath, (Join-Path $repoRoot "test\huge-pdf.pdf"), "--large-file", "-o", $hugePdfLargeOut
        )
        Assert-True ($r.ExitCode -eq 0) "huge-pdf-with-large-file exit code != 0"
        Assert-True (Test-Path -LiteralPath $hugePdfLargeOut) "huge-pdf-with-large-file output missing"
        Assert-True ((Get-Content -LiteralPath $hugePdfLargeOut -Raw).Length -gt 0) "huge-pdf-with-large-file output empty"
        Add-Result -Results $results -Name "huge-pdf-with-large-file" -Status "PASS" -Category "large-file" -Detail "huge-pdf.pdf converted successfully when --large-file was enabled."

        $mixedFormatsDefaultOut = Join-Path $OutputRoot "mixed-formats.default.md"
        $r = Invoke-Java -Name "mixed-formats-default-limit" -WorkingDirectory $OutputRoot -Arguments @(
            "-jar", $resolvedJarPath, (Join-Path $repoRoot "test\mixed-formats.zip"), "-o", $mixedFormatsDefaultOut
        )
        Assert-True ($r.ExitCode -eq 1) "mixed-formats-default-limit exit code != 1"
        Assert-True ($r.Combined -match "exceeds maximum allowed size" -and $r.Combined -match "--large-file") "mixed-formats-default-limit missing size-limit message"
        Add-Result -Results $results -Name "mixed-formats-default-limit" -Status "PASS" -Category "large-file" -Detail "mixed-formats.zip was rejected by the default 50 MB size limit and reported the --large-file guidance."

        $mixedFormatsLargeOut = Join-Path $OutputRoot "mixed-formats.large.md"
        $r = Invoke-Java -Name "mixed-formats-with-large-file" -WorkingDirectory $OutputRoot -Arguments @(
            "-jar", $resolvedJarPath, (Join-Path $repoRoot "test\mixed-formats.zip"), "--large-file", "-o", $mixedFormatsLargeOut
        )
        Assert-True ($r.ExitCode -eq 0) "mixed-formats-with-large-file exit code != 0"
        Assert-True (Test-Path -LiteralPath $mixedFormatsLargeOut) "mixed-formats-with-large-file output missing"
        Assert-True ((Get-Content -LiteralPath $mixedFormatsLargeOut -Raw).Length -gt 0) "mixed-formats-with-large-file output empty"
        Assert-True ($r.Combined -match "Error processing ZIP entry" -or $r.Combined -match "does not support stream-based conversion") "mixed-formats-with-large-file missing expected warning path"
        Add-Result -Results $results -Name "mixed-formats-with-large-file" -Status "PASS" -Category "large-file" -Detail "mixed-formats.zip completed successfully with --large-file and surfaced ZIP-entry warning-path messages during processing."
    }
}
catch {
    Write-Output ("FAILURE_MESSAGE=" + $_.Exception.Message)
    if ($_.InvocationInfo) {
        Write-Output ("FAILURE_LINE=" + $_.InvocationInfo.ScriptLineNumber)
        Write-Output ("FAILURE_TEXT=" + $_.InvocationInfo.Line.Trim())
    }
    exit 1
}

$summaryPath = Join-Path $OutputRoot "functional-results.csv"
$results | Export-Csv -LiteralPath $summaryPath -NoTypeInformation -Encoding UTF8
Write-Output ("OUTPUT_ROOT=" + $OutputRoot)
Write-Output ("SUMMARY=" + $summaryPath)
