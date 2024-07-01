package de.di.schema_matching;

import de.di.Relation;
import de.di.schema_matching.structures.SimilarityMatrix;
import de.di.similarity_measures.Jaccard;
import de.di.similarity_measures.helper.Tokenizer;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class FirstLineSchemaMatcher {

    /**
     * Matches the attributes of the source and target table and produces a #source_attributes x #target_attributes
     * sized similarity matrix that represents the attribute-to-attribute similarities of the two relations.
     * @param sourceRelation The first relation for the matching that determines the first (= y) dimension of the
     *                       similarity matrix, i.e., double[*][].
     * @param targetRelation The second relation for the matching that determines the second (= x) dimension of the
     *                       similarity matrix, i.e., double[][*].
     * @return The similarity matrix that describes the attribute-to-attribute similarities of the two relations.
     */
    public SimilarityMatrix match(Relation sourceRelation, Relation targetRelation) {
        String[][] sourceColumns = sourceRelation.getColumns();
        String[][] targetColumns = targetRelation.getColumns();

        Jaccard jaccard = new Jaccard(new Tokenizer(4, true), false);

        // Initialize the similarity matrix
        double[][] matrix = new double[sourceColumns.length][];
        for (int i = 0; i < sourceColumns.length; i++)
            matrix[i] = new double[targetColumns.length];

        // Calculate all pair-wise attribute similarities
        for (int i = 0; i < sourceColumns.length; i++) {
            for (int j = 0; j < targetColumns.length; j++) {
                matrix[i][j] = jaccard.calculate(sourceColumns[i], targetColumns[j]);
            }
        }
        return new SimilarityMatrix(matrix, sourceRelation, targetRelation);
    }
}
