#!/bin/bash
# ─────────────────────────────────────────────────────────────────────────────
#  MedLab CLI — Maven Build & Run Script  (Linux / macOS)
#  Requirements:
#    - JDK 17+
#    - Maven 3.6+  (mvn on PATH)
#    - MySQL 8.x running on localhost:3306
#
#  Optional DB config (override with env vars before running):
#    export MEDLAB_DB_URL="jdbc:mysql://localhost:3306/medlab_db?..."
#    export MEDLAB_DB_USER="root"
#    export MEDLAB_DB_PASSWORD="yourpassword"
# ─────────────────────────────────────────────────────────────────────────────

set -e

DB_URL="${MEDLAB_DB_URL:-jdbc:mysql://localhost:3306/medlab_db?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC}"
DB_USER="${MEDLAB_DB_USER:-root}"
DB_PASS="${MEDLAB_DB_PASSWORD:-root}"

echo "── Step 1: Check prerequisites ──────────────────────────────────────────"
if ! command -v javac &> /dev/null; then
    echo "ERROR: javac not found. Install JDK 17+."
    exit 1
fi
if ! command -v mvn &> /dev/null; then
    echo "ERROR: mvn not found. Install Maven 3.6+."
    exit 1
fi

echo "── Step 2: Build (compile + package fat JAR) ────────────────────────────"
mvn clean package -q -DskipTests
echo "Build successful → target/medlab.jar"

echo "── Step 3: Run ──────────────────────────────────────────────────────────"
java \
  -Dmedlab.db.url="$DB_URL" \
  -Dmedlab.db.user="$DB_USER" \
  -Dmedlab.db.password="$DB_PASS" \
  -jar target/medlab.jar
