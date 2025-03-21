package de.bushnaq.abdalla.projecthub.repository;

import de.bushnaq.abdalla.projecthub.dao.VersionDTO;
import org.springframework.data.repository.ListCrudRepository;

//public interface ProjectRepository extends JpaRepository<ProjectEntity, Long>
public interface VersionRepository extends ListCrudRepository<VersionDTO, Long> {
    VersionDTO getByName(String name);
}