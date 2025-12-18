# Solver for the Union of the Theories of Equality, Lists, and Arrays

Planning and Automated Reasoning - Automated Reasoning
I term, Academic Year 2025-26

**Due Date:** January 31st, 2026

## Overview

This project implements a decision procedure (satisfiability solver) for the union of three first-order theories:
- **T_E**: Theory of Equality with Uninterpreted Functions (Congruence Closure)
- **T_cons**: Theory of Lists (cons, car, cdr, atom)
- **T_A**: Theory of Arrays (select, store)

The solver is based on the algorithms described in:
- **Bradley & Manna**, *The Calculus of Computation*, Sections 9.3-9.5
- **Kroening & Strichman**, *Decision Procedures*

### Key Features

✅ **Complete Implementation** of CC algorithm with largest ccpar optimization
✅ **Multi-theory Support**: T_E, T_cons, and T_A with automatic theory detection
✅ **Two Input Formats**: Custom format and SMT-LIB 2.0 (QF_UF logic)
✅ **Comprehensive Testing**: 571 tests covering all theories and edge cases
✅ **Optional Optimizations**: Forbidden set (early UNSAT detection), path compression
✅ **Performance Tuned**: 1.62x speedup with optimizations enabled

## Project Structure

```
equality-lists-arrays-solver/
├── src/
│   ├── main/java/solver/          # Main source code
│   │   ├── core/                  # Congruence closure (CC algorithm)
│   │   ├── dag/                   # DAG representation & term structures
│   │   ├── equivalence/           # Equivalence class management
│   │   ├── theory/                # Theory-specific procedures
│   │   │   ├── te/                # T_E: Theory of Equality
│   │   │   ├── tcons/             # T_cons: Theory of Lists
│   │   │   └── tarray/            # T_A: Theory of Arrays
│   │   ├── parser/                # Input parsing (custom + SMT-LIB)
│   │   ├── config/                # Solver configuration
│   │   ├── testing/               # Test generation utilities
│   │   ├── UnifiedSolver.java     # Main solver entry point
│   │   ├── Main.java              # CLI (custom format)
│   │   └── SMTLIBSolver.java      # CLI (SMT-LIB format)
│   └── test/java/solver/          # JUnit 5 test suite (571 tests)
├── tests/                         # Test input files
│   ├── input/                     # Custom format tests
│   └── smtlib/                    # SMT-LIB format tests
├── output/                        # Expected outputs (for verification)
├── docs/                          # Documentation
│   ├── PROJECT_PLAN.md            # Project timeline & phases
│   ├── ARCHITECTURE.md            # System design & implementation
│   └── INPUT_FORMAT.md            # Input format specifications
├── experiments/                   # Performance analysis & benchmarks
├── assignment/                    # Course materials
├── pom.xml                        # Maven build configuration
└── README.md                      # This file
```

## Prerequisites

- **Java Development Kit (JDK) 17 or higher**
  - Download: [https://adoptium.net/](https://adoptium.net/)
  - Verify: `java -version`

- **Apache Maven 3.6 or higher**
  - Download: [https://maven.apache.org/download.cgi](https://maven.apache.org/download.cgi)
  - Verify: `mvn -version`

## Building the Project

### Compile:
```bash
mvn compile
```

### Run all tests (571 tests):
```bash
mvn test
```

### Build standalone JAR:
```bash
mvn clean package
```

This creates: `target/equality-lists-arrays-solver-1.0-SNAPSHOT-standalone.jar`

## Running the Solver

### Option 1: Using Maven
```bash
mvn exec:java -Dexec.mainClass="solver.Main"
```

### Option 2: Using standalone JAR
```bash
java -jar target/equality-lists-arrays-solver-1.0-SNAPSHOT-standalone.jar
```

### With input file:
```bash
java -jar target/equality-lists-arrays-solver-1.0-SNAPSHOT-standalone.jar tests/input/test_te_unsat.txt
```

### From stdin:
```bash
java -jar target/equality-lists-arrays-solver-1.0-SNAPSHOT-standalone.jar
# Type your literals, press Enter twice when done
```

### Show help:
```bash
java -jar target/equality-lists-arrays-solver-1.0-SNAPSHOT-standalone.jar --help
```

### For SMT-LIB format:
```bash
java -cp target/equality-lists-arrays-solver-1.0-SNAPSHOT-standalone.jar solver.SMTLIBSolver tests/smtlib/example.smt2
```

## Input Format

The solver supports two input formats:

### 1. Custom Format (Recommended)

Simple, human-readable format with one literal per line:

```
# Theory of Equality (T_E)
a = b
b = c
c != a        # This will be UNSAT
```

```
# Theory of Lists (T_cons)
cons(x, y) = z
car(z) = x
cdr(z) = y
atom(x)
!atom(z)
```

```
# Theory of Arrays (T_A)
select(store(a, i, v), j) = w
i = j
v = w
```

**Supported Literals:**
- **Equalities**: `t1 = t2` where t1, t2 are terms
- **Disequalities**: `t1 != t2`
- **Atom predicates**: `atom(t)` or `!atom(t)` (for lists)
- **Comments**: `# comment` or `// comment`
- **Terms**: Variables (`x`, `y`), constants (`0`, `42`), function applications (`f(x,y)`)

**Theory Functions:**
- **Lists**: `cons(x,y)`, `car(l)`, `cdr(l)`
- **Arrays**: `select(a,i)`, `store(a,i,v)`
- **Uninterpreted**: Any other function symbol

See [docs/INPUT_FORMAT.md](docs/INPUT_FORMAT.md) for complete specification.

### 2. SMT-LIB 2.0 Format

Standard SMT-LIB format (QF_UF logic):

```smt2
(set-logic QF_UF)
(declare-fun a () Int)
(declare-fun b () Int)
(declare-fun c () Int)
(assert (= a b))
(assert (= b c))
(assert (not (= c a)))
(check-sat)
```

## Examples

### Example 1: Simple Equality (SAT)
**Input** (`tests/input/test_te_sat.txt`):
```
a = b
b = c
```

**Output**:
```
Result: SAT
Model (Equivalence Classes):
  [1] {a, b, c}
```

### Example 2: Equality Contradiction (UNSAT)
**Input** (`tests/input/test_te_unsat.txt`):
```
a = b
b = c
c != a
```

**Output**:
```
Result: UNSAT
Why UNSAT:
  Disequality violation: Found c ≠ a but FIND(c) = FIND(a) after propagation
```

### Example 3: Congruence Closure (UNSAT)
**Input** (`tests/input/test_te_congruence_unsat.txt`):
```
a = b
f(a) != f(b)
```

**Output**:
```
Result: UNSAT
Why UNSAT:
  Congruence violation: Terms f(a) and f(b) must be equal by congruence
```

### Example 4: Lists (SAT)
**Input** (`tests/input/test_tcons_car_cdr_sat.txt`):
```
cons(x, y) = z
car(z) = x
cdr(z) = y
atom(x)
```

**Output**:
```
Result: SAT
```

### Example 5: Arrays (SAT)
**Input** (`tests/input/test_tarray_select_sat.txt`):
```
select(store(a, i, v), j) = w
i = j
v = w
```

**Output**:
```
Result: SAT
```

### Example 6: Mixed Theories
**Input**:
```
# Lists and equality
cons(a, b) = l
car(l) = x
a = x        # Should be SAT
```

**Output**:
```
Result: SAT
```

## Algorithm Overview

The solver implements **Congruence Closure (CC)** as described in Bradley & Manna Section 9.3:

1. **DAG Construction**: Build directed acyclic graph from input terms
2. **Equivalence Classes**: Manage terms using UNION-FIND data structure
3. **Merge Propagation**: Use pending list to propagate equalities through congruence
4. **Theory Integration**:
   - **T_cons**: Add axioms car(cons(x,y))=x, cdr(cons(x,y))=y
   - **T_A**: Decompose store operations into multiple subproblems

### Optimizations

- **Largest ccpar** (Mandatory): Merge smaller class into larger when performing UNION
- **Forbidden set** (Optional): Early detection of disequality violations during MERGE
- **Path compression** (Optional): Amortized O(α(n)) FIND operations

With all optimizations enabled: **1.62x speedup** (Phase 5 benchmarks)

## Testing

The project includes a comprehensive test suite:

- **571 total tests** (100% passing)
- **13 test classes** covering:
  - Core algorithms (CC, MERGE, FIND, UNION)
  - Each theory (T_E, T_cons, T_A)
  - Parser correctness
  - Edge cases and error handling
  - Performance benchmarks
  - Integration tests

### Run Tests:
```bash
mvn test                    # All tests
mvn test -Dtest=TEProcedureTest   # Specific test class
```

### Test Organization:
- `tests/input/` - Manual test files from textbooks
- `tests/smtlib/` - SMT-LIB format tests
- `src/test/java/` - JUnit test suite

See [tests/README.md](tests/README.md) for test catalog.

## Performance

Benchmarks from Phase 5 performance tuning (100 random problems):

| Configuration | Avg Time | Speedup |
|--------------|----------|---------|
| Baseline | 0.363 ms | 1.0x |
| Largest ccpar only | 0.306 ms | 1.19x |
| All optimizations | 0.224 ms | **1.62x** |

Performance characteristics:
- **Hash-consing speedup**: 3.37x faster for repeated term creation
- **Path compression**: 3.37x speedup on repeated FIND operations
- **Forbidden set**: Early UNSAT detection (up to 50% faster on UNSAT instances)

## Documentation

- **[docs/PROJECT_PLAN.md](docs/PROJECT_PLAN.md)**: Project timeline, phases, and task breakdown
- **[docs/ARCHITECTURE.md](docs/ARCHITECTURE.md)**: System design, data structures, algorithms
- **[docs/INPUT_FORMAT.md](docs/INPUT_FORMAT.md)**: Complete input format specification
- **[tests/README.md](tests/README.md)**: Test suite documentation
- **[experiments/README.md](experiments/README.md)**: Performance analysis and benchmarks

## Implementation Details

### Core Components

1. **TermFactory** (`dag/`): Hash-consing for term sharing
2. **ClassManager** (`equivalence/`): UNION-FIND with path compression
3. **MergeManager** (`core/`): Congruence closure propagation
4. **CongruenceChecker** (`core/`): Congruence testing with optimizations
5. **TEProcedure** (`theory/te/`): T_E satisfiability checking
6. **TConsProcedure** (`theory/tcons/`): T_cons axiom integration
7. **TArrayProcedure** (`theory/tarray/`): T_A store decomposition
8. **UnifiedSolver**: Automatic theory detection and routing

### Technologies

- **Language**: Java 17
- **Build**: Maven 3.9
- **Testing**: JUnit 5
- **Paradigm**: Object-oriented design with immutable data structures

## Troubleshooting

### Build Errors

```bash
# Clean and rebuild
mvn clean compile
```

### Test Failures

```bash
# Run tests with verbose output
mvn test -X
```

### JAR Not Found

```bash
# Ensure package was built
mvn clean package
ls target/*.jar
```

### Java Version Issues

```bash
# Check Java version (must be 17+)
java -version

# Set JAVA_HOME if needed
export JAVA_HOME=/path/to/jdk17
```

## References

1. **Bradley, A. R., & Manna, Z.** (2007). *The Calculus of Computation: Decision Procedures with Applications to Verification*. Springer. Sections 9.3-9.5.

2. **Kroening, D., & Strichman, O.** (2016). *Decision Procedures: An Algorithmic Point of View* (2nd ed.). Springer.

3. **Nelson, G., & Oppen, D. C.** (1980). Fast Decision Procedures Based on Congruence Closure. *Journal of the ACM*, 27(2), 356-364.

4. **Downey, P. J., Sethi, R., & Tarjan, R. E.** (1980). Variations on the Common Subexpression Problem. *Journal of the ACM*, 27(4), 758-771.

5. **Detlefs, D., Nelson, G., & Saxe, J. B.** (2005). Simplify: A Theorem Prover for Program Checking. *Journal of the ACM*, 52(3), 365-473.

## License

See [LICENSE](LICENSE) file for details.

## Author

Implementation for PAR course, I term, Academic Year 2025-26.

---

**Status**: ✅ All phases complete (Phases 1-5.4)
**Test Suite**: ✅ 571/571 tests passing
**Build**: ✅ Maven package successful
**Optimizations**: ✅ Forbidden set + path compression enabled
