package de.bushnaq.abdalla.projecthub;

import de.bushnaq.abdalla.projecthub.dto.Project;
import de.bushnaq.abdalla.projecthub.dto.Sprint;
import de.bushnaq.abdalla.projecthub.dto.Status;
import de.bushnaq.abdalla.projecthub.util.AbstractTestUtil;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(SpringExtension.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@Transactional
public class SprintTest extends AbstractTestUtil {

    @Test
    public void shouldCreateSprintWithinVersion() throws Exception {
        Project project = createProject();

        // Create sprint
        {
            Sprint sprint = new Sprint();
            sprint.setName("Sprint 1");
            sprint.setStart(OffsetDateTime.now());
            sprint.setEnd(OffsetDateTime.now().plusWeeks(2));
            sprint.setStatus(Status.OPEN);
            project.getVersions().getFirst().setSprints(List.of(sprint));
        }


        Project createdProject = projectApi.persist(project);

        Project retrievedProject = projectApi.getProject(createdProject.getId());

        asserEqual(createdProject, retrievedProject);
        List<Sprint> sprints = retrievedProject.getVersions().get(0).getSprints();
        assertFalse(retrievedProject.getVersions().get(0).getSprints().isEmpty());
        Sprint savedSprint = sprints.get(0);
        assertEquals("Sprint 1", savedSprint.getName());
        assertEquals(Status.OPEN, savedSprint.getStatus());
        assertNotNull(savedSprint.getStart());
        assertNotNull(savedSprint.getEnd());

        printTables();
    }

}