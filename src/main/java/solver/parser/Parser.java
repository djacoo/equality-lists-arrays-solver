package solver.parser;

import solver.dag.Term;
import solver.dag.TermFactory;
import solver.theory.te.Literal;

import java.util.ArrayList;
import java.util.List;

/**
 * Parser for the input language.
 *
 * Converts a stream of tokens into Literal objects representing:
 * - Equalities: a = b
 * - Disequalities: a != b
 * - Atom predicates: atom(x), !atom(cons(a,b))
 *
 * BRADLEY & MANNA (Section 9.3-9.5):
 * The solver accepts a set of literals in the combined theory T_E ∪ T_cons ∪ T_A.
 *
 * Grammar:
 * Input    ::= Literal*
 * Literal  ::= Equality | Disequality | Atom | NegAtom
 * Equality ::= Term '=' Term
 * Disequality ::= Term '!=' Term
 * Atom     ::= 'atom' '(' Term ')'
 * NegAtom  ::= '!' 'atom' '(' Term ')'
 * Term     ::= Identifier | Identifier '(' ArgList ')'
 * ArgList  ::= Term (',' Term)*
 */
public class Parser {
    private final List<Token> tokens;
    private final TermFactory termFactory;
    private int position;

    public Parser(List<Token> tokens, TermFactory termFactory) {
        this.tokens = tokens;
        this.termFactory = termFactory;
        this.position = 0;
    }

    /**
     * Parses the token stream and returns a list of literals.
     *
     * @return List of parsed literals
     * @throws ParseException if the input is malformed
     */
    public List<Literal> parse() throws ParseException {
        List<Literal> literals = new ArrayList<>();

        // Skip leading newlines
        skipNewlines();

        while (!isAtEnd()) {
            // Parse one literal
            Literal literal = parseLiteral();
            literals.add(literal);

            // Expect newline or EOF after each literal
            if (!check(Token.TokenType.NEWLINE) && !check(Token.TokenType.EOF)) {
                throw new ParseException(
                    String.format("Expected newline or EOF after literal at line %d, column %d",
                        peek().getLine(), peek().getColumn())
                );
            }

            // Skip newlines
            skipNewlines();
        }

        return literals;
    }

    /**
     * Parses a single literal.
     *
     * Literal ::= Atom | NegAtom | Equality | Disequality
     */
    private Literal parseLiteral() throws ParseException {
        // Check for atom predicates
        if (check(Token.TokenType.ATOM)) {
            return parseAtom(true);
        }

        // Check for negated atom predicates
        if (check(Token.TokenType.NOT)) {
            advance();  // Consume '!'
            if (!check(Token.TokenType.ATOM)) {
                throw new ParseException(
                    String.format("Expected 'atom' after '!' at line %d, column %d",
                        peek().getLine(), peek().getColumn())
                );
            }
            return parseAtom(false);
        }

        // Otherwise, parse as equality or disequality
        return parseEqualityOrDisequality();
    }

    /**
     * Parses an atom literal: atom(term) or !atom(term)
     */
    private Literal parseAtom(boolean positive) throws ParseException {
        consume(Token.TokenType.ATOM, "Expected 'atom'");
        consume(Token.TokenType.LPAREN, "Expected '(' after 'atom'");

        Term term = parseTerm();

        consume(Token.TokenType.RPAREN, "Expected ')' after atom term");

        return positive ? Literal.atom(term) : Literal.notAtom(term);
    }

    /**
     * Parses an equality or disequality: term = term | term != term
     */
    private Literal parseEqualityOrDisequality() throws ParseException {
        Term left = parseTerm();

        if (check(Token.TokenType.EQUALS)) {
            advance();  // Consume '='
            Term right = parseTerm();
            return Literal.equality(left, right);
        } else if (check(Token.TokenType.NOT_EQUALS)) {
            advance();  // Consume '!='
            Term right = parseTerm();
            return Literal.disequality(left, right);
        } else {
            throw new ParseException(
                String.format("Expected '=' or '!=' at line %d, column %d",
                    peek().getLine(), peek().getColumn())
            );
        }
    }

    /**
     * Parses a term: identifier | function(arg1, arg2, ...)
     *
     * Term ::= Identifier | Identifier '(' ArgList ')'
     */
    private Term parseTerm() throws ParseException {
        if (!check(Token.TokenType.IDENTIFIER)) {
            throw new ParseException(
                String.format("Expected identifier at line %d, column %d",
                    peek().getLine(), peek().getColumn())
            );
        }

        String name = advance().getValue();

        // Check if this is a function application
        if (check(Token.TokenType.LPAREN)) {
            advance();  // Consume '('

            // Parse argument list
            List<Term> arguments = new ArrayList<>();

            // Handle empty argument list (constants like nil())
            if (check(Token.TokenType.RPAREN)) {
                advance();  // Consume ')'
                return termFactory.createFunctionApp(name, arguments);
            }

            // Parse first argument
            arguments.add(parseTerm());

            // Parse remaining arguments
            while (check(Token.TokenType.COMMA)) {
                advance();  // Consume ','
                arguments.add(parseTerm());
            }

            consume(Token.TokenType.RPAREN, "Expected ')' after function arguments");

            return termFactory.createFunctionApp(name, arguments);
        } else {
            // Simple identifier (variable or constant)
            // By convention, we treat all simple identifiers as variables
            // Constants would be represented as nullary functions: c()
            return termFactory.createVariable(name);
        }
    }

    /**
     * Checks if the current token matches the given type.
     */
    private boolean check(Token.TokenType type) {
        if (isAtEnd()) return type == Token.TokenType.EOF;
        return peek().getType() == type;
    }

    /**
     * Consumes the current token if it matches the expected type.
     */
    private Token consume(Token.TokenType type, String errorMessage) throws ParseException {
        if (check(type)) {
            return advance();
        }

        throw new ParseException(
            String.format("%s at line %d, column %d",
                errorMessage, peek().getLine(), peek().getColumn())
        );
    }

    /**
     * Advances to the next token and returns the current one.
     */
    private Token advance() {
        if (!isAtEnd()) {
            position++;
        }
        return previous();
    }

    /**
     * Returns the current token without consuming it.
     */
    private Token peek() {
        return tokens.get(position);
    }

    /**
     * Returns the previous token.
     */
    private Token previous() {
        return tokens.get(position - 1);
    }

    /**
     * Returns true if we've reached the end of the token stream.
     */
    private boolean isAtEnd() {
        return peek().getType() == Token.TokenType.EOF;
    }

    /**
     * Skips any newline tokens.
     */
    private void skipNewlines() {
        while (check(Token.TokenType.NEWLINE)) {
            advance();
        }
    }
}
