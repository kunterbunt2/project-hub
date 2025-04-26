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

package de.bushnaq.abdalla.projecthub.dao;

import de.bushnaq.abdalla.projecthub.dto.Location;
import de.bushnaq.abdalla.projecthub.dto.User;
import de.bushnaq.abdalla.projecthub.util.AbstractEntityGenerator;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ServerErrorException;

import java.time.LocalDate;
import java.util.Locale;

import static org.assertj.core.api.Assertions.fail;
import static org.junit.jupiter.api.Assertions.assertEquals;


@ExtendWith(SpringExtension.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@Transactional
public class LocationTest extends AbstractEntityGenerator {
    public static final String FIRST_START_DATE  = "2024-03-14";
    public static final String SECOND_COUNTRY    = "us";
    public static final String SECOND_START_DATE = "2025-07-01";
    public static final String SECOND_STATE      = "fl";
    Logger logger = LoggerFactory.getLogger(LocationTest.class);

    @Test
    public void add() throws Exception {
        Long id;

        //create a user with australian locale
        {
            Locale.setDefault(new Locale.Builder().setLanguage("en").setRegion("AU").build());//australian locale
            User user = addRandomUser(LocalDate.parse(FIRST_START_DATE));
            Locale.setDefault(Locale.getDefault());
            id = user.getId();
        }

        //test if new location was persisted correctly
        {
            User user = userApi.getUser(id);
            assertEquals(LocalDate.parse(FIRST_START_DATE), user.getLocations().getFirst().getStart());
        }

        //add a working location in Germany
        {
            User user = userApi.getUser(id);
            //moving to Germany
            addLocation(user, "de", "nw", LocalDate.parse(SECOND_START_DATE));
//            user.addLocation("de", "nw", LocalDate.parse(SECOND_START_DATE));
            userApi.save(user);//persist the new location
        }

        //test the new location
        {
            User user = userApi.getUser(id);
            assertEquals(LocalDate.parse(SECOND_START_DATE), user.getLocations().get(1).getStart());
        }

        printTables();
    }

    @Test
    public void delete() throws Exception {
        Long id;

        //create a user with australian locale
        {
            Locale.setDefault(new Locale.Builder().setLanguage("en").setRegion("AU").build());//australian locale
            User user = addRandomUser(LocalDate.parse(FIRST_START_DATE));
            Locale.setDefault(Locale.getDefault());
            id = user.getId();
        }

        //try to delete the first location
        {
            User user = expectedUsers.getFirst();
            try {
                userApi.delete(user, user.getLocations().getFirst());
                fail("should not be able to delete the first location");
            } catch (ServerErrorException e) {
                //expected
            }
        }

        //add a working location in Germany
        {
            User user = expectedUsers.getFirst();
            //moving to Germany
            addLocation(user, "de", "nw", LocalDate.parse(SECOND_START_DATE));
            userApi.save(user);//persist the new location
        }
        testUsers();

        //try to delete the second location
        {
            User     user     = expectedUsers.getFirst();
            Location location = user.getLocations().get(1);
            removeLocation(location, user);
            testUsers();
        }

        //try to delete using fake user id
        {
            User user   = expectedUsers.getFirst();
            Long userId = user.getId();
            user.setId(999999L);
            try {
                userApi.delete(user, user.getLocations().getFirst());
                fail("should not be able to delete");
            } catch (ServerErrorException e) {
                //expected
            }
            user.setId(userId);
        }
        //try to delete using fake location id
        {
            User     user       = expectedUsers.getFirst();
            Location location   = user.getLocations().getFirst();
            Long     locationId = location.getId();
            location.setId(999999L);
            try {
                userApi.delete(user, user.getLocations().getFirst());
                fail("should not be able to delete");
            } catch (ServerErrorException e) {
                //expected
            }
            location.setId(locationId);
        }
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

        //fix location mistake
        {
            User     user     = expectedUsers.getFirst();
            Location location = user.getLocations().getFirst();
            location.setCountry(SECOND_COUNTRY);
            location.setState(SECOND_STATE);
            location.setStart(LocalDate.parse(SECOND_START_DATE));
            updateLocation(location, user);
        }

        printTables();
        //test if the location was updated correctly
        testUsers();

        //update using fake user id
        {
            User user   = expectedUsers.getFirst();
            Long userId = user.getId();
            user.setId(999999L);
            Location location = user.getLocations().getFirst();
            location.setCountry(SECOND_COUNTRY);
            location.setState(SECOND_STATE);
            location.setStart(LocalDate.parse(SECOND_START_DATE));
            try {
                updateLocation(location, user);
                fail("should not be able to update");
            } catch (ServerErrorException e) {
                //expected
            }
            user.setId(userId);
        }

        //update using fake location id
        {
            User     user       = expectedUsers.getFirst();
            Location location   = user.getLocations().getFirst();
            Long     locationId = location.getId();
            location.setId(999999L);
            location.setCountry(SECOND_COUNTRY);
            location.setState(SECOND_STATE);
            location.setStart(LocalDate.parse(SECOND_START_DATE));
            try {
                updateLocation(location, user);
                fail("should not be able to update");
            } catch (ServerErrorException e) {
                //expected
            }
            location.setId(locationId);
        }
    }

}