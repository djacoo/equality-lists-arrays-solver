package solver.equivalence;

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
 */
public class ClassManager {
    // Maps each term to its current equivalence class
    private final Map<Term, EquivalenceClass> termToClass;

    // Counter for assigning unique class IDs
    private int nextClassId;

    // Statistics
    private int findOperations;
    private int unionOperations;

    public ClassManager() {
        this.termToClass = new HashMap<>();
        this.nextClassId = 1;
        this.findOperations = 0;
        this.unionOperations = 0;
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
     * Implements path compression: updates all find pointers along the path
     * to point directly to the representative.
     *
     * Time complexity: O(α(n)) amortized, where α is inverse Ackermann function
     */
    public Term find(Term t) {
        findOperations++;

        // Follow find pointers until we reach representative
        Term current = t;
        while (current.getFind() != current) {
            current = current.getFind();
        }

        // Path compression: update find pointers along the path
        Term representative = current;
        current = t;
        while (current != representative) {
            Term next = current.getFind();
            current.setFind(representative);
            current = next;
        }

        return representative;
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
     * UNION: Merges the equivalence classes of t1 and t2.
     *
     * CRITICAL: Implements the MANDATORY largest ccpar optimization.
     * The representative is chosen from the class with the larger ccpar set.
     *
     * Time complexity: O(k) where k = size of smaller class
     */
    public void union(Term t1, Term t2) {
        unionOperations++;

        // Get representatives
        Term rep1 = find(t1);
        Term rep2 = find(t2);

        // Already in same class?
        if (rep1 == rep2) {
            return;
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
    }

    /**
     * Returns all current equivalence classes.
     */
    public Set<EquivalenceClass> getAllClasses() {
        Set<EquivalenceClass> classes = new HashSet<>();
        for (EquivalenceClass ec : termToClass.values()) {
            classes.add(ec);
        }
        return classes;
    }

    /**
     * Returns the total number of equivalence classes.
     */
    public int getClassCount() {
        return getAllClasses().size();
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

        return String.format(
            "Terms: %d, Classes: %d, Max class size: %d, " +
            "FIND ops: %d, UNION ops: %d",
            totalTerms, totalClasses, maxClassSize,
            findOperations, unionOperations
        );
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
