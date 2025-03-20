package de.bushnaq.abdalla.projecthub.repository;

import de.bushnaq.abdalla.projecthub.dao.SprintEntity;
import org.springframework.data.repository.ListCrudRepository;

public interface SprintRepository extends ListCrudRepository<SprintEntity, Long> {
//    SprintEntity getByName(String name);
}