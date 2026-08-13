@echo off
echo ========================================
echo   Terra Incognita - Build ^& Run
echo ========================================
echo.

where mvn >nul 2>&1
if %ERRORLEVEL% neq 0 (
    echo [ERROR] Maven is required. Install Maven and make sure "mvn" is on PATH.
    pause
    exit /b 1
)

echo [1/2] Compiling...
call mvn -q compile

if %ERRORLEVEL% neq 0 (
    echo.
    echo [ERROR] Compilation failed!
    pause
    exit /b 1
)

:: Neu truyen "test" -> chay test JUnit
if "%~1"=="test" (
    echo [2/2] Running tests...
    call mvn -q test
    pause
    exit /b %ERRORLEVEL%
)

echo [2/2] Running...
echo.

call mvn -q exec:java

pause
