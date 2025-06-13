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

package de.bushnaq.abdalla.projecthub.api;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import de.bushnaq.abdalla.projecthub.rest.util.ErrorResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.web.client.DefaultResponseErrorHandler;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.server.ServerErrorException;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Collection;
import java.util.stream.Collectors;

/**
 * Allows us to handle exceptions from server.
 */
//@Service
public class AbstractApi {
    private static final Logger logger = LoggerFactory.getLogger(AbstractApi.class);

    @Autowired(required = false)
    protected OAuth2AuthorizedClientService authorizedClientService;
    @Value("${projecthub.api.base-url:}")
    private   String                        configuredBaseUrl;
    @Autowired(required = false)
    private   Environment                   environment;
    protected ObjectMapper                  objectMapper;
    @Value("${server.port:8080}")
    private   int                           port;
    protected RestTemplate                  restTemplate;

    /**
     * used for uni tests to enforce in-memory db.
     *
     * @param restTemplate the rest template
     * @param objectMapper the object mapper
     * @param baseUrl      the base url to the local rest server
     */
    protected AbstractApi(RestTemplate restTemplate, ObjectMapper objectMapper, String baseUrl) {
        this.restTemplate      = restTemplate;
        this.objectMapper      = objectMapper;
        this.configuredBaseUrl = baseUrl;
        initialize(restTemplate, objectMapper);
    }

    protected AbstractApi(RestTemplate restTemplate, ObjectMapper objectMapper) {
        this.restTemplate = restTemplate;
        this.objectMapper = objectMapper;
        initialize(restTemplate, objectMapper);
    }

    protected AbstractApi() {
    }

    /**
     * Creates HTTP headers with authentication using either OIDC token or Basic Auth.
     * This ensures API calls made from the UI have proper authentication.
     * Role information is extracted from the security context and logged.
     * The OIDC token already contains the role information, so roles are handled automatically.
     */
    protected HttpHeaders createAuthHeaders() {
        HttpHeaders headers = new HttpHeaders();

        // Get current authentication from security context
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication != null && authentication.isAuthenticated()) {
            // Log user roles for debugging purposes
            Collection<? extends GrantedAuthority> authorities = authentication.getAuthorities();
            String roles = authorities.stream()
                    .map(GrantedAuthority::getAuthority)
                    .collect(Collectors.joining(", "));
//            logger.debug("User {} has roles: {}", authentication.getName(), roles);

            // Check if the authentication is OAuth2/OIDC
            if (authentication instanceof OAuth2AuthenticationToken oauth2Token && authorizedClientService != null) {
                OAuth2AuthorizedClient client = authorizedClientService.loadAuthorizedClient(
                        oauth2Token.getAuthorizedClientRegistrationId(),
                        oauth2Token.getName());

                if (client != null && client.getAccessToken() != null) {
                    // Use OAuth2 Bearer Token authentication
                    String tokenValue = client.getAccessToken().getTokenValue();

                    // Critical: Ensure token is properly formatted for JWT authentication
                    // Spring Security's JWT processing expects "Bearer" prefix
                    if (tokenValue != null && !tokenValue.isEmpty()) {
//                        headers.setBearerAuth(tokenValue);
                        headers.set("Authorization", "Bearer " + tokenValue);

                        // Enhanced logging for debugging
//                        logger.debug("Bearer token set in headers. Token length: {}", tokenValue.length());

                        // For debugging: add user information from OAuth2 token
                        OAuth2User principal = oauth2Token.getPrincipal();
//                        logger.debug("OAuth2 user attributes: {}", principal.getAttributes());
                    } else {
                        logger.warn("Access token value is empty or null");
                    }

                    // Ensure we're setting Content-Type for JSON properly
                    headers.set("Content-Type", "application/json");
                    headers.set("Accept", "application/json");

                    return headers;
                } else {
                    logger.warn("OAuth2 client or access token is null for user {}", oauth2Token.getName());
                }
            }

            // Fallback to basic auth if no OIDC token is available
//            logger.debug("Falling back to basic auth for user {}", authentication.getName());
            String username = authentication.getName();
            // For simplicity in this demo environment, we use a fixed password for API calls
            // In production, this would need a more secure approach
            String password = "test-password";

            String auth        = username + ":" + password;
            byte[] encodedAuth = Base64.getEncoder().encode(auth.getBytes(StandardCharsets.UTF_8));
            String authHeader  = "Basic " + new String(encodedAuth);

            headers.set("Authorization", authHeader);
            headers.set("Content-Type", "application/json");
            headers.set("Accept", "application/json");

            // For basic auth, we can optionally include roles in a custom header for debugging
            headers.set("X-User-Roles", roles);
//            logger.debug("Using basic auth with roles: {}", roles);
        } else {
            logger.warn("No authentication found in SecurityContextHolder");
        }

        return headers;
    }

    /**
     * Creates an HttpEntity with authentication headers for use with RestTemplate
     */
    protected <T> HttpEntity<T> createHttpEntity(T body) {
        return new HttpEntity<>(body, createAuthHeaders());
    }

    /**
     * Creates an HttpEntity with only authentication headers (no body) for use with RestTemplate
     */
    protected HttpEntity<?> createHttpEntity() {
        return new HttpEntity<>(createAuthHeaders());
    }

    protected <T> T executeWithErrorHandling(RestOperationWithResult<T> operation) {
        try {
            return operation.execute();
        } catch (HttpClientErrorException e) {
            logger.error("REST API call failed with status: {} and response: {}", e.getStatusCode(), e.getResponseBodyAsString());
            handleExceptions(e);
            return null;
        } catch (Exception e) {
            logger.error("Unexpected error in REST API call", e);
            throw new ServerErrorException("Failed to execute REST API call", e);
        }
    }

    protected void executeWithErrorHandling(RestOperation operation) {
        try {
            operation.execute();
        } catch (HttpClientErrorException e) {
            logger.error("REST API call failed with status: {} and response: {}", e.getStatusCode(), e.getResponseBodyAsString());
            handleExceptions(e);
        } catch (Exception e) {
            logger.error("Unexpected error in REST API call", e);
            throw new ServerErrorException("Failed to execute REST API call", e);
        }
    }

    protected String getBaseUrl() {
        if (configuredBaseUrl != null && !configuredBaseUrl.isBlank()) {
            return configuredBaseUrl;
        }
        if (port == 0) {
            port = 8080;
            //- we are probably in a test, try to get the port dynamically at runtime
            String portStr = null;
            if (environment != null) {
                portStr = environment.getProperty("local.server.port");
                if (portStr == null) {
                    portStr = environment.getProperty("server.port");
                }
            }
            if (portStr != null) {
                try {
                    port = Integer.parseInt(portStr);
                } catch (NumberFormatException ignored) {
                    logger.warn("Invalid port number '{}' from environment, using default port 8080", portStr);
                }
            }
        }
        configuredBaseUrl = "http://localhost:" + port + "/api";
        return configuredBaseUrl;
    }

    private void handleExceptions(HttpClientErrorException e) {
        try {
            // Handle authentication/authorization errors specifically for test cases
            if (e instanceof HttpClientErrorException.Unauthorized) {
                // Convert 401 Unauthorized to AuthenticationCredentialsNotFoundException
                throw new org.springframework.security.authentication.AuthenticationCredentialsNotFoundException("Authentication credentials not found when accessing API");
            } else if (e instanceof HttpClientErrorException.Forbidden) {
                // Convert 403 Forbidden to AccessDeniedException
                throw new org.springframework.security.access.AccessDeniedException("Access denied when accessing API");
            } else if (e instanceof HttpClientErrorException.BadRequest) {
                throw new ServerErrorException(e.getMessage(), e.getCause());
            } else if (e instanceof HttpClientErrorException.NotFound) {
                throw new ServerErrorException(e.getMessage(), e.getCause());
            } else {
                ErrorResponse error = objectMapper.readValue(e.getResponseBodyAsString(), ErrorResponse.class);
                throw new ServerErrorException(error.getMessage(), error.getException());
            }
        } catch (JsonProcessingException ex) {
            throw new IllegalArgumentException(String.format("Error processing server response '%s'.", e.getResponseBodyAsString()));
        }
    }

    private void initialize(RestTemplate restTemplate, ObjectMapper objectMapper) {
        this.restTemplate.setErrorHandler(new DefaultResponseErrorHandler());
        // Configure message converters for JSON
        restTemplate.getMessageConverters().clear();
        MappingJackson2HttpMessageConverter messageConverter = new MappingJackson2HttpMessageConverter();
        messageConverter.setObjectMapper(objectMapper);
        restTemplate.getMessageConverters().add(messageConverter);
    }

    @FunctionalInterface
    protected interface RestOperation {
        void execute() throws HttpClientErrorException;
    }

    @FunctionalInterface
    protected interface RestOperationWithResult<T> {
        T execute() throws HttpClientErrorException;
    }
}
