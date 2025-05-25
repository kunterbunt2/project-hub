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

package de.bushnaq.abdalla.projecthub.api;

import de.bushnaq.abdalla.projecthub.dto.User;
import de.bushnaq.abdalla.projecthub.util.AbstractEntityGenerator;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ServerErrorException;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;


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

        testUsers();
        printTables();
    }

    @Test
    public void delete() throws Exception {

        //create the users
        addRandomUsers(2);
        removeUser(expectedUsers.getFirst().getId());

        //delete by unknown id should be ignored
        {
            userApi.deleteById(9999999L);
        }
        testUsers();
        printTables();
    }

    @Test
    public void getAllEmpty() throws Exception {
        //get empty list
        userApi.getAll();
    }

    @Test
    public void getById() throws Exception {

        //create the users
        addRandomUsers(1);

        //get user by id
        {
            User user = userApi.getById(expectedUsers.first().getId());
            assertUserEquals(expectedUsers.first(), user);
        }
        //get by unknown id
        {
            try {
                User user = userApi.getById(9999999L);
                fail("User should not exist");
            } catch (ServerErrorException e) {
                //expected
            }
        }
        testUsers();
        printTables();
    }

    @Test
    public void getByUnknownId() throws Exception {

        //create the users
        addRandomUsers(1);

        //get by unknown id
        {
            try {
                User user = userApi.getById(9999999L);
                fail("User should not exist");
            } catch (ServerErrorException e) {
                //expected
            }
        }
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
            User user = userApi.getById(id);
            assertEquals(LocalDate.parse(FIRST_START_DATE), user.getLocations().getFirst().getStart());
        }

        Thread.sleep(1000);//ensure that update time is different

        //user leaves the company
        {
            User user = userApi.getById(id);
            user.setLastWorkingDay(LocalDate.parse(SECOND_START_DATE));
            updateUser(user);
        }

        testUsers();
        printTables();
    }


}