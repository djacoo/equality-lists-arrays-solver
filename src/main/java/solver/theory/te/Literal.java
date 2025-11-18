package solver.theory.te;

import solver.dag.Term;

/**
 * Represents a literal in equality theory: either an equality (t1 = t2),
 * a disequality (t1 != t2), or an atom predicate (atom(t) or ¬atom(t)).
 *
 * BRADLEY & MANNA (Section 9.4, page 259):
 * - atom is a unary predicate in Σ_cons
 * - Axiom 7: ∀x,y. ¬atom(cons(x,y))
 */
public class Literal {
    private final LiteralType type;
    private final Term left;
    private final Term right;  // null for atom literals
    private final boolean positive;  // true for equality/atom, false for disequality/¬atom

    private enum LiteralType {
        EQUALITY,    // t1 = t2 or t1 != t2
        ATOM         // atom(t) or ¬atom(t)
    }

    private Literal(LiteralType type, Term left, Term right, boolean positive) {
        this.type = type;
        this.left = left;
        this.right = right;
        this.positive = positive;
    }

    /**
     * Creates an equality literal: t1 = t2
     */
    public static Literal equality(Term left, Term right) {
        return new Literal(LiteralType.EQUALITY, left, right, true);
    }

    /**
     * Creates a disequality literal: t1 != t2
     */
    public static Literal disequality(Term left, Term right) {
        return new Literal(LiteralType.EQUALITY, left, right, false);
    }

    /**
     * Creates an atom literal: atom(t)
     * Bradley & Manna Section 9.4, page 259
     */
    public static Literal atom(Term term) {
        return new Literal(LiteralType.ATOM, term, null, true);
    }

    /**
     * Creates a negated atom literal: ¬atom(t)
     * Bradley & Manna Section 9.4, page 259
     */
    public static Literal notAtom(Term term) {
        return new Literal(LiteralType.ATOM, term, null, false);
    }

    /**
     * Returns the left term.
     */
    public Term getLeft() {
        return left;
    }

    /**
     * Returns the right term.
     */
    public Term getRight() {
        return right;
    }

    /**
     * Returns true if this is an equality (positive literal).
     */
    public boolean isEquality() {
        return type == LiteralType.EQUALITY && positive;
    }

    /**
     * Returns true if this is a disequality (negative literal).
     */
    public boolean isDisequality() {
        return type == LiteralType.EQUALITY && !positive;
    }

    /**
     * Returns true if this is an atom literal (atom(t) or ¬atom(t)).
     */
    public boolean isAtom() {
        return type == LiteralType.ATOM;
    }

    /**
     * Returns true if this is a positive atom literal: atom(t).
     */
    public boolean isPositiveAtom() {
        return type == LiteralType.ATOM && positive;
    }

    /**
     * Returns true if this is a negative atom literal: ¬atom(t).
     */
    public boolean isNegativeAtom() {
        return type == LiteralType.ATOM && !positive;
    }

    /**
     * Returns the term for atom literals.
     */
    public Term getAtomTerm() {
        if (type != LiteralType.ATOM) {
            throw new IllegalStateException("Not an atom literal");
        }
        return left;
    }

    @Override
    public String toString() {
        if (type == LiteralType.ATOM) {
            return (positive ? "atom(" : "¬atom(") + left.toSExpression() + ")";
        }
        return left.toSExpression() + (positive ? " = " : " != ") + right.toSExpression();
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof Literal)) return false;
        Literal other = (Literal) obj;

        if (type != other.type || positive != other.positive) {
            return false;
        }

        if (type == LiteralType.ATOM) {
            // For atom literals, just compare the term
            return left.equals(other.left);
        }

        // For equality literals, order doesn't matter: (a = b) is same as (b = a)
        boolean sameSides = left.equals(other.left) && right.equals(other.right);
        boolean flippedSides = left.equals(other.right) && right.equals(other.left);

        return sameSides || flippedSides;
    }

    @Override
    public int hashCode() {
        if (type == LiteralType.ATOM) {
            return left.hashCode() + (positive ? 1 : 0);
        }
        // Symmetric hash code (order doesn't matter for equality)
        return left.hashCode() + right.hashCode() + (positive ? 1 : 0);
    }
}
