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

import de.bushnaq.abdalla.projecthub.ui.ProjectListView;
import de.bushnaq.abdalla.projecthub.ui.SprintListView;
import de.bushnaq.abdalla.projecthub.ui.common.ConfirmDialog;
import de.bushnaq.abdalla.projecthub.ui.common.ProjectDialog;
import de.bushnaq.abdalla.projecthub.ui.util.selenium.SeleniumHandler;
import org.springframework.stereotype.Component;

import static de.bushnaq.abdalla.projecthub.ui.ProjectListView.PROJECT_GRID_NAME_PREFIX;

/**
 * Test helper class for interacting with the Project UI components.
 * <p>
 * This class provides methods to test project-related operations in the UI such as
 * creating, editing, deleting projects and navigating between views. It uses
 * {@link SeleniumHandler} to interact with UI elements and validate results.
 * <p>
 * Projects are organizational units within versions and contain sprints.
 * In the hierarchy: Products contain Versions, Versions contain Projects, and Projects contain Sprints.
 */
@Component
public class ProjectViewTester {
    private final SeleniumHandler seleniumHandler;

    /**
     * Constructs a new ProjectViewTester with the given Selenium handler.
     *
     * @param seleniumHandler the handler for Selenium operations
     */
    public ProjectViewTester(SeleniumHandler seleniumHandler) {
        this.seleniumHandler = seleniumHandler;
    }

    /**
     * Tests the creation of a project where the user cancels the operation.
     * <p>
     * Opens the project creation dialog, enters the given project name, then cancels
     * the dialog. Verifies that no project with the specified name appears in the project list.
     *
     * @param name the name of the project to attempt to create
     */
    public void createProjectCancel(String name) {
        seleniumHandler.click(ProjectListView.CREATE_PROJECT_BUTTON_ID);
        seleniumHandler.setTextField(ProjectDialog.PROJECT_NAME_FIELD, name);
        seleniumHandler.click(ProjectDialog.CANCEL_BUTTON);
        seleniumHandler.ensureIsNotInList(PROJECT_GRID_NAME_PREFIX, name);
    }

    /**
     * Tests the successful creation of a project.
     * <p>
     * Opens the project creation dialog, enters the given project name, then confirms
     * the dialog. Verifies that a project with the specified name appears in the project list.
     *
     * @param name the name of the project to create
     */
    public void createProjectConfirm(String name) {
        seleniumHandler.click(ProjectListView.CREATE_PROJECT_BUTTON_ID);
        seleniumHandler.setTextField(ProjectDialog.PROJECT_NAME_FIELD, name);
        seleniumHandler.click(ProjectDialog.CONFIRM_BUTTON);
        seleniumHandler.ensureIsInList(PROJECT_GRID_NAME_PREFIX, name);
    }

    /**
     * Tests project deletion where the user cancels the delete confirmation.
     * <p>
     * Opens the context menu for the specified project, selects the delete option,
     * then cancels the confirmation dialog. Verifies that the project still exists in the list.
     *
     * @param name the name of the project to attempt to delete
     */
    public void deleteProjectCancel(String name) {
        seleniumHandler.click(ProjectListView.PROJECT_GRID_ACTION_BUTTON_PREFIX + name);
        seleniumHandler.click(ProjectListView.PROJECT_GRID_DELETE_BUTTON_PREFIX + name);
        seleniumHandler.click(ConfirmDialog.CANCEL_BUTTON);
        seleniumHandler.ensureIsInList(PROJECT_GRID_NAME_PREFIX, name);
    }

    /**
     * Tests the successful deletion of a project.
     * <p>
     * Opens the context menu for the specified project, selects the delete option,
     * then confirms the deletion in the confirmation dialog. Verifies that the project
     * is removed from the project list.
     *
     * @param name the name of the project to delete
     */
    public void deleteProjectConfirm(String name) {
        seleniumHandler.click(ProjectListView.PROJECT_GRID_ACTION_BUTTON_PREFIX + name);
        seleniumHandler.click(ProjectListView.PROJECT_GRID_DELETE_BUTTON_PREFIX + name);
        seleniumHandler.click(ConfirmDialog.CONFIRM_BUTTON);
        seleniumHandler.ensureIsNotInList(PROJECT_GRID_NAME_PREFIX, name);
    }

    /**
     * Tests project editing where the user cancels the edit operation.
     * <p>
     * Opens the context menu for the specified project, selects the edit option,
     * enters a new name, then cancels the edit dialog. Verifies that the project
     * still exists with its original name and no project with the new name exists.
     *
     * @param name    the original name of the project to edit
     * @param newName the new name to attempt to assign to the project
     */
    public void editProjectCancel(String name, String newName) {
        seleniumHandler.click(ProjectListView.PROJECT_GRID_ACTION_BUTTON_PREFIX + name);
        seleniumHandler.click(ProjectListView.PROJECT_GRID_EDIT_BUTTON_PREFIX + name);

        seleniumHandler.setTextField(ProjectDialog.PROJECT_NAME_FIELD, newName);
        seleniumHandler.click(ProjectDialog.CANCEL_BUTTON);
        seleniumHandler.ensureIsInList(PROJECT_GRID_NAME_PREFIX, name);
        seleniumHandler.ensureIsNotInList(PROJECT_GRID_NAME_PREFIX, newName);
    }

    /**
     * Tests the successful editing of a project.
     * <p>
     * Opens the context menu for the specified project, selects the edit option,
     * enters a new name, then confirms the edit. Verifies that the project with
     * the new name appears in the list and the project with the old name is gone.
     *
     * @param name    the original name of the project to edit
     * @param newName the new name to assign to the project
     */
    public void editProjectConfirm(String name, String newName) {
        seleniumHandler.click(ProjectListView.PROJECT_GRID_ACTION_BUTTON_PREFIX + name);
        seleniumHandler.click(ProjectListView.PROJECT_GRID_EDIT_BUTTON_PREFIX + name);

        seleniumHandler.setTextField(ProjectDialog.PROJECT_NAME_FIELD, newName);
        seleniumHandler.click(ProjectDialog.CONFIRM_BUTTON);
        seleniumHandler.ensureIsInList(PROJECT_GRID_NAME_PREFIX, newName);
        seleniumHandler.ensureIsNotInList(PROJECT_GRID_NAME_PREFIX, name);
    }

    /**
     * Selects a project from the project grid and navigates to its sprints.
     * <p>
     * Clicks on the specified project row in the project grid, which should
     * navigate to the SprintListView for that project.
     *
     * @param name the name of the project to select
     */
    public void selectProject(String name) {
        seleniumHandler.selectGridRow(PROJECT_GRID_NAME_PREFIX, SprintListView.class, name);
    }
}
