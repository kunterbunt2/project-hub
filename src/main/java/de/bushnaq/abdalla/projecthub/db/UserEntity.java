package de.bushnaq.abdalla.projecthub.db;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.Proxy;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
@ToString(callSuper = true)
@EqualsAndHashCode(of = {"id"}, callSuper = false)
@Proxy(lazy = false)
public class UserEntity extends AbstractTimeAwareEntity {

    //    @OneToMany
//    private List<AvailabilityEntity> availability = new ArrayList<>();
    @Column(nullable = false)
    private String email;

    @Column(nullable = false)
    private LocalDate firstWorkingDay;//first working day

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(nullable = true)
    private LocalDate lastWorkingDay;//last working day

    @OneToMany(cascade = {CascadeType.ALL}, fetch = FetchType.EAGER)
    @JoinColumn(name = "user_id", referencedColumnName = "id")
    private List<LocationEntity> locations = new ArrayList<>();

    @Column(nullable = false)
    private String name;
//    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
//    private List<NonworkingEntity>   nonworking   = new ArrayList<>();

    @PrePersist
    protected void onCreate() {
        super.onCreate();
        firstWorkingDay = LocalDate.now();
    }

}
