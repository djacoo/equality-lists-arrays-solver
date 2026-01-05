# Decision Procedure Solver for the Union of Theories T_E, T_cons, and T_A

[![CI Build](https://github.com/djacoo/equality-lists-arrays-solver/actions/workflows/ci.yml/badge.svg?branch=main)](https://github.com/djacoo/equality-lists-arrays-solver/actions/workflows/ci.yml)
[![Java](https://img.shields.io/badge/Java-17%2B-blue)](https://adoptium.net/)
[![Maven](https://img.shields.io/badge/Maven-3.6%2B-C71A36)](https://maven.apache.org/)
[![Tests](https://img.shields.io/badge/tests-571%20passing-success)](https://github.com/djacoo/equality-lists-arrays-solver/actions)
[![License](https://img.shields.io/badge/License-Academic-orange)](LICENSE)
[![Code Quality](https://img.shields.io/badge/code%20coverage-comprehensive-brightgreen)](https://github.com/djacoo/equality-lists-arrays-solver/tree/main/src/test)

Jacopo Parretti (VR536104)
University of Verona
Planning and Automated Reasoning, Academic Year 2025-26

## Abstract

This project implements a decision procedure solver for determining the satisfiability of conjunctions of literals in the union of three quantifier-free first-order theories:

- **T_E**: Theory of Equality with Uninterpreted Functions (Congruence Closure)
- **T_cons**: Theory of Non-empty Possibly Cyclic Lists (cons, car, cdr operations)
- **T_A**: Theory of Arrays without Extensionality (select, store operations)

The implementation faithfully follows the algorithms presented in Bradley & Manna (2007), *The Calculus of Computation*, Sections 9.3, 9.4, and 9.5. The core algorithm is congruence closure on directed acyclic graphs (DAGs), augmented with theory-specific procedures for lists and arrays.

**Key Features:**
- Mandatory optimization: Largest ccpar heuristic in UNION operation
- Optional optimizations: Forbidden set (early UNSAT detection) and path compression (faster FIND)
- Comprehensive test suite: 571 JUnit tests (100% passing) + 56 integration tests
- Support for both custom and SMT-LIB input formats

## Prerequisites

- **Java Development Kit (JDK) 17 or higher**
  Verify: `java -version` (must show 17 or higher)

- **Apache Maven 3.6 or higher**
  Verify: `mvn -version`

## Compilation Instructions

###  Step 1: Compile Source Code

```bash
mvn compile
```

This compiles all Java source files to `target/classes/`.

### Step 2: Run Test Suite (Optional but Recommended)

```bash
mvn test
```

Expected output: `Tests run: 571, Failures: 0, Errors: 0, Skipped: 0`

### Step 3: Build Standalone Executable JAR

```bash
mvn clean package
```

This creates: `target/equality-lists-arrays-solver-1.0-SNAPSHOT-standalone.jar`

The generated JAR is self-contained and includes all dependencies.

## Execution Instructions

### Using Standalone JAR (Recommended)

#### Read from Standard Input (stdin)

```bash
java -jar target/equality-lists-arrays-solver-1.0-SNAPSHOT-standalone.jar
```

Then enter literals line-by-line. Press **Ctrl+D** (Unix/Mac) or **Ctrl+Z** (Windows) to finish input.

#### Read from File (as Command-Line Argument)

```bash
java -jar target/equality-lists-arrays-solver-1.0-SNAPSHOT-standalone.jar tests/input/te/test_te_sat.txt
```

#### Read from File (via stdin Redirection)

```bash
java -jar target/equality-lists-arrays-solver-1.0-SNAPSHOT-standalone.jar < tests/input/te/test_te_unsat.txt
```

or

```bash
cat tests/input/te/test_te_unsat.txt | java -jar target/equality-lists-arrays-solver-1.0-SNAPSHOT-standalone.jar
```

#### Display Help Message

```bash
java -jar target/equality-lists-arrays-solver-1.0-SNAPSHOT-standalone.jar --help
```

### Using Maven Exec Plugin (Alternative)

```bash
mvn exec:java -Dexec.mainClass="solver.Main" < tests/input/te/test_te_sat.txt
```

### Using SMT-LIB Format

For SMT-LIB 2.0 format files (QF_UF logic):

```bash
java -cp target/equality-lists-arrays-solver-1.0-SNAPSHOT-standalone.jar solver.SMTLIBSolver tests/input/smtlib/simple_sat.smt2
```

## Input Format

### Custom Format

The solver accepts a simple, human-readable format with one literal per line:

```
# Comments start with # or //

# Equalities
a = b
f(x) = g(y)

# Disequalities
x != y
select(arr, i) != v

# Atom predicates (for T_cons)
atom(x)
!atom(cons(a, b))
```

**Theory-Specific Functions:**

| Theory | Function | Arity | Description |
|--------|----------|-------|-------------|
| T_cons | `cons(x, y)` | 2 | Construct list with head x and tail y |
| T_cons | `car(list)` | 1 | Extract head of list |
| T_cons | `cdr(list)` | 1 | Extract tail of list |
| T_A | `select(array, index)` | 2 | Read array at index |
| T_A | `store(array, index, value)` | 3 | Write value to array at index |
| T_E | `f(...)`, `g(...)` | any | Uninterpreted functions |

**Example Input Files:**

- `tests/input/te/` - T_E examples (equality with uninterpreted functions)
- `tests/input/tcons/` - T_cons examples (list operations)
- `tests/input/tarray/` - T_A examples (array operations)
- `tests/input/combined/` - Mixed theory examples
- `tests/input/smtlib/` - SMT-LIB format examples

See [docs/INPUT_FORMAT.md](docs/INPUT_FORMAT.md) for complete grammar specification.

### SMT-LIB 2.0 Format

Standard SMT-LIB format is also supported for compatibility with benchmarks:

```smt2
(set-logic QF_UF)
(declare-fun a () Int)
(declare-fun b () Int)
(assert (= a b))
(assert (not (= b a)))
(check-sat)
```

## Output Format

### SAT Case

```
Result: SAT
Model (Equivalence Classes):
  [1] {a, b, c}
  [2] {f(a), f(b)}
  [3] {x}
```

All terms in the same equivalence class are equal in the satisfying model.

### UNSAT Case

```
Result: UNSAT
Why UNSAT:
  Disequality violation: Found x ≠ y but FIND(x) = FIND(y) after propagation
```

The solver provides a brief explanation of the unsatisfiability.

## Algorithm Overview

### 1. Congruence Closure (T_E-Procedure)

**Based on:** Bradley & Manna, Section 9.3

The congruence closure algorithm decides satisfiability for T_E using:

- **DAG Representation:** All terms represented as nodes in a directed acyclic graph
- **Equivalence Classes:** UNION-FIND data structure with path compression
- **ccpar Sets:** For each term t, ccpar(t) = {u | t appears as an argument in u}
- **MERGE Propagation:** Queue-based congruence propagation using pending list

**Key Operations:**
- `FIND(t)`: Returns representative of equivalence class containing t
- `UNION(s, t)`: Merges classes, selecting representative with largest ccpar (mandatory optimization)
- `CONGRUENT(f(...), g(...))`: Checks if two function applications are congruent

### 2. T_cons-Procedure (Theory of Lists)

**Based on:** Bradley & Manna, Section 9.4

Integrates list axioms into congruence closure:
- car(cons(x,y)) = x
- cdr(cons(x,y)) = y

For each cons(x,y) term in the DAG, the solver adds the above equality assertions and invokes the T_E-procedure on the augmented formula.

### 3. T_A-Procedure (Theory of Arrays)

**Based on:** Bradley & Manna, Section 9.5

Handles array operations through store decomposition:

For each `store(a, i, v)` term, create two subproblems:
1. **Case i = j:** Replace select(store(a,i,v),j) with v
2. **Case i ≠ j:** Replace select(store(a,i,v),j) with select(a,j)

Recursively solve subproblems until no store operations remain, then apply T_cons or T_E procedure.

### 4. Optimizations

**Mandatory: Largest ccpar in UNION**
When merging equivalence classes, choose the representative with the larger ccpar set. This reduces the number of congruence checks during propagation.
*References:* Downey et al. (1980), p. 761; Detlefs et al. (2005), p. 423

**Optional: Forbidden Set**
Maintain term pairs (s,t) from disequality constraints s ≠ t. Check before merging: if (FIND(s), FIND(t)) is forbidden, immediately return UNSAT.
*Reference:* Detlefs et al. (2005), p. 388

**Optional: Path Compression**
Iterative FIND with two-pass path compression for amortized O(α(n)) complexity.
*Reference:* Tarjan (1975)

## Testing and Validation

### Test Suite Organization

**Total Tests:** 571 JUnit tests + 56 integration test files

**Unit Tests (515):**
- Core CC algorithm: 89 tests
- DAG representation: 86 tests
- Equivalence classes: 43 tests
- Theory procedures: 73 tests
- Parsers: 136 tests
- Optimizations: 40 tests
- Performance: 48 tests

**Integration Tests (56 test files):**
- T_E: 14 tests (from Bradley & Manna Section 9.3)
- T_cons: 13 tests (from Bradley & Manna Section 9.4)
- T_A: 14 tests (from Bradley & Manna Section 9.5 + Kroening & Strichman)
- Combined: 10 tests (mixed theories)
- SMT-LIB: 5 tests (QF_UF benchmarks)

All test sources are documented in [tests/TEST_INDEX.md](tests/TEST_INDEX.md).

### Running Tests

```bash
# Run all tests
mvn test

# Run specific test class
mvn test -Dtest=TEProcedureTest
mvn test -Dtest=TConsProcedureTest
mvn test -Dtest=TArrayProcedureTest
mvn test -Dtest=UnifiedSolverTest
```

### Correctness Results

- **T_E tests:** 14/14 passing (100%)
- **T_cons tests:** 13/13 passing (100%)
- **T_A tests:** 14/14 passing (100%)
- **Combined tests:** 10/10 passing (100%)
- **SMT-LIB tests:** 5/5 passing (100%)

All textbook examples from Bradley & Manna produce correct results.

## Project Structure

```
equality-lists-arrays-solver/
├── README.md                      # This file
├── pom.xml                        # Maven build configuration
├── LICENSE                        # Project license
├── src/                           # Source code
│   ├── main/java/solver/          # Implementation
│   │   ├── core/                  # Congruence closure algorithm
│   │   ├── dag/                   # DAG representation
│   │   ├── equivalence/           # Equivalence class management
│   │   ├── theory/                # Theory-specific procedures (T_E, T_cons, T_A)
│   │   ├── parser/                # Input parsing (custom + SMT-LIB)
│   │   ├── config/                # Solver configuration
│   │   ├── UnifiedSolver.java     # Main solver orchestration
│   │   ├── Main.java              # CLI for custom format
│   │   └── SMTLIBSolver.java      # CLI for SMT-LIB format
│   └── test/java/solver/          # JUnit 5 test suite (571 tests)
├── tests/                         # Integration test files
│   ├── input/                     # Test input files (56 files)
│   │   ├── te/                    # T_E tests
│   │   ├── tcons/                 # T_cons tests
│   │   ├── tarray/                # T_A tests
│   │   ├── combined/              # Mixed theory tests
│   │   └── smtlib/                # SMT-LIB format tests
│   └── TEST_INDEX.md              # Complete test catalog with sources
├── docs/                          # Technical documentation
│   ├── ARCHITECTURE.md            # System architecture and design
│   └── INPUT_FORMAT.md            # Complete input format specification
├── experiments/                   # Performance analysis
│   ├── run_experiments.py         # Automated test execution
│   ├── analyze_results.py         # Performance analysis
│   └── results.csv                # Experimental results
├── bin/                           # Compiled executable
│   └── solver.jar                 # Standalone JAR (copy of target/...jar)
├── output/                        # Directory for generated outputs
└── report/                        # Technical report
    ├── report.pdf                 # Project report
    └── report.tex                 # LaTeX source
```

## References

This implementation is based on the following sources:

1. **Bradley, A. R., & Manna, Z.** (2007). *The Calculus of Computation: Decision Procedures with Applications to Verification*. Springer.
   - Section 9.3: T_E-procedure and Congruence Closure
   - Section 9.4: T_cons-procedure (Theory of Lists)
   - Section 9.5: T_A-procedure (Theory of Arrays)

2. **Kroening, D., & Strichman, O.** (2008). *Decision Procedures: An Algorithmic Point of View*. Springer.
   - Additional test cases for arrays and equality

3. **Downey, P. J., Sethi, R., & Tarjan, R. E.** (1980). Variations on the Common Subexpression Problem. *Journal of the ACM*, 27(4), 758-771.
   - Page 761: Largest ccpar optimization

4. **Detlefs, D., Nelson, G., & Saxe, J. B.** (2005). Simplify: A Theorem Prover for Program Checking. *Journal of the ACM*, 52(3), 365-473.
   - Page 423: ccpar optimization analysis
   - Page 388: Forbidden list optimization

5. **Nelson, G., & Oppen, D. C.** (1980). Fast Decision Procedures Based on Congruence Closure. *Journal of the ACM*, 27(2), 356-364.
   - Original congruence closure algorithm

6. **Tarjan, R. E.** (1975). Efficiency of a Good But Not Linear Set Union Algorithm. *Journal of the ACM*, 22(2), 215-225.
   - Path compression and UNION-FIND complexity

## Documentation

Comprehensive technical documentation is available in the [docs/](docs/) directory:

- **[ARCHITECTURE.md](docs/ARCHITECTURE.md)** - System architecture, component interactions, and design decisions
- **[INPUT_FORMAT.md](docs/INPUT_FORMAT.md)** - Complete input format specification with formal grammar
- **[tests/TEST_INDEX.md](tests/TEST_INDEX.md)** - Complete catalog of all 56 integration tests with source attributions

## Troubleshooting

### Build Issues

**Problem:** Compilation errors
**Solution:**
```bash
mvn clean compile
```

**Problem:** Cannot find JAR file
**Solution:**
```bash
mvn clean package
ls -lh target/*.jar
```

### Runtime Issues

**Problem:** Java version mismatch
**Solution:**
```bash
java -version  # Must be 17 or higher
export JAVA_HOME=$(/usr/libexec/java_home -v 17)  # macOS
```

**Problem:** Memory issues for large inputs
**Solution:**
```bash
java -Xmx2g -jar target/equality-lists-arrays-solver-1.0-SNAPSHOT-standalone.jar input.txt
```

## License

This project is an academic assignment for the Planning and Automated Reasoning course. All rights reserved.

## Author

Jacopo Parretti (VR536104)
University of Verona
Planning and Automated Reasoning, Academic Year 2025-26
