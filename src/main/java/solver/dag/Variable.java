package solver.dag;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * Represents a variable term (leaf node) in the DAG.
 *
 * Variables are atomic terms with no subterms.
 * Examples: x, y, a, b, myVar
 */
public class Variable extends Term {

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
    protected int computeHashCode() {
        return Objects.hash("Variable", getSymbol());
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof Variable)) return false;
        Variable other = (Variable) obj;
        return getSymbol().equals(other.getSymbol());
    }
}
