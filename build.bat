@echo off
echo ========================================
echo   Terra Incognita - Build ^& Run
echo ========================================
echo.

:: Tao thu muc output neu chua co
if not exist "out" mkdir out

:: Bien dich tat ca file Java
echo [1/2] Compiling...
dir /s /b src\*.java > sources.txt
javac -d out -sourcepath src @sources.txt
del sources.txt

if %ERRORLEVEL% neq 0 (
    echo.
    echo [ERROR] Compilation failed!
    pause
    exit /b 1
)

echo [2/2] Running...
echo.

:: Chay game
java -cp "out;resources" TerraIncognita.Main

pause
