package de.di.duplicate_detection;

import de.di.Relation;
import de.di.duplicate_detection.structures.Duplicate;

import java.util.HashSet;
import java.util.Set;

public class TransitiveClosure {

    /**
     * Calculates the transitive close over the provided set of duplicates. The result of the transitive closure
     * calculation are all input duplicates together with all additional duplicates that follow from the input
     * duplicates via transitive inference. For example, if (1,2) and (2,3) are two input duplicates, the algorithm
     * adds the transitive duplicate (1,3). Note that the duplicate relationship is commutative, i.e., (1,2) and (2,1)
     * both describe the same duplicate. The algorithm does not add identity duplicates, such as (1,1).
     * @param duplicates The duplicates over which the transitive closure is to be calculated.
     * @return The input set of duplicates with all transitively inferrable additional duplicates.
     */
    public Set<Duplicate> calculate(Set<Duplicate> duplicates) {
        Set<Duplicate> closedDuplicates = new HashSet<>(2 * duplicates.size());

        if (duplicates.size() <= 1)
            return duplicates;

        Relation relation = duplicates.iterator().next().getRelation();
        int numRecords = relation.getRecords().length;

        // Create an adjacency matrix to represent the duplicate relationships
        boolean[][] adjacencyMatrix = new boolean[numRecords][numRecords];

        // Populate the adjacency matrix with input duplicates
        for (Duplicate duplicate : duplicates) {
            int record1 = duplicate.getIndex1();
            int record2 = duplicate.getIndex2();
            adjacencyMatrix[record1][record2] = true;
            adjacencyMatrix[record2][record1] = true;  // Duplicate relationship is commutative
        }

        // Floyd-Warshall algorithm for transitive closure
        for (int k = 0; k < numRecords; k++) {
            for (int i = 0; i < numRecords; i++) {
                for (int j = 0; j < numRecords; j++) {
                    adjacencyMatrix[i][j] |= (adjacencyMatrix[i][k] && adjacencyMatrix[k][j]);
                }
            }
        }

        // Convert the adjacency matrix back to Duplicate objects
        for (int i = 0; i < numRecords; i++) {
            for (int j = i + 1; j < numRecords; j++) {  // Start from i+1 to avoid identity duplicates
                if (adjacencyMatrix[i][j]) {
                    closedDuplicates.add(new Duplicate(i, j,adjacencyMatrix[i][j] ? 1.0 : 0.0, relation));
                }
            }
        }

        return closedDuplicates;
    }
}
