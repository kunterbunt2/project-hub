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
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.stereotype.Service;
import org.springframework.web.client.DefaultResponseErrorHandler;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.server.ServerErrorException;

/**
 * Allows us to handle exceptions from server.
 */
@Service
public class AbstractApi {
    protected String       baseUrl = "http://localhost:8080"; // Configure as needed
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
        this.restTemplate.setErrorHandler(new DefaultResponseErrorHandler());

        this.baseUrl = baseUrl;
        // Configure message converters for JSON
        restTemplate.getMessageConverters().clear();
//        restTemplate.getMessageConverters().add(new MappingJackson2HttpMessageConverter());
        MappingJackson2HttpMessageConverter messageConverter = new MappingJackson2HttpMessageConverter();
        messageConverter.setObjectMapper(objectMapper);
        restTemplate.getMessageConverters().add(messageConverter);
    }

    protected AbstractApi() {
    }

    protected void executeWithErrorHandling(RestOperation operation) {
        try {
            operation.execute();
        } catch (HttpClientErrorException e) {
            try {
                if (e instanceof HttpClientErrorException.BadRequest) {
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
//            try {
//                ErrorResponse error = objectMapper.readValue(e.getResponseBodyAsString(), ErrorResponse.class);
//                throw new ServerErrorException(error.getMessage(), error.getException());
//            } catch (JsonProcessingException ex) {
//                throw new IllegalArgumentException(String.format("Error processing server response '%s'.", e.getResponseBodyAsString()));
//            }
        }
    }

    protected <T> T executeWithErrorHandling(RestOperationWithResult<T> operation) {
        try {
            return operation.execute();
        } catch (HttpClientErrorException e) {
            try {
                if (e instanceof HttpClientErrorException.NotFound) {
                    throw new ServerErrorException(e.getMessage(), e.getCause());
                } else {
                    ErrorResponse error = objectMapper.readValue(e.getResponseBodyAsString(), ErrorResponse.class);
                    throw new ServerErrorException(error.getMessage(), error.getException());
                }
            } catch (JsonProcessingException ex) {
                throw new IllegalArgumentException(String.format("Error processing server response '%s'.", e.getResponseBodyAsString()));
            }
        }
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