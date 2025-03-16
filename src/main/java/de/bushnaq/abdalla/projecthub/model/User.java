package de.bushnaq.abdalla.projecthub.model;

import lombok.*;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@ToString(callSuper = true)
@EqualsAndHashCode(of = {"id"}, callSuper = false)
public class User extends AbstractTimeAware {

    private List<Availability> availabilities = new ArrayList<>();
    private String             email;
    private LocalDate          firstWorkingDay;//first working day
    private Long               id;
    private LocalDate          lastWorkingDay;//last working day
    private List<Location>     locations      = new ArrayList<>();
    private String             name;

    public Availability addAvailability(float v, LocalDate start) {

        //close existing last period
        if (!availabilities.isEmpty()) {
            //TODO ensure availabilities are sorted to start date
            Availability last = availabilities.getLast();
            if (last.getLastDay() == null)
                last.setLastDay(start.minusDays(1));//if we are still employed, set the finish date of the last location to the day before the new location starts
        }
        Availability a = new Availability(v, start);
        availabilities.add(a);
        return a;

    }

    public Location addLocation(String country, String state, LocalDate start, LocalDate finish) {
        //TODO we should not allow overlap of periods
        //TODO set finish of last location to start of new location
        if (start == null)
            throw new IllegalArgumentException("start date is null");
        if (country == null)
            throw new IllegalArgumentException("start date is null");
        if (state == null)
            throw new IllegalArgumentException("start date is null");
        if (!locations.isEmpty()) {
            //TODO ensure locations are sorted to start date
            Location last = locations.getLast();
            if (!last.getFirstDay().isBefore(start)) {
                throw new IllegalArgumentException("overlapping periods");
            }
            if (last.getLastDay() == null)
                last.setLastDay(start.minusDays(1));//if we are still employed, set the finish date of the last location to the day before the new location starts
        }
        Location l = new Location(country, state, start, finish);
        locations.add(l);
        return l;
    }
//    private List<Nonworking>    nonworking   = new ArrayList<>();

    public void setLastWorkingDay(LocalDate lastWorkingDay) {
        this.lastWorkingDay = lastWorkingDay;
        locations.getLast().setLastDay(lastWorkingDay);
    }
}
