# Output Format Specification

This document defines the output format for the equality-lists-arrays-solver.

---

## Overview

The solver outputs a satisfiability result indicating whether the input literal set is consistent.

**Primary Output:** `SAT` or `UNSAT`

**Optional Output:**
- Equivalence classes (for SAT results)
- Conflict explanation (for UNSAT results)
- Runtime statistics
- Diagnostic information

---

## Basic Output Format

### Minimal Output

**SAT (Satisfiable):**
```
SAT
```

**UNSAT (Unsatisfiable):**
```
UNSAT
```

This is the minimum required output format.

---

## Extended Output Format

### SAT with Witness (Equivalence Classes)

When the solver determines the input is satisfiable, it can optionally output the equivalence classes showing which terms are equal:

```
SAT

Equivalence Classes:
  [a, b, c]
  [x, y]
  [f(a), g(b)]
  [arr1, store(arr, i, v)]
```

**Format:**
```
SAT

Equivalence Classes:
  [term1, term2, ...]
  [term3, term4, ...]
  ...
```

**Interpretation:**
- Each line represents one equivalence class
- Terms within brackets are equal to each other
- Terms in different brackets may or may not be equal (satisfiability doesn't determine this)

**Example:**

Input:
```
a = b
b = c
f(a) = g(b)
```

Output:
```
SAT

Equivalence Classes:
  [a, b, c]
  [f(a), g(b)]
```

This shows:
- `a`, `b`, and `c` are all equal
- `f(a)` and `g(b)` are equal (by congruence, since `a = b`)

---

### UNSAT with Conflict Explanation

When the solver determines the input is unsatisfiable, it can optionally output an explanation:

```
UNSAT

Conflict:
  Required: a = b (from input)
  Required: b = c (from input)
  Derived: a = c (by transitivity)
  Conflict: c != a (from input)
```

**Format:**
```
UNSAT

Conflict:
  <explanation lines>
```

**Types of Conflict Explanations:**

#### 1. Equality Chain Conflict

```
UNSAT

Conflict:
  a = b (asserted)
  b = c (asserted)
  Therefore: a = c (transitive)
  But: a != c (asserted)
  Contradiction!
```

#### 2. Congruence Conflict

```
UNSAT

Conflict:
  a = b (asserted)
  Therefore: f(a) = f(b) (congruence)
  But: f(a) != f(b) (asserted)
  Contradiction!
```

#### 3. Axiom Violation

```
UNSAT

Conflict:
  x = cons(a, b) (asserted)
  Therefore: car(x) = a (T_cons axiom)
  But: car(x) != a (asserted)
  Contradiction!
```

**Simple Conflict Format:**

For simpler implementation, a minimal conflict can just identify the conflicting disequality:

```
UNSAT

Conflict: Terms a and c are both equal and not equal
```

---

## Output with Statistics

### Minimal Statistics

```
SAT

Statistics:
  Terms: 15
  Equivalence classes: 8
  Merges performed: 7
  Runtime: 0.003s
```

```
UNSAT

Statistics:
  Terms: 12
  Equivalence classes: 4
  Merges performed: 8
  Conflict detected at: merge 8
  Runtime: 0.002s
```

### Detailed Statistics

```
SAT

Statistics:
  Input literals: 20
  Equalities: 15
  Disequalities: 5
  Terms created: 35
  Variables: 8
  Constants: 2
  Function applications: 25

  Theory detection:
    T_E: 12 literals
    T_cons: 5 literals (cons: 2, car: 2, cdr: 1)
    T_A: 3 literals (store: 1, select: 2)

  Solving:
    Subproblems: 2
    Merges performed: 18
    FIND operations: 76
    Final equivalence classes: 12

  Performance:
    Parsing time: 0.001s
    Solving time: 0.004s
    Total time: 0.005s
```

---

## Verbose Output Mode

For debugging and educational purposes, verbose mode shows the solver's step-by-step execution:

```
SAT

Verbose trace:
  [PARSE] Read 3 literals
  [INIT] Created 5 terms
  [INIT] Initialized 5 equivalence classes
  [MERGE] a = b
    -> Merged classes: [a] + [b] = [a, b]
  [MERGE] b = c
    -> Merged classes: [a, b] + [c] = [a, b, c]
  [CHECK] Disequality: f(a) != g(c)
    -> FIND(f(a)) = f(a)
    -> FIND(g(c)) = g(c)
    -> f(a) and g(c) are in different classes
    -> No conflict
  [RESULT] All disequalities satisfied

Equivalence Classes:
  [a, b, c]
  [f(a)]
  [g(c)]

Runtime: 0.002s
```

---

## SMT-LIB Compatible Output (Optional)

For compatibility with SMT-LIB tools:

```
sat

(model
  (= a b)
  (= b c)
  (= a c)
)
```

or

```
unsat
```

---

## JSON Output Format (Optional)

For programmatic consumption:

### SAT Result

```json
{
  "result": "SAT",
  "equivalenceClasses": [
    ["a", "b", "c"],
    ["f(a)", "g(b)"],
    ["x"]
  ],
  "statistics": {
    "terms": 15,
    "classes": 3,
    "merges": 12,
    "runtime": 0.003
  }
}
```

### UNSAT Result

```json
{
  "result": "UNSAT",
  "conflict": {
    "type": "disequality",
    "terms": ["a", "c"],
    "explanation": "Terms a and c are in the same equivalence class but asserted as disequal"
  },
  "statistics": {
    "terms": 12,
    "classes": 4,
    "merges": 8,
    "runtime": 0.002
  }
}
```

---

## Error Output

### Parse Errors

```
ERROR: Parse error at line 3, column 5
  Expected '=' or '!=' but found 'x'

  Line 3: a b x = c
             ^
```

### Runtime Errors

```
ERROR: Arity mismatch
  Function 'cons' requires 2 arguments but got 1

  At: cons(a) = x
```

---

## Output Implementation

### Result Class

```java
public class Result {
    public enum Status {
        SAT,
        UNSAT,
        ERROR
    }

    private final Status status;
    private final Set<EquivalenceClass> equivalenceClasses;
    private final String conflictExplanation;
    private final Statistics statistics;
    private final String errorMessage;

    // Constructors for different result types
    public static Result sat(Set<EquivalenceClass> classes) {
        return new Result(Status.SAT, classes, null, null, null);
    }

    public static Result unsat(String conflict) {
        return new Result(Status.UNSAT, null, conflict, null, null);
    }

    public static Result error(String message) {
        return new Result(Status.ERROR, null, null, null, message);
    }

    // Format as string
    public String toString() {
        // Basic format
        return status.toString();
    }

    public String toStringWithWitness() {
        // SAT with equivalence classes
        StringBuilder sb = new StringBuilder();
        sb.append(status).append("\n\n");

        if (status == Status.SAT && equivalenceClasses != null) {
            sb.append("Equivalence Classes:\n");
            for (EquivalenceClass ec : equivalenceClasses) {
                sb.append("  [");
                sb.append(ec.getMembers().stream()
                    .map(Term::toSExpression)
                    .collect(Collectors.joining(", ")));
                sb.append("]\n");
            }
        }

        return sb.toString();
    }

    public String toStringWithConflict() {
        // UNSAT with explanation
        StringBuilder sb = new StringBuilder();
        sb.append(status).append("\n\n");

        if (status == Status.UNSAT && conflictExplanation != null) {
            sb.append("Conflict:\n");
            sb.append("  ").append(conflictExplanation).append("\n");
        }

        return sb.toString();
    }

    public String toStringWithStatistics() {
        // Include statistics
        StringBuilder sb = new StringBuilder();
        sb.append(status).append("\n\n");

        if (statistics != null) {
            sb.append("Statistics:\n");
            sb.append(statistics.toString());
        }

        return sb.toString();
    }
}
```

### Statistics Class

```java
public class Statistics {
    private int termCount;
    private int equivalenceClassCount;
    private int mergesPerformed;
    private int findOperations;
    private double runtimeSeconds;

    // ... getters and setters ...

    @Override
    public String toString() {
        return String.format(
            "  Terms: %d\n" +
            "  Equivalence classes: %d\n" +
            "  Merges performed: %d\n" +
            "  FIND operations: %d\n" +
            "  Runtime: %.3fs\n",
            termCount, equivalenceClassCount, mergesPerformed,
            findOperations, runtimeSeconds
        );
    }
}
```

---

## Command-Line Flags for Output Control

```bash
# Minimal output (default)
java -jar solver.jar input.txt
# Output: SAT or UNSAT

# With equivalence classes
java -jar solver.jar --witness input.txt
# Output: SAT + equivalence classes

# With conflict explanation
java -jar solver.jar --explain input.txt
# Output: UNSAT + conflict explanation

# With statistics
java -jar solver.jar --stats input.txt
# Output: SAT/UNSAT + statistics

# Verbose mode
java -jar solver.jar --verbose input.txt
# Output: Step-by-step trace

# JSON output
java -jar solver.jar --json input.txt
# Output: JSON format

# All information
java -jar solver.jar --full input.txt
# Output: Result + witness/conflict + statistics
```

---

## Output Examples

### Example 1: Simple SAT

**Input:**
```
a = b
b = c
```

**Output (minimal):**
```
SAT
```

**Output (with witness):**
```
SAT

Equivalence Classes:
  [a, b, c]
```

---

### Example 2: Simple UNSAT

**Input:**
```
a = b
b = c
c != a
```

**Output (minimal):**
```
UNSAT
```

**Output (with conflict):**
```
UNSAT

Conflict:
  a = b (asserted)
  b = c (asserted)
  a = c (by transitivity)
  But: a != c (asserted)
  Contradiction!
```

---

### Example 3: Complex SAT

**Input:**
```
x = cons(a, b)
y = car(x)
z = cdr(x)
```

**Output (with witness and stats):**
```
SAT

Equivalence Classes:
  [a, car(x), y]
  [b, cdr(x), z]
  [x, cons(a, b)]

Statistics:
  Terms: 8
  Equivalence classes: 3
  Merges performed: 4
  Runtime: 0.002s
```

---

## Testing Output Format

### Test Cases

1. **Minimal SAT**: Just "SAT"
2. **Minimal UNSAT**: Just "UNSAT"
3. **SAT with single class**: `[a, b, c]`
4. **SAT with multiple classes**: Multiple lines
5. **UNSAT with simple conflict**: "Terms X and Y conflict"
6. **UNSAT with detailed conflict**: Multi-line explanation
7. **With statistics**: Runtime, counts
8. **With error message**: Parse error, arity error

### Output Validation

The output should be:
1. **Correct**: SAT ↔ satisfiable, UNSAT ↔ unsatisfiable
2. **Complete**: If SAT with witness, all terms should appear in some class
3. **Consistent**: Equivalence classes should be disjoint
4. **Readable**: Clear formatting, proper indentation

---

## Implementation Checklist

### Phase 1: Basic Output
- [ ] Implement Result class
- [ ] Output SAT or UNSAT
- [ ] Add unit tests

### Phase 2: Extended Output
- [ ] Add equivalence classes for SAT
- [ ] Add conflict explanation for UNSAT
- [ ] Add command-line flags
- [ ] Add unit tests

### Phase 3: Statistics
- [ ] Implement Statistics class
- [ ] Track term counts, merges, runtime
- [ ] Format statistics output
- [ ] Add unit tests

### Phase 4: Optional Formats
- [ ] JSON output
- [ ] SMT-LIB output
- [ ] Verbose trace mode

---

## Summary

**Output format priorities:**

1. **Minimal (MUST)**: `SAT` or `UNSAT`
2. **Extended (SHOULD)**: Equivalence classes for SAT, conflict for UNSAT
3. **Statistics (NICE)**: Runtime and algorithm metrics
4. **Verbose (OPTIONAL)**: Step-by-step trace
5. **Alternative formats (OPTIONAL)**: JSON, SMT-LIB

**Design goals:**
- Simple default output
- Optional detailed output via flags
- Machine-readable formats for integration
- Helpful for debugging and learning

---

**This output format balances simplicity with extensibility for various use cases!**
