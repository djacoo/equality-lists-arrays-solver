# Agile Development Workflow Guide

This document describes the agile development workflow for the equality-lists-arrays-solver project.

---

## Git Branching Strategy

We'll use **Git Flow**, a popular branching model for agile development:

```
main (production-ready)
  |
  └── develop (integration branch)
        |
        ├── feature/task-1.3-architecture
        ├── feature/task-2.1-data-structures
        ├── feature/task-2.2-congruence-closure
        └── ... (more feature branches)
```

### Branch Descriptions

| Branch | Purpose | Lifetime |
|--------|---------|----------|
| `main` | Production-ready code, tagged releases | Permanent |
| `develop` | Integration branch, latest development | Permanent |
| `feature/*` | Individual features/tasks | Temporary |
| `hotfix/*` | Emergency fixes for main | Temporary |

---

## Initial Setup

### Step 1: Create develop branch

```bash
# Ensure you're on main and it's up to date
git checkout main
git pull origin main

# Create develop branch from main
git checkout -b develop

# Push develop to remote
git push -u origin develop
```

### Step 2: Set main as default branch on GitHub (if not already)

1. Go to: https://github.com/djacoo/equality-lists-arrays-solver/settings
2. Under "Default branch", ensure `main` is selected
3. (We'll work on `develop` but keep `main` as default for stability)

### Step 3: Protect main and develop branches

On GitHub:
1. Go to Settings → Branches → Add rule
2. For `main`:
   - Branch name pattern: `main`
   - ✅ Require pull request before merging
   - ✅ Require approvals: 1 (you can approve your own for solo project)
3. For `develop`:
   - Branch name pattern: `develop`
   - ✅ Require pull request before merging

---

## Development Workflow

### Step 1: Create a GitHub Issue

For each task in the PROJECT_PLAN.md, create an issue:

**Example:**
```
Title: Design overall architecture for solver
Labels: enhancement, phase-1, design
Milestone: Phase 1: Setup & Planning

Description:
Design the overall architecture with modular components for each theory:
- [ ] T_E-procedure module
- [ ] T_cons-procedure module
- [ ] T_A-procedure module
- [ ] Main solver orchestration
- [ ] Define interfaces between components

References: PROJECT_PLAN.md task 1.3
```

### Step 2: Create a Feature Branch

**Naming convention:** `feature/<issue-number>-<short-description>`

```bash
# Make sure you're on develop and it's up to date
git checkout develop
git pull origin develop

# Create feature branch (example for issue #1)
git checkout -b feature/1-architecture-design

# Push to remote and set upstream
git push -u origin feature/1-architecture-design
```

### Step 3: Work on the Feature

```bash
# Make your changes
# Edit files, create new files, etc.

# Stage changes
git add <files>

# Commit with descriptive message
git commit -m "feat: design modular architecture for solver

- Define interfaces for T_E, T_cons, T_A procedures
- Create package structure for modules
- Document component responsibilities

Closes #1"

# Push to remote
git push
```

### Step 4: Create a Pull Request

**On GitHub:**
1. Go to: https://github.com/djacoo/equality-lists-arrays-solver/pulls
2. Click "New pull request"
3. Base: `develop` ← Compare: `feature/1-architecture-design`
4. Title: `feat: Design modular architecture (#1)`
5. Description:
   ```
   ## Changes
   - Designed modular architecture
   - Created package structure
   - Documented component interfaces

   ## Checklist
   - [x] Code compiles without errors
   - [x] Documentation updated
   - [ ] Tests added (N/A for design phase)

   Closes #1
   ```
6. Create pull request

### Step 5: Review and Merge

**Self-review checklist:**
- [ ] Code compiles (`mvn compile`)
- [ ] Tests pass (`mvn test`)
- [ ] Code follows project conventions
- [ ] Documentation updated
- [ ] Commit messages are clear

**Merge:**
1. Click "Merge pull request" on GitHub
2. Choose "Squash and merge" (for clean history) OR "Create a merge commit"
3. Delete the feature branch after merging

**After merge:**
```bash
# Switch to develop
git checkout develop

# Pull the merged changes
git pull origin develop

# Delete local feature branch
git branch -d feature/1-architecture-design
```

---

## Commit Message Convention

Follow **Conventional Commits** format:

```
<type>(<scope>): <subject>

<body>

<footer>
```

### Types
- `feat`: New feature
- `fix`: Bug fix
- `docs`: Documentation changes
- `refactor`: Code refactoring
- `test`: Adding tests
- `chore`: Build process or tooling changes
- `perf`: Performance improvements

### Examples

```bash
# Good commit messages
git commit -m "feat(cc): implement FIND function with path compression"
git commit -m "fix(parser): handle empty input gracefully"
git commit -m "docs: add algorithm pseudocode to literature review"
git commit -m "test(union): add tests for largest ccpar optimization"

# With body
git commit -m "feat(dag): implement DAG node representation

- Create Node class with children tracking
- Implement function symbol storage
- Add equivalence class reference

Refs #5"
```

---

## Issue Labels

Create these labels on GitHub:

| Label | Color | Purpose |
|-------|-------|---------|
| `phase-1` | `#d4c5f9` | Phase 1 tasks |
| `phase-2` | `#c5def5` | Phase 2 tasks |
| `phase-3` | `#bfdadc` | Phase 3 tasks |
| `enhancement` | `#a2eeef` | New features |
| `bug` | `#d73a4a` | Bug fixes |
| `documentation` | `#0075ca` | Documentation |
| `testing` | `#f9d0c4` | Testing related |
| `optimization` | `#e4e669` | Performance optimizations |
| `T_E` | `#fbca04` | Theory of Equality |
| `T_cons` | `#fbca04` | Theory of Lists |
| `T_A` | `#fbca04` | Theory of Arrays |
| `good-first-issue` | `#7057ff` | Easy tasks |

---

## Milestones

Create milestones on GitHub for each phase:

1. **Phase 1: Setup & Planning** (Nov 13 - Nov 27, 2025)
2. **Phase 2: Core Implementation** (Nov 27 - Dec 31, 2025)
3. **Phase 3: Input/Output** (Jan 1 - Jan 14, 2026)
4. **Phase 4: Testing** (Jan 14 - Jan 21, 2026)
5. **Phase 5: Optimization** (Jan 21 - Jan 24, 2026)
6. **Phase 6: Report** (Jan 24 - Jan 28, 2026)
7. **Phase 7: Submission** (Jan 28 - Jan 31, 2026)

---

## Sprint Planning (2-week sprints)

### Sprint Structure

**Sprint 1 (Nov 13-26):** Phase 1 completion
- Complete design decisions
- Finalize architecture
- Set up all project infrastructure

**Sprint 2 (Nov 27 - Dec 10):** Basic data structures
- Implement DAG representation
- Implement equivalence classes
- Start FIND/UNION

**Sprint 3 (Dec 11-24):** Congruence Closure
- Complete FIND/UNION/MERGE
- Implement CC algorithm
- Test with T_E examples

... (continue for remaining sprints)

---

## Daily Workflow Example

### Morning: Plan
```bash
# 1. Check current sprint board
# 2. Pick next task from PROJECT_PLAN.md
# 3. Create GitHub issue if not exists
# 4. Create feature branch
git checkout develop
git pull origin develop
git checkout -b feature/5-implement-find
```

### During: Code
```bash
# 1. Implement feature
# 2. Test frequently
mvn test

# 3. Commit often with clear messages
git add src/main/java/solver/core/Find.java
git commit -m "feat(cc): implement FIND function

- Add recursive FIND with path compression
- Track equivalence class representatives
- Add unit tests

Refs #5"

# 4. Push regularly
git push
```

### Evening: Review
```bash
# 1. Ensure all tests pass
mvn clean test

# 2. Create pull request on GitHub
# 3. Review your own code
# 4. Merge if ready
# 5. Update PROJECT_PLAN.md checkboxes
```

---

## Release Strategy

### Version Numbering: Semantic Versioning (SemVer)

Format: `MAJOR.MINOR.PATCH`

- **MAJOR**: Incompatible changes (e.g., 1.0.0 → 2.0.0)
- **MINOR**: New features, backwards compatible (e.g., 1.0.0 → 1.1.0)
- **PATCH**: Bug fixes, backwards compatible (e.g., 1.0.0 → 1.0.1)

### Milestones → Releases

**v0.1.0** - Phase 1 Complete (Design & Planning)
**v0.2.0** - Phase 2 Complete (Core Implementation)
**v0.3.0** - Phase 3 Complete (I/O)
**v0.4.0** - Phase 4 Complete (Testing)
**v0.5.0** - Phase 5 Complete (Optimization)
**v1.0.0** - Final submission (Jan 31, 2026)

### Creating a Release

```bash
# When a phase is complete and merged to main
git checkout main
git pull origin main

# Tag the release
git tag -a v0.1.0 -m "Phase 1: Setup & Planning complete

- Java environment configured
- Literature review complete
- Architecture designed
- Project structure ready"

# Push tag
git push origin v0.1.0
```

**On GitHub:**
1. Go to Releases
2. Create release from tag
3. Add release notes
4. Attach artifacts (JARs if applicable)

---

## Merging to Main

**Only merge to main at the end of each phase:**

```bash
# Create PR: develop → main
# On GitHub:
# 1. Base: main ← Compare: develop
# 2. Title: "Release v0.1.0: Phase 1 Complete"
# 3. Merge after review
# 4. Create tag and GitHub release
```

---

## Quick Reference Commands

### Starting new feature
```bash
git checkout develop
git pull origin develop
git checkout -b feature/<issue>-<name>
git push -u origin feature/<issue>-<name>
```

### Working on feature
```bash
# Make changes
git add .
git commit -m "feat: description"
git push
```

### Finishing feature
```bash
# Create PR on GitHub: feature → develop
# After merge:
git checkout develop
git pull origin develop
git branch -d feature/<issue>-<name>
```

### Checking status
```bash
git status
git log --oneline --graph --all --decorate
git branch -a
```

---

## Agile Best Practices for Solo Development

1. **Keep sprints short**: 1-2 weeks
2. **Break tasks small**: Each feature branch should be completable in 1-3 days
3. **Commit often**: Multiple times per day
4. **Write good commit messages**: Your future self will thank you
5. **Review your own PRs**: Take a break, then review with fresh eyes
6. **Keep main stable**: Only merge tested, working code
7. **Document as you go**: Update docs in the same PR as code
8. **Test continuously**: Run tests before every commit
9. **Update PROJECT_PLAN.md**: Check off items as you complete them
10. **Reflect weekly**: What went well? What to improve?

---

## Troubleshooting

### Merge conflicts
```bash
# If conflicts occur when merging
git checkout develop
git pull origin develop
git checkout feature/your-branch
git merge develop

# Resolve conflicts in files
# Then:
git add .
git commit -m "chore: resolve merge conflicts"
git push
```

### Accidentally committed to wrong branch
```bash
# If you committed to develop instead of feature branch
git log  # Note the commit hash
git reset --hard HEAD~1  # Undo last commit on develop
git checkout -b feature/new-branch
git cherry-pick <commit-hash>
git push -u origin feature/new-branch
```

### Need to update feature branch with latest develop
```bash
git checkout feature/your-branch
git fetch origin
git rebase origin/develop
# Or if you prefer merge:
git merge origin/develop
```

---

**Remember:** This workflow keeps your project organized, your code clean, and makes it easy to track what you've accomplished!
