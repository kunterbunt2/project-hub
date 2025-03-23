package de.bushnaq.abdalla.projecthub.util;

import de.bushnaq.abdalla.projecthub.dto.*;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class DTOAsserts {

    private static void assertAvailabilityEquals(Availability expected, Availability actual) {
        assertEquals(expected.getId(), actual.getId(), "Availability IDs do not match");
        assertEquals(expected.getAvailability(), actual.getAvailability(), "Availability values do not match");
        assertEquals(expected.getStart(), actual.getStart(), "Availability start dates do not match");
//        assertEquals(expected.getUser().getId(), actual.getUser().getId(), "Availability user IDs do not match");
    }

    protected static void assertLocalDateTimeEquals(LocalDateTime expected, LocalDateTime actual) {
        if (expected == null && actual == null) {
            return;
        }
        assertTrue(Math.abs(ChronoUnit.MICROS.between(expected, actual)) < 1, () -> String.format("Expected %s but was %s", expected, actual));
    }

    private static void assertLocationEquals(Location expected, Location actual) {
        assertEquals(expected.getCountry(), actual.getCountry(), "Location countries do not match");
        assertEquals(expected.getId(), actual.getId(), "Location IDs do not match");
        assertEquals(expected.getState(), actual.getState(), "Location states do not match");
        assertEquals(expected.getStart(), actual.getStart());
//        assertEquals(expected.getUser().getId(), actual.getUser().getId(), "Location user IDs do not match");
    }

    private static void assertOffDayEquals(OffDay expected, OffDay actual) {
        assertEquals(expected.getId(), actual.getId());
        assertEquals(expected.getFirstDay(), actual.getFirstDay());
        assertEquals(expected.getLastDay(), actual.getLastDay());
        assertEquals(expected.getType(), actual.getType());
//        assertEquals(expected.getUser().getId(), actual.getUser().getId(), "OffDay user IDs do not match");
    }

    protected static void assertProductEquals(Product expected, Product actual) {
        assertEquals(expected.getId(), actual.getId(), "Product IDs do not match");
        assertEquals(expected.getName(), actual.getName(), "Product names do not match");
        assertEquals(expected.getVersions().size(), actual.getVersions().size(), "Number of versions in product do not match");
        for (int i = 0; i < expected.getVersions().size(); i++) {
            assertVersionEquals(expected.getVersions().get(i), actual.getVersions().get(i));
        }
    }

    protected static void assertProjectEquals(Project expected, Project actual) {
        assertEquals(expected.getId(), actual.getId(), "Project IDs do not match");
        assertEquals(expected.getName(), actual.getName(), "Project names do not match");
        assertEquals(expected.getRequester(), actual.getRequester(), "Project requesters do not match");
        assertEquals(expected.getSprints().size(), actual.getSprints().size(), "Number of sprints in project do not match");
        for (int i = 0; i < expected.getSprints().size(); i++) {
            assertSprintEquals(expected.getSprints().get(i), actual.getSprints().get(i));
        }
    }

    protected static void assertRelationEquals(Relation expected, Relation actual) {
        assertEquals(expected.getId(), actual.getId(), "Relation IDs do not match");
        assertEquals(expected.getPredecessorId(), actual.getPredecessorId(), "Relation predecessor IDs do not match");
    }

    protected static void assertSprintEquals(Sprint expected, Sprint actual) {
        assertEquals(expected.getEnd(), actual.getEnd(), "Sprint end dates do not match");
        assertEquals(expected.getId(), actual.getId(), "Sprint IDs do not match");
        assertEquals(expected.getName(), actual.getName(), "Sprint names do not match");
        assertEquals(expected.getStart(), actual.getStart(), "Sprint start dates do not match");
        assertEquals(expected.getStatus(), actual.getStatus(), "Sprint status values do not match");

        //TODO reintroduce tests
//        assertEquals(expected.getTasks().size(), actual.getTasks().size(), "Number of tasks in sprint do not match");
//        for (int i = 0; i < expected.getTasks().size(); i++) {
//            assertTaskEquals(expected.getTasks().get(i), actual.getTasks().get(i));
//        }
    }

    protected static void assertTaskEquals(Task expected, Task actual) {
        assertEquals(expected.getChildTasks().size(), actual.getChildTasks().size(), "Number of child tasks do not match");
        for (int i = 0; i < expected.getChildTasks().size(); i++) {
            assertTaskEquals(expected.getChildTasks().get(i), actual.getChildTasks().get(i));
        }

        assertEquals(expected.getDuration(), actual.getDuration(), "Task durations do not match");
        assertLocalDateTimeEquals(expected.getFinish(), actual.getFinish());
        assertEquals(expected.getId(), actual.getId(), "Task IDs do not match");
        assertEquals(expected.getName(), actual.getName(), "Task names do not match");
        assertEquals(expected.getParent(), actual.getParent(), "Task parents do not match");

        assertEquals(expected.getPredecessors().size(), actual.getPredecessors().size(), "Number of task predecessors do not match");
        for (int i = 0; i < expected.getPredecessors().size(); i++) {
            assertRelationEquals(expected.getPredecessors().get(i), actual.getPredecessors().get(i));
        }

        assertEquals(expected.getResourceId(), actual.getResourceId(), "Task resource IDs do not match");
        assertLocalDateTimeEquals(expected.getStart(), actual.getStart());
    }

    protected static void assertUserEquals(User expected, User actual) {
//        assertEquals(expected.getAvailabilities(), actual.getAvailabilities(), "User availabilities do not match");

        assertEquals(expected.getAvailabilities().size(), actual.getAvailabilities().size(), "Number of user availabilities do not match");
        for (int i = 0; i < expected.getAvailabilities().size(); i++) {
            assertAvailabilityEquals(expected.getAvailabilities().get(i), actual.getAvailabilities().get(i));
        }

        assertEquals(expected.getEmail(), actual.getEmail(), "User emails do not match");
        assertEquals(expected.getFirstWorkingDay(), actual.getFirstWorkingDay(), "User first working days do not match");
        assertEquals(expected.getId(), actual.getId(), "User IDs do not match");
        assertEquals(expected.getLastWorkingDay(), actual.getLastWorkingDay(), "User last working days do not match");

        assertEquals(expected.getLocations().size(), actual.getLocations().size(), "Number of user locations do not match");
        for (int i = 0; i < expected.getLocations().size(); i++) {
            assertLocationEquals(expected.getLocations().get(i), actual.getLocations().get(i));
        }

        assertEquals(expected.getName(), actual.getName(), "User names do not match");

        assertEquals(expected.getOffDays().size(), actual.getOffDays().size(), "Number of user off days do not match");
        for (int i = 0; i < expected.getOffDays().size(); i++) {
            assertOffDayEquals(expected.getOffDays().get(i), actual.getOffDays().get(i));
        }
    }

    protected static void assertVersionEquals(Version expected, Version actual) {
        assertEquals(expected.getId(), actual.getId(), "Version IDs do not match");
        assertEquals(expected.getName(), actual.getName(), "Version names do not match");
        assertEquals(expected.getProjects().size(), actual.getProjects().size(), "Number of projects in version do not match");
        for (int i = 0; i < expected.getProjects().size(); i++) {
            assertProjectEquals(expected.getProjects().get(i), actual.getProjects().get(i));
        }
    }
}
