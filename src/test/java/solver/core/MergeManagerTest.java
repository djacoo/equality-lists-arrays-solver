package solver.core;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import static org.junit.jupiter.api.Assertions.*;

import solver.dag.*;
import solver.equivalence.ClassManager;

import java.util.Arrays;

/**
 * Tests for MergeManager and the MERGE procedure.
 */
public class MergeManagerTest {
    private TermFactory factory;
    private ClassManager classManager;
    private DAG dag;

    @BeforeEach
    public void setUp() {
        factory = new TermFactory();
        classManager = new ClassManager();
        dag = new DAG();
    }

    @Test
    public void testSimpleMerge() {
        Variable a = factory.createVariable("a");
        Variable b = factory.createVariable("b");

        dag.addTerm(a);
        dag.addTerm(b);
        classManager.initializeAll(dag.getTerms());

        MergeManager merger = new MergeManager(classManager, dag.getTerms());

        assertFalse(classManager.areEqual(a, b));

        merger.merge(a, b);

        assertTrue(classManager.areEqual(a, b));
        assertEquals(1, merger.getMergeCount());
    }

    @Test
    public void testMergePropagatesCongruence() {
        // If a=b, then f(a) should equal f(b)
        Variable a = factory.createVariable("a");
        Variable b = factory.createVariable("b");
        FunctionApp fa = factory.createFunctionApp("f", a);
        FunctionApp fb = factory.createFunctionApp("f", b);

        dag.addTerm(fa);
        dag.addTerm(fb);
        classManager.initializeAll(dag.getTerms());

        MergeManager merger = new MergeManager(classManager, dag.getTerms());

        // Initially, f(a) != f(b)
        assertFalse(classManager.areEqual(fa, fb));

        // Merge a and b
        merger.merge(a, b);

        // Now f(a) = f(b) due to congruence propagation
        assertTrue(classManager.areEqual(fa, fb),
            "MERGE should propagate: a=b implies f(a)=f(b)");

        // Check statistics
        assertTrue(merger.getPropagationCount() > 0,
            "Should have propagated at least one congruence");
    }

    @Test
    public void testTransitiveCongruence() {
        // a=b, b=c, so f(a)=f(b)=f(c)
        Variable a = factory.createVariable("a");
        Variable b = factory.createVariable("b");
        Variable c = factory.createVariable("c");
        FunctionApp fa = factory.createFunctionApp("f", a);
        FunctionApp fb = factory.createFunctionApp("f", b);
        FunctionApp fc = factory.createFunctionApp("f", c);

        dag.addTerm(fa);
        dag.addTerm(fb);
        dag.addTerm(fc);
        classManager.initializeAll(dag.getTerms());

        MergeManager merger = new MergeManager(classManager, dag.getTerms());

        merger.merge(a, b);
        merger.merge(b, c);

        // All should be equal
        assertTrue(classManager.areEqual(a, c));
        assertTrue(classManager.areEqual(fa, fb));
        assertTrue(classManager.areEqual(fb, fc));
        assertTrue(classManager.areEqual(fa, fc));
    }

    @Test
    public void testBinaryFunctionCongruence() {
        // a=c and b=d, so f(a,b)=f(c,d)
        Variable a = factory.createVariable("a");
        Variable b = factory.createVariable("b");
        Variable c = factory.createVariable("c");
        Variable d = factory.createVariable("d");

        FunctionApp fab = factory.createFunctionApp("f", a, b);
        FunctionApp fcd = factory.createFunctionApp("f", c, d);

        dag.addTerm(fab);
        dag.addTerm(fcd);
        classManager.initializeAll(dag.getTerms());

        MergeManager merger = new MergeManager(classManager, dag.getTerms());

        // Merge a=c
        merger.merge(a, c);
        // f(a,b) != f(c,d) yet (only one arg matches)
        assertFalse(classManager.areEqual(fab, fcd));

        // Merge b=d
        merger.merge(b, d);
        // Now f(a,b) = f(c,d) (both args match)
        assertTrue(classManager.areEqual(fab, fcd));
    }

    @Test
    public void testNestedCongruence() {
        // a=b implies g(a)=g(b) implies f(g(a))=f(g(b))
        Variable a = factory.createVariable("a");
        Variable b = factory.createVariable("b");
        FunctionApp ga = factory.createFunctionApp("g", a);
        FunctionApp gb = factory.createFunctionApp("g", b);
        FunctionApp fga = factory.createFunctionApp("f", ga);
        FunctionApp fgb = factory.createFunctionApp("f", gb);

        dag.addTerm(fga);
        dag.addTerm(fgb);
        classManager.initializeAll(dag.getTerms());

        MergeManager merger = new MergeManager(classManager, dag.getTerms());

        // Merge a=b
        merger.merge(a, b);

        // Should propagate through both levels
        assertTrue(classManager.areEqual(ga, gb),
            "g(a) should equal g(b)");
        assertTrue(classManager.areEqual(fga, fgb),
            "f(g(a)) should equal f(g(b))");
    }

    @Test
    public void testNoCongruenceForDifferentSymbols() {
        // a=b, but f(a) != g(a)
        Variable a = factory.createVariable("a");
        Variable b = factory.createVariable("b");
        FunctionApp fa = factory.createFunctionApp("f", a);
        FunctionApp gb = factory.createFunctionApp("g", b);

        dag.addTerm(fa);
        dag.addTerm(gb);
        classManager.initializeAll(dag.getTerms());

        MergeManager merger = new MergeManager(classManager, dag.getTerms());

        merger.merge(a, b);

        // f(a) and g(b) should NOT be merged (different symbols)
        assertFalse(classManager.areEqual(fa, gb));
    }

    @Test
    public void testIdempotentMerge() {
        Variable a = factory.createVariable("a");
        Variable b = factory.createVariable("b");

        dag.addTerm(a);
        dag.addTerm(b);
        classManager.initializeAll(dag.getTerms());

        MergeManager merger = new MergeManager(classManager, dag.getTerms());

        merger.merge(a, b);
        int classCountAfterFirst = classManager.getClassCount();

        // Merge again (should be no-op)
        merger.merge(a, b);
        int classCountAfterSecond = classManager.getClassCount();

        assertEquals(classCountAfterFirst, classCountAfterSecond);
        assertEquals(2, merger.getMergeCount(), "Should count both merge calls");
    }

    @Test
    public void testComplexCongruencePropagation() {
        // Example from Bradley & Manna:
        // f(f(a)) = a, f(f(f(a))) = a
        // Should derive f(a) = a

        Variable a = factory.createVariable("a");
        FunctionApp fa = factory.createFunctionApp("f", a);
        FunctionApp ffa = factory.createFunctionApp("f", fa);
        FunctionApp fffa = factory.createFunctionApp("f", ffa);

        dag.addTerm(fffa);  // Adds all nested terms
        classManager.initializeAll(dag.getTerms());

        MergeManager merger = new MergeManager(classManager, dag.getTerms());

        // Assert f(f(a)) = a
        merger.merge(ffa, a);

        // Assert f(f(f(a))) = a
        merger.merge(fffa, a);

        // Through congruence, should derive f(a) = a
        assertTrue(classManager.areEqual(fa, a),
            "Should derive f(a) = a through congruence closure");
    }

    @Test
    public void testMergeWithMultipleParents() {
        // a=b where both have multiple parents
        Variable a = factory.createVariable("a");
        Variable b = factory.createVariable("b");
        FunctionApp fa = factory.createFunctionApp("f", a);
        FunctionApp ga = factory.createFunctionApp("g", a);
        FunctionApp fb = factory.createFunctionApp("f", b);
        FunctionApp gb = factory.createFunctionApp("g", b);

        dag.addTerm(fa);
        dag.addTerm(ga);
        dag.addTerm(fb);
        dag.addTerm(gb);
        classManager.initializeAll(dag.getTerms());

        // a has 2 parents: f(a), g(a)
        // b has 2 parents: f(b), g(b)
        assertEquals(2, a.getCcparSize());
        assertEquals(2, b.getCcparSize());

        MergeManager merger = new MergeManager(classManager, dag.getTerms());

        merger.merge(a, b);

        // Should merge both pairs of parents
        assertTrue(classManager.areEqual(fa, fb));
        assertTrue(classManager.areEqual(ga, gb));
    }

    @Test
    public void testNoSpuriousMerges() {
        // Ensure MERGE doesn't merge non-congruent terms
        Variable a = factory.createVariable("a");
        Variable b = factory.createVariable("b");
        Variable c = factory.createVariable("c");

        FunctionApp fa = factory.createFunctionApp("f", a);
        FunctionApp fc = factory.createFunctionApp("f", c);

        dag.addTerm(fa);
        dag.addTerm(fc);
        dag.addTerm(b);
        classManager.initializeAll(dag.getTerms());

        MergeManager merger = new MergeManager(classManager, dag.getTerms());

        // Merge a=b (unrelated to c)
        merger.merge(a, b);

        // f(a) and f(c) should NOT be merged
        assertFalse(classManager.areEqual(fa, fc));
        // c should remain separate
        assertFalse(classManager.areEqual(b, c));
    }
}
