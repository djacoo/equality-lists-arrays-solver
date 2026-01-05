# Input Format Specification

**Project:** Decision Procedure Solver for T_E ∪ T_cons ∪ T_A
**Version:** 3.0.0 (Production)
**Last Updated:** January 2026

## Overview

This document specifies the input formats accepted by the solver. The solver supports two formats:

1. **Custom Format**: Human-readable text format (recommended for manual testing)
2. **SMT-LIB 2.0**: Standard format for compatibility with benchmarks (QF_UF logic)

Both formats accept conjunctions of literals representing constraints in the union of three theories:
- **T_E**: Theory of Equality with uninterpreted functions
- **T_cons**: Theory of Non-empty Possibly Cyclic Lists
- **T_A**: Theory of Arrays without Extensionality

**Input Methods:**
- Text file (command-line argument)
- Standard input (stdin)
- Pipe or redirection

**Output:**
- SAT (with optional model/equivalence classes)
- UNSAT (with optional conflict explanation)

---

## Literal Types

### 1. Equality Literals
Format: `term = term`

Examples:
```
a = b
f(x) = g(y)
cons(a, b) = x
select(arr, i) = v
```

### 2. Disequality Literals
Format: `term != term`

Examples:
```
a != b
car(x) != a
select(arr, i) != 0
```

### 3. Atom Predicates (T_cons)
Format: `atom(term)` or `!atom(term)`

The `atom` predicate is a unary predicate from the T_cons theory (Bradley & Manna Section 9.4).
By Axiom 7: `∀x,y. ¬atom(cons(x,y))` - all cons-constructed values are non-atomic.

Examples:
```
atom(x)
!atom(cons(a, b))
atom(car(z))
```

---

## Term Syntax

Terms are built from:
- **Variables**: lowercase identifiers (a-z, A-Z, 0-9, underscore)
- **Constants**: lowercase identifiers (same as variables - distinction is semantic, not syntactic)
- **Function Applications**: `functionName(arg1, arg2, ...)`

### Grammar (EBNF)

```ebnf
Literal     ::= Equality | Disequality | Atom | NegAtom

Equality    ::= Term '=' Term

Disequality ::= Term '!=' Term

Atom        ::= 'atom' '(' Term ')'

NegAtom     ::= '!' 'atom' '(' Term ')'

Term        ::= Variable
              | FunctionApp

Variable    ::= Identifier

FunctionApp ::= Identifier '(' Term (',' Term)* ')'

Identifier  ::= [a-zA-Z_][a-zA-Z0-9_]*

Whitespace  ::= [ \t\n\r]+
Comment     ::= '#' [^\n]* | '//' [^\n]*
```

**Note:** The parser treats all simple identifiers as variables. Function applications require parentheses.

### Reserved Function Symbols

These symbols have special meaning in their respective theories:

**T_cons (Lists):**
- `cons(x, y)` - construct list with head x and tail y
- `car(x)` - get head of list x
- `cdr(x)` - get tail of list x

**T_A (Arrays):**
- `select(a, i)` - read array a at index i
- `store(a, i, v)` - write value v to array a at index i

**Notes:**
- `cons` must have exactly 2 arguments
- `car` and `cdr` must have exactly 1 argument
- `select` must have exactly 2 arguments
- `store` must have exactly 3 arguments
- All other functions are uninterpreted (theory T_E)

---

## Input File Format

### Basic Format

```
# Comments start with # or //
# One literal per line

literal1
literal2
literal3
...
```

### Multiple Literals Per Line

Literals can be separated by newlines or semicolons:

```
a = b ; b = c ; c != a
```

### Whitespace

- Whitespace (spaces, tabs, newlines) is ignored except inside identifiers
- Use whitespace freely for readability

---

## Example Inputs

### Example 1: Pure Equality (T_E)

```
# Simple transitivity violation
a = b
b = c
c != a
```

**Expected:** UNSAT

### Example 2: Equality with Functions (T_E)

```
# Congruence example
f(a) = f(b)
a != b
```

**Expected:** UNSAT (if f is injective, but we can't prove it without more constraints)
**Actually:** SAT (f could map both a and b to same value despite a != b)

```
# Congruence that is UNSAT
f(a) = g(b)
a = b
g(a) != f(a)
```

**Expected:** UNSAT (by congruence and transitivity)

### Example 3: Lists (T_cons)

```
# car/cdr axiom example
x = cons(a, b)
car(x) != a
```

**Expected:** UNSAT (violates car(cons(a,b)) = a)

```
# Valid list constraint
x = cons(a, b)
car(x) = a
cdr(x) = b
```

**Expected:** SAT (consistent with axioms)

```
# Cyclic list
x = cons(a, x)
car(x) = a
```

**Expected:** SAT (cyclic lists are allowed)

### Example 4: Arrays (T_A)

```
# Basic array axiom
select(store(a, i, v), i) != v
```

**Expected:** UNSAT (violates read-over-write axiom)

```
# Array write different index
i != j
select(store(a, i, v), j) != select(a, j)
```

**Expected:** UNSAT (if i != j, then store doesn't affect index j)

```
# Multiple stores
a1 = store(a, i, v1)
a2 = store(a1, j, v2)
i != j
select(a2, i) != v1
```

**Expected:** UNSAT

### Example 5: Mixed Theories

```
# Combining lists and arrays
x = cons(a, b)
y = car(x)
z = select(arr, y)
z != select(arr, a)
```

**Expected:** UNSAT (because car(x) = a by axiom)

```
# Complex mixed example
arr1 = store(arr, i, v)
x = cons(i, j)
i2 = car(x)
v2 = select(arr1, i2)
v2 != v
```

**Expected:** UNSAT

---

## Parser Behavior

### Operator Precedence

Function application has highest precedence:
```
f(a) = g(b)    # Parsed as: (f(a)) = (g(b))
```

### Error Handling

The parser should provide clear error messages for:

1. **Syntax Errors:**
```
a =          # Missing right-hand side
f(a, ) = b   # Missing argument
a b = c      # Missing operator
```

2. **Arity Errors:**
```
cons(a) = b       # cons requires 2 arguments
car(a, b) = c     # car requires 1 argument
select(a) = b     # select requires 2 arguments
store(a, i) = b   # store requires 3 arguments
```

3. **Invalid Characters:**
```
a @ b = c         # '@' is not valid
a = $b            # '$' is not valid
```

### Warnings (Optional)

1. **Unused Variables:**
   - Variable appears in only one literal
   - May indicate typo

2. **Tautologies:**
   - `a = a` (always true, can be ignored)

3. **Trivial Conflicts:**
   - `a = b` and `a != b` in input (immediate UNSAT)

---

## Alternative Input Formats (Optional/Future)

### SMT-LIB Format (QF-UF)

For compatibility with SMT-LIB benchmarks:

```lisp
(set-logic QF-UF)
(declare-fun a () U)
(declare-fun b () U)
(declare-fun f (U) U)
(assert (= a b))
(assert (= (f a) (f b)))
(check-sat)
```

**Note:** Supporting SMT-LIB is optional but useful for testing against standard benchmarks.

### JSON Format (For Programmatic Use)

```json
{
  "literals": [
    {"type": "eq", "left": "a", "right": "b"},
    {"type": "neq", "left": {"func": "f", "args": ["a"]}, "right": {"func": "g", "args": ["b"]}}
  ]
}
```

**Note:** JSON format is optional, mainly for integration with other tools.

---

## Reading from Files vs stdin

### File Input
```bash
java -jar solver.jar < input.txt
java -jar solver.jar input.txt
```

### stdin Input
```bash
echo "a = b; b = c; c != a" | java -jar solver.jar
```

### Interactive Mode (Optional)
```bash
java -jar solver.jar
> a = b
> b = c
> c != a
> :solve
UNSAT
> :quit
```

---

## Lexer Token Specification

| Token Type | Pattern | Example |
|------------|---------|---------|
| IDENTIFIER | `[a-zA-Z_][a-zA-Z0-9_]*` | `a`, `f`, `var_1` |
| EQUALS | `=` | `=` |
| NOT_EQUALS | `!=` or `≠` | `!=` |
| LPAREN | `(` | `(` |
| RPAREN | `)` | `)` |
| COMMA | `,` | `,` |
| SEMICOLON | `;` | `;` |
| COMMENT | `#[^\n]*` or `//[^\n]*` | `# comment` |
| WHITESPACE | `[ \t\n\r]+` | (ignored) |
| EOF | End of input | |

---

## Parser Implementation Strategy

### Tokenization (Lexer)

```java
public class Lexer {
    private String input;
    private int position;

    public Token nextToken() {
        skipWhitespaceAndComments();

        if (isAtEnd()) return new Token(TokenType.EOF);

        char c = peek();

        // Check for multi-char operators
        if (c == '!' && peekNext() == '=') {
            advance(); advance();
            return new Token(TokenType.NOT_EQUALS, "!=");
        }

        if (c == '=') {
            advance();
            return new Token(TokenType.EQUALS, "=");
        }

        // ... handle other tokens ...

        if (isAlpha(c)) {
            return identifier();
        }

        throw new ParseException("Unexpected character: " + c);
    }
}
```

### Parsing (Recursive Descent)

```java
public class Parser {
    private Lexer lexer;
    private Token currentToken;

    // Parse: Literal = Term ('=' | '!=') Term
    public Literal parseLiteral() {
        Term left = parseTerm();

        if (match(TokenType.EQUALS)) {
            Term right = parseTerm();
            return new EqualityLiteral(left, right);
        } else if (match(TokenType.NOT_EQUALS)) {
            Term right = parseTerm();
            return new DisequalityLiteral(left, right);
        }

        throw new ParseException("Expected '=' or '!='");
    }

    // Parse: Term = Identifier [ '(' [Term (',' Term)*] ')' ]
    public Term parseTerm() {
        if (!match(TokenType.IDENTIFIER)) {
            throw new ParseException("Expected identifier");
        }

        String id = currentToken.getValue();
        advance();

        if (match(TokenType.LPAREN)) {
            // Function application
            List<Term> args = parseArguments();
            expect(TokenType.RPAREN);
            return factory.createFunctionApp(id, args);
        } else {
            // Variable or constant
            return factory.createVariable(id);
        }
    }
}
```

---

## Test Cases for Parser

### Valid Inputs

1. Simple equality: `a = b`
2. Simple disequality: `a != b`
3. Function application: `f(a, b) = g(c)`
4. Nested functions: `f(g(a)) = h(b)`
5. Lists: `cons(a, b) = x`
6. Arrays: `select(store(a, i, v), i) = v`
7. Multiple literals: `a = b; b = c`
8. With comments: `# comment\na = b`
9. Extra whitespace: `a   =   b`

### Invalid Inputs

1. Missing operator: `a b`
2. Missing operand: `a =`
3. Unmatched parens: `f(a = b`
4. Invalid characters: `a @ b`
5. Empty input: `` (should be accepted as empty set)

---

