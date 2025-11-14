# References Summary

This document provides quick access to all references from the assignment.

---

## Primary Reference (ESSENTIAL)

### [1] Bradley & Manna (2007)
**Full Citation:**
> Aaron R. Bradley, Zohar Manna. *The Calculus of Computation. Decision Procedures with Applications to Verification*. Springer, 2007, ISBN 978-3-642-09347-0.

**Critical Sections:**
- **Section 9.3:** T_E-procedure (Congruence Closure algorithm)
- **Section 9.4:** T_cons-procedure (Theory of Lists)
- **Section 9.5:** T_A-procedure (Theory of Arrays)

**Note:** This is the PRIMARY reference - these three sections form the foundation of the entire implementation.

---

## Optimization References (MANDATORY)

### [8] Downey, Sethi & Tarjan (1980)
**Full Citation:**
> Peter J. Downey, Ravi Sethi, and Robert Endre Tarjan. Variations on the common subexpression problem. *Journal of the ACM* 27(4):758–771, 1980.

**Critical Pages:**
- **Page 761 (top):** UNION optimization - largest ccpar set

---

### [7] Detlef, Nelson & Saxe (2005)
**Full Citation:**
> David L. Detlef, Greg Nelson and James B. Saxe. Simplify: a theorem prover for program checking. *Journal of the ACM* 52(3):365–473, 2005.

**Critical Pages:**
- **Page 423 (top):** UNION optimization - largest ccpar set
- **Page 388 (bottom):** Forbidden list/set (OPTIONAL)

---

## Supporting References

### [2] Kroening & Strichman (2008)
**Full Citation:**
> Daniel Kroening, Ofer Strichman: *Decision Procedures. An Algorithmic Point of View*, Springer, 2008, ISBN: 978-3-540-74104-6.

**Purpose:** Alternative explanations and broader context for decision procedures.

---

### [10] Nelson & Oppen (1980) - FOUNDATIONAL
**Full Citation:**
> Greg Nelson and Derek C. Oppen. Fast decision procedures based on congruence closure. *Journal of the ACM* 27(2):356–364, 1980.

**Purpose:** Original congruence closure algorithm - foundational understanding.

---

## Additional References (Optional but Useful)

### [3] Armando, Ranise & Rusinowitch (2003)
**Full Citation:**
> Alessandro Armando, Silvio Ranise, and Michaël Rusinowitch. A rewriting approach to satisfiability procedures. *Information and Computation* 183(2):140–164, 2003.

---

### [4] Armando et al. (2009)
**Full Citation:**
> Alessandro Armando, Maria Paola Bonacina, Silvio Ranise, and Stephan Schulz. New results on rewrite-based satisfiability procedures. *ACM Transactions on Computational Logic* 10(1):129–179, 2009.

---

### [5] Bachmair, Tiwari & Vigneron (2003)
**Full Citation:**
> Leo Bachmair, Ashish Tiwari and Laurent Vigneron. Abstract congruence closure. *Journal of Automated Reasoning* 31(2):129–168, 2003.

---

### [6] Bradley, Manna & Sipma (2006)
**Full Citation:**
> Aaron R. Bradley, Zohar Manna, and Henny B. Sipma. What's decidable about arrays? In E. Allen Emerson and Kedar S. Namjoshi (Eds.), *Proceedings of the Seventh International Conference on Verification, Model Checking and Abstract Interpretation (VMCAI)*, Lecture Notes in Computer Science 3055:427–442, Springer, 2006.

**Purpose:** Additional insights on array theory.

---

### [9] Kozen (1976)
**Full Citation:**
> Dexter Kozen. Complexity of finitely presented algebras. *Technical Report TR-76-294*, Department of Computer Science, Cornell University, 1976.

---

### [11] Nieuwenhuis & Oliveras (2007)
**Full Citation:**
> Robert Nieuwenhuis and Albert Oliveras. Fast congruence closure and extensions. *Information and Computation* 205:557–580, 2007.

**Purpose:** Modern optimizations and extensions to CC.

---

### [12] Shostak (1978)
**Full Citation:**
> Robert E. Shostak. An algorithm for reasoning about equality. *Communications of the ACM* 21(7):583–585, 1978.

---

### [13] Stump et al. (2001)
**Full Citation:**
> Aaron Stump, Clark W. Barrett, David L. Dill, and Jeremy Levitt. A decision procedure for an extensional theory of arrays. In Joseph Halpern (Ed.), *Proceedings of the 16th IEEE Symposium on Logic in Computer Science*, IEEE Computer Society Press, 2001.

**Purpose:** Arrays with extensionality (our project uses arrays WITHOUT extensionality).

---

## Reading Priority

### Phase 1: Core Understanding (CRITICAL)
1. Bradley & Manna Section 9.3 (T_E and CC algorithm)
2. Bradley & Manna Section 9.4 (T_cons)
3. Bradley & Manna Section 9.5 (T_A)

### Phase 2: Mandatory Optimization
4. Downey et al. page 761 (largest ccpar)
5. Detlef et al. page 423 (largest ccpar)

### Phase 3: Foundations and Context
6. Nelson & Oppen (1980) - original CC algorithm
7. Kroening & Strichman - alternative explanations

### Phase 4: Optional Deep Dive
8. Detlef et al. page 388 (forbidden list/set)
9. Nieuwenhuis & Oliveras (modern optimizations)
10. Other references as needed

---

## Where to Find These Papers

### Academic Databases
- **ACM Digital Library:** https://dl.acm.org/
- **SpringerLink:** https://link.springer.com/
- **IEEE Xplore:** https://ieeexplore.ieee.org/

### University Access
- Check if your university library has subscriptions
- VPN access to university resources

### Other Options
- Google Scholar: May have free PDFs from author websites
- ResearchGate: Authors sometimes share papers
- arXiv: Some papers may have preprints

---

## Key Terminology to Learn

From the assignment and references:

- **CC (Congruence Closure):** Core algorithm for T_E
- **DAG (Directed Acyclic Graph):** Representation of terms
- **ccpar (Congruence Closure Parents):** Set tracked for each term
- **FIND:** Function to get equivalence class representative
- **UNION:** Function to merge equivalence classes
- **MERGE:** Procedure to propagate merges
- **CONGRUENT:** Check if terms are congruent
- **Pending list/queue:** Tracks merges to propagate
- **Forbidden list/set:** Optional optimization to prevent certain merges
- **Read-over-write axioms:** For array select operations
- **Store decomposition:** Strategy for handling array store
- **T_E:** Theory of Equality with uninterpreted functions
- **T_cons:** Theory of Lists (cons, car, cdr)
- **T_A:** Theory of Arrays (select, store, without extensionality)

---

## Quick Lookup: What to Read for Each Component

| Component to Implement | Primary Reference | Page/Section |
|------------------------|-------------------|--------------|
| CC Algorithm | Bradley & Manna [1] | Section 9.3 |
| FIND function | Bradley & Manna [1] | Section 9.3 |
| UNION function | Bradley & Manna [1] | Section 9.3 |
| MERGE procedure | Bradley & Manna [1] | Section 9.3 |
| DAG representation | Bradley & Manna [1] | Section 9.3 |
| Largest ccpar optimization | Downey et al. [8] | Page 761 |
| Largest ccpar optimization | Detlef et al. [7] | Page 423 |
| T_cons integration | Bradley & Manna [1] | Section 9.4 |
| List axioms | Bradley & Manna [1] | Section 9.4 |
| Store decomposition | Bradley & Manna [1] | Section 9.5 |
| Select processing | Bradley & Manna [1] | Section 9.5 |
| Forbidden list (optional) | Detlef et al. [7] | Page 388 |
| Non-recursive FIND (optional) | Detlef et al. [7] | TBD |

---

**Note:** Keep this document updated as you progress through the readings and discover which sections are most useful.
