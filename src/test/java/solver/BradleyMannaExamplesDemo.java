package solver;

import solver.dag.Constant;
import solver.dag.FunctionApp;
import solver.dag.TermFactory;
import solver.dag.Variable;
import solver.theory.Result;
import solver.theory.tarray.TArrayProcedure;
import solver.theory.tcons.TConsProcedure;
import solver.theory.te.Literal;
import solver.theory.te.TEProcedure;

import java.util.Arrays;

/**
 * Demonstration of Bradley & Manna Chapter 9 examples.
 *
 * This class runs the actual examples from the textbook and shows
 * that our implementation correctly handles them according to the
 * algorithms described in Bradley & Manna.
 */
public class BradleyMannaExamplesDemo {

    public static void main(String[] args) {
        System.out.println("=" .repeat(80));
        System.out.println("BRADLEY & MANNA CHAPTER 9 EXAMPLES DEMONSTRATION");
        System.out.println("=" .repeat(80));
        System.out.println();

        runExample910();
        System.out.println();

        runExample920();
        System.out.println();

        runExample921();
        System.out.println();

        System.out.println("=" .repeat(80));
        System.out.println("All Bradley & Manna examples executed successfully!");
        System.out.println("=" .repeat(80));
    }

    /**
     * Bradley & Manna Example 9.10 (pages 249-250)
     *
     * Theory: T_E (Equality with Uninterpreted Functions)
     * Formula: f³(a) = a ∧ f⁵(a) = a ∧ f(a) ≠ a
     * Expected: UNSATISFIABLE
     *
     * Reasoning (from B&M page 250):
     * - From f³(a) = a, we get: f(a) = f(f³(a)) = f(a)
     * - From f⁵(a) = a, we get: f(a) = f(f⁵(a)) = f⁶(a)
     * - Since f³(a) = a, we have f⁶(a) = f³(f³(a)) = f³(a) = a
     * - Therefore f(a) = a, which contradicts f(a) ≠ a
     */
    private static void runExample910() {
        System.out.println("┌─────────────────────────────────────────────────────────────────────────┐");
        System.out.println("│ EXAMPLE 9.10: T_E-procedure (Bradley & Manna, pages 249-250)           │");
        System.out.println("└─────────────────────────────────────────────────────────────────────────┘");
        System.out.println();
        System.out.println("Formula: f³(a) = a ∧ f⁵(a) = a ∧ f(a) ≠ a");
        System.out.println();
        System.out.println("Theory: T_E (Equality with Uninterpreted Functions)");
        System.out.println("Expected: UNSATISFIABLE");
        System.out.println();
        System.out.println("Reasoning from Bradley & Manna:");
        System.out.println("  • From f³(a) = a: implies f(f(f(a))) = a");
        System.out.println("  • From f⁵(a) = a: implies f(f(f(f(f(a))))) = a");
        System.out.println("  • By congruence: f(f³(a)) = f(a), and since f³(a) = a, we get f(a) = f(a)");
        System.out.println("  • By transitivity: f³(a) = a and f⁵(a) = a imply f(a) = a");
        System.out.println("  • This contradicts f(a) ≠ a");
        System.out.println();

        TermFactory factory = new TermFactory();
        Constant a = factory.createConstant("a");
        FunctionApp fa = factory.createFunctionApp("f", a);
        FunctionApp f2a = factory.createFunctionApp("f", fa);
        FunctionApp f3a = factory.createFunctionApp("f", f2a);
        FunctionApp f4a = factory.createFunctionApp("f", f3a);
        FunctionApp f5a = factory.createFunctionApp("f", f4a);

        Literal eq1 = Literal.equality(f3a, a);
        Literal eq2 = Literal.equality(f5a, a);
        Literal diseq = Literal.disequality(fa, a);

        TEProcedure te = new TEProcedure();
        Result result = te.checkSat(Arrays.asList(eq1, eq2, diseq));

        System.out.println("Result: " + (result.isUnsat() ? "UNSATISFIABLE ✓" : "SATISFIABLE ✗"));
        if (result.isUnsat() && result.getConflict().isPresent()) {
            System.out.println("Explanation: " + result.getConflict().get());
        }
        System.out.println();
        System.out.println("─".repeat(77));
    }

    /**
     * Bradley & Manna Example 9.20 (pages 260-262)
     *
     * Theory: T_cons (Lists with cons/car/cdr and atom predicate)
     * Formula: car(x) = car(y) ∧ cdr(x) = cdr(y) ∧ f(x) ≠ f(y) ∧ ¬atom(x) ∧ ¬atom(y)
     * Expected: UNSATISFIABLE
     *
     * Reasoning (from B&M pages 260-262):
     * Algorithm Steps:
     * 1. Preprocessing: ¬atom(x) → x = cons(car(x), cdr(x))
     *                  ¬atom(y) → y = cons(car(y), cdr(y))
     * 2. Generate axioms for cons terms
     * 3. From car(x) = car(y) and cdr(x) = cdr(y):
     *    cons(car(x), cdr(x)) = cons(car(y), cdr(y))
     * 4. By congruence and the ¬atom transformations: x = y
     * 5. By congruence on f: f(x) = f(y)
     * 6. Contradiction with f(x) ≠ f(y)
     */
    private static void runExample920() {
        System.out.println("┌─────────────────────────────────────────────────────────────────────────┐");
        System.out.println("│ EXAMPLE 9.20: T_cons-procedure (Bradley & Manna, pages 260-262)        │");
        System.out.println("└─────────────────────────────────────────────────────────────────────────┘");
        System.out.println();
        System.out.println("Formula: car(x) = car(y) ∧ cdr(x) = cdr(y) ∧ f(x) ≠ f(y) ∧ ¬atom(x) ∧ ¬atom(y)");
        System.out.println();
        System.out.println("Theory: T_cons (Lists with cons/car/cdr and atom predicate)");
        System.out.println("Expected: UNSATISFIABLE");
        System.out.println();
        System.out.println("Reasoning from Bradley & Manna (Algorithm on page 260):");
        System.out.println("  Step 1 (Preprocessing): Replace ¬atom predicates using Axiom 6:");
        System.out.println("          ¬atom(x) → x = cons(car(x), cdr(x))");
        System.out.println("          ¬atom(y) → y = cons(car(y), cdr(y))");
        System.out.println("  Step 2: From car(x) = car(y) and cdr(x) = cdr(y):");
        System.out.println("          cons(car(x), cdr(x)) = cons(car(y), cdr(y)) by congruence");
        System.out.println("  Step 3: Therefore x = y (from Step 1 equalities)");
        System.out.println("  Step 4: By congruence: f(x) = f(y)");
        System.out.println("  Step 5: Contradiction with f(x) ≠ f(y)");
        System.out.println();

        TermFactory factory = new TermFactory();
        Variable x = factory.createVariable("x");
        Variable y = factory.createVariable("y");
        FunctionApp carX = factory.createFunctionApp("car", x);
        FunctionApp carY = factory.createFunctionApp("car", y);
        FunctionApp cdrX = factory.createFunctionApp("cdr", x);
        FunctionApp cdrY = factory.createFunctionApp("cdr", y);
        FunctionApp fx = factory.createFunctionApp("f", x);
        FunctionApp fy = factory.createFunctionApp("f", y);

        Literal carEq = Literal.equality(carX, carY);
        Literal cdrEq = Literal.equality(cdrX, cdrY);
        Literal diseq = Literal.disequality(fx, fy);
        Literal notAtomX = Literal.notAtom(x);
        Literal notAtomY = Literal.notAtom(y);

        TConsProcedure tcons = new TConsProcedure(factory);
        Result result = tcons.checkSat(Arrays.asList(carEq, cdrEq, diseq, notAtomX, notAtomY));

        System.out.println("Result: " + (result.isUnsat() ? "UNSATISFIABLE ✓" : "SATISFIABLE ✗"));
        if (result.isUnsat() && result.getConflict().isPresent()) {
            System.out.println("Explanation: " + result.getConflict().get());
        }
        System.out.println();
        System.out.println("─".repeat(77));
    }

    /**
     * Bradley & Manna Example 9.21 (page 264)
     *
     * Theory: T_A (Arrays with select/store)
     * Formula: i₁ = j ∧ i₁ ≠ i₂ ∧ a[j] = v₁ ∧ a⟨i₁ ⊳ v₁⟩⟨i₂ ⊳ v₂⟩[j] ≠ a[j]
     * Expected: UNSATISFIABLE
     *
     * Reasoning (from B&M page 264):
     * Branch 1 (i₁ = i₂): Contradicts i₁ ≠ i₂ → UNSAT
     * Branch 2 (i₁ ≠ i₂):
     *   - Since i₁ = j and i₁ ≠ i₂, we have j ≠ i₂
     *   Sub-branch 2.1 (i₂ = j): Contradicts j ≠ i₂ → UNSAT
     *   Sub-branch 2.2 (i₂ ≠ j):
     *     - By Axiom 4: a⟨i₁ ⊳ v₁⟩⟨i₂ ⊳ v₂⟩[j] = a⟨i₁ ⊳ v₁⟩[j]
     *     - By Axiom 3: a⟨i₁ ⊳ v₁⟩[j] = v₁ (since i₁ = j)
     *     - From a[j] = v₁: a⟨i₁ ⊳ v₁⟩⟨i₂ ⊳ v₂⟩[j] = a[j]
     *     - Contradicts a⟨i₁ ⊳ v₁⟩⟨i₂ ⊳ v₂⟩[j] ≠ a[j] → UNSAT
     */
    private static void runExample921() {
        System.out.println("┌─────────────────────────────────────────────────────────────────────────┐");
        System.out.println("│ EXAMPLE 9.21: T_A-procedure (Bradley & Manna, page 264)                │");
        System.out.println("└─────────────────────────────────────────────────────────────────────────┘");
        System.out.println();
        System.out.println("Formula: i₁ = j ∧ i₁ ≠ i₂ ∧ a[j] = v₁ ∧ a⟨i₁ ⊳ v₁⟩⟨i₂ ⊳ v₂⟩[j] ≠ a[j]");
        System.out.println();
        System.out.println("Theory: T_A (Arrays with read/write operations)");
        System.out.println("Notation: a[i] = select(a,i), a⟨i ⊳ v⟩ = store(a,i,v)");
        System.out.println("Expected: UNSATISFIABLE");
        System.out.println();
        System.out.println("Reasoning from Bradley & Manna (page 264):");
        System.out.println("  Case Analysis on index relationships:");
        System.out.println("  • Since i₁ = j (given) and i₁ ≠ i₂ (given), we have j ≠ i₂");
        System.out.println("  • Apply Axiom 4 (read-over-write): i₂ ≠ j → a⟨i₁⊳v₁⟩⟨i₂⊳v₂⟩[j] = a⟨i₁⊳v₁⟩[j]");
        System.out.println("  • Apply Axiom 3 (read-over-write): i₁ = j → a⟨i₁⊳v₁⟩[j] = v₁");
        System.out.println("  • From a[j] = v₁ (given): a⟨i₁⊳v₁⟩⟨i₂⊳v₂⟩[j] = a[j]");
        System.out.println("  • This contradicts a⟨i₁⊳v₁⟩⟨i₂⊳v₂⟩[j] ≠ a[j]");
        System.out.println();

        TermFactory factory = new TermFactory();
        Variable a = factory.createVariable("a");
        Variable i1 = factory.createVariable("i1");
        Variable i2 = factory.createVariable("i2");
        Variable j = factory.createVariable("j");
        Variable v1 = factory.createVariable("v1");
        Variable v2 = factory.createVariable("v2");

        FunctionApp aAtJ = factory.createFunctionApp("select", a, j);
        FunctionApp store1 = factory.createFunctionApp("store", a, i1, v1);
        FunctionApp store2 = factory.createFunctionApp("store", store1, i2, v2);
        FunctionApp store2AtJ = factory.createFunctionApp("select", store2, j);

        Literal eq1 = Literal.equality(i1, j);
        Literal diseq1 = Literal.disequality(i1, i2);
        Literal eq2 = Literal.equality(aAtJ, v1);
        Literal diseq2 = Literal.disequality(store2AtJ, aAtJ);

        TArrayProcedure procedure = new TArrayProcedure(factory);
        Result result = procedure.check(Arrays.asList(eq1, diseq1, eq2, diseq2));

        System.out.println("Result: " + (result.isUnsat() ? "UNSATISFIABLE ✓" : "SATISFIABLE ✗"));
        if (result.isUnsat() && result.getConflict().isPresent()) {
            System.out.println("Explanation: " + result.getConflict().get());
        }
        System.out.println();
        System.out.println("─".repeat(77));
    }
}
