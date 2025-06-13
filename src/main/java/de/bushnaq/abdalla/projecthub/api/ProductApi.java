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
import de.bushnaq.abdalla.projecthub.dto.Product;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Arrays;
import java.util.List;

@Service
public class ProductApi extends AbstractApi {

    public ProductApi(RestTemplate restTemplate, ObjectMapper objectMapper, String baseUrl) {
        super(restTemplate, objectMapper, baseUrl);
    }

    @Autowired
    public ProductApi(RestTemplate restTemplate, ObjectMapper objectMapper) {
        super(restTemplate, objectMapper);
    }

    public ProductApi() {

    }

    public void deleteById(Long id) {
        executeWithErrorHandling(() -> restTemplate.exchange(
                getBaseUrl() + "/product/{id}",
                HttpMethod.DELETE,
                createHttpEntity(),
                Void.class,
                id
        ));
    }

    public List<Product> getAll() {
        ResponseEntity<Product[]> response = executeWithErrorHandling(() -> restTemplate.exchange(
                getBaseUrl() + "/product",
                HttpMethod.GET,
                createHttpEntity(),
                Product[].class
        ));
        return Arrays.asList(response.getBody());
    }

    public Product getById(Long id) {
        ResponseEntity<Product> response = executeWithErrorHandling(() -> restTemplate.exchange(
                getBaseUrl() + "/product/{id}",
                HttpMethod.GET,
                createHttpEntity(),
                Product.class,
                id
        ));
        return response.getBody();
    }

    public Product persist(Product product) {
        ResponseEntity<Product> response = executeWithErrorHandling(() -> restTemplate.exchange(
                getBaseUrl() + "/product",
                HttpMethod.POST,
                createHttpEntity(product),
                Product.class
        ));
        return response.getBody();
    }

    public void update(Product product) {
        executeWithErrorHandling(() -> restTemplate.exchange(
                getBaseUrl() + "/product",
                HttpMethod.PUT,
                createHttpEntity(product),
                Void.class
        ));
    }
}
