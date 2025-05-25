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

import org.junit.jupiter.api.TestInfo;

public class TestInfoUtil {

    public static final String DAYS_AFTER_START = "daysAfterStart";
    public static final String TEST_CASE_INDEX  = "testCaseIndex";
    public static final String TEST_METHOD_NAME = "testMethodName";
    public static final String TEST_START       = "testStart";

    public static int getDaysAfterStart(TestInfo testInfo) {
        return testInfo.getTags().stream()
                .filter(tag -> tag.startsWith(DAYS_AFTER_START + "="))
                .map(tag -> Integer.parseInt(tag.substring(DAYS_AFTER_START.length() + 1)))
                .findFirst()
                .orElse(null);
    }

    public static Integer getTestCaseIndex(TestInfo testInfo) {
        return testInfo.getTags().stream()
                .filter(tag -> tag.startsWith(TEST_CASE_INDEX + "="))
                .map(tag -> tag.substring(TEST_CASE_INDEX.length() + 1))
                .map(Integer::parseInt)
                .findFirst()
                .orElse(null);
    }

    public static String getTestMethodName(TestInfo testInfo) {
        return testInfo.getTags().stream()
                .filter(tag -> tag.startsWith(TEST_METHOD_NAME + "="))
                .map(tag -> tag.substring(TEST_METHOD_NAME.length() + 1))
                .findFirst()
                .orElse(null);
    }

    public static String getTestStart(TestInfo testInfo) {
        return testInfo.getTags().stream()
                .filter(tag -> tag.startsWith(TEST_START + "="))
                .map(tag -> tag.substring(TEST_START.length() + 1))
                .findFirst()
                .orElse(null);
    }

    public static void setDaysAfterStart(TestInfo testInfo, int days) {
        testInfo.getTags().add(String.format("%s=%d", DAYS_AFTER_START, days));
    }

    public static void setTestCaseIndex(TestInfo testInfo, int testCaseIndex) {
        testInfo.getTags().add(String.format("%s=%s", TEST_CASE_INDEX, testCaseIndex));
    }

    public static void setTestMethod(TestInfo testInfo, String testMethodName) {
        testInfo.getTags().add(String.format("%s=%s", TEST_METHOD_NAME, testMethodName));
    }

    public static void setTestStart(TestInfo testInfo, String localDateTime) {
        testInfo.getTags().add(String.format("%s=%s", TEST_START, localDateTime));
    }
}
