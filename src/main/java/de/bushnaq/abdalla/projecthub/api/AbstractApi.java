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
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.client.DefaultResponseErrorHandler;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.server.ServerErrorException;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

/**
 * Allows us to handle exceptions from server.
 */
//@Service
public class AbstractApi {
    protected String       baseUrl = "http://localhost:8080/api"; // Configure as needed
    protected ObjectMapper objectMapper;
    protected RestTemplate restTemplate;

    /**
     * used for uni tests to enforce in-memory db.
     *
     * @param restTemplate the rest template
     * @param objectMapper the object mapper
     * @param baseUrl      the base url to the local rest server
     */
    protected AbstractApi(RestTemplate restTemplate, ObjectMapper objectMapper, String baseUrl) {
        this.restTemplate = restTemplate;
        this.objectMapper = objectMapper;
        this.baseUrl      = baseUrl;
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
     * Creates HTTP headers with Basic Authentication using the current user's credentials.
     * This ensures API calls made from the UI have proper authentication.
     */
    protected HttpHeaders createAuthHeaders() {
        HttpHeaders headers = new HttpHeaders();

        // Get current authentication from security context
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication != null && authentication.isAuthenticated()) {
            String username = authentication.getName();
            // For simplicity in this demo environment, we use a fixed password for API calls
            // In production, this would need a more secure approach
            String password = "test-password";

            String auth        = username + ":" + password;
            byte[] encodedAuth = Base64.getEncoder().encode(auth.getBytes(StandardCharsets.UTF_8));
            String authHeader  = "Basic " + new String(encodedAuth);

            headers.set("Authorization", authHeader);
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
            handleExceptions(e);
            return null;
        }
    }

    protected void executeWithErrorHandling(RestOperation operation) {
        try {
            operation.execute();
        } catch (HttpClientErrorException e) {
            handleExceptions(e);
        }
    }

    private void handleExceptions(HttpClientErrorException e) {
        try {
            // Handle authentication/authorization errors specifically for test cases
            if (e instanceof HttpClientErrorException.Unauthorized) {
                // Convert 401 Unauthorized to AuthenticationCredentialsNotFoundException
                throw new org.springframework.security.authentication.AuthenticationCredentialsNotFoundException(
                        "Authentication credentials not found when accessing API");
            } else if (e instanceof HttpClientErrorException.Forbidden) {
                // Convert 403 Forbidden to AccessDeniedException
                throw new org.springframework.security.access.AccessDeniedException(
                        "Access denied when accessing API");
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