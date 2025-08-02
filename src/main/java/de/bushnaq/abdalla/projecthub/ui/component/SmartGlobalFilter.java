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

package de.bushnaq.abdalla.projecthub.ui.component;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.provider.ListDataProvider;
import com.vaadin.flow.data.value.ValueChangeMode;
import de.bushnaq.abdalla.projecthub.service.NaturalLanguageSearchService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.regex.Pattern;

/**
 * Enhanced global filter component that supports both simple text search and natural language queries.
 * This component integrates with NaturalLanguageSearchService to parse complex search queries.
 */
public class SmartGlobalFilter<T> extends HorizontalLayout {

    private static final Logger                       logger = LoggerFactory.getLogger(SmartGlobalFilter.class);
    private final        Grid<T>                      grid;
    private final        Button                       helpButton;
    private final        ObjectMapper                 mapper;
    private final        NaturalLanguageSearchService nlSearchService;
    private final        TextField                    searchField;
    private final        Span                         statusSpan;

    public SmartGlobalFilter(String fieldId,
                             Grid<T> grid,
                             NaturalLanguageSearchService nlSearchService, ObjectMapper mapper) {

        this.grid            = grid;
        this.nlSearchService = nlSearchService;
        this.mapper          = mapper;

        setAlignItems(FlexComponent.Alignment.CENTER);
        setSpacing(true);

        // Create search field
        searchField = new TextField();
        searchField.setId(fieldId);
        searchField.setPlaceholder("Search... (try: 'products created after January 2024', press Enter to search)");
        searchField.setClearButtonVisible(true);
        searchField.setValueChangeMode(ValueChangeMode.ON_CHANGE); // Use ON_CHANGE but we'll control when to actually search
        searchField.setWidth("350px");

        // Create help button
        helpButton = new Button(new Icon(VaadinIcon.QUESTION_CIRCLE));
        helpButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY, ButtonVariant.LUMO_SMALL);
        helpButton.getElement().setAttribute("title", "Search Help");
        helpButton.addClickListener(e -> showSearchHelp());

        // Create status span for feedback
        statusSpan = new Span();
        statusSpan.getStyle().set("font-size", "var(--lumo-font-size-s)");
        statusSpan.getStyle().set("color", "var(--lumo-secondary-text-color)");
        statusSpan.setVisible(false);

        // Add search listener - use a more reliable approach for Enter key
        searchField.getElement().addEventListener("keydown", event -> {
            if ("Enter".equals(event.getEventData().getString("event.key"))) {
                performSearch();
            }
        }).addEventData("event.key");

        // Also trigger when field loses focus (blur) if there's a value
        searchField.addBlurListener(e -> {
            if (searchField.getValue() != null && !searchField.getValue().trim().isEmpty()) {
                performSearch();
            }
        });

        // Handle clear button - immediately clear filters when field is cleared
        searchField.addValueChangeListener(e -> {
            if (e.getValue() == null || e.getValue().trim().isEmpty()) {
                clearFilters();
            }
            // Don't trigger search on value change - only on Enter or blur
        });

        add(searchField, helpButton, statusSpan);
    }

    private void applySearchQuery(String regexPattern) {
        try {
            Pattern             pattern      = Pattern.compile(regexPattern);
            ListDataProvider<T> dataProvider = (ListDataProvider<T>) grid.getDataProvider();
            dataProvider.setFilter(item -> {
                        try {
                            String json = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(item);

                            // Apply the LLM-generated regex pattern
                            return pattern.matcher(json).find();
                        } catch (JsonProcessingException e) {
                            logger.error("Error serializing item to JSON for filtering", e);
                            return false;
                        }
                    }
            );
        } catch (Exception e) {
            logger.error("Invalid regex pattern '{}', falling back to simple text search: {}", regexPattern, e.getMessage());
        }

    }

    private void clearFilters() {
        ListDataProvider<T> dataProvider = (ListDataProvider<T>) grid.getDataProvider();
        dataProvider.clearFilters();
        statusSpan.setVisible(false);
    }

    public TextField getSearchField() {
        return searchField;
    }

    private void onSearchValueChange(com.vaadin.flow.component.AbstractField.ComponentValueChangeEvent<TextField, String> event) {
        String searchValue = event.getValue();

        if (searchValue == null || searchValue.trim().isEmpty()) {
            clearFilters();
            return;
        }

        try {
            // Parse the query using natural language service
            String searchQuery = nlSearchService.parseQuery(searchValue);

            // Apply the parsed search criteria
            applySearchQuery(searchQuery);

            // Show feedback to user about what was understood
            showSearchFeedback(searchQuery, searchValue);

        } catch (Exception e) {
            logger.error("Error processing search query: {}", searchValue, e);
            showErrorFeedback();
        }
    }

    private void performSearch() {
        String searchValue = searchField.getValue();

        if (searchValue == null || searchValue.trim().isEmpty()) {
            clearFilters();
            return;
        }

        try {
            // Parse the query using natural language service
            String searchQuery = nlSearchService.parseQuery(searchValue);

            // Apply the parsed search criteria
            applySearchQuery(searchQuery);

            // Show feedback to user about what was understood
            showSearchFeedback(searchQuery, searchValue);

        } catch (Exception e) {
            logger.error("Error processing search query: {}", searchValue, e);
            showErrorFeedback();
        }
    }

    private void showErrorFeedback() {
        statusSpan.setText("Using simple text search");
        statusSpan.getStyle().set("color", "var(--lumo-error-color)");
        statusSpan.setVisible(true);
    }

    private void showSearchFeedback(String searchQuery, String originalQuery) {
        StringBuilder feedback = new StringBuilder();

        feedback.append("Using pattern: '").append(searchQuery).append("'");

        if (!feedback.isEmpty()) {
            statusSpan.setText(feedback.toString().trim());
            statusSpan.getStyle().set("color", "var(--lumo-success-color)");
            statusSpan.setVisible(true);
        } else {
            statusSpan.setVisible(false);
        }
    }

    private void showSearchHelp() {
        String helpText = """
                Smart Search Examples:
                
                • Simple: "Product ABC" → searches everywhere
                • Column-specific: "name:Product" or "key:PROJ-123"
                • Natural language: "products created after January 2024"
                • Date ranges: "items created before 2024-12-01"
                • Combined: "name contains project created after january"
                
                Available columns: name, key, created, updated
                """;

        Notification notification = new Notification();
        notification.addThemeVariants(NotificationVariant.LUMO_PRIMARY);
        notification.setDuration(8000);
        notification.setPosition(Notification.Position.TOP_CENTER);

        Span content = new Span(helpText);
        content.getStyle().set("white-space", "pre-line");
        notification.add(content);

        notification.open();
    }
}
