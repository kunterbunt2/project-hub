package de.bushnaq.abdalla.projecthub.dao;

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
public class UserDTO extends AbstractTimeAwareDTO {

    @OneToMany(cascade = {CascadeType.ALL}, fetch = FetchType.EAGER)
    @JoinColumn(name = "user_id", referencedColumnName = "id")
    private List<AvailabilityDTO> availabilities = new ArrayList<>();

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
    private List<LocationDTO> locations = new ArrayList<>();

    @Column(nullable = false)
    private String name;

    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    @JoinColumn(name = "user_id", referencedColumnName = "id")
    private List<OffDayDTO> offDays = new ArrayList<>();

    @PrePersist
    protected void onCreate() {
        super.onCreate();
        firstWorkingDay = LocalDate.now();
    }

}
