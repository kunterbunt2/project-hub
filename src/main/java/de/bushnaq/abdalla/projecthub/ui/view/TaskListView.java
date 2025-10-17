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

import com.vaadin.flow.component.Svg;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.datetimepicker.DateTimePicker;
import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.html.Div;
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
import de.bushnaq.abdalla.projecthub.ui.component.AbstractMainGrid;
import de.bushnaq.abdalla.projecthub.ui.util.RenderUtil;
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
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

@Route("task-list")
@PageTitle("Task List Page")
@CssImport("./styles/grid-styles.css")
@PermitAll // When security is enabled, allow all authenticated users
@RolesAllowed({"USER", "ADMIN"}) // Allow access to users with specific roles
public class TaskListView extends Main implements AfterNavigationObserver {
    public static final String            CANCEL_BUTTON_ID      = "cancel-tasks-button";
    public static final String            CREATE_TASK_BUTTON    = "create-task-button";
    public static final String            EDIT_BUTTON_ID        = "edit-tasks-button";
    public static final String            SAVE_BUTTON_ID        = "save-tasks-button";
    public static final String            TASK_GRID_NAME_PREFIX = "task-grid-name-";
    public static final String            TASK_LIST_PAGE_TITLE  = "task-list-page-title";
    private             Button            cancelButton;
    private final       Clock             clock;
    @Autowired
    protected           Context           context;
    public final        DateTimeFormatter dtfymdhm              = DateTimeFormatter.ofPattern("yyyy.MMM.dd HH:mm");
    private             Button            editButton;
    private final       GanttErrorHandler eh                    = new GanttErrorHandler();
    private final       FeatureApi        featureApi;
    private             Long              featureId;
    private final       Svg               ganttChart            = new Svg();
    private             GanttUtil         ganttUtil;
    private             Grid<Task>        grid;
    private final       HorizontalLayout  headerLayout;
    // Edit mode state management
    private             boolean           isEditMode            = false;
    private final       Logger            logger                = LoggerFactory.getLogger(this.getClass());
    private final       Set<Task>         modifiedTasks         = new HashSet<>();
    private final       ProductApi        productApi;
    private             Long              productId;
    private             Button            saveButton;
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
            headerLayout = createHeaderWithButtons();
            add(headerLayout, createGrid(clock));
            this.getStyle().set("padding-left", "var(--lumo-space-m)");
            this.getStyle().set("padding-right", "var(--lumo-space-m)");
        } catch (Exception e) {
            logger.error("Error initializing TaskListView", e);
            throw e;
        }
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
     * Cancel edit mode and discard all changes
     */
    private void cancelEditMode() {
        modifiedTasks.clear();

        // Reload data to discard changes
        loadData();
        refreshGrid();
        exitEditMode();
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
        grid.addThemeVariants(com.vaadin.flow.component.grid.GridVariant.LUMO_NO_BORDER, com.vaadin.flow.component.grid.GridVariant.LUMO_NO_ROW_BORDERS);

        setupGridColumns();

        // Enable keyboard navigation in edit mode
        setupKeyboardNavigation();

        // Add borders between columns
        grid.addThemeVariants(GridVariant.LUMO_ROW_STRIPES);
//        grid.addThemeVariants(GridVariant.LUMO_COLUMN_BORDERS, GridVariant.LUMO_ROW_STRIPES);

        // Add custom styling for more JIRA-like appearance
//        grid.addClassName("jira-style-grid");

        // Apply custom styling directly to make borders lighter gray
//        grid.getElement().getStyle().set("--lumo-contrast-10pct", "#e0e0e0");

        // Remove the built-in scrollbar from the grid
//        grid.getElement().getStyle().set("overflow", "visible");

        // Set height to auto instead of 100% to allow grid to take only needed space
        grid.setHeight("auto");
        grid.setAllRowsVisible(true);
        setWidthFull();
        addClassNames(LumoUtility.BoxSizing.BORDER, LumoUtility.Display.FLEX, LumoUtility.FlexDirection.COLUMN);

        add(grid);
        return grid;
    }

    /**
     * Creates the header layout with Create, Edit, Save, and Cancel buttons
     */
    private HorizontalLayout createHeaderWithButtons() {
        HorizontalLayout header = AbstractMainGrid.createHeader("Tasks", TASK_LIST_PAGE_TITLE, VaadinIcon.TASKS, CREATE_TASK_BUTTON, () -> createTask());

        // Create Edit button
        editButton = new Button("Edit", VaadinIcon.EDIT.create());
        editButton.setId(EDIT_BUTTON_ID);
        editButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        editButton.addClickListener(e -> enterEditMode());

        // Create Save button
        saveButton = new Button("Save", VaadinIcon.CHECK.create());
        saveButton.setId(SAVE_BUTTON_ID);
        saveButton.addThemeVariants(ButtonVariant.LUMO_SUCCESS);
        saveButton.setVisible(false);
        saveButton.addClickListener(e -> saveAllChanges());

        // Create Cancel button
        cancelButton = new Button("Cancel", VaadinIcon.CLOSE.create());
        cancelButton.setId(CANCEL_BUTTON_ID);
        cancelButton.addThemeVariants(ButtonVariant.LUMO_ERROR);
        cancelButton.setVisible(false);
        cancelButton.addClickListener(e -> cancelEditMode());

        // Add buttons to header
        header.add(editButton, saveButton, cancelButton);

        return header;
    }

//    /**
//     * Creates a text field component for editing task names
//     *
//     * @return TextField component for the name editor
//     */
//    private com.vaadin.flow.component.textfield.TextField createNameEditor() {
//        com.vaadin.flow.component.textfield.TextField nameField = new com.vaadin.flow.component.textfield.TextField();
//        nameField.setWidthFull();
//
//
//        return nameField;
//    }

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

    /**
     * Enter edit mode - enable editing for all rows
     */
    private void enterEditMode() {
        isEditMode = true;
        modifiedTasks.clear();

        // Update button visibility
        editButton.setVisible(false);
        saveButton.setVisible(true);
        cancelButton.setVisible(true);

        // Add visual feedback for edit mode
        grid.addClassName("edit-mode");

        // Refresh grid to show editable components
        grid.getDataProvider().refreshAll();
    }

    /**
     * Exit edit mode
     */
    private void exitEditMode() {
        isEditMode = false;

        // Update button visibility
        editButton.setVisible(true);
        saveButton.setVisible(false);
        cancelButton.setVisible(false);

        // Remove visual feedback
        grid.removeClassName("edit-mode");

        // Refresh grid to show read-only components
        grid.getDataProvider().refreshAll();
    }

    private void generateGanttChart() {
        try {
            long time = System.currentTimeMillis();
            RenderUtil.generateGanttChartSvg(context, sprint, ganttChart);

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

    /**
     * Mark a task as modified
     */
    private void markTaskAsModified(Task task) {
        modifiedTasks.add(task);
        logger.debug("Task {} marked as modified. Total modified: {}", task.getKey(), modifiedTasks.size());
    }

    private void refreshGrid() {
        grid.setItems(sprint.getTasks());
        generateGanttChart();
    }

    /**
     * Save all modified tasks to backend
     */
    private void saveAllChanges() {
        if (modifiedTasks.isEmpty()) {
            exitEditMode();
            return;
        }

        logger.info("Saving {} modified tasks", modifiedTasks.size());

        // Persist all modified tasks
        for (Task task : modifiedTasks) {
            if (!task.isMilestone())
                task.setStart(null); // Reset start date to force recalculation
            taskApi.persist(task);
        }

        // Clear modified tasks and reload data
        modifiedTasks.clear();
        loadData();
        refreshGrid();
        exitEditMode();
    }

    private void setupGridColumns() {
        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofLocalizedDateTime(FormatStyle.LONG).withZone(clock.getZone()).withLocale(getLocale());

        //key
        {
            grid.addColumn(Task::getKey).setHeader("Key").setAutoWidth(true);
        }
        //Type
        {
            grid.addColumn(task -> {
                if (task.isMilestone()) {
                    return "Milestone";
                } else if (task.isStory()) {
                    return "Story";
                } else if (task.isTask()) {
                    return "Task";
                } else {
                    return "";
                }
            }).setHeader("Type").setAutoWidth(true);
        }
        //Parent
        {
            grid.addColumn(task -> task.getParentTask() != null ? task.getParentTask().getKey() : "").setHeader("Parent").setAutoWidth(true);
        }
        //name - Editable for all task types
        {
            Grid.Column<Task> nameColumn = grid.addColumn(new ComponentRenderer<>(task -> {
                if (isEditMode) {
                    TextField nameField = new TextField();
                    nameField.setValue(task.getName() != null ? task.getName() : "");
                    nameField.setWidthFull();
                    nameField.addValueChangeListener(e -> {
                        if (e.isFromClient()) {
                            task.setName(e.getValue());
                            markTaskAsModified(task);
                        }
                    });
                    return nameField;
                } else {
                    Div div = new Div();
                    div.setText(task.getName());
                    div.setId(TASK_GRID_NAME_PREFIX + task.getName());
                    return div;
                }
            })).setHeader("Name").setAutoWidth(true).setFlexGrow(1);
        }
        //Start - Editable only for Milestone tasks
        {
            grid.addColumn(new ComponentRenderer<>(task -> {
                if (isEditMode && task.isMilestone()) {
                    // Editable for Milestone tasks
                    DateTimePicker startField = new DateTimePicker();
                    startField.setValue(task.getStart() != null ? task.getStart() : LocalDateTime.now());
                    startField.setWidthFull();

                    startField.addValueChangeListener(e -> {
                        if (e.isFromClient()) {
                            try {
                                LocalDateTime dateTime = e.getValue();
                                if (dateTime != null) {
                                    task.setStart(dateTime);
                                    markTaskAsModified(task);
                                    startField.setInvalid(false);
                                } else {
                                    task.setStart(null);
                                    markTaskAsModified(task);
                                    startField.setInvalid(false);
                                }
                            } catch (Exception ex) {
                                startField.setInvalid(true);
                                startField.setErrorMessage("Invalid date/time format");
                            }
                        }
                    });

                    return startField;
                } else {
                    // Read-only for Story and Task tasks
                    Div div = new Div();
                    if (task.isMilestone())
                        div.setText(task.getStart() != null ? DateUtil.createDateString(task.getStart(), dtfymdhm) : "");
                    else
                        div.setText("");
                    return div;
                }
            })).setHeader("Start").setAutoWidth(true);
        }
        //Assigned - Editable only for Task tasks
        {
            grid.addColumn(new ComponentRenderer<>(task -> {
                if (isEditMode && task.isTask()) {
                    // Editable for Task tasks
                    ComboBox<User> userComboBox = new ComboBox<>();
                    userComboBox.setAllowCustomValue(false);
                    userComboBox.setClearButtonVisible(true);
                    userComboBox.setWidthFull();
                    userComboBox.setItemLabelGenerator(User::getName);

                    // Load ALL users from the system (not just sprint users) so we can assign new users
                    List<User> allUsers = userApi.getAll();
                    userComboBox.setItems(allUsers);

                    // Set current value only if task has an assigned user
                    if (task.getResourceId() != null) {
                        try {
                            User currentUser = sprint.getuser(task.getResourceId());
                            if (currentUser != null && allUsers.contains(currentUser)) {
                                userComboBox.setValue(currentUser);
                            }
                        } catch (Exception ex) {
                            logger.warn("Could not set user for task {}: {}", task.getKey(), ex.getMessage());
                        }
                    }

                    userComboBox.addValueChangeListener(e -> {
                        if (e.isFromClient()) {
                            User selectedUser = e.getValue();
                            task.setResourceId(selectedUser != null ? selectedUser.getId() : null);
                            markTaskAsModified(task);
                        }
                    });

                    return userComboBox;
                } else {
                    // Read-only for Milestone and Story tasks
                    Div div = new Div();
                    if (task.isTask())
                        div.setText(task.getResourceId() != null ? sprint.getuser(task.getResourceId()).getName() : "");
                    else
                        div.setText("");
                    return div;
                }
            })).setHeader("Assigned").setAutoWidth(true);
        }

        //Min Estimate - Editable only for Task tasks
        {
            grid.addColumn(new ComponentRenderer<>(task -> {
                if (isEditMode && task.isTask()) {
                    // Editable for Task tasks
                    TextField estimateField = new TextField();
                    estimateField.setValue(!task.getMinEstimate().equals(Duration.ZERO) ?
                            DateUtil.createWorkDayDurationString(task.getMinEstimate()) : "");
                    estimateField.setWidthFull();
                    estimateField.setPlaceholder("e.g., 1d 2h 30m");

                    estimateField.addValueChangeListener(e -> {
                        if (e.isFromClient()) {
                            try {
                                Duration duration = DateUtil.parseWorkDayDurationString(e.getValue().strip());
                                task.setMinEstimate(duration);
                                markTaskAsModified(task);
                                estimateField.setInvalid(false);
                            } catch (IllegalArgumentException ex) {
                                estimateField.setInvalid(true);
                                estimateField.setErrorMessage("Invalid format");
                            }
                        }
                    });

                    return estimateField;
                } else {
                    // Read-only for Milestone and Story tasks
                    Div div = new Div();
                    if (task.isTask())
                        div.setText(!task.getMinEstimate().equals(Duration.ZERO) ? DateUtil.createWorkDayDurationString(task.getMinEstimate()) : "");
                    else
                        div.setText("");
                    return div;
                }
            })).setHeader("Min Estimate").setAutoWidth(true);
        }
        //Max Estimate - Editable only for Task tasks
        {
            grid.addColumn(new ComponentRenderer<>(task -> {
                if (isEditMode && task.isTask()) {
                    // Editable for Task tasks
                    TextField estimateField = new TextField();
                    estimateField.setValue(!task.getMaxEstimate().equals(Duration.ZERO) ?
                            DateUtil.createWorkDayDurationString(task.getMaxEstimate()) : "");
                    estimateField.setWidthFull();
                    estimateField.setPlaceholder("e.g., 1d 2h 30m");

                    estimateField.addValueChangeListener(e -> {
                        if (e.isFromClient()) {
                            try {
                                Duration duration = DateUtil.parseWorkDayDurationString(e.getValue().strip());
                                task.setMaxEstimate(duration);
                                markTaskAsModified(task);
                                estimateField.setInvalid(false);
                            } catch (IllegalArgumentException ex) {
                                estimateField.setInvalid(true);
                                estimateField.setErrorMessage("Invalid format");
                            }
                        }
                    });

                    return estimateField;
                } else {
                    // Read-only for Milestone and Story tasks
                    Div div = new Div();
                    if (task.isTask())
                        div.setText(!task.getMaxEstimate().equals(Duration.ZERO) ? DateUtil.createWorkDayDurationString(task.getMaxEstimate()) : "");
                    else
                        div.setText("");
                    return div;
                }
            })).setHeader("Max Estimate").setAutoWidth(true);
        }

        //Dependency
        {
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
        }
//        grid.addColumn(task -> task.getTaskMode().name()).setHeader("Mode").setAutoWidth(true);
    }

    /**
     * Setup keyboard navigation for Excel-like behavior
     */
    private void setupKeyboardNavigation() {
        // Add keyboard navigation support for Tab key navigation between editable cells
        grid.getElement().executeJs(
                """
                        const grid = this;
                        
                        // Add event listener for Tab key navigation
                        grid.addEventListener('keydown', function(e) {
                            const activeElement = grid.getRootNode().activeElement || document.activeElement;
                        
                            // Check if we're in an input field or combobox
                            const isInInput = activeElement && (
                                activeElement.tagName === 'INPUT' || 
                                activeElement.tagName === 'VAADIN-TEXT-FIELD' ||
                                activeElement.tagName === 'VAADIN-COMBO-BOX'
                            );
                        
                            if (!isInInput) return;
                        
                            // Find current cell position
                            let currentCell = activeElement.closest('vaadin-grid-cell-content');
                            if (!currentCell) {
                                currentCell = activeElement.getRootNode().host?.closest('vaadin-grid-cell-content');
                            }
                            if (!currentCell) return;
                        
                            // Handle Tab key (move to next field)
                            if (e.key === 'Tab' && !e.shiftKey) {
                                e.preventDefault();
                                const next = currentCell.parentElement?.nextElementSibling?.querySelector('input, vaadin-combo-box');
                                if (next) {
                                    setTimeout(() => {
                                        if (next.tagName === 'VAADIN-COMBO-BOX') {
                                            next.focus();
                                        } else {
                                            next.focus();
                                            next.select();
                                        }
                                    }, 10);
                                } else {
                                    // Move to next row, first editable column
                                    const row = currentCell.closest('tr');
                                    const nextRow = row?.nextElementSibling;
                                    if (nextRow) {
                                        const firstInput = nextRow.querySelector('input, vaadin-combo-box');
                                        if (firstInput) {
                                            setTimeout(() => {
                                                if (firstInput.tagName === 'VAADIN-COMBO-BOX') {
                                                    firstInput.focus();
                                                } else {
                                                    firstInput.focus();
                                                    firstInput.select();
                                                }
                                            }, 10);
                                        }
                                    }
                                }
                            }
                        
                            // Handle Shift+Tab (move to previous field)
                            if (e.key === 'Tab' && e.shiftKey) {
                                e.preventDefault();
                                const prev = currentCell.parentElement?.previousElementSibling?.querySelector('input, vaadin-combo-box');
                                if (prev) {
                                    setTimeout(() => {
                                        if (prev.tagName === 'VAADIN-COMBO-BOX') {
                                            prev.focus();
                                        } else {
                                            prev.focus();
                                            prev.select();
                                        }
                                    }, 10);
                                } else {
                                    // Move to previous row, last editable column
                                    const row = currentCell.closest('tr');
                                    const prevRow = row?.previousElementSibling;
                                    if (prevRow) {
                                        const inputs = prevRow.querySelectorAll('input, vaadin-combo-box');
                                        const lastInput = inputs[inputs.length - 1];
                                        if (lastInput) {
                                            setTimeout(() => {
                                                if (lastInput.tagName === 'VAADIN-COMBO-BOX') {
                                                    lastInput.focus();
                                                } else {
                                                    lastInput.focus();
                                                    lastInput.select();
                                                }
                                            }, 10);
                                        }
                                    }
                                }
                            }
                        }, false);
                        """
        );
    }

}
