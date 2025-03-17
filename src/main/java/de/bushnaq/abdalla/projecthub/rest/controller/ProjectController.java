package de.bushnaq.abdalla.projecthub.rest.controller;

import de.bushnaq.abdalla.projecthub.dao.ProjectEntity;
import de.bushnaq.abdalla.projecthub.repository.ProjectRepository;
import de.bushnaq.abdalla.projecthub.repository.VersionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/project")
public class ProjectController {

    @Autowired
    private ProjectRepository projectRepository;

    @Autowired
    private VersionRepository versionRepository;

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        projectRepository.deleteById(id);
    }

    @GetMapping("/{id}")
    public Optional<ProjectEntity> get(@PathVariable Long id) {
        ProjectEntity projectEntity = projectRepository.findById(id).orElseThrow();
        return Optional.of(projectEntity);
    }

    @GetMapping
    public List<ProjectEntity> getAll() {
        return projectRepository.findAll();
    }

    @PostMapping(consumes = "application/json", produces = "application/json")
    public ProjectEntity save(@RequestBody ProjectEntity project) {
        return projectRepository.save(project);
    }

    @PutMapping("/{id}")
    public ProjectEntity update(@PathVariable Long id, @RequestBody ProjectEntity project) {
//        ProjectEntity project = projectRepository.findById(id).orElseThrow();
//        project.setName(projectDetails.getName());
//        project.setRequester(projectDetails.getRequester());
        return projectRepository.save(project);
    }
}