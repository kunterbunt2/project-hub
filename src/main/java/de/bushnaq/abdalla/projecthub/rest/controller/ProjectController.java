package de.bushnaq.abdalla.projecthub.rest.controller;

import de.bushnaq.abdalla.projecthub.db.ProjectEntity;
import de.bushnaq.abdalla.projecthub.db.repository.ProjectRepository;
import de.bushnaq.abdalla.projecthub.db.repository.VersionRepository;
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

    @PostMapping(consumes = "application/json", produces = "application/json")
    public ProjectEntity create(@RequestBody ProjectEntity project) {
//        for (VersionEntity ve : project.getVersions()) {
//            versionRepository.save(ve);
//        }

        ProjectEntity createdEntity = projectRepository.save(project);
        return createdEntity;

    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        projectRepository.deleteById(id);
    }

    @GetMapping
    public List<ProjectEntity> getAll() {
        return projectRepository.findAll();
    }

    @GetMapping("/{id}")
    public Optional<ProjectEntity> getById(@PathVariable Long id) {
        ProjectEntity projectEntity = projectRepository.findById(id).orElseThrow();
        return Optional.of(projectEntity);
    }

    @PutMapping("/{id}")
    public ProjectEntity update(@PathVariable Long id, @RequestBody ProjectEntity projectDetails) {
        ProjectEntity project = projectRepository.findById(id).orElseThrow();
        project.setName(projectDetails.getName());
        project.setRequester(projectDetails.getRequester());
        return projectRepository.save(project);
    }
}