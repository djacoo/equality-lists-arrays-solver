package solver.testing;

import solver.dag.Term;
import solver.dag.TermFactory;
import solver.dag.Variable;
import solver.theory.te.Literal;

import java.util.*;

/**
 * Random test case generator for stress testing the solver.
 *
 * Generates random sets of literals (equalities and disequalities) for testing
 * solver correctness and performance on various problem sizes.
 */
public class RandomTestGenerator {
    private final Random random;
    private final TermFactory termFactory;

    public RandomTestGenerator() {
        this(new Random());
    }

    public RandomTestGenerator(long seed) {
        this(new Random(seed));
    }

    private RandomTestGenerator(Random random) {
        this.random = random;
        this.termFactory = new TermFactory();
    }

    /**
     * Generates a random test case with specified parameters.
     *
     * @param numVariables Number of distinct variables to use
     * @param numEqualities Number of equality literals
     * @param numDisequalities Number of disequality literals
     * @return List of randomly generated literals
     */
    public List<Literal> generateTest(int numVariables, int numEqualities, int numDisequalities) {
        if (numVariables < 2) {
            throw new IllegalArgumentException("Need at least 2 variables");
        }

        // Create variables
        List<Variable> variables = new ArrayList<>();
        for (int i = 0; i < numVariables; i++) {
            variables.add(termFactory.createVariable("v" + i));
        }

        List<Literal> literals = new ArrayList<>();

        // Generate equalities
        for (int i = 0; i < numEqualities; i++) {
            Term t1 = variables.get(random.nextInt(numVariables));
            Term t2 = variables.get(random.nextInt(numVariables));

            // Avoid trivial equalities (x = x)
            while (t1.equals(t2)) {
                t2 = variables.get(random.nextInt(numVariables));
            }

            literals.add(Literal.equality(t1, t2));
        }

        // Generate disequalities
        for (int i = 0; i < numDisequalities; i++) {
            Term t1 = variables.get(random.nextInt(numVariables));
            Term t2 = variables.get(random.nextInt(numVariables));

            // Avoid trivial disequalities (x != x, which is always false)
            while (t1.equals(t2)) {
                t2 = variables.get(random.nextInt(numVariables));
            }

            literals.add(Literal.disequality(t1, t2));
        }

        // Shuffle to mix equalities and disequalities
        Collections.shuffle(literals, random);

        return literals;
    }

    /**
     * Generates a guaranteed SAT test case.
     * Creates equalities that form disjoint equivalence classes,
     * with disequalities only between different classes.
     *
     * @param numVariables Number of distinct variables
     * @param numClasses Number of equivalence classes to create
     * @return List of literals that form a SAT instance
     */
    public List<Literal> generateSatTest(int numVariables, int numClasses) {
        if (numVariables < numClasses || numClasses < 1) {
            throw new IllegalArgumentException("Invalid parameters");
        }

        // Create variables
        List<Variable> variables = new ArrayList<>();
        for (int i = 0; i < numVariables; i++) {
            variables.add(termFactory.createVariable("v" + i));
        }

        List<Literal> literals = new ArrayList<>();

        // Partition variables into classes
        int varsPerClass = numVariables / numClasses;
        List<List<Variable>> classes = new ArrayList<>();

        int idx = 0;
        for (int c = 0; c < numClasses; c++) {
            List<Variable> classVars = new ArrayList<>();
            int size = (c == numClasses - 1) ? (numVariables - idx) : varsPerClass;

            for (int i = 0; i < size && idx < numVariables; i++, idx++) {
                classVars.add(variables.get(idx));
            }

            if (!classVars.isEmpty()) {
                classes.add(classVars);
            }
        }

        // Create equalities within each class
        for (List<Variable> classVars : classes) {
            for (int i = 0; i < classVars.size() - 1; i++) {
                literals.add(Literal.equality(classVars.get(i), classVars.get(i + 1)));
            }
        }

        // Create disequalities between different classes
        for (int i = 0; i < classes.size(); i++) {
            for (int j = i + 1; j < classes.size(); j++) {
                if (!classes.get(i).isEmpty() && !classes.get(j).isEmpty()) {
                    Variable v1 = classes.get(i).get(0);
                    Variable v2 = classes.get(j).get(0);
                    literals.add(Literal.disequality(v1, v2));
                }
            }
        }

        Collections.shuffle(literals, random);
        return literals;
    }

    /**
     * Generates a guaranteed UNSAT test case.
     * Creates a chain of equalities and a contradicting disequality.
     *
     * @param chainLength Length of the equality chain
     * @return List of literals that form an UNSAT instance
     */
    public List<Literal> generateUnsatTest(int chainLength) {
        if (chainLength < 2) {
            throw new IllegalArgumentException("Chain length must be at least 2");
        }

        // Create variables
        List<Variable> variables = new ArrayList<>();
        for (int i = 0; i < chainLength; i++) {
            variables.add(termFactory.createVariable("v" + i));
        }

        List<Literal> literals = new ArrayList<>();

        // Create chain: v0 = v1 = v2 = ... = v(n-1)
        for (int i = 0; i < chainLength - 1; i++) {
            literals.add(Literal.equality(variables.get(i), variables.get(i + 1)));
        }

        // Add contradicting disequality: v0 != v(n-1)
        literals.add(Literal.disequality(variables.get(0), variables.get(chainLength - 1)));

        Collections.shuffle(literals, random);
        return literals;
    }

    /**
     * Generates a stress test with many literals.
     *
     * @param size Scale factor (larger = more literals)
     * @return Large test case
     */
    public List<Literal> generateStressTest(int size) {
        int numVars = size * 10;
        int numEqs = size * 15;
        int numDiseqs = size * 5;
        return generateTest(numVars, numEqs, numDiseqs);
    }

    /**
     * Generates a test case for the list theory (T_cons).
     * Creates terms with cons, car, and cdr operations.
     *
     * @param numVariables Number of base variables
     * @param numConsTerms Number of cons terms to create
     * @param numEqualities Number of equality literals
     * @param numDisequalities Number of disequality literals
     * @return List of literals involving list operations
     */
    public List<Literal> generateListTest(int numVariables, int numConsTerms,
                                          int numEqualities, int numDisequalities) {
        if (numVariables < 2) {
            throw new IllegalArgumentException("Need at least 2 variables");
        }

        // Create base variables
        List<Term> terms = new ArrayList<>();
        for (int i = 0; i < numVariables; i++) {
            terms.add(termFactory.createVariable("x" + i));
        }

        // Create cons terms: cons(x, y)
        for (int i = 0; i < numConsTerms && terms.size() >= 2; i++) {
            Term arg1 = terms.get(random.nextInt(terms.size()));
            Term arg2 = terms.get(random.nextInt(terms.size()));
            Term consTerm = termFactory.createFunctionApp("cons", arg1, arg2);
            terms.add(consTerm);

            // Optionally create car/cdr projections
            if (random.nextBoolean()) {
                terms.add(termFactory.createFunctionApp("car", consTerm));
            }
            if (random.nextBoolean()) {
                terms.add(termFactory.createFunctionApp("cdr", consTerm));
            }
        }

        List<Literal> literals = new ArrayList<>();

        // Generate equalities
        for (int i = 0; i < numEqualities && terms.size() >= 2; i++) {
            Term t1 = terms.get(random.nextInt(terms.size()));
            Term t2 = terms.get(random.nextInt(terms.size()));

            while (t1.equals(t2)) {
                t2 = terms.get(random.nextInt(terms.size()));
            }

            literals.add(Literal.equality(t1, t2));
        }

        // Generate disequalities
        for (int i = 0; i < numDisequalities && terms.size() >= 2; i++) {
            Term t1 = terms.get(random.nextInt(terms.size()));
            Term t2 = terms.get(random.nextInt(terms.size()));

            while (t1.equals(t2)) {
                t2 = terms.get(random.nextInt(terms.size()));
            }

            literals.add(Literal.disequality(t1, t2));
        }

        Collections.shuffle(literals, random);
        return literals;
    }

    /**
     * Generates a test case for the array theory (T_A).
     * Creates terms with select and store operations.
     *
     * @param numArrays Number of base array variables
     * @param numIndices Number of index variables
     * @param numStoreOps Number of store operations
     * @param numEqualities Number of equality literals
     * @param numDisequalities Number of disequality literals
     * @return List of literals involving array operations
     */
    public List<Literal> generateArrayTest(int numArrays, int numIndices, int numStoreOps,
                                           int numEqualities, int numDisequalities) {
        if (numArrays < 1 || numIndices < 1) {
            throw new IllegalArgumentException("Need at least 1 array and 1 index");
        }

        // Create array variables
        List<Term> baseArrays = new ArrayList<>();
        for (int i = 0; i < numArrays; i++) {
            baseArrays.add(termFactory.createVariable("a" + i));
        }

        // Create index variables
        List<Term> indices = new ArrayList<>();
        for (int i = 0; i < numIndices; i++) {
            indices.add(termFactory.createVariable("i" + i));
        }

        // Create value variables
        List<Term> values = new ArrayList<>();
        for (int i = 0; i < numIndices; i++) {
            values.add(termFactory.createVariable("v" + i));
        }

        List<Term> selectTerms = new ArrayList<>();

        // Create a limited number of store operations on base arrays only
        List<Term> storedArrays = new ArrayList<>(baseArrays);
        int actualStoreOps = Math.min(numStoreOps, 3); // Limit store nesting
        for (int i = 0; i < actualStoreOps; i++) {
            Term array = baseArrays.get(random.nextInt(baseArrays.size()));
            Term index = indices.get(random.nextInt(indices.size()));
            Term value = values.get(random.nextInt(values.size()));

            Term storeTerm = termFactory.createFunctionApp("store", array, index, value);
            storedArrays.add(storeTerm);
        }

        // Add select operations on all arrays (base and stored)
        for (Term array : storedArrays) {
            for (int j = 0; j < Math.min(2, indices.size()); j++) {
                Term index = indices.get(j);
                Term selectTerm = termFactory.createFunctionApp("select", array, index);
                selectTerms.add(selectTerm);
            }
        }

        // Combine all terms for generating literals
        List<Term> allTerms = new ArrayList<>();
        allTerms.addAll(indices);
        allTerms.addAll(values);
        allTerms.addAll(selectTerms);

        List<Literal> literals = new ArrayList<>();

        // Generate equalities between compatible terms
        int actualEqualities = Math.min(numEqualities, allTerms.size() / 2);
        for (int i = 0; i < actualEqualities; i++) {
            Term t1 = allTerms.get(random.nextInt(allTerms.size()));
            Term t2 = allTerms.get(random.nextInt(allTerms.size()));

            int attempts = 0;
            while (t1.equals(t2) && attempts < 10) {
                t2 = allTerms.get(random.nextInt(allTerms.size()));
                attempts++;
            }

            if (!t1.equals(t2)) {
                literals.add(Literal.equality(t1, t2));
            }
        }

        // Generate disequalities
        int actualDisequalities = Math.min(numDisequalities, allTerms.size() / 2);
        for (int i = 0; i < actualDisequalities; i++) {
            Term t1 = allTerms.get(random.nextInt(allTerms.size()));
            Term t2 = allTerms.get(random.nextInt(allTerms.size()));

            int attempts = 0;
            while (t1.equals(t2) && attempts < 10) {
                t2 = allTerms.get(random.nextInt(allTerms.size()));
                attempts++;
            }

            if (!t1.equals(t2)) {
                literals.add(Literal.disequality(t1, t2));
            }
        }

        Collections.shuffle(literals, random);
        return literals;
    }

    /**
     * Generates a mixed theory test case combining T_E, T_cons, and T_A.
     *
     * @param size Scale factor for test size
     * @return List of literals from mixed theories
     */
    public List<Literal> generateMixedTheoryTest(int size) {
        List<Literal> literals = new ArrayList<>();

        // Add some basic equality literals
        literals.addAll(generateTest(size * 2, size, size / 2));

        // Add list operations
        literals.addAll(generateListTest(size, size / 2, size / 2, size / 4));

        // Add array operations
        literals.addAll(generateArrayTest(size, size, size / 2, size / 2, size / 4));

        Collections.shuffle(literals, random);
        return literals;
    }
}
