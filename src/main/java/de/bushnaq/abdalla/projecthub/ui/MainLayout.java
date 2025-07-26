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

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.applayout.AppLayout;
import com.vaadin.flow.component.avatar.Avatar;
import com.vaadin.flow.component.avatar.AvatarVariant;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
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
import com.vaadin.flow.theme.lumo.Lumo;
import de.bushnaq.abdalla.projecthub.security.SecurityUtils;
import de.bushnaq.abdalla.projecthub.ui.component.Breadcrumbs;
import de.bushnaq.abdalla.projecthub.ui.component.ThemeToggle;
import jakarta.annotation.security.PermitAll;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;

import java.util.HashMap;
import java.util.Map;

import static com.vaadin.flow.theme.lumo.LumoUtility.*;

@Layout
@PermitAll // When security is enabled, allow all authenticated users
//@JsModule("/tooltips.js")
public final class MainLayout extends AppLayout {

    private final Breadcrumbs      breadcrumbs  = new Breadcrumbs();
    private       Image            logoImage;   // Store reference to logo image
    private final Map<Tab, String> tabToPathMap = new HashMap<>();
    private final Tabs             tabs         = new Tabs();

    MainLayout() {
        UI.getCurrent().getPage().addJavaScript("/js/tooltips.js");
        setPrimarySection(Section.NAVBAR);

        // Create main navigation bar components
        HorizontalLayout navbarLayout        = createNavBar();
        Div              breadcrumbContainer = createBreadcrumbs();

        var navAndBreadcrumbs = new VerticalLayout();
        navAndBreadcrumbs.setPadding(false);
        navAndBreadcrumbs.setSpacing(false);
        navAndBreadcrumbs.setMargin(false);

        navAndBreadcrumbs.add(navbarLayout, breadcrumbContainer);

        // Add the combined layout to the navbar area
        addToNavbar(true, navAndBreadcrumbs);
        // Remove margins from navbar layout and apply padding to the container instead
        navAndBreadcrumbs.getStyle().set("padding-left", "var(--lumo-space-m)");
        navAndBreadcrumbs.getStyle().set("padding-right", "var(--lumo-space-m)");
        navAndBreadcrumbs.getStyle().set("padding-bottom", "var(--lumo-space-m)");
        // Remove these lines that are causing the overflow
        this.getStyle().set("padding-left", "var(--lumo-space-m)");
        this.getStyle().set("padding-right", "var(--lumo-space-m)");

    }

    private Div createBreadcrumbs() {
        Div breadcrumbContainer = new Div(breadcrumbs);
        breadcrumbContainer.addClassNames(
                Padding.Horizontal.MEDIUM,
                Padding.Vertical.XSMALL,
                Width.FULL,
                Background.CONTRAST_5
        );

        return breadcrumbContainer;
    }

    private Image createLogo() {
        // Create the logo image component
        logoImage = new Image("images/logo.svg", "Kassandra Logo");
        logoImage.setHeight("32px");

        // Check initial theme and set appropriate logo
        UI      ui          = UI.getCurrent();
        boolean isDarkTheme = ui.getElement().getThemeList().contains(Lumo.DARK);
        updateLogoBasedOnTheme(isDarkTheme);

        return logoImage;
    }

    private HorizontalLayout createNavBar() {
        HorizontalLayout navbarLayout = new HorizontalLayout();
        navbarLayout.setWidthFull();
        navbarLayout.setJustifyContentMode(FlexComponent.JustifyContentMode.BETWEEN);
        navbarLayout.setAlignItems(FlexComponent.Alignment.CENTER);
        navbarLayout.addClassName("navbar-custom");

        // Add logo and app name to the left
        Image logoLayout = createLogo();

        // Add navigation tabs to the center
        createNavTabs();
        tabs.addClassNames(Margin.Horizontal.MEDIUM);

        // Create theme toggle and register theme change listener
        ThemeToggle themeToggle = createThemeToggle();

        // Add user menu to the right
        Component userMenu = createUserMenu();

        navbarLayout.add(logoLayout, tabs, themeToggle, userMenu);
        navbarLayout.expand(tabs);
        return navbarLayout;
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

    /**
     * Creates a theme toggle button and adds a listener to update the logo when theme changes
     *
     * @return the theme toggle button
     */
    private ThemeToggle createThemeToggle() {
        ThemeToggle themeToggle = new ThemeToggle();

        // Add click listener to update logo when theme is toggled
        themeToggle.addClickListener(event -> {
            UI      ui          = UI.getCurrent();
            boolean isDarkTheme = ui.getElement().getThemeList().contains(Lumo.DARK);
            updateLogoBasedOnTheme(isDarkTheme);
        });

        return themeToggle;
    }

    private Component createUserMenu() {
        final String username = getUserName();

        var avatar = new Avatar(username);
        avatar.addThemeVariants(AvatarVariant.LUMO_XSMALL);
        avatar.addClassNames(Margin.Right.SMALL);
        avatar.setColorIndex(5);

        var userMenu = new MenuBar();
        userMenu.addThemeVariants(MenuBarVariant.LUMO_TERTIARY_INLINE);
        userMenu.addClassNames(Margin.Right.MEDIUM);

        var userMenuItem = userMenu.addItem(avatar);
        userMenuItem.add(username);
        userMenuItem.getSubMenu().addItem("Manage Availability", e -> navigateToAvailability(username));
        userMenuItem.getSubMenu().addItem("Manage Location", e -> navigateToLocation(username));
        userMenuItem.getSubMenu().addItem("Manage Off Days", e -> navigateToOffDays(username));
        userMenuItem.getSubMenu().addItem("View Profile").setEnabled(false);
        userMenuItem.getSubMenu().addItem("Manage Settings").setEnabled(false);
        userMenuItem.getSubMenu().addItem("Logout", e -> logout());

        return userMenu;
    }

    // Method to get the breadcrumbs component (to be used by views)
    public Breadcrumbs getBreadcrumbs() {
        return breadcrumbs;
    }

    private String getUserName() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String         userName       = authentication != null ? authentication.getName() : "Guest";

        // If using OIDC, try to get the email address from authentication details
        if (authentication != null && authentication.getPrincipal() instanceof org.springframework.security.oauth2.core.oidc.user.OidcUser oidcUser) {
            String email = oidcUser.getEmail();
            if (email != null && !email.isEmpty()) {
                userName = email;
            }
        }
        return userName;
    }

    private void logout() {
        SecurityContextLogoutHandler logoutHandler = new SecurityContextLogoutHandler();
        logoutHandler.logout(SecurityUtils.getHttpServletRequest(), SecurityUtils.getHttpServletResponse(), SecurityContextHolder.getContext().getAuthentication());
        getUI().ifPresent(ui -> ui.getPage().setLocation("/ui/login"));
    }

    private void navigateToAvailability(String username) {
        getUI().ifPresent(ui -> ui.navigate("availability/" + username));
    }

    private void navigateToLocation(String username) {
        getUI().ifPresent(ui -> ui.navigate("location/" + username));
    }

    private void navigateToOffDays(String username) {
        getUI().ifPresent(ui -> ui.navigate("offday/" + username));
    }

    /**
     * Updates the logo source based on the theme
     *
     * @param isDarkTheme true if dark theme is active, false otherwise
     */
    private void updateLogoBasedOnTheme(boolean isDarkTheme) {
        if (logoImage != null) {
            if (isDarkTheme) {
                logoImage.setSrc("images/logo-dark.svg");
            } else {
                logoImage.setSrc("images/logo.svg");
            }
        }
    }
}
