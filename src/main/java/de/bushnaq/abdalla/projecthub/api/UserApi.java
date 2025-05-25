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
import de.bushnaq.abdalla.projecthub.dto.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Arrays;
import java.util.List;

@Service
public class UserApi extends AbstractApi {

    public UserApi(RestTemplate restTemplate, ObjectMapper objectMapper, String baseUrl) {
        super(restTemplate, objectMapper, baseUrl);
    }

    @Autowired
    public UserApi(RestTemplate restTemplate, ObjectMapper objectMapper) {
        super(restTemplate, objectMapper);
    }

    public UserApi() {

    }

    public void deleteById(Long id) {
        executeWithErrorHandling(() -> restTemplate.delete(
                baseUrl + "/user/{id}",
                id
        ));
    }

    public List<User> getAll() {
        ResponseEntity<User[]> response = executeWithErrorHandling(() -> restTemplate.getForEntity(
                baseUrl + "/user",
                User[].class
        ));
        return Arrays.asList(response.getBody());
    }

    public List<User> getAll(Long sprintId) {
        ResponseEntity<User[]> response = executeWithErrorHandling(() -> restTemplate.getForEntity(
                baseUrl + "/user/sprint/{sprintId}",
                User[].class,
                sprintId
        ));
        return Arrays.asList(response.getBody());
    }

    public User getById(Long id) {
        return executeWithErrorHandling(() ->
                restTemplate.getForObject(
                        baseUrl + "/user/{id}",
                        User.class,
                        id
                ));
    }

    public User persist(User user) {
        return executeWithErrorHandling(() ->
                restTemplate.postForObject(
                        baseUrl + "/user",
                        user,
                        User.class
                ));
    }

    public void update(User user) {
        executeWithErrorHandling(() -> restTemplate.put(
                baseUrl + "/user",
                user
        ));
    }

}