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
import de.bushnaq.abdalla.projecthub.dto.Version;
import de.bushnaq.abdalla.projecthub.rest.api.FeatureApi;
import de.bushnaq.abdalla.projecthub.rest.api.ProductApi;
import de.bushnaq.abdalla.projecthub.rest.api.VersionApi;
import de.bushnaq.abdalla.projecthub.ui.MainLayout;
import de.bushnaq.abdalla.projecthub.ui.dialog.ConfirmDialog;
import de.bushnaq.abdalla.projecthub.ui.dialog.FeatureDialog;
import de.bushnaq.abdalla.projecthub.ui.util.VaadinUtil;
import jakarta.annotation.security.PermitAll;
import jakarta.annotation.security.RolesAllowed;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.time.Clock;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.HashMap;
import java.util.Map;

@Route("feature-list")
@PageTitle("Feature List Page")
@PermitAll // When security is enabled, allow all authenticated users
@RolesAllowed({"USER", "ADMIN"}) // Restrict access to users with specific roles
public class FeatureListView extends Main implements AfterNavigationObserver {
    public static final String        CREATE_FEATURE_BUTTON_ID          = "create-feature-button";
    public static final String        FEATURE_GRID                      = "feature-grid";
    public static final String        FEATURE_GRID_DELETE_BUTTON_PREFIX = "feature-grid-delete-button-prefix-";
    public static final String        FEATURE_GRID_EDIT_BUTTON_PREFIX   = "feature-grid-edit-button-prefix-";
    public static final String        FEATURE_GRID_NAME_PREFIX          = "feature-grid-name-";
    public static final String        FEATURE_LIST_PAGE_TITLE           = "feature-list-page-title";
    private final       FeatureApi    featureApi;
    private final       Grid<Feature> grid;
    private final       ProductApi    productApi;
    //    private             H2            pageTitle;
    private             Long          productId;
    private final       VersionApi    versionApi;
    private             Long          versionId;

    public FeatureListView(FeatureApi featureApi, ProductApi productApi, VersionApi versionApi, Clock clock) {
        this.featureApi = featureApi;
        this.productApi = productApi;
        this.versionApi = versionApi;

        grid = createGrid(clock);

        setSizeFull();
        addClassNames(LumoUtility.BoxSizing.BORDER, LumoUtility.Display.FLEX, LumoUtility.FlexDirection.COLUMN);

        add(VaadinUtil.createHeader("Features", FEATURE_LIST_PAGE_TITLE, VaadinIcon.LIGHTBULB, CREATE_FEATURE_BUTTON_ID, () -> openFeatureDialog(null)), grid);
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
//            pageTitle.setText("Features of Version " + versionId);
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
                            mainLayout.getBreadcrumbs().addItem("Features", FeatureListView.class, params);
                        }
                    }
                });

        //- populate grid
        refreshGrid();
    }

    private void confirmDelete(Feature feature) {
        String message = "Are you sure you want to delete feature \"" + feature.getName() + "\"?";
        ConfirmDialog dialog = new ConfirmDialog(
                "Confirm Delete",
                message,
                "Delete",
                () -> {
                    featureApi.deleteById(feature.getId());
                    refreshGrid();
                    Notification.show("Feature deleted", 3000, Notification.Position.BOTTOM_START);
                }
        );
        dialog.open();
    }

    private Grid<Feature> createGrid(Clock clock) {
        final Grid<Feature> grid;
        DateTimeFormatter   dateTimeFormatter = DateTimeFormatter.ofLocalizedDateTime(FormatStyle.LONG).withZone(clock.getZone()).withLocale(getLocale());

        grid = new Grid<>();
        grid.setId(FEATURE_GRID);

        Grid.Column<Feature> keyColumn = grid.addColumn(Feature::getKey).setHeader("Key");
        keyColumn.setId("feature-grid-key-column");
        keyColumn.setHeader(new HorizontalLayout(new Icon(VaadinIcon.KEY), new Div(new Text("Key"))));

        Grid.Column<Feature> nameColumn = grid.addColumn(new ComponentRenderer<>(feature -> {
            Div div = new Div();
//            Div square = new Div();
//            square.setMinHeight("16px");
//            square.setMaxHeight("16px");
//            square.setMinWidth("16px");
//            square.setMaxWidth("16px");
//            square.getStyle().set("background-color", "#" + ColorUtil.colorToHtmlColor(feature.getColor()));
//            square.getStyle().set("float", "left");
//            square.getStyle().set("margin", "1px");
//            div.add(square);
            div.add(feature.getName());
            div.setId(FEATURE_GRID_NAME_PREFIX + feature.getName());
            return div;
        })).setHeader("Name");
        nameColumn.setId("feature-grid-name-column");
        nameColumn.setHeader(new HorizontalLayout(new Icon(VaadinIcon.LIGHTBULB), new Div(new Text("Name"))));

        Grid.Column<Feature> createdColumn = grid.addColumn(version -> dateTimeFormatter.format(version.getCreated())).setHeader("Created");
        createdColumn.setId("feature-grid-created-column");
        createdColumn.setHeader(new HorizontalLayout(new Icon(VaadinIcon.CALENDAR), new Div(new Text("Created"))));

        Grid.Column<Feature> updatedColumn = grid.addColumn(version -> dateTimeFormatter.format(version.getUpdated())).setHeader("Updated");
        updatedColumn.setId("feature-grid-updated-column");
        updatedColumn.setHeader(new HorizontalLayout(new Icon(VaadinIcon.CALENDAR), new Div(new Text("Updated"))));

        // Add actions column with direct buttons instead of context menu
        grid.addColumn(new ComponentRenderer<>(feature -> {
            HorizontalLayout layout = new HorizontalLayout();
            layout.setAlignItems(FlexComponent.Alignment.CENTER);
            layout.setSpacing(true);

            Button editButton = new Button(new Icon(VaadinIcon.EDIT));
            editButton.setId(FEATURE_GRID_EDIT_BUTTON_PREFIX + feature.getName());
            editButton.addThemeVariants(ButtonVariant.LUMO_SMALL, ButtonVariant.LUMO_TERTIARY);
            editButton.addClickListener(e -> openFeatureDialog(feature));
            editButton.getElement().setAttribute("title", "Edit");

            Button deleteButton = new Button(new Icon(VaadinIcon.TRASH));
            deleteButton.setId(FEATURE_GRID_DELETE_BUTTON_PREFIX + feature.getName());
            deleteButton.addThemeVariants(ButtonVariant.LUMO_SMALL, ButtonVariant.LUMO_TERTIARY, ButtonVariant.LUMO_ERROR);
            deleteButton.addClickListener(e -> confirmDelete(feature));
            deleteButton.getElement().setAttribute("title", "Delete");

            layout.add(editButton, deleteButton);
            return layout;
        })).setHeader("Actions").setFlexGrow(0).setWidth("120px");

        grid.setSizeFull();

        //- Add click listener to navigate to SprintView with the selected version ID
        grid.addItemClickListener(event -> {
            Feature selectedFeature = event.getItem();
            //- Create parameters map
            Map<String, String> params = new HashMap<>();
            params.put("product", String.valueOf(productId));
            params.put("version", String.valueOf(versionId));
            params.put("feature", String.valueOf(selectedFeature.getId()));
            //- Navigate with query parameters
            UI.getCurrent().navigate(
                    SprintListView.class,
                    QueryParameters.simple(params)
            );
        });
        return grid;
    }

    private void openFeatureDialog(Feature feature) {
        FeatureDialog dialog = new FeatureDialog(feature, (savedFeature, featureDialog) -> {
            try {
                if (savedFeature.getId() == null) {
                    savedFeature.setVersionId(versionId);
                    featureApi.persist(savedFeature);
                    Notification.show("Feature created", 3000, Notification.Position.BOTTOM_START);
                    featureDialog.close();
                } else {
                    featureApi.update(savedFeature);
                    Notification.show("Feature updated", 3000, Notification.Position.BOTTOM_START);
                    featureDialog.close();
                }
                refreshGrid();
            } catch (ResponseStatusException ex) {
                if (ex.getStatusCode() == HttpStatus.CONFLICT) {
                    // This is a name uniqueness violation
                    featureDialog.setErrorMessage("A feature with this name already exists");
                } else {
                    // Some other error
                    Notification.show("Error: " + ex.getMessage(), 3000, Notification.Position.MIDDLE);
                    featureDialog.close();
                }
            } catch (Exception ex) {
                Notification.show("Unexpected error: " + ex.getMessage(), 3000, Notification.Position.MIDDLE);
                featureDialog.close();
            }
        });
        dialog.open();
    }

    private void refreshGrid() {
        if (versionId != null) {
            grid.setItems(featureApi.getAll(versionId));
        } else {
            grid.setItems(featureApi.getAll());
        }
    }
}
