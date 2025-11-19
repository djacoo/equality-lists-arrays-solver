# SMT-LIB Parser Documentation

## Overview

The SMT-LIB parser allows the solver to read input in the standard SMT-LIB 2.0 format, specifically supporting the QF_UF (Quantifier-Free Uninterpreted Functions) logic.

## Components

### 1. SMTLIBToken.java
Defines token types for the SMT-LIB lexer, including:
- Parentheses: `(`, `)`
- Symbols: identifiers and keywords
- String literals
- Numeric literals
- Commands: `set-logic`, `declare-fun`, `assert`, `check-sat`, etc.

### 2. SMTLIBLexer.java
Tokenizes SMT-LIB input following the S-expression syntax:
- Handles comments (semicolon to end of line)
- Recognizes SMT-LIB keywords
- Supports string literals with escape sequences
- Tracks line and column numbers for error reporting

### 3. SMTLIBParser.java
Parses tokenized SMT-LIB input and converts it to internal Literal representation:
- Supports `set-logic QF_UF`
- Supports `declare-fun` and `declare-const`
- Converts `assert` statements to Literal objects
- Handles equality: `(= t1 t2)`
- Handles negation of equality: `(not (= t1 t2))` → disequality
- Supports conjunction: `(and ...)` → multiple literals
- Function applications in prefix notation: `(f x y)`

### 4. SMTLIBInputReader.java
High-level interface for reading SMT-LIB files:
- Read from file or stdin
- Integrates lexer and parser
- Returns list of Literal objects ready for solving

### 5. SMTLIBSolver.java
Command-line tool for solving SMT-LIB files:
- Usage: `java solver.SMTLIBSolver <file.smt2>`
- Displays parsed literals
- Runs the UnifiedSolver
- Shows SAT/UNSAT result with details

## Supported SMT-LIB Subset

### Commands
- `(set-logic QF_UF)` - Set the logic to QF_UF
- `(declare-fun name (arg-types) return-type)` - Declare a function
- `(declare-const name type)` - Declare a constant (nullary function)
- `(assert formula)` - Assert a formula as a constraint
- `(check-sat)` - Check satisfiability (triggers solving)
- `(get-model)` - Request a model (informational only)
- `(exit)` - Exit (informational only)

### Formulas
- Equality: `(= term1 term2)`
- Disequality: `(not (= term1 term2))`
- Conjunction: `(and formula1 formula2 ...)`
- Negation of equality: `(not (= t1 t2))`

### Terms
- Variables: `x`, `a`, `b`
- Function applications: `(f x y)`, `(cons a b)`
- Nested applications: `(car (cons x y))`

### Limitations
- **No disjunction**: `(or ...)` requires CNF conversion (not implemented)
- **No implication**: `(=>)` not supported
- **No quantifiers**: QF_UF is quantifier-free
- **Boolean terms**: Standalone boolean variables not fully supported
- **No theory-specific symbols**: Uses uninterpreted functions only

## Usage Examples

### Example 1: Simple Satisfiable Formula

File: `simple_sat.smt2`
```smt2
(set-logic QF_UF)
(declare-fun a () Int)
(declare-fun b () Int)
(declare-fun c () Int)

(assert (= a b))
(assert (= b c))

(check-sat)
```

Run:
```bash
mvn exec:java -Dexec.mainClass="solver.SMTLIBSolver" -Dexec.args="simple_sat.smt2"
```

Output:
```
Reading from file: simple_sat.smt2

Parsed 2 literals:
  1. a = b
  2. b = c

Solving...

SAT

Equivalence Classes:
  Class[2] rep=a size=3 members=[a, b, c]
```

### Example 2: Unsatisfiable Formula

File: `simple_unsat.smt2`
```smt2
(set-logic QF_UF)
(declare-fun a () Int)
(declare-fun b () Int)
(declare-fun c () Int)

(assert (= a b))
(assert (= b c))
(assert (not (= a c)))

(check-sat)
```

Output:
```
UNSAT

Conflict: Disequality a != c violated: both terms are equal by congruence closure
```

### Example 3: Function Applications

File: `function_sat.smt2`
```smt2
(set-logic QF_UF)
(declare-fun f (Int Int) Int)
(declare-fun a () Int)
(declare-fun b () Int)

(assert (= (f a b) a))

(check-sat)
```

Output:
```
SAT

Equivalence Classes:
  Class[2] rep=b size=1 members=[b]
  Class[1] rep=a size=2 members=[a, f]
```

### Example 4: Congruence Violation

File: `congruence_unsat.smt2`
```smt2
(set-logic QF_UF)
(declare-fun f (Int) Int)
(declare-fun a () Int)
(declare-fun b () Int)

(assert (= a b))
(assert (not (= (f a) (f b))))

(check-sat)
```

Output:
```
UNSAT

Conflict: Disequality f != f violated: both terms are equal by congruence closure
```

### Example 5: List Theory

File: `lists_sat.smt2`
```smt2
(set-logic QF_UF)
(declare-fun cons (Int Int) Int)
(declare-fun car (Int) Int)
(declare-fun cdr (Int) Int)
(declare-fun x () Int)
(declare-fun y () Int)

(assert (= (car (cons x y)) x))

(check-sat)
```

Output:
```
SAT

Equivalence Classes:
  Class[2] rep=y size=2 members=[cdr, y]
  Class[1] rep=x size=2 members=[car, x]
  Class[3] rep=cons size=1 members=[cons]
```

## Testing

Test files are located in `tests/input/smtlib/`:
- `simple_sat.smt2` - Basic SAT example
- `simple_unsat.smt2` - Basic UNSAT example
- `function_sat.smt2` - Function application
- `congruence_unsat.smt2` - Congruence violation
- `lists_sat.smt2` - List theory (cons/car/cdr)

Run all tests:
```bash
for f in tests/input/smtlib/*.smt2; do
    echo "Testing $f"
    mvn -q exec:java -Dexec.mainClass="solver.SMTLIBSolver" -Dexec.args="$f"
    echo ""
done
```

## Integration with Solver

The SMT-LIB parser integrates seamlessly with the existing solver infrastructure:

1. **Lexer** tokenizes SMT-LIB input
2. **Parser** converts tokens to `Literal` objects
3. **UnifiedSolver** processes literals using:
   - `TEProcedure` for pure equality
   - `TConsProcedure` for list theory (cons/car/cdr)
   - `TArrayProcedure` for array theory (select/store)
4. **Result** contains SAT/UNSAT verdict with witness/conflict

## Future Enhancements

Potential improvements for the SMT-LIB parser:

1. **CNF Conversion**: Support for disjunction `(or ...)` and implication `(=>)`
2. **Boolean Terms**: Full support for boolean variables and predicates
3. **More Logics**: Support for QF_UFLIA, QF_UFDT, etc.
4. **Benchmark Testing**: Automated testing with SMT-LIB benchmarks from Zenodo
5. **Error Recovery**: Better error messages and recovery from parse errors
6. **Model Generation**: Convert equivalence classes back to SMT-LIB model format
7. **Incremental Solving**: Support for `(push)` and `(pop)` commands

## References

- [SMT-LIB Standard 2.0](http://smtlib.cs.uiowa.edu/papers/smt-lib-reference-v2.0-r10.12.21.pdf)
- [SMT-LIB Tutorial](http://smtlib.github.io/jSMTLIB/SMTLIBTutorial.pdf)
- [QF_UF Logic Description](https://smt-lib.org/logics.shtml)
- [SMT-LIB Benchmarks on Zenodo](https://zenodo.org/records/16740866)
