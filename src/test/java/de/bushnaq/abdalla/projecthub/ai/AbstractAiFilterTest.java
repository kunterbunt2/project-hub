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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;
import java.util.stream.Collectors;

public class AbstractAiFilterTest<T> {
    private final   AiFilter     aiFilter;
    protected final ObjectMapper filterMapper;
    protected final Logger       logger = LoggerFactory.getLogger(this.getClass());
    protected       String       regexString;
    protected       List<T>      testProducts;

    public AbstractAiFilterTest(ObjectMapper mapper, AiFilter aiFilter) {
        this.filterMapper = mapper.copy();
        this.aiFilter     = aiFilter;
    }

    /**
     * Helper method to simulate filtering products using regex patterns
     * (mimics what SmartGlobalFilter does)
     */
    private List<T> applySearchQuery(Pattern regexPattern) throws Exception {
        return testProducts.stream()
                .filter(product -> {
                    try {
                        String json = filterMapper.writerWithDefaultPrettyPrinter().writeValueAsString(product);
                        return regexPattern.matcher(json).find();
                    } catch (Exception e) {
                        return false;
                    }
                })
                .collect(Collectors.toList());
    }

    protected List<T> performSearch(String searchValue, String entityType) throws Exception {
        int tryCount = 10;
        do {
            // Parse the query using natural language service with entity type
            try {
                regexString = aiFilter.parseQuery(searchValue, entityType);
                Pattern regexPattern = Pattern.compile(regexString);
                List<T> filtered     = applySearchQuery(regexPattern);
                System.out.println("\n=== Products matched by regex ===");
                for (T product : filtered) {
                    String json = filterMapper.writeValueAsString(product);
                    System.out.println(json);
                }
                return filtered;
            } catch (PatternSyntaxException e) {
                logger.error("Invalid regex pattern '{}', retrying {}/{}", regexString, tryCount, 10, e);
            }
        } while (--tryCount > 0);
        return null;
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
