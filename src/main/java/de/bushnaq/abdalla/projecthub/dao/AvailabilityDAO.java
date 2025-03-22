package de.bushnaq.abdalla.projecthub.dao;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.Proxy;

import java.time.LocalDate;

/**
 * Represents the availability of a user at a certain time.
 */
@Entity
@Table(name = "availabilities")
@Getter
@Setter
@NoArgsConstructor
@ToString(callSuper = true)
@EqualsAndHashCode(of = {"id"}, callSuper = false)
@Proxy(lazy = false)
@JsonIdentityInfo(
        scope = AvailabilityDAO.class,
        generator = ObjectIdGenerators.PropertyGenerator.class,
        property = "id")
public class AvailabilityDAO extends AbstractTimeAwareDAO {

    @Column(nullable = false)
    private float     availability;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long      id;
    @Column(nullable = false)
    private LocalDate start;

    @ManyToOne(fetch = FetchType.LAZY)
    @ToString.Exclude//help intellij debugger not to go into a loop
    private UserDAO user;

}
