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
import de.bushnaq.abdalla.projecthub.dto.User;
import de.bushnaq.abdalla.projecthub.rest.api.UserApi;
import de.bushnaq.abdalla.projecthub.ui.common.ConfirmDialog;
import de.bushnaq.abdalla.projecthub.ui.common.UserDialog;
import de.bushnaq.abdalla.projecthub.ui.view.MainLayout;
import jakarta.annotation.security.PermitAll;

import java.time.Clock;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;

@Route("user-list")
@PageTitle("User List Page")
@Menu(order = 2, icon = "vaadin:users", title = "User List")
@PermitAll // When security is enabled, allow all authenticated users
public class UserListView extends Main implements AfterNavigationObserver {
    public static final String     CREATE_USER_BUTTON             = "create-user-button";
    public static final String     ROUTE                          = "user-list";
    public static final String     USER_GRID_ACTION_BUTTON_PREFIX = "user-grid-action-button-prefix-";
    public static final String     USER_GRID_DELETE_BUTTON_PREFIX = "user-grid-delete-button-prefix-";
    public static final String     USER_GRID_EDIT_BUTTON_PREFIX   = "user-grid-edit-button-prefix-";
    public static final String     USER_GRID_NAME_PREFIX          = "user-grid-name-";
    public static final String     USER_LIST_PAGE_TITLE           = "user-list-page-title";
    private final       Clock      clock;
    private final       Grid<User> grid;
    private final       UserApi    userApi;

    public UserListView(UserApi userApi, Clock clock) {
        this.userApi = userApi;
        this.clock   = clock;

        // Create header layout with title and create button
        HorizontalLayout headerLayout = new HorizontalLayout();
        headerLayout.setWidthFull();
        headerLayout.setPadding(false);
        headerLayout.setAlignItems(FlexComponent.Alignment.CENTER);
        headerLayout.setJustifyContentMode(FlexComponent.JustifyContentMode.BETWEEN);

        H2 pageTitle = new H2("Users");
        pageTitle.setId(USER_LIST_PAGE_TITLE);
        pageTitle.addClassNames(
                LumoUtility.Margin.Top.MEDIUM,
                LumoUtility.Margin.Bottom.SMALL
        );

        Button createButton = new Button("Create", new Icon(VaadinIcon.PLUS));
        createButton.setId(CREATE_USER_BUTTON);
        createButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        createButton.addClickListener(e -> openUserDialog(null));

        headerLayout.add(pageTitle, createButton);

        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofLocalizedDateTime(FormatStyle.LONG).withZone(clock.getZone()).withLocale(getLocale());

        grid = new Grid<>();
        refreshGrid();
        {
            Grid.Column<User> column = grid.addColumn(User::getKey).setHeader("Key");
            column.setId("user-grid-key-column");
        }
        {
            Grid.Column<User> column = grid.addColumn(new ComponentRenderer<>(user -> {
                Div div = new Div();
                // Create color indicator if available
                if (user.getColor() != null) {
                    Div square = new Div();
                    square.setMinHeight("16px");
                    square.setMaxHeight("16px");
                    square.setMinWidth("16px");
                    square.setMaxWidth("16px");
                    square.getStyle().set("float", "left");
                    square.getStyle().set("margin", "1px");
                    square.getStyle().set("background-color", "#" + Integer.toHexString(user.getColor().getRGB()).substring(2));
                    div.add(square);
                }
                div.add(user.getName());
                div.setId(USER_GRID_NAME_PREFIX + user.getName());
                return div;
            })).setHeader("Name");
            column.setId("user-grid-name-column");
        }
        {
            Grid.Column<User> column = grid.addColumn(User::getEmail).setHeader("Email");
            column.setId("user-grid-email-column");
        }
        {
            Grid.Column<User> column = grid.addColumn(user -> user.getFirstWorkingDay() != null ? user.getFirstWorkingDay().toString() : "").setHeader("First Working Day");
            column.setId("user-grid-first-working-day-column");
        }
        {
            Grid.Column<User> column = grid.addColumn(user -> user.getLastWorkingDay() != null ? user.getLastWorkingDay().toString() : "").setHeader("Last Working Day");
            column.setId("user-grid-last-working-day-column");
        }
        {
            Grid.Column<User> column = grid.addColumn(user -> dateTimeFormatter.format(user.getCreated())).setHeader("Created");
            column.setId("user-grid-created-column");
        }
        {
            Grid.Column<User> column = grid.addColumn(user -> dateTimeFormatter.format(user.getUpdated())).setHeader("Updated");
            column.setId("user-grid-updated-column");
        }

        // Add actions column with context menu
        grid.addColumn(new ComponentRenderer<>(user -> {
            Button actionButton = new Button(new Icon(VaadinIcon.ELLIPSIS_DOTS_V));
            actionButton.setId(USER_GRID_ACTION_BUTTON_PREFIX + user.getName());
            actionButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY_INLINE);
            actionButton.getElement().setAttribute("aria-label", "More options");

            // Center the button with CSS
            actionButton.getStyle().set("margin", "auto");
            actionButton.getStyle().set("display", "block");

            ContextMenu contextMenu = new ContextMenu();
            contextMenu.setOpenOnClick(true);
            contextMenu.setTarget(actionButton);

            contextMenu.addItem("Edit...", e -> openUserDialog(user)).setId(USER_GRID_EDIT_BUTTON_PREFIX + user.getName());
            contextMenu.addItem("Delete...", e -> confirmDelete(user)).setId(USER_GRID_DELETE_BUTTON_PREFIX + user.getName());

            return actionButton;
        })).setWidth("70px").setFlexGrow(0);

        grid.setSizeFull();

        setSizeFull();
        addClassNames(LumoUtility.BoxSizing.BORDER, LumoUtility.Display.FLEX, LumoUtility.FlexDirection.COLUMN);

        add(headerLayout, grid);
    }

    @Override
    public void afterNavigation(AfterNavigationEvent event) {
        getElement().getParent().getComponent()
                .ifPresent(component -> {
                    if (component instanceof MainLayout mainLayout) {
                        mainLayout.getBreadcrumbs().clear();
                        mainLayout.getBreadcrumbs().addItem("Users", UserListView.class);
                    }
                });
    }

    private void confirmDelete(User user) {
        String message = "Are you sure you want to delete user \"" + user.getName() + "\"?";
        ConfirmDialog dialog = new ConfirmDialog(
                "Confirm Delete",
                message,
                "Delete",
                () -> {
                    userApi.deleteById(user.getId());
                    refreshGrid();
                    Notification.show("User deleted", 3000, Notification.Position.BOTTOM_START);
                }
        );
        dialog.open();
    }

    private void openUserDialog(User user) {
        UserDialog dialog = new UserDialog(user, savedUser -> {
            if (user != null) {
                // Edit mode
                userApi.update(savedUser);
                Notification.show("User updated", 3000, Notification.Position.BOTTOM_START);
            } else {
                // Create mode
                userApi.persist(savedUser);
                Notification.show("User created", 3000, Notification.Position.BOTTOM_START);
            }
            refreshGrid();
        });
        dialog.open();
    }

    private void refreshGrid() {
        grid.setItems(userApi.getAll());
    }
}
