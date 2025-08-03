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
import de.bushnaq.abdalla.projecthub.dto.Sprint;
import de.bushnaq.abdalla.projecthub.dto.Status;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestConstructor;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration test for Sprint AI filtering testing real LLM regex pattern generation
 * and filtering capabilities with various search scenarios.
 * <p>
 * This test requires Ollama to be running with a model available (e.g., llama3.2:1b).
 * The test will be skipped if Ollama is not available.
 */
@SpringBootTest
@ActiveProfiles("test")
@TestConstructor(autowireMode = TestConstructor.AutowireMode.ALL)
class SprintAiFilterTest extends AbstractAiFilterTest<Sprint> {

    public SprintAiFilterTest(ObjectMapper mapper, AiFilter aiFilter) {
        super(mapper, aiFilter);
    }

    private Sprint createSprint(Long id, String name, Status status, Long featureId, Long userId,
                                LocalDateTime start, LocalDateTime end, LocalDateTime releaseDate,
                                Duration originalEstimation, Duration worked, Duration remaining,
                                OffsetDateTime created, OffsetDateTime updated) {
        Sprint sprint = new Sprint();
        sprint.setId(id);
        sprint.setName(name);
        sprint.setStatus(status);
        sprint.setFeatureId(featureId);
        sprint.setUserId(userId);
        sprint.setStart(start);
        sprint.setEnd(end);
        sprint.setReleaseDate(releaseDate);
        sprint.setOriginalEstimation(originalEstimation);
        sprint.setWorked(worked);
        sprint.setRemaining(remaining);
        sprint.setCreated(created);
        sprint.setUpdated(updated);
        return sprint;
    }

    @BeforeEach
    void setUp() {
        // Create test data
        setupTestSprints();
    }

    private void setupTestSprints() {
        testProducts = new ArrayList<>();

        // Sprints with different patterns, statuses, and time estimates for comprehensive testing
        testProducts.add(createSprint(1L, "Sprint 1.0.0-Alpha", Status.CREATED, 1L, 1L,
                LocalDateTime.of(2024, 1, 15, 9, 0),
                LocalDateTime.of(2024, 1, 29, 17, 0),
                LocalDateTime.of(2024, 1, 29, 17, 0),
                Duration.ofHours(80), Duration.ofHours(0), Duration.ofHours(80),
                OffsetDateTime.of(2023, 12, 20, 10, 0, 0, 0, ZoneOffset.UTC),
                OffsetDateTime.of(2024, 1, 10, 14, 30, 0, 0, ZoneOffset.UTC)));

        testProducts.add(createSprint(2L, "Sprint 1.2.3-Beta", Status.STARTED, 1L, 2L,
                LocalDateTime.of(2024, 2, 1, 9, 0),
                LocalDateTime.of(2024, 2, 14, 17, 0),
                LocalDateTime.of(2024, 2, 14, 17, 0),
                Duration.ofHours(120), Duration.ofHours(60), Duration.ofHours(60),
                OffsetDateTime.of(2024, 1, 15, 11, 15, 0, 0, ZoneOffset.UTC),
                OffsetDateTime.of(2024, 2, 5, 16, 45, 0, 0, ZoneOffset.UTC)));

        testProducts.add(createSprint(3L, "Sprint 2.0.0-RC1", Status.CLOSED, 2L, 1L,
                LocalDateTime.of(2024, 3, 1, 9, 0),
                LocalDateTime.of(2024, 3, 15, 17, 0),
                LocalDateTime.of(2024, 3, 15, 17, 0),
                Duration.ofHours(100), Duration.ofHours(100), Duration.ofHours(0),
                OffsetDateTime.of(2024, 2, 20, 8, 30, 0, 0, ZoneOffset.UTC),
                OffsetDateTime.of(2024, 3, 16, 13, 20, 0, 0, ZoneOffset.UTC)));

        testProducts.add(createSprint(4L, "Authentication Sprint", Status.STARTED, 3L, 3L,
                LocalDateTime.of(2024, 4, 1, 9, 0),
                LocalDateTime.of(2024, 4, 30, 17, 0),
                LocalDateTime.of(2024, 4, 30, 17, 0),
                Duration.ofHours(160), Duration.ofHours(80), Duration.ofHours(80),
                OffsetDateTime.of(2024, 3, 25, 15, 45, 0, 0, ZoneOffset.UTC),
                OffsetDateTime.of(2024, 4, 15, 12, 10, 0, 0, ZoneOffset.UTC)));

        testProducts.add(createSprint(5L, "Payment Integration Sprint", Status.CREATED, 4L, 2L,
                LocalDateTime.of(2024, 5, 1, 9, 0),
                LocalDateTime.of(2024, 5, 21, 17, 0),
                LocalDateTime.of(2024, 5, 21, 17, 0),
                Duration.ofHours(140), Duration.ofHours(0), Duration.ofHours(140),
                OffsetDateTime.of(2024, 4, 20, 14, 0, 0, 0, ZoneOffset.UTC),
                OffsetDateTime.of(2024, 5, 1, 11, 40, 0, 0, ZoneOffset.UTC)));

        testProducts.add(createSprint(6L, "Dashboard Development", Status.STARTED, 5L, 4L,
                LocalDateTime.of(2024, 6, 3, 9, 0),
                LocalDateTime.of(2024, 6, 24, 17, 0),
                LocalDateTime.of(2024, 6, 24, 17, 0),
                Duration.ofHours(180), Duration.ofHours(90), Duration.ofHours(90),
                OffsetDateTime.of(2024, 5, 28, 9, 20, 0, 0, ZoneOffset.UTC),
                OffsetDateTime.of(2024, 6, 10, 16, 15, 0, 0, ZoneOffset.UTC)));

        testProducts.add(createSprint(7L, "Mobile App Sprint", Status.CLOSED, 6L, 1L,
                LocalDateTime.of(2024, 7, 1, 9, 0),
                LocalDateTime.of(2024, 7, 28, 17, 0),
                LocalDateTime.of(2024, 7, 28, 17, 0),
                Duration.ofHours(200), Duration.ofHours(200), Duration.ofHours(0),
                OffsetDateTime.of(2024, 6, 25, 12, 30, 0, 0, ZoneOffset.UTC),
                OffsetDateTime.of(2024, 7, 29, 15, 50, 0, 0, ZoneOffset.UTC)));

        testProducts.add(createSprint(8L, "Security Enhancement", Status.STARTED, 7L, 3L,
                LocalDateTime.of(2024, 8, 5, 9, 0),
                LocalDateTime.of(2024, 8, 26, 17, 0),
                LocalDateTime.of(2024, 8, 26, 17, 0),
                Duration.ofHours(150), Duration.ofHours(50), Duration.ofHours(100),
                OffsetDateTime.of(2024, 7, 30, 8, 15, 0, 0, ZoneOffset.UTC),
                OffsetDateTime.of(2024, 8, 15, 17, 30, 0, 0, ZoneOffset.UTC)));

        testProducts.add(createSprint(9L, "API Documentation Sprint", Status.CREATED, 8L, 4L,
                LocalDateTime.of(2024, 9, 2, 9, 0),
                LocalDateTime.of(2024, 9, 16, 17, 0),
                LocalDateTime.of(2024, 9, 16, 17, 0),
                Duration.ofHours(80), Duration.ofHours(0), Duration.ofHours(80),
                OffsetDateTime.of(2024, 8, 28, 13, 45, 0, 0, ZoneOffset.UTC),
                OffsetDateTime.of(2024, 9, 5, 9, 20, 0, 0, ZoneOffset.UTC)));

        testProducts.add(createSprint(10L, "Performance Optimization", Status.CLOSED, 9L, 2L,
                LocalDateTime.of(2024, 10, 1, 9, 0),
                LocalDateTime.of(2024, 10, 21, 17, 0),
                LocalDateTime.of(2024, 10, 21, 17, 0),
                Duration.ofHours(120), Duration.ofHours(120), Duration.ofHours(0),
                OffsetDateTime.of(2024, 9, 25, 10, 30, 0, 0, ZoneOffset.UTC),
                OffsetDateTime.of(2024, 10, 22, 14, 45, 0, 0, ZoneOffset.UTC)));

        testProducts.add(createSprint(11L, "Sprint 3.0.0-SNAPSHOT", Status.CREATED, 10L, 1L,
                LocalDateTime.of(2025, 1, 6, 9, 0),
                LocalDateTime.of(2025, 1, 27, 17, 0),
                LocalDateTime.of(2025, 1, 27, 17, 0),
                Duration.ofHours(160), Duration.ofHours(0), Duration.ofHours(160),
                OffsetDateTime.of(2024, 12, 28, 9, 0, 0, 0, ZoneOffset.UTC),
                OffsetDateTime.of(2025, 1, 10, 16, 30, 0, 0, ZoneOffset.UTC)));

        testProducts.add(createSprint(12L, "Bug Fix Sprint", Status.STARTED, 11L, 5L,
                LocalDateTime.of(2025, 2, 3, 9, 0),
                LocalDateTime.of(2025, 2, 17, 17, 0),
                LocalDateTime.of(2025, 2, 17, 17, 0),
                Duration.ofHours(60), Duration.ofHours(30), Duration.ofHours(30),
                OffsetDateTime.of(2025, 1, 28, 8, 15, 0, 0, ZoneOffset.UTC),
                OffsetDateTime.of(2025, 2, 10, 12, 20, 0, 0, ZoneOffset.UTC)));
    }

    @Test
    @DisplayName("Should find active sprints")
    void testActiveSprintSearch() throws Exception {
        List<Sprint> results = performSearch("active sprints", "Sprint");

        assertThat(results)
                .hasSizeGreaterThanOrEqualTo(4) // STARTED sprints
                .extracting(Sprint::getName)
                .contains("Sprint 1.2.3-Beta", "Authentication Sprint", "Dashboard Development", "Security Enhancement", "Bug Fix Sprint");
    }

    @Test
    @DisplayName("Should find alpha sprints")
    void testAlphaSprintSearch() throws Exception {
        List<Sprint> results = performSearch("alpha", "Sprint");

        assertThat(results)
                .hasSize(1)
                .extracting(Sprint::getName)
                .containsExactly("Sprint 1.0.0-Alpha");
    }

    @Test
    @DisplayName("Should find API sprints")
    void testApiSprintSearch() throws Exception {
        List<Sprint> results = performSearch("API", "Sprint");

        assertThat(results)
                .hasSize(1)
                .extracting(Sprint::getName)
                .containsExactly("API Documentation Sprint");
    }

    @Test
    @DisplayName("Should find authentication sprints")
    void testAuthenticationSprintSearch() throws Exception {
        List<Sprint> results = performSearch("authentication", "Sprint");

        assertThat(results)
                .hasSize(1)
                .extracting(Sprint::getName)
                .containsExactly("Authentication Sprint");
    }

    @Test
    @DisplayName("Should find beta sprints")
    void testBetaSprintSearch() throws Exception {
        List<Sprint> results = performSearch("beta", "Sprint");

        assertThat(results)
                .hasSize(1)
                .extracting(Sprint::getName)
                .containsExactly("Sprint 1.2.3-Beta");
    }

    @Test
    @DisplayName("Should find bug fix sprints")
    void testBugFixSprintSearch() throws Exception {
        List<Sprint> results = performSearch("bug fix", "Sprint");

        assertThat(results)
                .hasSize(1)
                .extracting(Sprint::getName)
                .containsExactly("Bug Fix Sprint");
    }

    @Test
    @DisplayName("Should find completed sprints")
    void testCompletedSprintSearch() throws Exception {
        List<Sprint> results = performSearch("completed sprints", "Sprint");

        assertThat(results)
                .hasSizeGreaterThanOrEqualTo(3) // CLOSED sprints
                .extracting(Sprint::getName)
                .contains("Sprint 2.0.0-RC1", "Mobile App Sprint", "Performance Optimization");
    }

    @Test
    @DisplayName("Should find created sprints")
    void testCreatedSprintSearch() throws Exception {
        List<Sprint> results = performSearch("created sprints", "Sprint");

        assertThat(results)
                .hasSizeGreaterThanOrEqualTo(4) // CREATED status sprints
                .extracting(Sprint::getName)
                .contains("Sprint 1.0.0-Alpha", "Payment Integration Sprint", "API Documentation Sprint", "Sprint 3.0.0-SNAPSHOT");
    }

    @Test
    @DisplayName("Should find dashboard sprints")
    void testDashboardSprintSearch() throws Exception {
        List<Sprint> results = performSearch("dashboard", "Sprint");

        assertThat(results)
                .hasSize(1)
                .extracting(Sprint::getName)
                .containsExactly("Dashboard Development");
    }

    @Test
    @DisplayName("Should find documentation sprints")
    void testDocumentationSprintSearch() throws Exception {
        List<Sprint> results = performSearch("documentation", "Sprint");

        assertThat(results)
                .hasSize(1)
                .extracting(Sprint::getName)
                .containsExactly("API Documentation Sprint");
    }

    @Test
    @DisplayName("Should handle empty search query")
    void testEmptySearchQuery() throws Exception {
        List<Sprint> results = performSearch("", "Sprint");

        assertThat(results).hasSize(12); // All sprints should match empty query
    }

    @Test
    @DisplayName("Should find sprints by exact name")
    void testExactSprintNameSearch() throws Exception {
        List<Sprint> results = performSearch("Authentication Sprint", "Sprint");

        assertThat(results)
                .hasSize(1)
                .extracting(Sprint::getName)
                .containsExactly("Authentication Sprint");
    }

    @Test
    @DisplayName("Should find integration sprints")
    void testIntegrationSprintSearch() throws Exception {
        List<Sprint> results = performSearch("integration", "Sprint");

        assertThat(results)
                .hasSize(1)
                .extracting(Sprint::getName)
                .containsExactly("Payment Integration Sprint");
    }

    @Test
    @DisplayName("Should find mobile sprints")
    void testMobileSprintSearch() throws Exception {
        List<Sprint> results = performSearch("mobile", "Sprint");

        assertThat(results)
                .hasSize(1)
                .extracting(Sprint::getName)
                .containsExactly("Mobile App Sprint");
    }

    @Test
    @DisplayName("Should handle nonsensical search query gracefully")
    void testNonsensicalSearchQuery() throws Exception {
        List<Sprint> results = performSearch("purple elephant dancing", "Sprint");

        // Should either return empty results or fall back to simple text matching
        assertThat(results).isNotNull();
    }

    @Test
    @DisplayName("Should find optimization sprints")
    void testOptimizationSprintSearch() throws Exception {
        List<Sprint> results = performSearch("optimization", "Sprint");

        assertThat(results)
                .hasSize(1)
                .extracting(Sprint::getName)
                .containsExactly("Performance Optimization");
    }

    @Test
    @DisplayName("Should find payment sprints")
    void testPaymentSprintSearch() throws Exception {
        List<Sprint> results = performSearch("payment", "Sprint");

        assertThat(results)
                .hasSize(1)
                .extracting(Sprint::getName)
                .containsExactly("Payment Integration Sprint");
    }

    @Test
    @DisplayName("Should find performance sprints")
    void testPerformanceSprintSearch() throws Exception {
        List<Sprint> results = performSearch("performance", "Sprint");

        assertThat(results)
                .hasSize(1)
                .extracting(Sprint::getName)
                .containsExactly("Performance Optimization");
    }

    @Test
    @DisplayName("Should find release candidate sprints")
    void testReleaseCandidateSprintSearch() throws Exception {
        List<Sprint> results = performSearch("rc", "Sprint");

        assertThat(results)
                .hasSize(1)
                .extracting(Sprint::getName)
                .containsExactly("Sprint 2.0.0-RC1");
    }

    @Test
    @DisplayName("Should find security sprints")
    void testSecuritySprintSearch() throws Exception {
        List<Sprint> results = performSearch("security", "Sprint");

        assertThat(results)
                .hasSize(1)
                .extracting(Sprint::getName)
                .containsExactly("Security Enhancement");
    }

    @Test
    @DisplayName("Should find snapshot sprints")
    void testSnapshotSprintSearch() throws Exception {
        List<Sprint> results = performSearch("snapshot", "Sprint");

        assertThat(results)
                .hasSize(1)
                .extracting(Sprint::getName)
                .containsExactly("Sprint 3.0.0-SNAPSHOT");
    }

    @Test
    @DisplayName("Should find sprints by name containing specific text")
    void testSprintNameContainsSearch() throws Exception {
        List<Sprint> results = performSearch("name contains development", "Sprint");

        assertThat(results)
                .hasSize(1)
                .extracting(Sprint::getName)
                .containsExactly("Dashboard Development");
    }

    @Test
    @DisplayName("Should find sprints between 80 and 150 hours")
    void testSprintsBetweenHoursSearch() throws Exception {
        List<Sprint> results = performSearch("sprints between 80 and 150 hours", "Sprint");

        assertThat(results)
                .hasSizeGreaterThanOrEqualTo(4) // Sprints with estimation between 80-150h
                .extracting(Sprint::getName)
                .contains("Sprint 1.0.0-Alpha", "Payment Integration Sprint", "Performance Optimization",
                        "Security Enhancement", "API Documentation Sprint");
    }

    @Test
    @DisplayName("Should find sprints created in 2025")
    void testSprintsCreatedInYearSearch() throws Exception {
        List<Sprint> results = performSearch("sprints created in 2025", "Sprint");

        assertThat(results)
                .hasSizeGreaterThanOrEqualTo(2) // Should include sprints created in 2025
                .extracting(Sprint::getName)
                .contains("Sprint 3.0.0-SNAPSHOT", "Bug Fix Sprint");
    }

    @Test
    @DisplayName("Should find sprints ending before March 2024")
    void testSprintsEndingBeforeDateSearch() throws Exception {
        List<Sprint> results = performSearch("sprints ending before March 2024", "Sprint");

        assertThat(results)
                .hasSizeGreaterThanOrEqualTo(2) // Should include first 2 sprints
                .extracting(Sprint::getName)
                .contains("Sprint 1.0.0-Alpha", "Sprint 1.2.3-Beta");
    }

    @Test
    @DisplayName("Should find sprints over 100 hours estimation")
    void testSprintsOverHundredHoursSearch() throws Exception {
        List<Sprint> results = performSearch("sprints over 100 hours estimation", "Sprint");

        assertThat(results)
                .hasSizeGreaterThanOrEqualTo(6) // Sprints with originalEstimation > 100h
                .extracting(Sprint::getName)
                .contains("Sprint 1.2.3-Beta", "Authentication Sprint", "Payment Integration Sprint",
                        "Dashboard Development", "Mobile App Sprint", "Security Enhancement", "Sprint 3.0.0-SNAPSHOT");
    }

    @Test
    @DisplayName("Should find sprints starting after February 2024")
    void testSprintsStartingAfterDateSearch() throws Exception {
        List<Sprint> results = performSearch("sprints starting after February 2024", "Sprint");

        assertThat(results)
                .hasSizeGreaterThanOrEqualTo(9) // Should exclude first 2 sprints
                .extracting(Sprint::getName)
                .contains("Sprint 2.0.0-RC1", "Authentication Sprint", "Payment Integration Sprint",
                        "Dashboard Development", "Mobile App Sprint", "Security Enhancement");
    }

    @Test
    @DisplayName("Should find sprints updated in 2025")
    void testSprintsUpdatedInYearSearch() throws Exception {
        List<Sprint> results = performSearch("sprints updated in 2025", "Sprint");

        assertThat(results)
                .hasSizeGreaterThanOrEqualTo(2) // Should include sprints updated in 2025
                .extracting(Sprint::getName)
                .contains("Sprint 3.0.0-SNAPSHOT", "Bug Fix Sprint");
    }

    @Test
    @DisplayName("Should find sprints with remaining work")
    void testSprintsWithRemainingWorkSearch() throws Exception {
        List<Sprint> results = performSearch("sprints with remaining work", "Sprint");

        assertThat(results)
                .hasSizeGreaterThanOrEqualTo(7) // Sprints with remaining > 0
                .extracting(Sprint::getName)
                .contains("Sprint 1.0.0-Alpha", "Sprint 1.2.3-Beta", "Authentication Sprint",
                        "Payment Integration Sprint", "Dashboard Development", "Security Enhancement",
                        "API Documentation Sprint", "Sprint 3.0.0-SNAPSHOT", "Bug Fix Sprint");
    }
}
