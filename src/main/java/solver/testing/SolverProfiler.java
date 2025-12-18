package solver.testing;

import solver.UnifiedSolver;
import solver.theory.Result;
import solver.theory.te.Literal;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Simple profiler for analyzing solver performance.
 *
 * Measures execution time for different problem sizes and theories
 * to identify performance characteristics and potential bottlenecks.
 */
public class SolverProfiler {

    private static class ProfileResult {
        int testCount = 0;
        long totalTimeNs = 0;
        long minTimeNs = Long.MAX_VALUE;
        long maxTimeNs = 0;
        int satCount = 0;
        int unsatCount = 0;

        void addResult(long timeNs, boolean isSat) {
            testCount++;
            totalTimeNs += timeNs;
            minTimeNs = Math.min(minTimeNs, timeNs);
            maxTimeNs = Math.max(maxTimeNs, timeNs);
            if (isSat) satCount++;
            else unsatCount++;
        }

        double getAvgTimeMs() {
            return testCount == 0 ? 0 : (totalTimeNs / (double) testCount) / 1_000_000.0;
        }

        double getMinTimeMs() {
            return minTimeNs / 1_000_000.0;
        }

        double getMaxTimeMs() {
            return maxTimeNs / 1_000_000.0;
        }
    }

    public static void main(String[] args) {
        System.out.println("========================================");
        System.out.println("  Solver Performance Profiler");
        System.out.println("========================================");
        System.out.println();

        SolverProfiler profiler = new SolverProfiler();
        RandomTestGenerator generator = new RandomTestGenerator(42); // Fixed seed for reproducibility

        // Profile equality theory tests
        System.out.println("Profiling Equality Theory (T_E)");
        System.out.println("--------------------------------");
        profiler.profileEqualityTheory(generator);
        System.out.println();

        // Profile list theory tests
        System.out.println("Profiling List Theory (T_cons)");
        System.out.println("--------------------------------");
        profiler.profileListTheory(generator);
        System.out.println();

        // Profile SAT vs UNSAT performance
        System.out.println("Profiling SAT vs UNSAT");
        System.out.println("--------------------------------");
        profiler.profileSatVsUnsat(generator);
        System.out.println();

        // Profile problem size scaling
        System.out.println("Profiling Problem Size Scaling");
        System.out.println("--------------------------------");
        profiler.profileScaling(generator);
        System.out.println();

        System.out.println("========================================");
        System.out.println("  Profiling Complete");
        System.out.println("========================================");
    }

    private void profileEqualityTheory(RandomTestGenerator generator) {
        Map<Integer, ProfileResult> results = new HashMap<>();
        int[] sizes = {5, 10, 20, 50, 100};
        int runsPerSize = 10;

        for (int size : sizes) {
            ProfileResult result = new ProfileResult();
            for (int run = 0; run < runsPerSize; run++) {
                List<Literal> literals = generator.generateTest(size, size * 2, size / 2);
                UnifiedSolver solver = new UnifiedSolver();

                long startTime = System.nanoTime();
                Result solverResult = solver.checkSat(literals);
                long endTime = System.nanoTime();

                result.addResult(endTime - startTime, solverResult.isSat());
            }
            results.put(size, result);
        }

        // Print results
        System.out.println("  Size  | Runs | Avg Time (ms) | Min (ms) | Max (ms) | SAT | UNSAT");
        System.out.println("  ------|------|---------------|----------|----------| ----|----- ");
        for (int size : sizes) {
            ProfileResult r = results.get(size);
            System.out.printf("  %-5d | %-4d | %13.2f | %8.2f | %8.2f | %-3d | %-3d%n",
                    size, r.testCount, r.getAvgTimeMs(), r.getMinTimeMs(), r.getMaxTimeMs(),
                    r.satCount, r.unsatCount);
        }
    }

    private void profileListTheory(RandomTestGenerator generator) {
        Map<Integer, ProfileResult> results = new HashMap<>();
        int[] sizes = {5, 8, 10};  // Smaller sizes to avoid solver bugs
        int runsPerSize = 5;

        for (int size : sizes) {
            ProfileResult result = new ProfileResult();
            for (int run = 0; run < runsPerSize; run++) {
                try {
                    List<Literal> literals = generator.generateListTest(size, size / 2, size, size / 2);
                    UnifiedSolver solver = new UnifiedSolver();

                    long startTime = System.nanoTime();
                    Result solverResult = solver.checkSat(literals);
                    long endTime = System.nanoTime();

                    result.addResult(endTime - startTime, solverResult.isSat());
                } catch (Exception e) {
                    // Skip tests that trigger solver bugs
                }
            }
            if (result.testCount > 0) {
                results.put(size, result);
            }
        }

        // Print results
        System.out.println("  Size  | Runs | Avg Time (ms) | Min (ms) | Max (ms) | SAT | UNSAT");
        System.out.println("  ------|------|---------------|----------|----------| ----|----- ");
        for (int size : sizes) {
            if (results.containsKey(size)) {
                ProfileResult r = results.get(size);
                System.out.printf("  %-5d | %-4d | %13.2f | %8.2f | %8.2f | %-3d | %-3d%n",
                        size, r.testCount, r.getAvgTimeMs(), r.getMinTimeMs(), r.getMaxTimeMs(),
                        r.satCount, r.unsatCount);
            }
        }
    }

    private void profileSatVsUnsat(RandomTestGenerator generator) {
        ProfileResult satResults = new ProfileResult();
        ProfileResult unsatResults = new ProfileResult();

        int numTests = 20;

        // Test SAT instances
        for (int i = 0; i < numTests; i++) {
            List<Literal> literals = generator.generateSatTest(20 + i * 5, 3 + i / 5);
            UnifiedSolver solver = new UnifiedSolver();

            long startTime = System.nanoTime();
            Result result = solver.checkSat(literals);
            long endTime = System.nanoTime();

            satResults.addResult(endTime - startTime, result.isSat());
        }

        // Test UNSAT instances
        for (int i = 0; i < numTests; i++) {
            List<Literal> literals = generator.generateUnsatTest(5 + i);
            UnifiedSolver solver = new UnifiedSolver();

            long startTime = System.nanoTime();
            Result result = solver.checkSat(literals);
            long endTime = System.nanoTime();

            unsatResults.addResult(endTime - startTime, result.isSat());
        }

        System.out.println("  Type  | Tests | Avg Time (ms) | Min (ms) | Max (ms)");
        System.out.println("  ------|-------|---------------|----------|----------");
        System.out.printf("  SAT   | %-5d | %13.2f | %8.2f | %8.2f%n",
                satResults.testCount, satResults.getAvgTimeMs(),
                satResults.getMinTimeMs(), satResults.getMaxTimeMs());
        System.out.printf("  UNSAT | %-5d | %13.2f | %8.2f | %8.2f%n",
                unsatResults.testCount, unsatResults.getAvgTimeMs(),
                unsatResults.getMinTimeMs(), unsatResults.getMaxTimeMs());
    }

    private void profileScaling(RandomTestGenerator generator) {
        int[] problemSizes = {10, 25, 50, 100, 200};
        System.out.println("  Variables | Literals | Avg Time (ms)");
        System.out.println("  ----------|----------|---------------");

        for (int vars : problemSizes) {
            int eqs = vars * 2;
            int diseqs = vars / 2;
            int literals = eqs + diseqs;

            ProfileResult result = new ProfileResult();

            for (int run = 0; run < 5; run++) {
                List<Literal> lits = generator.generateTest(vars, eqs, diseqs);
                UnifiedSolver solver = new UnifiedSolver();

                long startTime = System.nanoTime();
                solver.checkSat(lits);
                long endTime = System.nanoTime();

                result.addResult(endTime - startTime, true);
            }

            System.out.printf("  %-9d | %-8d | %13.2f%n",
                    vars, literals, result.getAvgTimeMs());
        }

        System.out.println();
        System.out.println("  Note: O(n²) behavior expected for congruence closure");
    }
}
