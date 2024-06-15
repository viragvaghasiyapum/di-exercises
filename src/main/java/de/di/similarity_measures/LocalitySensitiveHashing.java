package de.di.similarity_measures;

import de.di.similarity_measures.helper.MinHash;
import de.di.similarity_measures.helper.Tokenizer;

import java.util.*;

public class LocalitySensitiveHashing implements SimilarityMeasure {

    // The tokenizer that is used to transform string inputs into token lists.
    private final Tokenizer tokenizer;

    // A flag indicating whether the Jaccard algorithm should use set or bag semantics for the similarity calculation.
    private final boolean bagSemantics;

    // The MinHash functions that are used to calculate the LSH signatures.
    private final List<MinHash> minHashFunctions;

    public LocalitySensitiveHashing(final Tokenizer tokenizer, final boolean bagSemantics, final int numHashFunctions) {
        assert(tokenizer.getTokenSize() >= numHashFunctions);

        this.tokenizer = tokenizer;
        this.bagSemantics = bagSemantics;
        this.minHashFunctions = new ArrayList<>(numHashFunctions);
        for (int i = 0; i < numHashFunctions; i++)
            this.minHashFunctions.add(new MinHash(i));
    }

    /**
     * Calculates the LSH similarity of the two input strings.
     * The LHS algorithm calculates the LHS signatures by first tokenizing the input strings and then applying its
     * internal MinHash functions to the tokenized strings. Then, it uses the two signatures to approximate the Jaccard
     * similarity of the two strings with their signatures by simply applying the Jaccard algorithm on the two signatures.
     * @param string1 The first string argument for the similarity calculation.
     * @param string2 The second string argument for the similarity calculation.
     * @return The LSH similarity (= Jaccard approximation) of the two arguments.
     */
    @Override
    public double calculate(final String string1, final String string2) {
        String[] strings1 = this.tokenizer.tokenize(string1);
        String[] strings2 = this.tokenizer.tokenize(string2);
        return this.calculate(strings1, strings2);
    }

    /**
     * Calculates the LSH similarity of the two input string arrays.
     * The LHS algorithm calculates the LHS signatures by applying its internal MinHash functions to the two input string
     * lists. Then, it uses the two signatures to approximate the Jaccard similarity of the two strings with their
     * signatures by simply applying the Jaccard algorithm on the two signatures.
     * @param strings1 The first string argument for the similarity calculation.
     * @param strings2 The second string argument for the similarity calculation.
     * @return The LSH similarity (= Jaccard approximation) of the two arguments.
     */
    @Override
    public double calculate(final String[] strings1, final String[] strings2) {
        double lshJaccard = 0;

//        System.out.println(strings1.length);

        Set<String> tokens1 = tokenize(strings1);
        Set<String> tokens2 = tokenize(strings2);

//        System.out.println(tokens1.size());

        int[] minHash1 = computeMinHashSignature(tokens1);
        int[] minHash2 = computeMinHashSignature(tokens2);

        lshJaccard = calculateJaccardSimilarity(minHash1, minHash2);
        return lshJaccard;
    }

    private Set<String> tokenize(String[] array) {
        Set<String> tokens = new HashSet<>();
        for (String str : array) {
            String[] splitTokens = str.split("\\s+"); // Simple whitespace tokenization
            tokens.addAll(Arrays.asList(splitTokens));
        }
        return tokens;
    }

    private int[] computeMinHashSignature(Set<String> tokens) {
        int[] minHashSignature = new int[this.minHashFunctions.size()];
        Arrays.fill(minHashSignature, Integer.MAX_VALUE);

        for (String token : tokens) {
            for (int i = 0; i < this.minHashFunctions.size(); i++) {
                int hashValue = hash(i, token);
                if (hashValue < minHashSignature[i]) {
                    minHashSignature[i] = hashValue;
                }
            }
        }
        return minHashSignature;
    }

    private int hash(int i, String token) {
        Random random = new Random(i);
        int a = random.nextInt();
        int b = random.nextInt();
        int prime = 2147483647; // A large prime number
        return Math.abs((a * token.hashCode() + b) % prime);
    }

    private double calculateJaccardSimilarity(int[] minHash1, int[] minHash2) {
        int identicalMinHashes = 0;
        for (int i = 0; i < this.minHashFunctions.size(); i++) {
            if (minHash1[i] == minHash2[i]) {
                identicalMinHashes++;
            }
        }
        return (double) identicalMinHashes / this.minHashFunctions.size();
    }
}