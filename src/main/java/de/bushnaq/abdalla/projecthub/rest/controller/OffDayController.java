/*
 *
 * Copyright (C) 2025-2025 Abdalla Bushnaq
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 */

package de.bushnaq.abdalla.projecthub.rest.controller;

import de.bushnaq.abdalla.projecthub.dao.OffDayDAO;
import de.bushnaq.abdalla.projecthub.repository.OffDayRepository;
import de.bushnaq.abdalla.projecthub.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@RequestMapping("/api/offday")
public class OffDayController {

    @Autowired
    private OffDayRepository offDayRepository;

    @Autowired
    private UserRepository userRepository;

    @DeleteMapping("/{userId}/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Object> delete(@PathVariable Long userId, @PathVariable Long id) {
        return userRepository.findById(userId).map(
                user -> {
                    OffDayDAO offDay = offDayRepository.findById(id).orElseThrow();
                    user.getOffDays().remove(offDay);
                    userRepository.save(user);
                    offDayRepository.deleteById(id);
                    return ResponseEntity.ok().build(); // Return 200 OK
                }
        ).orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'USER')")
    public Optional<OffDayDAO> getById(@PathVariable Long id) {
        OffDayDAO e = offDayRepository.findById(id).orElseThrow();
        return Optional.of(e);
    }

    @PostMapping("/{userId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<OffDayDAO> save(@RequestBody OffDayDAO offDay, @PathVariable Long userId) {
        return userRepository.findById(userId).map(user -> {
            offDay.setUser(user);
            OffDayDAO save = offDayRepository.save(offDay);
            return ResponseEntity.ok(save); // Return 200 OK
        }).orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/{userId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Object> update(@RequestBody OffDayDAO offDay, @PathVariable Long userId) {
        return userRepository.findById(userId).map(user -> {
            offDay.setUser(user);
            OffDayDAO save = offDayRepository.save(offDay);
            return ResponseEntity.ok().build(); // Return 200 OK
        }).orElse(ResponseEntity.notFound().build());
    }
}