package de.bushnaq.abdalla.projecthub.rest.controller;

import de.bushnaq.abdalla.projecthub.dao.LocationDTO;
import de.bushnaq.abdalla.projecthub.dao.UserDTO;
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
        UserDTO     user     = userRepository.getById(userId);
        LocationDTO location = locationRepository.findById(id).orElseThrow();
        if (Objects.equals(user.getLocations().getFirst().getId(), id))
            throw new IllegalArgumentException("Cannot delete the first location");
        user.getLocations().remove(location);
        userRepository.save(user);
        locationRepository.deleteById(id);
//        locationRepository.deleteById(id);
    }

    @GetMapping("/{id}")
    public Optional<LocationDTO> getById(@PathVariable Long id) {
        LocationDTO e = locationRepository.findById(id).orElseThrow();
        return Optional.of(e);
    }

    @PutMapping()
    public void update(@RequestBody LocationDTO location) {
//        LocationEntity e = locationRepository.findById(locationDetails.getId()).orElseThrow();
//        e.setCountry(locationDetails.getCountry());
//        e.setState(locationDetails.getState());
//        e.setStart(locationDetails.getStart());
        locationRepository.save(location);
    }
}