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
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Main;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.theme.lumo.LumoUtility;
import de.bushnaq.abdalla.projecthub.dto.Availability;
import de.bushnaq.abdalla.projecthub.dto.Location;
import de.bushnaq.abdalla.projecthub.dto.User;
import de.bushnaq.abdalla.projecthub.rest.api.UserApi;
import de.bushnaq.abdalla.projecthub.ui.MainLayout;
import jakarta.annotation.security.PermitAll;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.server.ResponseStatusException;
import org.vaadin.addons.tatu.ColorPicker;

import java.awt.*;
import java.util.List;

@Route(value = "profile/:user-email?", layout = MainLayout.class)
@PageTitle("User Profile")
@PermitAll
public class UserProfileView extends Main implements BeforeEnterObserver {
    public static final String      PROFILE_PAGE_TITLE  = "profile-page-title";
    public static final String      ROUTE               = "profile";
    public static final String      SAVE_PROFILE_BUTTON = "save-profile-button";
    public static final String      USER_COLOR_PICKER   = "user-color-picker";
    private             ColorPicker colorPicker;
    private             User        currentUser;
    private final       UserApi     userApi;

    public UserProfileView(UserApi userApi) {
        this.userApi = userApi;

        setSizeFull();
        addClassNames(
                LumoUtility.BoxSizing.BORDER,
                LumoUtility.Display.FLEX,
                LumoUtility.FlexDirection.COLUMN
        );
        getStyle().set("padding-left", "var(--lumo-space-m)");
        getStyle().set("padding-right", "var(--lumo-space-m)");
    }

    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        // Get username from URL parameter or use the currently authenticated user
        String userEmailParam = event.getRouteParameters().get("user-email").orElse(null);

        Authentication authentication  = SecurityContextHolder.getContext().getAuthentication();
        String         currentUsername = authentication != null ? authentication.getName() : null;

        // If no username is provided, use the current authenticated user
        final String userEmail = (userEmailParam == null && currentUsername != null) ? currentUsername : userEmailParam;

        if (userEmail != null) {
            try {
                // Find user by username using the direct getByEmail method
                currentUser = userApi.getByEmail(userEmail);
                initializeView();
            } catch (ResponseStatusException ex) {
                if (ex.getStatusCode().equals(HttpStatus.NOT_FOUND)) {
                    // Create a new user since one wasn't found
                    User newUser = createDefaultUser(userEmail);
                    currentUser = userApi.persist(newUser);
                    initializeView();
                    Notification notification = Notification.show("Created new user: " + userEmail, 3000, Notification.Position.MIDDLE);
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

    private void initializeView() {
        removeAll();

        // Create header
        HorizontalLayout headerLayout = new HorizontalLayout();
        headerLayout.setWidthFull();
        headerLayout.setPadding(false);
        headerLayout.setAlignItems(com.vaadin.flow.component.orderedlayout.FlexComponent.Alignment.CENTER);

        Icon titleIcon = new Icon(VaadinIcon.USER);
        H2   pageTitle = new H2("User Profile");
        pageTitle.setId(PROFILE_PAGE_TITLE);
        pageTitle.addClassNames(
                LumoUtility.Margin.Top.MEDIUM,
                LumoUtility.Margin.Bottom.SMALL
        );

        HorizontalLayout titleLayout = new HorizontalLayout(titleIcon, pageTitle);
        titleLayout.setSpacing(true);
        titleLayout.setAlignItems(com.vaadin.flow.component.orderedlayout.FlexComponent.Alignment.CENTER);

        headerLayout.add(titleLayout);
        headerLayout.getStyle().set("padding-bottom", "var(--lumo-space-m)");

        // Create form layout
        VerticalLayout formLayout = new VerticalLayout();
        formLayout.setPadding(true);
        formLayout.setSpacing(true);
        formLayout.setMaxWidth("600px");
        formLayout.getStyle().set("background-color", "var(--lumo-contrast-5pct)");
        formLayout.getStyle().set("border-radius", "var(--lumo-border-radius-m)");

        // User info (read-only)
        com.vaadin.flow.component.textfield.TextField nameField = new com.vaadin.flow.component.textfield.TextField("Name");
        nameField.setValue(currentUser.getName() != null ? currentUser.getName() : "");
        nameField.setReadOnly(true);
        nameField.setWidthFull();

        com.vaadin.flow.component.textfield.TextField emailField = new com.vaadin.flow.component.textfield.TextField("Email");
        emailField.setValue(currentUser.getEmail() != null ? currentUser.getEmail() : "");
        emailField.setReadOnly(true);
        emailField.setWidthFull();

        // Color picker (editable)
        colorPicker = new ColorPicker();
        colorPicker.setId(USER_COLOR_PICKER);
        colorPicker.setWidth("100%");
        colorPicker.setLabel("User Color");

        // Set predefined color presets
        colorPicker.setPresets(List.of(
                new ColorPicker.ColorPreset("#FF0000", "Red"),
                new ColorPicker.ColorPreset("#0000FF", "Blue"),
                new ColorPicker.ColorPreset("#008000", "Green"),
                new ColorPicker.ColorPreset("#FFFF00", "Yellow"),
                new ColorPicker.ColorPreset("#FFA500", "Orange"),
                new ColorPicker.ColorPreset("#800080", "Purple"),
                new ColorPicker.ColorPreset("#FFC0CB", "Pink"),
                new ColorPicker.ColorPreset("#00FFFF", "Cyan"),
                new ColorPicker.ColorPreset("#FF00FF", "Magenta"),
                new ColorPicker.ColorPreset("#D3D3D3", "Light Gray"),
                new ColorPicker.ColorPreset("#808080", "Gray"),
                new ColorPicker.ColorPreset("#A9A9A9", "Dark Gray"),
                new ColorPicker.ColorPreset("#000000", "Black")
        ));

        // Set current color
        if (currentUser.getColor() != null) {
            String colorHex = String.format("#%06X", (0xFFFFFF & currentUser.getColor().getRGB()));
            colorPicker.setValue(colorHex);
        }

        // Save button
        Button saveButton = new Button("Save Changes", new Icon(VaadinIcon.CHECK));
        saveButton.setId(SAVE_PROFILE_BUTTON);
        saveButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        saveButton.addClickListener(e -> saveProfile());

        formLayout.add(nameField, emailField, colorPicker, saveButton);

        add(headerLayout, formLayout);
    }

    private void saveProfile() {
        try {
            // Convert Vaadin color string to AWT Color
            String colorValue = colorPicker.getValue();
            if (colorValue != null && !colorValue.isEmpty()) {
                currentUser.setColor(Color.decode(colorValue));
            }

            // Save user
            userApi.persist(currentUser);

            Notification notification = Notification.show("Profile updated successfully", 3000, Notification.Position.MIDDLE);
            notification.addThemeVariants(NotificationVariant.LUMO_SUCCESS);
        } catch (Exception ex) {
            Notification notification = Notification.show("Failed to update profile: " + ex.getMessage(), 3000, Notification.Position.MIDDLE);
            notification.addThemeVariants(NotificationVariant.LUMO_ERROR);
        }
    }
}

