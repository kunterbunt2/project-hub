package de.bushnaq.abdalla.projecthub.util;

import de.bushnaq.abdalla.projecthub.api.ProjectApi;
import de.bushnaq.abdalla.projecthub.api.TaskApi;
import de.bushnaq.abdalla.projecthub.api.UserApi;
import de.bushnaq.abdalla.projecthub.dto.Project;
import de.bushnaq.abdalla.projecthub.dto.User;
import de.bushnaq.abdalla.projecthub.dto.Version;
import org.ajbrown.namemachine.Name;
import org.ajbrown.namemachine.NameGenerator;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDate;
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

}
