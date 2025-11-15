package solver.theory.tcons;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import static org.junit.jupiter.api.Assertions.*;

import solver.dag.*;
import solver.theory.Result;
import solver.theory.te.Literal;

import java.util.Arrays;
import java.util.List;

/**
 * Tests for T_cons-Procedure (Theory of Lists).
 */
public class TConsProcedureTest {
    private TermFactory factory;

    @BeforeEach
    public void setUp() {
        factory = new TermFactory();
    }

    @Test
    public void testEmptyLiterals() {
        TConsProcedure tcons = new TConsProcedure(factory);
        Result result = tcons.checkSat(List.of());

        assertTrue(result.isSat(), "Empty literals should be SAT");
    }

    @Test
    public void testSimpleCarAxiom() {
        // cons(a, b) exists, car(cons(a, b)) != a → UNSAT
        Variable a = factory.createVariable("a");
        Variable b = factory.createVariable("b");
        FunctionApp cons = factory.createFunctionApp("cons", a, b);
        FunctionApp car = factory.createFunctionApp("car", cons);

        Literal diseq = Literal.disequality(car, a);

        TConsProcedure tcons = new TConsProcedure(factory);
        Result result = tcons.checkSat(Arrays.asList(diseq));

        assertTrue(result.isUnsat(),
            "car(cons(a, b)) != a should be UNSAT (violates car axiom)");
    }

    @Test
    public void testSimpleCdrAxiom() {
        // cons(a, b) exists, cdr(cons(a, b)) != b → UNSAT
        Variable a = factory.createVariable("a");
        Variable b = factory.createVariable("b");
        FunctionApp cons = factory.createFunctionApp("cons", a, b);
        FunctionApp cdr = factory.createFunctionApp("cdr", cons);

        Literal diseq = Literal.disequality(cdr, b);

        TConsProcedure tcons = new TConsProcedure(factory);
        Result result = tcons.checkSat(Arrays.asList(diseq));

        assertTrue(result.isUnsat(),
            "cdr(cons(a, b)) != b should be UNSAT (violates cdr axiom)");
    }

    @Test
    public void testCarAxiomSat() {
        // cons(a, b) exists, car(cons(a, b)) = a → SAT
        Variable a = factory.createVariable("a");
        Variable b = factory.createVariable("b");
        FunctionApp cons = factory.createFunctionApp("cons", a, b);
        FunctionApp car = factory.createFunctionApp("car", cons);

        Literal eq = Literal.equality(car, a);

        TConsProcedure tcons = new TConsProcedure(factory);
        Result result = tcons.checkSat(Arrays.asList(eq));

        assertTrue(result.isSat(),
            "car(cons(a, b)) = a should be SAT (consistent with axiom)");
    }

    @Test
    public void testBothAxioms() {
        // cons(a, b), car(cons(a,b)) = a, cdr(cons(a,b)) = b → SAT
        Variable a = factory.createVariable("a");
        Variable b = factory.createVariable("b");
        FunctionApp cons = factory.createFunctionApp("cons", a, b);
        FunctionApp car = factory.createFunctionApp("car", cons);
        FunctionApp cdr = factory.createFunctionApp("cdr", cons);

        Literal carEq = Literal.equality(car, a);
        Literal cdrEq = Literal.equality(cdr, b);

        TConsProcedure tcons = new TConsProcedure(factory);
        Result result = tcons.checkSat(Arrays.asList(carEq, cdrEq));

        assertTrue(result.isSat(), "Both axioms satisfied should be SAT");
    }

    @Test
    public void testConflictBetweenAxioms() {
        // cons(a, b), car(cons(a,b)) = c, c != a → UNSAT
        Variable a = factory.createVariable("a");
        Variable b = factory.createVariable("b");
        Variable c = factory.createVariable("c");
        FunctionApp cons = factory.createFunctionApp("cons", a, b);
        FunctionApp car = factory.createFunctionApp("car", cons);

        Literal eq = Literal.equality(car, c);
        Literal diseq = Literal.disequality(c, a);

        TConsProcedure tcons = new TConsProcedure(factory);
        Result result = tcons.checkSat(Arrays.asList(eq, diseq));

        assertTrue(result.isUnsat(),
            "car(cons(a,b)) = c AND c != a should be UNSAT (axiom says car = a)");
    }

    @Test
    public void testMultipleConsTerms() {
        // cons(a, b), cons(c, d), car(cons(a,b)) != a → UNSAT
        Variable a = factory.createVariable("a");
        Variable b = factory.createVariable("b");
        Variable c = factory.createVariable("c");
        Variable d = factory.createVariable("d");

        FunctionApp cons1 = factory.createFunctionApp("cons", a, b);
        FunctionApp cons2 = factory.createFunctionApp("cons", c, d);
        FunctionApp car1 = factory.createFunctionApp("car", cons1);

        Literal diseq = Literal.disequality(car1, a);

        TConsProcedure tcons = new TConsProcedure(factory);
        Result result = tcons.checkSat(Arrays.asList(diseq));

        assertTrue(result.isUnsat(),
            "Should generate axioms for both cons terms");
    }

    @Test
    public void testNestedCons() {
        // cons(a, cons(b, c)), car(cons(a, cons(b, c))) != a → UNSAT
        Variable a = factory.createVariable("a");
        Variable b = factory.createVariable("b");
        Variable c = factory.createVariable("c");

        FunctionApp innerCons = factory.createFunctionApp("cons", b, c);
        FunctionApp outerCons = factory.createFunctionApp("cons", a, innerCons);
        FunctionApp car = factory.createFunctionApp("car", outerCons);

        Literal diseq = Literal.disequality(car, a);

        TConsProcedure tcons = new TConsProcedure(factory);
        Result result = tcons.checkSat(Arrays.asList(diseq));

        assertTrue(result.isUnsat(),
            "Nested cons should generate axioms correctly");
    }

    @Test
    public void testCarOfCdr() {
        // cons(a, cons(b, c)), car(cdr(cons(a, cons(b, c)))) = b → SAT
        // Because: cdr(cons(a, cons(b,c))) = cons(b,c)
        //          car(cons(b,c)) = b
        Variable a = factory.createVariable("a");
        Variable b = factory.createVariable("b");
        Variable c = factory.createVariable("c");

        FunctionApp innerCons = factory.createFunctionApp("cons", b, c);
        FunctionApp outerCons = factory.createFunctionApp("cons", a, innerCons);
        FunctionApp cdr = factory.createFunctionApp("cdr", outerCons);
        FunctionApp car = factory.createFunctionApp("car", cdr);

        Literal eq = Literal.equality(car, b);

        TConsProcedure tcons = new TConsProcedure(factory);
        Result result = tcons.checkSat(Arrays.asList(eq));

        assertTrue(result.isSat(),
            "car(cdr(cons(a, cons(b, c)))) = b should be SAT");
    }

    @Test
    public void testCarOfCdrUnsat() {
        // cons(a, cons(b, c)), car(cdr(cons(a, cons(b, c)))) != b → UNSAT
        Variable a = factory.createVariable("a");
        Variable b = factory.createVariable("b");
        Variable c = factory.createVariable("c");

        FunctionApp innerCons = factory.createFunctionApp("cons", b, c);
        FunctionApp outerCons = factory.createFunctionApp("cons", a, innerCons);
        FunctionApp cdr = factory.createFunctionApp("cdr", outerCons);
        FunctionApp car = factory.createFunctionApp("car", cdr);

        Literal diseq = Literal.disequality(car, b);

        TConsProcedure tcons = new TConsProcedure(factory);
        Result result = tcons.checkSat(Arrays.asList(diseq));

        assertTrue(result.isUnsat(),
            "car(cdr(cons(a, cons(b, c)))) != b should be UNSAT");
    }

    @Test
    public void testListEquality() {
        // x = cons(a, b), y = cons(a, b), x != y → UNSAT
        Variable a = factory.createVariable("a");
        Variable b = factory.createVariable("b");
        Variable x = factory.createVariable("x");
        Variable y = factory.createVariable("y");

        FunctionApp cons1 = factory.createFunctionApp("cons", a, b);
        FunctionApp cons2 = factory.createFunctionApp("cons", a, b);

        // Note: hash-consing means cons1 == cons2
        Literal eq1 = Literal.equality(x, cons1);
        Literal eq2 = Literal.equality(y, cons2);
        Literal diseq = Literal.disequality(x, y);

        TConsProcedure tcons = new TConsProcedure(factory);
        Result result = tcons.checkSat(Arrays.asList(eq1, eq2, diseq));

        assertTrue(result.isUnsat(),
            "Two equal cons terms should make x = y");
    }

    @Test
    public void testNoConsSymbols() {
        // Pure T_E problem: a = b, b = c, a != c → UNSAT
        Variable a = factory.createVariable("a");
        Variable b = factory.createVariable("b");
        Variable c = factory.createVariable("c");

        Literal eq1 = Literal.equality(a, b);
        Literal eq2 = Literal.equality(b, c);
        Literal diseq = Literal.disequality(a, c);

        TConsProcedure tcons = new TConsProcedure(factory);
        Result result = tcons.checkSat(Arrays.asList(eq1, eq2, diseq));

        assertTrue(result.isUnsat(),
            "Should handle pure T_E problems (no cons symbols)");
    }

    @Test
    public void testCarCdrWithoutCons() {
        // car(x) = a, cdr(x) = b → SAT
        // (No cons term, so no axioms generated)
        Variable x = factory.createVariable("x");
        Variable a = factory.createVariable("a");
        Variable b = factory.createVariable("b");

        FunctionApp car = factory.createFunctionApp("car", x);
        FunctionApp cdr = factory.createFunctionApp("cdr", x);

        Literal eq1 = Literal.equality(car, a);
        Literal eq2 = Literal.equality(cdr, b);

        TConsProcedure tcons = new TConsProcedure(factory);
        Result result = tcons.checkSat(Arrays.asList(eq1, eq2));

        assertTrue(result.isSat(),
            "car and cdr without cons should be SAT (no axioms violated)");
    }

    @Test
    public void testComplexListStructure() {
        // Build: cons(a, cons(b, cons(c, nil)))
        // Assert: car(car(cdr(list))) != b → UNSAT
        Variable a = factory.createVariable("a");
        Variable b = factory.createVariable("b");
        Variable c = factory.createVariable("c");
        Variable nil = factory.createVariable("nil");

        FunctionApp cons3 = factory.createFunctionApp("cons", c, nil);
        FunctionApp cons2 = factory.createFunctionApp("cons", b, cons3);
        FunctionApp cons1 = factory.createFunctionApp("cons", a, cons2);

        FunctionApp cdr1 = factory.createFunctionApp("cdr", cons1);    // cons(b, cons(c, nil))
        FunctionApp car2 = factory.createFunctionApp("car", cdr1);     // b

        Literal diseq = Literal.disequality(car2, b);

        TConsProcedure tcons = new TConsProcedure(factory);
        Result result = tcons.checkSat(Arrays.asList(diseq));

        assertTrue(result.isUnsat(),
            "Complex list navigation should derive correct equalities");
    }

    @Test
    public void testSatWithMixedTerms() {
        // cons(a, b), f(car(cons(a,b))) = f(a) → SAT
        Variable a = factory.createVariable("a");
        Variable b = factory.createVariable("b");

        FunctionApp cons = factory.createFunctionApp("cons", a, b);
        FunctionApp car = factory.createFunctionApp("car", cons);
        FunctionApp fa = factory.createFunctionApp("f", a);
        FunctionApp fcar = factory.createFunctionApp("f", car);

        Literal eq = Literal.equality(fcar, fa);

        TConsProcedure tcons = new TConsProcedure(factory);
        Result result = tcons.checkSat(Arrays.asList(eq));

        assertTrue(result.isSat(),
            "f(car(cons(a,b))) = f(a) should be SAT (by congruence)");
    }

    @Test
    public void testGetTermFactory() {
        TConsProcedure tcons = new TConsProcedure(factory);
        assertEquals(factory, tcons.getTermFactory(),
            "Should return the term factory instance");
    }
}
