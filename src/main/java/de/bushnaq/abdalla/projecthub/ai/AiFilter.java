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
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

/**
 * Enhanced service for parsing natural language search queries using both offline LLM and fallback regex patterns.
 * This implementation uses Spring AI with Ollama for sophisticated natural language processing while maintaining
 * a regex-based fallback for reliability and performance.
 */
@Service
@ConditionalOnProperty(name = "projecthub.search.llm.enabled", havingValue = "true", matchIfMissing = true)
public class AiFilter {

    // Base template for LLM prompts
    private static final String BASE_PROMPT_TEMPLATE = """
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

    private static final Logger                    logger = LoggerFactory.getLogger(AiFilter.class);
    private final        ChatClient                chatModel;
    private final        Map<String, PromptConfig> promptConfigs;

    public AiFilter(ChatClient.Builder builder) {
        this.chatModel     = builder.build();
        this.promptConfigs = initializePromptConfigs();
    }

    /**
     * Initialize prompt configurations for different entity types
     */
    private Map<String, PromptConfig> initializePromptConfigs() {
        Map<String, PromptConfig> configs = new HashMap<>();

        // Product configuration
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
                "Special considerations for Products: Focus on product names, keys (like P-1, PROJ-123), and creation/update dates.",
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
                        Output: (?i).*"updated"\\s*:\\s*"2025-(0[1-9]|1[0-2])-.*"""
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
                        - Support version comparisons (greater than, less than, between)
                        - Version keys follow patterns like V-1, V-123
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
                        - Feature keys follow patterns like F-1, FEAT-123
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
        if (query == null || query.trim().isEmpty()) {
            return "";
        }

        System.out.println("Parsing natural language query: '" + query + "' for entity type: " + entityType);

        // Try LLM parsing
        try {
            String llmResult = parseWithLLM(query, entityType);
            if (llmResult != null && !llmResult.trim().isEmpty()) {
                return llmResult;
            }
        } catch (Exception e) {
            logger.warn("LLM parsing failed, falling back to simple search: {}", e.getMessage());
        }

        // Fallback to simple case-insensitive search pattern
        return String.format("(?i).*%s.*", query.replaceAll("([\\\\\\[\\]{}()*+?.^$|])", "\\\\$1"));
    }

    /**
     * Backward compatibility method - defaults to Product entity type
     */
    public String parseQuery(String query) {
        return parseQuery(query, "Product");
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

            System.out.println("LLM response: '" + content + "'");

            return content != null ? content.trim() : "";

        } catch (Exception e) {
            logger.error("Error calling LLM for query parsing: {}", e.getMessage(), e);
            throw e;
        }
    }

    /**
     * Configuration class for entity-specific prompts
     */
    private static class PromptConfig {
        final String examples;
        final String jsonStructure;
        final String specialConsiderations;

        PromptConfig(String jsonStructure, String specialConsiderations, String examples) {
            this.jsonStructure         = jsonStructure;
            this.specialConsiderations = specialConsiderations;
            this.examples              = examples;
        }
    }
}
