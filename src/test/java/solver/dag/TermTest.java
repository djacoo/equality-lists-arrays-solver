package solver.dag;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.util.ArrayList;
import java.util.List;

/**
 * Unit tests for the Term abstract class.
 * Tests general Term behavior across all implementations.
 */
public class TermTest {

    @Test
    public void testVariableIsLeaf() {
        Term var = new Variable(1, "x");

        assertTrue(var.isLeaf());
    }

    @Test
    public void testConstantIsLeaf() {
        Term const_ = new Constant(1, "nil");

        assertTrue(const_.isLeaf());
    }

    @Test
    public void testFunctionAppIsNotLeaf() {
        Term x = new Variable(1, "x");
        List<Term> args = new ArrayList<>();
        args.add(x);
        Term func = new FunctionApp(2, "f", args);

        assertFalse(func.isLeaf());
    }

    @Test
    public void testLeavesHaveNoArguments() {
        Term var = new Variable(1, "x");
        Term const_ = new Constant(2, "nil");

        assertTrue(var.getArguments().isEmpty());
        assertTrue(const_.getArguments().isEmpty());
    }

    @Test
    public void testFunctionAppHasArguments() {
        Term x = new Variable(1, "x");
        List<Term> args = new ArrayList<>();
        args.add(x);
        Term func = new FunctionApp(2, "f", args);

        assertFalse(func.getArguments().isEmpty());
        assertEquals(1, func.getArguments().size());
    }

    @Test
    public void testToStringUsesSExpression() {
        Term var = new Variable(1, "x");

        assertEquals(var.toSExpression(), var.toString());
    }

    @Test
    public void testInitialFindIsSelf() {
        Term var = new Variable(1, "x");
        Term const_ = new Constant(2, "nil");

        Term x = new Variable(3, "y");
        List<Term> args = new ArrayList<>();
        args.add(x);
        Term func = new FunctionApp(4, "f", args);

        assertSame(var, var.getFind());
        assertSame(const_, const_.getFind());
        assertSame(func, func.getFind());
    }

    @Test
    public void testSetFind() {
        Term var1 = new Variable(1, "x");
        Term var2 = new Variable(2, "y");

        var1.setFind(var2);

        assertSame(var2, var1.getFind());
    }

    @Test
    public void testCcparInitiallyEmpty() {
        Term var = new Variable(1, "x");
        Term const_ = new Constant(2, "nil");

        assertEquals(0, var.getCcparSize());
        assertEquals(0, const_.getCcparSize());
    }

    @Test
    public void testAddToCcpar() {
        Term x = new Variable(1, "x");
        List<Term> args = new ArrayList<>();
        args.add(x);
        FunctionApp func = new FunctionApp(2, "f", args);

        // Function automatically registers itself in argument's ccpar
        assertEquals(1, x.getCcparSize());
        assertTrue(x.getCcpar().contains(func));
    }

    @Test
    public void testMultipleCcparParents() {
        Term x = new Variable(1, "x");

        List<Term> args1 = new ArrayList<>();
        args1.add(x);
        FunctionApp func1 = new FunctionApp(2, "f", args1);

        List<Term> args2 = new ArrayList<>();
        args2.add(x);
        FunctionApp func2 = new FunctionApp(3, "g", args2);

        // x should have both functions in its ccpar set
        assertEquals(2, x.getCcparSize());
        assertTrue(x.getCcpar().contains(func1));
        assertTrue(x.getCcpar().contains(func2));
    }

    @Test
    public void testGetId() {
        Term var = new Variable(1, "x");
        Term const_ = new Constant(2, "nil");

        Term y = new Variable(3, "y");
        List<Term> args = new ArrayList<>();
        args.add(y);
        Term func = new FunctionApp(4, "f", args);

        assertEquals(1, var.getId());
        assertEquals(2, const_.getId());
        assertEquals(4, func.getId());
    }

    @Test
    public void testGetSymbol() {
        Term var = new Variable(1, "myVar");
        Term const_ = new Constant(2, "nil");

        Term x = new Variable(3, "x");
        List<Term> args = new ArrayList<>();
        args.add(x);
        Term func = new FunctionApp(4, "myFunc", args);

        assertEquals("myVar", var.getSymbol());
        assertEquals("nil", const_.getSymbol());
        assertEquals("myFunc", func.getSymbol());
    }

    @Test
    public void testDifferentTypesNotEqual() {
        Term var = new Variable(1, "x");
        Term const_ = new Constant(2, "x");

        Term y = new Variable(3, "y");
        List<Term> args = new ArrayList<>();
        args.add(y);
        Term func = new FunctionApp(4, "x", args);

        assertNotEquals(var, const_);
        assertNotEquals(var, func);
        assertNotEquals(const_, func);
    }

    @Test
    public void testEqualsReflexive() {
        Term var = new Variable(1, "x");

        assertEquals(var, var);
    }

    @Test
    public void testHashCodeCached() {
        Term var = new Variable(1, "x");

        int hash1 = var.hashCode();
        int hash2 = var.hashCode();

        assertEquals(hash1, hash2);
    }
}
