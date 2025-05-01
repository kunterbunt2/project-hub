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
    private static final long   FAKE_ID           = 999999L;
    private static final String FIRST_START_DATE  = "2024-03-14";
    private static final String SECOND_COUNTRY    = "us";
    private static final String SECOND_START_DATE = "2025-07-01";
    private static final String SECOND_STATE      = "fl";

    @Test
    public void add() throws Exception {

        //create a user with australian locale
        {
            Locale.setDefault(new Locale.Builder().setLanguage("en").setRegion("AU").build());//australian locale
            User user = addRandomUser(LocalDate.parse(FIRST_START_DATE));
            Locale.setDefault(Locale.getDefault());
        }

        //test if new location was persisted correctly
        {
            User user = expectedUsers.getFirst();
            assertEquals(LocalDate.parse(FIRST_START_DATE), user.getLocations().getFirst().getStart());
        }

        //add a working location in Germany
        {
            User user = expectedUsers.getFirst();
            //moving to Germany
            addLocation(user, "de", "nw", LocalDate.parse(SECOND_START_DATE));
            userApi.persist(user);//persist the new location
        }

        //test the new location
        {
            User user = expectedUsers.getFirst();
            assertEquals(LocalDate.parse(SECOND_START_DATE), user.getLocations().get(1).getStart());
        }
    }

    @Test
    public void deleteFirstLocation() throws Exception {

        {
            User user = addRandomUser(LocalDate.parse(FIRST_START_DATE));
        }

        //try to delete the first location
        {
            User user = expectedUsers.getFirst();
            try {
                locationApi.deleteById(user, user.getLocations().getFirst());
                fail("should not be able to delete the first location");
            } catch (ServerErrorException e) {
                //expected
            }
        }
    }

    @Test
    public void deleteSecondLocation() throws Exception {

        {
            User user = addRandomUser(LocalDate.parse(FIRST_START_DATE));
        }

        //add a working location in Germany
        {
            User user = expectedUsers.getFirst();
            //moving to Germany
            addLocation(user, "de", "nw", LocalDate.parse(SECOND_START_DATE));
            userApi.persist(user);//persist the new location
        }

        //try to delete the second location
        {
            User     user     = expectedUsers.getFirst();
            Location location = user.getLocations().get(1);
            removeLocation(location, user);
        }
    }

    @Test
    public void deleteUsingFakeId() throws Exception {

        //create a user with australian locale
        {
            Locale.setDefault(new Locale.Builder().setLanguage("en").setRegion("AU").build());//australian locale
            User user = addRandomUser(LocalDate.parse(FIRST_START_DATE));
            Locale.setDefault(Locale.getDefault());
        }

        //add a working location in Germany
        {
            User user = expectedUsers.getFirst();
            //moving to Germany
            addLocation(user, "de", "nw", LocalDate.parse(SECOND_START_DATE));
            userApi.persist(user);//persist the new location
        }

        //try to delete using fake location id
        {
            User     user       = expectedUsers.getFirst();
            Location location   = user.getLocations().getFirst();
            Long     locationId = location.getId();
            location.setId(FAKE_ID);
            try {
                locationApi.deleteById(user, user.getLocations().getFirst());
                fail("should not be able to delete");
            } catch (ServerErrorException e) {
                //expected
                location.setId(locationId);
            }
        }
    }

    @Test
    public void deleteUsingFakeUserId() throws Exception {

        //create a user with australian locale
        {
            Locale.setDefault(new Locale.Builder().setLanguage("en").setRegion("AU").build());//australian locale
            User user = addRandomUser(LocalDate.parse(FIRST_START_DATE));
            Locale.setDefault(Locale.getDefault());
        }

        //add a working location in Germany
        {
            User user = expectedUsers.getFirst();
            //moving to Germany
            addLocation(user, "de", "nw", LocalDate.parse(SECOND_START_DATE));
            userApi.persist(user);//persist the new location
        }

        //try to delete using fake user id
        {
            User user   = expectedUsers.getFirst();
            Long userId = user.getId();
            user.setId(FAKE_ID);
            try {
                locationApi.deleteById(user, user.getLocations().getFirst());
                fail("should not be able to delete");
            } catch (ServerErrorException e) {
                //expected
                user.setId(userId);
            }
        }
    }

    @Test
    public void updateLocation() throws Exception {

        {
            User user = addRandomUser(LocalDate.parse(FIRST_START_DATE));
        }

        //test if the location was persisted correctly
        {
            User user = expectedUsers.getFirst();
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
    }

    @Test
    public void updateUsingFakeId() throws Exception {

        //create the user with german locale
        {
            User user = addRandomUser(LocalDate.parse(FIRST_START_DATE));
        }

        //test if the location was persisted correctly
        {
            User user = expectedUsers.getFirst();
            assertEquals(LocalDate.parse(FIRST_START_DATE), user.getLocations().getFirst().getStart());
        }

        //update using fake location id
        {
            User     user       = expectedUsers.getFirst();
            Location location   = user.getLocations().getFirst();
            Long     locationId = location.getId();
            String   country    = location.getCountry();
            location.setId(FAKE_ID);
            location.setCountry(SECOND_COUNTRY);
            try {
                updateLocation(location, user);
                fail("should not be able to update");
            } catch (ServerErrorException e) {
                //expected
                location.setCountry(country);
                location.setId(locationId);
            }
        }
    }

    @Test
    public void updateUsingFakeUserId() throws Exception {

        {
            User user = addRandomUser(LocalDate.parse(FIRST_START_DATE));
        }

        //test if the location was persisted correctly
        {
            User user = expectedUsers.getFirst();
            assertEquals(LocalDate.parse(FIRST_START_DATE), user.getLocations().getFirst().getStart());
        }

        Thread.sleep(1000);//ensure that update time is different

        //update using fake user id
        {
            User user   = expectedUsers.getFirst();
            Long userId = user.getId();
            user.setId(FAKE_ID);
            Location location = user.getLocations().getFirst();
            String   country  = location.getCountry();
            location.setCountry(SECOND_COUNTRY);
            try {
                updateLocation(location, user);
                fail("should not be able to update");
            } catch (ServerErrorException e) {
                //expected
                location.setCountry(country);
                user.setId(userId);
            }
        }
    }
}