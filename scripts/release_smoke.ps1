[CmdletBinding()]
param(
    [string]$JarPath,
    [string]$ConfigPath = "verification\manual\markitdown.release.yml",
    [string]$FixtureDir = "verification\fixtures",
    [string]$RemoteUrl,
    [string]$TesseractPath,
    [string]$TessdataPath,
    [string]$OcrInputPath
)

$ErrorActionPreference = "Stop"
$repoRoot = (Get-Location).Path
$configFullPath = Join-Path $repoRoot $ConfigPath
$fixtureFullPath = Join-Path $repoRoot $FixtureDir
$sessionName = "release-validation-" + (Get-Date -Format "yyyyMMdd-HHmmss")
$outputRoot = Join-Path $repoRoot ("smoke-out\" + $sessionName)

function Assert-PathExists {
    param(
        [string]$PathValue,
        [string]$Label
    )

    if (-not (Test-Path -LiteralPath $PathValue)) {
        throw "$Label not found: $PathValue"
    }
}

function Assert-FileContains {
    param(
        [string]$PathValue,
        [string]$ExpectedText,
        [string]$Label
    )

    Assert-PathExists -PathValue $PathValue -Label $Label
    $content = Get-Content -LiteralPath $PathValue -Raw
    if ($content -notmatch [regex]::Escape($ExpectedText)) {
        throw "$Label did not contain expected text: $ExpectedText"
    }
}

function Resolve-JarPath {
    param(
        [string]$RepoRoot,
        [string]$RequestedJarPath
    )

    if ($RequestedJarPath) {
        return Join-Path $RepoRoot $RequestedJarPath
    }

    $targetDir = Join-Path $RepoRoot "target"
    Assert-PathExists -PathValue $targetDir -Label "Target directory"

    $jarCandidates = Get-ChildItem -LiteralPath $targetDir -Filter "markitdown4j-*.jar" -File |
            Where-Object { $_.Name -notlike "original-*" } |
            Sort-Object LastWriteTime -Descending

    if ($jarCandidates.Count -lt 1) {
        throw "No packaged CLI jar found under target. Run mvn clean package first."
    }

    return $jarCandidates[0].FullName
}

function Invoke-JavaStep {
    param(
        [string]$Name,
        [string[]]$Arguments
    )

    Write-Host ""
    Write-Host "==> $Name"
    & java @Arguments
    if ($LASTEXITCODE -ne 0) {
        throw "Step failed: $Name (exit code $LASTEXITCODE)"
    }
}

$jarFullPath = Resolve-JarPath -RepoRoot $repoRoot -RequestedJarPath $JarPath

Assert-PathExists -PathValue $jarFullPath -Label "Jar"
Assert-PathExists -PathValue $configFullPath -Label "Release validation config"
Assert-PathExists -PathValue $fixtureFullPath -Label "Fixture directory"

New-Item -ItemType Directory -Force -Path $outputRoot | Out-Null

$singleDir = Join-Path $outputRoot "single"
$batchDir = Join-Path $outputRoot "batch"
$remoteDir = Join-Path $outputRoot "remote"
$ocrDir = Join-Path $outputRoot "ocr"

New-Item -ItemType Directory -Force -Path $singleDir | Out-Null
New-Item -ItemType Directory -Force -Path $batchDir | Out-Null
New-Item -ItemType Directory -Force -Path $remoteDir | Out-Null
New-Item -ItemType Directory -Force -Path $ocrDir | Out-Null

$textOutput = Join-Path $singleDir "basic.md"
$jsonOutput = Join-Path $singleDir "sample.md"

$textFixture = Join-Path $fixtureFullPath "basic.txt"
$jsonFixture = Join-Path $fixtureFullPath "sample.json"
$batchFixture = Join-Path $fixtureFullPath "batch"

Assert-PathExists -PathValue $textFixture -Label "Text fixture"
Assert-PathExists -PathValue $jsonFixture -Label "JSON fixture"
Assert-PathExists -PathValue $batchFixture -Label "Batch fixture directory"

Invoke-JavaStep -Name "CLI help" -Arguments @("-jar", $jarFullPath, "--help")
Invoke-JavaStep -Name "Show config" -Arguments @("-jar", $jarFullPath, "--show-config", "--config-path", $configFullPath)
Invoke-JavaStep -Name "Validate config" -Arguments @("-jar", $jarFullPath, "--validate-config", "--config-path", $configFullPath)
Invoke-JavaStep -Name "Convert text fixture" -Arguments @("-jar", $jarFullPath, $textFixture, "--config-path", $configFullPath, "-o", $textOutput)
Assert-FileContains -PathValue $textOutput -ExpectedText "MarkItDown release smoke fixture." -Label "Text conversion output"

Invoke-JavaStep -Name "Convert JSON fixture" -Arguments @("-jar", $jarFullPath, $jsonFixture, "--config-path", $configFullPath, "-o", $jsonOutput)
Assert-FileContains -PathValue $jsonOutput -ExpectedText '"release-smoke"' -Label "JSON conversion output"

Invoke-JavaStep -Name "Batch convert fixture directory" -Arguments @("-jar", $jarFullPath, $batchFixture, "--batch", "--config-path", $configFullPath, "-o", $batchDir)

$batchOutputs = Get-ChildItem -LiteralPath $batchDir -File
if ($batchOutputs.Count -lt 2) {
    throw "Batch conversion did not produce the expected number of output files."
}

$batchNoteOutput = Join-Path $batchDir "note.txt.md"
$batchJsonOutput = Join-Path $batchDir "data.json.md"
Assert-FileContains -PathValue $batchNoteOutput -ExpectedText "Batch fixture note." -Label "Batch text conversion output"
Assert-FileContains -PathValue $batchJsonOutput -ExpectedText '"fixture"' -Label "Batch JSON conversion output"

if ($RemoteUrl) {
    Invoke-JavaStep -Name "Convert remote URL" -Arguments @("-jar", $jarFullPath, $RemoteUrl, "--config-path", $configFullPath, "-o", $remoteDir)

    $remoteOutputs = Get-ChildItem -LiteralPath $remoteDir -File
    if ($remoteOutputs.Count -lt 1) {
        throw "Remote conversion did not produce an output file."
    }
}

$effectiveOcrInput = $OcrInputPath
if (-not $effectiveOcrInput) {
    $defaultOcrInput = Join-Path $repoRoot "test\with-text.png"
    if (Test-Path -LiteralPath $defaultOcrInput) {
        $effectiveOcrInput = $defaultOcrInput
    }
}

if ($TesseractPath -and $TessdataPath -and $effectiveOcrInput) {
    Assert-PathExists -PathValue $TesseractPath -Label "Tesseract executable"
    Assert-PathExists -PathValue $TessdataPath -Label "Tessdata directory"
    Assert-PathExists -PathValue $effectiveOcrInput -Label "OCR input"

    $ocrConfigPath = Join-Path $outputRoot "ocr-config.yml"
    @"
ocr:
  enabled: true
  engine: tesseract-cli
  language: eng
tesseract:
  path: $TesseractPath
tessdata:
  path: $TessdataPath
"@ | Set-Content -LiteralPath $ocrConfigPath -Encoding UTF8

    Invoke-JavaStep -Name "Validate OCR config" -Arguments @("-jar", $jarFullPath, "--validate-config", "--config-path", $ocrConfigPath)
    Invoke-JavaStep -Name "Convert OCR input" -Arguments @("-jar", $jarFullPath, $effectiveOcrInput, "--ocr", "--config-path", $ocrConfigPath, "-o", $ocrDir)

    $ocrOutputs = Get-ChildItem -LiteralPath $ocrDir -File
    if ($ocrOutputs.Count -lt 1) {
        throw "OCR conversion did not produce an output file."
    }

    $ocrOutputFile = $ocrOutputs[0].FullName
    Assert-FileContains -PathValue $ocrOutputFile -ExpectedText "MARKER_12345" -Label "OCR conversion output"
}
elseif ($TesseractPath -or $TessdataPath -or $effectiveOcrInput) {
    Write-Warning "Skipping OCR smoke step because TesseractPath, TessdataPath, and OcrInputPath must all be available."
}

Write-Host ""
Write-Host "Release smoke validation completed."
Write-Host "Output directory: $outputRoot"
