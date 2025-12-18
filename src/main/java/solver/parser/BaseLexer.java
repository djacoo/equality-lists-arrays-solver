package solver.parser;

/**
 * Abstract base class for lexical analyzers.
 *
 * Provides common functionality for tokenization including:
 * - Character stream management (peek, advance, position tracking)
 * - Line and column tracking for error reporting
 * - End-of-input detection
 *
 * Phase 5.4: Code Refactoring - Extract common lexer utilities.
 */
public abstract class BaseLexer {
    protected final String input;
    protected int position;
    protected int line;
    protected int column;

    /**
     * Constructs a lexer for the given input string.
     *
     * @param input The input string to tokenize
     */
    protected BaseLexer(String input) {
        this.input = input;
        this.position = 0;
        this.line = 1;
        this.column = 1;
    }

    /**
     * Returns the current character without consuming it.
     *
     * @return The current character, or '\0' if at end of input
     */
    protected char peek() {
        if (isAtEnd()) return '\0';
        return input.charAt(position);
    }

    /**
     * Returns the next character without consuming it.
     *
     * @return The next character, or '\0' if at end of input
     */
    protected char peekNext() {
        if (position + 1 >= input.length()) return '\0';
        return input.charAt(position + 1);
    }

    /**
     * Consumes and returns the current character.
     * Also updates the column counter.
     *
     * @return The consumed character
     */
    protected char advance() {
        char c = input.charAt(position++);
        column++;
        return c;
    }

    /**
     * Returns true if we've reached the end of input.
     *
     * @return true if at end, false otherwise
     */
    protected boolean isAtEnd() {
        return position >= input.length();
    }

    /**
     * Increments the line counter and resets the column counter.
     * Should be called when a newline character is consumed.
     */
    protected void advanceLine() {
        line++;
        column = 1;
    }
}
