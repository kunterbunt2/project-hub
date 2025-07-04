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
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.Main;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import com.vaadin.flow.router.*;
import com.vaadin.flow.theme.lumo.LumoUtility;
import de.bushnaq.abdalla.projecthub.dto.Availability;
import de.bushnaq.abdalla.projecthub.dto.Location;
import de.bushnaq.abdalla.projecthub.dto.User;
import de.bushnaq.abdalla.projecthub.rest.api.LocationApi;
import de.bushnaq.abdalla.projecthub.rest.api.UserApi;
import de.bushnaq.abdalla.projecthub.ui.MainLayout;
import de.bushnaq.abdalla.projecthub.ui.dialog.ConfirmDialog;
import de.bushnaq.abdalla.projecthub.ui.dialog.LocationDialog;
import de.bushnaq.abdalla.projecthub.ui.util.VaadinUtils;
import de.focus_shift.jollyday.core.HolidayManager;
import de.focus_shift.jollyday.core.ManagerParameters;
import jakarta.annotation.security.PermitAll;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.server.ResponseStatusException;

import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

@Route(value = "location/:username?", layout = MainLayout.class)
@PageTitle("User Location")
@PermitAll
public class LocationListView extends Main implements BeforeEnterObserver, AfterNavigationObserver {
    public static final String         CREATE_LOCATION_BUTTON             = "create-location-button";
    //    public static final String         INFO_BOX                           = "location-info-box";
    public static final String         LOCATION_GRID                      = "location-grid";
    public static final String         LOCATION_GRID_COUNTRY_PREFIX       = "location-country-";
    public static final String         LOCATION_GRID_DELETE_BUTTON_PREFIX = "location-delete-button-";
    public static final String         LOCATION_GRID_EDIT_BUTTON_PREFIX   = "location-edit-button-";
    public static final String         LOCATION_GRID_START_DATE_PREFIX    = "location-start-";
    public static final String         LOCATION_GRID_STATE_PREFIX         = "location-state-";
    public static final String         LOCATION_LIST_PAGE_TITLE           = "location-page-title";
    public static final String         ROUTE                              = "location";
    private             User           currentUser;
    private final       LocationApi    locationApi;
    private final       Grid<Location> locationGrid                       = new Grid<>(Location.class, false);
    private final       UserApi        userApi;

    public LocationListView(LocationApi locationApi, UserApi userApi) {
        this.locationApi = locationApi;
        this.userApi     = userApi;

        setSizeFull();
        addClassNames(LumoUtility.BoxSizing.BORDER, LumoUtility.Display.FLEX, LumoUtility.FlexDirection.COLUMN);

        add(VaadinUtils.createHeader("User Location", LOCATION_LIST_PAGE_TITLE, VaadinIcon.MAP_MARKER, CREATE_LOCATION_BUTTON, () -> openLocationDialog(null)), createLocationGrid());
    }

    @Override
    public void afterNavigation(AfterNavigationEvent event) {
        refreshLocationGrid();
    }

    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        // Get username from URL parameter or use the currently authenticated user
        String usernameParam = event.getRouteParameters().get("username").orElse(null);

        Authentication authentication  = SecurityContextHolder.getContext().getAuthentication();
        String         currentUsername = authentication != null ? authentication.getName() : null;

        // If no username is provided, use the current authenticated user
        final String username = (usernameParam == null && currentUsername != null) ?
                currentUsername : usernameParam;

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
        if (locationGrid.getListDataView().getItems().count() <= 1) {
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
                        refreshLocationGrid();
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

    private HorizontalLayout createHeaderWithIcon(VaadinIcon icon, String text) {
        Icon headerIcon = new Icon(icon);
        headerIcon.setSize("16px");

        Span headerText = new Span(text);
        headerText.addClassNames(LumoUtility.Margin.XSMALL);

        HorizontalLayout headerLayout = new HorizontalLayout(headerIcon, headerText);
        headerLayout.setAlignItems(FlexComponent.Alignment.CENTER);
        headerLayout.setSpacing(false);

        return headerLayout;
    }

    private Grid<Location> createLocationGrid() {
        locationGrid.setId(LOCATION_GRID);
        locationGrid.setWidthFull();
        locationGrid.addClassNames(LumoUtility.Border.ALL, LumoUtility.BorderColor.CONTRAST_10);

        // Add columns
        locationGrid.addColumn(new ComponentRenderer<>(location -> {
                    String startDateStr = location.getStart().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
                    Span   span         = new Span(startDateStr);
                    span.setId(LOCATION_GRID_START_DATE_PREFIX + startDateStr);
                    return span;
                }))
                .setHeader(createHeaderWithIcon(VaadinIcon.CALENDAR, "Start Date"))
                .setSortable(true)
                .setKey("start");

        // Country column with descriptive name
        locationGrid.addColumn(new ComponentRenderer<>(location -> {
                    String countryCode = location.getCountry();
                    Locale locale      = new Locale("", countryCode);
                    String displayText = locale.getDisplayCountry() + " (" + countryCode + ")";
                    Span   span        = new Span(displayText);
                    span.setId(LOCATION_GRID_COUNTRY_PREFIX + displayText);
                    return span;
                }))
                .setHeader(createHeaderWithIcon(VaadinIcon.GLOBE, "Country"))
                .setSortable(true)
                .setKey("country");

        // State column with descriptive name
        locationGrid.addColumn(new ComponentRenderer<>(location -> {
                    String countryCode = location.getCountry();
                    String stateCode   = location.getState();
                    String displayText = getStateDescription(countryCode, stateCode);
                    Span   span        = new Span(displayText);
                    span.setId(LOCATION_GRID_STATE_PREFIX + displayText);
                    return span;
                }))
                .setHeader(createHeaderWithIcon(VaadinIcon.MAP_MARKER, "State/Region"))
                .setSortable(true)
                .setKey("state");

        // Add action column
        locationGrid.addColumn(new ComponentRenderer<>(location -> {
            String startDateStr = location.getStart().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));

            HorizontalLayout layout = new HorizontalLayout();
            layout.setAlignItems(FlexComponent.Alignment.CENTER);
            layout.setSpacing(true);

            Button editButton = new Button(new Icon(VaadinIcon.EDIT));
            editButton.setId(LOCATION_GRID_EDIT_BUTTON_PREFIX + startDateStr);
            editButton.addThemeVariants(ButtonVariant.LUMO_SMALL, ButtonVariant.LUMO_TERTIARY);
            editButton.addClickListener(e -> openLocationDialog(location));
            editButton.getElement().setAttribute("title", "Edit");

            Button deleteButton = new Button(new Icon(VaadinIcon.TRASH));
            deleteButton.setId(LOCATION_GRID_DELETE_BUTTON_PREFIX + startDateStr);
            deleteButton.addThemeVariants(ButtonVariant.LUMO_SMALL, ButtonVariant.LUMO_TERTIARY, ButtonVariant.LUMO_ERROR);
            deleteButton.addClickListener(e -> confirmDelete(location));
            deleteButton.getElement().setAttribute("title", "Delete");

            // Disable delete if this is the user's only location
            if (locationGrid.getListDataView().getItems().count() <= 1) {
                deleteButton.setEnabled(false);
                deleteButton.getElement().setAttribute("title", "Cannot delete - Users must have at least one location");
            }

            layout.add(editButton, deleteButton);
            return layout;
        })).setHeader(createHeaderWithIcon(VaadinIcon.COG, "Actions")).setFlexGrow(0).setWidth("120px");

        return locationGrid;
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

    private void openLocationDialog(Location location) {
        // Create new or edit existing location
        LocationDialog dialog = new LocationDialog(
                location,
                this.currentUser,
                this.locationApi,
                ignored -> refreshLocationGrid());
        dialog.open();
    }

    private void refreshLocationGrid() {
        if (currentUser != null) {
            // Get a fresh copy of the user to ensure we have the latest data
            User refreshedUser = userApi.getById(currentUser.getId());
            currentUser = refreshedUser;

            // Sort locations by start date in descending order (latest first)
            List<Location> sortedLocations = currentUser.getLocations().stream()
                    .sorted(Comparator.comparing(Location::getStart).reversed())
                    .collect(Collectors.toList());

            locationGrid.setItems(sortedLocations);
//            updateInfoBox();
        }
    }

//    private void updateInfoBox() {
//        infoBox.removeAll();
//        infoBox.setId(INFO_BOX);
//
//        VerticalLayout infoContent = new VerticalLayout();
//        infoContent.setSpacing(false);
//        infoContent.setPadding(false);
//
//        Span heading = new Span("Location Information");
//        heading.getElement().getStyle().set("font-weight", "bold");
//
//        Span info = new Span("Locations represent where you are working under contract. " +
//                "Country and state/region are used to calculate official holidays.");
//
//        Span instruction = new Span("Start dates must be unique. The most recent location will be used for current tasks. " +
//                "Your location history should cover all your time working for this organization.");
//
//        infoContent.add(heading, info, instruction);
//        infoContent.addClassNames(
//                LumoUtility.Padding.SMALL,
//                LumoUtility.Background.CONTRAST_5,
//                LumoUtility.Border.ALL,
//                LumoUtility.BorderColor.CONTRAST_10,
//                LumoUtility.Margin.Bottom.MEDIUM
//        );
//
//        infoBox.add(infoContent);
//    }
}
