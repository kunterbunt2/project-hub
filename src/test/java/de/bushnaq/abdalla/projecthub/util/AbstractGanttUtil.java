package de.bushnaq.abdalla.projecthub.util;

import de.bushnaq.abdalla.projecthub.dao.Context;
import de.bushnaq.abdalla.projecthub.dao.ParameterOptions;
import de.bushnaq.abdalla.projecthub.dto.*;
import de.bushnaq.abdalla.projecthub.gantt.GanttUtil;
import de.bushnaq.abdalla.projecthub.report.GanttChart;
import de.bushnaq.abdalla.util.GanttErrorHandler;
import de.bushnaq.abdalla.util.Util;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.TestInfo;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@Transactional
@TestMethodOrder(MethodOrderer.MethodName.class)
public class AbstractGanttUtil extends AbstractEntityGenerator {
    @Autowired
    protected       Context         context;
    protected final List<Throwable> exceptions       = new ArrayList<>();
    protected       User            resource1;
    protected       User            resource2;
    protected       Sprint          sprint;
    protected final String          testResultFolder = "target/test-results";

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

    protected void generateGanttChart() throws Exception {
        initialize();
        GanttUtil         ganttUtil = new GanttUtil(context);
        GanttErrorHandler eh        = new GanttErrorHandler();
        ganttUtil.calculateCriticalPath(eh, sprint, "", ParameterOptions.now);
        GanttChart ganttChart = new GanttChart(context, "", "/", "Gantt Chart", sprint.getName(), exceptions,
                ParameterOptions.now, false, sprint, 1887, 1000, "scheduleWithMargin", context.parameters.graphicsTheme);
        ganttChart.generateImage(Util.generateCopyrightString(ParameterOptions.now), testResultFolder);
    }

    protected void initialize() {
        List<User>    allUsers    = userApi.getAllUsers();
        List<Product> allProducts = productApi.getAllProducts();
        allProducts.forEach(product -> product.initialize(allUsers));

        sprint    = allProducts.getFirst().getVersions().getFirst().getProjects().getFirst().getSprints().getFirst();
        resource1 = allUsers.getFirst();
        resource2 = allUsers.get(1);
//        resourceLeveling();
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
