package solver.theory;

import solver.equivalence.EquivalenceClass;

import java.util.Collection;
import java.util.Collections;
import java.util.Optional;

/**
 * Result of a satisfiability check.
 *
 * Contains:
 * - satisfiable: true for SAT, false for UNSAT
 * - witness: Optional collection of equivalence classes (for SAT results)
 * - conflict: Optional explanation for unsatisfiability (for UNSAT results)
 */
public class Result {
    private final boolean satisfiable;
    private final Collection<EquivalenceClass> witness;
    private final String conflict;

    private Result(boolean satisfiable, Collection<EquivalenceClass> witness, String conflict) {
        this.satisfiable = satisfiable;
        this.witness = witness;
        this.conflict = conflict;
    }

    /**
     * Creates a SAT result.
     */
    public static Result sat() {
        return new Result(true, Collections.emptyList(), null);
    }

    /**
     * Creates a SAT result with equivalence class witness.
     */
    public static Result sat(Collection<EquivalenceClass> witness) {
        return new Result(true, witness, null);
    }

    /**
     * Creates an UNSAT result.
     */
    public static Result unsat() {
        return new Result(false, Collections.emptyList(), null);
    }

    /**
     * Creates an UNSAT result with conflict explanation.
     */
    public static Result unsat(String conflict) {
        return new Result(false, Collections.emptyList(), conflict);
    }

    /**
     * Returns true if the result is satisfiable.
     */
    public boolean isSat() {
        return satisfiable;
    }

    /**
     * Returns true if the result is unsatisfiable.
     */
    public boolean isUnsat() {
        return !satisfiable;
    }

    /**
     * Returns the witness (equivalence classes) if available.
     */
    public Optional<Collection<EquivalenceClass>> getWitness() {
        return satisfiable && !witness.isEmpty() ? Optional.of(witness) : Optional.empty();
    }

    /**
     * Returns the conflict explanation if available.
     */
    public Optional<String> getConflict() {
        return conflict != null ? Optional.of(conflict) : Optional.empty();
    }

    @Override
    public String toString() {
        if (satisfiable) {
            StringBuilder sb = new StringBuilder("SAT");
            if (!witness.isEmpty()) {
                sb.append("\n\nEquivalence Classes:");
                for (EquivalenceClass ec : witness) {
                    sb.append("\n  ").append(ec);
                }
            }
            return sb.toString();
        } else {
            StringBuilder sb = new StringBuilder("UNSAT");
            if (conflict != null) {
                sb.append("\n\nConflict: ").append(conflict);
            }
            return sb.toString();
        }
    }
}
