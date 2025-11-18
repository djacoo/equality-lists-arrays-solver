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
 * BRADLEY & MANNA NOTATION (Section 9.4, page 259):
 * ================================================================
 * Signature: Σ_cons : {cons, car, cdr, atom, =}
 *
 * AXIOMS (Bradley & Manna Section 9.4, page 259):
 * - Axiom 4 (left projection):  ∀x,y. car(cons(x,y)) = x
 * - Axiom 5 (right projection): ∀x,y. cdr(cons(x,y)) = y
 * - Axiom 6 (construction):     ∀x. ¬atom(x) → cons(car(x),cdr(x)) = x
 * - Axiom 7 (atom):             ∀x,y. ¬atom(cons(x,y))
 *
 * DECISION PROCEDURE (Bradley & Manna Section 9.4, page 260):
 * 1. Construct initial DAG for subterm set S_F
 * 2. For each node n such that n.fn = cons:
 *    - Add car(n) to DAG and MERGE car(n) n.args[1]
 *    - Add cdr(n) to DAG and MERGE cdr(n) n.args[2]
 * 3. For i ∈ {1,...,m}, MERGE s_i t_i
 * 4. For i ∈ {m+1,...,n}, if FIND s_i = FIND t_i, return unsatisfiable
 * 5. For i ∈ {1,...,ℓ}, if ∃v. FIND v = FIND u_i ∧ v.fn = cons,
 *    return unsatisfiable by axiom (atom)
 * 6. Otherwise, return satisfiable
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
     * Implements Bradley & Manna Section 9.4, page 260 algorithm.
     *
     * @param literals Collection of equality, disequality, and atom literals
     * @return Result indicating SAT or UNSAT with optional witness/conflict
     */
    public Result checkSat(Collection<Literal> literals) {
        // Preprocessing: Replace ¬atom(u_i) with u_i = cons(car(u_i), cdr(u_i))
        // per (construction) axiom (B&M page 260)
        List<Literal> processedLiterals = new ArrayList<>();
        List<Term> atomTerms = new ArrayList<>();  // Terms with atom(u_i) literals

        for (Literal lit : literals) {
            if (lit.isNegativeAtom()) {
                // ¬atom(u) → replace with u = cons(car(u), cdr(u))
                // per Axiom 6 (construction)
                Term u = lit.getAtomTerm();
                Term carU = termFactory.createFunctionApp(TConsSymbols.CAR, u);
                Term cdrU = termFactory.createFunctionApp(TConsSymbols.CDR, u);
                Term consCarCdr = termFactory.createFunctionApp(TConsSymbols.CONS, carU, cdrU);
                processedLiterals.add(Literal.equality(u, consCarCdr));
            } else if (lit.isPositiveAtom()) {
                // atom(u) → keep track for Step 5 check
                atomTerms.add(lit.getAtomTerm());
                // Don't add to processedLiterals - atom is implicit constraint
            } else {
                // Regular equality/disequality literal
                processedLiterals.add(lit);
            }
        }

        // Step 1 & 2: Extract all cons terms and generate axioms
        Set<FunctionApp> consTerms = TConsSymbols.extractConsTerms(processedLiterals);
        List<Literal> axioms = generateAxioms(consTerms);

        // Step 3: Combine literals with axioms
        List<Literal> allLiterals = new ArrayList<>(processedLiterals);
        allLiterals.addAll(axioms);

        // Step 4: Run T_E-procedure on combined set
        TEProcedure teProcedure = new TEProcedure();
        Result result = teProcedure.checkSat(allLiterals);

        // If UNSAT, return immediately
        if (result.isUnsat()) {
            return result;
        }

        // Step 5: Check atom constraints (B&M page 260, Step 5)
        // For i ∈ {1,...,ℓ}, if ∃v. FIND v = FIND u_i ∧ v.fn = cons,
        // return unsatisfiable by axiom (atom)
        if (!atomTerms.isEmpty()) {
            var cc = teProcedure.getCongruenceClosure();
            for (Term atomTerm : atomTerms) {
                // Find representative of atomTerm
                Term rep = cc.find(atomTerm);

                // Check if any cons term is in the same equivalence class
                for (FunctionApp cons : consTerms) {
                    if (cc.find(cons).equals(rep)) {
                        return Result.unsat(
                            String.format("Atom constraint violated: atom(%s) but %s ≡ %s (cons term). " +
                                         "Bradley & Manna Axiom 7: ∀x,y. ¬atom(cons(x,y))",
                                         atomTerm.getSymbol(), atomTerm.getSymbol(), cons.getSymbol())
                        );
                    }
                }
            }
        }

        // Step 6: Return satisfiable
        return result;
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
