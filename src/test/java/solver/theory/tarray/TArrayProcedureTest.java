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

    // ===================================================================
    // Bradley-Manna Exercise 9.8 Tests
    // These tests come from the textbook (page 268)
    // ===================================================================

    @Test
    public void testBradleyManna_9_8_a() {
        // a⟨i ⊳ e⟩[j] = e ∧ i ≠ j
        // By axiom 4: if i ≠ j then a⟨i⊳e⟩[j] = a[j]
        // So this is equivalent to: a[j] = e ∧ i ≠ j
        // This is SAT (a[j] can originally equal e)
        Variable a = factory.createVariable("a");
        Variable i = factory.createVariable("i");
        Variable j = factory.createVariable("j");
        Variable e = factory.createVariable("e");

        FunctionApp store = factory.createFunctionApp("store", a, i, e);
        FunctionApp select = factory.createFunctionApp("select", store, j);

        Literal eq = Literal.equality(select, e);
        Literal diseq = Literal.disequality(i, j);

        Result result = procedure.check(Arrays.asList(eq, diseq));

        assertTrue(result.isSat(),
            "Bradley-Manna 9.8(a): a⟨i⊳e⟩[j]=e ∧ i≠j should be SAT (a[j] can equal e)");
    }

    @Test
    public void testBradleyManna_9_8_b() {
        // a⟨i ⊳ e⟩[j] = e ∧ a[j] ≠ e
        // Branch 1 (i=j): e = e ∧ a[j] ≠ e is SAT (a[j] is old value, can be ≠ e)
        // Branch 2 (i≠j): a[j] = e ∧ a[j] ≠ e is UNSAT
        // Overall: SAT
        Variable a = factory.createVariable("a");
        Variable i = factory.createVariable("i");
        Variable j = factory.createVariable("j");
        Variable e = factory.createVariable("e");

        FunctionApp store = factory.createFunctionApp("store", a, i, e);
        FunctionApp selectStore = factory.createFunctionApp("select", store, j);
        FunctionApp selectA = factory.createFunctionApp("select", a, j);

        Literal eq1 = Literal.equality(selectStore, e);
        Literal diseq = Literal.disequality(selectA, e);

        Result result = procedure.check(Arrays.asList(eq1, diseq));

        assertTrue(result.isSat(),
            "Bradley-Manna 9.8(b): a⟨i⊳e⟩[j]=e ∧ a[j]≠e should be SAT (i=j case)");
    }

    @Test
    public void testBradleyManna_9_8_c() {
        // a⟨i ⊳ e⟩[j] = e ∧ i ≠ j ∧ a[j] ≠ e
        // Expected: UNSAT
        Variable a = factory.createVariable("a");
        Variable i = factory.createVariable("i");
        Variable j = factory.createVariable("j");
        Variable e = factory.createVariable("e");

        FunctionApp store = factory.createFunctionApp("store", a, i, e);
        FunctionApp selectStore = factory.createFunctionApp("select", store, j);
        FunctionApp selectA = factory.createFunctionApp("select", a, j);

        Literal eq = Literal.equality(selectStore, e);
        Literal diseq1 = Literal.disequality(i, j);
        Literal diseq2 = Literal.disequality(selectA, e);

        Result result = procedure.check(Arrays.asList(eq, diseq1, diseq2));

        assertTrue(result.isUnsat(),
            "Bradley-Manna 9.8(c): a⟨i⊳e⟩[j]=e ∧ i≠j ∧ a[j]≠e should be UNSAT");
    }

    @Test
    public void testBradleyManna_9_8_d() {
        // a⟨i ⊳ e⟩⟨j ⊳ f⟩[k] = g ∧ j ≠ k ∧ i = j ∧ a[k] ≠ g
        // Expected: UNSAT
        Variable a = factory.createVariable("a");
        Variable i = factory.createVariable("i");
        Variable j = factory.createVariable("j");
        Variable k = factory.createVariable("k");
        Variable e = factory.createVariable("e");
        Variable f = factory.createVariable("f");
        Variable g = factory.createVariable("g");

        FunctionApp store1 = factory.createFunctionApp("store", a, i, e);
        FunctionApp store2 = factory.createFunctionApp("store", store1, j, f);
        FunctionApp selectStore2 = factory.createFunctionApp("select", store2, k);
        FunctionApp selectA = factory.createFunctionApp("select", a, k);

        Literal eq1 = Literal.equality(selectStore2, g);
        Literal diseq1 = Literal.disequality(j, k);
        Literal eq2 = Literal.equality(i, j);
        Literal diseq2 = Literal.disequality(selectA, g);

        Result result = procedure.check(Arrays.asList(eq1, diseq1, eq2, diseq2));

        assertTrue(result.isUnsat(),
            "Bradley-Manna 9.8(d): a⟨i⊳e⟩⟨j⊳f⟩[k]=g ∧ j≠k ∧ i=j ∧ a[k]≠g should be UNSAT");
    }

    @Test
    public void testBradleyManna_9_8_e() {
        // i₁ = j ∧ a[j] = v₁ ∧ a⟨i₁ ⊳ v₁⟩⟨i₂ ⊳ v₂⟩[j] ≠ a[j]
        // This is Example 9.21 from the textbook
        // Branch 1 (i₂=j): i₁=j ∧ i₂=j ∧ a[j]=v₁ ∧ v₂ ≠ v₁ → SAT
        // Branch 2 (i₂≠j): leads to v₁ ≠ v₁ → UNSAT
        // Overall: SAT (from Branch 1)
        Variable a = factory.createVariable("a");
        Variable j = factory.createVariable("j");
        Variable i1 = factory.createVariable("i1");
        Variable i2 = factory.createVariable("i2");
        Variable v1 = factory.createVariable("v1");
        Variable v2 = factory.createVariable("v2");

        FunctionApp selectA = factory.createFunctionApp("select", a, j);
        FunctionApp store1 = factory.createFunctionApp("store", a, i1, v1);
        FunctionApp store2 = factory.createFunctionApp("store", store1, i2, v2);
        FunctionApp selectStore2 = factory.createFunctionApp("select", store2, j);

        Literal eq1 = Literal.equality(i1, j);
        Literal eq2 = Literal.equality(selectA, v1);
        Literal diseq = Literal.disequality(selectStore2, selectA);

        Result result = procedure.check(Arrays.asList(eq1, eq2, diseq));

        assertTrue(result.isSat(),
            "Bradley-Manna 9.8(e): i₁=j ∧ a[j]=v₁ ∧ a⟨i₁⊳v₁⟩⟨i₂⊳v₂⟩[j]≠a[j] should be SAT");
    }

    /**
     * Bradley & Manna Example 9.21 (page 264).
     *
     * F : i₁ = j ∧ i₁ ≠ i₂ ∧ a[j] = v₁ ∧ a⟨i₁ ⊳ v₁⟩⟨i₂ ⊳ v₂⟩[j] ≠ a[j]
     *
     * Using B&M mathematical notation:
     * - a[j] means select(a, j)
     * - a⟨i ⊳ v⟩ means store(a, i, v)
     *
     * This is TA-UNSATISFIABLE. The reasoning (page 264):
     *
     * Select read-over-write term: a⟨i₁⊳v₁⟩⟨i₂⊳v₂⟩[j]
     *
     * Branch 1 (i₂=j):
     *   F₁: i₂=j ∧ i₁=j ∧ i₁≠i₂ ∧ a[j]=v₁ ∧ v₂≠a[j]
     *   First two literals imply i₁=i₂, contradicting third → UNSAT
     *
     * Branch 2 (i₂≠j):
     *   F₂: i₂≠j ∧ i₁=j ∧ i₁≠i₂ ∧ a[j]=v₁ ∧ a⟨i₁⊳v₁⟩[j]≠a[j]
     *   Sub-branch 2.1 (i₁=j):
     *     F₃: i₁=j ∧ i₂≠j ∧ i₁=j ∧ i₁≠i₂ ∧ a[j]=v₁ ∧ v₁≠a[j]
     *     Last two literals: a[j]=v₁ and v₁≠a[j] → UNSAT
     *   Sub-branch 2.2 (i₁≠j):
     *     F₄: i₁≠j ∧ i₂≠j ∧ i₁=j ∧ i₁≠i₂ ∧ a[j]=v₁ ∧ a[j]≠a[j]
     *     First and third literals contradict → UNSAT
     *
     * All branches UNSAT → F is TA-unsatisfiable
     */
    @Test
    public void testBradleyMannaExample921() {
        // F : i₁ = j ∧ i₁ ≠ i₂ ∧ a[j] = v₁ ∧ a⟨i₁ ⊳ v₁⟩⟨i₂ ⊳ v₂⟩[j] ≠ a[j]
        Variable a = factory.createVariable("a");
        Variable i1 = factory.createVariable("i1");
        Variable i2 = factory.createVariable("i2");
        Variable j = factory.createVariable("j");
        Variable v1 = factory.createVariable("v1");
        Variable v2 = factory.createVariable("v2");

        // Build terms using B&M notation equivalents
        FunctionApp aAtJ = factory.createFunctionApp("select", a, j);           // a[j]
        FunctionApp store1 = factory.createFunctionApp("store", a, i1, v1);    // a⟨i₁⊳v₁⟩
        FunctionApp store2 = factory.createFunctionApp("store", store1, i2, v2); // a⟨i₁⊳v₁⟩⟨i₂⊳v₂⟩
        FunctionApp store2AtJ = factory.createFunctionApp("select", store2, j);  // a⟨i₁⊳v₁⟩⟨i₂⊳v₂⟩[j]

        // Literals from Example 9.21
        Literal eq1 = Literal.equality(i1, j);          // i₁ = j
        Literal diseq1 = Literal.disequality(i1, i2);   // i₁ ≠ i₂
        Literal eq2 = Literal.equality(aAtJ, v1);       // a[j] = v₁
        Literal diseq2 = Literal.disequality(store2AtJ, aAtJ); // a⟨i₁⊳v₁⟩⟨i₂⊳v₂⟩[j] ≠ a[j]

        TArrayProcedure procedure = new TArrayProcedure(factory);
        Result result = procedure.check(Arrays.asList(eq1, diseq1, eq2, diseq2));

        assertTrue(result.isUnsat(),
            "Bradley & Manna Example 9.21: i₁=j ∧ i₁≠i₂ ∧ a[j]=v₁ ∧ a⟨i₁⊳v₁⟩⟨i₂⊳v₂⟩[j]≠a[j] should be UNSAT");
        assertTrue(result.getConflict().isPresent(),
            "Should have conflict explanation");
    }
}
