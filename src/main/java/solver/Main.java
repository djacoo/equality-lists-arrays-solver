package solver;

import solver.dag.TermFactory;
import solver.parser.InputReader;
import solver.parser.LexerException;
import solver.parser.ParseException;
import solver.theory.Result;
import solver.theory.te.Literal;

import java.io.IOException;
import java.util.List;

/**
 * Main entry point for the Equality, Lists, and Arrays Solver.
 *
 * This solver implements satisfiability checking for the union of theories:
 * - T_E: Theory of Equality (Congruence Closure)
 * - T_cons: Theory of Lists (cons, car, cdr)
 * - T_A: Theory of Arrays (select, store)
 *
 * Based on Bradley & Manna, Sections 9.3-9.5
 *
 * Usage:
 *   java solver.Main                  # Read from stdin
 *   java solver.Main <input-file>     # Read from file
 *   java solver.Main -h | --help      # Show help
 *
 * Input Format:
 *   - One literal per line
 *   - Equalities: a = b, f(x) = g(y)
 *   - Disequalities: a != b
 *   - Atom predicates: atom(x), !atom(cons(a,b))
 *   - Comments: # comment or // comment
 *   - Blank lines are ignored
 *
 * Examples:
 *   a = b
 *   b = c
 *   c != a
 *
 *   cons(x, y) = z
 *   atom(x)
 *   !atom(z)
 *
 *   select(store(a, i, v), j) = v
 *   i = j
 */
public class Main {

    // ANSI Color codes
    private static final String RESET = "\033[0m";
    private static final String GREEN = "\033[1;32m";
    private static final String RED = "\033[1;31m";
    private static final String YELLOW = "\033[1;33m";
    private static final String CYAN = "\033[1;36m";
    private static final String BLUE = "\033[1;34m";
    private static final String BOLD = "\033[1m";

    public static void main(String[] args) {
        // Check for help flag
        if (args.length > 0 && (args[0].equals("-h") || args[0].equals("--help"))) {
            printHelp();
            return;
        }

        printBanner();
        System.out.println();

        try {
            // Create term factory and input reader
            TermFactory factory = new TermFactory();
            InputReader reader = new InputReader(factory);

            // Parse input
            List<Literal> literals;
            if (args.length == 0) {
                // Read from stdin
                System.out.println("Reading from stdin (press Enter twice to finish)...");
                System.out.println();
                literals = reader.readFromStdin();
            } else {
                // Read from file
                String filePath = args[0];
                System.out.println("Reading from file: " + filePath);
                System.out.println();
                literals = reader.readFromFile(filePath);
            }

            // Display parsed literals
            System.out.println(CYAN + "Parsed " + literals.size() + " literal(s):" + RESET);
            for (int i = 0; i < literals.size(); i++) {
                System.out.println("  " + (i + 1) + ". " + literals.get(i));
            }
            System.out.println();

            // Check satisfiability
            UnifiedSolver solver = new UnifiedSolver(factory);
            long startTime = System.nanoTime();
            Result result = solver.checkSat(literals);
            long endTime = System.nanoTime();
            double elapsedMs = (endTime - startTime) / 1_000_000.0;

            // Display result with colors
            String resultColor = result.isSat() ? GREEN : RED;
            String resultText = result.isSat() ? "SAT" : "UNSAT";
            System.out.println(BOLD + "Result: " + resultColor + resultText + RESET);
            System.out.println(YELLOW + "Time: " + String.format("%.3f ms", elapsedMs) + RESET);
            System.out.println();

            if (result.isSat()) {
                System.out.println(GREEN + "✓ The literal set is SATISFIABLE." + RESET);
                System.out.println("  This means there exists a model that satisfies all constraints.");
                if (result.getWitness() != null) {
                    System.out.println();
                    System.out.println(CYAN + "Equivalence classes:" + RESET);
                    System.out.println(result.getWitness());
                }
            } else {
                System.out.println(RED + "✗ The literal set is UNSATISFIABLE." + RESET);
                System.out.println("  This means no model exists that satisfies all constraints.");
                if (result.getConflict() != null && !result.getConflict().isEmpty()) {
                    System.out.println();
                    System.out.println(RED + "Conflict detected:" + RESET);
                    System.out.println(result.getConflict());
                }
            }

        } catch (IOException e) {
            System.err.println("Error reading input: " + e.getMessage());
            System.exit(1);
        } catch (LexerException e) {
            System.err.println("Lexer error: " + e.getMessage());
            System.err.println("\nPlease check your input syntax.");
            System.exit(1);
        } catch (ParseException e) {
            System.err.println("Parse error: " + e.getMessage());
            System.err.println("\nPlease check your input syntax.");
            System.exit(1);
        } catch (Exception e) {
            System.err.println("Unexpected error: " + e.getMessage());
            e.printStackTrace();
            System.exit(1);
        }
    }

    /**
     * Prints the ASCII art banner.
     */
    private static void printBanner() {
        System.out.println(CYAN + "╔══════════════════════════════════════════════════════════════════════════╗" + RESET);
        System.out.println(CYAN + "║" + RESET + BOLD + "   ███████╗ ██████╗ ██╗    ██╗   ██╗███████╗██████╗                    " + RESET + CYAN + "║" + RESET);
        System.out.println(CYAN + "║" + RESET + BOLD + "   ██╔════╝██╔═══██╗██║    ██║   ██║██╔════╝██╔══██╗                   " + RESET + CYAN + "║" + RESET);
        System.out.println(CYAN + "║" + RESET + BOLD + "   ███████╗██║   ██║██║    ██║   ██║█████╗  ██████╔╝                   " + RESET + CYAN + "║" + RESET);
        System.out.println(CYAN + "║" + RESET + BOLD + "   ╚════██║██║   ██║██║    ╚██╗ ██╔╝██╔══╝  ██╔══██╗                   " + RESET + CYAN + "║" + RESET);
        System.out.println(CYAN + "║" + RESET + BOLD + "   ███████║╚██████╔╝███████╗╚████╔╝ ███████╗██║  ██║                   " + RESET + CYAN + "║" + RESET);
        System.out.println(CYAN + "║" + RESET + BOLD + "   ╚══════╝ ╚═════╝ ╚══════╝ ╚═══╝  ╚══════╝╚═╝  ╚═╝                   " + RESET + CYAN + "║" + RESET);
        System.out.println(CYAN + "║" + RESET + "                                                                          " + CYAN + "║" + RESET);
        System.out.println(CYAN + "║" + RESET + YELLOW + "        Equality, Lists, and Arrays Solver                            " + RESET + CYAN + "║" + RESET);
        System.out.println(CYAN + "║" + RESET + "        Based on Bradley & Manna, Sections 9.3-9.5                    " + CYAN + "║" + RESET);
        System.out.println(CYAN + "╚══════════════════════════════════════════════════════════════════════════╝" + RESET);
    }

    /**
     * Prints usage help.
     */
    private static void printHelp() {
        System.out.println("Equality, Lists, and Arrays Solver");
        System.out.println("Based on Bradley & Manna, Sections 9.3-9.5");
        System.out.println();
        System.out.println("USAGE:");
        System.out.println("  java solver.Main [OPTIONS] [FILE]");
        System.out.println();
        System.out.println("OPTIONS:");
        System.out.println("  -h, --help    Show this help message");
        System.out.println();
        System.out.println("ARGUMENTS:");
        System.out.println("  FILE          Input file containing literals (if omitted, reads from stdin)");
        System.out.println();
        System.out.println("INPUT FORMAT:");
        System.out.println("  One literal per line");
        System.out.println("  Equalities:     a = b, f(x) = g(y)");
        System.out.println("  Disequalities:  a != b");
        System.out.println("  Atom predicates: atom(x), !atom(cons(a,b))");
        System.out.println("  Comments:       # comment or // comment");
        System.out.println();
        System.out.println("EXAMPLES:");
        System.out.println("  Theory of Equality (T_E):");
        System.out.println("    a = b");
        System.out.println("    b = c");
        System.out.println("    c != a");
        System.out.println();
        System.out.println("  Theory of Lists (T_cons):");
        System.out.println("    cons(x, y) = z");
        System.out.println("    car(z) = x");
        System.out.println("    atom(x)");
        System.out.println();
        System.out.println("  Theory of Arrays (T_A):");
        System.out.println("    select(store(a, i, v), j) = v");
        System.out.println("    i = j");
        System.out.println();
    }
}
