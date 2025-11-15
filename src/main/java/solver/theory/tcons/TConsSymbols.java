package solver.theory.tcons;

import solver.dag.FunctionApp;
import solver.dag.Term;
import solver.theory.te.Literal;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

/**
 * Utility class for recognizing T_cons (Theory of Lists) symbols.
 *
 * T_cons has three interpreted function symbols:
 * - cons(x, y): List constructor with head x and tail y
 * - car(x): Returns the head of list x
 * - cdr(x): Returns the tail of list x
 */
public class TConsSymbols {
    // Symbol names for T_cons
    public static final String CONS = "cons";
    public static final String CAR = "car";
    public static final String CDR = "cdr";

    /**
     * Checks if a term is a cons function application.
     *
     * @param term The term to check
     * @return true if term is cons(x, y)
     */
    public static boolean isCons(Term term) {
        if (term.isLeaf()) {
            return false;
        }
        FunctionApp f = (FunctionApp) term;
        return f.getSymbol().equals(CONS) && f.getArity() == 2;
    }

    /**
     * Checks if a term is a car function application.
     *
     * @param term The term to check
     * @return true if term is car(x)
     */
    public static boolean isCar(Term term) {
        if (term.isLeaf()) {
            return false;
        }
        FunctionApp f = (FunctionApp) term;
        return f.getSymbol().equals(CAR) && f.getArity() == 1;
    }

    /**
     * Checks if a term is a cdr function application.
     *
     * @param term The term to check
     * @return true if term is cdr(x)
     */
    public static boolean isCdr(Term term) {
        if (term.isLeaf()) {
            return false;
        }
        FunctionApp f = (FunctionApp) term;
        return f.getSymbol().equals(CDR) && f.getArity() == 1;
    }

    /**
     * Checks if a term is any T_cons symbol (cons, car, or cdr).
     *
     * @param term The term to check
     * @return true if term is a T_cons function application
     */
    public static boolean isTConsSymbol(Term term) {
        return isCons(term) || isCar(term) || isCdr(term);
    }

    /**
     * Checks if a literal contains any T_cons symbols.
     *
     * @param literal The literal to check
     * @return true if literal contains cons, car, or cdr
     */
    public static boolean containsTConsSymbols(Literal literal) {
        return containsTConsSymbol(literal.getLeft()) ||
               containsTConsSymbol(literal.getRight());
    }

    /**
     * Recursively checks if a term or any of its subterms contains T_cons symbols.
     *
     * @param term The term to check
     * @return true if term or any subterm is a T_cons symbol
     */
    private static boolean containsTConsSymbol(Term term) {
        if (isTConsSymbol(term)) {
            return true;
        }

        if (!term.isLeaf()) {
            FunctionApp f = (FunctionApp) term;
            for (Term arg : f.getArguments()) {
                if (containsTConsSymbol(arg)) {
                    return true;
                }
            }
        }

        return false;
    }

    /**
     * Checks if a collection of literals contains any T_cons symbols.
     *
     * @param literals The literals to check
     * @return true if any literal contains T_cons symbols
     */
    public static boolean containsTConsSymbols(Collection<Literal> literals) {
        for (Literal lit : literals) {
            if (containsTConsSymbols(lit)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Extracts all cons function applications from a collection of literals.
     *
     * @param literals The literals to scan
     * @return Set of all cons(x, y) terms found
     */
    public static Set<FunctionApp> extractConsTerms(Collection<Literal> literals) {
        Set<FunctionApp> consTerms = new HashSet<>();

        for (Literal lit : literals) {
            extractConsTermsFromTerm(lit.getLeft(), consTerms);
            extractConsTermsFromTerm(lit.getRight(), consTerms);
        }

        return consTerms;
    }

    /**
     * Recursively extracts cons terms from a term.
     */
    private static void extractConsTermsFromTerm(Term term, Set<FunctionApp> consTerms) {
        if (isCons(term)) {
            consTerms.add((FunctionApp) term);
        }

        if (!term.isLeaf()) {
            FunctionApp f = (FunctionApp) term;
            for (Term arg : f.getArguments()) {
                extractConsTermsFromTerm(arg, consTerms);
            }
        }
    }

    /**
     * Extracts all car function applications from a collection of literals.
     *
     * @param literals The literals to scan
     * @return Set of all car(x) terms found
     */
    public static Set<FunctionApp> extractCarTerms(Collection<Literal> literals) {
        Set<FunctionApp> carTerms = new HashSet<>();

        for (Literal lit : literals) {
            extractCarTermsFromTerm(lit.getLeft(), carTerms);
            extractCarTermsFromTerm(lit.getRight(), carTerms);
        }

        return carTerms;
    }

    /**
     * Recursively extracts car terms from a term.
     */
    private static void extractCarTermsFromTerm(Term term, Set<FunctionApp> carTerms) {
        if (isCar(term)) {
            carTerms.add((FunctionApp) term);
        }

        if (!term.isLeaf()) {
            FunctionApp f = (FunctionApp) term;
            for (Term arg : f.getArguments()) {
                extractCarTermsFromTerm(arg, carTerms);
            }
        }
    }

    /**
     * Extracts all cdr function applications from a collection of literals.
     *
     * @param literals The literals to scan
     * @return Set of all cdr(x) terms found
     */
    public static Set<FunctionApp> extractCdrTerms(Collection<Literal> literals) {
        Set<FunctionApp> cdrTerms = new HashSet<>();

        for (Literal lit : literals) {
            extractCdrTermsFromTerm(lit.getLeft(), cdrTerms);
            extractCdrTermsFromTerm(lit.getRight(), cdrTerms);
        }

        return cdrTerms;
    }

    /**
     * Recursively extracts cdr terms from a term.
     */
    private static void extractCdrTermsFromTerm(Term term, Set<FunctionApp> cdrTerms) {
        if (isCdr(term)) {
            cdrTerms.add((FunctionApp) term);
        }

        if (!term.isLeaf()) {
            FunctionApp f = (FunctionApp) term;
            for (Term arg : f.getArguments()) {
                extractCdrTermsFromTerm(arg, cdrTerms);
            }
        }
    }
}
