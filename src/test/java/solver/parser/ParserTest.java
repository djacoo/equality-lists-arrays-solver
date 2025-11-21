package solver.parser;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import solver.dag.TermFactory;
import solver.theory.te.Literal;

import java.util.List;

/**
 * Unit tests for the Parser class.
 * Tests parsing of the custom input format into Literal objects.
 */
public class ParserTest {
    private TermFactory termFactory;

    @BeforeEach
    public void setUp() {
        termFactory = new TermFactory();
    }

    private List<Literal> parse(String input) throws LexerException, ParseException {
        Lexer lexer = new Lexer(input);
        List<Token> tokens = lexer.tokenize();
        Parser parser = new Parser(tokens, termFactory);
        return parser.parse();
    }

    @Test
    public void testEmptyInput() throws LexerException, ParseException {
        List<Literal> literals = parse("");
        assertEquals(0, literals.size());
    }

    @Test
    public void testSimpleEquality() throws LexerException, ParseException {
        List<Literal> literals = parse("a = b");

        assertEquals(1, literals.size());
        assertTrue(literals.get(0).isEquality());
        assertFalse(literals.get(0).isDisequality());
        assertFalse(literals.get(0).isAtom());
    }

    @Test
    public void testSimpleDisequality() throws LexerException, ParseException {
        List<Literal> literals = parse("a != b");

        assertEquals(1, literals.size());
        assertTrue(literals.get(0).isDisequality());
        assertFalse(literals.get(0).isEquality());
        assertFalse(literals.get(0).isAtom());
    }

    @Test
    public void testAtomPredicate() throws LexerException, ParseException {
        List<Literal> literals = parse("atom(x)");

        assertEquals(1, literals.size());
        assertTrue(literals.get(0).isAtom());
        assertFalse(literals.get(0).isEquality());
        assertFalse(literals.get(0).isDisequality());
    }

    @Test
    public void testNegatedAtomPredicate() throws LexerException, ParseException {
        List<Literal> literals = parse("!atom(x)");

        assertEquals(1, literals.size());
        assertTrue(literals.get(0).isAtom());
        assertFalse(literals.get(0).isEquality());
        assertFalse(literals.get(0).isDisequality());
    }

    @Test
    public void testAtomWithUnicodeNot() throws LexerException, ParseException {
        List<Literal> literals = parse("¬atom(cons(a,b))");

        assertEquals(1, literals.size());
        assertTrue(literals.get(0).isAtom());
    }

    @Test
    public void testFunctionApplicationEquality() throws LexerException, ParseException {
        List<Literal> literals = parse("f(x) = g(y)");

        assertEquals(1, literals.size());
        assertTrue(literals.get(0).isEquality());
    }

    @Test
    public void testNestedFunctions() throws LexerException, ParseException {
        List<Literal> literals = parse("f(g(h(x))) = y");

        assertEquals(1, literals.size());
        assertTrue(literals.get(0).isEquality());
    }

    @Test
    public void testBinaryFunctions() throws LexerException, ParseException {
        List<Literal> literals = parse("cons(x, y) = l");

        assertEquals(1, literals.size());
        assertTrue(literals.get(0).isEquality());
    }

    @Test
    public void testListOperations() throws LexerException, ParseException {
        List<Literal> literals = parse("car(cons(x, y)) = x");

        assertEquals(1, literals.size());
        assertTrue(literals.get(0).isEquality());
    }

    @Test
    public void testArrayOperations() throws LexerException, ParseException {
        List<Literal> literals = parse("select(store(a, i, v), i) = v");

        assertEquals(1, literals.size());
        assertTrue(literals.get(0).isEquality());
    }

    @Test
    public void testMultipleLiterals() throws LexerException, ParseException {
        List<Literal> literals = parse("a = b\nb = c\nc != a");

        assertEquals(3, literals.size());
        assertTrue(literals.get(0).isEquality());
        assertTrue(literals.get(1).isEquality());
        assertTrue(literals.get(2).isDisequality());
    }

    @Test
    public void testLiteralsWithComments() throws LexerException, ParseException {
        List<Literal> literals = parse("# Comment\na = b\n// Another comment\nc = d");

        assertEquals(2, literals.size());
        assertTrue(literals.get(0).isEquality());
        assertTrue(literals.get(1).isEquality());
    }

    @Test
    public void testLiteralsWithBlankLines() throws LexerException, ParseException {
        List<Literal> literals = parse("a = b\n\n\nc = d");

        assertEquals(2, literals.size());
    }

    @Test
    public void testEmptyFunctionApplication() throws LexerException, ParseException {
        List<Literal> literals = parse("nil() = l");

        assertEquals(1, literals.size());
        assertTrue(literals.get(0).isEquality());
    }

    @Test
    public void testComplexCombination() throws LexerException, ParseException {
        List<Literal> literals = parse(
            "cons(x, y) = l1\n" +
            "cons(x, y) = l2\n" +
            "car(l1) = x\n" +
            "atom(x)"
        );

        assertEquals(4, literals.size());
        assertTrue(literals.get(0).isEquality());
        assertTrue(literals.get(1).isEquality());
        assertTrue(literals.get(2).isEquality());
        assertTrue(literals.get(3).isAtom());
    }

    @Test
    public void testMissingEquals() {
        ParseException exception = assertThrows(ParseException.class, () -> {
            parse("a b");
        });

        assertTrue(exception.getMessage().contains("Expected '='") ||
                   exception.getMessage().contains("Expected newline or EOF"));
    }

    @Test
    public void testMissingRightSide() {
        ParseException exception = assertThrows(ParseException.class, () -> {
            parse("a =");
        });

        assertTrue(exception.getMessage().contains("Expected identifier"));
    }

    @Test
    public void testMissingLeftParen() {
        ParseException exception = assertThrows(ParseException.class, () -> {
            parse("f x) = y");
        });

        assertTrue(exception.getMessage().contains("Expected '='") ||
                   exception.getMessage().contains("Expected newline or EOF"));
    }

    @Test
    public void testMissingRightParen() {
        ParseException exception = assertThrows(ParseException.class, () -> {
            parse("f(x = y");
        });

        assertTrue(exception.getMessage().contains("Expected ')'"));
    }

    @Test
    public void testMissingArgument() {
        ParseException exception = assertThrows(ParseException.class, () -> {
            parse("f(x,) = y");
        });

        assertTrue(exception.getMessage().contains("Expected identifier"));
    }

    @Test
    public void testInvalidAtomSyntax() {
        ParseException exception = assertThrows(ParseException.class, () -> {
            parse("atom x");
        });

        assertTrue(exception.getMessage().contains("Expected '('"));
    }

    @Test
    public void testNotWithoutAtom() {
        ParseException exception = assertThrows(ParseException.class, () -> {
            parse("!x = y");
        });

        assertTrue(exception.getMessage().contains("Expected 'atom'"));
    }

    @Test
    public void testExtraTokensAfterLiteral() {
        ParseException exception = assertThrows(ParseException.class, () -> {
            parse("a = b c");
        });

        assertTrue(exception.getMessage().contains("Expected newline or EOF"));
    }

    @Test
    public void testWhitespaceInsensitivity() throws LexerException, ParseException {
        List<Literal> literals1 = parse("a=b");
        List<Literal> literals2 = parse("a = b");
        List<Literal> literals3 = parse("a   =   b");

        assertEquals(1, literals1.size());
        assertEquals(1, literals2.size());
        assertEquals(1, literals3.size());

        // All should parse successfully
        assertTrue(literals1.get(0).isEquality());
        assertTrue(literals2.get(0).isEquality());
        assertTrue(literals3.get(0).isEquality());
    }

    @Test
    public void testNoCommaInFunctionArgs() {
        ParseException exception = assertThrows(ParseException.class, () -> {
            parse("f(x y) = z");
        });

        assertTrue(exception.getMessage().contains("Expected ')'"));
    }

    @Test
    public void testTrailingComma() {
        ParseException exception = assertThrows(ParseException.class, () -> {
            parse("f(x, y,) = z");
        });

        assertTrue(exception.getMessage().contains("Expected identifier"));
    }

    @Test
    public void testAtomMissingCloseParen() {
        ParseException exception = assertThrows(ParseException.class, () -> {
            parse("atom(x");
        });

        assertTrue(exception.getMessage().contains("Expected ')'"));
    }

    @Test
    public void testVariableNames() throws LexerException, ParseException {
        List<Literal> literals = parse("var1 = var_2");

        assertEquals(1, literals.size());
        assertTrue(literals.get(0).isEquality());
    }

    @Test
    public void testFunctionNames() throws LexerException, ParseException {
        List<Literal> literals = parse("my_func(arg_1, arg2) = result");

        assertEquals(1, literals.size());
        assertTrue(literals.get(0).isEquality());
    }

    @Test
    public void testComplexNestedStructure() throws LexerException, ParseException {
        List<Literal> literals = parse("select(store(store(a, i, v1), j, v2), i) = v1");

        assertEquals(1, literals.size());
        assertTrue(literals.get(0).isEquality());
    }

    @Test
    public void testAtomWithComplexTerm() throws LexerException, ParseException {
        List<Literal> literals = parse("atom(car(cdr(cons(x, cons(y, nil())))))");

        assertEquals(1, literals.size());
        assertTrue(literals.get(0).isAtom());
    }
}
