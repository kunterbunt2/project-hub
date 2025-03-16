package de.bushnaq.abdalla.projecthub;

import de.bushnaq.abdalla.projecthub.dto.Location;
import de.bushnaq.abdalla.projecthub.dto.User;
import de.bushnaq.abdalla.projecthub.util.AbstractTestUtil;
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
public class LocationTest extends AbstractTestUtil {
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
            User user  = createUser(LocalDate.parse(FIRST_START_DATE));
            User pUser = userApi.persist(user);
            Locale.setDefault(Locale.getDefault());
            id = pUser.getId();
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
            user.addLocation("de", "nw", LocalDate.parse(SECOND_START_DATE));
            userApi.persist(user);//persist the new location
        }

        //test the new location
        {
            User user = userApi.getUser(id);
            assertEquals(LocalDate.parse(SECOND_START_DATE), user.getLocations().get(1).getStart());
//            assertEquals(LocalDate.parse(SECOND_START_DATE).minusDays(1), user.getLocations().get(0).getLastDay());
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
            User pUser = userApi.persist(user);
            Locale.setDefault(Locale.getDefault());
            id = pUser.getId();
        }

        //test if the location was persisted correctly
        {
            User user = userApi.getUser(id);
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
            User pUser = userApi.persist(user);
            Locale.setDefault(Locale.getDefault());
            id = pUser.getId();
        }

        //test if new location was persisted correctly
        {
            User user = userApi.getUser(id);
            assertEquals(LocalDate.parse(FIRST_START_DATE), user.getLocations().getFirst().getStart());
        }

        //try to delete the first location
        {
            User user = userApi.getUser(id);
            try {
                userApi.delete(user, user.getLocations().getFirst());
                fail("should not be able to delete the first location");
            } catch (ServerErrorException e) {
                //expected
                logger.error(e.getMessage(), e);
            }
        }

        //add a working location in Germany
        {
            User user = userApi.getUser(id);
            //moving to Germany
            user.addLocation("de", "nw", LocalDate.parse(SECOND_START_DATE));
            userApi.persist(user);//persist the new location
        }

        //test the new location
        {
            User user = userApi.getUser(id);
            assertEquals(LocalDate.parse(SECOND_START_DATE), user.getLocations().get(1).getStart());
//            assertEquals(LocalDate.parse(SECOND_START_DATE).minusDays(1), user.getLocations().get(0).getLastDay());
        }

        //try to delete the second location
        {
            User user = userApi.getUser(id);
            userApi.delete(user, user.getLocations().get(1));
            user = userApi.getUser(id);
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
            User pUser = userApi.persist(user);
            Locale.setDefault(Locale.getDefault());
            id         = pUser.getId();
            locationId = pUser.getLocations().getFirst().getId();
        }

        //test if the location was persisted correctly
        {
            User user = userApi.getUser(id);
            assertEquals(LocalDate.parse(FIRST_START_DATE), user.getLocations().getFirst().getStart());
        }

        Thread.sleep(1000);//ensure that update time is different

        //fix location mistake
        {
            Location location = userApi.getLocation(locationId);
            location.setCountry(SECOND_COUNTRY);
            location.setState(SECOND_STATE);
            location.setStart(LocalDate.parse(SECOND_START_DATE));
            userApi.update(location);
            User user = userApi.getUser(id);
        }

        //test if the location was updated correctly
        {
            User user = userApi.getUser(id);
            assertEquals(SECOND_COUNTRY, user.getLocations().getFirst().getCountry());
            assertEquals(SECOND_STATE, user.getLocations().getFirst().getState());
            assertEquals(LocalDate.parse(SECOND_START_DATE), user.getLocations().getFirst().getStart());
        }

        printTables();
    }
}