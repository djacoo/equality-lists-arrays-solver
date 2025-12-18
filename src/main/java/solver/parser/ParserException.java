package solver.parser;

/**
 * Base exception for all parser-related errors.
 * Phase 5.4: Code Refactoring - Exception Hierarchy
 */
public class ParserException extends Exception {
    public ParserException(String message) {
        super(message);
    }

    public ParserException(String message, Throwable cause) {
        super(message, cause);
    }
}
