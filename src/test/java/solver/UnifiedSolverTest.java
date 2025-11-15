package solver;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import static org.junit.jupiter.api.Assertions.*;

import solver.dag.*;
import solver.theory.Result;
import solver.theory.te.Literal;

import java.util.Arrays;
import java.util.List;

/**
 * Tests for UnifiedSolver - the main entry point for all theories.
 */
public class UnifiedSolverTest {
    private TermFactory factory;
    private UnifiedSolver solver;

    @BeforeEach
    public void setUp() {
        factory = new TermFactory();
        solver = new UnifiedSolver(factory);
    }

    @Test
    public void testFactoryAccessor() {
        assertNotNull(solver.getFactory(), "Factory should be accessible");
        assertSame(factory, solver.getFactory(), "Should return same factory");
    }

    @Test
    public void testDefaultConstructor() {
        UnifiedSolver s = new UnifiedSolver();
        assertNotNull(s.getFactory(), "Default constructor should create factory");

        Variable a = s.getFactory().createVariable("a");
        Variable b = s.getFactory().createVariable("b");
        Literal lit = Literal.equality(a, b);

        Result result = s.checkSat(Arrays.asList(lit));
        assertTrue(result.isSat(), "Should work with default constructor");
    }

    // ========== Pure T_E Tests ==========

    @Test
    public void testPureEqualitySat() {
        Variable a = factory.createVariable("a");
        Variable b = factory.createVariable("b");
        Variable c = factory.createVariable("c");

        Literal lit1 = Literal.equality(a, b);
        Literal lit2 = Literal.equality(b, c);

        Result result = solver.checkSat(Arrays.asList(lit1, lit2));

        assertTrue(result.isSat(), "a=b, b=c should be SAT");
    }

    @Test
    public void testPureEqualityUnsat() {
        Variable a = factory.createVariable("a");
        Variable b = factory.createVariable("b");

        Literal eq = Literal.equality(a, b);
        Literal diseq = Literal.disequality(a, b);

        Result result = solver.checkSat(Arrays.asList(eq, diseq));

        assertTrue(result.isUnsat(), "a=b AND a≠b should be UNSAT");
    }

    @Test
    public void testTransitivityConflict() {
        Variable a = factory.createVariable("a");
        Variable b = factory.createVariable("b");
        Variable c = factory.createVariable("c");

        Literal eq1 = Literal.equality(a, b);
        Literal eq2 = Literal.equality(b, c);
        Literal diseq = Literal.disequality(a, c);

        Result result = solver.checkSat(Arrays.asList(eq1, eq2, diseq));

        assertTrue(result.isUnsat(), "Transitivity violation should be UNSAT");
    }

    @Test
    public void testCongruenceClosure() {
        Variable a = factory.createVariable("a");
        Variable b = factory.createVariable("b");
        FunctionApp fa = factory.createFunctionApp("f", a);
        FunctionApp fb = factory.createFunctionApp("f", b);

        Literal eq1 = Literal.equality(a, b);
        Literal diseq = Literal.disequality(fa, fb);

        Result result = solver.checkSat(Arrays.asList(eq1, diseq));

        assertTrue(result.isUnsat(), "a=b → f(a)=f(b), so f(a)≠f(b) should be UNSAT");
    }

    // ========== Pure T_cons Tests ==========

    @Test
    public void testCarAxiomSat() {
        Variable a = factory.createVariable("a");
        Variable b = factory.createVariable("b");
        FunctionApp cons = factory.createFunctionApp("cons", a, b);
        FunctionApp car = factory.createFunctionApp("car", cons);

        Literal lit = Literal.equality(car, a);

        Result result = solver.checkSat(Arrays.asList(lit));

        assertTrue(result.isSat(), "car(cons(a,b)) = a should be SAT");
    }

    @Test
    public void testCarAxiomUnsat() {
        Variable a = factory.createVariable("a");
        Variable b = factory.createVariable("b");
        Variable c = factory.createVariable("c");
        FunctionApp cons = factory.createFunctionApp("cons", a, b);
        FunctionApp car = factory.createFunctionApp("car", cons);

        Literal eq = Literal.equality(car, c);
        Literal diseq = Literal.disequality(a, c);

        Result result = solver.checkSat(Arrays.asList(eq, diseq));

        assertTrue(result.isUnsat(), "car(cons(a,b))=c AND a≠c should be UNSAT");
    }

    @Test
    public void testCdrAxiom() {
        Variable a = factory.createVariable("a");
        Variable b = factory.createVariable("b");
        FunctionApp cons = factory.createFunctionApp("cons", a, b);
        FunctionApp cdr = factory.createFunctionApp("cdr", cons);

        Literal lit = Literal.equality(cdr, b);

        Result result = solver.checkSat(Arrays.asList(lit));

        assertTrue(result.isSat(), "cdr(cons(a,b)) = b should be SAT");
    }

    @Test
    public void testNestedListStructure() {
        Variable a = factory.createVariable("a");
        Variable b = factory.createVariable("b");
        FunctionApp cons1 = factory.createFunctionApp("cons", a, b);
        FunctionApp cons2 = factory.createFunctionApp("cons", a, cons1);
        FunctionApp car = factory.createFunctionApp("car", cons2);

        Literal lit = Literal.equality(car, a);

        Result result = solver.checkSat(Arrays.asList(lit));

        assertTrue(result.isSat(), "car(cons(a, cons(a,b))) = a should be SAT");
    }

    // ========== Pure T_A Tests ==========

    @Test
    public void testReadOverWriteSameIndex() {
        Variable a = factory.createVariable("a");
        Variable i = factory.createVariable("i");
        Variable v = factory.createVariable("v");

        FunctionApp store = factory.createFunctionApp("store", a, i, v);
        FunctionApp select = factory.createFunctionApp("select", store, i);

        Literal lit = Literal.equality(select, v);

        Result result = solver.checkSat(Arrays.asList(lit));

        assertTrue(result.isSat(), "select(store(a,i,v),i) = v should be SAT");
    }

    @Test
    public void testReadOverWriteConflict() {
        Variable a = factory.createVariable("a");
        Variable i = factory.createVariable("i");
        Variable v = factory.createVariable("v");

        FunctionApp store = factory.createFunctionApp("store", a, i, v);
        FunctionApp select = factory.createFunctionApp("select", store, i);

        Literal lit = Literal.disequality(select, v);

        Result result = solver.checkSat(Arrays.asList(lit));

        assertTrue(result.isUnsat(), "select(store(a,i,v),i) ≠ v should be UNSAT");
    }

    @Test
    public void testMultipleStores() {
        Variable a = factory.createVariable("a");
        Variable i = factory.createVariable("i");
        Variable j = factory.createVariable("j");
        Variable v = factory.createVariable("v");
        Variable w = factory.createVariable("w");

        FunctionApp store1 = factory.createFunctionApp("store", a, i, v);
        FunctionApp store2 = factory.createFunctionApp("store", store1, j, w);
        FunctionApp select = factory.createFunctionApp("select", store2, j);

        Literal lit = Literal.equality(select, w);

        Result result = solver.checkSat(Arrays.asList(lit));

        assertTrue(result.isSat(), "Nested stores should work");
    }

    // ========== Mixed Theory Tests ==========

    @Test
    public void testEqualityAndLists() {
        // a = b, car(cons(a, c)) = b
        Variable a = factory.createVariable("a");
        Variable b = factory.createVariable("b");
        Variable c = factory.createVariable("c");

        FunctionApp cons = factory.createFunctionApp("cons", a, c);
        FunctionApp car = factory.createFunctionApp("car", cons);

        Literal eq1 = Literal.equality(a, b);
        Literal eq2 = Literal.equality(car, b);

        Result result = solver.checkSat(Arrays.asList(eq1, eq2));

        assertTrue(result.isSat(), "T_E + T_cons should be SAT");
    }

    @Test
    public void testEqualityAndArrays() {
        // i = j, select(store(a, i, v), j) = v
        Variable a = factory.createVariable("a");
        Variable i = factory.createVariable("i");
        Variable j = factory.createVariable("j");
        Variable v = factory.createVariable("v");

        FunctionApp store = factory.createFunctionApp("store", a, i, v);
        FunctionApp select = factory.createFunctionApp("select", store, j);

        Literal eq1 = Literal.equality(i, j);
        Literal eq2 = Literal.equality(select, v);

        Result result = solver.checkSat(Arrays.asList(eq1, eq2));

        assertTrue(result.isSat(), "T_E + T_A should be SAT");
    }

    @Test
    public void testListsAndArrays() {
        // x = cons(a, b), arr2 = store(arr1, car(x), v), select(arr2, a) = v
        Variable a = factory.createVariable("a");
        Variable b = factory.createVariable("b");
        Variable arr1 = factory.createVariable("arr1");
        Variable v = factory.createVariable("v");

        FunctionApp cons = factory.createFunctionApp("cons", a, b);
        FunctionApp car = factory.createFunctionApp("car", cons);
        FunctionApp arr2 = factory.createFunctionApp("store", arr1, car, v);
        FunctionApp select = factory.createFunctionApp("select", arr2, a);

        Literal lit = Literal.equality(select, v);

        Result result = solver.checkSat(Arrays.asList(lit));

        assertTrue(result.isSat(), "T_cons + T_A should be SAT");
    }

    @Test
    public void testAllThreeTheories() {
        // a = b, x = cons(a, c), arr2 = store(arr1, b, v), select(arr2, car(x)) = v
        Variable a = factory.createVariable("a");
        Variable b = factory.createVariable("b");
        Variable c = factory.createVariable("c");
        Variable arr1 = factory.createVariable("arr1");
        Variable v = factory.createVariable("v");

        FunctionApp cons = factory.createFunctionApp("cons", a, c);
        FunctionApp car = factory.createFunctionApp("car", cons);
        FunctionApp arr2 = factory.createFunctionApp("store", arr1, b, v);
        FunctionApp select = factory.createFunctionApp("select", arr2, car);

        Literal eq1 = Literal.equality(a, b);
        Literal eq2 = Literal.equality(select, v);

        Result result = solver.checkSat(Arrays.asList(eq1, eq2));

        assertTrue(result.isSat(), "T_E + T_cons + T_A should be SAT");
    }

    @Test
    public void testMixedTheoriesConflict() {
        // a = b, car(cons(a, c)) ≠ b
        Variable a = factory.createVariable("a");
        Variable b = factory.createVariable("b");
        Variable c = factory.createVariable("c");

        FunctionApp cons = factory.createFunctionApp("cons", a, c);
        FunctionApp car = factory.createFunctionApp("car", cons);

        Literal eq = Literal.equality(a, b);
        Literal diseq = Literal.disequality(car, b);

        Result result = solver.checkSat(Arrays.asList(eq, diseq));

        assertTrue(result.isUnsat(), "Mixed theory conflict should be UNSAT");
    }

    // ========== Edge Cases ==========

    @Test
    public void testEmptyLiterals() {
        Result result = solver.checkSat(Arrays.asList());

        assertTrue(result.isSat(), "Empty literals should be SAT");
    }

    @Test
    public void testSingleEquality() {
        Variable a = factory.createVariable("a");
        Variable b = factory.createVariable("b");

        Literal lit = Literal.equality(a, b);

        Result result = solver.checkSat(Arrays.asList(lit));

        assertTrue(result.isSat(), "Single equality should be SAT");
    }

    @Test
    public void testSingleDisequality() {
        Variable a = factory.createVariable("a");
        Variable b = factory.createVariable("b");

        Literal lit = Literal.disequality(a, b);

        Result result = solver.checkSat(Arrays.asList(lit));

        assertTrue(result.isSat(), "Single disequality should be SAT");
    }

    @Test
    public void testIsSatisfiableHelper() {
        Variable a = factory.createVariable("a");
        Variable b = factory.createVariable("b");

        Literal eq = Literal.equality(a, b);

        assertTrue(solver.isSatisfiable(Arrays.asList(eq)), "Should be satisfiable");

        Literal diseq = Literal.disequality(a, b);

        assertFalse(solver.isSatisfiable(Arrays.asList(eq, diseq)), "Should be unsatisfiable");
    }

    @Test
    public void testComplexMixedScenario() {
        // Complex scenario combining all theories
        // list1 = cons(x, y)
        // arr1 = store(arr0, car(list1), v1)
        // arr2 = store(arr1, cdr(list1), v2)
        // x = i1, y = i2
        // select(arr2, i1) = v1
        Variable x = factory.createVariable("x");
        Variable y = factory.createVariable("y");
        Variable i1 = factory.createVariable("i1");
        Variable i2 = factory.createVariable("i2");
        Variable arr0 = factory.createVariable("arr0");
        Variable v1 = factory.createVariable("v1");
        Variable v2 = factory.createVariable("v2");

        FunctionApp list1 = factory.createFunctionApp("cons", x, y);
        FunctionApp carList = factory.createFunctionApp("car", list1);
        FunctionApp cdrList = factory.createFunctionApp("cdr", list1);
        FunctionApp arr1 = factory.createFunctionApp("store", arr0, carList, v1);
        FunctionApp arr2 = factory.createFunctionApp("store", arr1, cdrList, v2);
        FunctionApp select1 = factory.createFunctionApp("select", arr2, i1);

        Literal eq1 = Literal.equality(x, i1);
        Literal eq2 = Literal.equality(y, i2);
        Literal eq3 = Literal.equality(select1, v1);

        Result result = solver.checkSat(Arrays.asList(eq1, eq2, eq3));

        assertTrue(result.isSat(), "Complex mixed scenario should be SAT");
    }
}
