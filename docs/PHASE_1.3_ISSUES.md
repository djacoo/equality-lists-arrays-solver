# Phase 1.3 GitHub Issues - Ready to Create

Copy and paste these into GitHub issues at: https://github.com/djacoo/equality-lists-arrays-solver/issues/new

---

## Issue #1: Design Overall Architecture

**Title:** `Design overall architecture for modular solver`

**Labels:** `enhancement`, `phase-1`, `documentation`

**Description:**
```markdown
## Description
Design the overall architecture with modular components for each theory (T_E, T_cons, T_A) to enable clean separation of concerns and easy testing.

## Related PROJECT_PLAN Section
Phase 1, Task 1.3 - Design Decisions

## Acceptance Criteria
- [ ] Define package structure for solver modules
- [ ] Design interfaces for T_E-procedure
- [ ] Design interfaces for T_cons-procedure
- [ ] Design interfaces for T_A-procedure
- [ ] Design main solver orchestration logic
- [ ] Document component responsibilities and interactions
- [ ] Create UML or architecture diagrams

## Implementation Notes
From literature review:
- T_cons reduces to T_E (integrate axioms into CC)
- T_A reduces to T_E (store decomposition + select processing)
- Main solver must handle theory detection and subproblem generation

## Definition of Done
- [ ] Architecture document created in docs/
- [ ] Package structure defined
- [ ] Interfaces documented
- [ ] Code reviewed
- [ ] Merged to develop
```

---

## Issue #2: Choose Data Structures for DAG Representation

**Title:** `Design data structures for DAG representation of terms`

**Labels:** `enhancement`, `phase-1`, `T_E`, `design`

**Description:**
```markdown
## Description
Design data structures for representing terms as a Directed Acyclic Graph (DAG) as required by the congruence closure algorithm.

## Related PROJECT_PLAN Section
Phase 1, Task 1.3 - Design Decisions

## Acceptance Criteria
- [ ] Design Node/Term class structure
- [ ] Define how to represent function symbols
- [ ] Define how to represent variables/constants
- [ ] Define how to track parent-child relationships
- [ ] Design ccpar set storage (congruence closure parents)
- [ ] Document memory and performance considerations
- [ ] Create example DAG representations

## Implementation Notes
From Bradley & Manna Section 9.3:
- Each term is a node in the DAG
- Function applications have children
- Need efficient lookup of congruent terms
- ccpar set critical for UNION optimization

## Definition of Done
- [ ] Data structure design documented
- [ ] Java class interfaces defined
- [ ] Examples provided
- [ ] Memory/performance analysis done
- [ ] Code reviewed
- [ ] Merged to develop
```

---

## Issue #3: Choose Data Structures for Equivalence Classes

**Title:** `Design data structures for equivalence classes and FIND/UNION operations`

**Labels:** `enhancement`, `phase-1`, `T_E`, `design`

**Description:**
```markdown
## Description
Design data structures to efficiently represent equivalence classes and support FIND/UNION operations with the largest ccpar optimization.

## Related PROJECT_PLAN Section
Phase 1, Task 1.3 - Design Decisions

## Acceptance Criteria
- [ ] Design equivalence class representation
- [ ] Define how to store class representatives (find field)
- [ ] Define how to track class members
- [ ] Design ccpar set tracking for UNION optimization
- [ ] Design pending list/queue for MERGE propagation
- [ ] Document time complexity of operations
- [ ] Create examples of FIND/UNION operations

## Implementation Notes
From Downey et al. page 761 and Detlef et al. page 423:
- UNION must choose representative with largest ccpar set (MANDATORY)
- This optimization improves performance significantly

From Bradley & Manna Section 9.3:
- FIND returns equivalence class representative
- UNION merges two classes
- MERGE propagates congruences using pending list

## Definition of Done
- [ ] Data structure design documented
- [ ] Java class interfaces defined
- [ ] Complexity analysis completed
- [ ] Examples provided
- [ ] Code reviewed
- [ ] Merged to develop
```

---

## Issue #4: Design Input Format for Literal Sets

**Title:** `Design input format and parser specification for literal sets`

**Labels:** `enhancement`, `phase-1`, `documentation`

**Description:**
```markdown
## Description
Design a simple, human-readable input format for literal sets that supports T_E, T_cons, and T_A theories.

## Related PROJECT_PLAN Section
Phase 1, Task 1.3 - Design Decisions

## Acceptance Criteria
- [ ] Define syntax for equality literals (e.g., `a = b`)
- [ ] Define syntax for disequality literals (e.g., `a != b`)
- [ ] Define syntax for function applications (e.g., `f(x, y)`)
- [ ] Define syntax for list operations (cons, car, cdr)
- [ ] Define syntax for array operations (select, store)
- [ ] Define comment syntax
- [ ] Create grammar/EBNF if needed
- [ ] Provide example input files

## Implementation Notes
Requirements from assignment:
- stdin/stdout or simple GUI interface
- Should handle mixed theories
- Must support free (uninterpreted) function symbols

Example ideas:
```
# Equality
a = b
f(a) = g(b, c)

# Disequality
x != y

# Lists
car(cons(a, b)) = a

# Arrays
select(store(arr, i, v), i) = v
```

## Definition of Done
- [ ] Input format specification documented
- [ ] Grammar defined
- [ ] Example input files created
- [ ] Code reviewed
- [ ] Merged to develop
```

---

## Issue #5: Design Output Format

**Title:** `Design output format for SAT/UNSAT with optional diagnostics`

**Labels:** `enhancement`, `phase-1`, `documentation`

**Description:**
```markdown
## Description
Design the output format for the solver, including SAT/UNSAT answers and optional witness/explanation information.

## Related PROJECT_PLAN Section
Phase 1, Task 1.3 - Design Decisions

## Acceptance Criteria
- [ ] Define basic SAT/UNSAT output format
- [ ] Design optional witness format for SAT cases (equivalence classes)
- [ ] Design optional explanation format for UNSAT cases (conflict)
- [ ] Define timing/statistics output format
- [ ] Provide output examples
- [ ] Make verbose output configurable

## Implementation Notes
Minimum requirement:
- Output: `SAT` or `UNSAT`

Optional enhancements:
- For SAT: Show equivalence classes
- For UNSAT: Show conflicting literals
- Performance: Runtime, number of merges, etc.

Example outputs:
```
# Simple
UNSAT

# Verbose SAT
SAT
Equivalence Classes:
  {a, b, f(c)}
  {x, y}
Time: 0.003s

# Verbose UNSAT
UNSAT
Conflict: a = b, b = c, c != a (transitivity violation)
Time: 0.001s
```

## Definition of Done
- [ ] Output format specification documented
- [ ] Examples provided
- [ ] Configurability designed
- [ ] Code reviewed
- [ ] Merged to develop
```

---

## Issue #6: Plan Optional Features

**Title:** `Plan optional optimization features (forbidden list, non-recursive FIND)`

**Labels:** `enhancement`, `phase-1`, `optimization`, `documentation`

**Description:**
```markdown
## Description
Plan the optional optimization features: forbidden list/set and non-recursive FIND function. These will be implemented as configurable options to evaluate their impact.

## Related PROJECT_PLAN Section
Phase 1, Task 1.3 - Design Decisions

## Acceptance Criteria
- [ ] Research forbidden list/set from Detlef et al. page 388
- [ ] Document how forbidden list prevents certain merges
- [ ] Design non-recursive FIND with path compression
- [ ] Design UNION variant that updates find fields
- [ ] Plan how to make these features configurable
- [ ] Define evaluation metrics for comparing with/without
- [ ] Document expected performance impact

## Implementation Notes
From assignment:
- These features are OPTIONAL
- Should be configurable options
- Impact should be evaluated in experiments

From Detlef et al.:
- Forbidden list/set is heuristic, may or may not help
- Non-recursive FIND couples with modified UNION

## Definition of Done
- [ ] Research complete
- [ ] Design documented
- [ ] Configuration strategy defined
- [ ] Evaluation plan created
- [ ] Code reviewed
- [ ] Merged to develop
```

---

## Quick Issue Creation Guide

### Using GitHub CLI (if installed):
```bash
# Install gh CLI if needed
brew install gh

# Authenticate
gh auth login

# Create issue from command line
gh issue create --title "Design overall architecture for modular solver" \
  --body-file issue_body.txt \
  --label "enhancement,phase-1,documentation"
```

### Using GitHub Web Interface:
1. Go to: https://github.com/djacoo/equality-lists-arrays-solver/issues/new
2. Select "Feature/Task" template
3. Copy-paste title and description from above
4. Add labels manually
5. Add to "Phase 1: Setup & Planning" milestone
6. Click "Submit new issue"

---

## After Creating Issues

### Create Feature Branch for First Issue
```bash
# Make sure you're on develop
git checkout develop
git pull origin develop

# Create feature branch for issue #1
git checkout -b feature/1-architecture-design

# Push to remote
git push -u origin feature/1-architecture-design

# Start working on the design!
```

---

**Tip:** Create all 6 issues at once, then tackle them one by one in order. This gives you a clear sprint backlog for Phase 1.3!
