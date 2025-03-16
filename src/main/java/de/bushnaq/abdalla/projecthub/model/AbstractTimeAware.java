package de.bushnaq.abdalla.projecthub.model;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.time.OffsetDateTime;

@Getter
@Setter
@ToString
public abstract class AbstractTimeAware {

    private OffsetDateTime created;

    private OffsetDateTime updated;

}
