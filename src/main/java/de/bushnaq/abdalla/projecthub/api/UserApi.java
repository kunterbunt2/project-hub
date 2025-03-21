package de.bushnaq.abdalla.projecthub.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import de.bushnaq.abdalla.projecthub.dto.Availability;
import de.bushnaq.abdalla.projecthub.dto.Location;
import de.bushnaq.abdalla.projecthub.dto.OffDay;
import de.bushnaq.abdalla.projecthub.dto.User;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Arrays;
import java.util.List;

@Service
public class UserApi extends AbstractApi {
//    private String       baseUrl = "http://localhost:8080"; // Configure as needed
//    private ObjectMapper objectMapper;
//    private RestTemplate restTemplate;

    public UserApi(RestTemplate restTemplate, ObjectMapper objectMapper, String baseUrl) {
        super(restTemplate, objectMapper, baseUrl);
//        this.restTemplate = restTemplate;
//        this.objectMapper = objectMapper;
//        this.restTemplate.setErrorHandler(new DefaultResponseErrorHandler());
//
//        this.baseUrl = baseUrl;
//        // Configure message converters for JSON
//        restTemplate.getMessageConverters().clear();
//        restTemplate.getMessageConverters().add(new MappingJackson2HttpMessageConverter());
    }

    public UserApi() {
    }

    public UserApi(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public void delete(User user, Location location) throws org.springframework.web.client.RestClientException {
        executeWithErrorHandling(() -> restTemplate.delete(
                baseUrl + "/location/{userId}/{id}",
                user.getId(),
                location.getId()
        ));
    }

    public void delete(User user, OffDay offDay) throws org.springframework.web.client.RestClientException {
        executeWithErrorHandling(() -> restTemplate.delete(
                baseUrl + "/offday/{userId}/{id}",
                user.getId(),
                offDay.getId()
        ));
    }

    public void delete(User user, Availability availability) throws org.springframework.web.client.RestClientException {
        executeWithErrorHandling(() -> restTemplate.delete(
                baseUrl + "/availability/{userId}/{id}",
                user.getId(),
                availability.getId()
        ));
    }

//    private void executeWithErrorHandling(RestOperation operation) {
//        try {
//            operation.execute();
//        } catch (HttpClientErrorException e) {
//            try {
//                ErrorResponse error = objectMapper.readValue(e.getResponseBodyAsString(), ErrorResponse.class);
//                throw new ServerErrorException(error.getMessage(), error.getException());
//            } catch (JsonProcessingException ex) {
//                throw new IllegalArgumentException(String.format("Error processing server response '%s'.", e.getResponseBodyAsString()));
//            }
//        }
//    }
//
//    private <T> T executeWithErrorHandling(RestOperationWithResult<T> operation) {
//        try {
//            return operation.execute();
//        } catch (HttpClientErrorException e) {
//            try {
//                ErrorResponse error = objectMapper.readValue(e.getResponseBodyAsString(), ErrorResponse.class);
//                throw new ServerErrorException(error.getMessage(), error.getException());
//            } catch (JsonProcessingException ex) {
//                throw new IllegalArgumentException(String.format("Error processing server response '%s'.", e.getResponseBodyAsString()));
//            }
//        }
//    }

    public List<User> getAllUsers() {

        ResponseEntity<User[]> response = executeWithErrorHandling(() -> restTemplate.getForEntity(
                baseUrl + "/user",
                User[].class
        ));
        return Arrays.asList(response.getBody());
    }

    public Availability getAvailability(Long id) {
        return executeWithErrorHandling(() ->
                restTemplate.getForObject(
                        baseUrl + "/availability/{id}",
                        Availability.class,
                        id
                ));
    }

    public Location getLocation(Long id) {
        return executeWithErrorHandling(() ->
                restTemplate.getForObject(
                        baseUrl + "/location/{id}",
                        Location.class,
                        id
                ));
    }

    public OffDay getOffDay(Long id) {
        return executeWithErrorHandling(() ->
                restTemplate.getForObject(
                        baseUrl + "/offday/{id}",
                        OffDay.class,
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

    public void update(OffDay offDay) {
        executeWithErrorHandling(() -> restTemplate.put(
                baseUrl + "/offday",
                offDay
        ));
    }

    public void update(Availability availability) {
        executeWithErrorHandling(() -> restTemplate.put(
                baseUrl + "/availability",
                availability
        ));
    }

    public void update(User user) {
        executeWithErrorHandling(() -> restTemplate.put(
                baseUrl + "/user",
                user
        ));
    }

//    @FunctionalInterface
//    private interface RestOperation {
//        void execute() throws HttpClientErrorException;
//    }
//
//    @FunctionalInterface
//    private interface RestOperationWithResult<T> {
//        T execute() throws HttpClientErrorException;
//    }
}