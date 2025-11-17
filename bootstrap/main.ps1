$BaseDir = Split-Path -Parent $MyInvocation.MyCommand.Path
$PlatformBaseDir = "$BaseDir/../"

& "$BaseDir/scripts/install-precommit-hooks.ps1" $PlatformBaseDir
