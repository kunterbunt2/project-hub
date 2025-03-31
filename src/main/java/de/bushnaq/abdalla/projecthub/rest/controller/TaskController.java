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
import de.bushnaq.abdalla.projecthub.dao.TaskDAO;
import de.bushnaq.abdalla.projecthub.repository.SprintRepository;
import de.bushnaq.abdalla.projecthub.repository.TaskRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/task")
public class TaskController {

//    @Autowired
//    DebugUtil debugUtil;

    @Autowired
    private SprintRepository sprintRepository;
    @Autowired
    private TaskRepository   taskRepository;

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        taskRepository.deleteById(id);
    }

    @GetMapping("/{id}")
    public Optional<TaskDAO> get(@PathVariable Long id) throws JsonProcessingException {
        Optional<TaskDAO> task = taskRepository.findById(id);
        return task;
    }

    @GetMapping
    public List<TaskDAO> getAll() {
        return taskRepository.findAll();
    }

    @PostMapping
    public TaskDAO save(@RequestBody TaskDAO task) {
        return taskRepository.save(task);
    }

//    @PostMapping("/{sprintId}/{parentId}")
//    public TaskDAO save(@RequestBody TaskDAO task, @PathVariable Long sprintId, @PathVariable Long parentId) {
//        SprintDAO sprint = sprintRepository.getById(sprintId);
//        TaskDAO   parent = taskRepository.getById(parentId);
////        task.setParentTask(parent);
//        task.setSprint(sprint);
//        TaskDAO save = taskRepository.save(task);
//        return save;
//    }

//    @PutMapping()
//    public void update(@RequestBody TaskDAO user) {
//        TaskDAO e = taskRepository.findById(user.getId()).orElseThrow();
//        e.setLastWorkingDay(user.getLastWorkingDay());
//        e.setFirstWorkingDay(user.getFirstWorkingDay());
//        e.setEmail(user.getEmail());
//        e.setLastWorkingDay(user.getLastWorkingDay());
//        e.setName(user.getName());
//        taskRepository.save(e);
//    }
}