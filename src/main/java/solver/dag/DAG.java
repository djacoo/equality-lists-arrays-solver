package solver.dag;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Container for all terms representing the Directed Acyclic Graph.
 *
 * The DAG maintains:
 * - A TermFactory for creating terms with hash-consing
 * - All terms in topological order (leaves first, roots last)
 * - Convenience methods for querying and traversing the DAG
 */
public class DAG {
    // Factory for creating terms with hash-consing
    private final TermFactory factory;

    // All terms in topological order (leaves first, roots last)
    private final List<Term> terms;

    public DAG() {
        this.factory = new TermFactory();
        this.terms = new ArrayList<>();
    }

    /**
     * Returns the term factory for creating new terms.
     */
    public TermFactory getFactory() {
        return factory;
    }

    /**
     * Adds a term to the DAG along with all its subterms.
     * Terms are added in topological order (depth-first).
     */
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

    /**
     * Returns all terms in topological order.
     */
    public List<Term> getTerms() {
        return Collections.unmodifiableList(terms);
    }

    /**
     * Returns all function applications in the DAG.
     */
    public List<FunctionApp> getFunctionApps() {
        return terms.stream()
                    .filter(t -> !t.isLeaf())
                    .map(t -> (FunctionApp) t)
                    .collect(Collectors.toList());
    }

    /**
     * Returns all variables in the DAG.
     */
    public List<Variable> getVariables() {
        return terms.stream()
                    .filter(t -> t instanceof Variable)
                    .map(t -> (Variable) t)
                    .collect(Collectors.toList());
    }

    /**
     * Returns all constants in the DAG.
     */
    public List<Constant> getConstants() {
        return terms.stream()
                    .filter(t -> t instanceof Constant)
                    .map(t -> (Constant) t)
                    .collect(Collectors.toList());
    }

    /**
     * Returns the number of terms in the DAG.
     */
    public int size() {
        return terms.size();
    }

    /**
     * Generates a DOT format representation for visualization.
     * Can be rendered with Graphviz.
     */
    public String toDot() {
        StringBuilder sb = new StringBuilder();
        sb.append("digraph DAG {\n");
        sb.append("  rankdir=BT;\n");  // Bottom to top layout
        sb.append("  node [shape=box];\n");

        for (Term term : terms) {
            // Node label
            String shape = term.isLeaf() ? "ellipse" : "box";
            sb.append(String.format("  t%d [label=\"%s\", shape=%s];\n",
                term.getId(), escapeLabel(term.getSymbol()), shape));

            // Edges from function to arguments
            if (!term.isLeaf()) {
                FunctionApp app = (FunctionApp) term;
                for (int i = 0; i < app.getArguments().size(); i++) {
                    Term arg = app.getArguments().get(i);
                    sb.append(String.format("  t%d -> t%d [label=\"%d\"];\n",
                        term.getId(), arg.getId(), i));
                }
            }
        }

        sb.append("}\n");
        return sb.toString();
    }

    private String escapeLabel(String label) {
        return label.replace("\"", "\\\"");
    }

    @Override
    public String toString() {
        return String.format("DAG[%d terms: %d variables, %d constants, %d functions]",
            terms.size(),
            getVariables().size(),
            getConstants().size(),
            getFunctionApps().size());
    }
}
