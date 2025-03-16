package de.bushnaq.abdalla.projecthub.db.repository;

import de.bushnaq.abdalla.projecthub.db.AvailabilityEntity;
import org.springframework.data.repository.ListCrudRepository;

public interface AvailabilityRepository extends ListCrudRepository<AvailabilityEntity, Long> {
//    LocationEntity getById(Long id);

    //    WorkingLocationEntity getByName(String name);
    void deleteById(Long id);
}