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
import de.bushnaq.abdalla.projecthub.dto.Project;

import java.util.function.Consumer;

/**
 * A reusable dialog for creating and editing projects.
 */
public class ProjectDialog extends Dialog {

    public static final String CANCEL_BUTTON      = "cancel-project-button";
    public static final String CONFIRM_BUTTON     = "save-project-button";
    public static final String PROJECT_NAME_FIELD = "project-name-field";
//    public static final String PROJECT_REQUESTER_FIELD = "project-requester-field";

    /**
     * Creates a dialog for creating or editing a project.
     *
     * @param project      The project to edit, or null for creating a new project
     * @param saveCallback Callback that receives the project with updated values
     */
    public ProjectDialog(Project project, Consumer<Project> saveCallback) {
        boolean isEditMode = project != null;

        setHeaderTitle(isEditMode ? "Edit Project" : "Create Project");

        VerticalLayout dialogLayout = new VerticalLayout();
        dialogLayout.setPadding(false);
        dialogLayout.setSpacing(true);

        TextField nameField = new TextField("Project Name");
        nameField.setId(PROJECT_NAME_FIELD);
        nameField.setWidthFull();
        nameField.setRequired(true);

        if (isEditMode) {
            nameField.setValue(project.getName());
        }

        dialogLayout.add(nameField);

        Button saveButton = new Button("Save", e -> {
            if (nameField.getValue().trim().isEmpty()) {
                Notification.show("Please enter a project name", 3000, Notification.Position.MIDDLE);
                return;
            }

            Project projectToSave;
            if (isEditMode) {
                projectToSave = project;
                projectToSave.setName(nameField.getValue().trim());
//                projectToSave.setRequester(requesterField.getValue().trim());
            } else {
                projectToSave = new Project();
                projectToSave.setName(nameField.getValue().trim());
//                projectToSave.setRequester(requesterField.getValue().trim());
            }

            saveCallback.accept(projectToSave);
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
