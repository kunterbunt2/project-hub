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
import com.vaadin.flow.router.AfterNavigationEvent;
import com.vaadin.flow.router.AfterNavigationObserver;
import com.vaadin.flow.router.Layout;
import com.vaadin.flow.server.menu.MenuConfiguration;
import com.vaadin.flow.server.menu.MenuEntry;
import com.vaadin.flow.theme.lumo.Lumo;
import de.bushnaq.abdalla.projecthub.security.SecurityUtils;
import de.bushnaq.abdalla.projecthub.ui.component.Breadcrumbs;
import de.bushnaq.abdalla.projecthub.ui.component.ThemeToggle;
import jakarta.annotation.security.PermitAll;
import lombok.Getter;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;

import java.util.HashMap;
import java.util.Map;

import static com.vaadin.flow.theme.lumo.LumoUtility.*;

@Layout
@PermitAll // When security is enabled, allow all authenticated users
//@JsModule("/tooltips.js")
public final class MainLayout extends AppLayout implements AfterNavigationObserver {

    public static final String           ID_BREADCRUMBS               = "main-layout-breadcrumbs";
    // Test IDs for Selenium testing
    public static final String           ID_LOGO                      = "main-layout-logo";
    public static final String           ID_NAVIGATION_TABS           = "main-layout-navigation-tabs";
    public static final String           ID_THEME_TOGGLE              = "main-layout-theme-toggle";
    public static final String           ID_USER_MENU                 = "main-layout-user-menu";
    public static final String           ID_USER_MENU_AVAILABILITY    = "main-layout-user-menu-availability";
    public static final String           ID_USER_MENU_ITEM            = "main-layout-user-menu-item";
    public static final String           ID_USER_MENU_LOCATION        = "main-layout-user-menu-location";
    public static final String           ID_USER_MENU_LOGOUT          = "main-layout-user-menu-logout";
    public static final String           ID_USER_MENU_MANAGE_SETTINGS = "main-layout-user-menu-manage-settings";
    public static final String           ID_USER_MENU_OFF_DAYS        = "main-layout-user-menu-off-days";
    public static final String           ID_USER_MENU_VIEW_PROFILE    = "main-layout-user-menu-view-profile";
    // Method to get the breadcrumbs component (to be used by views)
    @Getter
    private final       Breadcrumbs      breadcrumbs                  = new Breadcrumbs();
    private             Image            logoImage;   // Store reference to logo image
    private final       Map<Tab, String> tabToPathMap                 = new HashMap<>();
    private final       Tabs             tabs                         = new Tabs();

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

    @Override
    public void afterNavigation(AfterNavigationEvent event) {
        // Get the current location path
        String currentPath = "/" + event.getLocation().getPath();

        // Find and select the tab that matches the current path
        boolean matchFound = false;
        for (Map.Entry<Tab, String> entry : tabToPathMap.entrySet()) {
            String tabPath = entry.getValue();
            // Match if current path equals the tab path or starts with it (for sub-routes)
            if (currentPath.equals(tabPath)) {
                tabs.setSelectedTab(entry.getKey());
                matchFound = true;
                break;
            }
        }

        // If no match found, deselect all tabs
        if (!matchFound) {
            tabs.setSelectedTab(null);
        }
    }

    private Div createBreadcrumbs() {
        breadcrumbs.setId(ID_BREADCRUMBS);
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
        logoImage.setId(ID_LOGO);

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
        tabs.setId(ID_NAVIGATION_TABS);

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
        themeToggle.setId(ID_THEME_TOGGLE);

        // Add click listener to update logo when theme is toggled
        themeToggle.addClickListener(event -> {
            UI      ui          = UI.getCurrent();
            boolean isDarkTheme = ui.getElement().getThemeList().contains(Lumo.DARK);
            updateLogoBasedOnTheme(isDarkTheme);
        });

        return themeToggle;
    }

    private Component createUserMenu() {
        final String userEmail = getUserEmail();

        var avatar = new Avatar(userEmail);
        avatar.addThemeVariants(AvatarVariant.LUMO_XSMALL);
        avatar.addClassNames(Margin.Right.SMALL);
        avatar.setColorIndex(5);

        var userMenu = new MenuBar();
        userMenu.addThemeVariants(MenuBarVariant.LUMO_TERTIARY_INLINE);
        userMenu.addClassNames(Margin.Right.MEDIUM);
        userMenu.setId(ID_USER_MENU);

        var userMenuItem = userMenu.addItem(avatar);
        userMenuItem.add(userEmail);
        userMenuItem.setId(ID_USER_MENU_ITEM);

        var availabilityItem = userMenuItem.getSubMenu().addItem("Manage Availability", e -> navigateToAvailability(userEmail));
        availabilityItem.setId(ID_USER_MENU_AVAILABILITY);

        var locationItem = userMenuItem.getSubMenu().addItem("Manage Location", e -> navigateToLocation(userEmail));
        locationItem.setId(ID_USER_MENU_LOCATION);

        var offDaysItem = userMenuItem.getSubMenu().addItem("Manage Off Days", e -> navigateToOffDays(userEmail));
        offDaysItem.setId(ID_USER_MENU_OFF_DAYS);

        var viewProfileItem = userMenuItem.getSubMenu().addItem("View Profile");
        viewProfileItem.setEnabled(false);
        viewProfileItem.setId(ID_USER_MENU_VIEW_PROFILE);

        var manageSettingsItem = userMenuItem.getSubMenu().addItem("Manage Settings");
        manageSettingsItem.setEnabled(false);
        manageSettingsItem.setId(ID_USER_MENU_MANAGE_SETTINGS);

        var logoutItem = userMenuItem.getSubMenu().addItem("Logout", e -> logout());
        logoutItem.setId(ID_USER_MENU_LOGOUT);

        return userMenu;
    }

    private String getUserEmail() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String         userEmail      = authentication != null ? authentication.getName() : "Guest";

        // If using OIDC, try to get the email address from authentication details
        if (authentication != null && authentication.getPrincipal() instanceof org.springframework.security.oauth2.core.oidc.user.OidcUser oidcUser) {
            String email = oidcUser.getEmail();
            if (email != null && !email.isEmpty()) {
                userEmail = email;
            }
        }
        return userEmail;
    }

    private void logout() {
        SecurityContextLogoutHandler logoutHandler = new SecurityContextLogoutHandler();
        logoutHandler.logout(SecurityUtils.getHttpServletRequest(), SecurityUtils.getHttpServletResponse(), SecurityContextHolder.getContext().getAuthentication());
        getUI().ifPresent(ui -> ui.getPage().setLocation("/ui/login"));
    }

    private void navigateToAvailability(String userEmail) {
        getUI().ifPresent(ui -> ui.navigate("availability/" + userEmail));
    }

    private void navigateToLocation(String userEmail) {
        getUI().ifPresent(ui -> ui.navigate("location/" + userEmail));
    }

    private void navigateToOffDays(String userEmail) {
        getUI().ifPresent(ui -> ui.navigate("offday/" + userEmail));
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
