@echo off
REM ─────────────────────────────────────────────────────────────
REM  MedLab CLI — Build & Run Script (Windows)
REM  Requirements: JDK 17+, sqlite-jdbc jar in .\lib\
REM ─────────────────────────────────────────────────────────────

set JAR_NAME=medlab.jar
set LIB=lib\sqlite-jdbc-3.45.1.0.jar
set SRC_DIR=src\main\java
set OUT_DIR=out
set MAIN_CLASS=com.medlab.Main

echo -- Step 1: Check prerequisites --
javac -version >nul 2>&1 || (echo ERROR: javac not found. Install JDK 17+. && exit /b 1)
if not exist %LIB% (
    echo ERROR: SQLite JDBC driver not found at %LIB%
    echo Download from: https://github.com/xerial/sqlite-jdbc/releases
    exit /b 1
)

echo -- Step 2: Compile --
if not exist %OUT_DIR% mkdir %OUT_DIR%
dir /s /b %SRC_DIR%\*.java > sources.txt
javac -cp %LIB% -d %OUT_DIR% @sources.txt
del sources.txt
echo Compilation successful.

echo -- Step 3: Package --
echo Main-Class: %MAIN_CLASS%> manifest.txt
echo Class-Path: %LIB%>> manifest.txt
jar cfm %JAR_NAME% manifest.txt -C %OUT_DIR% .
del manifest.txt
echo Packaged as %JAR_NAME%

echo -- Step 4: Run --
java -cp "%JAR_NAME%;%LIB%" %MAIN_CLASS%
