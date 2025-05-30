/*
 *
 * Copyright (C) 2025-2025 Abdalla Bushnaq
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 */

package de.bushnaq.abdalla.projecthub.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import de.bushnaq.abdalla.projecthub.dto.Task;
import org.springframework.beans.factory.annotation.Autowired;
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

    @Autowired
    public TaskApi(RestTemplate restTemplate, ObjectMapper objectMapper) {
        super(restTemplate, objectMapper);
    }

    public TaskApi() {

    }


    //TODO missing deleteById restapi

    public List<Task> getAll() {

        ResponseEntity<Task[]> response = executeWithErrorHandling(() -> restTemplate.getForEntity(baseUrl + "/task", Task[].class));
        return Arrays.asList(response.getBody());
    }

    public List<Task> getAll(Long sprintId) {
        ResponseEntity<Task[]> response = executeWithErrorHandling(() -> restTemplate.getForEntity(
                baseUrl + "/task/sprint/{sprintId}",
                Task[].class,
                sprintId
        ));
        return Arrays.asList(response.getBody());
    }

    public Task getById(Long id) {
        return executeWithErrorHandling(() -> restTemplate.getForObject(baseUrl + "/task/{id}", Task.class, id));
    }

    public Task persist(Task task) {
        return executeWithErrorHandling(() -> restTemplate.postForObject(baseUrl + "/task", task, Task.class));
    }

    public void update(Task task) {
        executeWithErrorHandling(() -> restTemplate.put(
                baseUrl + "/task",
                task
        ));
    }

}