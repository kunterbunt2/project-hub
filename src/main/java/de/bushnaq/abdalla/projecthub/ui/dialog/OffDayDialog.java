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
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.data.binder.ValidationException;
import de.bushnaq.abdalla.projecthub.dto.OffDay;
import de.bushnaq.abdalla.projecthub.dto.OffDayType;
import de.bushnaq.abdalla.projecthub.dto.User;
import de.bushnaq.abdalla.projecthub.rest.api.OffDayApi;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;

public class OffDayDialog extends Dialog {
    public static final String               CANCEL_BUTTON           = "offday-dialog-cancel";
    public static final String               CONFIRM_BUTTON          = "offday-dialog-confirm";
    public static final String               OFFDAY_DIALOG           = "offday-dialog";
    public static final String               OFFDAY_END_DATE_FIELD   = "offday-end-date-field";
    public static final String               OFFDAY_START_DATE_FIELD = "offday-start-date-field";
    public static final String               OFFDAY_TYPE_FIELD       = "offday-type-field";
    private final       Binder<OffDay>       binder                  = new Binder<>(OffDay.class);
    private final       DatePicker           firstDayField           = new DatePicker("First Day");
    private final       boolean              isNewOffDay;
    private final       DatePicker           lastDayField            = new DatePicker("Last Day");
    private final       OffDay               offDay;
    private final       OffDayApi            offDayApi;
    private final       Runnable             onSaveCallback;
    private final       ComboBox<OffDayType> typeField               = new ComboBox<>("Type");
    private final       User                 user;

    public OffDayDialog(OffDay offDay, User user, OffDayApi offDayApi, Runnable onSaveCallback) {
        this.offDayApi      = offDayApi;
        this.user           = user;
        this.onSaveCallback = onSaveCallback;
        this.isNewOffDay    = (offDay == null);
        this.offDay         = isNewOffDay ? new OffDay(LocalDate.now(), LocalDate.now(), OffDayType.VACATION) : offDay;

        setId(OFFDAY_DIALOG);
        setCloseOnEsc(true);
        setCloseOnOutsideClick(false);
        setDraggable(true);
        setResizable(true);
        setWidth("400px");

        // Setup form and actions
        add(createHeader(), createForm(), createButtonLayout());
        configureFormBinder();
    }

    private void configureFormBinder() {
        // First day binding
        binder.forField(firstDayField)
                .asRequired("First day is required")
                .withValidator(date -> date == null || lastDayField.getValue() == null ||
                                !date.isAfter(lastDayField.getValue()),
                        "First day must be before or equal to last day")
                .bind(OffDay::getFirstDay, OffDay::setFirstDay);

        // Last day binding
        binder.forField(lastDayField)
                .asRequired("Last day is required")
                .withValidator(date -> date == null || firstDayField.getValue() == null ||
                                !date.isBefore(firstDayField.getValue()),
                        "Last day must be after or equal to first day")
                .bind(OffDay::getLastDay, OffDay::setLastDay);

        // Type binding
        binder.forField(typeField)
                .asRequired("Type is required")
                .bind(OffDay::getType, OffDay::setType);

        // Load data into the form
        if (!isNewOffDay) {
            binder.readBean(offDay);
        }
    }

    private HorizontalLayout createButtonLayout() {
        Button saveButton = new Button("Save", new Icon(VaadinIcon.CHECK));
        saveButton.setId(CONFIRM_BUTTON);
        saveButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        saveButton.addClickListener(event -> saveOffDay());

        Button cancelButton = new Button("Cancel", new Icon(VaadinIcon.CLOSE));
        cancelButton.setId(CANCEL_BUTTON);
        cancelButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
        cancelButton.addClickListener(event -> close());

        HorizontalLayout buttonLayout = new HorizontalLayout(saveButton, cancelButton);
        buttonLayout.setJustifyContentMode(FlexComponent.JustifyContentMode.END);
        buttonLayout.setWidthFull();
        buttonLayout.getStyle().set("margin-top", "var(--lumo-space-m)");

        return buttonLayout;
    }

    private FormLayout createForm() {
        FormLayout formLayout = new FormLayout();

        // Configure fields
        firstDayField.setWidthFull();
        firstDayField.setHelperText("The first day of the off period");
        firstDayField.setId(OFFDAY_START_DATE_FIELD);

        lastDayField.setWidthFull();
        lastDayField.setHelperText("The last day of the off period");
        lastDayField.setId(OFFDAY_END_DATE_FIELD);

        typeField.setWidthFull();
        typeField.setHelperText("Type of off day");
        typeField.setItems(OffDayType.values());
        typeField.setId(OFFDAY_TYPE_FIELD);

        // Add validation for date range relationship
        firstDayField.addValueChangeListener(e -> lastDayField.setMin(e.getValue()));
        lastDayField.addValueChangeListener(e -> firstDayField.setMax(e.getValue()));

        formLayout.add(typeField, firstDayField, lastDayField);
        return formLayout;
    }

    private H3 createHeader() {
        String title  = isNewOffDay ? "Add New Off Day" : "Edit Off Day";
        H3     header = new H3(title);
        header.getStyle().set("margin-top", "0");
        return header;
    }

    private void saveOffDay() {
        try {
            // Validate all fields first - this will trigger validation and show error messages
            if (!binder.validate().isOk()) {
                return; // Stop here if validation fails
            }

            // Write values to bean
            binder.writeBean(offDay);

            // Ensure user association is set
            offDay.setUser(user);

            // Save to backend
            if (isNewOffDay) {
                offDayApi.persist(offDay, user.getId());
            } else {
                offDayApi.update(offDay, user.getId());
            }
            // Notify success
            String message = isNewOffDay ? "New off day record added" : "Off day record updated";
            Notification.show(message, 3000, Notification.Position.MIDDLE);

            // Trigger refresh callback and close dialog
            onSaveCallback.run();
            close();
        } catch (ValidationException e) {
            System.out.printf("Validation failed: %s%n", e.getMessage());
            // Validation errors will already be displayed next to the fields
        } catch (Exception e) {
            System.out.printf("Validation failed: %s%n", e.getMessage());
            if (e instanceof ResponseStatusException && ((ResponseStatusException) e).getStatusCode().equals(HttpStatus.CONFLICT)) {
                String errorMessage = ((ResponseStatusException) e).getReason();
                firstDayField.setInvalid(errorMessage != null);
                firstDayField.setErrorMessage(errorMessage);
                lastDayField.setInvalid(errorMessage != null);
                lastDayField.setErrorMessage(errorMessage);
                // Keep the dialog open so the user can correct the name
            } else {
                // For other errors, show generic message and close dialog
                Notification notification = Notification.show("Error saving off day: " + e.getMessage(), 3000, Notification.Position.MIDDLE);
                notification.addThemeVariants(NotificationVariant.LUMO_ERROR);
                notification.open();
                // dialogReference.close();
                // Keep the dialog open so the user can correct the name
            }
            // Only show notification for unexpected errors
        }
    }
}
