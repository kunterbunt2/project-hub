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

@Component
public class ProjectViewTester {
    private final SeleniumHandler seleniumHandler;

    public ProjectViewTester(SeleniumHandler seleniumHandler) {
        this.seleniumHandler = seleniumHandler;
    }

    public void createProjectCancel(String name) {
        seleniumHandler.click(ProjectListView.CREATE_PROJECT_BUTTON_ID);
        seleniumHandler.setTextField(ProjectDialog.PROJECT_NAME_FIELD, name);
        seleniumHandler.click(ProjectDialog.CANCEL_BUTTON);
        seleniumHandler.ensureIsNotInList(PROJECT_GRID_NAME_PREFIX, name);
    }

    public void createProjectConfirm(String name) {
        seleniumHandler.click(ProjectListView.CREATE_PROJECT_BUTTON_ID);
        seleniumHandler.setTextField(ProjectDialog.PROJECT_NAME_FIELD, name);
        seleniumHandler.click(ProjectDialog.CONFIRM_BUTTON);
        seleniumHandler.ensureIsInList(PROJECT_GRID_NAME_PREFIX, name);
    }

    public void deleteProjectCancel(String name) {
        seleniumHandler.click(ProjectListView.PROJECT_GRID_ACTION_BUTTON_PREFIX + name);
        seleniumHandler.click(ProjectListView.PROJECT_GRID_DELETE_BUTTON_PREFIX + name);
        seleniumHandler.click(ConfirmDialog.CANCEL_BUTTON);
        seleniumHandler.ensureIsInList(PROJECT_GRID_NAME_PREFIX, name);
    }

    public void deleteProjectConfirm(String name) {
        seleniumHandler.click(ProjectListView.PROJECT_GRID_ACTION_BUTTON_PREFIX + name);
        seleniumHandler.click(ProjectListView.PROJECT_GRID_DELETE_BUTTON_PREFIX + name);
        seleniumHandler.click(ConfirmDialog.CONFIRM_BUTTON);
        seleniumHandler.ensureIsNotInList(PROJECT_GRID_NAME_PREFIX, name);
    }

    public void editProjectCancel(String name, String newName) {
        seleniumHandler.click(ProjectListView.PROJECT_GRID_ACTION_BUTTON_PREFIX + name);
        seleniumHandler.click(ProjectListView.PROJECT_GRID_EDIT_BUTTON_PREFIX + name);

        seleniumHandler.setTextField(ProjectDialog.PROJECT_NAME_FIELD, newName);
        seleniumHandler.click(ProjectDialog.CANCEL_BUTTON);
        seleniumHandler.ensureIsInList(PROJECT_GRID_NAME_PREFIX, name);
        seleniumHandler.ensureIsNotInList(PROJECT_GRID_NAME_PREFIX, newName);
    }

    public void editProjectConfirm(String name, String newName) {
        seleniumHandler.click(ProjectListView.PROJECT_GRID_ACTION_BUTTON_PREFIX + name);
        seleniumHandler.click(ProjectListView.PROJECT_GRID_EDIT_BUTTON_PREFIX + name);

        seleniumHandler.setTextField(ProjectDialog.PROJECT_NAME_FIELD, newName);
        seleniumHandler.click(ProjectDialog.CONFIRM_BUTTON);
        seleniumHandler.ensureIsInList(PROJECT_GRID_NAME_PREFIX, newName);
        seleniumHandler.ensureIsNotInList(PROJECT_GRID_NAME_PREFIX, name);
    }

    public void selectProject(String name) {
        seleniumHandler.selectGridRow(PROJECT_GRID_NAME_PREFIX, SprintListView.class, name);
    }
}
