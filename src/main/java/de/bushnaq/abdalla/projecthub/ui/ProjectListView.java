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
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.contextmenu.ContextMenu;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Main;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import com.vaadin.flow.router.*;
import com.vaadin.flow.theme.lumo.LumoUtility;
import de.bushnaq.abdalla.projecthub.api.ProjectApi;
import de.bushnaq.abdalla.projecthub.dto.Project;
import de.bushnaq.abdalla.projecthub.ui.common.ConfirmDialog;
import de.bushnaq.abdalla.projecthub.ui.common.ProjectDialog;
import de.bushnaq.abdalla.projecthub.ui.view.MainLayout;
import jakarta.annotation.security.PermitAll;

import java.time.Clock;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.HashMap;
import java.util.Map;

@Route("project-list")
@PageTitle("Project List Page")
//@Menu(order = 1, icon = "vaadin:factory", title = "project List")
@PermitAll // When security is enabled, allow all authenticated users
public class ProjectListView extends Main implements AfterNavigationObserver {
    public static final String        CREATE_PROJECT_BUTTON_ID          = "create-project-button";
    public static final String        DELETE_PROJECT_BUTTON_ID          = "delete-project-button";
    public static final String        EDIT_PROJECT_BUTTON_ID            = "edit-project-button";
    public static final String        PROJECT_GRID_ACTION_BUTTON_PREFIX = "project-grid-action-button-prefix-";
    public static final String        PROJECT_GRID_DELETE_BUTTON_PREFIX = "project-grid-delete-button-prefix-";
    public static final String        PROJECT_GRID_EDIT_BUTTON_PREFIX   = "project-grid-edit-button-prefix-";
    public static final String        PROJECT_GRID_NAME_PREFIX          = "project-grid-name-";
    private final       Grid<Project> grid;
    private final       H2            pageTitle;
    private             Long          productId;
    private final       ProjectApi    projectApi;
    private             Long          versionId;

    public ProjectListView(ProjectApi projectApi, Clock clock) {
        this.projectApi = projectApi;

        // Create header layout with title and create button
        HorizontalLayout headerLayout = new HorizontalLayout();
        headerLayout.setWidthFull();
        headerLayout.setPadding(false);
        headerLayout.setAlignItems(FlexComponent.Alignment.CENTER);
        headerLayout.setJustifyContentMode(FlexComponent.JustifyContentMode.BETWEEN);

        pageTitle = new H2("Projects");
        pageTitle.addClassNames(
                LumoUtility.Margin.Top.MEDIUM,
                LumoUtility.Margin.Bottom.SMALL
        );

        // Create button for adding new projects
        Button createButton = new Button("Create", new Icon(VaadinIcon.PLUS));
        createButton.setId(CREATE_PROJECT_BUTTON_ID);
        createButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        createButton.addClickListener(e -> openProjectDialog(null));

        headerLayout.add(pageTitle, createButton);

        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofLocalizedDateTime(FormatStyle.LONG).withZone(clock.getZone()).withLocale(getLocale());

        grid = new Grid<>();
        grid.addColumn(Project::getKey).setHeader("Key");
        grid.addColumn(new ComponentRenderer<>(project -> {
            Div div    = new Div();
            Div square = new Div();
            square.setMinHeight("16px");
            square.setMaxHeight("16px");
            square.setMinWidth("16px");
            square.setMaxWidth("16px");
//            square.getStyle().set("background-color", "#" + ColorUtil.colorToHtmlColor(project.getColor()));
            square.getStyle().set("float", "left");
            square.getStyle().set("margin", "1px");
            div.add(square);
            div.add(project.getName());
            div.setId(PROJECT_GRID_NAME_PREFIX + project.getName());
            return div;
        })).setHeader("Name");
        grid.addColumn(version -> dateTimeFormatter.format(version.getCreated())).setHeader("Created");
        grid.addColumn(version -> dateTimeFormatter.format(version.getUpdated())).setHeader("Updated");

        // Add actions column with context menu
        grid.addColumn(new ComponentRenderer<>(project -> {
            Button actionButton = new Button(new Icon(VaadinIcon.ELLIPSIS_DOTS_V));
            actionButton.setId(PROJECT_GRID_ACTION_BUTTON_PREFIX + project.getName());
            actionButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY_INLINE);
            actionButton.getElement().setAttribute("aria-label", "More options");

            // Center the button with CSS
            actionButton.getStyle().set("margin", "auto");
            actionButton.getStyle().set("display", "block");

            ContextMenu contextMenu = new ContextMenu();
            contextMenu.setOpenOnClick(true);
            contextMenu.setTarget(actionButton);

            contextMenu.addItem("Edit...", e -> openProjectDialog(project)).setId(PROJECT_GRID_EDIT_BUTTON_PREFIX + project.getName());
            contextMenu.addItem("Delete...", e -> confirmDelete(project)).setId(PROJECT_GRID_DELETE_BUTTON_PREFIX + project.getName());

            return actionButton;
        })).setWidth("70px").setFlexGrow(0);

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
                    SprintListView.class,
                    QueryParameters.simple(params)
            );
        });

        setSizeFull();
        addClassNames(LumoUtility.BoxSizing.BORDER, LumoUtility.Display.FLEX, LumoUtility.FlexDirection.COLUMN);

        add(headerLayout, grid);
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
            pageTitle.setText("Projects of Version " + versionId);
        }
        //- update breadcrumbs
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
                    }
                });

        //- populate grid
        refreshGrid();
    }

    private void confirmDelete(Project project) {
        String message = "Are you sure you want to delete project \"" + project.getName() + "\"?";
        ConfirmDialog dialog = new ConfirmDialog(
                "Confirm Delete",
                message,
                "Delete",
                () -> {
                    projectApi.deleteById(project.getId());
                    refreshGrid();
                    Notification.show("Project deleted", 3000, Notification.Position.BOTTOM_START);
                }
        );
        dialog.open();
    }

    private void openProjectDialog(Project project) {
        new ProjectDialog(project, this::saveProject).open();
    }

    private void refreshGrid() {
        if (versionId != null) {
            grid.setItems(projectApi.getAll(versionId));
        } else {
            grid.setItems(projectApi.getAll());
        }
    }

    private void saveProject(Project project) {
        try {
            if (project.getId() == null) {
                project.setVersionId(versionId);
                projectApi.persist(project);
                Notification.show("Project created", 3000, Notification.Position.BOTTOM_START);
            } else {
                projectApi.update(project);
                Notification.show("Project updated", 3000, Notification.Position.BOTTOM_START);
            }
            refreshGrid();
        } catch (Exception e) {
            Notification.show("Error saving project: " + e.getMessage(),
                    3000, Notification.Position.MIDDLE);
        }
    }
}

