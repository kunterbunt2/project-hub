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

package de.bushnaq.abdalla.projecthub.ui.login;

import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.login.LoginForm;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.auth.AnonymousAllowed;
import com.vaadin.flow.theme.lumo.LumoUtility.Margin;

@Route("login")
@PageTitle("Login | Project Hub")
@AnonymousAllowed
public class LoginView extends VerticalLayout implements BeforeEnterObserver {
    public static final String LOGIN_VIEW = "login-view";
    public static final String ROUTE      = "login";

    private final LoginForm loginForm = new LoginForm();

    public LoginView() {
        setId(LOGIN_VIEW);
        addClassName(LOGIN_VIEW);
        setSizeFull();
        setAlignItems(Alignment.CENTER);
        setJustifyContentMode(JustifyContentMode.CENTER);

        loginForm.setAction("login");
        loginForm.setForgotPasswordButtonVisible(false);

        H1 title = new H1("Project Hub");
        title.addClassNames(Margin.Bottom.MEDIUM);

        Paragraph hint = new Paragraph("Default credentials: admin / admin123");
        hint.addClassName(Margin.Top.SMALL);

        add(
                title,
                loginForm,
                hint
        );
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
