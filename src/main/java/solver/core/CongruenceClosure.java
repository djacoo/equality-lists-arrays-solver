package solver.core;

import solver.dag.DAG;
import solver.dag.Term;
import solver.equivalence.ClassManager;
import solver.equivalence.EquivalenceClass;

import java.util.Collection;

/**
 * Main Congruence Closure algorithm implementation.
 *
 * The Congruence Closure (CC) algorithm decides satisfiability for the theory
 * of equality (T_E). Given a DAG of terms and a set of equality constraints,
 * it builds the congruence closure by:
 *
 * 1. Initializing each term into its own equivalence class
 * 2. Merging classes for each asserted equality using MERGE
 * 3. Automatically propagating congruences via the pending list in MERGE
 *
 * After processing all equalities, terms are congruent if and only if they
 * are in the same equivalence class.
 */
public class CongruenceClosure {
    private final DAG dag;
    private final ClassManager classManager;
    private final MergeManager mergeManager;

    // Statistics
    private int equalitiesProcessed;

    /**
     * Creates a CongruenceClosure instance for the given DAG.
     *
     * @param dag The DAG containing all terms
     */
    public CongruenceClosure(DAG dag) {
        this.dag = dag;
        this.classManager = new ClassManager();
        this.mergeManager = new MergeManager(classManager, dag.getTerms());
        this.equalitiesProcessed = 0;

        // Initialize all terms into singleton equivalence classes
        classManager.initializeAll(dag.getTerms());
    }

    /**
     * Asserts that two terms are equal.
     * This triggers the MERGE procedure to merge their equivalence classes
     * and propagate all congruences.
     *
     * @param t1 First term
     * @param t2 Second term
     */
    public void assertEqual(Term t1, Term t2) {
        equalitiesProcessed++;
        mergeManager.merge(t1, t2);
    }

    /**
     * Asserts multiple equalities at once.
     *
     * @param equalities Collection of term pairs to assert equal
     */
    public void assertEqualities(Collection<TermPair> equalities) {
        for (TermPair pair : equalities) {
            assertEqual(pair.first, pair.second);
        }
    }

    /**
     * Checks if two terms are in the same equivalence class.
     *
     * @param t1 First term
     * @param t2 Second term
     * @return true if t1 and t2 are congruent (in same class)
     */
    public boolean areEqual(Term t1, Term t2) {
        return classManager.areEqual(t1, t2);
    }

    /**
     * Returns the representative (canonical element) of a term's equivalence class.
     *
     * @param t The term
     * @return The representative of t's class
     */
    public Term find(Term t) {
        return classManager.find(t);
    }

    /**
     * Returns all equivalence classes after congruence closure.
     *
     * @return Collection of equivalence classes
     */
    public Collection<EquivalenceClass> getEquivalenceClasses() {
        return classManager.getAllClasses();
    }

    /**
     * Returns the number of equivalence classes.
     *
     * @return Number of distinct equivalence classes
     */
    public int getClassCount() {
        return classManager.getClassCount();
    }

    /**
     * Returns the DAG used by this congruence closure instance.
     *
     * @return The DAG
     */
    public DAG getDAG() {
        return dag;
    }

    /**
     * Returns the class manager used by this instance.
     *
     * @return The ClassManager
     */
    public ClassManager getClassManager() {
        return classManager;
    }

    // Statistics methods

    /**
     * Returns the number of equality assertions processed.
     */
    public int getEqualitiesProcessed() {
        return equalitiesProcessed;
    }

    /**
     * Returns the total number of merge operations performed.
     */
    public int getMergeCount() {
        return mergeManager.getMergeCount();
    }

    /**
     * Returns the number of congruence propagations performed.
     */
    public int getPropagationCount() {
        return mergeManager.getPropagationCount();
    }

    /**
     * Returns a summary string with algorithm statistics.
     */
    public String getStatistics() {
        return String.format(
            "CongruenceClosure Statistics:\n" +
            "  Terms: %d\n" +
            "  Equalities asserted: %d\n" +
            "  Merge operations: %d\n" +
            "  Congruence propagations: %d\n" +
            "  Final equivalence classes: %d",
            dag.size(),
            equalitiesProcessed,
            getMergeCount(),
            getPropagationCount(),
            getClassCount()
        );
    }

    /**
     * Helper class to represent a pair of terms (an equality constraint).
     */
    public static class TermPair {
        public final Term first;
        public final Term second;

        public TermPair(Term first, Term second) {
            this.first = first;
            this.second = second;
        }

        @Override
        public String toString() {
            return first.getSymbol() + " = " + second.getSymbol();
        }

        @Override
        public boolean equals(Object obj) {
            if (!(obj instanceof TermPair)) return false;
            TermPair other = (TermPair) obj;
            return (first.equals(other.first) && second.equals(other.second)) ||
                   (first.equals(other.second) && second.equals(other.first));
        }

        @Override
        public int hashCode() {
            // Symmetric hash code (order doesn't matter)
            return first.hashCode() + second.hashCode();
        }
    }
}
