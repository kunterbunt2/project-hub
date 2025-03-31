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

package de.bushnaq.abdalla.projecthub.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import lombok.*;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@ToString(callSuper = true)
@EqualsAndHashCode(of = {"id"}, callSuper = false)
//@JsonIdentityInfo(
//        scope = User.class,
//        generator = ObjectIdGenerators.PropertyGenerator.class,
//        property = "id")
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class User extends AbstractTimeAware implements Comparable<User> {

    @JsonManagedReference
    private List<Availability> availabilities = new ArrayList<>();
    private String             email;
    private LocalDate          firstWorkingDay;
    private Long               id;
    private LocalDate          lastWorkingDay;
    @JsonManagedReference
    private List<Location>     locations      = new ArrayList<>();
    private String             name;
    @JsonManagedReference
    private List<OffDay>       offDays        = new ArrayList<>();

    public void addAvailability(Availability availability) {
        availabilities.add(availability);
    }

    public void addLocation(Location location) {
        if (location.getStart() == null)
            throw new IllegalArgumentException("start date is null");
        if (location.getCountry() == null)
            throw new IllegalArgumentException("start date is null");
        if (location.getState() == null)
            throw new IllegalArgumentException("start date is null");
//        Location l = new Location(country, state, start);
        locations.add(location);
    }

    public void addOffday(OffDay offDay) {
        offDays.add(offDay);
    }

    @Override
    public int compareTo(User other) {
        return this.id.compareTo(other.id);
    }

    String getKey() {
        return "U-" + id;
    }

    public void removeAvailability(Availability availability) {
        availabilities.remove(availability);
    }

    public void removeLocation(Location location) {
        locations.remove(location);
    }

    public void removeOffDay(OffDay offDay) {
        offDays.remove(offDay);
    }
}
