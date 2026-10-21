# Starts project-00 (University API) with the dev profile. Start project-01 first (demo keys).
# Settings are read from the untracked .env in this folder (see .env.example).
$ErrorActionPreference = 'Stop'
Set-Location $PSScriptRoot

if (-not (Test-Path .env)) { throw "Missing .env - copy .env.example to .env and fill in the values." }
if (-not $env:JAVA_HOME -and (Test-Path "$env:USERPROFILE\.jdks\openjdk-25.0.1")) {
    $env:JAVA_HOME = "$env:USERPROFILE\.jdks\openjdk-25.0.1"
}

& .\mvnw.cmd spring-boot:run "-Dspring-boot.run.profiles=dev"
exit $LASTEXITCODE
