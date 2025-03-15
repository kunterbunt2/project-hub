package de.bushnaq.abdalla.projecthub.db;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.Proxy;

import java.time.LocalDate;

@Entity
@Table(name = "location")
@Getter
@Setter
@NoArgsConstructor
@ToString(callSuper = true)
@EqualsAndHashCode(of = {"id"}, callSuper = false)
@Proxy(lazy = false)
public class LocationEntity extends AbstractTimeAwareEntity {
    private String    country;
    private LocalDate finish;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long      id;
    //    @Column(name = "start", nullable = false)
    private LocalDate start;
    private String    state;
}
