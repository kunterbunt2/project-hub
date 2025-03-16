package de.bushnaq.abdalla.projecthub.dto;

import lombok.*;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@ToString(callSuper = true)
@EqualsAndHashCode(of = {"type"}, callSuper = true)
public class OffDay extends AbstractDateRange {

    private Long       id;
    private OffDayType type;

    public OffDay(LocalDate firstDay, LocalDate lastDay, OffDayType offDayType) {
        super();
        this.setFirstDay(firstDay);
        this.setLastDay(lastDay);
        this.type = offDayType;
    }

}
