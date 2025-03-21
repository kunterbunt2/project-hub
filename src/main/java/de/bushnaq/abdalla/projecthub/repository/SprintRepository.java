package de.bushnaq.abdalla.projecthub.repository;

import de.bushnaq.abdalla.projecthub.dao.SprintDTO;
import org.springframework.data.repository.ListCrudRepository;

public interface SprintRepository extends ListCrudRepository<SprintDTO, Long> {
//    SprintEntity getByName(String name);
}