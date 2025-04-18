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

import de.bushnaq.abdalla.projecthub.dto.Availability;
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

import static org.assertj.core.api.Assertions.fail;


@ExtendWith(SpringExtension.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@Transactional
public class AvailabilityTest extends AbstractEntityGenerator {
    public static final float  FIRST_AVAILABILITY  = 0.7f;
    public static final String FIRST_START_DATE    = "2024-03-14";
    public static final float  SECOND_AVAILABILITY = 0.6f;
    public static final String SECOND_START_DATE   = "2025-07-01";
    Logger logger = LoggerFactory.getLogger(AvailabilityTest.class);

    @Test
    public void add() throws Exception {
        Long id;

        //create a user with australian locale
        {
            User user = addRandomUser(LocalDate.parse(FIRST_START_DATE));
//            User pUser = userApi.persist(user);
            id = user.getId();
        }

        //test if new location was persisted correctly
//        {
//            User user = userApi.getUser(id);
//            assertEquals(LocalDate.parse(FIRST_START_DATE), user.getAvailabilities().getFirst().getStart());
//            assertEquals(FIRST_AVAILABILITY, user.getAvailabilities().getFirst().getAvailability());
//        }

        //add an availability
        {
            User user = expectedUsers.getFirst();
            //moving to Germany
            addAvailability(user, SECOND_AVAILABILITY, LocalDate.parse(SECOND_START_DATE));
//            user.addAvailability(SECOND_AVAILABILITY, LocalDate.parse(SECOND_START_DATE));
//            userApi.persist(user);//persist the new location
            testUsers();
        }

        //test the new location
//        {
//            User user = userApi.getUser(id);
//            assertEquals(LocalDate.parse(FIRST_START_DATE), user.getAvailabilities().getFirst().getStart());
//            assertEquals(FIRST_AVAILABILITY, user.getAvailabilities().getFirst().getAvailability());
//            assertEquals(LocalDate.parse(SECOND_START_DATE), user.getAvailabilities().get(1).getStart());
//            assertEquals(SECOND_AVAILABILITY, user.getAvailabilities().get(1).getAvailability());
//
//        }

        printTables();
    }

//    @Test
//    public void create() throws Exception {
//        Long id;
//
//        //create the user with australian locale
//        {
//            Locale.setDefault(new Locale.Builder().setLanguage("en").setRegion("AU").build());//australian locale
//            User user  = addRandomUser(LocalDate.parse(FIRST_START_DATE));
//            User pUser = userApi.persist(user);
//            Locale.setDefault(Locale.getDefault());
//            id = pUser.getId();
//        }
//
//        //test if the location was persisted correctly
//        {
//            User user = userApi.getUser(id);
//            assertEquals(LocalDate.parse(FIRST_START_DATE), user.getAvailabilities().getFirst().getStart());
//            assertEquals(FIRST_AVAILABILITY, user.getAvailabilities().getFirst().getAvailability());
//        }
//
//        printTables();
//    }

    @Test
    public void delete() throws Exception {
        Long id;

        //create a user with australian locale
        {
            User user = addRandomUser(LocalDate.parse(FIRST_START_DATE));
//            User pUser = userApi.persist(user);
            id = user.getId();
        }

        //test if new location was persisted correctly
//        {
//            User user = userApi.getUser(id);
//            assertEquals(LocalDate.parse(FIRST_START_DATE), user.getAvailabilities().getFirst().getStart());
//            assertEquals(FIRST_AVAILABILITY, user.getAvailabilities().getFirst().getAvailability());
//        }

        //try to delete the first location
        {
            User user = expectedUsers.getFirst();
            try {
                Availability availability = user.getAvailabilities().getFirst();
                removeAvailability(availability, user);
//                userApi.delete(user, user.getAvailabilities().getFirst());
                fail("should not be able to delete the first availability");
            } catch (ServerErrorException e) {
                //expected
                logger.error(e.getMessage(), e);
            }
            testUsers();
        }

        //add an availability
        {
            User user = expectedUsers.getFirst();
            //moving to Germany
            addAvailability(user, SECOND_AVAILABILITY, LocalDate.parse(SECOND_START_DATE));
//            user.addAvailability(SECOND_AVAILABILITY, LocalDate.parse(SECOND_START_DATE));
            userApi.persist(user);//persist the new location
            testUsers();
        }

        //test the new location
//        {
//            User user = userApi.getUser(id);
//            assertEquals(LocalDate.parse(SECOND_START_DATE), user.getAvailabilities().get(1).getStart());
//            assertEquals(SECOND_AVAILABILITY, user.getAvailabilities().get(1).getAvailability());
//
//            assertEquals(LocalDate.parse(SECOND_START_DATE), user.getAvailabilities().get(1).getStart());
////            assertEquals(LocalDate.parse(SECOND_START_DATE).minusDays(1), user.getAvailabilities().get(0).getLastDay());
//        }

        //try to delete the second availability
        {
            User         user         = expectedUsers.getFirst();
            Availability availability = user.getAvailabilities().getLast();
            removeAvailability(availability, user);
//            userApi.delete(user, user.getAvailabilities().get(1));
//            user = userApi.getUser(id);
//            assertEquals(1, user.getAvailabilities().size());
            testUsers();
        }
        printTables();
    }

    @Test
    public void update() throws Exception {
        Long id;
        Long availabilityId;

        //create the user with australian locale
        {
            User user = addRandomUser(LocalDate.parse(FIRST_START_DATE));
//            User pUser = userApi.persist(user);
            id             = user.getId();
            availabilityId = user.getAvailabilities().getFirst().getId();
        }

        //test if the location was persisted correctly
//        {
//            User user = userApi.getUser(id);
//            assertEquals(LocalDate.parse(FIRST_START_DATE), user.getLocations().getFirst().getStart());
//            assertEquals(FIRST_AVAILABILITY, user.getAvailabilities().getFirst().getAvailability());
//        }

        Thread.sleep(1000);//ensure that update time is different

        //user availability is fixed
        {
            User         user         = expectedUsers.getFirst();
            Availability availability = user.getAvailabilities().getFirst();
            availability.setAvailability(SECOND_AVAILABILITY);
            updateAvailability(availability, user);
        }

        //test if the location was updated correctly
        printTables();
        testUsers();
//        {
//            Availability location = userApi.getAvailability(availabilityId);
//            assertEquals(SECOND_AVAILABILITY, location.getAvailability());
//        }

        printTables();
    }

}