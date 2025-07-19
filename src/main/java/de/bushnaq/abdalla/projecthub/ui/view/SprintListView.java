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

import com.vaadin.flow.component.Text;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Main;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import com.vaadin.flow.router.*;
import com.vaadin.flow.theme.lumo.LumoUtility;
import de.bushnaq.abdalla.projecthub.dto.Feature;
import de.bushnaq.abdalla.projecthub.dto.Product;
import de.bushnaq.abdalla.projecthub.dto.Sprint;
import de.bushnaq.abdalla.projecthub.dto.Version;
import de.bushnaq.abdalla.projecthub.rest.api.FeatureApi;
import de.bushnaq.abdalla.projecthub.rest.api.ProductApi;
import de.bushnaq.abdalla.projecthub.rest.api.SprintApi;
import de.bushnaq.abdalla.projecthub.rest.api.VersionApi;
import de.bushnaq.abdalla.projecthub.ui.MainLayout;
import de.bushnaq.abdalla.projecthub.ui.dialog.ConfirmDialog;
import de.bushnaq.abdalla.projecthub.ui.dialog.SprintDialog;
import de.bushnaq.abdalla.projecthub.ui.util.VaadinUtil;
import de.bushnaq.abdalla.util.date.DateUtil;
import jakarta.annotation.security.PermitAll;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

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
    public static final String       CREATE_SPRINT_BUTTON             = "create-sprint-button";
    public static final String       SPRINT_GRID                      = "sprint-grid";
    public static final String       SPRINT_GRID_CONFIG_BUTTON_PREFIX = "sprint-grid-config-button-prefix-";
    public static final String       SPRINT_GRID_DELETE_BUTTON_PREFIX = "sprint-grid-delete-button-prefix-";
    public static final String       SPRINT_GRID_EDIT_BUTTON_PREFIX   = "sprint-grid-edit-button-prefix-";
    public static final String       SPRINT_GRID_NAME_PREFIX          = "sprint-grid-name-";
    public static final String       SPRINT_LIST_PAGE_TITLE           = "sprint-list-page-title";
    private final       Clock        clock;
    private final       FeatureApi   featureApi;
    private             Long         featureId;
    private             Grid<Sprint> grid;
    private final       ProductApi   productApi;
    //    private             H2           pageTitle;
    private             Long         productId;
    private final       SprintApi    sprintApi;
    private final       VersionApi   versionApi;
    private             Long         versionId;

    public SprintListView(SprintApi sprintApi, ProductApi productApi, VersionApi versionApi, FeatureApi featureApi, Clock clock) {
        this.sprintApi  = sprintApi;
        this.productApi = productApi;
        this.versionApi = versionApi;
        this.featureApi = featureApi;
        this.clock      = clock;

        setSizeFull();
        addClassNames(LumoUtility.BoxSizing.BORDER, LumoUtility.Display.FLEX, LumoUtility.FlexDirection.COLUMN);

        add(VaadinUtil.createHeader("Sprints", SPRINT_LIST_PAGE_TITLE, "vaadin:exit", CREATE_SPRINT_BUTTON, () -> openSprintDialog(null)), createGrid(clock));
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
//            pageTitle.setText("Sprints of Feature ID: " + featureId);
        }
        //- update breadcrumbs
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
                            mainLayout.getBreadcrumbs().addItem("Sprints", SprintListView.class, params);
                        }
                    }
                });

        refreshGrid();
    }

    private void confirmDelete(Sprint sprint) {
        String message = "Are you sure you want to delete sprint \"" + sprint.getName() + "\"?";
        ConfirmDialog dialog = new ConfirmDialog(
                "Confirm Delete",
                message,
                "Delete",
                () -> {
                    sprintApi.deleteById(sprint.getId());
                    refreshGrid();
                    Notification.show("Sprint deleted", 3000, Notification.Position.BOTTOM_START);
                }
        );
        dialog.open();
    }

    private Grid<Sprint> createGrid(Clock clock) {
        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofLocalizedDateTime(FormatStyle.LONG).withZone(clock.getZone()).withLocale(getLocale());

        grid = new Grid<>();
        grid.setId(SPRINT_GRID);

        Grid.Column<Sprint> keyColumn = grid.addColumn(Sprint::getKey).setHeader("Key");
        keyColumn.setId("sprint-grid-key-column");
        keyColumn.setHeader(new HorizontalLayout(new Icon(VaadinIcon.KEY), new Div(new Text("Key"))));

        Grid.Column<Sprint> nameColumn = grid.addColumn(new ComponentRenderer<>(sprint -> {
            Div div    = new Div();
            Div square = new Div();
            square.setMinHeight("16px");
            square.setMaxHeight("16px");
            square.setMinWidth("16px");
            square.setMaxWidth("16px");
            square.getStyle().set("float", "left");
            square.getStyle().set("margin", "1px");
            div.add(square);
            div.add(sprint.getName());
            div.setId(SPRINT_GRID_NAME_PREFIX + sprint.getName());
            return div;
        })).setHeader("Name");
        nameColumn.setId("sprint-grid-name-column");
        nameColumn.setHeader(new HorizontalLayout(new Icon("vaadin:exit"), new Div(new Text("Name"))));

        Grid.Column<Sprint> startColumn = grid.addColumn(sprint -> sprint.getStart() != null ? dateTimeFormatter.format(sprint.getStart()) : "").setHeader("Start");
        startColumn.setId("sprint-grid-start-column");
        startColumn.setHeader(new HorizontalLayout(new Icon(VaadinIcon.CALENDAR), new Div(new Text("Start"))));

        Grid.Column<Sprint> endColumn = grid.addColumn(sprint -> sprint.getEnd() != null ? dateTimeFormatter.format(sprint.getEnd()) : "").setHeader("End");
        endColumn.setId("sprint-grid-end-column");
        endColumn.setHeader(new HorizontalLayout(new Icon(VaadinIcon.CALENDAR), new Div(new Text("End"))));

        Grid.Column<Sprint> statusColumn = grid.addColumn(sprint -> sprint.getStatus().name()).setHeader("Status");
        statusColumn.setId("sprint-grid-status-column");
        statusColumn.setHeader(new HorizontalLayout(new Icon(VaadinIcon.FLAG), new Div(new Text("Status"))));

        Grid.Column<Sprint> originalEstimationColumn = grid.addColumn(sprint -> sprint.getOriginalEstimation() != null ? DateUtil.createDurationString(sprint.getOriginalEstimation(), false, true, true) : "").setHeader("Original Estimation");
        originalEstimationColumn.setId("sprint-grid-original-estimation-column");
        originalEstimationColumn.setHeader(new HorizontalLayout(new Icon(VaadinIcon.CLOCK), new Div(new Text("Original Estimation"))));

        Grid.Column<Sprint> workedColumn = grid.addColumn(sprint -> sprint.getWorked() != null ? DateUtil.createDurationString(sprint.getWorked(), false, true, true) : "").setHeader("Worked");
        workedColumn.setId("sprint-grid-worked-column");
        workedColumn.setHeader(new HorizontalLayout(new Icon(VaadinIcon.TIMER), new Div(new Text("Worked"))));

        Grid.Column<Sprint> remainingColumn = grid.addColumn(sprint -> sprint.getRemaining() != null ? DateUtil.createDurationString(sprint.getRemaining(), false, true, true) : "").setHeader("Remaining");
        remainingColumn.setId("sprint-grid-remaining-column");
        remainingColumn.setHeader(new HorizontalLayout(new Icon(VaadinIcon.HOURGLASS), new Div(new Text("Remaining"))));

        // Add actions column with direct buttons instead of context menu
        grid.addColumn(new ComponentRenderer<>(sprint -> {
            HorizontalLayout layout = new HorizontalLayout();
            layout.setAlignItems(FlexComponent.Alignment.CENTER);
            layout.setSpacing(true);

            Button editButton = new Button(new Icon(VaadinIcon.EDIT));
            editButton.setId(SPRINT_GRID_EDIT_BUTTON_PREFIX + sprint.getName());
            editButton.addThemeVariants(ButtonVariant.LUMO_SMALL, ButtonVariant.LUMO_TERTIARY);
            editButton.addClickListener(e -> openSprintDialog(sprint));
            editButton.getElement().setAttribute("title", "Edit");

            Button configButton = new Button(new Icon(VaadinIcon.COG));
            configButton.setId(SPRINT_GRID_CONFIG_BUTTON_PREFIX + sprint.getName());
            configButton.addThemeVariants(ButtonVariant.LUMO_SMALL, ButtonVariant.LUMO_TERTIARY);
            configButton.addClickListener(e -> {
                Map<String, String> params = new HashMap<>();
                params.put("product", String.valueOf(productId));
                params.put("version", String.valueOf(versionId));
                params.put("feature", String.valueOf(featureId));
                params.put("sprint", String.valueOf(sprint.getId()));
                UI.getCurrent().navigate(
                        TaskListView.class,
                        QueryParameters.simple(params)
                );
            });
            configButton.getElement().setAttribute("title", "Tasks Configuration");

            Button deleteButton = new Button(new Icon(VaadinIcon.TRASH));
            deleteButton.setId(SPRINT_GRID_DELETE_BUTTON_PREFIX + sprint.getName());
            deleteButton.addThemeVariants(ButtonVariant.LUMO_SMALL, ButtonVariant.LUMO_TERTIARY, ButtonVariant.LUMO_ERROR);
            deleteButton.addClickListener(e -> confirmDelete(sprint));
            deleteButton.getElement().setAttribute("title", "Delete");

            layout.add(editButton, configButton, deleteButton);
            return layout;
        })).setWidth("160px").setFlexGrow(0);

        grid.setSizeFull();
        //- Add click listener to navigate to TaskView with the selected version ID
        grid.addItemClickListener(event -> {
            Sprint selectedSprint = event.getItem();
            //- Create parameters map
            Map<String, String> params = new HashMap<>();
            params.put("product", String.valueOf(productId));
            params.put("version", String.valueOf(versionId));
            params.put("feature", String.valueOf(featureId));
            params.put("sprint", String.valueOf(selectedSprint.getId()));
            //- Navigate with query parameters
            UI.getCurrent().navigate(
                    SprintQualityBoard.class,
                    QueryParameters.simple(params)
            );
        });
        return grid;
    }

    private void openSprintDialog(Sprint sprint) {
        SprintDialog dialog = new SprintDialog(sprint, (savedSprint, sprintDialog) -> {
            try {
                if (sprint != null) {
                    // Edit mode
                    sprintApi.update(savedSprint);
                    Notification.show("Sprint updated", 3000, Notification.Position.BOTTOM_START);
                    sprintDialog.close();
                } else {
                    // Create mode
                    savedSprint.setFeatureId(featureId);
                    sprintApi.persist(savedSprint);
                    Notification.show("Sprint created", 3000, Notification.Position.BOTTOM_START);
                    sprintDialog.close();
                }
                refreshGrid();
            } catch (ResponseStatusException ex) {
                if (ex.getStatusCode() == HttpStatus.CONFLICT) {
                    // This is a name uniqueness violation
                    sprintDialog.setErrorMessage("A sprint with this name already exists");
                } else {
                    // Some other error
                    Notification.show("Error: " + ex.getMessage(), 3000, Notification.Position.MIDDLE);
                    sprintDialog.close();
                }
            } catch (Exception ex) {
                Notification.show("Unexpected error: " + ex.getMessage(), 3000, Notification.Position.MIDDLE);
                sprintDialog.close();
            }
        });
        dialog.open();
    }

    private void refreshGrid() {
        // Only show sprints for the selected project
        if (featureId != null) {
            grid.setItems(sprintApi.getAll(featureId));
        } else {
            grid.setItems(sprintApi.getAll());
        }
    }
}
