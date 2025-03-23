package de.bushnaq.abdalla.projecthub.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import de.bushnaq.abdalla.projecthub.dto.Project;
import de.bushnaq.abdalla.projecthub.dto.Sprint;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Arrays;
import java.util.List;

@Service
public class ProjectApi extends AbstractApi {

    public ProjectApi(RestTemplate restTemplate, ObjectMapper objectMapper, String baseUrl) {
        super(restTemplate, objectMapper, baseUrl);
    }

    public ProjectApi() {
    }

    public ProjectApi(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public List<Project> getAllProjects() {

        ResponseEntity<Project[]> response = executeWithErrorHandling(() -> restTemplate.getForEntity(
                baseUrl + "/project",
                Project[].class
        ));
        return Arrays.asList(response.getBody());
    }

    public Project getProduct(Long id) {
        return executeWithErrorHandling(() -> restTemplate.getForObject(
                baseUrl + "/project/{id}",
                Project.class,
                id
        ));
    }

    public Project persist(Project project, Long versionId) {
        return executeWithErrorHandling(() ->
                restTemplate.postForObject(
                        baseUrl + "/project/{versionId}",
                        project,
                        Project.class,
                        versionId
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
}