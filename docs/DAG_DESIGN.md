# DAG Data Structures Design

This document provides detailed design for the Directed Acyclic Graph (DAG) representation of terms used in the Congruence Closure algorithm.

---

## Overview

The DAG is the fundamental data structure for representing terms in the solver. All terms (variables, constants, function applications) are represented as nodes in a shared DAG, enabling efficient structural sharing and equality checking.

**Key Reference:** Bradley & Manna Section 9.3

---

## Design Goals

1. **Structural Sharing:** Identical terms share the same node (hash-consing)
2. **Efficient Lookup:** O(1) term creation/retrieval for existing terms
3. **Immutability:** Terms are immutable once created
4. **Parent Tracking:** Efficient ccpar set computation for UNION optimization
5. **Type Safety:** Strong typing for different term types

---

## Term Class Hierarchy

```
Term (abstract)
├── Variable (leaf)
├── Constant (leaf)
└── FunctionApp (internal node)
```

### Base Class: Term

**File:** `src/main/java/solver/dag/Term.java`

```java
public abstract class Term {
    // Unique identifier for this term
    private final int id;

    // Symbol/name for this term
    private final String symbol;

    // Equivalence class representative (mutable, updated by FIND)
    private Term find;

    // Set of terms that have this term as an argument
    // ccpar(t) = { u | t is an argument of u }
    private final Set<FunctionApp> ccpar;

    // Additional metadata for algorithms
    private final int hashCode;  // Cached for efficiency

    // Constructor
    protected Term(int id, String symbol);

    // Getters
    public int getId();
    public String getSymbol();
    public Term getFind();
    public Set<FunctionApp> getCcpar();

    // Setters (only for mutable fields)
    public void setFind(Term find);
    public void addToCcpar(FunctionApp parent);

    // Abstract methods (subclass-specific)
    public abstract boolean isLeaf();
    public abstract List<Term> getArguments();
    public abstract String toSExpression();

    // Equality based on structural identity (not ==)
    @Override
    public abstract boolean equals(Object obj);

    @Override
    public abstract int hashCode();
}
```

**Design Decisions:**

1. **id field:** Unique identifier assigned sequentially
   - Used for debugging and ordering
   - Never changes once assigned

2. **symbol field:** The name/symbol of the term
   - For Variable: "x", "y", "a", etc.
   - For Constant: "0", "true", "nil", etc.
   - For FunctionApp: "f", "cons", "car", "select", etc.

3. **find field:** Mutable pointer for equivalence class
   - Initially points to self (each term is its own representative)
   - Updated by UNION/MERGE operations
   - Used by FIND to get current representative

4. **ccpar field:** Set of parent function applications
   - **Critical for largest ccpar optimization!**
   - Updated when term is used as argument to function
   - Used by UNION to choose representative with larger ccpar set

5. **Immutability:** Once a term is created, its structure never changes
   - symbol and arguments are final
   - Only find field is mutable (for equivalence class tracking)
   - Ensures hash-consing correctness

---

### Variable (Leaf Node)

**File:** `src/main/java/solver/dag/Variable.java`

```java
public class Variable extends Term {
    // No additional fields needed

    public Variable(int id, String name) {
        super(id, name);
    }

    @Override
    public boolean isLeaf() {
        return true;
    }

    @Override
    public List<Term> getArguments() {
        return Collections.emptyList();
    }

    @Override
    public String toSExpression() {
        return getSymbol();
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof Variable)) return false;
        Variable other = (Variable) obj;
        return getSymbol().equals(other.getSymbol());
    }

    @Override
    public int hashCode() {
        return Objects.hash("Variable", getSymbol());
    }
}
```

**Examples:**
- `x` → Variable(id=1, symbol="x")
- `a` → Variable(id=2, symbol="a")
- `y` → Variable(id=3, symbol="y")

---

### Constant (Leaf Node)

**File:** `src/main/java/solver/dag/Constant.java`

```java
public class Constant extends Term {
    // No additional fields needed

    public Constant(int id, String value) {
        super(id, value);
    }

    @Override
    public boolean isLeaf() {
        return true;
    }

    @Override
    public List<Term> getArguments() {
        return Collections.emptyList();
    }

    @Override
    public String toSExpression() {
        return getSymbol();
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof Constant)) return false;
        Constant other = (Constant) obj;
        return getSymbol().equals(other.getSymbol());
    }

    @Override
    public int hashCode() {
        return Objects.hash("Constant", getSymbol());
    }
}
```

**Examples:**
- `0` → Constant(id=4, symbol="0")
- `true` → Constant(id=5, symbol="true")
- `nil` → Constant(id=6, symbol="nil")

**Note:** Constants vs Variables distinction may not be strictly necessary for CC algorithm, but helps with clarity and potential future extensions.

---

### FunctionApp (Internal Node)

**File:** `src/main/java/solver/dag/FunctionApp.java`

```java
public class FunctionApp extends Term {
    // Arguments to this function (immutable)
    private final List<Term> arguments;

    public FunctionApp(int id, String functionSymbol, List<Term> arguments) {
        super(id, functionSymbol);
        this.arguments = Collections.unmodifiableList(new ArrayList<>(arguments));

        // Register this as a parent in all argument terms' ccpar sets
        for (Term arg : arguments) {
            arg.addToCcpar(this);
        }
    }

    @Override
    public boolean isLeaf() {
        return false;
    }

    @Override
    public List<Term> getArguments() {
        return arguments;
    }

    @Override
    public String toSExpression() {
        if (arguments.isEmpty()) {
            return getSymbol();  // Nullary function
        }
        StringBuilder sb = new StringBuilder();
        sb.append("(").append(getSymbol());
        for (Term arg : arguments) {
            sb.append(" ").append(arg.toSExpression());
        }
        sb.append(")");
        return sb.toString();
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof FunctionApp)) return false;
        FunctionApp other = (FunctionApp) obj;
        return getSymbol().equals(other.getSymbol()) &&
               arguments.equals(other.arguments);
    }

    @Override
    public int hashCode() {
        return Objects.hash("FunctionApp", getSymbol(), arguments);
    }

    // Arity of the function
    public int getArity() {
        return arguments.size();
    }
}
```

**Examples:**
- `f(x, y)` → FunctionApp(id=7, symbol="f", args=[Variable("x"), Variable("y")])
- `cons(a, b)` → FunctionApp(id=8, symbol="cons", args=[Variable("a"), Variable("b")])
- `car(x)` → FunctionApp(id=9, symbol="car", args=[Variable("x")])
- `select(arr, i)` → FunctionApp(id=10, symbol="select", args=[Variable("arr"), Variable("i")])

**Key Feature:** When a FunctionApp is created, it automatically registers itself in the ccpar sets of its arguments. This ensures ccpar sets are always up-to-date.

---

## TermFactory (Hash-Consing)

**File:** `src/main/java/solver/dag/TermFactory.java`

**Purpose:** Ensures that structurally identical terms share the same object.

```java
public class TermFactory {
    // Maps structural representation to existing Term objects
    private final Map<TermKey, Term> termCache;

    // Counter for assigning unique IDs
    private int nextId;

    public TermFactory() {
        this.termCache = new HashMap<>();
        this.nextId = 1;
    }

    // Create or retrieve a variable
    public Variable createVariable(String name) {
        TermKey key = new TermKey("Variable", name, Collections.emptyList());
        return (Variable) termCache.computeIfAbsent(key, k -> {
            return new Variable(nextId++, name);
        });
    }

    // Create or retrieve a constant
    public Constant createConstant(String value) {
        TermKey key = new TermKey("Constant", value, Collections.emptyList());
        return (Constant) termCache.computeIfAbsent(key, k -> {
            return new Constant(nextId++, value);
        });
    }

    // Create or retrieve a function application
    public FunctionApp createFunctionApp(String symbol, List<Term> arguments) {
        TermKey key = new TermKey("FunctionApp", symbol, arguments);
        return (FunctionApp) termCache.computeIfAbsent(key, k -> {
            return new FunctionApp(nextId++, symbol, arguments);
        });
    }

    // Convenience method for binary functions
    public FunctionApp createFunctionApp(String symbol, Term arg1, Term arg2) {
        return createFunctionApp(symbol, Arrays.asList(arg1, arg2));
    }

    // Convenience method for unary functions
    public FunctionApp createFunctionApp(String symbol, Term arg) {
        return createFunctionApp(symbol, Collections.singletonList(arg));
    }

    // Get total number of terms created
    public int getTermCount() {
        return termCache.size();
    }

    // Get all terms
    public Collection<Term> getAllTerms() {
        return Collections.unmodifiableCollection(termCache.values());
    }

    // Helper class for cache keys
    private static class TermKey {
        private final String type;
        private final String symbol;
        private final List<Term> arguments;
        private final int hashCode;

        public TermKey(String type, String symbol, List<Term> arguments) {
            this.type = type;
            this.symbol = symbol;
            this.arguments = arguments;
            this.hashCode = Objects.hash(type, symbol, arguments);
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) return true;
            if (!(obj instanceof TermKey)) return false;
            TermKey other = (TermKey) obj;
            return type.equals(other.type) &&
                   symbol.equals(other.symbol) &&
                   arguments.equals(other.arguments);
        }

        @Override
        public int hashCode() {
            return hashCode;
        }
    }
}
```

**Benefits of Hash-Consing:**
1. **Memory Efficiency:** Each unique term structure exists only once
2. **Equality Checking:** Can use `==` for structural equality (after normalization)
3. **DAG Construction:** Automatically creates shared subterm structure

**Example:**
```java
TermFactory factory = new TermFactory();
Variable x = factory.createVariable("x");
Variable y = factory.createVariable("y");

// These will be the same object!
FunctionApp f1 = factory.createFunctionApp("f", x, y);
FunctionApp f2 = factory.createFunctionApp("f", x, y);
assert f1 == f2;  // True! Same structural term → same object
```

---

## DAG Container

**File:** `src/main/java/solver/dag/DAG.java`

**Purpose:** Container for all terms and provides DAG-level operations.

```java
public class DAG {
    // Factory for creating terms with hash-consing
    private final TermFactory factory;

    // All terms in topological order (leaves first, roots last)
    private final List<Term> terms;

    public DAG() {
        this.factory = new TermFactory();
        this.terms = new ArrayList<>();
    }

    // Get the term factory
    public TermFactory getFactory() {
        return factory;
    }

    // Add a term to the DAG (with all its subterms)
    public void addTerm(Term term) {
        // Add all subterms first (depth-first)
        if (!term.isLeaf()) {
            for (Term arg : term.getArguments()) {
                addTerm(arg);
            }
        }

        // Add this term if not already present
        if (!terms.contains(term)) {
            terms.add(term);
        }
    }

    // Get all terms in topological order
    public List<Term> getTerms() {
        return Collections.unmodifiableList(terms);
    }

    // Get all function applications
    public List<FunctionApp> getFunctionApps() {
        return terms.stream()
                    .filter(t -> !t.isLeaf())
                    .map(t -> (FunctionApp) t)
                    .collect(Collectors.toList());
    }

    // Get all variables
    public List<Variable> getVariables() {
        return terms.stream()
                    .filter(t -> t instanceof Variable)
                    .map(t -> (Variable) t)
                    .collect(Collectors.toList());
    }

    // Get term count
    public int size() {
        return terms.size();
    }

    // Pretty print the DAG
    public String toDot() {
        // Generate GraphViz DOT format for visualization
        // Useful for debugging!
        StringBuilder sb = new StringBuilder();
        sb.append("digraph DAG {\n");
        for (Term term : terms) {
            if (!term.isLeaf()) {
                FunctionApp app = (FunctionApp) term;
                for (Term arg : app.getArguments()) {
                    sb.append(String.format("  t%d -> t%d;\n", term.getId(), arg.getId()));
                }
            }
            sb.append(String.format("  t%d [label=\"%s\"];\n",
                                    term.getId(), term.getSymbol()));
        }
        sb.append("}\n");
        return sb.toString();
    }
}
```

**Usage Example:**
```java
DAG dag = new DAG();
TermFactory factory = dag.getFactory();

// Build terms: f(a, b) = g(a, b)
Variable a = factory.createVariable("a");
Variable b = factory.createVariable("b");
FunctionApp fab = factory.createFunctionApp("f", a, b);
FunctionApp gab = factory.createFunctionApp("g", a, b);

// Add to DAG
dag.addTerm(fab);
dag.addTerm(gab);

// DAG now contains: [a, b, f(a,b), g(a,b)] in topological order
System.out.println("DAG size: " + dag.size());  // 4
```

---

## ccpar Set Design

**What is ccpar?**

From Downey et al. [8] and Detlef et al. [7]:

> ccpar(t) = { u | t is an argument of u }

In other words, the **congruence closure parents** of a term `t` is the set of all function applications that have `t` as one of their arguments.

**Why is it important?**

The largest ccpar optimization (MANDATORY) states:
> In UNION, choose the representative from the equivalence class with the larger ccpar set.

This reduces the number of congruence propagations needed in MERGE.

**Implementation:**

Each `Term` has a `Set<FunctionApp> ccpar` field that is:
1. Initialized to empty set when term is created
2. Updated when term is used as argument to a function
3. Used by UNION to compare sizes and choose representative

**Example:**
```
Terms: a, b, f(a), g(a, b), h(g(a, b))

ccpar(a) = {f(a), g(a, b)}        // a is argument to f and g
ccpar(b) = {g(a, b)}               // b is argument to g only
ccpar(f(a)) = {}                   // f(a) is not argument to anything
ccpar(g(a,b)) = {h(g(a, b))}      // g(a,b) is argument to h
ccpar(h(g(a,b))) = {}             // h(...) is not argument to anything
```

**In UNION:**
```java
// If merging equivalence classes of a and b:
// ccpar(a) has size 2
// ccpar(b) has size 1
// → Choose a as representative (larger ccpar)
```

---

## Initialization and Find Field

**Initial State:**

When a term `t` is created:
1. `t.find = t` (points to itself)
2. `t.ccpar = {}` (empty set)
3. Each term is in its own equivalence class

**After MERGE(t1, t2):**

1. FIND is called to get representatives
2. UNION merges the equivalence classes
3. One representative's find field is updated to point to the other
4. All terms in the non-representative class have their find updated (or lazily via FIND path compression)

**Example:**
```java
// Initial state
Variable a = factory.createVariable("a");
Variable b = factory.createVariable("b");
assert a.getFind() == a;  // a represents itself
assert b.getFind() == b;  // b represents itself

// After MERGE(a, b)
merge(a, b);
// Now either:
//   a.getFind() == b (b chosen as representative)
// or:
//   b.getFind() == a (a chosen as representative)
// Depending on ccpar sizes
```

---

## Data Structure Complexity

| Operation | Time Complexity | Notes |
|-----------|----------------|-------|
| Term creation (TermFactory) | O(1) amortized | Hash table lookup |
| Adding term to ccpar | O(1) | Set insertion |
| Getting ccpar size | O(1) | Size is cached |
| DAG traversal | O(n) | n = number of terms |
| Topological ordering | O(n) | During construction |

---

## Memory Layout

**Per Term:**
- id: 4 bytes (int)
- symbol: ~24 bytes (String object overhead + chars)
- find: 8 bytes (reference)
- ccpar: ~40 bytes (HashSet overhead) + 8 bytes per parent
- hashCode: 4 bytes (int)
- **Total per term:** ~80 bytes + size(ccpar) × 8

**For 1000 terms with average ccpar size of 3:**
- Memory: ~80KB + 24KB = ~104KB
- Very reasonable for in-memory operation

---

## Testing Strategy

### Unit Tests for Term Classes

1. **Variable Tests:**
   - Creation
   - Equality (same name → equal)
   - Hash code consistency
   - S-expression representation

2. **Constant Tests:**
   - Same as Variable tests

3. **FunctionApp Tests:**
   - Creation with 0, 1, 2, 3+ arguments
   - Structural equality
   - ccpar registration in arguments
   - Nested function applications

### Unit Tests for TermFactory

1. **Hash-Consing Tests:**
   - Same variable name → same object
   - Same function structure → same object
   - Different structures → different objects

2. **Ordering Tests:**
   - IDs assigned sequentially
   - IDs are unique

### Unit Tests for DAG

1. **Construction Tests:**
   - Adding single term
   - Adding term with subterms (automatic recursion)
   - Topological ordering

2. **Query Tests:**
   - Get all terms
   - Get only function apps
   - Get only variables

3. **DOT Generation Tests:**
   - Verify format for visualization

---

## Implementation Plan

### Step 1: Term Base Class
- [ ] Implement abstract Term class
- [ ] Add id, symbol, find, ccpar fields
- [ ] Implement getters/setters
- [ ] Add unit tests

### Step 2: Term Subclasses
- [ ] Implement Variable class
- [ ] Implement Constant class
- [ ] Implement FunctionApp class
- [ ] Add unit tests for each

### Step 3: TermFactory
- [ ] Implement hash-consing logic
- [ ] Add convenience methods
- [ ] Add unit tests for structural sharing

### Step 4: DAG Container
- [ ] Implement DAG class
- [ ] Add topological ordering
- [ ] Add query methods
- [ ] Add DOT visualization
- [ ] Add unit tests

### Step 5: Integration
- [ ] Test complex term structures
- [ ] Test ccpar tracking
- [ ] Verify memory efficiency

---

## Example: Building a DAG

```java
// Initialize
DAG dag = new DAG();
TermFactory factory = dag.getFactory();

// Create literals: f(a, b) = g(a, c), g(a, c) != h(b)

// Build terms
Variable a = factory.createVariable("a");
Variable b = factory.createVariable("b");
Variable c = factory.createVariable("c");

FunctionApp fab = factory.createFunctionApp("f", a, b);
FunctionApp gac = factory.createFunctionApp("g", a, c);
FunctionApp hb = factory.createFunctionApp("h", b);

// Add to DAG
dag.addTerm(fab);
dag.addTerm(gac);
dag.addTerm(hb);

// Check ccpar sets
System.out.println("ccpar(a) size: " + a.getCcpar().size());  // 2 (f and g)
System.out.println("ccpar(b) size: " + b.getCcpar().size());  // 2 (f and h)
System.out.println("ccpar(c) size: " + c.getCcpar().size());  // 1 (g only)

// Visualize
System.out.println(dag.toDot());
```

---

## Next Steps

After completing the DAG design and implementation:
1. Move to Issue #4: Equivalence class data structures
2. Then implement the CC algorithm using these structures
3. Test with simple equality examples

---

**This design provides a solid, efficient foundation for the entire solver!**
