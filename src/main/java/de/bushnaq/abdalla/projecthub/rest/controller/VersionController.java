package de.bushnaq.abdalla.projecthub.rest.controller;

import de.bushnaq.abdalla.projecthub.dao.VersionDTO;
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
    public Optional<VersionDTO> get(@PathVariable Long id) {
        VersionDTO projectEntity = versionRepository.findById(id).orElseThrow();
        return Optional.of(projectEntity);
    }

    @GetMapping
    public List<VersionDTO> getAll() {
        return versionRepository.findAll();
    }

    @PostMapping(consumes = "application/json", produces = "application/json")
    public VersionDTO save(@RequestBody VersionDTO version) {
        return versionRepository.save(version);
    }

    @PutMapping("/{id}")
    public VersionDTO update(@PathVariable Long id, @RequestBody VersionDTO version) {
        return versionRepository.save(version);
    }
}