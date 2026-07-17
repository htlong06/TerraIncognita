#!/bin/bash
echo "========================================"
echo "  Terra Incognita - Build & Run"
echo "========================================"
echo

# Tao thu muc output neu chua co
mkdir -p out

# Bien dich tat ca file Java
echo "[1/2] Compiling..."
find src -name "*.java" > sources.txt
javac -d out -sourcepath src -cp "lib/*" @sources.txt
rm sources.txt

if [ $? -ne 0 ]; then
    echo
    echo "[ERROR] Compilation failed!"
    exit 1
fi

# Neu truyen "test" -> chay test JUnit
if [ "$1" = "test" ]; then
    echo "[2/2] Running tests..."
    echo
    java -jar lib/junit-platform-console-standalone-1.10.2.jar --class-path "out;lib/sqlite-jdbc-3.46.1.3.jar" --scan-classpath
    exit $?
fi

echo "[2/2] Running..."
echo

# Chay game
java -cp "out:resources" TerraIncognita.Main
