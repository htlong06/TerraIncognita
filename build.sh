#!/bin/bash
echo "========================================"
echo "  Terra Incognita - Build & Run"
echo "========================================"
echo

if ! command -v mvn >/dev/null 2>&1; then
    echo '[ERROR] Maven is required. Install Maven and make sure "mvn" is on PATH.'
    exit 1
fi

echo "[1/2] Compiling..."
mvn -q compile

if [ $? -ne 0 ]; then
    echo
    echo "[ERROR] Compilation failed!"
    exit 1
fi

# Neu truyen "test" -> chay test JUnit
if [ "$1" = "test" ]; then
    echo "[2/2] Running tests..."
    mvn -q test
    exit $?
fi

echo "[2/2] Running..."
echo

# Chay game
mvn -q exec:java
