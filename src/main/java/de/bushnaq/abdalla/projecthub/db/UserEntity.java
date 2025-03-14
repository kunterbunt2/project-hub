package de.bushnaq.abdalla.projecthub.db;

import jakarta.persistence.*;
import lombok.*;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "resource")
@Getter
@Setter
@NoArgsConstructor
@ToString(callSuper = true)
@EqualsAndHashCode(of = {"id"}, callSuper = false)
public class UserEntity extends AbstractTimeAwareEntity {

    @OneToMany
    private List<AvailabilityEntity>    availability     = new ArrayList<>();
    private String                      email;
    private OffsetDateTime              firstWorkingDay;//first working day
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long                        id;
    private OffsetDateTime              lastWorkingDay;//last working day
    private String                      name;
    @OneToMany
    private List<NonworkingEntity>      nonworking       = new ArrayList<>();
    @OneToMany
    private List<WorkingLocationEntity> workingLocations = new ArrayList<>();

    @PrePersist
    protected void onCreate() {
        super.onCreate();
        firstWorkingDay = OffsetDateTime.now();
    }

}
