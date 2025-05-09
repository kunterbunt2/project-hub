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

import com.fasterxml.jackson.databind.ObjectMapper;
import de.bushnaq.abdalla.projecthub.ParameterOptions;
import de.bushnaq.abdalla.projecthub.api.*;
import de.bushnaq.abdalla.projecthub.dto.*;
import de.bushnaq.abdalla.projecthub.report.dao.GraphicsLightTheme;
import de.bushnaq.abdalla.projecthub.report.gantt.GanttContext;
import de.bushnaq.abdalla.util.date.DateUtil;
import jakarta.annotation.PostConstruct;
import net.sf.mpxj.ProjectCalendar;
import net.sf.mpxj.ProjectCalendarException;
import org.ajbrown.namemachine.Name;
import org.ajbrown.namemachine.NameGenerator;
import org.ajbrown.namemachine.NameGeneratorOptions;
import org.junit.jupiter.api.AfterEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.web.server.ServerErrorException;

import java.awt.*;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.util.*;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class AbstractEntityGenerator extends AbstractTestUtil {
    public static final String                FIRST_OFF_DAY_FINISH_DATE = "2024-04-10";
    public static final String                FIRST_OFF_DAY_START_DATE  = "2024-04-01";
    protected           AvailabilityApi       availabilityApi;
    protected final     TreeSet<Availability> expectedAvailabilities    = new TreeSet<>();
    protected final     TreeSet<Location>     expectedLocations         = new TreeSet<>();
    protected           TreeSet<OffDay>       expectedOffDays           = new TreeSet<>();
    protected           List<Product>         expectedProducts          = new ArrayList<>();
    protected           List<Project>         expectedProjects          = new ArrayList<>();
    protected           List<Sprint>          expectedSprints           = new ArrayList<>();
    protected           List<Task>            expectedTasks             = new ArrayList<>();
    protected           TreeSet<User>         expectedUsers             = new TreeSet<>();
    protected           List<Version>         expectedVersions          = new ArrayList<>();
    protected           List<Worklog>         expectedWorklogs          = new ArrayList<>();
    protected           LocationApi           locationApi;
    private             List<Name>            names;
    @Autowired
    protected           ObjectMapper          objectMapper;
    protected           OffDayApi             offDayApi;
    @LocalServerPort
    private             int                   port;
    protected           ProductApi            productApi;
    private static      int                   productIndex              = 0;
    protected           ProjectApi            projectApi;
    private static      int                   projectIndex              = 0;
    protected final     Random                random                    = new Random();
    protected           SprintApi             sprintApi;
    private static      int                   sprintIndex               = 0;
    protected           TaskApi               taskApi;
    @Autowired
    private             TestRestTemplate      testRestTemplate; // Use TestRestTemplate instead of RestTemplate
    protected           UserApi               userApi;
    protected static    int                   userIndex                 = 0;
    protected           VersionApi            versionApi;
    private static      int                   versionIndex              = 0;
    protected           WorklogApi            worklogApi;

    protected void addAvailability(User user, float availability, LocalDate start) {
        Availability a = new Availability(availability, start);
        a.setUser(user);
        a.setCreated(user.getCreated());
        a.setUpdated(user.getUpdated());
        Availability saved = availabilityApi.persist(a, user.getId());
        user.addAvailability(saved);
        expectedAvailabilities.add(saved);
    }

    protected void addLocation(User user, String country, String state, LocalDate start) {
        Location location = new Location(country, state, start);
        location.setUser(user);
        location.setCreated(user.getCreated());
        location.setUpdated(user.getUpdated());
        Location saved = locationApi.persist(location, user.getId());
        user.addLocation(saved);
        expectedLocations.add(saved);
    }

    protected void addOffDay(User user, LocalDate offDayStart, LocalDate offDayFinish, OffDayType type) {
        OffDay offDay = new OffDay(offDayStart, offDayFinish, type);
        offDay.setUser(user);
        offDay.setCreated(user.getCreated());
        offDay.setUpdated(user.getUpdated());
        OffDay saved = offDayApi.persist(offDay, user.getId());
        user.addOffday(saved);
        expectedOffDays.add(saved);
        ProjectCalendarException vacation = user.getCalendar().addCalendarException(offDayStart, offDayFinish);
        switch (type) {
            case VACATION -> vacation.setName("vacation");
            case SICK -> vacation.setName("sick");
            case TRIP -> vacation.setName("trip");
        }
    }

    /**
     * Adds vacation blocks by splitting them when non-working days are encountered
     * Returns the number of actual vacation days used
     */
    private int addOffDayBlockWithSplitting(User user, ProjectCalendar calendar, LocalDate firstDate, LocalDate startDate, int workingDaysCount, OffDayType offDayType) {
        int       daysUsed     = 0;
        LocalDate currentStart = startDate;
        LocalDate currentDate  = startDate;
        boolean   inBlock      = false;

        while (daysUsed < workingDaysCount) {
            boolean isWorkingDay = calendar.isWorkingDate(currentDate);

            if (isWorkingDay) {
                if (!inBlock) {
                    // Start a new block
                    currentStart = currentDate;
                    inBlock      = true;
                }
                daysUsed++;

                // If we've used all the days, add the final block
                if (daysUsed >= workingDaysCount) {
                    addOffDay(user, currentStart, currentDate, offDayType);
//                    logger.info(String.format("%s %s %s", currentStart, currentDate, "vacation"));
                    break;
                }
            } else {
                // We hit a non-working day, end the current block if there is one
                if (inBlock) {
                    LocalDate blockEnd = currentDate.minusDays(1);
                    addOffDay(user, currentStart, blockEnd, offDayType);
//                    logger.info(String.format("%s %s %s", currentStart, blockEnd, "vacation"));
                    inBlock = false;
                }
            }

            currentDate = currentDate.plusDays(1);

            // Safety check to prevent infinite loops
            if (currentDate.isAfter(startDate.plusYears(1))) {
                if (inBlock) {
                    // End any remaining block
                    LocalDate blockEnd = currentDate.minusDays(1);
                    addOffDay(user, currentStart, blockEnd, offDayType);
//                    logger.info(String.format("%s %s %s", currentStart, blockEnd, "vacation"));
                }
                break;
            }
        }

        return daysUsed;
    }

    private void addOffDays(User saved, LocalDate firstDate, int annualVacationDays, int year, OffDayType offDayType, int summerDurationMin, int summerDurationMax) {
        int             remainingDays = annualVacationDays;
        ProjectCalendar pc            = saved.getCalendar();

        LocalDate yearStart = LocalDate.of(year, 1, 1);
        LocalDate yearEnd   = yearStart.plusYears(1).minusDays(1);

        // First add a longer summer vacation block (2-4 weeks)
        int       summerStart         = random.nextInt(60) + 150; // Random start between day 150-210 (June-July)
        LocalDate summerVacationStart = pc.getNextWorkStart(yearStart.plusDays(summerStart).atStartOfDay()).toLocalDate();
        int       summerDuration      = random.nextInt(summerDurationMax - summerDurationMin + 1) + summerDurationMin; // 10-20 days (2-4 weeks)
        summerDuration = Math.min(summerDuration, remainingDays);

        // Add summer vacation with proper splitting of non-working days
        int daysUsed = addOffDayBlockWithSplitting(saved, pc, firstDate, summerVacationStart, summerDuration, offDayType);
        remainingDays -= daysUsed;

        // Distribute remaining days throughout the year in smaller blocks
        while (remainingDays > 0) {
            int       blockDuration = Math.min(remainingDays, random.nextInt(4) + 3); // 3-6 days blocks
            LocalDate startDate;

            do {
                int dayOffset = random.nextInt(365); // Random day in the year
                startDate = pc.getNextWorkStart(yearStart.plusDays(dayOffset).atStartOfDay()).toLocalDate();
            } while (startDate.isAfter(yearEnd) || isOverlapping(saved.getOffDays(), startDate, startDate.plusDays(blockDuration)));

            if (!startDate.isAfter(yearEnd)) {
                // Add vacation block with proper splitting of non-working days
                int actualDaysUsed = addOffDayBlockWithSplitting(saved, pc, firstDate, startDate, blockDuration, offDayType);
                remainingDays -= actualDaysUsed;
            }
        }
    }

    protected Task addParentTask(String name, Sprint sprint, Task parent, Task dependency) {
        return addTask(sprint, parent, name, null, Duration.ofDays(0), null, dependency);
    }

    protected Product addProduct(String name) {
        Product product = new Product();
        product.setName(name);
        product.setCreated(ParameterOptions.now);
        product.setUpdated(ParameterOptions.now);

        Product saved = productApi.persist(product);
        expectedProducts.add(saved);
        productIndex++;
        return saved;
    }

    protected Project addProject(Version version, String name, String requester) {
        Project project = new Project();
        project.setName(name);
        project.setRequester(requester);

        project.setVersion(version);
        project.setVersionId(version.getId());
        project.setCreated(ParameterOptions.now);
        project.setUpdated(ParameterOptions.now);
        Project saved = projectApi.persist(project, version.getId());
        expectedProjects.add(saved);

        version.addProject(saved);

        projectIndex++;
        return saved;
    }

    protected void addRandomProducts(int count) {
        User user1 = addRandomUser();

        for (int i = 0; i < count; i++) {
            Product product = addProduct(generateProductName(productIndex));
            Version version = addVersion(product, String.format("1.%d.0", i));
            Project project = addRandomProject(version);
            Sprint  sprint  = addRandomSprint(project);
            Task    task1   = addTask(sprint, null, "Project Phase 1", LocalDateTime.now(), Duration.ofDays(10), null, null);
            Task    task2   = addTask(sprint, task1, "Design", LocalDateTime.now(), Duration.ofDays(4), user1, null);
            Task    task3   = addTask(sprint, task2, "Implementation", LocalDateTime.now().plusDays(4), Duration.ofDays(6), user1, task1);
        }
        testProducts();
    }

    protected Project addRandomProject(Version version) {
        return addProject(version, generateProjectName(projectIndex), String.format("Requester-%d", projectIndex));
    }

    protected Sprint addRandomSprint(Project project) {
        return addSprint(project, String.format("sprint-%d", sprintIndex));
    }

    /**
     * Adds a random user with a random name and email address and location in de/nw.
     * The user is initialized with a GanttContext and has a vacation off day.
     * The user first working day is set to the given date.
     *
     * @return the created User object
     */
    protected User addRandomUser(LocalDate firstDate) {
        String       name  = generateUserName(userIndex);
        String       email = name + "@project-hub.org";
        User         saved = addUser(name, email, "de", "nw", firstDate, generateUserColor(userIndex), 0.7f);
        GanttContext gc    = new GanttContext();
        gc.initialize();
        saved.initialize(gc);
        addOffDay(saved, LocalDate.parse(FIRST_OFF_DAY_START_DATE), LocalDate.parse(FIRST_OFF_DAY_FINISH_DATE), OffDayType.VACATION);
        return saved;
    }

    /**
     * Adds a random user with a random name and email address and location in de/nw.
     * The user is initialized with a GanttContext and has a vacation off day.
     * The user first working day is set to ParameterOptions.now.
     *
     * @return the created User object
     */
    protected User addRandomUser() {
        String    name      = generateUserName(userIndex);
        String    email     = name + "@project-hub.org";
        LocalDate firstDate = ParameterOptions.now.toLocalDate();

        User         saved = addUser(name, email, "de", "nw", firstDate, generateUserColor(userIndex), 0.7f, LocalDate.parse(FIRST_OFF_DAY_START_DATE), LocalDate.parse(FIRST_OFF_DAY_FINISH_DATE), OffDayType.VACATION);
        GanttContext gc    = new GanttContext();
        gc.initialize();
        saved.initialize(gc);
        generateRandomOffDays(saved, firstDate);
        testUsers();
        return saved;
    }

    /**
     * Adds index random user with a random name and email address and location in de/nw.
     * The user is initialized with a GanttContext and has a vacation off day.
     * The user first working day is set to ParameterOptions.now.
     * The user availability is set to the given value.
     *
     * @return the created User object
     */
    protected User addRandomUser(int index, float availability) {
        String       name      = generateUserName(index);
        String       email     = name + "@project-hub.org";
        LocalDate    firstDate = ParameterOptions.now.toLocalDate().minusYears(1);
        User         saved     = addUser(name, email, "de", "nw", firstDate, generateUserColor(userIndex), availability);
        GanttContext gc        = new GanttContext();
        gc.initialize();
        saved.initialize(gc);
        generateRandomOffDays(saved, firstDate);
        testUsers();
        return saved;
    }

    /**
     * Adds the given number of random users with a random name and email address and location in de/nw.
     * The users are initialized with a GanttContext and have a vacation off day.
     * The users first working day is set to ParameterOptions.now.
     *
     * @param count the number of users to add
     */
    protected void addRandomUsers(int count) {
        for (int i = 0; i < count; i++) {
            String       name      = generateUserName(userIndex);
            String       email     = name + "@project-hub.org";
            LocalDate    firstDate = ParameterOptions.now.toLocalDate().minusYears(1);
            User         saved     = addUser(name, email, "de", "nw", firstDate, generateUserColor(userIndex), 0.5f);
            GanttContext gc        = new GanttContext();
            gc.initialize();
            saved.initialize(gc);
            generateRandomOffDays(saved, firstDate);
        }

        testUsers();
    }

    /**
     * Adds a random version with the given name to the given product.
     *
     * @param product the product to add the version to
     * @return the created Version object
     */
    protected Version addRandomVersion(Product product) {
        return addVersion(product, generateVersionName());
    }

    protected Sprint addSprint(Project project, String sprintName) {
        Sprint sprint = new Sprint();
        sprint.setName(sprintName);
        sprint.setStatus(Status.STARTED);
        sprint.setProject(project);
        sprint.setProjectId(project.getId());
        sprint.setCreated(ParameterOptions.now);
        sprint.setUpdated(ParameterOptions.now);
        Sprint saved = sprintApi.persist(sprint);
        expectedSprints.add(saved);
        project.addSprint(saved);

        sprintIndex++;
        return saved;
    }

    protected Task addTask(String name, String workString, User user, Sprint sprint, Task parent, Task dependency) {
        return addTask(sprint, parent, name, null, DateUtil.parseDurationString(workString, 7.5, 37.5), user, dependency, null, false);
    }

    protected Task addTask(Sprint sprint, Task parent, String name, LocalDateTime start, Duration work, User user, Task dependency) {
        return addTask(sprint, parent, name, start, work, user, dependency, null, false);
    }

    protected Task addTask(Sprint sprint, Task parent, String name, LocalDateTime start, Duration work, User user, Task dependency, TaskMode taskMode, boolean milestone) {
        Task task = new Task();
        task.setName(name);
        task.setStart(start);
        if (work != null) {
            task.setOriginalEstimate(work);
            task.setRemainingEstimate(work);
        }
//        if (work == null || work.equals(Duration.ZERO)) {
//            task.setFinish(start);
//        }
        if (taskMode != null) {
            task.setTaskMode(taskMode);
        }
        task.setMilestone(milestone);
        if (user != null) {
            task.setResourceId(user.getId());
        }
        if (dependency != null) {
            task.addPredecessor(dependency, true);
        }
        if (sprint != null) {
            task.setSprint(sprint);
            task.setSprintId(sprint.getId());
        }
        if (parent != null) {
            task.setParentTask(parent);
            task.setParentTaskId(parent.getId());
        }
        // Save the task
        Task saved = taskApi.persist(task);
        expectedTasks.add(saved);
        if (parent != null) {
            parent.addChildTask(saved);
        }
        if (sprint != null) {
            saved.setSprint(sprint);
            sprint.addTask(saved);
        }
        System.out.printf("Adding %s%n", saved.toString());
        return saved;
    }

    protected User addUser(String name, String email, String country, String state, LocalDate start, Color color, float availability) {
        User user = new User();
        user.setName(name);
        user.setEmail(email);
        user.setFirstWorkingDay(start);
        user.setColor(color);
        user.setCreated(DateUtil.localDateToOffsetDateTime(start).plusHours(8));
        user.setUpdated(DateUtil.localDateToOffsetDateTime(start).plusHours(8));
        User saved = userApi.persist(user);
        addLocation(saved, country, state, start);
        addAvailability(saved, availability, start);

        userIndex++;
        expectedUsers.add(saved);
        return saved;

    }

    protected User addUser(String name, String email, String country, String state, LocalDate start, Color color, float availability, LocalDate offDayStart, LocalDate offDayFinish, OffDayType offDayType) {
        User user = new User();
        user.setName(name);
        user.setEmail(email);
        user.setFirstWorkingDay(start);
        user.setColor(color);
        user.setCreated(DateUtil.localDateToOffsetDateTime(start).plusHours(8));
        user.setUpdated(DateUtil.localDateToOffsetDateTime(start).plusHours(8));
        User saved = userApi.persist(user);
        addLocation(saved, country, state, start);
        addAvailability(saved, availability, start);


        userIndex++;
        expectedUsers.add(saved);
        return saved;

    }

    protected Version addVersion(Product product, String versionName) {
        Version version = new Version();
        version.setName(versionName);
        version.setProduct(product);
        version.setProductId(product.getId());
        version.setCreated(ParameterOptions.now);
        version.setUpdated(ParameterOptions.now);
        Version saved = versionApi.persist(version);
        product.addVersion(saved);
        expectedVersions.add(saved);
        versionIndex++;
        return saved;
    }

    protected Worklog addWorklog(Task task, User user, OffsetDateTime start, Duration timeSpent, String comment) {
        Worklog worklog = new Worklog();
        worklog.setSprintId(task.getSprintId());
        worklog.setTaskId(task.getId());
        worklog.setAuthorId(user.getId());
        worklog.setStart(start);
        worklog.setTimeSpent(timeSpent);
        worklog.setComment(comment);
        task.addWorklog(worklog);
        Worklog saved = worklogApi.persist(worklog);
        expectedWorklogs.add(saved);
        return saved;
    }

    protected static String generateProductName(int index) {
        return String.format("Product-%d", index);
    }

    private static String generateProjectName(int index) {
        return String.format("Project-%d", index);
    }

    private void generateRandomOffDays(User saved, LocalDate employmentDate) {
        int employmentYear = employmentDate.getYear();
        for (int yearIndex = 0; yearIndex < 2; yearIndex++) {
            int year = employmentYear + yearIndex;
            random.setSeed(generateUserYearSeed(saved, year));
            addOffDays(saved, employmentDate, 30, year, OffDayType.VACATION, 10, 20);
            addOffDays(saved, employmentDate, random.nextInt(20), year, OffDayType.SICK, 1, 5);
            addOffDays(saved, employmentDate, random.nextInt(5), year, OffDayType.TRIP, 1, 5);
        }
    }

    protected Color generateUserColor(int userIndex) {
        int index = userIndex % GraphicsLightTheme.KELLY_COLORS.length;
        return GraphicsLightTheme.KELLY_COLORS[index];
    }

    private String generateUserName(int userIndex) {
        return names.get(userIndex).getFirstName() + " " + names.get(userIndex).getLastName();
    }

    private static int generateUserYearSeed(User saved, int year) {
        return (saved.getName() + year).hashCode();
    }

    private static String generateVersionName() {
        return String.format("1.%d.0", versionIndex);
    }

    private LocalDate getNextWorkingDay(ProjectCalendar calendar, LocalDate start, int workingDays) {
        LocalDate current   = start;
        int       daysCount = 0;

        while (daysCount < workingDays) {
            current = current.plusDays(1);
            if (calendar.isWorkingDate(current)) {
                daysCount++;
            }
        }

        return current;
    }

    @PostConstruct
    protected void init() {
        NameGeneratorOptions options = new NameGeneratorOptions();
        ParameterOptions.now = OffsetDateTime.parse("2025-01-01T08:00:00+01:00");
        options.setRandomSeed(123L);//Get deterministic results by setting a random seed.
        NameGenerator generator = new NameGenerator(options);
        names = generator.generateNames(1000);
        // Set the correct port after injection
        productApi      = new ProductApi(testRestTemplate.getRestTemplate(), objectMapper, "http://localhost:" + port);
        projectApi      = new ProjectApi(testRestTemplate.getRestTemplate(), objectMapper, "http://localhost:" + port);
        userApi         = new UserApi(testRestTemplate.getRestTemplate(), objectMapper, "http://localhost:" + port);
        availabilityApi = new AvailabilityApi(testRestTemplate.getRestTemplate(), objectMapper, "http://localhost:" + port);
        locationApi     = new LocationApi(testRestTemplate.getRestTemplate(), objectMapper, "http://localhost:" + port);
        offDayApi       = new OffDayApi(testRestTemplate.getRestTemplate(), objectMapper, "http://localhost:" + port);
        taskApi         = new TaskApi(testRestTemplate.getRestTemplate(), objectMapper, "http://localhost:" + port);
        versionApi      = new VersionApi(testRestTemplate.getRestTemplate(), objectMapper, "http://localhost:" + port);
        sprintApi       = new SprintApi(testRestTemplate.getRestTemplate(), objectMapper, "http://localhost:" + port);
        worklogApi      = new WorklogApi(testRestTemplate.getRestTemplate(), objectMapper, "http://localhost:" + port);
    }

    private boolean isOverlapping(List<OffDay> offDays, LocalDate start, LocalDate end) {
        return offDays.stream().anyMatch(offDay -> !(end.isBefore(offDay.getFirstDay()) || start.isAfter(offDay.getLastDay())));
    }

    /**
     * Move task from its parent to newParent
     *
     * @param task      the task to move
     * @param newParent the new parent
     */
    protected void move(Sprint sprint, Task task, Task newParent) {
        Task oldParent = task.getParentTask();
        newParent.addChildTask(task);

        taskApi.persist(newParent);
        taskApi.persist(task);
        taskApi.persist(oldParent);
    }

    protected void removeAvailability(Availability availability, User user) {
        availabilityApi.deleteById(user, availability);
        user.removeAvailability(availability);
        expectedAvailabilities.remove(availability);
    }

    protected void removeLocation(Location location, User user) {
        locationApi.deleteById(user, location);
        user.removeLocation(location);
        expectedLocations.remove(location);
    }

    protected void removeOffDay(OffDay offDay, User user) {
        offDayApi.deleteById(user, offDay);
        user.removeOffDay(offDay);
        expectedOffDays.remove(offDay);
    }

    protected void removeProduct(Long id) {
        Product productToRemove = expectedProducts.stream().filter(product -> product.getId().equals(id)).findFirst().orElse(null);
        productApi.deleteById(id);

        if (productToRemove != null) {
            // Remove all versions and their projects, sprints, and tasks
            for (Version version : productToRemove.getVersions()) {
                for (Project project : version.getProjects()) {
                    for (Sprint sprint : project.getSprints()) {
                        for (Task task : sprint.getTasks()) {
                            expectedTasks.remove(task);
                        }
                        expectedSprints.remove(sprint);
                    }
                    expectedProjects.remove(project);
                }
                expectedVersions.remove(version);
            }
            expectedProducts.remove(productToRemove);
        }
    }

    protected void removeProject(Long id) {
        Project projectToRemove = expectedProjects.stream().filter(project -> project.getId().equals(id)).findFirst().orElse(null);
        projectApi.deleteById(id);
        if (projectToRemove != null) {
            //remove this project from its parent version
            projectToRemove.getVersion().removeProject(projectToRemove);
            // Remove all sprints and their tasks
            for (Sprint sprint : projectToRemove.getSprints()) {
                for (Task task : sprint.getTasks()) {
                    expectedTasks.remove(task);
                }
                expectedSprints.remove(sprint);
            }
            expectedProjects.remove(projectToRemove);
        }
    }

    protected void removeSprint(Long id) {
        Sprint sprintToRemove = expectedSprints.stream().filter(sprint -> sprint.getId().equals(id)).findFirst().orElse(null);
        sprintApi.deleteById(id);
        if (sprintToRemove != null) {
            //remove this sprint from its parent project
            sprintToRemove.getProject().removePrint(sprintToRemove);
            for (Task task : sprintToRemove.getTasks()) {
                expectedTasks.remove(task);
            }
            expectedSprints.remove(sprintToRemove);
        }
    }

    private void removeTaskTree(Task task) {
        expectedTasks.remove(task);
        for (Task childTask : task.getChildTasks()) {
            removeTaskTree(childTask);
        }
    }

    protected void removeUser(Long id) {
        User userToRemove = expectedUsers.stream().filter(user -> user.getId().equals(id)).findFirst().orElse(null);
        userApi.deleteById(id);

        if (userToRemove != null) {
            // Remove all availabilities
            expectedAvailabilities.removeAll(userToRemove.getAvailabilities());
            // Remove all locations
            expectedLocations.removeAll(userToRemove.getLocations());
            // Remove all off days
            expectedOffDays.removeAll(userToRemove.getOffDays());
            // Remove the user
            expectedUsers.remove(userToRemove);
        }

    }

    protected void removeVersion(Long id) {
        Version versionToRemove = expectedVersions.stream().filter(version -> version.getId().equals(id)).findFirst().orElse(null);
        versionApi.deleteById(id);

        if (versionToRemove != null) {
            //remove this version from its parent product
            versionToRemove.getProduct().removeVersion(versionToRemove);
            // Remove all projects, sprints, and tasks
            for (Project project : versionToRemove.getProjects()) {
                for (Sprint sprint : project.getSprints()) {
                    for (Task task : sprint.getTasks()) {
                        expectedTasks.remove(task);
                    }
                    expectedSprints.remove(sprint);
                }
                expectedProjects.remove(project);
            }
            expectedVersions.remove(versionToRemove);
        }
    }

    protected void testAll() {
        testProducts();
        testUsers();
    }

    @AfterEach
    protected void testAllAndPrintTables() {
        testAll();
        printTables();
    }

    /**
     * ensure products in db match our expectations
     */
    protected void testProducts() {
        GanttContext gc = new GanttContext();
        gc.allUsers    = userApi.getAllUsers().stream().sorted().toList();
        gc.allProducts = productApi.getAll().stream().sorted().toList();
        gc.allVersions = versionApi.getAll().stream().sorted().toList();
        gc.allProjects = projectApi.getAll().stream().sorted().toList();
        gc.allSprints  = sprintApi.getAll().stream().sorted().toList();
        for (Sprint sprint : gc.allSprints) {
            sprint.setWorklogs(worklogApi.getAll(sprint.getId()).stream().sorted().toList());
        }
        gc.allTasks = taskApi.getAll().stream().sorted().toList();
        gc.initialize();

        expectedProducts.sort(Comparator.naturalOrder());
        expectedProjects.sort(Comparator.naturalOrder());
        expectedSprints.sort(Comparator.naturalOrder());
        expectedTasks.sort(Comparator.naturalOrder());
        expectedVersions.sort(Comparator.naturalOrder());
        expectedWorklogs.sort(Comparator.naturalOrder());

        assertEquals(expectedProducts.size(), gc.allProducts.size());
        for (int i = 0; i < expectedProducts.size(); i++) {
            assertProductEquals(expectedProducts.get(i), gc.allProducts.get(i));
        }
    }

    protected void testUsers() {
        entityManager.clear();//clear the cache to get the latest data from the database
        List<User> actual = userApi.getAllUsers();

        assertEquals(expectedUsers.size(), actual.size());
        int i = 0;
        for (User expectedUser : expectedUsers) {
            assertUserEquals(expectedUser, actual.get(i++));
        }
    }

    protected void updateAvailability(Availability availability, User user) {
        availabilityApi.update(availability, user.getId());
        expectedAvailabilities.remove(availability);
        expectedAvailabilities.add(availability);
    }

    protected void updateLocation(Location location, User user) throws ServerErrorException {
        locationApi.update(location, user.getId());
        expectedLocations.remove(location);
        expectedLocations.add(location);
    }

    protected void updateOffDay(OffDay offDay, User user) throws ServerErrorException {
        offDayApi.update(offDay, user.getId());
        expectedOffDays.remove(offDay);
        expectedOffDays.add(offDay);
    }

    protected void updateProduct(Product product) throws ServerErrorException {
        productApi.update(product);
        expectedProducts.remove(product);
        expectedProducts.add(product);//replace old products with the updated one
    }

    protected void updateProject(Project project) {
        projectApi.update(project);
        expectedProjects.remove(project);
        expectedProjects.add(project);//replace old products with the updated one
    }

    protected void updateSprint(Sprint sprint) throws ServerErrorException {
        sprintApi.update(sprint);
        expectedSprints.remove(sprint);
        expectedSprints.add(sprint); // Replace old sprint with the updated one
    }

    protected void updateUser(User user) throws ServerErrorException {
        userApi.update(user);
        expectedUsers.remove(user);
        expectedUsers.add(user);//replace old user with the updated one
    }

    protected void updateVersion(Version version) {
        versionApi.update(version);
        expectedVersions.remove(version);
        expectedVersions.add(version);//replace old products with the updated one
    }

}
