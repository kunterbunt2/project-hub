package de.bushnaq.abdalla.projecthub.client;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.DefaultResponseErrorHandler;
import org.springframework.web.client.RestTemplate;

import java.util.Arrays;
import java.util.List;

@Service
public class ProjectHubClient {
    private String       baseUrl = "http://localhost:8080"; // Configure as needed
    private RestTemplate restTemplate;

    public ProjectHubClient(RestTemplate restTemplate, String baseUrl) {
        this.restTemplate = restTemplate;
        this.restTemplate.setErrorHandler(new DefaultResponseErrorHandler());

        this.baseUrl = baseUrl;
    }

    public ProjectHubClient() {
    }

    public ProjectHubClient(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public void delete(User user, Location location) throws org.springframework.web.client.RestClientException {
        try {
            restTemplate.delete(
                    baseUrl + "/location/{userId}/{id}",
                    user.getId(),
                    location.getId()
            );
        } catch (Exception e) {
            throw new IllegalArgumentException("Failed to delete location: " + e.getMessage(), e);
        }
    }

    public List<Project> getAllProjects() {
        ResponseEntity<Project[]> response = restTemplate.getForEntity(
                baseUrl + "/project",
                Project[].class
        );
        return Arrays.asList(response.getBody());
    }

    public List<User> getAllUsers() {
        ResponseEntity<User[]> response = restTemplate.getForEntity(
                baseUrl + "/user",
                User[].class
        );
        return Arrays.asList(response.getBody());
    }

    public Location getLocation(Long id) {
        return restTemplate.getForObject(
                baseUrl + "/location/{id}",
                Location.class,
                id
        );
    }

    public Project getProject(Long id) {
        return restTemplate.getForObject(
                baseUrl + "/project/{id}",
                Project.class,
                id
        );
    }

    public User getUser(Long id) {
        return restTemplate.getForObject(
                baseUrl + "/user/{id}",
                User.class,
                id
        );
    }

    public Project persist(Project project) {
        return restTemplate.postForObject(
                baseUrl + "/project",
                project,
                Project.class
        );
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
        return restTemplate.postForObject(
                baseUrl + "/user",
                user,
                User.class
        );
    }

    public void update(Location location) {
        restTemplate.put(
                baseUrl + "/location",
                location
        );
    }

    public void update(User user) {
        restTemplate.put(
                baseUrl + "/user",
                user
        );
    }
}