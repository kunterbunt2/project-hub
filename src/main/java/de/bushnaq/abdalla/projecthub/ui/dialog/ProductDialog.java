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
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import de.bushnaq.abdalla.projecthub.dto.Product;
import de.bushnaq.abdalla.projecthub.ui.util.VaadinUtils;

/**
 * A reusable dialog for creating and editing products.
 */
public class ProductDialog extends Dialog {

    public static final String       CANCEL_BUTTON      = "cancel-product-button";
    public static final String       CONFIRM_BUTTON     = "save-product-button";
    public static final String       PRODUCT_DIALOG     = "product-dialog";
    public static final String       PRODUCT_NAME_FIELD = "product-name-field";
    private final       boolean      isEditMode;
    private final       TextField    nameField;
    private final       Product      product;
    private final       SaveCallback saveCallback;

    /**
     * Creates a dialog for creating or editing a product.
     *
     * @param product      The product to edit, or null for creating a new product
     * @param saveCallback Callback that receives the product with updated values and a reference to this dialog
     */
    public ProductDialog(Product product, SaveCallback saveCallback) {
        this.product      = product;
        this.saveCallback = saveCallback;
        isEditMode        = product != null;

        // Set the dialog title with an icon
        String title = isEditMode ? "Edit Product" : "Create Product";

        // Create a custom header with icon
        HorizontalLayout headerLayout = new HorizontalLayout();
        headerLayout.setAlignItems(FlexComponent.Alignment.CENTER);
        headerLayout.setSpacing(true);

        Icon titleIcon = new Icon(VaadinIcon.CUBE);
        titleIcon.getStyle().set("margin-right", "0.5em");

        com.vaadin.flow.component.html.H3 titleLabel = new com.vaadin.flow.component.html.H3(title);
        titleLabel.getStyle().set("margin", "0");

        headerLayout.add(titleIcon, titleLabel);

        // Set the custom header
        setHeaderTitle(null); // Clear the default title
        getHeader().add(headerLayout);

        setId(PRODUCT_DIALOG);
        setWidth("480px");

        VerticalLayout dialogLayout = new VerticalLayout();
        dialogLayout.setPadding(false);
        dialogLayout.setSpacing(true);

        // Create name field with icon
        nameField = new TextField("Product Name");
        nameField.setId(PRODUCT_NAME_FIELD);
        nameField.setWidthFull();
        nameField.setRequired(true);
        // Add helper text explaining the uniqueness requirement
        nameField.setHelperText("Product name must be unique");
        nameField.setPrefixComponent(new Icon(VaadinIcon.CUBE));

        if (isEditMode) {
            nameField.setValue(product.getName());
        }

        dialogLayout.add(nameField);

        dialogLayout.add(VaadinUtils.createDialogButtonLayout("Save", CONFIRM_BUTTON, "Cancel", CANCEL_BUTTON, this::save, this));

        add(dialogLayout);
    }

    private void save() {
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