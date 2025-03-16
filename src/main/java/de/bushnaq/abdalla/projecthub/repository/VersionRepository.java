package de.bushnaq.abdalla.projecthub.repository;

import de.bushnaq.abdalla.projecthub.dao.VersionEntity;
import org.springframework.data.repository.ListCrudRepository;

//public interface ProjectRepository extends JpaRepository<ProjectEntity, Long>
public interface VersionRepository extends ListCrudRepository<VersionEntity, Long> {
    VersionEntity getByName(String name);
}