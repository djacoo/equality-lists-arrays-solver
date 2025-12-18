package solver.parser;

/**
 * Represents a token in SMT-LIB format.
 *
 * SMT-LIB uses S-expression syntax with commands like:
 * (set-logic QF_UF)
 * (declare-fun f (Int Int) Int)
 * (assert (= (f a b) a))
 * (check-sat)
 *
 * Phase 5.4: Refactored to implement IToken interface.
 */
public class SMTLIBToken implements IToken {
    private final TokenType type;
    private final String value;
    private final int line;
    private final int column;

    public enum TokenType {
        // Delimiters
        LPAREN,         // (
        RPAREN,         // )

        // Literals
        SYMBOL,         // Identifier: a, x, Int, Bool, f, etc.
        STRING,         // String literal: "hello world"
        NUMERAL,        // Numeric literal: 0, 42, 123

        // Keywords (commands)
        SET_LOGIC,      // set-logic
        DECLARE_FUN,    // declare-fun
        DECLARE_CONST,  // declare-const
        ASSERT,         // assert
        CHECK_SAT,      // check-sat
        GET_MODEL,      // get-model
        EXIT,           // exit

        // Special
        EOF             // End of file
    }

    public SMTLIBToken(TokenType type, String value, int line, int column) {
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
        return String.format("SMTLIBToken(%s, '%s', %d:%d)", type, value, line, column);
    }
}
