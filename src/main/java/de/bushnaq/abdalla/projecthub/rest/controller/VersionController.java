package de.bushnaq.abdalla.projecthub.rest.controller;

import de.bushnaq.abdalla.projecthub.dao.ProductDAO;
import de.bushnaq.abdalla.projecthub.dao.VersionDAO;
import de.bushnaq.abdalla.projecthub.repository.ProductRepository;
import de.bushnaq.abdalla.projecthub.repository.VersionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/version")
public class VersionController {

    @Autowired
    private ProductRepository productRepository;
    @Autowired
    private VersionRepository versionRepository;

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        versionRepository.deleteById(id);
    }

    @GetMapping("/{id}")
    public Optional<VersionDAO> get(@PathVariable Long id) {
        VersionDAO projectEntity = versionRepository.findById(id).orElseThrow();
        return Optional.of(projectEntity);
    }

    @GetMapping
    public List<VersionDAO> getAll() {
        return versionRepository.findAll();
    }

    @PostMapping("/{productId}")
    public VersionDAO save(@RequestBody VersionDAO version, @PathVariable Long productId) {
        ProductDAO product = productRepository.getById(productId);
        version.setProduct(product);
        VersionDAO save = versionRepository.save(version);
        return save;
    }

    @PutMapping("/{id}")
    public VersionDAO update(@PathVariable Long id, @RequestBody VersionDAO version) {
        return versionRepository.save(version);
    }
}