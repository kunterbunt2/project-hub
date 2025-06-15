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
import de.bushnaq.abdalla.projecthub.dto.Feature;

import java.util.function.Consumer;

/**
 * A reusable dialog for creating and editing features.
 */
public class FeatureDialog extends Dialog {

    public static final String  CANCEL_BUTTON      = "cancel-feature-button";
    public static final String  CONFIRM_BUTTON     = "save-feature-button";
    public static final String  FEATURE_DIALOG     = "feature-dialog";
    public static final String  FEATURE_NAME_FIELD = "feature-name-field";
    private final       boolean isEditMode;

    /**
     * Creates a dialog for creating or editing a feature.
     *
     * @param feature      The feature to edit, or null for creating a new feature
     * @param saveCallback Callback that receives the feature with updated values
     */
    public FeatureDialog(Feature feature, Consumer<Feature> saveCallback) {
        isEditMode = feature != null;

        setHeaderTitle(isEditMode ? "Edit Feature" : "Create Feature");
        setId(FEATURE_DIALOG);

        VerticalLayout dialogLayout = new VerticalLayout();
        dialogLayout.setPadding(false);
        dialogLayout.setSpacing(true);

        TextField nameField = new TextField("Feature Name");
        nameField.setId(FEATURE_NAME_FIELD);
        nameField.setWidthFull();
        nameField.setRequired(true);

        if (isEditMode) {
            nameField.setValue(feature.getName());
        }

        dialogLayout.add(nameField);

        Button saveButton = new Button("Save", e -> {
            if (nameField.getValue().trim().isEmpty()) {
                Notification.show("Please enter a feature name", 3000, Notification.Position.MIDDLE);
                return;
            }

            Feature featureToSave;
            if (isEditMode) {
                featureToSave = feature;
                featureToSave.setName(nameField.getValue().trim());
            } else {
                featureToSave = new Feature();
                featureToSave.setName(nameField.getValue().trim());
            }

            saveCallback.accept(featureToSave);
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
