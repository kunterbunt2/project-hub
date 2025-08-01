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

import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import com.vaadin.flow.router.*;
import de.bushnaq.abdalla.projecthub.dto.Availability;
import de.bushnaq.abdalla.projecthub.dto.Location;
import de.bushnaq.abdalla.projecthub.dto.User;
import de.bushnaq.abdalla.projecthub.rest.api.LocationApi;
import de.bushnaq.abdalla.projecthub.rest.api.UserApi;
import de.bushnaq.abdalla.projecthub.ui.MainLayout;
import de.bushnaq.abdalla.projecthub.ui.component.AbstractMainGrid;
import de.bushnaq.abdalla.projecthub.ui.dialog.ConfirmDialog;
import de.bushnaq.abdalla.projecthub.ui.dialog.LocationDialog;
import de.bushnaq.abdalla.projecthub.ui.util.VaadinUtil;
import de.focus_shift.jollyday.core.HolidayManager;
import de.focus_shift.jollyday.core.ManagerParameters;
import jakarta.annotation.security.PermitAll;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.server.ResponseStatusException;

import java.time.Clock;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

@Route(value = "location/:username?", layout = MainLayout.class)
@PageTitle("User Location")
@PermitAll
public class LocationListView extends AbstractMainGrid<Location> implements BeforeEnterObserver, AfterNavigationObserver {
    public static final String      CREATE_LOCATION_BUTTON             = "create-location-button";
    public static final String      LOCATION_GRID                      = "location-grid";
    public static final String      LOCATION_GRID_COUNTRY_PREFIX       = "location-country-";
    public static final String      LOCATION_GRID_DELETE_BUTTON_PREFIX = "location-delete-button-";
    public static final String      LOCATION_GRID_EDIT_BUTTON_PREFIX   = "location-edit-button-";
    public static final String      LOCATION_GRID_START_DATE_PREFIX    = "location-start-";
    public static final String      LOCATION_GRID_STATE_PREFIX         = "location-state-";
    public static final String      LOCATION_LIST_PAGE_TITLE           = "location-page-title";
    public static final String      LOCATION_ROW_COUNTER               = "location-row-counter";
    public static final String      ROUTE                              = "location";
    private             User        currentUser;
    private final       LocationApi locationApi;
    private final       UserApi     userApi;

    public LocationListView(LocationApi locationApi, UserApi userApi, Clock clock) {
        super(clock);
        this.locationApi = locationApi;
        this.userApi     = userApi;

        add(
                VaadinUtil.createHeader(
                        "User Location",
                        LOCATION_LIST_PAGE_TITLE,
                        VaadinIcon.MAP_MARKER,
                        CREATE_LOCATION_BUTTON,
                        () -> openLocationDialog(null),
                        grid,
                        LOCATION_ROW_COUNTER
                ),
                grid
        );
    }

    @Override
    public void afterNavigation(AfterNavigationEvent event) {
        refreshGrid();
    }

    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        // Get username from URL parameter or use the currently authenticated user
        String usernameParam = event.getRouteParameters().get("username").orElse(null);

        Authentication authentication  = SecurityContextHolder.getContext().getAuthentication();
        String         currentUsername = authentication != null ? authentication.getName() : null;

        // If no username is provided, use the current authenticated user
        final String username = (usernameParam == null && currentUsername != null) ? currentUsername : usernameParam;

        if (username != null) {
            try {
                // Find user by username
                currentUser = userApi.getByName(username);
            } catch (ResponseStatusException ex) {
                if (ex.getStatusCode().equals(HttpStatus.NOT_FOUND)) {
                    // Create a new user since one wasn't found
                    User newUser = createDefaultUser(username);
                    currentUser = userApi.persist(newUser);
                    Notification notification = Notification.show("Created new user: " + username, 3000, Notification.Position.MIDDLE);
                    notification.addThemeVariants(NotificationVariant.LUMO_SUCCESS);
                } else {
                    throw ex;
                }
            }
        } else {
            // Redirect to main page if no username and not authenticated
            event.forwardTo("");
        }
    }

    private void confirmDelete(Location location) {
        if (dataProvider.getItems().size() <= 1) {
            Notification notification = Notification.show("Cannot delete - Users must have at least one location", 3000, Notification.Position.MIDDLE);
            notification.addThemeVariants(NotificationVariant.LUMO_ERROR);
            return;
        }

        ConfirmDialog dialog = new ConfirmDialog("Confirm Delete",
                "Are you sure you want to delete this location record?",
                "Delete",
                () -> {
                    try {
                        locationApi.deleteById(currentUser, location);
                        refreshGrid();
                        Notification.show("Location deleted", 3000, Notification.Position.MIDDLE);
                    } catch (Exception ex) {
                        Notification notification = Notification.show(
                                "Failed to delete: " + ex.getMessage(),
                                3000,
                                Notification.Position.MIDDLE);
                        notification.addThemeVariants(NotificationVariant.LUMO_ERROR);
                    }
                });
        dialog.open();
    }

    /**
     * Creates a default user with standard availability and location
     *
     * @param username The name for the new user
     * @return A new User object with default settings
     */
    private User createDefaultUser(String username) {
        User user = new User();
        user.setName(username);
        user.setEmail(username);
        user.setFirstWorkingDay(java.time.LocalDate.now());
        user.setColor(new java.awt.Color(51, 102, 204));
        Availability availability = new Availability(1.0f, java.time.LocalDate.now());
        user.addAvailability(availability);
        Location location = new Location("DE", "nw", java.time.LocalDate.now());
        user.addLocation(location);
        return user;
    }

    /**
     * Gets a descriptive name for a state/region based on its code and country.
     * Similar to the implementation in LocationDialog.
     *
     * @param countryCode The country code in ISO format
     * @param stateCode   The state/region code
     * @return A readable description of the state/region
     */
    private String getStateDescription(String countryCode, String stateCode) {
        // If state code equals country code, it represents the whole country
        if (stateCode.equals(countryCode)) {
            return "All of " + new Locale("", countryCode).getDisplayCountry();
        }

        try {
            // Try to get description from HolidayManager's calendar hierarchy
            HolidayManager manager     = HolidayManager.getInstance(ManagerParameters.create(countryCode));
            String         description = manager.getCalendarHierarchy().getChildren().get(stateCode).getDescription();

            if (description != null && !description.isEmpty()) {
                return description + " (" + stateCode + ")";
            }
        } catch (Exception e) {
            // If we can't get the description from HolidayManager, just use the code
        }

        // Default fallback is to just return the state code
        return stateCode;
    }

    protected void initGrid(Clock clock) {
        grid.setId(LOCATION_GRID);

        // Format dates consistently
        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

        // Start Date Column
        {
            Grid.Column<Location> startColumn = grid.addColumn(new ComponentRenderer<>(location -> {
                String startDateStr = location.getStart().format(dateFormatter);
                Span   span         = new Span(startDateStr);
                span.setId(LOCATION_GRID_START_DATE_PREFIX + startDateStr);
                return span;
            }));

            // Add filterable header with sorting
            VaadinUtil.addFilterableHeader(
                    grid,
                    startColumn,
                    "Start Date",
                    VaadinIcon.CALENDAR,
                    location -> location.getStart().format(dateFormatter)
            );
        }

        // Country column with descriptive name
        {
            Grid.Column<Location> countryColumn = grid.addColumn(new ComponentRenderer<>(location -> {
                String countryCode = location.getCountry();
                Locale locale      = new Locale("", countryCode);
                String displayText = locale.getDisplayCountry() + " (" + countryCode + ")";
                Span   span        = new Span(displayText);
                span.setId(LOCATION_GRID_COUNTRY_PREFIX + displayText);
                return span;
            }));

            // Add filterable header with sorting
            VaadinUtil.addFilterableHeader(
                    grid,
                    countryColumn,
                    "Country",
                    VaadinIcon.GLOBE,
                    location -> {
                        String countryCode = location.getCountry();
                        Locale locale      = new Locale("", countryCode);
                        return locale.getDisplayCountry() + " (" + countryCode + ")";
                    }
            );
        }

        // State column with descriptive name
        {
            Grid.Column<Location> stateColumn = grid.addColumn(new ComponentRenderer<>(location -> {
                String countryCode = location.getCountry();
                String stateCode   = location.getState();
                String displayText = getStateDescription(countryCode, stateCode);
                Span   span        = new Span(displayText);
                span.setId(LOCATION_GRID_STATE_PREFIX + displayText);
                return span;
            }));

            // Add filterable header with sorting
            VaadinUtil.addFilterableHeader(
                    grid,
                    stateColumn,
                    "State/Region",
                    VaadinIcon.MAP_MARKER,
                    location -> getStateDescription(location.getCountry(), location.getState())
            );
        }

        // Add actions column with delete validation
        VaadinUtil.addActionColumn(
                grid,
                LOCATION_GRID_EDIT_BUTTON_PREFIX,
                LOCATION_GRID_DELETE_BUTTON_PREFIX,
                location -> location.getStart().format(dateFormatter),
                this::openLocationDialog,
                this::confirmDelete,
                location -> {
                    // Validate: Users must have at least one location
                    if (dataProvider.getItems().size() <= 1) {
                        return VaadinUtil.DeleteValidationResult.invalid("Cannot delete - Users must have at least one location");
                    }
                    return VaadinUtil.DeleteValidationResult.valid();
                }
        );

    }

    private void openLocationDialog(Location location) {
        // Create new or edit existing location
        LocationDialog dialog = new LocationDialog(
                location,
                this.currentUser,
                this.locationApi,
                this::refreshGrid);
        dialog.open();
    }

    private void refreshGrid() {
        if (currentUser != null) {
            // Get a fresh copy of the user to ensure we have the latest data
            User refreshedUser = userApi.getById(currentUser.getId());
            currentUser = refreshedUser;

            // Sort locations by start date in descending order (latest first)
            List<Location> sortedLocations = currentUser.getLocations().stream().sorted(Comparator.comparing(Location::getStart).reversed()).collect(Collectors.toList());

            dataProvider.getItems().clear();
            dataProvider.getItems().addAll(sortedLocations);
            dataProvider.refreshAll();
        }
    }
}
