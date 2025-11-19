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
 * Unit tests for the SMTLIBInputReader class.
 */
public class SMTLIBInputReaderTest {
    private SMTLIBInputReader reader;
    private TermFactory termFactory;

    @TempDir
    Path tempDir;

    @BeforeEach
    public void setUp() {
        termFactory = new TermFactory();
        reader = new SMTLIBInputReader(termFactory);
    }

    @Test
    public void testReadFromFileSimple() throws IOException, LexerException, ParseException {
        Path testFile = tempDir.resolve("test.smt2");
        String content = "(set-logic QF_UF)\n(assert (= x y))\n(check-sat)";
        Files.writeString(testFile, content);

        List<Literal> literals = reader.readFromFile(testFile.toString());

        assertEquals(1, literals.size());
        assertTrue(literals.get(0).isEquality());
    }

    @Test
    public void testReadFromFileMultipleAsserts() throws IOException, LexerException, ParseException {
        Path testFile = tempDir.resolve("test.smt2");
        String content =
            "(set-logic QF_UF)\n" +
            "(assert (= a b))\n" +
            "(assert (= b c))\n" +
            "(assert (not (= c a)))\n" +
            "(check-sat)";
        Files.writeString(testFile, content);

        List<Literal> literals = reader.readFromFile(testFile.toString());

        assertEquals(3, literals.size());
        assertTrue(literals.get(0).isEquality());
        assertTrue(literals.get(1).isEquality());
        assertTrue(literals.get(2).isDisequality());
    }

    @Test
    public void testReadFromFileWithComments() throws IOException, LexerException, ParseException {
        Path testFile = tempDir.resolve("test.smt2");
        String content =
            "; This is a test file\n" +
            "(set-logic QF_UF)\n" +
            "; Another comment\n" +
            "(assert (= x y))\n" +
            "(check-sat)";
        Files.writeString(testFile, content);

        List<Literal> literals = reader.readFromFile(testFile.toString());

        assertEquals(1, literals.size());
    }

    @Test
    public void testReadFromFileNotFound() {
        assertThrows(IOException.class, () -> {
            reader.readFromFile("/nonexistent/file.smt2");
        });
    }

    @Test
    public void testReadFromFileMalformedLexer() throws IOException {
        Path testFile = tempDir.resolve("malformed.smt2");
        Files.writeString(testFile, "(set-logic \"unterminated)");

        assertThrows(LexerException.class, () -> {
            reader.readFromFile(testFile.toString());
        });
    }

    @Test
    public void testReadFromFileMalformedParser() throws IOException {
        Path testFile = tempDir.resolve("malformed.smt2");
        Files.writeString(testFile, "(unknown-command)");

        assertThrows(ParseException.class, () -> {
            reader.readFromFile(testFile.toString());
        });
    }

    @Test
    public void testReadFromFileComplexTheories() throws IOException, LexerException, ParseException {
        Path testFile = tempDir.resolve("complex.smt2");
        String content =
            "(set-logic QF_UF)\n" +
            "(assert (= (cons x y) l1))\n" +
            "(assert (= (car l1) x))\n" +
            "(assert (= (select (store a i v) i) v))\n" +
            "(check-sat)";
        Files.writeString(testFile, content);

        List<Literal> literals = reader.readFromFile(testFile.toString());

        assertEquals(3, literals.size());
        assertTrue(literals.get(0).isEquality());
        assertTrue(literals.get(1).isEquality());
        assertTrue(literals.get(2).isEquality());
    }

    @Test
    public void testGetTermFactory() {
        assertNotNull(reader.getTermFactory());
        assertSame(termFactory, reader.getTermFactory());
    }

    @Test
    public void testDefaultConstructor() {
        SMTLIBInputReader defaultReader = new SMTLIBInputReader();
        assertNotNull(defaultReader.getTermFactory());
    }
}
