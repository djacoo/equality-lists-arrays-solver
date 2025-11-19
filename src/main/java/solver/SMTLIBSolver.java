package solver;

import solver.parser.LexerException;
import solver.parser.ParseException;
import solver.parser.SMTLIBInputReader;
import solver.theory.te.Literal;
import solver.theory.Result;

import java.io.IOException;
import java.util.List;

/**
 * Main entry point for solving SMT-LIB format files.
 *
 * Usage:
 *   java solver.SMTLIBSolver <file.smt2>
 *   cat <file.smt2> | java solver.SMTLIBSolver
 *
 * Reads SMT-LIB format input and uses the UnifiedSolver to check satisfiability.
 */
public class SMTLIBSolver {

    public static void main(String[] args) {
        try {
            SMTLIBInputReader reader = new SMTLIBInputReader();
            List<Literal> literals;

            // Read from file or stdin
            if (args.length > 0) {
                String filename = args[0];
                System.out.println("Reading from file: " + filename);
                literals = reader.readFromFile(filename);
            } else {
                System.out.println("Reading from stdin...");
                literals = reader.readFromStdin();
            }

            System.out.println("\nParsed " + literals.size() + " literals:");
            for (int i = 0; i < literals.size(); i++) {
                System.out.println("  " + (i + 1) + ". " + literals.get(i));
            }

            // Solve
            System.out.println("\nSolving...");
            UnifiedSolver solver = new UnifiedSolver();
            Result result = solver.checkSat(literals);

            // Output result (Result.toString() includes all details)
            System.out.println("\n" + result);

        } catch (IOException e) {
            System.err.println("Error reading input: " + e.getMessage());
            System.exit(1);
        } catch (LexerException e) {
            System.err.println("Lexer error: " + e.getMessage());
            System.exit(1);
        } catch (ParseException e) {
            System.err.println("Parse error: " + e.getMessage());
            System.exit(1);
        } catch (Exception e) {
            System.err.println("Unexpected error: " + e.getMessage());
            e.printStackTrace();
            System.exit(1);
        }
    }
}
