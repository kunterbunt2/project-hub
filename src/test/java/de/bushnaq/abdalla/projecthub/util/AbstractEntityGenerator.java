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
import de.bushnaq.abdalla.projecthub.api.*;
import de.bushnaq.abdalla.projecthub.dao.ParameterOptions;
import de.bushnaq.abdalla.projecthub.dto.*;
import de.bushnaq.abdalla.projecthub.report.renderer.gantt.GanttContext;
import de.bushnaq.abdalla.util.date.DateUtil;
import jakarta.annotation.PostConstruct;
import net.sf.mpxj.ProjectCalendar;
import net.sf.mpxj.ProjectCalendarException;
import org.ajbrown.namemachine.Name;
import org.ajbrown.namemachine.NameGenerator;
import org.ajbrown.namemachine.NameGeneratorOptions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.TreeSet;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class AbstractEntityGenerator extends AbstractTestUtil {
    public static final String                FIRST_OFF_DAY_FINISH_DATE = "2024-04-10";
    public static final String                FIRST_OFF_DAY_START_DATE  = "2024-04-01";
    protected final     TreeSet<Availability> expectedAvailabilities    = new TreeSet<>();
    protected final     TreeSet<Location>     expectedLocations         = new TreeSet<>();
    protected           TreeSet<OffDay>       expectedOffDays           = new TreeSet<>();
    protected           List<Product>         expectedProducts          = new ArrayList<>();
    protected           List<Project>         expectedProjects          = new ArrayList<>();
    protected           List<Sprint>          expectedSprints           = new ArrayList<>();
    protected           List<Task>            expectedTasks             = new ArrayList<>();
    protected           TreeSet<User>         expectedUsers             = new TreeSet<>();
    protected           List<Version>         expectedVersions          = new ArrayList<>();
    private             List<Name>            names;
    @Autowired
    protected           ObjectMapper          objectMapper;
    @LocalServerPort
    private             int                   port;
    protected           ProductApi            productApi;
    private static      int                   productIndex              = 0;
    protected           ProjectApi            projectApi;
    private static      int                   projectIndex              = 0;
    private final       Random                random                    = new Random();
    protected           SprintApi             sprintApi;
    private static      int                   sprintIndex               = 0;
    protected           TaskApi               taskApi;
    @Autowired
    private             TestRestTemplate      testRestTemplate; // Use TestRestTemplate instead of RestTemplate
    protected           UserApi               userApi;
    protected static    int                   userIndex                 = 0;
    protected           VersionApi            versionApi;
    private static      int                   versionIndex              = 0;

    protected void addAvailability(User user, float availability, LocalDate start) {
        Availability a = new Availability(availability, start);
        a.setUser(user);
        a.setCreated(user.getCreated());
        a.setUpdated(user.getUpdated());
        Availability saved = userApi.persist(a, user.getId());
        user.addAvailability(saved);
        expectedAvailabilities.add(saved);
    }

    protected void addLocation(User user, String country, String state, LocalDate start) {
        Location location = new Location(country, state, start);
        location.setUser(user);
        location.setCreated(user.getCreated());
        location.setUpdated(user.getUpdated());
        Location saved = userApi.persist(location, user.getId());
        user.addLocation(saved);
        expectedLocations.add(saved);
    }

    protected void addOffDay(User user, LocalDate offDayStart, LocalDate offDayFinish, OffDayType type) {
        OffDay offDay = new OffDay(offDayStart, offDayFinish, type);
        offDay.setUser(user);
        offDay.setCreated(user.getCreated());
        offDay.setUpdated(user.getUpdated());
        OffDay saved = userApi.persist(offDay, user.getId());
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
            Product product = addProduct("Product " + i);
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
        return addProject(version, String.format("Project-%d", projectIndex), String.format("Requester-%d", projectIndex));
    }

    protected Sprint addRandomSprint(Project project) {
        return addSprint(project, String.format("sprint-%d", sprintIndex));
    }

    protected User addRandomUser(LocalDate firstDate) {
        String       name  = names.get(userIndex).getFirstName() + " " + names.get(userIndex).getLastName();
        String       email = name + "@project-hub.org";
        User         saved = addUser(name, email, "de", "nw", firstDate, 0.7f);
        GanttContext gc    = new GanttContext();
        gc.initialize();
        saved.initialize(gc);
        addOffDay(saved, LocalDate.parse(FIRST_OFF_DAY_START_DATE), LocalDate.parse(FIRST_OFF_DAY_FINISH_DATE), OffDayType.VACATION);
        return saved;
    }

    protected User addRandomUser() {
        String    name      = names.get(userIndex).getFirstName() + " " + names.get(userIndex).getLastName();
        String    email     = name + "@project-hub.org";
        LocalDate firstDate = LocalDate.now();

        User         saved = addUser(name, email, "de", "nw", firstDate, 0.7f, LocalDate.parse(FIRST_OFF_DAY_START_DATE), LocalDate.parse(FIRST_OFF_DAY_FINISH_DATE), OffDayType.VACATION);
        GanttContext gc    = new GanttContext();
        gc.initialize();
        saved.initialize(gc);
        generateRandomOffDays(saved, firstDate);
        testUsers();
        return saved;
    }

    protected User addRandomUser(int index, float availability) {
        String       name      = names.get(index).getFirstName() + " " + names.get(index).getLastName();
        String       email     = name + "@project-hub.org";
        LocalDate    firstDate = ParameterOptions.now.toLocalDate().minusYears(1);
        User         saved     = addUser(name, email, "de", "nw", firstDate, availability);
        GanttContext gc        = new GanttContext();
        gc.initialize();
        saved.initialize(gc);
        generateRandomOffDays(saved, firstDate);
        testUsers();
        return saved;
    }

    protected void addRandomUsers(int count) {
        for (int i = 0; i < count; i++) {
            String       name      = names.get(userIndex).getFirstName() + " " + names.get(userIndex).getLastName();
            String       email     = name + "@project-hub.org";
            LocalDate    firstDate = ParameterOptions.now.toLocalDate().minusYears(1);
            User         saved     = addUser(name, email, "de", "nw", firstDate, 0.5f);
            GanttContext gc        = new GanttContext();
            gc.initialize();
            saved.initialize(gc);
            generateRandomOffDays(saved, firstDate);
        }

        testUsers();
    }

    protected Version addRandomVersion(Product product) {
        return addVersion(product, String.format("1.%d.0", versionIndex));
    }

    protected Sprint addSprint(Project project, String sprintName) {
        Sprint sprint = new Sprint();
        sprint.setName(sprintName);
        sprint.setStatus(Status.OPEN);
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
            task.setWork(work);
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
            sprint.addTask(saved);
        }
        return saved;
    }

    protected User addUser(String name, String email, String country, String state, LocalDate start, float availability) {
        User user = new User();
        user.setName(name);
        user.setEmail(email);
        user.setFirstWorkingDay(start);
        user.setCreated(DateUtil.localDateToOffsetDateTime(start).plusHours(8));
        user.setUpdated(DateUtil.localDateToOffsetDateTime(start).plusHours(8));
        User saved = userApi.persist(user);
        addLocation(saved, country, state, start);
        addAvailability(saved, availability, start);

        userIndex++;
        expectedUsers.add(saved);
        return saved;

    }

    protected User addUser(String name, String email, String country, String state, LocalDate start, float availability, LocalDate offDayStart, LocalDate offDayFinish, OffDayType offDayType) {
        User user = new User();
        user.setName(name);
        user.setEmail(email);
        user.setFirstWorkingDay(start);
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
        Version saved = versionApi.persist(version, product.getId());
        product.addVersion(saved);
        expectedVersions.add(saved);
        versionIndex++;
        return saved;
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

    private static int generateUserYearSeed(User saved, int year) {
        return (saved.getName() + year).hashCode();
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
    private void init() {
        NameGeneratorOptions options = new NameGeneratorOptions();
        ParameterOptions.now = OffsetDateTime.parse("2025-01-01T08:00:00+01:00");
        options.setRandomSeed(123L);//Get deterministic results by setting a random seed.
        NameGenerator generator = new NameGenerator(options);
        names = generator.generateNames(1000);
        // Set the correct port after injection
        productApi = new ProductApi(testRestTemplate.getRestTemplate(), objectMapper, "http://localhost:" + port);
        projectApi = new ProjectApi(testRestTemplate.getRestTemplate(), objectMapper, "http://localhost:" + port);
        userApi    = new UserApi(testRestTemplate.getRestTemplate(), objectMapper, "http://localhost:" + port);
        taskApi    = new TaskApi(testRestTemplate.getRestTemplate(), objectMapper, "http://localhost:" + port);
        versionApi = new VersionApi(testRestTemplate.getRestTemplate(), objectMapper, "http://localhost:" + port);
        sprintApi  = new SprintApi(testRestTemplate.getRestTemplate(), objectMapper, "http://localhost:" + port);
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
        userApi.delete(user, availability);
        user.removeAvailability(availability);
        expectedAvailabilities.remove(availability);
    }

    protected void removeLocation(Location location, User user) {
        userApi.delete(user, location);
        user.removeLocation(location);
        expectedLocations.remove(location);
    }

    protected void removeOffDay(OffDay offDay, User user) {
        userApi.delete(user, offDay);
        user.removeOffDay(offDay);
        expectedOffDays.remove(offDay);
    }

    protected void removeProduct(Long id) {
        Product productToRemove = expectedProducts.stream().filter(product -> product.getId().equals(id)).findFirst().orElse(null);

        if (productToRemove != null) {
            // Remove all versions and their projects, sprints, and tasks
            for (Version version : productToRemove.getVersions()) {
                for (Project project : version.getProjects()) {
                    for (Sprint sprint : project.getSprints()) {
                        //TODO reintroduce tests
//                        for (Task task : sprint.getTasks()) {
//                            removeTaskTree(task);
//                        }
                        expectedSprints.remove(sprint);
                    }
                    expectedProjects.remove(project);
                }
                expectedVersions.remove(version);
            }
            expectedProducts.remove(productToRemove);
        }

        productApi.deleteById(id);
    }

    private void removeTaskTree(Task task) {
        for (Task childTask : task.getChildTasks()) {
            removeTaskTree(childTask);
        }
        expectedTasks.remove(task);
    }

    protected void removeUser(Long id) {
        User userToRemove = expectedUsers.stream().filter(user -> user.getId().equals(id)).findFirst().orElse(null);

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

        userApi.deleteById(id);
    }

    protected void testAll() {
        testProducts();
        testUsers();
    }

    /**
     * ensure products in db match our expectations
     */
    protected void testProducts() {
        GanttContext gc = new GanttContext();
        gc.allUsers    = userApi.getAllUsers();
        gc.allProducts = productApi.getAllProducts();
        gc.allVersions = versionApi.getAllVersions();
        gc.allProjects = projectApi.getAllProjects();
        gc.allSprints  = sprintApi.getAllSprints();
        gc.allTasks    = taskApi.getAllTasks();
        gc.initialize();

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
        userApi.update(availability, user.getId());
        expectedAvailabilities.remove(availability);
        expectedAvailabilities.add(availability);
    }

    protected void updateLocation(Location location, User user) {
        userApi.update(location, user.getId());
        expectedLocations.remove(location);
        expectedLocations.add(location);
    }

    protected void updateOffDay(OffDay offDay, User user) {
        userApi.update(offDay, user.getId());
        expectedOffDays.clear();
        expectedOffDays.add(offDay);
    }

    protected void updateUser(User user) {
        userApi.update(user);
        expectedUsers.remove(user);
        expectedUsers.add(user);//replace old user with the updated one
    }

}

