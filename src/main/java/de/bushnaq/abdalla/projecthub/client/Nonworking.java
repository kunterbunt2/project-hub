package de.bushnaq.abdalla.projecthub.client;

import de.bushnaq.abdalla.projecthub.db.NonworkingType;
import lombok.*;

import java.time.OffsetDateTime;

@Getter
@Setter
@NoArgsConstructor
@ToString(callSuper = true)
@EqualsAndHashCode(of = {"id"}, callSuper = false)
public class Nonworking extends AbstractTimeAware {
    private OffsetDateTime finish;
    private Long           id;
    private OffsetDateTime start;
    private NonworkingType type;
}
