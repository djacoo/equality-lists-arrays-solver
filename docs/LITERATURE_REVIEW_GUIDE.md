# Literature Review Guide - Task 1.2

## Overview
This document tracks the literature review for implementing the solver. Focus on understanding the algorithms, data structures, and optimization techniques needed for implementation.

---

## Required Readings (Priority Order)

### 1. Bradley & Manna - "The Calculus of Computation" (PRIMARY REFERENCE)

**Book Details:**
- Authors: Aaron R. Bradley, Zohar Manna
- Title: The Calculus of Computation. Decision Procedures with Applications to Verification
- Publisher: Springer, 2007
- ISBN: 978-3-642-09347-0

#### Section 9.3: T_E-Procedure (Theory of Equality)
**Status:** [ ] Not Started / [ ] In Progress / [ ] Completed

**Key Concepts to Understand:**
- [ ] Congruence Closure (CC) algorithm
- [ ] DAG representation of terms
- [ ] Equivalence classes
- [ ] FIND function
- [ ] UNION function
- [ ] MERGE procedure
- [ ] CONGRUENT check
- [ ] ccpar sets (congruence closure parents)
- [ ] Pending list/queue for merge propagation

**Implementation Notes:**
```
[Add notes here as you read]
-
```

---

#### Section 9.4: T_cons-Procedure (Theory of Lists)
**Status:** [ ] Not Started / [ ] In Progress / [ ] Completed

**Key Concepts to Understand:**
- [ ] T_cons symbols: cons, car, cdr
- [ ] Integration of T_cons axioms into CC
- [ ] Axiom: car(cons(x,y)) = x
- [ ] Axiom: cdr(cons(x,y)) = y
- [ ] Handling cyclic lists
- [ ] How T_cons reduces to T_E

**Implementation Notes:**
```
[Add notes here as you read]
-
```

---

#### Section 9.5: T_A-Procedure (Theory of Arrays)
**Status:** [ ] Not Started / [ ] In Progress / [ ] Completed

**Key Concepts to Understand:**
- [ ] Array symbols: select, store
- [ ] Store decomposition strategy
  - [ ] Subproblem 1: i = j ∧ select(store(a,i,v),j) = v
  - [ ] Subproblem 2: i ≠ j ∧ select(store(a,i,v),j) = select(a,j)
- [ ] Select processing (read-over-write axioms)
- [ ] Handling multiple store operations
- [ ] How T_A reduces to T_E
- [ ] Why no Nelson-Oppen is needed

**Implementation Notes:**
```
[Add notes here as you read]
-
```

---

### 2. Downey, Sethi & Tarjan - "Variations on the common subexpression problem"

**Paper Details:**
- Authors: Peter J. Downey, Ravi Sethi, and Robert Endre Tarjan
- Title: Variations on the common subexpression problem
- Journal: Journal of the ACM 27(4):758-771, 1980

**Focus:** Page 761 (top)
**Status:** [ ] Not Started / [ ] In Progress / [ ] Completed

**Key Concepts to Understand:**
- [ ] UNION optimization: largest ccpar set
- [ ] Why choosing the representative with largest ccpar matters
- [ ] Performance implications
- [ ] Implementation strategy

**Implementation Notes:**
```
[Add notes here as you read]
-
```

---

### 3. Detlef, Nelson & Saxe - "Simplify: a theorem prover for program checking"

**Paper Details:**
- Authors: David L. Detlef, Greg Nelson and James B. Saxe
- Title: Simplify: a theorem prover for program checking
- Journal: Journal of the ACM 52(3):365-473, 2005

**Focus:** Page 423 (top) and Page 388 (bottom)
**Status:** [ ] Not Started / [ ] In Progress / [ ] Completed

**Key Concepts to Understand:**
- [ ] Page 423: UNION optimization (largest ccpar)
- [ ] Page 388: Forbidden list/set optimization (OPTIONAL)
  - [ ] What is a forbidden list/set?
  - [ ] How does it prevent certain merges?
  - [ ] Performance trade-offs

**Implementation Notes:**
```
[Add notes here as you read]
-
```

---

### 4. Kroening & Strichman - "Decision Procedures"

**Book Details:**
- Authors: Daniel Kroening, Ofer Strichman
- Title: Decision Procedures. An Algorithmic Point of View
- Publisher: Springer, 2008
- ISBN: 978-3-540-74104-6

**Status:** [ ] Not Started / [ ] In Progress / [ ] Completed

**Key Concepts to Understand:**
- [ ] Alternative explanations of congruence closure
- [ ] Comparison with other decision procedures
- [ ] Implementation considerations

**Implementation Notes:**
```
[Add notes here as you read]
-
```

---

### 5. Nelson & Oppen - "Fast decision procedures based on congruence closure"

**Paper Details:**
- Authors: Greg Nelson and Derek C. Oppen
- Title: Fast decision procedures based on congruence closure
- Journal: Journal of the ACM 27(2):356-364, 1980

**Status:** [ ] Not Started / [ ] In Progress / [ ] Completed

**Key Concepts to Understand:**
- [ ] Original congruence closure algorithm
- [ ] Historical context
- [ ] Foundational concepts

**Implementation Notes:**
```
[Add notes here as you read]
-
```

---

## Additional Optional References

### 6. Nieuwenhuis & Oliveras - "Fast congruence closure and extensions"
- Journal: Information and Computation 205:557-580, 2007
- Focus: Modern optimizations and extensions

### 7. Bachmair, Tiwari & Vigneron - "Abstract congruence closure"
- Journal: Journal of Automated Reasoning 31(2):129-168, 2003
- Focus: Abstract/theoretical perspective

---

## Key Algorithms to Extract from Literature

### 1. FIND Function
**Purpose:** Find the representative of an equivalence class

**Variants:**
- [ ] Recursive version (from Bradley & Manna)
- [ ] Non-recursive version (OPTIONAL - from Detlef et al.)

**Pseudocode from reading:**
```
[Add pseudocode as you read]
```

---

### 2. UNION Function
**Purpose:** Merge two equivalence classes

**Required Optimization:**
- [x] Choose representative with largest ccpar set (MANDATORY)

**Optional Optimizations:**
- [ ] Update find fields in non-recursive version
- [ ] Forbidden list/set

**Pseudocode from reading:**
```
[Add pseudocode as you read]
```

---

### 3. MERGE Procedure
**Purpose:** Merge classes and propagate congruences

**Pseudocode from reading:**
```
[Add pseudocode as you read]
```

---

### 4. CONGRUENT Check
**Purpose:** Check if two function applications are congruent

**Pseudocode from reading:**
```
[Add pseudocode as you read]
```

---

### 5. CC Algorithm (Main)
**Purpose:** Build congruence closure on a DAG

**Pseudocode from reading:**
```
[Add pseudocode as you read]
```

---

## Data Structures to Extract from Literature

### 1. DAG Representation
**Purpose:** Represent terms as a directed acyclic graph

**Components:**
- [ ] Nodes
- [ ] Edges
- [ ] Function symbols
- [ ] Variables/constants

**Design notes:**
```
[Add notes as you read]
```

---

### 2. Equivalence Classes
**Purpose:** Track which terms are equal

**Components:**
- [ ] Representative (find field)
- [ ] Class members
- [ ] ccpar sets

**Design notes:**
```
[Add notes as you read]
```

---

### 3. Pending List/Queue
**Purpose:** Track merges that need to be propagated

**Design notes:**
```
[Add notes as you read]
```

---

## Understanding DAG Representation and Equivalence Classes

**Status:** [ ] Not Started / [ ] In Progress / [ ] Completed

### What is a DAG?
```
[Add notes from reading]
```

### How terms map to DAG nodes
```
Example from reading:
f(a, g(b)) maps to:
```

### Equivalence class structure
```
[Add diagrams/examples from reading]
```

---

## Summary of Key Insights

### Algorithm Flow
1. **Input:** Set of literals S
2. **Step 1:** Check for `store` symbols → decompose into subproblems
3. **Step 2:** For each subproblem, process `select` symbols
4. **Step 3:** For each subproblem, check for T_cons symbols
5. **Step 4:** Apply T_cons-procedure OR T_E-procedure
6. **Output:** SAT or UNSAT

### Critical Implementation Decisions
- [ ] Data structure for DAG nodes
- [ ] Data structure for equivalence classes
- [ ] How to track ccpar sets
- [ ] How to implement pending list
- [ ] Strategy for choosing UNION representative

---

## Questions to Resolve

1. What exactly is a ccpar set?
   - Answer: [Fill in after reading]

2. How does the largest ccpar optimization improve performance?
   - Answer: [Fill in after reading]

3. How do T_cons axioms integrate into CC?
   - Answer: [Fill in after reading]

4. Why does store create two subproblems?
   - Answer: [Fill in after reading]

5. [Add more questions as they arise]

---

## Implementation Checklist (Based on Reading)

After completing literature review, verify understanding of:
- [ ] Can explain CC algorithm step-by-step
- [ ] Can draw examples of DAG representations
- [ ] Can trace FIND/UNION operations on paper
- [ ] Understand why largest ccpar optimization works
- [ ] Can explain T_cons axiom integration
- [ ] Can explain T_A store decomposition
- [ ] Ready to design data structures
- [ ] Ready to write pseudocode

---

## Notes Section

### General Notes
```
[Free-form notes as you read]
```

### Examples from Papers
```
[Copy important examples here]
```

### Insights and Observations
```
[Record insights that might help implementation]
```

---

**Last Updated:** [Add date when you update this file]
