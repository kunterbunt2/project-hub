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

package de.bushnaq.abdalla.projecthub.ui;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.contextmenu.ContextMenu;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Main;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import com.vaadin.flow.router.*;
import com.vaadin.flow.theme.lumo.LumoUtility;
import de.bushnaq.abdalla.projecthub.api.ProductApi;
import de.bushnaq.abdalla.projecthub.dto.Product;
import de.bushnaq.abdalla.projecthub.ui.view.MainLayout;
import jakarta.annotation.security.PermitAll;

import java.time.Clock;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.HashMap;
import java.util.Map;

@Route("product-list")
@PageTitle("Product List Page")
@Menu(order = 1, icon = "vaadin:factory", title = "product List")
@PermitAll // When security is enabled, allow all authenticated users
public class ProductListView extends Main implements AfterNavigationObserver {
    public static final String        PRODUCT_GRID_NAME_PREFIX = "product-grid-name-";
    public static final String        ROUTE                    = "product-list";
    private final       Clock         clock;
    private final       Grid<Product> grid;
    private final       ProductApi    productApi;

    public ProductListView(ProductApi productApi, Clock clock) {
        this.productApi = productApi;
        this.clock      = clock;

        // Create header layout with title and create button
        HorizontalLayout headerLayout = new HorizontalLayout();
        headerLayout.setWidthFull();
        headerLayout.setPadding(false);
        headerLayout.setAlignItems(FlexComponent.Alignment.CENTER);
        headerLayout.setJustifyContentMode(FlexComponent.JustifyContentMode.BETWEEN);

        H2 pageTitle = new H2("Products");
        pageTitle.addClassNames(
                LumoUtility.Margin.Top.MEDIUM,
                LumoUtility.Margin.Bottom.SMALL
        );

        Button createButton = new Button("Create", new Icon(VaadinIcon.PLUS));
        createButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        createButton.addClickListener(e -> openProductDialog(null));

        headerLayout.add(pageTitle, createButton);

        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofLocalizedDateTime(FormatStyle.LONG).withZone(clock.getZone()).withLocale(getLocale());

        grid = new Grid<>();
        refreshGrid();
        {
            Grid.Column<Product> column = grid.addColumn(Product::getKey).setHeader("Key");
            column.setId("product-grid-key-column");
        }
        {
            Grid.Column<Product> column = grid.addColumn(new ComponentRenderer<>(product -> {
                Div div    = new Div();
                Div square = new Div();
                square.setMinHeight("16px");
                square.setMaxHeight("16px");
                square.setMinWidth("16px");
                square.setMaxWidth("16px");
//                        square.getStyle().set("background-color", "#" + ColorUtil.colorToHtmlColor(product.getColor()));
                square.getStyle().set("float", "left");
                square.getStyle().set("margin", "1px");
                div.add(square);
                div.add(product.getName());
                div.setId(PRODUCT_GRID_NAME_PREFIX + product.getName());
                return div;
            })).setHeader("Name");
            column.setId("product-grid-name-column");
        }
        {
            Grid.Column<Product> column = grid.addColumn(product -> dateTimeFormatter.format(product.getCreated())).setHeader("Created");
            column.setId("product-grid-created-column");
        }
        {
            Grid.Column<Product> column = grid.addColumn(product -> dateTimeFormatter.format(product.getUpdated())).setHeader("Updated");
            column.setId("product-grid-updated-column");
        }

        // Add actions column with context menu - fix the text alignment issue
        grid.addColumn(new ComponentRenderer<>(product -> {
            Button actionButton = new Button(new Icon(VaadinIcon.ELLIPSIS_DOTS_V));
            actionButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY_INLINE);
            actionButton.getElement().setAttribute("aria-label", "More options");

            // Center the button with CSS
            actionButton.getStyle().set("margin", "auto");
            actionButton.getStyle().set("display", "block");

            ContextMenu contextMenu = new ContextMenu();
            contextMenu.setOpenOnClick(true);
            contextMenu.setTarget(actionButton);

            contextMenu.addItem("Edit...", e -> openProductDialog(product));
            contextMenu.addItem("Delete...", e -> confirmDelete(product));

            return actionButton;
        })).setWidth("70px").setFlexGrow(0);

        grid.setSizeFull();

        // Add click listener to navigate to VersionView with the selected product ID
        grid.addItemClickListener(event -> {
            Product selectedProduct = event.getItem();
            // Create parameters map
            Map<String, String> params = new HashMap<>();
            params.put("product", String.valueOf(selectedProduct.getId()));
            // Navigate with query parameters
            UI.getCurrent().navigate(
                    VersionListView.class,
                    QueryParameters.simple(params)
            );
        });

        setSizeFull();
        addClassNames(LumoUtility.BoxSizing.BORDER, LumoUtility.Display.FLEX, LumoUtility.FlexDirection.COLUMN);

        add(headerLayout, grid);
    }

    @Override
    public void afterNavigation(AfterNavigationEvent event) {
        //- update breadcrumbs
        getElement().getParent().getComponent()
                .ifPresent(component -> {
                    if (component instanceof MainLayout mainLayout) {
                        mainLayout.getBreadcrumbs().clear();
                        mainLayout.getBreadcrumbs().addItem("Products", ProductListView.class);
                    }
                });
    }

    private void confirmDelete(Product product) {
        Dialog confirmDialog = new Dialog();
        confirmDialog.setHeaderTitle("Confirm Delete");

        VerticalLayout dialogLayout = new VerticalLayout();
        dialogLayout.add("Are you sure you want to delete product \"" + product.getName() + "\"?");
        dialogLayout.setPadding(true);

        Button deleteButton = new Button("Delete", e -> {
            productApi.deleteById(product.getId());
            confirmDialog.close();
            refreshGrid();
            Notification.show("Product deleted", 3000, Notification.Position.BOTTOM_START);
        });
        deleteButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY, ButtonVariant.LUMO_ERROR);

        Button cancelButton = new Button("Cancel", e -> confirmDialog.close());

        HorizontalLayout buttonLayout = new HorizontalLayout();
        buttonLayout.setJustifyContentMode(FlexComponent.JustifyContentMode.END);
        buttonLayout.add(cancelButton, deleteButton);
        buttonLayout.setWidthFull();

        dialogLayout.add(buttonLayout);
        confirmDialog.add(dialogLayout);

        confirmDialog.open();
    }

    private void openProductDialog(Product product) {
        Dialog  dialog     = new Dialog();
        boolean isEditMode = product != null;

        dialog.setHeaderTitle(isEditMode ? "Edit Product" : "Create Product");

        VerticalLayout dialogLayout = new VerticalLayout();
        dialogLayout.setPadding(false);
        dialogLayout.setSpacing(true);

        TextField nameField = new TextField("Product Name");
        nameField.setWidthFull();
        nameField.setRequired(true);

        if (isEditMode) {
            nameField.setValue(product.getName());
        }

        dialogLayout.add(nameField);

        Button saveButton = new Button("Save", e -> {
            if (nameField.getValue().trim().isEmpty()) {
                Notification.show("Please enter a product name", 3000, Notification.Position.MIDDLE);
                return;
            }

            if (isEditMode) {
                product.setName(nameField.getValue().trim());
                productApi.update(product);
                Notification.show("Product updated", 3000, Notification.Position.BOTTOM_START);
            } else {
                Product newProduct = new Product();
                newProduct.setName(nameField.getValue().trim());
                productApi.persist(newProduct);
                Notification.show("Product created", 3000, Notification.Position.BOTTOM_START);
            }

            dialog.close();
            refreshGrid();
        });

        saveButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        Button cancelButton = new Button("Cancel", e -> dialog.close());

        HorizontalLayout buttonLayout = new HorizontalLayout();
        buttonLayout.setJustifyContentMode(FlexComponent.JustifyContentMode.END);
        buttonLayout.add(cancelButton, saveButton);
        buttonLayout.setWidthFull();

        dialogLayout.add(buttonLayout);
        dialog.add(dialogLayout);

        dialog.open();
    }

    private void refreshGrid() {
        grid.setItems(productApi.getAll());
    }
}
