package solver.equivalence;

import solver.config.SolverConfig;
import solver.dag.Term;

import java.util.*;

/**
 * Global manager for all equivalence classes.
 *
 * Provides the fundamental operations:
 * - FIND: Get the representative of a term's equivalence class
 * - UNION: Merge two equivalence classes
 *
 * Implements:
 * - Path compression in FIND for O(α(n)) amortized complexity
 * - Largest ccpar optimization in UNION (MANDATORY requirement)
 * - Optional: Forbidden set for early UNSAT detection
 */
public class ClassManager {
    // Maps each term to its current equivalence class
    private final Map<Term, EquivalenceClass> termToClass;

    // Counter for assigning unique class IDs
    private int nextClassId;

    // Optional: Forbidden set for early UNSAT detection
    private final ForbiddenSet forbiddenSet;
    private final boolean useForbiddenSet;

    // Optional: Non-recursive FIND with path compression
    private final boolean usePathCompression;

    // Statistics
    private int findOperations;
    private int unionOperations;
    private int forbiddenMergeAttempts;

    /**
     * Creates a ClassManager with default configuration (no optional optimizations).
     */
    public ClassManager() {
        this(new SolverConfig());
    }

    /**
     * Creates a ClassManager with the specified configuration.
     *
     * @param config Solver configuration specifying which optimizations to enable
     */
    public ClassManager(SolverConfig config) {
        this.termToClass = new HashMap<>();
        this.nextClassId = 1;
        this.findOperations = 0;
        this.unionOperations = 0;
        this.forbiddenMergeAttempts = 0;

        // Initialize forbidden set if enabled
        this.useForbiddenSet = config.isUseForbiddenSet();
        this.forbiddenSet = useForbiddenSet ? new ForbiddenSet() : null;

        // Enable path compression if non-recursive FIND is configured
        this.usePathCompression = config.isUseNonRecursiveFIND();
    }

    /**
     * Initializes a term with its own singleton equivalence class.
     */
    public void initialize(Term term) {
        if (!termToClass.containsKey(term)) {
            EquivalenceClass newClass = new EquivalenceClass(nextClassId++, term);
            termToClass.put(term, newClass);
            term.setFind(term);  // Initially points to self
        }
    }

    /**
     * Initializes all terms in a collection.
     */
    public void initializeAll(Collection<Term> terms) {
        for (Term term : terms) {
            initialize(term);
        }
    }

    /**
     * FIND: Returns the representative of the equivalence class containing t.
     *
     * If path compression is enabled (usePathCompression=true):
     * - Uses iterative algorithm with two-pass path compression
     * - Updates all find pointers along the path to point directly to representative
     * - Time complexity: O(α(n)) amortized, where α is inverse Ackermann function
     *
     * Otherwise (baseline):
     * - Simple iterative traversal without path compression
     * - Time complexity: O(h) where h is the height of the find tree
     *
     * @param t The term to find the representative for
     * @return The representative of t's equivalence class
     */
    public Term find(Term t) {
        findOperations++;

        if (usePathCompression) {
            // OPTIMIZED VERSION: Iterative with two-pass path compression
            // First pass: Follow find pointers to the representative
            Term current = t;
            while (current.getFind() != current) {
                current = current.getFind();
            }

            // Second pass: Path compression - update all find pointers along the path
            Term representative = current;
            current = t;
            while (current != representative) {
                Term next = current.getFind();
                current.setFind(representative);
                current = next;
            }

            return representative;
        } else {
            // BASELINE VERSION: Simple iterative traversal without path compression
            Term current = t;
            while (current.getFind() != current) {
                current = current.getFind();
            }
            return current;
        }
    }

    /**
     * Checks if two terms are in the same equivalence class.
     */
    public boolean areEqual(Term t1, Term t2) {
        return find(t1) == find(t2);
    }

    /**
     * Gets the equivalence class for a term.
     */
    public EquivalenceClass getClass(Term t) {
        Term representative = find(t);
        return termToClass.get(representative);
    }

    /**
     * Registers a disequality (t1 != t2) with the forbidden set.
     * This should be called for all disequality constraints before processing equalities.
     * Only has effect if forbidden set optimization is enabled.
     *
     * @param t1 First term
     * @param t2 Second term
     */
    public void addDisequality(Term t1, Term t2) {
        if (useForbiddenSet) {
            forbiddenSet.addForbiddenPair(t1, t2);
        }
    }

    /**
     * UNION: Merges the equivalence classes of t1 and t2.
     *
     * CRITICAL: Implements the MANDATORY largest ccpar optimization.
     * The representative is chosen from the class with the larger ccpar set.
     *
     * If forbidden set is enabled, checks if the merge is forbidden before proceeding.
     *
     * @param t1 First term
     * @param t2 Second term
     * @return true if merge succeeded, false if merge is forbidden (UNSAT detected)
     *
     * Time complexity: O(k) where k = size of smaller class
     */
    public boolean union(Term t1, Term t2) {
        unionOperations++;

        // Get representatives
        Term rep1 = find(t1);
        Term rep2 = find(t2);

        // Already in same class?
        if (rep1 == rep2) {
            return true;  // Success (no-op)
        }

        // OPTIONAL OPTIMIZATION: Check if merge is forbidden
        if (useForbiddenSet && forbiddenSet.isForbidden(rep1, rep2)) {
            forbiddenMergeAttempts++;
            return false;  // UNSAT! Forbidden merge detected
        }

        // Get the two classes
        EquivalenceClass class1 = termToClass.get(rep1);
        EquivalenceClass class2 = termToClass.get(rep2);

        // MANDATORY OPTIMIZATION: Choose representative with larger ccpar set
        Term newRep;
        EquivalenceClass keepClass;
        EquivalenceClass mergeClass;

        if (class1.getCcparSize() >= class2.getCcparSize()) {
            // class1 has larger (or equal) ccpar, keep it as representative
            newRep = rep1;
            keepClass = class1;
            mergeClass = class2;
        } else {
            // class2 has larger ccpar, use it as representative
            newRep = rep2;
            keepClass = class2;
            mergeClass = class1;
        }

        // Merge: add all members from mergeClass into keepClass
        keepClass.addAll(mergeClass);

        // Update find pointers for all terms in merged class
        for (Term term : mergeClass.getMembers()) {
            term.setFind(newRep);
            termToClass.put(term, keepClass);
        }

        // Update representatives' find pointers
        rep1.setFind(newRep);
        rep2.setFind(newRep);

        return true;  // Success
    }

    /**
     * Returns all current equivalence classes.
     * OPTIMIZATION Phase 5.3: Use HashSet constructor for better performance
     */
    public Set<EquivalenceClass> getAllClasses() {
        return new HashSet<>(termToClass.values());
    }

    /**
     * Returns the total number of equivalence classes.
     */
    public int getClassCount() {
        return getAllClasses().size();
    }

    /**
     * Returns the forbidden set (if enabled).
     */
    public ForbiddenSet getForbiddenSet() {
        return forbiddenSet;
    }

    /**
     * Returns true if forbidden set optimization is enabled.
     */
    public boolean isUsingForbiddenSet() {
        return useForbiddenSet;
    }

    /**
     * Returns true if path compression (non-recursive FIND) is enabled.
     */
    public boolean isUsingPathCompression() {
        return usePathCompression;
    }

    /**
     * Returns the number of times a forbidden merge was attempted.
     */
    public int getForbiddenMergeAttempts() {
        return forbiddenMergeAttempts;
    }

    /**
     * Returns statistics about operations performed.
     */
    public String getStats() {
        int totalTerms = termToClass.size();
        int totalClasses = getClassCount();
        int maxClassSize = getAllClasses().stream()
            .mapToInt(EquivalenceClass::size)
            .max()
            .orElse(0);

        StringBuilder stats = new StringBuilder();
        stats.append(String.format(
            "Terms: %d, Classes: %d, Max class size: %d, " +
            "FIND ops: %d, UNION ops: %d",
            totalTerms, totalClasses, maxClassSize,
            findOperations, unionOperations
        ));

        // Add optimization statistics
        if (usePathCompression) {
            stats.append(", Path compression: ON");
        }

        if (useForbiddenSet) {
            stats.append(String.format(
                ", Forbidden pairs: %d, Forbidden checks: %d, Forbidden merges blocked: %d",
                forbiddenSet.size(),
                forbiddenSet.getForbiddenChecks(),
                forbiddenMergeAttempts
            ));
        }

        return stats.toString();
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("Equivalence Classes:\n");
        List<EquivalenceClass> sorted = new ArrayList<>(getAllClasses());
        sorted.sort(Comparator.comparingInt(EquivalenceClass::getId));
        for (EquivalenceClass ec : sorted) {
            sb.append("  ").append(ec.toString()).append("\n");
        }
        return sb.toString();
    }
}
