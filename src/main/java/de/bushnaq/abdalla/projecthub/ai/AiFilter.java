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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Predicate;

/**
 * Enhanced service for parsing natural language search queries using both offline LLM and fallback regex patterns.
 * This implementation uses Spring AI with Ollama for sophisticated natural language processing while maintaining
 * a regex-based fallback for reliability and performance.
 */
@Service
//@ConditionalOnProperty(name = "projecthub.search.llm.enabled", havingValue = "true", matchIfMissing = true)
public class AiFilter {
    private static final String                    ANSI_BLUE                  = "\u001B[36m";
    private static final String                    ANSI_GRAY                  = "\u001B[37m";
    private static final String                    ANSI_GREEN                 = "\u001B[32m";
    private static final String                    ANSI_RED                   = "\u001B[31m";
    private static final String                    ANSI_RESET                 = "\u001B[0m";    // Declaring ANSI_RESET so that we can reset the color
    private static final String                    ANSI_YELLOW                = "\u001B[33m";
    // Base template for LLM prompts for regex generation
    private static final String                    BASE_PROMPT_TEMPLATE       = """
            You are a regex pattern generator for filtering JSON objects. Convert natural language search queries into Java regex patterns that will be applied to JSON strings.
            
            The JSON objects have this structure:
            %s
            
            %s
            
            IMPORTANT RULES:
            1. Generate case-insensitive regex patterns using (?i) flag
            2. For simple text searches, match the text anywhere in the JSON
            3. For field-specific searches (e.g., "name contains project"), target the specific field value
            4. For date searches, work with the ISO date format in the JSON (ends with Z, not timezone offset)
            5. Return ONLY the regex pattern, no explanations, no quotes, no additional text
            6. Use proper escaping for special regex characters
            7. Current year is %d if year context is needed
            8. For full year searches like "updated in 2025", match the entire year (months 01-12)
            9. make extra sure that the result is a valid regex pattern that can be used in Java Pattern.compile() method
            
            %s
            
            Now generate a regex pattern for this query:
            "%s"
            """;
    // Template for JavaScript filter generation
    private static final String                    JAVASCRIPT_PROMPT_TEMPLATE = """
            You are a JavaScript function generator for filtering JavaScript objects. Convert natural language search queries into JavaScript filter functions.
            
            IMPORTANT CONTEXT: You are filtering %s entities. Each 'entity' parameter passed to your function is already a %s object.
            
            The JavaScript objects you'll be filtering have properties matching this JSON structure:
            %s
            
            %s
            
            IMPORTANT RULES:
            1. Generate a JavaScript function that takes a JavaScript object parameter called 'entity'
            2. The function should return true if the entity matches the search criteria, false otherwise
            3. Use case-insensitive string comparisons when appropriate (use toLowerCase())
            4. For date comparisons, parse dates using new Date() constructor
            5. Access object properties directly (e.g., entity.name, entity.created)
            6. Return ONLY the JavaScript function body, no function declaration, no explanations. The returned answer must be a valid JavaScript code. Any explanation can be added as comments, but the code must be executable.
            7. Handle null/undefined values gracefully
            8. Current year is %d if year context is needed
            9. Use proper JavaScript syntax and operators
            10. For date fields, they are in ISO format strings that can be parsed by new Date()
            
            %s
            
            Now generate a JavaScript function body for this EXACT query:
            "%s"
            """;
    // Template for Java filter generation
    private static final String                    JAVA_PROMPT_TEMPLATE       = """
            You are a Java method body generator for filtering Java objects. Convert natural language search queries into Java code that can be compiled and executed.
            
            IMPORTANT CONTEXT: You are filtering %s entities. The 'entity' parameter passed to your method is already a %s object.
            
            The Java objects you'll be filtering have properties matching this JSON structure:
            %s
            
            %s
            
            IMPORTANT RULES:
            1. Generate ONLY the method body code that goes inside a boolean test(Object entity) method
            2. The method should return true if the entity matches the search criteria, false otherwise
            3. Use proper Java syntax and safe null checking
            4. Cast the entity parameter to the appropriate type or use reflection via helper methods
            5. For date comparisons, use LocalDateTime/LocalDate classes
            6. Use the provided helper methods: getStringField(), getIntegerField(), getDoubleField(), getDateTimeField(), getDateField(), containsIgnoreCase(), matchesPattern()
            7. Handle exceptions gracefully - return false if any operation fails
            8. Current year is %d if year context is needed
            9. Use proper Java operators and control structures
            10. For string searches, use containsIgnoreCase() helper method
            11. Access fields using getStringField(entity, "fieldName") helper method
            12. Return ONLY the method body code, no method signature, no class declaration, no explanations
            
            %s
            
            Now generate a Java method body for this EXACT query:
            "%s"
            """;
    private static final Logger                    logger                     = LoggerFactory.getLogger(AiFilter.class);
    private final        ChatClient                chatModel;
    private final        JavaFilterCompiler        javaFilterCompiler;
    private final        Map<String, PromptConfig> promptConfigs;

    public AiFilter(ChatClient.Builder builder, JavaFilterCompiler javaFilterCompiler) {
        this.chatModel          = builder.build();
        this.promptConfigs      = initializePromptConfigs();
        this.javaFilterCompiler = javaFilterCompiler;
    }

    /**
     * Extract the actual answer from DeepSeek response by removing thinking process.
     * DeepSeek models often include reasoning in <think> tags or similar patterns.
     */
    private String extractAnswerFromDeepSeekResponse(String rawResponse) {
        if (rawResponse == null || rawResponse.trim().isEmpty()) {
            return rawResponse;
        }

        String response = rawResponse.trim();

        // Pattern 1: Remove content between <think> and </think> tags
        response = response.replaceAll("(?s)<think>.*?</think>", "").trim();

        // Pattern 2: Remove content between <thinking> and </thinking> tags
        response = response.replaceAll("(?s)<thinking>.*?</thinking>", "").trim();

        // Pattern 3: Remove content between <!-- thinking and --> comments
        response = response.replaceAll("(?s)<!--\\s*thinking.*?-->", "").trim();

        // Pattern 4: Remove lines that start with "Thinking:" or "Let me think:"
        response = response.replaceAll("(?m)^(Thinking:|Let me think:).*$", "").trim();

        // Pattern 5: If response starts with reasoning text followed by "Answer:" or "Result:", extract only the part after
        if (response.matches("(?s).*\\b(Answer|Result|Output):\\s*(.*)")) {
            String[] parts = response.split("\\b(?:Answer|Result|Output):\\s*", 2);
            if (parts.length > 1) {
                response = parts[1].trim();
            }
        }

        // Pattern 6: Remove common reasoning prefixes
//        response = response.replaceAll("(?m)^(Let me analyze this|First, I need to|I need to create|Looking at this query).*?\\n", "").trim();

        // Pattern 7: If the response contains multiple lines and looks like reasoning followed by code/regex,
        // try to extract just the final code/regex pattern
//        String[] lines = response.split("\n");
//        if (lines.length > 1) {
//            // Look for the last line that looks like a regex pattern or JavaScript code
//            for (int i = lines.length - 1; i >= 0; i--) {
//                String line = lines[i].trim();
//
//                // Check if this line looks like a regex pattern
//                if (line.startsWith("(?i)") || line.matches(".*\\(\\?[imsux]*\\).*")) {
//                    return line;
//                }
//
//                // Check if this line looks like JavaScript code (return statement or function body)
//                if (line.startsWith("return ") || line.contains("entity.") || line.contains("new Date(")) {
//                    // If it's a single line JavaScript, return it
//                    // If it's part of multi-line JavaScript, collect all relevant lines
//                    StringBuilder jsCode = new StringBuilder();
//                    for (int j = i; j < lines.length; j++) {
//                        String jsLine = lines[j].trim();
//                        if (!jsLine.isEmpty() && !jsLine.startsWith("//") && !jsLine.startsWith("/*")) {
//                            if (jsCode.length() > 0) jsCode.append(" ");
//                            jsCode.append(jsLine);
//                        }
//                    }
//                    return jsCode.toString();
//                }
//            }
//        }
//
//        // Pattern 8: Remove any remaining explanatory text at the beginning
//        response = response.replaceAll("(?s)^.*?(?=(?:\\(\\?i\\)|return |if \\(|const |let |var ))", "").trim();

        return response.isEmpty() ? rawResponse : response;
    }

    /**
     * Initialize prompt configurations for different entity types
     */
    private Map<String, PromptConfig> initializePromptConfigs() {
        Map<String, PromptConfig> configs = new HashMap<>();

        // Product configuration with both regex and JavaScript examples
        configs.put("Product", new PromptConfig(
                """
                        {
                          "id" : 1,
                          "created" : "2025-01-01T08:00:00+01:00",
                          "updated" : "2025-01-01T08:00:00+01:00",
                          "name" : "Orion",
                          "versions" : [ ],
                          "key" : "P-1"
                        }""",
                """
                        Special considerations for Products: 
                        - Focus on product names, keys (like P-1, PROJ-123), and creation/update dates
                        - Remember: you are filtering Product entities, so each 'entity' is already a Product
                        - When queries mention "products created in 2024" - this means filter by creation year, NOT by checking if entity.name contains "products"
                        - Product keys follow patterns like P-1, P-123. Keys are basically just unique database IDs of the Version entity.
                        - Terms like "products", "items", or similar generic terms refer to the entity type, not name content""",
                """
                        Examples:
                        Input: "Orion"
                        Output: (?i).*orion.*
                        
                        Input: "name contains project"
                        Output: (?i).*"name"\\s*:\\s*"[^"]*project[^"]*".*
                        
                        Input: "products created after January 2024"
                        Output: (?i).*"created"\\s*:\\s*"2024-(0[2-9]|1[0-2])-.*
                        
                        Input: "items created before December 2024"
                        Output: (?i).*"created"\\s*:\\s*"2024-(0[1-9]|1[01])-.*
                        
                        Input: "products updated in 2025"
                        Output: (?i).*"updated"\\s*:\\s*"2025-(0[1-9]|1[0-2])-.*""",
                """
                        Examples:
                        Input: "Orion"
                        Output: return entity && entity.name && entity.name.toLowerCase().includes('orion');
                        
                        Input: "name contains project"
                        Output: return entity && entity.name && entity.name.toLowerCase().includes('project');
                        
                        Input: "created in 2024"
                        Output: return entity && entity.created && new Date(entity.created).getFullYear() === 2024;
                        
                        Input: "products created in 2024"
                        Output: return entity && entity.created && new Date(entity.created).getFullYear() === 2024;
                        
                        Input: "items created in 2024"
                        Output: return entity && entity.created && new Date(entity.created).getFullYear() === 2024;
                        
                        Input: "products created after January 2024"
                        Output: if (!entity || !entity.created) return false; const created = new Date(entity.created); return created > new Date('2024-01-31');
                        
                        Input: "items created before December 2024"
                        Output: if (!entity || !entity.created) return false; const created = new Date(entity.created); return created < new Date('2024-12-01');
                        
                        Input: "products updated in 2025"
                        Output: if (!entity || !entity.updated) return false; const updated = new Date(entity.updated); return updated.getFullYear() === 2025;
                        
                        Input: "MARS"
                        Output: return entity && entity.name && entity.name.toLowerCase().includes('mars');
                        
                        Input: "space products created in 2024"
                        Output: if (!entity || !entity.name || !entity.created) return false; const hasSpace = entity.name.toLowerCase().includes('space'); const created = new Date(entity.created); const isCreated2024 = created.getFullYear() === 2024; return hasSpace && isCreated2024;""",
                """
                        Examples:
                        Input: "Orion"
                        Output: String name = getStringField(entity, "name"); return name != null && containsIgnoreCase(name, "orion");
                        
                        Input: "name contains project"
                        Output: String name = getStringField(entity, "name"); return name != null && containsIgnoreCase(name, "project");
                        
                        Input: "created in 2024"
                        Output: LocalDateTime created = getDateTimeField(entity, "created"); return created != null && created.getYear() == 2024;
                        
                        Input: "products created in 2024"
                        Output: LocalDateTime created = getDateTimeField(entity, "created"); return created != null && created.getYear() == 2024;
                        
                        Input: "items created in 2024"
                        Output: LocalDateTime created = getDateTimeField(entity, "created"); return created != null && created.getYear() == 2024;
                        
                        Input: "products created after January 2024"
                        Output: LocalDateTime created = getDateTimeField(entity, "created"); return created != null && created.isAfter(LocalDateTime.of(2024, 1, 31, 23, 59, 59));
                        
                        Input: "items created before December 2024"
                        Output: LocalDateTime created = getDateTimeField(entity, "created"); return created != null && created.isBefore(LocalDateTime.of(2024, 12, 1, 0, 0, 0));
                        
                        Input: "products updated in 2025"
                        Output: LocalDateTime updated = getDateTimeField(entity, "updated"); return updated != null && updated.getYear() == 2025;
                        
                        Input: "MARS"
                        Output: String name = getStringField(entity, "name"); return name != null && containsIgnoreCase(name, "mars");
                        
                        Input: "space products created in 2024"
                        Output: String name = getStringField(entity, "name"); LocalDateTime created = getDateTimeField(entity, "created"); return name != null && containsIgnoreCase(name, "space") && created != null && created.getYear() == 2024;
                        
                        Input: "key starts with P-"
                        Output: String key = getStringField(entity, "key"); return key != null && key.startsWith("P-");
                        
                        Input: "products with empty versions"
                        Output: try { java.lang.reflect.Field field = entity.getClass().getDeclaredField("versions"); field.setAccessible(true); java.util.List<?> versions = (java.util.List<?>) field.get(entity); return versions == null || versions.isEmpty(); } catch (Exception e) { return false; }"""
        ));

        // Version configuration
        configs.put("Version", new PromptConfig(
                """
                        {
                          "id" : 1,
                          "created" : "2025-01-01T08:00:00+01:00",
                          "updated" : "2025-01-01T08:00:00+01:00",
                          "name" : "1.2.3",
                          "features" : [ ],
                          "key" : "V-1",
                          "productId" : 1
                        }""",
                """
                        Special considerations for Versions:
                        - Version names often follow semantic versioning (1.0.0, 2.1.3, etc.)
                        - Version names are the actual version and can have attributes attached like alpha, beta or SNAPSHOT to signal prerelease versions, e.g. 1.0.0-alpha.
                        - Support version comparisons (greater than, less than, between), where we compare the versions line numbers, so 3.1.4 is bigger than 1.5.6. Basically you just multiply every number with 10 of its position. So 3*100+1*10+4 > 1*100+5*10+6.
                        - Version keys follow patterns like V-1, V-123. Keys are basically just unique database IDs of the Version entity.
                        - Consider major.minor.patch patterns""",
                """
                        Examples:
                        Input: "1.2.3"
                        Output: (?i).*1\\.2\\.3.*
                        
                        Input: "version greater than 1.0.0"
                        Output: (?i).*"name"\\s*:\\s*"([2-9]\\.[0-9]+\\.[0-9]+|1\\.[1-9][0-9]*\\.[0-9]+|1\\.0\\.[1-9][0-9]*)".*
                        
                        Input: "version less than 2.0.0"
                        Output: (?i).*"name"\\s*:\\s*"(0\\.[0-9]+\\.[0-9]+|1\\.[0-9]+\\.[0-9]+)".*
                        
                        Input: "versions between 1.0.0 and 2.0.0"
                        Output: (?i).*"name"\\s*:\\s*"1\\.[0-9]+\\.[0-9]+".*
                        
                        Input: "name contains beta"
                        Output: (?i).*"name"\\s*:\\s*"[^"]*beta[^"]*".*
                        
                        Input: "versions created after January 2024"
                        Output: (?i).*"created"\\s*:\\s*"2024-(0[2-9]|1[0-2])-.*"""
        ));

        // Feature configuration
        configs.put("Feature", new PromptConfig(
                """
                        {
                          "id" : 1,
                          "created" : "2025-01-01T08:00:00+01:00",
                          "updated" : "2025-01-01T08:00:00+01:00",
                          "name" : "User Authentication",
                          "sprints" : [ ],
                          "key" : "F-1",
                          "versionId" : 1
                        }""",
                """
                        Special considerations for Features:
                        - Feature names describe functionality (e.g., "User Authentication", "Payment Processing")
                        - Feature keys follow patterns like F-1, F-123. Keys are basically just unique database IDs of the Version entity.
                        - Features are grouped under versions and contain sprints
                        - Focus on feature purpose and functionality descriptions""",
                """
                        Examples:
                        Input: "authentication"
                        Output: (?i).*authentication.*
                        
                        Input: "name contains user"
                        Output: (?i).*"name"\\s*:\\s*"[^"]*user[^"]*".*
                        
                        Input: "features created after January 2024"
                        Output: (?i).*"created"\\s*:\\s*"2024-(0[2-9]|1[0-2])-.*
                        
                        Input: "features updated in 2025"
                        Output: (?i).*"updated"\\s*:\\s*"2025-(0[1-9]|1[0-2])-.*
                        
                        Input: "key starts with F-"
                        Output: (?i).*"key"\\s*:\\s*"F-[^"]*".*
                        
                        Input: "payment features"
                        Output: (?i).*payment.*"""
        ));

        // Sprint configuration
        configs.put("Sprint", new PromptConfig(
                """
                        {
                          "id" : 1,
                          "created" : "2025-01-01T08:00:00+01:00",
                          "updated" : "2025-01-01T08:00:00+01:00",
                          "name" : "Sprint 1.2.3-Alpha",
                          "key" : "S-1",
                          "featureId" : 1,
                          "start" : "2025-01-01T09:00:00+01:00",
                          "end" : "2025-01-15T17:00:00+01:00",
                          "status" : "ACTIVE",
                          "originalEstimation" : "PT80H",
                          "worked" : "PT40H",
                          "remaining" : "PT40H",
                          "releaseDate" : "2025-01-15T17:00:00+01:00"
                        }""",
                """
                        Special considerations for Sprints:
                        - Sprint names often include version numbers, alpha/beta/rc suffixes
                        - Status values: CREATED, ACTIVE, COMPLETED, CANCELLED, ON_HOLD
                        - Time durations in ISO-8601 format (PT80H = 80 hours, PT2D = 2 days)
                        - Sprint keys follow patterns like S-1, SPRINT-123
                        - Support time-based queries (start/end dates, duration comparisons)
                        - Consider sprint progress (worked vs remaining time)""",
                """
                        Examples:
                        Input: "sprint alpha"
                        Output: (?i).*alpha.*
                        
                        Input: "active sprints"
                        Output: (?i).*"status"\\s*:\\s*"ACTIVE".*
                        
                        Input: "sprints starting after January 2025"
                        Output: (?i).*"start"\\s*:\\s*"2025-(0[2-9]|1[0-2])-.*
                        
                        Input: "sprints ending before March 2025"
                        Output: (?i).*"end"\\s*:\\s*"2025-(0[1-2])-.*
                        
                        Input: "completed sprints"
                        Output: (?i).*"status"\\s*:\\s*"COMPLETED".*
                        
                        Input: "sprints with remaining work"
                        Output: (?i).*"remaining"\\s*:\\s*"PT[1-9][^"]*".*
                        
                        Input: "sprints over 60 hours estimation"
                        Output: (?i).*"originalEstimation"\\s*:\\s*"PT([6-9][0-9]|[1-9][0-9]{2,})H".*
                        
                        Input: "name contains beta"
                        Output: (?i).*"name"\\s*:\\s*"[^"]*beta[^"]*".*"""
        ));

        // Availability configuration
        configs.put("Availability", new PromptConfig(
                """
                        {
                          "id" : 1,
                          "created" : "2025-01-01T08:00:00+01:00",
                          "updated" : "2025-01-01T08:00:00+01:00",
                          "availability" : 0.8,
                          "start" : "2025-01-01",
                          "user" : {
                            "id" : 1,
                            "name" : "John Doe"
                          }
                        }""",
                """
                        Special considerations for Availability:
                        - Availability values are floats between 0.0 and 1.0 (e.g., 0.8 = 80% availability)
                        - Start dates are in LocalDate format (YYYY-MM-DD)
                        - Availability keys follow pattern A-1, A-123
                        - Support percentage-based queries and date range filtering
                        - Consider user associations for availability periods""",
                """
                        Examples:
                        Input: "80% availability"
                        Output: (?i).*"availability"\\s*:\\s*0\\.8.*
                        
                        Input: "availability greater than 50%"
                        Output: (?i).*"availability"\\s*:\\s*(0\\.[5-9][0-9]*|1\\.0).*
                        
                        Input: "availability less than 90%"
                        Output: (?i).*"availability"\\s*:\\s*(0\\.[0-8][0-9]*|0\\.0).*
                        
                        Input: "availability starting after January 2025"
                        Output: (?i).*"start"\\s*:\\s*"2025-(0[2-9]|1[0-2])-.*
                        
                        Input: "availability starting before March 2025"
                        Output: (?i).*"start"\\s*:\\s*"2025-(0[1-2])-.*
                        
                        Input: "full availability"
                        Output: (?i).*"availability"\\s*:\\s*1\\.0.*
                        
                        Input: "partial availability"
                        Output: (?i).*"availability"\\s*:\\s*(0\\.[1-9][0-9]*|0\\.[0-9][1-9]).*
                        
                        Input: "availability created in 2025"
                        Output: (?i).*"created"\\s*:\\s*"2025-(0[1-9]|1[0-2])-.*"""
        ));

        // Location configuration
        configs.put("Location", new PromptConfig(
                """
                        {
                          "id" : 1,
                          "created" : "2025-01-01T08:00:00+01:00",
                          "updated" : "2025-01-01T08:00:00+01:00",
                          "country" : "Germany",
                          "state" : "Bavaria",
                          "start" : "2025-01-01",
                          "user" : {
                            "id" : 1,
                            "name" : "John Doe"
                          }
                        }""",
                """
                        Special considerations for Locations:
                        - Country and state fields contain location information for determining public holidays
                        - Start dates indicate when the user began working at this location
                        - Location keys follow pattern L-1, L-123
                        - Support geographical searches and date-based filtering
                        - Consider legal/contract location contexts""",
                """
                        Examples:
                        Input: "Germany"
                        Output: (?i).*germany.*
                        
                        Input: "country Germany"
                        Output: (?i).*"country"\\s*:\\s*"[^"]*Germany[^"]*".*
                        
                        Input: "state Bavaria"
                        Output: (?i).*"state"\\s*:\\s*"[^"]*Bavaria[^"]*".*
                        
                        Input: "locations in Australia"
                        Output: (?i).*"country"\\s*:\\s*"[^"]*Australia[^"]*".*
                        
                        Input: "locations starting after January 2025"
                        Output: (?i).*"start"\\s*:\\s*"2025-(0[2-9]|1[0-2])-.*
                        
                        Input: "locations starting before March 2025"
                        Output: (?i).*"start"\\s*:\\s*"2025-(0[1-2])-.*
                        
                        Input: "European locations"
                        Output: (?i).*(Germany|France|Italy|Spain|Netherlands|Belgium|Austria|Switzerland|United Kingdom|Ireland|Portugal|Greece|Denmark|Sweden|Norway|Finland|Poland|Czech Republic|Hungary|Slovakia|Slovenia|Croatia|Romania|Bulgaria|Lithuania|Latvia|Estonia|Luxembourg|Malta|Cyprus).*
                        
                        Input: "locations created in 2025"
                        Output: (?i).*"created"\\s*:\\s*"2025-(0[1-9]|1[0-2])-.*"""
        ));

        // OffDay configuration
        configs.put("OffDay", new PromptConfig(
                """
                        {
                          "id" : 1,
                          "created" : "2025-01-01T08:00:00+01:00",
                          "updated" : "2025-01-01T08:00:00+01:00",
                          "firstDay" : "2025-01-15",
                          "lastDay" : "2025-01-17",
                          "type" : "VACATION",
                          "user" : {
                            "id" : 1,
                            "name" : "John Doe"
                          }
                        }""",
                """
                        Special considerations for OffDays:
                        - Type values: VACATION, SICK, TRIP, HOLIDAY
                        - Date ranges with firstDay and lastDay in LocalDate format
                        - OffDay keys follow pattern D-1, D-123
                        - Support type-based filtering and date range queries
                        - Consider duration calculations and overlap queries""",
                """
                        Examples:
                        Input: "vacation"
                        Output: (?i).*"type"\\s*:\\s*"VACATION".*
                        
                        Input: "sick days"
                        Output: (?i).*"type"\\s*:\\s*"SICK".*
                        
                        Input: "holidays"
                        Output: (?i).*"type"\\s*:\\s*"HOLIDAY".*
                        
                        Input: "trips"
                        Output: (?i).*"type"\\s*:\\s*"TRIP".*
                        
                        Input: "off days in January 2025"
                        Output: (?i).*"firstDay"\\s*:\\s*"2025-01-.*
                        
                        Input: "off days starting after February 2025"
                        Output: (?i).*"firstDay"\\s*:\\s*"2025-(0[3-9]|1[0-2])-.*
                        
                        Input: "off days ending before March 2025"
                        Output: (?i).*"lastDay"\\s*:\\s*"2025-(0[1-2])-.*
                        
                        Input: "long vacations"
                        Output: (?i).*"type"\\s*:\\s*"VACATION".*
                        
                        Input: "off days created in 2025"
                        Output: (?i).*"created"\\s*:\\s*"2025-(0[1-9]|1[0-2])-.*"""
        ));

        // User configuration
        configs.put("User", new PromptConfig(
                """
                        {
                          "id" : 1,
                          "created" : "2025-01-01T08:00:00+01:00",
                          "updated" : "2025-01-01T08:00:00+01:00",
                          "name" : "John Doe",
                          "email" : "john.doe@company.com",
                          "firstWorkingDay" : "2020-03-15",
                          "lastWorkingDay" : null,
                          "color" : {
                            "value" : -16776961,
                            "falpha" : 0.0
                          },
                          "availabilities" : [ ],
                          "locations" : [ ],
                          "offDays" : [ ]
                        }""",
                """
                        Special considerations for Users:
                        - User names contain first and last names (e.g., "John Doe", "Jane Smith")
                        - Email addresses follow standard patterns (firstname.lastname@domain.com)
                        - Employment status: active users have lastWorkingDay as null, former employees have a date
                        - User keys follow pattern U-1, U-123
                        - firstWorkingDay indicates hire date, lastWorkingDay indicates termination date
                        - Users have associated availabilities, locations, and off days
                        - Support tenure-based queries and employment status filtering""",
                """
                        Examples:
                        Input: "John Doe"
                        Output: (?i).*"name"\\s*:\\s*"[^"]*John Doe[^"]*".*
                        
                        Input: "first name John"
                        Output: (?i).*"name"\\s*:\\s*"[^"]*John[^"]*".*
                        
                        Input: "last name Smith"
                        Output: (?i).*"name"\\s*:\\s*"[^"]*Smith[^"]*".*
                        
                        Input: "email john.doe@company.com"
                        Output: (?i).*"email"\\s*:\\s*"[^"]*john\\.doe@company\\.com[^"]*".*
                        
                        Input: "@company.com"
                        Output: (?i).*"email"\\s*:\\s*"[^"]*@company\\.com[^"]*".*
                        
                        Input: "active users"
                        Output: (?i).*"lastWorkingDay"\\s*:\\s*null.*
                        
                        Input: "former employees"
                        Output: (?i).*"lastWorkingDay"\\s*:\\s*"[0-9]{4}-[0-9]{2}-[0-9]{2}".*
                        
                        Input: "users starting after 2020"
                        Output: (?i).*"firstWorkingDay"\\s*:\\s*"(202[1-9]|20[3-9][0-9])-.*
                        
                        Input: "users starting before 2020"
                        Output: (?i).*"firstWorkingDay"\\s*:\\s*"(19[0-9]{2}|201[0-9])-.*
                        
                        Input: "users starting in 2024"
                        Output: (?i).*"firstWorkingDay"\\s*:\\s*"2024-.*
                        
                        Input: "long-term employees"
                        Output: (?i).*"firstWorkingDay"\\s*:\\s*"(19[0-9]{2}|20[01][0-9]|2020)-.*
                        
                        Input: "new employees"
                        Output: (?i).*"firstWorkingDay"\\s*:\\s*"(202[3-9]|20[3-9][0-9])-.*
                        
                        Input: "users created in 2025"
                        Output: (?i).*"created"\\s*:\\s*"2025-(0[1-9]|1[0-2])-.*"""
        ));

        return configs;
    }

    /**
     * Parses a natural language search query using LLM with regex fallback.
     *
     * @param query      The natural language query from the user
     * @param entityType The type of entity being searched (e.g., "Product", "Version")
     * @return Regex pattern string for filtering JSON objects
     */
    public String parseQuery(String query, String entityType) {
        return parseQuery(query, entityType, FilterType.REGEX);
    }

    /**
     * Parses a natural language search query using LLM with the specified filter type.
     *
     * @param query      The natural language query from the user
     * @param entityType The type of entity being searched (e.g., "Product", "Version")
     * @param filterType The type of filter to generate (REGEX or JAVASCRIPT)
     * @return Filter string for filtering objects (regex pattern or JavaScript function body)
     */
    public String parseQuery(String query, String entityType, FilterType filterType) {
        if (query == null || query.trim().isEmpty()) {
            return "";
        }

        System.out.printf("\nParsing natural language query: '%s%s%s' for entity type: '%s%s%s' with filter type: '%s%s%s'%n\n", ANSI_GREEN, query, ANSI_RESET, ANSI_BLUE, entityType, ANSI_RESET, ANSI_BLUE, filterType, ANSI_RESET);

        try {
            String llmResult;
            if (filterType == FilterType.JAVASCRIPT) {
                llmResult = parseWithJavaScriptLLM(query, entityType);
            } else if (filterType == FilterType.JAVA) {
                llmResult = parseWithJavaLLM(query, entityType);
            } else {
                llmResult = parseWithLLM(query, entityType);
            }

            if (llmResult != null && !llmResult.trim().isEmpty()) {
                return llmResult;
            }
            logger.warn("LLM parsing failed, result is empty");
            throw new RuntimeException("LLM parsing failed, result is empty");
        } catch (Exception e) {
            logger.warn("LLM parsing failed {}", e.getMessage(), e);
            throw new RuntimeException("LLM parsing failed", e);
        }
    }

    /**
     * Parses a natural language search query and returns a compiled Java Predicate.
     *
     * @param query      The natural language query from the user
     * @param entityType The type of entity being searched (e.g., "Product", "Version")
     * @param <T>        The entity type
     * @return A compiled Predicate that can be used to filter entities
     * @throws RuntimeException if compilation fails
     */
    public <T> Predicate<T> parseQueryToPredicate(String query, String entityType) {
        if (query == null || query.trim().isEmpty()) {
            return entity -> true; // Return a predicate that matches everything
        }

        System.out.printf("\nParsing natural language query to Java Predicate: '%s%s%s' for entity type: '%s%s%s'%n\n",
                ANSI_GREEN, query, ANSI_RESET, ANSI_BLUE, entityType, ANSI_RESET);

        try {
            // Generate Java code using LLM
            String javaCode = parseWithJavaLLM(query, entityType);

            if (javaCode == null || javaCode.trim().isEmpty()) {
                logger.warn("Java code generation failed, result is empty");
                throw new RuntimeException("Java code generation failed, result is empty");
            }

            System.out.printf("Generated Java code:\n%s%s%s\n\n", ANSI_BLUE, javaCode, ANSI_RESET);

            // Compile the Java code and return the Predicate
            return javaFilterCompiler.compileFilter(javaCode, entityType);

        } catch (Exception e) {
            logger.error("Failed to parse query to Java Predicate: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to parse query to Java Predicate: " + e.getMessage(), e);
        }
    }

    /**
     * Parse query using offline LLM via Spring AI with Java code generation
     */
    private String parseWithJavaLLM(String query, String entityType) {
        try {
            PromptConfig config = promptConfigs.getOrDefault(entityType, promptConfigs.get("Product"));

            // Create prompt with current year context and entity-specific configuration
            int currentYear = LocalDate.now().getYear();
            String formattedPrompt = String.format(JAVA_PROMPT_TEMPLATE,
                    entityType,           // You are filtering %s entities
                    entityType,           // The 'entity' parameter passed to your method is already a %s object
                    config.jsonStructure, // The Java objects you'll be filtering have properties matching this JSON structure:
                    config.specialConsiderations,
                    currentYear,          // Current year is %d if year context is needed
                    config.javaExamples != null ? config.javaExamples : "",
                    query);

            // Create prompt and get response using Spring AI 1.0.1 API
            Prompt prompt = new Prompt(formattedPrompt);

            System.out.printf("Java LLM prompt for '%s%s%s'\n%s%s%s\n\n", ANSI_BLUE, entityType, ANSI_RESET, ANSI_GREEN, formattedPrompt, ANSI_RESET);

            ChatClient.ChatClientRequestSpec request  = chatModel.prompt(prompt);
            ChatClient.CallResponseSpec      response = request.call();

            // Get the content directly from the response
            String content = response.content();

            System.out.printf("Java LLM raw response\n\n%s%s%s\n\n", ANSI_YELLOW, content, ANSI_RESET);

            // Extract actual answer from DeepSeek response (remove thinking process)
            String extractedAnswer = extractAnswerFromDeepSeekResponse(content);

            System.out.printf("Java LLM extracted answer\n\n%s%s%s\n\n", ANSI_YELLOW, extractedAnswer, ANSI_RESET);

            return extractedAnswer != null ? extractedAnswer.trim() : "";

        } catch (Exception e) {
            logger.error("Error calling LLM for Java query parsing: {}", e.getMessage(), e);
            throw e;
        }
    }

    /**
     * Parse query using offline LLM via Spring AI with JavaScript generation
     */
    private String parseWithJavaScriptLLM(String query, String entityType) {
        try {
            PromptConfig config = promptConfigs.getOrDefault(entityType, promptConfigs.get("Product"));

            // Create prompt with current year context and entity-specific configuration
            int currentYear = LocalDate.now().getYear();
            String formattedPrompt = String.format(JAVASCRIPT_PROMPT_TEMPLATE,
                    entityType,           // You are filtering %s entities
                    entityType,           // Each 'entity' parameter passed to your function is already a %s object
                    config.jsonStructure, // The JavaScript objects you'll be filtering have properties matching this JSON structure:
                    config.specialConsiderations,
                    currentYear,          // Current year is %d if year context is needed
                    config.javascriptExamples,
                    query);

            // Create prompt and get response using Spring AI 1.0.1 API
            Prompt prompt = new Prompt(formattedPrompt);

            System.out.printf("JavaScript LLM prompt for '%s%s%s'\n%s%s%s\n\n", ANSI_BLUE, entityType, ANSI_RESET, ANSI_GREEN, formattedPrompt, ANSI_RESET);

            ChatClient.ChatClientRequestSpec request  = chatModel.prompt(prompt);
            ChatClient.CallResponseSpec      response = request.call();

            // Get the content directly from the response
            String content = response.content();

            System.out.printf("JavaScript LLM raw response\n\n%s%s%s\n\n", ANSI_YELLOW, content, ANSI_RESET);

            // Extract actual answer from DeepSeek response (remove thinking process)
            String extractedAnswer = extractAnswerFromDeepSeekResponse(content);

            System.out.printf("JavaScript LLM extracted answer\n\n%s%s%s\n\n", ANSI_YELLOW, extractedAnswer, ANSI_RESET);

            return extractedAnswer != null ? extractedAnswer.trim() : "";

        } catch (Exception e) {
            logger.error("Error calling LLM for JavaScript query parsing: {}", e.getMessage(), e);
            throw e;
        }
    }

    /**
     * Parse query using offline LLM via Spring AI with entity-specific prompts
     */
    private String parseWithLLM(String query, String entityType) {
        try {
            PromptConfig config = promptConfigs.getOrDefault(entityType, promptConfigs.get("Product"));

            // Create prompt with current year context and entity-specific configuration
            int currentYear = LocalDate.now().getYear();
            String formattedPrompt = String.format(BASE_PROMPT_TEMPLATE,
                    config.jsonStructure,
                    config.specialConsiderations,
                    currentYear,
                    config.examples,
                    query);

            // Create prompt and get response using Spring AI 1.0.1 API
            Prompt prompt = new Prompt(formattedPrompt);

            System.out.println("LLM prompt for " + entityType + ": '" + formattedPrompt + "'");
            ChatClient.ChatClientRequestSpec request  = chatModel.prompt(prompt);
            ChatClient.CallResponseSpec      response = request.call();
            String                           content  = response.content();

            System.out.println("LLM raw response: '" + content + "'");

            // Extract actual answer from DeepSeek response (remove thinking process)
            String extractedAnswer = extractAnswerFromDeepSeekResponse(content);

            System.out.println("LLM extracted answer: '" + extractedAnswer + "'");

            return extractedAnswer != null ? extractedAnswer.trim() : "";

        } catch (Exception e) {
            logger.error("Error calling LLM for query parsing: {}", e.getMessage(), e);
            throw e;
        }
    }

    /**
     * Enum for different filter types
     */
    public enum FilterType {
        REGEX,
        JAVASCRIPT,
        JAVA
    }

    /**
     * Configuration class for entity-specific prompts
     */
    private static class PromptConfig {
        final String examples;
        final String javaExamples;
        final String javascriptExamples;
        final String jsonStructure;
        final String specialConsiderations;

        PromptConfig(String jsonStructure, String specialConsiderations, String examples) {
            this.jsonStructure         = jsonStructure;
            this.specialConsiderations = specialConsiderations;
            this.examples              = examples;
            this.javascriptExamples    = ""; // Default empty for entities without JavaScript examples
            this.javaExamples          = ""; // Default empty for entities without Java examples
        }

        PromptConfig(String jsonStructure, String specialConsiderations, String examples, String javascriptExamples) {
            this.jsonStructure         = jsonStructure;
            this.specialConsiderations = specialConsiderations;
            this.examples              = examples;
            this.javascriptExamples    = javascriptExamples;
            this.javaExamples          = ""; // Default empty for entities without Java examples
        }

        PromptConfig(String jsonStructure, String specialConsiderations, String examples, String javascriptExamples, String javaExamples) {
            this.jsonStructure         = jsonStructure;
            this.specialConsiderations = specialConsiderations;
            this.examples              = examples;
            this.javascriptExamples    = javascriptExamples;
            this.javaExamples          = javaExamples;
        }
    }
}
