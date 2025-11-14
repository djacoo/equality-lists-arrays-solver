package solver.dag;

import java.util.*;

/**
 * Factory for creating terms with hash-consing (structural sharing).
 *
 * Ensures that structurally identical terms share the same object instance.
 * This provides:
 * - Memory efficiency (each unique term structure exists only once)
 * - Fast equality checking (can use == for structural equality after normalization)
 * - Automatic DAG construction through subterm sharing
 */
public class TermFactory {
    // Maps structural representation to existing Term objects
    private final Map<TermKey, Term> termCache;

    // Counter for assigning unique IDs
    private int nextId;

    public TermFactory() {
        this.termCache = new HashMap<>();
        this.nextId = 1;
    }

    /**
     * Creates or retrieves a variable term.
     * If a variable with the same name already exists, returns that instance.
     */
    public Variable createVariable(String name) {
        TermKey key = new TermKey("Variable", name, Collections.emptyList());
        return (Variable) termCache.computeIfAbsent(key, k ->
            new Variable(nextId++, name)
        );
    }

    /**
     * Creates or retrieves a constant term.
     * If a constant with the same value already exists, returns that instance.
     */
    public Constant createConstant(String value) {
        TermKey key = new TermKey("Constant", value, Collections.emptyList());
        return (Constant) termCache.computeIfAbsent(key, k ->
            new Constant(nextId++, value)
        );
    }

    /**
     * Creates or retrieves a function application.
     * If a function with the same symbol and arguments already exists, returns that instance.
     */
    public FunctionApp createFunctionApp(String symbol, List<Term> arguments) {
        TermKey key = new TermKey("FunctionApp", symbol, arguments);
        return (FunctionApp) termCache.computeIfAbsent(key, k ->
            new FunctionApp(nextId++, symbol, arguments)
        );
    }

    /**
     * Convenience method for binary functions.
     */
    public FunctionApp createFunctionApp(String symbol, Term arg1, Term arg2) {
        return createFunctionApp(symbol, Arrays.asList(arg1, arg2));
    }

    /**
     * Convenience method for unary functions.
     */
    public FunctionApp createFunctionApp(String symbol, Term arg) {
        return createFunctionApp(symbol, Collections.singletonList(arg));
    }

    /**
     * Convenience method for ternary functions.
     */
    public FunctionApp createFunctionApp(String symbol, Term arg1, Term arg2, Term arg3) {
        return createFunctionApp(symbol, Arrays.asList(arg1, arg2, arg3));
    }

    /**
     * Returns the total number of unique terms created.
     */
    public int getTermCount() {
        return termCache.size();
    }

    /**
     * Returns all terms created by this factory.
     */
    public Collection<Term> getAllTerms() {
        return Collections.unmodifiableCollection(termCache.values());
    }

    /**
     * Helper class for cache keys.
     * Represents the structural identity of a term.
     */
    private static class TermKey {
        private final String type;
        private final String symbol;
        private final List<Term> arguments;
        private final int hashCode;

        public TermKey(String type, String symbol, List<Term> arguments) {
            this.type = type;
            this.symbol = symbol;
            this.arguments = new ArrayList<>(arguments);
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
