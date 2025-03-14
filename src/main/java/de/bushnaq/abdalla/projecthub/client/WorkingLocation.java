package de.bushnaq.abdalla.projecthub.client;

import lombok.*;

import java.time.OffsetDateTime;

@Getter
@Setter
@NoArgsConstructor
@ToString(callSuper = true)
@EqualsAndHashCode(of = {"id"}, callSuper = false)
public class WorkingLocation extends TimeAware implements Comparable<WorkingLocation> {
    private String         country;
    private OffsetDateTime finish;
    private Long           id;
    private OffsetDateTime start;
    private String         state;

    public WorkingLocation(String country, String state, OffsetDateTime start, OffsetDateTime finish) {
        this.country = country;
        this.state   = state;
        this.start   = start;
        this.finish  = finish;
    }

    @Override
    public int compareTo(WorkingLocation other) {
        int result = this.start.compareTo(other.start);
        if (result == 0) {
            result = this.country.compareTo(other.country);
        }
        if (result == 0) {
            result = this.state.compareTo(other.state);
        }
        if (result == 0) {
            if (this.finish == null && other.finish != null) return -1;
            if (this.finish != null && other.finish == null) return 1;
            if (this.finish != null && other.finish != null)
                result = this.finish.compareTo(other.finish);
        }
        return result;
    }
}
