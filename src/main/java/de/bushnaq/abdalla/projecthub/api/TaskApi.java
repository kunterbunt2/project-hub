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

    public TaskApi(RestTemplate restTemplate, ObjectMapper objectMapper, String baseUrl) {
        super(restTemplate, objectMapper, baseUrl);
    }

    public TaskApi() {
    }

    public TaskApi(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public List<Task> getAllTasks() {

        ResponseEntity<Task[]> response = executeWithErrorHandling(() -> restTemplate.getForEntity(baseUrl + "/task", Task[].class));
        return Arrays.asList(response.getBody());
    }

    public Task getTask(Long id) {
        return executeWithErrorHandling(() -> restTemplate.getForObject(baseUrl + "/task/{id}", Task.class, id));
    }

    public Task persist(Task task, Long parentId, Long sprintId) {
        if (sprintId == null)
            throw new IllegalArgumentException("sprintId is required");
        if (parentId == null) {
            //root node
            return executeWithErrorHandling(() -> restTemplate.postForObject(baseUrl + "/task/{sprintId}", task, Task.class, sprintId));
        } else {
            //child node
            return executeWithErrorHandling(() -> restTemplate.postForObject(baseUrl + "/task/{sprintId}/{taskId}", task, Task.class, sprintId, parentId));
        }
    }

}