package solver.dag;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import static org.junit.jupiter.api.Assertions.*;

import java.util.Arrays;

/**
 * Tests for TermFactory hash-consing behavior.
 */
public class TermFactoryTest {
    private TermFactory factory;

    @BeforeEach
    public void setUp() {
        factory = new TermFactory();
    }

    @Test
    public void testVariableCreation() {
        Variable x = factory.createVariable("x");
        assertNotNull(x);
        assertEquals("x", x.getSymbol());
        assertTrue(x.isLeaf());
        assertEquals(0, x.getArguments().size());
    }

    @Test
    public void testVariableHashConsing() {
        Variable x1 = factory.createVariable("x");
        Variable x2 = factory.createVariable("x");

        // Same variable name should return same object
        assertSame(x1, x2, "Hash-consing should return same object for identical variables");

        Variable y = factory.createVariable("y");
        assertNotSame(x1, y, "Different variables should be different objects");
    }

    @Test
    public void testConstantCreation() {
        Constant zero = factory.createConstant("0");
        assertNotNull(zero);
        assertEquals("0", zero.getSymbol());
        assertTrue(zero.isLeaf());
    }

    @Test
    public void testConstantHashConsing() {
        Constant c1 = factory.createConstant("0");
        Constant c2 = factory.createConstant("0");
        assertSame(c1, c2, "Hash-consing should return same object for identical constants");
    }

    @Test
    public void testFunctionAppCreation() {
        Variable x = factory.createVariable("x");
        Variable y = factory.createVariable("y");
        FunctionApp f = factory.createFunctionApp("f", Arrays.asList(x, y));

        assertNotNull(f);
        assertEquals("f", f.getSymbol());
        assertFalse(f.isLeaf());
        assertEquals(2, f.getArity());
        assertEquals(Arrays.asList(x, y), f.getArguments());
    }

    @Test
    public void testFunctionAppHashConsing() {
        Variable x = factory.createVariable("x");
        Variable y = factory.createVariable("y");

        FunctionApp f1 = factory.createFunctionApp("f", x, y);
        FunctionApp f2 = factory.createFunctionApp("f", x, y);

        assertSame(f1, f2, "Hash-consing should return same object for identical function apps");
    }

    @Test
    public void testFunctionAppDifferentArgs() {
        Variable x = factory.createVariable("x");
        Variable y = factory.createVariable("y");

        FunctionApp fx = factory.createFunctionApp("f", x);
        FunctionApp fy = factory.createFunctionApp("f", y);

        assertNotSame(fx, fy, "Functions with different args should be different");
    }

    @Test
    public void testNestedFunctionApp() {
        Variable a = factory.createVariable("a");
        FunctionApp fa = factory.createFunctionApp("f", a);
        FunctionApp gfa = factory.createFunctionApp("g", fa);

        assertEquals("g", gfa.getSymbol());
        assertEquals(1, gfa.getArity());
        assertSame(fa, gfa.getArguments().get(0));
    }

    @Test
    public void testCcparTracking() {
        Variable x = factory.createVariable("x");
        Variable y = factory.createVariable("y");

        // x is not an argument to anything yet
        assertEquals(0, x.getCcparSize());

        // Create f(x, y)
        FunctionApp f = factory.createFunctionApp("f", x, y);

        // Now x and y should have f in their ccpar sets
        assertEquals(1, x.getCcparSize());
        assertEquals(1, y.getCcparSize());
        assertTrue(x.getCcpar().contains(f));
        assertTrue(y.getCcpar().contains(f));
    }

    @Test
    public void testCcparWithMultipleParents() {
        Variable a = factory.createVariable("a");

        FunctionApp fa = factory.createFunctionApp("f", a);
        FunctionApp ga = factory.createFunctionApp("g", a);

        // a is argument to both f and g
        assertEquals(2, a.getCcparSize());
        assertTrue(a.getCcpar().contains(fa));
        assertTrue(a.getCcpar().contains(ga));
    }

    @Test
    public void testSExpressionFormat() {
        Variable x = factory.createVariable("x");
        assertEquals("x", x.toSExpression());

        FunctionApp fx = factory.createFunctionApp("f", x);
        assertEquals("(f x)", fx.toSExpression());

        Variable y = factory.createVariable("y");
        FunctionApp fxy = factory.createFunctionApp("f", x, y);
        assertEquals("(f x y)", fxy.toSExpression());

        FunctionApp gfxy = factory.createFunctionApp("g", fxy);
        assertEquals("(g (f x y))", gfxy.toSExpression());
    }

    @Test
    public void testTermCount() {
        assertEquals(0, factory.getTermCount());

        factory.createVariable("x");
        assertEquals(1, factory.getTermCount());

        factory.createVariable("x");  // Same variable, shouldn't increase count
        assertEquals(1, factory.getTermCount());

        factory.createVariable("y");
        assertEquals(2, factory.getTermCount());
    }

    @Test
    public void testConvenienceMethods() {
        Variable x = factory.createVariable("x");
        Variable y = factory.createVariable("y");
        Variable z = factory.createVariable("z");

        // Unary
        FunctionApp f1 = factory.createFunctionApp("f", x);
        assertEquals(1, f1.getArity());

        // Binary
        FunctionApp f2 = factory.createFunctionApp("f", x, y);
        assertEquals(2, f2.getArity());

        // Ternary
        FunctionApp f3 = factory.createFunctionApp("f", x, y, z);
        assertEquals(3, f3.getArity());
    }
}
