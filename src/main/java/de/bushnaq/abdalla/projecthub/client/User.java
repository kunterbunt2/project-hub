package de.bushnaq.abdalla.projecthub.client;

import lombok.*;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;

@Getter
@Setter
@NoArgsConstructor
@ToString(callSuper = true)
@EqualsAndHashCode(of = {"id"}, callSuper = false)
public class User extends TimeAware {

    private List<Availability>         availability     = new ArrayList<>();
    private String                     email;
    private OffsetDateTime             firstWorkingDay;//first working day
    private Long                       id;
    private OffsetDateTime             lastWorkingDay;//last working day
    private String                     name;
    private List<Nonworking>           nonworking       = new ArrayList<>();
    private SortedSet<WorkingLocation> workingLocations = new TreeSet<>();

    public void addWorkingLocation(String country, String state, OffsetDateTime start, OffsetDateTime finish) {
        //TODO we should not allow overlap of periods
        //TODO set finish of last location to start of new location
        if (start == null)
            throw new IllegalArgumentException("start date is null");
        if (country == null)
            throw new IllegalArgumentException("start date is null");
        if (state == null)
            throw new IllegalArgumentException("start date is null");
        if (!workingLocations.isEmpty()) {
            WorkingLocation last = workingLocations.getLast();
            if (!last.getStart().isBefore(start)) {
                throw new IllegalArgumentException("overlapping periods");
            }
            last.setFinish(start.minusDays(1));//yesterday
        }
        workingLocations.add(new WorkingLocation(country, state, start, finish));
    }
}
