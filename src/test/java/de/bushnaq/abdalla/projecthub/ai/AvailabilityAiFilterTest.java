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
import de.bushnaq.abdalla.projecthub.dto.Availability;
import de.bushnaq.abdalla.projecthub.dto.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestConstructor;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration test for Availability AI filtering testing real LLM regex pattern generation
 * and filtering capabilities with various search scenarios.
 * <p>
 * This test requires Ollama to be running with a model available (e.g., llama3.2:1b).
 * The test will be skipped if Ollama is not available.
 */
@SpringBootTest
@ActiveProfiles("test")
@TestConstructor(autowireMode = TestConstructor.AutowireMode.ALL)
class AvailabilityAiFilterTest extends AbstractAiFilterTest<Availability> {

    public AvailabilityAiFilterTest(ObjectMapper mapper, AiFilter aiFilter) {
        super(mapper, aiFilter);
    }

    private Availability createAvailability(Long id, float availability, LocalDate start,
                                            User user, OffsetDateTime created, OffsetDateTime updated) {
        Availability avail = new Availability();
        avail.setId(id);
        avail.setAvailability(availability);
        avail.setStart(start);
        avail.setUser(user);
        avail.setCreated(created);
        avail.setUpdated(updated);
        return avail;
    }

    private User createUser(Long id, String name) {
        User user = new User();
        user.setId(id);
        user.setName(name);
        return user;
    }

    @BeforeEach
    void setUp() {
        // Create test data
        setupTestAvailabilities();
    }

    private void setupTestAvailabilities() {
        testProducts = new ArrayList<>();

        // Create test users
        User johnDoe     = createUser(1L, "John Doe");
        User janeSmith   = createUser(2L, "Jane Smith");
        User bobJohnson  = createUser(3L, "Bob Johnson");
        User aliceWilson = createUser(4L, "Alice Wilson");
        User mikeBrown   = createUser(5L, "Mike Brown");

        // Availabilities with different percentage values and dates for comprehensive testing
        testProducts.add(createAvailability(1L, 1.0f, LocalDate.of(2024, 1, 15), johnDoe,
                OffsetDateTime.of(2023, 12, 20, 10, 0, 0, 0, ZoneOffset.UTC),
                OffsetDateTime.of(2024, 1, 10, 14, 30, 0, 0, ZoneOffset.UTC)));

        testProducts.add(createAvailability(2L, 0.8f, LocalDate.of(2024, 2, 1), janeSmith,
                OffsetDateTime.of(2024, 1, 15, 11, 15, 0, 0, ZoneOffset.UTC),
                OffsetDateTime.of(2024, 2, 5, 16, 45, 0, 0, ZoneOffset.UTC)));

        testProducts.add(createAvailability(3L, 0.5f, LocalDate.of(2024, 3, 1), bobJohnson,
                OffsetDateTime.of(2024, 2, 20, 8, 30, 0, 0, ZoneOffset.UTC),
                OffsetDateTime.of(2024, 3, 16, 13, 20, 0, 0, ZoneOffset.UTC)));

        testProducts.add(createAvailability(4L, 0.75f, LocalDate.of(2024, 4, 1), aliceWilson,
                OffsetDateTime.of(2024, 3, 25, 15, 45, 0, 0, ZoneOffset.UTC),
                OffsetDateTime.of(2024, 4, 15, 12, 10, 0, 0, ZoneOffset.UTC)));

        testProducts.add(createAvailability(5L, 0.9f, LocalDate.of(2024, 5, 1), mikeBrown,
                OffsetDateTime.of(2024, 4, 20, 14, 0, 0, 0, ZoneOffset.UTC),
                OffsetDateTime.of(2024, 5, 1, 11, 40, 0, 0, ZoneOffset.UTC)));

        testProducts.add(createAvailability(6L, 0.6f, LocalDate.of(2024, 6, 1), johnDoe,
                OffsetDateTime.of(2024, 5, 28, 9, 20, 0, 0, ZoneOffset.UTC),
                OffsetDateTime.of(2024, 6, 10, 16, 15, 0, 0, ZoneOffset.UTC)));

        testProducts.add(createAvailability(7L, 0.25f, LocalDate.of(2024, 7, 1), janeSmith,
                OffsetDateTime.of(2024, 6, 25, 12, 30, 0, 0, ZoneOffset.UTC),
                OffsetDateTime.of(2024, 7, 29, 15, 50, 0, 0, ZoneOffset.UTC)));

        testProducts.add(createAvailability(8L, 0.85f, LocalDate.of(2024, 8, 1), bobJohnson,
                OffsetDateTime.of(2024, 7, 30, 8, 15, 0, 0, ZoneOffset.UTC),
                OffsetDateTime.of(2024, 8, 15, 17, 30, 0, 0, ZoneOffset.UTC)));

        testProducts.add(createAvailability(9L, 0.0f, LocalDate.of(2024, 9, 1), aliceWilson,
                OffsetDateTime.of(2024, 8, 28, 13, 45, 0, 0, ZoneOffset.UTC),
                OffsetDateTime.of(2024, 9, 5, 9, 20, 0, 0, ZoneOffset.UTC)));

        testProducts.add(createAvailability(10L, 0.95f, LocalDate.of(2024, 10, 1), mikeBrown,
                OffsetDateTime.of(2024, 9, 25, 10, 30, 0, 0, ZoneOffset.UTC),
                OffsetDateTime.of(2024, 10, 22, 14, 45, 0, 0, ZoneOffset.UTC)));

        testProducts.add(createAvailability(11L, 0.7f, LocalDate.of(2025, 1, 1), johnDoe,
                OffsetDateTime.of(2024, 12, 28, 9, 0, 0, 0, ZoneOffset.UTC),
                OffsetDateTime.of(2025, 1, 10, 16, 30, 0, 0, ZoneOffset.UTC)));

        testProducts.add(createAvailability(12L, 0.4f, LocalDate.of(2025, 2, 1), janeSmith,
                OffsetDateTime.of(2025, 1, 28, 8, 15, 0, 0, ZoneOffset.UTC),
                OffsetDateTime.of(2025, 2, 10, 12, 20, 0, 0, ZoneOffset.UTC)));
    }

    @Test
    @DisplayName("Should find availability between 60% and 80%")
    void testAvailabilityBetweenSixtyAndEightyPercentSearch() throws Exception {
        List<Availability> results = performSearch("availability between 60% and 80%", "Availability");

        assertThat(results)
                .hasSizeGreaterThanOrEqualTo(3) // Should include 0.6, 0.7, 0.75, 0.8
                .extracting(Availability::getAvailability)
                .contains(0.6f, 0.7f, 0.75f, 0.8f);
    }

    @Test
    @DisplayName("Should find availability created in 2025")
    void testAvailabilityCreatedInYearSearch() throws Exception {
        List<Availability> results = performSearch("availability created in 2025", "Availability");

        assertThat(results)
                .hasSize(1) // Only one availability created in 2025
                .extracting(availability -> availability.getCreated().getYear())
                .containsExactly(2025);
    }

    @Test
    @DisplayName("Should find availability for Jane Smith")
    void testAvailabilityForJaneSmithSearch() throws Exception {
        List<Availability> results = performSearch("Jane Smith", "Availability");

        assertThat(results)
                .hasSize(2) // Jane Smith has 2 availability records
                .extracting(availability -> availability.getUser().getName())
                .containsOnly("Jane Smith");
    }

    @Test
    @DisplayName("Should find availability for John Doe")
    void testAvailabilityForSpecificUserSearch() throws Exception {
        List<Availability> results = performSearch("John Doe", "Availability");

        assertThat(results)
                .hasSize(3) // John Doe has 3 availability records
                .extracting(availability -> availability.getUser().getName())
                .containsOnly("John Doe");
    }

    @Test
    @DisplayName("Should find availability greater than 50%")
    void testAvailabilityGreaterThanFiftyPercentSearch() throws Exception {
        List<Availability> results = performSearch("availability greater than 50%", "Availability");

        assertThat(results)
                .hasSizeGreaterThanOrEqualTo(7) // Should include 0.6, 0.75, 0.8, 0.85, 0.9, 0.95, 1.0
                .extracting(Availability::getAvailability)
                .contains(0.6f, 0.75f, 0.8f, 0.85f, 0.9f, 0.95f, 1.0f);
    }

    @Test
    @DisplayName("Should find availability greater than or equal to 70%")
    void testAvailabilityGreaterThanOrEqualSeventyPercentSearch() throws Exception {
        List<Availability> results = performSearch("availability greater than or equal to 70%", "Availability");

        assertThat(results)
                .hasSizeGreaterThanOrEqualTo(6) // Should include 0.7, 0.75, 0.8, 0.85, 0.9, 0.95, 1.0
                .extracting(Availability::getAvailability)
                .contains(0.7f, 0.75f, 0.8f, 0.85f, 0.9f, 0.95f, 1.0f);
    }

    @Test
    @DisplayName("Should find availability less than 90%")
    void testAvailabilityLessThanNinetyPercentSearch() throws Exception {
        List<Availability> results = performSearch("availability less than 90%", "Availability");

        assertThat(results)
                .hasSizeGreaterThanOrEqualTo(9) // Should exclude 0.9, 0.95, 1.0
                .extracting(Availability::getAvailability)
                .contains(0.0f, 0.25f, 0.4f, 0.5f, 0.6f, 0.7f, 0.75f, 0.8f, 0.85f);
    }

    @Test
    @DisplayName("Should find availability less than or equal to 40%")
    void testAvailabilityLessThanOrEqualFortyPercentSearch() throws Exception {
        List<Availability> results = performSearch("availability less than or equal to 40%", "Availability");

        assertThat(results)
                .hasSizeGreaterThanOrEqualTo(3) // Should include 0.0, 0.25, 0.4
                .extracting(Availability::getAvailability)
                .contains(0.0f, 0.25f, 0.4f);
    }

    @Test
    @DisplayName("Should find availability starting after January 2024")
    void testAvailabilityStartingAfterDateSearch() throws Exception {
        List<Availability> results = performSearch("availability starting after January 2024", "Availability");

        assertThat(results)
                .hasSizeGreaterThanOrEqualTo(10) // Should exclude the first availability
                .extracting(availability -> availability.getStart().getMonthValue())
                .allMatch(month -> month >= 2 || month == 1); // February onwards or January 2025
    }

    @Test
    @DisplayName("Should find availability starting before March 2024")
    void testAvailabilityStartingBeforeDateSearch() throws Exception {
        List<Availability> results = performSearch("availability starting before March 2024", "Availability");

        assertThat(results)
                .hasSizeGreaterThanOrEqualTo(2) // Should include January and February 2024
                .extracting(availability -> availability.getStart().getMonthValue())
                .contains(1, 2);
    }

    @Test
    @DisplayName("Should find availability starting in January 2025")
    void testAvailabilityStartingInSpecificMonthSearch() throws Exception {
        List<Availability> results = performSearch("availability starting in January 2025", "Availability");

        assertThat(results)
                .hasSize(1)
                .extracting(availability -> availability.getStart())
                .containsExactly(LocalDate.of(2025, 1, 1));
    }

    @Test
    @DisplayName("Should find availability starting in summer 2024")
    void testAvailabilityStartingSummerSearch() throws Exception {
        List<Availability> results = performSearch("availability starting in summer 2024", "Availability");

        assertThat(results)
                .hasSizeGreaterThanOrEqualTo(3) // Should include June, July, August
                .extracting(availability -> availability.getStart().getMonthValue())
                .contains(6, 7, 8);
    }

    @Test
    @DisplayName("Should find availability updated in 2025")
    void testAvailabilityUpdatedInYearSearch() throws Exception {
        List<Availability> results = performSearch("availability updated in 2025", "Availability");

        assertThat(results)
                .hasSize(2) // Two availabilities updated in 2025
                .extracting(availability -> availability.getUpdated().getYear())
                .contains(2025);
    }

    @Test
    @DisplayName("Should find 80% availability")
    void testEightyPercentAvailabilitySearch() throws Exception {
        List<Availability> results = performSearch("80% availability", "Availability");

        assertThat(results)
                .hasSize(1)
                .extracting(Availability::getAvailability)
                .containsExactly(0.8f);
    }

    @Test
    @DisplayName("Should handle empty search query")
    void testEmptySearchQuery() throws Exception {
        List<Availability> results = performSearch("", "Availability");

        assertThat(results).hasSize(12); // All availabilities should match empty query
    }

    @Test
    @DisplayName("Should find 50% availability")
    void testFiftyPercentAvailabilitySearch() throws Exception {
        List<Availability> results = performSearch("50% availability", "Availability");

        assertThat(results)
                .hasSize(1)
                .extracting(Availability::getAvailability)
                .containsExactly(0.5f);
    }

    @Test
    @DisplayName("Should find 100% availability")
    void testFullAvailabilitySearch() throws Exception {
        List<Availability> results = performSearch("full availability", "Availability");

        assertThat(results)
                .hasSize(1)
                .extracting(Availability::getAvailability)
                .containsExactly(1.0f);
    }

    @Test
    @DisplayName("Should find high availability (over 85%)")
    void testHighAvailabilitySearch() throws Exception {
        List<Availability> results = performSearch("high availability", "Availability");

        assertThat(results)
                .hasSizeGreaterThanOrEqualTo(3) // Should include 0.9, 0.95, 1.0
                .extracting(Availability::getAvailability)
                .contains(0.9f, 0.95f, 1.0f);
    }

    @Test
    @DisplayName("Should find low availability (under 30%)")
    void testLowAvailabilitySearch() throws Exception {
        List<Availability> results = performSearch("low availability", "Availability");

        assertThat(results)
                .hasSizeGreaterThanOrEqualTo(2) // Should include 0.0, 0.25
                .extracting(Availability::getAvailability)
                .contains(0.0f, 0.25f);
    }

    @Test
    @DisplayName("Should find 95% availability")
    void testNinetyFivePercentAvailabilitySearch() throws Exception {
        List<Availability> results = performSearch("95% availability", "Availability");

        assertThat(results)
                .hasSize(1)
                .extracting(Availability::getAvailability)
                .containsExactly(0.95f);
    }

    @Test
    @DisplayName("Should find 90% availability")
    void testNinetyPercentAvailabilitySearch() throws Exception {
        List<Availability> results = performSearch("90% availability", "Availability");

        assertThat(results)
                .hasSize(1)
                .extracting(Availability::getAvailability)
                .containsExactly(0.9f);
    }

    @Test
    @DisplayName("Should handle nonsensical search query gracefully")
    void testNonsensicalSearchQuery() throws Exception {
        List<Availability> results = performSearch("purple elephant dancing", "Availability");

        // Should either return empty results or fall back to simple text matching
        assertThat(results).isNotNull();
    }

    @Test
    @DisplayName("Should find partial availability")
    void testPartialAvailabilitySearch() throws Exception {
        List<Availability> results = performSearch("partial availability", "Availability");

        assertThat(results)
                .hasSizeGreaterThanOrEqualTo(10) // Should exclude full availability (1.0)
                .extracting(Availability::getAvailability)
                .contains(0.25f, 0.4f, 0.5f, 0.6f, 0.7f, 0.75f, 0.8f, 0.85f, 0.9f, 0.95f);
    }

    @Test
    @DisplayName("Should find 75% availability")
    void testSeventyFivePercentAvailabilitySearch() throws Exception {
        List<Availability> results = performSearch("75% availability", "Availability");

        assertThat(results)
                .hasSize(1)
                .extracting(Availability::getAvailability)
                .containsExactly(0.75f);
    }

    @Test
    @DisplayName("Should find zero availability")
    void testZeroAvailabilitySearch() throws Exception {
        List<Availability> results = performSearch("zero availability", "Availability");

        assertThat(results)
                .hasSize(1)
                .extracting(Availability::getAvailability)
                .containsExactly(0.0f);
    }
}
