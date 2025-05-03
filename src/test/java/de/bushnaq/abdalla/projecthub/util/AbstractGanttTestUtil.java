/*
 *
 * Copyright (C) 2025-2025 Abdalla Bushnaq
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 */

package de.bushnaq.abdalla.projecthub.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import de.bushnaq.abdalla.projecthub.Context;
import de.bushnaq.abdalla.projecthub.ParameterOptions;
import de.bushnaq.abdalla.projecthub.dto.*;
import de.bushnaq.abdalla.projecthub.report.BurnDownChart;
import de.bushnaq.abdalla.projecthub.report.CalendarChart;
import de.bushnaq.abdalla.projecthub.report.GanttChart;
import de.bushnaq.abdalla.projecthub.report.renderer.RenderDao;
import de.bushnaq.abdalla.projecthub.report.renderer.gantt.GanttContext;
import de.bushnaq.abdalla.projecthub.report.renderer.gantt.GanttUtil;
import de.bushnaq.abdalla.util.GanttErrorHandler;
import de.bushnaq.abdalla.util.Util;
import de.bushnaq.abdalla.util.date.DateUtil;
import jakarta.annotation.PostConstruct;
import net.sf.mpxj.ProjectCalendar;
import net.sf.mpxj.ProjectFile;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.TestInfo;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@Transactional
@TestMethodOrder(MethodOrderer.MethodName.class)
public class AbstractGanttTestUtil extends AbstractEntityGenerator {
    private static final String            ANSI_BLUE                 = "\u001B[36m";
    private static final String            ANSI_GREEN                = "\u001B[32m";
    private static final String            ANSI_RED                  = "\u001B[31m";
    private static final String            ANSI_RESET                = "\u001B[0m";    // Declaring ANSI_RESET so that we can reset the color
    private static final String            ANSI_YELLOW               = "\u001B[33m";
    @Autowired
    protected            Context           context;
    public final         DateTimeFormatter dtfymdhmss                = DateTimeFormatter.ofPattern("yyyy.MMM.dd HH:mm:ss.SSS");
    protected final      List<Throwable>   exceptions                = new ArrayList<>();
    protected            User              resource1;
    protected            User              resource2;
    protected            Sprint            sprint;
    protected final      String            testReferenceResultFolder = "test-reference-results";
    protected final      String            testResultFolder          = "test-results";

    protected void addOneProduct(String sprintName) {
        int count = 1;

        for (int i = 0; i < count; i++) {
            Product product = addProduct("Product-" + i);
            Version version = addVersion(product, String.format("1.%d.0", i));
            Project project = addProject(version, String.format("Project-%d", i), String.format("Requester-%d", i));
            sprint = addSprint(project, sprintName);
        }
        testProducts();
    }

    private void compareResults() throws IOException {
        String expectedJson = Files.readString(Paths.get(testReferenceResultFolder, sprint.getName() + ".json"));
        String actualJson   = Files.readString(Paths.get(testResultFolder, sprint.getName() + ".json"));

        try {
            TypeReference<Map<String, Object>> typeRef = new TypeReference<>() {
            };
            Map<String, Object> referenceMap = objectMapper.readValue(expectedJson, typeRef);
            Map<String, Object> map          = objectMapper.readValue(actualJson, typeRef);

            // Compare users
            Map<String, User> referenceUsers = objectMapper.convertValue(referenceMap.get("users"), new TypeReference<Map<String, User>>() {
            });
            Map<String, User> users = objectMapper.convertValue(map.get("users"), new TypeReference<Map<String, User>>() {
            });
            assertEquals(referenceUsers.size(), users.size(), "Number of users differs");
            for (String key : referenceUsers.keySet()) {
                assertTrue(users.containsKey(key), "Missing user: " + referenceUsers.get(key).getName());
                assertUserEquals(referenceUsers.get(key), users.get(key));
            }

            // Compare sprints
            Sprint referenceSprint = objectMapper.convertValue(referenceMap.get("sprint"), Sprint.class);
            Sprint sprint          = objectMapper.convertValue(map.get("sprint"), Sprint.class);
            assertSprintEquals(referenceSprint, sprint);

            // Compare tasks
            List<Task> referenceTasks = objectMapper.convertValue(referenceMap.get("tasks"), new TypeReference<List<Task>>() {
            });
            List<Task> tasks = objectMapper.convertValue(map.get("tasks"), new TypeReference<List<Task>>() {
            });
            for (Task task : tasks)
                sprint.addTask(task);
            for (Task task : referenceTasks)
                referenceSprint.addTask(task);
            {
                GanttContext gc = new GanttContext();
                gc.allUsers   = new ArrayList<>(referenceUsers.values());
                gc.allSprints = List.of(referenceSprint);
                gc.allTasks   = referenceTasks;
                gc.initialize();
            }
            {
                GanttContext gc = new GanttContext();
                gc.allUsers   = new ArrayList<>(users.values());
                gc.allSprints = List.of(sprint);
                gc.allTasks   = tasks;
                gc.initialize();
            }

            logProjectTasks(testResultFolder + "/" + this.sprint.getName() + ".json", sprint, testReferenceResultFolder + "/" + this.sprint.getName() + ".json", referenceSprint);
            compareTasks(tasks, referenceTasks);
        } catch (JsonProcessingException e) {
            fail("Failed to parse JSON: " + e.getMessage());
        }

    }

    protected void compareResults(ProjectFile projectFile) throws IOException {
        compareResults();
    }

    private static void compareTasks(List<Task> tasks, List<Task> referenceTasks) {
        assertEquals(referenceTasks.size(), tasks.size(), "Number of tasks differs");
        for (int i = 0; i < referenceTasks.size(); i++) {
            assertTaskEquals(referenceTasks.get(i), tasks.get(i));
        }
    }

    @BeforeEach
    protected void createProductAndUser(TestInfo testInfo) throws Exception {
//        ParameterOptions.now = OffsetDateTime.parse("1996-03-05T08:00:00");
        ParameterOptions.now = OffsetDateTime.parse("2025-01-01T08:00:00+01:00");
        addOneProduct(sanitise(testInfo.getDisplayName()));
    }

    private RenderDao createRenderDao(Context context, Sprint sprint, String column, LocalDateTime now, int chartWidth, int chartHeight, String link) {
        RenderDao dao = new RenderDao();
        dao.context    = context;
        dao.column     = column;
        dao.sprintName = column + "-" + sprint.getId();
        dao.link       = link;
        dao.start      = sprint.getStart();
        dao.now        = now;
        dao.end        = sprint.getEnd();
        dao.release    = sprint.getReleaseDate();
//        dao.completed          = sprint.isClosed();
        dao.chartWidth         = chartWidth;
        dao.chartHeight        = chartHeight;
        dao.sprint             = sprint;
        dao.estimatedBestWork  = DateUtil.add(sprint.getWorked(), sprint.getRemaining());
        dao.estimatedWorstWork = null;
        dao.maxWorked          = DateUtil.add(sprint.getWorked(), sprint.getRemaining());
        dao.remaining          = sprint.getRemaining();
        dao.worklog            = sprint.getWorklogs();
        dao.worklogRemaining   = sprint.getWorklogRemaining();
//        dao.sprintClosed       = getClosed();
        dao.cssClass      = "scheduleWithMargin";
        dao.graphicsTheme = context.parameters.graphicsTheme;
        return dao;
    }

    protected void generateBurndownChart(TestInfo testInfo) throws Exception {
        initialize();
        RenderDao     dao         = createRenderDao(context, sprint, "burn-down-small", ParameterOptions.getLocalNow(), 0, 36 * 20,  /*urlPrefix +*/ "sprint-" + sprint.getId() + "/sprint.html");
        BurnDownChart chart       = new BurnDownChart("/", dao);
        String        description = testInfo.getDisplayName().replace("_", "-");
        chart.generateImage(Util.generateCopyrightString(ParameterOptions.getLocalNow()), description, testResultFolder);
    }

    protected void generateGanttChart(TestInfo testInfo) throws Exception {
        generateGanttChart(testInfo, null);
    }

    protected void generateGanttChart(TestInfo testInfo, ProjectFile projectFile) throws Exception {
        initialize();
        GanttUtil         ganttUtil = new GanttUtil(context);
        GanttErrorHandler eh        = new GanttErrorHandler();
        ganttUtil.levelResources(eh, sprint, "", ParameterOptions.getLocalNow());

        //save back to the database
        sprint.getTasks().forEach(task -> {
            taskApi.update(task);
        });
        sprintApi.update(sprint);
        printTables();
        initialize();
        if (projectFile == null) {
            storeExpectedResult();
            storeResult();
        }


        GanttChart chart       = new GanttChart(context, "", "/", "Gantt Chart", sprint.getName(), exceptions, ParameterOptions.getLocalNow(), false, sprint/*, 1887, 1000*/, "scheduleWithMargin", context.parameters.graphicsTheme);
        String     description = testInfo.getDisplayName().replace("_", "-");
        chart.generateImage(Util.generateCopyrightString(ParameterOptions.getLocalNow()), description, testResultFolder);
        compareResults(projectFile);
    }

    private int getMaxTaskNameLength(List<Task> taskList) {
        int maxNameLength = 0;
        for (Task task : taskList) {
            if (GanttUtil.isValidTask(task)) {
                maxNameLength = Math.max(maxNameLength, task.getName().length());
            }
        }
        return maxNameLength;
    }

    @PostConstruct
    protected void init() {
        super.init();
        new File(testResultFolder).mkdirs();
        new File(testReferenceResultFolder).mkdirs();
    }

    protected void initialize() throws Exception {
        GanttContext gc = new GanttContext();
        gc.allUsers    = userApi.getAllUsers();
        gc.allProducts = productApi.getAll();
        gc.allVersions = versionApi.getAll();
        gc.allProjects = projectApi.getAll();
        gc.allSprints  = sprintApi.getAll();
        gc.allTasks    = taskApi.getAll();
        gc.allWorklogs = worklogApi.getAll();
        gc.initialize();

        for (User user : gc.allUsers) {
            CalendarChart chart = new CalendarChart(context, ParameterOptions.getLocalNow(), user, "scheduleWithMargin", context.parameters.graphicsTheme);
            chart.generateImage(Util.generateCopyrightString(ParameterOptions.getLocalNow()), "", testResultFolder);
        }


        sprint    = gc.allProducts.getFirst().getVersions().getFirst().getProjects().getFirst().getSprints().getFirst();
        resource1 = gc.allUsers.getFirst();
        resource2 = gc.allUsers.get(1);
    }

    public static boolean isValidTask(net.sf.mpxj.Task task) {
        //ignore task with ID 0
        //ignore tasks that have no name
        //ignore tasks that do not have a start date or finish date
        return task.getID() != 0 && task.getUniqueID() != null && task.getName() != null && task.getStart() != null && task.getFinish() != null && (task.getID() != 1);
    }

    private void logProjectTasks(String fileName, Sprint sprint, String referenceFileName, Sprint referenceSprint) {
        logger.trace("----------------------------------------------------------------------");
        logger.trace("Reference File Name=" + referenceFileName);
        logTasks(referenceSprint.getTasks());
        logger.trace("----------------------------------------------------------------------");
        logger.trace("File Name=" + fileName);
        logTasks(sprint, referenceSprint);
        logger.trace("----------------------------------------------------------------------");
    }

    private void logTask(Task task, Task referenceTask, int maxNameLength) {
        String   buffer         = "";
        String   criticalString = task.isCritical() ? "Y" : "N";
        String   startString    = DateUtil.createDateString(task.getStart(), dtfymdhmss);
        String   finishString   = DateUtil.createDateString(task.getFinish(), dtfymdhmss);
        String   durationString = null;
        Duration duration       = task.getDuration();
        if (duration != null) {
            //            int minutes = (int) ((duration.getDuration() * 7.5 * 60 * 60) / 60);
            //            double seconds = (duration.getDuration() * 7.5 * 60 * 60 - minutes * 60);
            durationString = DateUtil.createDurationString(duration, true, true, true);
        }
        Duration referenceDuration = null;
        if (referenceTask != null) {
            referenceDuration = referenceTask.getDuration();
            //            int minutes = (int) ((duration.getDuration() * 7.5 * 60 * 60) / 60);
            //            double seconds = (duration.getDuration() * 7.5 * 60 * 60 - minutes * 60);
//            String referenceDurationString = DateUtil.createDurationString(referenceDuration, true, true, true);
        }
        String          criticalFlag = ANSI_GREEN;
        String          startFlag    = ANSI_GREEN;
        String          finishFlag   = ANSI_GREEN;
        String          durationFlag = ANSI_GREEN;
        ProjectCalendar calendar     = GanttUtil.getCalendar(task);
        if (referenceTask != null) {
            if (task.getChildTasks().isEmpty() && task.isCritical() != referenceTask.isCritical()) {
                criticalFlag = ANSI_RED;
            }
            if (task.getStart() == null) {
                startFlag = ANSI_RED;
            } else if (!GanttUtil.equals(calendar, task.getStart(), referenceTask.getStart())) {
                startFlag = ANSI_RED;
            } else if (!task.getStart().equals(referenceTask.getStart())) {
                startFlag = ANSI_YELLOW;
            }
            if (task.getFinish() == null) {
                finishFlag = ANSI_RED;
            } else if (!GanttUtil.equals(calendar, task.getFinish(), referenceTask.getFinish())) {
                finishFlag = ANSI_RED;

            } else if (!task.getFinish().equals(referenceTask.getFinish())) {
                finishFlag = ANSI_YELLOW;
            }
            if (task.getDuration() == null) {
                durationFlag = ANSI_RED;
            } else if (!GanttUtil.equals(task.getDuration(), referenceTask.getDuration())) {
                durationFlag = ANSI_RED;
            }

        }
        buffer += String.format("[%2d] N='%-" + maxNameLength + "s' C=%s%s%s S='%s%20s%s' D='%s%-19s%s' F='%s%20s%s'", task.getId(),//
                task.getName(),//
                criticalFlag, criticalString, ANSI_RESET,//
                startFlag, startString, ANSI_RESET,//
                durationFlag, durationString, ANSI_RESET,//
                finishFlag, finishString, ANSI_RESET);
        logger.trace(buffer);
    }

    protected void logTasks(List<Task> taskList) {
        int maxNameLength = getMaxTaskNameLength(taskList);
        for (Task task : taskList) {
            if (GanttUtil.isValidTask(task)) {
                logTask(task, null, maxNameLength);
            }
        }
    }

    protected void logTasks(Sprint sprint, Sprint referenceSprint) {
        int maxNameLength = getMaxTaskNameLength(sprint.getTasks());
        for (Task task : sprint.getTasks()) {
            if (GanttUtil.isValidTask(task)) {
                logTask(task, referenceSprint.getTaskById(task.getId()), maxNameLength);
            }
        }
    }

    private List<String> readStoredData(String directory, String sprintName) throws IOException {
        Path filePath = Paths.get(directory, sprintName + ".json");
        if (Files.exists(filePath)) {
            return Files.readAllLines(filePath, StandardCharsets.UTF_8);
        }
        return new ArrayList<>();
    }

    private String sanitise(String displayName) {
        return displayName.replace("_", "-").replace("\\", "-").replace("/", "-").replace(":", "-");
    }

    private void store(String directory, boolean overwrite) throws IOException {
        Path filePath = Paths.get(directory, sprint.getName() + ".json");
        if (overwrite || !Files.exists(filePath)) {
            Map<String, Object> container = new LinkedHashMap<>();
            container.put("users", sprint.getUserMap());
            container.put("sprint", sprint);
            container.put("tasks", sprint.getTasks());

            String json = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(container);
            Files.writeString(filePath, json, StandardCharsets.UTF_8);
        }
    }

    private void storeExpectedResult() throws IOException {
        store(testResultFolder, true);
    }

    private void storeResult() throws IOException {
        store(testReferenceResultFolder, false);
    }

    @Override
    protected void testAllAndPrintTables() {
        //we do not want to test gantt charts same way as the other tests
    }

}
