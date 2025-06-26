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
import de.bushnaq.abdalla.projecthub.ui.view.util.ProductListViewTester;
import de.bushnaq.abdalla.projecthub.ui.view.util.VersionListViewTester;
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
 * Integration test for the VersionListView UI component.
 * Tests create, edit, and delete operations for versions in the UI.
 * <p>
 * These tests use {@link VersionListViewTester} to interact with the UI elements
 * and verify the expected behavior. Each test requires a product to be created first
 * as versions exist within the context of a product.
 */
@ExtendWith(SpringExtension.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@Transactional
public class VersionListViewTest extends AbstractUiTestUtil {
    private final String                newVersionName = "NewVersion-2";
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
     * Creates a product and navigates to its versions view, setting up the environment
     * for testing version-related operations.
     *
     * @throws Exception if any error occurs during setup
     */
    @BeforeEach
    public void createProduct(TestInfo testInfo) throws Exception {
        productListViewTester.switchToProductListView(testInfo.getTestClass().get().getSimpleName(), generateTestCaseName(testInfo));
        productListViewTester.createProductConfirm(productName);
        productListViewTester.selectProduct(productName);
    }

    /**
     * Tests the behavior when creating a version but canceling the operation.
     * <p>
     * Verifies that when a user clicks the create version button, enters a name, and then
     * cancels the operation, no version is created in the list.
     *
     * @throws Exception if any error occurs during the test
     */
    @Test
    public void testCreateCancel() throws Exception {
        versionListViewTester.createVersionCancel(versionName);
    }

    /**
     * Tests the behavior when successfully creating a version.
     * <p>
     * Verifies that when a user clicks the create version button, enters a name, and confirms
     * the creation, the version appears in the list.
     *
     * @throws Exception if any error occurs during the test
     */
    @Test
    public void testCreateConfirm() throws Exception {
        versionListViewTester.createVersionConfirm(versionName);
    }

    /**
     * Tests the behavior when attempting to create a version with a name that already exists.
     * <p>
     * Creates a version, then tries to create another version with the same name.
     * Verifies that the system displays an error and prevents the creation of the duplicate version.
     *
     * @throws Exception if any error occurs during the test
     */
    @Test
    public void testCreateDuplicateNameFails() throws Exception {
        // First, create a version
        versionListViewTester.createVersionConfirm(versionName);
        // Then try to create another version with the same name
        versionListViewTester.createVersionWithDuplicateName(versionName);
    }

    /**
     * Tests the behavior when attempting to delete a version but canceling the operation.
     * <p>
     * Creates a version, then attempts to delete it but cancels the confirmation dialog.
     * Verifies that the version remains in the list.
     *
     * @throws Exception if any error occurs during the test
     */
    @Test
    public void testDeleteCancel() throws Exception {
        versionListViewTester.createVersionConfirm(versionName);
        versionListViewTester.deleteVersionCancel(versionName);
    }

    /**
     * Tests the behavior when successfully deleting a version.
     * <p>
     * Creates a version, then deletes it by confirming the deletion in the confirmation dialog.
     * Verifies that the version is removed from the list.
     *
     * @throws Exception if any error occurs during the test
     */
    @Test
    public void testDeleteConfirm() throws Exception {
        versionListViewTester.createVersionConfirm(versionName);
        versionListViewTester.deleteVersionConfirm(versionName);
    }

    /**
     * Tests the behavior when attempting to edit a version but canceling the operation.
     * <p>
     * Creates a version, attempts to edit its name, but cancels the edit dialog.
     * Verifies that the original name remains unchanged and the new name is not present.
     *
     * @throws Exception if any error occurs during the test
     */
    @Test
    public void testEditCancel() throws Exception {
        versionListViewTester.createVersionConfirm(versionName);
        versionListViewTester.editVersionCancel(versionName, newVersionName);
    }

    /**
     * Tests the behavior when successfully editing a version.
     * <p>
     * Creates a version, edits its name, and confirms the edit.
     * Verifies that the version with the new name appears in the list and the old name is removed.
     *
     * @throws Exception if any error occurs during the test
     */
    @Test
    public void testEditConfirm() throws Exception {
        versionListViewTester.createVersionConfirm(versionName);
        versionListViewTester.editVersionConfirm(versionName, newVersionName);
    }

    /**
     * Tests the behavior when attempting to edit a version to have a name that already exists.
     * <p>
     * Creates two versions, then tries to update the second version to have the same name as the first.
     * Verifies that the system displays an error and prevents the update.
     *
     * @throws Exception if any error occurs during the test
     */
    @Test
    public void testEditDuplicateNameFails() throws Exception {
        // First, create two versions with different names
        versionListViewTester.createVersionConfirm(versionName);
        versionListViewTester.createVersionConfirm(newVersionName);

        // Then try to edit the second version to have the same name as the first
        versionListViewTester.editVersionWithDuplicateName(newVersionName, versionName);
    }
}
