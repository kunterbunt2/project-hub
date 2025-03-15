package de.bushnaq.abdalla.projecthub.rest.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import de.bushnaq.abdalla.projecthub.db.UserEntity;
import de.bushnaq.abdalla.projecthub.db.repository.LocationRepository;
import de.bushnaq.abdalla.projecthub.db.repository.UserRepository;
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
    DebugUtil     debugUtil;
    @Autowired
    EntityManager entityManager;
    @Autowired
    private LocationRepository locationRepository;
    @Autowired
    ObjectMapper objectMapper;
    @Autowired
    private UserRepository userRepository;

    @PostMapping(consumes = "application/json", produces = "application/json")
    public UserEntity create(@RequestBody UserEntity user) {
//        for (LocationEntity wl : user.getLocations()) {
//            locationRepository.save(wl);
//        }
//        debugUtil.logJson(user);
        UserEntity createdEntity = userRepository.save(user);
//        debugUtil.logJson(createdEntity);
        return createdEntity;
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        userRepository.deleteById(id);
    }

    @GetMapping
    public List<UserEntity> getAll() {
        return userRepository.findAll();
    }

    @GetMapping("/{id}")
    public Optional<UserEntity> getById(@PathVariable Long id) throws JsonProcessingException {
        UserEntity byId = userRepository.getById(id);
        return Optional.of(byId);
    }

    @PutMapping()
    public void update(@RequestBody UserEntity user) {
        UserEntity e = userRepository.findById(user.getId()).orElseThrow();
        //TODO update user
//        user.setName(userDetails.getName());
//        user.setRequester(projectDetails.getRequester());
        userRepository.save(e);
    }
}