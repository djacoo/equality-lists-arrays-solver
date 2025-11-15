package solver.theory.tcons;

import solver.dag.FunctionApp;
import solver.dag.Term;
import solver.dag.TermFactory;
import solver.theory.Result;
import solver.theory.te.Literal;
import solver.theory.te.TEProcedure;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;

/**
 * T_cons-Procedure: Satisfiability checker for the Theory of Lists.
 *
 * The Theory of Lists (T_cons) extends T_E with three interpreted symbols:
 * - cons(x, y): List constructor
 * - car(x): Head selector
 * - cdr(x): Tail selector
 *
 * Algorithm:
 * 1. Identify all cons(x, y) terms in the literals
 * 2. For each cons(x, y), add axioms:
 *    - car(cons(x, y)) = x
 *    - cdr(cons(x, y)) = y
 * 3. Run T_E-procedure on original literals + axioms
 * 4. Return result
 */
public class TConsProcedure {
    private final TermFactory termFactory;

    /**
     * Creates a new T_cons-procedure instance.
     */
    public TConsProcedure() {
        this.termFactory = new TermFactory();
    }

    /**
     * Creates a T_cons-procedure instance with a shared term factory.
     *
     * @param termFactory The term factory to use for creating axiom terms
     */
    public TConsProcedure(TermFactory termFactory) {
        this.termFactory = termFactory;
    }

    /**
     * Checks satisfiability of a set of literals in T_cons.
     *
     * @param literals Collection of equality and disequality literals
     * @return Result indicating SAT or UNSAT with optional witness/conflict
     */
    public Result checkSat(Collection<Literal> literals) {
        // Step 1: Extract all cons terms from the literals
        Set<FunctionApp> consTerms = TConsSymbols.extractConsTerms(literals);

        // Step 2: Generate axioms for each cons term
        List<Literal> axioms = generateAxioms(consTerms);

        // Step 3: Combine original literals with axioms
        List<Literal> allLiterals = new ArrayList<>(literals);
        allLiterals.addAll(axioms);

        // Step 4: Run T_E-procedure on combined set
        TEProcedure teProcedure = new TEProcedure();
        return teProcedure.checkSat(allLiterals);
    }

    /**
     * Generates T_cons axioms for all cons terms.
     *
     * For each cons(x, y), generates:
     * - car(cons(x, y)) = x
     * - cdr(cons(x, y)) = y
     *
     * @param consTerms Set of cons function applications
     * @return List of axiom literals
     */
    private List<Literal> generateAxioms(Set<FunctionApp> consTerms) {
        List<Literal> axioms = new ArrayList<>();

        for (FunctionApp cons : consTerms) {
            // cons has exactly 2 arguments (validated by TConsSymbols)
            Term x = cons.getArguments().get(0);  // head
            Term y = cons.getArguments().get(1);  // tail

            // Axiom 1: car(cons(x, y)) = x
            FunctionApp carCons = termFactory.createFunctionApp(TConsSymbols.CAR, cons);
            Literal carAxiom = Literal.equality(carCons, x);
            axioms.add(carAxiom);

            // Axiom 2: cdr(cons(x, y)) = y
            FunctionApp cdrCons = termFactory.createFunctionApp(TConsSymbols.CDR, cons);
            Literal cdrAxiom = Literal.equality(cdrCons, y);
            axioms.add(cdrAxiom);
        }

        return axioms;
    }

    /**
     * Returns the term factory used by this procedure.
     *
     * @return The TermFactory instance
     */
    public TermFactory getTermFactory() {
        return termFactory;
    }
}
