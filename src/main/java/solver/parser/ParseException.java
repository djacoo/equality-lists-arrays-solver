package solver.parser;

/**
 * Exception thrown when the parser encounters invalid input.
 * Phase 5.4: Refactored to use common exception hierarchy.
 */
public class ParseException extends ParserException {
    public ParseException(String message) {
        super(message);
    }

    public ParseException(String message, Throwable cause) {
        super(message, cause);
    }
}
