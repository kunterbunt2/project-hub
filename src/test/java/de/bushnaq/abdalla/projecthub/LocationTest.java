package de.bushnaq.abdalla.projecthub;

import de.bushnaq.abdalla.projecthub.client.Location;
import de.bushnaq.abdalla.projecthub.client.User;
import de.bushnaq.abdalla.projecthub.util.AbstractTestUtil;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.Locale;

import static org.assertj.core.api.Assertions.fail;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;


@ExtendWith(SpringExtension.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@Transactional
public class LocationTest extends AbstractTestUtil {
    public static final String FIRST_START_DATE  = "2024-03-14";
    public static final String SECOND_START_DATE = "2025-07-01";
    Logger logger = LoggerFactory.getLogger(LocationTest.class);

    @Test
    public void add() throws Exception {
        Long id;

        //create a user with australian locale
        {
            Locale.setDefault(new Locale.Builder().setLanguage("en").setRegion("AU").build());//australian locale
            User user  = createUser(LocalDate.parse(FIRST_START_DATE));
            User pUser = client.persist(user);
            Locale.setDefault(Locale.getDefault());
            id = pUser.getId();
        }

        //test if new location was persisted correctly
        {
            User user = client.getUser(id);
            assertEquals(LocalDate.parse(FIRST_START_DATE), user.getLocations().getFirst().getStart());
        }

        //add a working location in Germany
        {
            User user = client.getUser(id);
            //moving to Germany
            user.addWorkingLocation("de", "nw", LocalDate.parse(SECOND_START_DATE), null);
            client.persist(user);//persist the new location
        }

        //test the new location
        {
            User userByID = client.getUser(id);
            assertEquals(LocalDate.parse(SECOND_START_DATE), userByID.getLocations().get(1).getStart());
            assertEquals(LocalDate.parse(SECOND_START_DATE).minusDays(1), userByID.getLocations().get(0).getFinish());
        }

        printTables();
    }

    @Test
    public void create() throws Exception {
        Long id;

        //create the user with australian locale
        {
            Locale.setDefault(new Locale.Builder().setLanguage("en").setRegion("AU").build());//australian locale
            User user  = createUser(LocalDate.parse(FIRST_START_DATE));
            User pUser = client.persist(user);
            Locale.setDefault(Locale.getDefault());
            id = pUser.getId();
        }

        //test if the location was persisted correctly
        {
            User user = client.getUser(id);
            assertEquals(LocalDate.parse(FIRST_START_DATE), user.getLocations().getFirst().getStart());
        }

        printTables();
    }

    @Test
    public void delete() throws Exception {
        Long id;

        //create a user with australian locale
        {
            Locale.setDefault(new Locale.Builder().setLanguage("en").setRegion("AU").build());//australian locale
            User user  = createUser(LocalDate.parse(FIRST_START_DATE));
            User pUser = client.persist(user);
            Locale.setDefault(Locale.getDefault());
            id = pUser.getId();
        }

        //test if new location was persisted correctly
        {
            User user = client.getUser(id);
            assertEquals(LocalDate.parse(FIRST_START_DATE), user.getLocations().getFirst().getStart());
        }

        //try to delete the first location
        {
            User user = client.getUser(id);
            try {
                client.delete(user, user.getLocations().getFirst());
                fail("should not be able to delete the first location");
            } catch (IllegalArgumentException e) {
                //expected
                logger.error(e.getMessage(), e);
            }
        }

        //add a working location in Germany
        {
            User user = client.getUser(id);
            //moving to Germany
            user.addWorkingLocation("de", "nw", LocalDate.parse(SECOND_START_DATE), null);
            client.persist(user);//persist the new location
        }

        //test the new location
        {
            User userByID = client.getUser(id);
            assertEquals(LocalDate.parse(SECOND_START_DATE), userByID.getLocations().get(1).getStart());
            assertEquals(LocalDate.parse(SECOND_START_DATE).minusDays(1), userByID.getLocations().get(0).getFinish());
        }

        //try to delete the second location
        {
            User user = client.getUser(id);
            client.delete(user, user.getLocations().get(1));
            user = client.getUser(id);
            assertEquals(1, user.getLocations().size());
        }
        printTables();
    }

    @Test
    public void update() throws Exception {
        Long id;
        Long locationId;

        //create the user with australian locale
        {
            Locale.setDefault(new Locale.Builder().setLanguage("en").setRegion("AU").build());//australian locale
            User user  = createUser(LocalDate.parse(FIRST_START_DATE));
            User pUser = client.persist(user);
            Locale.setDefault(Locale.getDefault());
            id         = pUser.getId();
            locationId = pUser.getLocations().getFirst().getId();
        }

        //test if the location was persisted correctly
        {
            User user = client.getUser(id);
            assertEquals(LocalDate.parse(FIRST_START_DATE), user.getLocations().getFirst().getStart());
        }

        Thread.sleep(1000);//ensure that update time is different

        //user leaves the company
        {
            User     user     = client.getUser(id);
            Location location = user.getLocations().getFirst();
            user.setLastWorkingDay(LocalDate.parse(SECOND_START_DATE));
            client.update(location);
            client.update(user);
        }

        //test if the location was updated correctly
        {
            Location location = client.getLocation(locationId);
            assertEquals(LocalDate.parse(SECOND_START_DATE), location.getFinish());
            assertNotEquals(location.getStart(), location.getFinish());
        }

        printTables();
    }
}