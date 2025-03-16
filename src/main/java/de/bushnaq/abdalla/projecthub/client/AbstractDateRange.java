package de.bushnaq.abdalla.projecthub.client;

import jakarta.persistence.Column;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.time.LocalDate;

@Getter
@Setter
@ToString
public abstract class AbstractDateRange extends AbstractTimeAware {

    @Column(nullable = false)
    private LocalDate firstDay;

    @Column(nullable = true)
    private LocalDate lastDay;

}
