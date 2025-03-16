package de.bushnaq.abdalla.projecthub.client;

import lombok.*;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@ToString(callSuper = true)
@EqualsAndHashCode(of = {"id"}, callSuper = false)
public class Availability extends AbstractDateRange {
    private float availability;
    //    private OffsetDateTime finish;
    private Long  id;
//    private OffsetDateTime start;

    public Availability(float availability, LocalDate firstDate) {
        super();
        this.availability = availability;
        setFirstDay(firstDate);
    }
}
