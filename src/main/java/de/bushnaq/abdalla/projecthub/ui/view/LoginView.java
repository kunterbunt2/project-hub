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

import com.vaadin.flow.component.html.*;
import com.vaadin.flow.component.login.LoginForm;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.auth.AnonymousAllowed;
import com.vaadin.flow.theme.lumo.LumoUtility.Margin;
import org.springframework.core.env.Environment;


/**
 * Login view that supports both Basic Authentication and OIDC authentication.
 */
@Route("login")
@PageTitle("Login | Project Hub")
@AnonymousAllowed
public class LoginView extends VerticalLayout implements BeforeEnterObserver {
    public static final String    LOGIN_VIEW        = "login-view";
    // ID for OIDC login button used in tests
    public static final String    OIDC_LOGIN_BUTTON = "oidc-login-button";
    public static final String    ROUTE             = "login";
    private final       LoginForm loginForm         = new LoginForm();
    private final       boolean   oidcEnabled;

    public LoginView(Environment environment) {
        this.oidcEnabled = environment.getProperty("spring.security.oauth2.client.registration.keycloak.client-id") != null;

        setId(LOGIN_VIEW);
        addClassName(LOGIN_VIEW);
        setSizeFull();
        setAlignItems(Alignment.CENTER);
        setJustifyContentMode(JustifyContentMode.CENTER);

        loginForm.setAction("login");
        loginForm.setForgotPasswordButtonVisible(false);

        H1 title = new H1("Project Hub");
        title.addClassNames(Margin.Bottom.MEDIUM);

        add(title);

        // Create a container for authentication options
        VerticalLayout authContainer = new VerticalLayout();
        authContainer.setWidthFull();
        authContainer.setMaxWidth("400px");
        authContainer.setAlignItems(Alignment.CENTER);
        authContainer.getStyle().set("margin", "0 auto");

        // Only add basic auth form if OIDC is not the only authentication method
        authContainer.add(new H3("Login with username and password"));
        authContainer.add(loginForm);

        Paragraph hint = new Paragraph("Default credentials: admin / admin123");
        hint.addClassName(Margin.Top.SMALL);
        authContainer.add(hint);

        // Add OIDC login option if it's enabled
        if (oidcEnabled) {
            Div separator = new Div();
            separator.setText("OR");
            separator.getStyle().set("margin", "20px 0");
            separator.getStyle().set("text-align", "center");
            separator.getStyle().set("width", "100%");
            authContainer.add(separator);

            // Create OIDC login button
            Anchor loginWithOidcButton = new Anchor("/oauth2/authorization/keycloak", "Login with SSO");
            loginWithOidcButton.setRouterIgnore(true); // Prevent Vaadin from intercepting the link
            loginWithOidcButton.setId(OIDC_LOGIN_BUTTON);
            loginWithOidcButton.getStyle().set("background-color", "var(--lumo-primary-color)");
            loginWithOidcButton.getStyle().set("color", "var(--lumo-primary-contrast-color)");
            loginWithOidcButton.getStyle().set("padding", "10px 20px");
            loginWithOidcButton.getStyle().set("border-radius", "4px");
            loginWithOidcButton.getStyle().set("cursor", "pointer");
            loginWithOidcButton.getStyle().set("text-decoration", "none");
            loginWithOidcButton.getStyle().set("display", "block");
            loginWithOidcButton.getStyle().set("text-align", "center");
            loginWithOidcButton.getStyle().set("margin-top", "20px");

            authContainer.add(loginWithOidcButton);
        }

        add(authContainer);
    }

    @Override
    public void beforeEnter(BeforeEnterEvent beforeEnterEvent) {
        // Check if authentication error occurred
        if (beforeEnterEvent.getLocation()
                .getQueryParameters()
                .getParameters()
                .containsKey("error")) {
            loginForm.setError(true);
        }
    }
}
