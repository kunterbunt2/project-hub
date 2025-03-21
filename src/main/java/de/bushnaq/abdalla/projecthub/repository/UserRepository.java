package de.bushnaq.abdalla.projecthub.repository;

import de.bushnaq.abdalla.projecthub.dao.UserDTO;
import org.springframework.data.repository.ListCrudRepository;

public interface UserRepository extends ListCrudRepository<UserDTO, Long> {
    UserDTO getById(Long id);

    UserDTO getByName(String name);
}