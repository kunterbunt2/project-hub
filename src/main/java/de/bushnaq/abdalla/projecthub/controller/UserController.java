package de.bushnaq.abdalla.projecthub.controller;

import de.bushnaq.abdalla.projecthub.db.UserEntity;
import de.bushnaq.abdalla.projecthub.db.WorkingLocationEntity;
import de.bushnaq.abdalla.projecthub.db.repository.UserRepository;
import de.bushnaq.abdalla.projecthub.db.repository.WorkLocationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/user")
public class UserController {

    @Autowired
    private UserRepository         userRepository;
    @Autowired
    private WorkLocationRepository workingLocationRepository;

    @PostMapping(consumes = "application/json", produces = "application/json")
    public UserEntity create(@RequestBody UserEntity user) {
        for (WorkingLocationEntity wl : user.getWorkingLocations()) {
//            WorkingLocationEntity saved = workingLocationRepository.getByName(wl.getName());
//            if (saved == null) {
            workingLocationRepository.save(wl);
//            }
        }

        return userRepository.save(user);
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
    public UserEntity getById(@PathVariable Long id) {
        return userRepository.getById(id);
    }

    @PutMapping("/{id}")
    public UserEntity update(@PathVariable Long id, @RequestBody UserEntity userDetails) {
        UserEntity user = userRepository.findById(id).orElseThrow();
        //TODO update user
//        user.setName(userDetails.getName());
//        user.setRequester(projectDetails.getRequester());
        return userRepository.save(user);
    }
}