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

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import java.time.LocalDate;
import java.util.List;
import java.util.function.Predicate;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;
import java.util.stream.Collectors;

public class AbstractAiFilterTest<T> {
    private final   AiFilterService aiFilterService;
    protected final ScriptEngine    engine;
    protected final ObjectMapper    filterMapper;
    protected       String          javascriptFunction;
    protected final Logger          logger = LoggerFactory.getLogger(this.getClass());
    protected final LocalDate       now;
    protected       String          regexString;
    protected       List<T>         testProducts;

    public AbstractAiFilterTest(ObjectMapper mapper, AiFilterService aiFilterService, LocalDate now) {
        System.setProperty("polyglot.engine.WarnInterpreterOnly", "false");
        this.filterMapper    = mapper.copy();
        this.aiFilterService = aiFilterService;
        this.now             = now;
        // Use GraalVM JavaScript engine specifically
        ScriptEngine engineByName = new ScriptEngineManager().getEngineByName("graal.js");
        if (engineByName != null) {
            this.engine = new ScriptEngineManager().getEngineByName("graal.js");
        } else {
            // Fallback to default JavaScript engine
            this.engine = new ScriptEngineManager().getEngineByName("javascript");
        }

        if (this.engine == null) {
            throw new RuntimeException("No JavaScript engine found. Make sure GraalVM JS dependencies are available.");
        }

        // Set up the mapper to ignore @JsonIgnore for filtering purposes
//        this.filterMapper.setAnnotationIntrospector(new FilterAnnotationIntrospector());
    }

    /**
     * Helper method to simulate filtering products using JavaScript functions
     * Now works with Java objects directly instead of JSON conversion
     */
    private List<T> applyJavaScriptSearchQuery(String jsFunction) throws Exception {
        // Define the JavaScript function once with the provided function body
        String completeFunction = String.format("function filterEntity(entity) { %s }", jsFunction);
//        System.out.println(completeFunction);
        engine.eval(completeFunction);

        return testProducts.stream()
                .filter(product -> {
                    try {
                        // Create a JavaScript-friendly wrapper object
                        String jsonString = filterMapper.writeValueAsString(product);
                        // Parse the JSON in JavaScript to create a proper JavaScript object
                        engine.put("jsonString", jsonString);
                        Object jsObject = engine.eval("JSON.parse(jsonString)");
                        // Put the JavaScript object into the context
                        engine.put("currentEntity", jsObject);
                        // Call the JavaScript function with the JavaScript object
                        Object result = engine.eval("filterEntity(currentEntity)");
//                        System.out.println("JavaScript result: " + result + " (type: " + (result != null ? result.getClass().getSimpleName() : "null") + ")");
                        // Handle JavaScript truthy/falsy values
                        if (result != null) {
                            // Convert JavaScript truthy values to boolean
                            if (result instanceof Number) {
                                return ((Number) result).doubleValue() != 0.0;
                            }
                            if (result instanceof String) {
                                return !((String) result).isEmpty();
                            }
                            // For other objects, check if they're not explicitly false
                            return !Boolean.FALSE.equals(result);
                        }
                        return false;
                    } catch (Exception e) {
                        logger.warn("JavaScript execution failed for product: {}", e.getMessage());
                        e.printStackTrace(); // Add stack trace for debugging
                        return false;
                    }
                })
                .collect(Collectors.toList());
    }

    /**
     * Helper method to simulate filtering products using regex patterns
     * (mimics what SmartGlobalFilter does)
     */
    private List<T> applyRegexSearchQuery(Pattern regexPattern) throws Exception {
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

    /**
     * Perform search using regex approach (existing method)
     */
    protected List<T> performSearch(String searchValue, String entityType) throws Exception {
        return performSearch(searchValue, entityType, AiFilterGenerator.FilterType.JAVA);
    }

    /**
     * Perform search with specified filter type
     */
    protected List<T> performSearch(String searchValue, String entityType, AiFilterGenerator.FilterType filterType) throws Exception {
        int tryCount = 10;
        do {
            try {

                switch (filterType) {
                    case REGEX: {
                        // Parse the query using regex generation (existing logic)
                        regexString = aiFilterService.parseQuery(searchValue, entityType, filterType);
                        Pattern regexPattern = Pattern.compile(regexString);
                        List<T> filtered     = applyRegexSearchQuery(regexPattern);
                        System.out.println("\n=== Products matched by regex ===");
                        System.out.println("Regex pattern: " + regexString);
                        for (T product : filtered) {
                            String json = filterMapper.writeValueAsString(product);
                            System.out.println(json);
                        }
                        return filtered;
                    }
                    case JAVASCRIPT: {
                        // Parse the query using JavaScript generation
                        javascriptFunction = aiFilterService.parseQuery(searchValue, entityType, filterType);
                        List<T> filtered = applyJavaScriptSearchQuery(javascriptFunction);
                        System.out.println("\n=== Products matched by JavaScript filter ===");
                        System.out.println("JavaScript function: " + javascriptFunction);
                        for (T product : filtered) {
                            String json = filterMapper.writeValueAsString(product);
                            System.out.println(json);
                        }
                        return filtered;
                    }
                    case JAVA: {
                        // Parse the query using Java generation and get compiled predicate
                        var     javaPredicate = aiFilterService.parseQueryToPredicate(searchValue, entityType, now);
                        List<T> filtered      = testProducts.stream().filter(javaPredicate).collect(Collectors.toList());

                        System.out.println("\n=== Products matched by Java filter ===");
//                        System.out.println("Java predicate compiled successfully");
                        for (T product : filtered) {
                            String json = filterMapper.writeValueAsString(product);
                            System.out.println(json);
                        }
                        return filtered;
                    }
                }
            } catch (PatternSyntaxException e) {
                logger.error("Invalid regex pattern '{}', retrying {}/{}", regexString, tryCount, 10, e);
            } catch (ScriptException e) {
                logger.error("JavaScript execution failed '{}', retrying {}/{}", javascriptFunction, tryCount, 10, e);
            }
        } while (--tryCount > 0);
        return null;
    }

    protected List<T> performSearch(Predicate<T> javaPredicate) throws Exception {
        List<T> filtered = testProducts.stream().filter(javaPredicate).collect(Collectors.toList());

        System.out.println("\n=== Products matched by Java filter ===");
//        System.out.println("Java predicate compiled successfully");
        for (T product : filtered) {
            String json = filterMapper.writeValueAsString(product);
            System.out.println(json);
        }
        return filtered;
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
