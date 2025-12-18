package solver.parser;

/**
 * Exception thrown when the lexer encounters invalid input.
 * Phase 5.4: Refactored to use common exception hierarchy.
 */
public class LexerException extends ParserException {
    public LexerException(String message) {
        super(message);
    }

    public LexerException(String message, Throwable cause) {
        super(message, cause);
    }
}
