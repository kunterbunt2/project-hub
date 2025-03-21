package de.bushnaq.abdalla.projecthub.dao;

import jakarta.persistence.Column;
import jakarta.persistence.MappedSuperclass;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.time.LocalDate;

@MappedSuperclass
@Getter
@Setter
@ToString
public abstract class AbstractDateRangeDTO extends AbstractTimeAwareDTO {

    @Column(nullable = false)
    private LocalDate firstDay;

    @Column(nullable = true)
    private LocalDate lastDay;

}
