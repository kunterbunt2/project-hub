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

    @PostMapping("/{userId}")
    public LocationDAO save(@RequestBody LocationDAO location, @PathVariable Long userId) {
        UserDAO user = userRepository.getById(userId);
        location.setUser(user);
        LocationDAO save = locationRepository.save(location);
        return save;
    }

    @PutMapping("/{userId}")
    public void update(@RequestBody LocationDAO location, @PathVariable Long userId) {
        UserDAO user = userRepository.getById(userId);
        location.setUser(user);
        LocationDAO save = locationRepository.save(location);
    }
}