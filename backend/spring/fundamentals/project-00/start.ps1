# Starts project-00 (University API) with the dev profile. Start project-01 first (demo keys).
# Settings are read from the untracked .env in this folder (see .env.example).
$ErrorActionPreference = 'Stop'
Set-Location $PSScriptRoot

if (-not (Test-Path .env)) { throw "Missing .env - copy .env.example to .env and fill in the values." }
$jdk25 = "$env:USERPROFILE\.jdks\openjdk-25.0.1"
$javaVersion = if ($env:JAVA_HOME -and (Test-Path "$env:JAVA_HOME\release")) { Select-String -Path "$env:JAVA_HOME\release" -Pattern 'JAVA_VERSION="(\d+)' | ForEach-Object { [int]$_.Matches[0].Groups[1].Value } } else { 0 }
if ($javaVersion -lt 25 -and (Test-Path $jdk25)) {
    # The pom targets Java 25; an older JAVA_HOME (e.g. 17) cannot run the compiled classes.
    $env:JAVA_HOME = $jdk25
    $env:Path = "$jdk25\bin;$env:Path"
}

& .\mvnw.cmd spring-boot:run "-Dspring-boot.run.profiles=dev"
exit $LASTEXITCODE
