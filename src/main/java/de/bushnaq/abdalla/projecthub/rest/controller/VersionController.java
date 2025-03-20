package de.bushnaq.abdalla.projecthub.rest.controller;

import de.bushnaq.abdalla.projecthub.dao.VersionEntity;
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
    public Optional<VersionEntity> get(@PathVariable Long id) {
        VersionEntity projectEntity = versionRepository.findById(id).orElseThrow();
        return Optional.of(projectEntity);
    }

    @GetMapping
    public List<VersionEntity> getAll() {
        return versionRepository.findAll();
    }

    @PostMapping(consumes = "application/json", produces = "application/json")
    public VersionEntity save(@RequestBody VersionEntity version) {
        return versionRepository.save(version);
    }

    @PutMapping("/{id}")
    public VersionEntity update(@PathVariable Long id, @RequestBody VersionEntity version) {
        return versionRepository.save(version);
    }
}