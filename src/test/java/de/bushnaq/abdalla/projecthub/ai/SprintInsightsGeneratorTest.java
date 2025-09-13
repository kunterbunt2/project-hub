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

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * Test class for SprintInsightsGenerator.
 * Demonstrates how to use the AI-powered sprint analysis functionality.
 */
@SpringBootTest
public class SprintInsightsGeneratorTest {

    /**
     * Sample sprint data for testing
     */
    private static final String SAMPLE_SPRINT_DATA = """
            [
              {
                "created" : "2025-01-01T08:00:00+01:00",
                "updated" : "2025-07-04T14:36:34.2635+02:00",
                "end" : "2024-12-27T11:30:00",
                "featureId" : 2,
                "id" : 4,
                "name" : "paris",
                "originalEstimation" : "4d 3h",
                "releaseDate" : "2025-01-02T08:00:00",
                "remaining" : "49m 46s",
                "start" : "2024-12-06T08:00:00",
                "status" : "STARTED",
                "worked" : "4d 2h 10m 14s"
              },
              {
                "created" : "2025-01-01T08:00:00+01:00",
                "updated" : "2025-06-15T10:22:11.1234+02:00",
                "end" : "2024-11-15T17:00:00",
                "featureId" : 1,
                "id" : 2,
                "name" : "london",
                "originalEstimation" : "2d 5h",
                "releaseDate" : "2024-11-20T08:00:00",
                "remaining" : "0h",
                "start" : "2024-11-01T08:00:00",
                "status" : "COMPLETED",
                "worked" : "2d 6h 30m"
              },
              {
                "created" : "2025-01-01T08:00:00+01:00",
                "updated" : "2025-05-20T16:45:22.5678+02:00",
                "end" : "2024-10-31T17:00:00",
                "featureId" : 3,
                "id" : 1,
                "name" : "tokyo",
                "originalEstimation" : "1d 2h",
                "releaseDate" : "2024-11-05T08:00:00",
                "remaining" : "0h",
                "start" : "2024-10-25T08:00:00",
                "status" : "COMPLETED",
                "worked" : "1d 4h 15m"
              },
              {
                "created" : "2025-01-01T08:00:00+01:00",
                "updated" : "2025-08-10T09:15:44.9876+02:00",
                "end" : "2025-02-28T17:00:00",
                "featureId" : 4,
                "id" : 5,
                "name" : "newyork",
                "originalEstimation" : "6d 1h",
                "releaseDate" : "2025-03-05T08:00:00",
                "remaining" : "5d 12h 30m",
                "start" : "2025-02-01T08:00:00",
                "status" : "NOT_STARTED",
                "worked" : "0h"
              }
            ]
            """;
    @Autowired
    private SprintInsightsGenerator sprintInsightsGenerator;

    @Test
    public void testEstimationAccuracyAnalysis() {
        System.out.println("\n=== Testing Estimation Accuracy Analysis ===");

        try {
            String question = "How accurate are our sprint estimations? Are we consistently over or under-estimating?";
            String analysis = sprintInsightsGenerator.generateFocusedInsights(SAMPLE_SPRINT_DATA, question);

            System.out.println("Question: " + question);
            System.out.println("Analysis:");
            System.out.println(analysis);

        } catch (Exception e) {
            System.err.println("Error generating estimation analysis: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @Test
    public void testGenerateFocusedInsights() {
        System.out.println("\n=== Testing Focused Sprint Insights ===");

        try {
            String question        = "Which sprints are at risk of missing their deadlines and why?";
            String focusedInsights = sprintInsightsGenerator.generateFocusedInsights(SAMPLE_SPRINT_DATA, question);

            System.out.println("Question: " + question);
            System.out.println("Focused Insights:");
            System.out.println(focusedInsights);

            // Basic validation
            assert focusedInsights != null && !focusedInsights.trim().isEmpty() : "Focused insights should not be empty";

        } catch (Exception e) {
            System.err.println("Error generating focused insights: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @Test
    public void testGenerateInsights() {
        System.out.println("=== Testing General Sprint Insights ===");

        try {
            String insights = sprintInsightsGenerator.generateInsights(SAMPLE_SPRINT_DATA);
            System.out.println("Generated Insights:");
            System.out.println(insights);

            // Basic validation
            assert insights != null && !insights.trim().isEmpty() : "Insights should not be empty";

        } catch (Exception e) {
            System.err.println("Error generating insights: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @Test
    public void testGenerateQuickSummary() {
        System.out.println("\n=== Testing Quick Sprint Summary ===");

        try {
            String summary = sprintInsightsGenerator.generateQuickSummary(SAMPLE_SPRINT_DATA);

            System.out.println("Quick Summary:");
            System.out.println(summary);

            // Basic validation
            assert summary != null && !summary.trim().isEmpty() : "Summary should not be empty";

        } catch (Exception e) {
            System.err.println("Error generating quick summary: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @Test
    public void testWorkloadPatternsAnalysis() {
        System.out.println("\n=== Testing Workload Patterns Analysis ===");

        try {
            String question = "What patterns can you identify in our sprint workload and team velocity?";
            String analysis = sprintInsightsGenerator.generateFocusedInsights(SAMPLE_SPRINT_DATA, question);

            System.out.println("Question: " + question);
            System.out.println("Analysis:");
            System.out.println(analysis);

        } catch (Exception e) {
            System.err.println("Error generating workload analysis: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
