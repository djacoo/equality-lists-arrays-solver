package solver.parser;

import solver.dag.TermFactory;
import solver.theory.te.Literal;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

/**
 * Reads and parses input from files or stdin.
 *
 * This class provides a unified interface for reading literal sets from various sources,
 * handling the complete pipeline: reading → lexing → parsing → Literal objects.
 */
public class InputReader {
    private final TermFactory termFactory;

    public InputReader(TermFactory termFactory) {
        this.termFactory = termFactory;
    }

    /**
     * Reads and parses input from standard input.
     *
     * @return List of parsed literals
     * @throws IOException if reading fails
     * @throws LexerException if tokenization fails
     * @throws ParseException if parsing fails
     */
    public List<Literal> readFromStdin() throws IOException, LexerException, ParseException {
        StringBuilder input = new StringBuilder();
        BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));

        String line;
        while ((line = reader.readLine()) != null) {
            input.append(line).append("\n");
        }

        return parseInput(input.toString());
    }

    /**
     * Reads and parses input from a file.
     *
     * @param filePath Path to the input file
     * @return List of parsed literals
     * @throws IOException if reading fails
     * @throws LexerException if tokenization fails
     * @throws ParseException if parsing fails
     */
    public List<Literal> readFromFile(String filePath) throws IOException, LexerException, ParseException {
        String input = new String(Files.readAllBytes(Paths.get(filePath)));
        return parseInput(input);
    }

    /**
     * Parses a string containing literals.
     *
     * @param input Input string
     * @return List of parsed literals
     * @throws LexerException if tokenization fails
     * @throws ParseException if parsing fails
     */
    public List<Literal> parseInput(String input) throws LexerException, ParseException {
        // Lexical analysis
        Lexer lexer = new Lexer(input);
        List<Token> tokens = lexer.tokenize();

        // Syntactic analysis
        Parser parser = new Parser(tokens, termFactory);
        return parser.parse();
    }

    /**
     * Parses a single line containing one literal.
     *
     * @param line Input line
     * @return Parsed literal
     * @throws LexerException if tokenization fails
     * @throws ParseException if parsing fails
     */
    public Literal parseLiteral(String line) throws LexerException, ParseException {
        List<Literal> literals = parseInput(line);

        if (literals.isEmpty()) {
            throw new ParseException("No literal found in input");
        }

        if (literals.size() > 1) {
            throw new ParseException("Expected single literal but found " + literals.size());
        }

        return literals.get(0);
    }
}
