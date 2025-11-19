package solver.theory.te;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import solver.dag.Term;
import solver.dag.Variable;
import solver.dag.TermFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * Unit tests for the Literal class.
 */
public class LiteralTest {
    private TermFactory termFactory;
    private Term x, y, z;

    @BeforeEach
    public void setUp() {
        termFactory = new TermFactory();
        x = termFactory.createVariable("x");
        y = termFactory.createVariable("y");
        z = termFactory.createVariable("z");
    }

    @Test
    public void testCreateEquality() {
        Literal lit = Literal.equality(x, y);

        assertNotNull(lit);
        assertTrue(lit.isEquality());
        assertFalse(lit.isDisequality());
        assertFalse(lit.isAtom());
        assertEquals(x, lit.getLeft());
        assertEquals(y, lit.getRight());
    }

    @Test
    public void testCreateDisequality() {
        Literal lit = Literal.disequality(x, y);

        assertNotNull(lit);
        assertTrue(lit.isDisequality());
        assertFalse(lit.isEquality());
        assertFalse(lit.isAtom());
        assertEquals(x, lit.getLeft());
        assertEquals(y, lit.getRight());
    }

    @Test
    public void testCreateAtom() {
        Literal lit = Literal.atom(x);

        assertNotNull(lit);
        assertTrue(lit.isAtom());
        assertTrue(lit.isPositiveAtom());
        assertFalse(lit.isNegativeAtom());
        assertFalse(lit.isEquality());
        assertFalse(lit.isDisequality());
        assertEquals(x, lit.getAtomTerm());
    }

    @Test
    public void testCreateNotAtom() {
        Literal lit = Literal.notAtom(x);

        assertNotNull(lit);
        assertTrue(lit.isAtom());
        assertTrue(lit.isNegativeAtom());
        assertFalse(lit.isPositiveAtom());
        assertFalse(lit.isEquality());
        assertFalse(lit.isDisequality());
        assertEquals(x, lit.getAtomTerm());
    }

    @Test
    public void testEqualityToString() {
        Literal lit = Literal.equality(x, y);

        String str = lit.toString();
        assertTrue(str.contains("="));
        assertTrue(str.contains("x"));
        assertTrue(str.contains("y"));
    }

    @Test
    public void testDisequalityToString() {
        Literal lit = Literal.disequality(x, y);

        String str = lit.toString();
        assertTrue(str.contains("!="));
        assertTrue(str.contains("x"));
        assertTrue(str.contains("y"));
    }

    @Test
    public void testAtomToString() {
        Literal lit = Literal.atom(x);

        String str = lit.toString();
        assertTrue(str.contains("atom"));
        assertTrue(str.contains("x"));
        assertFalse(str.contains("¬"));
    }

    @Test
    public void testNotAtomToString() {
        Literal lit = Literal.notAtom(x);

        String str = lit.toString();
        assertTrue(str.contains("atom"));
        assertTrue(str.contains("x"));
        assertTrue(str.contains("¬"));
    }

    @Test
    public void testEqualityEqualsReflexive() {
        Literal lit = Literal.equality(x, y);

        assertEquals(lit, lit);
    }

    @Test
    public void testEqualitySameSides() {
        Literal lit1 = Literal.equality(x, y);
        Literal lit2 = Literal.equality(x, y);

        assertEquals(lit1, lit2);
    }

    @Test
    public void testEqualityFlippedSides() {
        Literal lit1 = Literal.equality(x, y);
        Literal lit2 = Literal.equality(y, x);

        assertEquals(lit1, lit2);
    }

    @Test
    public void testDisequalityNotEqualsEquality() {
        Literal eq = Literal.equality(x, y);
        Literal diseq = Literal.disequality(x, y);

        assertNotEquals(eq, diseq);
    }

    @Test
    public void testDifferentTermsNotEqual() {
        Literal lit1 = Literal.equality(x, y);
        Literal lit2 = Literal.equality(x, z);

        assertNotEquals(lit1, lit2);
    }

    @Test
    public void testAtomEqualsAtom() {
        Literal lit1 = Literal.atom(x);
        Literal lit2 = Literal.atom(x);

        assertEquals(lit1, lit2);
    }

    @Test
    public void testAtomNotEqualsNotAtom() {
        Literal lit1 = Literal.atom(x);
        Literal lit2 = Literal.notAtom(x);

        assertNotEquals(lit1, lit2);
    }

    @Test
    public void testAtomNotEqualsEquality() {
        Literal atom = Literal.atom(x);
        Literal eq = Literal.equality(x, y);

        assertNotEquals(atom, eq);
    }

    @Test
    public void testHashCodeConsistent() {
        Literal lit = Literal.equality(x, y);

        assertEquals(lit.hashCode(), lit.hashCode());
    }

    @Test
    public void testHashCodeEqualObjects() {
        Literal lit1 = Literal.equality(x, y);
        Literal lit2 = Literal.equality(x, y);

        assertEquals(lit1.hashCode(), lit2.hashCode());
    }

    @Test
    public void testHashCodeSymmetricEquality() {
        Literal lit1 = Literal.equality(x, y);
        Literal lit2 = Literal.equality(y, x);

        assertEquals(lit1.hashCode(), lit2.hashCode());
    }

    @Test
    public void testAtomTermThrowsForNonAtom() {
        Literal eq = Literal.equality(x, y);

        assertThrows(IllegalStateException.class, () -> {
            eq.getAtomTerm();
        });
    }

    @Test
    public void testGetLeftForEquality() {
        Literal lit = Literal.equality(x, y);

        assertEquals(x, lit.getLeft());
    }

    @Test
    public void testGetRightForEquality() {
        Literal lit = Literal.equality(x, y);

        assertEquals(y, lit.getRight());
    }

    @Test
    public void testGetLeftForAtom() {
        Literal lit = Literal.atom(x);

        assertEquals(x, lit.getLeft());
    }

    @Test
    public void testGetRightForAtomIsNull() {
        Literal lit = Literal.atom(x);

        assertNull(lit.getRight());
    }

    @Test
    public void testEqualityWithFunctionTerms() {
        List<Term> args = new ArrayList<>();
        args.add(x);
        Term fx = termFactory.createFunctionApp("f", args);

        List<Term> args2 = new ArrayList<>();
        args2.add(y);
        Term gy = termFactory.createFunctionApp("g", args2);

        Literal lit = Literal.equality(fx, gy);

        assertTrue(lit.isEquality());
        assertEquals(fx, lit.getLeft());
        assertEquals(gy, lit.getRight());
    }

    @Test
    public void testAtomWithFunctionTerm() {
        List<Term> args = new ArrayList<>();
        args.add(x);
        args.add(y);
        Term cons = termFactory.createFunctionApp("cons", args);

        Literal lit = Literal.notAtom(cons);

        assertTrue(lit.isNegativeAtom());
        assertEquals(cons, lit.getAtomTerm());
    }

    @Test
    public void testNotEqualsNull() {
        Literal lit = Literal.equality(x, y);

        assertNotEquals(lit, null);
    }

    @Test
    public void testNotEqualsDifferentType() {
        Literal lit = Literal.equality(x, y);

        assertNotEquals(lit, "x = y");
    }
}
