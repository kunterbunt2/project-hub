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

import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Main;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.data.provider.ListDataProvider;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import com.vaadin.flow.router.*;
import com.vaadin.flow.theme.lumo.LumoUtility;
import de.bushnaq.abdalla.projecthub.dto.Product;
import de.bushnaq.abdalla.projecthub.rest.api.ProductApi;
import de.bushnaq.abdalla.projecthub.ui.MainLayout;
import de.bushnaq.abdalla.projecthub.ui.dialog.ConfirmDialog;
import de.bushnaq.abdalla.projecthub.ui.dialog.ProductDialog;
import de.bushnaq.abdalla.projecthub.ui.util.VaadinUtil;
import jakarta.annotation.security.PermitAll;
import jakarta.annotation.security.RolesAllowed;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.time.Clock;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

@Route("product-list")
@PageTitle("Product List Page")
@Menu(order = 1, icon = "vaadin:factory", title = "Products")
@PermitAll
@RolesAllowed({"USER", "ADMIN"})
public class ProductListView extends Main implements AfterNavigationObserver {
    public static final String                    CREATE_PRODUCT_BUTTON             = "create-product-button";
    public static final String                    PRODUCT_GRID                      = "product-grid";
    public static final String                    PRODUCT_GRID_DELETE_BUTTON_PREFIX = "product-grid-delete-button-prefix-";
    public static final String                    PRODUCT_GRID_EDIT_BUTTON_PREFIX   = "product-grid-edit-button-prefix-";
    public static final String                    PRODUCT_GRID_NAME_PREFIX          = "product-grid-name-";
    public static final String                    PRODUCT_LIST_PAGE_TITLE           = "product-list-page-title";
    public static final String                    PRODUCT_ROW_COUNTER               = "product-row-counter";
    public static final String                    ROUTE                             = "product-list";
    private             ListDataProvider<Product> dataProvider;
    private             Grid<Product>             grid;
    private final       ProductApi                productApi;

    public ProductListView(ProductApi productApi, Clock clock) {
        this.productApi = productApi;

        setSizeFull();
        addClassNames(LumoUtility.BoxSizing.BORDER, LumoUtility.Display.FLEX, LumoUtility.FlexDirection.COLUMN);
        this.getStyle().set("padding-left", "var(--lumo-space-m)");
        this.getStyle().set("padding-right", "var(--lumo-space-m)");


        grid = createGrid(clock);
        add(
                VaadinUtil.createHeader(
                        "Products",
                        PRODUCT_LIST_PAGE_TITLE,
                        VaadinIcon.CUBE,
                        CREATE_PRODUCT_BUTTON,
                        () -> openProductDialog(null),
                        grid,
                        PRODUCT_ROW_COUNTER
                ),
                grid
        );
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
        grid.setSizeFull();
        grid.addThemeVariants(com.vaadin.flow.component.grid.GridVariant.LUMO_NO_BORDER, com.vaadin.flow.component.grid.GridVariant.LUMO_NO_ROW_BORDERS);
        dataProvider = new ListDataProvider<Product>(new ArrayList<>());
        grid.setDataProvider(dataProvider);

        // Add click listener to navigate to VersionView with the selected product ID
        grid.addItemClickListener(event -> {
            Product selectedProduct = event.getItem();
            // Create parameters map
            Map<String, String> params = new HashMap<>();
            params.put("product", String.valueOf(selectedProduct.getId()));
            // Navigate with query parameters
            UI.getCurrent().navigate(VersionListView.class, QueryParameters.simple(params));
        });

        refreshGrid();

        {
            Grid.Column<Product> keyColumn = grid.addColumn(Product::getKey);
            VaadinUtil.addFilterableHeader(grid, keyColumn, "Key", VaadinIcon.KEY, Product::getKey);
        }
        {
            Grid.Column<Product> nameColumn = grid.addColumn(new ComponentRenderer<>(product -> {
                Div div = new Div();
                div.add(product.getName());
                div.setId(PRODUCT_GRID_NAME_PREFIX + product.getName());
                return div;
            }));

            // Configure a custom comparator to properly sort by the name property
            nameColumn.setComparator((product1, product2) ->
                    product1.getName().compareToIgnoreCase(product2.getName()));

            VaadinUtil.addFilterableHeader(grid, nameColumn, "Name", VaadinIcon.CUBE, Product::getName);
        }
        {
            Grid.Column<Product> createdColumn = grid.addColumn(product -> dateTimeFormatter.format(product.getCreated()));
            VaadinUtil.addFilterableHeader(grid, createdColumn, "Created", VaadinIcon.CALENDAR, product -> dateTimeFormatter.format(product.getCreated()));
        }
        {
            Grid.Column<Product> updatedColumn = grid.addColumn(product -> dateTimeFormatter.format(product.getUpdated()));
            VaadinUtil.addFilterableHeader(grid, updatedColumn, "Updated", VaadinIcon.CALENDAR, product -> dateTimeFormatter.format(product.getUpdated()));
        }
        // Add actions column using VaadinUtil
        VaadinUtil.addActionColumn(
                grid,
                PRODUCT_GRID_EDIT_BUTTON_PREFIX,
                PRODUCT_GRID_DELETE_BUTTON_PREFIX,
                Product::getName,
                this::openProductDialog,
                this::confirmDelete
        );

        return grid;
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
        dataProvider.getItems().clear();
        dataProvider.getItems().addAll(productApi.getAll());
        dataProvider.refreshAll();
    }
}
