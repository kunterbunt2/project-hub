package de.bushnaq.abdalla.projecthub.dao;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.Proxy;

import java.time.LocalDate;

@Entity
@Table(name = "availabilities")
@Getter
@Setter
@NoArgsConstructor
@ToString(callSuper = true)
@EqualsAndHashCode(of = {"id"}, callSuper = false)
@Proxy(lazy = false)
/**
 * Represents the availability of a user at a certain time.
 */
public class AvailabilityEntity extends AbstractTimeAwareEntity {

    @Column(nullable = false)
    private float availability;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;
    @Column(nullable = false)
    private LocalDate start;

}
