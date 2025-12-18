package solver.parser;

/**
 * Common interface for all token types.
 *
 * Defines the common contract that all tokens must implement:
 * - Token type identification
 * - Lexeme value
 * - Source location (line and column)
 *
 * Phase 5.4: Code Refactoring - Unified token interface.
 */
public interface IToken {
    /**
     * Returns the type of this token.
     *
     * @return The token type (specific to the lexer implementation)
     */
    Object getType();

    /**
     * Returns the lexeme (string value) of this token.
     *
     * @return The token's string value
     */
    String getValue();

    /**
     * Returns the line number where this token appears in the source.
     *
     * @return Line number (1-indexed)
     */
    int getLine();

    /**
     * Returns the column number where this token starts in the source.
     *
     * @return Column number (1-indexed)
     */
    int getColumn();
}
