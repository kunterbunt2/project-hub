package de.bushnaq.abdalla.projecthub.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import de.bushnaq.abdalla.projecthub.api.ProductApi;
import de.bushnaq.abdalla.projecthub.api.ProjectApi;
import de.bushnaq.abdalla.projecthub.api.TaskApi;
import de.bushnaq.abdalla.projecthub.api.UserApi;
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

public class AbstractEntityGenerator {
    List<Name> names;
    @Autowired
    private        ObjectMapper     objectMapper;
    @LocalServerPort
    private        int              port;
    @Autowired
    protected      ProductApi       productApi;
    private static int              productIndex = 0;
    protected      List<Product>    products     = new ArrayList<>();
    @Autowired
    protected      ProjectApi       projectApi;
    private static int              projectIndex = 0;
    private static int              sprintIndex  = 0;
    @Autowired
    protected      TaskApi          taskApi;
    @Autowired
    private        TestRestTemplate testRestTemplate; // Use TestRestTemplate instead of RestTemplate
    @Autowired
    protected      UserApi          userApi;
    private static int              userIndex    = 0;
    protected      List<User>       users        = new ArrayList<>();
    private static int              versionIndex = 0;

    protected Product addProduct(String name) {
        Product product = new Product();
        product.setName(name);
        Product saved = productApi.persist(product);
        products.add(saved);
        productIndex++;
        return saved;
    }

    protected Project addProject(Version version) {
        Project project = new Project();
        project.setName(String.format("Project-%d", projectIndex));
        project.setRequester(String.format("Requester-%d", projectIndex));
        Project saved = projectApi.persist(project);

        version.addProject(saved);
        productApi.persist(version);

        projectIndex++;
        return saved;
    }

    protected Sprint addSprint(Project project) {
        Sprint sprint = new Sprint();
        sprint.setName(String.format("sprint-%d", sprintIndex));
//        sprint.setStart(OffsetDateTime.now());
//        sprint.setEnd(OffsetDateTime.now().plusWeeks(2));
        sprint.setStatus(Status.OPEN);
        Sprint saved = projectApi.persist(sprint);

        project.addSprint(saved);
        projectApi.persist(project);

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
        if (sprint != null) {
            sprint.addTask(saved);
            projectApi.persist(sprint);
        }
        return saved;
    }

    protected User addUser(LocalDate start) {
        String name  = names.get(userIndex).getFirstName() + " " + names.get(userIndex).getLastName();
        String email = name + "@project-hub.org";
        return addUser(name, email, "de", "nw", start, 0.7f);
    }

    protected User addUser() {
        String name  = names.get(userIndex).getFirstName() + " " + names.get(userIndex).getLastName();
        String email = name + "@project-hub.org";
        return addUser(name, email, "de", "nw", LocalDate.now(), 0.7f);
    }

    protected User addUser(String name, String email, String country, String state, LocalDate start, float availability) {
        User user = new User();
        user.setName(name);
        user.setEmail(email);
        user.addLocation(country, state, start);
        user.addAvailability(availability, start);

//        final HolidayManager holidayManager = HolidayManager.getInstance(ManagerParameters.create(HolidayCalendar.GERMANY));
//        final Set<Holiday>   holidays       = holidayManager.getHolidays(Year.of(2022), "nw");

        userIndex++;
        User saved = userApi.persist(user);
        users.add(saved);
        return saved;

    }

    protected Version addVersion(Product product, String versionName) {
        Version version = new Version();
        version.setName(String.format("1.%d.0", versionIndex));
        Version saved = productApi.persist(version);
        product.addVersion(saved);
        productApi.persist(product);
        versionIndex++;
        return saved;
    }

    @BeforeEach
    public void createUserNames() {
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
    }

    /**
     * Move task from its parent to newParent
     *
     * @param task      the task to move
     * @param newParent the new parent
     */
    protected void move(Task task, Task newParent) {
        Task oldParent = task.getParent();
        newParent.addChildTask(task);

        taskApi.persist(newParent);
        taskApi.persist(task);
        taskApi.persist(oldParent);
    }

    protected void removeProduct(Long id) {
        productApi.deleteById(id);
    }

}
