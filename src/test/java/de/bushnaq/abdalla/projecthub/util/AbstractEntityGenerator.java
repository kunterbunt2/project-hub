package de.bushnaq.abdalla.projecthub.util;

import de.bushnaq.abdalla.projecthub.client.Project;
import de.bushnaq.abdalla.projecthub.client.User;
import de.bushnaq.abdalla.projecthub.client.Version;
import org.ajbrown.namemachine.Name;
import org.ajbrown.namemachine.NameGenerator;
import org.junit.jupiter.api.BeforeEach;

import java.time.LocalDate;
import java.util.List;

public class AbstractEntityGenerator {
    List<Name> names;
    private static int projectIndex = 0;
    private static int userIndex    = 0;
    private static int versionIndex = 0;

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

    protected User createUser() {
        return createUser(LocalDate.now());
    }

    protected User createUser(LocalDate start) {
        User user = new User();
        user.setName(names.get(userIndex).getFirstName() + " " + names.get(userIndex).getLastName());
        user.setEmail(user.getName() + "@project-hub.org");
        user.addLocation("de", "nw", start, null);
        user.addAvailability(0.7f, start);

//        final HolidayManager holidayManager = HolidayManager.getInstance(ManagerParameters.create(HolidayCalendar.GERMANY));
//        final Set<Holiday>   holidays       = holidayManager.getHolidays(Year.of(2022), "nw");

        userIndex++;
        return user;
    }

    @BeforeEach
    public void createUserNames() {
        NameGenerator generator = new NameGenerator();
        names = generator.generateNames(1000);
    }

}
