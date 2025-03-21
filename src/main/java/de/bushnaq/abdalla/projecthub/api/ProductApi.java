package de.bushnaq.abdalla.projecthub.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import de.bushnaq.abdalla.projecthub.dto.Product;
import de.bushnaq.abdalla.projecthub.dto.Version;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Arrays;
import java.util.List;

@Service
public class ProductApi extends AbstractApi {
//    private String       baseUrl = "http://localhost:8080"; // Configure as needed
//    private ObjectMapper objectMapper;
//    private RestTemplate restTemplate;

    public ProductApi(RestTemplate restTemplate, ObjectMapper objectMapper, String baseUrl) {
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

    public ProductApi() {
    }

    public ProductApi(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public void deleteById(Long id) {
        executeWithErrorHandling(() -> restTemplate.delete(
                baseUrl + "/product/{id}",
                id
        ));
    }

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
//
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

    public List<Product> getAllProducts() {

        ResponseEntity<Product[]> response = executeWithErrorHandling(() -> restTemplate.getForEntity(
                baseUrl + "/project",
                Product[].class
        ));
        return Arrays.asList(response.getBody());
    }

    public Product getProduct(Long id) {
        return executeWithErrorHandling(() -> restTemplate.getForObject(
                baseUrl + "/product/{id}",
                Product.class,
                id
        ));
    }

    public Product persist(Product product) {
        return executeWithErrorHandling(() ->
                restTemplate.postForObject(
                        baseUrl + "/product",
                        product,
                        Product.class
                ));
    }

//    public Project persist(Project project) {
//        return executeWithErrorHandling(() ->
//                restTemplate.postForObject(
//                        baseUrl + "/project",
//                        project,
//                        Project.class
//                ));
//    }

    public Version persist(Version version) {
        return executeWithErrorHandling(() ->
                restTemplate.postForObject(
                        baseUrl + "/version",
                        version,
                        Version.class
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