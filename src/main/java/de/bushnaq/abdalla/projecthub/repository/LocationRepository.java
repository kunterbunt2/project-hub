package de.bushnaq.abdalla.projecthub.repository;

import de.bushnaq.abdalla.projecthub.dao.LocationDTO;
import org.springframework.data.repository.ListCrudRepository;

public interface LocationRepository extends ListCrudRepository<LocationDTO, Long> {
//    LocationEntity getById(Long id);

    //    WorkingLocationEntity getByName(String name);
    void deleteById(Long id);
}