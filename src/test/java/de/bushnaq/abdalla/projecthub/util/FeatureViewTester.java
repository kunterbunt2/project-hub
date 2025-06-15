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
import de.bushnaq.abdalla.projecthub.ui.SprintListView;
import de.bushnaq.abdalla.projecthub.ui.common.ConfirmDialog;
import de.bushnaq.abdalla.projecthub.ui.common.FeatureDialog;
import de.bushnaq.abdalla.projecthub.ui.util.selenium.SeleniumHandler;
import org.springframework.stereotype.Component;

import static de.bushnaq.abdalla.projecthub.ui.FeatureListView.FEATURE_GRID_NAME_PREFIX;

/**
 * Test helper class for interacting with the Feature UI components.
 * <p>
 * This class provides methods to test feature-related operations in the UI such as
 * creating, editing, deleting features and navigating between views. It uses
 * {@link SeleniumHandler} to interact with UI elements and validate results.
 * <p>
 * Features are organizational units within versions and contain sprints.
 * In the hierarchy: Products contain Versions, Versions contain Features, and Features contain Sprints.
 */
@Component
public class FeatureViewTester {
    private final SeleniumHandler seleniumHandler;

    /**
     * Constructs a new FeatureViewTester with the given Selenium handler.
     *
     * @param seleniumHandler the handler for Selenium operations
     */
    public FeatureViewTester(SeleniumHandler seleniumHandler) {
        this.seleniumHandler = seleniumHandler;
    }

    /**
     * Tests the creation of a feature where the user cancels the operation.
     * <p>
     * Opens the feature creation dialog, enters the given feature name, then cancels
     * the dialog. Verifies that no feature with the specified name appears in the feature list.
     *
     * @param name the name of the feature to attempt to create
     */
    public void createFeatureCancel(String name) {
        seleniumHandler.click(FeatureListView.CREATE_FEATURE_BUTTON_ID);
        seleniumHandler.setTextField(FeatureDialog.FEATURE_NAME_FIELD, name);
        seleniumHandler.click(FeatureDialog.CANCEL_BUTTON);
        seleniumHandler.ensureIsNotInList(FEATURE_GRID_NAME_PREFIX, name);
    }

    /**
     * Tests the successful creation of a feature.
     * <p>
     * Opens the feature creation dialog, enters the given feature name, then confirms
     * the dialog. Verifies that a feature with the specified name appears in the feature list.
     *
     * @param name the name of the feature to create
     */
    public void createFeatureConfirm(String name) {
        seleniumHandler.click(FeatureListView.CREATE_FEATURE_BUTTON_ID);
        seleniumHandler.setTextField(FeatureDialog.FEATURE_NAME_FIELD, name);
        seleniumHandler.click(FeatureDialog.CONFIRM_BUTTON);
        seleniumHandler.ensureIsInList(FEATURE_GRID_NAME_PREFIX, name);
    }

    /**
     * Tests feature deletion where the user cancels the delete confirmation.
     * <p>
     * Opens the context menu for the specified feature, selects the delete option,
     * then cancels the confirmation dialog. Verifies that the feature still exists in the list.
     *
     * @param name the name of the feature to attempt to delete
     */
    public void deleteFeatureCancel(String name) {
        seleniumHandler.click(FeatureListView.FEATURE_GRID_ACTION_BUTTON_PREFIX + name);
        seleniumHandler.click(FeatureListView.FEATURE_GRID_DELETE_BUTTON_PREFIX + name);
        seleniumHandler.click(ConfirmDialog.CANCEL_BUTTON);
        seleniumHandler.ensureIsInList(FEATURE_GRID_NAME_PREFIX, name);
    }

    /**
     * Tests the successful deletion of a feature.
     * <p>
     * Opens the context menu for the specified feature, selects the delete option,
     * then confirms the deletion in the confirmation dialog. Verifies that the feature
     * is removed from the feature list.
     *
     * @param name the name of the feature to delete
     */
    public void deleteFeatureConfirm(String name) {
        seleniumHandler.click(FeatureListView.FEATURE_GRID_ACTION_BUTTON_PREFIX + name);
        seleniumHandler.click(FeatureListView.FEATURE_GRID_DELETE_BUTTON_PREFIX + name);
        seleniumHandler.click(ConfirmDialog.CONFIRM_BUTTON);
        seleniumHandler.ensureIsNotInList(FEATURE_GRID_NAME_PREFIX, name);
    }

    /**
     * Tests feature editing where the user cancels the edit operation.
     * <p>
     * Opens the context menu for the specified feature, selects the edit option,
     * enters a new name, then cancels the edit dialog. Verifies that the feature
     * still exists with its original name and no feature with the new name exists.
     *
     * @param name    the original name of the feature to edit
     * @param newName the new name to attempt to assign to the feature
     */
    public void editFeatureCancel(String name, String newName) {
        seleniumHandler.click(FeatureListView.FEATURE_GRID_ACTION_BUTTON_PREFIX + name);
        seleniumHandler.click(FeatureListView.FEATURE_GRID_EDIT_BUTTON_PREFIX + name);

        seleniumHandler.setTextField(FeatureDialog.FEATURE_NAME_FIELD, newName);
        seleniumHandler.click(FeatureDialog.CANCEL_BUTTON);
        seleniumHandler.ensureIsInList(FEATURE_GRID_NAME_PREFIX, name);
        seleniumHandler.ensureIsNotInList(FEATURE_GRID_NAME_PREFIX, newName);
    }

    /**
     * Tests the successful editing of a feature.
     * <p>
     * Opens the context menu for the specified feature, selects the edit option,
     * enters a new name, then confirms the edit. Verifies that the feature with
     * the new name appears in the list and the feature with the old name is gone.
     *
     * @param name    the original name of the feature to edit
     * @param newName the new name to assign to the feature
     */
    public void editFeatureConfirm(String name, String newName) {
        seleniumHandler.click(FeatureListView.FEATURE_GRID_ACTION_BUTTON_PREFIX + name);
        seleniumHandler.click(FeatureListView.FEATURE_GRID_EDIT_BUTTON_PREFIX + name);

        seleniumHandler.setTextField(FeatureDialog.FEATURE_NAME_FIELD, newName);
        seleniumHandler.click(FeatureDialog.CONFIRM_BUTTON);
        seleniumHandler.ensureIsInList(FEATURE_GRID_NAME_PREFIX, newName);
        seleniumHandler.ensureIsNotInList(FEATURE_GRID_NAME_PREFIX, name);
    }

    /**
     * Selects a feature from the feature grid and navigates to its sprints.
     * <p>
     * Clicks on the specified feature row in the feature grid, which should
     * navigate to the SprintListView for that feature.
     *
     * @param name the name of the feature to select
     */
    public void selectFeature(String name) {
        seleniumHandler.selectGridRow(FEATURE_GRID_NAME_PREFIX, SprintListView.class, name);
    }
}
