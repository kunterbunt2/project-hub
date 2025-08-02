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

package de.bushnaq.abdalla.projecthub.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import de.bushnaq.abdalla.projecthub.dto.Product;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration test for NaturalLanguageSearchService testing real LLM regex pattern generation
 * and filtering capabilities with various search scenarios.
 * <p>
 * This test requires Ollama to be running with a model available (e.g., llama3.2:1b).
 * The test will be skipped if Ollama is not available.
 */
@SpringBootTest
@ActiveProfiles("test")
class NaturalLanguageSearchServiceTest {

    @Autowired
    private ObjectMapper objectMapper;
    String regexPattern;
    @Autowired
    private NaturalLanguageSearchService searchService;
    private List<Product>                testProducts;

    private Product createProduct(Long id, String name, OffsetDateTime created, OffsetDateTime updated) {
        Product product = new Product();
        product.setId(id);
        product.setName(name);
        product.setCreated(created);
        product.setUpdated(updated);
        return product;
    }

    private @NotNull List<Product> execute(String question) throws Exception {
        String answer = searchService.parseQuery(question);
        regexPattern = answer;

        assertThat(answer).isNotEmpty();

        // Test filtering
        List<Product> filtered = filterProducts(answer);

        System.out.println("\n=== Products matched by regex ===");
        for (Product product : filtered) {
            String json = objectMapper.writeValueAsString(product);
            System.out.println(json);
        }
        return filtered;
    }

    /**
     * Helper method to simulate filtering products using regex patterns
     * (mimics what SmartGlobalFilter does)
     */
    private List<Product> filterProducts(String regexPattern) throws Exception {
        if (regexPattern == null || regexPattern.trim().isEmpty()) {
            return testProducts;
        }

        Pattern pattern;
        try {
            pattern = Pattern.compile(regexPattern);
        } catch (Exception e) {
            // If regex is invalid, fall back to simple contains search
            return testProducts.stream()
                    .filter(product -> {
                        try {
                            String json = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(product);
                            return json.toLowerCase().contains("test");
                        } catch (Exception ex) {
                            return false;
                        }
                    })
                    .collect(Collectors.toList());
        }

        return testProducts.stream()
                .filter(product -> {
                    try {
                        String json = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(product);
                        // Use find() instead of matches() - matches() requires the entire string to match
                        return pattern.matcher(json).find();
                    } catch (Exception e) {
                        return false;
                    }
                })
                .collect(Collectors.toList());
    }

    @BeforeEach
    void setUp() {
        // Create test data
        setupTestProducts();
    }

    private void setupTestProducts() {
        testProducts = new ArrayList<>();

        // Products with different names and dates for comprehensive testing
        testProducts.add(createProduct(1L, "Orion Space System",
                OffsetDateTime.of(2023, 6, 15, 10, 0, 0, 0, ZoneOffset.UTC),
                OffsetDateTime.of(2024, 3, 20, 14, 30, 0, 0, ZoneOffset.UTC)));

        testProducts.add(createProduct(2L, "Project Apollo",
                OffsetDateTime.of(2024, 1, 10, 9, 0, 0, 0, ZoneOffset.UTC),
                OffsetDateTime.of(2024, 6, 5, 16, 45, 0, 0, ZoneOffset.UTC)));

        testProducts.add(createProduct(3L, "Mars Explorer",
                OffsetDateTime.of(2024, 2, 28, 11, 15, 0, 0, ZoneOffset.UTC),
                OffsetDateTime.of(2024, 8, 12, 13, 20, 0, 0, ZoneOffset.UTC)));

        testProducts.add(createProduct(4L, "Satellite Network",
                OffsetDateTime.of(2024, 4, 3, 8, 30, 0, 0, ZoneOffset.UTC),
                OffsetDateTime.of(2024, 9, 18, 12, 10, 0, 0, ZoneOffset.UTC)));

        testProducts.add(createProduct(5L, "Lunar Base",
                OffsetDateTime.of(2024, 7, 22, 15, 45, 0, 0, ZoneOffset.UTC),
                OffsetDateTime.of(2024, 12, 1, 10, 25, 0, 0, ZoneOffset.UTC)));

        testProducts.add(createProduct(6L, "Deep Space Probe",
                OffsetDateTime.of(2024, 9, 5, 14, 0, 0, 0, ZoneOffset.UTC),
                OffsetDateTime.of(2025, 1, 15, 11, 40, 0, 0, ZoneOffset.UTC)));

        testProducts.add(createProduct(7L, "Space Station Alpha",
                OffsetDateTime.of(2024, 11, 12, 9, 20, 0, 0, ZoneOffset.UTC),
                OffsetDateTime.of(2025, 2, 8, 16, 15, 0, 0, ZoneOffset.UTC)));

        testProducts.add(createProduct(8L, "Rocket Engine X",
                OffsetDateTime.of(2025, 1, 5, 12, 30, 0, 0, ZoneOffset.UTC),
                OffsetDateTime.of(2025, 1, 20, 15, 50, 0, 0, ZoneOffset.UTC)));
    }

    //    @Test
//    @DisplayName("Should handle case-insensitive searches")
//    void testCaseInsensitiveSearchWithLLM() throws Exception {
//        // When
//        String regexPattern = searchService.parseQuery("MARS");
//
//        // Then
//        assertThat(regexPattern).isNotEmpty();
//        System.out.println("Case-insensitive search pattern: " + regexPattern);
//
//        // Test filtering
//        List<Product> filtered = filterProducts(regexPattern);
//
//        // Should find Mars Explorer regardless of case
//        List<String> filteredNames = filtered.stream()
//                .map(Product::getName)
//                .collect(Collectors.toList());
//        assertThat(filteredNames).anyMatch(name -> name.toLowerCase().contains("mars"));
//
//        System.out.println("Case-insensitive search results: " + filteredNames);
//    }
//
//    @Test
//    @DisplayName("Should generate working regex for complex search")
//    void testComplexSearchWithLLM() throws Exception {
//        // When
//        String regexPattern = searchService.parseQuery("space products created in 2024");
//
//        // Then
//        assertThat(regexPattern).isNotEmpty();
//        System.out.println("Complex search pattern: " + regexPattern);
//
//        // Test filtering
//        List<Product> filtered = filterProducts(regexPattern);
//
//        // Log all results for analysis
//        System.out.println("Complex search results:");
//        for (Product product : filtered) {
//            boolean hasSpace      = product.getName().toLowerCase().contains("space");
//            boolean createdIn2024 = product.getCreated().getYear() == 2024;
//            System.out.println(product.getName() +
//                    " - has 'space': " + hasSpace +
//                    ", created in 2024: " + createdIn2024 +
//                    ", created: " + product.getCreated());
//        }
//
//        // Should find at least some results
//        assertThat(filtered).isNotEmpty();
//    }
//
//    @Test
//    @DisplayName("Should generate working regex for date-based search")
//    void testDateBasedSearchWithLLM() throws Exception {
//        // When
//        String regexPattern = searchService.parseQuery("products created after July 2024");
//
//        // Then
//        assertThat(regexPattern).isNotEmpty();
//        System.out.println("Date-based search pattern: " + regexPattern);
//
//        // Test filtering
//        List<Product> filtered = filterProducts(regexPattern);
//
//        // Should find products created after July 2024
//        // Expected: Lunar Base (July), Deep Space Probe (Sept), Space Station Alpha (Nov), Rocket Engine X (Jan 2025)
//        assertThat(filtered).hasSizeGreaterThanOrEqualTo(1);
//
//        // Verify the dates make sense
//        for (Product product : filtered) {
//            OffsetDateTime created         = product.getCreated();
//            boolean        isAfterJuly2024 = created.isAfter(OffsetDateTime.of(2024, 7, 1, 0, 0, 0, 0, ZoneOffset.UTC));
//            System.out.println(product.getName() + " created: " + created + " (after July 2024: " + isAfterJuly2024 + ")");
//        }
//    }
//
//    @Test
//    @DisplayName("Should generate working regex for name-specific search")
//    void testNameSpecificSearchWithLLM() throws Exception {
//        // When
//        String regexPattern = searchService.parseQuery("name contains project");
//
//        // Then
//        assertThat(regexPattern).isNotEmpty();
//        System.out.println("Name-specific search pattern: " + regexPattern);
//
//        // Test filtering
//        List<Product> filtered = filterProducts(regexPattern);
//
//        // Should find products with "project" in the name field
//        List<String> filteredNames = filtered.stream()
//                .map(Product::getName)
//                .collect(Collectors.toList());
//        assertThat(filteredNames).anyMatch(name -> name.toLowerCase().contains("project"));
//
//        // Log results for analysis
//        System.out.println("Found products: " + filteredNames);
//    }
//
//    @Test
//    @DisplayName("Should test multiple search patterns for consistency")
//    void testSearchConsistency() throws Exception {
//        String query = "project";
//
//        // Run the same query multiple times
//        List<String> patterns = new ArrayList<>();
//        for (int i = 0; i < 3; i++) {
//            String pattern = searchService.parseQuery(query);
//            patterns.add(pattern);
//            System.out.println("Pattern " + (i + 1) + ": " + pattern);
//        }
//
//        // All patterns should be non-empty
//        assertThat(patterns).allMatch(pattern -> !pattern.isEmpty());
//
//        // Test that all patterns produce similar results
//        List<List<Product>> results = new ArrayList<>();
//        for (String pattern : patterns) {
//            List<Product> filtered = filterProducts(pattern);
//            results.add(filtered);
//        }
//
//        // Log results for analysis
//        for (int i = 0; i < results.size(); i++) {
//            List<String> names = results.get(i).stream()
//                    .map(Product::getName)
//                    .collect(Collectors.toList());
//            System.out.println("Results " + (i + 1) + ": " + names);
//        }
//    }
//
    @Test
    @DisplayName("Should generate working regex for simple text search")
    void testSimpleTextSearchWithLLM() throws Exception {
        String        question = "Orion";
        List<Product> filtered = execute(question);


        // When
//        String regexPattern = searchService.parseQuery("Orion");

        // Then: Verify pattern is not empty and doesn't use fallback
//        assertThat(regexPattern).isNotEmpty();
        assertThat(regexPattern).containsIgnoringCase("orion");
//        System.out.println("Simple text search pattern: " + regexPattern);

        // Test filtering
//        List<Product> filtered = filterProducts(regexPattern);
        assertThat(filtered).isNotEmpty();
        assertThat(filtered).hasSize(1);

        // Should find products containing "Orion"
        List<String> filteredNames = filtered.stream()
                .map(Product::getName)
                .collect(Collectors.toList());
        assertThat(filteredNames).anyMatch(name -> name.toLowerCase().contains("orion"));
    }

    @Test
    @DisplayName("Should generate working regex for updated date search")
    void testUpdatedDateSearchWithLLM() throws Exception {
        String question = "products updated in 2025";

        List<Product> filtered = execute(question);

        // Debug: Show what we expect vs what we got
        System.out.println("\n=== Expected products updated in 2025 ===");
        for (Product product : testProducts) {
            if (product.getUpdated().getYear() == 2025) {
                String json = objectMapper.writeValueAsString(product);
                System.out.println(json);
            }
        }

        // Expected: Deep Space Probe, Space Station Alpha, Rocket Engine X
        assertThat(filtered).hasSize(3);

        // Verify the update dates make sense
        for (Product product : filtered) {
            OffsetDateTime updated  = product.getUpdated();
            boolean        isIn2025 = updated.getYear() == 2025;
            assertThat(isIn2025).isTrue(); // All matched products should actually be from 2025
        }
    }
}
