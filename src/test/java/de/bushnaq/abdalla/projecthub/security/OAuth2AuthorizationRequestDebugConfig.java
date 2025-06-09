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

import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.web.DefaultOAuth2AuthorizationRequestResolver;
import org.springframework.security.oauth2.client.web.OAuth2AuthorizationRequestRedirectFilter;
import org.springframework.security.oauth2.client.web.OAuth2AuthorizationRequestResolver;
import org.springframework.security.oauth2.core.endpoint.OAuth2AuthorizationRequest;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

/**
 * Enhanced debugging configuration for OAuth2 authorization requests.
 * This class provides a customized OAuth2AuthorizationRequestResolver that logs detailed information
 * about the authorization request process.
 */
@Configuration
@ConditionalOnProperty(name = "spring.security.oauth2.client.registration.keycloak.client-id")
public class OAuth2AuthorizationRequestDebugConfig {

    private static final Logger logger = LoggerFactory.getLogger(OAuth2AuthorizationRequestDebugConfig.class);

    /**
     * Creates a debugging OAuth2AuthorizationRequestResolver that logs detailed information
     * about the authorization request creation and any errors.
     */
    @Bean
    @Primary // Mark this bean as primary to resolve the conflict
    public OAuth2AuthorizationRequestResolver debugOAuth2AuthorizationRequestResolver(
            ClientRegistrationRepository clientRegistrationRepository) {

        logger.info("Initializing debug OAuth2AuthorizationRequestResolver");

        // Log client registration details for debugging
        try {
            ClientRegistration registration = clientRegistrationRepository.findByRegistrationId("keycloak");
            logger.info("Keycloak client registration details:");
            logger.info("- Client ID: {}", registration.getClientId());
            logger.info("- Authorization grant type: {}", registration.getAuthorizationGrantType());
            logger.info("- Redirect URI: {}", registration.getRedirectUri());
            logger.info("- Scopes: {}", registration.getScopes());
            logger.info("- Authorization URI: {}", registration.getProviderDetails().getAuthorizationUri());
            logger.info("- Token URI: {}", registration.getProviderDetails().getTokenUri());
            // UserInfoEndpoint is nested within ProviderDetails in the current Spring Security version
            logger.info("- User info URI: {}", registration.getProviderDetails().getUserInfoEndpoint().getUri());
            logger.info("- JWK set URI: {}", registration.getProviderDetails().getJwkSetUri());
        } catch (Exception e) {
            logger.error("Error accessing client registration details", e);
        }

        // Create the base resolver
        DefaultOAuth2AuthorizationRequestResolver resolver = new DefaultOAuth2AuthorizationRequestResolver(
                clientRegistrationRepository,
                OAuth2AuthorizationRequestRedirectFilter.DEFAULT_AUTHORIZATION_REQUEST_BASE_URI);

        // Customize the authorization request with explicit handling of redirect URI templates
        resolver.setAuthorizationRequestCustomizer(new FixedRedirectUriCustomizer("http://localhost:8080"));

        // Wrap the resolver with logging
        return new LoggingOAuth2AuthorizationRequestResolver(resolver);
    }

    /**
     * Customizes the OAuth2 authorization request with a fixed base URL for the redirect URI.
     * This helps resolve issues where template variables in redirect URIs aren't properly resolved.
     */
    private static class FixedRedirectUriCustomizer implements Consumer<OAuth2AuthorizationRequest.Builder> {
        private final String baseUrl;

        public FixedRedirectUriCustomizer(String baseUrl) {
            this.baseUrl = baseUrl;
        }

        @Override
        public void accept(OAuth2AuthorizationRequest.Builder builder) {
            // Build the request for logging
            OAuth2AuthorizationRequest request             = builder.build();
            String                     originalRedirectUri = request.getRedirectUri();

            logger.info("Original authorization request details:");
            logger.info("- Authorization URI: {}", request.getAuthorizationUri());
            logger.info("- Client ID: {}", request.getClientId());
            logger.info("- Original Redirect URI: {}", originalRedirectUri);
            logger.info("- Scopes: {}", request.getScopes());

            // Fix the redirect URI if it contains template variables
            if (originalRedirectUri != null && originalRedirectUri.contains("{baseUrl}")) {
                String registrationId = "keycloak"; // Use the known registration ID
                String fixedRedirectUri = originalRedirectUri
                        .replace("{baseUrl}", baseUrl)
                        .replace("{registrationId}", registrationId);

                logger.info("Fixed Redirect URI: {}", fixedRedirectUri);

                // Set the fixed redirect URI
                builder.redirectUri(fixedRedirectUri);
            }

            // Add debugging parameters
            Map<String, Object> additionalParams = new HashMap<>(request.getAdditionalParameters());
            additionalParams.put("debug", "true");
            builder.additionalParameters(params -> params.putAll(additionalParams));
        }
    }

    /**
     * Wrapper for OAuth2AuthorizationRequestResolver that adds detailed logging.
     */
    private static class LoggingOAuth2AuthorizationRequestResolver implements OAuth2AuthorizationRequestResolver {
        private final OAuth2AuthorizationRequestResolver delegate;

        public LoggingOAuth2AuthorizationRequestResolver(OAuth2AuthorizationRequestResolver delegate) {
            this.delegate = delegate;
        }

        private void logAuthorizationRequest(OAuth2AuthorizationRequest request) {
            logger.info("=== OAuth2 Authorization Request Details ===");
            logger.info("- Authorization URI: {}", request.getAuthorizationUri());
            logger.info("- Client ID: {}", request.getClientId());
            logger.info("- Redirect URI: {}", request.getRedirectUri());
            logger.info("- Response Type: {}", request.getResponseType().getValue());
            logger.info("- Scopes: {}", request.getScopes());
            logger.info("- State: {}", request.getState());
            logger.info("- Additional parameters: {}", request.getAdditionalParameters());
        }

        @Override
        public OAuth2AuthorizationRequest resolve(HttpServletRequest request, String clientRegistrationId) {
            logger.info("Resolving OAuth2 authorization request for client: {}", clientRegistrationId);
            try {
                OAuth2AuthorizationRequest authRequest = delegate.resolve(request, clientRegistrationId);
                if (authRequest == null) {
                    logger.error("Failed to resolve OAuth2 authorization request for client {} - returned NULL", clientRegistrationId);
                } else {
                    logAuthorizationRequest(authRequest);
                }
                return authRequest;
            } catch (Exception e) {
                logger.error("Exception resolving OAuth2 authorization request for client: " + clientRegistrationId, e);
                throw e;
            }
        }

        @Override
        public OAuth2AuthorizationRequest resolve(HttpServletRequest request) {
            logger.info("Resolving OAuth2 authorization request from: {}", request.getRequestURI());
            try {
                OAuth2AuthorizationRequest authRequest = delegate.resolve(request);
                if (authRequest == null) {
                    logger.error("Failed to resolve OAuth2 authorization request - returned NULL");
                } else {
                    logAuthorizationRequest(authRequest);
                }
                return authRequest;
            } catch (Exception e) {
                logger.error("Exception resolving OAuth2 authorization request", e);
                throw e;
            }
        }
    }
}
