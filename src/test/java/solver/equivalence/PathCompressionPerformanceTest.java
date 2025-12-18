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
 * Performance comparison tests for path compression optimization.
 *
 * Compares baseline (no path compression) vs optimized (with path compression).
 * Phase 5.2 optional optimization testing.
 */
@DisplayName("Path Compression Performance Tests")
public class PathCompressionPerformanceTest {

    private TermFactory factory;

    @BeforeEach
    void setUp() {
        factory = new TermFactory();
    }

    @Test
    @DisplayName("Performance: Long chain FIND operations")
    void testLongChainPerformance() {
        int chainLength = 1000;

        // Create two managers
        ClassManager baseline = new ClassManager(SolverConfig.baseline());
        ClassManager optimized = new ClassManager(SolverConfig.withNonRecursiveFIND());

        // Create chain of terms
        List<Term> baselineTerms = createChain("baseline", chainLength);
        List<Term> optimizedTerms = createChain("optimized", chainLength);

        baseline.initializeAll(baselineTerms);
        optimized.initializeAll(optimizedTerms);

        // Merge into long chain: t0 -> t1 -> t2 -> ... -> t999
        mergeChain(baseline, baselineTerms);
        mergeChain(optimized, optimizedTerms);

        // Measure FIND operations
        long baselineTime = measureFindOperations(baseline, baselineTerms);
        long optimizedTime = measureFindOperations(optimized, optimizedTerms);

        System.out.println("Chain length: " + chainLength);
        System.out.println("Baseline FIND time: " + baselineTime + " ns");
        System.out.println("Optimized FIND time: " + optimizedTime + " ns");
        System.out.println("Speedup: " + String.format("%.2fx", (double) baselineTime / optimizedTime));

        // Path compression should be faster or comparable
        // (we don't assert because performance can vary, but we log for analysis)
        assertTrue(optimizedTime > 0);
        assertTrue(baselineTime > 0);
    }

    @Test
    @DisplayName("Performance: Multiple classes with repeated FINDs")
    void testMultipleClassesPerformance() {
        int numClasses = 100;
        int classSize = 10;

        ClassManager baseline = new ClassManager(SolverConfig.baseline());
        ClassManager optimized = new ClassManager(SolverConfig.withNonRecursiveFIND());

        List<List<Term>> baselineClasses = createMultipleClasses("baseline", numClasses, classSize);
        List<List<Term>> optimizedClasses = createMultipleClasses("optimized", numClasses, classSize);

        // Initialize and merge
        initializeAndMergeClasses(baseline, baselineClasses);
        initializeAndMergeClasses(optimized, optimizedClasses);

        // Measure repeated FIND operations across all classes
        long baselineTime = measureRepeatedFinds(baseline, baselineClasses, 100);
        long optimizedTime = measureRepeatedFinds(optimized, optimizedClasses, 100);

        System.out.println("Classes: " + numClasses + ", Size: " + classSize);
        System.out.println("Baseline repeated FINDs: " + baselineTime + " ns");
        System.out.println("Optimized repeated FINDs: " + optimizedTime + " ns");

        // Both should complete successfully
        assertTrue(baselineTime > 0);
        assertTrue(optimizedTime > 0);
    }

    @Test
    @DisplayName("Performance: UNION-heavy workload")
    void testUnionHeavyWorkload() {
        int numTerms = 500;

        ClassManager baseline = new ClassManager(SolverConfig.baseline());
        ClassManager optimized = new ClassManager(SolverConfig.withNonRecursiveFIND());

        List<Term> baselineTerms = createTermList("baseline", numTerms);
        List<Term> optimizedTerms = createTermList("optimized", numTerms);

        baseline.initializeAll(baselineTerms);
        optimized.initializeAll(optimizedTerms);

        // Measure UNION operations (merging all into one class)
        long baselineTime = measureUnionOperations(baseline, baselineTerms);
        long optimizedTime = measureUnionOperations(optimized, optimizedTerms);

        System.out.println("Terms: " + numTerms);
        System.out.println("Baseline UNION time: " + baselineTime + " ns");
        System.out.println("Optimized UNION time: " + optimizedTime + " ns");

        // Both should produce same result
        assertEquals(1, baseline.getClassCount());
        assertEquals(1, optimized.getClassCount());
    }

    @Test
    @DisplayName("Performance: FIND-heavy workload after merges")
    void testFindHeavyWorkload() {
        int numTerms = 100;
        int numFinds = 10000;

        ClassManager baseline = new ClassManager(SolverConfig.baseline());
        ClassManager optimized = new ClassManager(SolverConfig.withNonRecursiveFIND());

        List<Term> baselineTerms = createTermList("baseline", numTerms);
        List<Term> optimizedTerms = createTermList("optimized", numTerms);

        baseline.initializeAll(baselineTerms);
        optimized.initializeAll(optimizedTerms);

        // Create some structure
        for (int i = 0; i < numTerms - 1; i += 2) {
            baseline.union(baselineTerms.get(i), baselineTerms.get(i + 1));
            optimized.union(optimizedTerms.get(i), optimizedTerms.get(i + 1));
        }

        // Measure many FIND operations
        long baselineStart = System.nanoTime();
        for (int i = 0; i < numFinds; i++) {
            baseline.find(baselineTerms.get(i % numTerms));
        }
        long baselineTime = System.nanoTime() - baselineStart;

        long optimizedStart = System.nanoTime();
        for (int i = 0; i < numFinds; i++) {
            optimized.find(optimizedTerms.get(i % numTerms));
        }
        long optimizedTime = System.nanoTime() - optimizedStart;

        System.out.println("FIND operations: " + numFinds);
        System.out.println("Baseline: " + baselineTime + " ns (" + (baselineTime / numFinds) + " ns/op)");
        System.out.println("Optimized: " + optimizedTime + " ns (" + (optimizedTime / numFinds) + " ns/op)");

        // Path compression should improve repeated FIND performance
        assertTrue(optimizedTime > 0);
    }

    @Test
    @DisplayName("Statistics: Path compression reduces FIND path length")
    void testPathLengthReduction() {
        // Create a deep chain
        int chainLength = 50;
        ClassManager optimized = new ClassManager(SolverConfig.withNonRecursiveFIND());

        List<Term> terms = createChain("test", chainLength);
        optimized.initializeAll(terms);

        // Merge into chain
        mergeChain(optimized, terms);

        // First FIND will traverse the chain
        Term rep1 = optimized.find(terms.get(0));

        // After path compression, all terms should point directly to representative
        for (Term term : terms) {
            assertEquals(rep1, term.getFind(), "After path compression, all should point to representative");
        }

        // Second FIND should be immediate (O(1))
        Term rep2 = optimized.find(terms.get(0));
        assertEquals(rep1, rep2);
    }

    @Test
    @DisplayName("Correctness: Both approaches produce identical equivalence classes")
    void testCorrectnessEquivalence() {
        ClassManager baseline = new ClassManager(SolverConfig.baseline());
        ClassManager optimized = new ClassManager(SolverConfig.withNonRecursiveFIND());

        // Create complex structure
        List<Term> baselineTerms = createComplexStructure("baseline");
        List<Term> optimizedTerms = createComplexStructure("optimized");

        baseline.initializeAll(baselineTerms);
        optimized.initializeAll(optimizedTerms);

        // Perform same merges
        performComplexMerges(baseline, baselineTerms);
        performComplexMerges(optimized, optimizedTerms);

        // Verify same number of classes
        assertEquals(baseline.getClassCount(), optimized.getClassCount());

        // Verify same equivalence relationships
        for (int i = 0; i < baselineTerms.size(); i++) {
            for (int j = i + 1; j < baselineTerms.size(); j++) {
                boolean baselineEqual = baseline.areEqual(baselineTerms.get(i), baselineTerms.get(j));
                boolean optimizedEqual = optimized.areEqual(optimizedTerms.get(i), optimizedTerms.get(j));
                assertEquals(baselineEqual, optimizedEqual,
                    "Equivalence should match at indices " + i + " and " + j);
            }
        }
    }

    // Helper methods

    private List<Term> createChain(String prefix, int length) {
        List<Term> terms = new ArrayList<>();
        for (int i = 0; i < length; i++) {
            terms.add(factory.createVariable(prefix + "_" + i));
        }
        return terms;
    }

    private List<Term> createTermList(String prefix, int count) {
        List<Term> terms = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            terms.add(factory.createVariable(prefix + "_v" + i));
        }
        return terms;
    }

    private List<List<Term>> createMultipleClasses(String prefix, int numClasses, int classSize) {
        List<List<Term>> classes = new ArrayList<>();
        for (int i = 0; i < numClasses; i++) {
            List<Term> cls = new ArrayList<>();
            for (int j = 0; j < classSize; j++) {
                cls.add(factory.createVariable(prefix + "_c" + i + "_t" + j));
            }
            classes.add(cls);
        }
        return classes;
    }

    private void mergeChain(ClassManager manager, List<Term> terms) {
        for (int i = 0; i < terms.size() - 1; i++) {
            manager.union(terms.get(i), terms.get(i + 1));
        }
    }

    private void initializeAndMergeClasses(ClassManager manager, List<List<Term>> classes) {
        for (List<Term> cls : classes) {
            manager.initializeAll(cls);
            // Merge all terms in the class
            for (int i = 0; i < cls.size() - 1; i++) {
                manager.union(cls.get(i), cls.get(i + 1));
            }
        }
    }

    private long measureFindOperations(ClassManager manager, List<Term> terms) {
        long start = System.nanoTime();
        for (Term term : terms) {
            manager.find(term);
        }
        return System.nanoTime() - start;
    }

    private long measureUnionOperations(ClassManager manager, List<Term> terms) {
        long start = System.nanoTime();
        for (int i = 0; i < terms.size() - 1; i++) {
            manager.union(terms.get(i), terms.get(i + 1));
        }
        return System.nanoTime() - start;
    }

    private long measureRepeatedFinds(ClassManager manager, List<List<Term>> classes, int iterations) {
        long start = System.nanoTime();
        for (int iter = 0; iter < iterations; iter++) {
            for (List<Term> cls : classes) {
                for (Term term : cls) {
                    manager.find(term);
                }
            }
        }
        return System.nanoTime() - start;
    }

    private List<Term> createComplexStructure(String prefix) {
        List<Term> terms = new ArrayList<>();
        for (int i = 0; i < 30; i++) {
            terms.add(factory.createVariable(prefix + "_x" + i));
        }
        return terms;
    }

    private void performComplexMerges(ClassManager manager, List<Term> terms) {
        // Create several classes with different structures
        // Class 1: {0,1,2,3,4}
        manager.union(terms.get(0), terms.get(1));
        manager.union(terms.get(1), terms.get(2));
        manager.union(terms.get(2), terms.get(3));
        manager.union(terms.get(3), terms.get(4));

        // Class 2: {5,6,7}
        manager.union(terms.get(5), terms.get(6));
        manager.union(terms.get(6), terms.get(7));

        // Class 3: {8,9,10,11,12,13}
        manager.union(terms.get(8), terms.get(9));
        manager.union(terms.get(10), terms.get(11));
        manager.union(terms.get(12), terms.get(13));
        manager.union(terms.get(8), terms.get(10));
        manager.union(terms.get(10), terms.get(12));

        // Remaining terms stay in singleton classes
    }
}
