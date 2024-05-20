package de.di.similarity_measures;

import de.di.similarity_measures.helper.Tokenizer;
import lombok.AllArgsConstructor;
import org.apache.commons.text.similarity.IntersectionResult;
import org.apache.commons.text.similarity.JaccardSimilarity;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@AllArgsConstructor
public class Jaccard implements SimilarityMeasure {

    // The tokenizer that is used to transform string inputs into token lists.
    private final Tokenizer tokenizer;

    // A flag indicating whether the Jaccard algorithm should use set or bag semantics for the similarity calculation.
    private final boolean bagSemantics;

    /**
     * Calculates the Jaccard similarity of the two input strings. Note that the Jaccard similarity may use set or
     * multiset, i.e., bag semantics for the union and intersect operations. The maximum Jaccard similarity with
     * multiset semantics is 1/2 and the maximum Jaccard similarity with set semantics is 1.
     * @param string1 The first string argument for the similarity calculation.
     * @param string2 The second string argument for the similarity calculation.
     * @return The multiset Jaccard similarity of the two arguments.
     */
    @Override
    public double calculate(String string1, String string2) {
        string1 = (string1 == null) ? "" : string1;
        string2 = (string2 == null) ? "" : string2;

        String[] strings1 = this.tokenizer.tokenize(string1);
        String[] strings2 = this.tokenizer.tokenize(string2);

        return this.calculate(strings1, strings2);
    }

    /**
     * Calculates the Jaccard similarity of the two string lists. Note that the Jaccard similarity may use set or
     * multiset, i.e., bag semantics for the union and intersect operations. The maximum Jaccard similarity with
     * multiset semantics is 1/2 and the maximum Jaccard similarity with set semantics is 1.
     * @param strings1 The first string list argument for the similarity calculation.
     * @param strings2 The second string list argument for the similarity calculation.
     * @return The multiset Jaccard similarity of the two arguments.
     */
    @Override
    public double calculate(String[] strings1, String[] strings2) {
        ////////////////////////////////////////////////////////////////////////////////////////////////////////////////
        //                                      DATA INTEGRATION ASSIGNMENT                                           //
        // Calculate the Jaccard similarity of the two String arrays. Note that the Jaccard similarity needs to be    //
        // calculated differently depending on the token semantics: set semantics remove duplicates while bag         //
        // semantics consider them during the calculation. The solution should be able to calculate the Jaccard       //
        // similarity either of the two semantics by respecting the inner bagSemantics flag.                          //
        //                                                                                                            //
        ////////////////////////////////////////////////////////////////////////////////////////////////////////////////

        //  Note : bag semantics - don't remove duplicates from neither the union nor intersection
        //         set semantics - remove duplicates from both union and intersection

        double jaccardSimilarity = 0;
        Arrays.sort(strings1);
        Arrays.sort(strings2);

        if (bagSemantics) {
            Map<String, Integer> countElem1 = new HashMap<>();
            Map<String, Integer> countElem2 = new HashMap<>();
            ArrayList<String> intersection = new ArrayList<>();

            // counting occurrences of each element in strings1 array
            for (String token : strings1) {
                countElem1.put(token, countElem1.containsKey(token) ? countElem1.get(token) + 1 : 1);
            }
            // counting occurrences of each element in strings2 array
            for (String token : strings2) {
                countElem2.put(token, countElem2.containsKey(token) ? countElem2.get(token) + 1 : 1);
            }
            // creating intersection array with duplicate elements
            for (Map.Entry<String, Integer> entry : countElem1.entrySet()) {
                String key = entry.getKey();
                if (!countElem2.containsKey(key)) {
                    continue;
                }
                int occurrence = Math.min(entry.getValue(), countElem2.get(key));
                while(occurrence > 0) {
                    intersection.add(key);
                    --occurrence;
                }
            }
            jaccardSimilarity = (double) intersection.size() / (strings1.length + strings2.length);
        } else {
            Set<String> set1 = new HashSet<>(Arrays.asList(strings1));
            Set<String> set2 = new HashSet<>(Arrays.asList(strings2));

            // intersection without duplicates
            Set<String> intersection = new HashSet<>(set1);
            intersection.retainAll(set2);

            // union of unique elements
            Set<String> union = new HashSet<>(set1);
            union.addAll(set2);

            jaccardSimilarity = (double) intersection.size() / union.size();
        }
        return Math.abs(jaccardSimilarity);
    }
}
