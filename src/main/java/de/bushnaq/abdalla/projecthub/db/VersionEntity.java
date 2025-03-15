package de.bushnaq.abdalla.projecthub.db;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.Proxy;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "version")
@Getter
@Setter
@NoArgsConstructor
@ToString(callSuper = true)
@EqualsAndHashCode(of = {"id"}, callSuper = false)
@Proxy(lazy = false)
public class VersionEntity extends AbstractTimeAwareEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long               id;
    private String             name;
    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    @JoinColumn(name = "version_id", referencedColumnName = "id")
    private List<SprintEntity> sprints = new ArrayList<>();

}
