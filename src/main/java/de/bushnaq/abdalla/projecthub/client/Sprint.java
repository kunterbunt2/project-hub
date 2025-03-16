package de.bushnaq.abdalla.projecthub.client;


import lombok.*;

import java.time.OffsetDateTime;

@Getter
@Setter
@NoArgsConstructor
@ToString(callSuper = true)
@EqualsAndHashCode(of = {"id"}, callSuper = false)
public class Sprint extends AbstractTimeAware {
    private OffsetDateTime end;
    private Long           id;
    private String         name;
    private OffsetDateTime start;
    private Status         status;
}
