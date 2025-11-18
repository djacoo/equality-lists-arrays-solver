package solver.theory.te;

import solver.core.CongruenceClosure;
import solver.dag.DAG;
import solver.dag.Term;
import solver.theory.Result;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

/**
 * T_E-Procedure: Satisfiability checker for the theory of equality with
 * uninterpreted functions.
 *
 * Given a set of equality and disequality literals, this procedure:
 * 1. Extracts all terms and builds a DAG
 * 2. Runs Congruence Closure on all equality literals
 * 3. Checks if any disequality is violated (both terms in same class)
 * 4. Returns SAT if consistent, UNSAT if violated
 *
 * Algorithm:
 * - Build DAG from all terms in literals
 * - Initialize CongruenceClosure with DAG
 * - Assert all equalities using assertEqual()
 * - For each disequality (a != b):
 *   - If find(a) == find(b), return UNSAT (conflict)
 * - Otherwise return SAT with equivalence classes
 */
public class TEProcedure {
    private final DAG dag;
    private CongruenceClosure cc;

    /**
     * Creates a new T_E-procedure instance.
     */
    public TEProcedure() {
        this.dag = new DAG();
    }

    /**
     * Checks satisfiability of a set of literals.
     *
     * @param literals Collection of equality and disequality literals
     * @return Result indicating SAT or UNSAT with optional witness/conflict
     */
    public Result checkSat(Collection<Literal> literals) {
        // Step 1: Build DAG from all terms in literals
        Set<Term> allTerms = new HashSet<>();
        for (Literal lit : literals) {
            allTerms.add(lit.getLeft());
            allTerms.add(lit.getRight());
        }

        for (Term term : allTerms) {
            dag.addTerm(term);
        }

        // Step 2: Initialize Congruence Closure
        cc = new CongruenceClosure(dag);

        // Step 3: Assert all equalities
        for (Literal lit : literals) {
            if (lit.isEquality()) {
                cc.assertEqual(lit.getLeft(), lit.getRight());
            }
        }

        // Step 4: Check disequalities for conflicts
        for (Literal lit : literals) {
            if (lit.isDisequality()) {
                Term left = lit.getLeft();
                Term right = lit.getRight();

                // If both sides are in the same equivalence class, we have a conflict
                if (cc.areEqual(left, right)) {
                    String conflict = String.format(
                        "Disequality %s != %s violated: both terms are equal by congruence closure",
                        left.getSymbol(),
                        right.getSymbol()
                    );
                    return Result.unsat(conflict);
                }
            }
        }

        // No conflicts found - return SAT with equivalence classes as witness
        return Result.sat(cc.getEquivalenceClasses());
    }

    /**
     * Returns the DAG built from the literals.
     */
    public DAG getDAG() {
        return dag;
    }

    /**
     * Returns the CongruenceClosure instance (available after checkSat).
     */
    public CongruenceClosure getCongruenceClosure() {
        return cc;
    }
}
