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

import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.function.Predicate;

/**
 * Main AI Filter service that acts as a facade for different filter generators.
 * This refactored version delegates to specialized implementations for each filter type.
 */
@Service
public class AiFilterService {

    private final JavaAiFilterGenerator       javaGenerator;
    private final JavaScriptAiFilterGenerator javaScriptGenerator;
    private final RegexAiFilterGenerator      regexGenerator;

    public AiFilterService(RegexAiFilterGenerator regexGenerator,
                           JavaScriptAiFilterGenerator javaScriptGenerator,
                           JavaAiFilterGenerator javaGenerator) {
        this.regexGenerator      = regexGenerator;
        this.javaScriptGenerator = javaScriptGenerator;
        this.javaGenerator       = javaGenerator;
    }

    /**
     * Parses a natural language search query using LLM with regex generation (default).
     *
     * @param query      The natural language query from the user
     * @param entityType The type of entity being searched (e.g., "Product", "Version")
     * @return Regex pattern string for filtering JSON objects
     */
    public String parseQuery(String query, String entityType) {
        return parseQuery(query, entityType, AiFilterGenerator.FilterType.REGEX);
    }

    /**
     * Parses a natural language search query using LLM with the specified filter type.
     *
     * @param query      The natural language query from the user
     * @param entityType The type of entity being searched (e.g., "Product", "Version")
     * @param filterType The type of filter to generate (REGEX, JAVASCRIPT, or JAVA)
     * @return Filter string for filtering objects (format depends on filter type)
     */
    public String parseQuery(String query, String entityType, AiFilterGenerator.FilterType filterType) {
        return switch (filterType) {
            case REGEX -> regexGenerator.generateFilter(query, entityType);
            case JAVASCRIPT -> javaScriptGenerator.generateFilter(query, entityType);
            case JAVA -> javaGenerator.generateFilter(query, entityType);
        };
    }

    /**
     * Parses a natural language search query and returns a compiled Java Predicate.
     *
     * @param query      The natural language query from the user
     * @param entityType The type of entity being searched (e.g., "Product", "Version")
     * @param now        The current date to use for date-based filtering (for testing purposes)
     * @param <T>        The entity type
     * @return A compiled Predicate that can be used to filter entities
     * @throws RuntimeException if compilation fails
     */
    public <T> Predicate<T> parseQueryToPredicate(String query, String entityType, LocalDate now) {
        return javaGenerator.generatePredicate(query, entityType, now);
    }

    /**
     * Parses a natural language search query and returns a compiled Java Predicate using current date.
     *
     * @param query      The natural language query from the user
     * @param entityType The type of entity being searched (e.g., "Product", "Version")
     * @param <T>        The entity type
     * @return A compiled Predicate that can be used to filter entities
     * @throws RuntimeException if compilation fails
     * @deprecated Use parseQueryToPredicate(String, String, LocalDate) instead for better testability
     */
    @Deprecated
    public <T> Predicate<T> parseQueryToPredicate(String query, String entityType) {
        return parseQueryToPredicate(query, entityType, LocalDate.now());
    }

    /**
     * @deprecated Use AiFilterGenerator.FilterType instead
     */
    @Deprecated
    public enum FilterType {
        REGEX,
        JAVASCRIPT,
        JAVA
    }
}
