package de.bushnaq.abdalla.projecthub.dto;

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
    private List<OffDay>       offDays        = new ArrayList<>();

    public Availability addAvailability(float v, LocalDate start) {

        Availability a = new Availability(v, start);
        availabilities.add(a);
        return a;

    }

    public Location addLocation(String country, String state, LocalDate start) {
        //TODO we should not allow overlap of periods
        //TODO set finish of last location to start of new location
        if (start == null)
            throw new IllegalArgumentException("start date is null");
        if (country == null)
            throw new IllegalArgumentException("start date is null");
        if (state == null)
            throw new IllegalArgumentException("start date is null");
//        if (!locations.isEmpty()) {
//            //TODO ensure locations are sorted to start date
//            Location last = locations.getLast();
//            if (!last.getFirstDay().isBefore(start)) {
//                throw new IllegalArgumentException("overlapping periods");
//            }
//            if (last.getLastDay() == null)
//                last.setLastDay(start.minusDays(1));//if we are still employed, set the finish date of the last location to the day before the new location starts
//        }
        Location l = new Location(country, state, start);
        locations.add(l);
        return l;
    }

    public OffDay addOffday(LocalDate firstDay, LocalDate lastDay, OffDayType offDayType) {
        OffDay a = new OffDay(firstDay, lastDay, offDayType);
        offDays.add(a);
        return a;
    }
}
