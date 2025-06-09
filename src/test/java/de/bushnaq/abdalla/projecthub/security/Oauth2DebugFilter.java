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

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * Debug filter specifically for OAuth2 authorization requests.
 * This filter logs detailed information about requests to the OAuth2 authorization endpoints
 * to help diagnose redirection issues.
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
@ConditionalOnProperty(name = "spring.security.oauth2.client.registration.keycloak.client-id")
public class Oauth2DebugFilter implements Filter {

    private static final Logger logger = LoggerFactory.getLogger(Oauth2DebugFilter.class);

    private final ClientRegistrationRepository clientRegistrationRepository;

    public Oauth2DebugFilter(ClientRegistrationRepository clientRegistrationRepository) {
        this.clientRegistrationRepository = clientRegistrationRepository;
        logger.info("OAuth2 Debug Filter initialized");

        if (clientRegistrationRepository != null) {
            try {
                logger.info("ClientRegistrationRepository contains keycloak: {}",
                        clientRegistrationRepository.findByRegistrationId("keycloak") != null);
            } catch (Exception e) {
                logger.error("Error checking keycloak registration", e);
            }
        } else {
            logger.error("ClientRegistrationRepository is null!");
        }
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest httpRequest = (HttpServletRequest) request;
        String             requestURI  = httpRequest.getRequestURI();

        // Only intercept OAuth2 authorization requests
        if (requestURI.startsWith("/oauth2/authorization/")) {
            logger.info("=== OAuth2 Authorization Request Intercepted ===");
            logger.info("Request URI: {}", requestURI);
            logger.info("Method: {}", httpRequest.getMethod());
            logger.info("Query String: {}", httpRequest.getQueryString());
            logger.info("Remote Address: {}", request.getRemoteAddr());

            // Log all request headers
            logger.info("=== Request Headers ===");
            httpRequest.getHeaderNames().asIterator().forEachRemaining(headerName ->
                    logger.info("{}: {}", headerName, httpRequest.getHeader(headerName))
            );

            // Check if the client registration exists
            String registrationId = requestURI.substring("/oauth2/authorization/".length());
            logger.info("Registration ID from URI: {}", registrationId);

            try {
                if (clientRegistrationRepository != null) {
                    boolean exists = clientRegistrationRepository.findByRegistrationId(registrationId) != null;
                    logger.info("Client registration '{}' exists: {}", registrationId, exists);

                    if (!exists) {
                        logger.error("Client registration '{}' not found - authorization will fail!", registrationId);
                    }
                } else {
                    logger.error("ClientRegistrationRepository is null - cannot check registration");
                }
            } catch (Exception e) {
                logger.error("Error checking client registration", e);
            }

            // Continue the filter chain, but catch any exceptions
            try {
                chain.doFilter(request, response);
            } catch (Exception e) {
                logger.error("Exception during OAuth2 authorization request processing", e);
                throw e;
            }

            // Log response information after the filter chain completes
            HttpServletResponse httpResponse = (HttpServletResponse) response;
            logger.info("=== OAuth2 Authorization Response ===");
            logger.info("Response Status: {}", httpResponse.getStatus());

            // Log all response headers
            logger.info("=== Response Headers ===");
            httpResponse.getHeaderNames().forEach(headerName ->
                    logger.info("{}: {}", headerName, httpResponse.getHeader(headerName))
            );

        } else {
            // For non-OAuth2 authorization requests, just continue the chain
            chain.doFilter(request, response);
        }
    }
}
