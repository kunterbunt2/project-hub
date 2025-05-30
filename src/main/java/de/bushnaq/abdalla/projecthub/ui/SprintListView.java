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
import de.bushnaq.abdalla.projecthub.api.SprintApi;
import de.bushnaq.abdalla.projecthub.dto.Sprint;
import de.bushnaq.abdalla.projecthub.ui.view.MainLayout;
import de.bushnaq.abdalla.util.date.DateUtil;
import jakarta.annotation.security.PermitAll;

import java.time.Clock;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.HashMap;
import java.util.Map;

@Route("sprint-list")
@PageTitle("Sprint List Page")
//@Menu(order = 1, icon = "vaadin:factory", title = "project List")
@PermitAll // When security is enabled, allow all authenticated users
public class SprintListView extends Main implements AfterNavigationObserver {
    public static final String       SPRINT_GRID_NAME_PREFIX = "sprint-grid-name-";
    private final       Grid<Sprint> grid;
    private final       H2           pageTitle;
    private             Long         productId;
    private             Long         projectId;
    private final       SprintApi    sprintApi;
    private             Long         versionId;

    public SprintListView(SprintApi sprintApi, Clock clock) {
        this.sprintApi = sprintApi;

        pageTitle = new H2("Projects");
        pageTitle.addClassNames(
                LumoUtility.Margin.Top.MEDIUM,
                LumoUtility.Margin.Bottom.SMALL
        );
        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofLocalizedDateTime(FormatStyle.LONG).withZone(clock.getZone()).withLocale(getLocale());

        grid = new Grid<>();
        // Only show sprints for the selected project
        if (projectId != null) {
            grid.setItems(sprintApi.getAll(projectId));
        } else {
            grid.setItems(sprintApi.getAll());
        }
        grid.addColumn(Sprint::getKey).setHeader("Key");
        grid.addColumn(new ComponentRenderer<>(sprint -> {
            Div div    = new Div();
            Div square = new Div();
            square.setMinHeight("16px");
            square.setMaxHeight("16px");
            square.setMinWidth("16px");
            square.setMaxWidth("16px");
//                        square.getStyle().set("background-color", "#" + ColorUtil.colorToHtmlColor(sprint.getColor()));
            square.getStyle().set("float", "left");
            square.getStyle().set("margin", "1px");
            div.add(square);
            div.add(sprint.getName());
            div.setId(SPRINT_GRID_NAME_PREFIX + sprint.getName());
            return div;
        })).setHeader("Name");
//        grid.addColumn(version -> dateTimeFormatter.format(version.getCreated())).setHeader("Created");
//        grid.addColumn(version -> dateTimeFormatter.format(version.getUpdated())).setHeader("Updated");
        grid.addColumn(sprint -> dateTimeFormatter.format(sprint.getStart())).setHeader("Start");
        grid.addColumn(sprint -> dateTimeFormatter.format(sprint.getEnd())).setHeader("End");
        grid.addColumn(sprint -> sprint.getStatus().name()).setHeader("Status");
        grid.addColumn(sprint -> DateUtil.createDurationString(sprint.getOriginalEstimation(), false, true, true)).setHeader("Original Estimation");
        grid.addColumn(sprint -> DateUtil.createDurationString(sprint.getWorked(), false, true, true)).setHeader("Worked");
        grid.addColumn(sprint -> DateUtil.createDurationString(sprint.getRemaining(), false, true, true)).setHeader("Remaining");
        grid.setSizeFull();
        //- Add click listener to navigate to TaskView with the selected version ID
        grid.addItemClickListener(event -> {
            Sprint selectedSprint = event.getItem();
            //- Create parameters map
            Map<String, String> params = new HashMap<>();
            params.put("product", String.valueOf(productId));
            params.put("version", String.valueOf(versionId));
            params.put("project", String.valueOf(projectId));
            params.put("sprint", String.valueOf(selectedSprint.getId()));
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
        }
        if (queryParameters.getParameters().containsKey("project")) {
            this.projectId = Long.parseLong(queryParameters.getParameters().get("project").getFirst());
            pageTitle.setText("Sprints of Project ID: " + projectId);
        }
        //- Only now the component is attached to the DOM
        getElement().getParent().getComponent()
                .ifPresent(component -> {
                    if (component instanceof MainLayout mainLayout) {
                        mainLayout.getBreadcrumbs().clear();
                        mainLayout.getBreadcrumbs().addItem("Products", ProductListView.class);
                        {
                            Map<String, String> params = new HashMap<>();
                            params.put("product", String.valueOf(productId));
                            mainLayout.getBreadcrumbs().addItem("Versions", VersionListView.class, params);
                        }
                        {
                            Map<String, String> params = new HashMap<>();
                            params.put("product", String.valueOf(productId));
                            params.put("version", String.valueOf(versionId));
                            mainLayout.getBreadcrumbs().addItem("Projects", ProjectListView.class, params);
                        }
                        {
                            Map<String, String> params = new HashMap<>();
                            params.put("product", String.valueOf(productId));
                            params.put("version", String.valueOf(versionId));
                            params.put("project", String.valueOf(projectId));
                            mainLayout.getBreadcrumbs().addItem("Sprints", SprintListView.class, params);
                        }
                    }
                });
    }

}
