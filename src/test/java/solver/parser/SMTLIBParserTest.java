package solver.parser;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import solver.dag.TermFactory;
import solver.theory.te.Literal;

import java.util.List;

/**
 * Unit tests for the SMTLIBParser class.
 * Tests parsing of SMT-LIB format.
 */
public class SMTLIBParserTest {
    private TermFactory termFactory;

    @BeforeEach
    public void setUp() {
        termFactory = new TermFactory();
    }

    private List<Literal> parse(String input) throws LexerException, ParseException {
        SMTLIBLexer lexer = new SMTLIBLexer(input);
        List<SMTLIBToken> tokens = lexer.tokenize();
        SMTLIBParser parser = new SMTLIBParser(tokens, termFactory);
        return parser.parse();
    }

    @Test
    public void testEmptyInput() throws LexerException, ParseException {
        List<Literal> literals = parse("");
        assertEquals(0, literals.size());
    }

    @Test
    public void testSetLogicOnly() throws LexerException, ParseException {
        List<Literal> literals = parse("(set-logic QF_UF)");
        assertEquals(0, literals.size());
    }

    @Test
    public void testCheckSatOnly() throws LexerException, ParseException {
        List<Literal> literals = parse("(check-sat)");
        assertEquals(0, literals.size());
    }

    @Test
    public void testSimpleEquality() throws LexerException, ParseException {
        List<Literal> literals = parse("(assert (= x y))");

        assertEquals(1, literals.size());
        assertTrue(literals.get(0).isEquality());
    }

    @Test
    public void testSimpleDisequality() throws LexerException, ParseException {
        List<Literal> literals = parse("(assert (not (= x y)))");

        assertEquals(1, literals.size());
        assertTrue(literals.get(0).isDisequality());
    }

    @Test
    public void testFunctionEquality() throws LexerException, ParseException {
        List<Literal> literals = parse("(assert (= (f x) (g y)))");

        assertEquals(1, literals.size());
        assertTrue(literals.get(0).isEquality());
    }

    @Test
    public void testAndConjunction() throws LexerException, ParseException {
        List<Literal> literals = parse("(assert (and (= a b) (= b c)))");

        assertEquals(2, literals.size());
        assertTrue(literals.get(0).isEquality());
        assertTrue(literals.get(1).isEquality());
    }

    @Test
    public void testNestedAnd() throws LexerException, ParseException {
        List<Literal> literals = parse("(assert (and (= a b) (and (= b c) (= c d))))");

        assertEquals(3, literals.size());
        assertTrue(literals.get(0).isEquality());
        assertTrue(literals.get(1).isEquality());
        assertTrue(literals.get(2).isEquality());
    }

    @Test
    public void testMultipleAsserts() throws LexerException, ParseException {
        List<Literal> literals = parse(
            "(set-logic QF_UF)\n" +
            "(assert (= a b))\n" +
            "(assert (= b c))\n" +
            "(check-sat)"
        );

        assertEquals(2, literals.size());
        assertTrue(literals.get(0).isEquality());
        assertTrue(literals.get(1).isEquality());
    }

    @Test
    public void testDeclareFun() throws LexerException, ParseException {
        List<Literal> literals = parse(
            "(declare-fun f (Int Int) Int)\n" +
            "(assert (= (f a b) c))"
        );

        assertEquals(1, literals.size());
        assertTrue(literals.get(0).isEquality());
    }

    @Test
    public void testDeclareConst() throws LexerException, ParseException {
        List<Literal> literals = parse(
            "(declare-const x Int)\n" +
            "(assert (= x y))"
        );

        assertEquals(1, literals.size());
        assertTrue(literals.get(0).isEquality());
    }

    @Test
    public void testListOperations() throws LexerException, ParseException {
        List<Literal> literals = parse(
            "(assert (= (cons x y) l))"
        );

        assertEquals(1, literals.size());
        assertTrue(literals.get(0).isEquality());
    }

    @Test
    public void testArrayOperations() throws LexerException, ParseException {
        List<Literal> literals = parse(
            "(assert (= (select (store a i v) i) v))"
        );

        assertEquals(1, literals.size());
        assertTrue(literals.get(0).isEquality());
    }

    @Test
    public void testDisjunctionNotSupported() {
        assertThrows(ParseException.class, () -> {
            parse("(assert (or (= a b) (= c d)))");
        });
    }

    @Test
    public void testComplexFormula() throws LexerException, ParseException {
        List<Literal> literals = parse(
            "(set-logic QF_UF)\n" +
            "(declare-fun f (Int) Int)\n" +
            "(assert (and (= (f a) (f b)) (= a b)))\n" +
            "(check-sat)"
        );

        assertEquals(2, literals.size());
    }

    @Test
    public void testInvalidSyntax() {
        assertThrows(ParseException.class, () -> {
            parse("(assert = x y)");
        });
    }
}
