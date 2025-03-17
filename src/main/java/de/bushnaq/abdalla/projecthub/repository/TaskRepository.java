package de.bushnaq.abdalla.projecthub.repository;

import de.bushnaq.abdalla.projecthub.dao.TaskDAO;
import org.springframework.data.repository.ListCrudRepository;

public interface TaskRepository extends ListCrudRepository<TaskDAO, Long> {

    void deleteById(Long id);
}