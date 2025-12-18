package solver.equivalence;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import solver.config.SolverConfig;
import solver.dag.Term;
import solver.dag.TermFactory;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for path compression optimization in ClassManager.
 *
 * Tests the non-recursive FIND with two-pass path compression.
 * This is the Phase 5.2 optional optimization.
 */
@DisplayName("Path Compression Tests")
public class PathCompressionTest {

    private TermFactory factory;
    private ClassManager baselineManager;
    private ClassManager pathCompressionManager;

    @BeforeEach
    void setUp() {
        factory = new TermFactory();

        // Baseline: no path compression
        SolverConfig baselineConfig = SolverConfig.baseline();
        baselineManager = new ClassManager(baselineConfig);

        // Optimized: with path compression
        SolverConfig optimizedConfig = SolverConfig.withNonRecursiveFIND();
        pathCompressionManager = new ClassManager(optimizedConfig);
    }

    @Test
    @DisplayName("Path compression gives same results as baseline")
    void testPathCompressionCorrectness() {
        // Create a chain: a -> b -> c -> d -> e
        Term a = factory.createVariable("a");
        Term b = factory.createVariable("b");
        Term c = factory.createVariable("c");
        Term d = factory.createVariable("d");
        Term e = factory.createVariable("e");

        List<Term> terms = List.of(a, b, c, d, e);

        // Initialize both managers
        baselineManager.initializeAll(terms);
        pathCompressionManager.initializeAll(terms);

        // Perform same unions on both
        baselineManager.union(a, b);
        baselineManager.union(b, c);
        baselineManager.union(c, d);
        baselineManager.union(d, e);

        pathCompressionManager.union(a, b);
        pathCompressionManager.union(b, c);
        pathCompressionManager.union(c, d);
        pathCompressionManager.union(d, e);

        // Both should have all terms in same class
        assertTrue(baselineManager.areEqual(a, e));
        assertTrue(pathCompressionManager.areEqual(a, e));

        // Representatives should be same term
        Term baselineRep = baselineManager.find(a);
        Term pathCompRep = pathCompressionManager.find(a);

        // They should point to the same term (since we used same TermFactory)
        assertEquals(baselineRep, pathCompRep);
    }

    @Test
    @DisplayName("Path compression maintains find field invariants")
    void testFindFieldInvariants() {
        Term a = factory.createVariable("a");
        Term b = factory.createVariable("b");
        Term c = factory.createVariable("c");

        pathCompressionManager.initialize(a);
        pathCompressionManager.initialize(b);
        pathCompressionManager.initialize(c);

        // Initially, each term points to itself
        assertEquals(a, a.getFind());
        assertEquals(b, b.getFind());
        assertEquals(c, c.getFind());

        // Merge a and b
        pathCompressionManager.union(a, b);
        Term rep1 = pathCompressionManager.find(a);

        // After union, both should point to representative
        assertEquals(rep1, a.getFind());
        assertEquals(rep1, b.getFind());

        // Merge with c
        pathCompressionManager.union(a, c);
        Term rep2 = pathCompressionManager.find(a);

        // All should point to same representative
        assertEquals(rep2, pathCompressionManager.find(a));
        assertEquals(rep2, pathCompressionManager.find(b));
        assertEquals(rep2, pathCompressionManager.find(c));
    }

    @Test
    @DisplayName("Path compression works with long chains")
    void testLongChain() {
        // Create a chain of 100 terms
        List<Term> terms = new ArrayList<>();
        for (int i = 0; i < 100; i++) {
            terms.add(factory.createVariable("x" + i));
        }

        pathCompressionManager.initializeAll(terms);

        // Merge them sequentially: x0 = x1 = x2 = ... = x99
        for (int i = 0; i < terms.size() - 1; i++) {
            pathCompressionManager.union(terms.get(i), terms.get(i + 1));
        }

        // All should be in same class
        Term representative = pathCompressionManager.find(terms.get(0));
        for (Term term : terms) {
            assertEquals(representative, pathCompressionManager.find(term));
        }

        // After path compression, all should point directly to representative
        for (Term term : terms) {
            assertEquals(representative, term.getFind());
        }
    }

    @Test
    @DisplayName("Path compression works with multiple classes")
    void testMultipleClasses() {
        // Create three separate chains
        Term a1 = factory.createVariable("a1");
        Term a2 = factory.createVariable("a2");
        Term a3 = factory.createVariable("a3");

        Term b1 = factory.createVariable("b1");
        Term b2 = factory.createVariable("b2");
        Term b3 = factory.createVariable("b3");

        Term c1 = factory.createVariable("c1");
        Term c2 = factory.createVariable("c2");
        Term c3 = factory.createVariable("c3");

        List<Term> allTerms = List.of(a1, a2, a3, b1, b2, b3, c1, c2, c3);
        pathCompressionManager.initializeAll(allTerms);

        // Create three classes: {a1,a2,a3}, {b1,b2,b3}, {c1,c2,c3}
        pathCompressionManager.union(a1, a2);
        pathCompressionManager.union(a2, a3);

        pathCompressionManager.union(b1, b2);
        pathCompressionManager.union(b2, b3);

        pathCompressionManager.union(c1, c2);
        pathCompressionManager.union(c2, c3);

        // Within each class, all should be equal
        assertTrue(pathCompressionManager.areEqual(a1, a3));
        assertTrue(pathCompressionManager.areEqual(b1, b3));
        assertTrue(pathCompressionManager.areEqual(c1, c3));

        // Between classes, all should be different
        assertFalse(pathCompressionManager.areEqual(a1, b1));
        assertFalse(pathCompressionManager.areEqual(b1, c1));
        assertFalse(pathCompressionManager.areEqual(a1, c1));

        // Should have exactly 3 classes
        assertEquals(3, pathCompressionManager.getClassCount());
    }

    @Test
    @DisplayName("Path compression handles repeated FIND calls efficiently")
    void testRepeatedFinds() {
        // Create a deep chain
        Term a = factory.createVariable("a");
        Term b = factory.createVariable("b");
        Term c = factory.createVariable("c");
        Term d = factory.createVariable("d");
        Term e = factory.createVariable("e");

        List<Term> terms = List.of(a, b, c, d, e);
        pathCompressionManager.initializeAll(terms);

        // Merge into chain: a -> b -> c -> d -> e
        pathCompressionManager.union(a, b);
        pathCompressionManager.union(b, c);
        pathCompressionManager.union(c, d);
        pathCompressionManager.union(d, e);

        Term rep = pathCompressionManager.find(a);

        // Multiple FINDs should all return same representative
        for (int i = 0; i < 100; i++) {
            assertEquals(rep, pathCompressionManager.find(a));
            assertEquals(rep, pathCompressionManager.find(b));
            assertEquals(rep, pathCompressionManager.find(c));
            assertEquals(rep, pathCompressionManager.find(d));
            assertEquals(rep, pathCompressionManager.find(e));
        }

        // After first FIND with path compression, all should point directly
        assertEquals(rep, a.getFind());
        assertEquals(rep, b.getFind());
        assertEquals(rep, c.getFind());
        assertEquals(rep, d.getFind());
        assertEquals(rep, e.getFind());
    }

    @Test
    @DisplayName("Path compression works with largest ccpar optimization")
    void testWithLargestCcpar() {
        // Create terms with function applications to test ccpar
        Term x = factory.createVariable("x");
        Term y = factory.createVariable("y");
        Term fx = factory.createFunctionApp("f", x);
        Term fy = factory.createFunctionApp("f", y);
        Term gfx = factory.createFunctionApp("g", fx);

        List<Term> terms = List.of(x, y, fx, fy, gfx);
        pathCompressionManager.initializeAll(terms);

        // x has ccpar={fx}, y has ccpar={fy}, fx has ccpar={gfx}
        // When we merge x and y, the class with more parents should be kept

        pathCompressionManager.union(x, y);

        // Both x and y should now be in same class
        assertTrue(pathCompressionManager.areEqual(x, y));

        // Representative should be determined by largest ccpar
        Term rep = pathCompressionManager.find(x);
        assertNotNull(rep);

        // After path compression, both should point directly to rep
        assertEquals(rep, x.getFind());
        assertEquals(rep, y.getFind());
    }

    @Test
    @DisplayName("Path compression maintains consistency after multiple operations")
    void testConsistencyAfterMultipleOperations() {
        Term a = factory.createVariable("a");
        Term b = factory.createVariable("b");
        Term c = factory.createVariable("c");

        pathCompressionManager.initialize(a);
        pathCompressionManager.initialize(b);
        pathCompressionManager.initialize(c);

        // Perform operations
        pathCompressionManager.union(a, b);
        pathCompressionManager.union(b, c);

        // Multiple finds should give consistent results
        Term rep1 = pathCompressionManager.find(a);
        Term rep2 = pathCompressionManager.find(b);
        Term rep3 = pathCompressionManager.find(c);

        // All should return same representative
        assertEquals(rep1, rep2);
        assertEquals(rep2, rep3);
    }

    @Test
    @DisplayName("Path compression preserves equivalence class structure")
    void testEquivalenceClassStructure() {
        Term a = factory.createVariable("a");
        Term b = factory.createVariable("b");
        Term c = factory.createVariable("c");

        pathCompressionManager.initializeAll(List.of(a, b, c));

        pathCompressionManager.union(a, b);
        pathCompressionManager.union(b, c);

        EquivalenceClass ec = pathCompressionManager.getClass(a);

        // All three should be in the same class
        assertEquals(3, ec.size());
        assertTrue(ec.contains(a));
        assertTrue(ec.contains(b));
        assertTrue(ec.contains(c));
    }

    @Test
    @DisplayName("Path compression handles single element classes")
    void testSingleElementClasses() {
        Term a = factory.createVariable("a");
        pathCompressionManager.initialize(a);

        // Find of singleton should return itself
        assertEquals(a, pathCompressionManager.find(a));
        assertEquals(a, a.getFind());

        // Should have 1 class
        assertEquals(1, pathCompressionManager.getClassCount());
    }

    @Test
    @DisplayName("Path compression handles empty initialization")
    void testEmptyInitialization() {
        // Should handle empty list gracefully
        pathCompressionManager.initializeAll(List.of());
        assertEquals(0, pathCompressionManager.getClassCount());
    }

    @Test
    @DisplayName("Baseline vs path compression give identical logical results")
    void testBaselineVsPathCompressionEquivalence() {
        // Create identical scenarios in both managers
        List<Term> baselineTerms = createTermSet("baseline");
        List<Term> pathCompTerms = createTermSet("pathcomp");

        baselineManager.initializeAll(baselineTerms);
        pathCompressionManager.initializeAll(pathCompTerms);

        // Perform same sequence of unions
        performStandardUnions(baselineManager, baselineTerms);
        performStandardUnions(pathCompressionManager, pathCompTerms);

        // Verify same equivalence relationships
        verifyEquivalenceRelationships(baselineManager, baselineTerms,
                                       pathCompressionManager, pathCompTerms);
    }

    // Helper methods

    private List<Term> createTermSet(String prefix) {
        List<Term> terms = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            terms.add(factory.createVariable(prefix + "_x" + i));
        }
        return terms;
    }

    private void performStandardUnions(ClassManager manager, List<Term> terms) {
        // Create several equivalence classes
        manager.union(terms.get(0), terms.get(1));
        manager.union(terms.get(1), terms.get(2));

        manager.union(terms.get(3), terms.get(4));

        manager.union(terms.get(5), terms.get(6));
        manager.union(terms.get(6), terms.get(7));
        manager.union(terms.get(7), terms.get(8));
    }

    private void verifyEquivalenceRelationships(ClassManager m1, List<Term> t1,
                                                 ClassManager m2, List<Term> t2) {
        // Class 1: {0, 1, 2}
        assertTrue(m1.areEqual(t1.get(0), t1.get(1)));
        assertTrue(m1.areEqual(t1.get(1), t1.get(2)));
        assertTrue(m2.areEqual(t2.get(0), t2.get(1)));
        assertTrue(m2.areEqual(t2.get(1), t2.get(2)));

        // Class 2: {3, 4}
        assertTrue(m1.areEqual(t1.get(3), t1.get(4)));
        assertTrue(m2.areEqual(t2.get(3), t2.get(4)));

        // Class 3: {5, 6, 7, 8}
        assertTrue(m1.areEqual(t1.get(5), t1.get(8)));
        assertTrue(m2.areEqual(t2.get(5), t2.get(8)));

        // Between classes should be unequal
        assertFalse(m1.areEqual(t1.get(0), t1.get(3)));
        assertFalse(m2.areEqual(t2.get(0), t2.get(3)));
        assertFalse(m1.areEqual(t1.get(0), t1.get(5)));
        assertFalse(m2.areEqual(t2.get(0), t2.get(5)));
    }
}
