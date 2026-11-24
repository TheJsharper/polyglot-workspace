@echo off
REM Run all tests for the 06-data-structures project
REM Usage: scripts\run_tests.bat

echo =========================================
echo   Running all data structure tests...
echo =========================================
echo.

cd /d "%~dp0.."

cargo test --test run_all -- --nocapture

echo.
echo =========================================
echo   Test run complete!
echo =========================================
