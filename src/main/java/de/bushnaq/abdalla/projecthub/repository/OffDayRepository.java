package de.bushnaq.abdalla.projecthub.repository;

import de.bushnaq.abdalla.projecthub.dao.OffDayDAO;
import org.springframework.data.repository.ListCrudRepository;

public interface OffDayRepository extends ListCrudRepository<OffDayDAO, Long> {

    void deleteById(Long id);
}