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

        testUsers();
        printTables();
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
        testUsers();

        //try to delete the vacation
        {
            User   user   = expectedUsers.getFirst();
            OffDay offDay = user.getOffDays().get(0);
            removeOffDay(offDay, user);
            testUsers();
        }

        //try to delete using fake user id
        {
            User user = expectedUsers.getFirst();
            Long id   = user.getId();
            user.setId(999999L);
            OffDay offDay = user.getOffDays().get(0);
            try {
                removeOffDay(offDay, user);
                fail("should not be able to delete the first location");
            } catch (ServerErrorException e) {
                //expected
            }
            user.setId(id);
            testUsers();
        }

        //try to delete using fake offday id
        {
            User   user   = expectedUsers.getFirst();
            OffDay offDay = user.getOffDays().get(0);
            Long   id     = offDay.getId();
            offDay.setId(999999L);
            try {
                removeOffDay(offDay, user);
                fail("should not be able to delete the first location");
            } catch (ServerErrorException e) {
                //expected
            }
            offDay.setId(id);
            testUsers();
            offDay.setId(id);
        }
        printTables();
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

        //update offday using fake user id
        {
            User user = expectedUsers.getFirst();
            Long id   = user.getId();
            user.setId(999999L);
            OffDay offDay = user.getOffDays().getFirst();
            offDay.setType(OffDayType.SICK);
            offDay.setFirstDay(LocalDate.parse(FIRST_DATE_1));
            offDay.setLastDay(LocalDate.parse(LAST_DATE_1));
            try {
                updateOffDay(offDay, user);
                fail("should not be able to update");
            } catch (ServerErrorException e) {
                //expected
            }
            user.setId(id);
        }

        //update offday using fake id
        {
            User   user   = expectedUsers.getFirst();
            OffDay offDay = user.getOffDays().getFirst();
            Long   id     = offDay.getId();
            offDay.setId(999999L);
            offDay.setType(OffDayType.SICK);
            offDay.setFirstDay(LocalDate.parse(FIRST_DATE_1));
            offDay.setLastDay(LocalDate.parse(LAST_DATE_1));
            try {
                updateOffDay(offDay, user);
                fail("should not be able to update");
            } catch (ServerErrorException e) {
                //expected
            }
            offDay.setId(id);
        }
        testUsers();
        printTables();
    }

}