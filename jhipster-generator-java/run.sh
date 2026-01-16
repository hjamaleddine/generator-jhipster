#!/bin/bash
#
# JHipster Java Generator - Run Script
#
# Usage:
#   ./run.sh [project-path] [options]
#
# Examples:
#   ./run.sh ./my-service --base-name=product --application-type=microservice
#   ./run.sh ./my-gateway --application-type=gateway --authentication-type=oauth2
#   ./run.sh --help
#

set -e

# Get script directory
SCRIPT_DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"
JAR_FILE="$SCRIPT_DIR/target/jhipster-generator-java-1.0.0-SNAPSHOT.jar"

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

echo ""
echo "╭───────────────────────────────────────────────────╮"
echo "│     JHipster Java Generator                       │"
echo "│     Java port of JHipster code generator          │"
echo "╰───────────────────────────────────────────────────╯"
echo ""

# Check if Java is installed
if ! command -v java &> /dev/null; then
    echo -e "${RED}Error: Java is not installed or not in PATH${NC}"
    echo "Please install Java 17 or higher"
    exit 1
fi

# Check Java version
JAVA_VERSION=$(java -version 2>&1 | head -n 1 | cut -d'"' -f2 | cut -d'.' -f1)
if [ "$JAVA_VERSION" -lt 17 ]; then
    echo -e "${YELLOW}Warning: Java 17+ is recommended. Found Java $JAVA_VERSION${NC}"
fi

# Check if jar exists, build if not
if [ ! -f "$JAR_FILE" ]; then
    echo -e "${YELLOW}JAR file not found. Building...${NC}"
    cd "$SCRIPT_DIR"

    # Check if Maven is installed
    if command -v mvn &> /dev/null; then
        mvn package -DskipTests -q
    elif [ -f "./mvnw" ]; then
        ./mvnw package -DskipTests -q
    else
        echo -e "${RED}Error: Maven is not installed and no wrapper found${NC}"
        echo "Please install Maven or run: mvn package"
        exit 1
    fi

    echo -e "${GREEN}Build complete!${NC}"
    echo ""
fi

# Run the generator
java -jar "$JAR_FILE" "$@"
