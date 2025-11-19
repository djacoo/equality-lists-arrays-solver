package solver.dag;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.util.ArrayList;
import java.util.List;

/**
 * Unit tests for the FunctionApp class.
 */
public class FunctionAppTest {

    @Test
    public void testCreation() {
        Variable x = new Variable(1, "x");
        List<Term> args = new ArrayList<>();
        args.add(x);

        FunctionApp func = new FunctionApp(2, "f", args);

        assertEquals(2, func.getId());
        assertEquals("f", func.getSymbol());
        assertEquals(1, func.getArguments().size());
        assertEquals(x, func.getArguments().get(0));
    }

    @Test
    public void testIsNotLeaf() {
        Variable x = new Variable(1, "x");
        List<Term> args = new ArrayList<>();
        args.add(x);

        FunctionApp func = new FunctionApp(2, "f", args);

        assertFalse(func.isLeaf());
    }

    @Test
    public void testNullaryFunction() {
        List<Term> args = new ArrayList<>();
        FunctionApp func = new FunctionApp(1, "nil", args);

        assertEquals(0, func.getArguments().size());
        assertEquals(0, func.getArity());
        assertFalse(func.isLeaf());
    }

    @Test
    public void testUnaryFunction() {
        Variable x = new Variable(1, "x");
        List<Term> args = new ArrayList<>();
        args.add(x);

        FunctionApp func = new FunctionApp(2, "car", args);

        assertEquals(1, func.getArity());
        assertEquals(x, func.getArguments().get(0));
    }

    @Test
    public void testBinaryFunction() {
        Variable x = new Variable(1, "x");
        Variable y = new Variable(2, "y");
        List<Term> args = new ArrayList<>();
        args.add(x);
        args.add(y);

        FunctionApp func = new FunctionApp(3, "cons", args);

        assertEquals(2, func.getArity());
        assertEquals(x, func.getArguments().get(0));
        assertEquals(y, func.getArguments().get(1));
    }

    @Test
    public void testTernaryFunction() {
        Variable a = new Variable(1, "a");
        Variable i = new Variable(2, "i");
        Variable v = new Variable(3, "v");
        List<Term> args = new ArrayList<>();
        args.add(a);
        args.add(i);
        args.add(v);

        FunctionApp func = new FunctionApp(4, "store", args);

        assertEquals(3, func.getArity());
    }

    @Test
    public void testToSExpressionNullary() {
        List<Term> args = new ArrayList<>();
        FunctionApp func = new FunctionApp(1, "nil", args);

        assertEquals("nil", func.toSExpression());
    }

    @Test
    public void testToSExpressionUnary() {
        Variable x = new Variable(1, "x");
        List<Term> args = new ArrayList<>();
        args.add(x);

        FunctionApp func = new FunctionApp(2, "f", args);

        assertEquals("(f x)", func.toSExpression());
    }

    @Test
    public void testToSExpressionBinary() {
        Variable x = new Variable(1, "x");
        Variable y = new Variable(2, "y");
        List<Term> args = new ArrayList<>();
        args.add(x);
        args.add(y);

        FunctionApp func = new FunctionApp(3, "cons", args);

        assertEquals("(cons x y)", func.toSExpression());
    }

    @Test
    public void testToSExpressionNested() {
        Variable x = new Variable(1, "x");
        List<Term> args1 = new ArrayList<>();
        args1.add(x);
        FunctionApp inner = new FunctionApp(2, "g", args1);

        List<Term> args2 = new ArrayList<>();
        args2.add(inner);
        FunctionApp outer = new FunctionApp(3, "f", args2);

        assertEquals("(f (g x))", outer.toSExpression());
    }

    @Test
    public void testArgumentsImmutable() {
        Variable x = new Variable(1, "x");
        List<Term> args = new ArrayList<>();
        args.add(x);

        FunctionApp func = new FunctionApp(2, "f", args);

        // Try to modify the original list
        args.add(new Variable(3, "y"));

        // Function should still have only one argument
        assertEquals(1, func.getArguments().size());
    }

    @Test
    public void testCcparRegistration() {
        Variable x = new Variable(1, "x");
        Variable y = new Variable(2, "y");
        List<Term> args = new ArrayList<>();
        args.add(x);
        args.add(y);

        FunctionApp func = new FunctionApp(3, "f", args);

        // The function should be registered in the ccpar sets of its arguments
        assertTrue(x.getCcpar().contains(func));
        assertTrue(y.getCcpar().contains(func));
        assertEquals(1, x.getCcparSize());
        assertEquals(1, y.getCcparSize());
    }

    @Test
    public void testEqualsSameStructure() {
        Variable x = new Variable(1, "x");
        List<Term> args1 = new ArrayList<>();
        args1.add(x);
        FunctionApp func1 = new FunctionApp(2, "f", args1);

        List<Term> args2 = new ArrayList<>();
        args2.add(x);
        FunctionApp func2 = new FunctionApp(3, "f", args2);

        assertEquals(func1, func2);
    }

    @Test
    public void testNotEqualsDifferentSymbol() {
        Variable x = new Variable(1, "x");
        List<Term> args1 = new ArrayList<>();
        args1.add(x);
        FunctionApp func1 = new FunctionApp(2, "f", args1);

        List<Term> args2 = new ArrayList<>();
        args2.add(x);
        FunctionApp func2 = new FunctionApp(3, "g", args2);

        assertNotEquals(func1, func2);
    }

    @Test
    public void testNotEqualsDifferentArguments() {
        Variable x = new Variable(1, "x");
        Variable y = new Variable(2, "y");

        List<Term> args1 = new ArrayList<>();
        args1.add(x);
        FunctionApp func1 = new FunctionApp(3, "f", args1);

        List<Term> args2 = new ArrayList<>();
        args2.add(y);
        FunctionApp func2 = new FunctionApp(4, "f", args2);

        assertNotEquals(func1, func2);
    }

    @Test
    public void testNotEqualsDifferentArity() {
        Variable x = new Variable(1, "x");

        List<Term> args1 = new ArrayList<>();
        args1.add(x);
        FunctionApp func1 = new FunctionApp(2, "f", args1);

        List<Term> args2 = new ArrayList<>();
        FunctionApp func2 = new FunctionApp(3, "f", args2);

        assertNotEquals(func1, func2);
    }

    @Test
    public void testHashCodeConsistent() {
        Variable x = new Variable(1, "x");
        List<Term> args = new ArrayList<>();
        args.add(x);
        FunctionApp func = new FunctionApp(2, "f", args);

        assertEquals(func.hashCode(), func.hashCode());
    }

    @Test
    public void testHashCodeEqualObjects() {
        Variable x = new Variable(1, "x");

        List<Term> args1 = new ArrayList<>();
        args1.add(x);
        FunctionApp func1 = new FunctionApp(2, "f", args1);

        List<Term> args2 = new ArrayList<>();
        args2.add(x);
        FunctionApp func2 = new FunctionApp(3, "f", args2);

        assertEquals(func1.hashCode(), func2.hashCode());
    }
}
