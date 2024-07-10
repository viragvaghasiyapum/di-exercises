package de.di.duplicate_detection;

import de.di.Relation;
import de.di.duplicate_detection.structures.AttrSimWeight;
import de.di.duplicate_detection.structures.Duplicate;
import de.di.similarity_measures.Jaccard;
import de.di.similarity_measures.Levenshtein;
import de.di.similarity_measures.helper.Tokenizer;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.*;

public class SortedNeighborhood {

    // A Record class that stores the values of a record with its original index. This class helps to remember the
    // original index of a record when this record is being sorted.
    @Data
    @AllArgsConstructor
    private static class Record {
        private int index;
        private String[] values;
    }

    /**
     * Discovers all duplicates in the relation by running the Sorted Neighborhood Method once with every sortingKey.
     * Each run uses one of the specified sortingKeys for the sorting, the windowsSize for the windowing, and
     * the recordComparator for the similarity calculations. A pair of records is classified as a duplicate and the
     * corresponding record indexes are returned as a Duplicate object, if the similarity of the two records w.r.t.
     * the provided recordComparator is equal to or greater than the similarityThreshold.
     * @param relation The relation, in which duplicates should be detected.
     * @param sortingKeys The sorting keys that should be used; a sorting key corresponds to an attribute index, whose
     *                    lexicographical order should determine a sortation; every specificed sorting key korresponds
     *                    to one Sorted Neighborhood run and the union of all duplicates of all runs is the result of
     *                    the call.
     * @param windowSize The window size each Sorted Neighborhood run should use.
     * @param recordComparator The record comparator each Sorted Neighborhood run should use when comparing records.
     * @return The list of discovered duplicate pairs of all Sorted Neighborhood runs.
     */
    public Set<Duplicate> detectDuplicates(Relation relation, int[] sortingKeys, int windowSize, RecordComparator recordComparator) {
        Set<Duplicate> duplicates = new HashSet<>();

        Record[] records = new Record[relation.getRecords().length];
        for (int i = 0; i < relation.getRecords().length; i++) {
            records[i] = new Record(i, relation.getRecords()[i]);
        }

        for (int sortingKey : sortingKeys) {
            // Sort the records based on the current sorting key
            Arrays.sort(records, Comparator.comparing(r -> r.getValues()[sortingKey]));

            // Apply the sliding window
            for (int i = 0; i < records.length - windowSize + 1; i++) {
                for (int j = i + 1; j < i + windowSize && j < records.length; j++) {
                    Record record1 = records[i];
                    Record record2 = records[j];
                    double similarity = recordComparator.compare(record1.getValues(), record2.getValues());
                    if (recordComparator.isDuplicate(similarity)) {
                        duplicates.add(new Duplicate(record1.getIndex(), record2.getIndex(), similarity, relation));
                    }
                }
            }
        }
        return duplicates;
    }

    /**
     * Suggests a RecordComparator instance based on the provided relation for duplicate detection purposes.
     * @param relation The relation a RecordComparator needs to be suggested for.
     * @return A RecordComparator instance for comparing records of the provided relation.
     */
    public static RecordComparator suggestRecordComparatorFor(Relation relation) {
        List<AttrSimWeight> attrSimWeights = new ArrayList<>(relation.getAttributes().length);
        double threshold = 0.0;

        for (int i = 0; i < relation.getAttributes().length; i++) {
            double avgLength = calculateAverageLength(relation, i);

            if (avgLength < 10) {
                // For shorter strings, use Levenshtein
                attrSimWeights.add(new AttrSimWeight(i, new Levenshtein(true), 0.1));
            } else {
                // For longer strings, use Jaccard
                attrSimWeights.add(new AttrSimWeight(i, new Jaccard(new Tokenizer(4, true), false), 0.2));
            }
            // Increase the threshold based on the number of attributes
            threshold += 0.8 / relation.getAttributes().length;
        }
        return new RecordComparator(attrSimWeights, threshold);
    }

    private static double calculateAverageLength(Relation relation, int attributeIndex) {
        double totalLength = 0;
        int count = 0;
        for (String[] record : relation.getRecords()) {
            String value = record[attributeIndex];
            if (value != null) {
                totalLength += value.length();
                count++;
            }
        }
        return count > 0 ? totalLength / count : 0;
    }
}
