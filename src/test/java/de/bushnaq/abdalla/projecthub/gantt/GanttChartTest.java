package de.bushnaq.abdalla.projecthub.gantt;

import de.bushnaq.abdalla.projecthub.dao.Context;
import de.bushnaq.abdalla.projecthub.dao.ParameterOptions;
import de.bushnaq.abdalla.projecthub.dto.*;
import de.bushnaq.abdalla.projecthub.report.GanttChart;
import de.bushnaq.abdalla.projecthub.util.AbstractEntityGenerator;
import de.bushnaq.abdalla.util.Util;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.io.File;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@Transactional
@TestMethodOrder(MethodOrderer.MethodName.class)
public class GanttChartTest extends AbstractEntityGenerator {
    @Autowired
    private       Context         context;
    private final List<Throwable> exceptions       = new ArrayList<>();
    private       User            resource1;
    private       User            resource2;
    private       Sprint          sprint;
    private final String          testResultFolder = "target/test-results";

    protected void addOneProduct(String sprintName) {
        int count = 1;

        for (int i = 0; i < count; i++) {
            Product product = addProduct("Product " + i);
            Version version = addVersion(product, String.format("1.%d.0", i));
            Project project = addProject(version, String.format("Project-%d", i), String.format("Requester-%d", i));
            sprint = addSprint(project, sprintName);
        }
        testProducts();
    }

    @BeforeEach
    protected void createProductAndUser(TestInfo testInfo) {
        new File(testResultFolder).mkdirs();
        addOneProduct(testInfo.getTestMethod().get().getName());
        addRandomUsers(2);
        initialize();
    }

    @Test
    public void gantt_01(TestInfo testInfo) throws Exception {

        //create tasks
        Task task1 = addParentTask("[1] Parent Task", sprint, null, null);
        Task task2 = addTask("[2] Child Task", Duration.ofDays(5), resource1, sprint, task1, null);
        Task task3 = addTask("[3] Child Task", Duration.ofDays(5), resource2, sprint, task1, task2);

        initialize();

        GanttChart ganttChart = new GanttChart(context, "", "/", "Gantt Chart", sprint.getName(), exceptions,
                ParameterOptions.now, false, sprint, 1887, 1000, "scheduleWithMargin", context.parameters.graphicsTheme);
        ganttChart.generateImage(Util.generateCopyrightString(ParameterOptions.now), testResultFolder);
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

        initialize();

        GanttChart ganttChart = new GanttChart(context, "", "/", "Gantt Chart", sprint.getName(), exceptions,
                ParameterOptions.now, false, sprint, 1887, 1000, "scheduleWithMargin", context.parameters.graphicsTheme);
        ganttChart.generateImage(Util.generateCopyrightString(ParameterOptions.now), testResultFolder);
    }

    private void initialize() {
        List<User>    allUsers    = userApi.getAllUsers();
        List<Product> allProducts = productApi.getAllProducts();
        allProducts.forEach(product -> product.initialize(allUsers));

        sprint    = allProducts.getFirst().getVersions().getFirst().getProjects().getFirst().getSprints().getFirst();
        resource1 = allUsers.getFirst();
        resource2 = allUsers.get(1);
        resourceLeveling();
    }

    private void resourceLeveling() {
        //dummy method to avoid null start/finish
        for (Task task : sprint.getTasks()) {
            if (task.getStart() == null) {
                task.setStart(ParameterOptions.now);
                task.setFinish(task.getStart().plus(task.getDuration()));
            }
        }

    }

}
