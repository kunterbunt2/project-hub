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

package de.bushnaq.abdalla.projecthub.ui.common;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import de.bushnaq.abdalla.projecthub.dto.Version;

/**
 * A reusable dialog for creating and editing versions.
 */
public class VersionDialog extends Dialog {

    public static final String    CANCEL_BUTTON      = "cancel-version-button";
    public static final String    CONFIRM_BUTTON     = "save-version-button";
    public static final String    VERSION_DIALOG     = "version-dialog";
    public static final String    VERSION_NAME_FIELD = "version-name-field";
    private final       boolean   isEditMode;
    private final       TextField nameField;

    /**
     * Creates a dialog for creating or editing a version.
     *
     * @param version      The version to edit, or null for creating a new version
     * @param saveCallback Callback that receives the version with updated values and a reference to this dialog
     */
    public VersionDialog(Version version, SaveCallback saveCallback) {
        isEditMode = version != null;

        // Set the dialog title
        setHeaderTitle(isEditMode ? "Edit Version" : "Create Version");
        setId(VERSION_DIALOG);

        VerticalLayout dialogLayout = new VerticalLayout();
        dialogLayout.setPadding(false);
        dialogLayout.setSpacing(true);

        nameField = new TextField("Version Name");
        nameField.setId(VERSION_NAME_FIELD);
        nameField.setWidthFull();
        nameField.setRequired(true);
        // Add helper text explaining the uniqueness requirement
        nameField.setHelperText("Version name must be unique");

        if (isEditMode) {
            nameField.setValue(version.getName());
        }

        dialogLayout.add(nameField);

        Button saveButton = new Button("Save", e -> {
            if (nameField.getValue().trim().isEmpty()) {
                Notification.show("Please enter a version name", 3000, Notification.Position.MIDDLE);
                return;
            }

            Version versionToSave;
            if (isEditMode) {
                versionToSave = version;
                versionToSave.setName(nameField.getValue().trim());
            } else {
                versionToSave = new Version();
                versionToSave.setName(nameField.getValue().trim());
            }

            // Call the save callback with the version and a reference to this dialog
            saveCallback.save(versionToSave, this);
        });
        saveButton.setId(CONFIRM_BUTTON);
        saveButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

        Button cancelButton = new Button("Cancel", e -> close());
        cancelButton.setId(CANCEL_BUTTON);

        HorizontalLayout buttonLayout = new HorizontalLayout();
        buttonLayout.setJustifyContentMode(FlexComponent.JustifyContentMode.END);
        buttonLayout.add(cancelButton, saveButton);
        buttonLayout.setWidthFull();

        dialogLayout.add(buttonLayout);
        add(dialogLayout);
    }

    /**
     * Sets an error message on the name field to indicate uniqueness violation
     *
     * @param errorMessage The error message to show
     */
    public void setNameFieldError(String errorMessage) {
        nameField.setInvalid(errorMessage != null);
        nameField.setErrorMessage(errorMessage);
    }

    /**
     * Functional interface for the save callback that receives the edited version and a reference to this dialog
     */
    @FunctionalInterface
    public interface SaveCallback {
        void save(Version version, VersionDialog dialog);
    }
}
