package de.bushnaq.abdalla.projecthub;

import de.bushnaq.abdalla.projecthub.dto.OffDay;
import de.bushnaq.abdalla.projecthub.dto.OffDayType;
import de.bushnaq.abdalla.projecthub.dto.User;
import de.bushnaq.abdalla.projecthub.util.AbstractTestUtil;
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
public class OffDayTest extends AbstractTestUtil {
    public static final String FIRST_DATE_0 = "2024-03-14";
    public static final String FIRST_DATE_1 = "2025-07-01";
    public static final String LAST_DATE_0  = "2024-03-14";
    public static final String LAST_DATE_1  = "2025-07-01";

    @Test
    public void add() throws Exception {
        Long id;

        //create a user
        {
            User user  = createUser(LocalDate.parse(FIRST_DATE_0));
            User pUser = userApi.persist(user);
            id = pUser.getId();
        }

        //add a vacation
        {
            User user = userApi.getUser(id);
            //vacation
            offDays.add(user.addOffday(LocalDate.parse(FIRST_DATE_0), LocalDate.parse(LAST_DATE_0), OffDayType.VACATION));
            userApi.persist(user);//persist the new offDay
        }

        //test the new offDay
        {
            User user = userApi.getUser(id);
            assertEquals(offDays, user.getOffDays());
        }

        printTables();
    }

    @Test
    public void delete() throws Exception {
        Long id;

        //create a user
        {
            User user  = createUser(LocalDate.parse(FIRST_DATE_0));
            User pUser = userApi.persist(user);
            id = pUser.getId();
        }

        //add a vacation
        {
            User user = userApi.getUser(id);
            //vacation
            offDays.add(user.addOffday(LocalDate.parse(FIRST_DATE_0), LocalDate.parse(LAST_DATE_0), OffDayType.VACATION));
            userApi.persist(user);//persist the new offDay
        }

        //test the new offDay
        {
            User user = userApi.getUser(id);
            assertEquals(offDays, user.getOffDays());
        }

        //try to delete the vacation
        {
            User user = userApi.getUser(id);
            userApi.delete(user, user.getOffDays().get(0));
            offDays.removeFirst();
            user = userApi.getUser(id);
            assertEquals(offDays, user.getOffDays());
        }
        printTables();
    }

    @Test
    public void update() throws Exception {
        Long id;

        //create a user
        {
            User user  = createUser(LocalDate.parse(FIRST_DATE_0));
            User pUser = userApi.persist(user);
            id = pUser.getId();
        }

        //add a vacation
        {
            User user = userApi.getUser(id);
            //vacation
            offDays.add(user.addOffday(LocalDate.parse(FIRST_DATE_0), LocalDate.parse(LAST_DATE_0), OffDayType.VACATION));
            userApi.persist(user);//persist the new offDay
        }

        //test the new offDay
        {
            User user = userApi.getUser(id);
            assertEquals(offDays, user.getOffDays());
        }

        Thread.sleep(1000);//ensure that update time is different

        //fix vacation mistake
        {
            User   user   = userApi.getUser(id);
            OffDay offDay = userApi.getOffDay(user.getOffDays().getFirst().getId());
            offDay.setType(OffDayType.SICK);
            offDay.setFirstDay(LocalDate.parse(FIRST_DATE_1));
            offDay.setLastDay(LocalDate.parse(LAST_DATE_1));
            userApi.update(offDay);
            offDays.clear();
            offDays.add(offDay);
        }

        //test if the location was updated correctly
        {
            User user = userApi.getUser(id);
            assertEquals(offDays, user.getOffDays());
        }

        printTables();
    }
}