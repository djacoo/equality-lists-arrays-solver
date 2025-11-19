# Contributing to Equality, Lists, and Arrays Solver

This document provides guidelines for developers working on this project.

## Table of Contents

1. [Getting Started](#getting-started)
2. [Project Structure](#project-structure)
3. [Development Workflow](#development-workflow)
4. [Code Style and Conventions](#code-style-and-conventions)
5. [Testing Guidelines](#testing-guidelines)
6. [Git Workflow](#git-workflow)
7. [Building and Running](#building-and-running)

---

## Getting Started

### Prerequisites

Before you begin, ensure you have the following installed:

- **Java Development Kit (JDK) 17 or higher**
  - Download from: https://adoptium.net/
  - Verify: `java -version`

- **Apache Maven 3.6 or higher**
  - Download from: https://maven.apache.org/download.cgi
  - Verify: `mvn -version`

- **Git**
  - Download from: https://git-scm.com/
  - Verify: `git --version`

### Quick Start for New Developers

```bash
# 1. Clone the repository
git clone <repository-url>
cd equality-lists-arrays-solver

# 2. Build the project
mvn clean compile

# 3. Run tests to ensure everything works
mvn test

# 4. Build the executable JAR
mvn package

# 5. Test the solver with a sample input
java -jar target/equality-lists-arrays-solver-1.0-SNAPSHOT-standalone.jar tests/input/te/te_simple_sat.txt
```

---

## Project Structure

The project follows a standard Maven directory layout with clear separation of concerns:

```
equality-lists-arrays-solver/
├── src/
│   ├── main/java/solver/          # Production source code
│   │   ├── core/                  # Core congruence closure algorithm
│   │   │   ├── CongruenceChecker.java    # Congruence checking logic
│   │   │   ├── CongruenceClosure.java    # Main CC algorithm
│   │   │   └── MergeManager.java         # UNION and MERGE operations
│   │   │
│   │   ├── dag/                   # DAG and term representation
│   │   │   ├── DAG.java                  # Directed Acyclic Graph
│   │   │   ├── Term.java                 # Base term interface
│   │   │   ├── Variable.java             # Variable terms
│   │   │   ├── Constant.java             # Constant terms
│   │   │   ├── FunctionApp.java          # Function applications
│   │   │   └── TermFactory.java          # Factory for creating terms
│   │   │
│   │   ├── equivalence/           # Equivalence class management
│   │   │   ├── EquivalenceClass.java     # Equivalence class representation
│   │   │   └── ClassManager.java         # FIND operations
│   │   │
│   │   ├── theory/                # Theory-specific procedures
│   │   │   ├── te/                # Theory of Equality
│   │   │   │   ├── Literal.java          # Equality/disequality literals
│   │   │   │   └── TEProcedure.java      # T_E satisfiability procedure
│   │   │   │
│   │   │   ├── tcons/             # Theory of Lists
│   │   │   │   ├── TConsSymbols.java     # cons, car, cdr identification
│   │   │   │   └── TConsProcedure.java   # T_cons satisfiability procedure
│   │   │   │
│   │   │   └── tarray/            # Theory of Arrays
│   │   │       ├── TArraySymbols.java    # select, store identification
│   │   │       └── TArrayProcedure.java  # T_A satisfiability procedure
│   │   │
│   │   ├── parser/                # Input parsing
│   │   │   ├── Lexer.java                # Tokenizer for custom format
│   │   │   ├── Parser.java               # Parser for custom format
│   │   │   ├── InputReader.java          # File/stdin reader
│   │   │   ├── PredicateTransformer.java # Free predicate handling
│   │   │   ├── SMTLIBLexer.java          # SMT-LIB tokenizer
│   │   │   ├── SMTLIBParser.java         # SMT-LIB parser
│   │   │   └── SMTLIBInputReader.java    # SMT-LIB reader
│   │   │
│   │   ├── UnifiedSolver.java     # Main solver entry point
│   │   ├── Main.java              # CLI interface (custom format)
│   │   └── SMTLIBSolver.java      # CLI interface (SMT-LIB format)
│   │
│   └── test/java/solver/          # Test source code
│       ├── core/                  # Core algorithm tests
│       ├── dag/                   # DAG structure tests
│       ├── equivalence/           # Equivalence class tests
│       └── theory/                # Theory-specific tests
│
├── tests/                         # Test files and documentation
│   ├── README.md                  # Test suite overview
│   ├── TEST_INDEX.md              # Comprehensive test catalog
│   └── input/                     # Test input files
│       ├── te/                    # Theory of Equality tests
│       ├── tcons/                 # Theory of Lists tests
│       ├── tarray/                # Theory of Arrays tests
│       ├── combined/              # Combined theory tests
│       └── smtlib/                # SMT-LIB format tests
│
├── docs/                          # Documentation
│   ├── README.md                  # Documentation index
│   ├── PROJECT_PLAN.md            # Development timeline
│   ├── ARCHITECTURE.md            # System architecture
│   ├── INPUT_FORMAT.md            # Input specifications
│   ├── OUTPUT_FORMAT.md           # Output specifications
│   └── SMTLIB_PARSER.md           # SMT-LIB parser details
│
├── output/                        # Expected outputs (future use)
├── experiments/                   # Experimental results
├── assignment/                    # Course materials
├── pom.xml                        # Maven configuration
├── .gitignore                     # Git ignore patterns
└── CONTRIBUTING.md                # This file
```

### Package Organization

- **`solver.core`**: Core congruence closure algorithm implementation
- **`solver.dag`**: DAG representation and term structures
- **`solver.equivalence`**: Equivalence class data structures
- **`solver.theory.te`**: Theory of Equality procedures
- **`solver.theory.tcons`**: Theory of Lists procedures
- **`solver.theory.tarray`**: Theory of Arrays procedures
- **`solver.parser`**: Input parsing for both custom and SMT-LIB formats

---

## Development Workflow

### Setting Up Your Development Environment

1. **IDE Setup** (recommended: IntelliJ IDEA or Eclipse)
   ```bash
   # For IntelliJ IDEA
   mvn idea:idea

   # For Eclipse
   mvn eclipse:eclipse
   ```

2. **Import as Maven Project** in your IDE

3. **Verify Build**
   ```bash
   mvn clean compile
   ```

### Making Changes

1. **Create a Feature Branch**
   ```bash
   git checkout -b feature/your-feature-name
   ```

2. **Make Your Changes**
   - Write code following the style guidelines (see below)
   - Add tests for new functionality
   - Update documentation as needed

3. **Test Your Changes**
   ```bash
   # Run all tests
   mvn test

   # Run specific test class
   mvn test -Dtest=CongruenceClosureTest

   # Run tests with coverage (if configured)
   mvn clean test jacoco:report
   ```

4. **Build and Verify**
   ```bash
   mvn clean package
   ```

5. **Commit Your Changes** (see Git Workflow below)

---

## Code Style and Conventions

### General Principles

- **Clarity over cleverness**: Write clear, readable code
- **Self-documenting code**: Use descriptive names for variables, methods, and classes
- **Consistent formatting**: Follow the established patterns in the codebase
- **Modular design**: Keep classes focused on a single responsibility

### Naming Conventions

#### Classes
- **CamelCase** starting with uppercase
- Descriptive names indicating purpose
- Examples: `CongruenceClosure`, `EquivalenceClass`, `TermFactory`

#### Methods
- **camelCase** starting with lowercase
- Verb-based names describing action
- Examples: `merge()`, `find()`, `checkSatisfiability()`

#### Variables
- **camelCase** starting with lowercase
- Descriptive names, avoid single letters except for:
  - Loop counters (`i`, `j`, `k`)
  - Mathematical variables from papers (`x`, `y`, `z`)
- Examples: `currentNode`, `equivClass`, `literalList`

#### Constants
- **UPPER_SNAKE_CASE**
- Examples: `MAX_ITERATIONS`, `DEFAULT_CAPACITY`

#### Packages
- **lowercase**, dot-separated
- Examples: `solver.core`, `solver.theory.te`

### Java Code Style

#### Indentation and Formatting
```java
// Use 4 spaces for indentation (not tabs)
public class ExampleClass {
    private int field;

    public void method(String param) {
        if (condition) {
            // Do something
        } else {
            // Do something else
        }
    }
}
```

#### Braces
```java
// Opening brace on same line
if (condition) {
    statement();
}

// Always use braces, even for single statements
if (condition) {
    singleStatement();
}
```

#### Method Structure
```java
/**
 * Brief description of what the method does.
 *
 * @param param description of parameter
 * @return description of return value
 */
public ReturnType methodName(ParameterType param) {
    // Implementation
}
```

#### Class Structure Order
1. Static constants
2. Static variables
3. Instance variables
4. Constructors
5. Public methods
6. Protected methods
7. Private methods
8. Inner classes

### Documentation

#### Javadoc Comments
- **All public classes and methods** should have Javadoc comments
- **Complex private methods** should have explanatory comments
- Reference paper algorithms where applicable

```java
/**
 * Implements the MERGE procedure from Bradley & Manna, Section 9.3.
 *
 * Merges two equivalence classes and propagates congruences.
 *
 * @param node1 first node to merge
 * @param node2 second node to merge
 * @return true if merge successful, false if conflict detected
 */
public boolean merge(Term node1, Term node2) {
    // Implementation
}
```

#### Inline Comments
```java
// Explain WHY, not WHAT
// Good: "Use largest ccpar optimization to reduce merge operations"
// Bad:  "Check if size is greater"

// Reference paper sections for algorithm steps
// Example: "Bradley & Manna Section 9.3, Algorithm 9.2, Step 3"
```

### Error Handling

```java
// Use specific exceptions
throw new IllegalArgumentException("Term cannot be null");

// Catch specific exceptions
try {
    operation();
} catch (ParseException e) {
    // Handle parse error
} catch (IOException e) {
    // Handle I/O error
}
```

### Code Organization Best Practices

1. **Single Responsibility**: Each class should have one clear purpose
2. **Small Methods**: Keep methods focused and under 50 lines when possible
3. **Avoid Deep Nesting**: Maximum 3 levels of nesting
4. **Null Checks**: Validate parameters at method entry
5. **Immutability**: Prefer immutable objects where appropriate

---

## Testing Guidelines

### Test Structure

```java
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class CongruenceClosureTest {

    @Test
    void testSimpleEquality() {
        // Arrange
        CongruenceClosure cc = new CongruenceClosure();

        // Act
        boolean result = cc.addEquality(termA, termB);

        // Assert
        assertTrue(result);
        assertEquals(cc.find(termA), cc.find(termB));
    }
}
```

### Test Organization

- **Test files** mirror the structure of source files
- **Test class naming**: `<ClassName>Test.java`
- **Test method naming**: `test<MethodName><Scenario>()`

### Test Coverage Goals

- **Core algorithms**: 100% coverage for critical paths
- **Theory procedures**: Comprehensive test cases from papers
- **Edge cases**: Null inputs, empty sets, large inputs
- **Integration tests**: End-to-end solver tests

### Test Input Files

- Located in `tests/input/`
- Organized by theory (`te/`, `tcons/`, `tarray/`, `combined/`)
- Named descriptively: `<theory>_<description>_<expected>.txt`
  - Example: `te_simple_sat.txt`, `tarray_complex_unsat.txt`

### Running Tests

```bash
# All tests
mvn test

# Specific test class
mvn test -Dtest=TEProcedureTest

# Specific test method
mvn test -Dtest=TEProcedureTest#testSimpleEquality

# With verbose output
mvn test -X
```

---

## Git Workflow

### Branch Strategy

- **`main`**: Stable, production-ready code
- **`develop`**: Integration branch for features
- **`feature/<name>`**: Feature development branches
- **`fix/<name>`**: Bug fix branches
- **`docs/<name>`**: Documentation update branches

### Creating a Feature Branch

```bash
# Start from develop
git checkout develop
git pull origin develop

# Create feature branch
git checkout -b feature/your-feature-name
```

### Committing Changes

#### Commit Message Format
```
<type>: <short summary>

<optional detailed description>

<optional references>
```

#### Types
- **feat**: New feature
- **fix**: Bug fix
- **docs**: Documentation changes
- **refactor**: Code refactoring
- **test**: Adding or updating tests
- **chore**: Build/tooling changes

#### Examples
```bash
# Good commit messages
git commit -m "feat: implement T_A store decomposition"
git commit -m "fix: handle null terms in merge operation"
git commit -m "docs: add SMT-LIB parser documentation"
git commit -m "test: add comprehensive T_cons test suite"

# With detailed description
git commit -m "feat: add largest ccpar optimization

Implements the largest ccpar optimization from Bradley & Manna
Section 9.3 to reduce the number of merge operations by always
merging the smaller equivalence class into the larger one.

Refs: PROJECT_PLAN.md Phase 2.2"
```

### Merging Changes

```bash
# Update your branch with latest develop
git checkout feature/your-feature
git rebase develop

# Merge into develop
git checkout develop
git merge --no-ff feature/your-feature

# Clean up
git branch -d feature/your-feature
```

### Pull Request Guidelines

When creating a pull request:

1. **Title**: Clear, concise description
2. **Description**: What changes were made and why
3. **Tests**: Confirm all tests pass
4. **Documentation**: Update relevant docs
5. **References**: Link to related issues/tasks

---

## Building and Running

### Build Commands

```bash
# Clean previous builds
mvn clean

# Compile source code
mvn compile

# Compile and run tests
mvn test

# Package into JAR (runs compile and test first)
mvn package

# Full clean build
mvn clean package

# Skip tests during build (not recommended)
mvn package -DskipTests
```

### Running the Solver

#### Custom Format
```bash
# From JAR
java -jar target/equality-lists-arrays-solver-1.0-SNAPSHOT-standalone.jar tests/input/te/te_simple_sat.txt

# From stdin
echo -e "a = b\nb = c\nc != a" | java -jar target/equality-lists-arrays-solver-1.0-SNAPSHOT-standalone.jar

# Using Maven
mvn exec:java -Dexec.mainClass="solver.Main" -Dexec.args="tests/input/te/te_simple_sat.txt"
```

#### SMT-LIB Format
```bash
# From JAR
java -cp target/equality-lists-arrays-solver-1.0-SNAPSHOT-standalone.jar solver.SMTLIBSolver tests/input/smtlib/example.smt2

# Using Maven
mvn exec:java -Dexec.mainClass="solver.SMTLIBSolver" -Dexec.args="tests/input/smtlib/example.smt2"
```

### Debugging

#### Enable Debug Output
Modify the code to add debug logging, or use your IDE's debugger.

#### Run Tests in Debug Mode
```bash
# In IntelliJ IDEA: Right-click test class → Debug
# In Eclipse: Right-click test class → Debug As → JUnit Test

# Command line with Maven (suspend for debugger attachment)
mvn test -Dmaven.surefire.debug
```

---

## Additional Resources

### Documentation
- [README.md](README.md) - Project overview
- [docs/ARCHITECTURE.md](docs/ARCHITECTURE.md) - System architecture
- [docs/PROJECT_PLAN.md](docs/PROJECT_PLAN.md) - Development timeline
- [docs/INPUT_FORMAT.md](docs/INPUT_FORMAT.md) - Input specifications
- [tests/README.md](tests/README.md) - Test suite documentation

### References
1. Bradley & Manna - *The Calculus of Computation*, Sections 9.3-9.5
2. Kroening & Strichman - *Decision Procedures*
3. Nelson & Oppen - Congruence Closure foundations

### Getting Help

- Check existing documentation in `docs/`
- Review test cases in `src/test/java/solver/`
- Examine example inputs in `tests/input/`
- Consult the referenced academic papers

---

## Project-Specific Notes

### Important Implementation Details

1. **Largest ccpar Optimization**: Always implemented in `MergeManager.java`
2. **DAG Representation**: All terms stored in a shared DAG for efficiency
3. **Theory Detection**: Automatic detection in `UnifiedSolver.java`
4. **Store Decomposition**: Array theory uses the decomposition approach (Section 9.5)

### Common Tasks

#### Adding a New Test Case
```bash
# 1. Create test file
echo "your literals here" > tests/input/category/test_name_sat.txt

# 2. Add entry to TEST_INDEX.md
# 3. Run the test
java -jar target/equality-lists-arrays-solver-1.0-SNAPSHOT-standalone.jar tests/input/category/test_name_sat.txt

# 4. Add automated test if needed
# Create test method in appropriate test class
```

#### Adding a New Theory Procedure
1. Create package under `solver.theory.<theoryname>/`
2. Implement symbol detection class
3. Implement procedure class extending/implementing common interface
4. Add integration to `UnifiedSolver.java`
5. Add comprehensive tests
6. Update documentation

---

**Thank you for contributing to this project!**
