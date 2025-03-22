package de.bushnaq.abdalla.projecthub.rest.controller;

import de.bushnaq.abdalla.projecthub.dao.OffDayDAO;
import de.bushnaq.abdalla.projecthub.dao.UserDAO;
import de.bushnaq.abdalla.projecthub.repository.OffDayRepository;
import de.bushnaq.abdalla.projecthub.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@RequestMapping("/offday")
public class OffDayController {

    @Autowired
    private OffDayRepository offDayRepository;

    @Autowired
    private UserRepository userRepository;

    @DeleteMapping("/{userId}/{id}")
    public void delete(@PathVariable Long userId, @PathVariable Long id) {
        UserDAO   user   = userRepository.getById(userId);
        OffDayDAO offDay = offDayRepository.findById(id).orElseThrow();
        user.getOffDays().remove(offDay);
        userRepository.save(user);
        offDayRepository.deleteById(id);
    }

    @GetMapping("/{id}")
    public Optional<OffDayDAO> getById(@PathVariable Long id) {
        OffDayDAO e = offDayRepository.findById(id).orElseThrow();
        return Optional.of(e);
    }

    @PutMapping()
    public void update(@RequestBody OffDayDAO offDay) {
//        OffDayEntity e = offDayRepository.findById(offDayDetails.getId()).orElseThrow();
//        e.setType(offDayDetails.getType());
//        e.setFirstDay(offDayDetails.getFirstDay());
//        e.setLastDay(offDayDetails.getLastDay());
        offDayRepository.save(offDay);
    }
}