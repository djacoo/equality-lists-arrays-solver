package solver.equivalence;

import solver.dag.Term;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Represents an equivalence class of terms.
 *
 * An equivalence class contains all terms that are known to be equal.
 * It maintains:
 * - A representative term (canonical member)
 * - The set of all member terms
 * - Cached ccpar size for efficient UNION operations
 */
public class EquivalenceClass {
    // Representative term of this equivalence class
    private Term representative;

    // All terms in this equivalence class
    private final Set<Term> members;

    // Cached size of the ccpar set of the representative
    // (For efficient comparison during UNION)
    private int ccparSize;

    // Unique ID for this equivalence class (for debugging)
    private final int id;

    /**
     * Creates a new equivalence class containing a single term.
     */
    public EquivalenceClass(int id, Term term) {
        this.id = id;
        this.representative = term;
        this.members = new HashSet<>();
        this.members.add(term);
        this.ccparSize = term.getCcparSize();
    }

    // Getters

    public Term getRepresentative() {
        return representative;
    }

    public Set<Term> getMembers() {
        return Collections.unmodifiableSet(members);
    }

    public int getCcparSize() {
        return ccparSize;
    }

    public int getId() {
        return id;
    }

    public int size() {
        return members.size();
    }

    // Modification methods

    /**
     * Checks if a term is in this class.
     */
    public boolean contains(Term term) {
        return members.contains(term);
    }

    /**
     * Adds a term to this class (used during UNION).
     */
    public void addMember(Term term) {
        members.add(term);
    }

    /**
     * Adds all terms from another class (used during UNION).
     */
    public void addAll(EquivalenceClass other) {
        members.addAll(other.members);
    }

    /**
     * Updates the representative of this class (used during UNION).
     */
    public void setRepresentative(Term newRep) {
        this.representative = newRep;
        this.ccparSize = newRep.getCcparSize();
    }

    @Override
    public String toString() {
        return String.format("Class[%d] rep=%s size=%d members=%s",
            id, representative.getSymbol(), members.size(),
            members.stream()
                .map(Term::getSymbol)
                .sorted()
                .collect(Collectors.toList()));
    }
}
