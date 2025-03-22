package de.bushnaq.abdalla.projecthub.dto;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import lombok.*;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@ToString(callSuper = true)
@EqualsAndHashCode(of = {"type"}, callSuper = true)
@JsonIdentityInfo(
        scope = OffDay.class,
        generator = ObjectIdGenerators.PropertyGenerator.class,
        property = "id")
public class OffDay extends AbstractDateRange {

    private Long       id;
    private OffDayType type;

    @ToString.Exclude//help intellij debugger not to go into a loop
    private User user;

    public OffDay(LocalDate firstDay, LocalDate lastDay, OffDayType offDayType) {
        super();
        this.setFirstDay(firstDay);
        this.setLastDay(lastDay);
        this.type = offDayType;
    }

    String getKey() {
        return "D-" + id;
    }

}
