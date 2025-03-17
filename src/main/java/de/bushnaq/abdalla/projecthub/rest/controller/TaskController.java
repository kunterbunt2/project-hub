package de.bushnaq.abdalla.projecthub.rest.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import de.bushnaq.abdalla.projecthub.dao.TaskDAO;
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
    private TaskRepository taskRepository;

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

    @PostMapping(consumes = "application/json", produces = "application/json")
    public TaskDAO save(@RequestBody TaskDAO user) {
        return taskRepository.save(user);
    }

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