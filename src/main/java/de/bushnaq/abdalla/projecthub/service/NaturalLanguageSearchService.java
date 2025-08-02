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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import java.time.LocalDate;

/**
 * Enhanced service for parsing natural language search queries using both offline LLM and fallback regex patterns.
 * This implementation uses Spring AI with Ollama for sophisticated natural language processing while maintaining
 * a regex-based fallback for reliability and performance.
 */
@Service
@ConditionalOnProperty(name = "projecthub.search.llm.enabled", havingValue = "true", matchIfMissing = true)
public class NaturalLanguageSearchService {

    // LLM prompt template for regex generation
    private static final String     LLM_PROMPT_TEMPLATE = """
            You are a regex pattern generator for filtering JSON objects. Convert natural language search queries into Java regex patterns that will be applied to JSON strings.
            
            The JSON objects have this structure:
            {
              "id" : 1,
              "created" : "2025-01-01T08:00:00Z",
              "updated" : "2025-01-01T08:00:00Z",
              "name" : "Orion"
            }
            
            IMPORTANT RULES:
            1. Generate case-insensitive regex patterns using (?i) flag
            2. For simple text searches, match the text anywhere in the JSON
            3. For field-specific searches (e.g., "name contains project"), target the specific field value
            4. For date searches, work with the ISO date format in the JSON (ends with Z, not timezone offset)
            5. Return ONLY the regex pattern, no explanations, no quotes, no additional text
            6. Use proper escaping for special regex characters
            7. Current year is %d if year context is needed
            8. For full year searches like "updated in 2025", match the entire year (months 01-12)
            
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
            Output: (?i).*"updated"\\s*:\\s*"2025-(0[1-9]|1[0-2])-.*
            
            Now generate a regex pattern for this query:
            "%s"
            """;
    private static final Logger     logger              = LoggerFactory.getLogger(NaturalLanguageSearchService.class);
    private final        ChatClient chatModel;

    public NaturalLanguageSearchService(
            ChatClient.Builder builder) {

        this.chatModel = builder.build();
    }

    /**
     * Parses a natural language search query using LLM with regex fallback.
     *
     * @param query The natural language query from the user
     * @return Regex pattern string for filtering JSON objects
     */
    public String parseQuery(String query) {
        if (query == null || query.trim().isEmpty()) {
            return "";
        }

        System.out.println("Parsing natural language query: '" + query + "'");

        // Try LLM parsing
        try {
            String llmResult = parseWithLLM(query);
            if (llmResult != null && !llmResult.trim().isEmpty()) {
//                logger.debug("Successfully parsed with LLM: {}", llmResult);
                return llmResult;
            }
        } catch (Exception e) {
            logger.warn("LLM parsing failed, falling back to simple search: {}", e.getMessage());
        }

        // Fallback to simple case-insensitive search pattern
        return String.format("(?i).*%s.*", query.replaceAll("([\\\\\\[\\]{}()*+?.^$|])", "\\\\$1"));
    }

    /**
     * Parse query using offline LLM via Spring AI
     */
    private String parseWithLLM(String query) {
        try {
            // Create prompt with current year context
            int    currentYear     = LocalDate.now().getYear();
            String formattedPrompt = String.format(LLM_PROMPT_TEMPLATE, currentYear, query);

            // Create prompt and get response using Spring AI 1.0.1 API
            Prompt prompt = new Prompt(formattedPrompt);

            System.out.println("LLM prompt: '" + formattedPrompt + "'");
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
}
