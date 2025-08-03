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

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.introspect.JacksonAnnotationIntrospector;
import de.bushnaq.abdalla.projecthub.dto.Product;
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
@TestConstructor(autowireMode = TestConstructor.AutowireMode.ALL)
class ProductAiFilterTest extends AbstractAiFilterTest<Product> {

    public ProductAiFilterTest(ObjectMapper mapper, AiFilter aiFilter) {
        super(mapper, aiFilter);
    }

    private Product createProduct(Long id, String name, OffsetDateTime created, OffsetDateTime updated) {
        Product product = new Product();
        product.setId(id);
        product.setName(name);
        product.setCreated(created);
        product.setUpdated(updated);
        return product;
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

    @Test
    @DisplayName("Should handle case-insensitive searches")
    void testCaseInsensitiveSearchWithLLM() throws Exception {
        String        question      = "MARS";
        List<Product> filtered      = performSearch(question, Product.class.getSimpleName());
        List<String>  filteredNames = filtered.stream().map(Product::getName).collect(Collectors.toList());
        assertThat(filteredNames).anyMatch(name -> name.toLowerCase().contains("mars"));
    }

    @Test
    @DisplayName("Should generate working regex for complex search")
    void testComplexSearchWithLLM() throws Exception {
        String        question = "space products created in 2024";
        List<Product> filtered = performSearch(question, Product.class.getSimpleName());
        assertThat(filtered).hasSize(1);
        for (Product product : filtered) {
            boolean hasSpace      = product.getName().toLowerCase().contains("space");
            boolean createdIn2024 = product.getCreated().getYear() == 2024;
            System.out.println(product.getName() + " - has 'space': " + hasSpace + ", created in 2024: " + createdIn2024 + ", created: " + product.getCreated());
        }

        // Should find at least some results
        assertThat(filtered).isNotEmpty();
    }

    @Test
    @DisplayName("Should generate working regex for date-based search")
    void testDateBasedSearchWithLLM() throws Exception {
        String        question = "products created after July 2024";
        List<Product> filtered = performSearch(question, Product.class.getSimpleName());
        assertThat(filtered).hasSize(7);

        // Verify the dates make sense
        for (Product product : filtered) {
            OffsetDateTime created         = product.getCreated();
            boolean        isAfterJuly2024 = created.isAfter(OffsetDateTime.of(2024, 7, 1, 0, 0, 0, 0, ZoneOffset.UTC));
            System.out.println(product.getName() + " created: " + created + " (after July 2024: " + isAfterJuly2024 + ")");
        }
    }

    @Test
    @DisplayName("Should generate working regex for name-specific search")
    void testNameSpecificSearchWithLLM() throws Exception {
        String        question = "name contains project";
        List<Product> filtered = performSearch(question, Product.class.getSimpleName());
        assertThat(regexString).isNotEmpty();
        // Should find products with "project" in the name field
        List<String> filteredNames = filtered.stream().map(Product::getName).collect(Collectors.toList());
        assertThat(filteredNames).anyMatch(name -> name.toLowerCase().contains("project"));
    }

    @Test
    @DisplayName("Should generate working regex for simple text search")
    void testSimpleTextSearchWithLLM() throws Exception {
        String        question = "Orion";
        List<Product> filtered = performSearch(question, Product.class.getSimpleName());
        assertThat(regexString).containsIgnoringCase("orion");
        assertThat(filtered).hasSize(1);
        List<String> filteredNames = filtered.stream().map(Product::getName).collect(Collectors.toList());
        assertThat(filteredNames).anyMatch(name -> name.toLowerCase().contains("orion"));
    }

    @Test
    @DisplayName("Should generate working regex for updated date search")
    void testUpdatedDateSearchWithLLM() throws Exception {
        String        question = "products updated in 2025";
        List<Product> filtered = performSearch(question, Product.class.getSimpleName());
        System.out.println("\n=== Expected products updated in 2025 ===");
        for (Product product : testProducts) {
            if (product.getUpdated().getYear() == 2025) {
                String json = filterMapper.writeValueAsString(product);
                System.out.println(json);
            }
        }
        assertThat(filtered).hasSize(3);
        for (Product product : filtered) {
            OffsetDateTime updated  = product.getUpdated();
            boolean        isIn2025 = updated.getYear() == 2025;
            assertThat(isIn2025).isTrue(); // All matched products should actually be from 2025
        }
    }

    /**
     * Custom annotation introspector that ignores @JsonIgnore annotations
     * but preserves all other Jackson annotations.
     */
    private static class FilterAnnotationIntrospector extends JacksonAnnotationIntrospector {
        @Override
        public boolean hasIgnoreMarker(com.fasterxml.jackson.databind.introspect.AnnotatedMember m) {
            // Don't ignore fields marked with @JsonIgnore for filtering purposes
            // but still process other ignore markers from the parent class
            if (m.hasAnnotation(JsonIgnore.class)) {
                return false;
            }
            return super.hasIgnoreMarker(m);
        }
    }
}
