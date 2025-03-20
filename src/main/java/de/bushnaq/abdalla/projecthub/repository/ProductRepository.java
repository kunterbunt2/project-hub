package de.bushnaq.abdalla.projecthub.repository;

import de.bushnaq.abdalla.projecthub.dao.ProductDAO;
import org.springframework.data.repository.ListCrudRepository;

public interface ProductRepository extends ListCrudRepository<ProductDAO, Long> {
    ProductDAO getById(Long id);

    ProductDAO getByName(String name);
}