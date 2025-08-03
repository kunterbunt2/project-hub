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

package de.bushnaq.abdalla.projecthub.ai;

import com.fasterxml.jackson.databind.ObjectMapper;
import de.bushnaq.abdalla.projecthub.dto.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestConstructor;

import java.awt.*;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration test for User AI filtering testing real LLM regex pattern generation
 * and filtering capabilities with various search scenarios.
 * <p>
 * This test requires Ollama to be running with a model available (e.g., llama3.2:1b).
 * The test will be skipped if Ollama is not available.
 */
@SpringBootTest
@ActiveProfiles("test")
@TestConstructor(autowireMode = TestConstructor.AutowireMode.ALL)
class UserAiFilterTest extends AbstractAiFilterTest<User> {

    public UserAiFilterTest(ObjectMapper mapper, AiFilter aiFilter) {
        super(mapper, aiFilter);
    }

    private User createUser(Long id, String name, String email, LocalDate firstWorkingDay, LocalDate lastWorkingDay,
                            Color color, OffsetDateTime created, OffsetDateTime updated) {
        User user = new User();
        user.setId(id);
        user.setName(name);
        user.setEmail(email);
        user.setFirstWorkingDay(firstWorkingDay);
        user.setLastWorkingDay(lastWorkingDay);
        user.setColor(color);
        user.setCreated(created);
        user.setUpdated(updated);
        return user;
    }

    @BeforeEach
    void setUp() {
        // Create test data
        setupTestUsers();
    }

    private void setupTestUsers() {
        testProducts = new ArrayList<>();

        // Users with different names, roles, departments, and employment periods for comprehensive testing
        testProducts.add(createUser(1L, "John Doe", "john.doe@company.com",
                LocalDate.of(2020, 3, 15), null, Color.BLUE,
                OffsetDateTime.of(2020, 3, 1, 10, 0, 0, 0, ZoneOffset.UTC),
                OffsetDateTime.of(2024, 1, 10, 14, 30, 0, 0, ZoneOffset.UTC)));

        testProducts.add(createUser(2L, "Jane Smith", "jane.smith@company.com",
                LocalDate.of(2019, 6, 1), null, Color.RED,
                OffsetDateTime.of(2019, 5, 15, 11, 15, 0, 0, ZoneOffset.UTC),
                OffsetDateTime.of(2024, 2, 5, 16, 45, 0, 0, ZoneOffset.UTC)));

        testProducts.add(createUser(3L, "Bob Johnson", "bob.johnson@company.com",
                LocalDate.of(2021, 1, 10), LocalDate.of(2024, 6, 30), Color.GREEN,
                OffsetDateTime.of(2020, 12, 20, 8, 30, 0, 0, ZoneOffset.UTC),
                OffsetDateTime.of(2024, 3, 16, 13, 20, 0, 0, ZoneOffset.UTC)));

        testProducts.add(createUser(4L, "Alice Wilson", "alice.wilson@company.com",
                LocalDate.of(2022, 9, 5), null, Color.ORANGE,
                OffsetDateTime.of(2022, 8, 25, 15, 45, 0, 0, ZoneOffset.UTC),
                OffsetDateTime.of(2024, 4, 15, 12, 10, 0, 0, ZoneOffset.UTC)));

        testProducts.add(createUser(5L, "Mike Brown", "mike.brown@company.com",
                LocalDate.of(2018, 11, 20), null, Color.magenta,
                OffsetDateTime.of(2018, 11, 1, 14, 0, 0, 0, ZoneOffset.UTC),
                OffsetDateTime.of(2024, 5, 1, 11, 40, 0, 0, ZoneOffset.UTC)));

        testProducts.add(createUser(6L, "Sarah Davis", "sarah.davis@company.com",
                LocalDate.of(2023, 2, 14), null, Color.PINK,
                OffsetDateTime.of(2023, 1, 28, 9, 20, 0, 0, ZoneOffset.UTC),
                OffsetDateTime.of(2024, 6, 10, 16, 15, 0, 0, ZoneOffset.UTC)));

        testProducts.add(createUser(7L, "David Martinez", "david.martinez@company.com",
                LocalDate.of(2017, 4, 3), LocalDate.of(2023, 12, 15), Color.CYAN,
                OffsetDateTime.of(2017, 3, 25, 12, 30, 0, 0, ZoneOffset.UTC),
                OffsetDateTime.of(2024, 7, 29, 15, 50, 0, 0, ZoneOffset.UTC)));

        testProducts.add(createUser(8L, "Lisa Anderson", "lisa.anderson@company.com",
                LocalDate.of(2021, 8, 16), null, Color.MAGENTA,
                OffsetDateTime.of(2021, 7, 30, 8, 15, 0, 0, ZoneOffset.UTC),
                OffsetDateTime.of(2024, 8, 15, 17, 30, 0, 0, ZoneOffset.UTC)));

        testProducts.add(createUser(9L, "Robert Taylor", "robert.taylor@company.com",
                LocalDate.of(2024, 1, 8), null, Color.YELLOW,
                OffsetDateTime.of(2023, 12, 28, 13, 45, 0, 0, ZoneOffset.UTC),
                OffsetDateTime.of(2024, 9, 5, 9, 20, 0, 0, ZoneOffset.UTC)));

        testProducts.add(createUser(10L, "Emily Clark", "emily.clark@company.com",
                LocalDate.of(2020, 10, 12), null, Color.GRAY,
                OffsetDateTime.of(2020, 9, 25, 10, 30, 0, 0, ZoneOffset.UTC),
                OffsetDateTime.of(2024, 10, 22, 14, 45, 0, 0, ZoneOffset.UTC)));

        testProducts.add(createUser(11L, "James White", "james.white@company.com",
                LocalDate.of(2019, 12, 2), null, Color.BLACK,
                OffsetDateTime.of(2019, 11, 28, 9, 0, 0, 0, ZoneOffset.UTC),
                OffsetDateTime.of(2025, 1, 10, 16, 30, 0, 0, ZoneOffset.UTC)));

        testProducts.add(createUser(12L, "Maria Garcia", "maria.garcia@company.com",
                LocalDate.of(2025, 3, 1), null, Color.LIGHT_GRAY,
                OffsetDateTime.of(2025, 2, 15, 8, 15, 0, 0, ZoneOffset.UTC),
                OffsetDateTime.of(2025, 3, 10, 12, 20, 0, 0, ZoneOffset.UTC)));
    }

    @Test
    @DisplayName("Should find active users")
    void testActiveUsersSearch() throws Exception {
        List<User> results = performSearch("active users", "User");

        assertThat(results)
                .hasSizeGreaterThanOrEqualTo(10) // Users without lastWorkingDay are active
                .extracting(User::getLastWorkingDay)
                .containsOnlyNulls();
    }

    @Test
    @DisplayName("Should find users by name containing 'Anderson'")
    void testAndersonNameSearch() throws Exception {
        List<User> results = performSearch("Anderson", "User");

        assertThat(results)
                .hasSize(1)
                .extracting(User::getName)
                .containsExactly("Lisa Anderson");
    }

    @Test
    @DisplayName("Should find users containing 'David'")
    void testDavidNameSearch() throws Exception {
        List<User> results = performSearch("David", "User");

        assertThat(results)
                .hasSize(1)
                .extracting(User::getName)
                .containsExactly("David Martinez");
    }

    @Test
    @DisplayName("Should find users by department email pattern")
    void testDepartmentEmailSearch() throws Exception {
        List<User> results = performSearch("company.com employees", "User");

        assertThat(results)
                .hasSize(12) // All users work at company.com
                .extracting(User::getEmail)
                .allMatch(email -> email.endsWith("@company.com"));
    }

    @Test
    @DisplayName("Should find users by email domain")
    void testEmailDomainSearch() throws Exception {
        List<User> results = performSearch("@company.com", "User");

        assertThat(results)
                .hasSize(12) // All users have company.com email
                .extracting(User::getEmail)
                .allMatch(email -> email.contains("@company.com"));
    }

    @Test
    @DisplayName("Should find users by email")
    void testEmailSearch() throws Exception {
        List<User> results = performSearch("alice.wilson@company.com", "User");

        assertThat(results)
                .hasSize(1)
                .extracting(User::getEmail)
                .containsExactly("alice.wilson@company.com");
    }

    @Test
    @DisplayName("Should find users with 'Emily' in name")
    void testEmilyNameSearch() throws Exception {
        List<User> results = performSearch("Emily", "User");

        assertThat(results)
                .hasSize(1)
                .extracting(User::getName)
                .containsExactly("Emily Clark");
    }

    @Test
    @DisplayName("Should handle empty search query")
    void testEmptySearchQuery() throws Exception {
        List<User> results = performSearch("", "User");

        assertThat(results).hasSize(12); // All users should match empty query
    }

    @Test
    @DisplayName("Should find users by exact email match")
    void testExactEmailSearch() throws Exception {
        List<User> results = performSearch("jane.smith@company.com", "User");

        assertThat(results)
                .hasSize(1)
                .extracting(User::getEmail)
                .containsExactly("jane.smith@company.com");
    }

    @Test
    @DisplayName("Should find users by exact name")
    void testExactUserNameSearch() throws Exception {
        List<User> results = performSearch("John Doe", "User");

        assertThat(results)
                .hasSize(1)
                .extracting(User::getName)
                .containsExactly("John Doe");
    }

    @Test
    @DisplayName("Should find users by first name")
    void testFirstNameSearch() throws Exception {
        List<User> results = performSearch("John", "User");

        assertThat(results)
                .hasSize(1)
                .extracting(User::getName)
                .containsExactly("John Doe");
    }

    @Test
    @DisplayName("Should find former employees")
    void testFormerEmployeesSearch() throws Exception {
        List<User> results = performSearch("former employees", "User");

        assertThat(results)
                .hasSizeGreaterThanOrEqualTo(2) // Users with lastWorkingDay are former employees
                .extracting(User::getLastWorkingDay)
                .doesNotContainNull();
    }

    @Test
    @DisplayName("Should find users by name containing 'Garcia'")
    void testGarciaNameSearch() throws Exception {
        List<User> results = performSearch("Garcia", "User");

        assertThat(results)
                .hasSize(1)
                .extracting(User::getName)
                .containsExactly("Maria Garcia");
    }

    @Test
    @DisplayName("Should find users by 'James' name")
    void testJamesNameSearch() throws Exception {
        List<User> results = performSearch("James", "User");

        assertThat(results)
                .hasSize(1)
                .extracting(User::getName)
                .containsExactly("James White");
    }

    @Test
    @DisplayName("Should find users by last name")
    void testLastNameSearch() throws Exception {
        List<User> results = performSearch("Smith", "User");

        assertThat(results)
                .hasSize(1)
                .extracting(User::getName)
                .containsExactly("Jane Smith");
    }

    @Test
    @DisplayName("Should find long-term employees")
    void testLongTermEmployeesSearch() throws Exception {
        List<User> results = performSearch("long-term employees", "User");

        assertThat(results)
                .hasSizeGreaterThanOrEqualTo(4) // Users who started before 2020 or in early 2020
                .extracting(user -> user.getFirstWorkingDay().getYear())
                .allMatch(year -> year <= 2020);
    }

    @Test
    @DisplayName("Should find users with 'martinez' in email")
    void testMartinezEmailSearch() throws Exception {
        List<User> results = performSearch("martinez", "User");

        assertThat(results)
                .hasSize(1)
                .extracting(User::getName)
                .containsExactly("David Martinez");
    }

    @Test
    @DisplayName("Should find users by name pattern 'Mike'")
    void testMikeNameSearch() throws Exception {
        List<User> results = performSearch("Mike", "User");

        assertThat(results)
                .hasSize(1)
                .extracting(User::getName)
                .containsExactly("Mike Brown");
    }

    @Test
    @DisplayName("Should find new employees")
    void testNewEmployeesSearch() throws Exception {
        List<User> results = performSearch("new employees", "User");

        assertThat(results)
                .hasSizeGreaterThanOrEqualTo(3) // Users who started recently (2023 onwards)
                .extracting(user -> user.getFirstWorkingDay().getYear())
                .allMatch(year -> year >= 2023);
    }

    @Test
    @DisplayName("Should handle nonsensical search query gracefully")
    void testNonsensicalSearchQuery() throws Exception {
        List<User> results = performSearch("purple elephant dancing", "User");

        // Should either return empty results or fall back to simple text matching
        assertThat(results).isNotNull();
    }

    @Test
    @DisplayName("Should find users by partial name")
    void testPartialNameSearch() throws Exception {
        List<User> results = performSearch("Johnson", "User");

        assertThat(results)
                .hasSize(1)
                .extracting(User::getName)
                .containsExactly("Bob Johnson");
    }

    @Test
    @DisplayName("Should find users with 'Robert' in name")
    void testRobertNameSearch() throws Exception {
        List<User> results = performSearch("Robert", "User");

        assertThat(results)
                .hasSize(1)
                .extracting(User::getName)
                .containsExactly("Robert Taylor");
    }

    @Test
    @DisplayName("Should find users by name pattern 'Sarah'")
    void testSarahNameSearch() throws Exception {
        List<User> results = performSearch("Sarah", "User");

        assertThat(results)
                .hasSize(1)
                .extracting(User::getName)
                .containsExactly("Sarah Davis");
    }

    @Test
    @DisplayName("Should find senior employees")
    void testSeniorEmployeesSearch() throws Exception {
        List<User> results = performSearch("senior employees", "User");

        assertThat(results)
                .hasSizeGreaterThanOrEqualTo(5) // Users with 5+ years of service
                .extracting(user -> user.getFirstWorkingDay().getYear())
                .allMatch(year -> year <= 2020);
    }

    @Test
    @DisplayName("Should find users created in 2025")
    void testUsersCreatedInYearSearch() throws Exception {
        List<User> results = performSearch("users created in 2025", "User");

        assertThat(results)
                .hasSize(1) // Only Maria Garcia was created in 2025
                .extracting(user -> user.getCreated().getYear())
                .containsExactly(2025);
    }

    @Test
    @DisplayName("Should find users starting work after 2020")
    void testUsersStartingAfterDateSearch() throws Exception {
        List<User> results = performSearch("users starting after 2020", "User");

        assertThat(results)
                .hasSizeGreaterThanOrEqualTo(6) // Should exclude users who started in 2020 or earlier
                .extracting(user -> user.getFirstWorkingDay().getYear())
                .allMatch(year -> year > 2020);
    }

    @Test
    @DisplayName("Should find users starting work before 2020")
    void testUsersStartingBeforeDateSearch() throws Exception {
        List<User> results = performSearch("users starting before 2020", "User");

        assertThat(results)
                .hasSizeGreaterThanOrEqualTo(2) // Should include users who started before 2020
                .extracting(user -> user.getFirstWorkingDay().getYear())
                .allMatch(year -> year < 2020);
    }

    @Test
    @DisplayName("Should find users starting work in 2021")
    void testUsersStartingIn2021Search() throws Exception {
        List<User> results = performSearch("started in 2021", "User");

        assertThat(results)
                .hasSize(2) // Bob Johnson and Lisa Anderson started in 2021
                .extracting(user -> user.getFirstWorkingDay().getYear())
                .containsOnly(2021);
    }

    @Test
    @DisplayName("Should find users starting work in summer")
    void testUsersStartingInSummerSearch() throws Exception {
        List<User> results = performSearch("users starting in summer", "User");

        assertThat(results)
                .hasSizeGreaterThanOrEqualTo(3) // Users starting in June, July, August
                .extracting(user -> user.getFirstWorkingDay().getMonthValue())
                .contains(6, 7, 8);
    }

    @Test
    @DisplayName("Should find users starting work in 2024")
    void testUsersStartingInYearSearch() throws Exception {
        List<User> results = performSearch("users starting in 2024", "User");

        assertThat(results)
                .hasSize(1) // Only Robert Taylor started in 2024
                .extracting(User::getName)
                .containsExactly("Robert Taylor");
    }

    @Test
    @DisplayName("Should find users updated in 2025")
    void testUsersUpdatedInYearSearch() throws Exception {
        List<User> results = performSearch("users updated in 2025", "User");

        assertThat(results)
                .hasSize(2) // James White and Maria Garcia were updated in 2025
                .extracting(user -> user.getUpdated().getYear())
                .containsOnly(2025);
    }

    @Test
    @DisplayName("Should find users who left the company")
    void testUsersWhoLeftSearch() throws Exception {
        List<User> results = performSearch("users who left", "User");

        assertThat(results)
                .hasSizeGreaterThanOrEqualTo(2) // Bob Johnson and David Martinez have lastWorkingDay
                .extracting(User::getLastWorkingDay)
                .doesNotContainNull();
    }
}
