package solver.theory.te;

import solver.dag.Term;

/**
 * Represents a literal in equality theory: either an equality (t1 = t2)
 * or a disequality (t1 != t2).
 */
public class Literal {
    private final Term left;
    private final Term right;
    private final boolean positive;  // true for equality, false for disequality

    private Literal(Term left, Term right, boolean positive) {
        this.left = left;
        this.right = right;
        this.positive = positive;
    }

    /**
     * Creates an equality literal: t1 = t2
     */
    public static Literal equality(Term left, Term right) {
        return new Literal(left, right, true);
    }

    /**
     * Creates a disequality literal: t1 != t2
     */
    public static Literal disequality(Term left, Term right) {
        return new Literal(left, right, false);
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
        return positive;
    }

    /**
     * Returns true if this is a disequality (negative literal).
     */
    public boolean isDisequality() {
        return !positive;
    }

    @Override
    public String toString() {
        return left.getSymbol() + (positive ? " = " : " != ") + right.getSymbol();
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof Literal)) return false;
        Literal other = (Literal) obj;

        // Equality is symmetric: (a = b) is same as (b = a)
        boolean sameSides = left.equals(other.left) && right.equals(other.right);
        boolean flippedSides = left.equals(other.right) && right.equals(other.left);

        return positive == other.positive && (sameSides || flippedSides);
    }

    @Override
    public int hashCode() {
        // Symmetric hash code (order doesn't matter for equality)
        return left.hashCode() + right.hashCode() + (positive ? 1 : 0);
    }
}
