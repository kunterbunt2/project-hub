package de.bushnaq.abdalla.projecthub.db.repository;

import de.bushnaq.abdalla.projecthub.db.LocationEntity;
import org.springframework.data.repository.ListCrudRepository;

public interface LocationRepository extends ListCrudRepository<LocationEntity, Long> {
//    LocationEntity getById(Long id);

//    WorkingLocationEntity getByName(String name);
}