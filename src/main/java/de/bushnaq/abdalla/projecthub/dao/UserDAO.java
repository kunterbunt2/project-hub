package de.bushnaq.abdalla.projecthub.dao;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonManagedReference;
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
//@JsonIdentityInfo(
//        scope = UserDAO.class,
//        generator = ObjectIdGenerators.PropertyGenerator.class,
//        property = "id")
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class UserDAO extends AbstractTimeAwareDAO {

    //    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    //    @OneToMany(cascade = {CascadeType.ALL}, fetch = FetchType.EAGER)
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
//    @JoinColumn(name = "user_id", referencedColumnName = "id")
    @JsonManagedReference
    private List<AvailabilityDAO> availabilities = new ArrayList<>();

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

    //    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    //    @OneToMany(cascade = {CascadeType.ALL}, fetch = FetchType.EAGER)
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
//    @JoinColumn(name = "user_id", referencedColumnName = "id")
    @JsonManagedReference
    private List<LocationDAO> locations = new ArrayList<>();

    @Column(nullable = false)
    private String name;

    //    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
//        @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
//    @JoinColumn(name = "user_id", referencedColumnName = "id")
    @JsonManagedReference
    private List<OffDayDAO> offDays = new ArrayList<>();

    @PrePersist
    protected void onCreate() {
        super.onCreate();
        firstWorkingDay = LocalDate.now();
    }

}
