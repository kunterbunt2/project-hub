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
import java.util.function.Predicate;

/**
 * AI filter generator for Java predicates.
 * Converts natural language queries into Java code that gets compiled and executed on the fly.
 */
@Component
public class JavaAiFilterGenerator implements AiFilterGenerator {
    private static final String ANSI_BLUE   = "\u001B[36m";
    private static final String ANSI_GRAY   = "\u001B[37m";
    private static final String ANSI_GREEN  = "\u001B[32m";
    private static final String ANSI_RED    = "\u001B[31m";
    private static final String ANSI_RESET  = "\u001B[0m";    // Declaring ANSI_RESET so that we can reset the color
    private static final String ANSI_YELLOW = "\u001B[33m";

    private static final String             JAVA_PROMPT_TEMPLATE = """
            You are a Java method body generator for filtering Java objects. Convert natural language search queries into Java code that can be compiled and executed.
            
            IMPORTANT CONTEXT: You are filtering %s entities. The 'entity' parameter passed to your method is a %s object.
            
            The Java class you'll be filtering has this structure:
            %s
            
            %s
            
            IMPORTANT RULES:
            1. Generate ONLY the method body code that goes inside a boolean test(%s entity) method.
            2. The method should return true if the entity matches the search criteria, false otherwise.
            3. You can access fields directly using entity.getFieldName() getter methods.
            4. For date comparisons, use OffsetDateTime/LocalDateTime classes.
            5. Handle exceptions gracefully - return false if any operation fails.
            6. Current year is %d if year context is needed.
            7. Use proper Java operators and control structures.
            8. For string searches, use direct getter access with .toLowerCase().contains().
            9. The entity parameter is already typed as %s, so you can access its getter methods directly.
            10. Never use reflection - only use public getter methods like entity.getName(), entity.getCreated(), etc.
            11. Return ONLY the method body code, no method signature, no class declaration. The code must be compilable as is.
            12. Any explanation/note can be added as java code comment using //, but the answer must be compilable java code.
            13. Do not add ` or " before or after your answer, as it would make the code not compilable.
            14. Explain your thought process within <think></think> tags to indicate your thinking process.
            15. Any line starting with 'Note:' must be commented out using Java comment //Note:.
            
            %s
            
            Now generate a Java method body for this EXACT query:
            "%s"
            """;
    private static final Logger             logger               = LoggerFactory.getLogger(JavaAiFilterGenerator.class);
    private final        ChatClient         chatModel;
    private final        JavaFilterCompiler javaFilterCompiler;

    public JavaAiFilterGenerator(ChatClient.Builder builder, JavaFilterCompiler javaFilterCompiler) {
        this.chatModel          = builder.build();
        this.javaFilterCompiler = javaFilterCompiler;
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

        logger.info("Generating Java filter for query: '{}' and entity type: '{}'", query, entityType);

        try {
            AiFilterConfig.PromptConfig config = AiFilterConfig.getPromptConfig(entityType);

            // Create prompt with current year context and entity-specific configuration
            int currentYear = LocalDate.now().getYear();
            String formattedPrompt = String.format(JAVA_PROMPT_TEMPLATE,
                    entityType,           // You are filtering %s entities
                    entityType,           // The 'entity' parameter passed to your method is already a %s object
                    config.javaClass,     // The Java class you'll be filtering has this structure:
                    config.specialConsiderations,
                    entityType,           // Generate ONLY the method body code that goes inside a boolean test(%s entity) method
                    currentYear,          // Current year is %d if year context is needed
                    entityType,           // The entity parameter is already typed as %s, so you can access its methods and fields directly
                    config.javaExamples,
                    query);

            // Create prompt and get response using Spring AI
            Prompt prompt = new Prompt(formattedPrompt);

            System.out.printf("Java LLM prompt for '%s%s%s'\n%s%s%s\n\n", ANSI_BLUE, entityType, ANSI_RESET, ANSI_GREEN, formattedPrompt, ANSI_RESET);
//            logger.debug("Java LLM prompt for '{}': {}", entityType, formattedPrompt);

            ChatClient.ChatClientRequestSpec request  = chatModel.prompt(prompt);
            ChatClient.CallResponseSpec      response = request.call();
            String                           content  = response.content();

            System.out.printf("Java LLM raw response\n\n%s%s%s\n\n", ANSI_YELLOW, content, ANSI_RESET);
//            logger.debug("Java LLM raw response: {}", content);

            // Extract actual answer from response (remove thinking process)
            String extractedAnswer = extractAnswerFromResponse(content);

            System.out.printf("Java LLM extracted answer\n\n%s%s%s\n\n", ANSI_YELLOW, extractedAnswer, ANSI_RESET);
//            logger.debug("Java LLM extracted answer: {}", extractedAnswer);

            if (extractedAnswer == null || extractedAnswer.trim().isEmpty()) {
                logger.warn("Java code generation failed, result is empty");
                throw new RuntimeException("Java code generation failed, result is empty");
            }

            return extractedAnswer.trim();

        } catch (Exception e) {
            logger.error("Error generating Java filter: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to generate Java filter: " + e.getMessage(), e);
        }
    }

    /**
     * Generates a Java filter and compiles it into a Predicate.
     *
     * @param query      The natural language query from the user
     * @param entityType The type of entity being searched (e.g., "Product", "Version")
     * @param <T>        The entity type
     * @return A compiled Predicate that can be used to filter entities
     * @throws RuntimeException if compilation fails
     */
    public <T> Predicate<T> generatePredicate(String query, String entityType) {
        int retries = 10;
        do {
            if (query == null || query.trim().isEmpty()) {
                return entity -> true; // Return a predicate that matches everything
            }

            logger.info("Generating Java Predicate for query: '{}' and entity type: '{}'", query, entityType);

            try {
                // Generate Java code using LLM
                String javaCode = generateFilter(query, entityType);

//                logger.debug("Generated Java code: {}", javaCode);

                // Compile the Java code and return the Predicate
                return javaFilterCompiler.compileFilter(javaCode, entityType);

            } catch (Exception e) {

                logger.error("Failed to generate Java Predicate: {}", e.getMessage(), e);
//                throw new RuntimeException("Failed to generate Java Predicate: " + e.getMessage(), e);
            }
        }
        while (--retries > 0);
        return null;
    }

    @Override
    public FilterType getFilterType() {
        return FilterType.JAVA;
    }
}
