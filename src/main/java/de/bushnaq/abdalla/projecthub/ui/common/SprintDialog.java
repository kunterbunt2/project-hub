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
import de.bushnaq.abdalla.projecthub.dto.Sprint;

import java.util.function.Consumer;

/**
 * A reusable dialog for creating and editing sprints.
 */
public class SprintDialog extends Dialog {

    public static final String CANCEL_BUTTON     = "cancel-sprint-button";
    public static final String CONFIRM_BUTTON    = "save-sprint-button";
    public static final String SPRINT_NAME_FIELD = "sprint-name-field";

    /**
     * Creates a dialog for creating or editing a sprint.
     *
     * @param sprint       The sprint to edit, or null for creating a new sprint
     * @param saveCallback Callback that receives the sprint with updated values
     */
    public SprintDialog(Sprint sprint, Consumer<Sprint> saveCallback) {
        boolean isEditMode = sprint != null;

        setHeaderTitle(isEditMode ? "Edit Sprint" : "Create Sprint");

        VerticalLayout dialogLayout = new VerticalLayout();
        dialogLayout.setPadding(false);
        dialogLayout.setSpacing(true);

        TextField nameField = new TextField("Sprint Name");
        nameField.setId(SPRINT_NAME_FIELD);
        nameField.setWidthFull();
        nameField.setRequired(true);

        if (isEditMode) {
            nameField.setValue(sprint.getName());
        }

        dialogLayout.add(nameField);

        Button saveButton = new Button("Save", e -> {
            if (nameField.getValue().trim().isEmpty()) {
                Notification.show("Please enter a sprint name", 3000, Notification.Position.MIDDLE);
                return;
            }

            Sprint sprintToSave;
            if (isEditMode) {
                sprintToSave = sprint;
                sprintToSave.setName(nameField.getValue().trim());
            } else {
                sprintToSave = new Sprint();
                sprintToSave.setName(nameField.getValue().trim());
            }

            saveCallback.accept(sprintToSave);
            close();
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
}
