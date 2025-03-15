package de.bushnaq.abdalla.projecthub.client;

import lombok.*;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@ToString(callSuper = true)
@EqualsAndHashCode(of = {"id"}, callSuper = false)
public class User extends TimeAware {

    //    private List<Availability>  availability = new ArrayList<>();
    private String         email;
    private LocalDate      firstWorkingDay;//first working day
    private Long           id;
    private LocalDate      lastWorkingDay;//last working day
    private List<Location> locations = new ArrayList<>();
    private String         name;

    public Location addWorkingLocation(String country, String state, LocalDate start, LocalDate finish) {
        //TODO we should not allow overlap of periods
        //TODO set finish of last location to start of new location
        if (start == null)
            throw new IllegalArgumentException("start date is null");
        if (country == null)
            throw new IllegalArgumentException("start date is null");
        if (state == null)
            throw new IllegalArgumentException("start date is null");
        if (!locations.isEmpty()) {
            Location last = locations.getLast();
            if (!last.getStart().isBefore(start)) {
                throw new IllegalArgumentException("overlapping periods");
            }
            if (last.getFinish() == null)
                last.setFinish(start.minusDays(1));//if we are still employed, set the finish date of the last location to the day before the new location starts
        }
        Location l = new Location(country, state, start, finish);
        locations.add(l);
        return l;
    }
//    private List<Nonworking>    nonworking   = new ArrayList<>();

    public void setLastWorkingDay(LocalDate lastWorkingDay) {
        this.lastWorkingDay = lastWorkingDay;
        locations.getLast().setFinish(lastWorkingDay);
    }
}
