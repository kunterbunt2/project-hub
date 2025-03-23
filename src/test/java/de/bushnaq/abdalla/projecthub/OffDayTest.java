package de.bushnaq.abdalla.projecthub;

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

import java.time.LocalDate;


@ExtendWith(SpringExtension.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@Transactional
public class OffDayTest extends AbstractEntityGenerator {
    public static final String FIRST_DATE_0 = "2024-03-14";
    public static final String FIRST_DATE_1 = "2025-07-01";
    public static final String LAST_DATE_0  = "2024-03-14";
    public static final String LAST_DATE_1  = "2025-07-01";

    @Test
    public void add() throws Exception {
        Long id;

        //create a user
        {
            User user = addRandomUser(LocalDate.parse(FIRST_DATE_0));
//            User pUser = userApi.persist(user);
            id = user.getId();
        }

        //add a vacation
//        {
//            User user = userApi.getUser(id);
//            //vacation
//            addOffDay(user, LocalDate.parse(FIRST_DATE_0), LocalDate.parse(LAST_DATE_0), OffDayType.VACATION);
////            expectedOffDays.add(user.addOffday(LocalDate.parse(FIRST_DATE_0), LocalDate.parse(LAST_DATE_0), OffDayType.VACATION));
////            userApi.persist(user);//persist the new offDay
//        }
        testUsers();
        //test the new offDay
//        {
//            User user = userApi.getUser(id);
//            assertEquals(expectedOffDays, user.getOffDays());
//        }

        printTables();
    }

    @Test
    public void delete() throws Exception {
        Long id;

        //create a user
        {
            User user = addRandomUser(LocalDate.parse(FIRST_DATE_0));
//            User pUser = userApi.persist(user);
            id = user.getId();
        }

        //add a vacation
        {
            User user = expectedUsers.getFirst();
            //vacation
            addOffDay(user, LocalDate.parse(FIRST_DATE_0), LocalDate.parse(LAST_DATE_0), OffDayType.VACATION);
//            expectedOffDays.add(user.addOffday(LocalDate.parse(FIRST_DATE_0), LocalDate.parse(LAST_DATE_0), OffDayType.VACATION));
//            userApi.persist(user);//persist the new offDay
        }
        testUsers();

        //test the new offDay
//        {
//            User user = userApi.getUser(id);
//            assertEquals(expectedOffDays, user.getOffDays());
//        }

        //try to delete the vacation
        {
            User user = expectedUsers.getFirst();
//            User user = userApi.getUser(id);
            OffDay offDay = user.getOffDays().get(0);
            removeOffDay(offDay, user);
//            user = userApi.getUser(id);
            testUsers();
        }
        printTables();
    }


    @Test
    public void update() throws Exception {
        Long id;

        //create a user
        {
            User user = addRandomUser(LocalDate.parse(FIRST_DATE_0));
//            User pUser = userApi.persist(user);
            id = user.getId();
        }

        //add a vacation
        {
            User user = expectedUsers.getFirst();
            //vacation
            addOffDay(user, LocalDate.parse(FIRST_DATE_0), LocalDate.parse(LAST_DATE_0), OffDayType.VACATION);
//            expectedOffDays.add(user.addOffday(LocalDate.parse(FIRST_DATE_0), LocalDate.parse(LAST_DATE_0), OffDayType.VACATION));
//            userApi.persist(user);//persist the new offDay
        }
        testUsers();
        //test the new offDay
//        {
//            User user = userApi.getUser(id);
//            assertEquals(expectedOffDays, user.getOffDays());
//        }

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
        testUsers();
        //test if the location was updated correctly
//        {
//            User user = userApi.getUser(id);
//            assertEquals(expectedOffDays, user.getOffDays());
//        }

        printTables();
    }

}