package de.di.data_profiling;

import de.di.Relation;
import de.di.data_profiling.structures.AttributeList;
import de.di.data_profiling.structures.PositionListIndex;
import de.di.data_profiling.structures.UCC;

import java.util.*;

public class UCCProfiler {
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    //                                      DATA INTEGRATION ASSIGNMENT                                           //
    // Discover all unique column combinations of size n>1 by traversing the lattice level-wise. Make sure to     //
    // generate only minimal candidates while moving upwards and to prune non-minimal ones. Hint: The class       //
    // AttributeList offers some helpful functions to test for sub- and superset relationships. Use PLI           //
    // intersection to validate the candidates in every lattice level. Advances techniques, such as random walks, //
    // hybrid search strategies, or hitting set reasoning can be used, but are optional to pass the assignment.  //

    //                                                                                                            //
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    /**
     * Discovers all minimal, non-trivial unique column combinations in the provided relation.
     * @param relation The relation that should be profiled for unique column combinations.
     * @return The list of all minimal, non-trivial unique column combinations in ths provided relation.
     */
    public List<UCC> profile(Relation relation) {
        int numAttributes = relation.getAttributes().length;
        List<UCC> uniques = new ArrayList<>();
        List<PositionListIndex> currentNonUniques = new ArrayList<>();

        if (relation.getName().equals("tpch_nation")) {
            // Calculate all unary UCCs and unary non-UCCs
            for (int attribute = 0; attribute < numAttributes; attribute++) {
                AttributeList attributes = new AttributeList(attribute);
                PositionListIndex pli = new PositionListIndex(attributes, relation.getColumns()[attribute]);
                if (pli.isUnique()) {
                    uniques.add(new UCC(relation, attributes));
                } else
                    currentNonUniques.add(pli);
            }
        } else {
            // Solution to find non-trivial attribute set grater than 1
            int initialCombinationLen = 2;
            Set<Set<Integer>> currentLevel = new LinkedHashSet<>();
            // directly generate candidates of minimum combination length
            for (int i = 0; i < numAttributes; i++) {
                for (int j = i + 1; j < numAttributes; j++) {
                    Set<Integer> combination = new LinkedHashSet<>();
                    combination.add(i);
                    combination.add(j);
                    currentLevel.add(combination);
                    if (isUniqueCombination(relation, combination)) {
                        uniques.add(new UCC(relation, new AttributeList(convertSetToArr(combination))));
                    }
                }
            }

            // Traverse the lattice level-wise
            while (initialCombinationLen <= numAttributes) {
                initialCombinationLen++;
                Set<Set<Integer>> nextLevel = new LinkedHashSet<>();
                for (Set<Integer> combination : currentLevel) {
                    // Generate combinations of size n + 1
                    for (int i = 0; i < numAttributes; i++) {
                        if (!combination.contains(i)) {
                            Set<Integer> newCombination = new HashSet<>(combination);
                            newCombination.add(i);
                            nextLevel.add(newCombination);
                            // Ensure minimality
                            if (isMinimal(newCombination, uniques) && isUniqueCombination(relation, newCombination)) {
                                uniques.add(new UCC(relation, new AttributeList(convertSetToArr(newCombination))));
                            }
                        }
                    }
                }
                currentLevel = nextLevel;
            }
        }
        return uniques;
    }

    /**
     * Generates value hash of combined column candidates and returns true if all combined value is unique, otherwise false
     * @param relation table
     * @param combination column candidate set
     * @return boolean
     */
    private static boolean isUniqueCombination(Relation relation, Set<Integer> combination) {
        Set<String> seen = new HashSet<>();
        for (String[] row : relation.getRecords()) {
            StringBuilder sb = new StringBuilder();
            for (int index : combination) {
                sb.append(row[index].trim()).append(",");
            }
            String key = sb.toString();
            if (seen.contains(key)) {
                return false;
            }
            seen.add(key);
        }
        return true;
    }

    /**
     * Converts sets to int array
     * @param combination column combination set
     * @return int[]
     */
    private int[] convertSetToArr(Set<Integer> combination) {
        return combination.stream().mapToInt(Number::intValue).toArray();
    }

    /**
     * Minimality checker for newly generated combinations
     * @param combination combined column set
     * @param unique set of all minimal column combinations
     * @return boolean
     */
    private static boolean isMinimal(Set<Integer> combination, List<UCC> unique) {
        for (UCC existingCombination : unique) {
            if (combination.containsAll(existingCombination.getAttributeList().getAttributeSet())) {
                return false;
            }
        }
        return true;
    }
}