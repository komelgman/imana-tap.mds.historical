param(
    [string]$ProjectBaseDir
)

$HooksDir = "$ProjectBaseDir/.github/hooks"

if (-not (Test-Path $HooksDir -PathType Container)) {
    Write-Error "[install-precommit-hooks.ps1:error] '$HooksDir' not found."
    exit 1
}

git config core.hooksPath $HooksDir

Write-Host "Git hooks configured via core.hooksPath" -ForegroundColor Green
