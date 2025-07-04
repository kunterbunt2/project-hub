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
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.theme.lumo.LumoUtility;

/**
 * Utility class for common Vaadin UI components and layouts
 */
public final class VaadinUtils {

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
    public static HorizontalLayout createHeader(String title, String titleId, VaadinIcon titleIcon, String createButtonId, CreateButtonClickHandler createButtonClickHandler) {
        HorizontalLayout headerLayout = new HorizontalLayout();
        headerLayout.setWidthFull();
        headerLayout.setPadding(false);
        headerLayout.setAlignItems(FlexComponent.Alignment.CENTER);
        headerLayout.setJustifyContentMode(FlexComponent.JustifyContentMode.BETWEEN);

        // Create page title with icon
        HorizontalLayout titleLayout = new HorizontalLayout();
        titleLayout.setSpacing(true);
        titleLayout.setAlignItems(FlexComponent.Alignment.CENTER);

        Icon productIcon = new Icon(titleIcon);
        H2   pageTitle   = new H2(title);
        pageTitle.setId(titleId);
        pageTitle.addClassNames(
                LumoUtility.Margin.Top.MEDIUM,
                LumoUtility.Margin.Bottom.SMALL
        );

        titleLayout.add(productIcon, pageTitle);

        Button createButton = new Button("Create", new Icon(VaadinIcon.PLUS));
        createButton.setId(createButtonId);
        createButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        createButton.addClickListener(e -> createButtonClickHandler.onClick());

        headerLayout.add(titleLayout, createButton);
        return headerLayout;
    }

    /**
     * Functional interface for create button click handlers
     */
    @FunctionalInterface
    public interface CreateButtonClickHandler {
        void onClick();
    }
}
