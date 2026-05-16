[CmdletBinding()]
param(
    [string]$JarPath,
    [string]$OutputRoot
)

$scriptPath = Join-Path $PSScriptRoot "run_functional_checks.ps1"
$arguments = @(
    "-ExecutionPolicy", "Bypass",
    "-File", $scriptPath,
    "-Suite", "stress"
)

if ($JarPath) {
    $arguments += @("-JarPath", $JarPath)
}

if ($OutputRoot) {
    $arguments += @("-OutputRoot", $OutputRoot)
}

powershell @arguments
