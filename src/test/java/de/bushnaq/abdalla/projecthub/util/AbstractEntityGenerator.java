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
import de.bushnaq.abdalla.projecthub.gantt.GanttContext;
import de.bushnaq.abdalla.util.date.DateUtil;
import jakarta.annotation.PostConstruct;
import org.ajbrown.namemachine.Name;
import org.ajbrown.namemachine.NameGenerator;
import org.ajbrown.namemachine.NameGeneratorOptions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
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
    //    @Autowired
    protected           ProductApi            productApi;
    private static      int                   productIndex              = 0;
    //    @Autowired
    protected           ProjectApi            projectApi;
    private static      int                   projectIndex              = 0;
    //    @Autowired
    protected           SprintApi             sprintApi;
    private static      int                   sprintIndex               = 0;
    //    @Autowired
    protected           TaskApi               taskApi;
    @Autowired
    private             TestRestTemplate      testRestTemplate; // Use TestRestTemplate instead of RestTemplate
    //    @Autowired
    protected           UserApi               userApi;
    private static      int                   userIndex                 = 0;
    //    @Autowired
    protected           VersionApi            versionApi;
    private static      int                   versionIndex              = 0;

    protected void addAvailability(User user, float availability, LocalDate start) {
        Availability a = new Availability(availability, start);
        a.setUser(user);
        Availability saved = userApi.persist(a, user.getId());
        user.addAvailability(saved);
        expectedAvailabilities.add(saved);
    }

    protected void addLocation(User user, String country, String state, LocalDate start) {
        Location location = new Location(country, state, start);
        location.setUser(user);
        Location saved = userApi.persist(location, user.getId());
        user.addLocation(saved);
        expectedLocations.add(saved);
    }

    protected void addOffDay(User user, LocalDate offDayStart, LocalDate offDayFinish, OffDayType type) {
        OffDay a = new OffDay(offDayStart, offDayFinish, type);
        a.setUser(user);
        OffDay saved = userApi.persist(a, user.getId());
        user.addOffday(saved);
        expectedOffDays.add(saved);
    }

    protected Task addParentTask(String name, Sprint sprint, Task parent, Task dependency) {
        return addTask(sprint, parent, name, null, Duration.ofDays(0), null, dependency);
    }

    protected Product addProduct(String name) {
        Product product = new Product();
        product.setName(name);
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

    protected User addRandomUser(LocalDate start) {
        String name  = names.get(userIndex).getFirstName() + " " + names.get(userIndex).getLastName();
        String email = name + "@project-hub.org";
        User   user  = addUser(name, email, "de", "nw", start, 0.7f, LocalDate.parse(FIRST_OFF_DAY_START_DATE), LocalDate.parse(FIRST_OFF_DAY_FINISH_DATE), OffDayType.VACATION);
        return user;
    }

    protected User addRandomUser() {
        String name  = names.get(userIndex).getFirstName() + " " + names.get(userIndex).getLastName();
        String email = name + "@project-hub.org";
        User   saved = addUser(name, email, "de", "nw", LocalDate.now(), 0.7f, LocalDate.parse(FIRST_OFF_DAY_START_DATE), LocalDate.parse(FIRST_OFF_DAY_FINISH_DATE), OffDayType.VACATION);
        testUsers();
        return saved;
    }

    protected void addRandomUsers(int count) {
        for (int i = 0; i < count; i++) {
            String    name      = names.get(userIndex).getFirstName() + " " + names.get(userIndex).getLastName();
            String    email     = name + "@project-hub.org";
            LocalDate firstDate = ParameterOptions.now.toLocalDate().minusYears(1);
            addUser(name, email, "de", "nw", firstDate, 0.5f, LocalDate.parse(FIRST_OFF_DAY_START_DATE), LocalDate.parse(FIRST_OFF_DAY_FINISH_DATE), OffDayType.VACATION);
        }
        printTables();
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
        Sprint saved = sprintApi.persist(sprint, project.getId());
        expectedSprints.add(saved);
        project.addSprint(saved);

        sprintIndex++;
        return saved;
    }

    protected Task addTask(String name, String workString, User user, Sprint sprint, Task parent, Task dependency) {
        return addTask(sprint, parent, name, null, DateUtil.parseDurationString(workString, 7.5, 37.5), user, dependency);
    }


    protected Task addTask(Sprint sprint, Task parent, String name, LocalDateTime start, Duration work, User user, Task dependency) {
        Task task = new Task();
        task.setName(name);
        task.setStart(start);
//        task.setProgress(0);
//        task.setTaskMode(TaskMode.AUTO_SCHEDULED);
        if (work != null) {

            task.setWork(work);
//            if (start != null)
//                task.setFinish(start.plus(duration));
        }
        if (user != null) {
            task.setResourceId(user.getId());
        }
        if (dependency != null) {
            task.addPredecessor(dependency);
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

    protected User addUser(String name, String email, String country, String state, LocalDate start, float availability, LocalDate offDayStart, LocalDate offDayFinish, OffDayType offDayType) {
        User user = new User();
        user.setName(name);
        user.setEmail(email);
        User saved = userApi.persist(user);
        addLocation(saved, country, state, start);
        addAvailability(saved, availability, start);
        addOffDay(saved, offDayStart, offDayFinish, offDayType);


        userIndex++;
        expectedUsers.add(saved);
        return saved;

    }

    protected Version addVersion(Product product, String versionName) {
        Version version = new Version();
        version.setName(versionName);
        version.setProduct(product);
        version.setProductId(product.getId());
        Version saved = versionApi.persist(version, product.getId());
        product.addVersion(saved);
        expectedVersions.add(saved);
        versionIndex++;
        return saved;
    }

    @PostConstruct
    private void init() {
        NameGeneratorOptions options = new NameGeneratorOptions();
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
        Product productToRemove = expectedProducts.stream()
                .filter(product -> product.getId().equals(id))
                .findFirst()
                .orElse(null);

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
        User userToRemove = expectedUsers.stream()
                .filter(user -> user.getId().equals(id))
                .findFirst()
                .orElse(null);

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

