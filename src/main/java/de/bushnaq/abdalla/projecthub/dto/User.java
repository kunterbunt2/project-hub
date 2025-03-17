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
    private LocalDate          firstWorkingDay;
    private Long               id;
    private LocalDate          lastWorkingDay;
    private List<Location>     locations      = new ArrayList<>();
    private String             name;
    private List<OffDay>       offDays        = new ArrayList<>();

    public Availability addAvailability(float v, LocalDate start) {

        Availability a = new Availability(v, start);
        availabilities.add(a);
        return a;

    }

    public Location addLocation(String country, String state, LocalDate start) {
        if (start == null)
            throw new IllegalArgumentException("start date is null");
        if (country == null)
            throw new IllegalArgumentException("start date is null");
        if (state == null)
            throw new IllegalArgumentException("start date is null");
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
