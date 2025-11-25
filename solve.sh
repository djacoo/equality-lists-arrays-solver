#!/bin/bash
# Solver for the Union of the Theories of Equality, Lists, and Arrays
# Quick execution script

set -e

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# JAR file location
JAR="target/equality-lists-arrays-solver-1.0-SNAPSHOT-standalone.jar"

# Check if JAR exists
if [ ! -f "$JAR" ]; then
    echo -e "${RED}Error: JAR file not found!${NC}"
    echo -e "${YELLOW}Building the project...${NC}"
    mvn clean package -DskipTests
    echo -e "${GREEN}Build complete!${NC}"
fi

# Display usage if --help is provided
if [ "$1" == "--help" ] || [ "$1" == "-h" ]; then
    echo "Usage: ./solve.sh [OPTIONS] [INPUT_FILE]"
    echo ""
    echo "Options:"
    echo "  (no args)           Run in interactive mode (type input, press Enter twice to finish)"
    echo "  -f, --file FILE     Run solver with input from FILE"
    echo "  -e, --example       Show example inputs"
    echo "  -l, --list          List all available test files"
    echo "  -t, --test FILE     Run a test from tests/input/ directory"
    echo "  -h, --help          Show this help message"
    echo ""
    echo "Examples:"
    echo "  ./solve.sh                                    # Interactive mode"
    echo "  ./solve.sh -f mytest.txt                      # Run from file"
    echo "  ./solve.sh -t te/test_te_sat.txt              # Run from tests/input/"
    echo "  echo 'a = b' | ./solve.sh                     # Pipe input"
    exit 0
fi

# List test files
if [ "$1" == "--list" ] || [ "$1" == "-l" ]; then
    echo -e "${GREEN}Available test files:${NC}"
    find tests/input -name "*.txt" -type f | sort
    exit 0
fi

# Show examples
if [ "$1" == "--example" ] || [ "$1" == "-e" ]; then
    echo -e "${GREEN}Example inputs:${NC}"
    echo ""
    echo -e "${YELLOW}1. Basic Equality (SAT):${NC}"
    echo "  a = b"
    echo "  b = c"
    echo ""
    echo -e "${YELLOW}2. Contradiction (UNSAT):${NC}"
    echo "  x = y"
    echo "  y = z"
    echo "  z != x"
    echo ""
    echo -e "${YELLOW}3. Lists:${NC}"
    echo "  cons(h, t) = mylist"
    echo "  car(mylist) = h"
    echo ""
    echo -e "${YELLOW}4. Arrays:${NC}"
    echo "  store(a, i, v) = a2"
    echo "  select(a2, i) = v"
    echo ""
    echo -e "${YELLOW}5. Functions:${NC}"
    echo "  f(x) = y"
    echo "  f(x) != y"
    exit 0
fi

# Run from test directory
if [ "$1" == "--test" ] || [ "$1" == "-t" ]; then
    if [ -z "$2" ]; then
        echo -e "${RED}Error: No test file specified${NC}"
        echo "Use: ./solve.sh -t te/test_te_sat.txt"
        exit 1
    fi
    TEST_FILE="tests/input/$2"
    if [ ! -f "$TEST_FILE" ]; then
        echo -e "${RED}Error: Test file not found: $TEST_FILE${NC}"
        exit 1
    fi
    echo -e "${GREEN}Running test: $TEST_FILE${NC}"
    echo ""
    java -jar "$JAR" "$TEST_FILE"
    exit 0
fi

# Run from file
if [ "$1" == "--file" ] || [ "$1" == "-f" ]; then
    if [ -z "$2" ]; then
        echo -e "${RED}Error: No file specified${NC}"
        exit 1
    fi
    if [ ! -f "$2" ]; then
        echo -e "${RED}Error: File not found: $2${NC}"
        exit 1
    fi
    java -jar "$JAR" "$2"
    exit 0
fi

# If a file is provided as first argument (without flag)
if [ -n "$1" ] && [ -f "$1" ]; then
    java -jar "$JAR" "$1"
    exit 0
fi

# Default: run in interactive or pipe mode
if [ -t 0 ]; then
    # Interactive mode (terminal input)
    echo -e "${GREEN}Solver running in interactive mode${NC}"
    echo -e "${YELLOW}Type your constraints (one per line), then press Enter twice to solve${NC}"
    echo ""
fi

java -jar "$JAR"
