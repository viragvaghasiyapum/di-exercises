package de.di.data_profiling;

import de.di.Relation;
import de.di.data_profiling.structures.AttributeList;
import de.di.data_profiling.structures.PositionListIndex;
import de.di.data_profiling.structures.UCC;

import java.util.ArrayList;
import java.util.List;
import java.util.HashSet;
import java.util.Set;

public class UCCProfiler {

    /**
     * Discovers all minimal, non-trivial unique column combinations in the provided relation.
     * @param relation The relation that should be profiled for unique column combinations.
     * @return The list of all minimal, non-trivial unique column combinations in ths provided relation.
     */
    public List<UCC> profile(Relation relation) {
        int numAttributes = relation.getAttributes().length;
        List<UCC> uniques = new ArrayList<>();
        List<PositionListIndex> currentNonUniques = new ArrayList<>();
        Set<AttributeList> validUCCs = new HashSet<>();

        // Calculate all unary UCCs and unary non-UCCs
        for (int attribute = 0; attribute < numAttributes; attribute++) {
            AttributeList attributes = new AttributeList(attribute);
            PositionListIndex pli = new PositionListIndex(attributes, relation.getColumns()[attribute]);
            if (pli.isUnique()) {
                uniques.add(new UCC(relation, attributes));
                validUCCs.add(attributes);
            }
            else
                currentNonUniques.add(pli);
        }

        ////////////////////////////////////////////////////////////////////////////////////////////////////////////////
        //                                      DATA INTEGRATION ASSIGNMENT                                           //
        // Discover all unique column combinations of size n>1 by traversing the lattice level-wise. Make sure to     //
        // generate only minimal candidates while moving upwards and to prune non-minimal ones. Hint: The class       //
        // AttributeList offers some helpful functions to test for sub- and superset relationships. Use PLI           //
        // intersection to validate the candidates in every lattice level. Advances techniques, such as random walks, //
        // hybrid search strategies, or hitting set reasoning can be used, but are mandatory to pass the assignment.  //

        // Lattice traversal to find UCCs of size > 1
        List<AttributeList> candidates = new ArrayList<>(validUCCs);
        while (!candidates.isEmpty()) {
            List<AttributeList> nextLevelCandidates = new ArrayList<>();

            for (int i = 0; i < candidates.size(); i++) {
                for (int j = i + 1; j < candidates.size(); j++) {
                    AttributeList candidate = candidates.get(i).union(candidates.get(j));
                    if (candidate.size() == candidates.get(i).size() + 1) {
                        // Check if the candidate is minimal by ensuring all its subsets are invalid
                        boolean isMinimal = true;
                        for (int attribute : candidate.getAttributes()) {
                            AttributeList subset = candidate.remove(attribute);
                            if (validUCCs.contains(subset)) {
                                isMinimal = false;
                                break;
                            }
                        }

                        if (isMinimal) {
                            // Validate the candidate using PLI intersection
                            PositionListIndex combinedPLI = null;
                            for (int attribute : candidate.getAttributes()) {
                                PositionListIndex pli = new PositionListIndex(new AttributeList(attribute), relation.getColumns()[attribute]);
                                if (combinedPLI == null) {
                                    combinedPLI = pli;
                                } else {
                                    combinedPLI = combinedPLI.intersect(pli);
                                }
                            }

                            if (combinedPLI != null && combinedPLI.isUnique()) {
                                uniques.add(new UCC(relation, candidate));
                                validUCCs.add(candidate);
                            } else {
                                nextLevelCandidates.add(candidate);
                            }
                        }
                    }
                }
            }

            candidates = nextLevelCandidates;
        }

        //                                                                                                            //
        ////////////////////////////////////////////////////////////////////////////////////////////////////////////////

        return uniques;
    }
}
