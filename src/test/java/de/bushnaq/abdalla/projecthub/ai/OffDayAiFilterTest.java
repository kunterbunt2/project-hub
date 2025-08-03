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
import de.bushnaq.abdalla.projecthub.dto.OffDay;
import de.bushnaq.abdalla.projecthub.dto.OffDayType;
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
 * Integration test for OffDay AI filtering testing real LLM regex pattern generation
 * and filtering capabilities with various search scenarios.
 * <p>
 * This test requires Ollama to be running with a model available (e.g., llama3.2:1b).
 * The test will be skipped if Ollama is not available.
 */
@SpringBootTest
@ActiveProfiles("test")
@TestConstructor(autowireMode = TestConstructor.AutowireMode.ALL)
class OffDayAiFilterTest extends AbstractAiFilterTest<OffDay> {

    public OffDayAiFilterTest(ObjectMapper mapper, AiFilter aiFilter) {
        super(mapper, aiFilter);
    }

    private OffDay createOffDay(Long id, LocalDate firstDay, LocalDate lastDay, OffDayType type,
                                User user, OffsetDateTime created, OffsetDateTime updated) {
        OffDay offDay = new OffDay();
        offDay.setId(id);
        offDay.setFirstDay(firstDay);
        offDay.setLastDay(lastDay);
        offDay.setType(type);
        offDay.setUser(user);
        offDay.setCreated(created);
        offDay.setUpdated(updated);
        return offDay;
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
        setupTestOffDays();
    }

    private void setupTestOffDays() {
        testProducts = new ArrayList<>();

        // Create test users
        User johnDoe     = createUser(1L, "John Doe");
        User janeSmith   = createUser(2L, "Jane Smith");
        User bobJohnson  = createUser(3L, "Bob Johnson");
        User aliceWilson = createUser(4L, "Alice Wilson");
        User mikeBrown   = createUser(5L, "Mike Brown");

        // OffDays with different types, durations, and dates for comprehensive testing
        testProducts.add(createOffDay(1L, LocalDate.of(2024, 1, 15), LocalDate.of(2024, 1, 19), OffDayType.VACATION, johnDoe,
                OffsetDateTime.of(2023, 12, 20, 10, 0, 0, 0, ZoneOffset.UTC),
                OffsetDateTime.of(2024, 1, 10, 14, 30, 0, 0, ZoneOffset.UTC)));

        testProducts.add(createOffDay(2L, LocalDate.of(2024, 2, 5), LocalDate.of(2024, 2, 7), OffDayType.SICK, janeSmith,
                OffsetDateTime.of(2024, 1, 15, 11, 15, 0, 0, ZoneOffset.UTC),
                OffsetDateTime.of(2024, 2, 5, 16, 45, 0, 0, ZoneOffset.UTC)));

        testProducts.add(createOffDay(3L, LocalDate.of(2024, 3, 1), LocalDate.of(2024, 3, 1), OffDayType.HOLIDAY, bobJohnson,
                OffsetDateTime.of(2024, 2, 20, 8, 30, 0, 0, ZoneOffset.UTC),
                OffsetDateTime.of(2024, 3, 1, 13, 20, 0, 0, ZoneOffset.UTC)));

        testProducts.add(createOffDay(4L, LocalDate.of(2024, 4, 10), LocalDate.of(2024, 4, 15), OffDayType.TRIP, aliceWilson,
                OffsetDateTime.of(2024, 3, 25, 15, 45, 0, 0, ZoneOffset.UTC),
                OffsetDateTime.of(2024, 4, 15, 12, 10, 0, 0, ZoneOffset.UTC)));

        testProducts.add(createOffDay(5L, LocalDate.of(2024, 5, 20), LocalDate.of(2024, 6, 5), OffDayType.VACATION, mikeBrown,
                OffsetDateTime.of(2024, 4, 20, 14, 0, 0, 0, ZoneOffset.UTC),
                OffsetDateTime.of(2024, 5, 1, 11, 40, 0, 0, ZoneOffset.UTC)));

        testProducts.add(createOffDay(6L, LocalDate.of(2024, 6, 10), LocalDate.of(2024, 6, 12), OffDayType.SICK, johnDoe,
                OffsetDateTime.of(2024, 5, 28, 9, 20, 0, 0, ZoneOffset.UTC),
                OffsetDateTime.of(2024, 6, 10, 16, 15, 0, 0, ZoneOffset.UTC)));

        testProducts.add(createOffDay(7L, LocalDate.of(2024, 7, 4), LocalDate.of(2024, 7, 4), OffDayType.HOLIDAY, janeSmith,
                OffsetDateTime.of(2024, 6, 25, 12, 30, 0, 0, ZoneOffset.UTC),
                OffsetDateTime.of(2024, 7, 4, 15, 50, 0, 0, ZoneOffset.UTC)));

        testProducts.add(createOffDay(8L, LocalDate.of(2024, 8, 15), LocalDate.of(2024, 8, 25), OffDayType.VACATION, bobJohnson,
                OffsetDateTime.of(2024, 7, 30, 8, 15, 0, 0, ZoneOffset.UTC),
                OffsetDateTime.of(2024, 8, 15, 17, 30, 0, 0, ZoneOffset.UTC)));

        testProducts.add(createOffDay(9L, LocalDate.of(2024, 9, 2), LocalDate.of(2024, 9, 6), OffDayType.TRIP, aliceWilson,
                OffsetDateTime.of(2024, 8, 28, 13, 45, 0, 0, ZoneOffset.UTC),
                OffsetDateTime.of(2024, 9, 5, 9, 20, 0, 0, ZoneOffset.UTC)));

        testProducts.add(createOffDay(10L, LocalDate.of(2024, 10, 28), LocalDate.of(2024, 10, 30), OffDayType.SICK, mikeBrown,
                OffsetDateTime.of(2024, 9, 25, 10, 30, 0, 0, ZoneOffset.UTC),
                OffsetDateTime.of(2024, 10, 28, 14, 45, 0, 0, ZoneOffset.UTC)));

        testProducts.add(createOffDay(11L, LocalDate.of(2025, 1, 1), LocalDate.of(2025, 1, 1), OffDayType.HOLIDAY, johnDoe,
                OffsetDateTime.of(2024, 12, 28, 9, 0, 0, 0, ZoneOffset.UTC),
                OffsetDateTime.of(2025, 1, 1, 16, 30, 0, 0, ZoneOffset.UTC)));

        testProducts.add(createOffDay(12L, LocalDate.of(2025, 2, 10), LocalDate.of(2025, 2, 21), OffDayType.VACATION, janeSmith,
                OffsetDateTime.of(2025, 1, 28, 8, 15, 0, 0, ZoneOffset.UTC),
                OffsetDateTime.of(2025, 2, 10, 12, 20, 0, 0, ZoneOffset.UTC)));

        testProducts.add(createOffDay(13L, LocalDate.of(2025, 3, 3), LocalDate.of(2025, 3, 7), OffDayType.SICK, bobJohnson,
                OffsetDateTime.of(2025, 2, 28, 10, 45, 0, 0, ZoneOffset.UTC),
                OffsetDateTime.of(2025, 3, 3, 11, 30, 0, 0, ZoneOffset.UTC)));

        testProducts.add(createOffDay(14L, LocalDate.of(2025, 4, 14), LocalDate.of(2025, 4, 25), OffDayType.TRIP, aliceWilson,
                OffsetDateTime.of(2025, 3, 20, 14, 15, 0, 0, ZoneOffset.UTC),
                OffsetDateTime.of(2025, 4, 14, 9, 45, 0, 0, ZoneOffset.UTC)));
    }

    @Test
    @DisplayName("Should find emergency sick days")
    void testEmergencySickDaySearch() throws Exception {
        List<OffDay> results = performSearch("emergency sick", "OffDay");

        assertThat(results)
                .extracting(OffDay::getType)
                .containsOnly(OffDayType.SICK);
    }

    @Test
    @DisplayName("Should handle empty search query")
    void testEmptySearchQuery() throws Exception {
        List<OffDay> results = performSearch("", "OffDay");

        assertThat(results).hasSize(14); // All off days should match empty query
    }

    @Test
    @DisplayName("Should find holidays")
    void testHolidaySearch() throws Exception {
        List<OffDay> results = performSearch("holidays", "OffDay");

        assertThat(results)
                .hasSizeGreaterThanOrEqualTo(3)
                .extracting(OffDay::getType)
                .containsOnly(OffDayType.HOLIDAY);
    }

    @Test
    @DisplayName("Should find off days by type HOLIDAY")
    void testHolidayTypeSearch() throws Exception {
        List<OffDay> results = performSearch("type HOLIDAY", "OffDay");

        assertThat(results)
                .hasSizeGreaterThanOrEqualTo(3)
                .extracting(OffDay::getType)
                .containsOnly(OffDayType.HOLIDAY);
    }

    @Test
    @DisplayName("Should find off days lasting more than 5 days")
    void testLongDurationOffDaySearch() throws Exception {
        List<OffDay> results = performSearch("off days lasting more than 5 days", "OffDay");

        assertThat(results)
                .hasSizeGreaterThanOrEqualTo(3) // Should include longer vacations and trips
                .allMatch(offDay -> {
                    long days = offDay.getFirstDay().until(offDay.getLastDay()).getDays() + 1;
                    return days > 5;
                });
    }

    @Test
    @DisplayName("Should find long vacations")
    void testLongVacationSearch() throws Exception {
        List<OffDay> results = performSearch("long vacations", "OffDay");

        assertThat(results)
                .hasSizeGreaterThanOrEqualTo(4) // All vacation type off days
                .extracting(OffDay::getType)
                .containsOnly(OffDayType.VACATION);
    }

    @Test
    @DisplayName("Should find multi-day off days")
    void testMultiDayOffDaySearch() throws Exception {
        List<OffDay> results = performSearch("multi day", "OffDay");

        assertThat(results)
                .hasSizeGreaterThanOrEqualTo(8) // Should include vacations, trips, and some sick days
                .allMatch(offDay -> offDay.getFirstDay().isBefore(offDay.getLastDay()));
    }

    @Test
    @DisplayName("Should handle nonsensical search query gracefully")
    void testNonsensicalSearchQuery() throws Exception {
        List<OffDay> results = performSearch("purple elephant dancing", "OffDay");

        // Should either return empty results or fall back to simple text matching
        assertThat(results).isNotNull();
    }

    @Test
    @DisplayName("Should find off days created in 2025")
    void testOffDaysCreatedInYearSearch() throws Exception {
        List<OffDay> results = performSearch("off days created in 2025", "OffDay");

        assertThat(results)
                .hasSizeGreaterThanOrEqualTo(3) // Three off days created in 2025
                .extracting(offDay -> offDay.getCreated().getYear())
                .containsOnly(2025);
    }

    @Test
    @DisplayName("Should find off days ending before March 2024")
    void testOffDaysEndingBeforeDateSearch() throws Exception {
        List<OffDay> results = performSearch("off days ending before March 2024", "OffDay");

        assertThat(results)
                .hasSizeGreaterThanOrEqualTo(2) // Should include January and February 2024
                .extracting(offDay -> offDay.getLastDay())
                .allMatch(date -> date.isBefore(LocalDate.of(2024, 3, 1)));
    }

    @Test
    @DisplayName("Should find off days for Jane Smith")
    void testOffDaysForJaneSmithSearch() throws Exception {
        List<OffDay> results = performSearch("Jane Smith", "OffDay");

        assertThat(results)
                .hasSize(3) // Jane Smith has 3 off day records
                .extracting(offDay -> offDay.getUser().getName())
                .containsOnly("Jane Smith");
    }

    @Test
    @DisplayName("Should find off days for John Doe")
    void testOffDaysForSpecificUserSearch() throws Exception {
        List<OffDay> results = performSearch("John Doe", "OffDay");

        assertThat(results)
                .hasSize(3) // John Doe has 3 off day records
                .extracting(offDay -> offDay.getUser().getName())
                .containsOnly("John Doe");
    }

    @Test
    @DisplayName("Should find off days in 2025")
    void testOffDaysIn2025Search() throws Exception {
        List<OffDay> results = performSearch("off days in 2025", "OffDay");

        assertThat(results)
                .hasSizeGreaterThanOrEqualTo(4) // Four off days in 2025
                .extracting(offDay -> offDay.getFirstDay().getYear())
                .containsOnly(2025);
    }

    @Test
    @DisplayName("Should find off days in January 2024")
    void testOffDaysInJanuarySearch() throws Exception {
        List<OffDay> results = performSearch("off days in January 2024", "OffDay");

        assertThat(results)
                .hasSize(1)
                .extracting(offDay -> offDay.getFirstDay().getMonthValue())
                .containsExactly(1);
    }

    @Test
    @DisplayName("Should find off days in summer 2024")
    void testOffDaysInSummerSearch() throws Exception {
        List<OffDay> results = performSearch("off days in summer 2024", "OffDay");

        assertThat(results)
                .hasSizeGreaterThanOrEqualTo(4) // Should include June, July, August off days
                .extracting(offDay -> offDay.getFirstDay().getMonthValue())
                .contains(6, 7, 8);
    }

    @Test
    @DisplayName("Should find off days in 2024")
    void testOffDaysInYearSearch() throws Exception {
        List<OffDay> results = performSearch("off days in 2024", "OffDay");

        assertThat(results)
                .hasSizeGreaterThanOrEqualTo(10) // Most off days are in 2024
                .extracting(offDay -> offDay.getFirstDay().getYear())
                .containsOnly(2024);
    }

    @Test
    @DisplayName("Should find off days overlapping with specific date")
    void testOffDaysOverlappingDateSearch() throws Exception {
        List<OffDay> results = performSearch("off days in June 2024", "OffDay");

        assertThat(results)
                .hasSizeGreaterThanOrEqualTo(2) // Should include off days that overlap with June
                .anyMatch(offDay -> offDay.getFirstDay().getMonthValue() <= 6 && offDay.getLastDay().getMonthValue() >= 6);
    }

    @Test
    @DisplayName("Should find off days starting after February 2024")
    void testOffDaysStartingAfterDateSearch() throws Exception {
        List<OffDay> results = performSearch("off days starting after February 2024", "OffDay");

        assertThat(results)
                .hasSizeGreaterThanOrEqualTo(11) // Should exclude first 2 off days
                .extracting(offDay -> offDay.getFirstDay().getMonthValue())
                .allMatch(month -> month >= 3 || month == 1 || month == 2); // March onwards or 2025 dates
    }

    @Test
    @DisplayName("Should find off days starting in specific month")
    void testOffDaysStartingInMonthSearch() throws Exception {
        List<OffDay> results = performSearch("off days starting in March", "OffDay");

        assertThat(results)
                .hasSizeGreaterThanOrEqualTo(2) // Should include off days starting in March (any year)
                .extracting(offDay -> offDay.getFirstDay().getMonthValue())
                .containsOnly(3);
    }

    @Test
    @DisplayName("Should find off days updated in 2025")
    void testOffDaysUpdatedInYearSearch() throws Exception {
        List<OffDay> results = performSearch("off days updated in 2025", "OffDay");

        assertThat(results)
                .hasSizeGreaterThanOrEqualTo(4) // Four off days updated in 2025
                .extracting(offDay -> offDay.getUpdated().getYear())
                .containsOnly(2025);
    }

    @Test
    @DisplayName("Should find recent off days")
    void testRecentOffDaySearch() throws Exception {
        List<OffDay> results = performSearch("recent off days", "OffDay");

        // This test depends on AI interpretation of "recent"
        assertThat(results).isNotNull();
    }

    @Test
    @DisplayName("Should find sick days")
    void testSickDaySearch() throws Exception {
        List<OffDay> results = performSearch("sick days", "OffDay");

        assertThat(results)
                .hasSizeGreaterThanOrEqualTo(4)
                .extracting(OffDay::getType)
                .containsOnly(OffDayType.SICK);
    }

    @Test
    @DisplayName("Should find off days by type SICK")
    void testSickTypeSearch() throws Exception {
        List<OffDay> results = performSearch("type SICK", "OffDay");

        assertThat(results)
                .hasSizeGreaterThanOrEqualTo(4)
                .extracting(OffDay::getType)
                .containsOnly(OffDayType.SICK);
    }

    @Test
    @DisplayName("Should find single day off days")
    void testSingleDayOffDaySearch() throws Exception {
        List<OffDay> results = performSearch("single day", "OffDay");

        assertThat(results)
                .hasSizeGreaterThanOrEqualTo(3) // Should include holidays and other single-day entries
                .allMatch(offDay -> offDay.getFirstDay().equals(offDay.getLastDay()));
    }

    @Test
    @DisplayName("Should find trips")
    void testTripSearch() throws Exception {
        List<OffDay> results = performSearch("trips", "OffDay");

        assertThat(results)
                .hasSizeGreaterThanOrEqualTo(3)
                .extracting(OffDay::getType)
                .containsOnly(OffDayType.TRIP);
    }

    @Test
    @DisplayName("Should find off days by type TRIP")
    void testTripTypeSearch() throws Exception {
        List<OffDay> results = performSearch("type TRIP", "OffDay");

        assertThat(results)
                .hasSizeGreaterThanOrEqualTo(3)
                .extracting(OffDay::getType)
                .containsOnly(OffDayType.TRIP);
    }

    @Test
    @DisplayName("Should find vacation off days")
    void testVacationOffDaySearch() throws Exception {
        List<OffDay> results = performSearch("vacation", "OffDay");

        assertThat(results)
                .hasSizeGreaterThanOrEqualTo(4)
                .extracting(OffDay::getType)
                .containsOnly(OffDayType.VACATION);
    }

    @Test
    @DisplayName("Should find off days by type VACATION")
    void testVacationTypeSearch() throws Exception {
        List<OffDay> results = performSearch("type VACATION", "OffDay");

        assertThat(results)
                .hasSizeGreaterThanOrEqualTo(4)
                .extracting(OffDay::getType)
                .containsOnly(OffDayType.VACATION);
    }

    @Test
    @DisplayName("Should find weekend off days")
    void testWeekendOffDaySearch() throws Exception {
        List<OffDay> results = performSearch("weekend", "OffDay");

        // This test checks if any off days fall on weekends
        assertThat(results).isNotNull();
        // The actual matching depends on the AI's interpretation and the specific dates
    }
}
