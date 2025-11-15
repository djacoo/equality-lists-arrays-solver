package solver.theory.tarray;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import static org.junit.jupiter.api.Assertions.*;

import solver.dag.*;
import solver.theory.te.Literal;

import java.util.Arrays;
import java.util.List;
import java.util.Set;

/**
 * Tests for T_A symbol recognition.
 */
public class TArraySymbolsTest {
    private TermFactory factory;

    @BeforeEach
    public void setUp() {
        factory = new TermFactory();
    }

    @Test
    public void testIsSelectTrue() {
        Variable a = factory.createVariable("a");
        Variable i = factory.createVariable("i");
        FunctionApp select = factory.createFunctionApp("select", a, i);

        assertTrue(TArraySymbols.isSelect(select), "select(a, i) should be recognized");
    }

    @Test
    public void testIsSelectFalseWrongSymbol() {
        Variable a = factory.createVariable("a");
        Variable i = factory.createVariable("i");
        FunctionApp f = factory.createFunctionApp("f", a, i);

        assertFalse(TArraySymbols.isSelect(f), "f(a, i) should not be recognized as select");
    }

    @Test
    public void testIsSelectFalseWrongArity() {
        Variable a = factory.createVariable("a");
        FunctionApp select = factory.createFunctionApp("select", a);

        assertFalse(TArraySymbols.isSelect(select), "select(a) has wrong arity");
    }

    @Test
    public void testIsSelectFalseLeaf() {
        Variable a = factory.createVariable("a");

        assertFalse(TArraySymbols.isSelect(a), "Variable should not be select");
    }

    @Test
    public void testIsStoreTrue() {
        Variable a = factory.createVariable("a");
        Variable i = factory.createVariable("i");
        Variable v = factory.createVariable("v");
        FunctionApp store = factory.createFunctionApp("store", a, i, v);

        assertTrue(TArraySymbols.isStore(store), "store(a, i, v) should be recognized");
    }

    @Test
    public void testIsStoreFalseWrongSymbol() {
        Variable a = factory.createVariable("a");
        Variable i = factory.createVariable("i");
        Variable v = factory.createVariable("v");
        FunctionApp f = factory.createFunctionApp("f", a, i, v);

        assertFalse(TArraySymbols.isStore(f), "f(a, i, v) should not be recognized as store");
    }

    @Test
    public void testIsStoreFalseWrongArity() {
        Variable a = factory.createVariable("a");
        Variable i = factory.createVariable("i");
        FunctionApp store = factory.createFunctionApp("store", a, i);

        assertFalse(TArraySymbols.isStore(store), "store(a, i) has wrong arity");
    }

    @Test
    public void testIsStoreFalseLeaf() {
        Variable a = factory.createVariable("a");

        assertFalse(TArraySymbols.isStore(a), "Variable should not be store");
    }

    @Test
    public void testIsTArraySymbol() {
        Variable a = factory.createVariable("a");
        Variable i = factory.createVariable("i");
        Variable v = factory.createVariable("v");

        FunctionApp select = factory.createFunctionApp("select", a, i);
        FunctionApp store = factory.createFunctionApp("store", a, i, v);
        FunctionApp f = factory.createFunctionApp("f", a);

        assertTrue(TArraySymbols.isTArraySymbol(select), "select is T_A symbol");
        assertTrue(TArraySymbols.isTArraySymbol(store), "store is T_A symbol");
        assertFalse(TArraySymbols.isTArraySymbol(f), "f is not T_A symbol");
        assertFalse(TArraySymbols.isTArraySymbol(a), "variable is not T_A symbol");
    }

    @Test
    public void testContainsTArraySymbolsInLiteral() {
        Variable a = factory.createVariable("a");
        Variable i = factory.createVariable("i");
        Variable v = factory.createVariable("v");
        FunctionApp store = factory.createFunctionApp("store", a, i, v);

        Literal lit = Literal.equality(store, a);

        assertTrue(TArraySymbols.containsTArraySymbols(lit),
            "Literal with store should contain T_A symbols");
    }

    @Test
    public void testContainsTArraySymbolsNestedInLiteral() {
        Variable a = factory.createVariable("a");
        Variable i = factory.createVariable("i");
        Variable v = factory.createVariable("v");
        FunctionApp store = factory.createFunctionApp("store", a, i, v);
        FunctionApp select = factory.createFunctionApp("select", store, i);
        Variable x = factory.createVariable("x");

        Literal lit = Literal.equality(select, x);

        assertTrue(TArraySymbols.containsTArraySymbols(lit),
            "Literal with select(store(...)) should contain T_A symbols");
    }

    @Test
    public void testNoTArraySymbolsInLiteral() {
        Variable a = factory.createVariable("a");
        Variable b = factory.createVariable("b");
        FunctionApp f = factory.createFunctionApp("f", a, b);

        Literal lit = Literal.equality(f, a);

        assertFalse(TArraySymbols.containsTArraySymbols(lit),
            "Literal without T_A symbols should return false");
    }

    @Test
    public void testContainsTArraySymbolsInCollection() {
        Variable a = factory.createVariable("a");
        Variable i = factory.createVariable("i");
        FunctionApp select = factory.createFunctionApp("select", a, i);

        Literal lit1 = Literal.equality(a, i);
        Literal lit2 = Literal.equality(select, a);

        List<Literal> literals = Arrays.asList(lit1, lit2);

        assertTrue(TArraySymbols.containsTArraySymbols(literals),
            "Collection with T_A literal should return true");
    }

    @Test
    public void testNoTArraySymbolsInCollection() {
        Variable a = factory.createVariable("a");
        Variable b = factory.createVariable("b");
        FunctionApp f = factory.createFunctionApp("f", a);

        Literal lit1 = Literal.equality(a, b);
        Literal lit2 = Literal.disequality(f, a);

        List<Literal> literals = Arrays.asList(lit1, lit2);

        assertFalse(TArraySymbols.containsTArraySymbols(literals),
            "Collection without T_A symbols should return false");
    }

    @Test
    public void testExtractSelectTerms() {
        Variable a = factory.createVariable("a");
        Variable b = factory.createVariable("b");
        Variable i = factory.createVariable("i");
        Variable j = factory.createVariable("j");

        FunctionApp select1 = factory.createFunctionApp("select", a, i);
        FunctionApp select2 = factory.createFunctionApp("select", b, j);
        Variable v = factory.createVariable("v");
        FunctionApp store = factory.createFunctionApp("store", a, i, v);

        Literal lit1 = Literal.equality(select1, select2);
        Literal lit2 = Literal.equality(store, a);

        List<Literal> literals = Arrays.asList(lit1, lit2);

        Set<FunctionApp> selectTerms = TArraySymbols.extractSelectTerms(literals);

        assertEquals(2, selectTerms.size(), "Should find 2 select terms");
        assertTrue(selectTerms.contains(select1), "Should contain select1");
        assertTrue(selectTerms.contains(select2), "Should contain select2");
    }

    @Test
    public void testExtractSelectTermsNested() {
        Variable a = factory.createVariable("a");
        Variable i = factory.createVariable("i");
        Variable j = factory.createVariable("j");
        Variable v = factory.createVariable("v");

        FunctionApp store = factory.createFunctionApp("store", a, i, v);
        FunctionApp select1 = factory.createFunctionApp("select", store, j);
        FunctionApp select2 = factory.createFunctionApp("select", a, i);

        Literal lit = Literal.equality(select1, select2);

        Set<FunctionApp> selectTerms = TArraySymbols.extractSelectTerms(Arrays.asList(lit));

        assertEquals(2, selectTerms.size(), "Should find both select terms");
        assertTrue(selectTerms.contains(select1), "Should contain select on store");
        assertTrue(selectTerms.contains(select2), "Should contain select on array");
    }

    @Test
    public void testExtractStoreTerms() {
        Variable a = factory.createVariable("a");
        Variable b = factory.createVariable("b");
        Variable i = factory.createVariable("i");
        Variable j = factory.createVariable("j");
        Variable v = factory.createVariable("v");
        Variable w = factory.createVariable("w");

        FunctionApp store1 = factory.createFunctionApp("store", a, i, v);
        FunctionApp store2 = factory.createFunctionApp("store", b, j, w);

        Literal lit1 = Literal.equality(store1, a);
        Literal lit2 = Literal.equality(store2, b);

        Set<FunctionApp> storeTerms = TArraySymbols.extractStoreTerms(Arrays.asList(lit1, lit2));

        assertEquals(2, storeTerms.size(), "Should find 2 store terms");
        assertTrue(storeTerms.contains(store1), "Should contain store1");
        assertTrue(storeTerms.contains(store2), "Should contain store2");
    }

    @Test
    public void testExtractStoreTermsNested() {
        Variable a = factory.createVariable("a");
        Variable i = factory.createVariable("i");
        Variable j = factory.createVariable("j");
        Variable v = factory.createVariable("v");
        Variable w = factory.createVariable("w");

        FunctionApp store1 = factory.createFunctionApp("store", a, i, v);
        FunctionApp store2 = factory.createFunctionApp("store", store1, j, w);

        Literal lit = Literal.equality(store2, a);

        Set<FunctionApp> storeTerms = TArraySymbols.extractStoreTerms(Arrays.asList(lit));

        assertEquals(2, storeTerms.size(), "Should find both nested store terms");
        assertTrue(storeTerms.contains(store1), "Should contain inner store");
        assertTrue(storeTerms.contains(store2), "Should contain outer store");
    }

    @Test
    public void testExtractNoSelectTerms() {
        Variable a = factory.createVariable("a");
        Variable b = factory.createVariable("b");
        FunctionApp f = factory.createFunctionApp("f", a, b);

        Literal lit = Literal.equality(f, a);

        Set<FunctionApp> selectTerms = TArraySymbols.extractSelectTerms(Arrays.asList(lit));

        assertEquals(0, selectTerms.size(), "Should find no select terms");
    }

    @Test
    public void testExtractNoStoreTerms() {
        Variable a = factory.createVariable("a");
        Variable b = factory.createVariable("b");
        FunctionApp f = factory.createFunctionApp("f", a, b);

        Literal lit = Literal.equality(f, a);

        Set<FunctionApp> storeTerms = TArraySymbols.extractStoreTerms(Arrays.asList(lit));

        assertEquals(0, storeTerms.size(), "Should find no store terms");
    }

    @Test
    public void testComplexMixedTerms() {
        Variable a = factory.createVariable("a");
        Variable i = factory.createVariable("i");
        Variable v = factory.createVariable("v");

        FunctionApp store = factory.createFunctionApp("store", a, i, v);
        FunctionApp select = factory.createFunctionApp("select", store, i);
        FunctionApp f = factory.createFunctionApp("f", select, v);

        Literal lit = Literal.equality(f, a);

        assertTrue(TArraySymbols.containsTArraySymbols(lit),
            "Should detect T_A symbols in complex nested structure");

        Set<FunctionApp> selectTerms = TArraySymbols.extractSelectTerms(Arrays.asList(lit));
        Set<FunctionApp> storeTerms = TArraySymbols.extractStoreTerms(Arrays.asList(lit));

        assertEquals(1, selectTerms.size());
        assertEquals(1, storeTerms.size());
    }

    @Test
    public void testReadOverWritePattern() {
        // Pattern: select(store(a, i, v), j)
        Variable a = factory.createVariable("a");
        Variable i = factory.createVariable("i");
        Variable j = factory.createVariable("j");
        Variable v = factory.createVariable("v");

        FunctionApp store = factory.createFunctionApp("store", a, i, v);
        FunctionApp select = factory.createFunctionApp("select", store, j);

        Literal lit = Literal.equality(select, v);

        Set<FunctionApp> selectTerms = TArraySymbols.extractSelectTerms(Arrays.asList(lit));
        Set<FunctionApp> storeTerms = TArraySymbols.extractStoreTerms(Arrays.asList(lit));

        assertEquals(1, selectTerms.size(), "Should find select term");
        assertEquals(1, storeTerms.size(), "Should find store term");
        assertTrue(TArraySymbols.isSelect(select), "Should recognize select");
        assertTrue(TArraySymbols.isStore(store), "Should recognize store");
    }

    @Test
    public void testMultipleStoresOnSameArray() {
        Variable a = factory.createVariable("a");
        Variable i = factory.createVariable("i");
        Variable j = factory.createVariable("j");
        Variable v = factory.createVariable("v");
        Variable w = factory.createVariable("w");

        FunctionApp store1 = factory.createFunctionApp("store", a, i, v);
        FunctionApp store2 = factory.createFunctionApp("store", store1, j, w);
        FunctionApp select = factory.createFunctionApp("select", store2, i);

        Literal lit = Literal.equality(select, v);

        Set<FunctionApp> storeTerms = TArraySymbols.extractStoreTerms(Arrays.asList(lit));

        assertEquals(2, storeTerms.size(), "Should find both stores");
    }
}
