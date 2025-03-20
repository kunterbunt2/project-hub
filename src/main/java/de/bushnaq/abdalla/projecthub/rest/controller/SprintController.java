package de.bushnaq.abdalla.projecthub.rest.controller;

import de.bushnaq.abdalla.projecthub.dao.SprintEntity;
import de.bushnaq.abdalla.projecthub.repository.SprintRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/sprint")
public class SprintController {

    @Autowired
    private SprintRepository sprintRepository;

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        sprintRepository.deleteById(id);
    }

    @GetMapping("/{id}")
    public Optional<SprintEntity> get(@PathVariable Long id) {
        SprintEntity sprintEntity = sprintRepository.findById(id).orElseThrow();
        return Optional.of(sprintEntity);
    }

    @GetMapping
    public List<SprintEntity> getAll() {
        return sprintRepository.findAll();
    }

    @PostMapping(consumes = "application/json", produces = "application/json")
    public SprintEntity save(@RequestBody SprintEntity sprintEntity) {
        return sprintRepository.save(sprintEntity);
    }

    @PutMapping("/{id}")
    public SprintEntity update(@PathVariable Long id, @RequestBody SprintEntity sprintEntity) {
//        ProjectEntity project = projectRepository.findById(id).orElseThrow();
//        project.setName(projectDetails.getName());
//        project.setRequester(projectDetails.getRequester());
        return sprintRepository.save(sprintEntity);
    }
}