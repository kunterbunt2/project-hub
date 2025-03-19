package de.bushnaq.abdalla.projecthub.util;

import de.bushnaq.abdalla.projecthub.api.ProjectApi;
import de.bushnaq.abdalla.projecthub.api.TaskApi;
import de.bushnaq.abdalla.projecthub.api.UserApi;
import de.bushnaq.abdalla.projecthub.dto.Project;
import de.bushnaq.abdalla.projecthub.dto.Task;
import de.bushnaq.abdalla.projecthub.dto.User;
import de.bushnaq.abdalla.projecthub.dto.Version;
import org.ajbrown.namemachine.Name;
import org.ajbrown.namemachine.NameGenerator;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class AbstractEntityGenerator {
    List<Name> names;
    @Autowired
    protected      ProjectApi projectApi;
    private static int        projectIndex = 0;
    @Autowired
    protected      TaskApi    taskApi;
    @Autowired
    protected      UserApi    userApi;
    private static int        userIndex    = 0;
    protected      List<User> users        = new ArrayList<>();
    private static int        versionIndex = 0;

    protected Project createProject() {
        Project project = new Project();
        project.setName(String.format("Project-%d", projectIndex));
        project.setRequester(String.format("Requester-%d", projectIndex));

        Version version = new Version();
        version.setName(String.format("1.%d.0", versionIndex));
        project.setVersions(List.of(version));
        projectIndex++;
        versionIndex++;
        return project;
    }

    protected Task createTask(Task parent, String name, LocalDateTime start, Duration duration, User user, Task dependency) {
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
        return saved;
    }


    protected User createUser(LocalDate start) {
        String name  = names.get(userIndex).getFirstName() + " " + names.get(userIndex).getLastName();
        String email = name + "@project-hub.org";
        return createUser(name, email, "de", "nw", start, 0.7f);
    }

    protected User createUser() {
        String name  = names.get(userIndex).getFirstName() + " " + names.get(userIndex).getLastName();
        String email = name + "@project-hub.org";
        return createUser(name, email, "de", "nw", LocalDate.now(), 0.7f);
    }

    protected User createUser(String name, String email, String country, String state, LocalDate start, float availability) {
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

    @BeforeEach
    public void createUserNames() {
        NameGenerator generator = new NameGenerator();
        names = generator.generateNames(1000);
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

}
