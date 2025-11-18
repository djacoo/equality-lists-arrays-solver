package solver.equivalence;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import static org.junit.jupiter.api.Assertions.*;

import solver.dag.*;

/**
 * Tests for ClassManager FIND and UNION operations.
 */
public class ClassManagerTest {
    private TermFactory factory;
    private ClassManager classManager;

    @BeforeEach
    public void setUp() {
        factory = new TermFactory();
        classManager = new ClassManager();
    }

    @Test
    public void testInitialize() {
        Variable x = factory.createVariable("x");
        classManager.initialize(x);

        // x should be in its own singleton class
        assertEquals(x, classManager.find(x));
        assertEquals(1, classManager.getClassCount());
    }

    @Test
    public void testInitializeMultiple() {
        Variable x = factory.createVariable("x");
        Variable y = factory.createVariable("y");
        Variable z = factory.createVariable("z");

        classManager.initializeAll(java.util.Arrays.asList(x, y, z));

        assertEquals(3, classManager.getClassCount());
        assertEquals(x, classManager.find(x));
        assertEquals(y, classManager.find(y));
        assertEquals(z, classManager.find(z));
    }

    @Test
    public void testUnionSimple() {
        Variable a = factory.createVariable("a");
        Variable b = factory.createVariable("b");

        classManager.initialize(a);
        classManager.initialize(b);

        assertFalse(classManager.areEqual(a, b));

        classManager.union(a, b);

        // After union, they should be in the same class
        assertTrue(classManager.areEqual(a, b));
        assertEquals(1, classManager.getClassCount());

        // They should have the same representative
        assertEquals(classManager.find(a), classManager.find(b));
    }

    @Test
    public void testUnionTransitivity() {
        Variable a = factory.createVariable("a");
        Variable b = factory.createVariable("b");
        Variable c = factory.createVariable("c");

        classManager.initializeAll(java.util.Arrays.asList(a, b, c));

        classManager.union(a, b);
        classManager.union(b, c);

        // a, b, c should all be in the same class
        assertTrue(classManager.areEqual(a, b));
        assertTrue(classManager.areEqual(b, c));
        assertTrue(classManager.areEqual(a, c));
        assertEquals(1, classManager.getClassCount());
    }

    @Test
    public void testUnionIdempotent() {
        Variable a = factory.createVariable("a");
        Variable b = factory.createVariable("b");

        classManager.initialize(a);
        classManager.initialize(b);

        classManager.union(a, b);
        int classesAfterFirst = classManager.getClassCount();

        classManager.union(a, b);  // Union again
        int classesAfterSecond = classManager.getClassCount();

        assertEquals(classesAfterFirst, classesAfterSecond);
    }

    @Test
    public void testLargestCcparOptimization() {
        // Create terms where x has larger ccpar than y
        Variable x = factory.createVariable("x");
        Variable y = factory.createVariable("y");

        // Make x an argument to multiple functions
        FunctionApp fx = factory.createFunctionApp("f", x);
        FunctionApp gx = factory.createFunctionApp("g", x);

        // Make y an argument to only one function
        FunctionApp hy = factory.createFunctionApp("h", y);

        // x.ccpar = {f(x), g(x)} size 2
        // y.ccpar = {h(y)} size 1
        assertEquals(2, x.getCcparSize());
        assertEquals(1, y.getCcparSize());

        // Initialize and union
        classManager.initializeAll(java.util.Arrays.asList(x, y, fx, gx, hy));
        classManager.union(x, y);

        // Representative should be x (larger ccpar)
        Term rep = classManager.find(x);
        assertEquals(x, rep, "Representative should be x (larger ccpar set)");
        assertEquals(rep, classManager.find(y));
    }

    @Test
    public void testPathCompression() {
        Variable a = factory.createVariable("a");
        Variable b = factory.createVariable("b");
        Variable c = factory.createVariable("c");

        classManager.initializeAll(java.util.Arrays.asList(a, b, c));

        // Create chain: a -> b -> c
        classManager.union(a, b);
        classManager.union(b, c);

        // Find on a should compress the path
        Term rep = classManager.find(a);

        // After path compression, a should point directly to representative
        assertEquals(rep, a.getFind());
    }

    @Test
    public void testGetAllClasses() {
        Variable x = factory.createVariable("x");
        Variable y = factory.createVariable("y");
        Variable z = factory.createVariable("z");

        classManager.initializeAll(java.util.Arrays.asList(x, y, z));

        classManager.union(x, y);

        var classes = classManager.getAllClasses();
        assertEquals(2, classes.size(), "Should have 2 classes: {x,y} and {z}");
    }

    @Test
    public void testGetClass() {
        Variable x = factory.createVariable("x");
        Variable y = factory.createVariable("y");

        classManager.initialize(x);
        classManager.initialize(y);

        classManager.union(x, y);

        EquivalenceClass classX = classManager.getClass(x);
        EquivalenceClass classY = classManager.getClass(y);

        assertSame(classX, classY, "x and y should be in the same class");
        assertEquals(2, classX.size());
    }

    @Test
    public void testComplexUnionChain() {
        // Create: a=b, b=c, c=d, d=e
        Variable[] vars = new Variable[5];
        for (int i = 0; i < 5; i++) {
            vars[i] = factory.createVariable("v" + i);
        }

        classManager.initializeAll(java.util.Arrays.asList(vars));

        // Union in chain
        for (int i = 0; i < vars.length - 1; i++) {
            classManager.union(vars[i], vars[i + 1]);
        }

        // All should be in same class
        assertEquals(1, classManager.getClassCount());
        for (int i = 0; i < vars.length; i++) {
            for (int j = i + 1; j < vars.length; j++) {
                assertTrue(classManager.areEqual(vars[i], vars[j]));
            }
        }
    }
}
