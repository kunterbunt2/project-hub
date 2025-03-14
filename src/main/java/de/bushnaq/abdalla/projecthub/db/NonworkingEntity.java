package de.bushnaq.abdalla.projecthub.db;

import jakarta.persistence.*;
import lombok.*;

import java.time.OffsetDateTime;

@Entity
@Table(name = "nonworking")
@Getter
@Setter
@NoArgsConstructor
@ToString(callSuper = true)
@EqualsAndHashCode(of = {"id"}, callSuper = false)
public class NonworkingEntity extends AbstractTimeAwareEntity {
    private OffsetDateTime finish;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long           id;
    private OffsetDateTime start;
    private NonworkingType type;
}
