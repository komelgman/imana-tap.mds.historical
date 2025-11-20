$BaseDir = Split-Path -Parent $MyInvocation.MyCommand.Path

$PlatformCompose = [IO.Path]::GetFullPath("$BaseDir/../../platform/compose-up.ps1")
$ServiceName = Split-Path -Leaf $BaseDir

& $PlatformCompose $ServiceName
