package de.di.similarity_measures;

import lombok.AllArgsConstructor;

import java.util.Arrays;

@AllArgsConstructor
public class Levenshtein implements SimilarityMeasure {

    public static int min(int... numbers) {
        return Arrays.stream(numbers).min().orElse(Integer.MAX_VALUE);
    }

    // The choice of whether Levenshtein or DamerauLevenshtein should be calculated.
    private final boolean withDamerau;

    /**
     * Calculates the Levenshtein similarity of the two input strings.
     * The Levenshtein similarity is defined as "1 - normalized Levenshtein distance".
     * @param string1 The first string argument for the similarity calculation.
     * @param string2 The second string argument for the similarity calculation.
     * @return The (Damerau) Levenshtein similarity of the two arguments.
     */
    @Override
    public double calculate(final String string1, final String string2) {
        ////////////////////////////////////////////////////////////////////////////////////////////////////////////////
        //                                      DATA INTEGRATION ASSIGNMENT                                           //
        // Use the three provided lines to successively calculate the Levenshtein matrix with the dynamic programming //
        // algorithm. Depending on whether the inner flag withDamerau is set, the Damerau extension rule should be    //
        // used during calculation or not. Hint: Implement the Levenshtein algorithm here first, then copy the code   //
        // to the String tuple function and adjust it a bit to work on the arrays - the algorithm is the same.        //
        //                                                                                                            //
        ////////////////////////////////////////////////////////////////////////////////////////////////////////////////

        double levenshteinSimilarity = 0;
        double levenshteinDistance = 0;
        int len1 = string1.length();
        int len2 = string2.length();
        double originalLength = Math.max(len1, len2);

        String str1 = string1;
        String str2 = string2;

        // Handle edge cases where one or both strings are empty
        if (len1 == 0 && len2 == 0) {
            return 1.0;
        } else if (len1 == 0 || len2 == 0) {
            return 0;
        } else if (string1.charAt(len1 - 1) == string2.charAt(len2 - 1)) {
            str1 = string1.substring(0, len1 - 1);
            str2 = string2.substring(0, len2 - 1);
            --len1;
            --len2;
        }

        int[] upperupperLine = new int[len1 + 1];   // line for Demarau lookups
        int[] upperLine = new int[len1 + 1];        // line for regular Levenshtein lookups
        int[] lowerLine = new int[len1 + 1];        // line to be filled next by the algorithm

        // Fill the first line with the initial positions (= edits to generate string1 from nothing)
        for (int i = 0; i <= len1; i++) {
            upperLine[i] = i;
        }
        for (int j = 1; j <= len2; j++ ) {
            lowerLine[0] = j;
            for (int i = 1; i <= len1; i++) {
                if (str1.charAt(i - 1) == str2.charAt(j - 1)) {
                    lowerLine[i] = upperLine[i - 1];
                } else {
                    int calcVal = Math.min(Math.min(lowerLine[i - 1], upperLine[i]), upperLine[i - 1]);
                    if (withDamerau && i > 1 && j > 1 && str1.charAt(i - 2) == str2.charAt(j - 1) && str1.charAt(i - 1) == str2.charAt(j - 2)) {
                        calcVal = Math.min(upperupperLine[i - 2], calcVal);
                    }
                    lowerLine[i] = ++calcVal;
                }
            }
            upperupperLine = upperLine;
            upperLine = lowerLine;
            lowerLine = new int[len1 + 1];
        }
        levenshteinDistance = upperLine[upperLine.length - 1];
        levenshteinSimilarity = 1 - (levenshteinDistance / originalLength);
        return levenshteinSimilarity;
    }

    /**
     * Calculates the Levenshtein similarity of the two input string lists.
     * The Levenshtein similarity is defined as "1 - normalized Levenshtein distance".
     * For string lists, we consider each list as an ordered list of tokens and calculate the distance as the number of
     * token insertions, deletions, replacements (and swaps) that transform one list into the other.
     * @param strings1 The first string list argument for the similarity calculation.
     * @param strings2 The second string list argument for the similarity calculation.
     * @return The (multiset) Levenshtein similarity of the two arguments.
     */
    @Override
    public double calculate(final String[] strings1, final String[] strings2) {
        double levenshteinSimilarity = 0;
        double totalSimilarity = 0;

        int len1 = strings1.length;
        int len2 = strings2.length;
        if (len1 != len2) {
            throw new IllegalArgumentException("Both arrays must have the same length.");
        }
        for (int i = 0; i < len1; i++) {
            totalSimilarity += calculate(strings1[i], strings2[i]);
        }
        double levenshteinDistance =  totalSimilarity / len1;
        levenshteinSimilarity = 1 - (levenshteinDistance / len1);
        return levenshteinSimilarity;
    }
}
