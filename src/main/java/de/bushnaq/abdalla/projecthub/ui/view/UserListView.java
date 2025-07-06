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
import de.bushnaq.abdalla.projecthub.dto.User;
import de.bushnaq.abdalla.projecthub.rest.api.UserApi;
import de.bushnaq.abdalla.projecthub.ui.MainLayout;
import de.bushnaq.abdalla.projecthub.ui.dialog.ConfirmDialog;
import de.bushnaq.abdalla.projecthub.ui.dialog.UserDialog;
import de.bushnaq.abdalla.projecthub.ui.util.VaadinUtils;
import jakarta.annotation.security.PermitAll;

import java.time.Clock;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;

@Route("user-list")
@PageTitle("User List Page")
@Menu(order = 2, icon = "vaadin:users", title = "Users")
@PermitAll // When security is enabled, allow all authenticated users
public class UserListView extends Main implements AfterNavigationObserver {
    public static final String     CREATE_USER_BUTTON             = "create-user-button";
    public static final String     ROUTE                          = "user-list";
    public static final String     USER_GRID_DELETE_BUTTON_PREFIX = "user-grid-delete-button-prefix-";
    public static final String     USER_GRID_EDIT_BUTTON_PREFIX   = "user-grid-edit-button-prefix-";
    public static final String     USER_GRID_NAME_PREFIX          = "user-grid-name-";
    public static final String     USER_LIST_PAGE_TITLE           = "user-list-page-title";
    private final       Clock      clock;
    private             Grid<User> grid;
    private final       UserApi    userApi;

    public UserListView(UserApi userApi, Clock clock) {
        this.userApi = userApi;
        this.clock   = clock;

        setSizeFull();
        addClassNames(LumoUtility.BoxSizing.BORDER, LumoUtility.Display.FLEX, LumoUtility.FlexDirection.COLUMN);

        add(VaadinUtils.createHeader("Users", USER_LIST_PAGE_TITLE, VaadinIcon.USERS, CREATE_USER_BUTTON, () -> openUserDialog(null)), createGrid(clock));
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

    private Grid<User> createGrid(Clock clock) {
        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofLocalizedDateTime(FormatStyle.LONG).withZone(clock.getZone()).withLocale(getLocale());

        grid = new Grid<>();
        refreshGrid();
        {
            Grid.Column<User> column = grid.addColumn(User::getKey).setHeader("Key");
            column.setId("user-grid-key-column");
            column.setHeader(new HorizontalLayout(new Icon(VaadinIcon.KEY), new Div(new Text("Key"))));
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
            column.setHeader(new HorizontalLayout(new Icon(VaadinIcon.USER), new Div(new Text("Name"))));
        }
        {
            Grid.Column<User> column = grid.addColumn(User::getEmail).setHeader("Email");
            column.setId("user-grid-email-column");
            column.setHeader(new HorizontalLayout(new Icon(VaadinIcon.ENVELOPE), new Div(new Text("Email"))));
        }
        {
            Grid.Column<User> column = grid.addColumn(user -> user.getFirstWorkingDay() != null ? user.getFirstWorkingDay().toString() : "").setHeader("First Working Day");
            column.setId("user-grid-first-working-day-column");
            column.setHeader(new HorizontalLayout(new Icon(VaadinIcon.CALENDAR_USER), new Div(new Text("First Working Day"))));
        }
        {
            Grid.Column<User> column = grid.addColumn(user -> user.getLastWorkingDay() != null ? user.getLastWorkingDay().toString() : "").setHeader("Last Working Day");
            column.setId("user-grid-last-working-day-column");
            column.setHeader(new HorizontalLayout(new Icon(VaadinIcon.CALENDAR_USER), new Div(new Text("Last Working Day"))));
        }
        {
            Grid.Column<User> column = grid.addColumn(user -> dateTimeFormatter.format(user.getCreated())).setHeader("Created");
            column.setId("user-grid-created-column");
            column.setHeader(new HorizontalLayout(new Icon(VaadinIcon.CALENDAR), new Div(new Text("Created"))));
        }
        {
            Grid.Column<User> column = grid.addColumn(user -> dateTimeFormatter.format(user.getUpdated())).setHeader("Updated");
            column.setId("user-grid-updated-column");
            column.setHeader(new HorizontalLayout(new Icon(VaadinIcon.CALENDAR), new Div(new Text("Updated"))));
        }

        // Add actions column with direct buttons instead of context menu
        grid.addColumn(new ComponentRenderer<>(user -> {
            HorizontalLayout layout = new HorizontalLayout();
            layout.setAlignItems(FlexComponent.Alignment.CENTER);
            layout.setSpacing(true);

            Button editButton = new Button(new Icon(VaadinIcon.EDIT));
            editButton.setId(USER_GRID_EDIT_BUTTON_PREFIX + user.getName());
            editButton.addThemeVariants(ButtonVariant.LUMO_SMALL, ButtonVariant.LUMO_TERTIARY);
            editButton.addClickListener(e -> openUserDialog(user));
            editButton.getElement().setAttribute("title", "Edit");

            Button deleteButton = new Button(new Icon(VaadinIcon.TRASH));
            deleteButton.setId(USER_GRID_DELETE_BUTTON_PREFIX + user.getName());
            deleteButton.addThemeVariants(ButtonVariant.LUMO_SMALL, ButtonVariant.LUMO_TERTIARY, ButtonVariant.LUMO_ERROR);
            deleteButton.addClickListener(e -> confirmDelete(user));
            deleteButton.getElement().setAttribute("title", "Delete");

            layout.add(editButton, deleteButton);
            return layout;
        })).setWidth("120px").setFlexGrow(0);

        grid.setSizeFull();
        return grid;
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
