# Branching and Milestone Strategy

**Last Updated:** November 14, 2025

---

## Git Branch Structure

This project follows a **Git Flow** branching model:

```
main (stable releases/milestones)
  ↑
  | merge at phase completion
  |
develop (active development)
  ↑
  | merge frequently via PRs
  |
feature/* (individual features/issues)
```

---

## Branch Purposes

### `main` Branch
- **Purpose:** Stable, release-ready code at major milestones
- **Merge Policy:** Only from `develop` after completing full phases
- **Protection:** Should have branch protection enabled
- **Tags:** Tagged at each phase completion

### `develop` Branch
- **Purpose:** Active development and feature integration
- **Merge Policy:** Feature branches merge here via Pull Requests
- **Protection:** Branch protection enabled (requires PRs)
- **Status:** Always contains latest integrated work

### `feature/*` Branches
- **Naming:** `feature/{issue-number}-{description}`
  - Example: `feature/20-merge-procedure`
  - Example: `feature/23-te-procedure`
- **Lifetime:** Created for each issue, deleted after PR merge
- **Merge Target:** Always merge to `develop`
- **Process:** Create → Implement → Test → PR → Review → Merge → Delete

---

## Workflow

### 1. Starting New Work
```bash
# Ensure develop is up to date
git checkout develop
git pull origin develop

# Create feature branch
git checkout -b feature/{issue-number}-{description}
```

### 2. During Development
```bash
# Regular commits
git add .
git commit -m "descriptive message"

# Push to remote
git push -u origin feature/{issue-number}-{description}
```

### 3. Completing Work
```bash
# Create Pull Request
gh pr create --title "Title" --body "Description" --base develop

# After approval, merge
gh pr merge {pr-number} --merge --delete-branch

# Update local develop
git checkout develop
git pull origin develop
```

### 4. Phase Completion (Merge to main)
```bash
# Only after full phase completion
git checkout main
git pull origin main

# Merge develop into main
git merge develop -m "Merge Phase X completion"

# Tag the release
git tag -a vX.Y -m "Phase X: Description"

# Push to remote
git push origin main --tags
```

---

## Milestone Strategy

### Milestone Structure
Each major phase has a corresponding GitHub milestone:

- **Milestone 1: Phase 1 - Setup & Planning** ✓ CLOSED
  - Due: November 27, 2025
  - Status: Complete (6/6 issues closed)
  - Closed: November 14, 2025

- **Milestone 2: Phase 2 - Core Implementation** 🔄 IN PROGRESS
  - Due: December 20, 2025
  - Status: Partial (3/6 subtasks complete)
  - Progress: T_E-procedure ✓, T_cons-procedure ⏳, T_A-procedure ⏳

- **Milestone 3: Phase 3 - Input/Output & Interface** ⏳ PLANNED
  - Due: TBD (Early January 2026)

- **Milestone 4: Phase 4-5 - Testing & Optimizations** ⏳ PLANNED
  - Due: TBD (Mid January 2026)

- **Milestone 5: Phase 6-7 - Report & Submission** ⏳ PLANNED
  - Due: January 31, 2026 (Project deadline)

### Milestone Usage

**When to Close a Milestone:**
- All issues in the milestone are closed
- All acceptance criteria met
- Tests passing
- Documentation updated

**When to Merge develop → main:**
- After closing a major milestone
- Code is stable and well-tested
- Represents significant completed functionality

---

## Current Status

### Branch Status
- **`main`**: Phase 1 complete (stable)
- **`develop`**: Phase 2.1-2.3 complete (88 tests passing)
- **Next merge to main**: After Phase 2 completion (tasks 2.1-2.6)

### Milestone Status
- **Milestone 1**: ✓ Closed (November 14, 2025)
- **Milestone 2**: 🔄 Active (Due: December 20, 2025)

### Merge Points (Planned)
1. **Phase 2 Complete** (December 2025)
   - All theory procedures implemented (T_E, T_cons, T_A)
   - Main solver integration complete
   - Comprehensive test coverage

2. **Phase 3 Complete** (Early January 2026)
   - Input/Output interface working
   - End-to-end solver functionality

3. **Final Release** (Before January 31, 2026)
   - All phases complete
   - Report written
   - Ready for submission

---

## Issue Management

### Issue Workflow
1. **Create Issue** - Document feature/bug with acceptance criteria
2. **Create Branch** - `feature/{issue-number}-description`
3. **Implement** - Write code and tests
4. **Test** - All tests must pass (`mvn test`)
5. **Commit** - Follow commit message conventions
6. **Push** - Push feature branch to remote
7. **Create PR** - Target `develop` branch
8. **Review** - Self-review or automated checks
9. **Merge** - Merge PR to `develop`
10. **Close Issue** - Reference PR in closing comment
11. **Delete Branch** - Cleanup feature branch

### Commit Message Format
```
type: short description

- Detailed bullet points if needed
- Use present tense
- Reference issue numbers

Types: feat, fix, docs, test, refactor, chore
```

---

## Best Practices

### DO:
- ✓ Always create feature branches from `develop`
- ✓ Keep feature branches short-lived (1-2 days max)
- ✓ Write descriptive commit messages
- ✓ Test thoroughly before creating PR
- ✓ Delete feature branches after merge
- ✓ Keep `develop` and `main` protected
- ✓ Tag releases on `main`

### DON'T:
- ✗ Commit directly to `develop` or `main`
- ✗ Merge feature branches directly to `main`
- ✗ Keep stale feature branches
- ✗ Merge without running tests
- ✗ Force push to shared branches
- ✗ Merge `develop` → `main` mid-phase

---

## GitHub Branch Protection

### `main` Branch Protection
- ✓ Require pull request reviews
- ✓ Require status checks to pass
- ✓ Require branches to be up to date
- ✓ Include administrators (safety)

### `develop` Branch Protection
- ✓ Require pull request reviews
- ✓ Require status checks to pass
- ✓ Require branches to be up to date

---

## Tags and Releases

### Tagging Convention
- **Format:** `vX.Y` or `phase-X`
- **Examples:**
  - `v1.0` or `phase-1` - Phase 1 completion
  - `v2.0` or `phase-2` - Phase 2 completion
  - `v3.0` or `phase-3` - Phase 3 completion
  - `v1.0.0` - Final submission version

### Creating Tags
```bash
# Annotated tag (recommended)
git tag -a v1.0 -m "Phase 1: Setup & Planning complete"

# Push tags to remote
git push origin --tags
```

---

## Emergency Procedures

### Hotfix on main
If critical fix needed on `main`:
```bash
git checkout main
git checkout -b hotfix/description
# Make fix
git commit -m "hotfix: description"
git push origin hotfix/description

# Create PR to main
gh pr create --title "Hotfix: description" --base main

# After merge, also merge to develop
git checkout develop
git merge main
```

### Reverting a Bad Merge
```bash
# Find the merge commit
git log --oneline

# Revert the merge
git revert -m 1 <merge-commit-hash>

# Push
git push origin <branch>
```

---

## Questions & Decisions Log

**Q: When should develop merge to main?**
A: After completing full phases (not individual tasks). Next merge: after Phase 2 completion.

**Q: How often to create milestones?**
A: One milestone per major phase (1, 2, 3, 4-5, 6-7).

**Q: Can we commit directly to develop?**
A: No - all changes must go through feature branches and PRs (branch protection enforced).

**Decision Date:** November 14, 2025
**Decision:** Option A (Conservative) - Merge develop → main only after full Phase 2 completion
