package de.bushnaq.abdalla.projecthub.db.repository;

import de.bushnaq.abdalla.projecthub.db.ProjectEntity;
import de.bushnaq.abdalla.projecthub.db.VersionEntity;
import org.springframework.data.repository.ListCrudRepository;

//public interface ProjectRepository extends JpaRepository<ProjectEntity, Long>
public interface ProjectRepository extends ListCrudRepository<ProjectEntity, Long> {
    VersionEntity getById(Long id);

    VersionEntity getByName(String name);
}