package solver.parser;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.util.List;

/**
 * Unit tests for the SMT-LIBLexer class.
 * Tests tokenization of SMT-LIB format.
 */
public class SMTLIBLexerTest {

    @Test
    public void testEmptyInput() throws LexerException {
        SMTLIBLexer lexer = new SMTLIBLexer("");
        List<SMTLIBToken> tokens = lexer.tokenize();

        assertEquals(1, tokens.size());
        assertEquals(SMTLIBToken.TokenType.EOF, tokens.get(0).getType());
    }

    @Test
    public void testSingleSymbol() throws LexerException {
        SMTLIBLexer lexer = new SMTLIBLexer("x");
        List<SMTLIBToken> tokens = lexer.tokenize();

        assertEquals(2, tokens.size());
        assertEquals(SMTLIBToken.TokenType.SYMBOL, tokens.get(0).getType());
        assertEquals("x", tokens.get(0).getValue());
    }

    @Test
    public void testKeywords() throws LexerException {
        SMTLIBLexer lexer = new SMTLIBLexer("set-logic declare-fun assert check-sat");
        List<SMTLIBToken> tokens = lexer.tokenize();

        assertEquals(5, tokens.size());
        assertEquals(SMTLIBToken.TokenType.SET_LOGIC, tokens.get(0).getType());
        assertEquals(SMTLIBToken.TokenType.DECLARE_FUN, tokens.get(1).getType());
        assertEquals(SMTLIBToken.TokenType.ASSERT, tokens.get(2).getType());
        assertEquals(SMTLIBToken.TokenType.CHECK_SAT, tokens.get(3).getType());
    }

    @Test
    public void testParentheses() throws LexerException {
        SMTLIBLexer lexer = new SMTLIBLexer("( )");
        List<SMTLIBToken> tokens = lexer.tokenize();

        assertEquals(3, tokens.size());
        assertEquals(SMTLIBToken.TokenType.LPAREN, tokens.get(0).getType());
        assertEquals(SMTLIBToken.TokenType.RPAREN, tokens.get(1).getType());
    }

    @Test
    public void testString() throws LexerException {
        SMTLIBLexer lexer = new SMTLIBLexer("\"hello world\"");
        List<SMTLIBToken> tokens = lexer.tokenize();

        assertEquals(2, tokens.size());
        assertEquals(SMTLIBToken.TokenType.STRING, tokens.get(0).getType());
        assertEquals("hello world", tokens.get(0).getValue());
    }

    @Test
    public void testNumeral() throws LexerException {
        SMTLIBLexer lexer = new SMTLIBLexer("123");
        List<SMTLIBToken> tokens = lexer.tokenize();

        assertEquals(2, tokens.size());
        assertEquals(SMTLIBToken.TokenType.NUMERAL, tokens.get(0).getType());
        assertEquals("123", tokens.get(0).getValue());
    }

    @Test
    public void testComment() throws LexerException {
        SMTLIBLexer lexer = new SMTLIBLexer("; This is a comment\nx");
        List<SMTLIBToken> tokens = lexer.tokenize();

        assertEquals(2, tokens.size());
        assertEquals(SMTLIBToken.TokenType.SYMBOL, tokens.get(0).getType());
        assertEquals("x", tokens.get(0).getValue());
    }

    @Test
    public void testSetLogicCommand() throws LexerException {
        SMTLIBLexer lexer = new SMTLIBLexer("(set-logic QF_UF)");
        List<SMTLIBToken> tokens = lexer.tokenize();

        assertEquals(5, tokens.size());
        assertEquals(SMTLIBToken.TokenType.LPAREN, tokens.get(0).getType());
        assertEquals(SMTLIBToken.TokenType.SET_LOGIC, tokens.get(1).getType());
        assertEquals(SMTLIBToken.TokenType.SYMBOL, tokens.get(2).getType());
        assertEquals("QF_UF", tokens.get(2).getValue());
        assertEquals(SMTLIBToken.TokenType.RPAREN, tokens.get(3).getType());
    }

    @Test
    public void testEqualityFormula() throws LexerException {
        SMTLIBLexer lexer = new SMTLIBLexer("(= x y)");
        List<SMTLIBToken> tokens = lexer.tokenize();

        assertEquals(6, tokens.size());
        assertEquals(SMTLIBToken.TokenType.LPAREN, tokens.get(0).getType());
        assertEquals(SMTLIBToken.TokenType.SYMBOL, tokens.get(1).getType());
        assertEquals("=", tokens.get(1).getValue());
        assertEquals(SMTLIBToken.TokenType.SYMBOL, tokens.get(2).getType());
        assertEquals("x", tokens.get(2).getValue());
        assertEquals(SMTLIBToken.TokenType.SYMBOL, tokens.get(3).getType());
        assertEquals("y", tokens.get(3).getValue());
    }

    @Test
    public void testFunctionApplication() throws LexerException {
        SMTLIBLexer lexer = new SMTLIBLexer("(f x y)");
        List<SMTLIBToken> tokens = lexer.tokenize();

        assertEquals(6, tokens.size());
        assertEquals(SMTLIBToken.TokenType.LPAREN, tokens.get(0).getType());
        assertEquals(SMTLIBToken.TokenType.SYMBOL, tokens.get(1).getType());
        assertEquals("f", tokens.get(1).getValue());
    }

    @Test
    public void testSpecialSymbolChars() throws LexerException {
        SMTLIBLexer lexer = new SMTLIBLexer("my-var_2 >? <! !=");
        List<SMTLIBToken> tokens = lexer.tokenize();

        assertTrue(tokens.size() > 1);
        assertEquals(SMTLIBToken.TokenType.SYMBOL, tokens.get(0).getType());
        assertEquals("my-var_2", tokens.get(0).getValue());
    }

    @Test
    public void testUnterminatedString() {
        SMTLIBLexer lexer = new SMTLIBLexer("\"unterminated");

        assertThrows(LexerException.class, () -> lexer.tokenize());
    }

    @Test
    public void testWhitespaceHandling() throws LexerException {
        SMTLIBLexer lexer = new SMTLIBLexer("  (  =  x  y  )  ");
        List<SMTLIBToken> tokens = lexer.tokenize();

        assertEquals(6, tokens.size());
        assertEquals(SMTLIBToken.TokenType.LPAREN, tokens.get(0).getType());
        assertEquals(SMTLIBToken.TokenType.SYMBOL, tokens.get(1).getType());
    }

    @Test
    public void testNestedParentheses() throws LexerException {
        SMTLIBLexer lexer = new SMTLIBLexer("(and (= a b) (= c d))");
        List<SMTLIBToken> tokens = lexer.tokenize();

        assertTrue(tokens.size() > 5);
        assertEquals(SMTLIBToken.TokenType.LPAREN, tokens.get(0).getType());
        assertEquals(SMTLIBToken.TokenType.SYMBOL, tokens.get(1).getType());
        assertEquals("and", tokens.get(1).getValue());
    }

    @Test
    public void testMultipleCommands() throws LexerException {
        SMTLIBLexer lexer = new SMTLIBLexer("(set-logic QF_UF)\n(check-sat)");
        List<SMTLIBToken> tokens = lexer.tokenize();

        assertTrue(tokens.size() > 6);
        assertEquals(SMTLIBToken.TokenType.SET_LOGIC, tokens.get(1).getType());
        assertEquals(SMTLIBToken.TokenType.CHECK_SAT, tokens.get(5).getType());
    }
}
