package de.bushnaq.abdalla.projecthub.rest.controller;

import de.bushnaq.abdalla.projecthub.dao.LocationDAO;
import de.bushnaq.abdalla.projecthub.dao.UserDAO;
import de.bushnaq.abdalla.projecthub.repository.LocationRepository;
import de.bushnaq.abdalla.projecthub.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Objects;
import java.util.Optional;

@RestController
@RequestMapping("/location")
public class LocationController {

    @Autowired
    private LocationRepository locationRepository;

    @Autowired
    private UserRepository userRepository;

    @DeleteMapping("/{userId}/{id}")
    public void delete(@PathVariable Long userId, @PathVariable Long id) {
        UserDAO     user     = userRepository.getById(userId);
        LocationDAO location = locationRepository.findById(id).orElseThrow();
        if (Objects.equals(user.getLocations().getFirst().getId(), id))
            throw new IllegalArgumentException("Cannot delete the first location");
        user.getLocations().remove(location);
        userRepository.save(user);
        locationRepository.deleteById(id);
//        locationRepository.deleteById(id);
    }

    @GetMapping("/{id}")
    public Optional<LocationDAO> getById(@PathVariable Long id) {
        LocationDAO e = locationRepository.findById(id).orElseThrow();
        return Optional.of(e);
    }

    @PostMapping(consumes = "application/json", produces = "application/json")
    public LocationDAO save(@RequestBody LocationDAO location) {
        LocationDAO save  = locationRepository.save(location);
        LocationDAO save2 = locationRepository.findById(save.getId()).orElseThrow();
        return save;
    }

    @PutMapping()
    public void update(@RequestBody LocationDAO location) {
//        LocationEntity e = locationRepository.findById(locationDetails.getId()).orElseThrow();
//        e.setCountry(locationDetails.getCountry());
//        e.setState(locationDetails.getState());
//        e.setStart(locationDetails.getStart());
        locationRepository.save(location);
    }
}