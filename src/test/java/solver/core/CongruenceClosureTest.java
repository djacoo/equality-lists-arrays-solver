package solver.core;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import static org.junit.jupiter.api.Assertions.*;

import solver.dag.*;

/**
 * Tests for the main CongruenceClosure algorithm.
 */
public class CongruenceClosureTest {
    private TermFactory factory;
    private DAG dag;

    @BeforeEach
    public void setUp() {
        factory = new TermFactory();
        dag = new DAG();
    }

    @Test
    public void testEmptyCongruenceClosure() {
        Variable x = factory.createVariable("x");
        dag.addTerm(x);

        CongruenceClosure cc = new CongruenceClosure(dag);

        // Single term should be in its own class
        assertEquals(1, cc.getClassCount());
        assertEquals(x, cc.find(x));
    }

    @Test
    public void testSingleEquality() {
        Variable a = factory.createVariable("a");
        Variable b = factory.createVariable("b");

        dag.addTerm(a);
        dag.addTerm(b);

        CongruenceClosure cc = new CongruenceClosure(dag);

        // Initially separate
        assertFalse(cc.areEqual(a, b));
        assertEquals(2, cc.getClassCount());

        // Assert a = b
        cc.assertEqual(a, b);

        // Now equal
        assertTrue(cc.areEqual(a, b));
        assertEquals(1, cc.getClassCount());
    }

    @Test
    public void testTransitiveEquality() {
        Variable a = factory.createVariable("a");
        Variable b = factory.createVariable("b");
        Variable c = factory.createVariable("c");

        dag.addTerm(a);
        dag.addTerm(b);
        dag.addTerm(c);

        CongruenceClosure cc = new CongruenceClosure(dag);

        cc.assertEqual(a, b);
        cc.assertEqual(b, c);

        // All three should be equal
        assertTrue(cc.areEqual(a, b));
        assertTrue(cc.areEqual(b, c));
        assertTrue(cc.areEqual(a, c));
        assertEquals(1, cc.getClassCount());
    }

    @Test
    public void testCongruencePropagation() {
        // If a=b, then f(a)=f(b)
        Variable a = factory.createVariable("a");
        Variable b = factory.createVariable("b");
        FunctionApp fa = factory.createFunctionApp("f", a);
        FunctionApp fb = factory.createFunctionApp("f", b);

        dag.addTerm(fa);
        dag.addTerm(fb);

        CongruenceClosure cc = new CongruenceClosure(dag);

        // Initially not equal
        assertFalse(cc.areEqual(fa, fb));

        // Assert a = b
        cc.assertEqual(a, b);

        // Congruence should propagate: f(a) = f(b)
        assertTrue(cc.areEqual(fa, fb),
            "Congruence should propagate: a=b implies f(a)=f(b)");
    }

    @Test
    public void testNestedCongruence() {
        // a=b implies g(f(a))=g(f(b))
        Variable a = factory.createVariable("a");
        Variable b = factory.createVariable("b");
        FunctionApp fa = factory.createFunctionApp("f", a);
        FunctionApp fb = factory.createFunctionApp("f", b);
        FunctionApp gfa = factory.createFunctionApp("g", fa);
        FunctionApp gfb = factory.createFunctionApp("g", fb);

        dag.addTerm(gfa);
        dag.addTerm(gfb);

        CongruenceClosure cc = new CongruenceClosure(dag);

        cc.assertEqual(a, b);

        // Should propagate through both levels
        assertTrue(cc.areEqual(fa, fb));
        assertTrue(cc.areEqual(gfa, gfb));
    }

    @Test
    public void testBinaryFunctionCongruence() {
        // a=c and b=d implies f(a,b)=f(c,d)
        Variable a = factory.createVariable("a");
        Variable b = factory.createVariable("b");
        Variable c = factory.createVariable("c");
        Variable d = factory.createVariable("d");

        FunctionApp fab = factory.createFunctionApp("f", a, b);
        FunctionApp fcd = factory.createFunctionApp("f", c, d);

        dag.addTerm(fab);
        dag.addTerm(fcd);

        CongruenceClosure cc = new CongruenceClosure(dag);

        // Assert a=c
        cc.assertEqual(a, c);
        // Not yet congruent (only first arg matches)
        assertFalse(cc.areEqual(fab, fcd));

        // Assert b=d
        cc.assertEqual(b, d);
        // Now congruent (both args match)
        assertTrue(cc.areEqual(fab, fcd));
    }

    @Test
    public void testBradleyMannaExample() {
        // Classic example from Bradley & Manna:
        // f(f(a)) = a, f(f(f(a))) = a
        // Should derive f(a) = a

        Variable a = factory.createVariable("a");
        FunctionApp fa = factory.createFunctionApp("f", a);
        FunctionApp ffa = factory.createFunctionApp("f", fa);
        FunctionApp fffa = factory.createFunctionApp("f", ffa);

        dag.addTerm(fffa);  // Adds all nested terms

        CongruenceClosure cc = new CongruenceClosure(dag);

        // Assert f(f(a)) = a
        cc.assertEqual(ffa, a);

        // Assert f(f(f(a))) = a
        cc.assertEqual(fffa, a);

        // Should derive f(a) = a through congruence
        assertTrue(cc.areEqual(fa, a),
            "Should derive f(a) = a through congruence closure");
    }

    @Test
    public void testMultipleIndependentEqualities() {
        Variable a = factory.createVariable("a");
        Variable b = factory.createVariable("b");
        Variable c = factory.createVariable("c");
        Variable d = factory.createVariable("d");

        dag.addTerm(a);
        dag.addTerm(b);
        dag.addTerm(c);
        dag.addTerm(d);

        CongruenceClosure cc = new CongruenceClosure(dag);

        // Create two separate equivalence classes: {a,b} and {c,d}
        cc.assertEqual(a, b);
        cc.assertEqual(c, d);

        // Check within classes
        assertTrue(cc.areEqual(a, b));
        assertTrue(cc.areEqual(c, d));

        // Check between classes
        assertFalse(cc.areEqual(a, c));
        assertFalse(cc.areEqual(b, d));

        // Should have 2 classes
        assertEquals(2, cc.getClassCount());
    }

    @Test
    public void testComplexDAG() {
        // Create a more complex term structure
        Variable x = factory.createVariable("x");
        Variable y = factory.createVariable("y");
        Variable z = factory.createVariable("z");

        FunctionApp fx = factory.createFunctionApp("f", x);
        FunctionApp fy = factory.createFunctionApp("f", y);
        FunctionApp fz = factory.createFunctionApp("f", z);

        FunctionApp gfx = factory.createFunctionApp("g", fx);
        FunctionApp gfy = factory.createFunctionApp("g", fy);

        dag.addTerm(gfx);
        dag.addTerm(gfy);
        dag.addTerm(fz);

        CongruenceClosure cc = new CongruenceClosure(dag);

        // Initially all separate
        assertEquals(8, cc.getClassCount());  // x, y, z, f(x), f(y), f(z), g(f(x)), g(f(y))

        // Assert x = y
        cc.assertEqual(x, y);

        // Should propagate to f(x) = f(y) and g(f(x)) = g(f(y))
        assertTrue(cc.areEqual(fx, fy));
        assertTrue(cc.areEqual(gfx, gfy));

        // But f(z) should remain separate
        assertFalse(cc.areEqual(fx, fz));
        assertFalse(cc.areEqual(fy, fz));
    }

    @Test
    public void testStatistics() {
        Variable a = factory.createVariable("a");
        Variable b = factory.createVariable("b");
        Variable c = factory.createVariable("c");

        dag.addTerm(a);
        dag.addTerm(b);
        dag.addTerm(c);

        CongruenceClosure cc = new CongruenceClosure(dag);

        cc.assertEqual(a, b);
        cc.assertEqual(b, c);

        assertEquals(2, cc.getEqualitiesProcessed());
        assertEquals(2, cc.getMergeCount());
        assertTrue(cc.getStatistics().contains("Terms: 3"));
        assertTrue(cc.getStatistics().contains("Equalities asserted: 2"));
    }

    @Test
    public void testGetEquivalenceClasses() {
        Variable a = factory.createVariable("a");
        Variable b = factory.createVariable("b");
        Variable c = factory.createVariable("c");

        dag.addTerm(a);
        dag.addTerm(b);
        dag.addTerm(c);

        CongruenceClosure cc = new CongruenceClosure(dag);

        cc.assertEqual(a, b);

        var classes = cc.getEquivalenceClasses();
        assertEquals(2, classes.size(), "Should have 2 classes: {a,b} and {c}");
    }

    @Test
    public void testFindRepresentative() {
        Variable a = factory.createVariable("a");
        Variable b = factory.createVariable("b");
        Variable c = factory.createVariable("c");

        dag.addTerm(a);
        dag.addTerm(b);
        dag.addTerm(c);

        CongruenceClosure cc = new CongruenceClosure(dag);

        cc.assertEqual(a, b);
        cc.assertEqual(b, c);

        // All should have the same representative
        Term rep = cc.find(a);
        assertEquals(rep, cc.find(b));
        assertEquals(rep, cc.find(c));
    }

    @Test
    public void testChainOfFunctions() {
        // Test: f(f(f(x))) structure
        Variable x = factory.createVariable("x");
        FunctionApp f1 = factory.createFunctionApp("f", x);
        FunctionApp f2 = factory.createFunctionApp("f", f1);
        FunctionApp f3 = factory.createFunctionApp("f", f2);

        dag.addTerm(f3);

        CongruenceClosure cc = new CongruenceClosure(dag);

        // Initially all separate
        assertFalse(cc.areEqual(x, f1));
        assertFalse(cc.areEqual(f1, f2));
        assertFalse(cc.areEqual(f2, f3));

        // Assert x = f1
        cc.assertEqual(x, f1);

        // This should trigger f(x) = f(f(x)) = f2
        // And then f(f(x)) = f(f(f(x))) = f3
        assertTrue(cc.areEqual(x, f1));
        assertTrue(cc.areEqual(f1, f2), "f(x) should equal f(f(x))");
        assertTrue(cc.areEqual(f2, f3), "f(f(x)) should equal f(f(f(x)))");
        assertTrue(cc.areEqual(x, f3), "x should equal f(f(f(x)))");
    }

    @Test
    public void testDifferentFunctionSymbols() {
        // f(a) != g(a) even if a is in some class
        Variable a = factory.createVariable("a");
        Variable b = factory.createVariable("b");
        FunctionApp fa = factory.createFunctionApp("f", a);
        FunctionApp gb = factory.createFunctionApp("g", b);

        dag.addTerm(fa);
        dag.addTerm(gb);

        CongruenceClosure cc = new CongruenceClosure(dag);

        cc.assertEqual(a, b);

        // a = b, but f(a) != g(b) (different symbols)
        assertTrue(cc.areEqual(a, b));
        assertFalse(cc.areEqual(fa, gb));
    }

    @Test
    public void testAssertEqualitiesBatch() {
        Variable a = factory.createVariable("a");
        Variable b = factory.createVariable("b");
        Variable c = factory.createVariable("c");
        Variable d = factory.createVariable("d");

        dag.addTerm(a);
        dag.addTerm(b);
        dag.addTerm(c);
        dag.addTerm(d);

        CongruenceClosure cc = new CongruenceClosure(dag);

        // Assert multiple equalities at once
        java.util.List<CongruenceClosure.TermPair> equalities = java.util.Arrays.asList(
            new CongruenceClosure.TermPair(a, b),
            new CongruenceClosure.TermPair(c, d)
        );

        cc.assertEqualities(equalities);

        assertTrue(cc.areEqual(a, b));
        assertTrue(cc.areEqual(c, d));
        assertFalse(cc.areEqual(a, c));
        assertEquals(2, cc.getClassCount());
    }
}
