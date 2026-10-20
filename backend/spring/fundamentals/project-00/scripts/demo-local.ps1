param(
  [int]$Port = 8080,
  [ValidateRange(1, 30)]
  [int]$Days = 10,
  [string]$Label = 'local demo'
)

$ErrorActionPreference = 'Stop'
Set-Location (Split-Path $PSScriptRoot -Parent)

if (-not (Test-Path '.demo-keys\private.pem')) {
  node scripts\demo-token.mjs keys | Out-Null
}
$token = (node scripts\demo-token.mjs issue --label $Label --days $Days).Trim()
Set-Clipboard -Value $token

Write-Host "`nDevelopment demo token (copied to clipboard):`n$token`n" -ForegroundColor Green
Write-Host "The backend is bound to localhost. Open http://localhost:4300/demo and paste the token."
if ($Port -ne 8080) {
  Write-Host "Point apps/web-app/proxy.conf.json at http://localhost:$Port" -ForegroundColor Yellow
}

$env:SPRING_PROFILES_ACTIVE = 'dev'
$env:DEMO_PUBLIC_KEY = Get-Content '.demo-keys\public.pem' -Raw
$env:SERVER_PORT = "$Port"
if (-not $env:JAVA_HOME) {
  Write-Host 'JAVA_HOME is not set; JDK 25 is required.' -ForegroundColor Yellow
}
.\mvnw.cmd spring-boot:run
