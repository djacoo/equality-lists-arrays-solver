package solver.theory.te;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import solver.config.SolverConfig;
import solver.dag.Term;
import solver.dag.TermFactory;
import solver.theory.Result;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration tests for T_E-procedure with path compression optimization.
 *
 * Tests the combination of path compression (Phase 5.2) with the Theory of Equality.
 */
@DisplayName("T_E-procedure with Path Compression Tests")
public class TEProcedurePathCompressionTest {

    private TermFactory factory;

    @BeforeEach
    void setUp() {
        factory = new TermFactory();
    }

    @Test
    @DisplayName("Path compression works correctly with T_E SAT case")
    void testPathCompressionSAT() {
        SolverConfig config = SolverConfig.withNonRecursiveFIND();
        TEProcedure solver = new TEProcedure(config);

        // Create literals: a = b, b = c, c = d
        Term a = factory.createVariable("a");
        Term b = factory.createVariable("b");
        Term c = factory.createVariable("c");
        Term d = factory.createVariable("d");

        List<Literal> literals = List.of(
            Literal.equality(a, b),
            Literal.equality(b, c),
            Literal.equality(c, d)
        );

        Result result = solver.checkSat(literals);

        assertTrue(result.isSat());
        assertTrue(result.getWitness().isPresent());
        assertEquals(1, result.getWitness().get().size());
    }

    @Test
    @DisplayName("Path compression works correctly with T_E UNSAT case")
    void testPathCompressionUNSAT() {
        SolverConfig config = SolverConfig.withNonRecursiveFIND();
        TEProcedure solver = new TEProcedure(config);

        // Create literals: a = b, b = c, c != a (contradiction)
        Term a = factory.createVariable("a");
        Term b = factory.createVariable("b");
        Term c = factory.createVariable("c");

        List<Literal> literals = List.of(
            Literal.equality(a, b),
            Literal.equality(b, c),
            Literal.disequality(c, a)
        );

        Result result = solver.checkSat(literals);

        assertFalse(result.isSat());
        assertNotNull(result.getConflict());
    }

    @Test
    @DisplayName("Path compression with congruence closure")
    void testPathCompressionWithCongruence() {
        SolverConfig config = SolverConfig.withNonRecursiveFIND();
        TEProcedure solver = new TEProcedure(config);

        // a = b, and also assert f(a) = f(b) explicitly to ensure they're in the DAG
        Term a = factory.createVariable("a");
        Term b = factory.createVariable("b");
        Term fa = factory.createFunctionApp("f", a);
        Term fb = factory.createFunctionApp("f", b);

        List<Literal> literals = List.of(
            Literal.equality(a, b),
            Literal.equality(fa, fb)  // Explicitly assert to ensure terms are in DAG
        );

        Result result = solver.checkSat(literals);

        assertTrue(result.isSat());
        // a and b should be in same class, and f(a) and f(b) should be too
    }

    @Test
    @DisplayName("Path compression detects congruence violations")
    void testPathCompressionCongruenceViolation() {
        SolverConfig config = SolverConfig.withNonRecursiveFIND();
        TEProcedure solver = new TEProcedure(config);

        // a = b but f(a) != f(b) is a contradiction
        Term a = factory.createVariable("a");
        Term b = factory.createVariable("b");
        Term fa = factory.createFunctionApp("f", a);
        Term fb = factory.createFunctionApp("f", b);

        List<Literal> literals = List.of(
            Literal.equality(a, b),
            Literal.disequality(fa, fb)
        );

        Result result = solver.checkSat(literals);

        assertFalse(result.isSat());
    }

    @Test
    @DisplayName("Path compression + forbidden set combination (both optimizations)")
    void testBothOptimizations() {
        SolverConfig config = SolverConfig.allOptimizations();
        TEProcedure solver = new TEProcedure(config);

        // Test early UNSAT detection with path compression
        Term a = factory.createVariable("a");
        Term b = factory.createVariable("b");
        Term c = factory.createVariable("c");

        List<Literal> literals = List.of(
            Literal.disequality(a, b),  // Forbidden set will register this
            Literal.equality(a, c),
            Literal.equality(c, b)      // This should trigger forbidden merge
        );

        Result result = solver.checkSat(literals);

        assertFalse(result.isSat());
        assertNotNull(result.getConflict());
    }

    @Test
    @DisplayName("Complex formula with path compression")
    void testComplexFormulaWithPathCompression() {
        SolverConfig config = SolverConfig.withNonRecursiveFIND();
        TEProcedure solver = new TEProcedure(config);

        // Large formula with multiple equivalence classes
        List<Literal> literals = new ArrayList<>();

        // Class 1: a = b = c = d = e
        Term a = factory.createVariable("a");
        Term b = factory.createVariable("b");
        Term c = factory.createVariable("c");
        Term d = factory.createVariable("d");
        Term e = factory.createVariable("e");

        literals.add(Literal.equality(a, b));
        literals.add(Literal.equality(b, c));
        literals.add(Literal.equality(c, d));
        literals.add(Literal.equality(d, e));

        // Class 2: x = y = z
        Term x = factory.createVariable("x");
        Term y = factory.createVariable("y");
        Term z = factory.createVariable("z");

        literals.add(Literal.equality(x, y));
        literals.add(Literal.equality(y, z));

        // Disequalities between classes
        literals.add(Literal.disequality(a, x));
        literals.add(Literal.disequality(c, y));

        Result result = solver.checkSat(literals);

        assertTrue(result.isSat());
        assertTrue(result.getWitness().isPresent());
        assertEquals(2, result.getWitness().get().size());
    }

    @Test
    @DisplayName("Path compression baseline vs optimized give same results")
    void testBaselineVsOptimizedEquivalence() {
        TEProcedure baseline = new TEProcedure(SolverConfig.baseline());
        TEProcedure optimized = new TEProcedure(SolverConfig.withNonRecursiveFIND());

        // Create same literals for both
        Term a1 = factory.createVariable("a1");
        Term b1 = factory.createVariable("b1");
        Term c1 = factory.createVariable("c1");
        Term fa1 = factory.createFunctionApp("f", a1);
        Term fb1 = factory.createFunctionApp("f", b1);

        List<Literal> literals = List.of(
            Literal.equality(a1, b1),
            Literal.equality(b1, c1),
            Literal.disequality(fa1, fb1)  // This should cause UNSAT
        );

        Result baselineResult = baseline.checkSat(literals);
        Result optimizedResult = optimized.checkSat(literals);

        // Both should give same SAT/UNSAT result
        assertEquals(baselineResult.isSat(), optimizedResult.isSat());
        assertFalse(baselineResult.isSat());  // Should be UNSAT
    }

    @Test
    @DisplayName("Nested functions with path compression")
    void testNestedFunctionsWithPathCompression() {
        SolverConfig config = SolverConfig.withNonRecursiveFIND();
        TEProcedure solver = new TEProcedure(config);

        // f(f(a)) = a, f(f(f(a))) = a implies f(a) = a (Bradley & Manna Example)
        Term a = factory.createVariable("a");
        Term fa = factory.createFunctionApp("f", a);
        Term ffa = factory.createFunctionApp("f", fa);
        Term fffa = factory.createFunctionApp("f", ffa);

        List<Literal> literals = List.of(
            Literal.equality(ffa, a),
            Literal.equality(fffa, a),
            Literal.disequality(fa, a)  // This contradicts the implied f(a) = a
        );

        Result result = solver.checkSat(literals);

        assertFalse(result.isSat());
    }

    @Test
    @DisplayName("Path compression handles empty literal set")
    void testEmptyLiteralsWithPathCompression() {
        SolverConfig config = SolverConfig.withNonRecursiveFIND();
        TEProcedure solver = new TEProcedure(config);

        Result result = solver.checkSat(List.of());

        assertTrue(result.isSat());
    }

    @Test
    @DisplayName("Path compression with binary functions")
    void testBinaryFunctionsWithPathCompression() {
        SolverConfig config = SolverConfig.withNonRecursiveFIND();
        TEProcedure solver = new TEProcedure(config);

        Term a = factory.createVariable("a");
        Term b = factory.createVariable("b");
        Term c = factory.createVariable("c");
        Term d = factory.createVariable("d");

        Term fab = factory.createFunctionApp("f", a, b);
        Term fcd = factory.createFunctionApp("f", c, d);

        List<Literal> literals = List.of(
            Literal.equality(a, c),
            Literal.equality(b, d),
            // By congruence, f(a,b) = f(c,d)
            Literal.disequality(fab, fcd)  // Contradiction
        );

        Result result = solver.checkSat(literals);

        assertFalse(result.isSat());
    }
}
