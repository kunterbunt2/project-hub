package de.bushnaq.abdalla.projecthub.db.repository;

import de.bushnaq.abdalla.projecthub.db.UserEntity;
import org.springframework.data.repository.ListCrudRepository;

//public interface ProjectRepository extends JpaRepository<ProjectEntity, Long>
public interface UserRepository extends ListCrudRepository<UserEntity, Long> {
    UserEntity getById(Long id);

    UserEntity getByName(String name);
}