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
import de.bushnaq.abdalla.projecthub.dto.Worklog;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Arrays;
import java.util.List;

@Service
public class WorklogApi extends AbstractApi {

    public WorklogApi(RestTemplate restTemplate, ObjectMapper objectMapper, String baseUrl) {
        super(restTemplate, objectMapper, baseUrl);
    }

    @Autowired
    public WorklogApi(RestTemplate restTemplate, ObjectMapper objectMapper) {
        super(restTemplate, objectMapper);
    }

    public WorklogApi() {

    }

    public List<Worklog> getAll(Long sprintId) {
        ResponseEntity<Worklog[]> response = executeWithErrorHandling(() -> restTemplate.exchange(
                baseUrl + "/worklog/sprint/{sprintId}",
                HttpMethod.GET,
                createHttpEntity(),
                Worklog[].class,
                sprintId
        ));
        return Arrays.asList(response.getBody());
    }

    public List<Worklog> getAll() {
        ResponseEntity<Worklog[]> response = executeWithErrorHandling(() -> restTemplate.exchange(
                baseUrl + "/worklog",
                HttpMethod.GET,
                createHttpEntity(),
                Worklog[].class
        ));
        return Arrays.asList(response.getBody());
    }

    public Worklog getWorklog(Long id) {
        ResponseEntity<Worklog> response = executeWithErrorHandling(() -> restTemplate.exchange(
                baseUrl + "/worklog/{id}",
                HttpMethod.GET,
                createHttpEntity(),
                Worklog.class,
                id
        ));
        return response.getBody();
    }

    public Worklog persist(Worklog worklog) {
        ResponseEntity<Worklog> response = executeWithErrorHandling(() -> restTemplate.exchange(
                baseUrl + "/worklog",
                HttpMethod.POST,
                createHttpEntity(worklog),
                Worklog.class
        ));
        return response.getBody();
    }
}