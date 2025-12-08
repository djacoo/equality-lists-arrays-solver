package solver.theory.te;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import solver.config.SolverConfig;
import solver.dag.Term;
import solver.dag.TermFactory;
import solver.theory.Result;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration tests for T_E-procedure with forbidden set optimization.
 * Tests early UNSAT detection capabilities.
 */
@DisplayName("T_E-procedure with Forbidden Set Tests")
public class TEProcedureForbiddenSetTest {

    private TermFactory factory;

    @org.junit.jupiter.api.BeforeEach
    void setUp() {
        factory = new TermFactory();
    }

    @Test
    @DisplayName("Baseline: Simple UNSAT without forbidden set")
    void testSimpleUnsatBaseline() {
        TEProcedure solver = new TEProcedure();  // Default config (no forbidden set)

        Term a = factory.createVariable("a");
        Term b = factory.createVariable("b");

        List<Literal> literals = Arrays.asList(
            Literal.equality(a, b),   // a = b
            Literal.disequality(a, b)   // a != b
        );

        Result result = solver.checkSat(literals);
        assertTrue(result.isUnsat());
    }

    @Test
    @DisplayName("Forbidden set: Early UNSAT detection for simple conflict")
    void testSimpleUnsatWithForbiddenSet() {
        SolverConfig config = SolverConfig.withForbiddenSet();
        TEProcedure solver = new TEProcedure(config);

        Term a = factory.createVariable("a");
        Term b = factory.createVariable("b");

        List<Literal> literals = Arrays.asList(
            Literal.equality(a, b),   // a = b
            Literal.disequality(a, b)   // a != b
        );

        Result result = solver.checkSat(literals);
        assertTrue(result.isUnsat());
        assertTrue(result.getConflict().isPresent());
        String conflict = result.getConflict().get();
        assertTrue(conflict.contains("early detection") ||
                   conflict.contains("conflicts"));
    }

    @Test
    @DisplayName("Forbidden set: Transitive UNSAT detection")
    void testTransitiveUnsatWithForbiddenSet() {
        SolverConfig config = SolverConfig.withForbiddenSet();
        TEProcedure solver = new TEProcedure(config);

        Term a = factory.createVariable("a");
        Term b = factory.createVariable("b");
        Term c = factory.createVariable("c");

        List<Literal> literals = Arrays.asList(
            Literal.equality(a, b),   // a = b
            Literal.equality(b, c),   // b = c
            Literal.disequality(a, c)   // a != c (but a = b = c!)
        );

        Result result = solver.checkSat(literals);
        assertTrue(result.isUnsat());
    }

    @Test
    @DisplayName("Forbidden set: SAT case with disequality")
    void testSatWithDisequality() {
        SolverConfig config = SolverConfig.withForbiddenSet();
        TEProcedure solver = new TEProcedure(config);

        Term a = factory.createVariable("a");
        Term b = factory.createVariable("b");
        Term c = factory.createVariable("c");

        List<Literal> literals = Arrays.asList(
            Literal.equality(a, b),   // a = b
            Literal.disequality(a, c)   // a != c (consistent, c is separate)
        );

        Result result = solver.checkSat(literals);
        assertTrue(result.isSat());
    }

    @Test
    @DisplayName("Forbidden set: Multiple disequalities SAT")
    void testMultipleDisequalities() {
        SolverConfig config = SolverConfig.withForbiddenSet();
        TEProcedure solver = new TEProcedure(config);

        Term a = factory.createVariable("a");
        Term b = factory.createVariable("b");
        Term c = factory.createVariable("c");
        Term d = factory.createVariable("d");

        List<Literal> literals = Arrays.asList(
            Literal.disequality(a, b),  // a != b
            Literal.disequality(c, d),  // c != d
            Literal.equality(a, c),   // a = c (fine, doesn't violate constraints)
            Literal.equality(b, d)    // b = d (fine, doesn't violate constraints)
        );

        Result result = solver.checkSat(literals);
        assertTrue(result.isSat());
    }

    @Test
    @DisplayName("Forbidden set: Complex UNSAT with multiple equalities")
    void testComplexUnsat() {
        SolverConfig config = SolverConfig.withForbiddenSet();
        TEProcedure solver = new TEProcedure(config);

        Term a = factory.createVariable("a");
        Term b = factory.createVariable("b");
        Term c = factory.createVariable("c");
        Term d = factory.createVariable("d");

        List<Literal> literals = Arrays.asList(
            Literal.equality(a, b),   // a = b
            Literal.equality(b, c),   // b = c
            Literal.equality(c, d),   // c = d
            Literal.disequality(a, d)   // a != d (but a = b = c = d!)
        );

        Result result = solver.checkSat(literals);
        assertTrue(result.isUnsat());
    }

    @Test
    @DisplayName("Forbidden set: Only disequalities (always SAT)")
    void testOnlyDisequalities() {
        SolverConfig config = SolverConfig.withForbiddenSet();
        TEProcedure solver = new TEProcedure(config);

        Term a = factory.createVariable("a");
        Term b = factory.createVariable("b");
        Term c = factory.createVariable("c");

        List<Literal> literals = Arrays.asList(
            Literal.disequality(a, b),  // a != b
            Literal.disequality(b, c),  // b != c
            Literal.disequality(a, c)   // a != c
        );

        Result result = solver.checkSat(literals);
        assertTrue(result.isSat());  // All distinct, always satisfiable
    }

    @Test
    @DisplayName("Forbidden set: Only equalities (always SAT)")
    void testOnlyEqualities() {
        SolverConfig config = SolverConfig.withForbiddenSet();
        TEProcedure solver = new TEProcedure(config);

        Term a = factory.createVariable("a");
        Term b = factory.createVariable("b");
        Term c = factory.createVariable("c");

        List<Literal> literals = Arrays.asList(
            Literal.equality(a, b),   // a = b
            Literal.equality(b, c)    // b = c
        );

        Result result = solver.checkSat(literals);
        assertTrue(result.isSat());  // All equal, always satisfiable
    }

    @Test
    @DisplayName("Forbidden set: Empty formula (always SAT)")
    void testEmptyFormula() {
        SolverConfig config = SolverConfig.withForbiddenSet();
        TEProcedure solver = new TEProcedure(config);

        List<Literal> literals = Arrays.asList();

        Result result = solver.checkSat(literals);
        assertTrue(result.isSat());  // Empty formula is satisfiable
    }

    @Test
    @DisplayName("Comparison: Same result with and without forbidden set")
    void testConsistencyBetweenConfigurations() {
        Term a = factory.createVariable("a");
        Term b = factory.createVariable("b");
        Term c = factory.createVariable("c");

        List<Literal> literals = Arrays.asList(
            Literal.equality(a, b),   // a = b
            Literal.equality(b, c),   // b = c
            Literal.disequality(a, c)   // a != c
        );

        // Baseline
        TEProcedure solver1 = new TEProcedure();
        Result result1 = solver1.checkSat(literals);

        // With forbidden set
        TEProcedure solver2 = new TEProcedure(SolverConfig.withForbiddenSet());
        Result result2 = solver2.checkSat(literals);

        // Both should detect UNSAT
        assertEquals(result1.isSat(), result2.isSat());
        assertEquals(result1.isUnsat(), result2.isUnsat());
    }

    @Test
    @DisplayName("Forbidden set: Verify CongruenceClosure uses config")
    void testCongruenceClosureUsesConfig() {
        SolverConfig config = SolverConfig.withForbiddenSet();
        TEProcedure solver = new TEProcedure(config);

        Term a = factory.createVariable("a");
        Term b = factory.createVariable("b");

        List<Literal> literals = Arrays.asList(
            Literal.equality(a, b),
            Literal.disequality(a, b)
        );

        solver.checkSat(literals);

        // Verify that CongruenceClosure was created with config
        assertNotNull(solver.getCongruenceClosure());
        assertEquals(config.isUseForbiddenSet(),
                     solver.getCongruenceClosure().getConfig().isUseForbiddenSet());
    }

    @Test
    @DisplayName("Forbidden set: Statistics collection")
    void testStatisticsCollection() {
        SolverConfig config = SolverConfig.withForbiddenSet();
        TEProcedure solver = new TEProcedure(config);

        Term a = factory.createVariable("a");
        Term b = factory.createVariable("b");
        Term c = factory.createVariable("c");

        List<Literal> literals = Arrays.asList(
            Literal.equality(a, b),
            Literal.equality(b, c),
            Literal.disequality(a, c)
        );

        solver.checkSat(literals);

        // Verify statistics are available
        String stats = solver.getCongruenceClosure().getStatistics();
        assertNotNull(stats);
        assertTrue(stats.contains("Disequalities registered") ||
                   stats.contains("Statistics"));
    }

    @Test
    @DisplayName("Forbidden set: Large formula with many disequalities")
    void testLargeFormulaWithManyDisequalities() {
        SolverConfig config = SolverConfig.withForbiddenSet();
        TEProcedure solver = new TEProcedure(config);

        // Create 10 variables, all must be distinct
        Term[] vars = new Term[10];
        for (int i = 0; i < 10; i++) {
            vars[i] = factory.createVariable("v" + i);
        }

        java.util.List<Literal> literals = new java.util.ArrayList<>();

        // Add disequalities for all pairs
        for (int i = 0; i < 10; i++) {
            for (int j = i + 1; j < 10; j++) {
                literals.add(Literal.disequality(vars[i], vars[j]));
            }
        }

        Result result = solver.checkSat(literals);
        assertTrue(result.isSat());  // All distinct is satisfiable
    }

    @Test
    @DisplayName("Forbidden set: UNSAT in large formula")
    void testUnsatInLargeFormula() {
        SolverConfig config = SolverConfig.withForbiddenSet();
        TEProcedure solver = new TEProcedure(config);

        // Create chain: a = b = c = d = e, but a != e
        Term[] vars = new Term[5];
        for (int i = 0; i < 5; i++) {
            vars[i] = factory.createVariable("v" + i);
        }

        java.util.List<Literal> literals = new java.util.ArrayList<>();

        // Chain equalities
        for (int i = 0; i < 4; i++) {
            literals.add(Literal.equality(vars[i], vars[i + 1]));
        }

        // Add conflicting disequality
        literals.add(Literal.disequality(vars[0], vars[4]));

        Result result = solver.checkSat(literals);
        assertTrue(result.isUnsat());
    }
}
