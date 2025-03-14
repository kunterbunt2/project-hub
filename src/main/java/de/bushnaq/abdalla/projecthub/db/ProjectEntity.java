package de.bushnaq.abdalla.projecthub.db;

import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "project")
@Getter
@Setter
@NoArgsConstructor
@ToString(callSuper = true)
@EqualsAndHashCode(of = {"id"}, callSuper = false)
public class ProjectEntity extends AbstractTimeAwareEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long                id;
    private String              name;
    private String              requester;
    @OneToMany
    private List<VersionEntity> versions = new ArrayList<>();

}
