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
 * 1. select(store(a, i, v), i) = v (reading the stored index gives the stored value)
 * 2. i ≠ j → select(store(a, i, v), j) = select(a, j) (reading different index gives original value)
 *
 * Algorithm:
 * 1. If no store symbols: delegate to TConsProcedure/TEProcedure
 * 2. If store symbols exist: decompose into subproblems using read-over-write axioms
 * 3. For each store term, create two subproblems (one assuming i=j, one assuming i≠j)
 * 4. Check each subproblem recursively
 * 5. Return SAT if any subproblem is SAT, otherwise UNSAT
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
     * @param literals The literals to check
     * @return SAT with witness if satisfiable, UNSAT with conflict otherwise
     */
    public Result check(Collection<Literal> literals) {
        // Extract store terms from literals
        Set<FunctionApp> storeTerms = TArraySymbols.extractStoreTerms(literals);

        // If no store terms, delegate to T_cons/T_E procedure
        if (storeTerms.isEmpty()) {
            return tconsProcedure.checkSat(literals);
        }

        // For each store term, we need to decompose into subproblems
        // We'll use a simpler approach: try all possible assignments
        return decomposeStores(literals, new ArrayList<>(storeTerms), 0);
    }

    /**
     * Recursively decomposes store terms into subproblems.
     *
     * For each store(a, i, v), we create subproblems by considering all select operations
     * that might read from this store.
     *
     * @param originalLiterals The original literals
     * @param storeTerms List of all store terms
     * @param index Current index in storeTerms list
     * @return SAT if any subproblem is SAT, UNSAT otherwise
     */
    private Result decomposeStores(Collection<Literal> originalLiterals,
                                   List<FunctionApp> storeTerms,
                                   int index) {
        // Base case: all stores processed, delegate to T_cons/T_E
        if (index >= storeTerms.size()) {
            return tconsProcedure.checkSat(originalLiterals);
        }

        // Get current store term: store(a, i, v)
        FunctionApp store = storeTerms.get(index);
        Term a = store.getArguments().get(0);  // base array
        Term i = store.getArguments().get(1);  // index
        Term v = store.getArguments().get(2);  // value

        // Find all select operations on this store
        Set<Term> selectIndices = findSelectIndices(originalLiterals, store);

        // If no selects on this store, just continue to next store
        if (selectIndices.isEmpty()) {
            return decomposeStores(originalLiterals, storeTerms, index + 1);
        }

        // For each select index j, create two subproblems:
        // 1. i = j ∧ select(store(a,i,v), j) = v
        // 2. i ≠ j ∧ select(store(a,i,v), j) = select(a, j)
        for (Term j : selectIndices) {
            // Subproblem 1: i = j case
            List<Literal> subproblem1 = new ArrayList<>(originalLiterals);
            subproblem1.add(Literal.equality(i, j));
            FunctionApp selectStore = factory.createFunctionApp("select", store, j);
            subproblem1.add(Literal.equality(selectStore, v));

            Result result1 = decomposeStores(subproblem1, storeTerms, index + 1);
            if (result1.isSat()) {
                return result1;
            }

            // Subproblem 2: i ≠ j case
            List<Literal> subproblem2 = new ArrayList<>(originalLiterals);
            subproblem2.add(Literal.disequality(i, j));
            FunctionApp selectA = factory.createFunctionApp("select", a, j);
            subproblem2.add(Literal.equality(selectStore, selectA));

            Result result2 = decomposeStores(subproblem2, storeTerms, index + 1);
            if (result2.isSat()) {
                return result2;
            }
        }

        // If we get here, both subproblems are UNSAT
        return Result.unsat();
    }

    /**
     * Finds all indices j where select(..., j) is applied to a store term.
     *
     * @param literals The literals to search
     * @param storeTerm The store term to find selects for
     * @return Set of index terms used in select operations
     */
    private Set<Term> findSelectIndices(Collection<Literal> literals, FunctionApp storeTerm) {
        Set<Term> indices = new HashSet<>();

        for (Literal lit : literals) {
            findSelectIndicesInTerm(lit.getLeft(), storeTerm, indices);
            findSelectIndicesInTerm(lit.getRight(), storeTerm, indices);
        }

        return indices;
    }

    /**
     * Recursively finds select indices in a term.
     */
    private void findSelectIndicesInTerm(Term term, FunctionApp storeTerm, Set<Term> indices) {
        if (TArraySymbols.isSelect(term)) {
            FunctionApp select = (FunctionApp) term;
            Term array = select.getArguments().get(0);
            Term index = select.getArguments().get(1);

            // Check if this select is on our store term
            if (array.equals(storeTerm)) {
                indices.add(index);
            }

            // Also recurse into arguments
            for (Term arg : select.getArguments()) {
                findSelectIndicesInTerm(arg, storeTerm, indices);
            }
        } else if (!term.isLeaf()) {
            FunctionApp f = (FunctionApp) term;
            for (Term arg : f.getArguments()) {
                findSelectIndicesInTerm(arg, storeTerm, indices);
            }
        }
    }
}
