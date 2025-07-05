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

package de.bushnaq.abdalla.projecthub.ui.dialog;

import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import de.bushnaq.abdalla.projecthub.dto.Sprint;
import de.bushnaq.abdalla.projecthub.ui.util.VaadinUtils;

/**
 * A reusable dialog for creating and editing sprints.
 */
public class SprintDialog extends Dialog {

    public static final String       CANCEL_BUTTON     = "cancel-sprint-button";
    public static final String       CONFIRM_BUTTON    = "save-sprint-button";
    public static final String       SPRINT_DIALOG     = "sprint-dialog";
    public static final String       SPRINT_NAME_FIELD = "sprint-name-field";
    private final       boolean      isEditMode;
    private final       TextField    nameField;
    private final       SaveCallback saveCallback;
    private final       Sprint       sprint;

    /**
     * Creates a dialog for creating or editing a sprint.
     *
     * @param sprint       The sprint to edit, or null for creating a new sprint
     * @param saveCallback Callback that receives the sprint with updated values and a reference to this dialog
     */
    public SprintDialog(Sprint sprint, SaveCallback saveCallback) {
        this.sprint       = sprint;
        this.saveCallback = saveCallback;
        isEditMode        = sprint != null;

        // Set the dialog title with an icon
        String title = isEditMode ? "Edit Sprint" : "Create Sprint";

        // Create a custom header with icon
        HorizontalLayout headerLayout = new HorizontalLayout();
        headerLayout.setAlignItems(FlexComponent.Alignment.CENTER);
        headerLayout.setSpacing(true);

        Icon titleIcon = new Icon("vaadin:exit");
        titleIcon.getStyle().set("margin-right", "0.5em");

        com.vaadin.flow.component.html.H3 titleLabel = new com.vaadin.flow.component.html.H3(title);
        titleLabel.getStyle().set("margin", "0");

        headerLayout.add(titleIcon, titleLabel);

        // Set the custom header
        setHeaderTitle(null); // Clear the default title
        getHeader().add(headerLayout);

        setId(SPRINT_DIALOG);
        setWidth("480px");

        VerticalLayout dialogLayout = new VerticalLayout();
        dialogLayout.setPadding(false);
        dialogLayout.setSpacing(true);

        nameField = new TextField("Sprint Name");
        nameField.setId(SPRINT_NAME_FIELD);
        nameField.setWidthFull();
        nameField.setPrefixComponent(new Icon("vaadin:exit"));
        nameField.setRequired(true);
        // Add helper text explaining the uniqueness requirement
        nameField.setHelperText("Sprint name must be unique");

        if (isEditMode) {
            nameField.setValue(sprint.getName());
        }

        dialogLayout.add(nameField);

        dialogLayout.add(VaadinUtils.createDialogButtonLayout("Save", CONFIRM_BUTTON, "Cancel", CANCEL_BUTTON, this::save, this));
        add(dialogLayout);
    }

    private void save() {
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

        // Call the save callback with the sprint and a reference to this dialog
        saveCallback.save(sprintToSave, this);

    }

    /**
     * Sets an error message on the name field to indicate uniqueness violation
     *
     * @param errorMessage The error message to show
     */
    public void setErrorMessage(String errorMessage) {
        nameField.setErrorMessage(errorMessage);
        nameField.setInvalid(true);
    }

    /**
     * Functional interface for the save callback that receives the edited sprint and a reference to this dialog
     */
    @FunctionalInterface
    public interface SaveCallback {
        void save(Sprint sprint, SprintDialog dialog);
    }
}
