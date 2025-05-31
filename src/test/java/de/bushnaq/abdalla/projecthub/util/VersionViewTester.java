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
import de.bushnaq.abdalla.projecthub.ui.VersionListView;
import de.bushnaq.abdalla.projecthub.ui.common.ConfirmDialog;
import de.bushnaq.abdalla.projecthub.ui.common.VersionDialog;
import de.bushnaq.abdalla.projecthub.ui.util.selenium.SeleniumHandler;
import org.springframework.stereotype.Component;

import static de.bushnaq.abdalla.projecthub.ui.VersionListView.VERSION_GRID_NAME_PREFIX;

@Component
public class VersionViewTester {
    private final SeleniumHandler seleniumHandler;

    public VersionViewTester(SeleniumHandler seleniumHandler) {
        this.seleniumHandler = seleniumHandler;
    }

    public void createVersionCancel(String name) {
        seleniumHandler.click(VersionListView.CREATE_VERSION_BUTTON);
        seleniumHandler.setTextField(VersionDialog.VERSION_NAME_FIELD, name);
        seleniumHandler.click(VersionDialog.CANCEL_BUTTON);
        seleniumHandler.ensureIsNotInList(VersionListView.VERSION_GRID_NAME_PREFIX, name);
    }

    public void createVersionConfirm(String name) {
        seleniumHandler.click(VersionListView.CREATE_VERSION_BUTTON);
        seleniumHandler.setTextField(VersionDialog.VERSION_NAME_FIELD, name);
        seleniumHandler.click(VersionDialog.CONFIRM_BUTTON);
        seleniumHandler.ensureIsInList(VersionListView.VERSION_GRID_NAME_PREFIX, name);
    }

    public void deleteVersionCancel(String name) {
        seleniumHandler.click(VersionListView.VERSION_GRID_ACTION_BUTTON_PREFIX + name);
        seleniumHandler.click(VersionListView.VERSION_GRID_DELETE_BUTTON_PREFIX + name);
        seleniumHandler.click(ConfirmDialog.CANCEL_BUTTON);
        seleniumHandler.ensureIsInList(VersionListView.VERSION_GRID_NAME_PREFIX, name);
    }

    public void deleteVersionConfirm(String name) {
        seleniumHandler.click(VersionListView.VERSION_GRID_ACTION_BUTTON_PREFIX + name);
        seleniumHandler.click(VersionListView.VERSION_GRID_DELETE_BUTTON_PREFIX + name);
        seleniumHandler.click(ConfirmDialog.CONFIRM_BUTTON);
        seleniumHandler.ensureIsNotInList(VersionListView.VERSION_GRID_NAME_PREFIX, name);
    }

    public void editVersionCancel(String name, String newName) {
        seleniumHandler.click(VersionListView.VERSION_GRID_ACTION_BUTTON_PREFIX + name);
        seleniumHandler.click(VersionListView.VERSION_GRID_EDIT_BUTTON_PREFIX + name);
        seleniumHandler.setTextField(VersionDialog.VERSION_NAME_FIELD, newName);
        seleniumHandler.click(VersionDialog.CANCEL_BUTTON);
        seleniumHandler.ensureIsInList(VersionListView.VERSION_GRID_NAME_PREFIX, name);
        seleniumHandler.ensureIsNotInList(VersionListView.VERSION_GRID_NAME_PREFIX, newName);
    }

    public void editVersionConfirm(String name, String newName) {
        seleniumHandler.click(VersionListView.VERSION_GRID_ACTION_BUTTON_PREFIX + name);
        seleniumHandler.click(VersionListView.VERSION_GRID_EDIT_BUTTON_PREFIX + name);
        seleniumHandler.setTextField(VersionDialog.VERSION_NAME_FIELD, newName);
        seleniumHandler.click(VersionDialog.CONFIRM_BUTTON);
        seleniumHandler.ensureIsInList(VersionListView.VERSION_GRID_NAME_PREFIX, newName);
        seleniumHandler.ensureIsNotInList(VersionListView.VERSION_GRID_NAME_PREFIX, name);
    }

    public void selectVersion(String name) {
        seleniumHandler.selectGridRow(VERSION_GRID_NAME_PREFIX, ProjectListView.class, name);
    }

}
