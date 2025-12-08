package solver.core;

import solver.config.SolverConfig;
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
 *
 * Optional optimizations:
 * - Forbidden set: Early UNSAT detection for disequality constraints
 */
public class CongruenceClosure {
    private final DAG dag;
    private final ClassManager classManager;
    private final MergeManager mergeManager;
    private final SolverConfig config;

    // Statistics
    private int equalitiesProcessed;
    private int disequalitiesRegistered;

    /**
     * Creates a CongruenceClosure instance for the given DAG with default configuration.
     *
     * @param dag The DAG containing all terms
     */
    public CongruenceClosure(DAG dag) {
        this(dag, new SolverConfig());
    }

    /**
     * Creates a CongruenceClosure instance for the given DAG with specified configuration.
     *
     * @param dag The DAG containing all terms
     * @param config Solver configuration specifying which optimizations to enable
     */
    public CongruenceClosure(DAG dag, SolverConfig config) {
        this.dag = dag;
        this.config = config;
        this.classManager = new ClassManager(config);
        this.mergeManager = new MergeManager(classManager, dag.getTerms());
        this.equalitiesProcessed = 0;
        this.disequalitiesRegistered = 0;

        // Initialize all terms into singleton equivalence classes
        classManager.initializeAll(dag.getTerms());
    }

    /**
     * Registers a disequality constraint (t1 != t2) with the forbidden set.
     * This should be called for all disequalities before asserting any equalities
     * (if forbidden set optimization is enabled).
     *
     * @param t1 First term
     * @param t2 Second term
     */
    public void assertDisequality(Term t1, Term t2) {
        disequalitiesRegistered++;
        classManager.addDisequality(t1, t2);
    }

    /**
     * Registers multiple disequalities at once.
     *
     * @param disequalities Collection of term pairs representing disequalities
     */
    public void assertDisequalities(Collection<TermPair> disequalities) {
        for (TermPair pair : disequalities) {
            assertDisequality(pair.first, pair.second);
        }
    }

    /**
     * Asserts that two terms are equal.
     * This triggers the MERGE procedure to merge their equivalence classes
     * and propagate all congruences.
     *
     * If forbidden set is enabled and a forbidden merge is detected, returns false.
     *
     * @param t1 First term
     * @param t2 Second term
     * @return true if merge succeeded, false if a forbidden merge was detected (UNSAT)
     */
    public boolean assertEqual(Term t1, Term t2) {
        equalitiesProcessed++;
        return mergeManager.merge(t1, t2);
    }

    /**
     * Asserts multiple equalities at once.
     *
     * @param equalities Collection of term pairs to assert equal
     * @return true if all merges succeeded, false if any forbidden merge was detected (UNSAT)
     */
    public boolean assertEqualities(Collection<TermPair> equalities) {
        for (TermPair pair : equalities) {
            boolean success = assertEqual(pair.first, pair.second);
            if (!success) {
                return false;  // Early UNSAT detection
            }
        }
        return true;
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

    /**
     * Returns the configuration used by this instance.
     *
     * @return The SolverConfig
     */
    public SolverConfig getConfig() {
        return config;
    }

    // Statistics methods

    /**
     * Returns the number of equality assertions processed.
     */
    public int getEqualitiesProcessed() {
        return equalitiesProcessed;
    }

    /**
     * Returns the number of disequalities registered with forbidden set.
     */
    public int getDisequalitiesRegistered() {
        return disequalitiesRegistered;
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
        StringBuilder stats = new StringBuilder();
        stats.append("CongruenceClosure Statistics:\n");
        stats.append(String.format("  Configuration: %s\n", config.getDescription()));
        stats.append(String.format("  Terms: %d\n", dag.size()));
        stats.append(String.format("  Equalities asserted: %d\n", equalitiesProcessed));
        stats.append(String.format("  Merge operations: %d\n", getMergeCount()));
        stats.append(String.format("  Congruence propagations: %d\n", getPropagationCount()));
        stats.append(String.format("  Final equivalence classes: %d\n", getClassCount()));

        // Add forbidden set statistics if enabled
        if (config.isUseForbiddenSet()) {
            stats.append(String.format("  Disequalities registered: %d\n", disequalitiesRegistered));
            stats.append(String.format("  Forbidden merge attempts: %d\n",
                classManager.getForbiddenMergeAttempts()));
        }

        return stats.toString();
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
