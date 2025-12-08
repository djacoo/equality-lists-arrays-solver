package solver.equivalence;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import solver.dag.Term;
import solver.dag.TermFactory;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for the ForbiddenSet class.
 * Tests the forbidden set optimization for early UNSAT detection.
 */
@DisplayName("ForbiddenSet Tests")
public class ForbiddenSetTest {

    private ForbiddenSet forbiddenSet;
    private TermFactory factory;
    private Term a, b, c, d;

    @BeforeEach
    void setUp() {
        forbiddenSet = new ForbiddenSet();
        factory = new TermFactory();
        a = factory.createVariable("a");
        b = factory.createVariable("b");
        c = factory.createVariable("c");
        d = factory.createVariable("d");
    }

    @Test
    @DisplayName("Initially empty forbidden set")
    void testInitiallyEmpty() {
        assertEquals(0, forbiddenSet.size());
        assertEquals(0, forbiddenSet.getForbiddenPairsAdded());
        assertEquals(0, forbiddenSet.getForbiddenChecks());
    }

    @Test
    @DisplayName("Add single forbidden pair")
    void testAddSinglePair() {
        forbiddenSet.addForbiddenPair(a, b);

        assertEquals(1, forbiddenSet.size());
        assertEquals(1, forbiddenSet.getForbiddenPairsAdded());
        assertTrue(forbiddenSet.isForbidden(a, b));
        assertEquals(1, forbiddenSet.getForbiddenChecks());
    }

    @Test
    @DisplayName("Forbidden check is symmetric")
    void testSymmetricCheck() {
        forbiddenSet.addForbiddenPair(a, b);

        assertTrue(forbiddenSet.isForbidden(a, b));
        assertTrue(forbiddenSet.isForbidden(b, a));  // Order doesn't matter
    }

    @Test
    @DisplayName("Add multiple forbidden pairs")
    void testAddMultiplePairs() {
        forbiddenSet.addForbiddenPair(a, b);
        forbiddenSet.addForbiddenPair(c, d);

        assertEquals(2, forbiddenSet.size());
        assertEquals(2, forbiddenSet.getForbiddenPairsAdded());

        assertTrue(forbiddenSet.isForbidden(a, b));
        assertTrue(forbiddenSet.isForbidden(c, d));
        assertFalse(forbiddenSet.isForbidden(a, c));
        assertFalse(forbiddenSet.isForbidden(b, d));
    }

    @Test
    @DisplayName("Adding duplicate pairs doesn't increase size")
    void testDuplicatePairs() {
        forbiddenSet.addForbiddenPair(a, b);
        forbiddenSet.addForbiddenPair(a, b);  // Duplicate
        forbiddenSet.addForbiddenPair(b, a);  // Duplicate (symmetric)

        // Size should be 1 (set semantics)
        assertEquals(1, forbiddenSet.size());

        // But pairs added count includes duplicates
        assertEquals(3, forbiddenSet.getForbiddenPairsAdded());
    }

    @Test
    @DisplayName("Check non-forbidden pairs")
    void testNonForbiddenPairs() {
        forbiddenSet.addForbiddenPair(a, b);

        assertFalse(forbiddenSet.isForbidden(a, c));
        assertFalse(forbiddenSet.isForbidden(b, c));
        assertFalse(forbiddenSet.isForbidden(c, d));
    }

    @Test
    @DisplayName("Clear forbidden set")
    void testClear() {
        forbiddenSet.addForbiddenPair(a, b);
        forbiddenSet.addForbiddenPair(c, d);

        assertEquals(2, forbiddenSet.size());

        forbiddenSet.clear();

        assertEquals(0, forbiddenSet.size());
        assertFalse(forbiddenSet.isForbidden(a, b));
        assertFalse(forbiddenSet.isForbidden(c, d));
    }

    @Test
    @DisplayName("Statistics tracking")
    void testStatisticsTracking() {
        forbiddenSet.addForbiddenPair(a, b);
        forbiddenSet.addForbiddenPair(c, d);

        // Check multiple times
        forbiddenSet.isForbidden(a, b);
        forbiddenSet.isForbidden(c, d);
        forbiddenSet.isForbidden(a, c);
        forbiddenSet.isForbidden(b, d);

        assertEquals(2, forbiddenSet.size());
        assertEquals(2, forbiddenSet.getForbiddenPairsAdded());
        assertEquals(4, forbiddenSet.getForbiddenChecks());
    }

    @Test
    @DisplayName("TermPair canonical ordering")
    void testTermPairCanonicalOrdering() {
        TermFactory tf = new TermFactory();
        Term t1 = tf.createVariable("x");
        Term t2 = tf.createVariable("y");

        // Assuming t1.getId() < t2.getId() or vice versa
        ForbiddenSet.TermPair pair1 = new ForbiddenSet.TermPair(t1, t2);
        ForbiddenSet.TermPair pair2 = new ForbiddenSet.TermPair(t2, t1);

        // Both pairs should be equal due to canonical ordering
        assertEquals(pair1, pair2);
        assertEquals(pair1.hashCode(), pair2.hashCode());
    }

    @Test
    @DisplayName("TermPair equality reflexive")
    void testTermPairEqualityReflexive() {
        ForbiddenSet.TermPair pair = new ForbiddenSet.TermPair(a, b);
        assertEquals(pair, pair);
    }

    @Test
    @DisplayName("TermPair equality symmetric")
    void testTermPairEqualitySymmetric() {
        ForbiddenSet.TermPair pair1 = new ForbiddenSet.TermPair(a, b);
        ForbiddenSet.TermPair pair2 = new ForbiddenSet.TermPair(a, b);

        assertEquals(pair1, pair2);
        assertEquals(pair2, pair1);
    }

    @Test
    @DisplayName("TermPair equality transitive")
    void testTermPairEqualityTransitive() {
        ForbiddenSet.TermPair pair1 = new ForbiddenSet.TermPair(a, b);
        ForbiddenSet.TermPair pair2 = new ForbiddenSet.TermPair(b, a);
        ForbiddenSet.TermPair pair3 = new ForbiddenSet.TermPair(a, b);

        assertEquals(pair1, pair2);
        assertEquals(pair2, pair3);
        assertEquals(pair1, pair3);
    }

    @Test
    @DisplayName("TermPair inequality for different terms")
    void testTermPairInequality() {
        ForbiddenSet.TermPair pair1 = new ForbiddenSet.TermPair(a, b);
        ForbiddenSet.TermPair pair2 = new ForbiddenSet.TermPair(c, d);

        assertNotEquals(pair1, pair2);
    }

    @Test
    @DisplayName("TermPair not equal to null")
    void testTermPairNotEqualToNull() {
        ForbiddenSet.TermPair pair = new ForbiddenSet.TermPair(a, b);
        assertNotEquals(pair, null);
    }

    @Test
    @DisplayName("TermPair not equal to different type")
    void testTermPairNotEqualToDifferentType() {
        ForbiddenSet.TermPair pair = new ForbiddenSet.TermPair(a, b);
        assertNotEquals(pair, "not a term pair");
    }

    @Test
    @DisplayName("TermPair toString")
    void testTermPairToString() {
        ForbiddenSet.TermPair pair = new ForbiddenSet.TermPair(a, b);
        String str = pair.toString();

        assertNotNull(str);
        assertTrue(str.contains("a") || str.contains("b"));
    }

    @Test
    @DisplayName("ForbiddenSet toString")
    void testForbiddenSetToString() {
        forbiddenSet.addForbiddenPair(a, b);
        String str = forbiddenSet.toString();

        assertNotNull(str);
        assertTrue(str.contains("1") || str.contains("size"));
    }

    @Test
    @DisplayName("Large forbidden set performance")
    void testLargeForbiddenSet() {
        TermFactory tf = new TermFactory();
        // Add many forbidden pairs
        for (int i = 0; i < 100; i++) {
            Term t1 = tf.createVariable("t" + i);
            Term t2 = tf.createVariable("t" + (i + 1));
            forbiddenSet.addForbiddenPair(t1, t2);
        }

        assertEquals(100, forbiddenSet.size());
        assertEquals(100, forbiddenSet.getForbiddenPairsAdded());
    }
}
