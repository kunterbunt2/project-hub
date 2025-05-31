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

package de.bushnaq.abdalla.projecthub.ui;

import de.bushnaq.abdalla.projecthub.ParameterOptions;
import de.bushnaq.abdalla.projecthub.dto.*;
import de.bushnaq.abdalla.projecthub.rest.debug.DebugUtil;
import de.bushnaq.abdalla.projecthub.ui.util.AbstractUiTestUtil;
import de.bushnaq.abdalla.projecthub.ui.util.selenium.SeleniumHandler;
import de.bushnaq.abdalla.projecthub.util.*;
import org.junit.jupiter.api.TestInfo;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

@ExtendWith(SpringExtension.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT, properties = "server.port=8080")
@AutoConfigureMockMvc
@Transactional
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
public class SprintViewTest extends AbstractUiTestUtil {
    @Autowired
    private ProductViewTester productViewTester;
    @Autowired
    private SeleniumHandler   seleniumHandler;
    @Autowired
    private VersionViewTester versionViewTester;

    private static String generateFeatureName(int t) {
        return String.format("Feature-%d", t);
    }

    private void generateTasks(RandomCase randomCase) {
        random.setSeed(randomCase.getSeed());
        int numberOfUsers    = random.nextInt(randomCase.getMaxNumberOfUsers()) + 2;
        int numberOfFeatures = random.nextInt(randomCase.getMaxNumberOfFeatures()) + 1;
        int numberOfTasks    = random.nextInt(randomCase.getMaxNumberOfWork()) + 1;
        {
            addRandomUsers(numberOfUsers);
            Product product = addProduct("Product-" + 1);
            Version version = addVersion(product, String.format("1.%d.0", 0));
            Project project = addRandomProject(version);
            sprint = addRandomSprint(project);
        }

        Task startMilestone = addTask(sprint, null, "Start", LocalDateTime.parse("2024-12-15T08:00:00"), Duration.ZERO, null, null, TaskMode.MANUALLY_SCHEDULED, true);
        for (int f = 0; f < numberOfFeatures; f++) {
            String featureName = generateFeatureName(f);
            Task   feature     = addParentTask(featureName, sprint, null, startMilestone);
            for (int t = 0; t < numberOfTasks; t++) {
                User   user     = expectedUsers.stream().toList().get(random.nextInt(numberOfUsers));
                String duration = String.format("%dd", random.nextInt(randomCase.getMaxDurationDays()) + 1);
                String workName = generateWorkName(featureName, t);
                addTask(workName, duration, user, sprint, feature, null);
            }
        }
    }

    private static String generateWorkName(String featureName, int t) {
        String[] workNames = new String[]{"pre-planning", "planning", "analysis", "design", "implementation", "module test", "Functional Test", "System Test", "debugging", "deployment"};
        return String.format("%s-%s", featureName, workNames[t]);
    }

    private static List<RandomCase> listRandomCases() {
        RandomCase[] randomCases = new RandomCase[]{//
                new RandomCase(1, 10, 2, 1, 2, 1),//
//                new RandomCase(2, 10, 3, 2, 3, 1)//
        };
        return Arrays.stream(randomCases).toList();
    }

    @ParameterizedTest
    @MethodSource("listRandomCases")
    public void testShowProducts(RandomCase randomCase, TestInfo testInfo) throws Exception {
        TestInfoUtil.setTestMethod(testInfo, testInfo.getTestMethod().get().getName() + "-" + randomCase.getTestCaseIndex());
        TestInfoUtil.setTestCaseIndex(testInfo, randomCase.getTestCaseIndex());
        setTestCaseName(this.getClass().getName(), testInfo.getTestMethod().get().getName() + "-" + randomCase.getTestCaseIndex());
        generateTasks(randomCase);
        levelResources(testInfo, null);
        generateWorklogs(ParameterOptions.getLocalNow());
        productViewTester.switchToProductListView();
        productViewTester.selectProduct("Product-1");
        versionViewTester.selectVersion("1.0.0");
        ProjectViewTester projectViewTester = new ProjectViewTester(seleniumHandler);
        projectViewTester.selectProject("Project-0");
        SprintViewTester sprintViewTester = new SprintViewTester(seleniumHandler);
        sprintViewTester.selectSprint("sprint-0");
        if (DebugUtil.DEBUG) {
            seleniumHandler.waitUntilBrowserClosed(0);
        } else {
            seleniumHandler.waitUntilBrowserClosed(5000);
        }
    }

}
