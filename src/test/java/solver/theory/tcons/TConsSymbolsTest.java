package solver.theory.tcons;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import static org.junit.jupiter.api.Assertions.*;

import solver.dag.*;
import solver.theory.te.Literal;

import java.util.Arrays;
import java.util.List;
import java.util.Set;

/**
 * Tests for T_cons symbol recognition.
 */
public class TConsSymbolsTest {
    private TermFactory factory;

    @BeforeEach
    public void setUp() {
        factory = new TermFactory();
    }

    @Test
    public void testIsConsTrue() {
        Variable a = factory.createVariable("a");
        Variable b = factory.createVariable("b");
        FunctionApp cons = factory.createFunctionApp("cons", a, b);

        assertTrue(TConsSymbols.isCons(cons), "cons(a, b) should be recognized");
    }

    @Test
    public void testIsConsFalseWrongSymbol() {
        Variable a = factory.createVariable("a");
        Variable b = factory.createVariable("b");
        FunctionApp f = factory.createFunctionApp("f", a, b);

        assertFalse(TConsSymbols.isCons(f), "f(a, b) should not be recognized as cons");
    }

    @Test
    public void testIsConsFalseWrongArity() {
        Variable a = factory.createVariable("a");
        FunctionApp cons = factory.createFunctionApp("cons", a);

        assertFalse(TConsSymbols.isCons(cons), "cons(a) has wrong arity");
    }

    @Test
    public void testIsConsFalseLeaf() {
        Variable a = factory.createVariable("a");

        assertFalse(TConsSymbols.isCons(a), "Variable should not be cons");
    }

    @Test
    public void testIsCarTrue() {
        Variable x = factory.createVariable("x");
        FunctionApp car = factory.createFunctionApp("car", x);

        assertTrue(TConsSymbols.isCar(car), "car(x) should be recognized");
    }

    @Test
    public void testIsCarFalseWrongSymbol() {
        Variable x = factory.createVariable("x");
        FunctionApp f = factory.createFunctionApp("f", x);

        assertFalse(TConsSymbols.isCar(f), "f(x) should not be recognized as car");
    }

    @Test
    public void testIsCarFalseWrongArity() {
        Variable a = factory.createVariable("a");
        Variable b = factory.createVariable("b");
        FunctionApp car = factory.createFunctionApp("car", a, b);

        assertFalse(TConsSymbols.isCar(car), "car(a, b) has wrong arity");
    }

    @Test
    public void testIsCdrTrue() {
        Variable x = factory.createVariable("x");
        FunctionApp cdr = factory.createFunctionApp("cdr", x);

        assertTrue(TConsSymbols.isCdr(cdr), "cdr(x) should be recognized");
    }

    @Test
    public void testIsCdrFalseWrongSymbol() {
        Variable x = factory.createVariable("x");
        FunctionApp g = factory.createFunctionApp("g", x);

        assertFalse(TConsSymbols.isCdr(g), "g(x) should not be recognized as cdr");
    }

    @Test
    public void testIsTConsSymbol() {
        Variable a = factory.createVariable("a");
        Variable b = factory.createVariable("b");

        FunctionApp cons = factory.createFunctionApp("cons", a, b);
        FunctionApp car = factory.createFunctionApp("car", a);
        FunctionApp cdr = factory.createFunctionApp("cdr", a);
        FunctionApp f = factory.createFunctionApp("f", a);

        assertTrue(TConsSymbols.isTConsSymbol(cons), "cons is T_cons symbol");
        assertTrue(TConsSymbols.isTConsSymbol(car), "car is T_cons symbol");
        assertTrue(TConsSymbols.isTConsSymbol(cdr), "cdr is T_cons symbol");
        assertFalse(TConsSymbols.isTConsSymbol(f), "f is not T_cons symbol");
        assertFalse(TConsSymbols.isTConsSymbol(a), "variable is not T_cons symbol");
    }

    @Test
    public void testContainsTConsSymbolsInLiteral() {
        Variable a = factory.createVariable("a");
        Variable b = factory.createVariable("b");
        FunctionApp cons = factory.createFunctionApp("cons", a, b);

        Literal lit = Literal.equality(cons, a);

        assertTrue(TConsSymbols.containsTConsSymbols(lit),
            "Literal with cons should contain T_cons symbols");
    }

    @Test
    public void testContainsTConsSymbolsNestedInLiteral() {
        Variable a = factory.createVariable("a");
        Variable b = factory.createVariable("b");
        FunctionApp cons = factory.createFunctionApp("cons", a, b);
        FunctionApp car = factory.createFunctionApp("car", cons);
        Variable c = factory.createVariable("c");

        Literal lit = Literal.equality(car, c);

        assertTrue(TConsSymbols.containsTConsSymbols(lit),
            "Literal with car(cons(...)) should contain T_cons symbols");
    }

    @Test
    public void testNoTConsSymbolsInLiteral() {
        Variable a = factory.createVariable("a");
        Variable b = factory.createVariable("b");
        FunctionApp f = factory.createFunctionApp("f", a, b);

        Literal lit = Literal.equality(f, a);

        assertFalse(TConsSymbols.containsTConsSymbols(lit),
            "Literal without T_cons symbols should return false");
    }

    @Test
    public void testContainsTConsSymbolsInCollection() {
        Variable a = factory.createVariable("a");
        Variable b = factory.createVariable("b");
        FunctionApp cons = factory.createFunctionApp("cons", a, b);

        Literal lit1 = Literal.equality(a, b);
        Literal lit2 = Literal.equality(cons, a);

        List<Literal> literals = Arrays.asList(lit1, lit2);

        assertTrue(TConsSymbols.containsTConsSymbols(literals),
            "Collection with T_cons literal should return true");
    }

    @Test
    public void testNoTConsSymbolsInCollection() {
        Variable a = factory.createVariable("a");
        Variable b = factory.createVariable("b");
        FunctionApp f = factory.createFunctionApp("f", a);

        Literal lit1 = Literal.equality(a, b);
        Literal lit2 = Literal.disequality(f, a);

        List<Literal> literals = Arrays.asList(lit1, lit2);

        assertFalse(TConsSymbols.containsTConsSymbols(literals),
            "Collection without T_cons symbols should return false");
    }

    @Test
    public void testExtractConsTerms() {
        Variable a = factory.createVariable("a");
        Variable b = factory.createVariable("b");
        Variable c = factory.createVariable("c");

        FunctionApp cons1 = factory.createFunctionApp("cons", a, b);
        FunctionApp cons2 = factory.createFunctionApp("cons", b, c);
        FunctionApp car = factory.createFunctionApp("car", cons1);

        Literal lit1 = Literal.equality(cons1, cons2);
        Literal lit2 = Literal.equality(car, a);

        List<Literal> literals = Arrays.asList(lit1, lit2);

        Set<FunctionApp> consTerms = TConsSymbols.extractConsTerms(literals);

        assertEquals(2, consTerms.size(), "Should find 2 cons terms");
        assertTrue(consTerms.contains(cons1), "Should contain cons1");
        assertTrue(consTerms.contains(cons2), "Should contain cons2");
    }

    @Test
    public void testExtractConsTermsNested() {
        Variable a = factory.createVariable("a");
        Variable b = factory.createVariable("b");

        FunctionApp cons1 = factory.createFunctionApp("cons", a, b);
        FunctionApp cons2 = factory.createFunctionApp("cons", cons1, a);

        Literal lit = Literal.equality(cons2, a);

        Set<FunctionApp> consTerms = TConsSymbols.extractConsTerms(Arrays.asList(lit));

        assertEquals(2, consTerms.size(), "Should find both nested cons terms");
        assertTrue(consTerms.contains(cons1), "Should contain inner cons");
        assertTrue(consTerms.contains(cons2), "Should contain outer cons");
    }

    @Test
    public void testExtractCarTerms() {
        Variable a = factory.createVariable("a");
        Variable x = factory.createVariable("x");

        FunctionApp cons = factory.createFunctionApp("cons", a, x);
        FunctionApp car1 = factory.createFunctionApp("car", cons);
        FunctionApp car2 = factory.createFunctionApp("car", x);

        Literal lit1 = Literal.equality(car1, a);
        Literal lit2 = Literal.equality(car2, a);

        Set<FunctionApp> carTerms = TConsSymbols.extractCarTerms(Arrays.asList(lit1, lit2));

        assertEquals(2, carTerms.size(), "Should find 2 car terms");
        assertTrue(carTerms.contains(car1), "Should contain car1");
        assertTrue(carTerms.contains(car2), "Should contain car2");
    }

    @Test
    public void testExtractCdrTerms() {
        Variable x = factory.createVariable("x");
        Variable y = factory.createVariable("y");

        FunctionApp cdr1 = factory.createFunctionApp("cdr", x);
        FunctionApp cdr2 = factory.createFunctionApp("cdr", y);

        Literal lit1 = Literal.equality(cdr1, y);
        Literal lit2 = Literal.disequality(cdr2, x);

        Set<FunctionApp> cdrTerms = TConsSymbols.extractCdrTerms(Arrays.asList(lit1, lit2));

        assertEquals(2, cdrTerms.size(), "Should find 2 cdr terms");
        assertTrue(cdrTerms.contains(cdr1), "Should contain cdr1");
        assertTrue(cdrTerms.contains(cdr2), "Should contain cdr2");
    }

    @Test
    public void testExtractNoConsTerms() {
        Variable a = factory.createVariable("a");
        Variable b = factory.createVariable("b");
        FunctionApp f = factory.createFunctionApp("f", a, b);

        Literal lit = Literal.equality(f, a);

        Set<FunctionApp> consTerms = TConsSymbols.extractConsTerms(Arrays.asList(lit));

        assertEquals(0, consTerms.size(), "Should find no cons terms");
    }

    @Test
    public void testComplexMixedTerms() {
        Variable a = factory.createVariable("a");
        Variable b = factory.createVariable("b");

        FunctionApp cons = factory.createFunctionApp("cons", a, b);
        FunctionApp car = factory.createFunctionApp("car", cons);
        FunctionApp cdr = factory.createFunctionApp("cdr", cons);
        FunctionApp f = factory.createFunctionApp("f", car, cdr);

        Literal lit = Literal.equality(f, a);

        assertTrue(TConsSymbols.containsTConsSymbols(lit),
            "Should detect T_cons symbols in complex nested structure");

        Set<FunctionApp> consTerms = TConsSymbols.extractConsTerms(Arrays.asList(lit));
        Set<FunctionApp> carTerms = TConsSymbols.extractCarTerms(Arrays.asList(lit));
        Set<FunctionApp> cdrTerms = TConsSymbols.extractCdrTerms(Arrays.asList(lit));

        assertEquals(1, consTerms.size());
        assertEquals(1, carTerms.size());
        assertEquals(1, cdrTerms.size());
    }
}
