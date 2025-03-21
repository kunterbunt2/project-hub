package de.bushnaq.abdalla.projecthub.repository;

import de.bushnaq.abdalla.projecthub.dao.ProjectDTO;
import org.springframework.data.repository.ListCrudRepository;

//public interface ProjectRepository extends JpaRepository<ProjectEntity, Long>
public interface ProjectRepository extends ListCrudRepository<ProjectDTO, Long> {
    ProjectDTO getById(Long id);

    ProjectDTO getByName(String name);
}