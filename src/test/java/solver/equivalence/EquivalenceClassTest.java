package solver.equivalence;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import solver.dag.Term;
import solver.dag.Variable;

import java.util.Set;

/**
 * Unit tests for the EquivalenceClass class.
 */
public class EquivalenceClassTest {

    @Test
    public void testCreation() {
        Term x = new Variable(1, "x");
        EquivalenceClass eqClass = new EquivalenceClass(100, x);

        assertEquals(100, eqClass.getId());
        assertSame(x, eqClass.getRepresentative());
        assertEquals(1, eqClass.size());
        assertTrue(eqClass.contains(x));
    }

    @Test
    public void testInitialMembersContainsOnlyTerm() {
        Term x = new Variable(1, "x");
        EquivalenceClass eqClass = new EquivalenceClass(100, x);

        Set<Term> members = eqClass.getMembers();

        assertEquals(1, members.size());
        assertTrue(members.contains(x));
    }

    @Test
    public void testAddMember() {
        Term x = new Variable(1, "x");
        Term y = new Variable(2, "y");
        EquivalenceClass eqClass = new EquivalenceClass(100, x);

        eqClass.addMember(y);

        assertEquals(2, eqClass.size());
        assertTrue(eqClass.contains(x));
        assertTrue(eqClass.contains(y));
    }

    @Test
    public void testAddMultipleMembers() {
        Term x = new Variable(1, "x");
        Term y = new Variable(2, "y");
        Term z = new Variable(3, "z");
        EquivalenceClass eqClass = new EquivalenceClass(100, x);

        eqClass.addMember(y);
        eqClass.addMember(z);

        assertEquals(3, eqClass.size());
        assertTrue(eqClass.contains(x));
        assertTrue(eqClass.contains(y));
        assertTrue(eqClass.contains(z));
    }

    @Test
    public void testAddDuplicateMember() {
        Term x = new Variable(1, "x");
        EquivalenceClass eqClass = new EquivalenceClass(100, x);

        eqClass.addMember(x);

        // Adding same term again should not increase size
        assertEquals(1, eqClass.size());
    }

    @Test
    public void testAddAll() {
        Term x = new Variable(1, "x");
        Term y = new Variable(2, "y");
        EquivalenceClass class1 = new EquivalenceClass(100, x);
        EquivalenceClass class2 = new EquivalenceClass(101, y);

        class1.addAll(class2);

        assertEquals(2, class1.size());
        assertTrue(class1.contains(x));
        assertTrue(class1.contains(y));
    }

    @Test
    public void testAddAllMultipleMembers() {
        Term x = new Variable(1, "x");
        Term y = new Variable(2, "y");
        Term z = new Variable(3, "z");
        Term w = new Variable(4, "w");

        EquivalenceClass class1 = new EquivalenceClass(100, x);
        class1.addMember(y);

        EquivalenceClass class2 = new EquivalenceClass(101, z);
        class2.addMember(w);

        class1.addAll(class2);

        assertEquals(4, class1.size());
        assertTrue(class1.contains(x));
        assertTrue(class1.contains(y));
        assertTrue(class1.contains(z));
        assertTrue(class1.contains(w));
    }

    @Test
    public void testSetRepresentative() {
        Term x = new Variable(1, "x");
        Term y = new Variable(2, "y");
        EquivalenceClass eqClass = new EquivalenceClass(100, x);

        eqClass.setRepresentative(y);

        assertSame(y, eqClass.getRepresentative());
    }

    @Test
    public void testCcparSizeInitialized() {
        Term x = new Variable(1, "x");
        EquivalenceClass eqClass = new EquivalenceClass(100, x);

        assertEquals(x.getCcparSize(), eqClass.getCcparSize());
    }

    @Test
    public void testCcparSizeUpdatedOnSetRepresentative() {
        Term x = new Variable(1, "x");
        Term y = new Variable(2, "y");
        EquivalenceClass eqClass = new EquivalenceClass(100, x);

        eqClass.setRepresentative(y);

        assertEquals(y.getCcparSize(), eqClass.getCcparSize());
    }

    @Test
    public void testContains() {
        Term x = new Variable(1, "x");
        Term y = new Variable(2, "y");
        Term z = new Variable(3, "z");
        EquivalenceClass eqClass = new EquivalenceClass(100, x);

        assertTrue(eqClass.contains(x));
        assertFalse(eqClass.contains(y));

        eqClass.addMember(y);

        assertTrue(eqClass.contains(y));
        assertFalse(eqClass.contains(z));
    }

    @Test
    public void testGetMembersIsUnmodifiable() {
        Term x = new Variable(1, "x");
        EquivalenceClass eqClass = new EquivalenceClass(100, x);

        Set<Term> members = eqClass.getMembers();

        assertThrows(UnsupportedOperationException.class, () -> {
            members.add(new Variable(2, "y"));
        });
    }

    @Test
    public void testToString() {
        Term x = new Variable(1, "x");
        EquivalenceClass eqClass = new EquivalenceClass(100, x);

        String str = eqClass.toString();

        assertNotNull(str);
        assertTrue(str.contains("Class[100]"));
        assertTrue(str.contains("rep=x"));
        assertTrue(str.contains("size=1"));
    }

    @Test
    public void testToStringWithMultipleMembers() {
        Term x = new Variable(1, "x");
        Term y = new Variable(2, "y");
        EquivalenceClass eqClass = new EquivalenceClass(100, x);
        eqClass.addMember(y);

        String str = eqClass.toString();

        assertTrue(str.contains("size=2"));
        assertTrue(str.contains("x"));
        assertTrue(str.contains("y"));
    }

    @Test
    public void testInitialSizeIsOne() {
        Term x = new Variable(1, "x");
        EquivalenceClass eqClass = new EquivalenceClass(100, x);

        assertEquals(1, eqClass.size());
    }

    @Test
    public void testSizeAfterAdditions() {
        Term x = new Variable(1, "x");
        Term y = new Variable(2, "y");
        Term z = new Variable(3, "z");
        EquivalenceClass eqClass = new EquivalenceClass(100, x);

        assertEquals(1, eqClass.size());

        eqClass.addMember(y);
        assertEquals(2, eqClass.size());

        eqClass.addMember(z);
        assertEquals(3, eqClass.size());
    }
}
