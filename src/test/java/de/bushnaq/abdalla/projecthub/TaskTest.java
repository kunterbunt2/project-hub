package de.bushnaq.abdalla.projecthub;

import de.bushnaq.abdalla.projecthub.dto.*;
import de.bushnaq.abdalla.projecthub.util.AbstractEntityGenerator;
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
public class TaskTest extends AbstractEntityGenerator {

    @Test
    public void create() throws Exception {
        User user1 = addRandomUser();

        for (int i = 0; i < 1; i++) {
            Product product = addProduct("Product " + i);
            Version version = addVersion(product, String.format("1.%d.0", i));
            Project project = addRandomProject(version);
            Sprint  sprint  = addRandomSprint(project);

            Task task1 = addTask(sprint, null, "Project Phase 1", LocalDateTime.now(), Duration.ofDays(10), null, null);
            Task task2 = addTask(sprint, task1, "Design", LocalDateTime.now(), Duration.ofDays(4), user1, null);
            Task task3 = addTask(sprint, task1, "Implementation", LocalDateTime.now().plusDays(4), Duration.ofDays(6), user1, task1);
        }

        // Verify the structure
//        {
//            assertNotNull(task1.getId());
//            assertEquals(2, task1.getChildTasks().size());
//            assertTrue(task1.getChildTasks().stream()
//                    .allMatch(child -> child.getParent().getId().equals(task1.getId())));
//        }
        printTables();
        testAll();
    }


    @Test
    public void update() throws Exception {
        User user1 = addRandomUser();

        for (int i = 0; i < 1; i++) {
            Product product = addProduct("Product " + i);
            Version version = addVersion(product, String.format("1.%d.0", i));
            Project project = addRandomProject(version);
            Sprint  sprint  = addRandomSprint(project);
            Task    task1   = addTask(sprint, null, "Project Phase 1", LocalDateTime.now(), Duration.ofDays(10), null, null);
            Task    task2   = addTask(sprint, task1, "Design", LocalDateTime.now(), Duration.ofDays(4), user1, null);
            Task    task3   = addTask(sprint, task1, "Implementation", LocalDateTime.now().plusDays(4), Duration.ofDays(6), user1, task1);
        }

        testAll();
        // Verify the structure
//        {
//            assertNotNull(task1.getId());
//            assertEquals(2, task1.getChildTasks().size());
//            assertTrue(task1.getChildTasks().stream()
//                    .allMatch(child -> child.getParent().getId().equals(task1.getId())));
//        }

        //update
        {
//            task2 = task1.getChildTasks().getFirst();
//            task3 = task1.getChildTasks().get(1);
            move(expectedSprints.getFirst(), expectedTasks.get(2), expectedTasks.get(1));
        }

        // Verify the structure
//        {
////            System.out.println(objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(task1));
//            Task savedParent2 = taskApi.getTask(task1.getId());
//            assertNotNull(savedParent2.getId());
//            assertEquals(1, savedParent2.getChildTasks().size());
//            assertEquals(savedParent2.getId(), savedParent2.getChildTasks().getFirst().getParent().getId());
//            assertEquals(savedParent2.getChildTasks().getFirst().getId(), savedParent2.getChildTasks().getFirst().getChildTasks().getFirst().getParent().getId());
//            assertEquals(1, savedParent2.getChildTasks().getFirst().getChildTasks().size());
//        }
        printTables();
        testAll();

    }

}