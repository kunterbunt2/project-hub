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
import de.bushnaq.abdalla.projecthub.api.ProjectApi;
import de.bushnaq.abdalla.projecthub.dto.Project;
import de.bushnaq.abdalla.projecthub.ui.view.MainLayout;
import jakarta.annotation.security.PermitAll;

import java.time.Clock;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.HashMap;
import java.util.Map;

@Route("project")
@PageTitle("Project Page")
//@Menu(order = 1, icon = "vaadin:factory", title = "project List")
@PermitAll // When security is enabled, allow all authenticated users
public class ProjectView extends Main implements AfterNavigationObserver {
    public static final String        PROJECT_GRID_NAME_PREFIX = "project-grid-name-";
    private final       Grid<Project> grid;
    private final       H2            pageTitle;
    private             Long          productId;
    private final       ProjectApi    projectApi;
    private             Long          versionId;

    public ProjectView(ProjectApi projectApi, Clock clock) {
        this.projectApi = projectApi;

        pageTitle = new H2("Projects");
        pageTitle.addClassNames(
                LumoUtility.Margin.Top.MEDIUM,
                LumoUtility.Margin.Bottom.SMALL
        );
        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofLocalizedDateTime(FormatStyle.LONG).withZone(clock.getZone()).withLocale(getLocale());

        grid = new Grid<>();
        //- Only show versions for the selected product
        if (versionId != null) {
            grid.setItems(projectApi.getAll(versionId));
        } else {
            grid.setItems(projectApi.getAll());
        }
        grid.addColumn(Project::getKey).setHeader("Key");
        grid.addColumn(new ComponentRenderer<>(project -> {
            Div div    = new Div();
            Div square = new Div();
            square.setMinHeight("16px");
            square.setMaxHeight("16px");
            square.setMinWidth("16px");
            square.setMaxWidth("16px");
//                        square.getStyle().set("background-color", "#" + ColorUtil.colorToHtmlColor(project.getColor()));
            square.getStyle().set("float", "left");
            square.getStyle().set("margin", "1px");
            div.add(square);
            div.add(project.getName());
            div.setId(PROJECT_GRID_NAME_PREFIX + project.getName());
            return div;
        })).setHeader("Name");
        grid.addColumn(version -> dateTimeFormatter.format(version.getCreated())).setHeader("Created");
        grid.addColumn(version -> dateTimeFormatter.format(version.getUpdated())).setHeader("Updated");
        grid.setSizeFull();
        //- Add click listener to navigate to SprintView with the selected version ID
        grid.addItemClickListener(event -> {
            Project selectedProject = event.getItem();
            //- Create parameters map
            Map<String, String> params = new HashMap<>();
            params.put("product", String.valueOf(productId));
            params.put("version", String.valueOf(versionId));
            params.put("project", String.valueOf(selectedProject.getId()));
            //- Navigate with query parameters
            UI.getCurrent().navigate(
                    SprintView.class,
                    QueryParameters.simple(params)
            );
        });

        setSizeFull();
        addClassNames(LumoUtility.BoxSizing.BORDER, LumoUtility.Display.FLEX, LumoUtility.FlexDirection.COLUMN);

        add(pageTitle, grid);
    }

    @Override
    public void afterNavigation(AfterNavigationEvent event) {
        //- Get productId from query parameters
        Location        location        = event.getLocation();
        QueryParameters queryParameters = location.getQueryParameters();
        // Check if productId is present in the query parameters
        if (queryParameters.getParameters().containsKey("product")) {
            this.productId = Long.parseLong(queryParameters.getParameters().get("product").getFirst());
        }
        if (queryParameters.getParameters().containsKey("version")) {
            this.versionId = Long.parseLong(queryParameters.getParameters().get("version").getFirst());
            pageTitle.setText("Projects of Version " + versionId);
        }
        //- Only now the component is attached to the DOM
        getElement().getParent().getComponent()
                .ifPresent(component -> {
                    if (component instanceof MainLayout mainLayout) {
                        mainLayout.getBreadcrumbs().clear();
                        mainLayout.getBreadcrumbs().addItem("Products", ProductView.class);
                        {
                            Map<String, String> params = new HashMap<>();
                            params.put("product", String.valueOf(productId));
                            mainLayout.getBreadcrumbs().addItem("Versions", VersionView.class, params);
                        }
                        {
                            Map<String, String> params = new HashMap<>();
                            params.put("product", String.valueOf(productId));
                            params.put("version", String.valueOf(versionId));
                            mainLayout.getBreadcrumbs().addItem("Projects", ProjectView.class, params);
                        }
                    }
                });
    }
}
