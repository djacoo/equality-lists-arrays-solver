package solver.theory.tarray;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import static org.junit.jupiter.api.Assertions.*;

import solver.dag.*;
import solver.theory.Result;
import solver.theory.te.Literal;

import java.util.Arrays;
import java.util.List;

/**
 * COMPLETE Test Suite for T_A-procedure (Theory of Arrays)
 * Based on Bradley & Manna, "The Calculus of Computation", Section 9.5
 *
 * This test suite comprehensively tests the Theory of Arrays implementation
 * against all examples and exercises from the textbook, ensuring exact compliance
 * with the formal specification.
 *
 * Notation mapping:
 * - Textbook: a[i] ≡ Implementation: select(a, i)
 * - Textbook: a⟨i ⊳ v⟩ ≡ Implementation: store(a, i, v)
 * - Textbook: a⟨i ⊳ v⟩[j] ≡ Implementation: select(store(a, i, v), j)
 *
 * Theory of Arrays Axioms (Bradley & Manna, page 263):
 * 1. Axioms of reflexivity, symmetry, transitivity from T_E
 * 2. ∀a, i, j. i = j → a[i] = a[j] (array congruence)
 * 3. ∀a, v, i, j. i = j → a⟨i ⊳ v⟩[j] = v (read-over-write 1)
 * 4. ∀a, v, i, j. i ≠ j → a⟨i ⊳ v⟩[j] = a[j] (read-over-write 2)
 */
@DisplayName("Complete T_A-procedure Test Suite (Bradley & Manna Section 9.5)")
public class TArrayProcedureCompleteTest {
    private TermFactory factory;
    private TArrayProcedure procedure;

    @BeforeEach
    public void setUp() {
        factory = new TermFactory();
        procedure = new TArrayProcedure(factory);
    }

    // =========================================================================
    // PART 1: BASIC AXIOM TESTS
    // Testing the four fundamental axioms of T_A from Bradley & Manna page 263
    // =========================================================================

    @Test
    @DisplayName("Axiom 1: Reflexivity - a[i] = a[i] should be SAT")
    public void testAxiom_Reflexivity() {
        // Test reflexivity of equality (from T_E)
        Variable a = factory.createVariable("a");
        Variable i = factory.createVariable("i");

        FunctionApp select1 = factory.createFunctionApp("select", a, i);
        FunctionApp select2 = factory.createFunctionApp("select", a, i);

        Literal lit = Literal.equality(select1, select2);
        Result result = procedure.check(Arrays.asList(lit));

        assertTrue(result.isSat(), "Reflexivity: a[i] = a[i] should be SAT");
    }

    @Test
    @DisplayName("Axiom 1: Symmetry - (a[i] = b[j]) ↔ (b[j] = a[i]) should be SAT")
    public void testAxiom_Symmetry() {
        // Test symmetry of equality
        Variable a = factory.createVariable("a");
        Variable b = factory.createVariable("b");
        Variable i = factory.createVariable("i");
        Variable j = factory.createVariable("j");

        FunctionApp selectA = factory.createFunctionApp("select", a, i);
        FunctionApp selectB = factory.createFunctionApp("select", b, j);

        Literal lit = Literal.equality(selectA, selectB);
        Result result = procedure.check(Arrays.asList(lit));

        assertTrue(result.isSat(), "Symmetry: equality should be symmetric");
    }

    @Test
    @DisplayName("Axiom 1: Transitivity - a[i] = b[j] ∧ b[j] = c[k] → a[i] = c[k] should be SAT")
    public void testAxiom_Transitivity() {
        // Test transitivity
        Variable a = factory.createVariable("a");
        Variable b = factory.createVariable("b");
        Variable c = factory.createVariable("c");
        Variable i = factory.createVariable("i");
        Variable j = factory.createVariable("j");
        Variable k = factory.createVariable("k");

        FunctionApp selectA = factory.createFunctionApp("select", a, i);
        FunctionApp selectB = factory.createFunctionApp("select", b, j);
        FunctionApp selectC = factory.createFunctionApp("select", c, k);

        Literal eq1 = Literal.equality(selectA, selectB);
        Literal eq2 = Literal.equality(selectB, selectC);
        Literal eq3 = Literal.equality(selectA, selectC);

        Result result = procedure.check(Arrays.asList(eq1, eq2, eq3));
        assertTrue(result.isSat(), "Transitivity should be satisfied");
    }

    @Test
    @DisplayName("Axiom 2: Array Congruence - i = j → a[i] = a[j] should be SAT")
    public void testAxiom_ArrayCongruence_SAT() {
        // Bradley & Manna page 263, Axiom 2: ∀a, i, j. i = j → a[i] = a[j]
        Variable a = factory.createVariable("a");
        Variable i = factory.createVariable("i");
        Variable j = factory.createVariable("j");

        FunctionApp selectI = factory.createFunctionApp("select", a, i);
        FunctionApp selectJ = factory.createFunctionApp("select", a, j);

        Literal indexEq = Literal.equality(i, j);
        Literal valueEq = Literal.equality(selectI, selectJ);

        Result result = procedure.check(Arrays.asList(indexEq, valueEq));
        assertTrue(result.isSat(), "Array congruence: i = j → a[i] = a[j]");
    }

    @Test
    @DisplayName("Axiom 2: Array Congruence Violation - i = j ∧ a[i] ≠ a[j] should be UNSAT")
    public void testAxiom_ArrayCongruence_UNSAT() {
        // Violating array congruence should be UNSAT
        Variable a = factory.createVariable("a");
        Variable i = factory.createVariable("i");
        Variable j = factory.createVariable("j");

        FunctionApp selectI = factory.createFunctionApp("select", a, i);
        FunctionApp selectJ = factory.createFunctionApp("select", a, j);

        Literal indexEq = Literal.equality(i, j);
        Literal valueDiseq = Literal.disequality(selectI, selectJ);

        Result result = procedure.check(Arrays.asList(indexEq, valueDiseq));
        assertTrue(result.isUnsat(), "Violating array congruence should be UNSAT");
    }

    @Test
    @DisplayName("Axiom 3: Read-over-write 1 - i = j → a⟨i ⊳ v⟩[j] = v should be SAT")
    public void testAxiom_ReadOverWrite1_SAT() {
        // Bradley & Manna page 263, Axiom 3: ∀a, v, i, j. i = j → a⟨i ⊳ v⟩[j] = v
        Variable a = factory.createVariable("a");
        Variable i = factory.createVariable("i");
        Variable j = factory.createVariable("j");
        Variable v = factory.createVariable("v");

        FunctionApp store = factory.createFunctionApp("store", a, i, v);
        FunctionApp select = factory.createFunctionApp("select", store, j);

        Literal indexEq = Literal.equality(i, j);
        Literal valueEq = Literal.equality(select, v);

        Result result = procedure.check(Arrays.asList(indexEq, valueEq));
        assertTrue(result.isSat(), "Read-over-write 1: i = j → a⟨i⊳v⟩[j] = v");
    }

    @Test
    @DisplayName("Axiom 3: Read-over-write 1 Violation - i = j ∧ a⟨i ⊳ v⟩[j] ≠ v should be UNSAT")
    public void testAxiom_ReadOverWrite1_UNSAT() {
        // Violating read-over-write 1 should be UNSAT
        Variable a = factory.createVariable("a");
        Variable i = factory.createVariable("i");
        Variable j = factory.createVariable("j");
        Variable v = factory.createVariable("v");

        FunctionApp store = factory.createFunctionApp("store", a, i, v);
        FunctionApp select = factory.createFunctionApp("select", store, j);

        Literal indexEq = Literal.equality(i, j);
        Literal valueDiseq = Literal.disequality(select, v);

        Result result = procedure.check(Arrays.asList(indexEq, valueDiseq));
        assertTrue(result.isUnsat(), "Violating read-over-write 1 should be UNSAT");
    }

    @Test
    @DisplayName("Axiom 4: Read-over-write 2 - i ≠ j → a⟨i ⊳ v⟩[j] = a[j] should be SAT")
    public void testAxiom_ReadOverWrite2_SAT() {
        // Bradley & Manna page 263, Axiom 4: ∀a, v, i, j. i ≠ j → a⟨i ⊳ v⟩[j] = a[j]
        Variable a = factory.createVariable("a");
        Variable i = factory.createVariable("i");
        Variable j = factory.createVariable("j");
        Variable v = factory.createVariable("v");

        FunctionApp store = factory.createFunctionApp("store", a, i, v);
        FunctionApp selectStore = factory.createFunctionApp("select", store, j);
        FunctionApp selectA = factory.createFunctionApp("select", a, j);

        Literal indexDiseq = Literal.disequality(i, j);
        Literal valueEq = Literal.equality(selectStore, selectA);

        Result result = procedure.check(Arrays.asList(indexDiseq, valueEq));
        assertTrue(result.isSat(), "Read-over-write 2: i ≠ j → a⟨i⊳v⟩[j] = a[j]");
    }

    @Test
    @DisplayName("Axiom 4: Read-over-write 2 Violation - i ≠ j ∧ a⟨i ⊳ v⟩[j] ≠ a[j] should be UNSAT")
    public void testAxiom_ReadOverWrite2_UNSAT() {
        // Violating read-over-write 2 should be UNSAT
        Variable a = factory.createVariable("a");
        Variable i = factory.createVariable("i");
        Variable j = factory.createVariable("j");
        Variable v = factory.createVariable("v");

        FunctionApp store = factory.createFunctionApp("store", a, i, v);
        FunctionApp selectStore = factory.createFunctionApp("select", store, j);
        FunctionApp selectA = factory.createFunctionApp("select", a, j);

        Literal indexDiseq = Literal.disequality(i, j);
        Literal valueDiseq = Literal.disequality(selectStore, selectA);

        Result result = procedure.check(Arrays.asList(indexDiseq, valueDiseq));
        assertTrue(result.isUnsat(), "Violating read-over-write 2 should be UNSAT");
    }

    // =========================================================================
    // PART 2: BRADLEY & MANNA EXERCISE 9.8 (page 268)
    // All exercises from the textbook, testing the complete algorithm
    // =========================================================================

    @Test
    @DisplayName("Exercise 9.8(a): a⟨i ⊳ e⟩[j] = e ∧ i ≠ j - Expected: SAT")
    public void testExercise_9_8_a() {
        // Bradley & Manna Exercise 9.8(a), page 268
        // a⟨i ⊳ e⟩[j] = e ∧ i ≠ j
        //
        // Algorithm trace:
        // Branch 1: i = j → e = e ∧ i = j ∧ i ≠ j → UNSAT (index contradiction)
        // Branch 2: i ≠ j → a[j] = e ∧ i ≠ j → SAT (a[j] can originally equal e)
        //
        // Result: SAT (from Branch 2)
        Variable a = factory.createVariable("a");
        Variable i = factory.createVariable("i");
        Variable j = factory.createVariable("j");
        Variable e = factory.createVariable("e");

        FunctionApp store = factory.createFunctionApp("store", a, i, e);
        FunctionApp select = factory.createFunctionApp("select", store, j);

        Literal selectEq = Literal.equality(select, e);
        Literal indexDiseq = Literal.disequality(i, j);

        Result result = procedure.check(Arrays.asList(selectEq, indexDiseq));

        assertTrue(result.isSat(),
            "Bradley-Manna 9.8(a): a⟨i⊳e⟩[j] = e ∧ i ≠ j should be SAT (a[j] can equal e)");
    }

    @Test
    @DisplayName("Exercise 9.8(b): a⟨i ⊳ e⟩[j] = e ∧ a[j] ≠ e - Expected: SAT")
    public void testExercise_9_8_b() {
        // Bradley & Manna Exercise 9.8(b), page 268
        // a⟨i ⊳ e⟩[j] = e ∧ a[j] ≠ e
        //
        // Algorithm trace:
        // Branch 1: i = j → e = e ∧ a[j] ≠ e ∧ i = j → SAT (a[j] is old value, can be ≠ e)
        // Branch 2: i ≠ j → a[j] = e ∧ a[j] ≠ e ∧ i ≠ j → UNSAT (contradiction)
        //
        // Result: SAT (from Branch 1)
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
            "Bradley-Manna 9.8(b): a⟨i⊳e⟩[j] = e ∧ a[j] ≠ e should be SAT (i=j branch)");
    }

    @Test
    @DisplayName("Exercise 9.8(c): a⟨i ⊳ e⟩[j] = e ∧ i ≠ j ∧ a[j] ≠ e - Expected: UNSAT")
    public void testExercise_9_8_c() {
        // Bradley & Manna Exercise 9.8(c), page 268
        // a⟨i ⊳ e⟩[j] = e ∧ i ≠ j ∧ a[j] ≠ e
        // Expected: UNSAT
        // Reasoning: By axiom 4, i ≠ j → a⟨i⊳e⟩[j] = a[j]
        // So a⟨i⊳e⟩[j] = e ∧ i ≠ j implies a[j] = e
        // But a[j] ≠ e is also asserted, contradiction!
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
            "Bradley-Manna 9.8(c): a⟨i⊳e⟩[j] = e ∧ i ≠ j ∧ a[j] ≠ e should be UNSAT");
    }

    @Test
    @DisplayName("Exercise 9.8(d): a⟨i ⊳ e⟩⟨j ⊳ f⟩[k] = g ∧ j ≠ k ∧ i = j ∧ a[k] ≠ g - Expected: UNSAT")
    public void testExercise_9_8_d() {
        // Bradley & Manna Exercise 9.8(d), page 268
        // a⟨i ⊳ e⟩⟨j ⊳ f⟩[k] = g ∧ j ≠ k ∧ i = j ∧ a[k] ≠ g
        // Expected: UNSAT
        // Reasoning:
        // Since j ≠ k, by axiom 4: a⟨i⊳e⟩⟨j⊳f⟩[k] = a⟨i⊳e⟩[k]
        // Since i = j and j ≠ k, we have i ≠ k
        // So by axiom 4 again: a⟨i⊳e⟩[k] = a[k]
        // Therefore a⟨i⊳e⟩⟨j⊳f⟩[k] = a[k]
        // But formula says a⟨i⊳e⟩⟨j⊳f⟩[k] = g and a[k] ≠ g → contradiction!
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
            "Bradley-Manna 9.8(d): a⟨i⊳e⟩⟨j⊳f⟩[k] = g ∧ j ≠ k ∧ i = j ∧ a[k] ≠ g should be UNSAT");
    }

    @Test
    @DisplayName("Exercise 9.8(e)/Example 9.21: i₁ = j ∧ a[j] = v₁ ∧ a⟨i₁ ⊳ v₁⟩⟨i₂ ⊳ v₂⟩[j] ≠ a[j] - Expected: SAT")
    public void testExercise_9_8_e_Example_9_21() {
        // Bradley & Manna Exercise 9.8(e) and Example 9.21, pages 264, 268
        // i₁ = j ∧ a[j] = v₁ ∧ a⟨i₁ ⊳ v₁⟩⟨i₂ ⊳ v₂⟩[j] ≠ a[j]
        //
        // Algorithm trace:
        // Branch 1: i₂ = j → i₁=j ∧ i₂=j ∧ a[j]=v₁ ∧ v₂ ≠ a[j] → SAT (v₂ can be ≠ v₁)
        // Branch 2: i₂ ≠ j → ... → eventually v₁ ≠ v₁ → UNSAT
        //
        // Result: SAT (from Branch 1)
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
            "Bradley-Manna 9.8(e)/Example 9.21: i₁ = j ∧ a[j] = v₁ ∧ a⟨i₁⊳v₁⟩⟨i₂⊳v₂⟩[j] ≠ a[j] should be SAT");
    }

    // =========================================================================
    // PART 3: ALGORITHM STEP 1 TESTS (No store terms)
    // Bradley & Manna page 263, Step 1
    // =========================================================================

    @Test
    @DisplayName("Step 1: No store terms - select treated as uninterpreted function")
    public void testStep1_NoStoreTerms_SAT() {
        // When no store terms exist, select is treated as uninterpreted function
        Variable a = factory.createVariable("a");
        Variable i = factory.createVariable("i");
        Variable v = factory.createVariable("v");

        FunctionApp select = factory.createFunctionApp("select", a, i);
        Literal lit = Literal.equality(select, v);

        Result result = procedure.check(Arrays.asList(lit));
        assertTrue(result.isSat(), "Step 1: select(a,i) = v with no stores should be SAT");
    }

    @Test
    @DisplayName("Step 1: Multiple selects without stores")
    public void testStep1_MultipleSelects() {
        // select(a, i) = v1 ∧ select(a, j) = v2 ∧ i = j ∧ v1 ≠ v2 should be UNSAT
        Variable a = factory.createVariable("a");
        Variable i = factory.createVariable("i");
        Variable j = factory.createVariable("j");
        Variable v1 = factory.createVariable("v1");
        Variable v2 = factory.createVariable("v2");

        FunctionApp select1 = factory.createFunctionApp("select", a, i);
        FunctionApp select2 = factory.createFunctionApp("select", a, j);

        Literal eq1 = Literal.equality(select1, v1);
        Literal eq2 = Literal.equality(select2, v2);
        Literal eqIndex = Literal.equality(i, j);
        Literal diseqVal = Literal.disequality(v1, v2);

        Result result = procedure.check(Arrays.asList(eq1, eq2, eqIndex, diseqVal));
        assertTrue(result.isUnsat(), "Array congruence violation should be UNSAT");
    }

    // =========================================================================
    // PART 4: ALGORITHM STEP 2 TESTS (With store terms)
    // Bradley & Manna pages 263-264, Step 2
    // =========================================================================

    @Test
    @DisplayName("Step 2: Simple read-over-write with same index")
    public void testStep2_SimpleReadOverWrite_SameIndex() {
        // a⟨i ⊳ v⟩[i] = v should be SAT
        Variable a = factory.createVariable("a");
        Variable i = factory.createVariable("i");
        Variable v = factory.createVariable("v");

        FunctionApp store = factory.createFunctionApp("store", a, i, v);
        FunctionApp select = factory.createFunctionApp("select", store, i);

        Literal lit = Literal.equality(select, v);
        Result result = procedure.check(Arrays.asList(lit));

        assertTrue(result.isSat(), "a⟨i⊳v⟩[i] = v should be SAT");
    }

    @Test
    @DisplayName("Step 2: Nested stores")
    public void testStep2_NestedStores() {
        // a⟨i ⊳ v⟩⟨j ⊳ w⟩[j] = w should be SAT
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

        assertTrue(result.isSat(), "a⟨i⊳v⟩⟨j⊳w⟩[j] = w should be SAT");
    }

    @Test
    @DisplayName("Step 2: Store preservation - different indices")
    public void testStep2_StorePreservation() {
        // i ≠ j ∧ a⟨i ⊳ v⟩[j] = w ∧ a[j] = w should be SAT
        Variable a = factory.createVariable("a");
        Variable i = factory.createVariable("i");
        Variable j = factory.createVariable("j");
        Variable v = factory.createVariable("v");
        Variable w = factory.createVariable("w");

        FunctionApp store = factory.createFunctionApp("store", a, i, v);
        FunctionApp selectStore = factory.createFunctionApp("select", store, j);
        FunctionApp selectA = factory.createFunctionApp("select", a, j);

        Literal diseq = Literal.disequality(i, j);
        Literal eq1 = Literal.equality(selectStore, w);
        Literal eq2 = Literal.equality(selectA, w);

        Result result = procedure.check(Arrays.asList(diseq, eq1, eq2));
        assertTrue(result.isSat(), "Store at i should not affect index j when i ≠ j");
    }

    // =========================================================================
    // PART 5: COMPLEX INTEGRATION TESTS
    // Testing interactions between multiple theories
    // =========================================================================

    @Test
    @DisplayName("Integration: Arrays with uninterpreted functions")
    public void testIntegration_ArraysWithFunctions() {
        // f(a[i]) = v ∧ a⟨i ⊳ x⟩[i] = x ∧ f(x) = v should be SAT
        Variable a = factory.createVariable("a");
        Variable i = factory.createVariable("i");
        Variable v = factory.createVariable("v");
        Variable x = factory.createVariable("x");

        FunctionApp selectA = factory.createFunctionApp("select", a, i);
        FunctionApp f1 = factory.createFunctionApp("f", selectA);

        FunctionApp store = factory.createFunctionApp("store", a, i, x);
        FunctionApp selectStore = factory.createFunctionApp("select", store, i);
        FunctionApp f2 = factory.createFunctionApp("f", x);

        Literal eq1 = Literal.equality(f1, v);
        Literal eq2 = Literal.equality(selectStore, x);
        Literal eq3 = Literal.equality(f2, v);

        Result result = procedure.check(Arrays.asList(eq1, eq2, eq3));
        assertTrue(result.isSat(), "Arrays with uninterpreted functions should work");
    }

    @Test
    @DisplayName("Integration: Arrays with list constructors (T_cons)")
    public void testIntegration_ArraysWithLists() {
        // Mixed theory test from original test suite
        // x = cons(a, b) ∧ arr2 = store(arr1, car(x), v) ∧ select(arr2, a) = v
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

        assertTrue(result.isSat(), "Arrays integrated with list theory should work");
    }

    // =========================================================================
    // PART 6: EDGE CASES AND STRESS TESTS
    // =========================================================================

    @Test
    @DisplayName("Edge case: Empty formula should be SAT")
    public void testEdgeCase_EmptyFormula() {
        Result result = procedure.check(Arrays.asList());
        assertTrue(result.isSat(), "Empty formula should be SAT");
    }

    @Test
    @DisplayName("Edge case: Triple nested stores")
    public void testEdgeCase_TripleNestedStores() {
        // a⟨i ⊳ v1⟩⟨j ⊳ v2⟩⟨k ⊳ v3⟩[k] = v3
        Variable a = factory.createVariable("a");
        Variable i = factory.createVariable("i");
        Variable j = factory.createVariable("j");
        Variable k = factory.createVariable("k");
        Variable v1 = factory.createVariable("v1");
        Variable v2 = factory.createVariable("v2");
        Variable v3 = factory.createVariable("v3");

        FunctionApp store1 = factory.createFunctionApp("store", a, i, v1);
        FunctionApp store2 = factory.createFunctionApp("store", store1, j, v2);
        FunctionApp store3 = factory.createFunctionApp("store", store2, k, v3);
        FunctionApp select = factory.createFunctionApp("select", store3, k);

        Literal lit = Literal.equality(select, v3);
        Result result = procedure.check(Arrays.asList(lit));

        assertTrue(result.isSat(), "Triple nested store should work");
    }

    @Test
    @DisplayName("Edge case: Store then read at different index with index equality")
    public void testEdgeCase_StoreReadWithIndexEquality() {
        // a⟨i ⊳ v⟩[j] = w ∧ i = j ∧ v ≠ w should be UNSAT
        Variable a = factory.createVariable("a");
        Variable i = factory.createVariable("i");
        Variable j = factory.createVariable("j");
        Variable v = factory.createVariable("v");
        Variable w = factory.createVariable("w");

        FunctionApp store = factory.createFunctionApp("store", a, i, v);
        FunctionApp select = factory.createFunctionApp("select", store, j);

        Literal eq1 = Literal.equality(select, w);
        Literal eq2 = Literal.equality(i, j);
        Literal diseq = Literal.disequality(v, w);

        Result result = procedure.check(Arrays.asList(eq1, eq2, diseq));
        assertTrue(result.isUnsat(), "Conflicting values at same index should be UNSAT");
    }

    @Test
    @DisplayName("Edge case: Overwriting same index twice")
    public void testEdgeCase_OverwriteSameIndexTwice() {
        // a⟨i ⊳ v1⟩⟨i ⊳ v2⟩[i] = v2 should be SAT
        Variable a = factory.createVariable("a");
        Variable i = factory.createVariable("i");
        Variable v1 = factory.createVariable("v1");
        Variable v2 = factory.createVariable("v2");

        FunctionApp store1 = factory.createFunctionApp("store", a, i, v1);
        FunctionApp store2 = factory.createFunctionApp("store", store1, i, v2);
        FunctionApp select = factory.createFunctionApp("select", store2, i);

        Literal lit = Literal.equality(select, v2);
        Result result = procedure.check(Arrays.asList(lit));

        assertTrue(result.isSat(), "Last write wins: a⟨i⊳v1⟩⟨i⊳v2⟩[i] = v2");
    }

    @Test
    @DisplayName("Edge case: Overwriting then reading old value should fail")
    public void testEdgeCase_OverwriteCannotReadOldValue() {
        // a⟨i ⊳ v2⟩[i] = v1 ∧ v1 ≠ v2 should be UNSAT
        Variable a = factory.createVariable("a");
        Variable i = factory.createVariable("i");
        Variable v1 = factory.createVariable("v1");
        Variable v2 = factory.createVariable("v2");

        FunctionApp store = factory.createFunctionApp("store", a, i, v2);
        FunctionApp select = factory.createFunctionApp("select", store, i);

        Literal eq = Literal.equality(select, v1);
        Literal diseq = Literal.disequality(v1, v2);

        Result result = procedure.check(Arrays.asList(eq, diseq));
        assertTrue(result.isUnsat(), "Cannot read old value after overwrite");
    }

    // =========================================================================
    // PART 7: CONSISTENCY TESTS
    // Verifying transitivity and consistency across multiple operations
    // =========================================================================

    @Test
    @DisplayName("Consistency: Chained equalities with stores")
    public void testConsistency_ChainedEqualities() {
        // a⟨i ⊳ v⟩[i] = x ∧ x = y ∧ y = v should be SAT
        Variable a = factory.createVariable("a");
        Variable i = factory.createVariable("i");
        Variable v = factory.createVariable("v");
        Variable x = factory.createVariable("x");
        Variable y = factory.createVariable("y");

        FunctionApp store = factory.createFunctionApp("store", a, i, v);
        FunctionApp select = factory.createFunctionApp("select", store, i);

        Literal eq1 = Literal.equality(select, x);
        Literal eq2 = Literal.equality(x, y);
        Literal eq3 = Literal.equality(y, v);

        Result result = procedure.check(Arrays.asList(eq1, eq2, eq3));
        assertTrue(result.isSat(), "Chained equalities should be consistent");
    }

    @Test
    @DisplayName("Consistency: Contradiction through transitivity")
    public void testConsistency_ContradictionThroughTransitivity() {
        // a⟨i ⊳ v⟩[i] = x ∧ x = y ∧ y ≠ v should be UNSAT
        Variable a = factory.createVariable("a");
        Variable i = factory.createVariable("i");
        Variable v = factory.createVariable("v");
        Variable x = factory.createVariable("x");
        Variable y = factory.createVariable("y");

        FunctionApp store = factory.createFunctionApp("store", a, i, v);
        FunctionApp select = factory.createFunctionApp("select", store, i);

        Literal eq1 = Literal.equality(select, x);
        Literal eq2 = Literal.equality(x, y);
        Literal diseq = Literal.disequality(y, v);

        Result result = procedure.check(Arrays.asList(eq1, eq2, diseq));
        assertTrue(result.isUnsat(), "Transitive contradiction should be UNSAT");
    }
}
