package solver.equivalence;

import solver.dag.Term;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

/**
 * Forbidden Set: maintains pairs of terms that must NOT be merged.
 *
 * This optimization enables early UNSAT detection:
 * - When a disequality (t1 != t2) is asserted, add (t1, t2) to the forbidden set
 * - Before performing UNION(t1, t2), check if the pair is forbidden
 * - If yes, return immediate UNSAT (conflict detected)
 * - If no, proceed with the merge
 *
 * Benefits:
 * - Early UNSAT detection: fail as soon as a forbidden merge is attempted
 * - Better diagnostics: know exactly which merge caused the conflict
 * - Performance: avoid unnecessary work on UNSAT instances
 *
 * Reference: Detlef et al. [7] page 388 (bottom)
 */
public class ForbiddenSet {
    // Set of term pairs that must remain in different equivalence classes
    private final Set<TermPair> forbidden;

    // Statistics
    private int forbiddenPairsAdded;
    private int forbiddenChecks;

    public ForbiddenSet() {
        this.forbidden = new HashSet<>();
        this.forbiddenPairsAdded = 0;
        this.forbiddenChecks = 0;
    }

    /**
     * Adds a forbidden pair from a disequality (t1 != t2).
     * This indicates that t1 and t2 must never be in the same equivalence class.
     *
     * @param t1 First term
     * @param t2 Second term
     */
    public void addForbiddenPair(Term t1, Term t2) {
        forbidden.add(new TermPair(t1, t2));
        forbiddenPairsAdded++;
    }

    /**
     * Checks if merging t1 and t2 is forbidden.
     * Should be called before UNION to detect conflicts early.
     *
     * @param t1 First term (representative)
     * @param t2 Second term (representative)
     * @return true if merging these terms would violate a disequality
     */
    public boolean isForbidden(Term t1, Term t2) {
        forbiddenChecks++;
        return forbidden.contains(new TermPair(t1, t2));
    }

    /**
     * Returns the number of forbidden pairs currently in the set.
     */
    public int size() {
        return forbidden.size();
    }

    /**
     * Returns the number of forbidden pairs added (including duplicates).
     */
    public int getForbiddenPairsAdded() {
        return forbiddenPairsAdded;
    }

    /**
     * Returns the number of times isForbidden() was called.
     */
    public int getForbiddenChecks() {
        return forbiddenChecks;
    }

    /**
     * Clears all forbidden pairs from the set.
     */
    public void clear() {
        forbidden.clear();
    }

    @Override
    public String toString() {
        return String.format("ForbiddenSet[size=%d, checks=%d]", forbidden.size(), forbiddenChecks);
    }

    /**
     * Represents a pair of terms with canonical ordering.
     * The pair (t1, t2) is equivalent to (t2, t1).
     * Uses term IDs for canonical ordering to ensure consistent hashing.
     */
    public static class TermPair {
        private final Term first;
        private final Term second;

        public TermPair(Term t1, Term t2) {
            // Canonical ordering: smaller ID first for consistent hashing
            if (t1.getId() <= t2.getId()) {
                this.first = t1;
                this.second = t2;
            } else {
                this.first = t2;
                this.second = t1;
            }
        }

        public Term getFirst() {
            return first;
        }

        public Term getSecond() {
            return second;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) return true;
            if (!(obj instanceof TermPair)) return false;
            TermPair other = (TermPair) obj;
            return first.equals(other.first) && second.equals(other.second);
        }

        @Override
        public int hashCode() {
            return Objects.hash(first, second);
        }

        @Override
        public String toString() {
            return String.format("(%s, %s)", first.getSymbol(), second.getSymbol());
        }
    }
}
