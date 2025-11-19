# Test Suite

This directory contains the comprehensive test suite for the Equality-Lists-Arrays Solver.

## Directory Structure

```
tests/
├── README.md           # This file
├── TEST_INDEX.md       # Comprehensive index of all test cases
└── input/              # Test input files
    ├── te/            # Theory of Equality tests (14 tests)
    ├── tcons/         # Theory of Lists tests (13 tests)
    ├── tarray/        # Theory of Arrays tests (14 tests)
    ├── combined/      # Combined theory tests (10 tests)
    └── smtlib/        # SMT-LIB format test files (5 tests)
```

## Test Organization

Test files are organized by theory in subdirectories within `tests/input/`:

### Theory of Equality (T_E) - `tests/input/te/` (14 tests)
- **SAT**: 7 tests - Basic transitivity, congruence, binary functions, chaining, nested functions
- **UNSAT**: 7 tests - Transitivity contradictions, congruence violations, complex examples

### Theory of Lists (T_cons) - `tests/input/tcons/` (13 tests)
- **SAT**: 7 tests - car/cdr axioms, list construction, nested cons, list equality
- **UNSAT**: 6 tests - Axiom violations, nested cons contradictions, atom properties

### Theory of Arrays (T_A) - `tests/input/tarray/` (14 tests)
- **SAT**: 8 tests - Read-over-write axioms, multiple stores, extensionality, overwriting
- **UNSAT**: 6 tests - Axiom violations, contradictory array properties

### Combined Theories - `tests/input/combined/` (10 tests)
- **SAT**: 5 tests - T_E+T_cons, T_E+T_A, T_cons+T_A, all three theories
- **UNSAT**: 5 tests - Combined theory contradictions

### SMT-LIB Format - `tests/input/smtlib/` (5 tests)
QF_UF (Quantifier-Free Uninterpreted Functions) logic tests:
- `simple_sat.smt2`, `simple_unsat.smt2` - Basic examples
- `function_sat.smt2` - Function applications
- `congruence_unsat.smt2` - Congruence closure
- `lists_sat.smt2` - List theory operations

### Naming Convention

All test files follow a consistent naming pattern:
- Format: `{theory}_{description}_{sat|unsat}.txt`
- Examples: `te_congruence_sat.txt`, `tarray_read_over_write_same_unsat.txt`
- Basic tests: `test_{theory}_{sat|unsat}.txt`

## Test Statistics

- **Total Test Files**: 56 (51 custom format + 5 SMT-LIB)
- **SAT Cases**: 27
- **UNSAT Cases**: 24
- **SMT-LIB**: 5

## Running Tests

### Single Test (Custom Format)
```bash
java -jar target/equality-lists-arrays-solver-1.0-SNAPSHOT-standalone.jar < tests/input/te/test_te_sat.txt
java -jar target/equality-lists-arrays-solver-1.0-SNAPSHOT-standalone.jar < tests/input/tarray/tarray_complex_sat.txt
```

### Single Test (SMT-LIB Format)
```bash
java -cp target/equality-lists-arrays-solver-1.0-SNAPSHOT-standalone.jar solver.SMTLIBSolver tests/input/smtlib/simple_sat.smt2
```

### All Unit Tests
```bash
mvn test
```

## Test Documentation

For detailed information about each test case including:
- Source references (Bradley & Manna, Kroening & Strichman, etc.)
- Test descriptions
- Expected results
- Theory coverage

See [TEST_INDEX.md](TEST_INDEX.md) for the complete test catalog.

## Test Sources

All test cases are derived from or inspired by:
1. **Bradley & Manna** - The Calculus of Computation (Sections 9.3-9.5)
2. **Kroening & Strichman** - Decision Procedures
3. **Original** - Custom tests for comprehensive coverage

## Adding New Tests

When adding new test cases:
1. Place the file in the appropriate theory subdirectory (`te/`, `tcons/`, `tarray/`, `combined/`, or `smtlib/`)
2. Follow the naming convention: `{theory}_{description}_{sat|unsat}.txt`
3. Include source and expected result in file header comments
4. Add entry to TEST_INDEX.md with source and description
5. Update test statistics in this README and TEST_INDEX.md
