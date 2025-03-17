package de.bushnaq.abdalla.projecthub;

import de.bushnaq.abdalla.projecthub.dto.Task;
import de.bushnaq.abdalla.projecthub.util.AbstractTestUtil;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;


@ExtendWith(SpringExtension.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@Transactional
public class TaskTest extends AbstractTestUtil {

    @Test
    public void create() throws Exception {

        Task task1 = createTask(null, "Project Phase 1", LocalDateTime.now(), Duration.ofDays(10));
        Task task2 = createTask(task1, "Design", LocalDateTime.now(), Duration.ofDays(4));
        Task task3 = createTask(task1, "Implementation", LocalDateTime.now().plusDays(4), Duration.ofDays(6));

        // Verify the structure
        {
            assertNotNull(task1.getId());
            assertEquals(2, task1.getChildTasks().size());
            assertTrue(task1.getChildTasks().stream()
                    .allMatch(child -> child.getParent().getId().equals(task1.getId())));
        }
        printTables();
    }

    private Task createTask(Task parent, String name, LocalDateTime start, Duration duration) {
        Task task = new Task();
        task.setName(name);
        task.setStart(start);
        task.setDuration(duration);
        task.setFinish(start.plus(duration));
        if (parent != null) {
            // Add the parent to the task
            task.setParent(parent);
        }
        // Save the task
        Task saved = taskApi.persist(task);
        if (parent != null) {
            // Add the task to the parent
            parent.addChildTask(saved);
            // Save the parent
            taskApi.persist(parent);
        }
        return saved;
    }

    @Test
    public void update() throws Exception {

        Task task1 = createTask(null, "Project Phase 1", LocalDateTime.now(), Duration.ofDays(10));
        Task task2 = createTask(task1, "Design", LocalDateTime.now(), Duration.ofDays(4));
        Task task3 = createTask(task1, "Implementation", LocalDateTime.now().plusDays(4), Duration.ofDays(6));

        // Verify the structure
        {
            assertNotNull(task1.getId());
            assertEquals(2, task1.getChildTasks().size());
            assertTrue(task1.getChildTasks().stream()
                    .allMatch(child -> child.getParent().getId().equals(task1.getId())));
        }

        //update
        {
            task2 = task1.getChildTasks().getFirst();
            task3 = task1.getChildTasks().get(1);
            Task oldParent = task3.getParent();
            task2.addChildTask(task3);

            taskApi.persist(task2);
            taskApi.persist(task3);
            taskApi.persist(oldParent);
        }

        // Verify the structure
        {
//            System.out.println(objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(task1));
            Task savedParent2 = taskApi.getTask(task1.getId());
            assertNotNull(savedParent2.getId());
            assertEquals(1, savedParent2.getChildTasks().size());
            assertEquals(savedParent2.getId(), savedParent2.getChildTasks().getFirst().getParent().getId());
            assertEquals(savedParent2.getChildTasks().getFirst().getId(), savedParent2.getChildTasks().getFirst().getChildTasks().getFirst().getParent().getId());
            assertEquals(1, savedParent2.getChildTasks().getFirst().getChildTasks().size());
        }

        printTables();

    }
}