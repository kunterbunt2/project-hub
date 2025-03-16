package de.bushnaq.abdalla.projecthub.repository;

import de.bushnaq.abdalla.projecthub.dao.OffDayEntity;
import org.springframework.data.repository.ListCrudRepository;

public interface OffDayRepository extends ListCrudRepository<OffDayEntity, Long> {

    void deleteById(Long id);
}