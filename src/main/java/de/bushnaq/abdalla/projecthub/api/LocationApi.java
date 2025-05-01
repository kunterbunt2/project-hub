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
import de.bushnaq.abdalla.projecthub.dto.Location;
import de.bushnaq.abdalla.projecthub.dto.User;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class LocationApi extends AbstractApi {

    public LocationApi(RestTemplate restTemplate, ObjectMapper objectMapper, String baseUrl) {
        super(restTemplate, objectMapper, baseUrl);
    }

    public LocationApi() {
    }

    public LocationApi(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    //TODO use ids instead of objects
    public void deleteById(User user, Location location) throws org.springframework.web.client.RestClientException {
        executeWithErrorHandling(() -> restTemplate.delete(
                baseUrl + "/location/{userId}/{id}",
                user.getId(),
                location.getId()
        ));
    }

    public Location getById(Long id) {
        return executeWithErrorHandling(() ->
                restTemplate.getForObject(
                        baseUrl + "/location/{id}",
                        Location.class,
                        id
                ));
    }

    public Location persist(Location location, Long userId) {
        return executeWithErrorHandling(() ->
                restTemplate.postForObject(
                        baseUrl + "/location/{userId}",
                        location,
                        Location.class,
                        userId
                ));
    }

    public void update(Location location, Long userId) {
        executeWithErrorHandling(() -> restTemplate.put(
                baseUrl + "/location/{userId}",
                location,
                userId
        ));
    }

}