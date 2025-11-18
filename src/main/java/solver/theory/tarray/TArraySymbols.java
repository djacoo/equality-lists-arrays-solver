package solver.theory.tarray;

import solver.dag.FunctionApp;
import solver.dag.Term;
import solver.theory.te.Literal;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

/**
 * Utility class for recognizing T_A (Theory of Arrays) symbols.
 *
 * BRADLEY & MANNA NOTATION (Section 9.5, page 263):
 * ================================================================
 * The signature of T_A is: Σ_A : {·[·], ·⟨· ⊳ ·⟩, =}
 *
 * Mathematical Notation → Implementation:
 * - a[i]         ↔  select(a, i)     [read/select operation]
 * - a⟨i ⊳ v⟩     ↔  store(a, i, v)   [write/store operation]
 *
 * AXIOMS (Bradley & Manna Section 9.5):
 * - Axiom 2 (array congruence): ∀a,i,j. i = j → a[i] = a[j]
 * - Axiom 3 (read-over-write 1): ∀a,v,i,j. i = j → a⟨i ⊳ v⟩[j] = v
 * - Axiom 4 (read-over-write 2): ∀a,v,i,j. i ≠ j → a⟨i ⊳ v⟩[j] = a[j]
 *
 * Note: We use "select" and "store" as ASCII representations of the
 * mathematical symbols ·[·] and ·⟨· ⊳ ·⟩ for implementation purposes.
 * This is standard practice (McCarthy 1962) and maintains compatibility
 * with SMT-LIB notation.
 */
public class TArraySymbols {
    // Symbol names for T_A
    // B&M notation: ·[·] (read operation)
    public static final String SELECT = "select";
    // B&M notation: ·⟨· ⊳ ·⟩ (write operation)
    public static final String STORE = "store";

    /**
     * Checks if a term is a select function application.
     *
     * @param term The term to check
     * @return true if term is select(a, i)
     */
    public static boolean isSelect(Term term) {
        if (term == null || term.isLeaf()) {
            return false;
        }
        FunctionApp f = (FunctionApp) term;
        return f.getSymbol().equals(SELECT) && f.getArity() == 2;
    }

    /**
     * Checks if a term is a store function application.
     *
     * @param term The term to check
     * @return true if term is store(a, i, v)
     */
    public static boolean isStore(Term term) {
        if (term == null || term.isLeaf()) {
            return false;
        }
        FunctionApp f = (FunctionApp) term;
        return f.getSymbol().equals(STORE) && f.getArity() == 3;
    }

    /**
     * Checks if a term is any T_A symbol (select or store).
     *
     * @param term The term to check
     * @return true if term is a T_A function application
     */
    public static boolean isTArraySymbol(Term term) {
        return isSelect(term) || isStore(term);
    }

    /**
     * Checks if a literal contains any T_A symbols.
     *
     * @param literal The literal to check
     * @return true if literal contains select or store
     */
    public static boolean containsTArraySymbols(Literal literal) {
        return containsTArraySymbol(literal.getLeft()) ||
               containsTArraySymbol(literal.getRight());
    }

    /**
     * Recursively checks if a term or any of its subterms contains T_A symbols.
     *
     * @param term The term to check
     * @return true if term or any subterm is a T_A symbol
     */
    private static boolean containsTArraySymbol(Term term) {
        if (term == null) {
            return false;
        }

        if (isTArraySymbol(term)) {
            return true;
        }

        if (!term.isLeaf()) {
            FunctionApp f = (FunctionApp) term;
            for (Term arg : f.getArguments()) {
                if (containsTArraySymbol(arg)) {
                    return true;
                }
            }
        }

        return false;
    }

    /**
     * Checks if a collection of literals contains any T_A symbols.
     *
     * @param literals The literals to check
     * @return true if any literal contains T_A symbols
     */
    public static boolean containsTArraySymbols(Collection<Literal> literals) {
        for (Literal lit : literals) {
            if (containsTArraySymbols(lit)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Extracts all select function applications from a collection of literals.
     *
     * @param literals The literals to scan
     * @return Set of all select(a, i) terms found
     */
    public static Set<FunctionApp> extractSelectTerms(Collection<Literal> literals) {
        Set<FunctionApp> selectTerms = new HashSet<>();

        for (Literal lit : literals) {
            extractSelectTermsFromTerm(lit.getLeft(), selectTerms);
            extractSelectTermsFromTerm(lit.getRight(), selectTerms);
        }

        return selectTerms;
    }

    /**
     * Recursively extracts select terms from a term.
     */
    private static void extractSelectTermsFromTerm(Term term, Set<FunctionApp> selectTerms) {
        if (term == null) {
            return;
        }

        if (isSelect(term)) {
            selectTerms.add((FunctionApp) term);
        }

        if (!term.isLeaf()) {
            FunctionApp f = (FunctionApp) term;
            for (Term arg : f.getArguments()) {
                extractSelectTermsFromTerm(arg, selectTerms);
            }
        }
    }

    /**
     * Extracts all store function applications from a collection of literals.
     *
     * @param literals The literals to scan
     * @return Set of all store(a, i, v) terms found
     */
    public static Set<FunctionApp> extractStoreTerms(Collection<Literal> literals) {
        Set<FunctionApp> storeTerms = new HashSet<>();

        for (Literal lit : literals) {
            extractStoreTermsFromTerm(lit.getLeft(), storeTerms);
            extractStoreTermsFromTerm(lit.getRight(), storeTerms);
        }

        return storeTerms;
    }

    /**
     * Recursively extracts store terms from a term.
     */
    private static void extractStoreTermsFromTerm(Term term, Set<FunctionApp> storeTerms) {
        if (term == null) {
            return;
        }

        if (isStore(term)) {
            storeTerms.add((FunctionApp) term);
        }

        if (!term.isLeaf()) {
            FunctionApp f = (FunctionApp) term;
            for (Term arg : f.getArguments()) {
                extractStoreTermsFromTerm(arg, storeTerms);
            }
        }
    }
}
