package de.bushnaq.abdalla.projecthub.util;

import de.bushnaq.abdalla.projecthub.dto.*;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class AbstractTestUtil extends AbstractEntityGenerator {
    @Autowired
    protected EntityManager entityManager;
    protected List<OffDay>  offDays = new ArrayList<>();

    protected static void assertLocalDateTimeEquals(LocalDateTime expected, LocalDateTime actual) {

//        LocalDateTime expectedFinish = expected != null ? expected.truncatedTo(ChronoUnit.MICROS) : null;
//        LocalDateTime actualFinish   = actual != null ? actual.truncatedTo(ChronoUnit.MICROS) : null;
//        assertEquals(expectedFinish, actualFinish);
        if (expected == null && actual == null) {
            return;
        }
        assertTrue(Math.abs(ChronoUnit.MICROS.between(expected, actual)) < 1,
                () -> String.format("Expected %s but was %s", expected, actual));
    }

    protected static void assertProductEquals(Product expected, Product actual) {
        assertEquals(expected.getId(), actual.getId());
        assertEquals(expected.getName(), actual.getName());
        assertEquals(expected.getVersions().size(), actual.getVersions().size());
        for (int i = 0; i < expected.getVersions().size(); i++) {
            assertVersionEquals(expected.getVersions().get(i), actual.getVersions().get(i));
        }
    }

    protected static void assertProjectEquals(Project expected, Project actual) {
        assertEquals(expected.getId(), actual.getId());
        assertEquals(expected.getName(), actual.getName());
        assertEquals(expected.getRequester(), actual.getRequester());
        assertEquals(expected.getSprints().size(), actual.getSprints().size());
        for (int i = 0; i < expected.getSprints().size(); i++) {
            assertSprintEquals(expected.getSprints().get(i), actual.getSprints().get(i));
        }
    }

    private static void assertRelationEquals(Relation expected, Relation actual) {
        assertEquals(expected.getId(), actual.getId());
        assertEquals(expected.getPredecessorId(), actual.getPredecessorId());
    }

    protected static void assertSprintEquals(Sprint expected, Sprint actual) {
        assertEquals(expected.getEnd(), actual.getEnd());
        assertEquals(expected.getId(), actual.getId());
        assertEquals(expected.getName(), actual.getName());
        assertEquals(expected.getStart(), actual.getStart());
        assertEquals(expected.getStatus(), actual.getStatus());

        assertEquals(expected.getTasks().size(), actual.getTasks().size());
        for (int i = 0; i < expected.getTasks().size(); i++) {
            assertTaskEquals(expected.getTasks().get(i), actual.getTasks().get(i));
        }
    }

    private static void assertTaskEquals(Task expected, Task actual) {
        assertEquals(expected.getChildTasks().size(), actual.getChildTasks().size());
        for (int i = 0; i < expected.getChildTasks().size(); i++) {
            assertTaskEquals(expected.getChildTasks().get(i), actual.getChildTasks().get(i));
        }

        assertEquals(expected.getDuration(), actual.getDuration());
        assertLocalDateTimeEquals(expected.getFinish(), actual.getFinish());
        assertEquals(expected.getId(), actual.getId());
        assertEquals(expected.getName(), actual.getName());
        assertEquals(expected.getParent(), actual.getParent());

        assertEquals(expected.getPredecessors().size(), actual.getPredecessors().size());
        for (int i = 0; i < expected.getPredecessors().size(); i++) {
            assertRelationEquals(expected.getPredecessors().get(i), actual.getPredecessors().get(i));
        }

        assertEquals(expected.getResourceId(), actual.getResourceId());
        assertLocalDateTimeEquals(expected.getStart(), actual.getStart());
    }

    protected static void assertUserEquals(User expected, User actual) {
        assertEquals(expected.getAvailabilities(), actual.getAvailabilities());

        assertEquals(expected.getAvailabilities().size(), actual.getAvailabilities().size());
        for (int i = 0; i < expected.getAvailabilities().size(); i++) {
            assertEquals(expected.getAvailabilities().get(i), actual.getAvailabilities().get(i));
        }

        assertEquals(expected.getEmail(), actual.getEmail());
        assertEquals(expected.getFirstWorkingDay(), actual.getFirstWorkingDay());
        assertEquals(expected.getId(), actual.getId());
        assertEquals(expected.getLastWorkingDay(), actual.getLastWorkingDay());

        assertEquals(expected.getLocations().size(), actual.getLocations().size());
        for (int i = 0; i < expected.getLocations().size(); i++) {
            assertEquals(expected.getLocations().get(i), actual.getLocations().get(i));
        }
        assertEquals(expected.getName(), actual.getName());

        assertEquals(expected.getOffDays().size(), actual.getOffDays().size());
        for (int i = 0; i < expected.getOffDays().size(); i++) {
            assertEquals(expected.getOffDays().get(i), actual.getOffDays().get(i));
        }
    }

    protected static void assertVersionEquals(Version expected, Version actual) {
        assertEquals(expected.getId(), actual.getId());
        assertEquals(expected.getName(), actual.getName());
        assertEquals(expected.getProjects().size(), actual.getProjects().size());
        for (int i = 0; i < expected.getProjects().size(); i++) {
            assertProjectEquals(expected.getProjects().get(i), actual.getProjects().get(i));
        }
    }

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
