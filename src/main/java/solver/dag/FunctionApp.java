package solver.dag;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * Represents a function application (internal node) in the DAG.
 *
 * Function applications have a function symbol and zero or more argument terms.
 * Examples: f(x, y), cons(a, b), car(x), select(arr, i)
 *
 * When a FunctionApp is created, it automatically registers itself in the
 * ccpar sets of all its argument terms.
 */
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
    protected int computeHashCode() {
        return Objects.hash("FunctionApp", getSymbol(), arguments);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof FunctionApp)) return false;
        FunctionApp other = (FunctionApp) obj;
        return getSymbol().equals(other.getSymbol()) &&
               arguments.equals(other.arguments);
    }

    /**
     * Returns the arity (number of arguments) of this function application.
     */
    public int getArity() {
        return arguments.size();
    }
}
