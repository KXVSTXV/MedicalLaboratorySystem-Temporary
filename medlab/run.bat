@echo off
REM ─────────────────────────────────────────────────────────────────────────────
REM  MedLab CLI — Maven Build & Run Script  (Windows)
REM  Requirements:
REM    - JDK 17+
REM    - Maven 3.6+  (mvn on PATH)
REM    - MySQL 8.x running on localhost:3306
REM
REM  Optional DB config (set env vars before running):
REM    set MEDLAB_DB_URL=jdbc:mysql://localhost:3306/medlab_db?...
REM    set MEDLAB_DB_USER=root
REM    set MEDLAB_DB_PASSWORD=yourpassword
REM ─────────────────────────────────────────────────────────────────────────────

if not defined MEDLAB_DB_URL     set MEDLAB_DB_URL=jdbc:mysql://localhost:3306/medlab_db?useSSL=false^&allowPublicKeyRetrieval=true^&serverTimezone=UTC
if not defined MEDLAB_DB_USER    set MEDLAB_DB_USER=root
if not defined MEDLAB_DB_PASSWORD set MEDLAB_DB_PASSWORD=

echo -- Step 1: Check prerequisites --
javac -version >nul 2>&1 || (echo ERROR: javac not found. Install JDK 17+. && exit /b 1)
mvn -version >nul 2>&1   || (echo ERROR: mvn not found. Install Maven 3.6+. && exit /b 1)

echo -- Step 2: Build (compile + package fat JAR) --
mvn clean package -q -DskipTests
echo Build successful: target\medlab.jar

echo -- Step 3: Run --
java ^
  -Dmedlab.db.url="%MEDLAB_DB_URL%" ^
  -Dmedlab.db.user="%MEDLAB_DB_USER%" ^
  -Dmedlab.db.password="%MEDLAB_DB_PASSWORD%" ^
  -jar target\medlab.jar
