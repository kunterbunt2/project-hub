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

import de.bushnaq.abdalla.projecthub.dto.Availability;
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

import static org.assertj.core.api.Assertions.fail;


@ExtendWith(SpringExtension.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@Transactional
public class AvailabilityTest extends AbstractEntityGenerator {
    private static final long   FAKE_ID             = 999999L;
    private static final String FIRST_START_DATE    = "2024-03-14";
    private static final float  SECOND_AVAILABILITY = 0.6f;
    private static final String SECOND_START_DATE   = "2025-07-01";

    @Test
    public void addAvailability() throws Exception {

        //create a user with australian locale
        {
            User user = addRandomUser(LocalDate.parse(FIRST_START_DATE));
        }

        //add an availability
        {
            User user = expectedUsers.getFirst();
            //moving to Germany
            addAvailability(user, SECOND_AVAILABILITY, LocalDate.parse(SECOND_START_DATE));
        }

        printTables();
    }

    @Test
    public void deleteFirstAvailability() throws Exception {

        //create a user with australian locale
        {
            User user = addRandomUser(LocalDate.parse(FIRST_START_DATE));
        }

        //try to delete the first location
        {
            User         user         = expectedUsers.getFirst();
            Availability availability = user.getAvailabilities().getFirst();
            try {
                removeAvailability(availability, user);
                fail("should not be able to delete the first availability");
            } catch (ServerErrorException e) {
                //expected
            }
        }

    }

    @Test
    public void deleteSecondAvailability() throws Exception {

        //create a user with australian locale
        {
            User user = addRandomUser(LocalDate.parse(FIRST_START_DATE));
        }

        //add an availability
        {
            User user = expectedUsers.getFirst();
            //moving to Germany
            addAvailability(user, SECOND_AVAILABILITY, LocalDate.parse(SECOND_START_DATE));
        }

        //try to delete the second availability
        {
            User         user         = expectedUsers.getFirst();
            Availability availability = user.getAvailabilities().getLast();
            removeAvailability(availability, user);
        }

    }

    @Test
    public void deleteUsingFakeId() throws Exception {

        //create a user with australian locale
        {
            User user = addRandomUser(LocalDate.parse(FIRST_START_DATE));
        }

        //add an availability
        {
            User user = expectedUsers.getFirst();
            //moving to Germany
            addAvailability(user, SECOND_AVAILABILITY, LocalDate.parse(SECOND_START_DATE));
        }

        //try to delete the second availability with fake availability id
        {
            User         user         = expectedUsers.getFirst();
            Availability availability = user.getAvailabilities().getLast();
            Long         id           = availability.getId();
            availability.setId(FAKE_ID);
            try {
                removeAvailability(availability, user);
                fail("should not be able to delete");
            } catch (ServerErrorException e) {
                availability.setId(id);
                //expected
            }
        }
    }

    @Test
    public void deleteUsingFakeUserId() throws Exception {

        //create a user with australian locale
        {
            User user = addRandomUser(LocalDate.parse(FIRST_START_DATE));
        }

        //add an availability
        {
            User user = expectedUsers.getFirst();
            //moving to Germany
            addAvailability(user, SECOND_AVAILABILITY, LocalDate.parse(SECOND_START_DATE));
        }

        //try to delete the second availability with fake user id
        {
            User user = expectedUsers.getFirst();
            Long id   = user.getId();
            user.setId(FAKE_ID);
            Availability availability = user.getAvailabilities().getLast();
            try {
                removeAvailability(availability, user);
                fail("should not be able to delete");
            } catch (ServerErrorException e) {
                user.setId(id);
                //expected
            }
        }
    }

    @Test
    public void updateAvailability() throws Exception {

        //create the user with australian locale
        {
            User user = addRandomUser(LocalDate.parse(FIRST_START_DATE));
        }

        //user availability is fixed
        {
            User         user         = expectedUsers.getFirst();
            Availability availability = user.getAvailabilities().getFirst();
            availability.setAvailability(SECOND_AVAILABILITY);
            updateAvailability(availability, user);
        }
    }

    @Test
    public void updateUsingFakeAvailabilityId() throws Exception {

        //create the user with australian locale
        {
            User user = addRandomUser(LocalDate.parse(FIRST_START_DATE));
        }

        //update availability using unknown availability id
        {
            User         user         = expectedUsers.getFirst();
            Availability availability = user.getAvailabilities().getFirst();
            float        a            = availability.getAvailability();
            availability.setAvailability(SECOND_AVAILABILITY);
            Long id = availability.getId();
            availability.setId(FAKE_ID);
            try {
                updateAvailability(availability, user);
                fail("should not be able to update");
            } catch (ServerErrorException e) {
                availability.setAvailability(a);
                availability.setId(id);
                //expected
            }
        }
    }

    @Test
    public void updateUsingFakeUserId() throws Exception {

        //create the user with australian locale
        {
            User user = addRandomUser(LocalDate.parse(FIRST_START_DATE));
        }

        //update availability using fake user id
        {
            User user = expectedUsers.getFirst();
            Long id   = user.getId();
            user.setId(FAKE_ID);
            Availability availability = user.getAvailabilities().getFirst();
            float        a            = availability.getAvailability();
            availability.setAvailability(SECOND_AVAILABILITY);
            try {
                updateAvailability(availability, user);
                fail("should not be able to update");
            } catch (ServerErrorException e) {
                //expected
                availability.setAvailability(a);
                user.setId(id);
            }
        }
    }
}