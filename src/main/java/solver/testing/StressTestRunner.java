package solver.testing;

import solver.UnifiedSolver;
import solver.config.SolverConfig;
import solver.theory.Result;
import solver.theory.te.Literal;
import solver.theory.te.TEProcedure;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;

/**
 * Command-line tool for generating and running stress tests.
 *
 * Generates synthetic test cases of various sizes and theories,
 * tests them with the solver, and optionally saves them to files.
 */
public class StressTestRunner {

    private static final String OUTPUT_DIR = "experiments/generated_tests/";

    public static void main(String[] args) {
        System.out.println("========================================");
        System.out.println("  Stress Test Runner");
        System.out.println("========================================");
        System.out.println();

        RandomTestGenerator generator = new RandomTestGenerator(System.currentTimeMillis());

        // Test 1: Equality theory (T_E) tests of increasing size
        System.out.println("Test Suite 1: Equality Theory (T_E)");
        System.out.println("-----------------------------------");
        runEqualityTests(generator);
        System.out.println();

        // Test 2: List theory (T_cons) tests
        System.out.println("Test Suite 2: List Theory (T_cons)");
        System.out.println("-----------------------------------");
        runListTests(generator);
        System.out.println();

        // Test 3: SAT vs UNSAT tests
        System.out.println("Test Suite 3: SAT/UNSAT Tests");
        System.out.println("-----------------------------------");
        runSatUnsatTests(generator);
        System.out.println();

        // Test 4: Stress tests with forbidden set optimization
        System.out.println("Test Suite 4: Forbidden Set Optimization");
        System.out.println("-----------------------------------");
        runForbiddenSetTests(generator);
        System.out.println();

        System.out.println("========================================");
        System.out.println("  All Stress Tests Completed");
        System.out.println("========================================");
    }

    private static void runEqualityTests(RandomTestGenerator generator) {
        int[] sizes = {5, 10, 20, 50, 100};
        int passCount = 0;
        int totalCount = 0;

        for (int size : sizes) {
            int numVars = size;
            int numEqs = size * 2;
            int numDiseqs = size / 2;

            System.out.printf("  Size %d (vars=%d, eqs=%d, diseqs=%d): ", size, numVars, numEqs, numDiseqs);

            try {
                List<Literal> literals = generator.generateTest(numVars, numEqs, numDiseqs);
                UnifiedSolver solver = new UnifiedSolver();
                long startTime = System.nanoTime();
                Result result = solver.checkSat(literals);
                long endTime = System.nanoTime();
                double timeMs = (endTime - startTime) / 1_000_000.0;

                System.out.printf("%s (%.2f ms)%n", result.isSat() ? "SAT" : "UNSAT", timeMs);

                // Optionally save larger tests
                if (size >= 50) {
                    saveTestToFile("equality_" + size + ".txt", literals, result);
                }

                passCount++;
            } catch (Exception e) {
                System.out.printf("ERROR: %s%n", e.getMessage());
            }
            totalCount++;
        }

        System.out.printf("  Results: %d/%d passed%n", passCount, totalCount);
    }

    private static void runListTests(RandomTestGenerator generator) {
        int[] sizes = {5, 10, 15, 20};
        int passCount = 0;
        int totalCount = 0;

        for (int size : sizes) {
            System.out.printf("  Size %d: ", size);

            try {
                List<Literal> literals = generator.generateListTest(size, size / 2, size * 2, size);
                UnifiedSolver solver = new UnifiedSolver();
                long startTime = System.nanoTime();
                Result result = solver.checkSat(literals);
                long endTime = System.nanoTime();
                double timeMs = (endTime - startTime) / 1_000_000.0;

                System.out.printf("%s (%.2f ms)%n", result.isSat() ? "SAT" : "UNSAT", timeMs);

                if (size >= 15) {
                    saveTestToFile("list_" + size + ".txt", literals, result);
                }

                passCount++;
            } catch (Exception e) {
                System.out.printf("ERROR: %s%n", e.getMessage());
            }
            totalCount++;
        }

        System.out.printf("  Results: %d/%d passed%n", passCount, totalCount);
    }

    private static void runSatUnsatTests(RandomTestGenerator generator) {
        int passCount = 0;
        int totalCount = 0;

        // Generate guaranteed SAT tests
        System.out.println("  Guaranteed SAT tests:");
        for (int i = 0; i < 5; i++) {
            int numVars = 10 + i * 10;
            int numClasses = 3 + i;
            System.out.printf("    SAT test %d (vars=%d, classes=%d): ", i + 1, numVars, numClasses);

            try {
                List<Literal> literals = generator.generateSatTest(numVars, numClasses);
                UnifiedSolver solver = new UnifiedSolver();
                Result result = solver.checkSat(literals);

                if (result.isSat()) {
                    System.out.println("SAT ✓");
                    passCount++;
                } else {
                    System.out.println("UNEXPECTED UNSAT ✗");
                }
            } catch (Exception e) {
                System.out.printf("ERROR: %s%n", e.getMessage());
            }
            totalCount++;
        }

        // Generate guaranteed UNSAT tests
        System.out.println("  Guaranteed UNSAT tests:");
        for (int i = 0; i < 5; i++) {
            int chainLength = 5 + i * 5;
            System.out.printf("    UNSAT test %d (chain=%d): ", i + 1, chainLength);

            try {
                List<Literal> literals = generator.generateUnsatTest(chainLength);
                UnifiedSolver solver = new UnifiedSolver();
                Result result = solver.checkSat(literals);

                if (result.isUnsat()) {
                    System.out.println("UNSAT ✓");
                    passCount++;
                } else {
                    System.out.println("UNEXPECTED SAT ✗");
                }
            } catch (Exception e) {
                System.out.printf("ERROR: %s%n", e.getMessage());
            }
            totalCount++;
        }

        System.out.printf("  Results: %d/%d passed%n", passCount, totalCount);
    }

    private static void runForbiddenSetTests(RandomTestGenerator generator) {
        int passCount = 0;
        int totalCount = 0;

        SolverConfig config = SolverConfig.withForbiddenSet();

        for (int i = 0; i < 5; i++) {
            int chainLength = 5 + i * 2;
            System.out.printf("  UNSAT test %d with forbidden set (chain=%d): ", i + 1, chainLength);

            try {
                List<Literal> literals = generator.generateUnsatTest(chainLength);
                // Use TEProcedure directly since these are pure equality tests
                TEProcedure solver = new TEProcedure(config);
                long startTime = System.nanoTime();
                Result result = solver.checkSat(literals);
                long endTime = System.nanoTime();
                double timeMs = (endTime - startTime) / 1_000_000.0;

                if (result.isUnsat()) {
                    System.out.printf("UNSAT ✓ (%.2f ms", timeMs);
                    if (result.getConflict().isPresent()) {
                        System.out.print(", early detection");
                    }
                    System.out.println(")");
                    passCount++;
                } else {
                    System.out.println("UNEXPECTED SAT ✗");
                }
            } catch (Exception e) {
                System.out.printf("ERROR: %s%n", e.getMessage());
            }
            totalCount++;
        }

        System.out.printf("  Results: %d/%d passed%n", passCount, totalCount);
    }

    private static void saveTestToFile(String filename, List<Literal> literals, Result result) {
        try {
            String filepath = OUTPUT_DIR + filename;
            // Create directory if it doesn't exist
            new java.io.File(OUTPUT_DIR).mkdirs();

            try (PrintWriter writer = new PrintWriter(new FileWriter(filepath))) {
                writer.println("# Generated test case");
                writer.println("# Expected result: " + (result.isSat() ? "SAT" : "UNSAT"));
                writer.println("# Number of literals: " + literals.size());
                writer.println();

                for (Literal lit : literals) {
                    writer.println(lit);
                }
            }

            System.out.println("    Saved to " + filepath);
        } catch (IOException e) {
            System.err.println("    Error saving file: " + e.getMessage());
        }
    }
}
