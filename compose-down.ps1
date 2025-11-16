$BaseDir = Split-Path -Parent $MyInvocation.MyCommand.Path

$PlatformCompose = [IO.Path]::GetFullPath("$BaseDir/../../platform/compose-down.ps1")

& $PlatformCompose
