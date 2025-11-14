package solver.core;

import solver.dag.FunctionApp;
import solver.dag.Term;
import solver.equivalence.ClassManager;

import java.util.*;

/**
 * Manages the MERGE procedure for the Congruence Closure algorithm.
 *
 * MERGE is responsible for:
 * 1. Merging two equivalence classes via UNION
 * 2. Propagating congruences to ensure all congruent terms are also merged
 *
 * The pending list tracks pairs of terms that have been merged but whose
 * congruences haven't yet been propagated.
 */
public class MergeManager {
    private final ClassManager classManager;

    // Pending list: pairs of terms waiting for congruence propagation
    private final Queue<TermPair> pending;

    // All terms in the DAG (needed to find congruent terms)
    private final List<Term> allTerms;

    // Statistics
    private int mergeOperations;
    private int congruencePropagations;

    public MergeManager(ClassManager classManager, List<Term> allTerms) {
        this.classManager = classManager;
        this.allTerms = allTerms;
        this.pending = new LinkedList<>();
        this.mergeOperations = 0;
        this.congruencePropagations = 0;
    }

    /**
     * MERGE procedure: merges two terms and propagates all congruences.
     *
     * Algorithm:
     * 1. If t1 and t2 are already in same class, return
     * 2. Add (t1, t2) to pending list
     * 3. While pending is not empty:
     *    a. Remove pair (a, b) from pending
     *    b. UNION(a, b)
     *    c. For each term in ccpar(a) and ccpar(b):
     *       Find congruent terms and merge them
     */
    public void merge(Term t1, Term t2) {
        mergeOperations++;

        // Check if already in same class
        if (classManager.areEqual(t1, t2)) {
            return;
        }

        // Add initial pair to pending
        pending.add(new TermPair(t1, t2));

        // Process pending until empty
        while (!pending.isEmpty()) {
            TermPair pair = pending.poll();
            Term a = pair.first;
            Term b = pair.second;

            // Get representatives before union
            Term repA = classManager.find(a);
            Term repB = classManager.find(b);

            // If already in same class, skip (may happen due to propagation)
            if (repA == repB) {
                continue;
            }

            // UNION the two classes
            classManager.union(a, b);

            // Propagate congruences:
            // For each parent of a and each parent of b,
            // check if they are congruent and merge them if so

            Set<FunctionApp> parentsA = new HashSet<>(repA.getCcpar());
            Set<FunctionApp> parentsB = new HashSet<>(repB.getCcpar());

            for (FunctionApp pa : parentsA) {
                for (FunctionApp pb : parentsB) {
                    if (CongruenceChecker.areCongruent(pa, pb, classManager)) {
                        // Found congruent terms, add to pending for merging
                        if (!classManager.areEqual(pa, pb)) {
                            pending.add(new TermPair(pa, pb));
                            congruencePropagations++;
                        }
                    }
                }
            }
        }
    }

    /**
     * Returns the number of merge operations performed.
     */
    public int getMergeCount() {
        return mergeOperations;
    }

    /**
     * Returns the number of congruence propagations performed.
     */
    public int getPropagationCount() {
        return congruencePropagations;
    }

    /**
     * Helper class to represent a pair of terms.
     */
    private static class TermPair {
        final Term first;
        final Term second;

        TermPair(Term first, Term second) {
            this.first = first;
            this.second = second;
        }

        @Override
        public String toString() {
            return "(" + first.getSymbol() + ", " + second.getSymbol() + ")";
        }
    }
}
