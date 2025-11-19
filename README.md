# Solver for the Union of the Theories of Equality, Lists, and Arrays

Planning and Automated Reasoning - Automated Reasoning
I term, Academic Year 2025-26

**Due Date:** January 31st, 2026

## Overview

This project implements a satisfiability solver for the union of three theories:
- **T_E**: Theory of Equality (Congruence Closure)
- **T_cons**: Theory of Lists (cons, car, cdr)
- **T_A**: Theory of Arrays (select, store)

The solver implements the congruence closure algorithm with the largest ccpar optimization, as described in Bradley & Manna, Sections 9.3-9.5.

## Project Structure

```
equality-lists-arrays-solver/
├── src/
│   ├── main/java/solver/          # Main source code (32 classes, 9 packages)
│   │   ├── core/                  # Congruence closure implementation (3 classes)
│   │   ├── dag/                   # DAG representation and term structures (6 classes)
│   │   ├── equivalence/           # Equivalence class management (2 classes)
│   │   ├── theory/                # Theory-specific procedures (5 classes)
│   │   │   ├── te/                # Theory of Equality (2 classes)
│   │   │   ├── tcons/             # Theory of Lists (2 classes)
│   │   │   └── tarray/            # Theory of Arrays (2 classes)
│   │   ├── parser/                # Input parsing - custom & SMT-LIB formats (11 classes)
│   │   ├── UnifiedSolver.java     # Main solver entry point
│   │   ├── Main.java              # CLI interface (custom format)
│   │   └── SMTLIBSolver.java      # CLI interface (SMT-LIB format)
│   └── test/java/solver/          # JUnit test cases (13 test classes, 188 tests)
│       ├── core/                  # Core algorithm tests (3 test classes)
│       ├── dag/                   # DAG structure tests (2 test classes)
│       ├── equivalence/           # Equivalence class tests (1 test class)
│       └── theory/                # Theory-specific tests (7 test classes)
├── tests/                         # Test suite
│   ├── README.md                  # Test suite documentation
│   ├── TEST_INDEX.md              # Comprehensive test catalog
│   └── input/                     # Test input files
│       ├── *.txt                  # Custom format tests (51 files)
│       └── smtlib/                # SMT-LIB format tests (5 files)
├── docs/                          # Documentation
│   ├── README.md                  # Documentation index
│   ├── PROJECT_PLAN.md            # Project timeline and phases
│   ├── ARCHITECTURE.md            # System architecture
│   ├── COMPLETION_STATUS.md       # Implementation status
│   ├── INPUT_FORMAT.md            # Input specification
│   ├── OUTPUT_FORMAT.md           # Output specification
│   ├── SMTLIB_PARSER.md           # SMT-LIB parser details
│   └── ...                        # Additional design docs
├── output/                        # Expected output files (for future use)
├── experiments/                   # Experimental results (for Phase 4)
├── assignment/                    # Course assignment description
│   └── ar-assignment.pdf
├── target/                        # Maven build output (not in git)
├── pom.xml                        # Maven build configuration
├── .gitignore                     # Git ignore rules
├── LICENSE                        # Project license
└── README.md                      # This file
```

## Prerequisites

- **Java Development Kit (JDK) 17 or higher**
  - Download from: https://adoptium.net/ or https://www.oracle.com/java/technologies/downloads/
  - Verify installation: `java -version`

- **Apache Maven 3.6 or higher**
  - Download from: https://maven.apache.org/download.cgi
  - Verify installation: `mvn -version`

## Building the Project

### Compile the source code:
```bash
mvn compile
```

### Run tests:
```bash
mvn test
```

### Build executable JAR:
```bash
mvn package
```

This creates two JAR files in the `target/` directory:
- `equality-lists-arrays-solver-1.0-SNAPSHOT.jar` - regular JAR
- `equality-lists-arrays-solver-1.0-SNAPSHOT-standalone.jar` - fat JAR with all dependencies

### Clean build artifacts:
```bash
mvn clean
```

## Running the Solver

### Using Maven:
```bash
mvn exec:java -Dexec.mainClass="solver.Main"
```

### Using the compiled JAR:
```bash
java -jar target/equality-lists-arrays-solver-1.0-SNAPSHOT-standalone.jar
```

### Reading from stdin:
```bash
echo "your input here" | java -jar target/equality-lists-arrays-solver-1.0-SNAPSHOT-standalone.jar
```

### Reading from a file:
```bash
java -jar target/equality-lists-arrays-solver-1.0-SNAPSHOT-standalone.jar < tests/example1.txt
```

## Input Formats

The solver supports two input formats:

### 1. Custom Format (Simple)
```
a = b
b = c
c != a
```

### 2. SMT-LIB 2.0 Format (QF_UF logic)
```smt2
(set-logic QF_UF)
(declare-fun a () Int)
(declare-fun b () Int)
(assert (= a b))
(assert (= b c))
(assert (not (= c a)))
(check-sat)
```

For complete format specifications, see:
- [docs/INPUT_FORMAT.md](docs/INPUT_FORMAT.md) - Custom format
- [docs/SMTLIB_PARSER.md](docs/SMTLIB_PARSER.md) - SMT-LIB format

## Output Format

The solver outputs either:
- `SAT` - the literal set is satisfiable
- `UNSAT` - the literal set is unsatisfiable

Optional information may include:
- Equivalence classes (for SAT)
- Conflict explanation (for UNSAT)
- Runtime statistics

## Development Status

See [docs/PROJECT_PLAN.md](docs/PROJECT_PLAN.md) for detailed implementation timeline and progress.

Current Status:
- ✅ Phase 1: Setup & Planning - COMPLETE
- ✅ Phase 2: Core Implementation - COMPLETE
- ✅ Phase 3: Input/Output & Interface - COMPLETE
- ✅ Phase 3.5: Repository Reorganization & Cleanup - IN PROGRESS
- ⏳ Phase 4: Testing & Experimentation - PENDING
- ⏳ Phase 5: Optional Optimizations - PENDING
- ⏳ Phase 6: Report Writing - PENDING
- ⏳ Phase 7: Submission Preparation - PENDING

## Quick Start

```bash
# Clone and navigate to the project
cd equality-lists-arrays-solver

# Build the project
mvn clean package

# Run a simple test
java -jar target/equality-lists-arrays-solver-1.0-SNAPSHOT-standalone.jar < tests/input/test_te_sat.txt

# Expected output: SAT
```

## Documentation

For comprehensive documentation, see the [docs/](docs/) directory:
- **Architecture**: [docs/ARCHITECTURE.md](docs/ARCHITECTURE.md)
- **Input Format**: [docs/INPUT_FORMAT.md](docs/INPUT_FORMAT.md)
- **Output Format**: [docs/OUTPUT_FORMAT.md](docs/OUTPUT_FORMAT.md)
- **Complete Documentation Index**: [docs/README.md](docs/README.md)

## References

1. Bradley & Manna - *The Calculus of Computation*, Sections 9.3-9.5
2. Kroening & Strichman - *Decision Procedures*
3. Nelson & Oppen - Congruence Closure foundations

## License

See [LICENSE](LICENSE) file for details.
