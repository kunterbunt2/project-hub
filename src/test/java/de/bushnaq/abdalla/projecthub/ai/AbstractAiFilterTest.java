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
import org.graalvm.polyglot.Context;
import org.graalvm.polyglot.HostAccess;
import org.graalvm.polyglot.ResourceLimits;
import org.graalvm.polyglot.Value;
import org.graalvm.polyglot.io.IOAccess;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.script.ScriptException;
import java.time.LocalDate;
import java.util.List;
import java.util.function.Predicate;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;
import java.util.stream.Collectors;

public class AbstractAiFilterTest<T> {
    private final   AiFilterService aiFilterService;
    protected final ObjectMapper    filterMapper;
    protected       String          javascriptFunction;
    protected final Context         jsContext;  // Changed from ScriptEngine to Context
    protected final Logger          logger = LoggerFactory.getLogger(this.getClass());
    protected final LocalDate       now;
    protected       String          regexString;
    protected       List<T>         testProducts;

    public AbstractAiFilterTest(ObjectMapper mapper, AiFilterService aiFilterService, LocalDate now) {
        this.filterMapper    = mapper.copy();
        this.aiFilterService = aiFilterService;
        this.now             = now;

        // Create secure GraalVM Context with comprehensive security settings
        HostAccess secureHostAccess = HostAccess.newBuilder(HostAccess.EXPLICIT)
                .allowPublicAccess(true)  // Allow calling public methods like getName()
                .build();

        ResourceLimits secureResourceLimits = ResourceLimits.newBuilder()
                .statementLimit(200, null)                    // Limit to 200 statements
                .build();

        this.jsContext = Context.newBuilder("js")
                // Security: Comprehensive access controls
                .allowHostAccess(secureHostAccess)           // Limited host access for getter methods
                .allowIO(IOAccess.NONE)                      // ❌ No file system access
                .allowNativeAccess(false)                    // ❌ No native code execution
                .allowCreateThread(false)                    // ❌ No thread creation
                // Allow specific Java time classes for date operations
                .allowHostClassLookup(className -> {
                    return className.equals("java.time.OffsetDateTime") ||
                            className.equals("java.time.LocalDateTime") ||
                            className.equals("java.time.LocalDate") ||
                            className.equals("java.time.Year") ||
                            className.equals("java.time.Duration") ||
                            className.equals("java.time.ZoneOffset") ||
                            className.equals("java.lang.String") ||
                            className.equals("java.lang.Integer") ||
                            className.equals("java.lang.Boolean");
                })
                .allowEnvironmentAccess(org.graalvm.polyglot.EnvironmentAccess.NONE) // ❌ No env variables
                .allowPolyglotAccess(org.graalvm.polyglot.PolyglotAccess.NONE)       // ❌ No other languages
                .resourceLimits(secureResourceLimits)        // Resource limits for DoS protection
                .option("engine.WarnInterpreterOnly", "false")
                .build();

        // Set up the mapper to ignore @JsonIgnore for filtering purposes
//        this.filterMapper.setAnnotationIntrospector(new FilterAnnotationIntrospector());
    }

    /**
     * Helper method to simulate filtering products using JavaScript functions
     * Now uses GraalVM Context API for secure execution
     */
    protected List<T> applyJavaScriptSearchQuery(String jsFunction, LocalDate now) throws Exception {
        // Define the JavaScript function once with the provided function body
        String completeFunction = String.format("function filterEntity(entity, now) {\n %s \n}", jsFunction);
        System.out.println(completeFunction);

        // Evaluate the function definition in the secure context
        jsContext.eval("js", completeFunction);

        return testProducts.stream()
                .filter(product -> {
                    try {
                        // Put the Java object into the JavaScript context
                        jsContext.getBindings("js").putMember("currentEntity", product);
                        jsContext.getBindings("js").putMember("currentDate", now);

                        // Call the JavaScript function and get the result as a Value
                        Value result = jsContext.eval("js", "filterEntity(currentEntity, currentDate)");


                        // Explicitly check for undefined and throw exception
                        if (result == null || result.isNull()) {
                            throw new RuntimeException("JavaScript function returned null/undefined for entity: " + product);
                        }

                        // Convert JavaScript result to boolean
                        if (result.isBoolean()) {
                            return result.asBoolean();
                        }
                        if (result.isNumber()) {
                            return result.asDouble() != 0.0;
                        }
                        if (result.isString()) {
                            return !result.asString().isEmpty();
                        }
                        // For other objects, check if they're truthy
                        return true; // Non-null, non-primitive objects are truthy
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
        return performSearch(searchValue, entityType, AiFilterGenerator.FilterType.JAVASCRIPT);
    }

    /**
     * Perform search with specified filter type
     */
    protected List<T> performSearch(String searchValue, String entityType, AiFilterGenerator.FilterType filterType) throws Exception {
        int tryCount = 10;
        do {
            try {

                switch (filterType) {
                    case JAVASCRIPT: {
                        // Parse the query using JavaScript generation
                        javascriptFunction = aiFilterService.parseQuery(searchValue, entityType, filterType);
                        List<T> filtered = applyJavaScriptSearchQuery(javascriptFunction, now);
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
