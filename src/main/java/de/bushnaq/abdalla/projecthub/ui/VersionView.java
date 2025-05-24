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

import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Main;
import com.vaadin.flow.router.BeforeEvent;
import com.vaadin.flow.router.HasUrlParameter;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.theme.lumo.LumoUtility;
import de.bushnaq.abdalla.projecthub.api.VersionApi;
import de.bushnaq.abdalla.projecthub.dto.Version;
import de.bushnaq.abdalla.projecthub.ui.view.ProjectView;
import jakarta.annotation.security.PermitAll;

import java.time.Clock;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;

@Route("version1")
@PageTitle("Version Page")
//@Menu(order = 1, icon = "vaadin:factory", title = "version List")
@PermitAll // When security is enabled, allow all authenticated users
public class VersionView extends Main implements HasUrlParameter<Long> {
    public static final String        ROUTE = "version1";
    final               Grid<Version> grid;
    H2 pageTitle;
    private Long productId;
    VersionApi versionApi;

    public VersionView(VersionApi versionApi, Clock clock) {
        this.versionApi = versionApi;

        pageTitle = new H2("Versions");
        pageTitle.addClassNames(
                LumoUtility.Margin.Top.MEDIUM,
                LumoUtility.Margin.Bottom.SMALL
        );
        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofLocalizedDateTime(FormatStyle.LONG).withZone(clock.getZone()).withLocale(getLocale());

        grid = new Grid<>();
        // Only show versions for the selected product
        if (productId != null) {
            grid.setItems(versionApi.getAll(productId));
        } else {
            grid.setItems(versionApi.getAll());
        }
        grid.addColumn(Version::getKey).setHeader("Key");
        grid.addColumn(Version::getName).setHeader("Name");
        grid.addColumn(version -> dateTimeFormatter.format(version.getCreated())).setHeader("Created");
        grid.addColumn(version -> dateTimeFormatter.format(version.getUpdated())).setHeader("Updated");
        grid.setSizeFull();
        // Add click listener to navigate to ProjectView with the selected version ID
        grid.addItemClickListener(event -> {
            Version selectedVersion = event.getItem();
            UI.getCurrent().navigate(ProjectView.class, selectedVersion.getId());
        });

        setSizeFull();
        addClassNames(LumoUtility.BoxSizing.BORDER, LumoUtility.Display.FLEX, LumoUtility.FlexDirection.COLUMN);

        add(pageTitle, grid);
    }

    @Override
    public void setParameter(BeforeEvent beforeEvent, Long productId) {
        this.productId = productId;
        pageTitle.setText("Versions of Product ID: " + productId);

    }
}
