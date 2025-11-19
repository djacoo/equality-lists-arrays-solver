package solver.parser;

import solver.dag.Term;
import solver.dag.TermFactory;
import solver.theory.te.Literal;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Parser for SMT-LIB format, focusing on QF_UF logic.
 *
 * QF_UF = Quantifier-Free Uninterpreted Functions with equality
 *
 * Supported commands:
 * - (set-logic QF_UF)
 * - (declare-fun name (arg-types) return-type)
 * - (declare-const name type)
 * - (assert formula)
 * - (check-sat)
 *
 * Formulas can include:
 * - Equality: (= t1 t2)
 * - Boolean operators: (and ...), (or ...), (not ...)
 * - Function applications: (f x y)
 *
 * Reference: SMT-LIB Standard Version 2.0
 */
public class SMTLIBParser {
    private final List<SMTLIBToken> tokens;
    private final TermFactory termFactory;
    private int position;

    // Track declared functions for validation
    private final Map<String, FunctionSignature> declaredFunctions;

    // Store parsed literals from assert statements
    private final List<Literal> literals;

    private static class FunctionSignature {
        final String name;
        final int arity;
        final String returnType;

        FunctionSignature(String name, int arity, String returnType) {
            this.name = name;
            this.arity = arity;
            this.returnType = returnType;
        }
    }

    public SMTLIBParser(List<SMTLIBToken> tokens, TermFactory termFactory) {
        this.tokens = tokens;
        this.termFactory = termFactory;
        this.position = 0;
        this.declaredFunctions = new HashMap<>();
        this.literals = new ArrayList<>();
    }

    /**
     * Parses the SMT-LIB input and returns a list of literals.
     *
     * @return List of parsed literals from assert statements
     * @throws ParseException if the input is malformed
     */
    public List<Literal> parse() throws ParseException {
        while (!isAtEnd()) {
            parseCommand();
        }

        return literals;
    }

    /**
     * Parses a top-level command.
     *
     * Command ::= '(' CommandName Args... ')'
     */
    private void parseCommand() throws ParseException {
        consume(SMTLIBToken.TokenType.LPAREN, "Expected '(' at start of command");

        SMTLIBToken.TokenType cmdType = peek().getType();

        switch (cmdType) {
            case SET_LOGIC:
                parseSetLogic();
                break;
            case DECLARE_FUN:
                parseDeclareFun();
                break;
            case DECLARE_CONST:
                parseDeclareConst();
                break;
            case ASSERT:
                parseAssert();
                break;
            case CHECK_SAT:
                parseCheckSat();
                break;
            case GET_MODEL:
                parseGetModel();
                break;
            case EXIT:
                parseExit();
                break;
            default:
                throw new ParseException(
                    String.format("Unknown command at line %d, column %d: %s",
                        peek().getLine(), peek().getColumn(), peek().getValue())
                );
        }

        consume(SMTLIBToken.TokenType.RPAREN, "Expected ')' at end of command");
    }

    /**
     * Parses (set-logic QF_UF)
     */
    private void parseSetLogic() throws ParseException {
        advance();  // Consume 'set-logic'
        SMTLIBToken logic = consume(SMTLIBToken.TokenType.SYMBOL, "Expected logic name");

        // We primarily support QF_UF but can accept others
        if (!logic.getValue().equals("QF_UF")) {
            System.err.println("Warning: Logic " + logic.getValue() +
                " specified, but solver is designed for QF_UF");
        }
    }

    /**
     * Parses (declare-fun name (arg-types) return-type)
     */
    private void parseDeclareFun() throws ParseException {
        advance();  // Consume 'declare-fun'

        SMTLIBToken name = consume(SMTLIBToken.TokenType.SYMBOL, "Expected function name");

        // Parse argument types
        consume(SMTLIBToken.TokenType.LPAREN, "Expected '(' before argument types");
        List<String> argTypes = new ArrayList<>();
        while (!check(SMTLIBToken.TokenType.RPAREN)) {
            SMTLIBToken argType = consume(SMTLIBToken.TokenType.SYMBOL, "Expected argument type");
            argTypes.add(argType.getValue());
        }
        consume(SMTLIBToken.TokenType.RPAREN, "Expected ')' after argument types");

        // Parse return type
        SMTLIBToken returnType = consume(SMTLIBToken.TokenType.SYMBOL, "Expected return type");

        // Register the function
        declaredFunctions.put(name.getValue(),
            new FunctionSignature(name.getValue(), argTypes.size(), returnType.getValue()));
    }

    /**
     * Parses (declare-const name type)
     */
    private void parseDeclareConst() throws ParseException {
        advance();  // Consume 'declare-const'

        SMTLIBToken name = consume(SMTLIBToken.TokenType.SYMBOL, "Expected constant name");
        SMTLIBToken type = consume(SMTLIBToken.TokenType.SYMBOL, "Expected constant type");

        // A constant is just a nullary function
        declaredFunctions.put(name.getValue(),
            new FunctionSignature(name.getValue(), 0, type.getValue()));
    }

    /**
     * Parses (assert formula)
     *
     * This is the key method that converts SMT-LIB formulas to our Literal representation.
     */
    private void parseAssert() throws ParseException {
        advance();  // Consume 'assert'

        // Parse the formula and convert to literals
        List<Literal> assertLiterals = parseFormula();
        literals.addAll(assertLiterals);
    }

    /**
     * Parses a formula and converts it to literals.
     *
     * Formula ::= Symbol | '(' Op Args... ')'
     *
     * Returns a list of literals because some formulas may expand to multiple literals
     * (e.g., (and (= a b) (= c d)) becomes two literals)
     */
    private List<Literal> parseFormula() throws ParseException {
        List<Literal> result = new ArrayList<>();

        if (check(SMTLIBToken.TokenType.SYMBOL)) {
            // Boolean variable or predicate
            String symbol = advance().getValue();
            // For QF_UF, we treat boolean variables as terms
            // This is a simplification - full support would require boolean terms
            throw new ParseException("Standalone boolean variables not yet supported");
        } else if (check(SMTLIBToken.TokenType.LPAREN)) {
            advance();  // Consume '('

            SMTLIBToken op = consume(SMTLIBToken.TokenType.SYMBOL, "Expected operator");

            switch (op.getValue()) {
                case "=":
                    result.add(parseEquality());
                    break;
                case "and":
                    result.addAll(parseAnd());
                    break;
                case "or":
                    result.addAll(parseOr());
                    break;
                case "not":
                    result.addAll(parseNot());
                    break;
                default:
                    // Function application in boolean context - not typically used in our format
                    throw new ParseException(
                        String.format("Unsupported operator in formula: %s at line %d, column %d",
                            op.getValue(), op.getLine(), op.getColumn())
                    );
            }

            consume(SMTLIBToken.TokenType.RPAREN, "Expected ')' after formula");
        } else {
            throw new ParseException("Expected formula");
        }

        return result;
    }

    /**
     * Parses equality: (= term1 term2)
     *
     * Already inside the parentheses after '='
     */
    private Literal parseEquality() throws ParseException {
        Term left = parseTerm();
        Term right = parseTerm();
        return Literal.equality(left, right);
    }

    /**
     * Parses conjunction: (and formula1 formula2 ...)
     *
     * Already inside the parentheses after 'and'
     */
    private List<Literal> parseAnd() throws ParseException {
        List<Literal> result = new ArrayList<>();

        while (!check(SMTLIBToken.TokenType.RPAREN)) {
            result.addAll(parseFormula());
        }

        return result;
    }

    /**
     * Parses disjunction: (or formula1 formula2 ...)
     *
     * Note: Our solver works with conjunctions of literals.
     * Disjunctions would require CNF conversion, which we don't support yet.
     */
    private List<Literal> parseOr() throws ParseException {
        throw new ParseException("Disjunction (or) requires CNF conversion, not yet supported");
    }

    /**
     * Parses negation: (not formula)
     *
     * Already inside the parentheses after 'not'
     */
    private List<Literal> parseNot() throws ParseException {
        // We only support negation of equality for now
        if (!check(SMTLIBToken.TokenType.LPAREN)) {
            throw new ParseException("Expected '(' after 'not'");
        }

        advance();  // Consume '('
        SMTLIBToken op = consume(SMTLIBToken.TokenType.SYMBOL, "Expected operator");

        if (op.getValue().equals("=")) {
            Term left = parseTerm();
            Term right = parseTerm();
            consume(SMTLIBToken.TokenType.RPAREN, "Expected ')' after equality");

            List<Literal> result = new ArrayList<>();
            result.add(Literal.disequality(left, right));
            return result;
        } else {
            throw new ParseException(
                String.format("Unsupported negation of operator: %s", op.getValue())
            );
        }
    }

    /**
     * Parses a term: variable | constant | (function args...)
     */
    private Term parseTerm() throws ParseException {
        if (check(SMTLIBToken.TokenType.SYMBOL)) {
            String name = advance().getValue();

            // Check if this is followed by an application
            if (check(SMTLIBToken.TokenType.LPAREN)) {
                // This shouldn't happen in proper SMT-LIB syntax
                throw new ParseException("Function name should be inside parentheses");
            }

            // It's a variable or constant
            // If it was declared as a constant (nullary function), treat as variable
            return termFactory.createVariable(name);

        } else if (check(SMTLIBToken.TokenType.LPAREN)) {
            advance();  // Consume '('

            SMTLIBToken funcName = consume(SMTLIBToken.TokenType.SYMBOL, "Expected function name");

            // Parse arguments
            List<Term> args = new ArrayList<>();
            while (!check(SMTLIBToken.TokenType.RPAREN)) {
                args.add(parseTerm());
            }

            consume(SMTLIBToken.TokenType.RPAREN, "Expected ')' after function arguments");

            return termFactory.createFunctionApp(funcName.getValue(), args);

        } else {
            throw new ParseException(
                String.format("Expected term at line %d, column %d",
                    peek().getLine(), peek().getColumn())
            );
        }
    }

    /**
     * Parses (check-sat)
     */
    private void parseCheckSat() throws ParseException {
        advance();  // Consume 'check-sat'
        // We don't do anything here - the solver will be invoked after parsing
    }

    /**
     * Parses (get-model)
     */
    private void parseGetModel() throws ParseException {
        advance();  // Consume 'get-model'
        // We don't do anything here - model generation is handled separately
    }

    /**
     * Parses (exit)
     */
    private void parseExit() throws ParseException {
        advance();  // Consume 'exit'
        // We don't do anything here
    }

    /**
     * Checks if the current token matches the given type.
     */
    private boolean check(SMTLIBToken.TokenType type) {
        if (isAtEnd()) return type == SMTLIBToken.TokenType.EOF;
        return peek().getType() == type;
    }

    /**
     * Consumes the current token if it matches the expected type.
     */
    private SMTLIBToken consume(SMTLIBToken.TokenType type, String errorMessage) throws ParseException {
        if (check(type)) {
            return advance();
        }

        throw new ParseException(
            String.format("%s at line %d, column %d (found: %s)",
                errorMessage, peek().getLine(), peek().getColumn(), peek().getType())
        );
    }

    /**
     * Advances to the next token and returns the current one.
     */
    private SMTLIBToken advance() {
        if (!isAtEnd()) {
            position++;
        }
        return previous();
    }

    /**
     * Returns the current token without consuming it.
     */
    private SMTLIBToken peek() {
        return tokens.get(position);
    }

    /**
     * Returns the previous token.
     */
    private SMTLIBToken previous() {
        return tokens.get(position - 1);
    }

    /**
     * Returns true if we've reached the end of the token stream.
     */
    private boolean isAtEnd() {
        return peek().getType() == SMTLIBToken.TokenType.EOF;
    }
}
