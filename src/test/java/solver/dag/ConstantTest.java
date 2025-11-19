package solver.dag;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for the Constant class.
 */
public class ConstantTest {

    @Test
    public void testCreation() {
        Constant const_ = new Constant(1, "true");

        assertEquals(1, const_.getId());
        assertEquals("true", const_.getSymbol());
    }

    @Test
    public void testIsLeaf() {
        Constant const_ = new Constant(1, "nil");

        assertTrue(const_.isLeaf());
    }

    @Test
    public void testNoArguments() {
        Constant const_ = new Constant(1, "zero");

        assertTrue(const_.getArguments().isEmpty());
        assertEquals(0, const_.getArguments().size());
    }

    @Test
    public void testToSExpression() {
        Constant const_ = new Constant(1, "false");

        assertEquals("false", const_.toSExpression());
    }

    @Test
    public void testToString() {
        Constant const_ = new Constant(1, "nil");

        assertEquals("nil", const_.toString());
    }

    @Test
    public void testInitialFind() {
        Constant const_ = new Constant(1, "true");

        assertSame(const_, const_.getFind());
    }

    @Test
    public void testEqualsSameSymbol() {
        Constant const1 = new Constant(1, "nil");
        Constant const2 = new Constant(2, "nil");

        assertEquals(const1, const2);
    }

    @Test
    public void testNotEqualsDifferentSymbol() {
        Constant const1 = new Constant(1, "true");
        Constant const2 = new Constant(2, "false");

        assertNotEquals(const1, const2);
    }

    @Test
    public void testNotEqualsVariable() {
        Constant const_ = new Constant(1, "x");
        Variable var = new Variable(2, "x");

        assertNotEquals(const_, var);
    }

    @Test
    public void testHashCodeConsistent() {
        Constant const_ = new Constant(1, "nil");

        assertEquals(const_.hashCode(), const_.hashCode());
    }

    @Test
    public void testHashCodeEqualObjects() {
        Constant const1 = new Constant(1, "true");
        Constant const2 = new Constant(2, "true");

        assertEquals(const1.hashCode(), const2.hashCode());
    }

    @Test
    public void testNumericConstants() {
        Constant zero = new Constant(1, "0");
        Constant one = new Constant(2, "1");

        assertEquals("0", zero.getSymbol());
        assertEquals("1", one.getSymbol());
        assertNotEquals(zero, one);
    }

    @Test
    public void testBooleanConstants() {
        Constant trueConst = new Constant(1, "true");
        Constant falseConst = new Constant(2, "false");

        assertNotEquals(trueConst, falseConst);
    }
}
