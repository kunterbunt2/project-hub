package de.bushnaq.abdalla.projecthub.repository;

import de.bushnaq.abdalla.projecthub.dao.AvailabilityDTO;
import org.springframework.data.repository.ListCrudRepository;

public interface AvailabilityRepository extends ListCrudRepository<AvailabilityDTO, Long> {
//    LocationEntity getById(Long id);

    //    WorkingLocationEntity getByName(String name);
    void deleteById(Long id);
}