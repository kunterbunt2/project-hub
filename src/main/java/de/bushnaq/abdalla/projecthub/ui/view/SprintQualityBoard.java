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

import com.vaadin.flow.component.html.*;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import com.vaadin.flow.router.*;
import com.vaadin.flow.router.Location;
import com.vaadin.flow.server.StreamResource;
import com.vaadin.flow.theme.lumo.LumoUtility;
import de.bushnaq.abdalla.projecthub.Context;
import de.bushnaq.abdalla.projecthub.ParameterOptions;
import de.bushnaq.abdalla.projecthub.dto.*;
import de.bushnaq.abdalla.projecthub.report.burndown.BurnDownChart;
import de.bushnaq.abdalla.projecthub.report.burndown.RenderDao;
import de.bushnaq.abdalla.projecthub.report.gantt.GanttChart;
import de.bushnaq.abdalla.projecthub.rest.api.*;
import de.bushnaq.abdalla.projecthub.ui.HtmlColor;
import de.bushnaq.abdalla.projecthub.ui.MainLayout;
import de.bushnaq.abdalla.util.Util;
import de.bushnaq.abdalla.util.date.DateUtil;
import de.bushnaq.abdalla.util.date.ReportUtil;
import jakarta.annotation.security.PermitAll;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.time.Clock;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.function.Function;

// Create a utility method for generating two-part cells


@Route("sprint-quality-board")
@PageTitle("Sprint Quality Board")
@PermitAll // When security is enabled, allow all authenticated users
public class SprintQualityBoard extends Main implements AfterNavigationObserver {
    public static final String        BURNDOWN_CHART          = "burndown-chart";
    public static final String        GANTT_CHART             = "gantt-chart";
    public static final String        SPRINT_GRID_NAME_PREFIX = "sprint-grid-name-";
    private final       Clock         clock;
    @Autowired
    protected           Context       context;
    private final       LocalDateTime created;
    private final       FeatureApi    featureApi;
    private             Long          featureId;
    final               Logger        logger                  = LoggerFactory.getLogger(this.getClass());
    private final       LocalDateTime now;
    private final       H2            pageTitle;
    private final       ProductApi    productApi;
    private             Long          productId;
    private             Sprint        sprint;
    private final       SprintApi     sprintApi;
    private             Long          sprintId;
    private final       TaskApi       taskApi;
    private final       UserApi       userApi;
    private final       VersionApi    versionApi;
    private             Long          versionId;
    private final       WorklogApi    worklogApi;

    public SprintQualityBoard(WorklogApi worklogApi, TaskApi taskApi, SprintApi sprintApi, ProductApi productApi, VersionApi versionApi, FeatureApi featureApi, UserApi userApi, Clock clock) {
        created         = LocalDateTime.now(clock);
        this.worklogApi = worklogApi;
        this.taskApi    = taskApi;
        this.sprintApi  = sprintApi;
        this.productApi = productApi;
        this.versionApi = versionApi;
        this.featureApi = featureApi;
        this.userApi    = userApi;
        this.clock      = clock;
        this.now        = LocalDateTime.now(clock);

        pageTitle = new H2("Sprint Quality Board");
        pageTitle.addClassNames(
                LumoUtility.Margin.Top.MEDIUM,
                LumoUtility.Margin.Bottom.SMALL
        );
        setSizeFull();
        addClassNames(LumoUtility.BoxSizing.BORDER, LumoUtility.Display.FLEX, LumoUtility.FlexDirection.COLUMN);

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
        }

        loadData();

        pageTitle.setText(sprint.getName());
        //- update breadcrumbs
        getElement().getParent().getComponent()
                .ifPresent(component -> {
                    if (component instanceof MainLayout mainLayout) {
                        mainLayout.getBreadcrumbs().clear();
                        Product product = productApi.getById(productId);
                        mainLayout.getBreadcrumbs().addItem("Products (" + product.getName() + ")", ProductListView.class);
//                        mainLayout.getBreadcrumbs().addItem("Products", ProductListView.class);
                        {
                            Map<String, String> params = new HashMap<>();
                            params.put("product", String.valueOf(productId));
                            Version version = versionApi.getById(versionId);
                            mainLayout.getBreadcrumbs().addItem("Versions (" + version.getName() + ")", VersionListView.class, params);
//                            mainLayout.getBreadcrumbs().addItem("Versions", VersionListView.class, params);
                        }
                        {
                            Map<String, String> params = new HashMap<>();
                            params.put("product", String.valueOf(productId));
                            params.put("version", String.valueOf(versionId));
                            Feature feature = featureApi.getById(featureId);
                            mainLayout.getBreadcrumbs().addItem("Features (" + feature.getName() + ")", FeatureListView.class, params);
                            mainLayout.getBreadcrumbs().addItem("Features", FeatureListView.class, params);
                        }
                        {
                            Map<String, String> params = new HashMap<>();
                            params.put("product", String.valueOf(productId));
                            params.put("version", String.valueOf(versionId));
                            params.put("FEATURE", String.valueOf(featureId));
                            Sprint sprint = sprintApi.getById(sprintId);
                            mainLayout.getBreadcrumbs().addItem("Sprints (" + sprint.getName() + ")", SprintListView.class, params);
//                            mainLayout.getBreadcrumbs().addItem("Sprints", SprintListView.class, params);
                        }
                        {
                            Map<String, String> params = new HashMap<>();
                            params.put("product", String.valueOf(productId));
                            params.put("version", String.valueOf(versionId));
                            params.put("FEATURE", String.valueOf(featureId));
                            params.put("sprint", String.valueOf(sprintId));
                            mainLayout.getBreadcrumbs().addItem("Tasks", TaskListView.class, params);
                        }
                    }
                });
//        renderBurnDownChart();
        createSprintDetailsLayout();
        createGanttChart();
        logTime();
    }

    private Div createFieldDisplay(String label, String value, String status) {
        Div container = new Div();
        container.getStyle()
                .set("display", "flex")
                .set("flex-direction", "column")
                .set("padding", "var(--lumo-space-s)")
                .set("background", "var(--lumo-base-color)");

        // Create a wrapper div for the value part that doesn't stretch
        Div valueWrapper = new Div();
        valueWrapper.getStyle()
                .set("display", "flex") // Use flex to allow inner content to determine size
                .set("width", "auto");  // Don't stretch to full width

        // Value part (top)
        Span valueSpan = new Span(value);
        valueSpan.getStyle()
                .set("font-weight", "500")
                .set("font-size", "var(--lumo-font-size-xl)");

        // Apply status-based styling if status is provided
        if (status != null) {
            valueSpan.getStyle()
                    .set("padding", "0 var(--lumo-space-xs)")
                    .set("border-radius", "var(--lumo-border-radius-s)");

            switch (status) {
                case "GOOD":
                    valueSpan.getStyle().set("background-color", "var(--lumo-success-color)");
                    break;
                case "NORMAL":
                    // Default styling
                    break;
                case "WARNING":
                    valueSpan.getStyle().set("background-color", "rgba(255, 179, 0, 1)");
                    break;
                case "CRITICAL":
                    valueSpan.getStyle().set("background-color", "var(--lumo-error-color)");
                    break;
            }
        }

        valueWrapper.add(valueSpan);

        // Name part with HTML support for special characters
        Span nameSpan = new Span();
        nameSpan.getElement().setProperty("innerHTML", label);
        nameSpan.getStyle()
                .set("font-size", "var(--lumo-font-size-xs)")
                .set("color", "var(--lumo-secondary-text-color)")
                .set("margin-top", "var(--lumo-space-xs)");

        container.add(valueWrapper, nameSpan);
        return container;
    }

    // Overload for backward compatibility
    private Div createFieldDisplay(String label, String value) {
        return createFieldDisplay(label, value, null);
    }

    private void createGanttChart() {
        try {
            long  time       = System.currentTimeMillis();
            Image ganttChart = generateGanttChartImage();
            ganttChart.getStyle()//.set("object-fit", "contain") // Maintain aspect ratio
                    .set("margin-top", "var(--lumo-space-m)");
            add(ganttChart);
            logger.info("Gantt chart generated in {} ms", System.currentTimeMillis() - time);
        } catch (Exception e) {
            add(new Paragraph("Error generating gantt chart: " + e.getMessage()));
        }
    }

    private RenderDao createRenderDao(Context context, Sprint sprint, String column, LocalDateTime now, int chartWidth, int chartHeight, String link) {
        RenderDao dao = new RenderDao();
        dao.context            = context;
        dao.column             = column;
        dao.sprintName         = column + "-burn-down";
        dao.link               = link;
        dao.start              = sprint.getStart();
        dao.now                = now;
        dao.end                = sprint.getEnd();
        dao.release            = sprint.getReleaseDate();
        dao.chartWidth         = chartWidth;
        dao.chartHeight        = chartHeight;
        dao.sprint             = sprint;
        dao.estimatedBestWork  = DateUtil.add(sprint.getWorked(), sprint.getRemaining());
        dao.estimatedWorstWork = null;
        dao.maxWorked          = DateUtil.add(sprint.getWorked(), sprint.getRemaining());
        dao.remaining          = sprint.getRemaining();
        dao.worklog            = sprint.getWorklogs();
        dao.worklogRemaining   = sprint.getWorklogRemaining();
        dao.cssClass           = "scheduleWithMargin";
        dao.graphicsTheme      = context.parameters.graphicsTheme;
        return dao;
    }

    private void createSprintDetailsLayout() {
        final DateTimeFormatter dtfymd = DateTimeFormatter.ofPattern("yyyy.MM.dd");

        // Create a container with CSS Grid layout
        Div gridContainer = new Div();
        gridContainer.getStyle()
                .set("display", "grid")
                .set("grid-template-columns", "repeat(4, 1fr) 2fr repeat(2, 1fr)")  // 4 columns left, 1 middle (wider), 2 right
                .set("grid-template-rows", "auto auto auto auto")  // 3 rows
                .set("gap", "var(--lumo-space-m)")  // spacing between cells
                .set("width", "100%")
                .set("height", "auto");

        Duration estimation                      = DateUtil.add(sprint.getWorked(), sprint.getRemaining());
        String   manDelayString                  = ReportUtil.calcualteManDelayString(sprint.getStart(), now, sprint.getEnd(), sprint.getWorked(), DateUtil.add(sprint.getWorked(), sprint.getRemaining()));
        double   delayFraction                   = ReportUtil.calcualteDelayFraction(sprint.getStart(), now, sprint.getEnd(), sprint.getWorked(), DateUtil.add(sprint.getWorked(), sprint.getRemaining()));
        String   status                          = HtmlColor.calculateStatusColor(delayFraction);
        Double   extrapolatedDelayFraction       = ReportUtil.calculateExtrapolatedScheduleDelayFraction(sprint.getStart(), now, sprint.getEnd(), sprint.getWorked(), DateUtil.add(sprint.getWorked(), sprint.getRemaining()));
        String   extrapolatedStatus              = HtmlColor.calculateStatusColor(extrapolatedDelayFraction);
        String   extrapolatedScheduleDelayString = ReportUtil.calculateExtrapolatedScheduleDelayString(sprint.getStart(), now, sprint.getEnd(), sprint.getWorked(), DateUtil.add(sprint.getWorked(), sprint.getRemaining()));// Delay

        // First row
        gridContainer.add(createFieldDisplay("Sprint Name", sprint.getName()));//column 1
        gridContainer.add(createFieldDisplay("Sprint Start Date", DateUtil.createDateString(sprint.getStart(), dtfymd)));//column 2
        gridContainer.add(createFieldDisplay("Total Work Days", "" + DateUtil.calculateWorkingDaysIncluding(sprint.getStart().toLocalDate(), sprint.getEnd().toLocalDate())));//column 3
        gridContainer.add(createFieldDisplay("Remaining Work Days", "" + DateUtil.calculateWorkingDaysIncluding(now, sprint.getEnd())));//column 4
        gridContainer.add(createFieldDisplay("Current Effort Delay", String.format("%s (%.0f%%)", manDelayString, 100 * delayFraction), status));//column 6
        gridContainer.add(createFieldDisplay("&Sigma; Effort Spent", DateUtil.createDurationString(sprint.getWorked(), false, true, false)));//column 7

        // Second row
        gridContainer.add(createFieldDisplay("", ""));//column 1
        gridContainer.add(createFieldDisplay("Sprint End Date", DateUtil.createDateString(sprint.getEnd(), dtfymd)));//column 2
        gridContainer.add(createFieldDisplay("Expected Progress", String.format("%.0f%%", 100 * ReportUtil.calcualteExpectedProgress(sprint.getStart(), now, sprint.getEnd(), sprint.getWorked(), sprint.getOriginalEstimation(), estimation, sprint.getRemaining()))));//column 3
        gridContainer.add(createFieldDisplay("Progress", String.format("%.0f%%", 100 * ReportUtil.calcualteProgress(sprint.getWorked(), estimation)), status));//column 4
        gridContainer.add(createFieldDisplay("Current Schedule Delay", DateUtil.createDurationString(ReportUtil.calcualteWorkDaysMiliseconsDelay(sprint.getStart(), now, sprint.getEnd(), sprint.getWorked(), sprint.getOriginalEstimation(), estimation, sprint.getRemaining()), false, true, false), status));//column 6
        gridContainer.add(createFieldDisplay("&Sigma; Effort Estimate", DateUtil.createWorkDayDurationString(DateUtil.add(sprint.getWorked(), sprint.getRemaining()), false, true, false)));//column 7


        // Third row
        gridContainer.add(createFieldDisplay("3.1", "a"));//column 1
        if (sprint.getRemaining() == null || sprint.getRemaining().equals(Duration.ZERO)) {
            gridContainer.add(createFieldDisplay("Actual Sprint Release Date", DateUtil.createDateString(sprint.getReleaseDate(), dtfymd), status));//column 2
        } else {
            gridContainer.add(createFieldDisplay("Extrapolated Sprint Release Date", DateUtil.createDateString(sprint.getReleaseDate(), dtfymd), extrapolatedStatus));//column 2
        }
        gridContainer.add(createFieldDisplay("Optimal Efficiency", ReportUtil.createPersonDayEfficiencyString(ReportUtil.calcualteOptimaleEfficiency(sprint.getStart(), sprint.getEnd(), DateUtil.add(sprint.getWorked(), sprint.getRemaining())))));//column 3
        gridContainer.add(createFieldDisplay("Efficiency", ReportUtil.createPersonDayEfficiencyString(ReportUtil.calcualteEfficiency(sprint.getStart(), now, sprint.getEnd(), sprint.getWorked(), sprint.getRemaining())), status));//column 4
        if (extrapolatedDelayFraction != null) {
            gridContainer.add(createFieldDisplay("Extrapolated Schedule Delay", String.format("%s (%.0f%%)", extrapolatedScheduleDelayString, 100 * extrapolatedDelayFraction), extrapolatedStatus));//column 6
        } else {
            gridContainer.add(createFieldDisplay("Extrapolated Schedule Delay", "NA", extrapolatedStatus));//column 6
        }
        gridContainer.add(createFieldDisplay("&Sigma; Remaining Effort Estimate", DateUtil.createDurationString(sprint.getRemaining(), false, true, false)));//column 7


        // forth row
        gridContainer.add(createFieldDisplay("4.1", "a"));//column 1
        gridContainer.add(createFieldDisplay("4.2", "b"));//column 2
        gridContainer.add(createFieldDisplay("4.3", "a"));//column 3
        gridContainer.add(createFieldDisplay("4.4", "b"));//column 4
        gridContainer.add(createFieldDisplay("4.6", "a"));//column 6
        gridContainer.add(createFieldDisplay("4.7", "b"));//column 7

        // Create the spanning column (column 5)
        Div spanningColumn = new Div();
        spanningColumn.getStyle()
                .set("grid-column", "5 / 6")  // Column 5
                .set("grid-row", "1 / 5")     // Span all 4 rows
                .set("padding", "var(--lumo-space-m)")
                .set("background-color", "var(--lumo-base-color)");

        try {
            long  time          = System.currentTimeMillis();
            Image burndownChart = generateBurnDownImage();
            burndownChart.getStyle().set("object-fit", "contain") // Maintain aspect ratio
                    .set("margin-top", "var(--lumo-space-m)");
            spanningColumn.add(burndownChart);
            logger.info("Burndown chart generated in {} ms", System.currentTimeMillis() - time);
        } catch (Exception e) {
            spanningColumn.add(new Paragraph("Error loading burndown chart: " + e.getMessage()));
        }
        gridContainer.add(spanningColumn);

        add(gridContainer);

    }

    private ComponentRenderer<Div, Sprint> createTwoPartRenderer(
            Function<Sprint, String> valueProvider,
            Function<Sprint, String> nameProvider) {

        return new ComponentRenderer<>(item -> {
            Div container = new Div();
            container.getStyle()
                    .set("display", "flex")
                    .set("flex-direction", "column");

            // Value part (top)
            Span value = new Span(valueProvider.apply(item));
            value.getStyle()
                    .set("font-weight", "normal");

            // Name part (bottom, smaller)
            Span name = new Span(nameProvider.apply(item));
            name.getStyle()
                    .set("font-size", "var(--lumo-font-size-xs)")
                    .set("color", "var(--lumo-secondary-text-color)");

            container.add(value, name);
            return container;
        });
    }

    private Image generateBurnDownImage() {
        StreamResource resource = new StreamResource("burndown.svg", () -> {
            try (ByteArrayOutputStream outputStream = renderBurnDownChart()) {
                // Convert ByteArrayOutputStream to ByteArrayInputStream
                return new ByteArrayInputStream(outputStream.toByteArray());
            } catch (Exception e) {
                logger.error("Error creating burndown chart", e);
                return new ByteArrayInputStream(new byte[0]); // Empty stream in case of error
            }
        });
        // Create an image component with the SVG resource
        Image burndownChart = new Image(resource, "Sprint Burndown Chart");
        burndownChart.setId(BURNDOWN_CHART);
        burndownChart.setWidthFull();
        return burndownChart;
    }

    private Image generateGanttChartImage() throws Exception {
        List<Throwable> exceptions = new ArrayList<>();
        GanttChart      chart      = new GanttChart(context, "", "/", "Gantt Chart", sprint.getName() + "-gant-chart", exceptions, ParameterOptions.getLocalNow(), false, sprint/*, 1887, 1000*/, "scheduleWithMargin", context.parameters.graphicsTheme);
        Image           ganttChart = renderGanttImage(chart);
        ganttChart.setId(GANTT_CHART);
        ganttChart.setWidth(chart.getChartWidth() + "px");
        return ganttChart;
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

    @Deprecated
    private void loadDataOld() {
        sprint = sprintApi.getById(sprintId);
        sprint.initialize();
        sprint.initUserMap(userApi.getAll(sprintId));
        sprint.initTaskMap(taskApi.getAll(sprintId), worklogApi.getAll(sprintId));
        sprint.recalculate(ParameterOptions.getLocalNow());
    }

    private void logTime() {
        logger.info("generated page in {}", DateUtil.create24hDurationString(Duration.between(created, LocalDateTime.now()), true, true, true, false));
    }

    ByteArrayOutputStream renderBurnDownChart() {
        RenderDao             dao = createRenderDao(context, sprint, "burn-down", ParameterOptions.getLocalNow(), 640, 400, "sprint-" + sprint.getId() + "/sprint.html");
        ByteArrayOutputStream o   = new ByteArrayOutputStream(64 * 1024); // 64 KB
        try {
            BurnDownChart chart = new BurnDownChart("/", dao);
            chart.render(Util.generateCopyrightString(ParameterOptions.getLocalNow()), o);
            return o;
        } catch (Exception e) {
            try {
                o.close(); // Close the stream in case of error
            } catch (Exception closeException) {
                logger.warn("Failed to close output stream", closeException);
            }
            throw new RuntimeException(e);
        }
    }

    ByteArrayOutputStream renderGanttChart(GanttChart chart) {
        ByteArrayOutputStream o = new ByteArrayOutputStream(64 * 1024); //begin size 64 KB
        try {
            chart.render(Util.generateCopyrightString(ParameterOptions.getLocalNow()), o);
            return o;
        } catch (Exception e) {
            try {
                o.close(); // Close the stream in case of error
            } catch (Exception closeException) {
                logger.warn("Failed to close output stream", closeException);
            }
            throw new RuntimeException(e);
        }
    }

    private Image renderGanttImage(GanttChart chart) {
        StreamResource resource = new StreamResource("gantt.svg", () -> {
            try (ByteArrayOutputStream outputStream = renderGanttChart(chart)) {
                // Convert ByteArrayOutputStream to ByteArrayInputStream
                return new ByteArrayInputStream(outputStream.toByteArray());
            } catch (Exception e) {
                logger.error("Error creating gantt chart", e);
                return new ByteArrayInputStream(new byte[0]); // Empty stream in case of error
            }
        });
        // Create an image component with the SVG resource
        Image ganttChart = new Image(resource, "Sprint Gantt Chart");
        return ganttChart;
    }

}
