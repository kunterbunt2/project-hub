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

package de.bushnaq.abdalla.projecthub.ui.dialog;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import de.bushnaq.abdalla.projecthub.dto.Location;
import de.bushnaq.abdalla.projecthub.dto.User;
import de.bushnaq.abdalla.projecthub.rest.api.LocationApi;
import de.focus_shift.jollyday.core.HolidayManager;
import de.focus_shift.jollyday.core.ManagerParameters;

import java.time.LocalDate;
import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;

public class LocationDialog extends Dialog {
    public static final String           CANCEL_BUTTON             = "location-dialog-cancel";
    public static final String           CONFIRM_BUTTON            = "location-dialog-confirm";
    public static final String           LOCATION_COUNTRY_FIELD    = "location-country-field";
    // Static IDs for UI elements to aid in testing
    public static final String           LOCATION_START_DATE_FIELD = "location-start-date-field";
    public static final String           LOCATION_STATE_FIELD      = "location-state-field";
    private final       ComboBox<String> countryComboBox           = new ComboBox<>("Country");
    private             Location         location;
    private final       LocationApi      locationApi;
    private final       Consumer<Void>   onSaveCallback;
    private final       DatePicker       startDatePicker           = new DatePicker("Start Date");
    private final       ComboBox<String> stateComboBox             = new ComboBox<>("State/Region");
    private final       User             user;

    public LocationDialog(Location location, User user, LocationApi locationApi, Consumer<Void> onSaveCallback) {
        this.location       = location;
        this.user           = user;
        this.locationApi    = locationApi;
        this.onSaveCallback = onSaveCallback;

        setWidth("500px");
        setHeaderTitle(location == null ? "Add New Location" : "Edit Location");

        VerticalLayout content = createDialogContent();
        add(content);

        initFormValues();
    }

    private HorizontalLayout createButtonLayout() {
        Button saveButton = new Button("Save");
        saveButton.setId(CONFIRM_BUTTON);
        saveButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        saveButton.addClickListener(event -> save());

        Button cancelButton = new Button("Cancel");
        cancelButton.setId(CANCEL_BUTTON);
        cancelButton.addClickListener(event -> close());

        HorizontalLayout buttonLayout = new HorizontalLayout(saveButton, cancelButton);
        buttonLayout.setJustifyContentMode(FlexComponent.JustifyContentMode.END);
        buttonLayout.setWidthFull();

        return buttonLayout;
    }

    private VerticalLayout createDialogContent() {
        VerticalLayout content = new VerticalLayout();
        content.setPadding(false);
        content.setSpacing(true);
        content.setAlignItems(FlexComponent.Alignment.STRETCH);

        // Configure country combobox with ISO 3166-1 alpha-2 values
        countryComboBox.setId(LOCATION_COUNTRY_FIELD);
        countryComboBox.setRequired(true);
        countryComboBox.setAllowCustomValue(false);
        populateCountries();

        // Country change listener to update states
        countryComboBox.addValueChangeListener(event -> {
            if (event.getValue() != null) {
                populateStates(event.getValue());
            } else {
                stateComboBox.setItems(new HashSet<>());
                stateComboBox.setEnabled(false);
            }
        });

        // Configure state/region combobox
        stateComboBox.setId(LOCATION_STATE_FIELD);
        stateComboBox.setRequired(true);
        stateComboBox.setAllowCustomValue(false);
        stateComboBox.setEnabled(false); // Initially disabled until country is selected

        // Configure start date picker
        startDatePicker.setId(LOCATION_START_DATE_FIELD);
        startDatePicker.setRequired(true);
        startDatePicker.setMax(LocalDate.now().plusYears(10));

        content.add(countryComboBox, stateComboBox, startDatePicker, createButtonLayout());
        return content;
    }

    private void initFormValues() {
        if (location != null) {
            // Edit existing location
            countryComboBox.setValue(location.getCountry());
            stateComboBox.setValue(location.getState());
            startDatePicker.setValue(location.getStart());
        } else {
            // Set default values for new location
            startDatePicker.setValue(LocalDate.now());
        }
    }

    private void populateCountries() {
        // Get list of supported countries from jollyday
        Set<String> countryCodeSet = new HashSet<>();
        for (String countryCode : HolidayManager.getSupportedCalendarCodes()) {
            countryCodeSet.add(countryCode);
        }

        // Convert to list and sort by display country name
        List<String> sortedCountryCodes = countryCodeSet.stream()
                .sorted((code1, code2) -> {
                    Locale locale1 = new Locale("", code1);
                    Locale locale2 = new Locale("", code2);
                    return locale1.getDisplayCountry().compareTo(locale2.getDisplayCountry());
                })
                .collect(Collectors.toList());

        countryComboBox.setItems(sortedCountryCodes);

        // Set custom label generator to display country name
        countryComboBox.setItemLabelGenerator(countryCode -> {
            Locale locale = new Locale("", countryCode);
            return locale.getDisplayCountry() + " (" + countryCode + ")";
        });
    }

    private void populateStates(String countryCode) {
        // Get list of supported states for the selected country from jollyday
        Set<String> stateCodeSet = new HashSet<>();
        try {
            HolidayManager manager = HolidayManager.getInstance(ManagerParameters.create(countryCode));
            stateCodeSet.addAll(manager.getCalendarHierarchy().getChildren().keySet());

            // If there are no states, add an empty one to represent the whole country
            if (stateCodeSet.isEmpty()) {
                stateCodeSet.add(countryCode);
                stateComboBox.setItems(stateCodeSet);
            } else {
                // Create a map of descriptions to state codes
                Map<String, String> stateDescriptionMap = null;

                // Function to get the best description for a state
                Function<String, String> getStateDescription = stateCode -> {
                    if (stateCode.equals(countryCode)) {
                        return "All of " + new Locale("", countryCode).getDisplayCountry();
                    }

                    try {
                        String description = manager.getCalendarHierarchy().getChildren().get(stateCode).getDescription();
                        return description;
                    } catch (Exception e) {
                        // If we can't get the description, just use the code
                    }

                    return stateCode;
                };

                // Sort state codes by their descriptions
                List<String> sortedStateCodes = stateCodeSet.stream()
                        .sorted((code1, code2) -> {
                            String desc1 = getStateDescription.apply(code1);
                            String desc2 = getStateDescription.apply(code2);
                            return desc1.compareTo(desc2);
                        })
                        .collect(Collectors.toList());

                stateComboBox.setItems(sortedStateCodes);
            }

            stateComboBox.setEnabled(true);

            // Set custom label generator for state names
            stateComboBox.setItemLabelGenerator(stateCode -> {
                if (stateCode.equals(countryCode)) {
                    return "All of " + new Locale("", countryCode).getDisplayCountry();
                }

                // Try to get a more descriptive name from CalendarHierarchy
                try {
                    String description = manager.getCalendarHierarchy().getChildren().get(stateCode).getDescription();
                    if (description != null && !description.isEmpty()) {
                        return description + " (" + stateCode + ")";
                    }
                } catch (Exception e) {
                    // If we can't get the description, just use the code
                }

                return stateCode;
            });
        } catch (Exception e) {
            Notification notification = Notification.show(
                    "Error loading regions for " + countryCode + ": " + e.getMessage(),
                    3000,
                    Notification.Position.MIDDLE);
            notification.addThemeVariants(NotificationVariant.LUMO_ERROR);
            stateComboBox.setEnabled(false);
        }
    }

    private void save() {
        if (!validateForm()) {
            return;
        }

        try {
            boolean isNewLocation = (location == null);
            if (isNewLocation) {
                location = new Location();
            }

            // Set values from form
            location.setCountry(countryComboBox.getValue());
            location.setState(stateComboBox.getValue());
            location.setStart(startDatePicker.getValue());

            if (isNewLocation) {
                locationApi.persist(location, user.getId());
                Notification.show("Location added", 3000, Notification.Position.MIDDLE);
            } else {
                locationApi.update(location, user.getId());
                Notification.show("Location updated", 3000, Notification.Position.MIDDLE);
            }

            // Call the callback
            onSaveCallback.accept(null);
            close();
        } catch (Exception e) {
            Notification notification = Notification.show(
                    "Error saving location: " + e.getMessage(),
                    3000,
                    Notification.Position.MIDDLE);
            notification.addThemeVariants(NotificationVariant.LUMO_ERROR);
        }
    }

    private boolean validateForm() {
        boolean isValid = true;

        // Validate country
        if (countryComboBox.getValue() == null || countryComboBox.getValue().isEmpty()) {
            countryComboBox.setInvalid(true);
            isValid = false;
        } else {
            countryComboBox.setInvalid(false);
        }

        // Validate state
        if (stateComboBox.getValue() == null || stateComboBox.getValue().isEmpty()) {
            stateComboBox.setInvalid(true);
            isValid = false;
        } else {
            stateComboBox.setInvalid(false);
        }

        // Validate start date
        if (startDatePicker.getValue() == null) {
            startDatePicker.setInvalid(true);
            isValid = false;
        } else {
            // Check if this start date already exists for the user (unless it's the current location being edited)
            if (location != null && user.getLocations().stream()
                    .filter(loc -> !loc.getId().equals(location.getId()))
                    .anyMatch(loc -> loc.getStart().equals(startDatePicker.getValue()))) {
                startDatePicker.setInvalid(true);
                Notification notification = Notification.show("A location with this start date already exists", 3000, Notification.Position.MIDDLE);
                notification.addThemeVariants(NotificationVariant.LUMO_ERROR);
                isValid = false;
            } else if (location == null && user.getLocations().stream().anyMatch(loc -> loc.getStart().equals(startDatePicker.getValue()))) {
                startDatePicker.setInvalid(true);
                Notification notification = Notification.show("A location with this start date already exists", 3000, Notification.Position.MIDDLE);
                notification.addThemeVariants(NotificationVariant.LUMO_ERROR);
                isValid = false;
            } else {
                startDatePicker.setInvalid(false);
            }
        }

        return isValid;
    }
}
