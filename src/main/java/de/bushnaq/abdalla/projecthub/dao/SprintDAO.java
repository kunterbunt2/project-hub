package de.bushnaq.abdalla.projecthub.dao;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonManagedReference;
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
//@JsonIdentityInfo(
//        scope = SprintDAO.class,
//        generator = ObjectIdGenerators.PropertyGenerator.class,
//        property = "id")
public class SprintDAO extends AbstractTimeAwareDAO {
    @Column(name = "end_date", nullable = true)  // renamed from 'end' as it is reserved in H2 databases
    private OffsetDateTime end;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(nullable = false)
    private String name;

    @ManyToOne(fetch = FetchType.LAZY)
    @ToString.Exclude//help intellij debugger not to go into a loop
    @JsonBackReference(value = "project-sprint")
    private ProjectDAO project;

    @Column(name = "start_date", nullable = true)  // renamed from 'start'
    private OffsetDateTime start;

    @Column(nullable = false)
    private Status status;

    @OneToMany(mappedBy = "sprint", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
//    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
//    @JoinColumn(name = "sprint_id", referencedColumnName = "id")
    @JsonManagedReference(value = "sprint-task")
    private List<TaskDAO> tasks = new ArrayList<>();
}
