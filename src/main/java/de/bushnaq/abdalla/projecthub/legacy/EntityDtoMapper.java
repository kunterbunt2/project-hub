package de.bushnaq.abdalla.projecthub.legacy;

import de.bushnaq.abdalla.projecthub.db.ProjectEntity;
import de.bushnaq.abdalla.projecthub.db.SprintEntity;
import de.bushnaq.abdalla.projecthub.db.VersionEntity;
import de.bushnaq.abdalla.projecthub.model.Project;
import de.bushnaq.abdalla.projecthub.model.Sprint;
import de.bushnaq.abdalla.projecthub.model.Version;
import org.springframework.stereotype.Component;

@Component
public class EntityDtoMapper {

    public Sprint toDTO(SprintEntity sprintEntity) {
        Sprint sprint = new Sprint();
        sprint.setId(sprintEntity.getId());
        sprint.setName(sprintEntity.getName());
        sprint.setStart(sprintEntity.getStart());
        sprint.setEnd(sprintEntity.getEnd());
        sprint.setStatus(sprintEntity.getStatus());
        sprint.setCreated(sprintEntity.getCreated());
        sprint.setUpdated(sprintEntity.getUpdated());
        return sprint;
    }

    public Version toDTO(VersionEntity versionEntity) {
        Version version = new Version();
        version.setId(versionEntity.getId());
        version.setName(versionEntity.getName());
        version.setSprints(versionEntity.getSprints().stream().map(this::toDTO).toList());
        version.setCreated(versionEntity.getCreated());
        version.setUpdated(versionEntity.getUpdated());
        return version;
    }

    public Project toDTO(ProjectEntity projectEntity) {
        Project project = new Project();
        project.setId(projectEntity.getId());
        project.setName(projectEntity.getName());
        project.setRequester(projectEntity.getRequester());
        project.setVersions(projectEntity.getVersions().stream().map(this::toDTO).toList());
        project.setCreated(projectEntity.getCreated());
        project.setUpdated(projectEntity.getUpdated());
        return project;
    }

    public VersionEntity toEntity(Version version) {
        VersionEntity versionEntity = new VersionEntity();
        versionEntity.setId(version.getId());
        versionEntity.setName(version.getName());
        versionEntity.setSprints(version.getSprints().stream().map(this::toEntity).toList());
        versionEntity.setCreated(version.getCreated());
        versionEntity.setUpdated(version.getUpdated());
        return versionEntity;
    }

    public SprintEntity toEntity(Sprint sprint) {
        SprintEntity sprintEntity = new SprintEntity();
        sprintEntity.setId(sprint.getId());
        sprintEntity.setName(sprint.getName());
        sprintEntity.setStart(sprint.getStart());
        sprintEntity.setEnd(sprint.getEnd());
        sprintEntity.setStatus(sprint.getStatus());
        sprintEntity.setCreated(sprint.getCreated());
        sprintEntity.setUpdated(sprint.getUpdated());
        return sprintEntity;
    }

    public ProjectEntity toEntity(Project project) {
        ProjectEntity projectEntity = new ProjectEntity();
        projectEntity.setId(project.getId());
        projectEntity.setName(project.getName());
        projectEntity.setRequester(project.getRequester());
        projectEntity.setVersions(project.getVersions().stream().map(this::toEntity).toList());
        projectEntity.setCreated(project.getCreated());
        projectEntity.setUpdated(project.getUpdated());
        return projectEntity;
    }

}