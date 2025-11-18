package solver;

import solver.dag.TermFactory;
import solver.theory.Result;
import solver.theory.te.Literal;
import solver.theory.tarray.TArrayProcedure;
import solver.theory.tarray.TArraySymbols;
import solver.theory.tcons.TConsProcedure;
import solver.theory.tcons.TConsSymbols;
import solver.theory.te.TEProcedure;

import java.util.Collection;

/**
 * Unified solver for the union of theories T_E, T_cons, and T_A.
 *
 * This is the main entry point for checking satisfiability of literal sets
 * that may contain symbols from multiple theories:
 * - T_E: Theory of Equality (congruence closure)
 * - T_cons: Theory of Lists (cons, car, cdr)
 * - T_A: Theory of Arrays (select, store)
 *
 * The solver automatically detects which theories are present in the input
 * and routes to the appropriate procedure.
 *
 * Algorithm:
 * 1. Detect which theories are present based on symbols in literals
 * 2. Route to the most specific procedure:
 *    - If T_A symbols (store/select) → TArrayProcedure (handles all theories)
 *    - Else if T_cons symbols (cons/car/cdr) → TConsProcedure (handles T_cons + T_E)
 *    - Else → TEProcedure (handles pure equality)
 * 3. Return SAT/UNSAT result
 *
 * Note: TArrayProcedure internally delegates to TConsProcedure, which in turn
 * delegates to TEProcedure, so the routing is hierarchical.
 */
public class UnifiedSolver {
    private final TermFactory factory;
    private final TEProcedure teProcedure;
    private final TConsProcedure tconsProcedure;
    private final TArrayProcedure tarrayProcedure;

    /**
     * Creates a new unified solver with a fresh term factory.
     */
    public UnifiedSolver() {
        this.factory = new TermFactory();
        this.teProcedure = new TEProcedure();
        this.tconsProcedure = new TConsProcedure(factory);
        this.tarrayProcedure = new TArrayProcedure(factory);
    }

    /**
     * Creates a new unified solver with a shared term factory.
     *
     * @param factory The term factory to use
     */
    public UnifiedSolver(TermFactory factory) {
        this.factory = factory;
        this.teProcedure = new TEProcedure();
        this.tconsProcedure = new TConsProcedure(factory);
        this.tarrayProcedure = new TArrayProcedure(factory);
    }

    /**
     * Gets the term factory used by this solver.
     *
     * @return The term factory
     */
    public TermFactory getFactory() {
        return factory;
    }

    /**
     * Checks satisfiability of a collection of literals.
     *
     * Automatically detects which theories are present and routes to the
     * appropriate procedure.
     *
     * @param literals The literals to check
     * @return SAT with witness if satisfiable, UNSAT with conflict otherwise
     */
    public Result checkSat(Collection<Literal> literals) {
        // Detect which theories are present
        boolean hasArraySymbols = TArraySymbols.containsTArraySymbols(literals);
        boolean hasListSymbols = TConsSymbols.containsTConsSymbols(literals);

        // Route to appropriate procedure
        // TArrayProcedure handles all theories, so use it if array symbols present
        if (hasArraySymbols) {
            return tarrayProcedure.check(literals);
        }
        // TConsProcedure handles lists + equality
        else if (hasListSymbols) {
            return tconsProcedure.checkSat(literals);
        }
        // Pure equality
        else {
            return teProcedure.checkSat(literals);
        }
    }

    /**
     * Checks satisfiability and returns a simple boolean result.
     *
     * @param literals The literals to check
     * @return true if satisfiable, false if unsatisfiable
     */
    public boolean isSatisfiable(Collection<Literal> literals) {
        return checkSat(literals).isSat();
    }
}
