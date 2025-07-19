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

import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.html.*;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import com.vaadin.flow.router.*;
import com.vaadin.flow.router.Location;
import com.vaadin.flow.theme.lumo.LumoUtility;
import de.bushnaq.abdalla.projecthub.Context;
import de.bushnaq.abdalla.projecthub.ParameterOptions;
import de.bushnaq.abdalla.projecthub.dto.*;
import de.bushnaq.abdalla.projecthub.rest.api.*;
import de.bushnaq.abdalla.projecthub.ui.MainLayout;
import de.bushnaq.abdalla.projecthub.ui.util.RenderUtil;
import de.bushnaq.abdalla.util.date.DateUtil;
import jakarta.annotation.security.PermitAll;
import jakarta.annotation.security.RolesAllowed;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.time.Clock;
import java.time.Duration;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

@Route("task-list")
@PageTitle("Task List Page")
@CssImport("./styles/grid-styles.css")
@PermitAll // When security is enabled, allow all authenticated users
@RolesAllowed({"USER", "ADMIN"}) // Allow access to users with specific roles
public class TaskListView extends Main implements AfterNavigationObserver {
    public static final String     TASK_GRID_NAME_PREFIX = "task-grid-name-";
    private final       Clock      clock;
    @Autowired
    protected           Context    context;
    private final       FeatureApi featureApi;
    private             Long       featureId;
    private final       Grid<Task> grid;
    final               Logger     logger                = LoggerFactory.getLogger(this.getClass());
    private final       H2         pageTitle;
    private final       ProductApi productApi;
    private             Long       productId;
    private             Sprint     sprint;
    private final       SprintApi  sprintApi;
    private             Long       sprintId;
    private final       TaskApi    taskApi;
    private final       UserApi    userApi;
    private final       VersionApi versionApi;
    private             Long       versionId;
    private final       WorklogApi worklogApi;

    public TaskListView(WorklogApi worklogApi, TaskApi taskApi, SprintApi sprintApi, ProductApi productApi, VersionApi versionApi, FeatureApi featureApi, UserApi userApi, Clock clock) {
        this.worklogApi = worklogApi;
        this.taskApi    = taskApi;
        this.sprintApi  = sprintApi;
        this.productApi = productApi;
        this.versionApi = versionApi;
        this.featureApi = featureApi;
        this.userApi    = userApi;
        this.clock      = clock;

        // Create title layout with icon
        HorizontalLayout titleLayout = new HorizontalLayout();
        titleLayout.setSpacing(true);
        titleLayout.setAlignItems(FlexComponent.Alignment.CENTER);

        Icon taskIcon = new Icon(VaadinIcon.TASKS);
        pageTitle = new H2("Tasks");
        pageTitle.addClassNames(
                LumoUtility.Margin.Top.MEDIUM,
                LumoUtility.Margin.Bottom.SMALL
        );

        titleLayout.add(taskIcon, pageTitle);

        grid = new Grid<>();
        setupGridColumns();
        // Add borders between columns
        grid.addThemeVariants(GridVariant.LUMO_COLUMN_BORDERS, GridVariant.LUMO_ROW_STRIPES);

        // Add custom styling for more JIRA-like appearance
        grid.addClassName("jira-style-grid");

        // Apply custom styling directly to make borders lighter gray
        grid.getElement().getStyle().set("--lumo-contrast-10pct", "#e0e0e0");

        // Remove the built-in scrollbar from the grid
        grid.getElement().getStyle().set("overflow", "visible");

        // Set height to auto instead of 100% to allow grid to take only needed space
        grid.setHeight("auto");
        grid.setAllRowsVisible(true);
        setWidthFull();
        addClassNames(LumoUtility.BoxSizing.BORDER, LumoUtility.Display.FLEX, LumoUtility.FlexDirection.COLUMN);

        add(titleLayout, grid);
    }

    @Override
    public void afterNavigation(AfterNavigationEvent event) {
        //- Get query parameters
        Location        location        = event.getLocation();
        QueryParameters queryParameters = location.getQueryParameters();
        if (queryParameters.getParameters().containsKey("product")) {
            this.productId = Long.parseLong(queryParameters.getParameters().get("product").getFirst());
        }
        if (queryParameters.getParameters().containsKey("version")) {
            this.versionId = Long.parseLong(queryParameters.getParameters().get("version").getFirst());
        }
        if (queryParameters.getParameters().containsKey("feature")) {
            this.featureId = Long.parseLong(queryParameters.getParameters().get("feature").getFirst());
        }
        if (queryParameters.getParameters().containsKey("sprint")) {
            this.sprintId = Long.parseLong(queryParameters.getParameters().get("sprint").getFirst());
            pageTitle.setText("Task of Sprint ID: " + sprintId);
        }
        loadData();

        //- Update breadcrumbs
        getElement().getParent().getComponent()
                .ifPresent(component -> {
                    if (component instanceof MainLayout mainLayout) {
                        mainLayout.getBreadcrumbs().clear();
                        Product product = productApi.getById(productId);
                        mainLayout.getBreadcrumbs().addItem("Products (" + product.getName() + ")", ProductListView.class);
                        {
                            Map<String, String> params = new HashMap<>();
                            params.put("product", String.valueOf(productId));
                            Version version = versionApi.getById(versionId);
                            mainLayout.getBreadcrumbs().addItem("Versions (" + version.getName() + ")", VersionListView.class, params);
                        }
                        {
                            Map<String, String> params = new HashMap<>();
                            params.put("product", String.valueOf(productId));
                            params.put("version", String.valueOf(versionId));
                            Feature feature = featureApi.getById(featureId);
                            mainLayout.getBreadcrumbs().addItem("Features (" + feature.getName() + ")", FeatureListView.class, params);
                        }
                        {
                            Map<String, String> params = new HashMap<>();
                            params.put("product", String.valueOf(productId));
                            params.put("version", String.valueOf(versionId));
                            params.put("feature", String.valueOf(featureId));
                            mainLayout.getBreadcrumbs().addItem("Sprints (" + sprint.getName() + ")", SprintListView.class, params);
                        }
                        {
                            Map<String, String> params = new HashMap<>();
                            params.put("product", String.valueOf(productId));
                            params.put("version", String.valueOf(versionId));
                            params.put("feature", String.valueOf(featureId));
                            params.put("sprint", String.valueOf(sprintId));
                            mainLayout.getBreadcrumbs().addItem("Tasks", TaskListView.class, params);
                        }
                    }
                });

        //- populate grid
//        pageTitle.setText("Task of Sprint ID: " + sprintId);
        grid.setItems(sprint.getTasks());
        createGanttChart();
    }

    private void createGanttChart() {
        try {
            long  time       = System.currentTimeMillis();
            Image ganttChart = RenderUtil.generateGanttChartImage(context, sprint);

            // Configure Gantt chart for proper scrolling display
            ganttChart.getStyle()
                    .set("margin-top", "var(--lumo-space-m)")
                    .set("max-width", "100%")
                    .set("height", "auto")
                    .set("display", "block");

            // Add the chart in a container div for better scrolling behavior
            Div chartContainer = new Div(ganttChart);
            chartContainer.getStyle()
                    .set("overflow-x", "auto")
                    .set("width", "100%");

            add(chartContainer);
            logger.info("Gantt chart generated in {} ms", System.currentTimeMillis() - time);
        } catch (Exception e) {
            add(new Paragraph("Error generating gantt chart: " + e.getMessage()));
        }
    }

    private void loadData() {
        //- populate grid with tasks of the sprint
        long time = System.currentTimeMillis();

        // Capture the security context from the current thread
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        // Load in parallel with security context propagation
        CompletableFuture<Sprint> sprintFuture = CompletableFuture.supplyAsync(() -> {
            // Set security context in this thread
            SecurityContext context = SecurityContextHolder.createEmptyContext();
            context.setAuthentication(authentication);
            SecurityContextHolder.setContext(context);
            try {
                Sprint s = sprintApi.getById(sprintId);
                s.initialize();
                return s;
            } finally {
                SecurityContextHolder.clearContext();// Clear the security context after execution
            }
        });

        CompletableFuture<List<User>> usersFuture = CompletableFuture.supplyAsync(() -> {
            // Set security context in this thread
            SecurityContext context = SecurityContextHolder.createEmptyContext();
            context.setAuthentication(authentication);
            SecurityContextHolder.setContext(context);
            try {
                return userApi.getAll(sprintId);
            } finally {
                SecurityContextHolder.clearContext();// Clear the security context after execution

            }
        });

        CompletableFuture<List<Task>> tasksFuture = CompletableFuture.supplyAsync(() -> {
            // Set security context in this thread
            SecurityContext context = SecurityContextHolder.createEmptyContext();
            context.setAuthentication(authentication);
            SecurityContextHolder.setContext(context);
            try {
                return taskApi.getAll(sprintId);
            } finally {
                SecurityContextHolder.clearContext();// Clear the security context after execution
            }
        });

        CompletableFuture<List<Worklog>> worklogsFuture = CompletableFuture.supplyAsync(() -> {
            // Set security context in this thread
            SecurityContext context = SecurityContextHolder.createEmptyContext();
            context.setAuthentication(authentication);
            SecurityContextHolder.setContext(context);
            try {
                return worklogApi.getAll(sprintId);
            } finally {
                SecurityContextHolder.clearContext();// Clear the security context after execution
            }
        });

        // Wait for all futures and combine results
        try {
            sprint = sprintFuture.get();
            logger.info("sprint loaded and initialized in {} ms", System.currentTimeMillis() - time);
            time = System.currentTimeMillis();
            sprint.initUserMap(usersFuture.get());
            sprint.initTaskMap(tasksFuture.get(), worklogsFuture.get());
            logger.info("sprint user, task and worklog maps initialized in {} ms", System.currentTimeMillis() - time);
            sprint.recalculate(ParameterOptions.getLocalNow());
        } catch (InterruptedException | ExecutionException e) {
            logger.error("Error loading sprint data", e);
            // Handle exception appropriately
        }
    }

    private void setupGridColumns() {
        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofLocalizedDateTime(FormatStyle.LONG).withZone(clock.getZone()).withLocale(getLocale());

        grid.addColumn(Task::getKey).setHeader("Key").setAutoWidth(true);
        grid.addColumn(task -> task.getParentTask() != null ? task.getParentTask().getKey() : "").setHeader("Parent").setAutoWidth(true);
        grid.addColumn(new ComponentRenderer<>(task -> {
            Div div    = new Div();
            Div square = new Div();
            square.setMinHeight("16px");
            square.setMaxHeight("16px");
            square.setMinWidth("16px");
            square.setMaxWidth("16px");
            square.getStyle().set("float", "left");
            square.getStyle().set("margin", "1px");
            div.add(square);
            div.add(task.getName());
            div.setId(TASK_GRID_NAME_PREFIX + task.getName());
            return div;
        })).setHeader("Name").setAutoWidth(true);
        grid.addColumn(task -> task.getResourceId() != null ? sprint.getuser(task.getResourceId()).getName() : "").setHeader("Assigned").setAutoWidth(true);
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

    }

}
