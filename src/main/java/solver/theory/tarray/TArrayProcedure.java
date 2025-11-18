package solver.theory.tarray;

import solver.dag.FunctionApp;
import solver.dag.Term;
import solver.dag.TermFactory;
import solver.theory.Result;
import solver.theory.te.Literal;
import solver.theory.tcons.TConsProcedure;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * T_A-procedure: Satisfiability checker for the Theory of Arrays.
 *
 * The Theory of Arrays includes two interpreted function symbols:
 * - select(a, i): Returns the value at index i in array a
 * - store(a, i, v): Returns a new array identical to a except at index i, which has value v
 *
 * The procedure handles array read-over-write axioms:
 * 1. i = j → select(store(a, i, v), j) = v         (read-over-write 1)
 * 2. i ≠ j → select(store(a, i, v), j) = select(a, j)  (read-over-write 2)
 *
 * Algorithm (Bradley & Manna Section 9.5, page 263-264):
 *
 * Step 1: If F contains no write/store terms:
 *   - Replace each read term select(a, i) with f_a(i) (fresh uninterpreted function)
 *   - Delegate to T_E-procedure
 *
 * Step 2: If F contains write/store terms:
 *   - Select some read-over-write term select(store(a, i, v), j)
 *   - Branch on two cases:
 *     Case 1: Replace F[select(store(a,i,v),j)] with F[v] ∧ i = j (axiom 1)
 *     Case 2: Replace F[select(store(a,i,v),j)] with F[select(a,j)] ∧ i ≠ j (axiom 2)
 *   - Recursively check both branches
 *   - Return SAT if any branch is SAT, else UNSAT
 *
 * Reference: Bradley & Manna Section 9.5
 */
public class TArrayProcedure {
    private final TermFactory factory;
    private final TConsProcedure tconsProcedure;

    /**
     * Creates a new T_A-procedure with a fresh term factory.
     */
    public TArrayProcedure() {
        this.factory = new TermFactory();
        this.tconsProcedure = new TConsProcedure(factory);
    }

    /**
     * Creates a new T_A-procedure with a shared term factory.
     *
     * @param factory The term factory to use
     */
    public TArrayProcedure(TermFactory factory) {
        this.factory = factory;
        this.tconsProcedure = new TConsProcedure(factory);
    }

    /**
     * Gets the term factory used by this procedure.
     *
     * @return The term factory
     */
    public TermFactory getFactory() {
        return factory;
    }

    /**
     * Checks satisfiability of a collection of literals in the Theory of Arrays.
     *
     * Implements the Bradley-Manna T_A decision procedure (Section 9.5).
     *
     * @param literals The literals to check
     * @return SAT with witness if satisfiable, UNSAT with conflict otherwise
     */
    public Result check(Collection<Literal> literals) {
        // Step 1: Check if there are any store terms
        Set<FunctionApp> storeTerms = TArraySymbols.extractStoreTerms(literals);

        if (storeTerms.isEmpty()) {
            // No store terms: Step 1 of Bradley-Manna algorithm
            // Replace each select(a, i) with f_a(i) and delegate to T_E
            return checkWithoutStores(literals);
        }

        // Step 2: Store terms exist - apply read-over-write axioms
        // Find a read-over-write term and branch on it
        return applyReadOverWrite(new ArrayList<>(literals));
    }

    /**
     * Step 1 of Bradley-Manna algorithm: No store terms exist.
     *
     * Replace each select(a, i) with f_a(i) (fresh uninterpreted function)
     * and delegate to T_E procedure.
     *
     * Note: For simplicity, we treat select as uninterpreted and delegate to T_cons
     * which in turn delegates to T_E. This is sound because without store terms,
     * the array axioms don't apply.
     *
     * @param literals The literals without store terms
     * @return Result from T_E procedure
     */
    private Result checkWithoutStores(Collection<Literal> literals) {
        // No store terms - select operations are uninterpreted
        // Delegate to T_cons (which handles T_E)
        return tconsProcedure.checkSat(literals);
    }

    /**
     * Step 2 of Bradley-Manna algorithm: Apply read-over-write axioms.
     *
     * Find a read-over-write term select(store(a, i, v), j) and branch:
     * - Case 1: i = j, replace with v
     * - Case 2: i ≠ j, replace with select(a, j)
     *
     * @param literals The current literals (mutable list)
     * @return SAT if any branch is SAT, UNSAT if all branches are UNSAT
     */
    private Result applyReadOverWrite(List<Literal> literals) {
        // Find a read-over-write term: select(store(...), j)
        ReadOverWriteTerm rowTerm = findReadOverWriteTerm(literals);

        if (rowTerm == null) {
            // No more read-over-write terms - delegate to T_cons/T_E
            return tconsProcedure.checkSat(literals);
        }

        // Extract components: select(store(a, i, v), j)
        Term a = rowTerm.baseArray;
        Term i = rowTerm.storeIndex;
        Term v = rowTerm.storedValue;
        Term j = rowTerm.selectIndex;
        FunctionApp selectStoreTerm = rowTerm.selectStoreApp;

        // Branch 1: i = j, replace select(store(a,i,v),j) with v
        List<Literal> branch1 = new ArrayList<>();
        branch1.add(Literal.equality(i, j));
        for (Literal lit : literals) {
            branch1.add(replaceTerm(lit, selectStoreTerm, v));
        }

        Result result1 = applyReadOverWrite(branch1);
        if (result1.isSat()) {
            return result1;  // Found satisfying branch
        }

        // Branch 2: i ≠ j, replace select(store(a,i,v),j) with select(a,j)
        FunctionApp selectA = factory.createFunctionApp("select", a, j);
        List<Literal> branch2 = new ArrayList<>();
        branch2.add(Literal.disequality(i, j));
        for (Literal lit : literals) {
            branch2.add(replaceTerm(lit, selectStoreTerm, selectA));
        }

        Result result2 = applyReadOverWrite(branch2);
        if (result2.isSat()) {
            return result2;  // Found satisfying branch
        }

        // Both branches UNSAT
        return Result.unsat("Both read-over-write branches are UNSAT");
    }

    /**
     * Helper class to hold components of a read-over-write term.
     */
    private static class ReadOverWriteTerm {
        final FunctionApp selectStoreApp;  // The full select(store(...), j) term
        final Term baseArray;               // a in store(a, i, v)
        final Term storeIndex;              // i in store(a, i, v)
        final Term storedValue;             // v in store(a, i, v)
        final Term selectIndex;             // j in select(..., j)

        ReadOverWriteTerm(FunctionApp selectStoreApp, Term baseArray,
                         Term storeIndex, Term storedValue, Term selectIndex) {
            this.selectStoreApp = selectStoreApp;
            this.baseArray = baseArray;
            this.storeIndex = storeIndex;
            this.storedValue = storedValue;
            this.selectIndex = selectIndex;
        }
    }

    /**
     * Finds the first read-over-write term in the literals.
     *
     * A read-over-write term has the form: select(store(a, i, v), j)
     *
     * @param literals The literals to search
     * @return ReadOverWriteTerm if found, null otherwise
     */
    private ReadOverWriteTerm findReadOverWriteTerm(List<Literal> literals) {
        for (Literal lit : literals) {
            ReadOverWriteTerm rowTerm = findReadOverWriteInTerm(lit.getLeft());
            if (rowTerm != null) return rowTerm;

            rowTerm = findReadOverWriteInTerm(lit.getRight());
            if (rowTerm != null) return rowTerm;
        }
        return null;
    }

    /**
     * Recursively searches a term for read-over-write patterns.
     *
     * @param term The term to search
     * @return ReadOverWriteTerm if found, null otherwise
     */
    private ReadOverWriteTerm findReadOverWriteInTerm(Term term) {
        if (TArraySymbols.isSelect(term)) {
            FunctionApp select = (FunctionApp) term;
            Term array = select.getArguments().get(0);
            Term j = select.getArguments().get(1);

            // Check if array is a store term
            if (TArraySymbols.isStore(array)) {
                FunctionApp store = (FunctionApp) array;
                Term a = store.getArguments().get(0);  // base array
                Term i = store.getArguments().get(1);  // store index
                Term v = store.getArguments().get(2);  // stored value

                return new ReadOverWriteTerm(select, a, i, v, j);
            }

            // Recurse into array argument (might be nested select)
            ReadOverWriteTerm result = findReadOverWriteInTerm(array);
            if (result != null) return result;

            // Recurse into index argument
            result = findReadOverWriteInTerm(j);
            if (result != null) return result;
        } else if (!term.isLeaf()) {
            // Recurse into function arguments
            FunctionApp f = (FunctionApp) term;
            for (Term arg : f.getArguments()) {
                ReadOverWriteTerm result = findReadOverWriteInTerm(arg);
                if (result != null) return result;
            }
        }

        return null;
    }

    /**
     * Replaces all occurrences of oldTerm with newTerm in a literal.
     *
     * @param literal The literal to transform
     * @param oldTerm The term to replace
     * @param newTerm The replacement term
     * @return A new literal with replacements applied
     */
    private Literal replaceTerm(Literal literal, Term oldTerm, Term newTerm) {
        Term newLeft = replaceInTerm(literal.getLeft(), oldTerm, newTerm);
        Term newRight = replaceInTerm(literal.getRight(), oldTerm, newTerm);

        if (literal.isEquality()) {
            return Literal.equality(newLeft, newRight);
        } else {
            return Literal.disequality(newLeft, newRight);
        }
    }

    /**
     * Recursively replaces oldTerm with newTerm in a term.
     *
     * @param term The term to transform
     * @param oldTerm The term to replace
     * @param newTerm The replacement term
     * @return The transformed term
     */
    private Term replaceInTerm(Term term, Term oldTerm, Term newTerm) {
        // Check for exact match (using equality based on hash-consing)
        if (term.equals(oldTerm)) {
            return newTerm;
        }

        // If it's a leaf, no replacement needed
        if (term.isLeaf()) {
            return term;
        }

        // Recurse into function arguments
        FunctionApp f = (FunctionApp) term;
        List<Term> newArgs = new ArrayList<>();
        boolean changed = false;

        for (Term arg : f.getArguments()) {
            Term newArg = replaceInTerm(arg, oldTerm, newTerm);
            newArgs.add(newArg);
            if (newArg != arg) {
                changed = true;
            }
        }

        // If any argument changed, create new function application
        if (changed) {
            return factory.createFunctionApp(f.getSymbol(), newArgs);
        }

        return term;
    }
}
