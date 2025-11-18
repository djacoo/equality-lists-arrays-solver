package solver.parser;

import java.util.ArrayList;
import java.util.List;

/**
 * Lexical analyzer (tokenizer) for the input language.
 *
 * Converts raw input text into a stream of tokens that can be parsed.
 *
 * BRADLEY & MANNA: The input consists of literals in the theories T_E, T_cons, and T_A.
 * We support:
 * - Equalities: a = b, f(x) = g(y)
 * - Disequalities: a != b
 * - Atom predicates: atom(x), !atom(cons(a,b))
 * - Function applications: cons(a,b), car(l), select(arr,i), store(arr,i,v)
 */
public class Lexer {
    private final String input;
    private int position;
    private int line;
    private int column;

    public Lexer(String input) {
        this.input = input;
        this.position = 0;
        this.line = 1;
        this.column = 1;
    }

    /**
     * Tokenizes the entire input string.
     *
     * @return List of tokens
     * @throws LexerException if the input contains invalid characters
     */
    public List<Token> tokenize() throws LexerException {
        List<Token> tokens = new ArrayList<>();

        while (!isAtEnd()) {
            skipWhitespaceExceptNewline();
            if (isAtEnd()) break;

            Token token = nextToken();
            if (token != null) {
                tokens.add(token);
            }
        }

        // Add EOF token
        tokens.add(new Token(Token.TokenType.EOF, "", line, column));
        return tokens;
    }

    /**
     * Reads and returns the next token.
     */
    private Token nextToken() throws LexerException {
        char c = peek();

        // Handle newlines (literal separators)
        if (c == '\n') {
            return consumeNewline();
        }

        // Handle comments
        if (c == '#' || (c == '/' && peekNext() == '/')) {
            skipComment();
            return null;
        }

        // Handle operators and delimiters
        switch (c) {
            case '(':
                return consumeSingleChar(Token.TokenType.LPAREN);
            case ')':
                return consumeSingleChar(Token.TokenType.RPAREN);
            case ',':
                return consumeSingleChar(Token.TokenType.COMMA);
            case '!':
                if (peekNext() == '=') {
                    return consumeTwoChars(Token.TokenType.NOT_EQUALS);
                } else {
                    return consumeSingleChar(Token.TokenType.NOT);
                }
            case '=':
                return consumeSingleChar(Token.TokenType.EQUALS);
            case '¬':  // Unicode negation symbol
                return consumeSingleChar(Token.TokenType.NOT);
            default:
                if (isIdentifierStart(c)) {
                    return consumeIdentifier();
                } else {
                    throw new LexerException(
                        String.format("Unexpected character '%c' at line %d, column %d", c, line, column)
                    );
                }
        }
    }

    /**
     * Consumes an identifier or keyword.
     */
    private Token consumeIdentifier() {
        int startLine = line;
        int startColumn = column;
        StringBuilder sb = new StringBuilder();

        while (!isAtEnd() && isIdentifierChar(peek())) {
            sb.append(advance());
        }

        String value = sb.toString();
        Token.TokenType type;

        // Check for keywords
        if (value.equals("atom")) {
            type = Token.TokenType.ATOM;
        } else {
            type = Token.TokenType.IDENTIFIER;
        }

        return new Token(type, value, startLine, startColumn);
    }

    /**
     * Consumes a single-character token.
     */
    private Token consumeSingleChar(Token.TokenType type) {
        int startLine = line;
        int startColumn = column;
        char c = advance();
        return new Token(type, String.valueOf(c), startLine, startColumn);
    }

    /**
     * Consumes a two-character token.
     */
    private Token consumeTwoChars(Token.TokenType type) {
        int startLine = line;
        int startColumn = column;
        char c1 = advance();
        char c2 = advance();
        return new Token(type, String.valueOf(c1) + c2, startLine, startColumn);
    }

    /**
     * Consumes a newline character.
     */
    private Token consumeNewline() {
        int startLine = line;
        int startColumn = column;
        advance();  // Consume '\n'
        line++;
        column = 1;
        return new Token(Token.TokenType.NEWLINE, "\n", startLine, startColumn);
    }

    /**
     * Skips whitespace characters except newlines.
     */
    private void skipWhitespaceExceptNewline() {
        while (!isAtEnd()) {
            char c = peek();
            if (c == ' ' || c == '\t' || c == '\r') {
                advance();
            } else {
                break;
            }
        }
    }

    /**
     * Skips a comment (from # or // to end of line).
     */
    private void skipComment() {
        // Skip # or //
        if (peek() == '#') {
            advance();
        } else if (peek() == '/' && peekNext() == '/') {
            advance();
            advance();
        }

        // Skip until newline or EOF
        while (!isAtEnd() && peek() != '\n') {
            advance();
        }
    }

    /**
     * Returns the current character without consuming it.
     */
    private char peek() {
        if (isAtEnd()) return '\0';
        return input.charAt(position);
    }

    /**
     * Returns the next character without consuming it.
     */
    private char peekNext() {
        if (position + 1 >= input.length()) return '\0';
        return input.charAt(position + 1);
    }

    /**
     * Consumes and returns the current character.
     */
    private char advance() {
        char c = input.charAt(position++);
        column++;
        return c;
    }

    /**
     * Returns true if we've reached the end of input.
     */
    private boolean isAtEnd() {
        return position >= input.length();
    }

    /**
     * Returns true if the character can start an identifier.
     */
    private boolean isIdentifierStart(char c) {
        return Character.isLetter(c) || c == '_';
    }

    /**
     * Returns true if the character can be part of an identifier.
     */
    private boolean isIdentifierChar(char c) {
        return Character.isLetterOrDigit(c) || c == '_';
    }
}
