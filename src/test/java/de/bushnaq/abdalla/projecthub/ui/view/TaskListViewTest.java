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

package de.bushnaq.abdalla.projecthub.ui.view;

import de.bushnaq.abdalla.projecthub.ParameterOptions;
import de.bushnaq.abdalla.projecthub.ui.util.AbstractUiTestUtil;
import de.bushnaq.abdalla.projecthub.ui.util.selenium.SeleniumHandler;
import de.bushnaq.abdalla.projecthub.ui.view.util.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;

/**
 * Integration test for the TaskListView UI component.
 * Tests create, edit, and delete operations for tasks in the UI.
 * <p>
 * These tests use {@link TaskListViewTester} to interact with the UI elements
 * and verify the expected behavior. Each test requires a product, version, project,
 * and sprint to be created first, as tasks exist within the context of a sprint.
 */
@ExtendWith(SpringExtension.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@Transactional
public class TaskListViewTest extends AbstractUiTestUtil {
    @Autowired
    private       FeatureListViewTester featureListViewTester;
    private final String                featureName = nameGenerator.generateFeatureName(0);
    //    private final String                newTaskName = "NewTask-3";
    @Autowired
    private       ProductListViewTester productListViewTester;
    private final String                productName = nameGenerator.generateProductName(0);
    @Autowired
    private       SeleniumHandler       seleniumHandler;
    @Autowired
    private       SprintListViewTester  sprintListViewTester;
    private final String                sprintName  = nameGenerator.generateSprintName(0);
    @Autowired
    private       TaskListViewTester    taskListViewTester;
    private final String                taskName0   = nameGenerator.generateSprintName(0);
    @Autowired
    private       VersionListViewTester versionListViewTester;
    private final String                versionName = nameGenerator.generateVersionName(0);

    /**
     * Setup method that runs before each test.
     * <p>
     * Creates a product, selects it, then creates a version, selects it,
     * creates a project, selects it, and finally creates a sprint and selects it.
     * This establishes the required hierarchy for testing task operations,
     * as tasks exist within sprints, which exist within projects,
     * which exist within versions, which exist within products.
     *
     * @throws Exception if any error occurs during setup
     */
    @BeforeEach
    public void createPrerequisites(TestInfo testInfo) throws Exception {
        ParameterOptions.now = OffsetDateTime.now().withHour(8).withMinute(0).withSecond(0).withNano(0);
        // Navigate to the product list and create a product
        productListViewTester.switchToProductListView(testInfo.getTestClass().get().getSimpleName(), generateTestCaseName(testInfo));
        productListViewTester.createProductConfirm(productName);
        productListViewTester.selectProduct(productName);

        // Create a version
        versionListViewTester.createVersionConfirm(versionName);
        versionListViewTester.selectVersion(versionName);

        // Create a project (feature)
        featureListViewTester.createFeatureConfirm(featureName);
        featureListViewTester.selectFeature(featureName);

        // Create a sprint
        sprintListViewTester.createSprintConfirm(sprintName);
        // Navigate to TaskListView
        sprintListViewTester.configSprint(sprintName);
    }

    /**
     * Tests the behavior when creating a task but canceling the operation.
     * <p>
     * Verifies that when a user clicks the create task button, enters a name, and then
     * cancels the operation, no task is created in the list.
     *
     * @throws Exception if any error occurs during the test
     */
    @Test
    public void testCreate() throws Exception {
        taskListViewTester.createTask(taskName0);
        seleniumHandler.waitUntilBrowserClosed(0);
    }
}
