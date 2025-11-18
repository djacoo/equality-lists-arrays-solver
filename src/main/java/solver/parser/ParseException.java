package solver.parser;

/**
 * Exception thrown when the parser encounters invalid input.
 */
public class ParseException extends Exception {
    public ParseException(String message) {
        super(message);
    }

    public ParseException(String message, Throwable cause) {
        super(message, cause);
    }
}
