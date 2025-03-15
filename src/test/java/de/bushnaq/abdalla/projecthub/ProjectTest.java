package de.bushnaq.abdalla.projecthub;

import de.bushnaq.abdalla.projecthub.client.Project;
import de.bushnaq.abdalla.projecthub.util.AbstractTestUtil;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;


@ExtendWith(SpringExtension.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@Transactional
public class ProjectTest extends AbstractTestUtil {
    Logger logger = LoggerFactory.getLogger(ProjectTest.class);

    @Test
    public void create() throws Exception {

        for (int i = 0; i < 1; i++) {
            Project project        = createProject();
            Project createdProject = client.persist(project);
//            printTables();
            Project retrievedProject = client.getProject(createdProject.getId());
            asserEqual(createdProject, retrievedProject);
        }
//        Project project          = createProject();
//        Project createdProject   = client.createProject(project);
//        Project retrievedProject = client.getProjectById(createdProject.getId());
//        asserEqual(createdProject, retrievedProject);

//        List<Project> allProjects = client.getAllProjects();

        printTables();
    }


    @Test
    public void getAll() throws Exception {
        List<Project> allProjects = client.getAllProjects();
        printTables();
    }

    @Test
    public void getById() throws Exception {
        Project project        = createProject();
        Project createdProject = client.persist(project);

        Project retrievedProject = client.getProject(createdProject.getId());
        asserEqual(createdProject, retrievedProject);
        printTables();
    }

}