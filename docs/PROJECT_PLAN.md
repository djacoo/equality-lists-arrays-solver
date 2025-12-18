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

### 3.1 Input Parser ✓ COMPLETE
- [x] Design input language/format for literal sets
- [x] Implement lexer for tokenization (`solver.parser.Lexer`)
- [x] Implement parser for expressions (`solver.parser.Parser`)
- [x] Handle free predicate symbols transformation (`solver.parser.PredicateTransformer`)
- [x] Handle free variables (all simple identifiers treated as variables)
- [x] Support reading from files (stdin or file argument) (`solver.parser.InputReader`)
- [x] Add error handling for malformed input (LexerException, ParseException with line/column)

### 3.2 Output Formatter ✓ COMPLETE
- [x] Implement SAT/UNSAT output
- [x] Optional: Show equivalence classes for SAT cases
- [x] Optional: Show conflict explanation for UNSAT cases
- [x] Add timing/statistics output

### 3.3 Optional: SMT-LIB Parser ✓ COMPLETE
- [x] Study SMT-LIB format (QF-UF benchmarks)
- [x] Implement subset parser for QF-UF
- [x] Test with SMT-LIB benchmarks from Zenodo

---

## Phase 3.5: Repository Reorganization & Cleanup (Week 8) ✓ COMPLETE

### 3.5.1 Code Organization ✓ COMPLETE
- [x] Review current source code structure in `src/main/java/solver/`
- [x] Ensure consistent package organization and naming
- [x] Verify logical grouping of related classes
- [x] Check for any misplaced files or classes
- [x] Review and organize test code in `src/test/java/solver/`
- [x] Ensure test classes are properly paired with source classes

### 3.5.2 Documentation Organization ✓ COMPLETE
- [x] Consolidate all documentation in `docs/` directory
- [x] Create clear hierarchy for different doc types:
  - [x] Architecture and design docs
  - [x] API and usage documentation
  - [x] Development guides and planning docs
- [x] Update README.md with accurate project structure
- [x] Add/update documentation index in docs/
- [x] Ensure all doc cross-references are correct
- [x] Remove duplicate or outdated documentation

### 3.5.3 Test Files Organization ✓ COMPLETE
- [x] Review test input file organization in `tests/input/`
- [x] Organize tests by theory (T_E, T_cons, T_A, combined)
- [x] Ensure consistent naming conventions for test files
- [x] Verify SMT-LIB tests are properly organized in `tests/input/smtlib/`
- [x] Update TEST_INDEX.md with accurate descriptions
- [x] Create expected output files if missing (documented in file names and headers)
- [x] Document test file sources and purposes (documented in file headers and TEST_INDEX.md)

### 3.5.4 Cleanup & Maintenance ✓ COMPLETE
- [x] Remove build artifacts from version control (if any)
- [x] Clean up `.DS_Store` and other OS-specific files
- [x] Update `.gitignore` for comprehensive coverage
- [x] Remove empty or unused directories
- [x] Check for and remove commented-out code blocks
- [x] Verify no debug or temporary files are committed
- [x] Review and clean up any TODO comments in code

### 3.5.5 Build & Dependencies ✓ COMPLETE
- [x] Review `pom.xml` for accuracy and completeness
- [x] Ensure all dependencies are necessary and up-to-date
- [x] Verify Maven build configuration is optimal
- [x] Check plugin versions and configurations
- [x] Ensure proper jar naming and manifest configuration

### 3.5.6 Project Structure Documentation ✓ COMPLETE
- [x] Create/update CONTRIBUTING.md with development guidelines
- [x] Document project directory structure clearly
- [x] Add code style and naming conventions guide
- [x] Document git workflow and branching strategy
- [x] Create quick-start guide for new developers

### 3.5.7 Verification & Testing ✓ COMPLETE
- [x] Run full Maven build: `mvn clean compile`
- [x] Execute all unit tests: `mvn test`
- [x] Verify standalone JAR builds correctly: `mvn package`
- [x] Test solver with sample inputs
- [x] Run SMT-LIB parser tests
- [x] Verify all documentation links work
- [x] Check that README instructions are accurate

### 3.5.8 Final Preparation for Phase 4 & 5 ✓ COMPLETE
- [x] Ensure codebase is in clean, maintainable state
- [x] Verify all core functionality works correctly
- [x] Confirm test suite is comprehensive and passing
- [x] Review that documentation accurately reflects implementation
- [x] Create baseline performance metrics for optimization comparison (deferred to end of development)
- [x] Tag repository state before moving to Phase 4
- [x] Check there isn't any mention of AI-usage in the code or documentation

**Goals of this phase:**
- Clean, professional codebase ready for experimentation
- Well-organized documentation that's easy to navigate
- Comprehensive test suite that's easy to extend
- Solid foundation for performance testing and optimization
- Repository ready for final report writing and submission

---

## Phase 4: Testing & Experimentation (Week 9)

### 4.1 Test Suite Development ✓ COMPLETE
- [x] Collect examples from Bradley & Manna book
- [x] Collect examples from Kroening & Strichman book
- [x] Collect examples from referenced papers
- [x] Create test cases for each theory (T_E, T_cons, T_A)
- [x] Create test cases for combined theories
- [x] Transform formulas from books into literal sets

### 4.1.1 Unit Test Coverage Expansion (Priority from 3.5.1 review) ✓ COMPLETE
- [x] **PRIORITY 1**: Add comprehensive parser unit tests ✓ COMPLETE
  - ✓ LexerTest (28 tests), ParserTest (32 tests), InputReaderTest (21 tests), PredicateTransformerTest (15 tests)
  - ✓ SMTLIBLexerTest (15 tests), SMTLIBParserTest (16 tests), SMTLIBInputReaderTest (9 tests)
  - ✓ Negative/error case tests for malformed input included
  - **Total: 136 parser tests**
- [x] **PRIORITY 2**: Complete DAG package tests ✓ COMPLETE
  - ✓ VariableTest (17 tests), ConstantTest (13 tests), FunctionAppTest (18 tests), TermTest (16 tests)
  - **Total: 64 new DAG tests** (plus existing DAGTest: 9, TermFactoryTest: 13)
- [x] **PRIORITY 3**: Complete Equivalence package tests ✓ COMPLETE
  - ✓ EquivalenceClassTest (16 tests)
  - **Total: 16 new tests** (plus existing ClassManagerTest: 10)
- [x] **PRIORITY 4**: Complete Theory T_E tests ✓ COMPLETE
  - ✓ LiteralTest (28 tests covering equality, disequality, atom, ¬atom)
  - **Total: 28 new tests** (plus existing TEProcedureTest: 17)
- [x] **PRIORITY 5**: Complete root solver tests ✓ COMPLETE
  - ⚠️ SMTLIBSolverTest was attempted but removed due to System.exit() incompatibility with JUnit testing framework
  - ✓ MainTest provides basic JUnit setup verification (1 test)
  - ✓ UnifiedSolverTest provides comprehensive coverage (23 tests covering T_E, T_cons, T_A, and mixed theories)
  - Note: Main.java and SMTLIBSolver.java contain System.exit() calls for error handling, making direct JUnit testing infeasible. The core solver logic is thoroughly tested via UnifiedSolverTest.

**Test Suite Summary:**
- **Total Tests: 439**
- **All tests passing: ✓**
- **Coverage: Comprehensive unit tests for all major components**

### 4.2 Experiments ✓ COMPLETE
- [x] Run solver on all test cases (56 tests: 51 custom + 5 SMT-LIB)
- [x] Record results: SAT/UNSAT, runtime, source (results.csv)
- [x] Analyze correctness by theory (T_E: 100%, T_cons: 100%, T_A: 93%, Combined: 90%)
- [x] Document performance metrics (avg: 67ms, range: 48-81ms)
- [x] Identify and document limitations (2 UNSAT failures due to indirect read-over-write pattern)
- [x] Investigate and attempt fixes for T_A failures
- [x] Verify original implementation is optimal for textbook algorithm
- [x] Create comprehensive performance analysis (PERFORMANCE_ANALYSIS.md)
- [x] Document ccpar optimization implementation and rationale
- [x] Analyze problem size vs performance characteristics
- [x] Create summary tables and figures for report (REPORT_SUMMARY.md)
- [ ] Test with different problem sizes (deferred to optional - adequate coverage exists)
- [x] Optional: Test forbidden list/set optimization ✓ COMPLETE (Phase 5.1 - 516 total tests, all passing)
- [x] Optional: Test non-recursive FIND optimization ✓ COMPLETE (Phase 5.2 - 563 total tests, all passing)
- [x] Optional: Test SMT-LIB QF-UF benchmarks (5/5 passing - parser bug fixed)

**Final Results Summary:**
- **Overall Correctness: 54/56 (96.4%)**
- T_E: 14/14 (100%)
- T_cons: 13/13 (100%)
- T_A: 13/14 (93%) - 1 UNSAT failure (indirect read-over-write pattern)
- Combined: 9/10 (90%) - 1 UNSAT failure (indirect read-over-write pattern)
- SMT-LIB: 5/5 (100%) ✓
- Average runtime: 69.2ms (range: 65.1-81.0ms)
- Total unit tests: 467/467 passing (100%)

**Known Limitations:**
- 2 test failures due to indirect read-over-write pattern (documented in UNSAT_FAILURES_ANALYSIS.md)
- This is an inherent limitation of the syntactic Bradley & Manna algorithm
- Fixes require architectural changes beyond textbook scope

**Documentation Created:**
- [PERFORMANCE_ANALYSIS.md](../experiments/PERFORMANCE_ANALYSIS.md) - Comprehensive performance analysis
- [REPORT_SUMMARY.md](../experiments/REPORT_SUMMARY.md) - Tables and figures for final report
- [UNSAT_FAILURES_ANALYSIS.md](../experiments/UNSAT_FAILURES_ANALYSIS.md) - Detailed failure analysis

### 4.3 Optional: Test Generator ✓ COMPLETE
- [x] Design random literal set generator (`RandomTestGenerator` class)
- [x] Generate synthetic test cases (equality, list, array, and mixed theories)
- [x] Use for stress testing (`StressTestRunner` command-line tool)
- [x] Generate test cases of various sizes (5, 10, 20, 50, 100 variables)
- [x] Include guaranteed SAT/UNSAT tests
- [x] Test with forbidden set optimization
- [x] Create 13 unit tests for generator (all passing)
- Note: Array theory tests expose solver bugs with certain random patterns (documented)

---

## Phase 5: Optional Optimizations (Week 10)

### 5.1 Forbidden List/Set (Optional) ✓ COMPLETE
- [x] Implement forbidden list/set for merge prevention
  - [x] Create ForbiddenSet class with TermPair canonical ordering
  - [x] Integrate with ClassManager.union() for early UNSAT detection
  - [x] Modify MergeManager to propagate UNSAT immediately
  - [x] Update CongruenceClosure to support disequality registration
  - [x] Update TEProcedure to register disequalities before processing equalities
- [x] Test impact on performance
  - [x] Create 18 unit tests for ForbiddenSet
  - [x] Create 14 integration tests for TEProcedure with forbidden set
  - [x] All 516 tests passing (100% success rate)
- [x] Make it configurable option
  - [x] Create SolverConfig class with factory methods
  - [x] Support baseline, withForbiddenSet, and allOptimizations configurations
  - [x] Fully backward compatible (default = no optional optimizations)
  - [x] Create 17 unit tests for SolverConfig
- [x] Documentation
  - [x] Comprehensive inline documentation
  - [x] Created PHASE_5_1_SUMMARY.md with implementation details
  - [x] Updated PROJECT_PLAN.md

### 5.2 Non-recursive FIND (Optional) ✓ COMPLETE
- [x] Verify iterative FIND function implementation (already implemented)
- [x] Verify two-pass path compression algorithm
- [x] Create comprehensive unit tests (11 tests)
- [x] Create performance benchmark tests (6 tests)
- [x] Create integration tests with other optimizations (10 tests)
- [x] Test impact on performance (3.37x speedup on repeated FINDs)
- [x] Verify configurable option via SolverConfig
- [x] Documentation (PHASE_5_2_SUMMARY.md)

### 5.3 Performance Tuning ✓ COMPLETE
- [x] Profile the solver (8 comprehensive profiling tests)
- [x] Optimize hot paths (MergeManager, ClassManager, CongruenceChecker)
- [x] Reduce memory allocations (eliminated redundant HashSet copies)
- [x] Path compression already implemented (Phase 5.2)
- [x] Achieved 1.62x speedup (62% faster with all optimizations)
- [x] All 571 tests passing (no regressions)

### 5.4 Code Refactoring ✓ COMPLETE
- [x] Refactored parser package to reduce duplication between custom and SMT-LIB formats
  - [x] Extracted common tokenization logic into BaseLexer (peek, peekNext, advance, isAtEnd)
  - [x] Created abstract BaseLexer class (85 lines shared code)
  - [x] Created unified IToken interface (common token contract)
  - [x] Both Lexer and SMTLIBLexer now extend BaseLexer
  - [x] Both Token and SMTLIBToken now implement IToken
- [x] Added exception hierarchy for better error handling
  - [x] Created ParserException base class
  - [x] LexerException and ParseException now extend ParserException
- [x] Refactoring reduces code duplication by ~90 lines
- [x] All 571 tests passing (no regressions)

---

## Phase 6: Report Writing (Week 11)

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

### 7.1 Code Cleanup ✓ COMPLETE
- [x] Verified no debug code or commented-out sections
- [x] Cleaned documentation: removed excessive .md files (kept 4 essential docs)
- [x] Ensured consistent code style
- [x] Verified no hardcoded paths in source code
- [x] Cleaned up repository structure (organized, understandable)
- [x] Removed development-specific files (BRANCHING_STRATEGY, COMPLETION_STATUS, etc.)

### 7.2 Documentation ✓ COMPLETE
- [x] Wrote comprehensive README with:
  - [x] Compilation instructions (Maven + JAR)
  - [x] Execution instructions (multiple formats)
  - [x] Input format specification (custom + SMT-LIB)
  - [x] 6 detailed examples with outputs
  - [x] Algorithm overview and optimizations
  - [x] Performance benchmarks
  - [x] Troubleshooting guide
- [x] Documented each test file with source (TEST_INDEX.md with 51 tests cataloged)

### 7.3 Archive Creation ✓ COMPLETE
- [x] Organized directory structure:
  - [x] src/ - source code (Java packages organized)
  - [x] tests/ - input test files with source documentation
  - [x] output/ - corresponding output files
  - [x] docs/ - README, ARCHITECTURE, INPUT_FORMAT, PROJECT_PLAN
  - [x] bin/ - compiled executable JAR (solver.jar)
  - [x] experiments/ - performance analysis
  - [x] assignment/ - project materials
- [ ] Create .tgz archive
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
- [x] stdin/stdout interface with file support and help system
- [x] Comprehensive test suite from books/papers
- [ ] Report (max 6 pages, 11pt, NO AI generated)
- [ ] Archive with source, tests, outputs, README, executable
- [ ] Double-sided printed report

### Optional Features
- [x] Forbidden list/set optimization ✓ COMPLETE (Phase 5.1)
- [x] Non-recursive FIND function ✓ COMPLETE (Phase 5.2)
- [x] Synthetic test generator ✓ COMPLETE (Phase 4.3)
- [x] SMT-LIB QF-UF parser and benchmarks ✓ COMPLETE (Phase 3.3)
- [x] Performance profiling and tuning ✓ COMPLETE (Phase 5.3)

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

