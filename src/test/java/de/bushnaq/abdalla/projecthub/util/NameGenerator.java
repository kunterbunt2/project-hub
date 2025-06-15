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

import org.ajbrown.namemachine.Name;
import org.ajbrown.namemachine.NameGeneratorOptions;

import java.util.List;

public class NameGenerator {
    private final String[]   productNames = new String[]{};
    private final List<Name> userNames;

    NameGenerator() {
        NameGeneratorOptions options = new NameGeneratorOptions();
        options.setRandomSeed(123L);//Get deterministic results by setting a random seed.
        org.ajbrown.namemachine.NameGenerator generator = new org.ajbrown.namemachine.NameGenerator(options);
        userNames = generator.generateNames(1000);
    }

    public String generateProductName(int index) {
        return String.format("Product-%d", index);
    }

    public String generateProjectName(int index) {
        return String.format("Project-%d", index);
    }

    public String generateSprintName(int index) {
        return String.format("Sprint-%d", index);
    }

    public String generateUserName(int userIndex) {
        return userNames.get(userIndex).getFirstName() + " " + userNames.get(userIndex).getLastName();
    }

    public String generateVersionName(int index) {
        return String.format("1.%d.0", index);
    }
}
