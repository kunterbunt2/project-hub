package de.bushnaq.abdalla.projecthub.controller;

import de.bushnaq.abdalla.projecthub.db.ProjectEntity;
import de.bushnaq.abdalla.projecthub.db.repository.ProjectRepository;
import de.bushnaq.abdalla.projecthub.service.ProjectService;
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
    private ProjectService projectService;

    @PostMapping(consumes = "application/json", produces = "application/json")
    public ProjectEntity create(@RequestBody ProjectEntity project) {
        return projectService.createProject(project);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        projectRepository.deleteById(id);
    }

    @GetMapping
    public List<ProjectEntity> getAll() {
        return projectService.getAllProjects();
    }

    @GetMapping("/{id}")
    public Optional<ProjectEntity> getById(@PathVariable Long id) {
        return projectService.getProjectById(id);
    }

    @PutMapping("/{id}")
    public ProjectEntity update(@PathVariable Long id, @RequestBody ProjectEntity projectDetails) {
        ProjectEntity project = projectRepository.findById(id).orElseThrow();
        project.setName(projectDetails.getName());
        project.setRequester(projectDetails.getRequester());
        return projectRepository.save(project);
    }
}