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

import static org.junit.jupiter.api.Assertions.*;


@ExtendWith(SpringExtension.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@Transactional
public class TaskTest extends AbstractTestUtil {

    @Test
    public void create() throws Exception {
        User user1 = createUser();

        Task task1 = createTask(null, "Project Phase 1", LocalDateTime.now(), Duration.ofDays(10), null, null);
        Task task2 = createTask(task1, "Design", LocalDateTime.now(), Duration.ofDays(4), user1, null);
        Task task3 = createTask(task1, "Implementation", LocalDateTime.now().plusDays(4), Duration.ofDays(6), user1, task1);

        // Verify the structure
        {
            assertNotNull(task1.getId());
            assertEquals(2, task1.getChildTasks().size());
            assertTrue(task1.getChildTasks().stream()
                    .allMatch(child -> child.getParent().getId().equals(task1.getId())));
        }
        printTables();
    }

    @Test
    public void update() throws Exception {
        User user1 = createUser();

        Task task1 = createTask(null, "Project Phase 1", LocalDateTime.now(), Duration.ofDays(10), null, null);
        Task task2 = createTask(task1, "Design", LocalDateTime.now(), Duration.ofDays(4), user1, null);
        Task task3 = createTask(task1, "Implementation", LocalDateTime.now().plusDays(4), Duration.ofDays(6), user1, task1);

        // Verify the structure
        {
            assertNotNull(task1.getId());
            assertEquals(2, task1.getChildTasks().size());
            assertTrue(task1.getChildTasks().stream()
                    .allMatch(child -> child.getParent().getId().equals(task1.getId())));
        }

        //update
        {
//            task2 = task1.getChildTasks().getFirst();
//            task3 = task1.getChildTasks().get(1);
            move(task3, task2);
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