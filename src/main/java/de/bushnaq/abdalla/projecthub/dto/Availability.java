package de.bushnaq.abdalla.projecthub.dto;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import lombok.*;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@ToString(callSuper = true)
@EqualsAndHashCode(of = {"id"}, callSuper = false)
@JsonIdentityInfo(
        scope = Availability.class,
        generator = ObjectIdGenerators.PropertyGenerator.class,
        property = "id")
public class Availability extends AbstractTimeAware {
    private float     availability;
    //    private OffsetDateTime finish;
    private Long      id;
    private LocalDate start;

    @ToString.Exclude//help intellij debugger not to go into a loop
    private User user;

    public Availability(float availability, LocalDate firstDate) {
        super();
        this.availability = availability;
        setStart(firstDate);
    }

    String getKey() {
        return "A-" + id;
    }

}
