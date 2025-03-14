package de.bushnaq.abdalla.projecthub.client;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.time.OffsetDateTime;

@Getter
@Setter
@ToString
public abstract class TimeAware {
    private OffsetDateTime created;
    private OffsetDateTime updated;
}
