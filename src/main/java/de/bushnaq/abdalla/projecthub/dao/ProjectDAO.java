package de.bushnaq.abdalla.projecthub.dao;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.Proxy;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "projects")
@Getter
@Setter
@NoArgsConstructor
@ToString(callSuper = true)
@EqualsAndHashCode(of = {"id"}, callSuper = false)
@Proxy(lazy = false)
@JsonIdentityInfo(
        scope = ProjectDAO.class,
        generator = ObjectIdGenerators.PropertyGenerator.class,
        property = "id")
public class ProjectDAO extends AbstractTimeAwareDAO {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String requester;

    @OneToMany(mappedBy = "project", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
//    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
//    @JoinColumn(name = "project_id", referencedColumnName = "id")
    private List<SprintDAO> sprints = new ArrayList<>();

    @ManyToOne(fetch = FetchType.LAZY)
//    @JsonBackReference
    @ToString.Exclude//help intellij debugger not to go into a loop
    private VersionDAO version;
}
