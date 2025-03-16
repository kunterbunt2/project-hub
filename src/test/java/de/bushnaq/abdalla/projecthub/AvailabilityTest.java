package de.bushnaq.abdalla.projecthub;

import de.bushnaq.abdalla.projecthub.client.Availability;
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
import org.springframework.web.server.ServerErrorException;

import java.time.LocalDate;
import java.util.Locale;

import static org.assertj.core.api.Assertions.fail;
import static org.junit.jupiter.api.Assertions.assertEquals;


@ExtendWith(SpringExtension.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@Transactional
public class AvailabilityTest extends AbstractTestUtil {
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
            User user  = createUser(LocalDate.parse(FIRST_START_DATE));
            User pUser = client.persist(user);
            id = pUser.getId();
        }

        //test if new location was persisted correctly
        {
            User user = client.getUser(id);
            assertEquals(LocalDate.parse(FIRST_START_DATE), user.getAvailabilities().getFirst().getFirstDay());
            assertEquals(FIRST_AVAILABILITY, user.getAvailabilities().getFirst().getAvailability());
        }

        //add an availability
        {
            User user = client.getUser(id);
            //moving to Germany
            user.addAvailability(SECOND_AVAILABILITY, LocalDate.parse(SECOND_START_DATE));
            client.persist(user);//persist the new location
        }

        //test the new location
        {
            User user = client.getUser(id);
            assertEquals(LocalDate.parse(FIRST_START_DATE), user.getAvailabilities().getFirst().getFirstDay());
            assertEquals(FIRST_AVAILABILITY, user.getAvailabilities().getFirst().getAvailability());
            assertEquals(LocalDate.parse(SECOND_START_DATE), user.getAvailabilities().get(1).getFirstDay());
            assertEquals(SECOND_AVAILABILITY, user.getAvailabilities().get(1).getAvailability());

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
            assertEquals(LocalDate.parse(FIRST_START_DATE), user.getAvailabilities().getFirst().getFirstDay());
            assertEquals(FIRST_AVAILABILITY, user.getAvailabilities().getFirst().getAvailability());
        }

        printTables();
    }

    @Test
    public void delete() throws Exception {
        Long id;

        //create a user with australian locale
        {
            User user  = createUser(LocalDate.parse(FIRST_START_DATE));
            User pUser = client.persist(user);
            id = pUser.getId();
        }

        //test if new location was persisted correctly
        {
            User user = client.getUser(id);
            assertEquals(LocalDate.parse(FIRST_START_DATE), user.getAvailabilities().getFirst().getFirstDay());
            assertEquals(FIRST_AVAILABILITY, user.getAvailabilities().getFirst().getAvailability());
        }

        //try to delete the first location
        {
            User user = client.getUser(id);
            try {
                client.delete(user, user.getAvailabilities().getFirst());
                fail("should not be able to delete the first availability");
            } catch (ServerErrorException e) {
                //expected
                logger.error(e.getMessage(), e);
            }
        }

        //add an availability
        {
            User user = client.getUser(id);
            //moving to Germany
            user.addAvailability(SECOND_AVAILABILITY, LocalDate.parse(SECOND_START_DATE));
            client.persist(user);//persist the new location
        }

        //test the new location
        {
            User user = client.getUser(id);
            assertEquals(LocalDate.parse(SECOND_START_DATE), user.getAvailabilities().get(1).getFirstDay());
            assertEquals(SECOND_AVAILABILITY, user.getAvailabilities().get(1).getAvailability());

            assertEquals(LocalDate.parse(SECOND_START_DATE), user.getAvailabilities().get(1).getFirstDay());
            assertEquals(LocalDate.parse(SECOND_START_DATE).minusDays(1), user.getAvailabilities().get(0).getLastDay());
        }

        //try to delete the second availability
        {
            User user = client.getUser(id);
            client.delete(user, user.getAvailabilities().get(1));
            user = client.getUser(id);
            assertEquals(1, user.getAvailabilities().size());
        }
        printTables();
    }

    @Test
    public void update() throws Exception {
        Long id;
        Long availabilityId;

        //create the user with australian locale
        {
            User user  = createUser(LocalDate.parse(FIRST_START_DATE));
            User pUser = client.persist(user);
            id             = pUser.getId();
            availabilityId = pUser.getAvailabilities().getFirst().getId();
        }

        //test if the location was persisted correctly
        {
            User user = client.getUser(id);
            assertEquals(LocalDate.parse(FIRST_START_DATE), user.getLocations().getFirst().getFirstDay());
            assertEquals(FIRST_AVAILABILITY, user.getAvailabilities().getFirst().getAvailability());
        }

        Thread.sleep(1000);//ensure that update time is different

        //user availability is fixed
        {
            User         user     = client.getUser(id);
            Availability location = user.getAvailabilities().getFirst();
            location.setAvailability(SECOND_AVAILABILITY);
            client.update(location);
            client.update(user);
        }

        //test if the location was updated correctly
        {
            Availability location = client.getAvailability(availabilityId);
            assertEquals(SECOND_AVAILABILITY, location.getAvailability());
        }

        printTables();
    }
}