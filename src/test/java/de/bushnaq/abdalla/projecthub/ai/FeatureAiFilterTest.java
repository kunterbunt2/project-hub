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
import de.bushnaq.abdalla.projecthub.dto.Feature;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestConstructor;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration test for Feature AI filtering testing real LLM regex pattern generation
 * and filtering capabilities with various search scenarios.
 * <p>
 * This test requires Ollama to be running with a model available (e.g., llama3.2:1b).
 * The test will be skipped if Ollama is not available.
 */
@SpringBootTest
@ActiveProfiles("test")
@TestConstructor(autowireMode = TestConstructor.AutowireMode.ALL)
class FeatureAiFilterTest extends AbstractAiFilterTest<Feature> {

    public FeatureAiFilterTest(ObjectMapper mapper, AiFilter aiFilter) {
        super(mapper, aiFilter);
    }

    private Feature createFeature(Long id, String name, Long versionId, OffsetDateTime created, OffsetDateTime updated) {
        Feature feature = new Feature();
        feature.setId(id);
        feature.setName(name);
        feature.setVersionId(versionId);
        feature.setCreated(created);
        feature.setUpdated(updated);
        return feature;
    }

    @BeforeEach
    void setUp() {
        // Create test data
        setupTestFeatures();
    }

    private void setupTestFeatures() {
        testProducts = new ArrayList<>();

        // Features with different functional domains and naming patterns for comprehensive testing
        testProducts.add(createFeature(1L, "User Authentication",
                1L,
                OffsetDateTime.of(2023, 6, 15, 10, 0, 0, 0, ZoneOffset.UTC),
                OffsetDateTime.of(2024, 3, 20, 14, 30, 0, 0, ZoneOffset.UTC)));

        testProducts.add(createFeature(2L, "Payment Processing",
                1L,
                OffsetDateTime.of(2024, 1, 10, 9, 0, 0, 0, ZoneOffset.UTC),
                OffsetDateTime.of(2024, 6, 5, 16, 45, 0, 0, ZoneOffset.UTC)));

        testProducts.add(createFeature(3L, "User Profile Management",
                2L,
                OffsetDateTime.of(2024, 2, 28, 11, 15, 0, 0, ZoneOffset.UTC),
                OffsetDateTime.of(2024, 8, 12, 13, 20, 0, 0, ZoneOffset.UTC)));

        testProducts.add(createFeature(4L, "Shopping Cart",
                2L,
                OffsetDateTime.of(2024, 4, 3, 8, 30, 0, 0, ZoneOffset.UTC),
                OffsetDateTime.of(2024, 9, 18, 12, 10, 0, 0, ZoneOffset.UTC)));

        testProducts.add(createFeature(5L, "Email Notifications",
                3L,
                OffsetDateTime.of(2024, 7, 22, 15, 45, 0, 0, ZoneOffset.UTC),
                OffsetDateTime.of(2024, 12, 1, 10, 25, 0, 0, ZoneOffset.UTC)));

        testProducts.add(createFeature(6L, "Data Analytics Dashboard",
                3L,
                OffsetDateTime.of(2024, 9, 5, 14, 0, 0, 0, ZoneOffset.UTC),
                OffsetDateTime.of(2025, 1, 15, 11, 40, 0, 0, ZoneOffset.UTC)));

        testProducts.add(createFeature(7L, "API Security Enhancement",
                4L,
                OffsetDateTime.of(2024, 11, 12, 9, 20, 0, 0, ZoneOffset.UTC),
                OffsetDateTime.of(2025, 2, 8, 16, 15, 0, 0, ZoneOffset.UTC)));

        testProducts.add(createFeature(8L, "Mobile App Integration",
                4L,
                OffsetDateTime.of(2025, 1, 5, 12, 30, 0, 0, ZoneOffset.UTC),
                OffsetDateTime.of(2025, 1, 20, 15, 50, 0, 0, ZoneOffset.UTC)));

        testProducts.add(createFeature(9L, "Search Functionality",
                5L,
                OffsetDateTime.of(2025, 2, 10, 8, 15, 0, 0, ZoneOffset.UTC),
                OffsetDateTime.of(2025, 2, 25, 17, 30, 0, 0, ZoneOffset.UTC)));

        testProducts.add(createFeature(10L, "Reporting System",
                5L,
                OffsetDateTime.of(2025, 3, 18, 13, 45, 0, 0, ZoneOffset.UTC),
                OffsetDateTime.of(2025, 4, 2, 9, 20, 0, 0, ZoneOffset.UTC)));

        testProducts.add(createFeature(11L, "Social Media Integration",
                6L,
                OffsetDateTime.of(2025, 5, 10, 10, 30, 0, 0, ZoneOffset.UTC),
                OffsetDateTime.of(2025, 6, 15, 14, 45, 0, 0, ZoneOffset.UTC)));

        testProducts.add(createFeature(12L, "Machine Learning Recommendations",
                6L,
                OffsetDateTime.of(2025, 7, 1, 9, 0, 0, 0, ZoneOffset.UTC),
                OffsetDateTime.of(2025, 8, 1, 16, 30, 0, 0, ZoneOffset.UTC)));
    }

    @Test
    @DisplayName("Should find analytics related features")
    void testAnalyticsFeatureSearch() throws Exception {
        List<Feature> results = performSearch("analytics", "Feature");

        assertThat(results)
                .hasSize(1)
                .extracting(Feature::getName)
                .containsExactly("Data Analytics Dashboard");
    }

    @Test
    @DisplayName("Should find API related features")
    void testApiFeatureSearch() throws Exception {
        List<Feature> results = performSearch("API", "Feature");

        assertThat(results)
                .hasSize(1)
                .extracting(Feature::getName)
                .containsExactly("API Security Enhancement");
    }

    @Test
    @DisplayName("Should find authentication features")
    void testAuthenticationFeatureSearch() throws Exception {
        List<Feature> results = performSearch("authentication", "Feature");

        assertThat(results)
                .hasSize(1)
                .extracting(Feature::getName)
                .containsExactly("User Authentication");
    }

    @Test
    @DisplayName("Should find cart features")
    void testCartFeatureSearch() throws Exception {
        List<Feature> results = performSearch("cart", "Feature");

        assertThat(results)
                .hasSize(1)
                .extracting(Feature::getName)
                .containsExactly("Shopping Cart");
    }

    @Test
    @DisplayName("Should find dashboard features")
    void testDashboardFeatureSearch() throws Exception {
        List<Feature> results = performSearch("dashboard", "Feature");

        assertThat(results)
                .hasSize(1)
                .extracting(Feature::getName)
                .containsExactly("Data Analytics Dashboard");
    }

    @Test
    @DisplayName("Should handle empty search query")
    void testEmptySearchQuery() throws Exception {
        List<Feature> results = performSearch("", "Feature");

        assertThat(results).hasSize(12); // All features should match empty query
    }

    @Test
    @DisplayName("Should find enhancement features")
    void testEnhancementFeatureSearch() throws Exception {
        List<Feature> results = performSearch("enhancement", "Feature");

        assertThat(results)
                .hasSize(1)
                .extracting(Feature::getName)
                .containsExactly("API Security Enhancement");
    }

    @Test
    @DisplayName("Should find features by exact name")
    void testExactFeatureNameSearch() throws Exception {
        List<Feature> results = performSearch("User Authentication", "Feature");

        assertThat(results)
                .hasSize(1)
                .extracting(Feature::getName)
                .containsExactly("User Authentication");
    }

    @Test
    @DisplayName("Should find features created after January 2024")
    void testFeaturesCreatedAfterDateSearch() throws Exception {
        List<Feature> results = performSearch("features created after January 2024", "Feature");

        assertThat(results)
                .hasSizeGreaterThanOrEqualTo(10) // Should exclude feature 1 created in 2023
                .extracting(Feature::getName)
                .contains("Payment Processing", "User Profile Management", "Shopping Cart",
                        "Email Notifications", "Data Analytics Dashboard", "API Security Enhancement",
                        "Mobile App Integration", "Search Functionality", "Reporting System");
    }

    @Test
    @DisplayName("Should find features created before March 2024")
    void testFeaturesCreatedBeforeDateSearch() throws Exception {
        List<Feature> results = performSearch("features created before March 2024", "Feature");

        assertThat(results)
                .hasSizeGreaterThanOrEqualTo(3) // Should include features created in 2023 and January-February 2024
                .extracting(Feature::getName)
                .contains("User Authentication", "Payment Processing", "User Profile Management");
    }

    @Test
    @DisplayName("Should find features updated in 2025")
    void testFeaturesUpdatedInYearSearch() throws Exception {
        List<Feature> results = performSearch("features updated in 2025", "Feature");

        assertThat(results)
                .hasSizeGreaterThanOrEqualTo(7) // Should include features updated in 2025
                .extracting(Feature::getName)
                .contains("Data Analytics Dashboard", "API Security Enhancement", "Mobile App Integration",
                        "Search Functionality", "Reporting System", "Social Media Integration",
                        "Machine Learning Recommendations");
    }

    @Test
    @DisplayName("Should find integration features")
    void testIntegrationFeatureSearch() throws Exception {
        List<Feature> results = performSearch("integration", "Feature");

        assertThat(results)
                .hasSize(2)
                .extracting(Feature::getName)
                .contains("Mobile App Integration", "Social Media Integration");
    }

    @Test
    @DisplayName("Should find machine learning features")
    void testMachineLearningFeatureSearch() throws Exception {
        List<Feature> results = performSearch("machine learning", "Feature");

        assertThat(results)
                .hasSize(1)
                .extracting(Feature::getName)
                .containsExactly("Machine Learning Recommendations");
    }

    @Test
    @DisplayName("Should find management features")
    void testManagementFeatureSearch() throws Exception {
        List<Feature> results = performSearch("management", "Feature");

        assertThat(results)
                .hasSize(1)
                .extracting(Feature::getName)
                .containsExactly("User Profile Management");
    }

    @Test
    @DisplayName("Should find mobile related features")
    void testMobileFeatureSearch() throws Exception {
        List<Feature> results = performSearch("mobile", "Feature");

        assertThat(results)
                .hasSize(1)
                .extracting(Feature::getName)
                .containsExactly("Mobile App Integration");
    }

    @Test
    @DisplayName("Should find features with 'name contains' query")
    void testNameContainsSearch() throws Exception {
        List<Feature> results = performSearch("name contains dashboard", "Feature");

        assertThat(results)
                .hasSize(1)
                .extracting(Feature::getName)
                .containsExactly("Data Analytics Dashboard");
    }

    @Test
    @DisplayName("Should handle nonsensical search query gracefully")
    void testNonsensicalSearchQuery() throws Exception {
        List<Feature> results = performSearch("purple elephant dancing", "Feature");

        // Should either return empty results or fall back to simple text matching
        assertThat(results).isNotNull();
    }

    @Test
    @DisplayName("Should find notification features")
    void testNotificationFeatureSearch() throws Exception {
        List<Feature> results = performSearch("notifications", "Feature");

        assertThat(results)
                .hasSize(1)
                .extracting(Feature::getName)
                .containsExactly("Email Notifications");
    }

    @Test
    @DisplayName("Should find payment related features")
    void testPaymentFeatureSearch() throws Exception {
        List<Feature> results = performSearch("payment", "Feature");

        assertThat(results)
                .hasSize(1)
                .extracting(Feature::getName)
                .containsExactly("Payment Processing");
    }

    @Test
    @DisplayName("Should find profile features")
    void testProfileFeatureSearch() throws Exception {
        List<Feature> results = performSearch("profile", "Feature");

        assertThat(results)
                .hasSize(1)
                .extracting(Feature::getName)
                .containsExactly("User Profile Management");
    }

    @Test
    @DisplayName("Should find reporting features")
    void testReportingFeatureSearch() throws Exception {
        List<Feature> results = performSearch("reporting", "Feature");

        assertThat(results)
                .hasSize(1)
                .extracting(Feature::getName)
                .containsExactly("Reporting System");
    }

    @Test
    @DisplayName("Should find search functionality features")
    void testSearchFunctionalityFeatureSearch() throws Exception {
        List<Feature> results = performSearch("search functionality", "Feature");

        assertThat(results)
                .hasSize(1)
                .extracting(Feature::getName)
                .containsExactly("Search Functionality");
    }

    @Test
    @DisplayName("Should find security related features")
    void testSecurityFeatureSearch() throws Exception {
        List<Feature> results = performSearch("security", "Feature");

        assertThat(results)
                .hasSize(1)
                .extracting(Feature::getName)
                .containsExactly("API Security Enhancement");
    }

    @Test
    @DisplayName("Should find shopping related features")
    void testShoppingFeatureSearch() throws Exception {
        List<Feature> results = performSearch("shopping", "Feature");

        assertThat(results)
                .hasSize(1)
                .extracting(Feature::getName)
                .containsExactly("Shopping Cart");
    }

    @Test
    @DisplayName("Should find social media features")
    void testSocialMediaFeatureSearch() throws Exception {
        List<Feature> results = performSearch("social media", "Feature");

        assertThat(results)
                .hasSize(1)
                .extracting(Feature::getName)
                .containsExactly("Social Media Integration");
    }

    @Test
    @DisplayName("Should find features containing 'user' keyword")
    void testUserRelatedFeatureSearch() throws Exception {
        List<Feature> results = performSearch("user", "Feature");

        assertThat(results)
                .hasSizeGreaterThanOrEqualTo(2)
                .extracting(Feature::getName)
                .contains("User Authentication", "User Profile Management");
    }
}
