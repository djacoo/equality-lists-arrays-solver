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
│   ├── main/java/solver/     # Main source code
│   └── test/java/solver/     # JUnit test cases
├── tests/                     # Test input files
├── output/                    # Expected output files
├── docs/                      # Additional documentation
├── experiments/               # Experimental results and analysis
├── assignment/                # Assignment description
├── pom.xml                    # Maven build configuration
└── README.md                  # This file
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

## Input Format

(To be defined as the parser is implemented)

Example:
```
a = b
b = c
c != a
```

## Output Format

The solver outputs either:
- `SAT` - the literal set is satisfiable
- `UNSAT` - the literal set is unsatisfiable

Optional information may include:
- Equivalence classes (for SAT)
- Conflict explanation (for UNSAT)
- Runtime statistics

## Development Status

See [PROJECT_PLAN.md](PROJECT_PLAN.md) for detailed implementation timeline and progress.

## References

1. Bradley & Manna - *The Calculus of Computation*, Sections 9.3-9.5
2. Kroening & Strichman - *Decision Procedures*
3. Nelson & Oppen - Congruence Closure foundations

## License

See [LICENSE](LICENSE) file for details.
