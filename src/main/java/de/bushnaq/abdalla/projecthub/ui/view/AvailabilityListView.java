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
import com.vaadin.flow.data.renderer.NumberRenderer;
import com.vaadin.flow.router.*;
import com.vaadin.flow.theme.lumo.LumoUtility;
import de.bushnaq.abdalla.projecthub.dto.Availability;
import de.bushnaq.abdalla.projecthub.dto.Location;
import de.bushnaq.abdalla.projecthub.dto.User;
import de.bushnaq.abdalla.projecthub.rest.api.AvailabilityApi;
import de.bushnaq.abdalla.projecthub.rest.api.UserApi;
import de.bushnaq.abdalla.projecthub.ui.MainLayout;
import de.bushnaq.abdalla.projecthub.ui.dialog.AvailabilityDialog;
import de.bushnaq.abdalla.projecthub.ui.dialog.ConfirmDialog;
import de.bushnaq.abdalla.projecthub.ui.util.VaadinUtil;
import jakarta.annotation.security.PermitAll;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.server.ResponseStatusException;

import java.text.NumberFormat;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

@Route(value = "availability/:username?", layout = MainLayout.class)
@PageTitle("User Availability")
@PermitAll
public class AvailabilityListView extends Main implements BeforeEnterObserver, AfterNavigationObserver {
    public static final String             AVAILABILITY_GRID                      = "availability-grid";
    public static final String             AVAILABILITY_GRID_AVAILABILITY_PREFIX  = "availability-value-";
    public static final String             AVAILABILITY_GRID_DELETE_BUTTON_PREFIX = "availability-delete-button-";
    public static final String             AVAILABILITY_GRID_EDIT_BUTTON_PREFIX   = "availability-edit-button-";
    public static final String             AVAILABILITY_GRID_START_DATE_PREFIX    = "availability-start-";
    public static final String             AVAILABILITY_LIST_PAGE_TITLE           = "availability-page-title";
    public static final String             CREATE_AVAILABILITY_BUTTON             = "create-availability-button";
    //    public static final String             INFO_BOX                               = "availability-info-box";
    public static final String             ROUTE                                  = "availability";
    private final       AvailabilityApi    availabilityApi;
    private             User               currentUser;
    private final       Grid<Availability> grid                                   = new Grid<>(Availability.class, false);
    //    private final       Div                infoBox                                = new Div();
    private final       UserApi            userApi;

    public AvailabilityListView(AvailabilityApi availabilityApi, UserApi userApi) {
        this.availabilityApi = availabilityApi;
        this.userApi         = userApi;

        setSizeFull();
        addClassNames(LumoUtility.BoxSizing.BORDER, LumoUtility.Display.FLEX, LumoUtility.FlexDirection.COLUMN);
        add(VaadinUtil.createHeader("User Availability", AVAILABILITY_LIST_PAGE_TITLE, VaadinIcon.CHART, CREATE_AVAILABILITY_BUTTON, () -> openAvailabilityDialog(null)), createGrid());
    }

    @Override
    public void afterNavigation(AfterNavigationEvent event) {
        refreshAvailabilityGrid();
    }

    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        // Get username from URL parameter or use the currently authenticated user
        String usernameParam = event.getRouteParameters().get("username").orElse(null);

        Authentication authentication  = SecurityContextHolder.getContext().getAuthentication();
        String         currentUsername = authentication != null ? authentication.getName() : null;

        // If no username is provided, use the current authenticated user
        // Store in a final variable to use in lambda
        final String username = (usernameParam == null && currentUsername != null) ? currentUsername : usernameParam;

        if (username != null) {
            try {
                // Find user by username using the direct getByName method
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

    private void confirmDelete(Availability availability) {
        if (grid.getListDataView().getItems().count() <= 1) {
            Notification notification = Notification.show("Cannot delete - Users must have at least one availability", 3000, Notification.Position.MIDDLE);
            notification.addThemeVariants(NotificationVariant.LUMO_ERROR);
            return;
        }

        ConfirmDialog dialog = new ConfirmDialog("Confirm Delete",
                "Are you sure you want to delete this availability record?",
                "Delete",
                () -> {
                    try {
                        availabilityApi.deleteById(currentUser, availability);
                        refreshAvailabilityGrid();
                        Notification.show("Availability deleted", 3000, Notification.Position.MIDDLE);
                    } catch (Exception ex) {
                        Notification notification = Notification.show("Failed to delete: " + ex.getMessage(), 3000, Notification.Position.MIDDLE);
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

    private Grid<Availability> createGrid() {
        grid.setId(AVAILABILITY_GRID);
//        grid.setWidthFull();
        grid.setSizeFull();
        grid.addClassNames(LumoUtility.Border.ALL, LumoUtility.BorderColor.CONTRAST_10);

        // Add columns
        grid.addColumn(new ComponentRenderer<>(availability -> {
                    String startDateStr = availability.getStart().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
                    Span   span         = new Span(startDateStr);
                    span.setId(AVAILABILITY_GRID_START_DATE_PREFIX + startDateStr);
                    span.setId(AVAILABILITY_GRID_START_DATE_PREFIX + startDateStr);
                    return span;
                }))
                .setHeader(createHeaderWithIcon(VaadinIcon.CALENDAR, "Start Date"))
                .setSortable(true)
                .setKey("start");

        NumberFormat percentageFormat = NumberFormat.getPercentInstance(Locale.US);
        grid.addColumn(new NumberRenderer<>(
                        availability -> {
                            Span span = new Span(percentageFormat.format(availability.getAvailability()));
                            span.setId(AVAILABILITY_GRID_AVAILABILITY_PREFIX + availability.getAvailability());
                            return availability.getAvailability();
                        }, percentageFormat))
                .setHeader(createHeaderWithIcon(VaadinIcon.CHART, "Availability"))
                .setSortable(true)
                .setKey("availability");

        // Add action column
        grid.addColumn(new ComponentRenderer<>(availability -> {
            String startDateStr = availability.getStart().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));

            HorizontalLayout layout = new HorizontalLayout();
            layout.setAlignItems(FlexComponent.Alignment.CENTER);
            layout.setSpacing(true);

            Button editButton = new Button(new Icon(VaadinIcon.EDIT));
            editButton.setId(AVAILABILITY_GRID_EDIT_BUTTON_PREFIX + startDateStr);
            editButton.addThemeVariants(ButtonVariant.LUMO_SMALL, ButtonVariant.LUMO_TERTIARY);
            editButton.addClickListener(e -> openAvailabilityDialog(availability));
            editButton.getElement().setAttribute("title", "Edit");

            Button deleteButton = new Button(new Icon(VaadinIcon.TRASH));
            deleteButton.setId(AVAILABILITY_GRID_DELETE_BUTTON_PREFIX + startDateStr);
            deleteButton.addThemeVariants(ButtonVariant.LUMO_SMALL, ButtonVariant.LUMO_TERTIARY, ButtonVariant.LUMO_ERROR);
            deleteButton.addClickListener(e -> confirmDelete(availability));
            deleteButton.getElement().setAttribute("title", "Delete");

            // Disable delete if this is the user's only availability
            if (grid.getListDataView().getItems().count() <= 1) {
                deleteButton.setEnabled(false);
                deleteButton.getElement().setAttribute("title", "Cannot delete - Users must have at least one availability");
            }

            layout.add(editButton, deleteButton);
            return layout;
        })).setHeader("Actions").setFlexGrow(0).setWidth("120px");

        return grid;
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

    private void openAvailabilityDialog(Availability availability) {
        // Create new or edit existing availability
        AvailabilityDialog dialog = new AvailabilityDialog(
                availability,
                this.currentUser,
                this.availabilityApi,
                this::refreshAvailabilityGrid);
        dialog.open();
    }

    private void refreshAvailabilityGrid() {
        if (currentUser != null) {
            // Get a fresh copy of the user to ensure we have the latest data
            User refreshedUser = userApi.getById(currentUser.getId());
            currentUser = refreshedUser;

            // Sort availabilities by start date in descending order (latest first)
            List<Availability> sortedAvailabilities = currentUser.getAvailabilities().stream()
                    .sorted(Comparator.comparing(Availability::getStart).reversed())
                    .collect(Collectors.toList());

            grid.setItems(sortedAvailabilities);
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
//        Span heading = new Span("Availability Information");
//        heading.getElement().getStyle().set("font-weight", "bold");
//
//        Span info = new Span("Availability represents the percentage of time you are available for work. " +
//                "Values should be between 0 (unavailable) and 150% availability.");
//        Span instruction = new Span("Start dates must be unique. The most recent availability will be used for current tasks. +" +
//                "Your availability list should cover all your time you are working for this organization.");
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
