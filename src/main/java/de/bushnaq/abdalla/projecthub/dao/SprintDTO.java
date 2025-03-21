package de.bushnaq.abdalla.projecthub.dao;

import de.bushnaq.abdalla.projecthub.dto.Status;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.Proxy;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "sprints")
@Getter
@Setter
@NoArgsConstructor
@ToString(callSuper = true)
@EqualsAndHashCode(of = {"id"}, callSuper = false)
@Proxy(lazy = false)
public class SprintDTO extends AbstractTimeAwareDTO {
    @Column(name = "end_date", nullable = true)  // renamed from 'end' as it is reserved in H2 databases
    private OffsetDateTime end;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(name = "start_date", nullable = true)  // renamed from 'start'
    private OffsetDateTime start;

    @Column(nullable = false)
    private Status status;

    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    @JoinColumn(name = "sprint_id", referencedColumnName = "id")
    private List<TaskDAO> tasks = new ArrayList<>();
}
