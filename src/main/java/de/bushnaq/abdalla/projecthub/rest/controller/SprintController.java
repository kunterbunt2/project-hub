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

import de.bushnaq.abdalla.projecthub.dao.SprintDAO;
import de.bushnaq.abdalla.projecthub.repository.FeatureRepository;
import de.bushnaq.abdalla.projecthub.repository.SprintRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/sprint")
public class SprintController {

    @Autowired
    private FeatureRepository featureRepository;
    @Autowired
    private SprintRepository  sprintRepository;

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public void delete(@PathVariable Long id) {
        sprintRepository.deleteById(id);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'USER')")
    public SprintDAO get(@PathVariable Long id) {
        SprintDAO sprintEntity = sprintRepository.findById(id).orElseThrow();
        return sprintEntity;
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'USER')")
    public List<SprintDAO> getAll() {
        return sprintRepository.findAll();
    }

    @GetMapping("/feature/{featureId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'USER')")
    public List<SprintDAO> getAll(@PathVariable Long featureId) {
        return sprintRepository.findByFeatureId(featureId);
    }

    @PostMapping()
    @PreAuthorize("hasRole('ADMIN')")
    public SprintDAO save(@RequestBody SprintDAO sprintDAO) {
        SprintDAO save = sprintRepository.save(sprintDAO);
        return save;
    }

    @PutMapping()
    @PreAuthorize("hasRole('ADMIN')")
    public SprintDAO update(@RequestBody SprintDAO sprintEntity) {
        return sprintRepository.save(sprintEntity);
    }
}