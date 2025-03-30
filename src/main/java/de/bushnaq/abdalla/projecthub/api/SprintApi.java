package de.bushnaq.abdalla.projecthub.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import de.bushnaq.abdalla.projecthub.dto.Sprint;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Arrays;
import java.util.List;

@Service
public class SprintApi extends AbstractApi {

    public SprintApi(RestTemplate restTemplate, ObjectMapper objectMapper, String baseUrl) {
        super(restTemplate, objectMapper, baseUrl);
    }

    public SprintApi() {
    }

    public SprintApi(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public List<Sprint> getAllSprints() {

        ResponseEntity<Sprint[]> response = executeWithErrorHandling(() -> restTemplate.getForEntity(
                baseUrl + "/sprint",
                Sprint[].class
        ));
        return Arrays.asList(response.getBody());
    }

    public Sprint getSprint(Long id) {
        return executeWithErrorHandling(() -> restTemplate.getForObject(
                baseUrl + "/sprint/{id}",
                Sprint.class,
                id
        ));
    }

    public Sprint persist(Sprint sprint, Long projectId) {
        return executeWithErrorHandling(() ->
                restTemplate.postForObject(
                        baseUrl + "/sprint/{projectId}",
                        sprint,
                        Sprint.class,
                        projectId
                ));
    }
}