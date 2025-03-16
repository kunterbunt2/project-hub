package de.bushnaq.abdalla.projecthub.db;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.Proxy;

@Entity
@Table(name = "locations")
@Getter
@Setter
@NoArgsConstructor
@ToString(callSuper = true)
@EqualsAndHashCode(of = {"id"}, callSuper = false)
@Proxy(lazy = false)
public class LocationEntity extends AbstractDateRangeEntity {
    @Column(nullable = false)
    private String country;

//    @Column(nullable = true)
//    private LocalDate finish;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

//    @Column(nullable = false)
//    private LocalDate start;

    @Column(nullable = false)
    private String state;
}
