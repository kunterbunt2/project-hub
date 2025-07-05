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
import de.bushnaq.abdalla.projecthub.dto.Product;
import de.bushnaq.abdalla.projecthub.dto.Version;
import de.bushnaq.abdalla.projecthub.rest.api.ProductApi;
import de.bushnaq.abdalla.projecthub.rest.api.VersionApi;
import de.bushnaq.abdalla.projecthub.ui.MainLayout;
import de.bushnaq.abdalla.projecthub.ui.dialog.ConfirmDialog;
import de.bushnaq.abdalla.projecthub.ui.dialog.VersionDialog;
import de.bushnaq.abdalla.projecthub.ui.util.VaadinUtils;
import jakarta.annotation.security.PermitAll;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

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
    public static final String        VERSION_GRID                      = "version-grid";
    public static final String        VERSION_GRID_DELETE_BUTTON_PREFIX = "version-grid-delete-button-prefix-";
    public static final String        VERSION_GRID_EDIT_BUTTON_PREFIX   = "version-grid-edit-button-prefix-";
    public static final String        VERSION_GRID_NAME_PREFIX          = "version-grid-name-";
    public static final String        VERSION_LIST_PAGE_TITLE           = "version-list-page-title";
    private final       Clock         clock;
    private             Grid<Version> grid;
    private final       ProductApi    productApi;
    //    private             H2            pageTitle;
    private             Long          productId;
    private final       VersionApi    versionApi;

    public VersionListView(VersionApi versionApi, ProductApi productApi, Clock clock) {
        this.versionApi = versionApi;
        this.productApi = productApi;
        this.clock      = clock;

        setSizeFull();
        addClassNames(LumoUtility.BoxSizing.BORDER, LumoUtility.Display.FLEX, LumoUtility.FlexDirection.COLUMN);

        add(VaadinUtils.createHeader("Versions", VERSION_LIST_PAGE_TITLE, VaadinIcon.TAG, CREATE_VERSION_BUTTON, () -> openVersionDialog(null)), createGrid(clock));
    }

    @Override
    public void afterNavigation(AfterNavigationEvent event) {
        //- Get query parameters
        Location        location        = event.getLocation();
        QueryParameters queryParameters = location.getQueryParameters();
        if (queryParameters.getParameters().containsKey("product")) {
            this.productId = Long.parseLong(queryParameters.getParameters().get("product").getFirst());
        }
        //- update breadcrumbs
        getElement().getParent().getComponent()
                .ifPresent(component -> {
                    if (component instanceof MainLayout mainLayout) {
                        mainLayout.getBreadcrumbs().clear();
                        Product product = productApi.getById(productId);
                        mainLayout.getBreadcrumbs().addItem("Products (" + product.getName() + ")", ProductListView.class);
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

    private Grid<Version> createGrid(Clock clock) {
//        final Grid<Version> grid;
        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofLocalizedDateTime(FormatStyle.LONG).withZone(clock.getZone()).withLocale(getLocale());

        grid = new Grid<>();
        grid.setId(VERSION_GRID);

        Grid.Column<Version> keyColumn = grid.addColumn(Version::getKey).setHeader("Key");
        keyColumn.setId("version-grid-key-column");
        keyColumn.setHeader(new HorizontalLayout(new Icon(VaadinIcon.KEY), new Div(new Text("Key"))));

        Grid.Column<Version> nameColumn = grid.addColumn(new ComponentRenderer<>(version -> {
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
        nameColumn.setId("version-grid-name-column");
        nameColumn.setHeader(new HorizontalLayout(new Icon(VaadinIcon.TAG), new Div(new Text("Name"))));

        Grid.Column<Version> createdColumn = grid.addColumn(version -> dateTimeFormatter.format(version.getCreated())).setHeader("Created");
        createdColumn.setId("version-grid-created-column");
        createdColumn.setHeader(new HorizontalLayout(new Icon(VaadinIcon.CALENDAR), new Div(new Text("Created"))));

        Grid.Column<Version> updatedColumn = grid.addColumn(version -> dateTimeFormatter.format(version.getUpdated())).setHeader("Updated");
        updatedColumn.setId("version-grid-updated-column");
        updatedColumn.setHeader(new HorizontalLayout(new Icon(VaadinIcon.CALENDAR), new Div(new Text("Updated"))));

        // Add actions column with direct buttons instead of context menu
        grid.addColumn(new ComponentRenderer<>(version -> {
            HorizontalLayout layout = new HorizontalLayout();
            layout.setAlignItems(FlexComponent.Alignment.CENTER);
            layout.setSpacing(true);

            Button editButton = new Button(new Icon(VaadinIcon.EDIT));
            editButton.setId(VERSION_GRID_EDIT_BUTTON_PREFIX + version.getName());
            editButton.addThemeVariants(ButtonVariant.LUMO_SMALL, ButtonVariant.LUMO_TERTIARY);
            editButton.addClickListener(e -> openVersionDialog(version));
            editButton.getElement().setAttribute("title", "Edit");

            Button deleteButton = new Button(new Icon(VaadinIcon.TRASH));
            deleteButton.setId(VERSION_GRID_DELETE_BUTTON_PREFIX + version.getName());
            deleteButton.addThemeVariants(ButtonVariant.LUMO_SMALL, ButtonVariant.LUMO_TERTIARY, ButtonVariant.LUMO_ERROR);
            deleteButton.addClickListener(e -> confirmDelete(version));
            deleteButton.getElement().setAttribute("title", "Delete");

            layout.add(editButton, deleteButton);
            return layout;
        })).setHeader("Actions").setFlexGrow(0).setWidth("120px");

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
        return grid;
    }

    private void openVersionDialog(Version version) {
        VersionDialog dialog = new VersionDialog(version, (savedVersion, versionDialog) -> {
            try {
                if (version != null) {
                    // Edit mode
                    versionApi.update(savedVersion);
                    Notification.show("Version updated", 3000, Notification.Position.BOTTOM_START);
                    versionDialog.close();
                } else {
                    // Create mode
                    savedVersion.setProductId(productId);
                    versionApi.persist(savedVersion);
                    Notification.show("Version created", 3000, Notification.Position.BOTTOM_START);
                    versionDialog.close();
                }
                refreshGrid();
            } catch (ResponseStatusException e) {
                if (e.getStatusCode() == HttpStatus.CONFLICT) {
                    // This is a name uniqueness violation
                    versionDialog.setNameFieldError(e.getReason());
                } else {
                    // Some other error
                    Notification.show("Error: " + e.getMessage(), 3000, Notification.Position.MIDDLE);
                    versionDialog.close();
                }
            } catch (Exception ex) {
                Notification.show("Unexpected error: " + ex.getMessage(), 3000, Notification.Position.MIDDLE);
                versionDialog.close();
            }
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
