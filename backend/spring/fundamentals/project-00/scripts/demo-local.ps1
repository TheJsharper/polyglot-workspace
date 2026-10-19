# One-command local recruiter demo: creates keys on first run, issues a token (copied to the clipboard),
# and starts the backend in the demo profile. Run from the backend project directory.
#   .\scripts\demo-local.ps1 [-Port 8080] [-Days 10] [-Label "HR test"]
param(
  [int]$Port = 8080,
  [int]$Days = 10,
  [string]$Label = 'local demo'
)

$ErrorActionPreference = 'Stop'
Set-Location (Split-Path $PSScriptRoot -Parent)

if (-not (Test-Path .demo-keys\private.pem)) {
  node scripts\demo-token.mjs keys | Out-Null
}
$token = (node scripts\demo-token.mjs issue --label $Label --days $Days).Trim()
Set-Clipboard -Value $token

Write-Host "`nDemo token (also copied to the clipboard):`n$token`n" -ForegroundColor Green
Write-Host "Open the frontend at http://localhost:4300/demo and paste it. Starting backend on port $Port...`n"
if ($Port -ne 8080) { Write-Host "Note: point apps/web-app/proxy.conf.json at http://localhost:$Port" -ForegroundColor Yellow }

$env:SPRING_PROFILES_ACTIVE = 'demo'
$env:DEMO_PUBLIC_KEY = (Get-Content .demo-keys\public.pem -Raw)
$env:SERVER_PORT = "$Port"
if (-not $env:JAVA_HOME) { Write-Host 'JAVA_HOME is not set; JDK 25 is required.' -ForegroundColor Yellow }
.\mvnw.cmd spring-boot:run
