package de.bushnaq.abdalla.projecthub;

import de.bushnaq.abdalla.projecthub.dto.Task;
import de.bushnaq.abdalla.projecthub.dto.User;
import de.bushnaq.abdalla.projecthub.util.AbstractTestUtil;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;

@ExtendWith(SpringExtension.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@Transactional
public class SprintTest extends AbstractTestUtil {

    @Test
    public void case01() throws Exception {
        User          user1 = addUser();
        User          user2 = addUser();
        LocalDateTime start = LocalDateTime.now();
        Task          task1 = addTask(null, null, "[1] Parent Task", start, Duration.ofDays(0), null, null);
        Task          task2 = addTask(null, task1, "[2] Child Task", start, Duration.ofDays(1), user1, null);
        Task          task3 = addTask(null, task1, "[3] Child Task", start, Duration.ofDays(1), user2, task2);
    }

//    @Test
//    public void create() throws Exception {
//        Project project = createProject();
//
//        // Create sprint
//        {
//            Sprint sprint = new Sprint();
//            sprint.setName("Sprint 1");
//            sprint.setStart(OffsetDateTime.now());
//            sprint.setEnd(OffsetDateTime.now().plusWeeks(2));
//            sprint.setStatus(Status.OPEN);
//            project.getVersions().getFirst().setSprints(List.of(sprint));
//        }
//
//
//        Project createdProject = productApi.persist(project);
//
//        Project retrievedProject = productApi.getProject(createdProject.getId());
//
//        asserEqual(createdProject, retrievedProject);
//        List<Sprint> sprints = retrievedProject.getVersions().get(0).getSprints();
//        assertFalse(retrievedProject.getVersions().get(0).getSprints().isEmpty());
//        Sprint savedSprint = sprints.get(0);
//        assertEquals("Sprint 1", savedSprint.getName());
//        assertEquals(Status.OPEN, savedSprint.getStatus());
//        assertNotNull(savedSprint.getStart());
//        assertNotNull(savedSprint.getEnd());
//
//        printTables();
//    }


}