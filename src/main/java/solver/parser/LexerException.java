package solver.parser;

/**
 * Exception thrown when the lexer encounters invalid input.
 */
public class LexerException extends Exception {
    public LexerException(String message) {
        super(message);
    }

    public LexerException(String message, Throwable cause) {
        super(message, cause);
    }
}
