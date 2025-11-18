package solver.dag;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for DAG container and operations.
 */
public class DAGTest {
    private DAG dag;
    private TermFactory factory;

    @BeforeEach
    public void setUp() {
        dag = new DAG();
        factory = dag.getFactory();
    }

    @Test
    public void testEmptyDAG() {
        assertEquals(0, dag.size());
        assertEquals(0, dag.getVariables().size());
        assertEquals(0, dag.getConstants().size());
        assertEquals(0, dag.getFunctionApps().size());
    }

    @Test
    public void testAddVariable() {
        Variable x = factory.createVariable("x");
        dag.addTerm(x);

        assertEquals(1, dag.size());
        assertEquals(1, dag.getVariables().size());
        assertTrue(dag.getTerms().contains(x));
    }

    @Test
    public void testAddFunctionWithArguments() {
        Variable x = factory.createVariable("x");
        Variable y = factory.createVariable("y");
        FunctionApp f = factory.createFunctionApp("f", x, y);

        dag.addTerm(f);

        // Should add f, x, and y
        assertEquals(3, dag.size());
        assertEquals(2, dag.getVariables().size());
        assertEquals(1, dag.getFunctionApps().size());
    }

    @Test
    public void testTopologicalOrder() {
        Variable a = factory.createVariable("a");
        FunctionApp fa = factory.createFunctionApp("f", a);
        FunctionApp gfa = factory.createFunctionApp("g", fa);

        dag.addTerm(gfa);

        // Should be in order: a, f(a), g(f(a))
        assertEquals(3, dag.size());

        // Verify leaves come before internal nodes
        var terms = dag.getTerms();
        int aIndex = terms.indexOf(a);
        int faIndex = terms.indexOf(fa);
        int gfaIndex = terms.indexOf(gfa);

        assertTrue(aIndex < faIndex, "Leaf 'a' should come before 'f(a)'");
        assertTrue(faIndex < gfaIndex, "'f(a)' should come before 'g(f(a))'");
    }

    @Test
    public void testNoDuplicates() {
        Variable x = factory.createVariable("x");

        dag.addTerm(x);
        dag.addTerm(x);
        dag.addTerm(x);

        assertEquals(1, dag.size(), "Same term added multiple times should only appear once");
    }

    @Test
    public void testGetVariables() {
        Variable x = factory.createVariable("x");
        Variable y = factory.createVariable("y");
        FunctionApp f = factory.createFunctionApp("f", x, y);

        dag.addTerm(f);

        var vars = dag.getVariables();
        assertEquals(2, vars.size());
        assertTrue(vars.contains(x));
        assertTrue(vars.contains(y));
    }

    @Test
    public void testGetFunctionApps() {
        Variable x = factory.createVariable("x");
        FunctionApp fx = factory.createFunctionApp("f", x);
        FunctionApp gfx = factory.createFunctionApp("g", fx);

        dag.addTerm(gfx);

        var funcs = dag.getFunctionApps();
        assertEquals(2, funcs.size());
        assertTrue(funcs.contains(fx));
        assertTrue(funcs.contains(gfx));
    }

    @Test
    public void testToString() {
        Variable x = factory.createVariable("x");
        dag.addTerm(x);

        String str = dag.toString();
        assertTrue(str.contains("1 terms"));
        assertTrue(str.contains("1 variables"));
    }

    @Test
    public void testDotGeneration() {
        Variable a = factory.createVariable("a");
        FunctionApp fa = factory.createFunctionApp("f", a);

        dag.addTerm(fa);

        String dot = dag.toDot();

        // Should contain graph structure
        assertTrue(dot.contains("digraph DAG"));
        assertTrue(dot.contains("t" + a.getId()));
        assertTrue(dot.contains("t" + fa.getId()));
        // Should have edge from f to a
        assertTrue(dot.contains("->"));
    }
}
