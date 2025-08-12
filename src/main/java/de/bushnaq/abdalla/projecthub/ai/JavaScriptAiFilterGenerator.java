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
 * AI filter generator for JavaScript functions.
 * Converts natural language queries into JavaScript filter functions.
 */
@Component
public class JavaScriptAiFilterGenerator implements AiFilterGenerator {
    private static final String ANSI_BLUE   = "\u001B[36m";
    private static final String ANSI_GRAY   = "\u001B[37m";
    private static final String ANSI_GREEN  = "\u001B[32m";
    private static final String ANSI_RED    = "\u001B[31m";
    private static final String ANSI_RESET  = "\u001B[0m";    // Declaring ANSI_RESET so that we can reset the color
    private static final String ANSI_YELLOW = "\u001B[33m";

    private static final String     JAVASCRIPT_PROMPT_TEMPLATE = """
            You are a JavaScript function generator for filtering Java objects via GraalJS. Convert natural language search queries into JavaScript filter functions.
            
            IMPORTANT CONTEXT: You are filtering %s entities. Each 'entity' parameter passed to your function is a Java %s object with host access enabled.
            
            The Java class you'll be filtering has this structure:
            %s
            
            %s
            
            IMPORTANT RULES:
            1. Generate a JavaScript function that takes a Java object parameter called 'entity'
            2. The function should return true if the entity matches the search criteria, false otherwise
            3. Use case-insensitive string comparisons when appropriate (use toLowerCase() on strings)
            4. CRITICAL: The 'entity' is a Java object, NOT a JavaScript object - call Java getter methods like entity.getName(), entity.getCreated(), entity.getUpdated()
            5. For date comparisons, use Java date methods directly - DO NOT convert to JavaScript Date objects
            6. Use Java date methods: entity.getCreated().getYear(), entity.getCreated().isAfter(), entity.getCreated().isBefore()
            7. To create Java date objects, use: Java.type('java.time.OffsetDateTime').of(year, month, day, hour, minute, second, nano, offset)
            8. For current year: Java.type('java.time.Year').now().getValue()
            9. Return ONLY the JavaScript function body, no function declaration, no explanations. The returned answer must be a valid JavaScript code.
            10. Handle null values gracefully by checking if entity and its methods return non-null values
            11. Current year is %d if year context is needed
            12. NEVER use new Date() - always use Java time methods directly
            13. NEVER access properties directly like entity.name - always use getter methods like entity.getName()
            
            %s
            
            Now generate a JavaScript function body for this EXACT query:
            "%s"
            """;
    private static final Logger     logger                     = LoggerFactory.getLogger(JavaScriptAiFilterGenerator.class);
    private final        ChatClient chatModel;

    public JavaScriptAiFilterGenerator(ChatClient.Builder builder) {
        this.chatModel = builder.build();
    }

    /**
     * Extract the actual answer from AI response by removing thinking process.
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

        logger.info("Generating JavaScript filter for query: '{}' and entity type: '{}'", query, entityType);

        try {
            AiFilterConfig.PromptConfig config = AiFilterConfig.getPromptConfig(entityType);

            // Create prompt with current year context and entity-specific configuration
            int currentYear = LocalDate.now().getYear();
            String formattedPrompt = String.format(JAVASCRIPT_PROMPT_TEMPLATE,
                    entityType,           // You are filtering %s entities
                    entityType,           // Each 'entity' parameter passed to your function is already a %s object
                    config.javaClass, // The JavaScript objects you'll be filtering have properties matching this JSON structure:
                    config.specialConsiderations,
                    currentYear,          // Current year is %d if year context is needed
                    config.javascriptExamples,
                    query);

            // Create prompt and get response using Spring AI
            Prompt prompt = new Prompt(formattedPrompt);

            System.out.printf("JavaScript LLM prompt for '%s%s%s'\n%s%s%s\n\n", ANSI_BLUE, entityType, ANSI_RESET, ANSI_GREEN, formattedPrompt, ANSI_RESET);
//            logger.debug("JavaScript LLM prompt for '{}': {}", entityType, formattedPrompt);

            ChatClient.ChatClientRequestSpec request  = chatModel.prompt(prompt);
            ChatClient.CallResponseSpec      response = request.call();
            String                           content  = response.content();

            System.out.printf("JavaScript LLM raw response\n\n%s%s%s\n\n", ANSI_YELLOW, content, ANSI_RESET);
//            logger.debug("JavaScript LLM raw response: {}", content);

            // Extract actual answer from response (remove thinking process)
            String extractedAnswer = extractAnswerFromResponse(content);

            System.out.printf("JavaScript LLM extracted answer\n\n%s%s%s\n\n", ANSI_YELLOW, extractedAnswer, ANSI_RESET);
//            logger.debug("JavaScript LLM extracted answer: {}", extractedAnswer);

            if (extractedAnswer == null || extractedAnswer.trim().isEmpty()) {
                logger.warn("JavaScript generation failed, result is empty");
                throw new RuntimeException("JavaScript generation failed, result is empty");
            }

            return extractedAnswer.trim();

        } catch (Exception e) {
            logger.error("Error generating JavaScript filter: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to generate JavaScript filter: " + e.getMessage(), e);
        }
    }

    @Override
    public FilterType getFilterType() {
        return FilterType.JAVASCRIPT;
    }
}
