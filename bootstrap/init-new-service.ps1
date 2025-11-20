param(
    [Parameter(Mandatory=$true)]
    [string]$ParentPomVersion,

    [string]$SourceDir = "./bootstrap/templates",
    [string]$TargetDir = "."
)

$ErrorActionPreference = "Stop"

if (-not (Test-Path $SourceDir)) {
    throw "Source directory not found: $SourceDir"
}

$NewServiceName = Split-Path $TargetDir -Leaf
$NewRepo = (git remote get-url origin) -replace '.*github\.com[:/](.+)/(.+)\.git', '$1/$2'

$fileCount = 0

Get-ChildItem -Path $SourceDir -File -Recurse | ForEach-Object {
    $sourceFile = $_
    $content = Get-Content $sourceFile.FullName -Raw -Encoding UTF8

    $content = $content -replace '\$\{newServiceName\}', $NewServiceName
    $content = $content -replace '\$\{newRepo\}', $NewRepo
    $content = $content -replace '\$\{currentParentPomVersion\}', $ParentPomVersion

    $relativePath = $sourceFile.FullName.Substring((Resolve-Path $SourceDir).Path.Length).TrimStart('\', '/')
    $targetPath = Join-Path (Resolve-Path $TargetDir) $relativePath

    [System.IO.File]::WriteAllText($targetPath, $content, [System.Text.Encoding]::UTF8)

    Write-Host "  [OK] $relativePath" -ForegroundColor Green
    $fileCount++
}

Write-Host ""
Write-Host "Successfully processed $fileCount file(s)" -ForegroundColor Green
