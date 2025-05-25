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
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Main;
import com.vaadin.flow.router.BeforeEvent;
import com.vaadin.flow.router.HasUrlParameter;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.theme.lumo.LumoUtility;
import de.bushnaq.abdalla.projecthub.api.SprintApi;
import de.bushnaq.abdalla.projecthub.dto.Sprint;
import de.bushnaq.abdalla.util.date.DateUtil;
import jakarta.annotation.security.PermitAll;

import java.time.Clock;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;

@Route("sprint")
@PageTitle("Sprint Page")
//@Menu(order = 1, icon = "vaadin:factory", title = "project List")
@PermitAll // When security is enabled, allow all authenticated users
public class SprintView extends Main implements HasUrlParameter<Long> {
    final Grid<Sprint> grid;
    H2 pageTitle;
    private Long projectId;
    SprintApi sprintApi;

    public SprintView(SprintApi sprintApi, Clock clock) {
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
        grid.addColumn(Sprint::getName).setHeader("Name");
//        grid.addColumn(version -> dateTimeFormatter.format(version.getCreated())).setHeader("Created");
//        grid.addColumn(version -> dateTimeFormatter.format(version.getUpdated())).setHeader("Updated");
        grid.addColumn(sprint -> dateTimeFormatter.format(sprint.getStart())).setHeader("Start");
        grid.addColumn(sprint -> dateTimeFormatter.format(sprint.getEnd())).setHeader("End");
        grid.addColumn(sprint -> sprint.getStatus().name()).setHeader("Status");
        grid.addColumn(sprint -> DateUtil.createDurationString(sprint.getOriginalEstimation(), false, true, true)).setHeader("Original Estimation");
        grid.addColumn(sprint -> DateUtil.createDurationString(sprint.getWorked(), false, true, true)).setHeader("Worked");
        grid.addColumn(sprint -> DateUtil.createDurationString(sprint.getRemaining(), false, true, true)).setHeader("Remaining");
        grid.setSizeFull();
        // Add click listener to navigate to ProjectView with the selected version ID
        grid.addItemClickListener(event -> {
            Sprint selectedSprint = event.getItem();
            UI.getCurrent().navigate(TaskView.class, selectedSprint.getId());
        });

        setSizeFull();
        addClassNames(LumoUtility.BoxSizing.BORDER, LumoUtility.Display.FLEX, LumoUtility.FlexDirection.COLUMN);

        add(pageTitle, grid);
    }

    @Override
    public void setParameter(BeforeEvent beforeEvent, Long projectId) {
        this.projectId = projectId;
        pageTitle.setText("Sprints of Project ID: " + projectId);

    }
}
