# Equivalence Class Data Structures Design

This document provides detailed design for the equivalence class management system used in the Congruence Closure algorithm.

---

## Overview

Equivalence classes are the core data structure for tracking which terms are known to be equal. The CC algorithm maintains and merges these classes as it processes equality constraints.

**Key Operations:**
- **FIND(t)**: Returns the representative term of the equivalence class containing t
- **UNION(t1, t2)**: Merges the equivalence classes of t1 and t2

**Key References:**
- Bradley & Manna Section 9.3 (CC algorithm)
- Downey et al. [8] page 761 (largest ccpar optimization)
- Detlef et al. [7] page 423 (largest ccpar optimization)

---

## Design Goals

1. **Efficient FIND**: O(log n) or better with path compression
2. **Efficient UNION**: O(1) reference update + O(k) class merging where k = size of smaller class
3. **Largest ccpar Optimization**: MANDATORY - choose representative with larger ccpar set in UNION
4. **Maintain Class Membership**: Track all terms in each equivalence class
5. **Support Disequality Checks**: Quick check if two terms are in same class

---

## Core Data Structures

### EquivalenceClass

**File:** `src/main/java/solver/equivalence/EquivalenceClass.java`

```java
public class EquivalenceClass {
    // Representative term of this equivalence class
    private Term representative;

    // All terms in this equivalence class
    private final Set<Term> members;

    // Cached size of the ccpar set of the representative
    // (For efficient comparison during UNION)
    private int ccparSize;

    // Unique ID for this equivalence class (for debugging)
    private final int id;

    // Constructor for initial singleton class
    public EquivalenceClass(int id, Term term) {
        this.id = id;
        this.representative = term;
        this.members = new HashSet<>();
        this.members.add(term);
        this.ccparSize = term.getCcpar().size();
    }

    // Getters
    public Term getRepresentative() {
        return representative;
    }

    public Set<Term> getMembers() {
        return Collections.unmodifiableSet(members);
    }

    public int getCcparSize() {
        return ccparSize;
    }

    public int getId() {
        return id;
    }

    public int size() {
        return members.size();
    }

    // Check if a term is in this class
    public boolean contains(Term term) {
        return members.contains(term);
    }

    // Add a term to this class (used during UNION)
    public void addMember(Term term) {
        members.add(term);
    }

    // Add all terms from another class (used during UNION)
    public void addAll(EquivalenceClass other) {
        members.addAll(other.members);
    }

    // Update representative (used during UNION)
    public void setRepresentative(Term newRep) {
        this.representative = newRep;
        this.ccparSize = newRep.getCcpar().size();
    }

    @Override
    public String toString() {
        return String.format("Class[%d] rep=%s size=%d members=%s",
            id, representative.getSymbol(), members.size(),
            members.stream().map(Term::getSymbol).collect(Collectors.toList()));
    }
}
```

**Design Decisions:**

1. **Representative Field**: The canonical term for this class
   - Used by FIND to return consistent results
   - Updated during UNION based on ccpar size

2. **Members Set**: Explicitly tracks all terms in the class
   - Needed for merging classes during UNION
   - Useful for debugging and result reporting

3. **Cached ccpar Size**: Avoid repeated set size calculations
   - Updated when representative changes
   - Critical for performance in UNION comparisons

---

### ClassManager

**File:** `src/main/java/solver/equivalence/ClassManager.java`

**Purpose:** Global manager for all equivalence classes, provides FIND and UNION operations.

```java
public class ClassManager {
    // Maps each term to its current equivalence class
    private final Map<Term, EquivalenceClass> termToClass;

    // Counter for assigning unique class IDs
    private int nextClassId;

    public ClassManager() {
        this.termToClass = new HashMap<>();
        this.nextClassId = 1;
    }

    // Initialize a term with its own singleton equivalence class
    public void initialize(Term term) {
        if (!termToClass.containsKey(term)) {
            EquivalenceClass newClass = new EquivalenceClass(nextClassId++, term);
            termToClass.put(term, newClass);
            term.setFind(term);  // Initially points to self
        }
    }

    // Initialize all terms in a collection
    public void initializeAll(Collection<Term> terms) {
        for (Term term : terms) {
            initialize(term);
        }
    }

    // FIND: Get the representative of t's equivalence class
    public Term find(Term t) {
        // Follow find pointers until we reach representative
        Term current = t;
        while (current.getFind() != current) {
            current = current.getFind();
        }

        // Path compression: update find pointers along the path
        Term representative = current;
        current = t;
        while (current != representative) {
            Term next = current.getFind();
            current.setFind(representative);
            current = next;
        }

        return representative;
    }

    // Check if two terms are in the same equivalence class
    public boolean areEqual(Term t1, Term t2) {
        return find(t1) == find(t2);
    }

    // Get the equivalence class for a term
    public EquivalenceClass getClass(Term t) {
        Term representative = find(t);
        return termToClass.get(representative);
    }

    // UNION: Merge the equivalence classes of t1 and t2
    // CRITICAL: Implements largest ccpar optimization!
    public void union(Term t1, Term t2) {
        // Get representatives
        Term rep1 = find(t1);
        Term rep2 = find(t2);

        // Already in same class?
        if (rep1 == rep2) {
            return;
        }

        // Get the two classes
        EquivalenceClass class1 = termToClass.get(rep1);
        EquivalenceClass class2 = termToClass.get(rep2);

        // MANDATORY OPTIMIZATION: Choose representative with larger ccpar set
        Term newRep;
        EquivalenceClass keepClass;
        EquivalenceClass mergeClass;

        if (class1.getCcparSize() >= class2.getCcparSize()) {
            // class1 has larger (or equal) ccpar, keep it as representative
            newRep = rep1;
            keepClass = class1;
            mergeClass = class2;
        } else {
            // class2 has larger ccpar, use it as representative
            newRep = rep2;
            keepClass = class2;
            mergeClass = class1;
        }

        // Merge: add all members from mergeClass into keepClass
        keepClass.addAll(mergeClass);

        // Update find pointers for all terms in merged class
        for (Term term : mergeClass.getMembers()) {
            term.setFind(newRep);
            termToClass.put(term, keepClass);
        }

        // Update representative's find pointer
        rep1.setFind(newRep);
        rep2.setFind(newRep);
    }

    // Get all equivalence classes (for result reporting)
    public Set<EquivalenceClass> getAllClasses() {
        Set<EquivalenceClass> classes = new HashSet<>();
        for (EquivalenceClass ec : termToClass.values()) {
            classes.add(ec);
        }
        return classes;
    }

    // Get total number of equivalence classes
    public int getClassCount() {
        return getAllClasses().size();
    }

    // Get statistics (for debugging)
    public String getStats() {
        int totalTerms = termToClass.size();
        int totalClasses = getClassCount();
        int maxClassSize = getAllClasses().stream()
            .mapToInt(EquivalenceClass::size)
            .max()
            .orElse(0);

        return String.format("Terms: %d, Classes: %d, Max class size: %d",
            totalTerms, totalClasses, maxClassSize);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("Equivalence Classes:\n");
        for (EquivalenceClass ec : getAllClasses()) {
            sb.append("  ").append(ec.toString()).append("\n");
        }
        return sb.toString();
    }
}
```

**Key Algorithms:**

#### FIND with Path Compression

```
FIND(t):
    1. Follow find pointers until we reach a term that points to itself
    2. This term is the representative
    3. PATH COMPRESSION: Update all find pointers along the path to point directly to representative
    4. Return representative

Time Complexity: O(α(n)) amortized, where α is inverse Ackermann function (effectively O(1))
```

**Example:**
```
Initial: a.find=b, b.find=c, c.find=c
FIND(a):
  1. Follow: a → b → c (c is representative)
  2. Compress: a.find=c, b.find=c
  3. Return c

Next FIND(a): a → c (only one hop!)
```

#### UNION with Largest ccpar

```
UNION(t1, t2):
    1. Find representatives: rep1 = FIND(t1), rep2 = FIND(t2)
    2. If rep1 == rep2, return (already merged)
    3. Get ccpar sizes: size1 = |ccpar(rep1)|, size2 = |ccpar(rep2)|
    4. Choose newRep = (size1 >= size2) ? rep1 : rep2
    5. Merge smaller class into larger class
    6. Update find pointers for all terms in smaller class
    7. Update term-to-class mapping

Time Complexity: O(k) where k = size of smaller class
```

**Why Largest ccpar?**

From Downey et al.:
> "Choosing the representative with the largest ccpar set minimizes the number of congruence propagations in MERGE"

When we merge class A into class B:
- All parents in ccpar(A) need to be checked for congruence
- By keeping the larger ccpar set as representative, we minimize this work

**Example:**
```
Terms: a, b, f(a), g(a, b), h(b)

ccpar(a) = {f(a), g(a, b)}  // size 2
ccpar(b) = {g(a, b), h(b)}  // size 2

UNION(a, b):
  - Sizes are equal, either can be chosen
  - Let's say we choose a as representative
  - Now: FIND(b) = a
  - Class contains: {a, b}
```

---

## Integration with DAG

**Workflow:**

1. **Initialization Phase:**
```java
DAG dag = new DAG();
ClassManager cm = new ClassManager();

// Build all terms
Term a = dag.getFactory().createVariable("a");
Term b = dag.getFactory().createVariable("b");
dag.addTerm(a);
dag.addTerm(b);

// Initialize equivalence classes
cm.initializeAll(dag.getTerms());

// Now each term is in its own singleton class
```

2. **Merging Phase (from equalities):**
```java
// Process equality: a = b
cm.union(a, b);

// Now FIND(a) == FIND(b)
assert cm.areEqual(a, b);
```

3. **Query Phase (check disequalities):**
```java
// Check disequality: a != b
if (cm.areEqual(a, b)) {
    // UNSAT! They must be equal but shouldn't be
    return Result.UNSAT;
}
```

---

## Optimization: Lazy vs Eager Path Compression

### Current Design: Eager Path Compression

In our FIND implementation above, we use **eager path compression**:
- During FIND, update all nodes along the path immediately
- Next FIND on any of those nodes is O(1)

### Alternative: Lazy Path Compression (Optional)

```java
// Simple recursive version (no path compression)
public Term findSimple(Term t) {
    if (t.getFind() == t) {
        return t;
    }
    return findSimple(t.getFind());
}

// Lazy path compression (halving)
public Term findLazy(Term t) {
    while (t.getFind() != t) {
        Term grandparent = t.getFind().getFind();
        t.setFind(grandparent);  // Skip one level
        t = grandparent;
    }
    return t;
}
```

**Trade-off:**
- Eager: More work in single FIND, but subsequent FINDs are faster
- Lazy: Less work per FIND, but may need multiple hops

**Decision:** Use eager path compression (from Design 1)
- Provides best amortized complexity
- Standard in union-find implementations

---

## Optional Optimization: Non-Recursive FIND

From Detlef et al. [7]:

> "Non-recursive FIND coupled with UNION that updates find fields"

**Modified Design:**

```java
// Non-recursive FIND (already implemented above!)
public Term findNonRecursive(Term t) {
    // Find representative
    Term current = t;
    while (current.getFind() != current) {
        current = current.getFind();
    }
    Term representative = current;

    // Path compression pass
    current = t;
    while (current != representative) {
        Term next = current.getFind();
        current.setFind(representative);
        current = next;
    }

    return representative;
}

// Modified UNION that updates all find fields immediately
public void unionNonRecursive(Term t1, Term t2) {
    // ... choose newRep based on ccpar size ...

    // Update find fields for ALL terms in merged class
    for (Term term : mergeClass.getMembers()) {
        term.setFind(newRep);  // Direct update, no lazy evaluation
    }
}
```

**Trade-off:**
- Pro: FIND is guaranteed O(1) after UNION (no path traversal)
- Con: UNION is more expensive (must update all finds immediately)

**Decision:** Start with standard implementation, add this as optional optimization in Phase 5

---

## Forbidden List/Set (Optional Optimization)

From Detlef et al. [7] page 388:

> "Forbidden list prevents certain merges that would lead to inconsistencies"

**Extended Design:**

```java
public class ClassManager {
    // ... existing fields ...

    // Optional: Set of term pairs that must NOT be merged
    private final Set<TermPair> forbiddenMerges;

    public void addForbiddenMerge(Term t1, Term t2) {
        forbiddenMerges.add(new TermPair(t1, t2));
    }

    public void union(Term t1, Term t2) {
        Term rep1 = find(t1);
        Term rep2 = find(t2);

        // Check if merge is forbidden
        if (forbiddenMerges.contains(new TermPair(rep1, rep2))) {
            throw new UnsatisfiableException("Forbidden merge detected");
        }

        // ... continue with normal union ...
    }
}
```

**Use Case:** Early UNSAT detection
- If we know certain terms must be different, add to forbidden list
- If UNION attempts to merge them → immediate UNSAT

**Decision:** Implement as optional feature in Phase 5

---

## Data Structure Complexity

| Operation | Time Complexity | Space |
|-----------|----------------|--------|
| initialize(term) | O(1) | O(1) per term |
| find(term) | O(α(n)) amortized | O(1) |
| find with path compression | O(1) amortized after compression | O(1) |
| union(t1, t2) | O(k) where k = min(size(class1), size(class2)) | O(1) |
| areEqual(t1, t2) | O(α(n)) amortized | O(1) |
| getAllClasses() | O(n) where n = total terms | O(n) |

**Overall Complexity for CC Algorithm:**
- m equality merges on n terms: O(m × α(n)) ≈ O(m) in practice
- Near-linear performance!

---

## Memory Layout

**Per Term:**
- find pointer: 8 bytes
- Already part of Term structure

**Per EquivalenceClass:**
- representative: 8 bytes (reference)
- members Set: ~40 bytes (HashSet overhead) + 8 bytes per member
- ccparSize: 4 bytes (int)
- id: 4 bytes (int)
- **Total:** ~56 bytes + size(members) × 8

**For ClassManager:**
- termToClass Map: ~40 bytes + (16 bytes × num_terms)
- **Total:** ~40 bytes + 16n where n = number of terms

**Example: 1000 terms, 100 equivalence classes (average 10 terms/class):**
- Terms: Already counted in DAG (~80KB)
- Classes: 100 × (56 + 10×8) = ~13.6KB
- Manager: 40 + 16×1000 = ~16KB
- **Total overhead:** ~30KB (very reasonable)

---

## Testing Strategy

### Unit Tests for EquivalenceClass

1. **Creation Tests:**
   - Singleton class creation
   - Initial representative
   - Initial members set

2. **Modification Tests:**
   - addMember
   - addAll
   - setRepresentative

3. **Query Tests:**
   - contains
   - size
   - getCcparSize

### Unit Tests for ClassManager

1. **Initialization Tests:**
   - Single term initialization
   - Multiple term initialization
   - Idempotent initialization

2. **FIND Tests:**
   - Find on singleton class
   - Find after union
   - Path compression verification

3. **UNION Tests:**
   - Union two singletons
   - Union with largest ccpar check
   - Union already merged (no-op)
   - Multiple sequential unions

4. **Query Tests:**
   - areEqual
   - getClass
   - getAllClasses
   - getClassCount

### Integration Tests

1. **CC Integration:**
   - Build DAG, initialize classes
   - Merge based on equalities
   - Check final equivalence classes

2. **Performance Tests:**
   - Large number of terms
   - Many unions
   - Measure time for FIND/UNION

---

## Example: Complete Workflow

```java
// 1. Setup
DAG dag = new DAG();
ClassManager cm = new ClassManager();
TermFactory factory = dag.getFactory();

// 2. Build terms for: a = b, b = c, f(a) = f(b)
Variable a = factory.createVariable("a");
Variable b = factory.createVariable("b");
Variable c = factory.createVariable("c");
FunctionApp fa = factory.createFunctionApp("f", a);
FunctionApp fb = factory.createFunctionApp("f", b);

dag.addTerm(fa);
dag.addTerm(fb);
dag.addTerm(c);

// 3. Initialize all terms with singleton equivalence classes
cm.initializeAll(dag.getTerms());

System.out.println("Initial classes: " + cm.getClassCount());  // 5 classes
// Classes: {a}, {b}, {c}, {f(a)}, {f(b)}

// 4. Process equality: a = b
cm.union(a, b);
System.out.println("After a=b: " + cm.getClassCount());  // 4 classes
// Classes: {a,b}, {c}, {f(a)}, {f(b)}

// Check: ccpar(a) = {f(a)}, ccpar(b) = {f(b)}
// Union chose representative with larger ccpar (or equal)

// 5. Process equality: b = c
cm.union(b, c);
System.out.println("After b=c: " + cm.getClassCount());  // 3 classes
// Classes: {a,b,c}, {f(a)}, {f(b)}

// 6. Query: Are a and c equal?
System.out.println("a == c? " + cm.areEqual(a, c));  // true

// 7. Query: Representative of a?
Term repA = cm.find(a);
System.out.println("Representative of a: " + repA.getSymbol());

// 8. Get final equivalence classes
for (EquivalenceClass ec : cm.getAllClasses()) {
    System.out.println(ec);
}
// Output:
//   Class[1] rep=a size=3 members=[a, b, c]
//   Class[4] rep=f(a) size=1 members=[f(a)]
//   Class[5] rep=f(b) size=1 members=[f(b)]
```

---

## Implementation Plan

### Step 1: EquivalenceClass
- [ ] Implement EquivalenceClass class
- [ ] Add representative, members, ccparSize fields
- [ ] Implement getters and modifiers
- [ ] Add unit tests

### Step 2: ClassManager Initialization
- [ ] Implement ClassManager class
- [ ] Add termToClass mapping
- [ ] Implement initialize methods
- [ ] Add unit tests

### Step 3: FIND Implementation
- [ ] Implement basic FIND (follow find pointers)
- [ ] Add path compression
- [ ] Add unit tests
- [ ] Performance test

### Step 4: UNION Implementation
- [ ] Implement basic UNION (merge classes)
- [ ] Add largest ccpar optimization
- [ ] Update all find pointers
- [ ] Add unit tests
- [ ] Verify optimization works

### Step 5: Query Operations
- [ ] Implement areEqual
- [ ] Implement getClass
- [ ] Implement getAllClasses
- [ ] Add unit tests

### Step 6: Integration Testing
- [ ] Test with DAG
- [ ] Test with multiple merges
- [ ] Verify correctness

---

## Design Rationale

### Why Explicit EquivalenceClass Objects?

**Alternative:** Just use term.find pointers without class objects

**Our Choice:** Explicit EquivalenceClass objects

**Rationale:**
1. **Clarity:** Makes equivalence classes first-class entities
2. **Membership:** Easy to get all terms in a class
3. **Statistics:** Track class sizes, ccpar sizes
4. **Debugging:** Can inspect and print classes
5. **Results:** Easy to report final equivalence classes for SAT results

Trade-off: Slightly more memory, but much better usability

### Why Eager Path Compression?

**Alternatives:** No compression, lazy compression, halving

**Our Choice:** Eager path compression in FIND

**Rationale:**
1. **Performance:** Best amortized complexity O(α(n))
2. **Simplicity:** Standard implementation, well-understood
3. **Predictability:** Subsequent FINDs are very fast

### Why Cached ccpar Size?

**Alternative:** Compute ccpar size on-demand in UNION

**Our Choice:** Cache ccpar size in EquivalenceClass

**Rationale:**
1. **Performance:** O(1) comparison instead of O(k) set iteration
2. **Frequent Access:** UNION is called many times
3. **Low Overhead:** Just 4 bytes per class

---

## Next Steps

After completing the equivalence class design and implementation:
1. Move to Issue #5: Input format specification
2. Move to Issue #6: Output format specification
3. Then implement the core CC algorithm using DAG + EquivalenceClass
4. Test with simple equality examples

---

**This design provides efficient, optimized equivalence class management for the CC algorithm!**
