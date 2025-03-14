package de.bushnaq.abdalla.projecthub.db;

import jakarta.persistence.*;
import lombok.*;

import java.time.OffsetDateTime;

@Entity
@Table(name = "working_location")
@Getter
@Setter
@NoArgsConstructor
@ToString(callSuper = true)
@EqualsAndHashCode(of = {"country", "state", "start", "finish"}, callSuper = false)
public class WorkingLocationEntity extends AbstractTimeAwareEntity {
    private String         country;
    private OffsetDateTime finish;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long           id;
    private OffsetDateTime start;
    private String         state;
}
