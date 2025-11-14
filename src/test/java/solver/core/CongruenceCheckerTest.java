package solver.core;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import static org.junit.jupiter.api.Assertions.*;

import solver.dag.*;
import solver.equivalence.ClassManager;

/**
 * Tests for CongruenceChecker.
 */
public class CongruenceCheckerTest {
    private TermFactory factory;
    private ClassManager classManager;

    @BeforeEach
    public void setUp() {
        factory = new TermFactory();
        classManager = new ClassManager();
    }

    @Test
    public void testLeavesNotCongruent() {
        Variable a = factory.createVariable("a");
        Variable b = factory.createVariable("b");

        classManager.initialize(a);
        classManager.initialize(b);

        assertFalse(CongruenceChecker.areCongruent(a, b, classManager));
    }

    @Test
    public void testSameFunctionSameArgsCongruent() {
        Variable x = factory.createVariable("x");
        Variable y = factory.createVariable("y");
        FunctionApp fx1 = factory.createFunctionApp("f", x);
        FunctionApp fx2 = factory.createFunctionApp("f", x);

        classManager.initializeAll(java.util.Arrays.asList(x, y, fx1, fx2));

        // f(x) and f(x) are congruent (actually the same due to hash-consing)
        assertTrue(CongruenceChecker.areCongruent(fx1, fx2, classManager));
    }

    @Test
    public void testDifferentFunctionSymbolNotCongruent() {
        Variable x = factory.createVariable("x");
        FunctionApp fx = factory.createFunctionApp("f", x);
        FunctionApp gx = factory.createFunctionApp("g", x);

        classManager.initializeAll(java.util.Arrays.asList(x, fx, gx));

        // f(x) and g(x) are not congruent (different symbols)
        assertFalse(CongruenceChecker.areCongruent(fx, gx, classManager));
    }

    @Test
    public void testDifferentArityNotCongruent() {
        Variable x = factory.createVariable("x");
        Variable y = factory.createVariable("y");
        FunctionApp fx = factory.createFunctionApp("f", x);
        FunctionApp fxy = factory.createFunctionApp("f", x, y);

        classManager.initializeAll(java.util.Arrays.asList(x, y, fx, fxy));

        // f(x) and f(x, y) are not congruent (different arity)
        assertFalse(CongruenceChecker.areCongruent(fx, fxy, classManager));
    }

    @Test
    public void testCongruentAfterMerge() {
        // f(a) and f(b) should be congruent after merging a and b
        Variable a = factory.createVariable("a");
        Variable b = factory.createVariable("b");
        FunctionApp fa = factory.createFunctionApp("f", a);
        FunctionApp fb = factory.createFunctionApp("f", b);

        classManager.initializeAll(java.util.Arrays.asList(a, b, fa, fb));

        // Initially not congruent
        assertFalse(CongruenceChecker.areCongruent(fa, fb, classManager));

        // Merge a and b
        classManager.union(a, b);

        // Now f(a) and f(b) are congruent (because a and b are in same class)
        assertTrue(CongruenceChecker.areCongruent(fa, fb, classManager));
    }

    @Test
    public void testCongruentBinaryFunction() {
        // f(a, b) and f(c, d) should be congruent if a=c and b=d
        Variable a = factory.createVariable("a");
        Variable b = factory.createVariable("b");
        Variable c = factory.createVariable("c");
        Variable d = factory.createVariable("d");

        FunctionApp fab = factory.createFunctionApp("f", a, b);
        FunctionApp fcd = factory.createFunctionApp("f", c, d);

        classManager.initializeAll(java.util.Arrays.asList(a, b, c, d, fab, fcd));

        // Initially not congruent
        assertFalse(CongruenceChecker.areCongruent(fab, fcd, classManager));

        // Merge a=c
        classManager.union(a, c);
        // Still not congruent (only first arg matches)
        assertFalse(CongruenceChecker.areCongruent(fab, fcd, classManager));

        // Merge b=d
        classManager.union(b, d);
        // Now congruent (both args match)
        assertTrue(CongruenceChecker.areCongruent(fab, fcd, classManager));
    }

    @Test
    public void testNotCongruentIfOneArgDiffers() {
        // f(a, b) and f(a, c) should not be congruent if b != c
        Variable a = factory.createVariable("a");
        Variable b = factory.createVariable("b");
        Variable c = factory.createVariable("c");

        FunctionApp fab = factory.createFunctionApp("f", a, b);
        FunctionApp fac = factory.createFunctionApp("f", a, c);

        classManager.initializeAll(java.util.Arrays.asList(a, b, c, fab, fac));

        // Not congruent (second arg differs)
        assertFalse(CongruenceChecker.areCongruent(fab, fac, classManager));

        // Even after merging b=c, they should become congruent
        classManager.union(b, c);
        assertTrue(CongruenceChecker.areCongruent(fab, fac, classManager));
    }

    @Test
    public void testNestedFunctions() {
        // f(g(a)) and f(g(b)) should be congruent if a=b
        Variable a = factory.createVariable("a");
        Variable b = factory.createVariable("b");
        FunctionApp ga = factory.createFunctionApp("g", a);
        FunctionApp gb = factory.createFunctionApp("g", b);
        FunctionApp fga = factory.createFunctionApp("f", ga);
        FunctionApp fgb = factory.createFunctionApp("f", gb);

        classManager.initializeAll(java.util.Arrays.asList(a, b, ga, gb, fga, fgb));

        // Initially not congruent
        assertFalse(CongruenceChecker.areCongruent(fga, fgb, classManager));

        // Merge a=b (this should also make g(a) and g(b) congruent)
        classManager.union(a, b);

        // But f(g(a)) and f(g(b)) are NOT automatically congruent yet!
        // We would need to merge g(a) and g(b) first
        // This is what the MERGE procedure will do
        assertFalse(CongruenceChecker.areCongruent(fga, fgb, classManager),
            "Without MERGE propagation, nested terms are not automatically congruent");

        // After manually merging g(a) and g(b)
        classManager.union(ga, gb);
        // Now f(g(a)) and f(g(b)) are congruent
        assertTrue(CongruenceChecker.areCongruent(fga, fgb, classManager));
    }

    @Test
    public void testFindCongruentTerm() {
        Variable a = factory.createVariable("a");
        Variable b = factory.createVariable("b");
        Variable c = factory.createVariable("c");

        FunctionApp fa = factory.createFunctionApp("f", a);
        FunctionApp fb = factory.createFunctionApp("f", b);
        FunctionApp fc = factory.createFunctionApp("f", c);

        classManager.initializeAll(java.util.Arrays.asList(a, b, c, fa, fb, fc));

        // Merge a and b
        classManager.union(a, b);

        // Find a term congruent to f(a) among the candidates
        java.util.List<Term> candidates = java.util.Arrays.asList(fb, fc);
        Term congruent = CongruenceChecker.findCongruentTerm(fa, candidates, classManager);

        // Should find f(b) as congruent (since a=b)
        assertSame(fb, congruent);
    }

    @Test
    public void testFindCongruentTermNotFound() {
        Variable a = factory.createVariable("a");
        Variable b = factory.createVariable("b");

        FunctionApp fa = factory.createFunctionApp("f", a);
        FunctionApp fb = factory.createFunctionApp("f", b);

        classManager.initializeAll(java.util.Arrays.asList(a, b, fa, fb));

        // Without merging a and b, f(a) is not congruent to f(b)
        java.util.List<Term> candidates = java.util.Arrays.asList(fb);
        Term congruent = CongruenceChecker.findCongruentTerm(fa, candidates, classManager);

        assertNull(congruent, "Should not find congruent term when args are not equal");
    }

    @Test
    public void testNullaryFunctions() {
        // Nullary functions (constants) with same symbol are congruent
        FunctionApp c1 = factory.createFunctionApp("c", java.util.Collections.emptyList());
        FunctionApp c2 = factory.createFunctionApp("c", java.util.Collections.emptyList());

        // Due to hash-consing, these are the same object
        assertSame(c1, c2);

        classManager.initialize(c1);

        assertTrue(CongruenceChecker.areCongruent(c1, c2, classManager));
    }
}
