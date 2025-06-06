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

import de.bushnaq.abdalla.projecthub.dao.ProjectDAO;
import de.bushnaq.abdalla.projecthub.repository.ProjectRepository;
import de.bushnaq.abdalla.projecthub.repository.VersionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/project")
public class ProjectController {

    @Autowired
    private ProjectRepository projectRepository;
    @Autowired
    private VersionRepository versionRepository;

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public void delete(@PathVariable Long id) {
        projectRepository.deleteById(id);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'USER')")
    public ResponseEntity<ProjectDAO> get(@PathVariable Long id) {
        return projectRepository.findById(id).map(ResponseEntity::ok).orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/version/{versionId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'USER')")
    public List<ProjectDAO> getAll(@PathVariable Long versionId) {
        return projectRepository.findByVersionId(versionId);
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'USER')")
    public List<ProjectDAO> getAll() {
        return projectRepository.findAll();
    }

    @PostMapping()
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ProjectDAO> save(@RequestBody ProjectDAO project) {
        return versionRepository.findById(project.getVersionId()).map(version -> {
            ProjectDAO save = projectRepository.save(project);
            return ResponseEntity.ok(save);
        }).orElse(ResponseEntity.notFound().build());
    }

    @PutMapping()
    @PreAuthorize("hasRole('ADMIN')")
    public ProjectDAO update(@RequestBody ProjectDAO project) {
        return projectRepository.save(project);
    }
}

