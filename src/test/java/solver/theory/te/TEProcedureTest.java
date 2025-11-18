package solver.theory.te;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import static org.junit.jupiter.api.Assertions.*;

import solver.dag.*;
import solver.theory.Result;

import java.util.Arrays;
import java.util.List;

/**
 * Tests for T_E-Procedure (Theory of Equality).
 */
public class TEProcedureTest {
    private TermFactory factory;

    @BeforeEach
    public void setUp() {
        factory = new TermFactory();
    }

    @Test
    public void testEmptyLiterals() {
        TEProcedure te = new TEProcedure();
        Result result = te.checkSat(List.of());

        assertTrue(result.isSat(), "Empty set of literals should be SAT");
    }

    @Test
    public void testSingleEquality() {
        Variable a = factory.createVariable("a");
        Variable b = factory.createVariable("b");

        Literal eq = Literal.equality(a, b);

        TEProcedure te = new TEProcedure();
        Result result = te.checkSat(List.of(eq));

        assertTrue(result.isSat(), "Single equality should be SAT");
        assertTrue(result.getWitness().isPresent());
    }

    @Test
    public void testSingleDisequality() {
        Variable a = factory.createVariable("a");
        Variable b = factory.createVariable("b");

        Literal diseq = Literal.disequality(a, b);

        TEProcedure te = new TEProcedure();
        Result result = te.checkSat(List.of(diseq));

        assertTrue(result.isSat(), "Single disequality should be SAT (no conflict)");
    }

    @Test
    public void testDirectConflict() {
        // a = b AND a != b → UNSAT
        Variable a = factory.createVariable("a");
        Variable b = factory.createVariable("b");

        Literal eq = Literal.equality(a, b);
        Literal diseq = Literal.disequality(a, b);

        TEProcedure te = new TEProcedure();
        Result result = te.checkSat(Arrays.asList(eq, diseq));

        assertTrue(result.isUnsat(), "a = b AND a != b should be UNSAT");
        assertTrue(result.getConflict().isPresent());
    }

    @Test
    public void testTransitiveConflict() {
        // a = b, b = c, a != c → UNSAT
        Variable a = factory.createVariable("a");
        Variable b = factory.createVariable("b");
        Variable c = factory.createVariable("c");

        Literal eq1 = Literal.equality(a, b);
        Literal eq2 = Literal.equality(b, c);
        Literal diseq = Literal.disequality(a, c);

        TEProcedure te = new TEProcedure();
        Result result = te.checkSat(Arrays.asList(eq1, eq2, diseq));

        assertTrue(result.isUnsat(),
            "a = b, b = c, a != c should be UNSAT (transitivity)");
    }

    @Test
    public void testCongruenceConflict() {
        // a = b, f(a) != f(b) → UNSAT
        Variable a = factory.createVariable("a");
        Variable b = factory.createVariable("b");
        FunctionApp fa = factory.createFunctionApp("f", a);
        FunctionApp fb = factory.createFunctionApp("f", b);

        Literal eq = Literal.equality(a, b);
        Literal diseq = Literal.disequality(fa, fb);

        TEProcedure te = new TEProcedure();
        Result result = te.checkSat(Arrays.asList(eq, diseq));

        assertTrue(result.isUnsat(),
            "a = b, f(a) != f(b) should be UNSAT (congruence)");
    }

    @Test
    public void testSatWithMultipleEqualities() {
        // a = b, c = d (no conflicts)
        Variable a = factory.createVariable("a");
        Variable b = factory.createVariable("b");
        Variable c = factory.createVariable("c");
        Variable d = factory.createVariable("d");

        Literal eq1 = Literal.equality(a, b);
        Literal eq2 = Literal.equality(c, d);

        TEProcedure te = new TEProcedure();
        Result result = te.checkSat(Arrays.asList(eq1, eq2));

        assertTrue(result.isSat(), "Two independent equalities should be SAT");

        // Verify witness has correct number of classes
        assertTrue(result.getWitness().isPresent());
        assertEquals(2, result.getWitness().get().size(),
            "Should have 2 equivalence classes: {a,b} and {c,d}");
    }

    @Test
    public void testSatWithCompatibleDisequalities() {
        // a = b, c = d, a != c → SAT
        Variable a = factory.createVariable("a");
        Variable b = factory.createVariable("b");
        Variable c = factory.createVariable("c");
        Variable d = factory.createVariable("d");

        Literal eq1 = Literal.equality(a, b);
        Literal eq2 = Literal.equality(c, d);
        Literal diseq = Literal.disequality(a, c);

        TEProcedure te = new TEProcedure();
        Result result = te.checkSat(Arrays.asList(eq1, eq2, diseq));

        assertTrue(result.isSat(),
            "a = b, c = d, a != c should be SAT (no conflict)");
    }

    @Test
    public void testNestedCongruenceConflict() {
        // a = b, f(f(a)) != f(f(b)) → UNSAT
        Variable a = factory.createVariable("a");
        Variable b = factory.createVariable("b");
        FunctionApp fa = factory.createFunctionApp("f", a);
        FunctionApp fb = factory.createFunctionApp("f", b);
        FunctionApp ffa = factory.createFunctionApp("f", fa);
        FunctionApp ffb = factory.createFunctionApp("f", fb);

        Literal eq = Literal.equality(a, b);
        Literal diseq = Literal.disequality(ffa, ffb);

        TEProcedure te = new TEProcedure();
        Result result = te.checkSat(Arrays.asList(eq, diseq));

        assertTrue(result.isUnsat(),
            "a = b, f(f(a)) != f(f(b)) should be UNSAT");
    }

    @Test
    public void testBinaryFunctionCongruenceConflict() {
        // a = c, b = d, f(a,b) != f(c,d) → UNSAT
        Variable a = factory.createVariable("a");
        Variable b = factory.createVariable("b");
        Variable c = factory.createVariable("c");
        Variable d = factory.createVariable("d");

        FunctionApp fab = factory.createFunctionApp("f", a, b);
        FunctionApp fcd = factory.createFunctionApp("f", c, d);

        Literal eq1 = Literal.equality(a, c);
        Literal eq2 = Literal.equality(b, d);
        Literal diseq = Literal.disequality(fab, fcd);

        TEProcedure te = new TEProcedure();
        Result result = te.checkSat(Arrays.asList(eq1, eq2, diseq));

        assertTrue(result.isUnsat(),
            "a = c, b = d, f(a,b) != f(c,d) should be UNSAT");
    }

    @Test
    public void testComplexSatExample() {
        // a = b, f(a) = c, g(c) != g(f(b)) would be UNSAT
        // But a = b, f(a) = c, g(c) = g(f(b)) is SAT
        Variable a = factory.createVariable("a");
        Variable b = factory.createVariable("b");
        Variable c = factory.createVariable("c");

        FunctionApp fa = factory.createFunctionApp("f", a);
        FunctionApp fb = factory.createFunctionApp("f", b);
        FunctionApp gc = factory.createFunctionApp("g", c);
        FunctionApp gfb = factory.createFunctionApp("g", fb);

        Literal eq1 = Literal.equality(a, b);
        Literal eq2 = Literal.equality(fa, c);
        Literal eq3 = Literal.equality(gc, gfb);

        TEProcedure te = new TEProcedure();
        Result result = te.checkSat(Arrays.asList(eq1, eq2, eq3));

        assertTrue(result.isSat(), "Complex example should be SAT");
    }

    @Test
    public void testBradleyMannaExample() {
        // f(f(a)) = a, f(f(f(a))) = a, f(a) != a → UNSAT
        // (Because CC derives f(a) = a)
        Variable a = factory.createVariable("a");
        FunctionApp fa = factory.createFunctionApp("f", a);
        FunctionApp ffa = factory.createFunctionApp("f", fa);
        FunctionApp fffa = factory.createFunctionApp("f", ffa);

        Literal eq1 = Literal.equality(ffa, a);
        Literal eq2 = Literal.equality(fffa, a);
        Literal diseq = Literal.disequality(fa, a);

        TEProcedure te = new TEProcedure();
        Result result = te.checkSat(Arrays.asList(eq1, eq2, diseq));

        assertTrue(result.isUnsat(),
            "Bradley & Manna example: should derive f(a) = a and conflict");
    }

    @Test
    public void testMultipleDisequalities() {
        // a != b, b != c, c != d → SAT (all can be distinct)
        Variable a = factory.createVariable("a");
        Variable b = factory.createVariable("b");
        Variable c = factory.createVariable("c");
        Variable d = factory.createVariable("d");

        Literal diseq1 = Literal.disequality(a, b);
        Literal diseq2 = Literal.disequality(b, c);
        Literal diseq3 = Literal.disequality(c, d);

        TEProcedure te = new TEProcedure();
        Result result = te.checkSat(Arrays.asList(diseq1, diseq2, diseq3));

        assertTrue(result.isSat(),
            "Multiple disequalities with no equalities should be SAT");
    }

    @Test
    public void testDifferentFunctionSymbols() {
        // a = b, f(a) != g(b) → SAT (different function symbols)
        Variable a = factory.createVariable("a");
        Variable b = factory.createVariable("b");
        FunctionApp fa = factory.createFunctionApp("f", a);
        FunctionApp gb = factory.createFunctionApp("g", b);

        Literal eq = Literal.equality(a, b);
        Literal diseq = Literal.disequality(fa, gb);

        TEProcedure te = new TEProcedure();
        Result result = te.checkSat(Arrays.asList(eq, diseq));

        assertTrue(result.isSat(),
            "f(a) and g(b) are different even if a = b");
    }

    @Test
    public void testLiteralEquality() {
        Variable a = factory.createVariable("a");
        Variable b = factory.createVariable("b");

        Literal lit1 = Literal.equality(a, b);
        Literal lit2 = Literal.equality(b, a);

        assertEquals(lit1, lit2, "Equality should be symmetric");
    }

    @Test
    public void testLiteralToString() {
        Variable a = factory.createVariable("a");
        Variable b = factory.createVariable("b");

        Literal eq = Literal.equality(a, b);
        Literal diseq = Literal.disequality(a, b);

        assertTrue(eq.toString().contains("="));
        assertTrue(diseq.toString().contains("!="));
    }

    /**
     * Bradley & Manna Example 9.10 (page 249-250).
     *
     * F : f(f(f(a))) = a ∧ f(f(f(f(f(a))))) = a ∧ f(a) ≠ a
     *
     * This is TE-unsatisfiable. The algorithm deduces:
     * 1. From f³(a) = a: f⁴(a) = f(a), f⁵(a) = f²(a)
     * 2. From f⁵(a) = a and f⁵(a) = f²(a): f²(a) = a
     * 3. From f³(a) = a and f²(a) = a: f(a) = a
     * 4. Contradiction with f(a) ≠ a
     *
     * This corresponds to the detailed execution shown in Figure 9.2 of B&M.
     */
    @Test
    public void testBradleyMannaExample910() {
        // F : f(f(f(a))) = a ∧ f(f(f(f(f(a))))) = a ∧ f(a) ≠ a
        Constant a = factory.createConstant("a");
        FunctionApp fa = factory.createFunctionApp("f", a);         // f(a)
        FunctionApp f2a = factory.createFunctionApp("f", fa);       // f²(a)
        FunctionApp f3a = factory.createFunctionApp("f", f2a);      // f³(a)
        FunctionApp f4a = factory.createFunctionApp("f", f3a);      // f⁴(a)
        FunctionApp f5a = factory.createFunctionApp("f", f4a);      // f⁵(a)

        // Literals from Example 9.10
        Literal eq1 = Literal.equality(f3a, a);     // f³(a) = a
        Literal eq2 = Literal.equality(f5a, a);     // f⁵(a) = a
        Literal diseq = Literal.disequality(fa, a); // f(a) ≠ a

        TEProcedure te = new TEProcedure();
        Result result = te.checkSat(Arrays.asList(eq1, eq2, diseq));

        assertTrue(result.isUnsat(),
            "Bradley & Manna Example 9.10: f³(a) = a ∧ f⁵(a) = a ∧ f(a) ≠ a should be UNSAT");
        assertTrue(result.getConflict().isPresent(),
            "Should have conflict explanation");
    }
}
