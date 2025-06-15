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

package de.bushnaq.abdalla.projecthub.util;

import lombok.Getter;
import lombok.ToString;

@Getter
@ToString(callSuper = false)
public class RandomCase {
    private final int  maxDurationDays;
    private final int  maxNumberOfFeatures;
    private final int  maxNumberOfProducts;
    private final int  maxNumberOfProjects;
    private final int  maxNumberOfSprints;
    private final int  maxNumberOfUsers;
    private final int  maxNumberOfVersions;
    private final int  maxNumberOfWork;
    private final long seed;
    private final int  testCaseIndex;

    public RandomCase(int testCaseIndex, int maxDurationDays, int maxNumberOfFeatures, int maxNumberOfUsers, int maxNumberOfWork, int seed) {
        this.testCaseIndex       = testCaseIndex;
        this.maxNumberOfProducts = 1;
        this.maxNumberOfVersions = 1;
        this.maxNumberOfProjects = 1;
        this.maxNumberOfSprints  = 1;
        this.maxDurationDays     = maxDurationDays;
        this.maxNumberOfFeatures = maxNumberOfFeatures;
        this.maxNumberOfUsers    = maxNumberOfUsers;
        this.maxNumberOfWork     = maxNumberOfWork;
        this.seed                = seed;
    }

    public RandomCase(int testCaseIndex, int maxNumberOfProducts, int maxNumberOfVersions, int maxNumberOfProjects, int maxNumberOfSprints, int maxDurationDays, int maxNumberOfFeatures, int maxNumberOfUsers, int maxNumberOfWork, int seed) {
        this.testCaseIndex       = testCaseIndex;
        this.maxNumberOfProducts = maxNumberOfProducts;
        this.maxNumberOfVersions = maxNumberOfVersions;
        this.maxNumberOfProjects = maxNumberOfProjects;
        this.maxNumberOfSprints  = maxNumberOfSprints;
        this.maxDurationDays     = maxDurationDays;
        this.maxNumberOfFeatures = maxNumberOfFeatures;
        this.maxNumberOfUsers    = maxNumberOfUsers;
        this.maxNumberOfWork     = maxNumberOfWork;
        this.seed                = seed;
    }

}
