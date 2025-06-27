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

package de.bushnaq.abdalla.projecthub.rest.api;

import de.bushnaq.abdalla.projecthub.dto.OffDay;
import de.bushnaq.abdalla.projecthub.dto.OffDayType;
import de.bushnaq.abdalla.projecthub.dto.User;
import de.bushnaq.abdalla.projecthub.util.AbstractEntityGenerator;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.server.ServerErrorException;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.fail;
import static org.junit.jupiter.api.Assertions.assertThrows;


@ExtendWith(SpringExtension.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@Transactional
public class OffDayApiTest extends AbstractEntityGenerator {
    private static final long   FAKE_ID      = 999999L;
    private static final String FIRST_DATE_0 = "2024-03-14";
    private static final String FIRST_DATE_1 = "2025-07-01";
    private static final String LAST_DATE_0  = "2024-03-14";
    private static final String LAST_DATE_1  = "2025-07-01";


    @Test
    @WithMockUser(username = "admin-user", roles = "ADMIN")
    public void add() throws Exception {
        Long id;

        //create a user
        {
            User user = addRandomUser(LocalDate.parse(FIRST_DATE_0));
            id = user.getId();
        }

        //add a vacation
        {
            User user = expectedUsers.getFirst();
            //vacation
            addOffDay(user, LocalDate.parse(FIRST_DATE_0), LocalDate.parse(LAST_DATE_0), OffDayType.VACATION);
        }
    }

    @Test
    @WithMockUser(username = "admin-user", roles = "ADMIN")
    public void addOverlappingRanges() throws Exception {
//        final String DATE_0_START = "2024-03-05";
//        final String DATE_0_END   = "2024-03-10";
//        final String DATE_1_START = "2024-03-01";
//        final String DATE_1_END   = "2024-03-10";
//        final String DATE_2_START = "2024-03-05";
//        final String DATE_2_END   = "2024-03-10";
//        final String DATE_3_START = "2024-03-08";
//        final String DATE_3_END   = "2024-03-15";
        Long id;

        DateRange[] rangeList = {//
//                new DateRange("2024-03-05", "2024-03-10"),//
                new DateRange("2024-03-01", "2024-03-10"),//
                new DateRange("2024-03-05", "2024-03-10"),//
                new DateRange("2024-03-08", "2024-03-15"),//
                new DateRange("2024-03-01", "2024-03-15"),//
        };

        //create a user
        {
            User user = addRandomUser(LocalDate.parse(FIRST_DATE_0));
            id = user.getId();
        }

        //add a vacation
        {
            User user = expectedUsers.getFirst();
            //vacation
            addOffDay(user, LocalDate.parse("2024-03-05"), LocalDate.parse("2024-03-10"), OffDayType.VACATION);
            for (DateRange range : rangeList) {
                try {
                    addOffDay(user, LocalDate.parse(range.start), LocalDate.parse(range.end), OffDayType.VACATION);
                    fail("Should not be able to add overlapping OffDay " + range.start + " to " + range.end);
                } catch (ResponseStatusException e) {
                    //ok
                }
            }
        }
    }

    @Test
    public void anonymousSecurity() {
        {
            setUser("admin-user", "ROLE_ADMIN");
            User user = addRandomUser(LocalDate.parse(FIRST_DATE_0));
            addOffDay(user, LocalDate.parse(FIRST_DATE_0), LocalDate.parse(LAST_DATE_0), OffDayType.VACATION);
            SecurityContextHolder.clearContext();
        }

        assertThrows(AuthenticationCredentialsNotFoundException.class, () -> {
            User user = expectedUsers.getFirst();
            addOffDay(user, LocalDate.parse(FIRST_DATE_1), LocalDate.parse(LAST_DATE_1), OffDayType.SICK);
        });

        {
            User       user         = expectedUsers.getFirst();
            OffDay     offDay       = user.getOffDays().getFirst();
            OffDayType originalType = offDay.getType();
            try {
                offDay.setType(OffDayType.SICK);
                updateOffDay(offDay, user);
                fail("Should not be able to update OffDay");
            } catch (AuthenticationCredentialsNotFoundException e) {
                // Restore original values
                offDay.setType(originalType);
            }
        }

        assertThrows(AuthenticationCredentialsNotFoundException.class, () -> {
            User   user   = expectedUsers.getFirst();
            OffDay offDay = user.getOffDays().getFirst();
            removeOffDay(offDay, user);
        });
    }

    @Test
    @WithMockUser(username = "admin-user", roles = "ADMIN")
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
    @WithMockUser(username = "admin-user", roles = "ADMIN")
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
    @WithMockUser(username = "admin-user", roles = "ADMIN")
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
            } catch (ResponseStatusException e) {
                //expected
                user.setId(id);
            }
        }
    }

    @Test
    @WithMockUser(username = "admin-user", roles = "ADMIN")
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
    @WithMockUser(username = "admin-user", roles = "ADMIN")
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
            LocalDate firstDay = offDay.getFirstDay();
            LocalDate lastDay  = offDay.getLastDay();
            offDay.setFirstDay(LocalDate.parse(FIRST_DATE_1));
            offDay.setLastDay(LocalDate.parse(LAST_DATE_1));
            OffDayType type = offDay.getType();
            offDay.setType(OffDayType.SICK);
            try {
                updateOffDay(offDay, user);
                fail("should not be able to update");
            } catch (ServerErrorException e) {
                //expected
                offDay.setId(id);
                offDay.setType(type);
                offDay.setFirstDay(firstDay);
                offDay.setLastDay(lastDay);
            }
        }
    }

    @Test
    @WithMockUser(username = "admin-user", roles = "ADMIN")
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
            } catch (ResponseStatusException e) {
                //expected
                user.setId(id);
                offDay.setType(type);
            }
        }
    }

    @Test
    public void userSecurity() {
        {
            setUser("admin-user", "ROLE_ADMIN");
            User user = addRandomUser(LocalDate.parse(FIRST_DATE_0));
            addOffDay(user, LocalDate.parse(FIRST_DATE_0), LocalDate.parse(LAST_DATE_0), OffDayType.VACATION);
            setUser("user", "ROLE_USER");
        }

        assertThrows(AccessDeniedException.class, () -> {
            User user = expectedUsers.getFirst();
            addOffDay(user, LocalDate.parse(FIRST_DATE_1), LocalDate.parse(LAST_DATE_1), OffDayType.SICK);
        });

        {
            User       user         = expectedUsers.getFirst();
            OffDay     offDay       = user.getOffDays().getFirst();
            OffDayType originalType = offDay.getType();
            try {
                offDay.setType(OffDayType.SICK);
                updateOffDay(offDay, user);
                fail("Should not be able to update OffDay");
            } catch (AccessDeniedException e) {
                // Restore original values
                offDay.setType(originalType);
            }
        }

        assertThrows(AccessDeniedException.class, () -> {
            User   user   = expectedUsers.getFirst();
            OffDay offDay = user.getOffDays().getFirst();
            removeOffDay(offDay, user);
        });
    }

    class DateRange {
        String end;
        String start;

        public DateRange(String start, String end) {
            this.start = start;
            this.end   = end;
        }
    }
}