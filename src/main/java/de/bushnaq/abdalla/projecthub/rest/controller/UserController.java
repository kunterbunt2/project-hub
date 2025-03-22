package de.bushnaq.abdalla.projecthub.rest.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import de.bushnaq.abdalla.projecthub.dao.UserDAO;
import de.bushnaq.abdalla.projecthub.repository.LocationRepository;
import de.bushnaq.abdalla.projecthub.repository.UserRepository;
import de.bushnaq.abdalla.projecthub.rest.debug.DebugUtil;
import jakarta.persistence.EntityManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/user")
public class UserController {

    @Autowired
    DebugUtil debugUtil;

    @Autowired
    EntityManager entityManager;

    @Autowired
    private LocationRepository locationRepository;

    @Autowired
    private UserRepository userRepository;

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        userRepository.deleteById(id);
    }

    @GetMapping("/{id}")
    public Optional<UserDAO> get(@PathVariable Long id) throws JsonProcessingException {
        UserDAO byId = userRepository.getById(id);
        return Optional.of(byId);
    }

    @GetMapping
    public List<UserDAO> getAll() {
        return userRepository.findAll();
    }

    @PostMapping(consumes = "application/json", produces = "application/json")
    public UserDAO save(@RequestBody UserDAO user) {
        return userRepository.save(user);
    }

    @PutMapping()
    public void update(@RequestBody UserDAO user) {
//            UserEntity e = userRepository.findById(user.getId()).orElseThrow();
//            e.setLastWorkingDay(user.getLastWorkingDay());
//            e.setFirstWorkingDay(user.getFirstWorkingDay());
//            e.setEmail(user.getEmail());
//            e.setLastWorkingDay(user.getLastWorkingDay());
//            e.setName(user.getName());
//            userRepository.save(e);
        userRepository.save(user);
    }
}