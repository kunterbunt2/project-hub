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

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import de.bushnaq.abdalla.projecthub.ParameterOptions;
import de.bushnaq.abdalla.projecthub.report.gantt.GanttContext;
import de.focus_shift.jollyday.core.Holiday;
import de.focus_shift.jollyday.core.HolidayManager;
import de.focus_shift.jollyday.core.ManagerParameters;
import de.focus_shift.jollyday.core.parameter.UrlManagerParameter;
import lombok.*;
import net.sf.mpxj.ProjectCalendar;
import net.sf.mpxj.ProjectCalendarException;

import java.net.URL;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import java.util.stream.Collectors;

@Getter
@Setter
@NoArgsConstructor
@ToString(callSuper = true)
@EqualsAndHashCode(of = {"id"}, callSuper = false)
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class User extends AbstractTimeAware implements Comparable<User> {

    private static final long               MAX_PROJECT_LENGTH = 1;
    @JsonManagedReference
    private              List<Availability> availabilities     = new ArrayList<>();

    @JsonIgnore
    private ProjectCalendar calendar;
    private String          email;
    private LocalDate       firstWorkingDay;
    private Long            id;
    private LocalDate       lastWorkingDay;
    @JsonManagedReference
    private List<Location>  locations = new ArrayList<>();
    private String          name;
    @JsonManagedReference
    private List<OffDay>    offDays   = new ArrayList<>();

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

    public void initialize(GanttContext gc) {
        ProjectCalendar resourceCalendar = getCalendar();
        if (resourceCalendar == null) {
            resourceCalendar = gc.getProjectFile().addDefaultDerivedCalendar();
            resourceCalendar.setParent(gc.getCalendar());
            resourceCalendar.setName(getName());
            setCalendar(resourceCalendar);
        }

        //TODO rethink employee leaving company and coming back
        ProjectCalendar pc = getCalendar();
        LocalDate       endDateInclusive;
        for (int i = 0; i < locations.size(); i++) {
            Location  location           = locations.get(i);
            LocalDate startDateInclusive = location.getStart();
            if (i + 1 < locations.size())
                endDateInclusive = locations.get(i + 1).getStart();//end of this location is start of next location
            else
                endDateInclusive = ParameterOptions.now.plusYears(MAX_PROJECT_LENGTH).toLocalDate();
            HolidayManager holidayManager = HolidayManager.getInstance(ManagerParameters.create(location.getCountry()));
            List<Holiday>  holidays       = holidayManager.getHolidays(startDateInclusive, endDateInclusive, location.getState()).stream().sorted().collect(Collectors.toList());
            URL            url            = getClass().getClassLoader().getResource("holidays/carnival-holidays.xml");
            if (url != null && "nw".equals(location.getState())) {
                UrlManagerParameter urlManParam        = new UrlManagerParameter(url, new Properties());
                HolidayManager      customManager      = HolidayManager.getInstance(urlManParam);
                Set<Holiday>        additionalHolidays = customManager.getHolidays(startDateInclusive, endDateInclusive, location.getState());
                holidays.addAll(additionalHolidays.stream().toList());
            }
            for (Holiday holiday : holidays) {
                ProjectCalendarException pce = pc.addCalendarException(holiday.getDate());
                pce.setName(holiday.getDescription());
            }
        }
        for (OffDay offDay : getOffDays()) {
            ProjectCalendarException pce = pc.addCalendarException(offDay.getFirstDay(), offDay.getLastDay());
            pce.setName(offDay.getType().name());
        }

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
