package de.di.data_profiling;

import de.di.Relation;
import de.di.data_profiling.structures.AttributeList;
import de.di.data_profiling.structures.IND;

import java.sql.Array;
import java.util.*;
import java.util.stream.Collectors;

public class INDProfiler {

    /**
     * Discovers all non-trivial unary (and n-ary) inclusion dependencies in the provided relations.
     * @param relations The relations that should be profiled for inclusion dependencies.
     * @return The list of all non-trivial unary (and n-ary) inclusion dependencies in the provided relations.
     */
    public List<IND> profile(List<Relation> relations, boolean discoverNary) {

        if (discoverNary) {
             throw new RuntimeException("nary discovery is not implemented!");
        }
        List<IND> inclusionDependencies = new ArrayList<>();
        for (Relation table1 : relations) {
            for (Relation table2 : relations) {
                if (!table1.getName().equals(table2.getName())) {
                    discoverUnaryInclusionDependencies(table1, table2, inclusionDependencies);
                }
            }
        }
        return inclusionDependencies;
    }

    /**
     * Processes inclusion dependencies between two given relations
     * @param r1 Relation 1
     * @param r2 Relation 2
     * @param inclusionDependencies Unary inclusion dependencies
     */
    private void discoverUnaryInclusionDependencies (Relation r1, Relation r2, List<IND> inclusionDependencies) {

        List<String> r1Attributes = Arrays.asList(r1.getAttributes());
        List<String> r2Attributes = Arrays.asList(r2.getAttributes());

        for (String attr1 : r1Attributes) {
            Set<String> attr1Values = getColumnValues(r1, attr1);
            for (String attr2 : r2Attributes) {
                Set<String> attr2Values = getColumnValues(r2, attr2);
                if (attr2Values.containsAll(attr1Values)) {
                    inclusionDependencies.add(new IND(r1, r1Attributes.indexOf(attr1), r2, r2Attributes.indexOf(attr2)));
                }
            }
        }
    }

    /**
     * Gets column values for an attribute in given relation
     * @param r Relation
     * @param columnName Attribute name
     * @return Set<String> columnValues
     */
    private Set<String> getColumnValues(Relation r, String columnName) {
        Set<String> columnValues = new HashSet<>();
        String[] attributes = r.getAttributes();

        int columnIndex = Arrays.asList(attributes).indexOf(columnName);
        if (columnIndex < 0) {
            throw new IllegalArgumentException("Column " + columnName + " does not exist in table data.");
        }
        int rSize = r.getRecords().length;
        String[][] records = r.getRecords();
        for (int row = 0; row < rSize; row++) {
            columnValues.add(records[row][columnIndex].trim());
        }
        return columnValues;
    }

//    private List<Set<String>> toColumnSets(String[][] columns) {
//        return Arrays.stream(columns)
//                .map(column -> new HashSet<>(new ArrayList<>(List.of(column))))
//                .collect(Collectors.toList());
//    }
}
