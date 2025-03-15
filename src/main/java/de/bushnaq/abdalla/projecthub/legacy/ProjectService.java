package de.bushnaq.abdalla.projecthub.legacy;

import de.bushnaq.abdalla.projecthub.db.ProjectEntity;
import de.bushnaq.abdalla.projecthub.db.repository.ProjectRepository;
import de.bushnaq.abdalla.projecthub.db.repository.VersionRepository;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

//@Service
public class ProjectService {

    @Autowired
    private EntityDtoMapper   objectMapper;
    @Autowired
    private ProjectRepository projectRepository;
    @Autowired
    private VersionRepository versionRepository;

//    public ProjectEntity createProject(ProjectEntity project) {
//        ProjectEntity projectEntity = project;

    /// /        ProjectEntity projectEntity = objectMapper.toEntity(project);
//        for (VersionEntity version : projectEntity.getVersions()) {
//            VersionEntity saved = versionRepository.getByName(version.getName());
//            if (saved == null) {
//                versionRepository.save(version);
//            }
//        }
//
//        projectEntity = projectRepository.save(projectEntity);
//        return projectEntity /*objectMapper.toDTO(projectEntity)*/;
//    }
    public List<ProjectEntity> getAllProjects() {
        return projectRepository.findAll();
//        .stream().map(objectMapper::toDTO).collect(Collectors.toList());
    }

//    public Optional<ProjectEntity> getProjectById(Long id) {
//        ProjectEntity projectEntity = projectRepository.findById(id).orElseThrow();
//        return Optional.of(projectEntity);
////        return Optional.of(objectMapper.toDTO(projectEntity));
//    }
}