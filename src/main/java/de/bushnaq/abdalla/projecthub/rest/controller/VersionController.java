package de.bushnaq.abdalla.projecthub.rest.controller;

import de.bushnaq.abdalla.projecthub.dao.VersionDAO;
import de.bushnaq.abdalla.projecthub.repository.VersionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/version")
public class VersionController {

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

    @PostMapping(consumes = "application/json", produces = "application/json")
    public VersionDAO save(@RequestBody VersionDAO version) {
        return versionRepository.save(version);
    }

    @PutMapping("/{id}")
    public VersionDAO update(@PathVariable Long id, @RequestBody VersionDAO version) {
        return versionRepository.save(version);
    }
}