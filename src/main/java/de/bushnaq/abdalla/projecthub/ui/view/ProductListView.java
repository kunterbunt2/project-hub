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

package de.bushnaq.abdalla.projecthub.ui.view;

import com.vaadin.flow.component.Text;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Main;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import com.vaadin.flow.router.*;
import com.vaadin.flow.theme.lumo.LumoUtility;
import de.bushnaq.abdalla.projecthub.dto.Product;
import de.bushnaq.abdalla.projecthub.rest.api.ProductApi;
import de.bushnaq.abdalla.projecthub.ui.MainLayout;
import de.bushnaq.abdalla.projecthub.ui.dialog.ConfirmDialog;
import de.bushnaq.abdalla.projecthub.ui.dialog.ProductDialog;
import jakarta.annotation.security.PermitAll;
import jakarta.annotation.security.RolesAllowed;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.time.Clock;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.HashMap;
import java.util.Map;

@Route("product-list")
@PageTitle("Product List Page")
@Menu(order = 1, icon = "vaadin:factory", title = "product List")
@PermitAll
@RolesAllowed({"USER", "ADMIN"})
public class ProductListView extends Main implements AfterNavigationObserver {
    public static final String        CREATE_PRODUCT_BUTTON             = "create-product-button";
    public static final String        PRODUCT_GRID                      = "product-grid";
    public static final String        PRODUCT_GRID_DELETE_BUTTON_PREFIX = "product-grid-delete-button-prefix-";
    public static final String        PRODUCT_GRID_EDIT_BUTTON_PREFIX   = "product-grid-edit-button-prefix-";
    public static final String        PRODUCT_GRID_NAME_PREFIX          = "product-grid-name-";
    public static final String        PRODUCT_LIST_PAGE_TITLE           = "product-list-page-title";
    public static final String        ROUTE                             = "product-list";
    private final       Clock         clock;
    private             Grid<Product> grid;
    private final       ProductApi    productApi;

    public ProductListView(ProductApi productApi, Clock clock) {
        this.productApi = productApi;
        this.clock      = clock;

        setSizeFull();
        addClassNames(LumoUtility.BoxSizing.BORDER, LumoUtility.Display.FLEX, LumoUtility.FlexDirection.COLUMN);

        add(createHeader(), createGrid(clock));
    }

    @Override
    public void afterNavigation(AfterNavigationEvent event) {
        getElement().getParent().getComponent()
                .ifPresent(component -> {
                    if (component instanceof MainLayout mainLayout) {
                        mainLayout.getBreadcrumbs().clear();
                        mainLayout.getBreadcrumbs().addItem("Products", ProductListView.class);
                    }
                });
    }

    private void confirmDelete(Product product) {
        String message = "Are you sure you want to delete product \"" + product.getName() + "\"?";
        ConfirmDialog dialog = new ConfirmDialog(
                "Confirm Delete",
                message,
                "Delete",
                () -> {
                    productApi.deleteById(product.getId());
                    refreshGrid();
                    Notification.show("Product deleted", 3000, Notification.Position.BOTTOM_START);
                }
        );
        dialog.open();
    }

    private Grid<Product> createGrid(Clock clock) {
        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofLocalizedDateTime(FormatStyle.LONG).withZone(clock.getZone()).withLocale(getLocale());

        grid = new Grid<>();
        grid.setId(PRODUCT_GRID);
        refreshGrid();
        {
            Grid.Column<Product> column = grid.addColumn(Product::getKey).setHeader("Key");
            column.setId("product-grid-key-column");
            column.setHeader(new HorizontalLayout(new Icon(VaadinIcon.KEY), new Div(new Text("Key"))));
        }
        {
            Grid.Column<Product> column = grid.addColumn(new ComponentRenderer<>(product -> {
                Div div    = new Div();
                Div square = new Div();
                square.setMinHeight("16px");
                square.setMaxHeight("16px");
                square.setMinWidth("16px");
                square.setMaxWidth("16px");
                square.getStyle().set("float", "left");
                square.getStyle().set("margin", "1px");
                div.add(square);
                div.add(product.getName());
                div.setId(PRODUCT_GRID_NAME_PREFIX + product.getName());
                return div;
            })).setHeader("Name");
            column.setId("product-grid-name-column");
            column.setHeader(new HorizontalLayout(new Icon(VaadinIcon.CUBE), new Div(new Text("Name"))));
        }
        {
            Grid.Column<Product> column = grid.addColumn(product -> dateTimeFormatter.format(product.getCreated())).setHeader("Created");
            column.setId("product-grid-created-column");
            column.setHeader(new HorizontalLayout(new Icon(VaadinIcon.CALENDAR), new Div(new Text("Created"))));
        }
        {
            Grid.Column<Product> column = grid.addColumn(product -> dateTimeFormatter.format(product.getUpdated())).setHeader("Updated");
            column.setId("product-grid-updated-column");
            column.setHeader(new HorizontalLayout(new Icon(VaadinIcon.CALENDAR), new Div(new Text("Updated"))));
        }

        // Add actions column with direct buttons instead of context menu
        grid.addColumn(new ComponentRenderer<>(product -> {
            HorizontalLayout layout = new HorizontalLayout();
            layout.setAlignItems(FlexComponent.Alignment.CENTER);
            layout.setSpacing(true);

            Button editButton = new Button(new Icon(VaadinIcon.EDIT));
            editButton.setId(PRODUCT_GRID_EDIT_BUTTON_PREFIX + product.getName());
            editButton.addThemeVariants(ButtonVariant.LUMO_SMALL, ButtonVariant.LUMO_TERTIARY);
            editButton.addClickListener(e -> openProductDialog(product));
            editButton.getElement().setAttribute("title", "Edit");

            Button deleteButton = new Button(new Icon(VaadinIcon.TRASH));
            deleteButton.setId(PRODUCT_GRID_DELETE_BUTTON_PREFIX + product.getName());
            deleteButton.addThemeVariants(ButtonVariant.LUMO_SMALL, ButtonVariant.LUMO_TERTIARY, ButtonVariant.LUMO_ERROR);
            deleteButton.addClickListener(e -> confirmDelete(product));
            deleteButton.getElement().setAttribute("title", "Delete");

            layout.add(editButton, deleteButton);
            return layout;
        })).setHeader("Actions").setFlexGrow(0).setWidth("120px");

        grid.setSizeFull();

        // Add click listener to navigate to VersionView with the selected product ID
        grid.addItemClickListener(event -> {
            Product selectedProduct = event.getItem();
            // Create parameters map
            Map<String, String> params = new HashMap<>();
            params.put("product", String.valueOf(selectedProduct.getId()));
            // Navigate with query parameters
            UI.getCurrent().navigate(VersionListView.class, QueryParameters.simple(params));
        });
        return grid;
    }

    private HorizontalLayout createHeader() {
        HorizontalLayout headerLayout;
        headerLayout = new HorizontalLayout();
        headerLayout.setWidthFull();
        headerLayout.setPadding(false);
        headerLayout.setAlignItems(FlexComponent.Alignment.CENTER);
        headerLayout.setJustifyContentMode(FlexComponent.JustifyContentMode.BETWEEN);

        // Create page title with icon
        HorizontalLayout titleLayout = new HorizontalLayout();
        titleLayout.setSpacing(true);
        titleLayout.setAlignItems(FlexComponent.Alignment.CENTER);

        Icon productIcon = new Icon(VaadinIcon.CUBE);
        H2   pageTitle   = new H2("Products");
        pageTitle.setId(PRODUCT_LIST_PAGE_TITLE);
        pageTitle.addClassNames(
                LumoUtility.Margin.Top.MEDIUM,
                LumoUtility.Margin.Bottom.SMALL
        );

        titleLayout.add(productIcon, pageTitle);

        Button createButton = new Button("Create", new Icon(VaadinIcon.PLUS));
        createButton.setId(CREATE_PRODUCT_BUTTON);
        createButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        createButton.addClickListener(e -> openProductDialog(null));

        headerLayout.add(titleLayout, createButton);
        return headerLayout;
    }

    private void openProductDialog(Product product) {
        // Use the new SaveCallback interface that passes both the product and the dialog
        ProductDialog dialog = new ProductDialog(product, (savedProduct, dialogReference) -> {
            try {
                if (product != null) {
                    // Edit mode
                    productApi.update(savedProduct);
                    Notification.show("Product updated", 3000, Notification.Position.BOTTOM_START);
                } else {
                    // Create mode
                    productApi.persist(savedProduct);
                    Notification.show("Product created", 3000, Notification.Position.BOTTOM_START);
                }
                refreshGrid();
                dialogReference.close();
            } catch (Exception e) {
                if (e instanceof ResponseStatusException && ((ResponseStatusException) e).getStatusCode().equals(HttpStatus.CONFLICT)) {
                    dialogReference.setNameFieldError(((ResponseStatusException) e).getReason());
                    // Keep the dialog open so the user can correct the name
                } else {
                    // For other errors, show generic message and close dialog
                    Notification notification = new Notification("An error occurred: " + e.getMessage(), 5000, Notification.Position.MIDDLE);
                    notification.addThemeVariants(com.vaadin.flow.component.notification.NotificationVariant.LUMO_ERROR);
                    notification.open();
                    // dialogReference.close();
                    // Keep the dialog open so the user can correct the name
                }
            }
        });
        dialog.open();
    }

    private void refreshGrid() {
        grid.setItems(productApi.getAll());
    }
}
