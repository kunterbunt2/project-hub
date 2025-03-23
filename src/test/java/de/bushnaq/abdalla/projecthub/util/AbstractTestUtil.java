package de.bushnaq.abdalla.projecthub.util;

import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

public class AbstractTestUtil extends DTOAsserts {
    @Autowired
    protected EntityManager entityManager;

    @BeforeEach
    protected void clearDatabase() {
        List<String> tableNames = getAllTableNames();
        entityManager.createNativeQuery("SET REFERENTIAL_INTEGRITY FALSE").executeUpdate();
        for (String tableName : tableNames) {
            entityManager.createNativeQuery("TRUNCATE TABLE " + tableName).executeUpdate();
        }
        entityManager.createNativeQuery("SET REFERENTIAL_INTEGRITY TRUE").executeUpdate();
        entityManager.flush();
    }

    protected List<String> getAllTableNames() {
        // Get all table names
        @SuppressWarnings("unchecked") List<String> tableNames = entityManager.createNativeQuery("SELECT TABLE_NAME FROM INFORMATION_SCHEMA.TABLES WHERE TABLE_SCHEMA = SCHEMA()").getResultList();

        // Filter out system tables
        tableNames = tableNames.stream().filter(name -> !name.equals("SPATIAL_REF_SYS") && !name.equals("GEOMETRY_COLUMNS")).toList();
        return tableNames;
    }

    protected void printTables() {
        System.out.println("\n\n\n");
        try {
            String        separator = "+";
            StringBuilder output    = new StringBuilder("\n=== Database Content ===\n");

            List<String> tableNames = getAllTableNames();

            for (String tableName : tableNames) {
                // Get column names
                @SuppressWarnings("unchecked") List<String> columnNames = entityManager.createNativeQuery("SELECT COLUMN_NAME FROM INFORMATION_SCHEMA.COLUMNS " + "WHERE TABLE_SCHEMA = SCHEMA() AND TABLE_NAME = '" + tableName + "' " + "ORDER BY ORDINAL_POSITION").getResultList();

                // Get all data
                String  dataQuery = "SELECT * FROM " + tableName;
                List<?> results   = entityManager.createNativeQuery(dataQuery).getResultList();

                // Calculate max width for each column
                int[] maxWidths = new int[columnNames.size()];
                // Initialize with column name lengths
                for (int i = 0; i < columnNames.size(); i++) {
                    maxWidths[i] = columnNames.get(i).length();
                }

                // Check data widths
                for (Object row : results) {
                    if (row instanceof Object[] cells) {
                        for (int i = 0; i < cells.length; i++) {
                            String value = cells[i] != null ? cells[i].toString() : "null";
                            maxWidths[i] = Math.max(maxWidths[i], value.length());
                        }
                    } else {
                        maxWidths[0] = Math.max(maxWidths[0], row != null ? row.toString().length() : 4);
                    }
                }

                // Create dynamic separator
                separator = "+";
                for (int width : maxWidths) {
                    separator += "-".repeat(width + 2) + "+";
                }
                separator += "\n";


                if (!results.isEmpty()) {
                    // Print table
                    output.append("\n").append(tableName).append(":\n").append(separator);
                    // Print header
                    output.append("|");
                    for (int i = 0; i < columnNames.size(); i++) {
                        output.append(String.format(" %-" + maxWidths[i] + "s |", columnNames.get(i)));
                    }
                    output.append("\n").append(separator);
                    // Print data
                    for (Object row : results) {
                        output.append("|");
                        if (row instanceof Object[] cells) {
                            for (int i = 0; i < cells.length; i++) {
                                String value = cells[i] != null ? cells[i].toString() : "null";
                                output.append(String.format(" %-" + maxWidths[i] + "s |", value));
                            }
                        } else {
                            output.append(String.format(" %-" + maxWidths[0] + "s |", row != null ? row.toString() : "null"));
                        }
                        output.append("\n");
                    }
                    output.append(separator);
                }

            }

            System.out.println(output);
        } catch (Exception e) {
            System.err.println("Error printing tables: " + e.getMessage());
            e.printStackTrace();
        }
    }

}
