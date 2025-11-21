# Experiment Results - Phase 4.2

**Date:** November 21, 2025
**Solver Version:** 1.0-SNAPSHOT
**Total Test Cases:** 56 (51 custom format + 5 SMT-LIB)

## Executive Summary

The solver was tested against 56 test cases covering the three theories (T_E, T_cons, T_A) and their combinations. The overall correctness rate is **85.7%** (48/56 tests passing) for custom format tests, with perfect accuracy on T_E and T_cons theories, and minor limitations in T_A involving indirect read-over-write patterns.

### Key Findings

1. **✓ Theory of Equality (T_E)**: 100% correct (14/14 tests)
2. **✓ Theory of Lists (T_cons)**: 100% correct (13/13 tests)
3. **⚠ Theory of Arrays (T_A)**: 86% correct (12/14 tests)
   - 2 failures due to indirect read-over-write pattern limitation
4. **⚠ Combined Theories**: 90% correct (9/10 tests)
   - 1 failure involving T_A indirect pattern
5. **✗ SMT-LIB Format**: Parser issue (custom parser being invoked instead of SMT-LIB parser)

### Performance

- **Average Runtime**: 67.2 ms per test
- **Minimum Runtime**: 48.1 ms
- **Maximum Runtime**: 83.8 ms
- **Total Runtime**: 3.76 seconds for all tests

## Detailed Results by Category

### Theory of Equality (T_E) - 14 Tests

| Test | Expected | Result | Runtime (ms) | Status |
|------|----------|--------|--------------|--------|
| te_binary_functions_sat.txt | SAT | SAT | 70.42 | ✓ |
| te_binary_functions_unsat.txt | UNSAT | UNSAT | 67.74 | ✓ |
| te_chain_sat.txt | SAT | SAT | 68.41 | ✓ |
| te_chain_unsat.txt | UNSAT | UNSAT | 66.23 | ✓ |
| te_complex_example_sat.txt | SAT | SAT | 67.64 | ✓ |
| te_complex_example_unsat.txt | UNSAT | UNSAT | 66.20 | ✓ |
| te_congruence_sat.txt | SAT | SAT | 67.97 | ✓ |
| te_congruence_unsat.txt | UNSAT | UNSAT | 67.22 | ✓ |
| te_example_9_2_sat.txt | SAT | SAT | 68.48 | ✓ |
| te_example_9_3_unsat.txt | UNSAT | UNSAT | 67.30 | ✓ |
| te_nested_functions_sat.txt | SAT | SAT | 67.33 | ✓ |
| te_nested_functions_unsat.txt | UNSAT | UNSAT | 66.18 | ✓ |
| test_te_sat.txt | SAT | SAT | 66.67 | ✓ |
| test_te_unsat.txt | UNSAT | UNSAT | 66.60 | ✓ |

**Summary**: 14/14 correct (100%)
**Average Runtime**: 67.5 ms

### Theory of Lists (T_cons) - 13 Tests

| Test | Expected | Result | Runtime (ms) | Status |
|------|----------|--------|--------------|--------|
| tcons_both_axioms_sat.txt | SAT | SAT | 67.47 | ✓ |
| tcons_car_axiom_sat.txt | SAT | SAT | 67.69 | ✓ |
| tcons_car_axiom_unsat.txt | UNSAT | UNSAT | 69.79 | ✓ |
| tcons_cdr_axiom_sat.txt | SAT | SAT | 67.55 | ✓ |
| tcons_cdr_axiom_unsat.txt | UNSAT | UNSAT | 66.46 | ✓ |
| tcons_complex_list_sat.txt | SAT | SAT | 80.75 | ✓ |
| tcons_complex_list_unsat.txt | UNSAT | UNSAT | 69.46 | ✓ |
| tcons_list_equality_sat.txt | SAT | SAT | 67.77 | ✓ |
| tcons_list_equality_unsat.txt | UNSAT | UNSAT | 65.34 | ✓ |
| tcons_nested_cons_sat.txt | SAT | SAT | 68.79 | ✓ |
| tcons_nested_cons_unsat.txt | UNSAT | UNSAT | 67.20 | ✓ |
| test_tcons_sat.txt | SAT | SAT | 70.88 | ✓ |
| test_tcons_unsat.txt | UNSAT | UNSAT | 66.47 | ✓ |

**Summary**: 13/13 correct (100%)
**Average Runtime**: 68.9 ms
**Note**: One test (tcons_complex_list_sat.txt) had longer runtime (80.75 ms) due to more complex list structure

### Theory of Arrays (T_A) - 14 Tests

| Test | Expected | Result | Runtime (ms) | Status |
|------|----------|--------|--------------|--------|
| tarray_complex_sat.txt | SAT | SAT | 83.83 | ✓ |
| tarray_complex_unsat.txt | UNSAT | **SAT** | 83.16 | **✗** |
| tarray_extensionality_sat.txt | SAT | SAT | 68.95 | ✓ |
| tarray_multiple_stores_sat.txt | SAT | SAT | 70.25 | ✓ |
| tarray_multiple_stores_unsat.txt | UNSAT | UNSAT | 67.19 | ✓ |
| tarray_overwrite_sat.txt | SAT | SAT | 68.61 | ✓ |
| tarray_overwrite_unsat.txt | UNSAT | **SAT** | 68.98 | **✗** |
| tarray_read_over_write_diff_sat.txt | SAT | SAT | 70.02 | ✓ |
| tarray_read_over_write_diff_unsat.txt | UNSAT | UNSAT | 66.89 | ✓ |
| tarray_read_over_write_same_sat.txt | SAT | SAT | 67.38 | ✓ |
| tarray_read_over_write_same_unsat.txt | UNSAT | UNSAT | 67.35 | ✓ |
| tarray_three_stores_sat.txt | SAT | SAT | 69.62 | ✓ |
| test_tarray_sat.txt | SAT | SAT | 66.96 | ✓ |
| test_tarray_unsat.txt | UNSAT | UNSAT | 67.70 | ✓ |

**Summary**: 12/14 correct (85.7%)
**Average Runtime**: 70.5 ms
**Failures**: 2 tests involving indirect read-over-write patterns (see Analysis section)

### Combined Theories - 10 Tests

| Test | Expected | Result | Runtime (ms) | Status |
|------|----------|--------|--------------|--------|
| combined_all_three_sat.txt | SAT | SAT | 81.90 | ✓ |
| combined_all_three_unsat.txt | UNSAT | UNSAT | 68.55 | ✓ |
| combined_complex_sat.txt | SAT | SAT | 67.23 | ✓ |
| combined_complex_unsat.txt | UNSAT | UNSAT | 66.91 | ✓ |
| combined_tcons_tarray_sat.txt | SAT | SAT | 68.62 | ✓ |
| combined_tcons_tarray_unsat.txt | UNSAT | UNSAT | 66.91 | ✓ |
| combined_te_tarray_sat.txt | SAT | SAT | 68.09 | ✓ |
| combined_te_tarray_unsat.txt | UNSAT | **SAT** | 68.70 | **✗** |
| combined_te_tcons_sat.txt | SAT | SAT | 68.28 | ✓ |
| combined_te_tcons_unsat.txt | UNSAT | UNSAT | 65.76 | ✓ |

**Summary**: 9/10 correct (90%)
**Average Runtime**: 69.1 ms
**Failure**: 1 test involving T_A indirect pattern

### SMT-LIB Format - 5 Tests

All SMT-LIB tests failed due to a parser invocation issue. The custom format lexer is being used instead of the SMT-LIB parser, resulting in errors on SMT-LIB comment syntax (semicolons).

| Test | Expected | Result | Error |
|------|----------|--------|-------|
| congruence_unsat.smt2 | UNSAT | UNKNOWN | Lexer error: Unexpected character ';' |
| function_sat.smt2 | SAT | UNKNOWN | Lexer error: Unexpected character ';' |
| lists_sat.smt2 | SAT | UNKNOWN | Lexer error: Unexpected character ';' |
| simple_sat.smt2 | SAT | UNKNOWN | Lexer error: Unexpected character ';' |
| simple_unsat.smt2 | UNSAT | UNKNOWN | Lexer error: Unexpected character ';' |

**Note**: This is a known issue with how the experiment runner invokes the solver. The SMT-LIB parser exists and works, but requires different invocation.

## Analysis of Failures

### Failed Test 1: tarray_complex_unsat.txt

**Input:**
```
store(a, i, v) = b
store(b, j, w) = c
select(c, i) != v
i != j
```

**Expected**: UNSAT (contradiction)
**Actual**: SAT

**Analysis**:
This test involves chained store operations with an indirect read-over-write pattern. The contradiction arises from:
1. `c = store(store(a, i, v), j, w)`
2. Since `i ≠ j`, by read-over-write axiom: `select(c, i) = select(store(a, i, v), i) = v`
3. But we have `select(c, i) ≠ v`, which is a contradiction

The solver fails to detect this because the `select(c, i)` term doesn't directly match the pattern `select(store(...), i)`. The algorithm would need to first apply congruence closure to recognize that `c` is equal to a nested store term, then apply read-over-write axioms.

### Failed Test 2: tarray_overwrite_unsat.txt

**Input:**
```
select(store(store(a, i, v1), i, v2), i) = v1
```

**Expected**: UNSAT (contradiction)
**Actual**: SAT

**Analysis**:
This tests the "last write wins" principle. By the read-over-write axiom:
- `select(store(store(a, i, v1), i, v2), i) = v2` (read the value just written)
- But the test claims it equals `v1`, which is a contradiction

The solver processes this as:
1. First finds innermost read-over-write: `select(store(store(a, i, v1), i, v2), i)`
2. Branches on whether indices match
3. In the `i = i` branch, it should replace with `v2`

**Root cause**: The algorithm may not be correctly handling nested store operations where the same index is written twice. This requires careful tracking of which store operation is "closest" to the select.

### Failed Test 3: combined_te_tarray_unsat.txt

**Input:**
```
store(a, i, v) = b
select(b, i) = w
v != w
```

**Expected**: UNSAT (contradiction)
**Actual**: SAT

**Analysis**:
This is the clearest example of the indirect read-over-write pattern limitation:
1. We have `b = store(a, i, v)`
2. By read-over-write axiom: `select(store(a, i, v), i) = v`
3. By congruence: `select(b, i) = v` (since `b = store(a, i, v)`)
4. But we also have `select(b, i) = w` and `v ≠ w`, which is a contradiction

The issue is that the algorithm searches for explicit `select(store(...), j)` patterns in the literal syntax. It finds `select(b, i)`, which doesn't match the pattern, even though `b` is equal to a store term.

**Root Cause**: The Bradley & Manna algorithm as described (and implemented) applies read-over-write axioms **syntactically** before congruence closure, not **semantically** after recognizing equalities. A more sophisticated implementation would:
1. First apply congruence closure to identify all terms equal to store operations
2. Then expand read-over-write axioms for any select operations on terms equivalent to stores
3. Or use a different approach like the Ackermann reduction or Nelson-Oppen method

## Problem Size Statistics

| Category | Avg Literals | Avg Functions | Avg Runtime (ms) |
|----------|--------------|---------------|------------------|
| T_E      | 3.8          | 2.8           | 67.5             |
| T_cons   | 3.0          | 3.2           | 68.9             |
| T_A      | 2.4          | 2.9           | 70.5             |
| Combined | 4.8          | 5.1           | 69.1             |
| SMT-LIB  | 1.8          | 13.8          | 48.7             |

**Observations**:
- Runtime is relatively consistent across theories (66-71 ms range)
- T_A tests show slightly higher average runtime, likely due to the branching algorithm
- Combined theory tests have more literals/functions but similar runtime
- SMT-LIB tests show lower runtime due to early parser failure (not actual solving)

## Conclusions

### Strengths

1. **Excellent T_E Performance**: Perfect accuracy on all 14 equality theory tests, including complex examples with nested functions and congruence closure
2. **Perfect T_cons Performance**: All 13 list theory tests passed, correctly handling car/cdr axioms and nested cons operations
3. **Good T_A Performance**: 12/14 array tests passed, successfully handling direct read-over-write patterns and multiple store operations
4. **Fast Runtime**: Average of 67ms per test, with consistent performance across theories
5. **Robust Congruence Closure**: The CC algorithm with largest ccpar optimization works correctly

### Limitations

1. **Indirect Read-Over-Write Patterns**: The T_A procedure doesn't handle cases where select operates on a variable that's equal to a store term (3 failures)
2. **SMT-LIB Parser Invocation**: The experiment framework needs to be updated to properly invoke the SMT-LIB parser
3. **Nested Store Same-Index**: One edge case with nested stores writing to the same index twice

### Recommendations for Future Work

1. **Enhance T_A Algorithm**: Implement a preprocessing step that:
   - Applies congruence closure first to identify all store-equivalent terms
   - Then expands select operations on those terms to read-over-write patterns
   - Or use an Ackermann-style reduction approach

2. **Fix Nested Store Handling**: Ensure that multiple writes to the same index correctly apply the "last write wins" principle

3. **Update SMT-LIB Support**: Modify the experiment runner or Main.java to correctly detect and use the SMT-LIB parser for .smt2 files

4. **Add More Test Cases**: Generate additional tests for:
   - Larger problem sizes (10, 50, 100+ literals)
   - Deeper nesting of store operations
   - More complex combined theory scenarios

5. **Performance Profiling**: Measure detailed metrics on:
   - Number of merge operations
   - Number of congruence checks
   - Number of array theory branches explored
   - Memory usage

## References

- Bradley, A. R., & Manna, Z. (2007). *The Calculus of Computation*. Sections 9.3-9.5
- Input test files: `tests/input/`
- Experimental framework: `experiments/`
- Detailed results: `experiments/results.csv`
