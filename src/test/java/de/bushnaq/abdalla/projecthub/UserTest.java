package de.bushnaq.abdalla.projecthub;

import de.bushnaq.abdalla.projecthub.client.User;
import de.bushnaq.abdalla.projecthub.util.AbstractTestUtil;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;


@ExtendWith(SpringExtension.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@Transactional
public class UserTest extends AbstractTestUtil {

    @Test
    public void create() throws Exception {
        List<User> users = new ArrayList<>();

        //create the users
        for (int i = 0; i < 1; i++) {
            User user  = createUser();
            User pUser = client.persist(user);
            users.add(pUser);
        }
        printTables();

        //test if the users were persisted correctly
        {
            List<User> allUsers = client.getAllUsers();
            assertEquals(users.size(), allUsers.size());
            for (int i = 0; i < users.size(); i++) {
                asserEqual(users.get(i), allUsers.get(i));
            }
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