# Optional Optimizations Planning

This document plans the optional optimization features that can be implemented in Phase 5 to improve solver performance.

---

## Overview

The assignment specifies two optional optimizations beyond the mandatory largest ccpar optimization:

1. **Forbidden List/Set** - Prevents certain merges that would lead to inconsistencies
2. **Non-recursive FIND** - Eliminates recursion with immediate find field updates

**References:**
- Detlef et al. [7] page 388 (bottom) - Forbidden list/set
- Detlef et al. [7] - Non-recursive FIND coupled with UNION

---

## Optimization 1: Forbidden List/Set

### Purpose

The forbidden list/set maintains pairs of terms that must NOT be merged into the same equivalence class. This enables:
1. **Early UNSAT Detection**: Detect contradictions as soon as a forbidden merge is attempted
2. **Pruning**: Avoid exploring branches that will lead to UNSAT
3. **Performance**: Fail fast instead of completing all merges

### How It Works

**Core Idea:**
- When we encounter a disequality `t1 != t2`, add the pair `(t1, t2)` to the forbidden set
- Before performing `UNION(t1, t2)`, check if `(t1, t2)` is forbidden
- If yes → immediate UNSAT
- If no → proceed with merge

**From Detlef et al. [7]:**
> "The forbidden set contains pairs of terms that must remain in different equivalence classes. Attempting to merge them indicates unsatisfiability."

### Design

#### Data Structure

```java
public class ForbiddenSet {
    // Set of term pairs that must not be merged
    private final Set<TermPair> forbidden;

    public ForbiddenSet() {
        this.forbidden = new HashSet<>();
    }

    // Add a forbidden pair (from disequality t1 != t2)
    public void addForbiddenPair(Term t1, Term t2) {
        forbidden.add(new TermPair(t1, t2));
    }

    // Check if merging t1 and t2 is forbidden
    public boolean isForbidden(Term t1, Term t2) {
        return forbidden.contains(new TermPair(t1, t2));
    }

    // TermPair class for canonical representation
    private static class TermPair {
        private final Term first;
        private final Term second;

        public TermPair(Term t1, Term t2) {
            // Canonical ordering (smaller ID first)
            if (t1.getId() < t2.getId()) {
                this.first = t1;
                this.second = t2;
            } else {
                this.first = t2;
                this.second = t1;
            }
        }

        @Override
        public boolean equals(Object obj) {
            if (!(obj instanceof TermPair)) return false;
            TermPair other = (TermPair) obj;
            return first.equals(other.first) && second.equals(other.second);
        }

        @Override
        public int hashCode() {
            return Objects.hash(first, second);
        }
    }
}
```

#### Integration with ClassManager

```java
public class ClassManager {
    // ... existing fields ...

    // Optional: Forbidden set
    private ForbiddenSet forbiddenSet;
    private boolean useForbiddenSet;

    // Constructor with configuration
    public ClassManager(boolean useForbiddenSet) {
        this.useForbiddenSet = useForbiddenSet;
        if (useForbiddenSet) {
            this.forbiddenSet = new ForbiddenSet();
        }
        // ... other initialization ...
    }

    // Add disequality to forbidden set
    public void addDisequality(Term t1, Term t2) {
        if (useForbiddenSet) {
            forbiddenSet.addForbiddenPair(t1, t2);
        }
    }

    // Modified UNION with forbidden check
    public boolean union(Term t1, Term t2) {
        Term rep1 = find(t1);
        Term rep2 = find(t2);

        // Already in same class?
        if (rep1 == rep2) {
            return true;  // Success (no-op)
        }

        // Check if merge is forbidden
        if (useForbiddenSet && forbiddenSet.isForbidden(rep1, rep2)) {
            return false;  // UNSAT! Forbidden merge detected
        }

        // ... proceed with normal union ...
        return true;
    }
}
```

#### Modified Solver Algorithm

```java
public Result solve(Set<Literal> literals) {
    // ... build DAG ...
    // ... initialize equivalence classes ...

    // Phase 1: Add all disequalities to forbidden set
    if (useForbiddenSet) {
        for (Literal lit : literals) {
            if (lit.isDisequality()) {
                classManager.addDisequality(lit.getLeft(), lit.getRight());
            }
        }
    }

    // Phase 2: Process equalities
    for (Literal lit : literals) {
        if (lit.isEquality()) {
            boolean success = classManager.union(lit.getLeft(), lit.getRight());
            if (!success) {
                // Forbidden merge attempted!
                return Result.unsat("Merge of " + lit.getLeft() + " and " +
                    lit.getRight() + " violates disequality constraint");
            }
        }
    }

    // Phase 3: Check remaining disequalities
    // (if not using forbidden set, or as additional check)
    // ... existing logic ...
}
```

### Benefits

1. **Early Detection**: Fail as soon as contradiction is detected, not after all merges
2. **Performance**: Avoid unnecessary work on UNSAT instances
3. **Better Diagnostics**: Know exactly which merge caused UNSAT

### Trade-offs

**Pros:**
- Faster UNSAT detection
- Clearer conflict explanation
- Minimal memory overhead (one set)

**Cons:**
- Extra check on every UNION (small overhead)
- More complex implementation
- May not help much on SAT instances

**When to Use:**
- Problems with many disequalities
- Problems likely to be UNSAT
- When early termination is valuable

### Complexity

| Operation | Time | Space |
|-----------|------|-------|
| addForbiddenPair | O(1) | O(1) per pair |
| isForbidden | O(1) | - |
| Overall space | - | O(d) where d = # disequalities |

**Impact on UNION:** Adds O(1) check before each merge

---

## Optimization 2: Non-Recursive FIND

### Purpose

The standard FIND algorithm uses recursion (or iteration with path compression afterward). The non-recursive variant eliminates all recursion and guarantees O(1) FIND after initial setup.

### How It Works

**Standard FIND (with path compression):**
```java
public Term find(Term t) {
    // Follow pointers to representative
    Term current = t;
    while (current.getFind() != current) {
        current = current.getFind();
    }
    Term rep = current;

    // Path compression: update all pointers
    current = t;
    while (current != rep) {
        Term next = current.getFind();
        current.setFind(rep);
        current = next;
    }

    return rep;
}
```

**Non-Recursive FIND (optimized):**
```java
public Term findNonRecursive(Term t) {
    // With eager updates in UNION, this is truly O(1)
    return t.getFind();
}
```

**The trick:** In UNION, eagerly update ALL find fields in the merged class

### Design

#### Modified UNION

```java
public void unionNonRecursive(Term t1, Term t2) {
    // Get current representatives
    Term rep1 = t1.getFind();  // O(1) since find fields are always up-to-date
    Term rep2 = t2.getFind();

    // Already in same class?
    if (rep1 == rep2) {
        return;
    }

    // Get the two classes
    EquivalenceClass class1 = termToClass.get(rep1);
    EquivalenceClass class2 = termToClass.get(rep2);

    // Choose representative based on largest ccpar
    Term newRep;
    EquivalenceClass keepClass, mergeClass;
    if (class1.getCcparSize() >= class2.getCcparSize()) {
        newRep = rep1;
        keepClass = class1;
        mergeClass = class2;
    } else {
        newRep = rep2;
        keepClass = class2;
        mergeClass = class1;
    }

    // Merge classes
    keepClass.addAll(mergeClass);

    // CRITICAL: Eagerly update ALL find fields in merged class
    for (Term term : mergeClass.getMembers()) {
        term.setFind(newRep);  // Direct update, no lazy evaluation
        termToClass.put(term, keepClass);
    }

    // Update representatives
    rep1.setFind(newRep);
    rep2.setFind(newRep);
}
```

#### Invariant

**Key Invariant:** After every UNION, all find fields point directly to the class representative.

**Maintained by:**
1. Initialization: Each term's find points to itself
2. UNION: Eagerly updates all find fields in merged class

**Result:** FIND is always O(1) - just return `term.getFind()`

### Benefits

1. **Guaranteed O(1) FIND**: No path traversal ever needed
2. **Simpler FIND**: No recursion, no loops, no path compression
3. **Predictable Performance**: Every FIND takes exactly the same time

### Trade-offs

**Pros:**
- True O(1) FIND (not amortized)
- Simpler FIND implementation
- More predictable performance

**Cons:**
- More expensive UNION (must update all finds)
- Higher memory writes
- May be slower overall if FIND is rare

**When to Use:**
- FIND is called very frequently
- Problem has many equivalence classes (FINDs outnumber UNIONs)
- Predictable performance is important

### Complexity

**Standard Approach:**
- FIND: O(α(n)) amortized
- UNION: O(k) where k = size of smaller class

**Non-Recursive Approach:**
- FIND: O(1) guaranteed
- UNION: O(k) where k = size of merged class (same, but always updates all finds)

**Trade-off:** Pay more in UNION to guarantee O(1) FIND

### Comparison Table

| Aspect | Standard FIND | Non-Recursive FIND |
|--------|---------------|-------------------|
| FIND complexity | O(α(n)) amortized | O(1) guaranteed |
| UNION complexity | O(k) | O(k) with more work |
| Path compression | Yes, lazy | No, eager in UNION |
| Recursion | Optional | None |
| Memory writes | Lazy (in FIND) | Eager (in UNION) |
| Best for | Balanced FIND/UNION | FIND-heavy workloads |

---

## Optimization 3: Additional Ideas (Beyond Assignment)

### 3.1 Path Halving (Alternative to Path Compression)

Instead of full path compression, update every other pointer:

```java
public Term findWithHalving(Term t) {
    while (t.getFind() != t) {
        Term grandparent = t.getFind().getFind();
        t.setFind(grandparent);  // Skip one level
        t = grandparent;
    }
    return t;
}
```

**Benefit:** Simpler than full path compression, still good amortized performance

### 3.2 Union by Rank

Alternative to union by ccpar size - union by rank (depth):

```java
// Each equivalence class has a rank (maximum depth)
if (class1.getRank() > class2.getRank()) {
    // Attach class2 under class1
} else if (class1.getRank() < class2.getRank()) {
    // Attach class1 under class2
} else {
    // Ranks equal, attach either and increment rank
}
```

**Note:** Our assignment requires union by largest ccpar, not by rank!

### 3.3 Incremental Congruence Closure

Instead of processing all equalities at once, process them incrementally:
- Maintain CC structure across multiple queries
- Only update what changed

**Use Case:** Interactive theorem provers, SMT solvers

### 3.4 Term Indexing

For large DAGs, use indexing structures to speed up congruence checks:
- Hash table keyed by (function symbol, arg representatives)
- Quickly find function applications with congruent arguments

### 3.5 Backtracking Support

For integration with SAT solvers:
- Support undoing UNIONs
- Maintain version history of equivalence classes

**Use Case:** DPLL(T) architecture

---

## Implementation Strategy

### Phase 5 Plan

#### Week 1: Forbidden List/Set
1. Implement ForbiddenSet class
2. Integrate with ClassManager
3. Add configuration flag (enable/disable)
4. Test with UNSAT examples
5. Measure performance impact

#### Week 2: Non-Recursive FIND
1. Implement modified UNION with eager updates
2. Implement O(1) FIND
3. Add configuration flag (enable/disable)
4. Test correctness
5. Measure performance impact

#### Week 3: Experimental Evaluation
1. Create benchmark suite:
   - SAT instances (various sizes)
   - UNSAT instances (various sizes)
   - Mixed theories
2. Run experiments with:
   - Baseline (no optimizations)
   - Baseline + forbidden set
   - Baseline + non-recursive FIND
   - All optimizations enabled
3. Collect metrics:
   - Runtime
   - Number of FINDs
   - Number of UNIONs
   - Memory usage
4. Analyze results:
   - Which optimization helps when?
   - Any negative impacts?
   - Interaction between optimizations?

---

## Configuration System

### Config Class

```java
public class SolverConfig {
    // Mandatory optimization (always enabled)
    private final boolean useLargestCcpar = true;

    // Optional optimizations (configurable)
    private boolean useForbiddenSet = false;
    private boolean useNonRecursiveFIND = false;

    // Getters and setters
    public boolean isUseForbiddenSet() { return useForbiddenSet; }
    public void setUseForbiddenSet(boolean value) { this.useForbiddenSet = value; }

    public boolean isUseNonRecursiveFIND() { return useNonRecursiveFIND; }
    public void setUseNonRecursiveFIND(boolean value) { this.useNonRecursiveFIND = value; }

    // Factory methods for common configurations
    public static SolverConfig baseline() {
        return new SolverConfig();  // No optional optimizations
    }

    public static SolverConfig allOptimizations() {
        SolverConfig config = new SolverConfig();
        config.setUseForbiddenSet(true);
        config.setUseNonRecursiveFIND(true);
        return config;
    }
}
```

### Command-Line Interface

```bash
# Baseline (only mandatory optimizations)
java -jar solver.jar input.txt

# Enable forbidden set
java -jar solver.jar --forbidden-set input.txt

# Enable non-recursive FIND
java -jar solver.jar --non-recursive-find input.txt

# Enable all optimizations
java -jar solver.jar --all-optimizations input.txt

# Experimental comparison
java -jar solver.jar --benchmark input.txt
# Runs with all configurations and reports comparison
```

---

## Expected Performance Impact

### Forbidden List/Set

**Expected Speedup:**
- UNSAT instances: 10-50% faster (early detection)
- SAT instances: 0-5% slower (extra checks)
- Overall: Depends on SAT/UNSAT ratio

**Best Case:** UNSAT with early contradiction
**Worst Case:** SAT with many disequalities (wasted checks)

### Non-Recursive FIND

**Expected Speedup:**
- FIND-heavy workloads: 5-20% faster
- UNION-heavy workloads: 0-10% slower
- Overall: Depends on FIND/UNION ratio

**Best Case:** Many FINDs, few UNIONs
**Worst Case:** Few FINDs, many UNIONs (extra work in UNION not amortized)

---

## Evaluation Criteria

For the report, evaluate optimizations on:

1. **Correctness**: Do they preserve correctness?
2. **Performance**: Runtime improvement?
3. **Memory**: Memory overhead?
4. **Ease of Implementation**: How complex to implement?
5. **When to Use**: Under what conditions do they help?

---

## Report Section Planning

### Implementation Section

Describe:
- Which optional optimizations were implemented
- How they work
- Design decisions made

### Experiments Section

Present:
- Benchmark suite used
- Results with/without optimizations
- Tables and plots comparing configurations
- Statistical analysis

### Analysis Section

Discuss:
- Performance characteristics observed
- When each optimization helps
- Unexpected results
- Recommendations for use

---

## Summary

### Mandatory (Already Planned)
- ✅ Largest ccpar optimization in UNION

### Optional (Phase 5)
- ⏳ Forbidden list/set for early UNSAT detection
- ⏳ Non-recursive FIND for O(1) guaranteed performance

### Additional Ideas (If Time Permits)
- Path halving
- Term indexing
- Backtracking support

### Evaluation Plan
- Implement both optional optimizations
- Create benchmark suite
- Run experiments comparing configurations
- Analyze results for report

---

**These optimizations provide opportunities to improve performance while demonstrating understanding of algorithm trade-offs!**
