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
import de.bushnaq.abdalla.projecthub.dto.Version;
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
 * Integration test for Version AI filtering testing real LLM regex pattern generation
 * and filtering capabilities with various search scenarios.
 * <p>
 * This test requires Ollama to be running with a model available (e.g., llama3.2:1b).
 * The test will be skipped if Ollama is not available.
 */
@SpringBootTest
@ActiveProfiles("test")
@TestConstructor(autowireMode = TestConstructor.AutowireMode.ALL)
class VersionAiFilterTest extends AbstractAiFilterTest<Version> {

    public VersionAiFilterTest(ObjectMapper mapper, AiFilter aiFilter) {
        super(mapper, aiFilter);
    }

    private Version createVersion(Long id, String name, Long productId, OffsetDateTime created, OffsetDateTime updated) {
        Version version = new Version();
        version.setId(id);
        version.setName(name);
        version.setProductId(productId);
        version.setCreated(created);
        version.setUpdated(updated);
        return version;
    }

    @BeforeEach
    void setUp() {
        // Create test data
        setupTestVersions();
    }

    private void setupTestVersions() {
        testProducts = new ArrayList<>();

        // Versions with different semantic version patterns and dates for comprehensive testing
        testProducts.add(createVersion(1L, "1.0.0",
                1L,
                OffsetDateTime.of(2023, 6, 15, 10, 0, 0, 0, ZoneOffset.UTC),
                OffsetDateTime.of(2024, 3, 20, 14, 30, 0, 0, ZoneOffset.UTC)));

        testProducts.add(createVersion(2L, "1.2.3",
                1L,
                OffsetDateTime.of(2024, 1, 10, 9, 0, 0, 0, ZoneOffset.UTC),
                OffsetDateTime.of(2024, 6, 5, 16, 45, 0, 0, ZoneOffset.UTC)));

        testProducts.add(createVersion(3L, "2.0.0",
                2L,
                OffsetDateTime.of(2024, 2, 28, 11, 15, 0, 0, ZoneOffset.UTC),
                OffsetDateTime.of(2024, 8, 12, 13, 20, 0, 0, ZoneOffset.UTC)));

        testProducts.add(createVersion(4L, "2.1.5",
                2L,
                OffsetDateTime.of(2024, 4, 3, 8, 30, 0, 0, ZoneOffset.UTC),
                OffsetDateTime.of(2024, 9, 18, 12, 10, 0, 0, ZoneOffset.UTC)));

        testProducts.add(createVersion(5L, "3.0.0-beta",
                3L,
                OffsetDateTime.of(2024, 7, 22, 15, 45, 0, 0, ZoneOffset.UTC),
                OffsetDateTime.of(2024, 12, 1, 10, 25, 0, 0, ZoneOffset.UTC)));

        testProducts.add(createVersion(6L, "3.1.0-alpha",
                3L,
                OffsetDateTime.of(2024, 9, 5, 14, 0, 0, 0, ZoneOffset.UTC),
                OffsetDateTime.of(2025, 1, 15, 11, 40, 0, 0, ZoneOffset.UTC)));

        testProducts.add(createVersion(7L, "4.0.0-rc1",
                4L,
                OffsetDateTime.of(2024, 11, 12, 9, 20, 0, 0, ZoneOffset.UTC),
                OffsetDateTime.of(2025, 2, 8, 16, 15, 0, 0, ZoneOffset.UTC)));

        testProducts.add(createVersion(8L, "0.9.0",
                4L,
                OffsetDateTime.of(2025, 1, 5, 12, 30, 0, 0, ZoneOffset.UTC),
                OffsetDateTime.of(2025, 1, 20, 15, 50, 0, 0, ZoneOffset.UTC)));

        testProducts.add(createVersion(9L, "1.0.0-SNAPSHOT",
                5L,
                OffsetDateTime.of(2025, 2, 10, 8, 15, 0, 0, ZoneOffset.UTC),
                OffsetDateTime.of(2025, 2, 25, 17, 30, 0, 0, ZoneOffset.UTC)));

        testProducts.add(createVersion(10L, "5.2.1",
                5L,
                OffsetDateTime.of(2025, 3, 18, 13, 45, 0, 0, ZoneOffset.UTC),
                OffsetDateTime.of(2025, 4, 2, 9, 20, 0, 0, ZoneOffset.UTC)));
    }

    @Test
    @DisplayName("Should find alpha versions")
    void testAlphaVersionSearch() throws Exception {
        List<Version> results = performSearch("alpha", "Version");

        assertThat(results)
                .hasSize(1)
                .extracting(Version::getName)
                .containsExactly("3.1.0-alpha");
    }

    @Test
    @DisplayName("Should find beta versions")
    void testBetaVersionSearch() throws Exception {
        List<Version> results = performSearch("beta", "Version");

        assertThat(results)
                .hasSize(1)
                .extracting(Version::getName)
                .containsExactly("3.0.0-beta");
    }

    @Test
    @DisplayName("Should handle empty search query")
    void testEmptySearchQuery() throws Exception {
        List<Version> results = performSearch("", "Version");

        assertThat(results).hasSize(10); // All versions should match empty query
    }

    @Test
    @DisplayName("Should find versions by exact version number")
    void testExactVersionSearch() throws Exception {
        List<Version> results = performSearch("1.2.3", "Version");

        assertThat(results)
                .hasSize(1)
                .extracting(Version::getName)
                .containsExactly("1.2.3");
    }

    @Test
    @DisplayName("Should find major version 3 releases")
    void testMajorVersion3Search() throws Exception {
        List<Version> results = performSearch("version 3", "Version");

        assertThat(results)
                .hasSize(2)
                .extracting(Version::getName)
                .contains("3.0.0-beta", "3.1.0-alpha");
    }

    @Test
    @DisplayName("Should find major version 1.x.x")
    void testMajorVersionSearch() throws Exception {
        List<Version> results = performSearch("version 1", "Version");

        assertThat(results)
                .hasSizeGreaterThanOrEqualTo(3) // 1.0.0, 1.2.3, 1.0.0-SNAPSHOT
                .extracting(Version::getName)
                .contains("1.0.0", "1.2.3", "1.0.0-SNAPSHOT");
    }

    @Test
    @DisplayName("Should handle nonsensical search query gracefully")
    void testNonsensicalSearchQuery() throws Exception {
        List<Version> results = performSearch("purple elephant dancing", "Version");

        // Should either return empty results or fall back to simple text matching
        assertThat(results).isNotNull();
    }

    @Test
    @DisplayName("Should find release candidate versions")
    void testReleaseCandidateVersionSearch() throws Exception {
        List<Version> results = performSearch("rc", "Version");

        assertThat(results)
                .hasSize(1)
                .extracting(Version::getName)
                .containsExactly("4.0.0-rc1");
    }

    @Test
    @DisplayName("Should find versions with semantic version pattern")
    void testSemanticVersionPatternSearch() throws Exception {
        List<Version> results = performSearch("2.0.0", "Version");

        assertThat(results)
                .hasSize(1)
                .extracting(Version::getName)
                .containsExactly("2.0.0");
    }

    @Test
    @DisplayName("Should find snapshot versions")
    void testSnapshotVersionSearch() throws Exception {
        List<Version> results = performSearch("snapshot", "Version");

        assertThat(results)
                .hasSize(1)
                .extracting(Version::getName)
                .containsExactly("1.0.0-SNAPSHOT");
    }

    @Test
    @DisplayName("Should find stable versions (without pre-release suffixes)")
    void testStableVersionSearch() throws Exception {
        List<Version> results = performSearch("stable versions", "Version");

        // This should find versions without beta, alpha, rc, or SNAPSHOT suffixes
        assertThat(results)
                .hasSizeGreaterThanOrEqualTo(5)
                .extracting(Version::getName)
                .contains("1.0.0", "1.2.3", "2.0.0", "2.1.5", "0.9.0", "5.2.1");
    }

    @Test
    @DisplayName("Should find versions between 1.0.0 and 3.0.0")
    void testVersionBetweenSearch() throws Exception {
        List<Version> results = performSearch("versions between 1.0.0 and 3.0.0", "Version");

        assertThat(results)
                .hasSizeGreaterThanOrEqualTo(3) // Should include 1.x.x and 2.x.x versions
                .extracting(Version::getName)
                .contains("1.2.3", "2.0.0", "2.1.5");
    }

    @Test
    @DisplayName("Should find versions greater than 1.0.0")
    void testVersionGreaterThanSearch() throws Exception {
        List<Version> results = performSearch("version greater than 1.0.0", "Version");

        assertThat(results)
                .hasSizeGreaterThanOrEqualTo(4) // Should include 1.2.3, 2.0.0, 2.1.5, 3.0.0-beta, 3.1.0-alpha, 4.0.0-rc1, 5.2.1
                .extracting(Version::getName)
                .contains("1.2.3", "2.0.0", "2.1.5", "5.2.1");
    }

    @Test
    @DisplayName("Should find versions less than 2.0.0")
    void testVersionLessThanSearch() throws Exception {
        List<Version> results = performSearch("version less than 2.0.0", "Version");

        assertThat(results)
                .hasSizeGreaterThanOrEqualTo(3) // Should include 1.0.0, 1.2.3, 0.9.0, 1.0.0-SNAPSHOT
                .extracting(Version::getName)
                .contains("1.0.0", "1.2.3", "0.9.0", "1.0.0-SNAPSHOT");
    }

    @Test
    @DisplayName("Should find versions by name containing specific text")
    void testVersionNameContainsSearch() throws Exception {
        List<Version> results = performSearch("name contains beta", "Version");

        assertThat(results)
                .hasSize(1)
                .extracting(Version::getName)
                .containsExactly("3.0.0-beta");
    }

    @Test
    @DisplayName("Should find versions created after January 2024")
    void testVersionsCreatedAfterDateSearch() throws Exception {
        List<Version> results = performSearch("versions created after January 2024", "Version");

        assertThat(results)
                .hasSizeGreaterThanOrEqualTo(8) // Should exclude version 1 created in 2023
                .extracting(Version::getName)
                .contains("1.2.3", "2.0.0", "2.1.5", "3.0.0-beta", "3.1.0-alpha", "4.0.0-rc1", "0.9.0", "1.0.0-SNAPSHOT", "5.2.1");
    }

    @Test
    @DisplayName("Should find versions created before March 2024")
    void testVersionsCreatedBeforeDateSearch() throws Exception {
        List<Version> results = performSearch("versions created before March 2024", "Version");

        assertThat(results)
                .hasSizeGreaterThanOrEqualTo(3) // Should include versions created in 2023 and January-February 2024
                .extracting(Version::getName)
                .contains("1.0.0", "1.2.3", "2.0.0");
    }

    @Test
    @DisplayName("Should find versions updated in 2025")
    void testVersionsUpdatedInYearSearch() throws Exception {
        List<Version> results = performSearch("versions updated in 2025", "Version");

        assertThat(results)
                .hasSizeGreaterThanOrEqualTo(5) // Should include versions updated in 2025
                .extracting(Version::getName)
                .contains("3.1.0-alpha", "4.0.0-rc1", "0.9.0", "1.0.0-SNAPSHOT", "5.2.1");
    }
}
