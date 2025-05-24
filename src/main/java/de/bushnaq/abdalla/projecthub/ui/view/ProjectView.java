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
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Main;
import com.vaadin.flow.router.BeforeEvent;
import com.vaadin.flow.router.HasUrlParameter;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.theme.lumo.LumoUtility;
import de.bushnaq.abdalla.projecthub.api.ProjectApi;
import de.bushnaq.abdalla.projecthub.dto.Project;
import jakarta.annotation.security.PermitAll;

import java.time.Clock;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;

@Route("product1")
@PageTitle("Project Page")
//@Menu(order = 1, icon = "vaadin:factory", title = "project List")
@PermitAll // When security is enabled, allow all authenticated users
public class ProjectView extends Main implements HasUrlParameter<Long> {
    public static final String        ROUTE = "product1";
    final               Grid<Project> grid;
    H2         pageTitle;
    ProjectApi projectApi;
    private Long versionId;

    public ProjectView(ProjectApi projectApi, Clock clock) {
        this.projectApi = projectApi;

        pageTitle = new H2("Projects");
        pageTitle.addClassNames(
                LumoUtility.Margin.Top.MEDIUM,
                LumoUtility.Margin.Bottom.SMALL
        );
        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofLocalizedDateTime(FormatStyle.LONG).withZone(clock.getZone()).withLocale(getLocale());

        grid = new Grid<>();
        // Only show versions for the selected product
        if (versionId != null) {
            grid.setItems(projectApi.getAll(versionId));
        } else {
            grid.setItems(projectApi.getAll());
        }
        grid.addColumn(Project::getKey).setHeader("Key");
        grid.addColumn(Project::getName).setHeader("Name");
        grid.addColumn(version -> dateTimeFormatter.format(version.getCreated())).setHeader("Created");
        grid.addColumn(version -> dateTimeFormatter.format(version.getUpdated())).setHeader("Updated");
        grid.setSizeFull();
        // Add click listener to navigate to ProjectView with the selected version ID
        grid.addItemClickListener(event -> {
            Project selectedProject = event.getItem();
            UI.getCurrent().navigate(ProjectView.class, selectedProject.getId());
        });

        setSizeFull();
        addClassNames(LumoUtility.BoxSizing.BORDER, LumoUtility.Display.FLEX, LumoUtility.FlexDirection.COLUMN);

        add(pageTitle, grid);
    }

    @Override
    public void setParameter(BeforeEvent beforeEvent, Long versionId) {
        this.versionId = versionId;
        pageTitle.setText("Projects of Version ID: " + versionId);

    }
}
