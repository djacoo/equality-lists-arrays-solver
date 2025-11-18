package solver.parser;

import solver.dag.Term;
import solver.dag.TermFactory;
import solver.theory.te.Literal;

/**
 * Transforms free (uninterpreted) predicate symbols into equalities.
 *
 * BRADLEY & MANNA (Section 9.2, page 253):
 * Free predicate symbols can be handled by introducing boolean constants.
 * For example:
 * - P(x) is transformed to P(x) = true
 * - ¬P(x) is transformed to P(x) = false
 *
 * This allows the solver to handle arbitrary predicates in the equality framework.
 *
 * NOTE: The 'atom' predicate is special and handled directly by T_cons-procedure
 * (Section 9.4, Axiom 7), so it is NOT transformed.
 */
public class PredicateTransformer {
    private final TermFactory termFactory;
    private final Term trueConstant;
    private final Term falseConstant;

    public PredicateTransformer(TermFactory termFactory) {
        this.termFactory = termFactory;
        // Create boolean constants as variables
        // We use special names to avoid conflicts with user-defined variables
        this.trueConstant = termFactory.createVariable("$true");
        this.falseConstant = termFactory.createVariable("$false");
    }

    /**
     * Transforms a predicate application into an equality.
     *
     * @param predicateName Name of the predicate (e.g., "P", "Q")
     * @param arguments Arguments to the predicate
     * @param positive true for P(x), false for ¬P(x)
     * @return Literal representing the transformed equality
     */
    public Literal transformPredicate(String predicateName, java.util.List<Term> arguments, boolean positive) {
        // Create predicate as a function application
        Term predicateApp = termFactory.createFunctionApp(predicateName, arguments);

        // Transform to equality with boolean constant
        Term booleanValue = positive ? trueConstant : falseConstant;

        return Literal.equality(predicateApp, booleanValue);
    }

    /**
     * Adds axioms for boolean constants to ensure they are distinct.
     *
     * Returns the disequality: true != false
     *
     * @return Literal representing true != false
     */
    public Literal getBooleanDistinctnessAxiom() {
        return Literal.disequality(trueConstant, falseConstant);
    }

    /**
     * Returns the 'true' constant term.
     */
    public Term getTrueConstant() {
        return trueConstant;
    }

    /**
     * Returns the 'false' constant term.
     */
    public Term getFalseConstant() {
        return falseConstant;
    }
}
