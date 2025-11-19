package solver.parser;

import solver.dag.TermFactory;
import solver.theory.te.Literal;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;

/**
 * Input reader for SMT-LIB format files.
 *
 * Similar to InputReader but specifically for SMT-LIB format.
 * Can read from stdin or from a file.
 *
 * Usage:
 *   SMTLIBInputReader reader = new SMTLIBInputReader();
 *   List<Literal> literals = reader.readFromFile("problem.smt2");
 *   // or
 *   List<Literal> literals = reader.readFromStdin();
 */
public class SMTLIBInputReader {
    private final TermFactory termFactory;

    public SMTLIBInputReader() {
        this.termFactory = new TermFactory();
    }

    public SMTLIBInputReader(TermFactory termFactory) {
        this.termFactory = termFactory;
    }

    /**
     * Reads SMT-LIB input from stdin.
     *
     * @return List of parsed literals
     * @throws IOException if reading from stdin fails
     * @throws LexerException if tokenization fails
     * @throws ParseException if parsing fails
     */
    public List<Literal> readFromStdin() throws IOException, LexerException, ParseException {
        StringBuilder input = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(System.in))) {
            String line;
            while ((line = reader.readLine()) != null) {
                input.append(line).append('\n');
            }
        }

        return parseInput(input.toString());
    }

    /**
     * Reads SMT-LIB input from a file.
     *
     * @param filename Path to the input file
     * @return List of parsed literals
     * @throws IOException if reading the file fails
     * @throws LexerException if tokenization fails
     * @throws ParseException if parsing fails
     */
    public List<Literal> readFromFile(String filename) throws IOException, LexerException, ParseException {
        StringBuilder input = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new FileReader(filename))) {
            String line;
            while ((line = reader.readLine()) != null) {
                input.append(line).append('\n');
            }
        }

        return parseInput(input.toString());
    }

    /**
     * Parses SMT-LIB input string into literals.
     *
     * @param input SMT-LIB formatted input string
     * @return List of parsed literals
     * @throws LexerException if tokenization fails
     * @throws ParseException if parsing fails
     */
    private List<Literal> parseInput(String input) throws LexerException, ParseException {
        // Tokenize
        SMTLIBLexer lexer = new SMTLIBLexer(input);
        List<SMTLIBToken> tokens = lexer.tokenize();

        // Parse
        SMTLIBParser parser = new SMTLIBParser(tokens, termFactory);
        return parser.parse();
    }

    /**
     * Returns the term factory used by this reader.
     * Useful for accessing the DAG after parsing.
     */
    public TermFactory getTermFactory() {
        return termFactory;
    }
}
