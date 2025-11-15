# Project Plan: Solver for the Union of Theories of Equality, Lists, and Arrays

## Timeline
- **Assigned:** November 13, 2025
- **Due:** January 31, 2026 (23:59)
- **Duration:** ~11 weeks

---

## Phase 1: Setup & Planning (Week 1-2)

### 1.1 Language Selection & Environment Setup
- [x] Choose programming language (Java, C++, C, Rust, OCaml, or Standard ML)
- [x] Set up development environment and build system
- [x] Create project structure (src/, tests/, docs/, experiments/)
- [x] Set up version control workflow

### 1.2 Literature Review
- [x] Read Section 9.3 of Bradley & Manna (T_E-procedure, CC algorithm)
- [x] Read Section 9.4 of Bradley & Manna (T_cons-procedure)
- [x] Read Section 9.5 of Bradley & Manna (T_A-procedure)
- [x] Study UNION optimization (page 761 in Downey et al., page 423 in Detlef et al.)
- [x] Review congruence closure algorithms from other references
- [x] Understand DAG representation and equivalence classes

### 1.3 Design Decisions
- [x] Design overall architecture (modular components for each theory)
- [x] Choose data structures for DAG representation
- [x] Choose data structures for equivalence classes and find/union operations
- [x] Design input format for literal sets
- [x] Design output format (SAT/UNSAT with optional witness/explanation)
- [x] Plan optional features (forbidden list/set, non-recursive FIND)

---

## Phase 2: Core Implementation (Week 3-7)

### 2.1 Basic Data Structures ✓ COMPLETE
- [x] Implement term/node representation for DAG
- [x] Implement equivalence class data structure
- [x] Implement ccpar sets tracking
- [x] Implement pending list/queue for merge propagation

### 2.2 Congruence Closure (CC) Algorithm - Core of T_E ✓ COMPLETE
- [x] Implement FIND function
- [x] Implement UNION function with largest ccpar optimization
- [x] Implement MERGE procedure
- [x] Implement CONGRUENT check
- [x] Implement main CC algorithm on DAGs
- [x] Test CC algorithm with simple equality examples

### 2.3 T_E-Procedure (Theory of Equality) ✓ COMPLETE
- [x] Parse equality literals (e.g., a = b, f(x) = g(y))
- [x] Parse disequality literals (e.g., a ≠ b)
- [x] Implement T_E satisfiability check using CC
- [x] Test with examples from Section 9.3

### 2.4 T_cons-Procedure (Theory of Lists) ✓ COMPLETE
- [x] Identify T_cons symbols (car, cdr, cons)
- [x] Implement T_cons axioms integration into CC
  - [x] Axiom: car(cons(x,y)) = x
  - [x] Axiom: cdr(cons(x,y)) = y
  - [x] Handle potential cycles
- [x] Test with list examples from Section 9.4

### 2.5 T_A-Procedure (Theory of Arrays) ✓ COMPLETE
- [x] Identify array symbols (select, store)
- [x] Implement store decomposition (creates two subproblems per store)
  - [x] Subproblem 1: i = j ∧ select(store(a,i,v),j) = v
  - [x] Subproblem 2: i ≠ j ∧ select(store(a,i,v),j) = select(a,j)
- [x] Implement select processing (read-over-write axioms)
- [x] Handle multiple store operations (recursive decomposition)
- [x] Test with array examples from Section 9.5

### 2.6 Main Solver Integration ✓ COMPLETE
- [x] Implement UnifiedSolver as main entry point
- [x] Implement automatic theory detection (T_A, T_cons, T_E)
- [x] Implement intelligent routing to appropriate procedures
- [x] Provide clean public API (checkSat, isSatisfiable)
- [x] Test with pure theories (T_E, T_cons, T_A)
- [x] Test with mixed theory examples
- [x] Test with all three theories combined

---

## Phase 3: Input/Output & Interface (Week 7-8)

### 3.1 Input Parser
- [ ] Design input language/format for literal sets
- [ ] Implement lexer for tokenization
- [ ] Implement parser for expressions
- [ ] Handle free predicate symbols transformation
- [ ] Handle free variables
- [ ] Support reading from files (stdin or file argument)
- [ ] Add error handling for malformed input

### 3.2 Output Formatter
- [ ] Implement SAT/UNSAT output
- [ ] Optional: Show equivalence classes for SAT cases
- [ ] Optional: Show conflict explanation for UNSAT cases
- [ ] Add timing/statistics output

### 3.3 Optional: SMT-LIB Parser
- [ ] Study SMT-LIB format (QF-UF benchmarks)
- [ ] Implement subset parser for QF-UF
- [ ] Test with SMT-LIB benchmarks from Zenodo

---

## Phase 4: Testing & Experimentation (Week 8-9)

### 4.1 Test Suite Development
- [ ] Collect examples from Bradley & Manna book
- [ ] Collect examples from Kroening & Strichman book
- [ ] Collect examples from referenced papers
- [ ] Create test cases for each theory (T_E, T_cons, T_A)
- [ ] Create test cases for combined theories
- [ ] Transform formulas from books into literal sets

### 4.2 Experiments
- [ ] Run solver on all test cases
- [ ] Record results: SAT/UNSAT, runtime, source
- [ ] Test with different problem sizes
- [ ] Evaluate impact of largest ccpar optimization
- [ ] Optional: Test forbidden list/set optimization
- [ ] Optional: Test non-recursive FIND optimization
- [ ] Optional: Test SMT-LIB QF-UF benchmarks

### 4.3 Optional: Test Generator
- [ ] Design random literal set generator
- [ ] Generate synthetic test cases
- [ ] Use for stress testing

---

## Phase 5: Optional Optimizations (Week 9-10)

### 5.1 Forbidden List/Set (Optional)
- [ ] Implement forbidden list/set for merge prevention
- [ ] Test impact on performance
- [ ] Make it configurable option

### 5.2 Non-recursive FIND (Optional)
- [ ] Implement iterative FIND function
- [ ] Update UNION to modify find fields
- [ ] Test impact on performance
- [ ] Make it configurable option

### 5.3 Performance Tuning
- [ ] Profile the solver
- [ ] Optimize hot paths
- [ ] Reduce memory allocations
- [ ] Consider path compression in FIND

---

## Phase 6: Report Writing (Week 10-11)

### 6.1 Implementation Section
- [ ] Describe overall architecture
- [ ] Explain data structures chosen (with rationale)
- [ ] Describe CC algorithm implementation details
- [ ] Explain theory-specific procedures
- [ ] Discuss optimization choices (largest ccpar, etc.)
- [ ] Comment on ease of implementation vs. performance trade-offs

### 6.2 Experiments Section
- [ ] Create tables/plots of experimental results
  - [ ] Problem source
  - [ ] SAT/UNSAT result
  - [ ] Runtime
  - [ ] Problem size metrics
- [ ] Include performance comparison with/without optimizations

### 6.3 Analysis Section
- [ ] Analyze performance characteristics
- [ ] Discuss impact of optimizations
- [ ] Identify bottlenecks
- [ ] Suggest future improvements
- [ ] Include interesting observations

### 6.4 Report Finalization
- [ ] Ensure max 6 pages, 11pt font
- [ ] Proofread (NO generative AI allowed!)
- [ ] Check all figures and tables
- [ ] Verify citations and references
- [ ] Print double-sided version

---

## Phase 7: Submission Preparation (Week 11)

### 7.1 Code Cleanup
- [ ] Remove debug code and commented-out sections
- [ ] Add code documentation/comments
- [ ] Ensure consistent code style
- [ ] Verify no hardcoded paths

### 7.2 Documentation
- [ ] Write comprehensive README with:
  - [ ] Compilation instructions
  - [ ] Execution instructions
  - [ ] Input format specification
  - [ ] Examples of usage
- [ ] Document each test file with its source

### 7.3 Archive Creation
- [ ] Organize directory structure:
  - [ ] src/ - source code
  - [ ] tests/ - input test files (with source comments)
  - [ ] output/ - corresponding output files
  - [ ] docs/ - README and any additional docs
  - [ ] bin/ or executable - compiled binary (if applicable)
- [ ] Create .tgz or .zip archive
- [ ] Name archive: FirstNameLastNameStudentId
- [ ] Test extraction and compilation from archive

### 7.4 Final Submission
- [ ] Email archive to instructor
- [ ] Print report double-sided
- [ ] Place report in instructor's mailbox
- [ ] Verify submission before deadline (Jan 31, 23:59)

---

## Key Requirements Checklist

### Must Have
- [x] Language: Java, C++, C, Rust, OCaml, or Standard ML
- [x] Implements CC algorithm with largest ccpar optimization
- [x] Implements T_E-procedure
- [x] Implements T_cons-procedure
- [x] Implements T_A-procedure (store decomposition + select processing)
- [x] Handles mixed theories correctly
- [ ] stdin/stdout or simple GUI interface
- [ ] Comprehensive test suite from books/papers
- [ ] Report (max 6 pages, 11pt, NO AI generated)
- [ ] Archive with source, tests, outputs, README, executable
- [ ] Double-sided printed report

### Optional Features
- [ ] Forbidden list/set optimization
- [ ] Non-recursive FIND function
- [ ] Synthetic test generator
- [ ] SMT-LIB QF-UF parser and benchmarks
- [ ] Performance profiling and tuning

---

## Important Notes

1. **Do NOT use Python** - must implement from scratch
2. **Do NOT translate existing code** - original implementation required
3. **Do NOT use AI for report** - original writing required
4. **Archive format:** .tgz or .zip (NO .rar)
5. **Deadline:** January 31, 2026, 23:59 (strict)
6. **Report:** Must be printed double-sided and placed in mailbox

---

## References to Study

1. Bradley & Manna - Sections 9.3, 9.4, 9.5 (primary reference)
2. Kroening & Strichman - Decision Procedures
3. Downey et al. - Page 761 (ccpar optimization)
4. Detlef et al. - Page 423 (ccpar), Page 388 (forbidden list)
5. Nelson & Oppen - Congruence closure foundations
6. Other papers in references for additional examples

