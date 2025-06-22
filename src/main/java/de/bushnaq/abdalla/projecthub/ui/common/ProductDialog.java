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
import de.bushnaq.abdalla.projecthub.dto.Product;

/**
 * A reusable dialog for creating and editing products.
 */
public class ProductDialog extends Dialog {

    public static final String    CANCEL_BUTTON      = "cancel-product-button";
    public static final String    CONFIRM_BUTTON     = "save-product-button";
    public static final String    PRODUCT_DIALOG     = "product-dialog";
    public static final String    PRODUCT_NAME_FIELD = "product-name-field";
    private final       boolean   isEditMode;
    private final       TextField nameField;

    /**
     * Creates a dialog for creating or editing a product.
     *
     * @param product      The product to edit, or null for creating a new product
     * @param saveCallback Callback that receives the product with updated values and a reference to this dialog
     */
    public ProductDialog(Product product, SaveCallback saveCallback) {
        isEditMode = product != null;

        // Set the dialog title
        setHeaderTitle(isEditMode ? "Edit Product" : "Create Product");
        setId(PRODUCT_DIALOG);

        VerticalLayout dialogLayout = new VerticalLayout();
        dialogLayout.setPadding(false);
        dialogLayout.setSpacing(true);

        nameField = new TextField("Product Name");
        nameField.setId(PRODUCT_NAME_FIELD);
        nameField.setWidthFull();
        nameField.setRequired(true);
        // Add helper text explaining the uniqueness requirement
        nameField.setHelperText("Product name must be unique");

        if (isEditMode) {
            nameField.setValue(product.getName());
        }

        dialogLayout.add(nameField);

        Button saveButton = new Button("Save", e -> {
            if (nameField.getValue().trim().isEmpty()) {
                Notification.show("Please enter a product name", 3000, Notification.Position.MIDDLE);
                return;
            }

            Product productToSave;
            if (isEditMode) {
                productToSave = product;
                productToSave.setName(nameField.getValue().trim());
            } else {
                productToSave = new Product();
                productToSave.setName(nameField.getValue().trim());
            }

            // Call the save callback with the product and a reference to this dialog
            saveCallback.save(productToSave, this);
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
     * Sets an error message on the product name field.
     *
     * @param errorMessage The error message to display, or null to clear the error
     */
    public void setNameFieldError(String errorMessage) {
        nameField.setInvalid(errorMessage != null);
        nameField.setErrorMessage(errorMessage);
    }

    /**
     * Functional interface for the save callback that receives both the product and a reference to this dialog
     */
    @FunctionalInterface
    public interface SaveCallback {
        void save(Product product, ProductDialog dialog);
    }
}