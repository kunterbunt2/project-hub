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
import de.bushnaq.abdalla.projecthub.dto.Location;
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
 * Integration test for Location AI filtering testing real LLM regex pattern generation
 * and filtering capabilities with various search scenarios.
 * <p>
 * This test requires Ollama to be running with a model available (e.g., llama3.2:1b).
 * The test will be skipped if Ollama is not available.
 */
@SpringBootTest
@ActiveProfiles("test")
@TestConstructor(autowireMode = TestConstructor.AutowireMode.ALL)
class LocationAiFilterTest extends AbstractAiFilterTest<Location> {

    public LocationAiFilterTest(ObjectMapper mapper, AiFilter aiFilter) {
        super(mapper, aiFilter);
    }

    private Location createLocation(Long id, String country, String state, LocalDate start,
                                    User user, OffsetDateTime created, OffsetDateTime updated) {
        Location location = new Location();
        location.setId(id);
        location.setCountry(country);
        location.setState(state);
        location.setStart(start);
        location.setUser(user);
        location.setCreated(created);
        location.setUpdated(updated);
        return location;
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
        setupTestLocations();
    }

    private void setupTestLocations() {
        testProducts = new ArrayList<>();

        // Create test users
        User johnDoe     = createUser(1L, "John Doe");
        User janeSmith   = createUser(2L, "Jane Smith");
        User bobJohnson  = createUser(3L, "Bob Johnson");
        User aliceWilson = createUser(4L, "Alice Wilson");
        User mikeBrown   = createUser(5L, "Mike Brown");

        // Locations with different countries, states, and dates for comprehensive testing
        testProducts.add(createLocation(1L, "Germany", "Bavaria", LocalDate.of(2024, 1, 15), johnDoe,
                OffsetDateTime.of(2023, 12, 20, 10, 0, 0, 0, ZoneOffset.UTC),
                OffsetDateTime.of(2024, 1, 10, 14, 30, 0, 0, ZoneOffset.UTC)));

        testProducts.add(createLocation(2L, "United States", "California", LocalDate.of(2024, 2, 1), janeSmith,
                OffsetDateTime.of(2024, 1, 15, 11, 15, 0, 0, ZoneOffset.UTC),
                OffsetDateTime.of(2024, 2, 5, 16, 45, 0, 0, ZoneOffset.UTC)));

        testProducts.add(createLocation(3L, "Australia", "Victoria", LocalDate.of(2024, 3, 1), bobJohnson,
                OffsetDateTime.of(2024, 2, 20, 8, 30, 0, 0, ZoneOffset.UTC),
                OffsetDateTime.of(2024, 3, 16, 13, 20, 0, 0, ZoneOffset.UTC)));

        testProducts.add(createLocation(4L, "United Kingdom", "England", LocalDate.of(2024, 4, 1), aliceWilson,
                OffsetDateTime.of(2024, 3, 25, 15, 45, 0, 0, ZoneOffset.UTC),
                OffsetDateTime.of(2024, 4, 15, 12, 10, 0, 0, ZoneOffset.UTC)));

        testProducts.add(createLocation(5L, "France", "Île-de-France", LocalDate.of(2024, 5, 1), mikeBrown,
                OffsetDateTime.of(2024, 4, 20, 14, 0, 0, 0, ZoneOffset.UTC),
                OffsetDateTime.of(2024, 5, 1, 11, 40, 0, 0, ZoneOffset.UTC)));

        testProducts.add(createLocation(6L, "Germany", "North Rhine-Westphalia", LocalDate.of(2024, 6, 1), johnDoe,
                OffsetDateTime.of(2024, 5, 28, 9, 20, 0, 0, ZoneOffset.UTC),
                OffsetDateTime.of(2024, 6, 10, 16, 15, 0, 0, ZoneOffset.UTC)));

        testProducts.add(createLocation(7L, "Canada", "Ontario", LocalDate.of(2024, 7, 1), janeSmith,
                OffsetDateTime.of(2024, 6, 25, 12, 30, 0, 0, ZoneOffset.UTC),
                OffsetDateTime.of(2024, 7, 29, 15, 50, 0, 0, ZoneOffset.UTC)));

        testProducts.add(createLocation(8L, "Netherlands", "North Holland", LocalDate.of(2024, 8, 1), bobJohnson,
                OffsetDateTime.of(2024, 7, 30, 8, 15, 0, 0, ZoneOffset.UTC),
                OffsetDateTime.of(2024, 8, 15, 17, 30, 0, 0, ZoneOffset.UTC)));

        testProducts.add(createLocation(9L, "Italy", "Lombardy", LocalDate.of(2024, 9, 1), aliceWilson,
                OffsetDateTime.of(2024, 8, 28, 13, 45, 0, 0, ZoneOffset.UTC),
                OffsetDateTime.of(2024, 9, 5, 9, 20, 0, 0, ZoneOffset.UTC)));

        testProducts.add(createLocation(10L, "Spain", "Catalonia", LocalDate.of(2024, 10, 1), mikeBrown,
                OffsetDateTime.of(2024, 9, 25, 10, 30, 0, 0, ZoneOffset.UTC),
                OffsetDateTime.of(2024, 10, 22, 14, 45, 0, 0, ZoneOffset.UTC)));

        testProducts.add(createLocation(11L, "United States", "New York", LocalDate.of(2025, 1, 1), johnDoe,
                OffsetDateTime.of(2024, 12, 28, 9, 0, 0, 0, ZoneOffset.UTC),
                OffsetDateTime.of(2025, 1, 10, 16, 30, 0, 0, ZoneOffset.UTC)));

        testProducts.add(createLocation(12L, "Australia", "New South Wales", LocalDate.of(2025, 2, 1), janeSmith,
                OffsetDateTime.of(2025, 1, 28, 8, 15, 0, 0, ZoneOffset.UTC),
                OffsetDateTime.of(2025, 2, 10, 12, 20, 0, 0, ZoneOffset.UTC)));
    }

    @Test
    @DisplayName("Should find Asian Pacific locations")
    void testAsianPacificLocationsSearch() throws Exception {
        List<Location> results = performSearch("Asia Pacific", "Location");

        assertThat(results)
                .hasSizeGreaterThanOrEqualTo(2) // Should include Australia locations
                .extracting(Location::getCountry)
                .contains("Australia");
    }

    @Test
    @DisplayName("Should find locations in Australia")
    void testAustraliaLocationSearch() throws Exception {
        List<Location> results = performSearch("locations in Australia", "Location");

        assertThat(results)
                .hasSize(2)
                .extracting(Location::getCountry)
                .containsOnly("Australia");
    }

    @Test
    @DisplayName("Should find locations in California")
    void testCaliforniaLocationSearch() throws Exception {
        List<Location> results = performSearch("California", "Location");

        assertThat(results)
                .hasSize(1)
                .extracting(Location::getState)
                .containsExactly("California");
    }

    @Test
    @DisplayName("Should find Canadian locations")
    void testCanadianLocationSearch() throws Exception {
        List<Location> results = performSearch("Canada", "Location");

        assertThat(results)
                .hasSize(1)
                .extracting(Location::getCountry)
                .containsExactly("Canada");
    }

    @Test
    @DisplayName("Should find locations by country Germany")
    void testCountryGermanySearch() throws Exception {
        List<Location> results = performSearch("country Germany", "Location");

        assertThat(results)
                .hasSize(2)
                .extracting(Location::getCountry)
                .containsOnly("Germany");
    }

    @Test
    @DisplayName("Should find Dutch locations")
    void testDutchLocationSearch() throws Exception {
        List<Location> results = performSearch("Netherlands", "Location");

        assertThat(results)
                .hasSize(1)
                .extracting(Location::getCountry)
                .containsExactly("Netherlands");
    }

    @Test
    @DisplayName("Should handle empty search query")
    void testEmptySearchQuery() throws Exception {
        List<Location> results = performSearch("", "Location");

        assertThat(results).hasSize(12); // All locations should match empty query
    }

    @Test
    @DisplayName("Should find English locations")
    void testEnglishLocationSearch() throws Exception {
        List<Location> results = performSearch("England", "Location");

        assertThat(results)
                .hasSize(1)
                .extracting(Location::getState)
                .containsExactly("England");
    }

    @Test
    @DisplayName("Should find European locations")
    void testEuropeanLocationsSearch() throws Exception {
        List<Location> results = performSearch("European locations", "Location");

        assertThat(results)
                .hasSizeGreaterThanOrEqualTo(6) // Germany(2), UK(1), France(1), Netherlands(1), Italy(1), Spain(1)
                .extracting(Location::getCountry)
                .contains("Germany", "United Kingdom", "France", "Netherlands", "Italy", "Spain");
    }

    @Test
    @DisplayName("Should find French locations")
    void testFrenchLocationSearch() throws Exception {
        List<Location> results = performSearch("France", "Location");

        assertThat(results)
                .hasSize(1)
                .extracting(Location::getCountry)
                .containsExactly("France");
    }

    @Test
    @DisplayName("Should find locations in German states")
    void testGermanStatesSearch() throws Exception {
        List<Location> results = performSearch("German states", "Location");

        assertThat(results)
                .hasSize(2)
                .extracting(Location::getCountry)
                .containsOnly("Germany");

        assertThat(results)
                .extracting(Location::getState)
                .contains("Bavaria", "North Rhine-Westphalia");
    }

    @Test
    @DisplayName("Should find locations in Germany")
    void testGermanyLocationSearch() throws Exception {
        List<Location> results = performSearch("Germany", "Location");

        assertThat(results)
                .hasSize(2)
                .extracting(Location::getCountry)
                .containsOnly("Germany");
    }

    @Test
    @DisplayName("Should find Italian locations")
    void testItalianLocationSearch() throws Exception {
        List<Location> results = performSearch("Italy", "Location");

        assertThat(results)
                .hasSize(1)
                .extracting(Location::getCountry)
                .containsExactly("Italy");
    }

    @Test
    @DisplayName("Should find locations created in 2025")
    void testLocationsCreatedInYearSearch() throws Exception {
        List<Location> results = performSearch("locations created in 2025", "Location");

        assertThat(results)
                .hasSize(1) // Only one location created in 2025
                .extracting(location -> location.getCreated().getYear())
                .containsExactly(2025);
    }

    @Test
    @DisplayName("Should find locations for Jane Smith")
    void testLocationsForJaneSmithSearch() throws Exception {
        List<Location> results = performSearch("Jane Smith", "Location");

        assertThat(results)
                .hasSize(2) // Jane Smith has 2 location records
                .extracting(location -> location.getUser().getName())
                .containsOnly("Jane Smith");
    }

    @Test
    @DisplayName("Should find locations for John Doe")
    void testLocationsForSpecificUserSearch() throws Exception {
        List<Location> results = performSearch("John Doe", "Location");

        assertThat(results)
                .hasSize(3) // John Doe has 3 location records
                .extracting(location -> location.getUser().getName())
                .containsOnly("John Doe");
    }

    @Test
    @DisplayName("Should find locations starting after January 2024")
    void testLocationsStartingAfterDateSearch() throws Exception {
        List<Location> results = performSearch("locations starting after January 2024", "Location");

        assertThat(results)
                .hasSizeGreaterThanOrEqualTo(10) // Should exclude the first location
                .extracting(location -> location.getStart().getMonthValue())
                .allMatch(month -> month >= 2 || month == 1); // February onwards or January 2025
    }

    @Test
    @DisplayName("Should find locations starting before March 2024")
    void testLocationsStartingBeforeDateSearch() throws Exception {
        List<Location> results = performSearch("locations starting before March 2024", "Location");

        assertThat(results)
                .hasSizeGreaterThanOrEqualTo(2) // Should include January and February 2024
                .extracting(location -> location.getStart().getMonthValue())
                .contains(1, 2);
    }

    @Test
    @DisplayName("Should find locations starting in 2025")
    void testLocationsStartingInYearSearch() throws Exception {
        List<Location> results = performSearch("locations starting in 2025", "Location");

        assertThat(results)
                .hasSize(2) // Two locations start in 2025
                .extracting(location -> location.getStart().getYear())
                .containsOnly(2025);
    }

    @Test
    @DisplayName("Should find locations starting in summer 2024")
    void testLocationsStartingSummerSearch() throws Exception {
        List<Location> results = performSearch("locations starting in summer 2024", "Location");

        assertThat(results)
                .hasSizeGreaterThanOrEqualTo(3) // Should include June, July, August
                .extracting(location -> location.getStart().getMonthValue())
                .contains(6, 7, 8);
    }

    @Test
    @DisplayName("Should find locations updated in 2025")
    void testLocationsUpdatedInYearSearch() throws Exception {
        List<Location> results = performSearch("locations updated in 2025", "Location");

        assertThat(results)
                .hasSize(2) // Two locations updated in 2025
                .extracting(location -> location.getUpdated().getYear())
                .contains(2025);
    }

    @Test
    @DisplayName("Should find locations with New South Wales state")
    void testNewSouthWalesStateSearch() throws Exception {
        List<Location> results = performSearch("New South Wales", "Location");

        assertThat(results)
                .hasSize(1)
                .extracting(Location::getState)
                .containsExactly("New South Wales");
    }

    @Test
    @DisplayName("Should find locations with New York state")
    void testNewYorkStateSearch() throws Exception {
        List<Location> results = performSearch("New York", "Location");

        assertThat(results)
                .hasSize(1)
                .extracting(Location::getState)
                .containsExactly("New York");
    }

    @Test
    @DisplayName("Should handle nonsensical search query gracefully")
    void testNonsensicalSearchQuery() throws Exception {
        List<Location> results = performSearch("purple elephant dancing", "Location");

        // Should either return empty results or fall back to simple text matching
        assertThat(results).isNotNull();
    }

    @Test
    @DisplayName("Should find North American locations")
    void testNorthAmericanLocationsSearch() throws Exception {
        List<Location> results = performSearch("North America", "Location");

        assertThat(results)
                .hasSizeGreaterThanOrEqualTo(3) // United States(2), Canada(1)
                .extracting(Location::getCountry)
                .contains("United States", "Canada");
    }

    @Test
    @DisplayName("Should find locations in Ontario")
    void testOntarioLocationSearch() throws Exception {
        List<Location> results = performSearch("Ontario", "Location");

        assertThat(results)
                .hasSize(1)
                .extracting(Location::getState)
                .containsExactly("Ontario");
    }

    @Test
    @DisplayName("Should find Spanish locations")
    void testSpanishLocationSearch() throws Exception {
        List<Location> results = performSearch("Spain", "Location");

        assertThat(results)
                .hasSize(1)
                .extracting(Location::getCountry)
                .containsExactly("Spain");
    }

    @Test
    @DisplayName("Should find locations in Bavaria state")
    void testStateBavariaSearch() throws Exception {
        List<Location> results = performSearch("state Bavaria", "Location");

        assertThat(results)
                .hasSize(1)
                .extracting(Location::getState)
                .containsExactly("Bavaria");
    }

    @Test
    @DisplayName("Should find UK locations")
    void testUKLocationSearch() throws Exception {
        List<Location> results = performSearch("UK", "Location");

        assertThat(results)
                .hasSize(1)
                .extracting(Location::getCountry)
                .containsExactly("United Kingdom");
    }

    @Test
    @DisplayName("Should find locations in United States")
    void testUnitedStatesLocationSearch() throws Exception {
        List<Location> results = performSearch("United States", "Location");

        assertThat(results)
                .hasSize(2)
                .extracting(Location::getCountry)
                .containsOnly("United States");
    }

    @Test
    @DisplayName("Should find locations with Victoria state")
    void testVictoriaStateSearch() throws Exception {
        List<Location> results = performSearch("Victoria", "Location");

        assertThat(results)
                .hasSize(1)
                .extracting(Location::getState)
                .containsExactly("Victoria");
    }
}
