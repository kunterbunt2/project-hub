package de.bushnaq.abdalla.projecthub.repository;

import de.bushnaq.abdalla.projecthub.dao.ProjectDAO;
import org.springframework.data.repository.ListCrudRepository;

//public interface ProjectRepository extends JpaRepository<ProjectEntity, Long>
public interface ProjectRepository extends ListCrudRepository<ProjectDAO, Long> {
    ProjectDAO getById(Long id);

    ProjectDAO getByName(String name);
}