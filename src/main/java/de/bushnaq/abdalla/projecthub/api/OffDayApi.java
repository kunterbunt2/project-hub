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
import de.bushnaq.abdalla.projecthub.dto.OffDay;
import de.bushnaq.abdalla.projecthub.dto.User;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class OffDayApi extends AbstractApi {

    public OffDayApi(RestTemplate restTemplate, ObjectMapper objectMapper, String baseUrl) {
        super(restTemplate, objectMapper, baseUrl);
    }

    public OffDayApi() {
    }

    public OffDayApi(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    //TODO use ids instead of objects
    public void deleteById(User user, OffDay offDay) throws org.springframework.web.client.RestClientException {
        executeWithErrorHandling(() -> restTemplate.delete(
                baseUrl + "/offday/{userId}/{id}",
                user.getId(),
                offDay.getId()
        ));
    }

    public OffDay getById(Long id) {
        return executeWithErrorHandling(() ->
                restTemplate.getForObject(
                        baseUrl + "/offday/{id}",
                        OffDay.class,
                        id
                ));
    }

    public OffDay persist(OffDay offDay, Long userId) {
        return executeWithErrorHandling(() ->
                restTemplate.postForObject(
                        baseUrl + "/offday/{userId}",
                        offDay,
                        OffDay.class,
                        userId
                ));
    }

    public void update(OffDay offDay, Long userId) {
        executeWithErrorHandling(() -> restTemplate.put(
                baseUrl + "/offday/{userId}",
                offDay,
                userId
        ));
    }

}