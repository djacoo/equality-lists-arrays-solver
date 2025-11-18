package solver.dag;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Abstract base class for all terms in the DAG.
 *
 * Terms are the fundamental building blocks representing variables, constants,
 * and function applications. Each term has a unique ID, a symbol name, and
 * maintains its equivalence class representative through the find field.
 *
 * The ccpar (congruence closure parents) set tracks all function applications
 * that use this term as an argument, which is critical for the largest ccpar
 * optimization in UNION operations.
 */
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

    // Cached hash code for efficiency
    private final int cachedHashCode;

    /**
     * Constructs a new term with the given ID and symbol.
     *
     * @param id Unique identifier for this term
     * @param symbol The name/symbol of this term
     */
    protected Term(int id, String symbol) {
        this.id = id;
        this.symbol = symbol;
        this.find = this;  // Initially, each term is its own representative
        this.ccpar = new HashSet<>();
        this.cachedHashCode = computeHashCode();
    }

    /**
     * Computes the hash code for this term.
     * Subclasses must implement this based on their structure.
     */
    protected abstract int computeHashCode();

    // Getters

    public int getId() {
        return id;
    }

    public String getSymbol() {
        return symbol;
    }

    public Term getFind() {
        return find;
    }

    public Set<FunctionApp> getCcpar() {
        return ccpar;
    }

    public int getCcparSize() {
        return ccpar.size();
    }

    // Setters (only for mutable fields)

    public void setFind(Term find) {
        this.find = find;
    }

    public void addToCcpar(FunctionApp parent) {
        ccpar.add(parent);
    }

    // Abstract methods that subclasses must implement

    /**
     * Returns true if this term is a leaf (Variable or Constant),
     * false if it's a function application.
     */
    public abstract boolean isLeaf();

    /**
     * Returns the list of arguments if this is a function application,
     * or an empty list if this is a leaf.
     */
    public abstract List<Term> getArguments();

    /**
     * Returns an S-expression representation of this term.
     * Examples: "x", "(f x y)", "(cons a b)"
     */
    public abstract String toSExpression();

    // Object methods

    /**
     * Structural equality: two terms are equal if they have the same structure.
     * This is used by TermFactory for hash-consing.
     */
    @Override
    public abstract boolean equals(Object obj);

    @Override
    public int hashCode() {
        return cachedHashCode;
    }

    @Override
    public String toString() {
        return toSExpression();
    }
}
