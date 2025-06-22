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

import de.bushnaq.abdalla.projecthub.ui.util.AbstractUiTestUtil;
import de.bushnaq.abdalla.projecthub.ui.util.selenium.SeleniumHandler;
import de.bushnaq.abdalla.projecthub.util.FeatureViewTester;
import de.bushnaq.abdalla.projecthub.util.ProductViewTester;
import de.bushnaq.abdalla.projecthub.util.SprintViewTester;
import de.bushnaq.abdalla.projecthub.util.VersionViewTester;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.transaction.annotation.Transactional;

/**
 * Integration test for the SprintListView UI component.
 * Tests create, edit, and delete operations for sprints in the UI.
 * <p>
 * These tests use {@link SprintViewTester} to interact with the UI elements
 * and verify the expected behavior. Each test requires a product, version, and project
 * to be created first, as sprints exist within the context of a project.
 */
@ExtendWith(SpringExtension.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@Transactional
public class SprintListViewTest extends AbstractUiTestUtil {
    @Autowired
    private       FeatureViewTester featureViewTester;
    private final String            newSprintName = "NewSprint-3";
    private final String            productName   = "Feature-3";
    @Autowired
    private       ProductViewTester productViewTester;
    private final String            projectName   = "Feature-3";
    @Autowired
    private       SeleniumHandler   seleniumHandler;
    private final String            sprintName    = "Sprint-3";
    @Autowired
    private       SprintViewTester  sprintViewTester;
    private final String            versionName   = "Version-3";
    @Autowired
    private       VersionViewTester versionViewTester;

    /**
     * Setup method that runs before each test.
     * <p>
     * Creates a product, selects it, then creates a version, selects it, and finally
     * creates a project and selects it. This establishes the required hierarchy for testing
     * sprint operations, as sprints exist within projects, which exist within versions,
     * which exist within products.
     *
     * @throws Exception if any error occurs during setup
     */
    @BeforeEach
    public void createPrerequisites(TestInfo testInfo) throws Exception {
        // Navigate to the product list and create a product
        productViewTester.switchToProductListView(testInfo.getTestClass().get().getSimpleName(), generateTestCaseName(testInfo));
//        seleniumHandler.startRecording(testInfo.getTestClass().get().getSimpleName(), generateTestCaseName(testInfo));
        productViewTester.createProductConfirm(productName);
        productViewTester.selectProduct(productName);

        // Create a version
        versionViewTester.createVersionConfirm(versionName);
        versionViewTester.selectVersion(versionName);

        // Create a project
        featureViewTester.createFeatureConfirm(projectName);
        featureViewTester.selectFeature(projectName);
    }

    /**
     * Tests the behavior when creating a sprint but canceling the operation.
     * <p>
     * Verifies that when a user clicks the create sprint button, enters a name, and then
     * cancels the operation, no sprint is created in the list.
     *
     * @throws Exception if any error occurs during the test
     */
    @Test
    public void testCreateCancel() throws Exception {
        sprintViewTester.createSprintCancel(sprintName);
    }

    /**
     * Tests the behavior when successfully creating a sprint.
     * <p>
     * Verifies that when a user clicks the create sprint button, enters a name, and confirms
     * the creation, the sprint appears in the list.
     *
     * @throws Exception if any error occurs during the test
     */
    @Test
    public void testCreateConfirm() throws Exception {
        sprintViewTester.createSprintConfirm(sprintName);
    }

    /**
     * Tests the behavior when attempting to delete a sprint but canceling the operation.
     * <p>
     * Creates a sprint, then attempts to delete it but cancels the confirmation dialog.
     * Verifies that the sprint remains in the list.
     *
     * @throws Exception if any error occurs during the test
     */
    @Test
    public void testDeleteCancel() throws Exception {
        sprintViewTester.createSprintConfirm(sprintName);
        sprintViewTester.deleteSprintCancel(sprintName);
    }

    /**
     * Tests the behavior when successfully deleting a sprint.
     * <p>
     * Creates a sprint, then deletes it by confirming the deletion in the confirmation dialog.
     * Verifies that the sprint is removed from the list.
     *
     * @throws Exception if any error occurs during the test
     */
    @Test
    public void testDeleteConfirm() throws Exception {
        sprintViewTester.createSprintConfirm(sprintName);
        sprintViewTester.deleteSprintConfirm(sprintName);
    }

    /**
     * Tests the behavior when attempting to edit a sprint but canceling the operation.
     * <p>
     * Creates a sprint, attempts to edit its name, but cancels the edit dialog.
     * Verifies that the original name remains unchanged and the new name is not present.
     *
     * @throws Exception if any error occurs during the test
     */
    @Test
    public void testEditCancel() throws Exception {
        sprintViewTester.createSprintConfirm(sprintName);
        sprintViewTester.editSprintCancel(sprintName, newSprintName);
    }

    /**
     * Tests the behavior when successfully editing a sprint.
     * <p>
     * Creates a sprint, edits its name, and confirms the edit.
     * Verifies that the sprint with the new name appears in the list and the old name is removed.
     *
     * @throws Exception if any error occurs during the test
     */
    @Test
    public void testEditConfirm() throws Exception {
        sprintViewTester.createSprintConfirm(sprintName);
        sprintViewTester.editSprintConfirm(sprintName, newSprintName);
    }
}
