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

    public List<Sprint> getAll() {

        ResponseEntity<Sprint[]> response = executeWithErrorHandling(() -> restTemplate.getForEntity(
                baseUrl + "/sprint",
                Sprint[].class
        ));
        return Arrays.asList(response.getBody());
    }

    public Sprint getById(Long id) {
        return executeWithErrorHandling(() -> restTemplate.getForObject(
                baseUrl + "/sprint/{id}",
                Sprint.class,
                id
        ));
    }

    public Sprint persist(Sprint sprint) {
        return executeWithErrorHandling(() ->
                restTemplate.postForObject(
                        baseUrl + "/sprint",
                        sprint,
                        Sprint.class
                ));
    }
}