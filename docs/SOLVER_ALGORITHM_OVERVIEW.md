# Solver Algorithm Overview

This document provides a high-level overview of what the solver needs to do, extracted from the assignment. Use this as a reference while reading the literature.

---

## Input
A set (conjunction) of literals in the union of:
- **T_E:** Theory of Equality with uninterpreted functions
- **T_cons:** Theory of Lists (cons, car, cdr)
- **T_A:** Theory of Arrays (select, store) - without extensionality

**Key Insight:** The three theories can be MIXED in the input. It's not just one theory at a time.

---

## Output
- **SAT:** The literal set is satisfiable
- **UNSAT:** The literal set is unsatisfiable

---

## High-Level Algorithm (From Assignment)

```
Algorithm: Main Solver
Input: Set of literals S
Output: SAT or UNSAT

1. Test if S contains any `store` symbols

   IF store symbols exist THEN
      Decompose S into subproblems S_1, S_2, ...
      (Two subproblems for EACH occurrence of store)
      According to T_A-procedure (Bradley & Manna Section 9.5)
   ELSE
      S = {S}  // S is the only subproblem

2. FOR EACH subproblem S_k:

   a. Process all `select` symbols in S_k
      According to T_A-procedure (Bradley & Manna Section 9.5)

   b. Test if S_k contains T_cons symbols (cons, car, cdr)

      IF T_cons symbols exist THEN
         Apply T_cons-procedure to S_k
         (Bradley & Manna Section 9.4)
         This builds T_cons axioms into CC algorithm
      ELSE
         Apply T_E-procedure to S_k
         (Bradley & Manna Section 9.3)
         This is just the CC algorithm

   c. If S_k is SAT THEN
         RETURN SAT

3. IF all subproblems are UNSAT THEN
      RETURN UNSAT
```

---

## Why No Nelson-Oppen?

From the assignment:
> "Since satisfiability in T_cons reduces to satisfiability in T_E, and satisfiability in T_A reduces to satisfiability in T_E, it is not necessary to implement the Nelson-Oppen scheme for theory combination."

**Translation:**
- T_cons → T_E (through axiom integration)
- T_A → T_E (through store decomposition and select processing)
- Everything ultimately reduces to checking satisfiability in T_E using CC

---

## The Heart: Congruence Closure (CC) Algorithm

The CC algorithm is the foundation that everything else builds upon.

### Core Components

1. **FIND(t):** Returns the representative of the equivalence class containing term t

2. **UNION(t1, t2):** Merges the equivalence classes of t1 and t2
   - **CRITICAL OPTIMIZATION:** Choose representative with largest ccpar set
   - Mandatory: From Downey et al. page 761 and Detlef et al. page 423

3. **MERGE(t1, t2):** Merge classes and propagate congruences
   - Uses pending list/queue

4. **CONGRUENT(t1, t2):** Check if two function applications are congruent

### Data Structures

1. **DAG:** Terms represented as Directed Acyclic Graph
   - Nodes = terms
   - Edges = subterm relationships

2. **Equivalence Classes:** Track which terms are equal
   - Each class has a representative (find field)
   - Track ccpar sets for UNION optimization

3. **Pending List/Queue:** Track merges that need propagation

---

## T_cons-Procedure (Theory of Lists)

### Symbols
- `cons(x, y)` - construct list
- `car(x)` - head of list
- `cdr(x)` - tail of list

### Axioms (Built into CC)
1. `car(cons(x, y)) = x`
2. `cdr(cons(x, y)) = y`

### Special Consideration
- Lists can be **cyclic**

**Read:** Bradley & Manna Section 9.4

---

## T_A-Procedure (Theory of Arrays)

### Symbols
- `select(a, i)` - read array a at index i
- `store(a, i, v)` - write value v to array a at index i

### Store Decomposition
For each occurrence of `store(a, i, v)` in position j:
- Create TWO subproblems:
  1. `i = j ∧ select(store(a,i,v), j) = v`
  2. `i ≠ j ∧ select(store(a,i,v), j) = select(a, j)`

**Translation:**
- If the index matches, you get the stored value
- If the index doesn't match, you get the original array value

### Select Processing
After all stores are processed, handle select operations using read-over-write axioms.

**Read:** Bradley & Manna Section 9.5

---

## Mandatory Optimization

### Largest ccpar Set in UNION

**From assignment:**
> "The CC algorithm as described in Sect. 9.3 of reference [1] should be improved with a non-arbitrary choice of the representative of the union class in the UNION function: pick the one with the largest ccpar set"

**Why?**
- Performance optimization
- Reduces number of congruence checks needed

**Where to read:**
- Downey et al. [8], page 761 (top)
- Detlef et al. [7], page 423 (top)

---

## Optional Optimizations

### 1. Forbidden List/Set
**Source:** Detlef et al. [7], page 388 (bottom)

**Purpose:** Prevent certain merges

**Implementation:** Can be made configurable option to evaluate impact

### 2. Non-recursive FIND
**Source:** Detlef et al. [7]

**Coupled with:** UNION that updates find fields of all terms in the class whose representative is not chosen

**Implementation:** Can be made configurable option to evaluate impact

---

## Implementation Strategy Summary

### Phase 1: Core CC Algorithm
1. Implement DAG representation
2. Implement equivalence classes
3. Implement FIND (recursive version)
4. Implement UNION (with largest ccpar optimization)
5. Implement MERGE
6. Implement CONGRUENT
7. Test with T_E examples

### Phase 2: T_cons Integration
1. Recognize cons, car, cdr symbols
2. Build T_cons axioms into CC
3. Test with list examples

### Phase 3: T_A Integration
1. Implement store decomposition
2. Implement select processing
3. Integrate with CC
4. Test with array examples

### Phase 4: Main Solver
1. Implement input parser
2. Implement theory detection
3. Implement subproblem generation
4. Implement main solver loop
5. Test with mixed theory examples

---

## Example Input/Output

### Example 1: T_E (Pure Equality)
**Input:**
```
a = b
b = c
c != a
```
**Expected Output:** UNSAT (transitivity violation)

### Example 2: T_cons (Lists)
**Input:**
```
x = cons(a, b)
car(x) != a
```
**Expected Output:** UNSAT (violates car axiom)

### Example 3: T_A (Arrays)
**Input:**
```
select(store(a, i, v), i) != v
```
**Expected Output:** UNSAT (violates array axiom)

### Example 4: Mixed Theories
**Input:**
```
x = cons(a, b)
y = select(arr, car(x))
y != select(arr, a)
```
**Expected Output:** UNSAT (because car(x) = a by T_cons)

---

## Testing Strategy

### Test Sources (From Assignment)
1. Examples from Bradley & Manna book
2. Examples from Kroening & Strichman book
3. Examples from referenced papers
4. Transform general formulas to literal sets
5. (Optional) Generate synthetic tests
6. (Optional) SMT-LIB QF-UF benchmarks

### Transformations for Non-Literal Inputs
- Free predicate symbols → transformation explained in class
- Not CNF → convert to DNF, test each disjunct
- Arithmetic symbols → replace with free symbols
- Quantifiers → drop them, treat variables as free

**Note:** Last two transformations don't preserve equisatisfiability - just for more test cases

---

## Report Requirements (To Keep in Mind While Reading)

The report (max 6 pages, 11pt) must present:

1. **Implementation**
   - Data structures chosen (why?)
   - Heuristics used (impact?)
   - Other significant choices

2. **Experiments**
   - Tables/plots of results
   - SAT/UNSAT answers
   - Runtime
   - Problem sources

3. **Analysis**
   - Performance comments
   - Impact of optimizations
   - Interesting observations

**Critical:** NO AI-generated content allowed in report!

---

## Questions to Answer Through Literature Review

1. What exactly is a ccpar set?
2. How does largest ccpar optimization work?
3. How do we represent terms as a DAG?
4. What data structure for equivalence classes?
5. How does the pending list work in MERGE?
6. How do T_cons axioms integrate into CC?
7. Why does store create exactly two subproblems?
8. What are read-over-write axioms for select?
9. How do we handle cyclic lists?
10. What is the complexity of CC algorithm?

---

**Keep this document handy while reading the papers - it provides context for WHY you need to understand each concept!**
