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

import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Main;
import com.vaadin.flow.router.*;
import com.vaadin.flow.theme.lumo.LumoUtility;
import de.bushnaq.abdalla.projecthub.api.SprintApi;
import de.bushnaq.abdalla.projecthub.api.TaskApi;
import de.bushnaq.abdalla.projecthub.api.UserApi;
import de.bushnaq.abdalla.projecthub.dto.Relation;
import de.bushnaq.abdalla.projecthub.dto.Sprint;
import de.bushnaq.abdalla.projecthub.dto.Task;
import de.bushnaq.abdalla.projecthub.ui.view.MainLayout;
import de.bushnaq.abdalla.util.date.DateUtil;
import jakarta.annotation.security.PermitAll;

import java.time.Clock;
import java.time.Duration;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Route("task")
@PageTitle("Task Page")
@CssImport("./styles/grid-styles.css")
@PermitAll // When security is enabled, allow all authenticated users
public class TaskView extends Main implements AfterNavigationObserver {
    private final Clock      clock;
    private final Grid<Task> grid;
    private final H2         pageTitle;
    private       Long       productId;
    private       Long       projectId;
    private       Sprint     sprint;
    private final SprintApi  sprintApi;
    private       Long       sprintId;
    private final TaskApi    taskApi;
    private final UserApi    userApi;
    private       Long       versionId;

    public TaskView(TaskApi taskApi, SprintApi sprintApi, UserApi userApi, Clock clock) {
        this.taskApi   = taskApi;
        this.sprintApi = sprintApi;
        this.userApi   = userApi;
        this.clock     = clock;

        pageTitle = new H2("Tasks");
        pageTitle.addClassNames(
                LumoUtility.Margin.Top.MEDIUM,
                LumoUtility.Margin.Bottom.SMALL
        );

        grid = new Grid<>();
        setupGridColumns();
        // Add borders between columns
        grid.addThemeVariants(GridVariant.LUMO_COLUMN_BORDERS, GridVariant.LUMO_ROW_STRIPES);

// Add custom styling for more JIRA-like appearance
        grid.addClassName("jira-style-grid");

// Apply custom styling directly to make borders lighter gray
        grid.getElement().getStyle().set("--lumo-contrast-10pct", "#e0e0e0");
        grid.setSizeFull();
        // Add click listener to navigate to ProjectView with the selected version ID
//        grid.addItemClickListener(event -> {
//            Task selectedTask = event.getItem();
//            UI.getCurrent().navigate(TaskView.class, selectedTask.getId());
//        });

        setSizeFull();
        addClassNames(LumoUtility.BoxSizing.BORDER, LumoUtility.Display.FLEX, LumoUtility.FlexDirection.COLUMN);

        add(pageTitle, grid);
    }

    @Override
    public void afterNavigation(AfterNavigationEvent event) {
        //- Get productId from query parameters
        Location        location        = event.getLocation();
        QueryParameters queryParameters = location.getQueryParameters();
        if (queryParameters.getParameters().containsKey("product")) {
            this.productId = Long.parseLong(queryParameters.getParameters().get("product").getFirst());
        }
        if (queryParameters.getParameters().containsKey("version")) {
            this.versionId = Long.parseLong(queryParameters.getParameters().get("version").getFirst());
        }
        if (queryParameters.getParameters().containsKey("project")) {
            this.projectId = Long.parseLong(queryParameters.getParameters().get("project").getFirst());
        }
        if (queryParameters.getParameters().containsKey("sprint")) {
            this.sprintId = Long.parseLong(queryParameters.getParameters().get("sprint").getFirst());
            pageTitle.setText("Task of Sprint ID: " + sprintId);
        }
        //- populate grid with tasks of the sprint
        sprint = sprintApi.getById(sprintId);
        sprint.initUserMap(userApi.getAll(sprintId));
        sprint.initTaskMap(taskApi.getAll(sprintId));
        pageTitle.setText("Task of Sprint ID: " + sprintId);
        grid.setItems(sprint.getTasks());
        //- Update breadcrumbs
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
                        {
                            Map<String, String> params = new HashMap<>();
                            params.put("product", String.valueOf(productId));
                            params.put("version", String.valueOf(versionId));
                            params.put("project", String.valueOf(projectId));
                            mainLayout.getBreadcrumbs().addItem("Sprints", SprintView.class, params);
                        }
                        {
                            Map<String, String> params = new HashMap<>();
                            params.put("product", String.valueOf(productId));
                            params.put("version", String.valueOf(versionId));
                            params.put("project", String.valueOf(projectId));
                            params.put("sprint", String.valueOf(sprintId));
                            mainLayout.getBreadcrumbs().addItem("Tasks", TaskView.class, params);
                        }
                    }
                });
    }

    private void setupGridColumns() {
        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofLocalizedDateTime(FormatStyle.LONG).withZone(clock.getZone()).withLocale(getLocale());

        grid.addColumn(Task::getKey).setHeader("Key").setAutoWidth(true);
        grid.addColumn(task -> task.getParentTask() != null ? task.getParentTask().getKey() : "").setHeader("Parent").setAutoWidth(true);
        grid.addColumn(Task::getName).setHeader("Name").setAutoWidth(true);
        grid.addColumn(task -> task.getResourceId() != null ? sprint.getuser(task.getResourceId()).getName() : "").setHeader("Assigned").setAutoWidth(true);
//        grid.addColumn(version -> dateTimeFormatter.format(version.getCreated())).setHeader("Created");
//        grid.addColumn(version -> dateTimeFormatter.format(version.getUpdated())).setHeader("Updated");
        grid.addColumn(task -> dateTimeFormatter.format(task.getStart())).setHeader("Start").setAutoWidth(true);
        grid.addColumn(task -> dateTimeFormatter.format(task.getFinish())).setHeader("End").setAutoWidth(true);
        grid.addColumn(task -> !task.getOriginalEstimate().equals(Duration.ZERO) ? DateUtil.createDurationString(task.getOriginalEstimate(), false, true, false) : "").setHeader("Original Estimate").setAutoWidth(true);
        grid.addColumn(task -> !task.getOriginalEstimate().equals(Duration.ZERO) ? DateUtil.createDurationString(task.getTimeSpent(), false, true, false) : "").setHeader("Time Spent").setAutoWidth(true);
        grid.addColumn(task -> !task.getOriginalEstimate().equals(Duration.ZERO) ? DateUtil.createDurationString(task.getRemainingEstimate(), false, true, false) : "").setHeader("Remaining Estimate").setAutoWidth(true);
        grid.addColumn(task -> !task.getOriginalEstimate().equals(Duration.ZERO) ? String.format("%2.0f%%", task.getProgress().doubleValue() * 100) : "").setHeader("Progress").setAutoWidth(true);
        grid.addColumn(task -> {
            List<Relation> relations = task.getPredecessors();
            if (relations == null || relations.isEmpty()) {
                return "";
            }

            return relations.stream()
                    .map(relation -> {
                        Task predecessor = sprint.getTaskById(relation.getPredecessorId());
                        return predecessor != null ? predecessor.getKey() : "";
                    })
                    .filter(key -> !key.isEmpty())
                    .collect(Collectors.joining(", "));
        }).setHeader("Dependency").setAutoWidth(true);
        grid.addColumn(task -> task.getTaskMode().name()).setHeader("Mode").setAutoWidth(true);

// Or set CSS variables that control padding
//        grid.getElement().getStyle().set("--vaadin-grid-header-cell-height", "auto");
//        grid.getElement().getStyle().set("--vaadin-grid-cell-padding", "4px 8px");
//        grid.getElement().executeJs(
//                "this.querySelector('thead tr th').forEach(cell => cell.style.padding = '4px 8px');"
//        );
//        grid.getElement().executeJs(
//                "this.querySelector('thead tr th').forEach(cell => cell.style.padding = '4px 8px');"
//        );
//        "min-height: var(--lumo-size-s)"
    }

}
