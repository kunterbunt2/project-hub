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
import de.bushnaq.abdalla.projecthub.dto.Project;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpMethod;
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

    @Autowired
    public ProjectApi(RestTemplate restTemplate, ObjectMapper objectMapper) {
        super(restTemplate, objectMapper);
    }

    public ProjectApi() {

    }

    public void deleteById(long id) {
        executeWithErrorHandling(() -> restTemplate.exchange(
                baseUrl + "/project/{id}",
                HttpMethod.DELETE,
                createHttpEntity(),
                Void.class,
                id
        ));
    }

    public List<Project> getAll() {
        ResponseEntity<Project[]> response = executeWithErrorHandling(() -> restTemplate.exchange(
                baseUrl + "/project",
                HttpMethod.GET,
                createHttpEntity(),
                Project[].class
        ));
        return Arrays.asList(response.getBody());
    }

    public List<Project> getAll(Long versionId) {
        ResponseEntity<Project[]> response = executeWithErrorHandling(() -> restTemplate.exchange(
                baseUrl + "/project/version/{versionId}",
                HttpMethod.GET,
                createHttpEntity(),
                Project[].class,
                versionId
        ));
        return Arrays.asList(response.getBody());
    }

    public Project getById(Long id) {
        ResponseEntity<Project> response = executeWithErrorHandling(() -> restTemplate.exchange(
                baseUrl + "/project/{id}",
                HttpMethod.GET,
                createHttpEntity(),
                Project.class,
                id
        ));
        return response.getBody();
    }

    public Project persist(Project project) {
        ResponseEntity<Project> response = executeWithErrorHandling(() -> restTemplate.exchange(
                baseUrl + "/project",
                HttpMethod.POST,
                createHttpEntity(project),
                Project.class
        ));
        return response.getBody();
    }

    public void update(Project project) {
        executeWithErrorHandling(() -> restTemplate.exchange(
                baseUrl + "/project",
                HttpMethod.PUT,
                createHttpEntity(project),
                Void.class
        ));
    }
}