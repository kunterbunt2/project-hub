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

import com.fasterxml.jackson.core.JsonProcessingException;
import de.bushnaq.abdalla.projecthub.dao.WorklogDAO;
import de.bushnaq.abdalla.projecthub.repository.WorklogRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/worklog")
public class WorklogController {

    @Autowired
    private WorklogRepository worklogRepository;

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        worklogRepository.deleteById(id);
    }

    @GetMapping("/{id}")
    public Optional<WorklogDAO> get(@PathVariable Long id) throws JsonProcessingException {
        Optional<WorklogDAO> task = worklogRepository.findById(id);
        return task;
    }

    @GetMapping("/sprint/{sprintId}")
    public List<WorklogDAO> getBySprintId(@PathVariable Long sprintId) {
        return worklogRepository.findBySprintId(sprintId);
    }

    @PostMapping
    public WorklogDAO save(@RequestBody WorklogDAO worklog) {
        return worklogRepository.save(worklog);
    }

}