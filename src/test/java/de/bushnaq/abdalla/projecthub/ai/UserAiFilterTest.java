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
import java.util.Arrays;
import java.util.Collections;
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

    // === OTHER VALUABLE TEST CASES (preserved from original) ===
    @Test
    @DisplayName("Should find active users")
    void testActiveUsersSearch() throws Exception {
        List<User> results = performSearch("active users", "User");
        List<User> expected = Arrays.asList(
                testProducts.get(0), // John Doe (no lastWorkingDay)
                testProducts.get(1), // Jane Smith (no lastWorkingDay)
                testProducts.get(3), // Alice Wilson (no lastWorkingDay)
                testProducts.get(4), // Mike Brown (no lastWorkingDay)
                testProducts.get(5), // Sarah Davis (no lastWorkingDay)
                testProducts.get(7), // Lisa Anderson (no lastWorkingDay)
                testProducts.get(8), // Robert Taylor (no lastWorkingDay)
                testProducts.get(9), // Emily Clark (no lastWorkingDay)
                testProducts.get(10), // James White (no lastWorkingDay)
                testProducts.get(11)  // Maria Garcia (no lastWorkingDay)
        );

        assertThat(results).hasSize(expected.size());
        assertThat(results).containsExactlyInAnyOrderElementsOf(expected);
    }

    @Test
    @DisplayName("Should find users by created date column")
    void testCreatedDateSpecificSearchWithLLM() throws Exception {
        List<User> results  = performSearch("created in 2025", "User");
        List<User> expected = Collections.singletonList(testProducts.get(11)); // Maria Garcia

        assertThat(results).hasSize(expected.size());
        assertThat(results).containsExactlyInAnyOrderElementsOf(expected);
    }

    @Test
    @DisplayName("Should find users by department email pattern")
    void testDepartmentEmailSearch() throws Exception {
        List<User> results  = performSearch("company.com employees", "User");
        List<User> expected = new ArrayList<>(testProducts); // All users work at company.com

        assertThat(results).hasSize(expected.size());
        assertThat(results).containsExactlyInAnyOrderElementsOf(expected);
    }

    @Test
    @DisplayName("Should find users by email domain")
    void testEmailDomainSearch() throws Exception {
        List<User> results  = performSearch("@company.com", "User");
        List<User> expected = new ArrayList<>(testProducts); // All users have company.com email

        assertThat(results).hasSize(expected.size());
        assertThat(results).containsExactlyInAnyOrderElementsOf(expected);
    }

    @Test
    @DisplayName("Should find users by email column")
    void testEmailSpecificSearchWithLLM() throws Exception {
        List<User> results  = performSearch("email contains alice", "User");
        List<User> expected = Collections.singletonList(testProducts.get(3)); // Alice Wilson

        assertThat(results).hasSize(expected.size());
        assertThat(results).containsExactlyInAnyOrderElementsOf(expected);
    }

    @Test
    @DisplayName("Should handle empty search query")
    void testEmptySearchQuery() throws Exception {
        List<User> results  = performSearch("", "User");
        List<User> expected = new ArrayList<>(testProducts); // All users should match empty query

        assertThat(results).hasSize(expected.size());
        assertThat(results).containsExactlyInAnyOrderElementsOf(expected);
    }

    @Test
    @DisplayName("Should find users by firstWorkingDay column")
    void testFirstWorkingDaySpecificSearchWithLLM() throws Exception {
        List<User> results  = performSearch("firstWorkingDay in 2018", "User");
        List<User> expected = Collections.singletonList(testProducts.get(4)); // Mike Brown

        assertThat(results).hasSize(expected.size());
        assertThat(results).containsExactlyInAnyOrderElementsOf(expected);
    }

    @Test
    @DisplayName("Should find former employees")
    void testFormerEmployeesSearch() throws Exception {
        List<User> results = performSearch("former employees", "User");
        List<User> expected = Arrays.asList(
                testProducts.get(2), // Bob Johnson (lastWorkingDay: 2024-06-30)
                testProducts.get(6)  // David Martinez (lastWorkingDay: 2023-12-15)
        );

        assertThat(results).hasSize(expected.size());
        assertThat(results).containsExactlyInAnyOrderElementsOf(expected);
    }

    @Test
    @DisplayName("Should find users by lastWorkingDay column")
    void testLastWorkingDaySpecificSearchWithLLM() throws Exception {
        List<User> results  = performSearch("lastWorkingDay is not null", "User");
        List<User> expected = Arrays.asList(testProducts.get(2), testProducts.get(6)); // Bob Johnson, David Martinez

        assertThat(results).hasSize(expected.size());
        assertThat(results).containsExactlyInAnyOrderElementsOf(expected);
    }

    @Test
    @DisplayName("Should find long-term employees")
    void testLongTermEmployeesSearch() throws Exception {
        List<User> results = performSearch("long-term employees", "User");
        List<User> expected = Arrays.asList(
                testProducts.get(0), // John Doe (started 2020-03-15)
                testProducts.get(1), // Jane Smith (started 2019-06-01)
                testProducts.get(4), // Mike Brown (started 2018-11-20)
                testProducts.get(6), // David Martinez (started 2017-04-03)
                testProducts.get(9), // Emily Clark (started 2020-10-12)
                testProducts.get(10) // James White (started 2019-12-02)
        );

        assertThat(results).hasSize(expected.size());
        assertThat(results).containsExactlyInAnyOrderElementsOf(expected);
    }

    // === COLUMN-SPECIFIC TESTS (one per column) ===
    @Test
    @DisplayName("Should find users by name column")
    void testNameSpecificSearchWithLLM() throws Exception {
        List<User> results  = performSearch("name contains Smith", "User");
        List<User> expected = Collections.singletonList(testProducts.get(1)); // Jane Smith

        assertThat(results).hasSize(expected.size());
        assertThat(results).containsExactlyInAnyOrderElementsOf(expected);
    }

    @Test
    @DisplayName("Should find new employees")
    void testNewEmployeesSearch() throws Exception {
        List<User> results = performSearch("new employees", "User");
        List<User> expected = Arrays.asList(
                testProducts.get(5), // Sarah Davis (started 2023-02-14)
                testProducts.get(8), // Robert Taylor (started 2024-01-08)
                testProducts.get(11) // Maria Garcia (started 2025-03-01)
        );

        assertThat(results).hasSize(expected.size());
        assertThat(results).containsExactlyInAnyOrderElementsOf(expected);
    }

    @Test
    @DisplayName("Should handle nonsensical search query gracefully")
    void testNonsensicalSearchQuery() throws Exception {
        List<User> results  = performSearch("purple elephant dancing", "User");
        List<User> expected = Collections.emptyList(); // Should return empty results for nonsensical queries

        assertThat(results).hasSize(expected.size());
        assertThat(results).containsExactlyInAnyOrderElementsOf(expected);
    }

    @Test
    @DisplayName("Should find senior employees")
    void testSeniorEmployeesSearch() throws Exception {
        List<User> results = performSearch("senior employees", "User");
        List<User> expected = Arrays.asList(
                testProducts.get(0), // John Doe (started 2020-03-15)
                testProducts.get(1), // Jane Smith (started 2019-06-01)
                testProducts.get(4), // Mike Brown (started 2018-11-20)
                testProducts.get(6), // David Martinez (started 2017-04-03)
                testProducts.get(9), // Emily Clark (started 2020-10-12)
                testProducts.get(10) // James White (started 2019-12-02)
        );

        assertThat(results).hasSize(expected.size());
        assertThat(results).containsExactlyInAnyOrderElementsOf(expected);
    }

    // === SIMPLE TEST CASE (keeping only ONE) ===
    @Test
    @DisplayName("Should generate working regex for simple text search")
    void testSimpleTextSearchWithLLM() throws Exception {
        List<User> results  = performSearch("John", "User");
        List<User> expected = Collections.singletonList(testProducts.get(0)); // John Doe

        assertThat(results).hasSize(expected.size());
        assertThat(results).containsExactlyInAnyOrderElementsOf(expected);
    }

    @Test
    @DisplayName("Should find users by updated date column")
    void testUpdatedDateSpecificSearchWithLLM() throws Exception {
        List<User> results  = performSearch("updated in 2025", "User");
        List<User> expected = Arrays.asList(testProducts.get(10), testProducts.get(11)); // James White, Maria Garcia

        assertThat(results).hasSize(expected.size());
        assertThat(results).containsExactlyInAnyOrderElementsOf(expected);
    }

    @Test
    @DisplayName("Should find users starting work after 2020")
    void testUsersStartingAfterDateSearch() throws Exception {
        List<User> results = performSearch("users starting after 2020", "User");
        List<User> expected = Arrays.asList(
                testProducts.get(2), // Bob Johnson (started 2021-01-10)
                testProducts.get(3), // Alice Wilson (started 2022-09-05)
                testProducts.get(5), // Sarah Davis (started 2023-02-14)
                testProducts.get(7), // Lisa Anderson (started 2021-08-16)
                testProducts.get(8), // Robert Taylor (started 2024-01-08)
                testProducts.get(11) // Maria Garcia (started 2025-03-01)
        );

        assertThat(results).hasSize(expected.size());
        assertThat(results).containsExactlyInAnyOrderElementsOf(expected);
    }

    @Test
    @DisplayName("Should find users starting work before 2020")
    void testUsersStartingBeforeDateSearch() throws Exception {
        List<User> results = performSearch("users starting before 2020", "User");
        List<User> expected = Arrays.asList(
                testProducts.get(1), // Jane Smith (started 2019-06-01)
                testProducts.get(4), // Mike Brown (started 2018-11-20)
                testProducts.get(6), // David Martinez (started 2017-04-03)
                testProducts.get(10) // James White (started 2019-12-02)
        );

        assertThat(results).hasSize(expected.size());
        assertThat(results).containsExactlyInAnyOrderElementsOf(expected);
    }

    @Test
    @DisplayName("Should find users starting work in 2021")
    void testUsersStartingIn2021Search() throws Exception {
        List<User> results = performSearch("started in 2021", "User");
        List<User> expected = Arrays.asList(
                testProducts.get(2), // Bob Johnson (started 2021-01-10)
                testProducts.get(7)  // Lisa Anderson (started 2021-08-16)
        );

        assertThat(results).hasSize(expected.size());
        assertThat(results).containsExactlyInAnyOrderElementsOf(expected);
    }

    @Test
    @DisplayName("Should find users starting work in summer")
    void testUsersStartingInSummerSearch() throws Exception {
        List<User> results = performSearch("users starting in summer", "User");
        List<User> expected = Arrays.asList(
                testProducts.get(1), // Jane Smith (started June 1)
                testProducts.get(7)  // Lisa Anderson (started August 16)
        );

        assertThat(results).hasSize(expected.size());
        assertThat(results).containsExactlyInAnyOrderElementsOf(expected);
    }

    @Test
    @DisplayName("Should find users starting work in 2024")
    void testUsersStartingInYearSearch() throws Exception {
        List<User> results  = performSearch("users starting in 2024", "User");
        List<User> expected = Collections.singletonList(testProducts.get(8)); // Robert Taylor (started 2024-01-08)

        assertThat(results).hasSize(expected.size());
        assertThat(results).containsExactlyInAnyOrderElementsOf(expected);
    }

    @Test
    @DisplayName("Should find users who left the company")
    void testUsersWhoLeftSearch() throws Exception {
        List<User> results = performSearch("users who left", "User");
        List<User> expected = Arrays.asList(
                testProducts.get(2), // Bob Johnson (lastWorkingDay: 2024-06-30)
                testProducts.get(6)  // David Martinez (lastWorkingDay: 2023-12-15)
        );

        assertThat(results).hasSize(expected.size());
        assertThat(results).containsExactlyInAnyOrderElementsOf(expected);
    }
}
