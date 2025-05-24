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
import de.bushnaq.abdalla.projecthub.dto.Availability;
import de.bushnaq.abdalla.projecthub.dto.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class AvailabilityApi extends AbstractApi {

    public AvailabilityApi(RestTemplate restTemplate, ObjectMapper objectMapper, String baseUrl) {
        super(restTemplate, objectMapper, baseUrl);
    }

    @Autowired
    public AvailabilityApi(RestTemplate restTemplate, ObjectMapper objectMapper) {
        super(restTemplate, objectMapper);
    }

    public AvailabilityApi() {
    }

    //TODO use ids instead of objects
    public void deleteById(User user, Availability availability) throws org.springframework.web.client.RestClientException {
        executeWithErrorHandling(() -> restTemplate.delete(
                baseUrl + "/availability/{userId}/{id}",
                user.getId(),
                availability.getId()
        ));
    }

    public Availability getById(Long id) {
        return executeWithErrorHandling(() ->
                restTemplate.getForObject(
                        baseUrl + "/availability/{id}",
                        Availability.class,
                        id
                ));
    }

    public Availability persist(Availability availability, Long userId) {
        return executeWithErrorHandling(() ->
                restTemplate.postForObject(
                        baseUrl + "/availability/{userId}",
                        availability,
                        Availability.class,
                        userId
                ));
    }

    public void update(Availability availability, Long userId) {
        executeWithErrorHandling(() -> restTemplate.put(
                baseUrl + "/availability/{userId}",
                availability,
                userId
        ));
    }

}