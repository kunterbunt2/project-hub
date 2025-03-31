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

package de.bushnaq.abdalla.projecthub;

import de.bushnaq.abdalla.projecthub.dto.User;
import de.bushnaq.abdalla.projecthub.util.AbstractEntityGenerator;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;


@ExtendWith(SpringExtension.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@Transactional
public class UserTest extends AbstractEntityGenerator {
    public static final String FIRST_START_DATE  = "2024-03-14";
    public static final String SECOND_START_DATE = "2025-07-01";

    @Test
    public void create() throws Exception {

        //create the users
        addRandomUsers(1);
        printTables();

    }

    @Test
    public void delete() throws Exception {

        //create the users
        addRandomUsers(2);
        removeUser(expectedUsers.getFirst().getId());
        testUsers();

        printTables();
    }

    @Test
    public void update() throws Exception {
        Long id;

        //create the user with australian locale
        {
            User user = addRandomUser(LocalDate.parse(FIRST_START_DATE));
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
            updateUser(user);
        }

        //test if user was updated correctly
//        {
//            User user = userApi.getUser(id);
//            assertEquals(LocalDate.parse(SECOND_START_DATE), user.getLastWorkingDay());
//        }

        testUsers();
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