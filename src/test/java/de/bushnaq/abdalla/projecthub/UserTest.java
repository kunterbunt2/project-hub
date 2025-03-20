package de.bushnaq.abdalla.projecthub;

import de.bushnaq.abdalla.projecthub.dto.User;
import de.bushnaq.abdalla.projecthub.util.AbstractTestUtil;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;


@ExtendWith(SpringExtension.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@Transactional
public class UserTest extends AbstractTestUtil {
    public static final String FIRST_START_DATE  = "2024-03-14";
    public static final String SECOND_START_DATE = "2025-07-01";

    @Test
    public void create() throws Exception {

        //create the users
        for (int i = 0; i < 1; i++) {
            User user = addUser();
        }
        printTables();

        //test if the users were persisted correctly
        {
            List<User> allUsers = userApi.getAllUsers();
            assertEquals(users.size(), allUsers.size());
            for (int i = 0; i < users.size(); i++) {
                assertUserEquals(users.get(i), allUsers.get(i));
            }
        }

        printTables();
    }

    @Test
    public void update() throws Exception {
        Long id;

        //create the user with australian locale
        {
            User user = addUser(LocalDate.parse(FIRST_START_DATE));
            id = user.getId();
        }

        //test if the location was persisted correctly
        {
            User user = userApi.getUser(id);
            assertEquals(LocalDate.parse(FIRST_START_DATE), user.getLocations().getFirst().getStart());
        }

        Thread.sleep(1000);//ensure that update time is different

        //user leaves the company
        {
            User user = userApi.getUser(id);
            user.setLastWorkingDay(LocalDate.parse(SECOND_START_DATE));
            userApi.update(user);
        }

        //test if user was updated correctly
        {
            User user = userApi.getUser(id);
            assertEquals(LocalDate.parse(SECOND_START_DATE), user.getLastWorkingDay());
        }

        printTables();
    }

//
//
//    @Test
//    public void getAll() throws Exception {
//        List<Project> allProjects = client.getAllProjects();
//        printTables();
//    }
//
//    @Test
//    public void getById() throws Exception {
//        Project project        = createProject();
//        Project createdProject = client.createProject(project);
//
//        Project retrievedProject = client.getProjectById(createdProject.getId());
//        asserEqual(createdProject, retrievedProject);
//        printTables();
//    }

}