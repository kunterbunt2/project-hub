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

import de.bushnaq.abdalla.projecthub.ui.FeatureListView;
import de.bushnaq.abdalla.projecthub.ui.VersionListView;
import de.bushnaq.abdalla.projecthub.ui.common.ConfirmDialog;
import de.bushnaq.abdalla.projecthub.ui.common.VersionDialog;
import de.bushnaq.abdalla.projecthub.ui.util.selenium.SeleniumHandler;
import org.springframework.stereotype.Component;

import static de.bushnaq.abdalla.projecthub.ui.VersionListView.VERSION_GRID_NAME_PREFIX;

/**
 * Test helper class for interacting with the Version UI components.
 * <p>
 * This class provides methods to test version-related operations in the UI such as
 * creating, editing, deleting versions and navigating between views. It uses
 * {@link SeleniumHandler} to interact with UI elements and validate results.
 * <p>
 * Versions represent a specific release of a product and contain multiple features.
 */
@Component
public class VersionViewTester {
    private final SeleniumHandler seleniumHandler;

    /**
     * Constructs a new VersionViewTester with the given Selenium handler.
     *
     * @param seleniumHandler the handler for Selenium operations
     */
    public VersionViewTester(SeleniumHandler seleniumHandler) {
        this.seleniumHandler = seleniumHandler;
    }

    /**
     * Tests the creation of a version where the user cancels the operation.
     * <p>
     * Opens the version creation dialog, enters the given version name, then cancels
     * the dialog. Verifies that no version with the specified name appears in the version list.
     *
     * @param name the name of the version to attempt to create
     */
    public void createVersionCancel(String name) {
        seleniumHandler.click(VersionListView.CREATE_VERSION_BUTTON);
        seleniumHandler.setTextField(VersionDialog.VERSION_NAME_FIELD, name);
        seleniumHandler.click(VersionDialog.CANCEL_BUTTON);
        seleniumHandler.ensureIsNotInList(VersionListView.VERSION_GRID_NAME_PREFIX, name);
    }

    /**
     * Tests the successful creation of a version.
     * <p>
     * Opens the version creation dialog, enters the given version name, then confirms
     * the dialog. Verifies that a version with the specified name appears in the version list.
     *
     * @param name the name of the version to create
     */
    public void createVersionConfirm(String name) {
        seleniumHandler.click(VersionListView.CREATE_VERSION_BUTTON);
        seleniumHandler.setTextField(VersionDialog.VERSION_NAME_FIELD, name);
        seleniumHandler.click(VersionDialog.CONFIRM_BUTTON);
        seleniumHandler.ensureIsInList(VersionListView.VERSION_GRID_NAME_PREFIX, name);
    }

    /**
     * Tests version deletion where the user cancels the delete confirmation.
     * <p>
     * Opens the context menu for the specified version, selects the delete option,
     * then cancels the confirmation dialog. Verifies that the version still exists in the list.
     *
     * @param name the name of the version to attempt to delete
     */
    public void deleteVersionCancel(String name) {
        seleniumHandler.click(VersionListView.VERSION_GRID_ACTION_BUTTON_PREFIX + name);
        seleniumHandler.click(VersionListView.VERSION_GRID_DELETE_BUTTON_PREFIX + name);
        seleniumHandler.click(ConfirmDialog.CANCEL_BUTTON);
        seleniumHandler.ensureIsInList(VersionListView.VERSION_GRID_NAME_PREFIX, name);
    }

    /**
     * Tests the successful deletion of a version.
     * <p>
     * Opens the context menu for the specified version, selects the delete option,
     * then confirms the deletion in the confirmation dialog. Verifies that the version
     * is removed from the version list.
     *
     * @param name the name of the version to delete
     */
    public void deleteVersionConfirm(String name) {
        seleniumHandler.click(VersionListView.VERSION_GRID_ACTION_BUTTON_PREFIX + name);
        seleniumHandler.click(VersionListView.VERSION_GRID_DELETE_BUTTON_PREFIX + name);
        seleniumHandler.click(ConfirmDialog.CONFIRM_BUTTON);
        seleniumHandler.ensureIsNotInList(VersionListView.VERSION_GRID_NAME_PREFIX, name);
    }

    /**
     * Tests version editing where the user cancels the edit operation.
     * <p>
     * Opens the context menu for the specified version, selects the edit option,
     * enters a new name, then cancels the edit dialog. Verifies that the version
     * still exists with its original name and no version with the new name exists.
     *
     * @param name    the original name of the version to edit
     * @param newName the new name to attempt to assign to the version
     */
    public void editVersionCancel(String name, String newName) {
        seleniumHandler.click(VersionListView.VERSION_GRID_ACTION_BUTTON_PREFIX + name);
        seleniumHandler.click(VersionListView.VERSION_GRID_EDIT_BUTTON_PREFIX + name);
        seleniumHandler.setTextField(VersionDialog.VERSION_NAME_FIELD, newName);
        seleniumHandler.click(VersionDialog.CANCEL_BUTTON);
        seleniumHandler.ensureIsInList(VersionListView.VERSION_GRID_NAME_PREFIX, name);
        seleniumHandler.ensureIsNotInList(VersionListView.VERSION_GRID_NAME_PREFIX, newName);
    }

    /**
     * Tests the successful editing of a version.
     * <p>
     * Opens the context menu for the specified version, selects the edit option,
     * enters a new name, then confirms the edit. Verifies that the version with
     * the new name appears in the list and the version with the old name is gone.
     *
     * @param name    the original name of the version to edit
     * @param newName the new name to assign to the version
     */
    public void editVersionConfirm(String name, String newName) {
        seleniumHandler.click(VersionListView.VERSION_GRID_ACTION_BUTTON_PREFIX + name);
        seleniumHandler.click(VersionListView.VERSION_GRID_EDIT_BUTTON_PREFIX + name);
        seleniumHandler.setTextField(VersionDialog.VERSION_NAME_FIELD, newName);
        seleniumHandler.click(VersionDialog.CONFIRM_BUTTON);
        seleniumHandler.ensureIsInList(VersionListView.VERSION_GRID_NAME_PREFIX, newName);
        seleniumHandler.ensureIsNotInList(VersionListView.VERSION_GRID_NAME_PREFIX, name);
    }

    /**
     * Selects a version from the version grid and navigates to its features.
     * <p>
     * Clicks on the specified version row in the version grid, which should
     * navigate to the FeatureListView for that version.
     *
     * @param name the name of the version to select
     */
    public void selectVersion(String name) {
        seleniumHandler.selectGridRow(VERSION_GRID_NAME_PREFIX, FeatureListView.class, name);
    }
}
