package de.bushnaq.abdalla.projecthub.rest.controller;

import de.bushnaq.abdalla.projecthub.db.LocationEntity;
import de.bushnaq.abdalla.projecthub.db.repository.LocationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@RequestMapping("/location")
public class LocationController {

    @Autowired
    private LocationRepository locationRepository;


    @GetMapping("/{id}")
    public Optional<LocationEntity> getById(@PathVariable Long id) {
        LocationEntity e = locationRepository.findById(id).orElseThrow();
        return Optional.of(e);
    }

    @PutMapping()
    public void update(@RequestBody LocationEntity locationDetails) {
        LocationEntity e = locationRepository.findById(locationDetails.getId()).orElseThrow();
        e.setCountry(locationDetails.getCountry());
        e.setState(locationDetails.getState());
        e.setStart(locationDetails.getStart());
        e.setFinish(locationDetails.getFinish());
        locationRepository.save(e);
    }
}