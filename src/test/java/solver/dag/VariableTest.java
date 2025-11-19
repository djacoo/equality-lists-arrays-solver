package solver.dag;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.util.HashSet;
import java.util.Set;

/**
 * Unit tests for the Variable class.
 */
public class VariableTest {

    @Test
    public void testCreation() {
        Variable var = new Variable(1, "x");

        assertEquals(1, var.getId());
        assertEquals("x", var.getSymbol());
    }

    @Test
    public void testIsLeaf() {
        Variable var = new Variable(1, "x");

        assertTrue(var.isLeaf());
    }

    @Test
    public void testNoArguments() {
        Variable var = new Variable(1, "x");

        assertTrue(var.getArguments().isEmpty());
        assertEquals(0, var.getArguments().size());
    }

    @Test
    public void testToSExpression() {
        Variable var = new Variable(1, "myVar");

        assertEquals("myVar", var.toSExpression());
    }

    @Test
    public void testToString() {
        Variable var = new Variable(1, "x");

        assertEquals("x", var.toString());
    }

    @Test
    public void testInitialFind() {
        Variable var = new Variable(1, "x");

        assertSame(var, var.getFind());
    }

    @Test
    public void testSetFind() {
        Variable var1 = new Variable(1, "x");
        Variable var2 = new Variable(2, "y");

        var1.setFind(var2);

        assertSame(var2, var1.getFind());
    }

    @Test
    public void testCcparInitiallyEmpty() {
        Variable var = new Variable(1, "x");

        assertTrue(var.getCcpar().isEmpty());
        assertEquals(0, var.getCcparSize());
    }

    @Test
    public void testEqualsReflexive() {
        Variable var = new Variable(1, "x");

        assertEquals(var, var);
    }

    @Test
    public void testEqualsSameSymbol() {
        Variable var1 = new Variable(1, "x");
        Variable var2 = new Variable(2, "x");

        assertEquals(var1, var2);
    }

    @Test
    public void testNotEqualsDifferentSymbol() {
        Variable var1 = new Variable(1, "x");
        Variable var2 = new Variable(2, "y");

        assertNotEquals(var1, var2);
    }

    @Test
    public void testNotEqualsNull() {
        Variable var = new Variable(1, "x");

        assertNotEquals(var, null);
    }

    @Test
    public void testNotEqualsDifferentType() {
        Variable var = new Variable(1, "x");
        Constant const_ = new Constant(2, "x");

        assertNotEquals(var, const_);
    }

    @Test
    public void testHashCodeConsistent() {
        Variable var = new Variable(1, "x");

        int hash1 = var.hashCode();
        int hash2 = var.hashCode();

        assertEquals(hash1, hash2);
    }

    @Test
    public void testHashCodeEqualObjects() {
        Variable var1 = new Variable(1, "x");
        Variable var2 = new Variable(2, "x");

        assertEquals(var1.hashCode(), var2.hashCode());
    }

    @Test
    public void testHashSetBehavior() {
        Variable var1 = new Variable(1, "x");
        Variable var2 = new Variable(2, "x");

        Set<Variable> set = new HashSet<>();
        set.add(var1);

        assertTrue(set.contains(var2));
    }

    @Test
    public void testDifferentVariableNames() {
        Variable x = new Variable(1, "x");
        Variable y = new Variable(2, "y");
        Variable z = new Variable(3, "z");

        assertNotEquals(x, y);
        assertNotEquals(y, z);
        assertNotEquals(x, z);
    }
}
