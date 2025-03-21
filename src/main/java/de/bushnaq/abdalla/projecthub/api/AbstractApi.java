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
        restTemplate.getMessageConverters().add(new MappingJackson2HttpMessageConverter());
    }

    protected AbstractApi() {
    }

//    protected AbstractApi(RestTemplate restTemplate) {
//        this.restTemplate = restTemplate;
//    }

    protected void executeWithErrorHandling(RestOperation operation) {
        try {
            operation.execute();
        } catch (HttpClientErrorException e) {
            try {
                ErrorResponse error = objectMapper.readValue(e.getResponseBodyAsString(), ErrorResponse.class);
                throw new ServerErrorException(error.getMessage(), error.getException());
            } catch (JsonProcessingException ex) {
                throw new IllegalArgumentException(String.format("Error processing server response '%s'.", e.getResponseBodyAsString()));
            }
        }
    }

    protected <T> T executeWithErrorHandling(RestOperationWithResult<T> operation) {
        try {
            return operation.execute();
        } catch (HttpClientErrorException e) {
            try {
                ErrorResponse error = objectMapper.readValue(e.getResponseBodyAsString(), ErrorResponse.class);
                throw new ServerErrorException(error.getMessage(), error.getException());
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