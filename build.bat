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
javac -d out -sourcepath src -cp "lib/*" @sources.txt
del sources.txt

if %ERRORLEVEL% neq 0 (
    echo.
    echo [ERROR] Compilation failed!
    pause
    exit /b 1
)

:: Neu truyen "test" -> chay test JUnit
if "%~1"=="test" (
    echo [2/2] Running tests...
    echo.
    java -jar lib\junit-platform-console-standalone-1.10.2.jar --class-path out --scan-classpath -cp out
    pause
    exit /b %ERRORLEVEL%
)

echo [2/2] Running...
echo.

:: Chay game
java -cp "out;resources" TerraIncognita.Main

pause
