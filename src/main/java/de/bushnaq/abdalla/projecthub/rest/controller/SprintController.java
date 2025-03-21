package de.bushnaq.abdalla.projecthub.rest.controller;

import de.bushnaq.abdalla.projecthub.dao.SprintDTO;
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
    public Optional<SprintDTO> get(@PathVariable Long id) {
        SprintDTO sprintEntity = sprintRepository.findById(id).orElseThrow();
        return Optional.of(sprintEntity);
    }

    @GetMapping
    public List<SprintDTO> getAll() {
        return sprintRepository.findAll();
    }

    @PostMapping(consumes = "application/json", produces = "application/json")
    public SprintDTO save(@RequestBody SprintDTO sprintEntity) {
        return sprintRepository.save(sprintEntity);
    }

    @PutMapping("/{id}")
    public SprintDTO update(@PathVariable Long id, @RequestBody SprintDTO sprintEntity) {
//        ProjectEntity project = projectRepository.findById(id).orElseThrow();
//        project.setName(projectDetails.getName());
//        project.setRequester(projectDetails.getRequester());
        return sprintRepository.save(sprintEntity);
    }
}