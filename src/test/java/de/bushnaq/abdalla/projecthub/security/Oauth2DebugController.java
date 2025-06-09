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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.core.endpoint.OAuth2ParameterNames;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.servlet.view.RedirectView;

import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * A controller to handle OAuth2 authorization requests during testing.
 * <p>
 * This controller provides a direct endpoint to initiate OAuth2 authorization with Keycloak
 * during integration tests, bypassing the standard Spring Security OAuth2 flow if there are issues.
 */
@Controller
public class Oauth2DebugController {

    private static final Logger                       logger = LoggerFactory.getLogger(Oauth2DebugController.class);
    private final        ClientRegistrationRepository clientRegistrationRepository;

    public Oauth2DebugController(ClientRegistrationRepository clientRegistrationRepository) {
        this.clientRegistrationRepository = clientRegistrationRepository;
        logger.info("OAuth2 Debug Controller initialized");
    }

    /**
     * A direct endpoint to handle OAuth2 authorization requests.
     * This endpoint is similar to the standard /oauth2/authorization/{registrationId} endpoint
     * but provides more detailed logging and error handling.
     *
     * @param registrationId the ID of the client registration to use for authorization
     * @return a redirect to the authorization server
     */
    @GetMapping("/oauth2/authorization/{registrationId}")
    public RedirectView authorizeWithProvider(@PathVariable String registrationId) {
        logger.info("Handling OAuth2 authorization request for: {}", registrationId);

        try {
            // Find the client registration
            ClientRegistration clientRegistration = clientRegistrationRepository.findByRegistrationId(registrationId);

            if (clientRegistration == null) {
                logger.error("Client registration not found for ID: {}", registrationId);
                throw new IllegalArgumentException("Invalid registration ID: " + registrationId);
            }

            logger.info("Found client registration for: {}", registrationId);
            logger.info("Authorization URI: {}", clientRegistration.getProviderDetails().getAuthorizationUri());
            logger.info("Redirect URI template: {}", clientRegistration.getRedirectUri());

            // Create the redirect URI
            String redirectUri = clientRegistration.getRedirectUri()
                    .replace("{baseUrl}", "http://localhost:8080")
                    .replace("{registrationId}", registrationId);

            logger.info("Using redirect URI: {}", redirectUri);

            // Generate state for CSRF protection
            String state = Base64.getEncoder().encodeToString(UUID.randomUUID().toString().getBytes());

            // Build the authorization URL with query parameters
            Map<String, String> params = new HashMap<>();
            params.put(OAuth2ParameterNames.RESPONSE_TYPE, "code");
            params.put(OAuth2ParameterNames.CLIENT_ID, clientRegistration.getClientId());
            params.put(OAuth2ParameterNames.SCOPE, String.join(" ", clientRegistration.getScopes()));
            params.put(OAuth2ParameterNames.STATE, state);
            params.put(OAuth2ParameterNames.REDIRECT_URI, redirectUri);

            StringBuilder authorizationUrl = new StringBuilder(clientRegistration.getProviderDetails().getAuthorizationUri());
            authorizationUrl.append('?');

            boolean first = true;
            for (Map.Entry<String, String> param : params.entrySet()) {
                if (!first) {
                    authorizationUrl.append('&');
                }
                authorizationUrl.append(param.getKey()).append('=').append(param.getValue());
                first = false;
            }

            String finalUrl = authorizationUrl.toString();
            logger.info("Redirecting to: {}", finalUrl);

            return new RedirectView(finalUrl);
        } catch (Exception e) {
            logger.error("Error processing OAuth2 authorization request", e);
            // In case of an error, redirect to the login page
            return new RedirectView("/login?error=oauth2_error");
        }
    }
}
