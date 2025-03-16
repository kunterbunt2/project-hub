package de.bushnaq.abdalla.projecthub.dto;

import jakarta.persistence.Column;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.time.LocalDate;

@Getter
@Setter
@ToString
@EqualsAndHashCode(of = {"firstDay", "lastDay"}, callSuper = false)
public abstract class AbstractDateRange extends AbstractTimeAware {

    @Column(nullable = false)
    private LocalDate firstDay;

    @Column(nullable = false)
    private LocalDate lastDay;

}
