package solver.config;

/**
 * Configuration options for the solver.
 *
 * Controls which optimizations are enabled:
 * - Mandatory: Largest ccpar optimization (always enabled)
 * - Optional: Forbidden list/set optimization
 * - Optional: Non-recursive FIND optimization
 *
 * Provides factory methods for common configurations.
 */
public class SolverConfig {
    // Mandatory optimization (always enabled, cannot be disabled)
    private final boolean useLargestCcpar = true;

    // Optional optimizations (configurable)
    private boolean useForbiddenSet;
    private boolean useNonRecursiveFIND;

    // Debug and statistics options
    private boolean collectStatistics;
    private boolean verboseOutput;

    /**
     * Creates a default configuration with no optional optimizations enabled.
     */
    public SolverConfig() {
        this.useForbiddenSet = false;
        this.useNonRecursiveFIND = false;
        this.collectStatistics = false;
        this.verboseOutput = false;
    }

    /**
     * Copy constructor.
     */
    public SolverConfig(SolverConfig other) {
        this.useForbiddenSet = other.useForbiddenSet;
        this.useNonRecursiveFIND = other.useNonRecursiveFIND;
        this.collectStatistics = other.collectStatistics;
        this.verboseOutput = other.verboseOutput;
    }

    // ========== Factory Methods ==========

    /**
     * Creates baseline configuration with only mandatory optimizations.
     * This is the default configuration matching the textbook algorithm.
     */
    public static SolverConfig baseline() {
        return new SolverConfig();
    }

    /**
     * Creates configuration with forbidden set optimization enabled.
     * Good for problems with many disequalities or likely UNSAT instances.
     */
    public static SolverConfig withForbiddenSet() {
        SolverConfig config = new SolverConfig();
        config.setUseForbiddenSet(true);
        return config;
    }

    /**
     * Creates configuration with non-recursive FIND enabled.
     * Good for FIND-heavy workloads.
     */
    public static SolverConfig withNonRecursiveFIND() {
        SolverConfig config = new SolverConfig();
        config.setUseNonRecursiveFIND(true);
        return config;
    }

    /**
     * Creates configuration with all optional optimizations enabled.
     */
    public static SolverConfig allOptimizations() {
        SolverConfig config = new SolverConfig();
        config.setUseForbiddenSet(true);
        config.setUseNonRecursiveFIND(true);
        return config;
    }

    /**
     * Creates configuration with statistics collection enabled.
     */
    public static SolverConfig withStatistics() {
        SolverConfig config = new SolverConfig();
        config.setCollectStatistics(true);
        return config;
    }

    // ========== Getters and Setters ==========

    public boolean isUseLargestCcpar() {
        return useLargestCcpar;
    }

    public boolean isUseForbiddenSet() {
        return useForbiddenSet;
    }

    public void setUseForbiddenSet(boolean useForbiddenSet) {
        this.useForbiddenSet = useForbiddenSet;
    }

    public boolean isUseNonRecursiveFIND() {
        return useNonRecursiveFIND;
    }

    public void setUseNonRecursiveFIND(boolean useNonRecursiveFIND) {
        this.useNonRecursiveFIND = useNonRecursiveFIND;
    }

    public boolean isCollectStatistics() {
        return collectStatistics;
    }

    public void setCollectStatistics(boolean collectStatistics) {
        this.collectStatistics = collectStatistics;
    }

    public boolean isVerboseOutput() {
        return verboseOutput;
    }

    public void setVerboseOutput(boolean verboseOutput) {
        this.verboseOutput = verboseOutput;
    }

    // ========== Utility Methods ==========

    /**
     * Returns a human-readable description of the enabled optimizations.
     */
    public String getDescription() {
        StringBuilder sb = new StringBuilder();
        sb.append("Configuration: ");
        sb.append("[Largest ccpar: ON (mandatory)]");
        if (useForbiddenSet) {
            sb.append(" [Forbidden set: ON]");
        }
        if (useNonRecursiveFIND) {
            sb.append(" [Non-recursive FIND: ON]");
        }
        if (!useForbiddenSet && !useNonRecursiveFIND) {
            sb.append(" [Optional optimizations: OFF]");
        }
        return sb.toString();
    }

    @Override
    public String toString() {
        return String.format(
            "SolverConfig{largestCcpar=%b, forbiddenSet=%b, nonRecursiveFIND=%b, stats=%b}",
            useLargestCcpar, useForbiddenSet, useNonRecursiveFIND, collectStatistics
        );
    }
}
