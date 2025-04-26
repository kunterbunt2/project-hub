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

package de.bushnaq.abdalla.projecthub.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import de.bushnaq.abdalla.projecthub.dto.Availability;
import de.bushnaq.abdalla.projecthub.dto.Location;
import de.bushnaq.abdalla.projecthub.dto.OffDay;
import de.bushnaq.abdalla.projecthub.dto.User;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Arrays;
import java.util.List;

@Service
public class UserApi extends AbstractApi {

    //TODO move availability to its own api
    //TODO move location to its own api
    //TODO move offday to its own api
    public UserApi(RestTemplate restTemplate, ObjectMapper objectMapper, String baseUrl) {
        super(restTemplate, objectMapper, baseUrl);
    }

    public UserApi() {
    }

    public UserApi(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    //TODO use ids instead of objects
    public void delete(User user, Location location) throws org.springframework.web.client.RestClientException {
        executeWithErrorHandling(() -> restTemplate.delete(
                baseUrl + "/location/{userId}/{id}",
                user.getId(),
                location.getId()
        ));
    }

    //TODO use ids instead of objects
    public void delete(User user, OffDay offDay) throws org.springframework.web.client.RestClientException {
        executeWithErrorHandling(() -> restTemplate.delete(
                baseUrl + "/offday/{userId}/{id}",
                user.getId(),
                offDay.getId()
        ));
    }

    //TODO use ids instead of objects
    public void delete(User user, Availability availability) throws org.springframework.web.client.RestClientException {
        executeWithErrorHandling(() -> restTemplate.delete(
                baseUrl + "/availability/{userId}/{id}",
                user.getId(),
                availability.getId()
        ));
    }

    public void deleteById(Long id) {
        executeWithErrorHandling(() -> restTemplate.delete(
                baseUrl + "/user/{id}",
                id
        ));
    }

    public List<User> getAllUsers() {

        ResponseEntity<User[]> response = executeWithErrorHandling(() -> restTemplate.getForEntity(
                baseUrl + "/user",
                User[].class
        ));
        return Arrays.asList(response.getBody());
    }

    public Availability getAvailability(Long id) {
        return executeWithErrorHandling(() ->
                restTemplate.getForObject(
                        baseUrl + "/availability/{id}",
                        Availability.class,
                        id
                ));
    }

    public User getById(Long id) {
        return executeWithErrorHandling(() ->
                restTemplate.getForObject(
                        baseUrl + "/user/{id}",
                        User.class,
                        id
                ));
    }

    public Location getLocation(Long id) {
        return executeWithErrorHandling(() ->
                restTemplate.getForObject(
                        baseUrl + "/location/{id}",
                        Location.class,
                        id
                ));
    }

    public OffDay getOffDay(Long id) {
        return executeWithErrorHandling(() ->
                restTemplate.getForObject(
                        baseUrl + "/offday/{id}",
                        OffDay.class,
                        id
                ));
    }

    public User save(User user) {
        return executeWithErrorHandling(() ->
                restTemplate.postForObject(
                        baseUrl + "/user",
                        user,
                        User.class
                ));
    }

    public Location save(Location location, Long userId) {
        return executeWithErrorHandling(() ->
                restTemplate.postForObject(
                        baseUrl + "/location/{userId}",
                        location,
                        Location.class,
                        userId
                ));
    }

    public Availability save(Availability availability, Long userId) {
        return executeWithErrorHandling(() ->
                restTemplate.postForObject(
                        baseUrl + "/availability/{userId}",
                        availability,
                        Availability.class,
                        userId
                ));
    }

    public OffDay save(OffDay offDay, Long userId) {
        return executeWithErrorHandling(() ->
                restTemplate.postForObject(
                        baseUrl + "/offday/{userId}",
                        offDay,
                        OffDay.class,
                        userId
                ));
    }

    public void update(Location location, Long userId) {
        executeWithErrorHandling(() -> restTemplate.put(
                baseUrl + "/location/{userId}",
                location,
                userId
        ));
    }

    public void update(OffDay offDay, Long userId) {
        executeWithErrorHandling(() -> restTemplate.put(
                baseUrl + "/offday/{userId}",
                offDay,
                userId
        ));
    }

    public void update(Availability availability, Long userId) {
        executeWithErrorHandling(() -> restTemplate.put(
                baseUrl + "/availability/{userId}",
                availability,
                userId
        ));
    }

    public void update(User user) {
        executeWithErrorHandling(() -> restTemplate.put(
                baseUrl + "/user",
                user
        ));
    }

}