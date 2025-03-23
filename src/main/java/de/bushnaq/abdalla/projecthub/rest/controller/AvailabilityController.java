package de.bushnaq.abdalla.projecthub.rest.controller;

import de.bushnaq.abdalla.projecthub.dao.AvailabilityDAO;
import de.bushnaq.abdalla.projecthub.dao.UserDAO;
import de.bushnaq.abdalla.projecthub.repository.AvailabilityRepository;
import de.bushnaq.abdalla.projecthub.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Objects;
import java.util.Optional;

@RestController
@RequestMapping("/availability")
public class AvailabilityController {

    @Autowired
    private AvailabilityRepository availabilityRepository;

    @Autowired
    private UserRepository userRepository;

    @DeleteMapping("/{userId}/{id}")
    public void delete(@PathVariable Long userId, @PathVariable Long id) {
        UserDAO         user         = userRepository.getById(userId);
        AvailabilityDAO availability = availabilityRepository.findById(id).orElseThrow();
        if (Objects.equals(user.getAvailabilities().getFirst().getId(), id))
            throw new IllegalArgumentException("Cannot delete the first availability");
        user.getAvailabilities().remove(availability);
        userRepository.save(user);
        availabilityRepository.deleteById(id);
    }

    @GetMapping("/{id}")
    public Optional<AvailabilityDAO> getById(@PathVariable Long id) {
        AvailabilityDAO e = availabilityRepository.findById(id).orElseThrow();
        return Optional.of(e);
    }

    @PostMapping("/{userId}")
    public AvailabilityDAO save(@RequestBody AvailabilityDAO availability, @PathVariable Long userId) {
        UserDAO user = userRepository.getById(userId);
        availability.setUser(user);
        AvailabilityDAO save = availabilityRepository.save(availability);
        return save;
    }

    @PutMapping("/{userId}")
    public void update(@RequestBody AvailabilityDAO availability, @PathVariable Long userId) {
        UserDAO user = userRepository.getById(userId);
        availability.setUser(user);
        AvailabilityDAO save = availabilityRepository.save(availability);
    }
}