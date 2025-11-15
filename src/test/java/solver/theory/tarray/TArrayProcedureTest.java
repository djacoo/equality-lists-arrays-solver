package solver.theory.tarray;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import static org.junit.jupiter.api.Assertions.*;

import solver.dag.*;
import solver.theory.Result;
import solver.theory.te.Literal;

import java.util.Arrays;
import java.util.List;

/**
 * Tests for T_A-procedure (Theory of Arrays).
 */
public class TArrayProcedureTest {
    private TermFactory factory;
    private TArrayProcedure procedure;

    @BeforeEach
    public void setUp() {
        factory = new TermFactory();
        procedure = new TArrayProcedure(factory);
    }

    @Test
    public void testFactoryAccessor() {
        assertNotNull(procedure.getFactory(), "Factory should be accessible");
        assertSame(factory, procedure.getFactory(), "Should return same factory");
    }

    @Test
    public void testEmptyLiterals() {
        List<Literal> literals = Arrays.asList();

        Result result = procedure.check(literals);

        assertTrue(result.isSat(), "Empty literals should be SAT");
    }

    @Test
    public void testPureEqualityNonArrays() {
        Variable a = factory.createVariable("a");
        Variable b = factory.createVariable("b");

        Literal lit = Literal.equality(a, b);

        Result result = procedure.check(Arrays.asList(lit));

        assertTrue(result.isSat(), "Pure equality should be SAT");
    }

    @Test
    public void testPureEqualityConflict() {
        Variable a = factory.createVariable("a");
        Variable b = factory.createVariable("b");

        Literal eq = Literal.equality(a, b);
        Literal diseq = Literal.disequality(a, b);

        Result result = procedure.check(Arrays.asList(eq, diseq));

        assertTrue(result.isUnsat(), "a=b AND a≠b should be UNSAT");
    }

    @Test
    public void testReadOverWriteSameIndex() {
        // select(store(a, i, v), i) = v should be SAT
        Variable a = factory.createVariable("a");
        Variable i = factory.createVariable("i");
        Variable v = factory.createVariable("v");

        FunctionApp store = factory.createFunctionApp("store", a, i, v);
        FunctionApp select = factory.createFunctionApp("select", store, i);

        Literal lit = Literal.equality(select, v);

        Result result = procedure.check(Arrays.asList(lit));

        assertTrue(result.isSat(), "select(store(a,i,v),i) = v should be SAT");
    }

    @Test
    public void testReadOverWriteSameIndexConflict() {
        // select(store(a, i, v), i) ≠ v should be UNSAT
        Variable a = factory.createVariable("a");
        Variable i = factory.createVariable("i");
        Variable v = factory.createVariable("v");

        FunctionApp store = factory.createFunctionApp("store", a, i, v);
        FunctionApp select = factory.createFunctionApp("select", store, i);

        Literal lit = Literal.disequality(select, v);

        Result result = procedure.check(Arrays.asList(lit));

        assertTrue(result.isUnsat(), "select(store(a,i,v),i) ≠ v should be UNSAT");
    }

    @Test
    public void testReadOverWriteDifferentIndex() {
        // i ≠ j, select(store(a,i,v),j) = select(a,j) should be SAT
        Variable a = factory.createVariable("a");
        Variable i = factory.createVariable("i");
        Variable j = factory.createVariable("j");
        Variable v = factory.createVariable("v");

        FunctionApp store = factory.createFunctionApp("store", a, i, v);
        FunctionApp selectStore = factory.createFunctionApp("select", store, j);
        FunctionApp selectA = factory.createFunctionApp("select", a, j);

        Literal diseq = Literal.disequality(i, j);
        Literal eq = Literal.equality(selectStore, selectA);

        Result result = procedure.check(Arrays.asList(diseq, eq));

        assertTrue(result.isSat(), "Different index case should be SAT");
    }

    @Test
    public void testReadOverWriteDifferentIndexConflict() {
        // i ≠ j, select(store(a,i,v),j) ≠ select(a,j) should be UNSAT
        Variable a = factory.createVariable("a");
        Variable i = factory.createVariable("i");
        Variable j = factory.createVariable("j");
        Variable v = factory.createVariable("v");

        FunctionApp store = factory.createFunctionApp("store", a, i, v);
        FunctionApp selectStore = factory.createFunctionApp("select", store, j);
        FunctionApp selectA = factory.createFunctionApp("select", a, j);

        Literal indexDiseq = Literal.disequality(i, j);
        Literal selectDiseq = Literal.disequality(selectStore, selectA);

        Result result = procedure.check(Arrays.asList(indexDiseq, selectDiseq));

        assertTrue(result.isUnsat(), "Different index with select mismatch should be UNSAT");
    }

    @Test
    public void testSimpleStoreEquality() {
        // store(a, i, v) = a is satisfiable (could be true if a already has v at i)
        Variable a = factory.createVariable("a");
        Variable i = factory.createVariable("i");
        Variable v = factory.createVariable("v");

        FunctionApp store = factory.createFunctionApp("store", a, i, v);

        Literal lit = Literal.equality(store, a);

        Result result = procedure.check(Arrays.asList(lit));

        assertTrue(result.isSat(), "store(a,i,v) = a is satisfiable");
    }

    @Test
    public void testNoSelectOperations() {
        // Just a store term without select - should delegate to T_E
        Variable a = factory.createVariable("a");
        Variable i = factory.createVariable("i");
        Variable v = factory.createVariable("v");
        Variable b = factory.createVariable("b");

        FunctionApp store = factory.createFunctionApp("store", a, i, v);

        Literal lit = Literal.equality(store, b);

        Result result = procedure.check(Arrays.asList(lit));

        assertTrue(result.isSat(), "Store without select should be SAT");
    }

    @Test
    public void testMultipleStores() {
        // store(store(a, i, v), j, w) with appropriate selects
        Variable a = factory.createVariable("a");
        Variable i = factory.createVariable("i");
        Variable j = factory.createVariable("j");
        Variable v = factory.createVariable("v");
        Variable w = factory.createVariable("w");

        FunctionApp store1 = factory.createFunctionApp("store", a, i, v);
        FunctionApp store2 = factory.createFunctionApp("store", store1, j, w);
        FunctionApp select = factory.createFunctionApp("select", store2, j);

        Literal lit = Literal.equality(select, w);

        Result result = procedure.check(Arrays.asList(lit));

        assertTrue(result.isSat(), "Nested stores should work correctly");
    }

    @Test
    public void testSelectOnNonStore() {
        // select(a, i) = v where a is not a store
        Variable a = factory.createVariable("a");
        Variable i = factory.createVariable("i");
        Variable v = factory.createVariable("v");

        FunctionApp select = factory.createFunctionApp("select", a, i);

        Literal lit = Literal.equality(select, v);

        Result result = procedure.check(Arrays.asList(lit));

        assertTrue(result.isSat(), "Select on non-store should be SAT");
    }

    @Test
    public void testComplexArrayExpression() {
        // a1 = store(a, i, v)
        // a2 = store(a1, j, w)
        // select(a2, i) = v
        // i ≠ j
        Variable a = factory.createVariable("a");
        Variable i = factory.createVariable("i");
        Variable j = factory.createVariable("j");
        Variable v = factory.createVariable("v");
        Variable w = factory.createVariable("w");

        FunctionApp a1 = factory.createFunctionApp("store", a, i, v);
        FunctionApp a2 = factory.createFunctionApp("store", a1, j, w);
        FunctionApp select = factory.createFunctionApp("select", a2, i);

        Literal eq1 = Literal.equality(select, v);
        Literal diseq = Literal.disequality(i, j);

        Result result = procedure.check(Arrays.asList(eq1, diseq));

        assertTrue(result.isSat(), "Complex array expression should be SAT");
    }

    @Test
    public void testArrayWithLists() {
        // Mixed theory: arrays and lists
        // x = cons(a, b)
        // arr2 = store(arr1, car(x), v)
        // select(arr2, a) = v
        Variable a = factory.createVariable("a");
        Variable b = factory.createVariable("b");
        Variable arr1 = factory.createVariable("arr1");
        Variable v = factory.createVariable("v");

        FunctionApp cons = factory.createFunctionApp("cons", a, b);
        FunctionApp car = factory.createFunctionApp("car", cons);
        FunctionApp arr2 = factory.createFunctionApp("store", arr1, car, v);
        FunctionApp select = factory.createFunctionApp("select", arr2, a);

        Literal lit = Literal.equality(select, v);

        Result result = procedure.check(Arrays.asList(lit));

        assertTrue(result.isSat(), "Arrays with lists should be SAT");
    }

    @Test
    public void testStorePreservesOtherIndices() {
        // a1 = store(a, i, v)
        // select(a, j) = w
        // select(a1, j) = w
        // i ≠ j
        // Should be SAT (storing at i doesn't affect j)
        Variable a = factory.createVariable("a");
        Variable i = factory.createVariable("i");
        Variable j = factory.createVariable("j");
        Variable v = factory.createVariable("v");
        Variable w = factory.createVariable("w");

        FunctionApp a1 = factory.createFunctionApp("store", a, i, v);
        FunctionApp selectA = factory.createFunctionApp("select", a, j);
        FunctionApp selectA1 = factory.createFunctionApp("select", a1, j);

        Literal eq1 = Literal.equality(selectA, w);
        Literal eq2 = Literal.equality(selectA1, w);
        Literal diseq = Literal.disequality(i, j);

        Result result = procedure.check(Arrays.asList(eq1, eq2, diseq));

        assertTrue(result.isSat(), "Store preserves other indices");
    }

    @Test
    public void testConflictingStoreValues() {
        // a1 = store(a, i, v)
        // select(a1, i) = w
        // v ≠ w
        // Should be UNSAT
        Variable a = factory.createVariable("a");
        Variable i = factory.createVariable("i");
        Variable v = factory.createVariable("v");
        Variable w = factory.createVariable("w");

        FunctionApp a1 = factory.createFunctionApp("store", a, i, v);
        FunctionApp select = factory.createFunctionApp("select", a1, i);

        Literal eq = Literal.equality(select, w);
        Literal diseq = Literal.disequality(v, w);

        Result result = procedure.check(Arrays.asList(eq, diseq));

        assertTrue(result.isUnsat(), "Conflicting store values should be UNSAT");
    }

    @Test
    public void testDefaultConstructor() {
        TArrayProcedure proc = new TArrayProcedure();
        assertNotNull(proc.getFactory(), "Default constructor should create factory");

        Variable a = factory.createVariable("a");
        Variable b = factory.createVariable("b");

        Literal lit = Literal.equality(a, b);

        Result result = proc.check(Arrays.asList(lit));

        assertTrue(result.isSat(), "Should work with default constructor");
    }
}
