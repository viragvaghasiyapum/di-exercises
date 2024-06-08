package de.di.data_profiling;

import com.opencsv.CSVReader;
import com.opencsv.exceptions.CsvValidationException;
import de.di.Relation;
import de.di.data_profiling.structures.IND;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
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

        Map<String, List<String[]>> allRelations = new HashMap<>();
        for (Relation relation : relations) {
            allRelations.put(relation.getName(), readCsvData(relation));
        }

        List<IND> inclusionDependencies = new ArrayList<>();
        for (Relation table1 : relations) {
            for (Relation table2 : relations) {
                if (!table1.getName().equals(table2.getName())) {
                    discoverUnaryInclusionDependencies(table1, table2, allRelations, inclusionDependencies);
                    System.out.println(inclusionDependencies.size());
                }
            }
        }
        return inclusionDependencies;
    }

    private List<String[]> readCsvData(Relation relation) {

        List<String[]> data = new ArrayList<>();
        try (CSVReader csvReader = new CSVReader(new FileReader(System.getProperty("user.dir")+"/data/data_profiling/"+relation.getName()+".csv"))) {
            String[] values;
            while ((values = csvReader.readNext()) != null) {
                values = Arrays.asList(values).get(0).replaceAll("\"", "").split(";");
                values = Arrays.stream(values).map(String::trim).toArray(String[]::new);
                data.add(values);
            }
        } catch (IOException | CsvValidationException e) {
            throw new RuntimeException(e);
        }
        return data;
    }

    private void discoverUnaryInclusionDependencies (Relation r1, Relation r2, Map<String, List<String[]>> relations, List<IND> inclusionDependencies) {
        for (String attr1 : r1.getAttributes()) {
            Set<String> attr1Values = getColumnValues(relations.get(r1.getName()), attr1);

            for (String attr2 : r2.getAttributes()) {
                Set<String> attr2Values = getColumnValues(relations.get(r2.getName()), attr2);

                if (attr2Values.containsAll(attr1Values)) {
                    inclusionDependencies.add(new IND(r1, 1, r2, 1));
                }
            }
        }
    }

    private Set<String> getColumnValues(List<String[]> tableData, String columnName) {

        Set<String> columnValues = new HashSet<>();
        int columnIndex = Arrays.asList(tableData.get(0)).indexOf(columnName);
        if (columnIndex < 0) {
            throw new IllegalArgumentException("Column " + columnName + " does not exist in table data.");
        }
        for (int row = 1; row < tableData.size(); row++) {
            columnValues.add(Arrays.asList(tableData.get(row)).get(columnIndex));
        }
        return columnValues;
    }

    private List<Set<String>> toColumnSets(String[][] columns) {
        return Arrays.stream(columns)
                .map(column -> new HashSet<>(new ArrayList<>(List.of(column))))
                .collect(Collectors.toList());
    }
}
