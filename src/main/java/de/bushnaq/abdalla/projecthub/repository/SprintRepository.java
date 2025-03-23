package de.bushnaq.abdalla.projecthub.repository;

import de.bushnaq.abdalla.projecthub.dao.SprintDAO;
import org.springframework.data.repository.ListCrudRepository;

public interface SprintRepository extends ListCrudRepository<SprintDAO, Long> {
    SprintDAO getById(Long id);
}