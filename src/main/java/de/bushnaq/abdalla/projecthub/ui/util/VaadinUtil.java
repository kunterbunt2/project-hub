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

package de.bushnaq.abdalla.projecthub.ui.util;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.theme.lumo.LumoUtility;

/**
 * Utility class for common Vaadin UI components and layouts
 */
public final class VaadinUtil {
    public static String DIALOG_DEFAULT_WIDTH = "480px";

    /**
     * Creates a standardized dialog button layout with save and cancel buttons
     *
     * @param saveButtonText   Text for the save button (e.g., "Save", "Create", "Confirm")
     * @param saveButtonId     ID for the save button
     * @param cancelButtonText Text for the cancel button (e.g., "Cancel", "Close")
     * @param cancelButtonId   ID for the cancel button
     * @param saveClickHandler Click handler for the save button
     * @param dialog           The dialog instance that contains these buttons (for closing when cancel is clicked)
     * @return A configured HorizontalLayout containing the dialog buttons
     */
    public static HorizontalLayout createDialogButtonLayout(
            String saveButtonText,
            String saveButtonId,
            String cancelButtonText,
            String cancelButtonId,
            SaveButtonClickHandler saveClickHandler,
            Dialog dialog) {

        Button saveButton = new Button(saveButtonText, new Icon(VaadinIcon.CHECK));
        saveButton.setId(saveButtonId);
        saveButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        saveButton.addClickListener(event -> saveClickHandler.onClick());

        Button cancelButton = new Button(cancelButtonText, new Icon(VaadinIcon.CLOSE));
        cancelButton.setId(cancelButtonId);
        cancelButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
        cancelButton.addClickListener(event -> dialog.close());

        HorizontalLayout buttonLayout = new HorizontalLayout(saveButton, cancelButton);
        buttonLayout.setJustifyContentMode(FlexComponent.JustifyContentMode.END);
        buttonLayout.setWidthFull();
        buttonLayout.getStyle().set("margin-top", "var(--lumo-space-m)");

        return buttonLayout;
    }

    public static HorizontalLayout createDialogHeader(String title, String icon) {
        return createDialogHeader(title, new Icon(icon));
    }

    public static HorizontalLayout createDialogHeader(String title, VaadinIcon icon) {
        return createDialogHeader(title, new Icon(icon));
    }

    public static HorizontalLayout createDialogHeader(String title, Icon icon) {
        HorizontalLayout headerLayout = new HorizontalLayout();
        headerLayout.setAlignItems(FlexComponent.Alignment.CENTER);
        headerLayout.setSpacing(true);

        icon.getStyle().set("margin-right", "0.5em");

        H3 titleLabel = new H3(title);
        titleLabel.getStyle().set("margin", "0");

        headerLayout.add(icon, titleLabel);

        return headerLayout;
    }

    /**
     * Creates a standardized header layout with a title, icon, and create button
     *
     * @param title                    The title text to display
     * @param titleId                  ID for the title component
     * @param titleIcon                Icon to display next to the title
     * @param createButtonId           ID for the create button
     * @param createButtonClickHandler Click handler for the create button
     * @return A configured HorizontalLayout containing the header elements
     */
    public static HorizontalLayout createHeader(
            String title,
            String titleId,
            Icon titleIcon,
            String createButtonId,
            CreateButtonClickHandler createButtonClickHandler) {
        HorizontalLayout headerLayout = new HorizontalLayout();
        headerLayout.setWidthFull();
        headerLayout.setPadding(false);
        headerLayout.setAlignItems(FlexComponent.Alignment.CENTER);
        headerLayout.setJustifyContentMode(FlexComponent.JustifyContentMode.BETWEEN);

        // Create page title with icon
        HorizontalLayout titleLayout = new HorizontalLayout();
        titleLayout.setSpacing(true);
        titleLayout.setAlignItems(FlexComponent.Alignment.CENTER);

        H2 pageTitle = new H2(title);
        pageTitle.setId(titleId);
        pageTitle.addClassNames(
                LumoUtility.Margin.Top.MEDIUM,
                LumoUtility.Margin.Bottom.SMALL
        );

        titleLayout.add(titleIcon, pageTitle);

        Button createButton = new Button("Create", new Icon(VaadinIcon.PLUS));
        createButton.setId(createButtonId);
        createButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        createButton.addClickListener(e -> createButtonClickHandler.onClick());

        headerLayout.add(titleLayout, createButton);
        return headerLayout;
    }

    public static HorizontalLayout createHeader(
            String title,
            String titleId,
            String titleIcon,
            String createButtonId,
            CreateButtonClickHandler createButtonClickHandler) {
        return createHeader(title, titleId, new Icon(titleIcon), createButtonId, createButtonClickHandler);
    }

    public static HorizontalLayout createHeader(
            String title,
            String titleId,
            VaadinIcon titleIcon,
            String createButtonId,
            CreateButtonClickHandler createButtonClickHandler) {
        return createHeader(title, titleId, new Icon(titleIcon), createButtonId, createButtonClickHandler);
    }

    /**
     * Functional interface for create button click handlers
     */
    @FunctionalInterface
    public interface CreateButtonClickHandler {
        void onClick();
    }

    /**
     * Functional interface for dialog save button click handlers
     */
    @FunctionalInterface
    public interface SaveButtonClickHandler {
        void onClick();
    }
}
