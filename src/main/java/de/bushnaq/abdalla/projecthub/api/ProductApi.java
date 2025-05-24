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
        executeWithErrorHandling(() -> restTemplate.delete(
                baseUrl + "/product/{id}",
                id
        ));
    }

    public List<Product> getAll() {
        ResponseEntity<Product[]> response = executeWithErrorHandling(() -> restTemplate.getForEntity(
                baseUrl + "/product",
                Product[].class
        ));
        return Arrays.asList(response.getBody());
    }

    public Product getById(Long id) {
        return executeWithErrorHandling(() ->
                restTemplate.getForObject(
                        baseUrl + "/product/{id}",
                        Product.class,
                        id
                ));
    }

    public Product persist(Product product) {
        return executeWithErrorHandling(() ->
                restTemplate.postForObject(
                        baseUrl + "/product",
                        product,
                        Product.class
                ));
    }

    public void update(Product product) {
        executeWithErrorHandling(() -> restTemplate.put(
                baseUrl + "/product",
                product
        ));
    }

}