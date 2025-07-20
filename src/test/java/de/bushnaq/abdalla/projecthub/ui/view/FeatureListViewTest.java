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

import de.bushnaq.abdalla.projecthub.ui.util.AbstractUiTestUtil;
import de.bushnaq.abdalla.projecthub.ui.util.selenium.SeleniumHandler;
import de.bushnaq.abdalla.projecthub.ui.view.util.FeatureListViewTester;
import de.bushnaq.abdalla.projecthub.ui.view.util.ProductListViewTester;
import de.bushnaq.abdalla.projecthub.ui.view.util.VersionListViewTester;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.transaction.annotation.Transactional;

/**
 * Integration test for the ProjectListView UI component.
 * Tests create, edit, and delete operations for projects in the UI.
 * <p>
 * These tests use {@link FeatureListViewTester} to interact with the UI elements
 * and verify the expected behavior. Each test requires a product and version
 * to be created first, as projects exist within the context of a version.
 */
@ExtendWith(SpringExtension.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@Transactional
public class FeatureListViewTest extends AbstractUiTestUtil {
    @Autowired
    private       FeatureListViewTester featureListViewTester;
    private final String                featureName    = "Project-2";
    private final String                newProjectName = "NewProject-2";
    @LocalServerPort
    private       int                   port;
    @Autowired
    private       ProductListViewTester productListViewTester;
    private final String                productName    = "Product-2";
    @Autowired
    private       SeleniumHandler       seleniumHandler;
    @Autowired
    private       VersionListViewTester versionListViewTester;
    private final String                versionName    = "Version-2";

    /**
     * Setup method that runs before each test.
     * <p>
     * Creates a product, selects it, then creates a version and selects it.
     * This establishes the required hierarchy for testing project operations,
     * as projects exist within versions, which exist within products.
     *
     * @throws Exception if any error occurs during setup
     */
    @BeforeEach
    public void setupTest(TestInfo testInfo) throws Exception {
        // Navigate to product list and create a product
        productListViewTester.switchToProductListView(testInfo.getTestClass().get().getSimpleName(), generateTestCaseName(testInfo));
//        seleniumHandler.startRecording(testInfo.getTestClass().get().getSimpleName(), generateTestCaseName(testInfo));
        productListViewTester.createProductConfirm(productName);
        productListViewTester.selectProduct(productName);

        // Create a version
        versionListViewTester.createVersionConfirm(versionName);
        versionListViewTester.selectVersion(versionName);
    }

    /**
     * Tests the behavior when creating a project but canceling the operation.
     * <p>
     * Verifies that when a user clicks the create project button, enters a name, and then
     * cancels the operation, no project is created in the list.
     *
     * @throws Exception if any error occurs during the test
     */
    @Test
    public void testCreateCancel() throws Exception {
        featureListViewTester.createFeatureCancel(featureName);
    }

    /**
     * Tests the behavior when successfully creating a project.
     * <p>
     * Verifies that when a user clicks the create project button, enters a name, and confirms
     * the creation, the project appears in the list.
     *
     * @throws Exception if any error occurs during the test
     */
    @Test
    public void testCreateConfirm() throws Exception {
        featureListViewTester.createFeatureConfirm(featureName);
    }

    /**
     * Tests the behavior when attempting to create a feature with a name that already exists.
     * <p>
     * Creates a feature, then tries to create another feature with the same name.
     * Verifies that the system displays an error and prevents the creation of the duplicate feature.
     *
     * @throws Exception if any error occurs during the test
     */
    @Test
    public void testCreateDuplicateNameFails() throws Exception {
        // First, create a feature
        featureListViewTester.createFeatureConfirm(featureName);
        // Then try to create another feature with the same name
        featureListViewTester.createFeatureWithDuplicateName(featureName);
    }

    /**
     * Tests the behavior when attempting to delete a project but canceling the operation.
     * <p>
     * Creates a project, then attempts to delete it but cancels the confirmation dialog.
     * Verifies that the project remains in the list.
     *
     * @throws Exception if any error occurs during the test
     */
    @Test
    public void testDeleteCancel() throws Exception {
        featureListViewTester.createFeatureConfirm(featureName);
        featureListViewTester.deleteFeatureCancel(featureName);
    }

    /**
     * Tests the behavior when successfully deleting a project.
     * <p>
     * Creates a project, then deletes it by confirming the deletion in the confirmation dialog.
     * Verifies that the project is removed from the list.
     *
     * @throws Exception if any error occurs during the test
     */
    @Test
    public void testDeleteConfirm() throws Exception {
        featureListViewTester.createFeatureConfirm(featureName);
        featureListViewTester.deleteFeatureConfirm(featureName);
    }

    /**
     * Tests the behavior when attempting to edit a project but canceling the operation.
     * <p>
     * Creates a project, attempts to edit its name, but cancels the edit dialog.
     * Verifies that the original name remains unchanged and the new name is not present.
     *
     * @throws Exception if any error occurs during the test
     */
    @Test
    public void testEditCancel() throws Exception {
        featureListViewTester.createFeatureConfirm(featureName);
        featureListViewTester.editFeatureCancel(featureName, newProjectName);
    }

    /**
     * Tests the behavior when successfully editing a project.
     * <p>
     * Creates a project, edits its name, and confirms the edit.
     * Verifies that the project with the new name appears in the list and the old name is removed.
     *
     * @throws Exception if any error occurs during the test
     */
    @Test
    public void testEditConfirm() throws Exception {
        featureListViewTester.createFeatureConfirm(featureName);
        featureListViewTester.editFeatureConfirm(featureName, newProjectName);
    }

    /**
     * Tests the behavior when attempting to edit a feature to have a name that already exists.
     * <p>
     * Creates two features, then tries to update the second feature to have the same name as the first.
     * Verifies that the system displays an error and prevents the update.
     *
     * @throws Exception if any error occurs during the test
     */
    @Test
    public void testEditDuplicateNameFails() throws Exception {
        // First, create two features with different names
        featureListViewTester.createFeatureConfirm(featureName);
        featureListViewTester.createFeatureConfirm(newProjectName);

        // Then try to edit the second feature to have the same name as the first
        featureListViewTester.editFeatureWithDuplicateName(newProjectName, featureName);
    }
}
