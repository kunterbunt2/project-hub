package de.bushnaq.abdalla.projecthub.repository;

import de.bushnaq.abdalla.projecthub.dao.LocationDAO;
import org.springframework.data.repository.ListCrudRepository;

public interface LocationRepository extends ListCrudRepository<LocationDAO, Long> {
//    LocationEntity getById(Long id);

    //    WorkingLocationEntity getByName(String name);
    void deleteById(Long id);
}