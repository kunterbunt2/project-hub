package de.bushnaq.abdalla.projecthub.db.repository;

import de.bushnaq.abdalla.projecthub.db.WorkingLocationEntity;
import org.springframework.data.repository.ListCrudRepository;

//public interface ProjectRepository extends JpaRepository<ProjectEntity, Long>
public interface WorkLocationRepository extends ListCrudRepository<WorkingLocationEntity, Long> {
    WorkingLocationEntity getById(Long id);

//    WorkingLocationEntity getByName(String name);
}