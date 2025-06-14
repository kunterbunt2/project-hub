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

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.applayout.AppLayout;
import com.vaadin.flow.component.avatar.Avatar;
import com.vaadin.flow.component.avatar.AvatarVariant;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.menubar.MenuBar;
import com.vaadin.flow.component.menubar.MenuBarVariant;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.tabs.Tab;
import com.vaadin.flow.component.tabs.Tabs;
import com.vaadin.flow.router.Layout;
import com.vaadin.flow.server.menu.MenuConfiguration;
import com.vaadin.flow.server.menu.MenuEntry;
import de.bushnaq.abdalla.projecthub.security.SecurityUtils;
import de.bushnaq.abdalla.projecthub.ui.component.Breadcrumbs;
import jakarta.annotation.security.PermitAll;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;

import java.util.HashMap;
import java.util.Map;

import static com.vaadin.flow.theme.lumo.LumoUtility.*;

@Layout
@PermitAll // When security is enabled, allow all authenticated users
public final class MainLayout extends AppLayout {

    private final Breadcrumbs      breadcrumbs  = new Breadcrumbs();
    private final Map<Tab, String> tabToPathMap = new HashMap<>();
    private final Tabs             tabs         = new Tabs();

    MainLayout() {
        setPrimarySection(Section.NAVBAR);

        // Create main navigation bar components
        HorizontalLayout navbarLayout = new HorizontalLayout();
        navbarLayout.setWidthFull();
        navbarLayout.setJustifyContentMode(FlexComponent.JustifyContentMode.BETWEEN);
        navbarLayout.setAlignItems(FlexComponent.Alignment.CENTER);

        // Add logo and app name to the left
        HorizontalLayout logoLayout = createHeader();

        // Add navigation tabs to the center
        createNavTabs();
        tabs.addClassNames(Margin.Horizontal.MEDIUM);
        ThemeToggle themeToggle = new ThemeToggle();

        // Add user menu to the right
        Component userMenu = createUserMenu();

        navbarLayout.add(logoLayout, tabs, themeToggle, userMenu);
        navbarLayout.expand(tabs);

        Div breadcrumbContainer = new Div(breadcrumbs);
        breadcrumbContainer.addClassNames(
                Padding.Horizontal.MEDIUM,
                Padding.Vertical.XSMALL,
                Width.FULL,
                Background.CONTRAST_5
        );

        var navAndBreadcrumbs = new VerticalLayout();
        navAndBreadcrumbs.setPadding(false);
        navAndBreadcrumbs.setSpacing(false);
        navAndBreadcrumbs.setMargin(false);

        navAndBreadcrumbs.add(breadcrumbContainer, navbarLayout);

        // Add the combined layout to the navbar area
        addToNavbar(true, navAndBreadcrumbs);
    }

    private HorizontalLayout createHeader() {
        // App logo
        var appLogo = VaadinIcon.CUBES.create();
        appLogo.addClassNames(TextColor.PRIMARY, IconSize.MEDIUM);

        // App name
        var appName = new H1("Project Hub");
        appName.addClassNames(FontWeight.SEMIBOLD, FontSize.MEDIUM, Margin.NONE);

        // Combine in a layout
        var logoLayout = new HorizontalLayout(appLogo, appName);
        logoLayout.addClassNames(Padding.SMALL, Gap.SMALL, AlignItems.CENTER);

        return logoLayout;
    }

    private void createNavTabs() {
        tabs.setOrientation(Tabs.Orientation.HORIZONTAL);

        MenuConfiguration.getMenuEntries().forEach(entry -> {
            Tab tab = createTab(entry);
            tabToPathMap.put(tab, entry.path());
            tabs.add(tab);
        });

        tabs.addSelectedChangeListener(event -> {
            Tab    selectedTab = event.getSelectedTab();
            String targetPath  = tabToPathMap.get(selectedTab);
            if (targetPath != null && !targetPath.isEmpty()) {
                getUI().ifPresent(ui -> ui.navigate(targetPath));
            }
        });
    }

    private Tab createTab(MenuEntry menuEntry) {
        Icon icon = null;
        if (menuEntry.icon() != null) {
            icon = new Icon(menuEntry.icon());
            icon.addClassNames(Margin.Right.XSMALL);
        }

        Span text = new Span(menuEntry.title());

        if (icon != null) {
            return new Tab(new HorizontalLayout(icon, text));
        } else {
            return new Tab(text);
        }
    }

    private Component createUserMenu() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String         username       = authentication != null ? authentication.getName() : "Guest";

        var avatar = new Avatar(username);
        avatar.addThemeVariants(AvatarVariant.LUMO_XSMALL);
        avatar.addClassNames(Margin.Right.SMALL);
        avatar.setColorIndex(5);

        var userMenu = new MenuBar();
        userMenu.addThemeVariants(MenuBarVariant.LUMO_TERTIARY_INLINE);
        userMenu.addClassNames(Margin.Right.MEDIUM);

        var userMenuItem = userMenu.addItem(avatar);
        userMenuItem.add(username);
        userMenuItem.getSubMenu().addItem("View Profile").setEnabled(false);
        userMenuItem.getSubMenu().addItem("Manage Settings").setEnabled(false);
        userMenuItem.getSubMenu().addItem("Logout", e -> logout());

        return userMenu;
    }

    // Method to get the breadcrumbs component (to be used by views)
    public Breadcrumbs getBreadcrumbs() {
        return breadcrumbs;
    }

    private void logout() {
        SecurityContextLogoutHandler logoutHandler = new SecurityContextLogoutHandler();
        logoutHandler.logout(SecurityUtils.getHttpServletRequest(), SecurityUtils.getHttpServletResponse(), SecurityContextHolder.getContext().getAuthentication());
        getUI().ifPresent(ui -> ui.getPage().setLocation("/login"));
    }
}

