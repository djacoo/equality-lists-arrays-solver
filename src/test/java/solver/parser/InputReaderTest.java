package solver.parser;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import static org.junit.jupiter.api.Assertions.*;

import solver.dag.TermFactory;
import solver.theory.te.Literal;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

/**
 * Unit tests for the InputReader class.
 * Tests reading and parsing input from various sources.
 */
public class InputReaderTest {
    private InputReader inputReader;
    private TermFactory termFactory;

    @TempDir
    Path tempDir;

    @BeforeEach
    public void setUp() {
        termFactory = new TermFactory();
        inputReader = new InputReader(termFactory);
    }

    @Test
    public void testParseInputSimple() throws LexerException, ParseException {
        String input = "a = b";
        List<Literal> literals = inputReader.parseInput(input);

        assertEquals(1, literals.size());
        assertTrue(literals.get(0).isEquality());
    }

    @Test
    public void testParseInputMultipleLiterals() throws LexerException, ParseException {
        String input = "a = b\nb = c\nc != a";
        List<Literal> literals = inputReader.parseInput(input);

        assertEquals(3, literals.size());
        assertTrue(literals.get(0).isEquality());
        assertTrue(literals.get(1).isEquality());
        assertTrue(literals.get(2).isDisequality());
    }

    @Test
    public void testParseInputWithComments() throws LexerException, ParseException {
        String input = "# This is a test\na = b\n// Another comment\nc = d";
        List<Literal> literals = inputReader.parseInput(input);

        assertEquals(2, literals.size());
        assertTrue(literals.get(0).isEquality());
        assertTrue(literals.get(1).isEquality());
    }

    @Test
    public void testParseInputEmpty() throws LexerException, ParseException {
        String input = "";
        List<Literal> literals = inputReader.parseInput(input);

        assertEquals(0, literals.size());
    }

    @Test
    public void testParseInputBlankLines() throws LexerException, ParseException {
        String input = "\n\n\n";
        List<Literal> literals = inputReader.parseInput(input);

        assertEquals(0, literals.size());
    }

    @Test
    public void testParseLiteralSingle() throws LexerException, ParseException {
        String line = "f(x) = g(y)";
        Literal literal = inputReader.parseLiteral(line);

        assertNotNull(literal);
        assertTrue(literal.isEquality());
    }

    @Test
    public void testParseLiteralDisequality() throws LexerException, ParseException {
        String line = "a != b";
        Literal literal = inputReader.parseLiteral(line);

        assertNotNull(literal);
        assertTrue(literal.isDisequality());
    }

    @Test
    public void testParseLiteralAtom() throws LexerException, ParseException {
        String line = "atom(x)";
        Literal literal = inputReader.parseLiteral(line);

        assertNotNull(literal);
        assertTrue(literal.isAtom());
    }

    @Test
    public void testParseLiteralEmpty() {
        ParseException exception = assertThrows(ParseException.class, () -> {
            inputReader.parseLiteral("");
        });

        assertTrue(exception.getMessage().contains("No literal found"));
    }

    @Test
    public void testParseLiteralMultiple() {
        ParseException exception = assertThrows(ParseException.class, () -> {
            inputReader.parseLiteral("a = b\nc = d");
        });

        assertTrue(exception.getMessage().contains("Expected single literal but found 2"));
    }

    @Test
    public void testReadFromFile() throws IOException, LexerException, ParseException {
        // Create a temporary test file
        Path testFile = tempDir.resolve("test_input.txt");
        String content = "a = b\nc = d\ne != f";
        Files.writeString(testFile, content);

        List<Literal> literals = inputReader.readFromFile(testFile.toString());

        assertEquals(3, literals.size());
        assertTrue(literals.get(0).isEquality());
        assertTrue(literals.get(1).isEquality());
        assertTrue(literals.get(2).isDisequality());
    }

    @Test
    public void testReadFromFileWithComments() throws IOException, LexerException, ParseException {
        Path testFile = tempDir.resolve("test_comments.txt");
        String content = "# Test file\na = b\n// Comment\nc = d";
        Files.writeString(testFile, content);

        List<Literal> literals = inputReader.readFromFile(testFile.toString());

        assertEquals(2, literals.size());
    }

    @Test
    public void testReadFromFileEmpty() throws IOException, LexerException, ParseException {
        Path testFile = tempDir.resolve("test_empty.txt");
        Files.writeString(testFile, "");

        List<Literal> literals = inputReader.readFromFile(testFile.toString());

        assertEquals(0, literals.size());
    }

    @Test
    public void testReadFromFileNotFound() {
        assertThrows(IOException.class, () -> {
            inputReader.readFromFile("/nonexistent/file.txt");
        });
    }

    @Test
    public void testReadFromFileComplexTheories() throws IOException, LexerException, ParseException {
        Path testFile = tempDir.resolve("test_theories.txt");
        String content =
            "cons(x, y) = l1\n" +
            "car(l1) = x\n" +
            "select(store(a, i, v), i) = v\n" +
            "atom(x)";
        Files.writeString(testFile, content);

        List<Literal> literals = inputReader.readFromFile(testFile.toString());

        assertEquals(4, literals.size());
        assertTrue(literals.get(0).isEquality());
        assertTrue(literals.get(1).isEquality());
        assertTrue(literals.get(2).isEquality());
        assertTrue(literals.get(3).isAtom());
    }

    @Test
    public void testParseInputMalformedLexer() {
        String input = "a @ b";

        assertThrows(LexerException.class, () -> {
            inputReader.parseInput(input);
        });
    }

    @Test
    public void testParseInputMalformedParser() {
        String input = "a b";

        assertThrows(ParseException.class, () -> {
            inputReader.parseInput(input);
        });
    }

    @Test
    public void testReadFromFileMalformed() throws IOException {
        Path testFile = tempDir.resolve("test_malformed.txt");
        Files.writeString(testFile, "a @ b");

        assertThrows(LexerException.class, () -> {
            inputReader.readFromFile(testFile.toString());
        });
    }

    @Test
    public void testParseInputWithNestedFunctions() throws LexerException, ParseException {
        String input = "f(g(h(x))) = y\nselect(store(store(a, i, v1), j, v2), i) = v1";
        List<Literal> literals = inputReader.parseInput(input);

        assertEquals(2, literals.size());
        assertTrue(literals.get(0).isEquality());
        assertTrue(literals.get(1).isEquality());
    }

    @Test
    public void testParseInputWithAtoms() throws LexerException, ParseException {
        String input = "atom(x)\n!atom(cons(a, b))";
        List<Literal> literals = inputReader.parseInput(input);

        assertEquals(2, literals.size());
        assertTrue(literals.get(0).isAtom());
        assertTrue(literals.get(1).isAtom());
    }

    @Test
    public void testReadFromFileRealExample() throws IOException, LexerException, ParseException {
        Path testFile = tempDir.resolve("test_real.txt");
        String content =
            "# Test case for T_E (Theory of Equality) - UNSAT\n" +
            "# Source: Bradley & Manna, Section 9.3, Example 9.1\n" +
            "# Expected: UNSAT\n" +
            "\n" +
            "# Transitivity leads to contradiction\n" +
            "a = b\n" +
            "b = c\n" +
            "c != a";
        Files.writeString(testFile, content);

        List<Literal> literals = inputReader.readFromFile(testFile.toString());

        assertEquals(3, literals.size());
        assertTrue(literals.get(0).isEquality());
        assertTrue(literals.get(1).isEquality());
        assertTrue(literals.get(2).isDisequality());
    }
}
