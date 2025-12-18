package solver.parser;

/**
 * Represents a token in the input stream.
 *
 * Tokens are produced by the Lexer and consumed by the Parser.
 *
 * Phase 5.4: Refactored to implement IToken interface.
 */
public class Token implements IToken {
    private final TokenType type;
    private final String value;
    private final int line;
    private final int column;

    public enum TokenType {
        // Literals
        IDENTIFIER,     // Variable or function name: a, x, cons, select

        // Operators
        EQUALS,         // =
        NOT_EQUALS,     // !=

        // Delimiters
        LPAREN,         // (
        RPAREN,         // )
        COMMA,          // ,

        // Keywords
        ATOM,           // atom
        NOT,            // ! or ¬

        // Special
        NEWLINE,        // End of line (literal separator)
        EOF             // End of file
    }

    public Token(TokenType type, String value, int line, int column) {
        this.type = type;
        this.value = value;
        this.line = line;
        this.column = column;
    }

    public TokenType getType() {
        return type;
    }

    public String getValue() {
        return value;
    }

    public int getLine() {
        return line;
    }

    public int getColumn() {
        return column;
    }

    @Override
    public String toString() {
        return String.format("Token(%s, '%s', %d:%d)", type, value, line, column);
    }
}
