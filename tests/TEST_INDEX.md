# Test Suite Index

This document catalogs all test cases in the test suite, organized by theory and expected result.

## Summary Statistics

- **Total Test Files:** 51
- **T_E (Equality) Tests:** 14
- **T_cons (Lists) Tests:** 13
- **T_A (Arrays) Tests:** 14
- **Combined Theory Tests:** 10

---

## Theory of Equality (T_E) Tests

**Location:** `tests/input/te/`

### SAT Cases

| Test File | Source | Description |
|-----------|--------|-------------|
| `test_te_sat.txt` | Bradley & Manna, Section 9.3 | Simple transitivity |
| `te_congruence_sat.txt` | Bradley & Manna, Section 9.3 | Tests congruence - if a=b then f(a)=f(b) |
| `te_example_9_2_sat.txt` | Bradley & Manna, Section 9.3, Example 9.2 (modified) | Complex congruence closure example |
| `te_binary_functions_sat.txt` | Kroening & Strichman | Congruence with binary function symbols |
| `te_chain_sat.txt` | General CC testing | Long transitive equality chain |
| `te_nested_functions_sat.txt` | Bradley & Manna, Section 9.3 | Multiple levels of function nesting |
| `te_complex_example_sat.txt` | Kroening & Strichman, Chapter 1 | Complex congruence closure with multiple functions |

### UNSAT Cases

| Test File | Source | Description |
|-----------|--------|-------------|
| `test_te_unsat.txt` | Bradley & Manna, Section 9.3, Example 9.1 | Transitivity leads to contradiction |
| `te_congruence_unsat.txt` | Bradley & Manna, Section 9.3, Example 9.2 | Congruence leads to contradiction |
| `te_example_9_3_unsat.txt` | Based on Bradley & Manna, Section 9.3 | Nested function applications with contradiction |
| `te_binary_functions_unsat.txt` | Kroening & Strichman | Binary function congruence with contradiction |
| `te_chain_unsat.txt` | General CC testing | Long transitive chain with contradiction at end |
| `te_nested_functions_unsat.txt` | Bradley & Manna, Section 9.3 | Nested functions with contradiction |
| `te_complex_example_unsat.txt` | Based on Kroening & Strichman | Complex congruence with contradiction |

---

## Theory of Lists (T_cons) Tests

**Location:** `tests/input/tcons/`

### SAT Cases

| Test File | Source | Description |
|-----------|--------|-------------|
| `test_tcons_sat.txt` | Bradley & Manna, Section 9.4 | List construction and decomposition |
| `tcons_car_axiom_sat.txt` | Bradley & Manna, Section 9.4, Axiom 5 | Basic car decomposition axiom: car(cons(x,y)) = x |
| `tcons_cdr_axiom_sat.txt` | Bradley & Manna, Section 9.4, Axiom 6 | Basic cdr decomposition axiom: cdr(cons(x,y)) = y |
| `tcons_both_axioms_sat.txt` | Bradley & Manna, Section 9.4 | Using both car and cdr axioms together |
| `tcons_nested_cons_sat.txt` | Bradley & Manna, Section 9.4 | Nested list construction |
| `tcons_list_equality_sat.txt` | Bradley & Manna, Section 9.4 | Two lists equal if same car and cdr |
| `tcons_complex_list_sat.txt` | Kroening & Strichman | Multiple list operations |

### UNSAT Cases

| Test File | Source | Description |
|-----------|--------|-------------|
| `test_tcons_unsat.txt` | Bradley & Manna, Section 9.4, Axiom 7 | atom(cons(x,y)) is false by axiom |
| `tcons_car_axiom_unsat.txt` | Bradley & Manna, Section 9.4, Axiom 5 | Violating car axiom leads to contradiction |
| `tcons_cdr_axiom_unsat.txt` | Bradley & Manna, Section 9.4, Axiom 6 | Violating cdr axiom leads to contradiction |
| `tcons_nested_cons_unsat.txt` | Bradley & Manna, Section 9.4 | Nested list construction with contradiction |
| `tcons_list_equality_unsat.txt` | Bradley & Manna, Section 9.4 | Same car/cdr but lists claimed unequal |
| `tcons_complex_list_unsat.txt` | Kroening & Strichman | List operations with contradiction |

---

## Theory of Arrays (T_A) Tests

**Location:** `tests/input/tarray/`

### SAT Cases

| Test File | Source | Description |
|-----------|--------|-------------|
| `test_tarray_sat.txt` | Bradley & Manna, Section 9.5 | Read-over-write with same index |
| `tarray_read_over_write_same_sat.txt` | Bradley & Manna, Section 9.5, Axiom 8 | select(store(a,i,v),i) = v |
| `tarray_read_over_write_diff_sat.txt` | Bradley & Manna, Section 9.5, Axiom 9 | i != j implies select(store(a,i,v),j) = select(a,j) |
| `tarray_multiple_stores_sat.txt` | Bradley & Manna, Section 9.5 | Nested store operations |
| `tarray_overwrite_sat.txt` | Kroening & Strichman | Last write wins when overwriting same index |
| `tarray_extensionality_sat.txt` | Kroening & Strichman, Chapter 3 | Arrays equal if all reads equal |
| `tarray_complex_sat.txt` | Bradley & Manna, Section 9.5, Example 9.11 | Complex interleaved store and select |
| `tarray_three_stores_sat.txt` | General testing | Triple nested store operations |

### UNSAT Cases

| Test File | Source | Description |
|-----------|--------|-------------|
| `test_tarray_unsat.txt` | Bradley & Manna, Section 9.5, Example 9.10 | Contradictory array properties |
| `tarray_read_over_write_same_unsat.txt` | Bradley & Manna, Section 9.5, Example 9.10 | Violating read-over-write axiom |
| `tarray_read_over_write_diff_unsat.txt` | Bradley & Manna, Section 9.5 | Violating read-over-write with different indices |
| `tarray_multiple_stores_unsat.txt` | Bradley & Manna, Section 9.5 | Nested stores with contradiction |
| `tarray_overwrite_unsat.txt` | Kroening & Strichman | Last write wins - claiming otherwise is contradiction |
| `tarray_complex_unsat.txt` | Bradley & Manna, Section 9.5 | Complex operations with contradiction |

---

## Combined Theory Tests

**Location:** `tests/input/combined/`

### SAT Cases

| Test File | Source | Description |
|-----------|--------|-------------|
| `combined_te_tcons_sat.txt` | Based on Bradley & Manna, Sections 9.3 and 9.4 | Equality reasoning with list operations |
| `combined_te_tarray_sat.txt` | Based on Bradley & Manna, Sections 9.3 and 9.5 | Equality with arrays |
| `combined_tcons_tarray_sat.txt` | Original combination | Lists and arrays together |
| `combined_all_three_sat.txt` | Original combination | All three theories working together |
| `combined_complex_sat.txt` | Comprehensive integration test | Complex interaction between all theories |

### UNSAT Cases

| Test File | Source | Description |
|-----------|--------|-------------|
| `combined_te_tcons_unsat.txt` | Based on Bradley & Manna | Equality reasoning with lists leads to contradiction |
| `combined_te_tarray_unsat.txt` | Based on Bradley & Manna | Equality with arrays leads to contradiction |
| `combined_tcons_tarray_unsat.txt` | Original combination | Lists and arrays with contradiction |
| `combined_all_three_unsat.txt` | Original combination | All three theories with contradiction |
| `combined_complex_unsat.txt` | Comprehensive integration test | Complex interaction with contradiction |

---

## SMT-LIB Format Tests

**Location:** `tests/input/smtlib/`

These tests use the SMT-LIB 2.0 format and are designed for the QF_UF (Quantifier-Free Uninterpreted Functions) logic.

| Test File | Expected Result | Description |
|-----------|----------------|-------------|
| `simple_sat.smt2` | SAT | Basic satisfiable example with transitivity |
| `simple_unsat.smt2` | UNSAT | Basic unsatisfiable example with contradiction |
| `function_sat.smt2` | SAT | Function application example |
| `congruence_unsat.smt2` | UNSAT | Congruence closure leads to contradiction |
| `lists_sat.smt2` | SAT | List theory example with cons/car/cdr |

**Running SMT-LIB tests:**
```bash
java -cp target/equality-lists-arrays-solver-1.0-SNAPSHOT-standalone.jar solver.SMTLIBSolver tests/input/smtlib/<filename>.smt2
```

---

## Test Categories by Difficulty

### Basic Tests (Foundational axioms)
- All `test_te_*.txt`, `test_tcons_*.txt`, `test_tarray_*.txt`
- All `*_axiom_*.txt` files
- Simple transitivity and congruence tests

### Intermediate Tests (Multiple operations)
- Chain tests
- Nested function/cons tests
- Multiple store operations
- Binary function tests

### Advanced Tests (Complex interactions)
- Combined theory tests
- Complex examples with multiple theories
- Deep nesting and long chains

---

## References

1. **Bradley & Manna**: The Calculus of Computation
   - Section 9.3: Theory of Equality (T_E-procedure, Congruence Closure)
   - Section 9.4: Theory of Lists (T_cons-procedure)
   - Section 9.5: Theory of Arrays (T_A-procedure)

2. **Kroening & Strichman**: Decision Procedures: An Algorithmic Point of View
   - Chapter 1: Introduction to decision procedures
   - Chapter 3: Array theory and extensionality

3. **General Testing**: Original test cases for comprehensive coverage

---

## Expected Results Summary

| Category | SAT | UNSAT | Total |
|----------|-----|-------|-------|
| T_E | 7 | 7 | 14 |
| T_cons | 7 | 6 | 13 |
| T_A | 8 | 6 | 14 |
| Combined | 5 | 5 | 10 |
| **Total** | **27** | **24** | **51** |

---

## Running the Tests

To run all tests and verify expected results:

```bash
# Run a single test (custom format)
java -jar target/equality-lists-arrays-solver-1.0-SNAPSHOT-standalone.jar < tests/input/te/test_te_sat.txt

# Run a single SMT-LIB test
java -cp target/equality-lists-arrays-solver-1.0-SNAPSHOT-standalone.jar solver.SMTLIBSolver tests/input/smtlib/simple_sat.smt2

# Run all unit tests
mvn test

# Check expected vs actual results
# Compare output against expected SAT/UNSAT in file names and headers
```

---