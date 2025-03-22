package de.bushnaq.abdalla.projecthub.repository;

import de.bushnaq.abdalla.projecthub.dao.UserDAO;
import org.springframework.data.repository.ListCrudRepository;

public interface UserRepository extends ListCrudRepository<UserDAO, Long> {
    UserDAO getById(Long id);

    UserDAO getByName(String name);
}