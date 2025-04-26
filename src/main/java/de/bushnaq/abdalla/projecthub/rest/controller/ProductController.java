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

package de.bushnaq.abdalla.projecthub.rest.controller;

import de.bushnaq.abdalla.projecthub.dao.ProductDAO;
import de.bushnaq.abdalla.projecthub.repository.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/product")
public class ProductController {

    @Autowired
    private ProductRepository productRepository;

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        productRepository.deleteById(id);
    }

    @GetMapping("/{id}")
    public Optional<ProductDAO> get(@PathVariable Long id) {
        ProductDAO productEntity = productRepository.findById(id).orElseThrow();
        return Optional.of(productEntity);
    }

    @GetMapping
    public List<ProductDAO> getAll() {
        return productRepository.findAll();
    }

    @PostMapping(consumes = "application/json;charset=UTF-8", produces = "application/json;charset=UTF-8")
    public ProductDAO save(@RequestBody ProductDAO product) {
        return productRepository.save(product);
    }

    @PutMapping()
    public void update(@RequestBody ProductDAO product) {
        productRepository.save(product);
    }
}