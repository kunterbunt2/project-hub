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

/**
 * AI-powered insights generator for sprint data analysis.
 * Analyzes JSON lists of sprints and provides AI-generated insights about patterns, trends, and recommendations.
 */
@Component
public class SprintInsightsGenerator {
    private static final String ANSI_BLUE   = "\u001B[36m";
    private static final String ANSI_GREEN  = "\u001B[32m";
    private static final String ANSI_RESET  = "\u001B[0m";
    private static final String ANSI_YELLOW = "\u001B[33m";

    private static final String SPRINT_INSIGHTS_PROMPT_TEMPLATE = """
            You are a project management and sprint analysis expert. Analyze the provided JSON list of sprints and provide actionable insights.
            
            SPRINT DATA STRUCTURE:
            Each sprint has the following structure:
            {
              "created" : "2025-01-01T08:00:00+01:00",      // When the sprint was created
              "updated" : "2025-07-04T14:36:34.2635+02:00", // Last update timestamp
              "end" : "2024-12-27T11:30:00",                // Sprint end date/time
              "featureId" : 2,                               // Associated feature ID
              "id" : 4,                                      // Unique sprint ID
              "name" : "paris",                              // Sprint name
              "originalEstimation" : "4d 3h",                // Original time estimation
              "releaseDate" : "2025-01-02T08:00:00",         // Planned release date
              "remaining" : "49m 46s",                       // Time remaining
              "start" : "2024-12-06T08:00:00",               // Sprint start date/time
              "status" : "STARTED",                          // Current status
              "worked" : "4d 2h 10m 14s"                     // Time already worked
            }
            
            ANALYSIS FOCUS AREAS:
            1. **Sprint Performance**: Compare original estimations vs. actual work + remaining time
            2. **Timeline Analysis**: Examine sprint durations, delays, and schedule adherence
            3. **Status Distribution**: Analyze the current state of sprints
            4. **Estimation Accuracy**: How accurate are the original estimations?
            5. **Workload Patterns**: Identify trends in sprint sizing and completion
            6. **Risk Assessment**: Identify sprints that may be at risk of delay or overrun
            7. **Recommendations**: Provide actionable suggestions for improvement
            
            RESPONSE FORMAT:
            Provide your analysis in the following structured format:
            
            ## Sprint Analysis Summary
            
            ### Key Metrics
            - Total sprints analyzed: [number]
            - Status breakdown: [percentages/counts]
            - Average sprint duration: [calculation]
            - Estimation accuracy: [analysis]
            
            ### Performance Insights
            [Detailed analysis of sprint performance]
            
            ### Timeline Analysis
            [Analysis of scheduling and timing patterns]
            
            ### Risk Assessment
            [Identification of potential risks and problematic sprints]
            
            ### Recommendations
            [Actionable suggestions for improvement]
            
            Now analyze this sprint data:
            %s
            """;

    private static final Logger     logger = LoggerFactory.getLogger(SprintInsightsGenerator.class);
    private final        ChatClient chatModel;

    public SprintInsightsGenerator(ChatClient.Builder builder) {
        this.chatModel = builder.build();
    }

    /**
     * Analyzes sprint data and generates insights focused on a specific question or area.
     *
     * @param sprintJsonData The JSON string containing an array of sprint objects
     * @param focusQuestion  Specific question or area to focus the analysis on
     * @return A targeted analysis report answering the specific question
     * @throws RuntimeException if analysis fails
     */
    public String generateFocusedInsights(String sprintJsonData, String focusQuestion) {
        if (sprintJsonData == null || sprintJsonData.trim().isEmpty()) {
            return "No sprint data provided for analysis.";
        }

        if (focusQuestion == null || focusQuestion.trim().isEmpty()) {
            return generateInsights(sprintJsonData);
        }

        logger.info("Generating focused AI insights for sprint data with question: '{}'", focusQuestion);

        try {
            // Create a focused prompt that includes the specific question
            String focusedPrompt = SPRINT_INSIGHTS_PROMPT_TEMPLATE.replace(
                    "Now analyze this sprint data:",
                    "Focus your analysis on this specific question: \"" + focusQuestion + "\"\n\nNow analyze this sprint data:"
            );

            String formattedPrompt = String.format(focusedPrompt, sprintJsonData);

            // Create prompt and get response using Spring AI
            Prompt prompt = new Prompt(formattedPrompt);

            System.out.printf("Focused Sprint Insights LLM prompt for '%s%s%s':\n%s%s%s\n\n",
                    ANSI_BLUE, focusQuestion, ANSI_RESET, ANSI_GREEN, formattedPrompt, ANSI_RESET);
            logger.debug("Focused Sprint Insights LLM prompt for '{}': {}", focusQuestion, formattedPrompt);

            ChatClient.ChatClientRequestSpec request  = chatModel.prompt(prompt);
            ChatClient.CallResponseSpec      response = request.call();
            String                           content  = response.content();

            System.out.printf("Focused Sprint Insights LLM response:\n\n%s%s%s\n\n", ANSI_YELLOW, content, ANSI_RESET);
            logger.debug("Focused Sprint Insights LLM response: {}", content);

            if (content == null || content.trim().isEmpty()) {
                logger.warn("Focused sprint insights generation failed, result is empty");
                return "Failed to generate focused insights - empty response from AI model.";
            }

            return content.trim();

        } catch (Exception e) {
            logger.error("Error generating focused sprint insights: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to generate focused sprint insights: " + e.getMessage(), e);
        }
    }

    /**
     * Analyzes a JSON list of sprints and generates AI-powered insights.
     *
     * @param sprintJsonData The JSON string containing an array of sprint objects
     * @return A formatted analysis report with insights and recommendations
     * @throws RuntimeException if analysis fails
     */
    public String generateInsights(String sprintJsonData) {
        if (sprintJsonData == null || sprintJsonData.trim().isEmpty()) {
            return "No sprint data provided for analysis.";
        }

        logger.info("Generating AI insights for sprint data");

        try {
            // Create prompt with the sprint data
            String formattedPrompt = String.format(SPRINT_INSIGHTS_PROMPT_TEMPLATE, sprintJsonData);

            // Create prompt and get response using Spring AI
            Prompt prompt = new Prompt(formattedPrompt);

            System.out.printf("Sprint Insights LLM prompt:\n%s%s%s\n\n", ANSI_GREEN, formattedPrompt, ANSI_RESET);
            logger.debug("Sprint Insights LLM prompt: {}", formattedPrompt);

            ChatClient.ChatClientRequestSpec request  = chatModel.prompt(prompt);
            ChatClient.CallResponseSpec      response = request.call();
            String                           content  = response.content();

            System.out.printf("Sprint Insights LLM response:\n\n%s%s%s\n\n", ANSI_YELLOW, content, ANSI_RESET);
            logger.debug("Sprint Insights LLM response: {}", content);

            if (content == null || content.trim().isEmpty()) {
                logger.warn("Sprint insights generation failed, result is empty");
                return "Failed to generate insights - empty response from AI model.";
            }

            return content.trim();

        } catch (Exception e) {
            logger.error("Error generating sprint insights: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to generate sprint insights: " + e.getMessage(), e);
        }
    }

    /**
     * Generates a quick summary of key metrics from the sprint data.
     *
     * @param sprintJsonData The JSON string containing an array of sprint objects
     * @return A concise summary of key sprint metrics
     * @throws RuntimeException if analysis fails
     */
    public String generateQuickSummary(String sprintJsonData) {
        return generateFocusedInsights(sprintJsonData,
                "Provide only a brief summary with key metrics: total sprints, status distribution, " +
                        "and top 2 most important insights. Keep it under 200 words.");
    }
}
