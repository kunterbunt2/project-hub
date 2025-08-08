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
import org.springframework.stereotype.Component;

import java.time.LocalDate;

/**
 * AI filter generator for regex patterns.
 * Converts natural language queries into Java regex patterns for filtering JSON objects.
 */
@Component
public class RegexAiFilterGenerator implements AiFilterGenerator {

    private static final String REGEX_PROMPT_TEMPLATE = """
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
    private static final Logger logger = LoggerFactory.getLogger(RegexAiFilterGenerator.class);
    private final ChatClient chatModel;

    public RegexAiFilterGenerator(ChatClient.Builder builder) {
        this.chatModel = builder.build();
    }

    /**
     * Extract the actual answer from AI response by removing thinking process.
     * Some AI models include reasoning in tags or similar patterns.
     */
    private String extractAnswerFromResponse(String rawResponse) {
        if (rawResponse == null || rawResponse.trim().isEmpty()) {
            return rawResponse;
        }

        String response = rawResponse.trim();

        // Remove content between thinking tags
        response = response.replaceAll("(?s)<think>.*?</think>", "").trim();
        response = response.replaceAll("(?s)<thinking>.*?</thinking>", "").trim();
        response = response.replaceAll("(?s)<!--\\s*thinking.*?-->", "").trim();

        // Remove lines that start with reasoning markers
        response = response.replaceAll("(?m)^(Thinking:|Let me think:).*$", "").trim();

        // Extract content after answer markers
        if (response.matches("(?s).*\\b(Answer|Result|Output):\\s*(.*)")) {
            String[] parts = response.split("\\b(?:Answer|Result|Output):\\s*", 2);
            if (parts.length > 1) {
                response = parts[1].trim();
            }
        }

        return response.isEmpty() ? rawResponse : response;
    }

    @Override
    public String generateFilter(String query, String entityType) {
        if (query == null || query.trim().isEmpty()) {
            return "";
        }

        logger.info("Generating regex filter for query: '{}' and entity type: '{}'", query, entityType);

        try {
            AiFilterConfig.PromptConfig config = AiFilterConfig.getPromptConfig(entityType);

            // Create prompt with current year context and entity-specific configuration
            int currentYear = LocalDate.now().getYear();
            String formattedPrompt = String.format(REGEX_PROMPT_TEMPLATE,
                    config.jsonStructure,
                    config.specialConsiderations,
                    currentYear,
                    config.regexExamples,
                    query);

            // Create prompt and get response using Spring AI
            Prompt prompt = new Prompt(formattedPrompt);

            logger.debug("Regex LLM prompt for '{}': {}", entityType, formattedPrompt);

            ChatClient.ChatClientRequestSpec request  = chatModel.prompt(prompt);
            ChatClient.CallResponseSpec      response = request.call();
            String                           content  = response.content();

            logger.debug("Regex LLM raw response: {}", content);

            // Extract actual answer from response (remove thinking process)
            String extractedAnswer = extractAnswerFromResponse(content);

            logger.debug("Regex LLM extracted answer: {}", extractedAnswer);

            if (extractedAnswer == null || extractedAnswer.trim().isEmpty()) {
                logger.warn("Regex generation failed, result is empty");
                throw new RuntimeException("Regex generation failed, result is empty");
            }

            return extractedAnswer.trim();

        } catch (Exception e) {
            logger.error("Error generating regex filter: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to generate regex filter: " + e.getMessage(), e);
        }
    }

    @Override
    public FilterType getFilterType() {
        return FilterType.REGEX;
    }
}
