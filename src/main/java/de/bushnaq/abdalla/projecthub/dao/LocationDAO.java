package de.bushnaq.abdalla.projecthub.dao;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.Proxy;

import java.time.LocalDate;

@Entity
@Table(name = "locations")
@Getter
@Setter
@NoArgsConstructor
@ToString(callSuper = true)
@EqualsAndHashCode(of = {"id"}, callSuper = false)
@Proxy(lazy = false)
@JsonIdentityInfo(
        scope = LocationDAO.class,
        generator = ObjectIdGenerators.PropertyGenerator.class,
        property = "id")
public class LocationDAO extends AbstractTimeAwareDAO {
    @Column(nullable = false)
    private String    country;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long      id;
    @Column(nullable = false)
    private LocalDate start;
    @Column(nullable = false)
    private String    state;
    @ManyToOne(fetch = FetchType.LAZY)
    @ToString.Exclude//help intellij debugger not to go into a loop
    private UserDAO   user;
    @Column(name = "USER_ID", insertable = false, updatable = false)
    private Long      userId;
}
