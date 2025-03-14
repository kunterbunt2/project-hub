package de.bushnaq.abdalla.projecthub.client;

import lombok.*;

import java.time.OffsetDateTime;

@Getter
@Setter
@NoArgsConstructor
@ToString(callSuper = true)
@EqualsAndHashCode(of = {"id"}, callSuper = false)
public class Availability extends TimeAware {
    private float          availability;
    private OffsetDateTime finish;
    private Long           id;
    private OffsetDateTime start;
}
