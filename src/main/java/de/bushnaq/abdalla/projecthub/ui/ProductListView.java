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
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Main;
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
    private final       Grid<Product> grid;
    private final       ProductApi    productApi;

    public ProductListView(ProductApi productApi, Clock clock) {
        this.productApi = productApi;


        H2 pageTitle = new H2("Products");
        pageTitle.addClassNames(
                LumoUtility.Margin.Top.MEDIUM,
                LumoUtility.Margin.Bottom.SMALL
        );
        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofLocalizedDateTime(FormatStyle.LONG).withZone(clock.getZone()).withLocale(getLocale());

        grid = new Grid<>();
        grid.setItems(productApi.getAll());
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

        add(pageTitle, grid);
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

}
