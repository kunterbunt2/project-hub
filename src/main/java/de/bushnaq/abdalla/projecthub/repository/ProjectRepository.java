package de.bushnaq.abdalla.projecthub.repository;

import de.bushnaq.abdalla.projecthub.dao.ProjectEntity;
import org.springframework.data.repository.ListCrudRepository;

//public interface ProjectRepository extends JpaRepository<ProjectEntity, Long>
public interface ProjectRepository extends ListCrudRepository<ProjectEntity, Long> {
    ProjectEntity getById(Long id);

    ProjectEntity getByName(String name);
}