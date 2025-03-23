package de.bushnaq.abdalla.projecthub.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import de.bushnaq.abdalla.projecthub.api.*;
import de.bushnaq.abdalla.projecthub.dto.*;
import jakarta.annotation.PostConstruct;
import org.ajbrown.namemachine.Name;
import org.ajbrown.namemachine.NameGenerator;
import org.junit.jupiter.api.BeforeEach;
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
    @Autowired
    protected           ProductApi            productApi;
    private static      int                   productIndex              = 0;
    @Autowired
    protected           ProjectApi            projectApi;
    private static      int                   projectIndex              = 0;
    private static      int                   sprintIndex               = 0;
    @Autowired
    protected           TaskApi               taskApi;
    @Autowired
    private             TestRestTemplate      testRestTemplate; // Use TestRestTemplate instead of RestTemplate
    @Autowired
    protected           UserApi               userApi;
    private static      int                   userIndex                 = 0;
    @Autowired
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

    protected Product addProduct(String name) {
        Product product = new Product();
        product.setName(name);
        Product saved = productApi.persist(product);
        expectedProducts.add(saved);
        productIndex++;
        return saved;
    }

    protected Project addProject(Version version) {
        Project project = new Project();
        project.setName(String.format("Project-%d", projectIndex));
        project.setRequester(String.format("Requester-%d", projectIndex));

        project.setVersion(version);
        Project saved = projectApi.persist(project, version.getId());
        expectedProjects.add(saved);

        version.addProject(saved);
//        productApi.persist(version);

        projectIndex++;
        return saved;
    }

    protected void addRandomProducts(int count) {
        User user1 = addRandomUser();

        for (int i = 0; i < count; i++) {
            Product product = addProduct("Product " + i);
            Version version = addVersion(product, String.format("1.%d.0", i));
            Project project = addProject(version);
            Sprint  sprint  = addSprint(project);
            Task    task1   = addTask(sprint, null, "Project Phase 1", LocalDateTime.now(), Duration.ofDays(10), null, null);
            Task    task2   = addTask(null, task1, "Design", LocalDateTime.now(), Duration.ofDays(4), user1, null);
            Task    task3   = addTask(null, task2, "Implementation", LocalDateTime.now().plusDays(4), Duration.ofDays(6), user1, task1);
        }
        testProducts();
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
            String name  = names.get(userIndex).getFirstName() + " " + names.get(userIndex).getLastName();
            String email = name + "@project-hub.org";
            addUser(name, email, "de", "nw", LocalDate.now(), 0.7f, LocalDate.parse(FIRST_OFF_DAY_START_DATE), LocalDate.parse(FIRST_OFF_DAY_FINISH_DATE), OffDayType.VACATION);
        }
        printTables();
        testUsers();
    }

    protected Sprint addSprint(Project project) {
        Sprint sprint = new Sprint();
        sprint.setName(String.format("sprint-%d", sprintIndex));
//        sprint.setStart(OffsetDateTime.now());
//        sprint.setEnd(OffsetDateTime.now().plusWeeks(2));
        sprint.setStatus(Status.OPEN);
        sprint.setProject(project);
        Sprint saved = projectApi.persist(sprint, project.getId());
        expectedSprints.add(saved);
        project.addSprint(saved);
//        projectApi.persist(project);

        sprintIndex++;
        return saved;
    }

    protected Task addTask(Sprint sprint, Task parent, String name, LocalDateTime start, Duration duration, User user, Task dependency) {
        Task task = new Task();
        task.setName(name);
        task.setStart(start);
        if (duration != null) {
            task.setDuration(duration);
            task.setFinish(start.plus(duration));
        }
        if (user != null) {
            task.setResourceId(user.getId());
        }
        if (dependency != null) {
            task.addDependency(dependency);
        }
        if (sprint != null) {
            task.setSprint(sprint);
        }
        if (parent != null) {
            task.setParent(parent);
        }
        // Save the task
        Task saved = taskApi.persist(task, parent == null ? null : parent.getId(), sprint == null ? null : sprint.getId());
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

//        final HolidayManager holidayManager = HolidayManager.getInstance(ManagerParameters.create(HolidayCalendar.GERMANY));
//        final Set<Holiday>   holidays       = holidayManager.getHolidays(Year.of(2022), "nw");

        userIndex++;
        expectedUsers.add(saved);
        return saved;

    }

    protected Version addVersion(Product product, String versionName) {
        Version version = new Version();
        version.setName(String.format("1.%d.0", versionIndex));
        version.setProduct(product);
        Version saved = versionApi.persist(version, product.getId());
        product.addVersion(saved);
        expectedVersions.add(saved);
//        productApi.persist(product);
        versionIndex++;
        return saved;
    }

    @BeforeEach
    protected void createUserNames() {
        NameGenerator generator = new NameGenerator();
        names = generator.generateNames(1000);
    }

    @PostConstruct
    private void init() {
        // Set the correct port after injection
        productApi = new ProductApi(testRestTemplate.getRestTemplate(), objectMapper, "http://localhost:" + port);
        projectApi = new ProjectApi(testRestTemplate.getRestTemplate(), objectMapper, "http://localhost:" + port);
        userApi    = new UserApi(testRestTemplate.getRestTemplate(), objectMapper, "http://localhost:" + port);
        taskApi    = new TaskApi(testRestTemplate.getRestTemplate(), objectMapper, "http://localhost:" + port);
        versionApi = new VersionApi(testRestTemplate.getRestTemplate(), objectMapper, "http://localhost:" + port);
    }

    /**
     * Move task from its parent to newParent
     *
     * @param task      the task to move
     * @param newParent the new parent
     */
    protected void move(Sprint sprint, Task task, Task newParent) {
        Task oldParent = task.getParent();
        newParent.addChildTask(task);


        taskApi.persist(newParent, newParent.getParent() == null ? null : newParent.getParent().getId(), sprint.getId());
        taskApi.persist(task, task.getParent() == null ? null : task.getParent().getId(), sprint.getId());
        taskApi.persist(oldParent, oldParent.getParent() == null ? null : oldParent.getParent().getId(), sprint.getId());
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
     * ensure products in db match oru expectations
     */
    protected void testProducts() {
        List<Product> actual = productApi.getAllProducts();

        assertEquals(expectedProducts.size(), actual.size());
        for (int i = 0; i < expectedProducts.size(); i++) {
            assertProductEquals(expectedProducts.get(i), actual.get(i));
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


//        for (int i = 0; i < expectedUsers.size(); i++) {
//        }
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

