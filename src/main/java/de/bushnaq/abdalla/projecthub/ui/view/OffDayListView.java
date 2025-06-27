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
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Main;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import com.vaadin.flow.router.*;
import com.vaadin.flow.theme.lumo.LumoUtility;
import de.bushnaq.abdalla.projecthub.dto.OffDay;
import de.bushnaq.abdalla.projecthub.dto.OffDayType;
import de.bushnaq.abdalla.projecthub.dto.User;
import de.bushnaq.abdalla.projecthub.rest.api.OffDayApi;
import de.bushnaq.abdalla.projecthub.rest.api.UserApi;
import de.bushnaq.abdalla.projecthub.ui.MainLayout;
import de.bushnaq.abdalla.projecthub.ui.dialog.ConfirmDialog;
import de.bushnaq.abdalla.projecthub.ui.dialog.OffDayDialog;
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
    public static final String       CREATE_OFFDAY_BUTTON             = "create-offday-button";
    public static final String       INFO_BOX                         = "offday-info-box";
    public static final String       OFFDAY_GRID                      = "offday-grid";
    public static final String       OFFDAY_GRID_DELETE_BUTTON_PREFIX = "offday-delete-button-";
    public static final String       OFFDAY_GRID_EDIT_BUTTON_PREFIX   = "offday-edit-button-";
    public static final String       OFFDAY_GRID_END_DATE_PREFIX      = "offday-end-";
    public static final String       OFFDAY_GRID_START_DATE_PREFIX    = "offday-start-";
    public static final String       OFFDAY_GRID_TYPE_PREFIX          = "offday-type-";
    public static final String       OFFDAY_LIST_PAGE_TITLE           = "offday-page-title";
    public static final String       ROUTE                            = "offday";
    private             User         currentUser;
    private final       Div          infoBox                          = new Div();
    private final       OffDayApi    offDayApi;
    private final       Grid<OffDay> offDayGrid                       = new Grid<>(OffDay.class, false);
    private final       UserApi      userApi;

    public OffDayListView(OffDayApi offDayApi, UserApi userApi) {
        this.offDayApi = offDayApi;
        this.userApi   = userApi;

        addClassName("offday-view");
        setWidthFull();
        addClassNames(LumoUtility.Padding.LARGE);

        HorizontalLayout headerLayout = createHeader();
        add(headerLayout, infoBox, createOffDayGrid());
    }

    @Override
    public void afterNavigation(AfterNavigationEvent event) {
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

    private void confirmDelete(OffDay offDay) {
        ConfirmDialog dialog = new ConfirmDialog("Confirm Delete",
                "Are you sure you want to delete this off day record?",
                "Delete",
                () -> {
                    try {
                        offDayApi.deleteById(currentUser, offDay);
                        refreshOffDayGrid();
                        Notification.show("Off day deleted", 3000, Notification.Position.MIDDLE);
                    } catch (Exception ex) {
                        Notification notification = Notification.show("Failed to delete: " + ex.getMessage(), 3000, Notification.Position.MIDDLE);
                        notification.addThemeVariants(NotificationVariant.LUMO_ERROR);
                    }
                });
        dialog.open();
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
        return user;
    }

    private HorizontalLayout createHeader() {
        H2 heading = new H2("Manage Your Off Days");
        heading.setId(OFFDAY_LIST_PAGE_TITLE);
        heading.addClassNames(LumoUtility.Margin.NONE);

        Button addButton = new Button("Add Off Day");
        addButton.setId(CREATE_OFFDAY_BUTTON);
        addButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        addButton.setIcon(new Icon(VaadinIcon.PLUS));
        addButton.addClickListener(e -> openOffDayDialog(null));

        HorizontalLayout headerLayout = new HorizontalLayout(heading, addButton);
        headerLayout.setWidthFull();
        headerLayout.setJustifyContentMode(FlexComponent.JustifyContentMode.BETWEEN);
        headerLayout.setAlignItems(FlexComponent.Alignment.CENTER);
        headerLayout.addClassNames(LumoUtility.Margin.Bottom.MEDIUM);

        return headerLayout;
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

    private Grid<OffDay> createOffDayGrid() {
        offDayGrid.setId(OFFDAY_GRID);
        offDayGrid.setWidthFull();
        offDayGrid.addClassNames(LumoUtility.Border.ALL, LumoUtility.BorderColor.CONTRAST_10);

        // Add columns
        offDayGrid.addColumn(new ComponentRenderer<>(offDay -> {
                    DateTimeFormatter formatter    = DateTimeFormatter.ofPattern("yyyy-MM-dd");
                    String            startDateStr = offDay.getFirstDay().format(formatter);
                    Span              span         = new Span(startDateStr);
                    span.setId(OFFDAY_GRID_START_DATE_PREFIX + offDay.getId());
                    return span;
                }))
                .setHeader(createHeaderWithIcon(VaadinIcon.CALENDAR, "First Day"))
                .setSortable(true)
                .setKey("firstDay");

        offDayGrid.addColumn(new ComponentRenderer<>(offDay -> {
                    DateTimeFormatter formatter  = DateTimeFormatter.ofPattern("yyyy-MM-dd");
                    String            endDateStr = offDay.getLastDay().format(formatter);
                    Span              span       = new Span(endDateStr);
                    span.setId(OFFDAY_GRID_END_DATE_PREFIX + offDay.getId());
                    return span;
                }))
                .setHeader(createHeaderWithIcon(VaadinIcon.CALENDAR, "Last Day"))
                .setSortable(true)
                .setKey("lastDay");

        offDayGrid.addColumn(new ComponentRenderer<>(offDay -> {
                    OffDayType type = offDay.getType();
                    Span       span = new Span(type.name());
                    span.setId(OFFDAY_GRID_TYPE_PREFIX + offDay.getId());
                    return span;
                }))
                .setHeader(createHeaderWithIcon(VaadinIcon.TAGS, "Type"))
                .setSortable(true)
                .setKey("type");

        // Add action column
        offDayGrid.addColumn(new ComponentRenderer<>(offDay -> {
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

        return offDayGrid;
    }

    private void openOffDayDialog(OffDay offDay) {
        // Create new or edit existing off day
        OffDayDialog dialog = new OffDayDialog(
                offDay,
                this.currentUser,
                this.offDayApi,
                this::refreshOffDayGrid);
        dialog.open();
    }

    private void refreshOffDayGrid() {
        if (currentUser != null) {
            // Get a fresh copy of the user to ensure we have the latest data
            User refreshedUser = userApi.getById(currentUser.getId());
            currentUser = refreshedUser;

            // Sort offdays by first day in descending order (latest first)
            List<OffDay> sortedOffDays = currentUser.getOffDays().stream()
                    .sorted(Comparator.comparing(OffDay::getFirstDay).reversed())
                    .collect(Collectors.toList());

            offDayGrid.setItems(sortedOffDays);
            updateInfoBox();
        }
    }

    private void updateInfoBox() {
        infoBox.removeAll();
        infoBox.setId(INFO_BOX);

        VerticalLayout infoContent = new VerticalLayout();
        infoContent.setSpacing(false);
        infoContent.setPadding(false);

        Span heading = new Span("Off Day Information");
        heading.getElement().getStyle().set("font-weight", "bold");

        Span info = new Span("Off days represent periods when you are not available for work due to vacation, " +
                "sickness, holidays, or business trips.");
        Span instruction = new Span("Each off day record consists of a date range and a type. " +
                "Adding your off days helps with accurate project planning and resource allocation.");

        infoContent.add(heading, info, instruction);
        infoContent.addClassNames(
                LumoUtility.Padding.SMALL,
                LumoUtility.Background.CONTRAST_5,
                LumoUtility.Border.ALL,
                LumoUtility.BorderColor.CONTRAST_10,
                LumoUtility.Margin.Bottom.MEDIUM
        );

        infoBox.add(infoContent);
    }
}
