package solver.parser;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Lexical analyzer for SMT-LIB format.
 *
 * SMT-LIB uses S-expression syntax. This lexer tokenizes SMT-LIB input
 * focusing on the QF_UF (Quantifier-Free Uninterpreted Functions) logic.
 *
 * Reference: SMT-LIB Standard Version 2.0
 * http://smtlib.cs.uiowa.edu/papers/smt-lib-reference-v2.0-r10.12.21.pdf
 *
 * Phase 5.4: Refactored to use BaseLexer for common functionality.
 */
public class SMTLIBLexer extends BaseLexer {

    // Keywords map
    private static final Map<String, SMTLIBToken.TokenType> KEYWORDS = new HashMap<>();
    static {
        KEYWORDS.put("set-logic", SMTLIBToken.TokenType.SET_LOGIC);
        KEYWORDS.put("declare-fun", SMTLIBToken.TokenType.DECLARE_FUN);
        KEYWORDS.put("declare-const", SMTLIBToken.TokenType.DECLARE_CONST);
        KEYWORDS.put("assert", SMTLIBToken.TokenType.ASSERT);
        KEYWORDS.put("check-sat", SMTLIBToken.TokenType.CHECK_SAT);
        KEYWORDS.put("get-model", SMTLIBToken.TokenType.GET_MODEL);
        KEYWORDS.put("exit", SMTLIBToken.TokenType.EXIT);
    }

    public SMTLIBLexer(String input) {
        super(input);
    }

    /**
     * Tokenizes the entire input string.
     *
     * @return List of tokens
     * @throws LexerException if the input contains invalid characters
     */
    public List<SMTLIBToken> tokenize() throws LexerException {
        List<SMTLIBToken> tokens = new ArrayList<>();

        while (!isAtEnd()) {
            skipWhitespace();
            if (isAtEnd()) break;

            SMTLIBToken token = nextToken();
            if (token != null) {
                tokens.add(token);
            }
        }

        // Add EOF token
        tokens.add(new SMTLIBToken(SMTLIBToken.TokenType.EOF, "", line, column));
        return tokens;
    }

    /**
     * Reads and returns the next token.
     */
    private SMTLIBToken nextToken() throws LexerException {
        char c = peek();

        // Handle comments (semicolon to end of line)
        if (c == ';') {
            skipComment();
            return null;
        }

        // Handle parentheses
        if (c == '(') {
            return consumeSingleChar(SMTLIBToken.TokenType.LPAREN);
        }
        if (c == ')') {
            return consumeSingleChar(SMTLIBToken.TokenType.RPAREN);
        }

        // Handle string literals
        if (c == '"') {
            return consumeString();
        }

        // Handle numerals
        if (Character.isDigit(c)) {
            return consumeNumeral();
        }

        // Handle symbols (identifiers and keywords)
        if (isSymbolStart(c)) {
            return consumeSymbol();
        }

        throw new LexerException(
            String.format("Unexpected character '%c' at line %d, column %d", c, line, column)
        );
    }

    /**
     * Consumes a symbol (identifier or keyword).
     *
     * SMT-LIB symbols can contain letters, digits, and special characters like:
     * ~!@$%^&*_-+=<>.?/
     */
    private SMTLIBToken consumeSymbol() {
        int startLine = line;
        int startColumn = column;
        StringBuilder sb = new StringBuilder();

        while (!isAtEnd() && isSymbolChar(peek())) {
            sb.append(advance());
        }

        String value = sb.toString();

        // Check if it's a keyword
        SMTLIBToken.TokenType type = KEYWORDS.getOrDefault(value, SMTLIBToken.TokenType.SYMBOL);

        return new SMTLIBToken(type, value, startLine, startColumn);
    }

    /**
     * Consumes a numeral (non-negative integer).
     */
    private SMTLIBToken consumeNumeral() {
        int startLine = line;
        int startColumn = column;
        StringBuilder sb = new StringBuilder();

        while (!isAtEnd() && Character.isDigit(peek())) {
            sb.append(advance());
        }

        return new SMTLIBToken(SMTLIBToken.TokenType.NUMERAL, sb.toString(), startLine, startColumn);
    }

    /**
     * Consumes a string literal.
     */
    private SMTLIBToken consumeString() throws LexerException {
        int startLine = line;
        int startColumn = column;
        StringBuilder sb = new StringBuilder();

        advance();  // Consume opening quote

        while (!isAtEnd() && peek() != '"') {
            // Handle escaped quotes
            if (peek() == '\\' && peekNext() == '"') {
                advance();  // Skip backslash
                sb.append(advance());  // Add quote
            } else {
                sb.append(advance());
            }
        }

        if (isAtEnd()) {
            throw new LexerException(
                String.format("Unterminated string at line %d, column %d", startLine, startColumn)
            );
        }

        advance();  // Consume closing quote

        return new SMTLIBToken(SMTLIBToken.TokenType.STRING, sb.toString(), startLine, startColumn);
    }

    /**
     * Consumes a single-character token.
     */
    private SMTLIBToken consumeSingleChar(SMTLIBToken.TokenType type) {
        int startLine = line;
        int startColumn = column;
        char c = advance();
        return new SMTLIBToken(type, String.valueOf(c), startLine, startColumn);
    }

    /**
     * Skips whitespace and newlines.
     */
    private void skipWhitespace() {
        while (!isAtEnd()) {
            char c = peek();
            if (c == ' ' || c == '\t' || c == '\r') {
                advance();
            } else if (c == '\n') {
                advance();
                advanceLine();
            } else {
                break;
            }
        }
    }

    /**
     * Skips a comment (from semicolon to end of line).
     */
    private void skipComment() {
        advance();  // Skip semicolon
        while (!isAtEnd() && peek() != '\n') {
            advance();
        }
    }

    /**
     * Returns true if the character can start a symbol.
     *
     * SMT-LIB symbols can start with: letters, ~!@$%^&*_-+=<>.?/
     */
    private boolean isSymbolStart(char c) {
        return Character.isLetter(c) ||
               c == '~' || c == '!' || c == '@' || c == '$' || c == '%' ||
               c == '^' || c == '&' || c == '*' || c == '_' || c == '-' ||
               c == '+' || c == '=' || c == '<' || c == '>' || c == '.' ||
               c == '?' || c == '/';
    }

    /**
     * Returns true if the character can be part of a symbol.
     */
    private boolean isSymbolChar(char c) {
        return Character.isLetterOrDigit(c) ||
               c == '~' || c == '!' || c == '@' || c == '$' || c == '%' ||
               c == '^' || c == '&' || c == '*' || c == '_' || c == '-' ||
               c == '+' || c == '=' || c == '<' || c == '>' || c == '.' ||
               c == '?' || c == '/';
    }
}
