package de.bushnaq.abdalla.projecthub.rest.controller;

import de.bushnaq.abdalla.projecthub.dao.OffDayDTO;
import de.bushnaq.abdalla.projecthub.dao.UserDTO;
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
        UserDTO   user   = userRepository.getById(userId);
        OffDayDTO offDay = offDayRepository.findById(id).orElseThrow();
        user.getOffDays().remove(offDay);
        userRepository.save(user);
        offDayRepository.deleteById(id);
    }

    @GetMapping("/{id}")
    public Optional<OffDayDTO> getById(@PathVariable Long id) {
        OffDayDTO e = offDayRepository.findById(id).orElseThrow();
        return Optional.of(e);
    }

    @PutMapping()
    public void update(@RequestBody OffDayDTO offDay) {
//        OffDayEntity e = offDayRepository.findById(offDayDetails.getId()).orElseThrow();
//        e.setType(offDayDetails.getType());
//        e.setFirstDay(offDayDetails.getFirstDay());
//        e.setLastDay(offDayDetails.getLastDay());
        offDayRepository.save(offDay);
    }
}