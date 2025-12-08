package solver.config;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for the SolverConfig class.
 * Tests configuration options and factory methods.
 */
@DisplayName("SolverConfig Tests")
public class SolverConfigTest {

    @Test
    @DisplayName("Default configuration has no optional optimizations enabled")
    void testDefaultConfiguration() {
        SolverConfig config = new SolverConfig();

        assertTrue(config.isUseLargestCcpar());  // Mandatory, always true
        assertFalse(config.isUseForbiddenSet());
        assertFalse(config.isUseNonRecursiveFIND());
        assertFalse(config.isCollectStatistics());
        assertFalse(config.isVerboseOutput());
    }

    @Test
    @DisplayName("Baseline configuration factory method")
    void testBaselineConfiguration() {
        SolverConfig config = SolverConfig.baseline();

        assertTrue(config.isUseLargestCcpar());
        assertFalse(config.isUseForbiddenSet());
        assertFalse(config.isUseNonRecursiveFIND());
    }

    @Test
    @DisplayName("Forbidden set configuration factory method")
    void testWithForbiddenSetConfiguration() {
        SolverConfig config = SolverConfig.withForbiddenSet();

        assertTrue(config.isUseLargestCcpar());
        assertTrue(config.isUseForbiddenSet());
        assertFalse(config.isUseNonRecursiveFIND());
    }

    @Test
    @DisplayName("Non-recursive FIND configuration factory method")
    void testWithNonRecursiveFINDConfiguration() {
        SolverConfig config = SolverConfig.withNonRecursiveFIND();

        assertTrue(config.isUseLargestCcpar());
        assertFalse(config.isUseForbiddenSet());
        assertTrue(config.isUseNonRecursiveFIND());
    }

    @Test
    @DisplayName("All optimizations configuration factory method")
    void testAllOptimizationsConfiguration() {
        SolverConfig config = SolverConfig.allOptimizations();

        assertTrue(config.isUseLargestCcpar());
        assertTrue(config.isUseForbiddenSet());
        assertTrue(config.isUseNonRecursiveFIND());
    }

    @Test
    @DisplayName("Statistics configuration factory method")
    void testWithStatisticsConfiguration() {
        SolverConfig config = SolverConfig.withStatistics();

        assertTrue(config.isUseLargestCcpar());
        assertTrue(config.isCollectStatistics());
    }

    @Test
    @DisplayName("Set and get forbidden set option")
    void testSetUseForbiddenSet() {
        SolverConfig config = new SolverConfig();
        assertFalse(config.isUseForbiddenSet());

        config.setUseForbiddenSet(true);
        assertTrue(config.isUseForbiddenSet());

        config.setUseForbiddenSet(false);
        assertFalse(config.isUseForbiddenSet());
    }

    @Test
    @DisplayName("Set and get non-recursive FIND option")
    void testSetUseNonRecursiveFIND() {
        SolverConfig config = new SolverConfig();
        assertFalse(config.isUseNonRecursiveFIND());

        config.setUseNonRecursiveFIND(true);
        assertTrue(config.isUseNonRecursiveFIND());

        config.setUseNonRecursiveFIND(false);
        assertFalse(config.isUseNonRecursiveFIND());
    }

    @Test
    @DisplayName("Set and get collect statistics option")
    void testSetCollectStatistics() {
        SolverConfig config = new SolverConfig();
        assertFalse(config.isCollectStatistics());

        config.setCollectStatistics(true);
        assertTrue(config.isCollectStatistics());

        config.setCollectStatistics(false);
        assertFalse(config.isCollectStatistics());
    }

    @Test
    @DisplayName("Set and get verbose output option")
    void testSetVerboseOutput() {
        SolverConfig config = new SolverConfig();
        assertFalse(config.isVerboseOutput());

        config.setVerboseOutput(true);
        assertTrue(config.isVerboseOutput());

        config.setVerboseOutput(false);
        assertFalse(config.isVerboseOutput());
    }

    @Test
    @DisplayName("Copy constructor creates independent copy")
    void testCopyConstructor() {
        SolverConfig original = new SolverConfig();
        original.setUseForbiddenSet(true);
        original.setCollectStatistics(true);

        SolverConfig copy = new SolverConfig(original);

        assertTrue(copy.isUseForbiddenSet());
        assertTrue(copy.isCollectStatistics());

        // Modify copy - should not affect original
        copy.setUseForbiddenSet(false);
        assertTrue(original.isUseForbiddenSet());
        assertFalse(copy.isUseForbiddenSet());
    }

    @Test
    @DisplayName("Configuration description for baseline")
    void testDescriptionBaseline() {
        SolverConfig config = SolverConfig.baseline();
        String desc = config.getDescription();

        assertNotNull(desc);
        assertTrue(desc.contains("Largest ccpar"));
        assertTrue(desc.contains("OFF") || desc.contains("Optional optimizations"));
    }

    @Test
    @DisplayName("Configuration description with forbidden set")
    void testDescriptionWithForbiddenSet() {
        SolverConfig config = SolverConfig.withForbiddenSet();
        String desc = config.getDescription();

        assertNotNull(desc);
        assertTrue(desc.contains("Largest ccpar"));
        assertTrue(desc.contains("Forbidden set"));
    }

    @Test
    @DisplayName("Configuration description with all optimizations")
    void testDescriptionAllOptimizations() {
        SolverConfig config = SolverConfig.allOptimizations();
        String desc = config.getDescription();

        assertNotNull(desc);
        assertTrue(desc.contains("Largest ccpar"));
        assertTrue(desc.contains("Forbidden set"));
        assertTrue(desc.contains("Non-recursive FIND"));
    }

    @Test
    @DisplayName("toString produces non-null string")
    void testToString() {
        SolverConfig config = new SolverConfig();
        String str = config.toString();

        assertNotNull(str);
        assertTrue(str.contains("SolverConfig"));
    }

    @Test
    @DisplayName("Largest ccpar is always enabled")
    void testLargestCcparAlwaysEnabled() {
        SolverConfig config = new SolverConfig();
        assertTrue(config.isUseLargestCcpar());

        // Try different configurations
        config.setUseForbiddenSet(true);
        assertTrue(config.isUseLargestCcpar());

        config.setUseNonRecursiveFIND(true);
        assertTrue(config.isUseLargestCcpar());
    }

    @Test
    @DisplayName("Multiple configuration changes")
    void testMultipleConfigurationChanges() {
        SolverConfig config = new SolverConfig();

        config.setUseForbiddenSet(true);
        config.setUseNonRecursiveFIND(true);
        config.setCollectStatistics(true);
        config.setVerboseOutput(true);

        assertTrue(config.isUseForbiddenSet());
        assertTrue(config.isUseNonRecursiveFIND());
        assertTrue(config.isCollectStatistics());
        assertTrue(config.isVerboseOutput());
    }
}
