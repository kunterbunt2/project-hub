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

import com.fasterxml.jackson.databind.ObjectMapper;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Main;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.provider.DataProvider;
import com.vaadin.flow.data.provider.ListDataProvider;
import com.vaadin.flow.data.value.ValueChangeMode;
import com.vaadin.flow.theme.lumo.LumoUtility;
import de.bushnaq.abdalla.projecthub.ai.AiFilter;
import de.bushnaq.abdalla.projecthub.ui.util.VaadinUtil;

import java.time.Clock;
import java.util.ArrayList;

public abstract class AbstractMainGrid<T> extends Main {
    protected       ListDataProvider<T> dataProvider;
    protected final Grid<T>             grid;
    private final   ObjectMapper        mapper;

    public AbstractMainGrid(Clock clock) {
        this(clock, null);
    }

    public AbstractMainGrid(Clock clock, ObjectMapper mapper) {
        this.mapper = mapper;
        setSizeFull();
        addClassNames(LumoUtility.BoxSizing.BORDER, LumoUtility.Display.FLEX, LumoUtility.FlexDirection.COLUMN);
        this.getStyle().set("padding-left", "var(--lumo-space-m)");
        this.getStyle().set("padding-right", "var(--lumo-space-m)");
        grid         = new Grid<>();
        dataProvider = new ListDataProvider<T>(new ArrayList<>());
        grid.setDataProvider(dataProvider);
        grid.setSizeFull();
        grid.addThemeVariants(com.vaadin.flow.component.grid.GridVariant.LUMO_NO_BORDER, com.vaadin.flow.component.grid.GridVariant.LUMO_NO_ROW_BORDERS);
        initGrid(clock);
    }

    /**
     * Creates a standardized header layout with a title, icon, and create button.
     * If a grid is provided, also includes a row counter showing filtered/total rows.
     *
     * @param title                    The title text to display
     * @param titleId                  ID for the title component
     * @param titleIcon                Icon to display next to the title
     * @param createButtonId           ID for the create button
     * @param createButtonClickHandler Click handler for the create button
     * @param grid                     Optional grid to count rows from (can be null)
     * @param rowCounterId             Optional ID for row counter (can be null if grid is null)
     * @return A configured HorizontalLayout containing the header elements
     */
    private static <T> HorizontalLayout createHeader(
            String title,
            String titleId,
            Icon titleIcon,
            String createButtonId,
            VaadinUtil.CreateButtonClickHandler createButtonClickHandler,
            Grid<T> grid,
            String rowCounterId) {

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

        // Right side with row counter (if grid provided) and create button
        HorizontalLayout rightLayout = new HorizontalLayout();
        rightLayout.setSpacing(true);
        rightLayout.setAlignItems(FlexComponent.Alignment.CENTER);

        // Add row counter if grid is provided
        if (grid != null && rowCounterId != null) {
            Span rowCounter = new Span();
            rowCounter.setId(rowCounterId);
            rowCounter.getStyle().set("margin-right", "var(--lumo-space-m)");
            rowCounter.getStyle().set("color", "var(--lumo-secondary-text-color)");

            // Initial counter update
            updateRowCounter(grid, rowCounter);

            DataProvider<T, ?> dataProvider = grid.getDataProvider();

            // Register a ComponentEventListener for grid's data changes, will be triggered when filtering
            grid.getDataProvider().addDataProviderListener(event -> {
                updateRowCounter(grid, rowCounter);
            });

            rightLayout.add(rowCounter);
        }

        Button createButton = new Button("Create", new Icon(VaadinIcon.PLUS));
        createButton.setId(createButtonId);
        createButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        createButton.addClickListener(e -> createButtonClickHandler.onClick());

        rightLayout.add(createButton);
        headerLayout.add(titleLayout, rightLayout);
        headerLayout.getStyle().set("padding-bottom", "var(--lumo-space-m)");
        return headerLayout;
    }

    /**
     * Convenience methods that use VaadinIcon instead of Icon
     */
    public static <T> HorizontalLayout createHeader(
            String title,
            String titleId,
            VaadinIcon titleIcon,
            String createButtonId,
            VaadinUtil.CreateButtonClickHandler createButtonClickHandler,
            Grid<T> grid,
            String rowCounterId) {
        return createHeader(title, titleId, new Icon(titleIcon), createButtonId, createButtonClickHandler, grid, rowCounterId);
    }

    /**
     * Convenience methods that use VaadinIcon instead of Icon
     */
    public <T> HorizontalLayout createHeader(
            String title,
            String titleId,
            VaadinIcon titleIcon,
            String createButtonId,
            VaadinUtil.CreateButtonClickHandler createButtonClickHandler,
            String rowCounterId) {
        return createHeader(title, titleId, new Icon(titleIcon), createButtonId, createButtonClickHandler, grid, rowCounterId);
    }

    /**
     * Convenience overload that uses VaadinIcon and doesn't require row counter
     */
    public static HorizontalLayout createHeader(
            String title,
            String titleId,
            VaadinIcon titleIcon,
            String createButtonId,
            VaadinUtil.CreateButtonClickHandler createButtonClickHandler) {
        return createHeader(title, titleId, new Icon(titleIcon), createButtonId, createButtonClickHandler, null, null);
    }

    /**
     * Creates a standardized header layout with a title, icon, global filter, and create button.
     * If a grid is provided, also includes a row counter showing filtered/total rows.
     *
     * @param title                    The title text to display
     * @param titleId                  ID for the title component
     * @param titleIcon                Icon to display next to the title
     * @param createButtonId           ID for the create button
     * @param createButtonClickHandler Click handler for the create button
     * @param grid                     Optional grid to count rows from (can be null)
     * @param rowCounterId             Optional ID for row counter (can be null if grid is null)
     * @param globalFilterId           ID for the global filter field
     * @param globalFilterFunction     Function to apply global filtering to items
     * @return A configured HorizontalLayout containing the header elements
     */
    private static <T> HorizontalLayout createHeader(
            String title,
            String titleId,
            Icon titleIcon,
            String createButtonId,
            VaadinUtil.CreateButtonClickHandler createButtonClickHandler,
            Grid<T> grid,
            String rowCounterId,
            String globalFilterId,
            java.util.function.Function<T, String> globalFilterFunction) {

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

        // Right side with global filter, row counter (if grid provided) and create button
        HorizontalLayout rightLayout = new HorizontalLayout();
        rightLayout.setSpacing(true);
        rightLayout.setAlignItems(FlexComponent.Alignment.CENTER);

        // Add global filter if provided
        if (globalFilterId != null && globalFilterFunction != null && grid != null) {
            TextField globalFilter = new TextField();
            globalFilter.setId(globalFilterId);
            globalFilter.setPlaceholder("Search...");
            globalFilter.setClearButtonVisible(true);
            globalFilter.setValueChangeMode(ValueChangeMode.EAGER);
            globalFilter.getStyle().set("margin-right", "var(--lumo-space-m)");
            globalFilter.setWidth("250px");

            // Add global filter change listener
            globalFilter.addValueChangeListener(e -> {
                ListDataProvider<T> dataProvider = (ListDataProvider<T>) grid.getDataProvider();
                String              filterValue  = e.getValue().toLowerCase().trim();

                if (filterValue.isEmpty()) {
                    dataProvider.clearFilters();
                } else {
                    dataProvider.setFilter(item -> {
                        String searchText = globalFilterFunction.apply(item);
                        return searchText != null && searchText.toLowerCase().contains(filterValue);
                    });
                }
            });

            rightLayout.add(globalFilter);
        }

        // Add row counter if grid is provided
        if (grid != null && rowCounterId != null) {
            Span rowCounter = new Span();
            rowCounter.setId(rowCounterId);
            rowCounter.getStyle().set("margin-right", "var(--lumo-space-m)");
            rowCounter.getStyle().set("color", "var(--lumo-secondary-text-color)");

            // Initial counter update
            updateRowCounter(grid, rowCounter);

            DataProvider<T, ?> dataProvider = grid.getDataProvider();

            // Register a ComponentEventListener for grid's data changes, will be triggered when filtering
            grid.getDataProvider().addDataProviderListener(event -> {
                updateRowCounter(grid, rowCounter);
            });

            rightLayout.add(rowCounter);
        }

        Button createButton = new Button("Create", new Icon(VaadinIcon.PLUS));
        createButton.setId(createButtonId);
        createButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        createButton.addClickListener(e -> createButtonClickHandler.onClick());

        rightLayout.add(createButton);
        headerLayout.add(titleLayout, rightLayout);
        headerLayout.getStyle().set("padding-bottom", "var(--lumo-space-m)");
        return headerLayout;
    }

    /**
     * Convenience methods that use VaadinIcon instead of Icon with global filter
     */
    public static <T> HorizontalLayout createHeader(
            String title,
            String titleId,
            VaadinIcon titleIcon,
            String createButtonId,
            VaadinUtil.CreateButtonClickHandler createButtonClickHandler,
            Grid<T> grid,
            String rowCounterId,
            String globalFilterId,
            java.util.function.Function<T, String> globalFilterFunction) {
        return createHeader(title, titleId, new Icon(titleIcon), createButtonId, createButtonClickHandler, grid, rowCounterId, globalFilterId, globalFilterFunction);
    }

    /**
     * Convenience methods that use VaadinIcon instead of Icon with global filter (instance method)
     */
    public HorizontalLayout createHeader(
            String title,
            String titleId,
            VaadinIcon titleIcon,
            String createButtonId,
            VaadinUtil.CreateButtonClickHandler createButtonClickHandler,
            String rowCounterId,
            String globalFilterId,
            java.util.function.Function<T, String> globalFilterFunction) {
        return createHeader(title, titleId, new Icon(titleIcon), createButtonId, createButtonClickHandler, grid, rowCounterId, globalFilterId, globalFilterFunction);
    }

    /**
     * Creates a standardized header layout with a title, icon, smart global filter, and create button.
     * The smart filter supports both simple text search and natural language queries.
     *
     * @param title                    The title text to display
     * @param titleId                  ID for the title component
     * @param titleIcon                Icon to display next to the title
     * @param createButtonId           ID for the create button
     * @param createButtonClickHandler Click handler for the create button
     * @param grid                     Optional grid to count rows from (can be null)
     * @param rowCounterId             Optional ID for row counter (can be null if grid is null)
     * @param globalFilterId           ID for the global filter field
     * @param nlSearchService          Natural language search service
     * @param mapper                   ObjectMapper for JSON serialization
     * @param entityType               The type of entity being searched (e.g., "Product", "Version")
     * @return A configured HorizontalLayout containing the header elements
     */
    private <T> HorizontalLayout createSmartHeader(
            String title,
            String titleId,
            Icon titleIcon,
            String createButtonId,
            VaadinUtil.CreateButtonClickHandler createButtonClickHandler,
            Grid<T> grid,
            String rowCounterId,
            String globalFilterId,
            AiFilter nlSearchService,
            ObjectMapper mapper,
            String entityType) {

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

        // Right side with smart global filter, row counter (if grid provided) and create button
        HorizontalLayout rightLayout = new HorizontalLayout();
        rightLayout.setSpacing(true);
        rightLayout.setAlignItems(FlexComponent.Alignment.CENTER);

        // Add smart global filter if provided
        if (globalFilterId != null && grid != null) {
            de.bushnaq.abdalla.projecthub.ui.component.SmartGlobalFilter<T> smartFilter =
                    new de.bushnaq.abdalla.projecthub.ui.component.SmartGlobalFilter<>(
                            globalFilterId,
                            grid,
                            nlSearchService,
                            mapper,
                            entityType
                    );
            smartFilter.getStyle().set("margin-right", "var(--lumo-space-m)");
            rightLayout.add(smartFilter);
        }

        // Add row counter if grid is provided
        if (grid != null && rowCounterId != null) {
            Span rowCounter = new Span();
            rowCounter.setId(rowCounterId);
            rowCounter.getStyle().set("margin-right", "var(--lumo-space-m)");
            rowCounter.getStyle().set("color", "var(--lumo-secondary-text-color)");

            // Initial counter update
            updateRowCounter(grid, rowCounter);

            DataProvider<T, ?> dataProvider = grid.getDataProvider();

            // Register a ComponentEventListener for grid's data changes, will be triggered when filtering
            grid.getDataProvider().addDataProviderListener(event -> {
                updateRowCounter(grid, rowCounter);
            });

            rightLayout.add(rowCounter);
        }

        Button createButton = new Button("Create", new Icon(VaadinIcon.PLUS));
        createButton.setId(createButtonId);
        createButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        createButton.addClickListener(e -> createButtonClickHandler.onClick());

        rightLayout.add(createButton);
        headerLayout.add(titleLayout, rightLayout);
        headerLayout.getStyle().set("padding-bottom", "var(--lumo-space-m)");
        return headerLayout;
    }

    /**
     * Convenience methods that use VaadinIcon instead of Icon with smart global filter
     */
    public <T> HorizontalLayout createSmartHeader(
            String title,
            String titleId,
            VaadinIcon titleIcon,
            String createButtonId,
            VaadinUtil.CreateButtonClickHandler createButtonClickHandler,
            Grid<T> grid,
            String rowCounterId,
            String globalFilterId,
            AiFilter nlSearchService,
            ObjectMapper mapper,
            String entityType) {
        return createSmartHeader(title, titleId, new Icon(titleIcon), createButtonId, createButtonClickHandler, grid, rowCounterId, globalFilterId, nlSearchService, mapper, entityType);
    }

    /**
     * Convenience methods that use VaadinIcon instead of Icon with smart global filter (instance method)
     */
    public HorizontalLayout createSmartHeader(
            String title,
            String titleId,
            VaadinIcon titleIcon,
            String createButtonId,
            VaadinUtil.CreateButtonClickHandler createButtonClickHandler,
            String rowCounterId,
            String globalFilterId,
            AiFilter nlSearchService,
            ObjectMapper mapper,
            String entityType) {
        return createSmartHeader(title, titleId, new Icon(titleIcon), createButtonId, createButtonClickHandler, grid, rowCounterId, globalFilterId, nlSearchService, mapper, entityType);
    }

    protected abstract void initGrid(Clock clock);

    /**
     * Updates a row counter component with the current visible row count vs total rows
     *
     * @param <T>        The type of items in the grid
     * @param grid       The grid to count rows from
     * @param rowCounter The Span component to update with the row count
     */
    private static <T> void updateRowCounter(Grid<T> grid, Span rowCounter) {
        DataProvider<T, ?> dataProvider = grid.getDataProvider();

        // Get counts based on the type of DataProvider
        int totalSize;
        int filteredSize;

        // Handle ListDataProvider
        ListDataProvider<T> listProvider = (ListDataProvider<T>) dataProvider;
        totalSize = listProvider.getItems().size();

        // Handle the case when no filter is applied yet (filter is null)
        if (listProvider.getFilter() == null) {
            filteredSize = totalSize;
        } else {
            filteredSize = listProvider.getItems().stream()
                    .filter(item -> listProvider.getFilter().test(item))
                    .toList().size();
        }

        String text = String.format("Showing %d of %d rows", filteredSize, totalSize);
        rowCounter.setText(text);
    }

}
