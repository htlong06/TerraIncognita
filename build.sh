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
javac -d out -sourcepath src @sources.txt
rm sources.txt

if [ $? -ne 0 ]; then
    echo
    echo "[ERROR] Compilation failed!"
    exit 1
fi

echo "[2/2] Running..."
echo

# Chay game
java -cp "out:resources" TerraIncognita.Main
