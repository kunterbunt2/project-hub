package de.bushnaq.abdalla.projecthub.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import de.bushnaq.abdalla.projecthub.dto.Task;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Arrays;
import java.util.List;

@Service
public class TaskApi extends AbstractApi {
//    private String       baseUrl = "http://localhost:8080"; // Configure as needed
//    private ObjectMapper objectMapper;
//    private RestTemplate restTemplate;

    public TaskApi(RestTemplate restTemplate, ObjectMapper objectMapper, String baseUrl) {
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

    public TaskApi() {
    }

    public TaskApi(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
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

    public List<Task> getAllTasks() {

        ResponseEntity<Task[]> response = executeWithErrorHandling(() -> restTemplate.getForEntity(baseUrl + "/task", Task[].class));
        return Arrays.asList(response.getBody());
    }

    public Task getTask(Long id) {
        return executeWithErrorHandling(() -> restTemplate.getForObject(baseUrl + "/task/{id}", Task.class, id));
    }

    public Task persist(Task task) {
        return executeWithErrorHandling(() -> restTemplate.postForObject(baseUrl + "/task", task, Task.class));
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