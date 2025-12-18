package solver.performance;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import solver.UnifiedSolver;
import solver.config.SolverConfig;
import solver.dag.Term;
import solver.dag.TermFactory;
import solver.testing.RandomTestGenerator;
import solver.theory.Result;
import solver.theory.te.Literal;
import solver.theory.te.TEProcedure;

import java.util.ArrayList;
import java.util.List;

/**
 * Performance profiling tests to identify hot paths and measure optimizations.
 * Phase 5.3: Performance Tuning
 */
@DisplayName("Performance Profiling Tests")
public class PerformanceProfileTest {

    @Test
    @DisplayName("Profile: Term creation and hash-consing overhead")
    void profileTermCreation() {
        TermFactory factory = new TermFactory();
        int iterations = 10000;

        // Measure variable creation
        long start = System.nanoTime();
        for (int i = 0; i < iterations; i++) {
            factory.createVariable("x" + i);
        }
        long varTime = System.nanoTime() - start;

        // Measure function application creation
        Term x = factory.createVariable("x");
        start = System.nanoTime();
        for (int i = 0; i < iterations; i++) {
            factory.createFunctionApp("f", x);
        }
        long funcTime = System.nanoTime() - start;

        // Measure hash-consing effectiveness (repeated creation)
        start = System.nanoTime();
        for (int i = 0; i < iterations; i++) {
            factory.createVariable("x"); // Same variable
        }
        long hashConsTime = System.nanoTime() - start;

        System.out.println("Term Creation Profiling:");
        System.out.println("  Variables: " + (varTime / iterations) + " ns/op");
        System.out.println("  Functions: " + (funcTime / iterations) + " ns/op");
        System.out.println("  Hash-cons: " + (hashConsTime / iterations) + " ns/op");
        System.out.println("  Hash-cons speedup: " + String.format("%.2fx", (double) varTime / hashConsTime));
    }

    @Test
    @DisplayName("Profile: Congruence closure operations")
    void profileCongruenceClosure() {
        SolverConfig config = SolverConfig.allOptimizations();
        TEProcedure solver = new TEProcedure(config);
        TermFactory factory = new TermFactory();

        // Create a large equality chain
        int chainLength = 1000;
        List<Literal> literals = new ArrayList<>();
        for (int i = 0; i < chainLength - 1; i++) {
            Term t1 = factory.createVariable("x" + i);
            Term t2 = factory.createVariable("x" + (i + 1));
            literals.add(Literal.equality(t1, t2));
        }

        // Profile the solving
        long start = System.nanoTime();
        Result result = solver.checkSat(literals);
        long time = System.nanoTime() - start;

        System.out.println("Congruence Closure Profiling:");
        System.out.println("  Chain length: " + chainLength);
        System.out.println("  Result: " + (result.isSat() ? "SAT" : "UNSAT"));
        System.out.println("  Total time: " + (time / 1_000_000.0) + " ms");
        System.out.println("  Time per merge: " + (time / chainLength) + " ns");
    }

    @Test
    @DisplayName("Profile: Collection allocations in hot paths")
    void profileCollectionAllocations() {
        RandomTestGenerator generator = new RandomTestGenerator(42);
        UnifiedSolver solver = new UnifiedSolver();

        int[] problemSizes = {10, 50, 100, 200};
        System.out.println("Collection Allocation Profiling:");

        for (int size : problemSizes) {
            List<Literal> literals = generator.generateTest(size, size * 2, size / 2);

            // Warm up
            for (int i = 0; i < 5; i++) {
                solver.checkSat(literals);
            }

            // Measure
            long start = System.nanoTime();
            int runs = 100;
            for (int i = 0; i < runs; i++) {
                solver.checkSat(literals);
            }
            long time = System.nanoTime() - start;

            System.out.println("  Problem size " + size + ": " +
                             (time / runs / 1_000_000.0) + " ms/run");
        }
    }

    @Test
    @DisplayName("Profile: FIND vs UNION ratio in different workloads")
    void profileFindUnionRatio() {
        TermFactory factory = new TermFactory();

        // FIND-heavy workload
        System.out.println("FIND-heavy workload:");
        List<Literal> findHeavy = new ArrayList<>();
        for (int i = 0; i < 50; i++) {
            Term t1 = factory.createVariable("x" + i);
            Term t2 = factory.createVariable("x" + (i + 1));
            findHeavy.add(Literal.equality(t1, t2));
        }
        // Add many disequality checks (FIND operations)
        for (int i = 0; i < 100; i++) {
            Term t1 = factory.createVariable("x" + i);
            Term t2 = factory.createVariable("y" + i);
            findHeavy.add(Literal.disequality(t1, t2));
        }

        TEProcedure solver = new TEProcedure(SolverConfig.allOptimizations());
        long start = System.nanoTime();
        solver.checkSat(findHeavy);
        long time = System.nanoTime() - start;
        System.out.println("  Time: " + (time / 1_000_000.0) + " ms");

        // UNION-heavy workload
        System.out.println("UNION-heavy workload:");
        List<Literal> unionHeavy = new ArrayList<>();
        for (int i = 0; i < 200; i++) {
            Term t1 = factory.createVariable("z" + i);
            Term t2 = factory.createVariable("z" + (i + 1));
            unionHeavy.add(Literal.equality(t1, t2));
        }

        solver = new TEProcedure(SolverConfig.allOptimizations());
        start = System.nanoTime();
        solver.checkSat(unionHeavy);
        time = System.nanoTime() - start;
        System.out.println("  Time: " + (time / 1_000_000.0) + " ms");
    }

    @Test
    @DisplayName("Profile: Theory-specific overhead (T_E vs T_cons vs T_A)")
    void profileTheoryOverhead() {
        RandomTestGenerator generator = new RandomTestGenerator(42);
        UnifiedSolver solver = new UnifiedSolver();
        int problemSize = 50;
        int runs = 50;

        System.out.println("Theory-Specific Overhead:");

        // T_E (pure equality)
        long teTime = 0;
        for (int i = 0; i < runs; i++) {
            List<Literal> literals = generator.generateTest(problemSize, problemSize * 2, problemSize / 2);
            long start = System.nanoTime();
            solver.checkSat(literals);
            teTime += System.nanoTime() - start;
        }
        System.out.println("  T_E average: " + (teTime / runs / 1_000_000.0) + " ms");

        // Note: T_cons and T_A would require specific test generation
        System.out.println("  (T_cons and T_A profiling would require specific test cases)");
    }

    @Test
    @DisplayName("Profile: Memory allocation patterns")
    void profileMemoryPatterns() {
        System.out.println("Memory Allocation Profiling:");

        Runtime runtime = Runtime.getRuntime();
        System.gc(); // Suggest garbage collection

        long beforeMemory = runtime.totalMemory() - runtime.freeMemory();

        // Create many terms
        TermFactory factory = new TermFactory();
        for (int i = 0; i < 10000; i++) {
            factory.createVariable("x" + i);
            factory.createFunctionApp("f", factory.createVariable("y" + i));
        }

        long afterMemory = runtime.totalMemory() - runtime.freeMemory();
        long memoryUsed = afterMemory - beforeMemory;

        System.out.println("  Memory used for 10000 terms: " +
                         (memoryUsed / 1024) + " KB");
        System.out.println("  Average per term: " +
                         (memoryUsed / 20000) + " bytes");
    }

    @Test
    @DisplayName("Profile: Optimization impact (baseline vs all optimizations)")
    void profileOptimizationImpact() {
        RandomTestGenerator generator = new RandomTestGenerator(42);
        int problemSize = 100;
        int runs = 20;

        System.out.println("Optimization Impact Profiling:");

        // Baseline
        long baselineTime = 0;
        for (int i = 0; i < runs; i++) {
            List<Literal> literals = generator.generateTest(problemSize, problemSize * 2, problemSize / 2);
            TEProcedure solver = new TEProcedure(SolverConfig.baseline());
            long start = System.nanoTime();
            solver.checkSat(literals);
            baselineTime += System.nanoTime() - start;
        }

        // All optimizations
        long optimizedTime = 0;
        for (int i = 0; i < runs; i++) {
            List<Literal> literals = generator.generateTest(problemSize, problemSize * 2, problemSize / 2);
            TEProcedure solver = new TEProcedure(SolverConfig.allOptimizations());
            long start = System.nanoTime();
            solver.checkSat(literals);
            optimizedTime += System.nanoTime() - start;
        }

        double baselineAvg = baselineTime / runs / 1_000_000.0;
        double optimizedAvg = optimizedTime / runs / 1_000_000.0;
        double speedup = baselineAvg / optimizedAvg;

        System.out.println("  Baseline average: " + baselineAvg + " ms");
        System.out.println("  Optimized average: " + optimizedAvg + " ms");
        System.out.println("  Speedup: " + String.format("%.2fx", speedup));
    }

    @Test
    @DisplayName("Profile: Hot path identification")
    void profileHotPaths() {
        System.out.println("Hot Path Identification:");
        System.out.println("  Based on code analysis:");
        System.out.println("    1. ClassManager.find() - Called on every equality check");
        System.out.println("    2. ClassManager.union() - Called for every merge");
        System.out.println("    3. MergeManager.merge() - Propagates congruences");
        System.out.println("    4. CongruenceChecker.areCongruent() - Checks term congruence");
        System.out.println("    5. TermFactory operations - Hash-consing overhead");
        System.out.println("    6. Collection operations in ccpar management");
    }
}
