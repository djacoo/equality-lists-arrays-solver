# Agile Workflow Quick Start

Your agile development workflow is now fully set up! Here's how to start working on Phase 1.3.

---

## ✅ What's Already Done

1. **Git Branching Structure**
   - ✅ `main` branch (production-ready code)
   - ✅ `develop` branch (integration branch)
   - ✅ GitHub templates for issues and PRs

2. **Documentation**
   - ✅ [AGILE_WORKFLOW_GUIDE.md](AGILE_WORKFLOW_GUIDE.md) - Complete workflow reference
   - ✅ [PHASE_1.3_ISSUES.md](PHASE_1.3_ISSUES.md) - 6 pre-written GitHub issues
   - ✅ Literature review guides and references

3. **Current State**
   - Currently on: `develop` branch
   - Remote: https://github.com/djacoo/equality-lists-arrays-solver

---

## 🚀 Next Steps (Do This Now!)

### Step 1: Set Up Branch Protection on GitHub (2 minutes)

**Why?** Prevents accidental direct commits to main/develop.

**How:**
1. Go to: https://github.com/djacoo/equality-lists-arrays-solver/settings/branches
2. Click "Add rule"
3. For `main`:
   - Branch name pattern: `main`
   - ☑️ Require a pull request before merging
   - ☑️ Require approvals: 1
   - Save changes
4. Repeat for `develop`:
   - Branch name pattern: `develop`
   - ☑️ Require a pull request before merging
   - Save changes

---

### Step 2: Create GitHub Issues for Phase 1.3 (5 minutes)

**Option A: Using GitHub Web (Recommended for first time)**

1. Go to: https://github.com/djacoo/equality-lists-arrays-solver/issues/new
2. Click "Feature/Task" template
3. Open [PHASE_1.3_ISSUES.md](PHASE_1.3_ISSUES.md) in another tab
4. Copy-paste **Issue #1** content
5. Add labels: `enhancement`, `phase-1`, `documentation`
6. Click "Submit new issue"
7. Repeat for issues #2-6

**Option B: Using GitHub CLI (Faster if you have gh installed)**

```bash
# Install GitHub CLI (if not installed)
brew install gh

# Authenticate
gh auth login

# You can then create issues from command line
# (Manual for now, could script later)
```

**After creating all 6 issues:**
- You'll have a clear sprint backlog
- Each issue is a separate feature branch
- You can track progress visually

---

### Step 3: Create Milestones on GitHub (2 minutes)

1. Go to: https://github.com/djacoo/equality-lists-arrays-solver/milestones
2. Click "New milestone"
3. Create:
   - **Phase 1: Setup & Planning** (Due: Nov 27, 2025)
   - **Phase 2: Core Implementation** (Due: Dec 31, 2025)
   - **Phase 3: Input/Output** (Due: Jan 14, 2026)
   - **Phase 4: Testing** (Due: Jan 21, 2026)
   - **Phase 5: Optimization** (Due: Jan 24, 2026)
   - **Phase 6: Report** (Due: Jan 28, 2026)
   - **Phase 7: Submission** (Due: Jan 31, 2026)

4. Assign all 6 Phase 1.3 issues to "Phase 1: Setup & Planning" milestone

---

### Step 4: Start Working on First Feature (Now!)

```bash
# Make sure you're on develop
git checkout develop
git pull origin develop

# Create feature branch for issue #1 (architecture design)
git checkout -b feature/1-architecture-design

# Push to remote
git push -u origin feature/1-architecture-design

# Now you're ready to work!
```

---

## 📋 Your First Task: Issue #1 - Architecture Design

### What to Create

Create a new file: `docs/ARCHITECTURE.md`

**Should include:**
1. **Package Structure**
   ```
   solver/
   ├── core/          # CC algorithm (FIND, UNION, MERGE)
   ├── dag/           # DAG representation
   ├── theory/        # Theory-specific procedures
   │   ├── te/        # T_E procedure
   │   ├── tcons/     # T_cons procedure
   │   └── ta/        # T_A procedure
   ├── parser/        # Input parsing
   ├── solver/        # Main solver orchestration
   └── Main.java
   ```

2. **Component Interfaces**
   - Define what each component does
   - Define how components interact
   - Define data flow

3. **Algorithm Flow Diagram**
   - Show how theories are detected
   - Show how subproblems are created
   - Show how CC is invoked

### Example Work Session

```bash
# Create the architecture document
touch docs/ARCHITECTURE.md
# (Edit the file with your design)

# Create package structure
mkdir -p src/main/java/solver/{core,dag,theory/{te,tcons,ta},parser,solver}

# Commit your work
git add docs/ARCHITECTURE.md src/
git commit -m "feat: design modular architecture for solver

- Define package structure for all components
- Document T_E, T_cons, T_A procedure interfaces
- Create algorithm flow diagram
- Define component responsibilities

Closes #1"

# Push to remote
git push

# Create Pull Request on GitHub
# Go to: https://github.com/djacoo/equality-lists-arrays-solver/pulls
# Click "New pull request"
# Base: develop ← Compare: feature/1-architecture-design
# Fill in PR template
# Submit PR
```

---

## 🔄 Daily Agile Workflow

### Morning (Planning - 5 min)
```bash
# Check your sprint board (GitHub issues)
# Pick next task (or continue current one)
# Create feature branch if starting new task
git checkout develop
git pull origin develop
git checkout -b feature/X-task-name
git push -u origin feature/X-task-name
```

### During Day (Coding)
```bash
# Make changes
# Test frequently
mvn test

# Commit often (multiple times per day)
git add .
git commit -m "feat: implement X"
git push
```

### Evening (Review - 10 min)
```bash
# Run full test suite
mvn clean test

# If feature is complete:
# 1. Create PR on GitHub
# 2. Review your own code
# 3. Merge to develop
# 4. Delete feature branch
# 5. Update PROJECT_PLAN.md checkboxes
```

---

## 📊 Phase 1.3 Task Order

Work on issues in this order:

1. **Issue #1**: Architecture design (1 day)
   - Creates foundation for everything else

2. **Issue #2**: DAG data structures (1 day)
   - Core data structure for CC algorithm

3. **Issue #3**: Equivalence classes (1 day)
   - Critical for FIND/UNION operations

4. **Issue #4**: Input format (0.5 day)
   - Defines how users interact with solver

5. **Issue #5**: Output format (0.5 day)
   - Defines solver responses

6. **Issue #6**: Optional features planning (0.5 day)
   - Plans future optimizations

**Total estimated time: 4.5 days**

---

## 🎯 Success Criteria for Phase 1.3

By the end of Phase 1.3, you should have:

- [ ] Complete architecture document
- [ ] Package structure created
- [ ] Data structure designs for DAG and equivalence classes
- [ ] Input/output format specifications
- [ ] Optional features planned
- [ ] All 6 issues closed
- [ ] All PRs merged to develop
- [ ] PROJECT_PLAN.md task 1.3 marked complete

Then you'll be ready for Phase 2: Core Implementation!

---

## 🆘 Quick Commands Reference

```bash
# Check current branch
git branch -a

# Check status
git status

# Create new feature branch
git checkout -b feature/X-name

# Stage all changes
git add .

# Commit
git commit -m "type: description"

# Push
git push

# Update from develop
git checkout develop
git pull origin develop
git checkout feature/X-name
git merge develop

# View commit history
git log --oneline --graph --all

# Run tests
mvn test

# Compile
mvn compile

# Build JAR
mvn package
```

---

## 📚 Documentation Quick Links

- [AGILE_WORKFLOW_GUIDE.md](AGILE_WORKFLOW_GUIDE.md) - Full workflow reference
- [PHASE_1.3_ISSUES.md](PHASE_1.3_ISSUES.md) - Pre-written GitHub issues
- [PROJECT_PLAN.md](../PROJECT_PLAN.md) - Overall project plan
- [LITERATURE_REVIEW_GUIDE.md](LITERATURE_REVIEW_GUIDE.md) - Research notes
- [SOLVER_ALGORITHM_OVERVIEW.md](SOLVER_ALGORITHM_OVERVIEW.md) - Algorithm reference

---

## 💡 Tips for Success

1. **Keep feature branches small**: Each should be completable in 1-3 days
2. **Commit often**: Multiple commits per day with clear messages
3. **Test continuously**: Run `mvn test` before every commit
4. **Document as you go**: Update docs in the same PR as code
5. **Review your own PRs**: Take a 15-minute break, then review with fresh eyes
6. **Keep develop green**: Only merge working, tested code
7. **Update PROJECT_PLAN.md**: Check off items as you complete them
8. **Weekly reflection**: What went well? What to improve?

---

**You're all set! Start with Step 1 (branch protection), then create the 6 GitHub issues, then start coding! 🚀**
