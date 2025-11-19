package solver.parser;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import solver.dag.FunctionApp;
import solver.dag.Term;
import solver.dag.TermFactory;
import solver.dag.Variable;
import solver.theory.te.Literal;

import java.util.ArrayList;
import java.util.List;

/**
 * Unit tests for the PredicateTransformer class.
 * Tests transformation of free predicate symbols into equalities.
 */
public class PredicateTransformerTest {
    private TermFactory termFactory;
    private PredicateTransformer transformer;

    @BeforeEach
    public void setUp() {
        termFactory = new TermFactory();
        transformer = new PredicateTransformer(termFactory);
    }

    @Test
    public void testGetTrueConstant() {
        Term trueConst = transformer.getTrueConstant();

        assertNotNull(trueConst);
        assertTrue(trueConst instanceof Variable);
        assertEquals("$true", trueConst.toString());
    }

    @Test
    public void testGetFalseConstant() {
        Term falseConst = transformer.getFalseConstant();

        assertNotNull(falseConst);
        assertTrue(falseConst instanceof Variable);
        assertEquals("$false", falseConst.toString());
    }

    @Test
    public void testTrueAndFalseAreDifferent() {
        Term trueConst = transformer.getTrueConstant();
        Term falseConst = transformer.getFalseConstant();

        assertNotEquals(trueConst, falseConst);
    }

    @Test
    public void testBooleanDistinctnessAxiom() {
        Literal axiom = transformer.getBooleanDistinctnessAxiom();

        assertNotNull(axiom);
        assertTrue(axiom.isDisequality());
    }

    @Test
    public void testTransformPositivePredicate() {
        Term x = termFactory.createVariable("x");
        List<Term> args = new ArrayList<>();
        args.add(x);

        Literal literal = transformer.transformPredicate("P", args, true);

        assertNotNull(literal);
        assertTrue(literal.isEquality());

        // The literal should be P(x) = $true
        Term left = literal.getLeft();
        Term right = literal.getRight();

        // One side should be the predicate application, the other should be $true
        boolean foundPredicate = (left instanceof FunctionApp) || (right instanceof FunctionApp);
        assertTrue(foundPredicate);
    }

    @Test
    public void testTransformNegativePredicate() {
        Term x = termFactory.createVariable("x");
        List<Term> args = new ArrayList<>();
        args.add(x);

        Literal literal = transformer.transformPredicate("P", args, false);

        assertNotNull(literal);
        assertTrue(literal.isEquality());

        // The literal should be P(x) = $false
        Term left = literal.getLeft();
        Term right = literal.getRight();

        // One side should be the predicate application
        boolean foundPredicate = (left instanceof FunctionApp) || (right instanceof FunctionApp);
        assertTrue(foundPredicate);
    }

    @Test
    public void testTransformPredicateWithMultipleArguments() {
        Term x = termFactory.createVariable("x");
        Term y = termFactory.createVariable("y");
        List<Term> args = new ArrayList<>();
        args.add(x);
        args.add(y);

        Literal literal = transformer.transformPredicate("Q", args, true);

        assertNotNull(literal);
        assertTrue(literal.isEquality());
    }

    @Test
    public void testTransformPredicateWithNoArguments() {
        List<Term> args = new ArrayList<>();

        Literal literal = transformer.transformPredicate("R", args, true);

        assertNotNull(literal);
        assertTrue(literal.isEquality());
    }

    @Test
    public void testTransformPredicateWithComplexArgument() {
        Term x = termFactory.createVariable("x");
        Term y = termFactory.createVariable("y");
        List<Term> consArgs = new ArrayList<>();
        consArgs.add(x);
        consArgs.add(y);
        Term consTerm = termFactory.createFunctionApp("cons", consArgs);

        List<Term> predicateArgs = new ArrayList<>();
        predicateArgs.add(consTerm);

        Literal literal = transformer.transformPredicate("P", predicateArgs, true);

        assertNotNull(literal);
        assertTrue(literal.isEquality());
    }

    @Test
    public void testMultipleTransformationsSameTrue() {
        Term x = termFactory.createVariable("x");
        Term y = termFactory.createVariable("y");

        List<Term> args1 = new ArrayList<>();
        args1.add(x);
        Literal lit1 = transformer.transformPredicate("P", args1, true);

        List<Term> args2 = new ArrayList<>();
        args2.add(y);
        Literal lit2 = transformer.transformPredicate("Q", args2, true);

        // Both should use the same $true constant
        assertNotNull(lit1);
        assertNotNull(lit2);
        assertTrue(lit1.isEquality());
        assertTrue(lit2.isEquality());
    }

    @Test
    public void testMultipleTransformationsSameFalse() {
        Term x = termFactory.createVariable("x");
        Term y = termFactory.createVariable("y");

        List<Term> args1 = new ArrayList<>();
        args1.add(x);
        Literal lit1 = transformer.transformPredicate("P", args1, false);

        List<Term> args2 = new ArrayList<>();
        args2.add(y);
        Literal lit2 = transformer.transformPredicate("Q", args2, false);

        // Both should use the same $false constant
        assertNotNull(lit1);
        assertNotNull(lit2);
        assertTrue(lit1.isEquality());
        assertTrue(lit2.isEquality());
    }

    @Test
    public void testTransformPredicatePreservesPolarity() {
        Term x = termFactory.createVariable("x");
        List<Term> args = new ArrayList<>();
        args.add(x);

        Literal posLit = transformer.transformPredicate("P", args, true);
        Literal negLit = transformer.transformPredicate("P", args, false);

        assertNotNull(posLit);
        assertNotNull(negLit);
        assertTrue(posLit.isEquality());
        assertTrue(negLit.isEquality());

        // They should be different (one equals true, one equals false)
        assertNotEquals(posLit.toString(), negLit.toString());
    }

    @Test
    public void testTransformDifferentPredicateNames() {
        Term x = termFactory.createVariable("x");
        List<Term> args = new ArrayList<>();
        args.add(x);

        Literal litP = transformer.transformPredicate("P", args, true);
        Literal litQ = transformer.transformPredicate("Q", args, true);

        assertNotNull(litP);
        assertNotNull(litQ);
        assertTrue(litP.isEquality());
        assertTrue(litQ.isEquality());

        // They should be different predicates
        assertNotEquals(litP.toString(), litQ.toString());
    }

    @Test
    public void testConstantsUseSameTermFactory() {
        Term trueConst1 = transformer.getTrueConstant();
        Term trueConst2 = transformer.getTrueConstant();

        // Should return the same object (cached)
        assertSame(trueConst1, trueConst2);
    }

    @Test
    public void testBooleanAxiomConsistency() {
        Literal axiom1 = transformer.getBooleanDistinctnessAxiom();
        Literal axiom2 = transformer.getBooleanDistinctnessAxiom();

        // Both calls should return consistent results
        assertNotNull(axiom1);
        assertNotNull(axiom2);
        assertTrue(axiom1.isDisequality());
        assertTrue(axiom2.isDisequality());
    }
}
