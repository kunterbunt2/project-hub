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
import de.bushnaq.abdalla.projecthub.api.VersionApi;
import de.bushnaq.abdalla.projecthub.dto.Version;
import de.bushnaq.abdalla.projecthub.ui.view.MainLayout;
import jakarta.annotation.security.PermitAll;

import java.time.Clock;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.HashMap;
import java.util.Map;

@Route("version-list")
@PageTitle("Version List Page")
//@Menu(order = 1, icon = "vaadin:factory", title = "version List")
@PermitAll // When security is enabled, allow all authenticated users
public class VersionListView extends Main implements AfterNavigationObserver {
    public static final String        VERSION_GRID_NAME_PREFIX = "version-grid-name-";
    private final       Grid<Version> grid;
    private final       H2            pageTitle;
    private             Long          productId;
    private final       VersionApi    versionApi;

    public VersionListView(VersionApi versionApi, Clock clock) {
        this.versionApi = versionApi;

        pageTitle = new H2("Versions");
        pageTitle.addClassNames(
                LumoUtility.Margin.Top.MEDIUM,
                LumoUtility.Margin.Bottom.SMALL
        );
        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofLocalizedDateTime(FormatStyle.LONG).withZone(clock.getZone()).withLocale(getLocale());

        grid = new Grid<>();
        // Only show versions for the selected product
        if (productId != null) {
            grid.setItems(versionApi.getAll(productId));
        } else {
            grid.setItems(versionApi.getAll());
        }
        grid.addColumn(Version::getKey).setHeader("Key");
        grid.addColumn(new ComponentRenderer<>(version -> {
            Div div    = new Div();
            Div square = new Div();
            square.setMinHeight("16px");
            square.setMaxHeight("16px");
            square.setMinWidth("16px");
            square.setMaxWidth("16px");
//                        square.getStyle().set("background-color", "#" + ColorUtil.colorToHtmlColor(version.getColor()));
            square.getStyle().set("float", "left");
            square.getStyle().set("margin", "1px");
            div.add(square);
            div.add(version.getName());
            div.setId(VERSION_GRID_NAME_PREFIX + version.getName());
            return div;
        })).setHeader("Name");
        grid.addColumn(version -> dateTimeFormatter.format(version.getCreated())).setHeader("Created");
        grid.addColumn(version -> dateTimeFormatter.format(version.getUpdated())).setHeader("Updated");
        grid.setSizeFull();
        //- Add click listener to navigate to ProjectView with the selected version ID
        grid.addItemClickListener(event -> {
            Version selectedVersion = event.getItem();
            //- Create parameters map
            Map<String, String> params = new HashMap<>();
            params.put("product", String.valueOf(productId));
            params.put("version", String.valueOf(selectedVersion.getId()));
            //- Navigate with query parameters
            UI.getCurrent().navigate(
                    ProjectListView.class,
                    QueryParameters.simple(params)
            );
        });

        setSizeFull();
        addClassNames(LumoUtility.BoxSizing.BORDER, LumoUtility.Display.FLEX, LumoUtility.FlexDirection.COLUMN);

        add(pageTitle, grid);
    }

    @Override
    public void afterNavigation(AfterNavigationEvent event) {
        // Get productId from query parameters
        Location        location        = event.getLocation();
        QueryParameters queryParameters = location.getQueryParameters();
        // Check if productId is present in the query parameters
        if (queryParameters.getParameters().containsKey("product")) {
            this.productId = Long.parseLong(queryParameters.getParameters().get("product").getFirst());
            pageTitle.setText("Versions of Product ID: " + productId);
        }
        // Only now the component is attached to the DOM
        getElement().getParent().getComponent()
                .ifPresent(component -> {
                    if (component instanceof MainLayout mainLayout) {
                        mainLayout.getBreadcrumbs().clear();
                        mainLayout.getBreadcrumbs().addItem("Products", ProductListView.class);
                        Map<String, String> params = new HashMap<>();
                        params.put("product", String.valueOf(productId));
                        mainLayout.getBreadcrumbs().addItem("Versions", VersionListView.class, params);
                    }
                });
    }

}
