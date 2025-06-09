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

package de.bushnaq.abdalla.projecthub.security;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.web.DefaultOAuth2AuthorizationRequestResolver;
import org.springframework.security.oauth2.client.web.OAuth2AuthorizationRequestRedirectFilter;
import org.springframework.security.oauth2.client.web.OAuth2AuthorizationRequestResolver;
import org.springframework.security.oauth2.core.endpoint.OAuth2AuthorizationRequest;
import org.springframework.web.client.DefaultResponseErrorHandler;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.time.Duration;

/**
 * Special test configuration for debugging OIDC authentication issues.
 * This configuration provides custom beans for the OAuth2 client that include
 * logging and error handling to help diagnose connection issues.
 */
@TestConfiguration
@ConditionalOnProperty(name = "spring.security.oauth2.client.registration.keycloak.client-id")
public class OidcTestDebugConfig {

    /**
     * Add a bean to explicitly log available client registrations at startup
     */
    @Bean
    public ClientRegistrationRepositoryLogger clientRegistrationRepositoryLogger(
            ClientRegistrationRepository clientRegistrationRepository) {

        return new ClientRegistrationRepositoryLogger(clientRegistrationRepository);
    }

    /**
     * Custom OAuth2AuthorizationRequestResolver that logs the authorization requests
     */
    @Bean
    public OAuth2AuthorizationRequestResolver oauth2AuthorizationRequestResolver(
            ClientRegistrationRepository clientRegistrationRepository) {

        if (clientRegistrationRepository == null) {
            System.out.println("OIDC Debug: Cannot create OAuth2AuthorizationRequestResolver - ClientRegistrationRepository is null!");
            return null;
        }

        System.out.println("OIDC Debug: Creating OAuth2AuthorizationRequestResolver with base URI: " +
                OAuth2AuthorizationRequestRedirectFilter.DEFAULT_AUTHORIZATION_REQUEST_BASE_URI);

        DefaultOAuth2AuthorizationRequestResolver resolver =
                new DefaultOAuth2AuthorizationRequestResolver(
                        clientRegistrationRepository,
                        OAuth2AuthorizationRequestRedirectFilter.DEFAULT_AUTHORIZATION_REQUEST_BASE_URI);

        // Add logging to help diagnose issues
        resolver.setAuthorizationRequestCustomizer(customizer -> {
            OAuth2AuthorizationRequest request = customizer.build();
            System.out.println("OIDC Auth Request: Creating authorization request");
            System.out.println("OIDC Auth Request: Authorization URI: " + request.getAuthorizationUri());
            System.out.println("OIDC Auth Request: Client ID: " + request.getClientId());
            System.out.println("OIDC Auth Request: Redirect URI: " + request.getRedirectUri());
            System.out.println("OIDC Auth Request: Scopes: " + request.getScopes());
            System.out.println("OIDC Auth Request: State: " + request.getState());
        });

        return resolver;
    }

    /**
     * Custom RestTemplate for OAuth2 client with debugging capabilities
     */
    @Bean
    public RestTemplate oauth2ClientRestTemplate(RestTemplateBuilder builder) {
        return builder
                .setConnectTimeout(Duration.ofSeconds(30))
                .setReadTimeout(Duration.ofSeconds(30))
                .additionalInterceptors(new LoggingInterceptor())
                .errorHandler(new CustomResponseErrorHandler())
                .build();
    }

    /**
     * Helper bean to log client registration details
     */
    public static class ClientRegistrationRepositoryLogger {
        public ClientRegistrationRepositoryLogger(ClientRegistrationRepository repository) {
            System.out.println("OIDC Debug: Initializing ClientRegistrationRepository logger");

            if (repository == null) {
                System.out.println("OIDC Debug: ClientRegistrationRepository is NULL - OAuth2 endpoints will not be available!");
                return;
            }

            try {
                System.out.println("OIDC Debug: Logging available client registrations:");
                repository.findByRegistrationId("keycloak");
                System.out.println("OIDC Debug: Found keycloak registration");
            } catch (Exception e) {
                System.out.println("OIDC Debug: Error finding keycloak registration: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }

    /**
     * Custom error handler that provides more details on HTTP errors
     */
    private static class CustomResponseErrorHandler extends DefaultResponseErrorHandler {
        @Override
        public void handleError(ClientHttpResponse response) throws IOException {
            // Log the error before handling it
            System.out.println("OIDC Debug: Error response: " + response.getStatusCode() +
                    " " + response.getStatusText());
            // Continue with normal error handling
            super.handleError(response);
        }
    }

    /**
     * HTTP request/response interceptor that logs details of OAuth2 HTTP communications
     */
    private static class LoggingInterceptor implements ClientHttpRequestInterceptor {
        @Override
        public ClientHttpResponse intercept(HttpRequest request, byte[] body,
                                            ClientHttpRequestExecution execution) throws IOException {

            // Log the request
            System.out.println("OIDC Request: " + request.getMethod() + " " +
                    request.getURI());
            request.getHeaders().forEach((key, value) ->
                    System.out.println("OIDC Request Header: " + key + "=" + value));

            // Execute the request
            ClientHttpResponse response = execution.execute(request, body);

            // Log the response
            System.out.println("OIDC Response: " + response.getStatusCode() + " from " +
                    request.getURI());
            response.getHeaders().forEach((key, value) ->
                    System.out.println("OIDC Response Header: " + key + "=" + value));

            return response;
        }
    }
}
