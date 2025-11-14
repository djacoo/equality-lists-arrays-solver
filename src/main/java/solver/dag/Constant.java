package solver.dag;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * Represents a constant term (leaf node) in the DAG.
 *
 * Constants are atomic terms with no subterms.
 * Examples: 0, 1, true, false, nil
 *
 * Note: The distinction between Variable and Constant is semantic rather than
 * syntactic - both are represented as identifiers in the input.
 */
public class Constant extends Term {

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
    protected int computeHashCode() {
        return Objects.hash("Constant", getSymbol());
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof Constant)) return false;
        Constant other = (Constant) obj;
        return getSymbol().equals(other.getSymbol());
    }
}
