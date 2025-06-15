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

package de.bushnaq.abdalla.projecthub.rest.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import de.bushnaq.abdalla.projecthub.dto.Sprint;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Arrays;
import java.util.List;

@Service
public class SprintApi extends AbstractApi {

    @Autowired
    public SprintApi(RestTemplate restTemplate, ObjectMapper objectMapper) {
        super(restTemplate, objectMapper);
    }

    public SprintApi(RestTemplate restTemplate, ObjectMapper objectMapper, String baseUrl) {
        super(restTemplate, objectMapper, baseUrl);
    }

    public SprintApi() {

    }

    public void deleteById(long id) {
        executeWithErrorHandling(() -> restTemplate.exchange(
                getBaseUrl() + "/sprint/{id}",
                HttpMethod.DELETE,
                createHttpEntity(),
                Void.class,
                id
        ));
    }

    public List<Sprint> getAll() {
        ResponseEntity<Sprint[]> response = executeWithErrorHandling(() -> restTemplate.exchange(
                getBaseUrl() + "/sprint",
                HttpMethod.GET,
                createHttpEntity(),
                Sprint[].class
        ));
        return Arrays.asList(response.getBody());
    }

    public List<Sprint> getAll(Long featureId) {
        ResponseEntity<Sprint[]> response = executeWithErrorHandling(() -> restTemplate.exchange(
                getBaseUrl() + "/sprint/feature/{featureId}",
                HttpMethod.GET,
                createHttpEntity(),
                Sprint[].class,
                featureId
        ));
        return Arrays.asList(response.getBody());
    }

    public Sprint getById(Long id) {
        ResponseEntity<Sprint> response = executeWithErrorHandling(() -> restTemplate.exchange(
                getBaseUrl() + "/sprint/{id}",
                HttpMethod.GET,
                createHttpEntity(),
                Sprint.class,
                id
        ));
        return response.getBody();
    }

    public Sprint persist(Sprint sprint) {
        ResponseEntity<Sprint> response = executeWithErrorHandling(() -> restTemplate.exchange(
                getBaseUrl() + "/sprint",
                HttpMethod.POST,
                createHttpEntity(sprint),
                Sprint.class
        ));
        return response.getBody();
    }

    public void update(Sprint sprint) {
        executeWithErrorHandling(() -> restTemplate.exchange(
                getBaseUrl() + "/sprint",
                HttpMethod.PUT,
                createHttpEntity(sprint),
                Void.class
        ));
    }
}
