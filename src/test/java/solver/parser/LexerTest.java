package solver.parser;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.util.List;

/**
 * Unit tests for the Lexer class.
 * Tests tokenization of the custom input format.
 */
public class LexerTest {

    @Test
    public void testEmptyInput() throws LexerException {
        Lexer lexer = new Lexer("");
        List<Token> tokens = lexer.tokenize();

        assertEquals(1, tokens.size());
        assertEquals(Token.TokenType.EOF, tokens.get(0).getType());
    }

    @Test
    public void testSingleIdentifier() throws LexerException {
        Lexer lexer = new Lexer("abc");
        List<Token> tokens = lexer.tokenize();

        assertEquals(2, tokens.size());
        assertEquals(Token.TokenType.IDENTIFIER, tokens.get(0).getType());
        assertEquals("abc", tokens.get(0).getValue());
        assertEquals(Token.TokenType.EOF, tokens.get(1).getType());
    }

    @Test
    public void testIdentifierWithUnderscore() throws LexerException {
        Lexer lexer = new Lexer("my_var");
        List<Token> tokens = lexer.tokenize();

        assertEquals(2, tokens.size());
        assertEquals(Token.TokenType.IDENTIFIER, tokens.get(0).getType());
        assertEquals("my_var", tokens.get(0).getValue());
    }

    @Test
    public void testIdentifierWithNumbers() throws LexerException {
        Lexer lexer = new Lexer("var123");
        List<Token> tokens = lexer.tokenize();

        assertEquals(2, tokens.size());
        assertEquals(Token.TokenType.IDENTIFIER, tokens.get(0).getType());
        assertEquals("var123", tokens.get(0).getValue());
    }

    @Test
    public void testEqualsOperator() throws LexerException {
        Lexer lexer = new Lexer("=");
        List<Token> tokens = lexer.tokenize();

        assertEquals(2, tokens.size());
        assertEquals(Token.TokenType.EQUALS, tokens.get(0).getType());
        assertEquals("=", tokens.get(0).getValue());
    }

    @Test
    public void testNotEqualsOperator() throws LexerException {
        Lexer lexer = new Lexer("!=");
        List<Token> tokens = lexer.tokenize();

        assertEquals(2, tokens.size());
        assertEquals(Token.TokenType.NOT_EQUALS, tokens.get(0).getType());
        assertEquals("!=", tokens.get(0).getValue());
    }

    @Test
    public void testNotOperator() throws LexerException {
        Lexer lexer = new Lexer("!");
        List<Token> tokens = lexer.tokenize();

        assertEquals(2, tokens.size());
        assertEquals(Token.TokenType.NOT, tokens.get(0).getType());
        assertEquals("!", tokens.get(0).getValue());
    }

    @Test
    public void testUnicodeNotOperator() throws LexerException {
        Lexer lexer = new Lexer("¬");
        List<Token> tokens = lexer.tokenize();

        assertEquals(2, tokens.size());
        assertEquals(Token.TokenType.NOT, tokens.get(0).getType());
        assertEquals("¬", tokens.get(0).getValue());
    }

    @Test
    public void testParentheses() throws LexerException {
        Lexer lexer = new Lexer("()");
        List<Token> tokens = lexer.tokenize();

        assertEquals(3, tokens.size());
        assertEquals(Token.TokenType.LPAREN, tokens.get(0).getType());
        assertEquals(Token.TokenType.RPAREN, tokens.get(1).getType());
    }

    @Test
    public void testComma() throws LexerException {
        Lexer lexer = new Lexer(",");
        List<Token> tokens = lexer.tokenize();

        assertEquals(2, tokens.size());
        assertEquals(Token.TokenType.COMMA, tokens.get(0).getType());
        assertEquals(",", tokens.get(0).getValue());
    }

    @Test
    public void testAtomKeyword() throws LexerException {
        Lexer lexer = new Lexer("atom");
        List<Token> tokens = lexer.tokenize();

        assertEquals(2, tokens.size());
        assertEquals(Token.TokenType.ATOM, tokens.get(0).getType());
        assertEquals("atom", tokens.get(0).getValue());
    }

    @Test
    public void testNewline() throws LexerException {
        Lexer lexer = new Lexer("\n");
        List<Token> tokens = lexer.tokenize();

        assertEquals(2, tokens.size());
        assertEquals(Token.TokenType.NEWLINE, tokens.get(0).getType());
        assertEquals(Token.TokenType.EOF, tokens.get(1).getType());
    }

    @Test
    public void testSimpleEquality() throws LexerException {
        Lexer lexer = new Lexer("a = b");
        List<Token> tokens = lexer.tokenize();

        assertEquals(4, tokens.size());
        assertEquals(Token.TokenType.IDENTIFIER, tokens.get(0).getType());
        assertEquals("a", tokens.get(0).getValue());
        assertEquals(Token.TokenType.EQUALS, tokens.get(1).getType());
        assertEquals(Token.TokenType.IDENTIFIER, tokens.get(2).getType());
        assertEquals("b", tokens.get(2).getValue());
        assertEquals(Token.TokenType.EOF, tokens.get(3).getType());
    }

    @Test
    public void testSimpleDisequality() throws LexerException {
        Lexer lexer = new Lexer("a != b");
        List<Token> tokens = lexer.tokenize();

        assertEquals(4, tokens.size());
        assertEquals(Token.TokenType.IDENTIFIER, tokens.get(0).getType());
        assertEquals(Token.TokenType.NOT_EQUALS, tokens.get(1).getType());
        assertEquals(Token.TokenType.IDENTIFIER, tokens.get(2).getType());
        assertEquals(Token.TokenType.EOF, tokens.get(3).getType());
    }

    @Test
    public void testFunctionApplication() throws LexerException {
        Lexer lexer = new Lexer("f(x, y)");
        List<Token> tokens = lexer.tokenize();

        assertEquals(7, tokens.size());
        assertEquals(Token.TokenType.IDENTIFIER, tokens.get(0).getType());
        assertEquals("f", tokens.get(0).getValue());
        assertEquals(Token.TokenType.LPAREN, tokens.get(1).getType());
        assertEquals(Token.TokenType.IDENTIFIER, tokens.get(2).getType());
        assertEquals("x", tokens.get(2).getValue());
        assertEquals(Token.TokenType.COMMA, tokens.get(3).getType());
        assertEquals(Token.TokenType.IDENTIFIER, tokens.get(4).getType());
        assertEquals("y", tokens.get(4).getValue());
        assertEquals(Token.TokenType.RPAREN, tokens.get(5).getType());
        assertEquals(Token.TokenType.EOF, tokens.get(6).getType());
    }

    @Test
    public void testAtomPredicate() throws LexerException {
        Lexer lexer = new Lexer("atom(x)");
        List<Token> tokens = lexer.tokenize();

        assertEquals(5, tokens.size());
        assertEquals(Token.TokenType.ATOM, tokens.get(0).getType());
        assertEquals(Token.TokenType.LPAREN, tokens.get(1).getType());
        assertEquals(Token.TokenType.IDENTIFIER, tokens.get(2).getType());
        assertEquals("x", tokens.get(2).getValue());
        assertEquals(Token.TokenType.RPAREN, tokens.get(3).getType());
        assertEquals(Token.TokenType.EOF, tokens.get(4).getType());
    }

    @Test
    public void testNegatedAtomPredicate() throws LexerException {
        Lexer lexer = new Lexer("!atom(cons(a,b))");
        List<Token> tokens = lexer.tokenize();

        assertEquals(11, tokens.size());
        assertEquals(Token.TokenType.NOT, tokens.get(0).getType());
        assertEquals(Token.TokenType.ATOM, tokens.get(1).getType());
        assertEquals(Token.TokenType.LPAREN, tokens.get(2).getType());
        assertEquals(Token.TokenType.IDENTIFIER, tokens.get(3).getType());
        assertEquals("cons", tokens.get(3).getValue());
    }

    @Test
    public void testMultipleLiterals() throws LexerException {
        Lexer lexer = new Lexer("a = b\nc != d");
        List<Token> tokens = lexer.tokenize();

        // a = b NEWLINE c != d EOF
        assertEquals(8, tokens.size());
        assertEquals(Token.TokenType.IDENTIFIER, tokens.get(0).getType());
        assertEquals(Token.TokenType.EQUALS, tokens.get(1).getType());
        assertEquals(Token.TokenType.IDENTIFIER, tokens.get(2).getType());
        assertEquals(Token.TokenType.NEWLINE, tokens.get(3).getType());
        assertEquals(Token.TokenType.IDENTIFIER, tokens.get(4).getType());
        assertEquals(Token.TokenType.NOT_EQUALS, tokens.get(5).getType());
        assertEquals(Token.TokenType.IDENTIFIER, tokens.get(6).getType());
        assertEquals(Token.TokenType.EOF, tokens.get(7).getType());
    }

    @Test
    public void testHashComment() throws LexerException {
        Lexer lexer = new Lexer("# This is a comment\na = b");
        List<Token> tokens = lexer.tokenize();

        // Comment is skipped, newline is kept
        assertEquals(5, tokens.size());
        assertEquals(Token.TokenType.NEWLINE, tokens.get(0).getType());
        assertEquals(Token.TokenType.IDENTIFIER, tokens.get(1).getType());
        assertEquals("a", tokens.get(1).getValue());
    }

    @Test
    public void testSlashSlashComment() throws LexerException {
        Lexer lexer = new Lexer("// This is a comment\na = b");
        List<Token> tokens = lexer.tokenize();

        // Comment is skipped, newline is kept
        assertEquals(5, tokens.size());
        assertEquals(Token.TokenType.NEWLINE, tokens.get(0).getType());
        assertEquals(Token.TokenType.IDENTIFIER, tokens.get(1).getType());
        assertEquals("a", tokens.get(1).getValue());
    }

    @Test
    public void testWhitespaceHandling() throws LexerException {
        Lexer lexer = new Lexer("  a   =   b  ");
        List<Token> tokens = lexer.tokenize();

        assertEquals(4, tokens.size());
        assertEquals(Token.TokenType.IDENTIFIER, tokens.get(0).getType());
        assertEquals("a", tokens.get(0).getValue());
        assertEquals(Token.TokenType.EQUALS, tokens.get(1).getType());
        assertEquals(Token.TokenType.IDENTIFIER, tokens.get(2).getType());
        assertEquals("b", tokens.get(2).getValue());
    }

    @Test
    public void testLineAndColumnTracking() throws LexerException {
        Lexer lexer = new Lexer("a = b\nc != d");
        List<Token> tokens = lexer.tokenize();

        // First token on line 1, column 1
        assertEquals(1, tokens.get(0).getLine());
        assertEquals(1, tokens.get(0).getColumn());

        // Newline at line 1 (where it appears)
        assertEquals(1, tokens.get(3).getLine());

        // First token on second line
        assertEquals(2, tokens.get(4).getLine());
        assertEquals(1, tokens.get(4).getColumn());
    }

    @Test
    public void testInvalidCharacter() {
        Lexer lexer = new Lexer("a @ b");

        LexerException exception = assertThrows(LexerException.class, () -> {
            lexer.tokenize();
        });

        assertTrue(exception.getMessage().contains("Unexpected character"));
        assertTrue(exception.getMessage().contains("@"));
    }

    @Test
    public void testComplexExpression() throws LexerException {
        Lexer lexer = new Lexer("select(store(a, i, v), i) = v");
        List<Token> tokens = lexer.tokenize();

        // select ( store ( a , i , v ) , i ) = v EOF
        assertEquals(16, tokens.size());
        assertEquals(Token.TokenType.IDENTIFIER, tokens.get(0).getType());
        assertEquals("select", tokens.get(0).getValue());
        assertEquals(Token.TokenType.LPAREN, tokens.get(1).getType());
        assertEquals(Token.TokenType.IDENTIFIER, tokens.get(2).getType());
        assertEquals("store", tokens.get(2).getValue());
    }

    @Test
    public void testEmptyParentheses() throws LexerException {
        Lexer lexer = new Lexer("nil()");
        List<Token> tokens = lexer.tokenize();

        assertEquals(4, tokens.size());
        assertEquals(Token.TokenType.IDENTIFIER, tokens.get(0).getType());
        assertEquals("nil", tokens.get(0).getValue());
        assertEquals(Token.TokenType.LPAREN, tokens.get(1).getType());
        assertEquals(Token.TokenType.RPAREN, tokens.get(2).getType());
        assertEquals(Token.TokenType.EOF, tokens.get(3).getType());
    }

    @Test
    public void testTabsAndCarriageReturns() throws LexerException {
        Lexer lexer = new Lexer("\ta\t=\tb\r\n");
        List<Token> tokens = lexer.tokenize();

        assertEquals(5, tokens.size());
        assertEquals(Token.TokenType.IDENTIFIER, tokens.get(0).getType());
        assertEquals("a", tokens.get(0).getValue());
        assertEquals(Token.TokenType.EQUALS, tokens.get(1).getType());
        assertEquals(Token.TokenType.IDENTIFIER, tokens.get(2).getType());
        assertEquals("b", tokens.get(2).getValue());
        assertEquals(Token.TokenType.NEWLINE, tokens.get(3).getType());
    }

    @Test
    public void testNestedFunctions() throws LexerException {
        Lexer lexer = new Lexer("f(g(h(x)))");
        List<Token> tokens = lexer.tokenize();

        // f ( g ( h ( x ) ) ) EOF
        assertEquals(11, tokens.size());
        assertEquals(Token.TokenType.IDENTIFIER, tokens.get(0).getType());
        assertEquals("f", tokens.get(0).getValue());
        assertEquals(Token.TokenType.LPAREN, tokens.get(1).getType());
        assertEquals(Token.TokenType.IDENTIFIER, tokens.get(2).getType());
        assertEquals("g", tokens.get(2).getValue());
    }

    @Test
    public void testListOperations() throws LexerException {
        Lexer lexer = new Lexer("car(cons(x, y)) = x");
        List<Token> tokens = lexer.tokenize();

        assertEquals(12, tokens.size());
        assertEquals(Token.TokenType.IDENTIFIER, tokens.get(0).getType());
        assertEquals("car", tokens.get(0).getValue());
        assertEquals(Token.TokenType.IDENTIFIER, tokens.get(2).getType());
        assertEquals("cons", tokens.get(2).getValue());
    }
}
