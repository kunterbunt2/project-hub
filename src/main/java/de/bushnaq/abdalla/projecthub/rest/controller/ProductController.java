package de.bushnaq.abdalla.projecthub.rest.controller;

import de.bushnaq.abdalla.projecthub.dao.ProductDAO;
import de.bushnaq.abdalla.projecthub.repository.ProductRepository;
import de.bushnaq.abdalla.projecthub.repository.VersionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/product")
public class ProductController {

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private VersionRepository versionRepository;

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        productRepository.deleteById(id);
    }

    @GetMapping("/{id}")
    public Optional<ProductDAO> get(@PathVariable Long id) {
        ProductDAO productEntity = productRepository.findById(id).orElseThrow();
        return Optional.of(productEntity);
    }

    @GetMapping
    public List<ProductDAO> getAll() {
        return productRepository.findAll();
    }

    @PostMapping(consumes = "application/json", produces = "application/json")
    public ProductDAO save(@RequestBody ProductDAO product) {
        return productRepository.save(product);
    }

    @PutMapping("/{id}")
    public ProductDAO update(@PathVariable Long id, @RequestBody ProductDAO product) {
//        ProjectEntity product = projectRepository.findById(id).orElseThrow();
//        product.setName(projectDetails.getName());
//        product.setRequester(projectDetails.getRequester());
        return productRepository.save(product);
    }
}