package de.bushnaq.abdalla.projecthub.rest.controller;

import de.bushnaq.abdalla.projecthub.dao.ProjectDAO;
import de.bushnaq.abdalla.projecthub.dao.SprintDAO;
import de.bushnaq.abdalla.projecthub.repository.ProjectRepository;
import de.bushnaq.abdalla.projecthub.repository.SprintRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/sprint")
public class SprintController {

    @Autowired
    private ProjectRepository projectRepository;
    @Autowired
    private SprintRepository  sprintRepository;

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        sprintRepository.deleteById(id);
    }

    @GetMapping("/{id}")
    public SprintDAO get(@PathVariable Long id) {
        SprintDAO sprintEntity = sprintRepository.findById(id).orElseThrow();
        return sprintEntity;
    }

    @GetMapping
    public List<SprintDAO> getAll() {
        return sprintRepository.findAll();
    }

//    @PostMapping(consumes = "application/json", produces = "application/json")
//    public SprintDAO save(@RequestBody SprintDAO sprintEntity) {
//        return sprintRepository.save(sprintEntity);
//    }

    @PostMapping("/{projectId}")
    public SprintDAO save(@RequestBody SprintDAO sprintDAO, @PathVariable Long projectId) {
        ProjectDAO project = projectRepository.getById(projectId);
//        sprintDAO.setProject(project);
        SprintDAO save = sprintRepository.save(sprintDAO);
        return save;
    }

    @PutMapping("/{id}")
    public SprintDAO update(@PathVariable Long id, @RequestBody SprintDAO sprintEntity) {
//        ProjectEntity project = projectRepository.findById(id).orElseThrow();
//        project.setName(projectDetails.getName());
//        project.setRequester(projectDetails.getRequester());
        return sprintRepository.save(sprintEntity);
    }
}