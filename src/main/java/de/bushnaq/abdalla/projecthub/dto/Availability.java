package de.bushnaq.abdalla.projecthub.dto;

import lombok.*;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@ToString(callSuper = true)
@EqualsAndHashCode(of = {"id"}, callSuper = false)
public class Availability extends AbstractTimeAware {
    private float     availability;
    //    private OffsetDateTime finish;
    private Long      id;
    private LocalDate start;

    public Availability(float availability, LocalDate firstDate) {
        super();
        this.availability = availability;
        setStart(firstDate);
    }
}
