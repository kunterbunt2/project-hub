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
import de.bushnaq.abdalla.projecthub.dto.Version;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Arrays;
import java.util.List;

@Service
public class VersionApi extends AbstractApi {

    public VersionApi(RestTemplate restTemplate, ObjectMapper objectMapper, String baseUrl) {
        super(restTemplate, objectMapper, baseUrl);
    }

    public VersionApi() {
    }

    public VersionApi(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public void deleteById(Long id) {
        executeWithErrorHandling(() -> restTemplate.delete(
                baseUrl + "/version/{id}",
                id
        ));
    }

    public List<Version> getAll() {
        ResponseEntity<Version[]> response = executeWithErrorHandling(() -> restTemplate.getForEntity(
                baseUrl + "/version",
                Version[].class
        ));
        return Arrays.asList(response.getBody());
    }

    public List<Version> getAll(Long productId) {
        ResponseEntity<Version[]> response = executeWithErrorHandling(() -> restTemplate.getForEntity(
                baseUrl + "/version/product/{productId}",
                Version[].class,
                productId
        ));
        return Arrays.asList(response.getBody());
    }

    public Version getById(Long id) {
        return executeWithErrorHandling(() ->
                restTemplate.getForObject(
                        baseUrl + "/version/{id}",
                        Version.class,
                        id
                ));
    }

    public Version persist(Version version, Long productId) {
        return executeWithErrorHandling(() ->
                restTemplate.postForObject(
                        baseUrl + "/version/{productId}",
                        version,
                        Version.class,
                        productId
                ));
    }

    public void update(Version version) {
        executeWithErrorHandling(() -> restTemplate.put(
                baseUrl + "/version",
                version
        ));
    }

}