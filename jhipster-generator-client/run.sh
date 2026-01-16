#!/bin/bash
#
# JHipster Client Generator - Run Script
#
# Usage:
#   ./run.sh [project-path] [options]
#
# Examples:
#   ./run.sh ./my-app --framework=angular
#   ./run.sh ./my-app --framework=react --cypress=true
#   ./run.sh ./my-app --framework=vue --bundler=vite
#   ./run.sh --help
#

set -e

# Get script directory
SCRIPT_DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"
JAR_FILE="$SCRIPT_DIR/target/jhipster-generator-client-1.0.0-SNAPSHOT-jar-with-dependencies.jar"

# Check if jar exists
if [ ! -f "$JAR_FILE" ]; then
    echo "JAR file not found. Building..."
    cd "$SCRIPT_DIR"
    mvn package -DskipTests -q
fi

# Run the generator
java -jar "$JAR_FILE" "$@"
