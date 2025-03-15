package de.bushnaq.abdalla.projecthub.client;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import de.bushnaq.abdalla.projecthub.rest.util.ErrorResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.stereotype.Service;
import org.springframework.web.client.DefaultResponseErrorHandler;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.server.ServerErrorException;

import java.util.Arrays;
import java.util.List;

@Service
public class Client {
    private String       baseUrl = "http://localhost:8080"; // Configure as needed
    private ObjectMapper objectMapper;
    private RestTemplate restTemplate;

    public Client(RestTemplate restTemplate, ObjectMapper objectMapper, String baseUrl) {
        this.restTemplate = restTemplate;
        this.objectMapper = objectMapper;
        this.restTemplate.setErrorHandler(new DefaultResponseErrorHandler());

        this.baseUrl = baseUrl;
        // Configure message converters for JSON
        restTemplate.getMessageConverters().clear();
        restTemplate.getMessageConverters().add(new MappingJackson2HttpMessageConverter());
    }

    public Client() {
    }

    public Client(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public void delete(User user, Location location) throws org.springframework.web.client.RestClientException {
        executeWithErrorHandling(() -> restTemplate.delete(
                baseUrl + "/location/{userId}/{id}",
                user.getId(),
                location.getId()
        ));
    }

    private void executeWithErrorHandling(RestOperation operation) {
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

    private <T> T executeWithErrorHandling(RestOperationWithResult<T> operation) {
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

    public List<Project> getAllProjects() {

        ResponseEntity<Project[]> response = executeWithErrorHandling(() -> restTemplate.getForEntity(
                baseUrl + "/project",
                Project[].class
        ));
        return Arrays.asList(response.getBody());
    }

    public List<User> getAllUsers() {

        ResponseEntity<User[]> response = executeWithErrorHandling(() -> restTemplate.getForEntity(
                baseUrl + "/user",
                User[].class
        ));
        return Arrays.asList(response.getBody());
    }

    public Location getLocation(Long id) {
        return executeWithErrorHandling(() ->
                restTemplate.getForObject(
                        baseUrl + "/location/{id}",
                        Location.class,
                        id
                ));
    }

    public Project getProject(Long id) {
        return executeWithErrorHandling(() -> restTemplate.getForObject(
                baseUrl + "/project/{id}",
                Project.class,
                id
        ));
    }

    public User getUser(Long id) {
        return executeWithErrorHandling(() ->
                restTemplate.getForObject(
                        baseUrl + "/user/{id}",
                        User.class,
                        id
                ));
    }

    public Project persist(Project project) {
        return executeWithErrorHandling(() ->
                restTemplate.postForObject(
                        baseUrl + "/project",
                        project,
                        Project.class
                ));
    }

    // Example usage with Sprint
//    public Sprint persist(Sprint sprint, Long projectId, String version) {
//        return restTemplate.postForObject(
//                baseUrl + "/project/{id}/versions/{version}/sprints",
//                sprint,
//                Sprint.class,
//                projectId,
//                version
//        );
//    }

    public User persist(User user) {
        return executeWithErrorHandling(() ->
                restTemplate.postForObject(
                        baseUrl + "/user",
                        user,
                        User.class
                ));
    }

    public void update(Location location) {
        executeWithErrorHandling(() -> restTemplate.put(
                baseUrl + "/location",
                location
        ));
    }

    public void update(User user) {
        executeWithErrorHandling(() -> restTemplate.put(
                baseUrl + "/user",
                user
        ));
    }

    @FunctionalInterface
    private interface RestOperation {
        void execute() throws HttpClientErrorException;
    }

    @FunctionalInterface
    private interface RestOperationWithResult<T> {
        T execute() throws HttpClientErrorException;
    }
}