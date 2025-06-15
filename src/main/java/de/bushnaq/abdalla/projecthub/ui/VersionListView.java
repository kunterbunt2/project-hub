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
import de.bushnaq.abdalla.projecthub.dto.Version;
import de.bushnaq.abdalla.projecthub.rest.api.VersionApi;
import de.bushnaq.abdalla.projecthub.ui.common.ConfirmDialog;
import de.bushnaq.abdalla.projecthub.ui.common.VersionDialog;
import de.bushnaq.abdalla.projecthub.ui.view.MainLayout;
import jakarta.annotation.security.PermitAll;

import java.time.Clock;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.HashMap;
import java.util.Map;

@Route("version-list")
@PageTitle("Version List Page")
//@Menu(order = 1, icon = "vaadin:factory", title = "version List")
@PermitAll // When security is enabled, allow all authenticated users
public class VersionListView extends Main implements AfterNavigationObserver {
    public static final String        CREATE_VERSION_BUTTON             = "create-version-button";
    public static final String        ROUTE                             = "version-list";
    public static final String        VERSION_GRID_ACTION_BUTTON_PREFIX = "version-grid-action-button-prefix-";
    public static final String        VERSION_GRID_DELETE_BUTTON_PREFIX = "version-grid-delete-button-prefix-";
    public static final String        VERSION_GRID_EDIT_BUTTON_PREFIX   = "version-grid-edit-button-prefix-";
    public static final String        VERSION_GRID_NAME_PREFIX          = "version-grid-name-";
    public static final String        VERSION_LIST_PAGE_TITLE           = "version-list-page-title";
    private final       Clock         clock;
    private final       Grid<Version> grid;
    private final       H2            pageTitle;
    private             Long          productId;
    private final       VersionApi    versionApi;

    public VersionListView(VersionApi versionApi, Clock clock) {
        this.versionApi = versionApi;
        this.clock      = clock;

        // Create header layout with title and create button
        HorizontalLayout headerLayout = new HorizontalLayout();
        headerLayout.setWidthFull();
        headerLayout.setPadding(false);
        headerLayout.setAlignItems(FlexComponent.Alignment.CENTER);
        headerLayout.setJustifyContentMode(FlexComponent.JustifyContentMode.BETWEEN);

        pageTitle = new H2("Versions");
        pageTitle.setId(VERSION_LIST_PAGE_TITLE);
        pageTitle.addClassNames(
                LumoUtility.Margin.Top.MEDIUM,
                LumoUtility.Margin.Bottom.SMALL
        );

        Button createButton = new Button("Create", new Icon(VaadinIcon.PLUS));
        createButton.setId(CREATE_VERSION_BUTTON);
        createButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        createButton.addClickListener(e -> openVersionDialog(null));

        headerLayout.add(pageTitle, createButton);

        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofLocalizedDateTime(FormatStyle.LONG).withZone(clock.getZone()).withLocale(getLocale());

        grid = new Grid<>();
        grid.addColumn(Version::getKey).setHeader("Key");
        grid.addColumn(new ComponentRenderer<>(version -> {
            Div div    = new Div();
            Div square = new Div();
            square.setMinHeight("16px");
            square.setMaxHeight("16px");
            square.setMinWidth("16px");
            square.setMaxWidth("16px");
            square.getStyle().set("float", "left");
            square.getStyle().set("margin", "1px");
            div.add(square);
            div.add(version.getName());
            div.setId(VERSION_GRID_NAME_PREFIX + version.getName());
            return div;
        })).setHeader("Name");
        grid.addColumn(version -> dateTimeFormatter.format(version.getCreated())).setHeader("Created");
        grid.addColumn(version -> dateTimeFormatter.format(version.getUpdated())).setHeader("Updated");

        // Add actions column with context menu
        grid.addColumn(new ComponentRenderer<>(version -> {
            Button actionButton = new Button(new Icon(VaadinIcon.ELLIPSIS_DOTS_V));
            actionButton.setId(VERSION_GRID_ACTION_BUTTON_PREFIX + version.getName());
            actionButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY_INLINE);
            actionButton.getElement().setAttribute("aria-label", "More options");

            // Center the button with CSS
            actionButton.getStyle().set("margin", "auto");
            actionButton.getStyle().set("display", "block");

            ContextMenu contextMenu = new ContextMenu();
            contextMenu.setOpenOnClick(true);
            contextMenu.setTarget(actionButton);

            contextMenu.addItem("Edit...", e -> openVersionDialog(version)).setId(VERSION_GRID_EDIT_BUTTON_PREFIX + version.getName());
            contextMenu.addItem("Delete...", e -> confirmDelete(version)).setId(VERSION_GRID_DELETE_BUTTON_PREFIX + version.getName());

            return actionButton;
        })).setWidth("70px").setFlexGrow(0);

        grid.setSizeFull();

        //- Add click listener to navigate to ProjectView with the selected version ID
        grid.addItemClickListener(event -> {
            Version selectedVersion = event.getItem();
            //- Create parameters map
            Map<String, String> params = new HashMap<>();
            params.put("product", String.valueOf(productId));
            params.put("version", String.valueOf(selectedVersion.getId()));
            //- Navigate with query parameters
            UI.getCurrent().navigate(
                    FeatureListView.class,
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
            pageTitle.setText("Versions of Product ID: " + productId);
        }
        //- update breadcrumbs
        getElement().getParent().getComponent()
                .ifPresent(component -> {
                    if (component instanceof MainLayout mainLayout) {
                        mainLayout.getBreadcrumbs().clear();
                        mainLayout.getBreadcrumbs().addItem("Products", ProductListView.class);
                        Map<String, String> params = new HashMap<>();
                        params.put("product", String.valueOf(productId));
                        mainLayout.getBreadcrumbs().addItem("Versions", VersionListView.class, params);
                    }
                });

        refreshGrid();
    }

    private void confirmDelete(Version version) {
        String message = "Are you sure you want to delete version \"" + version.getName() + "\"?";
        ConfirmDialog dialog = new ConfirmDialog(
                "Confirm Delete",
                message,
                "Delete",
                () -> {
                    versionApi.deleteById(version.getId());
                    refreshGrid();
                    Notification.show("Version deleted", 3000, Notification.Position.BOTTOM_START);
                }
        );
        dialog.open();
    }

    private void openVersionDialog(Version version) {
        VersionDialog dialog = new VersionDialog(version, savedVersion -> {
            if (version != null) {
                // Edit mode
                versionApi.update(savedVersion);
                Notification.show("Version updated", 3000, Notification.Position.BOTTOM_START);
            } else {
                // Create mode
                savedVersion.setProductId(productId);
                versionApi.persist(savedVersion);
                Notification.show("Version created", 3000, Notification.Position.BOTTOM_START);
            }
            refreshGrid();
        });
        dialog.open();
    }

    private void refreshGrid() {
        // Only show versions for the selected product
        if (productId != null) {
            grid.setItems(versionApi.getAll(productId));
        } else {
            grid.setItems(versionApi.getAll());
        }
    }
}
