package de.bushnaq.abdalla.projecthub.repository;

import de.bushnaq.abdalla.projecthub.dao.OffDayDTO;
import org.springframework.data.repository.ListCrudRepository;

public interface OffDayRepository extends ListCrudRepository<OffDayDTO, Long> {

    void deleteById(Long id);
}