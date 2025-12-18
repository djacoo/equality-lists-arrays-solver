package solver.core;

import solver.dag.FunctionApp;
import solver.dag.Term;
import solver.equivalence.ClassManager;

/**
 * Utility for checking if two terms are congruent.
 *
 * Two function applications f(t1, t2, ..., tn) and g(s1, s2, ..., sm) are congruent if:
 * 1. f and g are the same function symbol
 * 2. n = m (same arity)
 * 3. For all i, FIND(ti) = FIND(si) (corresponding arguments are in same equivalence class)
 */
public class CongruenceChecker {

    /**
     * Checks if two terms are congruent.
     *
     * @param t1 First term
     * @param t2 Second term
     * @param classManager ClassManager for FIND operations
     * @return true if the terms are congruent, false otherwise
     */
    public static boolean areCongruent(Term t1, Term t2, ClassManager classManager) {
        // OPTIMIZATION Phase 5.3: Check identity first (fast path)
        if (t1 == t2) {
            return true;
        }

        // Terms must both be function applications
        if (t1.isLeaf() || t2.isLeaf()) {
            return false;
        }

        FunctionApp f1 = (FunctionApp) t1;
        FunctionApp f2 = (FunctionApp) t2;

        // Must have the same function symbol
        if (!f1.getSymbol().equals(f2.getSymbol())) {
            return false;
        }

        // Must have the same arity
        if (f1.getArity() != f2.getArity()) {
            return false;
        }

        // All corresponding arguments must be in the same equivalence classes
        for (int i = 0; i < f1.getArity(); i++) {
            Term arg1 = f1.getArguments().get(i);
            Term arg2 = f2.getArguments().get(i);

            // Use FIND to get the representatives
            Term rep1 = classManager.find(arg1);
            Term rep2 = classManager.find(arg2);

            if (rep1 != rep2) {
                return false;
            }
        }

        return true;
    }

    /**
     * Checks if a term is congruent to any term in the given iterable.
     *
     * @param term Term to check
     * @param candidates Candidate terms to check against
     * @param classManager ClassManager for FIND operations
     * @return The first congruent term found, or null if none are congruent
     */
    public static Term findCongruentTerm(Term term, Iterable<Term> candidates, ClassManager classManager) {
        if (term.isLeaf()) {
            return null;
        }

        for (Term candidate : candidates) {
            if (areCongruent(term, candidate, classManager)) {
                return candidate;
            }
        }

        return null;
    }
}
