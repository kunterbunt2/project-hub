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
import de.bushnaq.abdalla.projecthub.util.ProductViewTester;
import de.bushnaq.abdalla.projecthub.util.VersionViewTester;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
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
 * These tests use {@link VersionViewTester} to interact with the UI elements
 * and verify the expected behavior. Each test requires a product to be created first
 * as versions exist within the context of a product.
 */
@ExtendWith(SpringExtension.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT, properties = "server.port=8080")
@AutoConfigureMockMvc
@Transactional
public class VersionListViewTest extends AbstractUiTestUtil {
    private final String            newVersionName = "NewVersion-2";
    private final String            productName    = "Product-2";
    @Autowired
    private       ProductViewTester productViewTester;
    private final String            versionName    = "Version-2";
    @Autowired
    private       VersionViewTester versionViewTester;

    /**
     * Setup method that runs before each test.
     * <p>
     * Creates a product and navigates to its versions view, setting up the environment
     * for testing version-related operations.
     *
     * @throws Exception if any error occurs during setup
     */
    @BeforeEach
    public void createProduct() throws Exception {
        productViewTester.switchToProductListView();
        productViewTester.createProductConfirm(productName);
        productViewTester.selectProduct(productName);
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
        versionViewTester.createVersionCancel(versionName);
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
        versionViewTester.createVersionConfirm(versionName);
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
        versionViewTester.createVersionConfirm(versionName);
        versionViewTester.deleteVersionCancel(versionName);
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
        versionViewTester.createVersionConfirm(versionName);
        versionViewTester.deleteVersionConfirm(versionName);
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
        versionViewTester.createVersionConfirm(versionName);
        versionViewTester.editVersionCancel(versionName, newVersionName);
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
        versionViewTester.createVersionConfirm(versionName);
        versionViewTester.editVersionConfirm(versionName, newVersionName);
    }
}
