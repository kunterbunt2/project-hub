package de.bushnaq.abdalla.projecthub.db;

import de.bushnaq.abdalla.projecthub.client.Status;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.Proxy;

import java.time.OffsetDateTime;

@Entity
@Table(name = "sprint")
@Getter
@Setter
@NoArgsConstructor
@ToString(callSuper = true)
@EqualsAndHashCode(of = {"id"}, callSuper = false)
@Proxy(lazy = false)
public class SprintEntity extends AbstractTimeAwareEntity {
    @Column(name = "end_date")  // renamed from 'end' as it is reserved in H2 databases
    private OffsetDateTime end;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long           id;
    private String         name;
    @Column(name = "start_date")  // renamed from 'start'
    private OffsetDateTime start;

    private Status status;
}
