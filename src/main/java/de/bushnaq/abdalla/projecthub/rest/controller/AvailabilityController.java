package de.bushnaq.abdalla.projecthub.rest.controller;

import de.bushnaq.abdalla.projecthub.dao.AvailabilityDTO;
import de.bushnaq.abdalla.projecthub.dao.UserDTO;
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
        UserDTO         user         = userRepository.getById(userId);
        AvailabilityDTO availability = availabilityRepository.findById(id).orElseThrow();
        if (Objects.equals(user.getAvailabilities().getFirst().getId(), id))
            throw new IllegalArgumentException("Cannot delete the first availability");
        user.getAvailabilities().remove(availability);
        userRepository.save(user);
        availabilityRepository.deleteById(id);
    }

    @GetMapping("/{id}")
    public Optional<AvailabilityDTO> getById(@PathVariable Long id) {
        AvailabilityDTO e = availabilityRepository.findById(id).orElseThrow();
        return Optional.of(e);
    }

    @PutMapping()
    public void update(@RequestBody AvailabilityDTO availability) {
//        AvailabilityEntity e = availabilityRepository.findById(availabilityDetails.getId()).orElseThrow();
//        e.setAvailability(availabilityDetails.getAvailability());
//        e.setStart(availabilityDetails.getStart());
        availabilityRepository.save(availability);
    }
}