package de.bushnaq.abdalla.projecthub.gantt;

import de.bushnaq.abdalla.projecthub.dto.Task;
import de.bushnaq.abdalla.projecthub.util.AbstractGanttUtil;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@Transactional
@TestMethodOrder(MethodOrderer.MethodName.class)
public class GanttChartTest extends AbstractGanttUtil {

    @Test
    public void gantt_01(TestInfo testInfo) throws Exception {

        //create tasks
        Task task1 = addParentTask("[1] Parent Task", sprint, null, null);
        Task task2 = addTask("[2] Child Task", Duration.ofDays(5), resource1, sprint, task1, null);
        Task task3 = addTask("[3] Child Task", Duration.ofDays(5), resource2, sprint, task1, task2);

        generateGanttChart();
    }

    @Test
    public void gantt_02(TestInfo testInfo) throws Exception {

        //create tasks
        Task task1 = addParentTask("[1] Parent Task", sprint, null, null);
        Task task2 = addTask("[2] Child Task ", Duration.ofDays(5), resource1, sprint, task1, null);
        Task task3 = addTask("[3] Child Task ", Duration.ofDays(5), resource2, sprint, task1, task2);

        Task task4 = addParentTask("[4] Parent Task", sprint, null, task1);
        Task task5 = addTask("[5] Child Task ", Duration.ofDays(5), resource1, sprint, task4, null);
        Task task6 = addTask("[6] Child Task ", Duration.ofDays(5), resource2, sprint, task4, task5);

        generateGanttChart();
    }


}
