#!/bin/bash
# ─────────────────────────────────────────────────────────────
#  MedLab CLI — Build & Run Script
#  Requirements: JDK 17+, sqlite-jdbc jar in ./lib/
# ─────────────────────────────────────────────────────────────

set -e

JAR_NAME="medlab.jar"
LIB="lib/sqlite-jdbc-3.45.1.0.jar"   # adjust version if needed
SRC_DIR="src/main/java"
OUT_DIR="out"
MAIN_CLASS="com.medlab.Main"

echo "── Step 1: Check prerequisites ──────────────────────────"
if ! command -v javac &> /dev/null; then
    echo "ERROR: javac not found. Install JDK 17+."
    exit 1
fi
if [ ! -f "$LIB" ]; then
    echo "ERROR: SQLite JDBC driver not found at $LIB"
    echo "Download from: https://github.com/xerial/sqlite-jdbc/releases"
    echo "Place the .jar in the lib/ directory."
    exit 1
fi

echo "── Step 2: Compile ──────────────────────────────────────"
mkdir -p $OUT_DIR
find $SRC_DIR -name "*.java" > sources.txt
javac -cp "$LIB" -d $OUT_DIR @sources.txt
rm sources.txt
echo "Compilation successful."

echo "── Step 3: Package as JAR ───────────────────────────────"
# Create manifest
echo "Main-Class: $MAIN_CLASS" > manifest.txt
echo "Class-Path: $LIB" >> manifest.txt
jar cfm $JAR_NAME manifest.txt -C $OUT_DIR .
rm manifest.txt
echo "Packaged as $JAR_NAME"

echo "── Step 4: Run ──────────────────────────────────────────"
java -cp "$JAR_NAME:$LIB" $MAIN_CLASS
