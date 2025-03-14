package de.bushnaq.abdalla.projecthub.client;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Arrays;
import java.util.List;

@Service
public class ProjectHubClient {
    private String       baseUrl = "http://localhost:8080"; // Configure as needed
    private RestTemplate restTemplate;

    public ProjectHubClient(RestTemplate restTemplate, String baseUrl) {
        this.restTemplate = restTemplate;
        this.baseUrl      = baseUrl;
    }

    public ProjectHubClient() {
    }

    public ProjectHubClient(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public Project createProject(Project project) {
        return restTemplate.postForObject(
                baseUrl + "/project",
                project,
                Project.class
        );
    }

    // Example usage with Sprint
    public Sprint createSprint(Sprint sprint, Long projectId, String version) {
        return restTemplate.postForObject(
                baseUrl + "/project/{id}/versions/{version}/sprints",
                sprint,
                Sprint.class,
                projectId,
                version
        );
    }

    public User createUser(User user) {
        return restTemplate.postForObject(
                baseUrl + "/user",
                user,
                User.class
        );
    }

    public List<Project> getAllProjects() {
        ResponseEntity<Project[]> response = restTemplate.getForEntity(
                baseUrl + "/project",
                Project[].class
        );
        return Arrays.asList(response.getBody());
    }

    public Project getProjectById(Long id) {
        return restTemplate.getForObject(
                baseUrl + "/project/{id}",
                Project.class,
                id
        );
    }
}