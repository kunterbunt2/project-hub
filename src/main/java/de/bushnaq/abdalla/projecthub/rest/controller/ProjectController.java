package de.bushnaq.abdalla.projecthub.rest.controller;

import de.bushnaq.abdalla.projecthub.dao.ProjectDAO;
import de.bushnaq.abdalla.projecthub.dao.VersionDAO;
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
    public Optional<ProjectDAO> get(@PathVariable Long id) {
        ProjectDAO projectEntity = projectRepository.findById(id).orElseThrow();
        return Optional.of(projectEntity);
    }

    @GetMapping
    public List<ProjectDAO> getAll() {
        return projectRepository.findAll();
    }

    @PostMapping("/{versionId}")
    public ProjectDAO save(@RequestBody ProjectDAO project, @PathVariable Long versionId) {
        VersionDAO version = versionRepository.getById(versionId);
        project.setVersion(version);
        ProjectDAO save = projectRepository.save(project);
        return save;
    }

    @PutMapping("/{id}")
    public ProjectDAO update(@PathVariable Long id, @RequestBody ProjectDAO project) {
//        ProjectEntity project = projectRepository.findById(id).orElseThrow();
//        project.setName(projectDetails.getName());
//        project.setRequester(projectDetails.getRequester());
        return projectRepository.save(project);
    }
}