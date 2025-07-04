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
import de.bushnaq.abdalla.projecthub.dto.*;
import de.bushnaq.abdalla.projecthub.dto.Location;
import de.bushnaq.abdalla.projecthub.rest.api.OffDayApi;
import de.bushnaq.abdalla.projecthub.rest.api.UserApi;
import de.bushnaq.abdalla.projecthub.ui.MainLayout;
import de.bushnaq.abdalla.projecthub.ui.component.YearCalendarComponent;
import de.bushnaq.abdalla.projecthub.ui.dialog.ConfirmDialog;
import de.bushnaq.abdalla.projecthub.ui.dialog.OffDayDialog;
import de.bushnaq.abdalla.projecthub.ui.util.VaadinUtils;
import jakarta.annotation.security.PermitAll;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.server.ResponseStatusException;

import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Route(value = "offday/:username?", layout = MainLayout.class)
@PageTitle("User Off Days")
@PermitAll
public class OffDayListView extends Main implements BeforeEnterObserver, AfterNavigationObserver {
    public static final String                CREATE_OFFDAY_BUTTON             = "create-offday-button";
    //    public static final String                INFO_BOX                         = "offday-info-box";
    public static final String                OFFDAY_GRID                      = "offday-grid";
    public static final String                OFFDAY_GRID_DELETE_BUTTON_PREFIX = "offday-delete-button-";
    public static final String                OFFDAY_GRID_EDIT_BUTTON_PREFIX   = "offday-edit-button-";
    public static final String                OFFDAY_GRID_END_DATE_PREFIX      = "offday-end-";
    public static final String                OFFDAY_GRID_START_DATE_PREFIX    = "offday-start-";
    public static final String                OFFDAY_GRID_TYPE_PREFIX          = "offday-type-";
    public static final String                OFFDAY_LIST_PAGE_TITLE           = "offday-page-title";
    public static final String                ROUTE                            = "offday";
    private             User                  currentUser;
    private final       Grid<OffDay>          grid                             = new Grid<>(OffDay.class, false);
    private final       OffDayApi             offDayApi;
    private final       UserApi               userApi;
    private             YearCalendarComponent yearCalendar;


    public OffDayListView(OffDayApi offDayApi, UserApi userApi) {
        this.offDayApi = offDayApi;
        this.userApi   = userApi;

        setSizeFull();
        addClassNames(LumoUtility.BoxSizing.BORDER, LumoUtility.Display.FLEX, LumoUtility.FlexDirection.COLUMN);

        add(VaadinUtils.createHeader("User Off-Days", OFFDAY_LIST_PAGE_TITLE, VaadinIcon.CALENDAR, CREATE_OFFDAY_BUTTON, () -> openOffDayDialog(null)), new HorizontalLayout(createGrid(), createCalendar()));
    }

    @Override
    public void afterNavigation(AfterNavigationEvent event) {
//        createCalendar();
        refreshOffDayGrid();
    }

    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        // Get username from URL parameter or use the currently authenticated user
        String usernameParam = event.getRouteParameters().get("username").orElse(null);

        Authentication authentication  = SecurityContextHolder.getContext().getAuthentication();
        String         currentUsername = authentication != null ? authentication.getName() : null;

        // If no username is provided, use the current authenticated user
        // Store in a final variable to use in lambda
        final String username = (usernameParam == null && currentUsername != null) ?
                currentUsername : usernameParam;

        if (username != null) {
            try {
                // Find user by username using the direct getByName method
                currentUser = userApi.getByName(username);
                currentUser.initialize();
            } catch (ResponseStatusException ex) {
                if (ex.getStatusCode().equals(HttpStatus.NOT_FOUND)) {
                    // Create a new user since one wasn't found
                    User newUser = createDefaultUser(username);
                    currentUser = userApi.persist(newUser);
                    currentUser.initialize();
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
        createCalendar();
    }

    private void confirmDelete(OffDay offDay) {
        ConfirmDialog dialog = new ConfirmDialog("Confirm Delete", "Are you sure you want to delete this off day record?", "Delete",
                () -> {
                    try {
                        offDayApi.deleteById(currentUser, offDay);
                        refreshOffDayGrid();
                        // Update the calendar with the fresh user data
                        if (yearCalendar != null) {
                            yearCalendar.updateCalendar(currentUser);
                        }
                        Notification.show("Off day deleted", 3000, Notification.Position.MIDDLE);
                    } catch (Exception ex) {
                        Notification notification = Notification.show("Failed to delete: " + ex.getMessage(), 3000, Notification.Position.MIDDLE);
                        notification.addThemeVariants(NotificationVariant.LUMO_ERROR);
                    }
                });
        dialog.open();
    }

    private YearCalendarComponent createCalendar() {
        // Create a new YearCalendarComponent with the current user and the current year
        if (yearCalendar != null)
            return yearCalendar;
        yearCalendar = new YearCalendarComponent(currentUser, java.time.LocalDate.now().getYear(), this::handleCalendarDayClick);
        return yearCalendar;
    }

    /**
     * Creates a default user with standard settings
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
        de.bushnaq.abdalla.projecthub.dto.Location location = new Location("DE", "nw", java.time.LocalDate.now());
        user.addLocation(location);
        return user;
    }

    private Grid<OffDay> createGrid() {
        grid.setId(OFFDAY_GRID);
        grid.setWidthFull();
        grid.addClassNames(LumoUtility.Border.ALL, LumoUtility.BorderColor.CONTRAST_10);

        // Add columns
        grid.addColumn(new ComponentRenderer<>(offDay -> {
                    DateTimeFormatter formatter    = DateTimeFormatter.ofPattern("yyyy-MM-dd");
                    String            startDateStr = offDay.getFirstDay().format(formatter);
                    Span              span         = new Span(startDateStr);
                    span.setId(OFFDAY_GRID_START_DATE_PREFIX + offDay.getId());
                    return span;
                }))
                .setHeader(createHeaderWithIcon(VaadinIcon.CALENDAR, "First Day"))
                .setSortable(true)
                .setKey("firstDay");

        grid.addColumn(new ComponentRenderer<>(offDay -> {
                    DateTimeFormatter formatter  = DateTimeFormatter.ofPattern("yyyy-MM-dd");
                    String            endDateStr = offDay.getLastDay().format(formatter);
                    Span              span       = new Span(endDateStr);
                    span.setId(OFFDAY_GRID_END_DATE_PREFIX + offDay.getId());
                    return span;
                }))
                .setHeader(createHeaderWithIcon(VaadinIcon.CALENDAR, "Last Day"))
                .setSortable(true)
                .setKey("lastDay");

        grid.addColumn(new ComponentRenderer<>(offDay -> {
                    OffDayType type = offDay.getType();
                    Span       span = new Span(type.name());
                    span.setId(OFFDAY_GRID_TYPE_PREFIX + offDay.getId());
                    return span;
                }))
                .setHeader(createHeaderWithIcon(VaadinIcon.TAGS, "Type"))
                .setSortable(true)
                .setKey("type");

        // Add action column
        grid.addColumn(new ComponentRenderer<>(offDay -> {
            HorizontalLayout layout = new HorizontalLayout();
            layout.setAlignItems(FlexComponent.Alignment.CENTER);
            layout.setSpacing(true);

            Button editButton = new Button(new Icon(VaadinIcon.EDIT));
            editButton.setId(OFFDAY_GRID_EDIT_BUTTON_PREFIX + offDay.getId());
            editButton.addThemeVariants(ButtonVariant.LUMO_SMALL, ButtonVariant.LUMO_TERTIARY);
            editButton.addClickListener(e -> openOffDayDialog(offDay));
            editButton.getElement().setAttribute("title", "Edit");

            Button deleteButton = new Button(new Icon(VaadinIcon.TRASH));
            deleteButton.setId(OFFDAY_GRID_DELETE_BUTTON_PREFIX + offDay.getId());
            deleteButton.addThemeVariants(ButtonVariant.LUMO_SMALL, ButtonVariant.LUMO_TERTIARY, ButtonVariant.LUMO_ERROR);
            deleteButton.addClickListener(e -> confirmDelete(offDay));
            deleteButton.getElement().setAttribute("title", "Delete");

            layout.add(editButton, deleteButton);
            return layout;
        })).setHeader(createHeaderWithIcon(VaadinIcon.COG, "Actions")).setFlexGrow(0).setWidth("120px");

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

    /**
     * Handles clicks on calendar days that have off days
     *
     * @param date The date that was clicked
     */
    private void handleCalendarDayClick(java.time.LocalDate date) {
        if (currentUser != null) {
            // Find the off day that contains this date
            OffDay matchingOffDay = currentUser.getOffDays().stream()
                    .filter(offDay ->
                            (date.equals(offDay.getFirstDay()) || date.isAfter(offDay.getFirstDay())) &&
                                    (date.equals(offDay.getLastDay()) || date.isBefore(offDay.getLastDay())))
                    .findFirst()
                    .orElse(null);

            if (matchingOffDay != null) {
                // Open the dialog to edit this off day
                openOffDayDialog(matchingOffDay);
            }
        }
    }

    private void openOffDayDialog(OffDay offDay) {
        // Create new or edit existing off day
        OffDayDialog dialog = new OffDayDialog(
                offDay,
                this.currentUser,
                this.offDayApi,
                () -> {
                    refreshOffDayGrid();
                    if (yearCalendar != null) {
                        // Pass the updated user to the calendar
                        yearCalendar.updateCalendar(currentUser);
                    }
                });
        dialog.open();
    }


    private void refreshOffDayGrid() {
        if (currentUser != null) {
            // Reload the user to get fresh data
            currentUser = userApi.getById(currentUser.getId());
            currentUser.initialize();

            // Sort off days by start date (newest to oldest)
            List<OffDay> sortedOffDays = currentUser.getOffDays().stream().sorted(Comparator.comparing(OffDay::getFirstDay).reversed()).collect(Collectors.toList());

            grid.setItems(sortedOffDays);

            yearCalendar.updateCalendar(currentUser);
        }
    }

}
