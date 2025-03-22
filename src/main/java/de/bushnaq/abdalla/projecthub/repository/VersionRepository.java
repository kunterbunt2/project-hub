package de.bushnaq.abdalla.projecthub.repository;

import de.bushnaq.abdalla.projecthub.dao.VersionDAO;
import org.springframework.data.repository.ListCrudRepository;

//public interface ProjectRepository extends JpaRepository<ProjectEntity, Long>
public interface VersionRepository extends ListCrudRepository<VersionDAO, Long> {
    VersionDAO getByName(String name);
}