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

import de.bushnaq.abdalla.projecthub.dto.OffDay;
import de.bushnaq.abdalla.projecthub.dto.OffDayType;
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
public class OffDayTest extends AbstractEntityGenerator {
    private static final long   FAKE_ID      = 999999L;
    private static final String FIRST_DATE_0 = "2024-03-14";
    private static final String FIRST_DATE_1 = "2025-07-01";
    private static final String LAST_DATE_0  = "2024-03-14";
    private static final String LAST_DATE_1  = "2025-07-01";

    @Test
    public void add() throws Exception {
        Long id;

        //create a user
        {
            User user = addRandomUser(LocalDate.parse(FIRST_DATE_0));
            id = user.getId();
        }
    }

    @Test
    public void delete() throws Exception {

        //create a user
        {
            User user = addRandomUser(LocalDate.parse(FIRST_DATE_0));
        }

        //add a vacation
        {
            User user = expectedUsers.getFirst();
            //vacation
            addOffDay(user, LocalDate.parse(FIRST_DATE_0), LocalDate.parse(LAST_DATE_0), OffDayType.VACATION);
        }

        //try to delete the vacation
        {
            User   user   = expectedUsers.getFirst();
            OffDay offDay = user.getOffDays().get(0);
            removeOffDay(offDay, user);
        }
    }

    @Test
    public void deleteUsingFakeId() throws Exception {

        //create a user
        {
            User user = addRandomUser(LocalDate.parse(FIRST_DATE_0));
        }

        //add a vacation
        {
            User user = expectedUsers.getFirst();
            //vacation
            addOffDay(user, LocalDate.parse(FIRST_DATE_0), LocalDate.parse(LAST_DATE_0), OffDayType.VACATION);
        }

        //try to delete using fake offday id
        {
            User   user   = expectedUsers.getFirst();
            OffDay offDay = user.getOffDays().get(0);
            Long   id     = offDay.getId();
            offDay.setId(FAKE_ID);
            try {
                removeOffDay(offDay, user);
                fail("should not be able to delete the first location");
            } catch (ServerErrorException e) {
                //expected
                offDay.setId(id);
            }
            offDay.setId(id);
        }
    }

    @Test
    public void deleteUsingFakeUserId() throws Exception {

        //create a user
        {
            User user = addRandomUser(LocalDate.parse(FIRST_DATE_0));
        }

        //add a vacation
        {
            User user = expectedUsers.getFirst();
            //vacation
            addOffDay(user, LocalDate.parse(FIRST_DATE_0), LocalDate.parse(LAST_DATE_0), OffDayType.VACATION);
        }

        //try to delete using fake user id
        {
            User user = expectedUsers.getFirst();
            Long id   = user.getId();
            user.setId(FAKE_ID);
            OffDay offDay = user.getOffDays().getFirst();
            try {
                removeOffDay(offDay, user);
                fail("should not be able to delete the first location");
            } catch (ServerErrorException e) {
                //expected
                user.setId(id);
            }
        }
    }

    @Test
    public void update() throws Exception {

        //create a user
        {
            User user = addRandomUser(LocalDate.parse(FIRST_DATE_0));
        }

        //add a vacation
        {
            User user = expectedUsers.getFirst();
            //vacation
            addOffDay(user, LocalDate.parse(FIRST_DATE_0), LocalDate.parse(LAST_DATE_0), OffDayType.VACATION);
        }
        testUsers();

        Thread.sleep(1000);//ensure that update time is different

        //fix vacation mistake
        {
            User   user   = expectedUsers.getFirst();
            OffDay offDay = user.getOffDays().getFirst();
            offDay.setType(OffDayType.SICK);
            offDay.setFirstDay(LocalDate.parse(FIRST_DATE_1));
            offDay.setLastDay(LocalDate.parse(LAST_DATE_1));
            updateOffDay(offDay, user);
        }
    }

    @Test
    public void updateUsingFakeId() throws Exception {

        //create a user
        {
            User user = addRandomUser(LocalDate.parse(FIRST_DATE_0));
        }

        //add a vacation
        {
            User user = expectedUsers.getFirst();
            //vacation
            addOffDay(user, LocalDate.parse(FIRST_DATE_0), LocalDate.parse(LAST_DATE_0), OffDayType.VACATION);
        }

        //update offday using fake id
        {
            User   user   = expectedUsers.getFirst();
            OffDay offDay = user.getOffDays().getFirst();
            Long   id     = offDay.getId();
            offDay.setId(FAKE_ID);
            OffDayType type = offDay.getType();
            offDay.setType(OffDayType.SICK);
            try {
                updateOffDay(offDay, user);
                fail("should not be able to update");
            } catch (ServerErrorException e) {
                //expected
                offDay.setId(id);
                offDay.setType(type);
            }
        }
    }

    @Test
    public void updateUsingFakeUserId() throws Exception {

        //create a user
        {
            User user = addRandomUser(LocalDate.parse(FIRST_DATE_0));
        }

        //add a vacation
        {
            User user = expectedUsers.getFirst();
            //vacation
            addOffDay(user, LocalDate.parse(FIRST_DATE_0), LocalDate.parse(LAST_DATE_0), OffDayType.VACATION);
        }

        //update offday using fake user id
        {
            User user = expectedUsers.getFirst();
            Long id   = user.getId();
            user.setId(FAKE_ID);
            OffDay     offDay = user.getOffDays().getFirst();
            OffDayType type   = offDay.getType();
            offDay.setType(OffDayType.SICK);
            try {
                updateOffDay(offDay, user);
                fail("should not be able to update");
            } catch (ServerErrorException e) {
                //expected
                user.setId(id);
                offDay.setType(type);
            }
        }
    }

}