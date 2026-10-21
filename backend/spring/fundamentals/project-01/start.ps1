# Starts project-01 (demo-keys service) on 127.0.0.1:8081. Start it before project-00.
# SERVICE_AUTH_SECRET is read from the untracked .env in this folder (see .env.example).
$ErrorActionPreference = 'Stop'
Set-Location $PSScriptRoot

if (-not (Test-Path .env)) { throw "Missing .env - copy .env.example to .env and fill in the values." }
if (-not $env:JAVA_HOME -and (Test-Path "$env:USERPROFILE\.jdks\openjdk-25.0.1")) {
    $env:JAVA_HOME = "$env:USERPROFILE\.jdks\openjdk-25.0.1"
}

& .\mvnw.cmd spring-boot:run
exit $LASTEXITCODE
