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

import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import com.vaadin.flow.router.*;
import de.bushnaq.abdalla.projecthub.dto.Product;
import de.bushnaq.abdalla.projecthub.dto.Version;
import de.bushnaq.abdalla.projecthub.rest.api.ProductApi;
import de.bushnaq.abdalla.projecthub.rest.api.VersionApi;
import de.bushnaq.abdalla.projecthub.ui.MainLayout;
import de.bushnaq.abdalla.projecthub.ui.component.AbstractMainGrid;
import de.bushnaq.abdalla.projecthub.ui.dialog.ConfirmDialog;
import de.bushnaq.abdalla.projecthub.ui.dialog.VersionDialog;
import de.bushnaq.abdalla.projecthub.ui.util.VaadinUtil;
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
@PermitAll // When security is enabled, allow all authenticated users
public class VersionListView extends AbstractMainGrid<Version> implements AfterNavigationObserver {
    public static final String     CREATE_VERSION_BUTTON             = "create-version-button";
    public static final String     ROUTE                             = "version-list";
    public static final String     VERSION_GRID                      = "version-grid";
    public static final String     VERSION_GRID_DELETE_BUTTON_PREFIX = "version-grid-delete-button-prefix-";
    public static final String     VERSION_GRID_EDIT_BUTTON_PREFIX   = "version-grid-edit-button-prefix-";
    public static final String     VERSION_GRID_NAME_PREFIX          = "version-grid-name-";
    public static final String     VERSION_LIST_PAGE_TITLE           = "version-list-page-title";
    public static final String     VERSION_ROW_COUNTER               = "version-row-counter";
    private final       ProductApi productApi;
    private             Long       productId;
    private final       VersionApi versionApi;

    public VersionListView(VersionApi versionApi, ProductApi productApi, Clock clock) {
        super(clock);
        this.versionApi = versionApi;
        this.productApi = productApi;

        add(
                VaadinUtil.createHeader(
                        "Versions",
                        VERSION_LIST_PAGE_TITLE,
                        VaadinIcon.TAG,
                        CREATE_VERSION_BUTTON,
                        () -> openVersionDialog(null),
                        grid,
                        VERSION_ROW_COUNTER
                ),
                grid
        );
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

    protected void initGrid(Clock clock) {
        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofLocalizedDateTime(FormatStyle.LONG).withZone(clock.getZone()).withLocale(getLocale());

        grid.setId(VERSION_GRID);

        // Add click listener to navigate to FeatureListView with the selected version ID
        grid.addItemClickListener(event -> {
            Version selectedVersion = event.getItem();
            // Create parameters map
            Map<String, String> params = new HashMap<>();
            params.put("product", String.valueOf(productId));
            params.put("version", String.valueOf(selectedVersion.getId()));
            // Navigate with query parameters
            UI.getCurrent().navigate(
                    FeatureListView.class,
                    QueryParameters.simple(params)
            );
        });

        {
            Grid.Column<Version> keyColumn = grid.addColumn(Version::getKey);
            VaadinUtil.addFilterableHeader(grid, keyColumn, "Key", VaadinIcon.KEY, Version::getKey);
        }
        {
            // Add name column with filtering and sorting
            Grid.Column<Version> nameColumn = grid.addColumn(new ComponentRenderer<>(version -> {
                Div div = new Div();
                div.add(version.getName());
                div.setId(VERSION_GRID_NAME_PREFIX + version.getName());
                return div;
            }));

            // Configure a custom comparator to properly sort by the name property
            nameColumn.setComparator((version1, version2) ->
                    version1.getName().compareToIgnoreCase(version2.getName()));

            VaadinUtil.addFilterableHeader(grid, nameColumn, "Name", VaadinIcon.TAG, Version::getName);
        }
        {
            Grid.Column<Version> createdColumn = grid.addColumn(version -> dateTimeFormatter.format(version.getCreated()));
            VaadinUtil.addFilterableHeader(grid, createdColumn, "Created", VaadinIcon.CALENDAR,
                    version -> dateTimeFormatter.format(version.getCreated()));
        }
        {
            Grid.Column<Version> updatedColumn = grid.addColumn(version -> dateTimeFormatter.format(version.getUpdated()));
            VaadinUtil.addFilterableHeader(grid, updatedColumn, "Updated", VaadinIcon.CALENDAR,
                    version -> dateTimeFormatter.format(version.getUpdated()));
        }

        // Add actions column using VaadinUtil
        VaadinUtil.addActionColumn(
                grid,
                VERSION_GRID_EDIT_BUTTON_PREFIX,
                VERSION_GRID_DELETE_BUTTON_PREFIX,
                Version::getName,
                this::openVersionDialog,
                this::confirmDelete
        );

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
        dataProvider.getItems().clear();
        dataProvider.getItems().addAll((productId != null) ? versionApi.getAll(productId) : versionApi.getAll());
        dataProvider.refreshAll();
    }
}
