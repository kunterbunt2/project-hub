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

import java.util.HashMap;
import java.util.Map;

/**
 * Shared configuration for entity-specific prompts across all AI filter implementations.
 */
public class AiFilterConfig {

    private static final Map<String, PromptConfig> PROMPT_CONFIGS = initializePromptConfigs();

    /**
     * Gets the prompt configuration for the specified entity type.
     *
     * @param entityType The entity type (e.g., "Product", "Version")
     * @return The prompt configuration, or Product config as fallback
     */
    public static PromptConfig getPromptConfig(String entityType) {
        return PROMPT_CONFIGS.getOrDefault(entityType, PROMPT_CONFIGS.get("Product"));
    }

    /**
     * Initialize prompt configurations for different entity types
     */
    private static Map<String, PromptConfig> initializePromptConfigs() {
        Map<String, PromptConfig> configs = new HashMap<>();

        // Product configuration with all three filter types
        configs.put("Product", new PromptConfig(
                """
                        @Getter
                        @Setter
                        public class Product {
                            private String name;//never null
                            private OffsetDateTime created;//never null
                            private OffsetDateTime updated;//never null
                        }
                        """,
                """
                        Special considerations for Products:
                        - Remember: you are filtering Product entities, so each 'entity' is a Product.
                        - When queries mention "products created in 2024" - this means filter by creation year.
                        - Terms like "products", "items", or similar generic terms refer to the entity type, not name content.
                        - Use only getter methods like entity.getName(), entity.getCreated(), entity.getUpdated().
                        """,
                """
                        Examples:
                        Input: "Orion"
                        Output: return entity && entity.getName() && entity.getName().toLowerCase().includes('orion');
                        
                        Input: "name contains project"
                        Output: return entity && entity.getName() && entity.getName().toLowerCase().includes('project');
                        
                        Input: "created in 2024"
                        Output: return entity && entity.getCreated() && entity.getCreated().getYear() === 2024;
                        
                        Input: "products created in 2024"
                        Output: return entity && entity.getCreated() && entity.getCreated().getYear() === 2024;
                        
                        Input: "items created in 2024"
                        Output: return entity && entity.getCreated() && entity.getCreated().getYear() === 2024;
                        
                        Input: "products created after January 2024"
                        Output: if (!entity || !entity.getCreated()) return false; const refDate = Java.type('java.time.OffsetDateTime').of(2024, 1, 31, 23, 59, 59, 0, entity.getCreated().getOffset()); return entity.getCreated().isAfter(refDate);
                        
                        Input: "items created before December 2024"
                        Output: if (!entity || !entity.getCreated()) return false; const refDate = Java.type('java.time.OffsetDateTime').of(2024, 12, 1, 0, 0, 0, 0, entity.getCreated().getOffset()); return entity.getCreated().isBefore(refDate);
                        
                        Input: "products created this year"
                        Output: if (!entity || !entity.getCreated()) return false; const currentYear = Java.type('java.time.Year').now().getValue(); return entity.getCreated().getYear() === currentYear;
                        
                        Input: "updated in 2024"
                        Output: return entity && entity.getUpdated() && entity.getUpdated().getYear() === 2024;
                        
                        Input: "products updated in 2024"
                        Output: return entity && entity.getUpdated() && entity.getUpdated().getYear() === 2024;
                        
                        Input: "items updated in 2024"
                        Output: return entity && entity.getUpdated() && entity.getUpdated().getYear() === 2024;
                        
                        Input: "products updated after January 2024"
                        Output: if (!entity || !entity.getUpdated()) return false; const refDate = Java.type('java.time.OffsetDateTime').of(2024, 1, 31, 23, 59, 59, 0, entity.getUpdated().getOffset()); return entity.getUpdated().isAfter(refDate);
                        
                        Input: "items updated before December 2024"
                        Output: if (!entity || !entity.getUpdated()) return false; const refDate = Java.type('java.time.OffsetDateTime').of(2024, 12, 1, 0, 0, 0, 0, entity.getUpdated().getOffset()); return entity.getUpdated().isBefore(refDate);
                        
                        Input: "products updated this year"
                        Output: if (!entity || !entity.getUpdated()) return false; const currentYear = Java.type('java.time.Year').now().getValue(); return entity.getUpdated().getYear() === currentYear;
                        
                        Input: "MARS"
                        Output: return entity && entity.getName() && entity.getName().toLowerCase().includes('mars');
                        
                        Input: "space products created in 2024"
                        Output: if (!entity || !entity.getName() || !entity.getCreated()) return false; const hasSpace = entity.getName().toLowerCase().includes('space'); const isCreated2024 = entity.getCreated().getYear() === 2024; return hasSpace && isCreated2024;""",
                """
                        Examples:
                        Input: "Orion"
                        Output: return entity.getName().toLowerCase().contains("orion");
                        
                        Input: "name contains project"
                        Output: return entity.getName().toLowerCase().contains("project");
                        
                        
                        Input: "created in 2024"
                        Output: return entity.getCreated().getYear() == 2024;
                        
                        Input: "products created in 2024"
                        Output: return entity.getCreated().getYear() == 2024;
                        
                        Input: "items created in 2024"
                        Output: return entity.getCreated().getYear() == 2024;
                        
                        Input: "products created after January 2024"
                        Output: return entity.getCreated().isAfter(OffsetDateTime.of(2024, 1, 31, 23, 59, 59, 0, OffsetDateTime.now().getOffset()));
                        
                        Input: "items created before December 2024"
                        Output: return entity.getCreated().isBefore(OffsetDateTime.of(2024, 12, 1, 0, 0, 0, 0, OffsetDateTime.now().getOffset()));
                        
                        Input: "products created this year"
                        Output: return entity.getCreated().getYear() == java.time.Year.now().getValue();
                        
                        
                        Input: "updated in 2024"
                        Output: return entity.getUpdated().getYear() == 2024;
                        
                        Input: "products updated in 2024"
                        Output: return entity.getUpdated().getYear() == 2024;
                        
                        Input: "items updated in 2024"
                        Output: return entity.getUpdated().getYear() == 2024;
                        
                        Input: "products updated after January 2024"
                        Output: return entity.getUpdated().isAfter(OffsetDateTime.of(2024, 1, 31, 23, 59, 59, 0, OffsetDateTime.now().getOffset()));
                        
                        Input: "items updated before December 2024"
                        Output: return entity.getUpdated().isBefore(OffsetDateTime.of(2024, 12, 1, 0, 0, 0, 0, OffsetDateTime.now().getOffset()));
                        
                        Input: "products updated this year"
                        Output: return entity.getUpdated().getYear() == java.time.Year.now().getValue();
                        
                        """

        ));

        // Version configuration with all three filter types
        configs.put("Version", new PromptConfig(
                """
                        @Getter
                        @Setter
                        public class Version {
                            private String name;//never null
                            private OffsetDateTime created;//never null
                            private OffsetDateTime updated;//never null
                        }
                        """,
                """
                        Special considerations for Versions:
                        - Focus on version names (semantic versions like "1.0.0", "2.1.5") and creation/update dates
                        - Remember: you are filtering Version entities, so each 'entity' is already a Version
                        - When queries mention "versions created in 2024" - this means filter by creation year, NOT by checking if entity.name contains "versions"
                        - Terms like "versions", "releases", or similar generic terms refer to the entity type, not name content
                        - Version names often follow semantic versioning (major.minor.patch) or include suffixes like "-beta", "-alpha"
                        - Use only getter methods like entity.getName(), entity.getCreated(), entity.getUpdated(), entity.getId(), entity.getProductId()
                        - Never use reflection or field access, always use public getter methods
                        
                        VERSION NUMBER COMPARISONS:
                        - Support version comparisons (greater than, less than, equal, between) by comparing semantic version numbers numerically
                        - Version format: MAJOR.MINOR.PATCH (e.g., "3.1.4", "1.5.6", "2.0.0")
                        - To compare versions numerically, convert each version to a comparable number using positional weighting:
                          * Split version by dots: "3.1.4" → [3, 1, 4]
                          * Calculate weighted value: major*10000 + minor*100 + patch*1
                          * Example: "3.1.4" → 3*10000 + 1*100 + 4*1 = 30104
                          * Example: "1.5.6" → 1*10000 + 5*100 + 6*1 = 10506
                          * Therefore: 30104 > 10506, so "3.1.4" > "1.5.6"
                        - For versions with suffixes like "-beta", "-alpha", treat them as lower than their base version
                        - Handle missing parts by treating them as 0: "2.1" = "2.1.0"
                        - Examples of comparisons:
                          * "2.0.0" > "1.9.9" (20000 > 10909)
                          * "1.10.0" > "1.9.0" (11000 > 10900) 
                          * "3.0.0" > "2.99.99" (30000 > 29999)
                          * "1.0.0-beta" < "1.0.0" (pre-release is lower)""",
                """
                        Examples:
                        Input: "1.0.0"
                        Output: return entity.getName().toLowerCase().contains("1.0.0");
                        
                        Input: "name contains beta"
                        Output: return entity.getName().toLowerCase().contains("beta");
                        
                        Input: "created in 2024"
                        Output: return entity.getCreated().getYear() == 2024;
                        
                        Input: "versions created in 2024"
                        Output: return entity.getCreated().getYear() == 2024;
                        
                        Input: "releases created in 2024"
                        Output: return entity.getCreated().getYear() == 2024;
                        
                        Input: "versions created after January 2024"
                        Output: return entity.getCreated().isAfter(OffsetDateTime.of(2024, 1, 31, 23, 59, 59, 0, OffsetDateTime.now().getOffset()));
                        
                        Input: "releases created before December 2024"
                        Output: return entity.getCreated().isBefore(OffsetDateTime.of(2024, 12, 1, 0, 0, 0, 0, OffsetDateTime.now().getOffset()));
                        
                        Input: "versions updated in 2025"
                        Output: return entity.getUpdated().getYear() == 2025;
                        
                        Input: "versions greater than 2.0.0"
                        Output: String[] parts = entity.getName().split("\\."); if (parts.length < 3) return false; try { int major = Integer.parseInt(parts[0]); int minor = Integer.parseInt(parts[1]); String patchPart = parts[2]; int patch = Integer.parseInt(patchPart.contains("-") ? patchPart.substring(0, patchPart.indexOf("-")) : patchPart); int versionValue = major * 10000 + minor * 100 + patch; return versionValue > 20000; } catch (NumberFormatException e) { return false; }
                        
                        Input: "versions less than 1.5.0"
                        Output: String[] parts = entity.getName().split("\\."); if (parts.length < 3) return false; try { int major = Integer.parseInt(parts[0]); int minor = Integer.parseInt(parts[1]); String patchPart = parts[2]; int patch = Integer.parseInt(patchPart.contains("-") ? patchPart.substring(0, patchPart.indexOf("-")) : patchPart); int versionValue = major * 10000 + minor * 100 + patch; return versionValue < 10500; } catch (NumberFormatException e) { return false; }
                        
                        Input: "versions between 1.0.0 and 2.0.0"
                        Output: String[] parts = entity.getName().split("\\."); if (parts.length < 3) return false; try { int major = Integer.parseInt(parts[0]); int minor = Integer.parseInt(parts[1]); String patchPart = parts[2]; int patch = Integer.parseInt(patchPart.contains("-") ? patchPart.substring(0, patchPart.indexOf("-")) : patchPart); int versionValue = major * 10000 + minor * 100 + patch; return versionValue >= 10000 && versionValue <= 20000; } catch (NumberFormatException e) { return false; }"""
        ));

        // Feature configuration
        configs.put("Feature", new PromptConfig(
                """
                        @Getter
                        @Setter
                        public class Feature {
                            private String name;//never null
                            private OffsetDateTime created;//never null
                            private OffsetDateTime updated;//never null
                        }
                        """,
                """
                        Special considerations for Features:
                        - Feature names describe functionality (e.g., "User Authentication", "Payment Processing")
                        - Feature keys follow patterns like F-1, F-123. Keys are basically just unique database IDs of the Feature entity.
                        - Features are grouped under versions and contain sprints
                        - Focus on feature purpose and functionality descriptions
                        - Remember: you are filtering Feature entities, so each 'entity' is already a Feature
                        - When queries mention "features created in 2024" - this means filter by creation year, NOT by checking if entity.name contains "features"
                        - Terms like "features", "items", or similar generic terms refer to the entity type, not name content
                        - Use only getter methods like entity.getName(), entity.getCreated(), entity.getUpdated(), entity.getId(), entity.getVersionId(), entity.getKey()
                        - Never use reflection or field access, always use public getter methods
                        """,
                """
                        Examples:
                        Input: "authentication"
                        Output: return entity.getName().toLowerCase().contains("authentication");
                        
                        Input: "name contains user"
                        Output: return entity.getName().toLowerCase().contains("user");
                        
                        Input: "created in 2024"
                        Output: return entity.getCreated().getYear() == 2024;
                        
                        Input: "features created in 2024"
                        Output: return entity.getCreated().getYear() == 2024;
                        
                        Input: "features created after January 2024"
                        Output: return entity.getCreated().isAfter(OffsetDateTime.of(2024, 1, 31, 23, 59, 59, 0, OffsetDateTime.now().getOffset()));
                        
                        Input: "features created before December 2024"
                        Output: return entity.getCreated().isBefore(OffsetDateTime.of(2024, 12, 1, 0, 0, 0, 0, OffsetDateTime.now().getOffset()));
                        
                        Input: "updated in 2025"
                        Output: return entity.getUpdated().getYear() == 2025;
                        
                        Input: "features updated in 2025"
                        Output: return entity.getUpdated().getYear() == 2025;
                        
                        Input: "payment features"
                        Output: return entity.getName().toLowerCase().contains("payment");
                        
                        """
        ));

        // Sprint configuration
        configs.put("Sprint", new PromptConfig(
                """
                        @Getter
                        @Setter
                        public class Sprint {
                            private String name;//never null
                            private LocalDateTime start;
                            private LocalDateTime end;
                            private LocalDateTime releaseDate;
                            private Status status;//never null
                            private Duration originalEstimation;
                            private Duration worked;
                            private Duration remaining;
                            private OffsetDateTime created;//never null
                            private OffsetDateTime updated;//never null
                        }
                        """,
                """
                        Special considerations for Sprints:
                        - Sprint names often include version numbers, alpha/beta/rc suffixes (e.g., "Sprint 1.2.3-Alpha", "Authentication Sprint")
                        - Status is an enum with following values: CREATED, STARTED, CLOSED.
                        - Time durations are Duration objects (originalEstimation, worked, remaining)
                        - Sprint keys follow patterns like S-1, S-123. Keys are basically just unique database IDs of the Sprint entity.
                        - Support time-based queries (start/end dates, duration comparisons)
                        - Consider sprint progress (worked vs remaining time)
                        - Sprints belong to features and are assigned to users
                        - Remember: you are filtering Sprint entities, so each 'entity' is already a Sprint
                        - When queries mention "sprints created in 2024" - this means filter by creation year, NOT by checking if entity.name contains "sprints"
                        - Terms like "sprints", "items", or similar generic terms refer to the entity type, not name content
                        - Use only getter methods like entity.getName(), entity.getCreated(), entity.getUpdated(), entity.getFeatureId(), entity.getStatus(), entity.getStart(), entity.getEnd(), entity.getOriginalEstimation(), entity.getWorked(), entity.getRemaining()
                        - Never use reflection or field access, always use public getter methods
                        """,
                """
                        Examples:
                        Input: "sprint alpha"
                        Output: return entity.getName() != null && entity.getName().toLowerCase().contains("alpha");
                        
                        Input: "name contains beta"
                        Output: return entity.getName() != null && entity.getName().toLowerCase().contains("beta");
                        
                        Input: "authentication"
                        Output: return entity.getName() != null && entity.getName().toLowerCase().contains("authentication");
                        
                        Input: "created sprints"
                        Output: return entity.getStatus() != null && entity.getStatus() == Status.CREATED;
                        
                        Input: "started sprints"
                        Output: return entity.getStatus() != null && entity.getStatus() == Status.STARTED;
                        
                        Input: "closed sprints"
                        Output: return entity.getStatus() != null && entity.getStatus() == Status.CLOSED;
                        
                        Input: "sprints starting after January 2025"
                        Output: return entity.getStart() != null && entity.getStart().isAfter(LocalDateTime.of(2025, 1, 31, 23, 59, 59));
                        
                        Input: "sprints ending before March 2025"
                        Output: return entity.getEnd() != null && entity.getEnd().isBefore(LocalDateTime.of(2025, 3, 1, 0, 0, 0));
                        
                        Input: "sprints with remaining work"
                        Output: return entity.getRemaining() != null && !entity.getRemaining().isZero();
                        
                        Input: "sprints over 60 hours estimation"
                        Output: return entity.getOriginalEstimation() != null && entity.getOriginalEstimation().toHours() > 60;
                        
                        Input: "sprints with more than 40 hours worked"
                        Output: return entity.getWorked() != null && entity.getWorked().toHours() > 40;
                        
                        Input: "created in 2024"
                        Output: return null && entity.getCreated().getYear() == 2024;
                        
                        Input: "sprints created in 2024"
                        Output: return && entity.getCreated().getYear() == 2024;
                        
                        Input: "updated in 2025"
                        Output: return entity.getUpdated() != null && entity.getUpdated().getYear() == 2025;
                        
                        Input: "sprints starting this year"
                        Output: return entity.getStart() != null && entity.getStart().getYear() == java.time.Year.now().getValue();
                        
                        Input: "sprints ending this month"
                        Output: return entity.getEnd() != null && entity.getEnd().getYear() == java.time.LocalDateTime.now().getYear() && entity.getEnd().getMonth() == java.time.LocalDateTime.now().getMonth();"""
        ));

        // User configuration
        configs.put("User", new PromptConfig(
                """
                        @Getter
                        @Setter
                        public class User {
                            LocalDate now;//never null, injected with current date
                            private String name;//never null
                            private String email;//never null
                            private LocalDate firstWorkingDay;//never null
                            private LocalDate lastWorkingDay; // null for active users
                            private Color color;//never null
                            private OffsetDateTime created;//never null, indicates only when this entity was created
                            private OffsetDateTime updated;//never null, indicates only when this entity was updated
                        }
                        """,
                """
                        Special considerations for Users:
                        - User names contain first and last names (e.g., "John Doe", "Jane Smith")
                        - Email addresses follow standard patterns (firstname.lastname@domain.com)
                        - Employment status: active users have lastWorkingDay as null, former employees have a date
                        - firstWorkingDay indicates hire date, the date an employee starts his contract, lastWorkingDay indicates termination date
                        - Users have associated availabilities, locations, and off days
                        - Support tenure-based queries and employment status filtering
                        - Remember: you are filtering User entities, so each 'entity' is already a User
                        - When queries mention "users created in 2024" - this means filter by creation year, NOT by checking if entity.name contains "users"
                        - Terms like "users", "employees", or similar generic terms refer to the entity type, not name content
                        - Use only getter methods like entity.getName(), entity.getEmail(), entity.getFirstWorkingDay(), entity.getLastWorkingDay(), entity.getCreated(), entity.getUpdated()
                        - Never use reflection or field access, always use public getter methods
                        """,
                """
                        Examples:
                        Input: "John Doe"
                        Output: return entity.getName().toLowerCase().contains("john doe");
                        
                        Input: "first name John"
                        Output: return entity.getName().toLowerCase().contains("john");
                        
                        Input: "last name Smith"
                        Output: return entity.getName().toLowerCase().contains("smith");
                        
                        Input: "name contains Doe"
                        Output: return entity.getName().toLowerCase().contains("doe");
                        
                        Input: "email john.doe@company.com"
                        Output: return entity.getEmail() != null && entity.getEmail().toLowerCase().equals("john.doe@company.com");
                        
                        Input: "email contains @company.com"
                        Output: return entity.getEmail() != null && entity.getEmail().toLowerCase().contains("@company.com");
                        
                        Input: "active users"
                        Output: return entity.getLastWorkingDay() == null;
                        
                        Input: "former employees"
                        Output: return entity.getLastWorkingDay() != null;
                        
                        Input: "users starting after 2020"
                        Output: return entity.getFirstWorkingDay() != null && entity.getFirstWorkingDay().isAfter(LocalDate.of(2020, 12, 31));
                        
                        Input: "users starting before 2020"
                        Output: return entity.getFirstWorkingDay() != null && entity.getFirstWorkingDay().isBefore(LocalDate.of(2020, 1, 1));
                        
                        Input: "users starting in 2024"
                        Output: return entity.getFirstWorkingDay() != null && entity.getFirstWorkingDay().getYear() == 2024;
                        
                        Input: "long-term employees"
                        Output: return entity.getFirstWorkingDay() != null && entity.getFirstWorkingDay().isBefore(LocalDate.of(2021, 1, 1));
                        
                        Input: "new employees"
                        Output: return entity.getFirstWorkingDay() != null && entity.getFirstWorkingDay().isAfter(LocalDate.of(2023, 12, 31));
                        
                        Input: "users hired this year"
                        Output: return entity.getFirstWorkingDay() != null && entity.getFirstWorkingDay().getYear() == java.time.Year.now().getValue();
                        
                        Input: "created in 2024"
                        Output: return entity.getCreated().getYear() == 2024;
                        
                        Input: "users created in 2024"
                        Output: return entity.getCreated().getYear() == 2024;
                        
                        Input: "employees created in 2024"
                        Output: return entity.getCreated().getYear() == 2024;
                        
                        Input: "updated in 2025"
                        Output: return entity.getUpdated().getYear() == 2025;
                        
                        Input: "users ending employment in 2024"
                        Output: return entity.getLastWorkingDay() != null && entity.getLastWorkingDay().getYear() == 2024;
                        
                        Input: "users ending employment after June 2024"
                        Output: return entity.getLastWorkingDay() != null && entity.getLastWorkingDay().isAfter(LocalDate.of(2024, 6, 30));
                        
                        Input: "employees since over 4 years"
                        Output: return entity.getFirstWorkingDay() != null && entity.getFirstWorkingDay().isBefore(now.minusYears(4));
                        
                        Input: "employees started within last 6 months"
                        Output: return entity.getFirstWorkingDay() != null && entity.getFirstWorkingDay().isAfter(now.minusMonths(6));
                        """
        ));

        // Availability configuration
        configs.put("Availability", new PromptConfig(
                """
                        @Getter
                        @Setter
                        public class Availability {
                            private float availability;
                            private LocalDate start;//never null
                            private OffsetDateTime created;//never null
                            private OffsetDateTime updated;//never null
                        }
                        """,
                """
                        Special considerations for Availability:
                        - Availability represents a users availability to work on projects.
                        - Availability values are floats between 0.0 and 1.0 (e.g., 0.8 = 80% availability)
                        - Start dates indicate when this availability period begins
                        - Support percentage-based queries and date range filtering
                        - Remember: you are filtering Availability entities, so each 'entity' is already an Availability
                        - When queries mention "availability created in 2024" - this means filter by creation year, NOT by checking if entity content contains "availability"
                        - Never use reflection or field access, always use public getter methods
                        """,
                """
                        Examples:
                        Input: "80% availability"
                        Output: return entity.getAvailability() == 0.8f;
                        
                        Input: "availability greater than 50%"
                        Output: return entity.getAvailability() > 0.5f;
                        
                        Input: "availability less than 90%"
                        Output: return entity.getAvailability() < 0.9f;
                        
                        Input: "full availability"
                        Output: return entity.getAvailability() == 1.0f;
                        
                        Input: "partial availability"
                        Output: return entity.getAvailability() > 0.0f && entity.getAvailability() < 1.0f;
                        
                        Input: "availability starting after January 2025"
                        Output: return entity.getStart() != null && entity.getStart().isAfter(LocalDate.of(2025, 1, 31));
                        
                        Input: "availability starting before March 2025"
                        Output: return entity.getStart() != null && entity.getStart().isBefore(LocalDate.of(2025, 3, 1));
                        
                        Input: "availability starting in 2025"
                        Output: return entity.getStart() != null && entity.getStart().getYear() == 2025;
                        
                        Input: "availability starting this year"
                        Output: return entity.getStart() != null && entity.getStart().getYear() == java.time.Year.now().getValue();
                        
                        Input: "availability starting this month"
                        Output: return entity.getStart() != null && entity.getStart().getYear() == java.time.LocalDate.now().getYear() && entity.getStart().getMonth() == java.time.LocalDate.now().getMonth();
                        
                        Input: "high availability"
                        Output: return entity.getAvailability() >= 0.8f;
                        
                        Input: "low availability"
                        Output: return entity.getAvailability() <= 0.5f;
                        
                        Input: "created in 2025"
                        Output: return entity.getCreated().getYear() == 2025;
                        
                        Input: "availability created in 2025"
                        Output: return entity.getCreated().getYear() == 2025;
                        
                        Input: "updated in 2025"
                        Output: return entity.getUpdated().getYear() == 2025;
                        
                        """
        ));

        // Location configuration
        configs.put("Location", new PromptConfig(
                """
                        @Getter
                        @Setter
                        public class Location extends AbstractTimeAware {
                            private String country;//never null
                            private LocalDate start;//never null
                            private String state;//never null
                            private OffsetDateTime created;//never null
                            private OffsetDateTime updated;//never null
                        }
                        """,
                """
                        Special considerations for Locations:
                        - Country and state fields contain location information for determining public holidays
                        - Start dates indicate when the user began working at this location
                        - Support geographical searches and date-based filtering
                        - Consider legal/contract location contexts for employment purposes
                        - Remember: you are filtering Location entities, so each 'entity' is already a Location
                        - When queries mention "locations created in 2024" - this means filter by creation year, NOT by checking if entity content contains "locations"
                        - Use only getter methods like entity.getCountry(), entity.getState(), entity.getStart(), entity.getUser(), entity.getCreated(), entity.getUpdated(), entity.getId(), entity.getKey()
                        - Never use reflection or field access, always use public getter methods
                        """,
                """
                        Examples:
                        Input: "Germany"
                        Output: return entity.getCountry().toLowerCase().contains("germany");
                        
                        Input: "country Germany"
                        Output: return entity.getCountry().toLowerCase().contains("germany");
                        
                        Input: "state Bavaria"
                        Output: return entity.getState().toLowerCase().contains("bavaria");
                        
                        Input: "locations in Australia"
                        Output: return entity.getCountry().toLowerCase().contains("australia");
                        
                        Input: "locations starting after January 2025"
                        Output: return entity.getStart().isAfter(LocalDate.of(2025, 1, 31));
                        
                        Input: "locations starting before March 2025"
                        Output: return entity.getStart().isBefore(LocalDate.of(2025, 3, 1));
                        
                        Input: "locations starting in 2025"
                        Output: return entity.getStart().getYear() == 2025;
                        
                        Input: "locations starting this year"
                        Output: return entity.getStart().getYear() == java.time.Year.now().getValue();
                        
                        Input: "European locations"
                        Output: return (entity.getCountry().toLowerCase().contains("germany") || entity.getCountry().toLowerCase().contains("france") || entity.getCountry().toLowerCase().contains("italy") || entity.getCountry().toLowerCase().contains("spain") || entity.getCountry().toLowerCase().contains("netherlands") || entity.getCountry().toLowerCase().contains("belgium") || entity.getCountry().toLowerCase().contains("austria") || entity.getCountry().toLowerCase().contains("switzerland") || entity.getCountry().toLowerCase().contains("united kingdom") || entity.getCountry().toLowerCase().contains("ireland") || entity.getCountry().toLowerCase().contains("portugal") || entity.getCountry().toLowerCase().contains("greece") || entity.getCountry().toLowerCase().contains("denmark") || entity.getCountry().toLowerCase().contains("sweden") || entity.getCountry().toLowerCase().contains("norway") || entity.getCountry().toLowerCase().contains("finland") || entity.getCountry().toLowerCase().contains("poland"));
                        
                        Input: "US locations"
                        Output: return (entity.getCountry().toLowerCase().contains("united states") || entity.getCountry().toLowerCase().contains("usa") || entity.getCountry().toLowerCase().contains("america"));
                        
                        Input: "Asian locations"
                        Output: return (entity.getCountry().toLowerCase().contains("japan") || entity.getCountry().toLowerCase().contains("china") || entity.getCountry().toLowerCase().contains("india") || entity.getCountry().toLowerCase().contains("korea") || entity.getCountry().toLowerCase().contains("singapore") || entity.getCountry().toLowerCase().contains("thailand") || entity.getCountry().toLowerCase().contains("vietnam") || entity.getCountry().toLowerCase().contains("malaysia") || entity.getCountry().toLowerCase().contains("indonesia"));
                        
                        Input: "created in 2025"
                        Output: return entity.getCreated().getYear() == 2025;
                        
                        Input: "locations created in 2025"
                        Output: return entity.getCreated().getYear() == 2025;
                        
                        Input: "updated in 2025"
                        Output: return entity.getUpdated().getYear() == 2025;
                        
                        Input: "California"
                        Output: return entity.getState().toLowerCase().contains("california");
                        
                        Input: "Texas locations"
                        Output: return entity.getState().toLowerCase().contains("texas");"""
        ));

        // OffDay configuration
        configs.put("OffDay", new PromptConfig(
                """
                        @Getter
                        @Setter
                        public class OffDay extends AbstractDateRange {
                            private OffDayType type; //never null, enum: VACATION, SICK, TRIP, HOLIDAY
                            private LocalDate firstDay;//never null
                            private LocalDate lastDay;//never null
                            private OffsetDateTime created;//never null
                            private OffsetDateTime updated;//never null
                        }
                        """,
                """
                        Special considerations for OffDays:
                        - Type values: VACATION, SICK, TRIP, HOLIDAY (enum OffDayType)
                        - Date ranges with firstDay and lastDay in LocalDate format
                        - Support type-based filtering and date range queries
                        - Consider duration calculations and overlap queries for scheduling
                        - Remember: you are filtering OffDay entities, so each 'entity' is already an OffDay
                        - When queries mention "off days created in 2024" - this means filter by creation year, NOT by checking if entity content contains "off days"
                        - Use only getter methods like entity.getType(), entity.getFirstDay(), entity.getLastDay(), entity.getCreated(), entity.getUpdated()
                        - Never use reflection or field access, always use public getter methods
                        """,
                """
                        Examples:
                        Input: "vacation"
                        Output: return entity.getType() == OffDayType.VACATION;
                        
                        Input: "sick days"
                        Output: return entity.getType() == OffDayType.SICK;
                        
                        Input: "holidays"
                        Output: return entity.getType() == OffDayType.HOLIDAY;
                        
                        Input: "trips"
                        Output: return entity.getType() == OffDayType.TRIP;
                        
                        Input: "off days in January 2025"
                        Output: return entity.getFirstDay().getYear() == 2025 && entity.getFirstDay().getMonthValue() == 1;
                        
                        Input: "off days starting after February 2025"
                        Output: return entity.getFirstDay().isAfter(LocalDate.of(2025, 2, 28));
                        
                        Input: "off days ending before March 2025"
                        Output: return entity.getLastDay() != null && entity.getLastDay().isBefore(LocalDate.of(2025, 3, 1));
                        
                        Input: "off days starting in 2025"
                        Output: return entity.getFirstDay().getYear() == 2025;
                        
                        Input: "off days ending in 2025"
                        Output: return entity.getLastDay().getYear() == 2025;
                        
                        Input: "long vacations"
                        Output: return entity.getType() == OffDayType.VACATION && entity.getFirstDay() != null && entity.getLastDay() != null && java.time.temporal.ChronoUnit.DAYS.between(entity.getFirstDay(), entity.getLastDay()) >= 7;
                        
                        Input: "short trips"
                        Output: return entity.getType() == OffDayType.TRIP && entity.getFirstDay() != null && entity.getLastDay() != null && java.time.temporal.ChronoUnit.DAYS.between(entity.getFirstDay(), entity.getLastDay()) <= 3;
                        
                        Input: "off days this year"
                        Output: return entity.getFirstDay().getYear() == java.time.Year.now().getValue();
                        
                        Input: "off days this month"
                        Output: return entity.getFirstDay().getYear() == java.time.LocalDate.now().getYear() && entity.getFirstDay().getMonth() == java.time.LocalDate.now().getMonth();
                        
                        Input: "created in 2025"
                        Output: return entity.getCreated().getYear() == 2025;
                        
                        Input: "off days created in 2025"
                        Output: return entity.getCreated().getYear() == 2025;
                        
                        Input: "updated in 2025"
                        Output: return entity.getUpdated().getYear() == 2025;
                        
                        Input: "weekend off days"
                        Output: return (entity.getFirstDay().getDayOfWeek() == java.time.DayOfWeek.SATURDAY || entity.getFirstDay().getDayOfWeek() == java.time.DayOfWeek.SUNDAY);
                        
                        Input: "multi-day off periods"
                        Output: return !entity.getFirstDay().equals(entity.getLastDay());"""
        ));

        return configs;
    }

    /**
     * Configuration class for entity-specific prompts
     */
    public static class PromptConfig {
        public final String javaClass;
        public final String javaExamples;
        public final String javascriptExamples;
        public final String specialConsiderations;

        public PromptConfig(String javaClass, String specialConsiderations, String javaExamples) {
            this.javaClass             = javaClass;
            this.specialConsiderations = specialConsiderations;
            this.javascriptExamples    = null;
            this.javaExamples          = javaExamples;
        }

        public PromptConfig(String javaClass, String specialConsiderations, String javascriptExamples, String javaExamples) {
            this.javaClass             = javaClass;
            this.specialConsiderations = specialConsiderations;
            this.javascriptExamples    = javascriptExamples;
            this.javaExamples          = javaExamples;
        }
    }
}
