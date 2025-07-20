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

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.component.html.Main;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import com.vaadin.flow.router.*;
import com.vaadin.flow.router.Location;
import com.vaadin.flow.theme.lumo.LumoUtility;
import de.bushnaq.abdalla.projecthub.Context;
import de.bushnaq.abdalla.projecthub.ParameterOptions;
import de.bushnaq.abdalla.projecthub.dto.*;
import de.bushnaq.abdalla.projecthub.report.gantt.GanttUtil;
import de.bushnaq.abdalla.projecthub.rest.api.*;
import de.bushnaq.abdalla.projecthub.ui.MainLayout;
import de.bushnaq.abdalla.projecthub.ui.util.RenderUtil;
import de.bushnaq.abdalla.projecthub.ui.util.VaadinUtil;
import de.bushnaq.abdalla.util.GanttErrorHandler;
import de.bushnaq.abdalla.util.date.DateUtil;
import jakarta.annotation.security.PermitAll;
import jakarta.annotation.security.RolesAllowed;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.time.Clock;
import java.time.Duration;
import java.time.LocalDateTime;
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
    public static final String            CREATE_TASK_BUTTON    = "create-task-button";
    public static final String            TASK_GRID_NAME_PREFIX = "task-grid-name-";
    public static final String            TASK_LIST_PAGE_TITLE  = "task-list-page-title";
    private final       Clock             clock;
    @Autowired
    protected           Context           context;
    private final       GanttErrorHandler eh                    = new GanttErrorHandler();
    private final       FeatureApi        featureApi;
    private             Long              featureId;
    private final       Image             ganttChart            = new Image();
    private             GanttUtil         ganttUtil;
    private             Grid<Task>        grid;
    private final       Logger            logger                = LoggerFactory.getLogger(this.getClass());
    private final       ProductApi        productApi;
    private             Long              productId;
    private             Sprint            sprint;
    private final       SprintApi         sprintApi;
    private             Long              sprintId;
    private final       LocalDateTime     start                 = LocalDateTime.now().withHour(8).withMinute(0).withSecond(0).withNano(0);
    private final       TaskApi           taskApi;
    private final       UserApi           userApi;
    private final       VersionApi        versionApi;
    private             Long              versionId;
    private final       WorklogApi        worklogApi;

    public TaskListView(WorklogApi worklogApi, TaskApi taskApi, SprintApi sprintApi, ProductApi productApi, VersionApi versionApi, FeatureApi featureApi, UserApi userApi, Clock clock) {
        this.worklogApi = worklogApi;
        this.taskApi    = taskApi;
        this.sprintApi  = sprintApi;
        this.productApi = productApi;
        this.versionApi = versionApi;
        this.featureApi = featureApi;
        this.userApi    = userApi;
        this.clock      = clock;

        try {
            setSizeFull();
            addClassNames(LumoUtility.BoxSizing.BORDER, LumoUtility.Display.FLEX, LumoUtility.FlexDirection.COLUMN);
            add(VaadinUtil.createHeader("Tasks", TASK_LIST_PAGE_TITLE, VaadinIcon.TASKS, CREATE_TASK_BUTTON, () -> createTask()), createGrid(clock));
        } catch (Exception e) {
            logger.error("Error initializing TaskListView", e);
            throw e;
        }
    }

    /**
     * Adds a column with edit/save/cancel buttons for inline editing
     */
    private void addEditColumn() {
        Grid.Column<Task> editorColumn = grid.addComponentColumn(task -> {
            Button editButton = new com.vaadin.flow.component.button.Button("Edit");
            editButton.addThemeVariants(com.vaadin.flow.component.button.ButtonVariant.LUMO_SMALL);

            // Start edit mode when the edit button is clicked
            editButton.addClickListener(e -> {
                if (grid.getEditor().isOpen()) {
                    grid.getEditor().cancel();
                }
                grid.getEditor().editItem(task);
            });

            return editButton;
        }).setWidth("150px").setHeader("Actions").setFlexGrow(0);

        // Add save and cancel buttons to the editor toolbar
        Button saveButton = new Button("Save", e -> grid.getEditor().save());
        saveButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY, ButtonVariant.LUMO_SMALL);

        Button cancelButton = new Button(VaadinIcon.CLOSE.create(), e -> grid.getEditor().cancel());
        cancelButton.addThemeVariants(ButtonVariant.LUMO_ICON, ButtonVariant.LUMO_ERROR);

        HorizontalLayout actions = new HorizontalLayout(saveButton, cancelButton);
        actions.setPadding(false);

        editorColumn.setEditorComponent(actions);
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
//            pageTitle.setText("Task of Sprint ID: " + sprintId);
        }
        ganttUtil = new GanttUtil(context);
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
        generateGanttChart();
    }

    /**
     * Creates a text field component for editing task durations
     *
     * @return TextField component for the duration editor
     */
    private TextField createDurationEditor() {
        TextField durationField = new TextField();
        durationField.setWidthFull();


        return durationField;
    }

    private Grid<Task> createGrid(Clock clock) {
        grid = new Grid<>();

        // Create and configure binder for the grid editor
        com.vaadin.flow.data.binder.Binder<Task> binder = new com.vaadin.flow.data.binder.Binder<>(Task.class);
        grid.getEditor().setBinder(binder);

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

        add(grid);
        return grid;
    }

    /**
     * Creates a text field component for editing task names
     *
     * @return TextField component for the name editor
     */
    private com.vaadin.flow.component.textfield.TextField createNameEditor() {
        com.vaadin.flow.component.textfield.TextField nameField = new com.vaadin.flow.component.textfield.TextField();
        nameField.setWidthFull();


        return nameField;
    }

    private void createTask() {
        Task task = new Task();
        task.setName("New Task");
        task.setSprint(sprint);
        task.setSprintId(sprint.getId());
//        task.setStart(start);
        Duration work = Duration.ofHours(7).plus(Duration.ofMinutes(30));
        task.setOriginalEstimate(work);
        task.setRemainingEstimate(work);


        Task saved = taskApi.persist(task);
//        saved.setSprint(sprint);
//        sprint.addTask(saved);
        loadData();
        refreshGrid();
    }

    private void generateGanttChart() {
        try {
            long time = System.currentTimeMillis();
            RenderUtil.generateGanttChartImage(context, sprint, ganttChart);

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
            logger.error(e.getMessage(), e);
            // Convert stack trace to string
            StringWriter stringWriter = new StringWriter();
            PrintWriter  printWriter  = new PrintWriter(stringWriter);
            e.printStackTrace(printWriter);
            String stackTrace = stringWriter.toString();

            // Display error message with stack trace
            Paragraph errorParagraph      = new Paragraph("Error generating gantt chart: " + e.getMessage());
            Paragraph stackTraceParagraph = new Paragraph(stackTrace);
            stackTraceParagraph.getStyle().set("white-space", "pre-wrap").set("font-family", "monospace").set("font-size", "12px");

            Div errorContainer = new Div(errorParagraph, stackTraceParagraph);
            add(errorContainer);
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
        ganttUtil.levelResources(eh, sprint, "", ParameterOptions.getLocalNow());
    }

    private void refreshGrid() {
        grid.setItems(sprint.getTasks());
        generateGanttChart();
    }

    private void setupGridColumns() {
        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofLocalizedDateTime(FormatStyle.LONG).withZone(clock.getZone()).withLocale(getLocale());

        grid.addColumn(Task::getKey).setHeader("Key").setAutoWidth(true);
        grid.addColumn(task -> task.getParentTask() != null ? task.getParentTask().getKey() : "").setHeader("Parent").setAutoWidth(true);

        // Create an editable column for the task name with validation for text and numbers only
        Grid.Column<Task> nameColumn = grid.addColumn(new ComponentRenderer<>(task -> {
            Div div = new Div();
            div.add(task.getName());
            div.setId(TASK_GRID_NAME_PREFIX + task.getName());
            return div;
        })).setHeader("Name").setAutoWidth(true);

        // Create the name editor field
        com.vaadin.flow.component.textfield.TextField nameEditor = createNameEditor();
        nameColumn.setEditorComponent(nameEditor);

        // Bind the name field to the Task's name property
        grid.getEditor().getBinder()
                .forField(nameEditor)
                .withValidator(name -> name.matches("[a-zA-Z0-9 ]+"), "Only letters and numbers are allowed")
                .bind(Task::getName, Task::setName);

        grid.addColumn(task -> task.getResourceId() != null ? sprint.getuser(task.getResourceId()).getName() : "").setHeader("Assigned").setAutoWidth(true);
        grid.addColumn(task -> task.getStart() != null ? dateTimeFormatter.format(task.getStart()) : "").setHeader("Start").setAutoWidth(true);
        grid.addColumn(task -> task.getFinish() != null ? dateTimeFormatter.format(task.getFinish()) : "").setHeader("End").setAutoWidth(true);

        // Add an editable column for original estimate
        Grid.Column<Task> originalEstimateColumn = grid.addColumn(
                task -> !task.getOriginalEstimate().equals(Duration.ZERO) ? DateUtil.createWorkDayDurationString(task.getOriginalEstimate()) : ""
        ).setHeader("Original Estimate").setAutoWidth(true);

        // Create a text field for duration editing
        com.vaadin.flow.component.textfield.TextField originalEstimateEditor = createDurationEditor();
        originalEstimateColumn.setEditorComponent(originalEstimateEditor);

        // Bind the duration field to the Task's originalEstimate property with validation
        grid.getEditor().getBinder()
                .forField(originalEstimateEditor)
                .withValidator(duration -> {
                    try {
                        DateUtil.parseWorkDayDurationString(duration.strip());
                        return true;
                    } catch (IllegalArgumentException e) {
                        return false;
                    }
                }, "Invalid duration format. Use format like '1d 2h 30m'")
                .withConverter(
                        // String to Duration conversion
                        duration -> {
                            try {
                                return DateUtil.parseWorkDayDurationString(duration.strip());
                            } catch (IllegalArgumentException e) {
                                return Duration.ZERO;
                            }
                        },
                        // Duration to String conversion
                        duration -> DateUtil.createWorkDayDurationString(duration)
                )
                .bind(Task::getOriginalEstimate, (task, newEstimate) -> {
                    task.setOriginalEstimate(newEstimate);
                    // Also update remaining estimate if it was equal to the original estimate
                    if (task.getRemainingEstimate().equals(task.getOriginalEstimate()) || task.getRemainingEstimate().equals(Duration.ZERO)) {
                        task.setRemainingEstimate(newEstimate);
                    }
                });

        grid.addColumn(task -> !task.getTimeSpent().equals(Duration.ZERO) ? DateUtil.createWorkDayDurationString(task.getTimeSpent()) : "").setHeader("Time Spent").setAutoWidth(true);
        grid.addColumn(task -> !task.getRemainingEstimate().equals(Duration.ZERO) ? DateUtil.createWorkDayDurationString(task.getRemainingEstimate()) : "").setHeader("Remaining Estimate").setAutoWidth(true);
        grid.addColumn(task -> task.getProgress() != null ? String.format("%2.0f%%", task.getProgress().doubleValue() * 100) : "").setHeader("Progress").setAutoWidth(true);
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

        // Add edit column with edit and save buttons
        addEditColumn();

        // Make the grid editable with buffered mode
        grid.getEditor().setBuffered(true);

        // Close the editor when clicking outside
        grid.getEditor().addCloseListener(e -> {
            Task task = e.getItem();
            if (task != null) {
                // Check if editor was closed with a save operation
                // We need to manually track the save operation since isSaved() is not available
                task.setStart(null);// Reset start date to null to force recalculation of duration and finish
                taskApi.persist(task);
                loadData();
                refreshGrid();
            }
        });
    }

}
